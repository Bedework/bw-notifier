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

import org.bedework.util.misc.ToString;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.UUID;

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
public class SubscriptionImpl extends DbItem<Subscription>
        implements Subscription {
  private String subscriptionId;

  private boolean transientSub;

  private String connectorName;

  private String principalHref;

  private String lastRefresh;

  private String lastRefreshStatus;

  private int errorCt;

  private boolean missingTarget;

  private String uri;

  /* Following not persisted */

  private boolean reserved;

  private boolean deleted;

  /** null constructor for hibernate
   *
   */
  public SubscriptionImpl() {
  }

  /** Factory method
   *
   */
  public static SubscriptionImpl make() {
    final SubscriptionImpl si = new SubscriptionImpl();

    si.setSubscriptionId(UUID.randomUUID().toString());

    return si;
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

  /** Transient subscriptions are deleted at startup. Used for
   * special subscriptions.
   *
   * @param val True if this is a transient subscription
   */
  public void setTransientSub(final boolean val) {
    transientSub = val;
  }

  /** Transient subscriptions are deleted at startup. Used for
   * special subscriptions.
   *
   * @return True if this is a transient subscription
   */
  public boolean getTransientSub() {
    return transientSub;
  }

  /**
   * @param val name
   */
  public void setConnectorName(final String val) {
    connectorName = val;
  }

  /**
   * @return name
   */
  public String getConnectorName() {
    return connectorName;
  }

  /** Principal requesting service
   *
   * @param val    String
   */
  public void setPrincipalHref(final String val) {
    principalHref = val;
  }

  /** Principal requesting service
   *
   * @return String
   */
  public String getPrincipalHref() {
    return principalHref;
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

  /** HTTP status or other appropriate value
   * @param val the status
   */
  public void setLastRefreshStatus(final String val) {
    lastRefreshStatus = val;
  }

  /**
   * @return String lastRefreshStatus
   */
  public String getLastRefreshStatus() {
    return lastRefreshStatus;
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

  /** Path to the notifications source - possibly located by probing
   * the principal.
   *
   * @param val    String
   */
  public void setUri(final String val) {
    uri = val;
  }

  /** Path to the notifications source.
   *
   * @return String
   */
  public String getUri() {
    return uri;
  }

  @Override
  public boolean reserved() {
    return reserved;
  }

  @Override
  public void reserve() {
    reserved = true;
  }

  @Override
  public void release() {
    reserved = false;
  }

  public void setDeleted(final boolean val) {
    deleted = val;
  }

  @JsonIgnore
  public boolean getDeleted() {
    return deleted;
  }

  /* ====================================================================
   *                   Convenience methods
   * ==================================================================== */

  /** Add our stuff to the StringBuilder
   *
   * @param ts    for result
   */
  public void toStringSegment(final ToString ts) {
    super.toStringSegment(ts);
    ts.append("reserved", reserved());

    ts.newLine();
    ts.append("subscriptionId", getSubscriptionId());
    ts.append("transientSub", getTransientSub());
    ts.append("connectorName", getConnectorName());
    ts.append("principalHref", getPrincipalHref());

    ts.append("lastRefresh", getLastRefresh());
    ts.append("lastRefreshStatus", getLastRefreshStatus());

    ts.newLine();
    ts.append("errorCt", getErrorCt());
    ts.append("missingTarget", getMissingTarget());
    ts.append("uri", getUri());
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
    final ToString ts = new ToString(this);

    toStringSegment(ts);

    return ts.toString();
  }
}
