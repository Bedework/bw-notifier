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

import org.bedework.notifier.exception.NoteException;
import org.bedework.util.misc.ToString;

import java.util.Map;

/** A subscription wrapper allows us to extend the data model without
 * affecting the scheme.
 *
 * <p>A basic set of properties are set in the subscription. All other
 * properties are stored in the serializable set and accessed via the
 * wrappers which extend the subscription interface.
 *
 * <p>This class has no extra properties.
 *
 * @author Mike Douglass
 */
@SuppressWarnings("rawtypes")
public class SubscriptionWrapper implements Subscription {
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

  @Override
  public void setTransientSub(final boolean val) {
    getSubscription().setTransientSub(val);
  }

  @Override
  public boolean getTransientSub() {
    return getSubscription().getTransientSub();
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

  @Override
  public boolean reserved() {
    return getSubscription().reserved();
  }

  @Override
  public void reserve() {
    getSubscription().reserve();
  }

  @Override
  public void release() {
    getSubscription().release();
  }

  public void setDeleted(final boolean val) {
    getSubscription().setDeleted(val);
  }

  public boolean getDeleted() {
    return getSubscription().getDeleted();
  }

  /* ====================================================================
   *                   Convenience methods
   * ==================================================================== */

  /** Add our stuff to the StringBuilder
   *
   * @param ts    for result
   */
  public void toStringSegment(final ToString ts) {
    getSubscription().toStringSegment(ts);
  }

  protected SubscriptionImpl getSubi() {
    return (SubscriptionImpl)getSubscription();
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
    return getSubscription().compareTo(that);
  }

  @Override
  public String toString() {
    ToString ts = new ToString(this);

    toStringSegment(ts);

    return ts.toString();
  }
}
