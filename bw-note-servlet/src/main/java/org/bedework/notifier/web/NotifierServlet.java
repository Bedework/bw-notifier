/* ********************************************************************
    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.
*/
package org.bedework.notifier.web;

import org.bedework.notifier.NotifyEngine;
import org.bedework.notifier.exception.NoteException;
import org.bedework.notifier.service.NotifyConf;
import org.bedework.notifier.web.MethodBase.MethodInfo;
import org.bedework.util.http.service.HttpOut;
import org.bedework.util.jmx.ConfBase;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;
import org.bedework.util.servlet.io.CharArrayWrappedResponse;
import org.bedework.util.xml.XmlEmit;
import org.bedework.util.xml.tagdefs.WebdavTags;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.HashMap;

import javax.management.ObjectName;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.xml.namespace.QName;

/** WebDAV Servlet.
 * This abstract servlet handles the request/response nonsense and calls
 * abstract routines to interact with an underlying data source.
 *
 * @author Mike Douglass   douglm@bedework.edu
 * @version 1.0
 */
public class NotifierServlet extends HttpServlet
        implements Logged, HttpSessionListener, ServletContextListener {
  protected boolean dumpContent;

  /** Table of methods - set at init
   */
  protected HashMap<String, MethodInfo> methods = new HashMap<String, MethodInfo>();

  /* Try to serialize requests from a single session
   * This is very imperfect.
   */
  static class Waiter {
    boolean active;
    int waiting;
  }

  private static volatile HashMap<String, Waiter> waiters = new HashMap<String, Waiter>();

  @Override
  public void init(final ServletConfig config) throws ServletException {
    super.init(config);

    dumpContent = "true".equals(config.getInitParameter("dumpContent"));

    addMethods();
  }

  @Override
  protected void service(final HttpServletRequest req,
                         HttpServletResponse resp)
      throws ServletException, IOException {
    NotifyEngine notifier = null;
    boolean serverError = false;

    try {
      if (debug()) {
        debug("entry: " + req.getMethod());
        dumpRequest(req);
      }

      tryWait(req, true);

      notifier = NotifyEngine.getNotifier();

      if (req.getCharacterEncoding() == null) {
        req.setCharacterEncoding("UTF-8");
        if (debug()) {
          debug("No charset specified in request; forced to UTF-8");
        }
      }

      if (debug() && dumpContent) {
        resp = new CharArrayWrappedResponse(resp);
      }

      String methodName = req.getHeader("X-HTTP-Method-Override");

      if (methodName == null) {
        methodName = req.getMethod();
      }

      MethodBase method = getMethod(notifier, methodName);

      if (method == null) {
        info("No method for '" + methodName + "'");

        // ================================================================
        //     Set the correct response
        // ================================================================
      } else {
        method.init(notifier, dumpContent);
        method.doMethod(req, resp);
      }
//    } catch (WebdavForbidden wdf) {
  //    sendError(syncher, wdf, resp);
    } catch (Throwable t) {
      serverError = handleException(notifier, t, resp, serverError);
    } finally {
      if (notifier != null) {
        try {
//          syncher.close();
        } catch (Throwable t) {
          serverError = handleException(notifier, t, resp, serverError);
        }
      }

      try {
        tryWait(req, false);
      } catch (Throwable t) {}

      if (debug() && dumpContent &&
          (resp instanceof CharArrayWrappedResponse)) {
        /* instanceof check because we might get a subsequent exception before
         * we wrap the response
         */
        CharArrayWrappedResponse wresp = (CharArrayWrappedResponse)resp;

        if (wresp.getUsedOutputStream()) {
          debug("------------------------ response written to output stream -------------------");
        } else {
          String str = wresp.toString();

          debug("------------------------ Dump of response -------------------");
          debug(str);
          debug("---------------------- End dump of response -----------------");

          byte[] bs = str.getBytes();
          resp = (HttpServletResponse)wresp.getResponse();
          debug("contentLength=" + bs.length);
          resp.setContentLength(bs.length);
          resp.getOutputStream().write(bs);
        }
      }

      /* WebDAV is stateless - toss away the session */
      try {
        HttpSession sess = req.getSession(false);
        if (sess != null) {
          sess.invalidate();
        }
      } catch (Throwable t) {}
    }
  }

  /* Return true if it's a server error */
  private boolean handleException(final NotifyEngine syncher, final Throwable t,
                                  final HttpServletResponse resp,
                                  boolean serverError) {
    if (serverError) {
      return true;
    }

    try {
      if (t instanceof NoteException) {
        NoteException se = (NoteException)t;

        int status = se.getStatusCode();
        if (status == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
          error(se);
          serverError = true;
        }
        sendError(syncher, t, resp);
        return serverError;
      }

      error(t);
      sendError(syncher, t, resp);
      return true;
    } catch (Throwable t1) {
      // Pretty much screwed if we get here
      return true;
    }
  }

  private void sendError(final NotifyEngine syncher, final Throwable t,
                         final HttpServletResponse resp) {
    try {
      if (t instanceof NoteException) {
        NoteException se = (NoteException)t;
        QName errorTag = se.getErrorTag();

        if (errorTag != null) {
          if (debug()) {
            debug("setStatus(" + se.getStatusCode() + ")");
          }
          resp.setStatus(se.getStatusCode());
          resp.setContentType("text/xml; charset=UTF-8");
          if (!emitError(syncher, errorTag, se.getMessage(),
                         resp.getWriter())) {
            StringWriter sw = new StringWriter();
            emitError(syncher, errorTag, se.getMessage(), sw);

            try {
              if (debug()) {
                debug("setStatus(" + se.getStatusCode() + ")");
              }
              resp.sendError(se.getStatusCode(), sw.toString());
            } catch (Throwable t1) {
            }
          }
        } else {
          if (debug()) {
            debug("setStatus(" + se.getStatusCode() + ")");
          }
          resp.sendError(se.getStatusCode(), se.getMessage());
        }
      } else {
        if (debug()) {
          debug("setStatus(" + HttpServletResponse.SC_INTERNAL_SERVER_ERROR + ")");
        }
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                       t.getMessage());
      }
    } catch (Throwable t1) {
      // Pretty much screwed if we get here
    }
  }

  private boolean emitError(final NotifyEngine syncher,
                            final QName errorTag,
                            final String extra,
                            final Writer wtr) {
    try {
      XmlEmit xml = new XmlEmit();
//      syncher.addNamespace(xml);

      xml.startEmit(wtr);
      xml.openTag(WebdavTags.error);

  //    syncher.emitError(errorTag, extra, xml);

      xml.closeTag(WebdavTags.error);
      xml.flush();

      return true;
    } catch (Throwable t1) {
      // Pretty much screwed if we get here
      return false;
    }
  }

  /** Add methods for this namespace
   *
   */
  protected void addMethods() {
    methods.put("POST", new MethodInfo(PostMethod.class, true));
    /*
    methods.put("ACL", new MethodInfo(AclMethod.class, false));
    methods.put("COPY", new MethodInfo(CopyMethod.class, false));
    methods.put("GET", new MethodInfo(GetMethod.class, false));
    methods.put("HEAD", new MethodInfo(HeadMethod.class, false));
    methods.put("OPTIONS", new MethodInfo(OptionsMethod.class, false));
    methods.put("PROPFIND", new MethodInfo(PropFindMethod.class, false));

    methods.put("DELETE", new MethodInfo(DeleteMethod.class, true));
    methods.put("MKCOL", new MethodInfo(MkcolMethod.class, true));
    methods.put("MOVE", new MethodInfo(MoveMethod.class, true));
    methods.put("POST", new MethodInfo(PostMethod.class, true));
    methods.put("PROPPATCH", new MethodInfo(PropPatchMethod.class, true));
    methods.put("PUT", new MethodInfo(PutMethod.class, true));
    */

    //methods.put("LOCK", new MethodInfo(LockMethod.class, true));
    //methods.put("UNLOCK", new MethodInfo(UnlockMethod.class, true));
  }

  /**
   * @param syncher notify engine
   * @param name of method
   * @return method
   * @throws NoteException on error
   */
  public MethodBase getMethod(final NotifyEngine syncher,
                              final String name) throws NoteException {
    MethodInfo mi = methods.get(name.toUpperCase());

//    if ((mi == null) || (getAnonymous() && mi.getRequiresAuth())) {
  //    return null;
    //}

    try {
      MethodBase mb = mi.getMethodClass().newInstance();

      mb.init(syncher, dumpContent);

      return mb;
    } catch (Throwable t) {
      if (debug()) {
        error(t);
      }
      throw new NoteException(t);
    }
  }

  private void tryWait(final HttpServletRequest req, final boolean in) throws Throwable {
    Waiter wtr = null;
    synchronized (waiters) {
      //String key = req.getRequestedSessionId();
      String key = req.getRemoteUser();
      if (key == null) {
        return;
      }

      wtr = waiters.get(key);
      if (wtr == null) {
        if (!in) {
          return;
        }

        wtr = new Waiter();
        wtr.active = true;
        waiters.put(key, wtr);
        return;
      }
    }

    synchronized (wtr) {
      if (!in) {
        wtr.active = false;
        wtr.notify();
        return;
      }

      wtr.waiting++;
      while (wtr.active) {
        if (debug()) {
          debug("in: waiters=" + wtr.waiting);
        }

        wtr.wait();
      }
      wtr.waiting--;
      wtr.active = true;
    }
  }

  @Override
  public void sessionCreated(final HttpSessionEvent se) {
  }

  @Override
  public void sessionDestroyed(final HttpSessionEvent se) {
    HttpSession session = se.getSession();
    String sessid = session.getId();
    if (sessid == null) {
      return;
    }

    synchronized (waiters) {
      waiters.remove(sessid);
    }
  }

  /** Debug
   *
   * @param req the http request
   */
  public void dumpRequest(final HttpServletRequest req) {
    try {
      Enumeration names = req.getHeaderNames();

      String title = "Request headers";

      debug(title);

      while (names.hasMoreElements()) {
        String key = (String)names.nextElement();
        String val = req.getHeader(key);
        debug("  " + key + " = \"" + val + "\"");
      }

      names = req.getParameterNames();

      title = "Request parameters";

      debug(title + " - global info and uris");
      debug("getRemoteAddr = " + req.getRemoteAddr());
      debug("getRequestURI = " + req.getRequestURI());
      debug("getRemoteUser = " + req.getRemoteUser());
      debug("getRequestedSessionId = " + req.getRequestedSessionId());
      debug("HttpUtils.getRequestURL(req) = " + req.getRequestURL());
      debug("contextPath=" + req.getContextPath());
      debug("query=" + req.getQueryString());
      debug("contentlen=" + req.getContentLength());
      debug("request=" + req);
      debug("parameters:");

      debug(title);

      while (names.hasMoreElements()) {
        String key = (String)names.nextElement();
        String val = req.getParameter(key);
        debug("  " + key + " = \"" + val + "\"");
      }
    } catch (Throwable t) {
    }
  }

  /* -----------------------------------------------------------------------
   *                         JMX support
   */

  class Configurator extends ConfBase {
    NotifyConf notifyConf;

    Configurator() {
      super("org.bedework.notify:service=Notify");
    }

    @Override
    public String loadConfig() {
      return null;
    }

    @Override
    public void start() {
      try {
        ConfBase.getManagementContext().start();

        notifyConf = new NotifyConf();
        register("notifyConf", "notifyConf", notifyConf);
        notifyConf.loadConfig();
        notifyConf.start();

      /* ------------- Http properties -------------------- */
        HttpOut ho = new HttpOut("org.bedework.notify.confuri",
                                 "org.bedework.notify",
                                 "httpConfig");
        register(new ObjectName(ho.getServiceName()), ho);
        ho.loadConfig();
      } catch (Throwable t){
        t.printStackTrace();
      }
    }

    @Override
    public void stop() {
      try {
        notifyConf.stop();
        ConfBase.getManagementContext().stop();
      } catch (Throwable t){
        t.printStackTrace();
      }
    }
  }

  private Configurator conf = new Configurator();

  @Override
  public void contextInitialized(final ServletContextEvent sce) {
    conf.start();
  }

  @Override
  public void contextDestroyed(final ServletContextEvent sce) {
    conf.stop();
  }

  /* ====================================================================
   *                   Logged methods
   * ==================================================================== */

  private BwLogger logger = new BwLogger();

  @Override
  public BwLogger getLogger() {
    if ((logger.getLoggedClass() == null) && (logger.getLoggedName() == null)) {
      logger.setLoggedClass(getClass());
    }

    return logger;
  }
}
