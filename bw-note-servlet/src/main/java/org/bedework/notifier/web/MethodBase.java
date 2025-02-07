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
import org.bedework.notifier.db.JsonUtil;
import org.bedework.notifier.db.NotifyDb;
import org.bedework.notifier.exception.NoteException;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.w3c.dom.Document;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;

/** Base class for all webdav servlet methods.
 */
public abstract class MethodBase extends JsonUtil implements Logged {
  protected boolean dumpContent;

  protected NotifyEngine notifier;

  private NotifyDb db;

  protected ObjectMapper om = new ObjectMapper();
  //private String resourceUri;

  // private String content;

  //protected XmlEmit xml;

  /** Called at each request
   *
   */
  public abstract void init();

  private final SimpleDateFormat httpDateFormatter =
      new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss ");

  /**
   * @param req http request
   * @param resp http response
   */
  public abstract void doMethod(HttpServletRequest req,
                                HttpServletResponse resp)
        throws NoteException;

  /** Allow servlet to create method.
   */
  public static class MethodInfo {
    private final Class<? extends MethodBase> methodClass;

    private final boolean requiresAuth;

    /**
     * @param methodClass class
     * @param requiresAuth true if this methd requires auth
     */
    public MethodInfo(final Class<? extends MethodBase> methodClass,
                      final boolean requiresAuth) {
      this.methodClass = methodClass;
      this.requiresAuth = requiresAuth;
    }

    /**
     * @return Class for this method
     */
    public Class<? extends MethodBase> getMethodClass() {
      return methodClass;
    }

    /** Called when servicing a request to determine if this method requires
     * authentication. Allows the servlet to reject attempts to change state
     * while unauthenticated.
     *
     * @return boolean true if authentication required.
     */
    public boolean getRequiresAuth() {
      return requiresAuth;
    }
  }

  /** Called at each request
   *
   * @param notifier the engine
   * @param dumpContent should we dump content for trace
   */
  public void init(final NotifyEngine notifier,
                   final boolean dumpContent) {
    this.notifier = notifier;
    this.dumpContent = dumpContent;
//    xml = notifier.getXmlEmit();

    // content = null;
    //resourceUri = null;

    init();
  }

  /** Get notifier
   *
   * @return NotifyEngine
   */
  public NotifyEngine getNotifier() {
    return notifier;
  }

  /** Get notifier db
   *
   * @return NotifyDb
   */
  public NotifyDb getDb() {
    if (db != null) {
      return db;
    }

    db = NotifyEngine.getNewDb();
    return db;
  }

  /** Get the decoded and fixed resource URI. This calls getServletPath() to
   * obtain the path information. The description of that method is a little
   * obscure in it's meaning. In a request of this form:<br><br>
   * "GET /ucaldav/user/douglm/calendar/1302064354993-g.ics HTTP/1.1[\r][\n]"<br><br>
   * getServletPath() will return <br><br>
   * /user/douglm/calendar/1302064354993-g.ics<br><br>
   * that is the context has been removed. In addition this method will URL
   * decode the path. getRequestUrl() does neither.
   *
   * @param req      Servlet request object
   * @return List    Path elements of fixed up uri
   */
  public List<String> getResourceUri(final HttpServletRequest req) {
    String uri = req.getServletPath();

    if ((uri == null) || (uri.isEmpty())) {
      /* No path specified - set it to root. */
      uri = "/";
    }

    return fixPath(uri);
  }

  /** Return a path, broken into its elements, after "." and ".." are removed.
   * If the parameter path attempts to go above the root we return null.
   * <p>
   * Other than the backslash thing why not use URI?
   *
   * @param path      String path to be fixed
   * @return String[]   fixed path broken into elements
   */
  public static List<String> fixPath(final String path) {
    if (path == null) {
      return null;
    }

    String decoded;
    try {
      decoded = URLDecoder.decode(path, StandardCharsets.UTF_8);
    } catch (final Throwable t) {
      throw new NoteException("bad path: " + path);
    }

    if (decoded == null) {
      return (null);
    }

    /* Make any backslashes into forward slashes.
     */
    if (decoded.indexOf('\\') >= 0) {
      decoded = decoded.replace('\\', '/');
    }

    /* Ensure a leading '/'
     */
    if (!decoded.startsWith("/")) {
      decoded = "/" + decoded;
    }

    /* Remove all instances of '//'.
     */
    while (decoded.contains("//")) {
      decoded = decoded.replaceAll("//", "/");
    }

    /* Somewhere we may have /./ or /../
     */

    final StringTokenizer st = new StringTokenizer(decoded, "/");

    final ArrayList<String> al = new ArrayList<>();
    while (st.hasMoreTokens()) {
      final String s = st.nextToken();

      if (s.equals(".")) {
        continue;// ignore
      }
      if (s.equals("..")) {
        // Back up 1
        if (al.isEmpty()) {
          // back too far
          return null;
        }

        al.remove(al.size() - 1);
      } else {
        al.add(s);
      }
    }

    return al;
  }

  /*
  protected void addStatus(final int status,
                           final String message) {
    try {
      if (message == null) {
//        message = WebdavStatusCode.getMessage(status);
      }

      property(WebdavTags.status, "HTTP/1.1 " + status + " " + message);
    } catch (NoteException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new NoteException(t);
    }
  }
  */

  protected void addHeaders(final HttpServletResponse resp) {
    // This probably needs changes
/*
    StringBuilder methods = new StringBuilder();
    for (String name: getNotifier().getMethodNames()) {
      if (methods.length() > 0) {
        methods.append(", ");
      }

      methods.append(name);
    }

    resp.addHeader("Allow", methods.toString());
    */
    resp.addHeader("Allow", "POST, GET");
  }

  protected Map<?, ?> getJson(final HttpServletRequest req,
                              final HttpServletResponse resp) {
    final int len = req.getContentLength();
    if (len == 0) {
      return null;
    }

    try {
      return (Map<?, ?>)om.readValue(req.getInputStream(), Object.class);
    } catch (final Throwable t) {
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      throw new NoteException(t);
    }
  }

  /** Parse the request body, and return the DOM representation.
   *
   * @param req        Servlet request object
   * @param resp       Servlet response object for bad status
   * @return Document  Parsed body or null for no body
   */
  protected Document parseContent(final HttpServletRequest req,
                                  final HttpServletResponse resp)
      throws NoteException {
    final int len = req.getContentLength();
    if (len == 0) {
      return null;
    }

    try {
      final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);

      //DocumentBuilder builder = factory.newDocumentBuilder();
/*
      Reader rdr = getNsIntf().getReader(req);

      if (rdr == null) {
        // No content?
        return null;
      }

      return builder.parse(new InputSource(rdr));*/
      return null;
//    } catch (SAXException e) {
  //    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    //  throw new NoteException(HttpServletResponse.SC_BAD_REQUEST);
    } catch (Throwable t) {
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      throw new NoteException(t);
    }
  }

  protected String formatHTTPDate(final Timestamp val) {
    if (val == null) {
      return null;
    }

    synchronized (httpDateFormatter) {
      return httpDateFormatter.format(val) + "GMT";
    }
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

