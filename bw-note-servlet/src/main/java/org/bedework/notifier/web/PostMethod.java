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
import org.bedework.notifier.NotifyRegistry;
import org.bedework.notifier.db.Subscription;
import org.bedework.notifier.exception.NoteException;
import org.bedework.util.misc.Util;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Handle POST for exchange synch servlet.
 */
public class PostMethod extends MethodBase {
  @Override
  public void init() throws NoteException {
  }

  @SuppressWarnings({"unchecked"})
  @Override
  public void doMethod(final HttpServletRequest req,
                       final HttpServletResponse resp) throws NoteException {
    try {
      getDb().startTransaction();

      List<String> resourceUri = getResourceUri(req);

      if (Util.isEmpty(resourceUri)) {
        throw new NoteException("Bad resource url - no connector specified");
      }

      final String ruri = resourceUri.get(0);

      if ("notification".equals(ruri)) {
        processNotification(req, resp, resourceUri);
        return;
      }

      if ("subscribe".equals(ruri)) {
        processSubscribe(req, resp, resourceUri);
        return;
      } else if ("unsubscribe".equals(ruri)) {
        processUnsubscribe(req, resp, resourceUri);
        return;
      }

      if (debug) {
        debugMsg("Unknown POST uri: " + ruri);
      }

      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    } catch (final NoteException se) {
      throw se;
    } catch(final Throwable t) {
      throw new NoteException(t);
    } finally {
      getDb().endTransaction();
    }
  }

  private void processNotification(final HttpServletRequest req,
                                   final HttpServletResponse resp,
                                   final List<String> resourceUri) throws NoteException {
    /* A system is telling us there are notifications we need to
     * take care of.
     *
     * We get a message which defines the system, provides a token and
     * a href identifying the notification collection to be queried.
     * The resource name indicates which resource message we should wait for, at a minimum.
     */

    final Map vals = getJson(req, resp);

    try {
      final String system = must("system", vals);
      final String token = must("token", vals);
      final String href = may("href", vals);
      final String resourceName = may("resourceName", vals);

      if (!NotifyEngine.authenticate(system, token)) {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }

      /* Queue the hrefs */

      resp.setStatus(HttpServletResponse.SC_OK);

      if (href == null) {
        return;
      }

      notifier.addNotificationMsg(
              new NotifyEngine.NotificationMsg(system,
                                               Util.buildPath(true, href),
                                               resourceName));
    } catch(final Throwable t) {
      if (debug) {
        error(t);
      }
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
  }

  private void processSubscribe(final HttpServletRequest req,
                                final HttpServletResponse resp,
                                final List<String> resourceUri) throws NoteException {
    /* We have a subscription message for a user.
     * Eventually this will have information about which type of
     * notifications and how they are to be sent. For the moment we
     * just need a system name, a token and a user principal.
     */

    final Map vals = getJson(req, resp);

    try {
      final String system = must("system", vals);
      final String token = must("token", vals);

      if (!NotifyEngine.authenticate(system, token)) {
        if (debug) {
          debugMsg("Bad sys/token " + system + ", " + token);
        }
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }

      Subscription sub = NotifyRegistry.
              getConnector(system).subscribe(getDb(), vals);

      if (sub == null) {
        if (debug) {
          debugMsg("Subscribe failed for " + system + ", " + token);
        }
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }

      resp.setStatus(HttpServletResponse.SC_OK);
    } catch(final Throwable t) {
      if (debug) {
        error(t);
      }
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
  }

  private void processUnsubscribe(final HttpServletRequest req,
                                  final HttpServletResponse resp,
                                  final List<String> resourceUri) throws NoteException {
    /* We have an unsubscribe message for a user.
     */

    final Map vals = getJson(req, resp);

    try {
      final String system = must("system", vals);
      final String token = must("token", vals);

      if (!NotifyEngine.authenticate(system, token)) {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }

      Subscription sub = NotifyRegistry.
              getConnector(system).unsubscribe(getDb(), vals);

      if (sub == null) {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }

      resp.setStatus(HttpServletResponse.SC_OK);
    } catch(final Throwable t) {
      if (debug) {
        error(t);
      }
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
  }
}

