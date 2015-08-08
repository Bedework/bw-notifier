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

import org.bedework.notifier.NotifyRegistry;
import org.bedework.notifier.exception.NoteException;
import org.bedework.util.misc.ToString;

import java.util.Map;

/** Serializable form of information for a connection to a system via a
 * connector - a connector id and the serialized properties.
 *
 * @author douglm
 */
public class SubscriptionConnectorInfo extends SerializableProperties
    implements Comparable<SubscriptionConnectorInfo> {
  private String connectorName;

  private String type;

  private String uri;

  private String principalHref;

  private String lastRefreshStatus;

  public SubscriptionConnectorInfo() {
  }

  @Override
  public void init(final Map vals) throws NoteException {
    super.init(vals);

    setConnectorName(must("connectorName"));
    setType(must("type"));
    setUri(must("uri"));
    setPrincipalHref(must("principalHref"));
    setLastRefreshStatus(may("lastRefreshStatus"));
  }

  /**
   * @param val id
   */
  public void setConnectorName(final String val) {
    connectorName = val;
  }

  /**
   * @return id
   */
  public String getConnectorName() {
    return connectorName;
  }

  /** Type of connector.
   *
   * @param val    String
   * @throws NoteException
   */
  public void setType(final String val) throws NoteException {
    type = val;
  }

  /** Type of connector.
   *
   * @return String
   * @throws NoteException
   */
  public String getType() throws NoteException {
    return type;
  }

  /** Path to the notifications source.
   *
   * @param val    String
   * @throws NoteException
   */
  public void setUri(final String val) throws NoteException {
    uri = val;
  }

  /** Path to the notifications source.
   *
   * @return String
   * @throws NoteException
   */
  public String getUri() throws NoteException {
    return uri;
  }

  /** Principal requesting service
   *
   * @param val    String
   * @throws NoteException
   */
  public void setPrincipalHref(final String val) throws NoteException {
    principalHref = val;
  }

  /** Principal requesting service
   *
   * @return String
   * @throws NoteException
   */
  public String getPrincipalHref() throws NoteException {
    return principalHref;
  }

  /** HTTP status or other appropriate value
   * @param val
   * @throws NoteException
   */
  public void setLastRefreshStatus(final String val) throws NoteException {
    lastRefreshStatus = val;
  }

  /**
   * @return String lastRefreshStatus
   * @throws NoteException
   */
  public String getLastRefreshStatus() throws NoteException {
    return lastRefreshStatus;
  }

  /* ====================================================================
   *                   Deserialization methods
   * ==================================================================== */

  public static SubscriptionConnectorInfo getInfo(final String type,
                                                  final Map vals) throws NoteException {
    final NotifyRegistry.Info info = NotifyRegistry.getInfo(type);

    if (info == null) {
      throw new NoteException("Unhandled type " + type);
    }

    SubscriptionConnectorInfo connInfo;
    try {
      connInfo = info.getConnectorInfoClass().newInstance();
    } catch (final Throwable t) {
      throw new NoteException(t);
    }

    connInfo.init(vals);

    return connInfo;
  }

  /* ====================================================================
   *                   Convenience methods
   * ==================================================================== */

  /* ====================================================================
   *                   Object methods
   * ==================================================================== */

  @Override
  public int hashCode() {
    try {
      return getConnectorName().hashCode() * super.hashCode();
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  @Override
  public int compareTo(final SubscriptionConnectorInfo that) {
    if (this == that) {
      return 0;
    }

    try {
      return getConnectorName().compareTo(that.getConnectorName());
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  @Override
  public String toString() {
    try {
      ToString ts = new ToString(this);

      ts.append("connectorName", getConnectorName());

      //super.toStringSegment(ts);

      return ts.toString();
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

}
