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
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
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
  protected HashMap<String, MethodInfo> methods = new HashMap<>();

  /* Try to serialize requests from a single session
   * This is very imperfect.
   */
  static class Waiter {
    boolean active;
    int waiting;
  }

  private static final HashMap<String, Waiter> waiters = new HashMap<>();

  @Override
  public void init(final ServletConfig config) throws ServletException {
    super.init(config);

    dumpContent = "true".equals(config.getInitParameter("dumpContent"));

    addMethods();
  }

  @Override
  protected void service(final HttpServletRequest req,
                         HttpServletResponse resp)
      throws IOException {
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

      final MethodBase method = getMethod(notifier, methodName);

      if (method == null) {
        info("No method for '" + methodName + "'");

        // ==========================================================
        //     Set the correct response
        // ==========================================================
      } else {
        method.init(notifier, dumpContent);
        method.doMethod(req, resp);
      }
//    } catch (WebdavForbidden wdf) {
  //    sendError(notifier, wdf, resp);
    } catch (final Throwable t) {
      serverError = handleException(notifier, t, resp, serverError);
    } finally {
      if (notifier != null) {
        try {
//          notifier.close();
        } catch (final Throwable t) {
          serverError = handleException(notifier, t, resp, serverError);
        }
      }

      try {
        tryWait(req, false);
      } catch (final Throwable ignored) {}

      if (debug() && dumpContent &&
          (resp instanceof final CharArrayWrappedResponse wresp)) {
        /* instanceof check because we might get a subsequent exception before
         * we wrap the response
         */

        if (wresp.getUsedOutputStream()) {
          debug("------------------------ response written to output stream -------------------");
        } else {
          final String str = wresp.toString();

          debug("------------------------ Dump of response -------------------");
          debug(str);
          debug("---------------------- End dump of response -----------------");

          final byte[] bs = str.getBytes();
          resp = (HttpServletResponse)wresp.getResponse();
          debug("contentLength=" + bs.length);
          resp.setContentLength(bs.length);
          resp.getOutputStream().write(bs);
        }
      }

      /* WebDAV is stateless - toss away the session */
      try {
        final HttpSession sess = req.getSession(false);
        if (sess != null) {
          sess.invalidate();
        }
      } catch (final Throwable ignored) {}
    }
  }

  /* Return true if it's a server error */
  private boolean handleException(final NotifyEngine notifier, final Throwable t,
                                  final HttpServletResponse resp,
                                  boolean serverError) {
    if (serverError) {
      return true;
    }

    try {
      if (t instanceof final NoteException se) {
        final int status = se.getStatusCode();
        if (status == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
          error(se);
          serverError = true;
        }
        sendError(notifier, t, resp);
        return serverError;
      }

      error(t);
      sendError(notifier, t, resp);
      return true;
    } catch (final Throwable t1) {
      // Pretty much screwed if we get here
      return true;
    }
  }

  private void sendError(final NotifyEngine notifier, final Throwable t,
                         final HttpServletResponse resp) {
    try {
      if (t instanceof final NoteException se) {
        final QName errorTag = se.getErrorTag();

        if (errorTag != null) {
          if (debug()) {
            debug("setStatus(" + se.getStatusCode() + ")");
          }
          resp.setStatus(se.getStatusCode());
          resp.setContentType("text/xml; charset=UTF-8");
          if (!emitError(notifier, errorTag, se.getMessage(),
                         resp.getWriter())) {
            final StringWriter sw = new StringWriter();
            emitError(notifier, errorTag, se.getMessage(), sw);

            try {
              if (debug()) {
                debug("setStatus(" + se.getStatusCode() + ")");
              }
              resp.sendError(se.getStatusCode(), sw.toString());
            } catch (final Throwable ignored) {
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
    } catch (final Throwable ignored) {
      // Pretty much screwed if we get here
    }
  }

  private boolean emitError(final NotifyEngine notifier,
                            final QName errorTag,
                            final String extra,
                            final Writer wtr) {
    try {
      final XmlEmit xml = new XmlEmit();
//      notifier.addNamespace(xml);

      xml.startEmit(wtr);
      xml.openTag(WebdavTags.error);

  //    notifier.emitError(errorTag, extra, xml);

      xml.closeTag(WebdavTags.error);
      xml.flush();

      return true;
    } catch (final Throwable t1) {
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
   * @param notifier notify engine
   * @param name of method
   * @return method
   */
  public MethodBase getMethod(final NotifyEngine notifier,
                              final String name) {
    final MethodInfo mi = methods.get(name.toUpperCase());

//    if ((mi == null) || (getAnonymous() && mi.getRequiresAuth())) {
  //    return null;
    //}

    try {
      final MethodBase mb = mi.getMethodClass().newInstance();

      mb.init(notifier, dumpContent);

      return mb;
    } catch (final Throwable t) {
      if (debug()) {
        error(t);
      }
      throw new NoteException(t);
    }
  }

  private void tryWait(final HttpServletRequest req, final boolean in) {
    Waiter wtr;
    synchronized (waiters) {
      //String key = req.getRequestedSessionId();
      final String key = req.getRemoteUser();
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

        try {
          wtr.wait();
        } catch (final InterruptedException e) {
          throw new RuntimeException(e);
        }
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
    final HttpSession session = se.getSession();
    final String sessid = session.getId();
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
      final Enumeration<String> hnames = req.getHeaderNames();

      String title = "Request headers";

      debug(title);

      while (hnames.hasMoreElements()) {
        final String key = hnames.nextElement();
        final String val = req.getHeader(key);
        debug("  " + key + " = \"" + val + "\"");
      }

      final Enumeration<String> pnames = req.getParameterNames();

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

      while (pnames.hasMoreElements()) {
        final String key = pnames.nextElement();
        final String val = req.getParameter(key);
        debug("  " + key + " = \"" + val + "\"");
      }
    } catch (final Throwable ignored) {
    }
  }

  /* -----------------------------------------------------------------------
   *                         JMX support
   */

  static class Configurator extends ConfBase {
    NotifyConf notifyConf;

    Configurator() {
      super("org.bedework.notify:service=Notify",
            (String)null,
            null);
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
        final HttpOut ho = new HttpOut("notify",
                                       "httpConfig");
        register(new ObjectName(ho.getServiceName()), ho);
        ho.loadConfig();
      } catch (final Throwable t){
        t.printStackTrace();
      }
    }

    @Override
    public void stop() {
      try {
        notifyConf.stop();
        ConfBase.getManagementContext().stop();
      } catch (final Throwable t){
        t.printStackTrace();
      }
    }
  }

  private final Configurator conf = new Configurator();

  @Override
  public void contextInitialized(final ServletContextEvent sce) {
    conf.start();
  }

  @Override
  public void contextDestroyed(final ServletContextEvent sce) {
    conf.stop();
  }

  /* ==============================================================
   *                   Logged methods
   * ============================================================== */

  private final BwLogger logger = new BwLogger();

  @Override
  public BwLogger getLogger() {
    if ((logger.getLoggedClass() == null) && (logger.getLoggedName() == null)) {
      logger.setLoggedClass(getClass());
    }

    return logger;
  }
}
