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
package org.bedework.notifier.db;

import org.bedework.notifier.BaseSubscriptionInfo;
import org.bedework.notifier.NotifyDefs.NotifyKind;
import org.bedework.notifier.cnctrs.Connector;
import org.bedework.notifier.cnctrs.ConnectorInstance;
import org.bedework.notifier.exception.NoteException;
import org.bedework.util.misc.ToString;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.property.DtStamp;

import java.util.Date;
import java.util.UUID;

/** Represents a subscription for the notification engine.
 *
 * <p>A subscription will need to list the source(s) and the manner of
 * notification. Or perhaps one subscription per source.
 *
 * <p>Each connection has a kind which is a name used to retrieve a connector
 * from the notification engine. The retrieved connector implements the Connector
 * interface. This connector object can then be used to retrieve a ConnectionInst
 * implementation which uses information stored in a serializable object to
 * obtain connection specific properties such as id and password.
 *
 * <p>These properties are obtained by presenting the user with a list of
 * required properties and then encrypting and storing the response. The
 * serialized result is stored as a field in the subscription.
 *
 * <p>Connections are either polling or notify. Polling means that
 * the host will be polled to see if anything has changed. Notify means that
 * the subscription will be activated when the system is notified of a change.
 *
 * <p>Connections are also resynch only - that is the far end does not support
 * fetching of individual items but must be completely resynched each time, or
 * the support the full synch feature set.
 *
 * <p>Resynch connections support relatively simple protocols or file synch.
 *
 * <p>The full feature connections are used for bedework, Exchange etc.
 *
 * <h1>Skip Lists</h1>
 * A skip list allows the diffing process to skip properties that are not to be
 * considered, for example lastmod. We create a skip list from 3 lists;<ul>
 * <li>one for each end of the subscription. This marks properties used
 * exclusively by that end, for example x-properties.</li>
 * <li> One for the middle which might skip properties we want to ignore such as
 * alarms</li>
 * </ul>
 *
 * <p>An empty list means exactly that, no skip properties. A null list means
 * the default diff skip list - probably more useful.
 *
 * @author Mike Douglass
 */
@SuppressWarnings("rawtypes")
public class Subscription extends DbItem<Subscription> {
  private String subscriptionId;

  private String owner;

  private String lastRefresh;

  private int errorCt;

  private boolean missingTarget;

  private SubscriptionConnectorInfo sourceConnectorInfo;

  private SubscriptionInfo info;

  /* Following not persisted */

  private boolean deleted;

  private Connector sourceConn;

  private ConnectorInstance sourceConnInst;

  /** null constructor for hibernate
   *
   */
  public Subscription() {
  }

  /** Constructor to create a new subscription.
   *
   * @param subscriptionId - null means generate one
   */
  public Subscription(final String subscriptionId) {
    if (subscriptionId == null) {
      this.subscriptionId = UUID.randomUUID().toString();
    } else {
      this.subscriptionId = subscriptionId;
    }
  }

  /** Our generated subscriptionId.
   *
   * @param val    String
   */
  public void setSubscriptionId(final String val) {
    subscriptionId = val;
  }

  /**
   *
   * @return String Our generated subscriptionId.
   */
  public String getSubscriptionId() {
    return subscriptionId;
  }

  /**
   *
   * @param val A UTC dtstamp value
   */
  public void setLastRefresh(final String val) {
    lastRefresh = val;
  }

  /**
   * @return String lastRefresh
   */
  public String getLastRefresh() {
    return lastRefresh;
  }

  /**
   *
   * @param val int consecutive errors
   */
  public void setErrorCt(final int val) {
    errorCt = val;
  }

  /**
   * @return int consecutive errors
   */
  public int getErrorCt() {
    return errorCt;
  }

  /**
   *
   * @param val True if the target is missing
   */
  public void setMissingTarget(final boolean val) {
    missingTarget = val;
  }

  /**
   * @return True if either target is missing
   */
  public boolean getMissingTarget() {
    return missingTarget;
  }

  /** The owner. This is the (verified) account that set up the subscription.
   * It is either the authenticated user or provided by a proxy that has
   * verified the account. The owner is only needed for subcribing, unsubscribing
   * and display of and updates to the subscription itself.
   *
   * <p>Interactions with the end points use information contained within the
   * subscription.
   *
   * @param val    String
   */
  public void setOwner(final String val) {
    owner = val;
  }

  /** Owner
   *
   * @return String
   */
  public String getOwner() {
    return owner;
  }

  /** Info for the source connector.
   *
   * @param val SubscriptionConnectorInfo
   */
  public void setSourceConnectorInfo(final SubscriptionConnectorInfo val) {
	  sourceConnectorInfo = val;
  }

  /**
   * @return SubscriptionConnectorInfo
   */
  public SubscriptionConnectorInfo getSourceConnectorInfo() {
    return sourceConnectorInfo;
  }

  /** Info for the subscription.
   *
   * @param val    SubscriptionInfo
   */
  public void setInfo(final SubscriptionInfo val) {
    info = val;
  }

  /**
   * @return SubscriptionInfo
   */
  public SubscriptionInfo getInfo() {
    return info;
  }

  /**
   *
   * @param val True if subscription deleted
   */
  public void setDeleted(final boolean val) {
    deleted = val;
  }

  /**
   * @return True if deleted
   */
  public boolean getDeleted() {
    return deleted;
  }

  /**
   * @param val a connection
   */
  public void setSourceConn(final Connector val) {
    sourceConn = val;
  }

  /**
   * @return a connection or null
   */
  public Connector getSourceConn() {
    return sourceConn;
  }

  /**
   * @param val a connection instance
   */
  public void setSourceConnInst(final ConnectorInstance val) {
    sourceConnInst = val;
  }

  /**
   * @return a connection instance or null
   */
  public ConnectorInstance getSourceConnInst() {
    return sourceConnInst;
  }

  /**
   * @return true if any connector info changed
   */
  public boolean changed() {
    return getSourceConnectorInfo().getChanged();
  }

  /**
   * reset the changed flag.
   */
  public synchronized void resetChanged() {
    if (!changed()) {
      return;
    }

    getSourceConnectorInfo().resetChanged();
  }

  /* ====================================================================
   *                   Convenience methods
   * ==================================================================== */

  /**
   * @return true if this has to be put on a poll queue
   */
  public boolean polling() {
    return getSourceConn().getKind() == NotifyKind.poll;
  }

  /**
   * @return the delay in millisecs.
   * @throws org.bedework.notifier.exception.NoteException
   */
  public long refreshDelay() throws NoteException {
    String delay = "31536000000"; // About a year

    delay = new BaseSubscriptionInfo(getSourceConnectorInfo()).getRefreshDelay();

    return Long.valueOf(delay);
  }

  /** Set the lastRefresh from the current time
   *
   */
  public void updateLastRefresh() {
    setLastRefresh(new DtStamp(new DateTime(true)).getValue());
  }

  /** Get a next refresh date based on the last refresh value
   *
   * @return date value incremented by delay.
   * @throws org.bedework.notifier.exception.NoteException
   */
  public Date nextRefresh() throws NoteException {
    if (getLastRefresh() == null) {
      return new Date();
    }

    try {
      Date dt = new DtStamp(getLastRefresh()).getDate();

      return new Date(dt.getTime() + ((getErrorCt() + 1) * refreshDelay()));
    } catch (Throwable t) {
      throw new NoteException(t);
    }
  }

  /** Add our stuff to the StringBuilder
   *
   * @param ts    for result
   */
  protected void toStringSegment(final ToString ts) {
    super.toStringSegment(ts);

    ts.newLine();
    ts.append("subscriptionId", getSubscriptionId());

    ts.append("lastRefresh", getLastRefresh());

    ts.newLine();
    ts.append("errorCt", getErrorCt());
    ts.append("missingTarget", getMissingTarget());

    ts.newLine();
    ts.append("sourceConnectorInfo", getSourceConnectorInfo());

    ts.newLine();
    ts.append("info", getInfo());
  }

  /* ====================================================================
   *                   Object methods
   * The following are required for a db object.
   * ==================================================================== */

  @Override
  public int hashCode() {
    return getSubscriptionId().hashCode();
  }

  @Override
  public int compareTo(final Subscription that) {
    if (this == that) {
      return 0;
    }

    return getSubscriptionId().compareTo(that.getSubscriptionId());
  }

  @Override
  public String toString() {
    ToString ts = new ToString(this);

    toStringSegment(ts);

    return ts.toString();
  }
}
