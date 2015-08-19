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

import org.bedework.notifier.cnctrs.Connector;
import org.bedework.notifier.cnctrs.ConnectorInstance;
import org.bedework.notifier.exception.NoteException;
import org.bedework.util.misc.ToString;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Map;

/** Represents a subscription for the notification engine.
 *
 * <p>A subscription will need to list the source and the manner(s) of
 * notification.
 *
 * This object is the form of data stored in the db. A set of common
 * properties are represented in the usual pojo manner. All others are
 * stored as a serializable json string.
 *
 * The SubscriptionWrapper class extends this object and can be extended
 * by connectors to provide easy access to connector specific properties.
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
public class SubscriptionWrapper extends Subscription {
  private final Subscription subscription;

  /** constructor
   *
   */
  public SubscriptionWrapper(final Subscription subscription) {
    // Unwrap if wrapped.
    Subscription theSub = subscription;
    while (theSub instanceof SubscriptionWrapper) {
      theSub = ((SubscriptionWrapper)theSub).getSubscription();
    }

    this.subscription = theSub;
  }

  /**
   *
   * @return String Our subscription.
   */
  public Subscription getSubscription() {
    return subscription;
  }

  public void init(final Map vals) throws NoteException {
    getSubscription().init(vals);
  }

  /** Our generated subscriptionId.
   *
   * @param val    String
   */
  public void setSubscriptionId(final String val) {
    getSubscription().setSubscriptionId(val);
  }

  /**
   *
   * @return String Our generated subscriptionId.
   */
  public String getSubscriptionId() {
    return getSubscription().getSubscriptionId();
  }

  /**
   * @param val id
   */
  public void setConnectorName(final String val) {
    getSubscription().setConnectorName(val);
  }

  /**
   * @return id
   */
  public String getConnectorName() {
    return getSubscription().getConnectorName();
  }

  /** Principal requesting service
   *
   * @param val    String
   */
  public void setPrincipalHref(final String val) {
    getSubscription().setPrincipalHref(val);
  }

  /** Principal requesting service
   *
   * @return String
   */
  public String getPrincipalHref() {
    return getSubscription().getPrincipalHref();
  }

  /**
   *
   * @param val A UTC dtstamp value
   */
  public void setLastRefresh(final String val) {
    getSubscription().setLastRefresh(val);
  }

  /**
   * @return String lastRefresh
   */
  public String getLastRefresh() {
    return getSubscription().getLastRefresh();
  }

  /** HTTP status or other appropriate value
   * @param val the status
   */
  public void setLastRefreshStatus(final String val) {
    getSubscription().setLastRefreshStatus(val);
  }

  /**
   * @return String lastRefreshStatus
   */
  public String getLastRefreshStatus() {
    return getSubscription().getLastRefreshStatus();
  }

  /**
   *
   * @param val int consecutive errors
   */
  public void setErrorCt(final int val) {
    getSubscription().setErrorCt(val);
  }

  /**
   * @return int consecutive errors
   */
  public int getErrorCt() {
    return getSubscription().getErrorCt();
  }

  /**
   *
   * @param val True if the target is missing
   */
  public void setMissingTarget(final boolean val) {
    getSubscription().setMissingTarget(val);
  }

  /**
   * @return True if either target is missing
   */
  public boolean getMissingTarget() {
    return getSubscription().getMissingTarget();
  }

  /** Path to the notifications source - possibly located by probing
   * the principal.
   *
   * @param val    String
   */
  public void setUri(final String val) {
    getSubscription().setUri(val);
  }

  /** Path to the notifications source.
   *
   * @return String
   */
  public String getUri() {
    return getSubscription().getUri();
  }

  public void setDeleted(final boolean val) {
    getSubscription().setDeleted(val);
  }

  @JsonIgnore
  public boolean getDeleted() {
    return getSubscription().getDeleted();
  }

  /**
   * @param val a connection
   */
  public void setSourceConn(final Connector val) {
    getSubscription().setSourceConn(val);
  }

  /**
   * @return a connection or null
   */
  @JsonIgnore
  public Connector getSourceConn() {
    return getSubscription().getSourceConn();
  }

  /**
   * @param val a connection instance
   */
  public void setSourceConnInst(final ConnectorInstance val) {
    getSubscription().setSourceConnInst(val);
  }

  /**
   * @return a connection instance or null
   */
  @JsonIgnore
  public ConnectorInstance getSourceConnInst() {
    return getSubscription().getSourceConnInst();
  }

  /* ====================================================================
   *                   Convenience methods
   * ==================================================================== */

  /** Add our stuff to the StringBuilder
   *
   * @param ts    for result
   */
  protected void toStringSegment(final ToString ts) {
    super.toStringSegment(ts);
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
    return super.compareTo(that);
  }

  @Override
  public String toString() {
    ToString ts = new ToString(this);

    toStringSegment(ts);

    return ts.toString();
  }
}
