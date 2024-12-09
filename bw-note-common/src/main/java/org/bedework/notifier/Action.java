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

import org.bedework.notifier.cnctrs.Connector;
import org.bedework.notifier.cnctrs.ConnectorInstance;
import org.bedework.notifier.db.Subscription;
import org.bedework.notifier.notifications.Note;
import org.bedework.util.misc.ToString;

import static org.bedework.notifier.NotifyEngine.NotificationMsg;

/** Internal system action.
 *
 * <p>Usually a request for a noteling to do something.
 *
 * @author douglm
 *
 */
public class Action {
  public enum ActionType {
    /** signal came in that href may need processing */
    notificationMsg,

    /** check for new items - polling or a signal came in */
    checkItems,

    /** Process an item */
    processItem,

    /** Process a single outbound notification */
    processOutbound
  }

  private ActionType type;

  private NotificationMsg msg;

  private Subscription sub;

  private Note note;

  private Connector sourceConn;

  private ConnectorInstance sourceConnInst;

  private int retries = 0;

  /** Create an action
   */
  public Action(final ActionType type,
                final NotificationMsg msg) {
    this.type = type;
    this.msg = msg;
  }

  /** Create an action
   */
  public Action(final ActionType type,
                final Subscription sub) {
    this(type, sub, null);
  }

  /** Create an action
   */
  public Action(final ActionType type,
                final Subscription sub,
                final Note note) {
    this.type = type;
    this.sub = sub;
    this.note = note;
  }

  /**
   * @param val the action type
   */
  public void setType(final ActionType val) {
    type = val;
  }

  /**
   * @return the type
   */
  public ActionType getType() {
    return type;
  }

  /**
   * @return msg for this action
   */
  public NotificationMsg getMsg() {
    return msg;
  }

  /**
   *
   * @param val associated subscription
   */
  public void setSub(final Subscription val) {
    sub = val;
  }

  /**
   * @return subscription for this action
   */
  public Subscription getSub() {
    return sub;
  }

  /**
   * @return notification for this action
   */
  public Note getNote() {
    return note;
  }

  /**
   * @param val a connection
   */
  public void setConn(final Connector val) {
    sourceConn = val;
  }

  /**
   * @return a connection or null
   */
  public Connector getConn() {
    return sourceConn;
  }

  /**
   * @param val a connection instance
   */
  public void setConnInst(final ConnectorInstance val) {
    sourceConnInst = val;
  }

  /**
   * @return a connection instance or null
   */
  public ConnectorInstance getConnInst() {
    return sourceConnInst;
  }

  public int getRetries() {
    return retries;
  }

  public void setRetries(final int retries) {
    this.retries = retries;
  }

  protected void toStringSegment(final ToString ts) {
    ts.append("type", getType());
  }

  public String toString() {
    final ToString ts = new ToString(this);

    toStringSegment(ts);

    return ts.toString();
  }
}
