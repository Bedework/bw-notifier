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
import org.bedework.notifier.db.SubscriptionWrapper;
import org.bedework.notifier.exception.NoteException;
import org.bedework.util.misc.ToString;

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

  // For the moment send everything by email
  private List<String> emails = new ArrayList<>();

  public BedeworkSubscription() throws NoteException {
    super(new Subscription());
  }

  public BedeworkSubscription(final Subscription sub) throws NoteException {
    super(sub);
  }

  @Override
  public void init(final Map vals) throws NoteException {
    super.init(vals);

    setEmails(mustList("emails"));
  }

  public void setEmails(final List<String> val) {
    emails = val;
  }

  public List<String> getEmails() {
    return emails;
  }

  public void addEmail(final String val) {
    if (!emails.contains(val)) {
      emails.add(val);
    }
  }

  protected void toStringSegment(final ToString ts) {
    super.toStringSegment(ts);

    ts.append("emails", getEmails());
  }
}
