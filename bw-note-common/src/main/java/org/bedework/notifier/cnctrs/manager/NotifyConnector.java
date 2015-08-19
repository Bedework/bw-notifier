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
package org.bedework.notifier.cnctrs.manager;

import org.bedework.notifier.NotifyDefs.NotifyKind;
import org.bedework.notifier.NotifyEngine;
import org.bedework.notifier.NotifyRegistry;
import org.bedework.notifier.cnctrs.AbstractConnector;
import org.bedework.notifier.conf.ConnectorConfig;
import org.bedework.notifier.db.Subscription;
import org.bedework.notifier.db.SubscriptionWrapper;
import org.bedework.notifier.exception.NoteException;
import org.bedework.notifier.notifications.Note;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** A special connector to handle calls to the notifier engine via the web context.
 *
 * <p>Not sure we need this at all
 *
 * @author Mike Douglass
 */
public class NotifyConnector extends AbstractConnector<NotifyConnector,
        NotifyConnectorInstance,
        Note,
        ConnectorConfig> {

  private static class Authenticator implements NotifyRegistry.Authenticator {
    ConnectorConfig conf;

    @Override
    public boolean authenticate(final String token)
            throws NoteException {
      return false;
    }
  }

  /**
   */
  public NotifyConnector() {
    //super(null);
  }

  @Override
  public void start(final String callbackUri,
                    final NotifyEngine syncher) throws NoteException {
    super.start(callbackUri, syncher);

    stopped = false;
    running = true;
  }

  @Override
  public NotifyRegistry.Info getInfo() {
    final Authenticator authenticator = new Authenticator();

    authenticator.conf = config;

    return new NotifyRegistry.Info(getConnectorName(),
                                   SubscriptionWrapper.class,
                                   authenticator);
  }

  @Override
  public Subscription subscribe(final Map<?, ?> vals)
          throws NoteException {
    return null;
  }

  @Override
  public Subscription unsubscribe(final Map<?, ?> vals)
          throws NoteException {
    return null;
  }

  @Override
  public boolean isManager() {
    return true;
  }

  @Override
  public NotifyKind getKind() {
    return NotifyKind.notify;
  }

  @Override
  public boolean isReadOnly() {
    return true;
  }

  @Override
  public boolean getTrustLastmod() {
    return false;
  }

  @Override
  public NotifyConnectorInstance getConnectorInstance(final Subscription sub) throws NoteException {
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public NotificationBatch handleCallback(final HttpServletRequest req,
                                          final HttpServletResponse resp,
                                          final List<String> resourceUri) throws NoteException {
    try {
      // Resource uri unused for the moment - must be null or zero length (or "/")

      if (resourceUri.size() > 0) {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return null;
      }

      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return null;
//    } catch (NoteException se) {
 //     throw se;
    } catch(Throwable t) {
      throw new NoteException(t);
    }
  }

  @Override
  public void respondCallback(final HttpServletResponse resp,
                              final NotificationBatch<Note> notifications)
                                                    throws NoteException {
    try {
      /* We only expect single notification items in a batch */

      if (notifications.getNotifications().size() != 1) {
        // XXX Error?
        return;
      }
//    } catch (NoteException se) {
  //    throw se;
    } catch(Throwable t) {
      throw new NoteException(t);
    }
  }

  /* ====================================================================
   *                   Private methods
   * ==================================================================== */
}
