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

import org.bedework.caldav.util.notifications.NotificationType;
import org.bedework.caldav.util.notifications.ProcessorType;
import org.bedework.notifier.Action;
import org.bedework.notifier.exception.NoteException;
import org.bedework.notifier.notifications.Note;
import org.bedework.notifier.outbound.common.AbstractAdaptor;
import org.bedework.util.http.HttpUtil;

import java.util.List;

import javax.xml.namespace.QName;

/** The interface implemented by destination adaptors. A destination
 * may be an email address or sms.
 * <pre>
 *
 * &lt;?xml version="1.0" encoding="UTF-8" ?&gt;
 &lt;CSS:notification xmlns:C="urn:ietf:params:xml:ns:caldav"
 xmlns:BSS="http://bedework.org/ns/"
 xmlns:BW="http://bedeworkcalserver.org/ns/"
 xmlns:CSS="http://calendarserver.org/ns/"
 xmlns:DAV="DAV:"&gt;
 &lt;BSS:processors&gt;
 &lt;BSS:processor&gt;
 &lt;BSS:type&gt;email&lt;/BSS:type&gt;
 &lt;/BSS:processor&gt;
 &lt;/BSS:processors&gt;
 &lt;CSS:dtstamp&gt;20150819T173132Z&lt;/CSS:dtstamp&gt;
 &lt;CSS:invite-notification shared-type="calendar"&gt;
 &lt;BW:name&gt;402881f4-4f470345-014f-47040de1-00000004&lt;/BW:name&gt;
 &lt;CSS:uid&gt;402881f4-4f470345-014f-47040de1-00000004&lt;/CSS:uid&gt;
 &lt;DAV:href&gt;mailto:douglm@mysite.edu&lt;/DAV:href&gt;
 &lt;CSS:invite-noresponse/&gt;
 &lt;CSS:access&gt;
 &lt;CSS:read-write/&gt;
 &lt;/CSS:access&gt;
 &lt;CSS:hosturl&gt;
 &lt;DAV:href&gt;/notifyws/user/mtwain/share&lt;/DAV:href&gt;
 &lt;/CSS:hosturl&gt;
 &lt;CSS:organizer&gt;
 &lt;DAV:href&gt;mailto:mtwain@mysite.edu&lt;/DAV:href&gt;
 &lt;CSS:common-name&gt;&lt;/CSS:common-name&gt;
 &lt;/CSS:organizer&gt;
 &lt;CSS:summary&gt;share&lt;/CSS:summary&gt;
 &lt;C:supported-calendar-component-set&gt;
 &lt;C:comp name="VEVENT"/&gt;
 &lt;C:comp name="VTODO"/&gt;
 &lt;C:comp name="VAVAILABILITY"/&gt;
 &lt;/C:supported-calendar-component-set&gt;
 &lt;/CSS:invite-notification&gt;
 &lt;/CSS:notification&gt;
 </pre>
 *
 * @author Greg Allen
 * @author Mike Douglass
 *
 */
public class EmailAdaptor extends AbstractAdaptor<EmailConf> {
  final static String processorType = "email";

  private Mailer mailer;

  @Override
  public boolean process(final Action action) throws NoteException {
    final Note note = action.getNote();
    final NotificationType nt = note.getNotification();
    final EmailSubscription sub = EmailSubscription.rewrap(action.getSub());
    final ProcessorType pt = getProcessorStatus(note, processorType);

    if (processed(pt)) {
      return true;
    }

    final EmailMessage email = new EmailMessage(conf.getFrom(), null);

    // if (note.isRegisteredRecipient()) {
    //   do one thing
    // ? else { ... }

    final QName elementName =  nt.getNotification().getElementName();
    String prefix = nt.getParsed().getDocumentElement().getPrefix();

    if (prefix == null) {
      prefix = "default";
    }

    String subject = getConfig().getSubject(prefix + "-" + elementName.getLocalPart());
    if (subject == null) {
      subject = getConfig().getDefaultSubject();
    }
    email.setSubject(subject);

    List<TemplateResult> results = applyTemplates(elementName, Note.DeliveryMethod.email, nt, note.getExtraValues());
    for (TemplateResult result : results) {
      String from = result.getStringVariable("from");
      if (from != null) {
        email.setFrom(from);
      }

      for (final String to: result.getListVariable("to")) {
        email.addTo(to);
      }

      for (final String cc: result.getListVariable("cc")) {
        email.addCc(cc);
      }

      for (final String bcc: result.getListVariable("bcc")) {
        email.addBcc(bcc);
      }

      subject = result.getStringVariable("subject");
      if (subject != null) {
        email.setSubject(subject);
      }

      String contentType = result.getStringVariable("contentType");
      if (contentType == null) {
        contentType = EmailMessage.CONTENT_TYPE_PLAIN;
      }
      email.addBody(contentType, result.getValue());
    }

    if (email.getTos().size() == 0) {
            /* The subscription will define one or more recipients */
      for (final String emailAddress: sub.getEmails()) {
        email.addTo(stripMailTo(emailAddress));
      }

    }
    try {
      if (email.getBodies().keySet().size() > 0) {
        // No template results returned, don't email but still return success.
        getMailer().send(email);

        pt.setDtstamp(getDtstamp());
        pt.setStatus(HttpUtil.makeOKHttpStatus());
      }
      return true;
    } catch (final NoteException ne) {
      if (debug) {
        error(ne);
      }
    }
    return false;
  }

  private Mailer getMailer() throws NoteException {
    if (mailer == null) {
      mailer = new Mailer(getConfig());
    }
    return mailer;
  }
}
