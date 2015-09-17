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

import org.bedework.caldav.util.notifications.BaseNotificationType;
import org.bedework.caldav.util.notifications.NotificationType;
import org.bedework.notifier.cnctrs.ConnectorInstance.ItemInfo;
import org.bedework.util.misc.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Notification from external system. We extract some of the system specific
 * information and represent it as generic fields in this object.
 *
 * <p>For instance the delivery method may be specified in a
 * number of system specific ways. It's up to the connector instance
 * to express that in a generic manner</p>
 *
 * <p>This can enclose an invitation or notification from bedework.
 *
 * <p>The incoming notification is a generic wrapper with common fields
 * which encloses different types of notification message.</p>
 *
 * @author douglm
 *
 */
public class Note {
  private final ItemInfo itemInfo;

  private final NotificationType notification;

  private Map extraValues;

  public enum DeliveryMethod {
    email,

    sms
  }

  private final List<DeliveryMethod> deliveryMethods = new ArrayList<>();

  /** Create a notification
   *
   * @param itemInfo so we can update it
   * @param notification the notification
   */
  public Note(final ItemInfo itemInfo,
              final NotificationType notification) {
    this.itemInfo = itemInfo;
    this.notification = notification;
  }

  /**
   * @return info about where it came from
   */
  public ItemInfo getItemInfo() {
    return itemInfo;
  }

  /**
   * @return the wrapped notification content
   */
  public NotificationType getNotification() {
    return notification;
  }

  /**
   * @return the wrapped notification content
   */
  public BaseNotificationType getNotificationContent() {
    return notification.getNotification();
  }

  /**
   * @param val - email etc
   */
  public void addDeliveryMethod(final DeliveryMethod val) {
    getDeliveryMethods().add(val);
  }

  /**
   * @return the delivery method - email etc
   */
  public List<DeliveryMethod> getDeliveryMethods() {
    return deliveryMethods;
  }

  /**
   *
   * @param val extra template values
   */
  public void setExtraValues(final Map val) {
    extraValues = val;
  }

  /**
   * @return extra template values
   */
  public Map getExtraValues() {
    return extraValues;
  }

  protected void toStringSegment(final ToString ts) {
    ts.append("deliveryMethods", getDeliveryMethods());
    ts.append("notification", getNotification());
  }

  @Override
  public String toString() {
    final ToString ts = new ToString(this);

    toStringSegment(ts);

    return ts.toString();
  }
}
