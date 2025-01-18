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
package org.bedework.notifier.outbound.email;

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
public class EmailSubscription extends SubscriptionWrapper {
  /* Provides access to the email information for a subscription.
   */

  // For the moment send everything by email
  private List<String> emails = new ArrayList<>();

  public EmailSubscription() {
    super(SubscriptionImpl.make());
  }

  public EmailSubscription(final Subscription sub) {
    super(sub);
    init(getSubi().getVals());
  }

  public static EmailSubscription rewrap(final Subscription sub) {
    if (sub instanceof EmailSubscription) {
      return (EmailSubscription)sub;
    }

    if (sub instanceof SubscriptionWrapper) {
      return new EmailSubscription(((SubscriptionWrapper)sub).getSubscription());
    }

    return new EmailSubscription(sub);
  }

  @Override
  public void init(final Map vals) {
    super.init(vals);
    final SubscriptionImpl subi = getSubi();

    setEmails(subi.mustList("emails"));
  }

  public void setEmails(final List<String> val) {
    emails = val;
    getSubi().setObject("emails", emails);
  }

  public List<String> getEmails() {
    return emails;
  }

  public void addEmail(final String val) {
    if (!emails.contains(val)) {
      emails.add(val);
    }
  }

  public void toStringSegment(final ToString ts) {
    super.toStringSegment(ts);

    ts.append("emails", getEmails());
  }
}
