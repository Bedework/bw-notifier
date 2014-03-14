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
package org.bedework.notifier.notifications;

import org.bedework.util.misc.ToString;

/** Notification from external system. We extract some of the system specific
 * information and represent it as generic fields in this object.
 *
 * <p>For instance the delivery method may be specified in a
 * number of system specific ways. It's up to the connector instance
 * to express that in a generic manner</p>
 *
 * <p>This can enclose an invitation or notification from bedework.
 *
 * @author douglm
 *
 * @param <T>
 */
public abstract class Notification<T> {
  private final T notification;

  public static enum NotificationKind {
    /** The owner of a collection is inviting the recipient to share
     * the collection
     */
    sharingInvitation,

    /** The sender - not necessarily the owner - suggests the recipient
     * subscribe to an available collection.
     */
    subscribeInvitation,

    /** A resource in a collection to which the recipient is
     * subscribed or a sharee, has changed.
     */
    resourceChange
  }

  public static enum DeliveryMethod {
    email,

    sms
  }

  private DeliveryMethod deliveryMethod;

  /** Create a notification
   */
  public Notification(final T notification) {
    this.notification = notification;
  }

  /** This allows us to determine if the recipient is a registered
   * user of the system, an unregistered user or unknown.
   *
   * @return true/false or null for unknown.
   */
  public abstract Boolean isRegisteredRecipient();

  /**
   * @return null for unknown.
   */
  public abstract NotificationKind getKind();

  /**
   * @return the wrapped notification
   */
  public T getNotification() {
    return notification;
  }

  /**
   * @param deliveryMethod - email etc
   */
  public void setDeliveryMethod(final DeliveryMethod deliveryMethod) {
    this.deliveryMethod = deliveryMethod;
  }

  /**
   * @return the delivery method - email etc
   */
  public DeliveryMethod getDeliveryMethod() {
    return deliveryMethod;
  }

  protected void toStringSegment(final ToString ts) {
    ts.append("registeredRecipient", isRegisteredRecipient());
    ts.append("kind", getKind());
    ts.append("deliveryMethod", getDeliveryMethod());
    ts.append("notification", getNotification());
  }

  @Override
  public String toString() {
    final ToString ts = new ToString(this);

    toStringSegment(ts);

    return ts.toString();
  }
}
