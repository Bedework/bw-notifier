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
package org.bedework.notifier.cnctrs;

import org.bedework.notifier.notifications.Notification;
import org.bedework.notifier.NotifyDefs.NotifyKind;
import org.bedework.notifier.NotifyEngine;
import org.bedework.notifier.conf.ConnectorConfig;
import org.bedework.notifier.db.Subscription;
import org.bedework.notifier.exception.NoteException;

import org.oasis_open.docs.ws_calendar.ns.soap.StatusType;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** The interface implemented by connectors. This represents the kind of object
 * used to communicate with a particular system or entity. We may implement
 * connectors for files, for exchange for bedework etc.
 *
 * <p>The connector instance carries out global initialization and provides
 * ConnectorInstance objects per subscription.
 *
 * <p>There is only one instance of each kind of connector with a particular
 * configuration. This connector is called to obtain a ConnectorInstance which
 * carries out the actual operations for a given subscription.
 *
 * @author Mike Douglass
 *
 * @param <C>
 * @param <N>
 */
public interface Connector<C extends ConnectorInstance,
                           N extends Notification> {
  /** Start the connector. A response of null means no synch available.
   *
   * <p>The callback url is unique to the connector. It will be used as a path
   * prefix to allow the callback service to locate the handler for incoming
   * callback requests.
   *
   * <p>For example, if the callback context is /synchcb/ and the connector id
   * is "bedework" then the callback uri might be /synchcb/bedework/. The
   * connector might append a uid to that path to allow it to locate the
   * active subscription for which the callback is intended.
   *
   * @param connectorId - registered id for the connector
   * @param conf
   * @param callbackUri
   * @param notifier
   * @throws org.bedework.notifier.exception.NoteException
   */
  void start(String connectorId,
             ConnectorConfig conf,
             String callbackUri,
             NotifyEngine notifier) throws NoteException;

  /**
   * @return a useful status message
   */
  String getStatus();

  /**
   * @return true if we are the manager
   */
  boolean isManager();

  /**
   * @return true if we started
   */
  boolean isStarted();

  /**
   * @return true if we failed in some way
   */
  boolean isFailed();

  /**
   * @return true if we're stopped
   */
  boolean isStopped();

  /**
   * @return poll or notify?
   */
  NotifyKind getKind();

  /** Is this a read-only connector?
   *
   * @return boolean
   */
  boolean isReadOnly();

  /** Can we trust the lastmod from this connector?
   *
   * @return boolean
   */
  boolean getTrustLastmod();

  /**
   * @return id provided at start
   */
  String getId();

  /**
   * @return callbackUri provided at start
   */
  String getCallbackUri();

  /**
   * @return notifher provided at start
   */
  NotifyEngine getNotifier();

  /**
   * @return an object factory for icalendar
   */
  ietf.params.xml.ns.icalendar_2.ObjectFactory getIcalObjectFactory();

  /**
   * @return list of icalendar properties to skip.
   */
  List<Object> getSkipList();

  /** Called to obtain a connector instance for a subscription.
   * A response of null means none available.
   *
   * @param sub - the subscription
   * @return null for none else a connector instance.
   * @throws org.bedework.notifier.exception.NoteException
   */
  C getConnectorInstance(Subscription sub) throws NoteException;

  /** Far end may send a batch of notifications. These should not be batched
   * arbitrarily. One batch per message and response.
   *
   * @param <N>
   */
  static class NotificationBatch<N extends Notification> {
    private List<N> notifications = new ArrayList<N>();

    private StatusType status;
    private String message;

    public NotificationBatch() {
    }

    public NotificationBatch(final N notification) {
      notifications.add(notification);
    }

    public List<N> getNotifications() {
      return notifications;
    }

    public void addNotification(final N notification) {
      notifications.add(notification);
    }

    public void setStatus(final StatusType val) {
      status = val;
    }

    public StatusType getStatus() {
      return status;
    }

    public void setMessage(final String val) {
      message = val;
    }

    public String getMessage() {
      return message;
    }
  }

  /** Will create a notification batch object which will be passed to a synchling for
   * processing. When processing is complete respond will be called.
   *
   * <p>The resource URI has been stripped of the context element and the
   * element which identifies the connector. What remains is used by the connector
   * to determine a subscription id allowing retrieval of the subscription from
   * the synch engine.
   *
   * @param req
   * @param resp
   * @param resourceUri - elements of the path with context and connector id removed
   * @return Notification with 1 or more Notification items or null for no action.
   * @throws org.bedework.notifier.exception.NoteException
   */
  NotificationBatch<N> handleCallback(HttpServletRequest req,
                                      HttpServletResponse resp,
                                      List<String> resourceUri) throws NoteException;

  /** Will respond to a notification.
   *
   * @param resp
   * @param notifications from handleCallback.
   * @throws org.bedework.notifier.exception.NoteException
   */
  void respondCallback(HttpServletResponse resp,
                       NotificationBatch<N> notifications) throws NoteException;

  /** Shut down the connector
   * @throws org.bedework.notifier.exception.NoteException
   */
  void stop() throws NoteException;
}
