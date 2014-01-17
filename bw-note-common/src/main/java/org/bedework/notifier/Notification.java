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
package org.bedework.notifier;

import org.bedework.util.misc.ToString;

/** Notification from external system.
 *
 * <p>This can enclose an invitation or notification from bedework.
 *
 * @author douglm
 *
 * @param <T>
 */
public abstract class Notification<T> {
  private T notification;

  /** Create a notification
   */
  public Notification(T notification) {
    this.notification = notification;
  }

  /**
   * @return the wrapped notification
   */
  public T getNotification() {
    return notification;
  }

  protected abstract void toStringSegment(final ToString ts);

  @Override
  public String toString() {
    ToString ts = new ToString(this);

    toStringSegment(ts);

    return ts.toString();
  }
}
