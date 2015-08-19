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
public interface Subscription extends Comparable<Subscription> {
  /**
   *
   * @param vals from json
   * @throws NoteException
   */
  void init(Map vals) throws NoteException;

  /** Our generated subscriptionId.
   *
   * @param val    String
   */
  void setSubscriptionId(String val);

  /**
   *
   * @return String Our generated subscriptionId.
   */
  String getSubscriptionId();

  /** Transient subscriptions are deleted at startup. Used for
   * special subscriptions.
   *
   * @param val True if this is a transient subscription
   */
  void setTransientSub(boolean val);

  /** Transient subscriptions are deleted at startup. Used for
   * special subscriptions.
   *
   * @return True if this is a transient subscription
   */
  boolean getTransientSub();

  /**
   * @param val name
   */
  void setConnectorName(String val);

  /**
   * @return name
   */
  String getConnectorName();

  /** Principal requesting service
   *
   * @param val    String
   */
  void setPrincipalHref(String val);

  /** Principal requesting service
   *
   * @return String
   */
  String getPrincipalHref();

  /**
   *
   * @param val A UTC dtstamp value
   */
  void setLastRefresh(String val);

  /**
   * @return String lastRefresh
   */
  String getLastRefresh();

  /** HTTP status or other appropriate value
   * @param val the status
   */
  void setLastRefreshStatus(String val);

  /**
   * @return String lastRefreshStatus
   */
  String getLastRefreshStatus();

  /**
   *
   * @param val int consecutive errors
   */
  void setErrorCt(int val);

  /**
   * @return int consecutive errors
   */
  int getErrorCt();

  /**
   *
   * @param val True if the target is missing
   */
  void setMissingTarget(boolean val);

  /**
   * @return True if either target is missing
   */
  boolean getMissingTarget();

  /** Path to the notifications source - possibly located by probing
   * the principal.
   *
   * @param val    String
   */
  void setUri(String val);

  /** Path to the notifications source.
   *
   * @return String
   */
  String getUri();

  void setDeleted(boolean val);

  @JsonIgnore
  boolean getDeleted();

  /** Add our stuff to the StringBuilder
   *
   * @param ts    for result
   */
  void toStringSegment(ToString ts);
}
