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

import org.bedework.caldav.util.notifications.NotificationType;
import org.bedework.notifier.notifications.Notification;
import org.bedework.util.misc.ToString;
import org.bedework.util.xml.tagdefs.AppleServerTags;

import javax.xml.namespace.QName;

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
 */
public class BedeworkNotification extends Notification<NotificationType> {

  /** Create a notification
   */
  public BedeworkNotification(final NotificationType notification) {
    super(notification);
  }

  @Override
  public Boolean isRegisteredRecipient() {
    return null;
  }

  @Override
  public NotificationKind getKind() {
    QName nm = getNotification().getNotification().getElementName();

    if (nm.equals(AppleServerTags.inviteNotification)) {
      return NotificationKind.sharingInvitation;
    }

    return null;
  }

  protected void toStringSegment(final ToString ts) {
    ts.append("notification", getNotification());
  }

  @Override
  public String toString() {
    final ToString ts = new ToString(this);

    toStringSegment(ts);

    return ts.toString();
  }
}
