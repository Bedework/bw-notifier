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

import org.bedework.notifier.db.SubscriptionConnectorInfo;
import org.bedework.notifier.exception.NoteException;
import org.bedework.util.misc.ToString;

/** Provides an internal deserialized view of the subscription info for one
 * end. A number of common methods are provided here and it is assumed that
 * connectors and instances will subclass this class.
 *
 * @author Mike Douglass
 */
public class BaseSubscriptionInfo {
  private SubscriptionConnectorInfo info;

  /* properties saved by connector instance */

  /** A uri referring to the endpoint. Perhaps the URL of a file or the href
   * for a caldav collection
   */
  public static final String propnameUri = "uri";

  /** The value of some sort of change token. This could be a caldav ctag or an
   * http etag.
   */
  public static final String propnameChangeToken = "ctoken";

  /** A principal - possibly an href or an account */
  public static final String propnamePrincipal = "principal";

  /** The (encoded) password for the principal */
  public static final String propnamePassword = "password";

  /** Refresh period for polling subscriptions (millisecs) */
  public static final String propnameRefreshDelay = "refreshDelay";

  /** A string value that provides information about the last refresh for this
   * end of the subscription
   */
  public static final String propnameLastRefreshStatus = "last-refresh-status";

  /** The numbers created, updated, deleted last time */
  public static final String propnameLastCrudCts = "lastCrudCt";

  /** The numbers created, updated, deleted this subscription */
  public static final String propnameTotalCrudCts = "totalCrudCt";

  /** maintain some counts
   */
  public static class CrudCts {
    /** The counts
     */
    public long created;
    /** The counts
     */
    public long updated;
    /** The counts
     */
    public long deleted;

    /** Deserialize
     * @param val
     * @return cts
     */
    public static CrudCts fromString(final String val) {
      CrudCts cc = new CrudCts();

      if (val == null) {
        return cc;
      }

      String[] cts = val.split(",");

      if (cts.length != 3) {
        return cc;
      }

      try {
        cc.created = Long.valueOf(cts[0]);
        cc.updated = Long.valueOf(cts[1]);
        cc.deleted = Long.valueOf(cts[2]);
      } catch (Throwable t) {
      }

      return cc;
    }

    @Override
    public String toString() {
      return new StringBuilder().append(created).append(",").
          append(deleted).append(",").append(updated).toString();
    }
  }

  /**
   * @param info
   * @throws NoteException
   */
  public BaseSubscriptionInfo(final SubscriptionConnectorInfo info) throws NoteException {
    this.info = info;
    info.loadProperties();
  }

  /** Path to the calendar collection.
   *
   * @param val    String
   * @throws NoteException
   */
  public void setUri(final String val) throws NoteException {
    info.setProperty(propnameUri, val);
  }

  /** Path to the calendar collection
   *
   * @return String
   * @throws NoteException
   */
  public String getUri() throws NoteException {
    return info.getProperty(propnameUri);
  }

  /** Principal requesting synch service
   *
   * @param val    String
   * @throws NoteException
   */
  public void setPrincipalHref(final String val) throws NoteException {
    info.setProperty(propnamePrincipal, val);
  }

  /** Principal requesting synch service
   *
   * @return String
   * @throws NoteException
   */
  public String getPrincipalHref() throws NoteException {
    return info.getProperty(propnamePrincipal);
  }

  /** Principals password
   *
   * @param val    String
   * @throws NoteException
   */
  public void setPassword(final String val) throws NoteException {
    info.setProperty(propnamePassword, val);
  }

  /** Principal password
   *
   * @return String
   * @throws NoteException
   */
  public String getPassword() throws NoteException {
    return info.getProperty(propnamePassword);
  }

  /** ChangeToken
   *
   * @param val    String
   * @throws NoteException
   */
  public void setChangeToken(final String val) throws NoteException {
    info.setProperty(propnameChangeToken, val);
  }

  /** ChangeToken
   *
   * @return String
   * @throws NoteException
   */
  public String getChangeToken() throws NoteException {
    return info.getProperty(propnameChangeToken);
  }

  /** HTTP status or other appropriate value
   * @param val
   * @throws NoteException
   */
  public void setLastRefreshStatus(final String val) throws NoteException {
    info.setProperty(propnameLastRefreshStatus, val);
  }

  /**
   * @return String lastRefreshStatus
   * @throws NoteException
   */
  public String getLastRefreshStatus() throws NoteException {
    return info.getProperty(propnameLastRefreshStatus);
  }

  /**
   * @param val
   * @throws NoteException
   */
  public void setLastCrudCts(final CrudCts val) throws NoteException {
    info.setProperty(propnameLastCrudCts, val.toString());
  }

  /**
   * @return cts
   * @throws NoteException
   */
  public CrudCts getLastCrudCts() throws NoteException {
    String s = info.getProperty(propnameLastCrudCts);


    CrudCts cc = CrudCts.fromString(s);

    if (s == null) {
      setLastCrudCts(cc); // Ensure they get saved
    }

    return cc;
  }

  /**
   * @param val
   * @throws NoteException
   */
  public void setTotalCrudCts(final CrudCts val) throws NoteException {
    info.setProperty(propnameTotalCrudCts, val.toString());
  }

  /**
   * @return cts
   * @throws NoteException
   */
  public CrudCts getTotalCrudCts() throws NoteException {
    String s = info.getProperty(propnameTotalCrudCts);


    CrudCts cc = CrudCts.fromString(s);

    if (s == null) {
      setTotalCrudCts(cc); // Ensure they get saved
    }

    return cc;
  }

  /** Refresh delay - millisecs
   *
   * @param val
   * @throws NoteException
   */
  public void setRefreshDelay(final String val) throws NoteException {
    info.setProperty(propnameRefreshDelay, val);
  }

  /** Refresh delay - millisecs
   *
   * @return String refreshDelay
   * @throws NoteException
   */
  public String getRefreshDelay() throws NoteException {
    return info.getProperty(propnameRefreshDelay);
  }

  /** set arbitrary named property
   * @param name
   * @param val - String property value
   * @throws NoteException
   */
  public void setProperty(final String name, final String val) throws NoteException {
    info.setProperty(name, val);
  }

  /** Get arbitrary named property
   * @param name
   * @return String property value
   * @throws NoteException
   */
  public String getProperty(final String name) throws NoteException {
    return info.getProperty(name);
  }

  /* ====================================================================
   *                   Convenience methods
   * ==================================================================== */

  protected void toStringSegment(final ToString ts) {
    try {
      ts.append("uri", getUri());

      ts.newLine();
      ts.append("principalHref", getPrincipalHref());
      ts.append("password", getPassword());
      ts.append("changeToken", getChangeToken());

      ts.newLine();
      ts.append("lastRefreshStatus", getLastRefreshStatus());

      ts.newLine();
      ts.append("lastCrudCts", getLastCrudCts());
      ts.append("totalCrudCts", getTotalCrudCts());
      ts.append("refreshDelay", getRefreshDelay());
    } catch (final Throwable t) {
      ts.append("Exception", t.getLocalizedMessage());
    }
  }

  /* ====================================================================
   *                        Object methods
   * ==================================================================== */

  @Override
  public String toString() {
    ToString ts = new ToString(this);

    toStringSegment(ts);

    return ts.toString();
  }
}
