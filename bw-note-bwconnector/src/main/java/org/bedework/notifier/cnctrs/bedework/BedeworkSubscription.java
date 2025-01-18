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

import org.bedework.notifier.db.Subscription;
import org.bedework.notifier.db.SubscriptionImpl;
import org.bedework.notifier.db.SubscriptionWrapper;
import org.bedework.base.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Stores information about one end of a subscription for connector.
 *
 *
 * @author Mike Douglass
 */
public class BedeworkSubscription extends SubscriptionWrapper {
  /* Here we will specify what notifications the user is interested
   * in and how they are to be delivered.
   */

  private String userToken;

  // For the moment send everything by email
  private List<String> emails = new ArrayList<>();

  private String synchToken;

  private List<String> noteHrefs;

  public BedeworkSubscription() {
    super(SubscriptionImpl.make());
  }

  public BedeworkSubscription(final Subscription sub) {
    super(sub);
    init(getSubi().getVals());
  }

  @Override
  public void init(final Map vals) {
    super.init(vals);
    final SubscriptionImpl subi = getSubi();

    setUserToken(subi.must("userToken"));
    setEmails(subi.mustList("emails"));
    setSynchToken(subi.may("synchToken"));
    setNoteHrefs(subi.mayList("noteHrefs"));
  }

  /**
   *
   * @param val token for authentication
   */
  public void setUserToken(final String val) {
    userToken = val;
    getSubi().setString("userToken", userToken);
  }

  /**
   *
   * @return token for authentication
   */
  public String getUserToken() {
    return userToken;
  }

  /**
   *
   * @param val list of email addresses to which we send notifications
   */
  public void setEmails(final List<String> val) {
    emails = val;
    getSubi().setObject("emails", emails);
  }

  /**
   *
   * @return list of email addresses to which we send notifications
   */
  public List<String> getEmails() {
    if (emails == null) {
      emails = new ArrayList<>();
    }
    return emails;
  }

  public void addEmail(final String val) {
    if (!emails.contains(val)) {
      emails.add(val);
    }
  }

  /**
   *
   * @param val token for synchronization
   */
  public void setSynchToken(final String val) {
    synchToken = val;
    getSubi().setString("synchToken", synchToken);
  }

  /**
   *
   * @return token for synchronization
   */
  public String getSynchToken() {
    return synchToken;
  }

  /**
   *
   * @param val list of notification hrefs
   */
  public void setNoteHrefs(final List<String> val) {
    noteHrefs = val;
    getSubi().setObject("noteHrefs", noteHrefs);
  }

  /**
   *
   * @return list of notification hrefs
   */
  public List<String> getNoteHrefs() {
    if (noteHrefs == null) {
      noteHrefs = new ArrayList<>();
    }
    return noteHrefs;
  }

  public void toStringSegment(final ToString ts) {
    super.toStringSegment(ts);

    ts.append("userToken", getUserToken());
    ts.append("emails", getEmails());
    ts.append("synchToken", getSynchToken());
    ts.append("noteHrefs", getNoteHrefs());
  }
}
