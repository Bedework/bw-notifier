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
package org.bedework.notifier.cnctrs.bedework;

import org.bedework.notifier.NotifyDefs.NotifyKind;
import org.bedework.notifier.NotifyEngine;
import org.bedework.notifier.NotifyRegistry;
import org.bedework.notifier.cnctrs.AbstractConnector;
import org.bedework.notifier.cnctrs.ConnectorInstanceMap;
import org.bedework.notifier.db.Subscription;
import org.bedework.notifier.exception.NoteException;
import org.bedework.notifier.notifications.Note;
import org.bedework.util.misc.Util;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** The notification inbound processor connector for connections to bedework.
 *
 * <p>This connector sets up inbound connection instances to provide a
 * stream of notification messages into the notification system</p>
 *
 * <p>The notification directory path points to a special collection
 * which contains links to the calendar homes of accounts for which
 * we want outbound notifications</p>
 *
 * @author Mike Douglass
 */
public class BedeworkConnector
      extends AbstractConnector<BedeworkConnector,
                                BedeworkConnectorInstance,
                                Note,
                                BedeworkConnectorConfig> {
  private final ConnectorInstanceMap<BedeworkConnectorInstance> cinstMap =
      new ConnectorInstanceMap<>();

  private static class Authenticator implements NotifyRegistry.Authenticator {
    BedeworkConnectorConfig conf;

    @Override
    public boolean authenticate(final String token)
            throws NoteException {
      return (token != null) && (conf.getToken() != null) &&
              token.equals(conf.getToken());
    }
  }

  /**
   */
  public BedeworkConnector() {
  }

  @Override
  public void start(final String callbackUri,
                    final NotifyEngine notifier) throws NoteException {
    super.start(callbackUri, notifier);

    initSpecialNotifications();
  }

  @Override
  public NotifyRegistry.Info getInfo() {
    final Authenticator authenticator = new Authenticator();

    authenticator.conf = config;

    return new NotifyRegistry.Info(getConnectorName(),
                                   BedeworkSubscription.class,
                                   authenticator);
  }

  @Override
  public Subscription subscribe(final Map<?, ?> vals)
          throws NoteException {
    /* We require an href - the principal owning the
     * notifications.
     *
     * We also need at least one email address.
     *
     * There may already be a subscription for this principal.
     * If so we add the email address. If not we create a new
     * one.
     */
    final String href = must("href", vals);
    final List<String> emails = mustList("emailAddresses", vals);

    Subscription theSub =
            notifier.find(getConnectorName(), href);
    if (theSub == null) {
      if (debug) {
        trace("Adding subscription");
      }
      final BedeworkSubscription sub = new BedeworkSubscription();

      sub.setConnectorName(getConnectorName());

      sub.setPrincipalHref(href);

      for (final String email: emails) {
        sub.addEmail(email);
      }

      notifier.addSubscription(sub);
      return sub;
    }

    if (debug) {
      trace("Updating subscription");
    }

    final BedeworkSubscription sub = new BedeworkSubscription(theSub);

    for (final String email: emails) {
      sub.addEmail(email);
    }

    notifier.updateSubscription(sub);

    return sub;
  }

  @Override
  public Subscription unsubscribe(final Map<?, ?> vals)
          throws NoteException {
    /* We require an href - the principal owning the
     * notifications.
     *
     * There should be a subscription for this principal.
     * If there are no email adresses we remove the subscription.
     *
     * Otherwise we remove the addresses. If there are none left we
     * remove the subscription otherwise we update.
     */
    final String href = must("href", vals);
    final List<String> emails = mayList("emailAddresses", vals);

    Subscription theSub = notifier.find(getConnectorName(), href);
    if (theSub == null) {
      return null;
    }

    final BedeworkSubscription sub = new BedeworkSubscription(theSub);

    if (Util.isEmpty(emails)) {
      notifier.deleteSubscription(sub);
      sub.setDeleted(true);
      return sub;
    }

    for (final String email: emails) {
      sub.getEmails().remove(email);
    }

    if (Util.isEmpty(sub.getEmails())) {
      notifier.deleteSubscription(sub);
      sub.setDeleted(true);
      return sub;
    }

    notifier.updateSubscription(sub);

    return sub;
  }

  @Override
  public boolean isManager() {
    return false;
  }

  @Override
  public NotifyKind getKind() {
//    return NotifyKind.notify;
    return NotifyKind.poll;
  }

  @Override
  public boolean isReadOnly() {
    return config.getReadOnly();
  }

  @Override
  public boolean getTrustLastmod() {
    return config.getTrustLastmod();
  }

  @Override
  public BedeworkConnectorInstance getConnectorInstance(final Subscription sub) throws NoteException {
    if (!running) {
      return null;
    }

    BedeworkConnectorInstance inst = cinstMap.find(sub);

    if (inst != null) {
      return inst;
    }

    inst = new BedeworkConnectorInstance(config,
                                         this, sub);
    cinstMap.add(sub, inst);

    return inst;
  }

  class BedeworkNotificationBatch extends NotificationBatch<Note> {
  }

  @Override
  public BedeworkNotificationBatch handleCallback(final HttpServletRequest req,
                                     final HttpServletResponse resp,
                                     final List<String> resourceUri) throws NoteException {
    return null;
  }

  @Override
  public void respondCallback(final HttpServletResponse resp,
                              final NotificationBatch<Note> notifications)
                                                    throws NoteException {
  }

  @Override
  public void stop() throws NoteException {
    stopped = true;
  }

  /* ====================================================================
   *                         Package methods
   * ==================================================================== */

  private List<Header> authheaders;

  List<Header> getAuthHeaders() {
    if (authheaders != null) {
      return authheaders;
    }

    final String id = config.getId();
    final String token = config.getToken();

    if ((id == null) || (token == null)) {
      return null;
    }

    authheaders = new ArrayList<>(1);
    authheaders.add(new BasicHeader("X-BEDEWORK-NOTE", id + ":" + token));
    authheaders.add(new BasicHeader("X-BEDEWORK-EXTENSIONS", "true"));

    return authheaders;
  }

  /* ====================================================================
   *                   Private methods
   * ==================================================================== */

  private void initSpecialNotifications() throws NoteException {
    try {
      if (config.getSystemNotificationHref() == null) {
        return;
      }

      if (debug) {
        trace("Notification collections available on " +
                      config.getSystemNotificationHref());
      }

      /* Create a special subscription for this principal
       */

      final BedeworkSubscription sub = new BedeworkSubscription();

      sub.setConnectorName(getConnectorName());
      sub.setPrincipalHref(config.getSystemNotificationHref());

      notifier.add(sub);
    } catch (final Throwable t) {
      throw new NoteException(t);
    }
  }
}