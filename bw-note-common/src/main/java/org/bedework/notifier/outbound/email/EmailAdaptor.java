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

import org.bedework.caldav.util.notifications.ProcessorType;
import org.bedework.caldav.util.sharing.InviteNotificationType;
import org.bedework.notifier.NotifyEngine;
import org.bedework.notifier.exception.NoteException;
import org.bedework.notifier.notifications.Note;
import org.bedework.notifier.outbound.common.AbstractAdaptor;
import org.bedework.util.http.HttpUtil;

import java.util.ResourceBundle;

/** The interface implemented by destination adaptors. A destination
 * may be an email address or sms.
 *
 * @author Greg Allen
 *
 */
public class EmailAdaptor extends AbstractAdaptor {

	private static final String BUNDLE_NAME = "adaptors.EmailBundle";
	private static final String KEY_SHARE_EXTERNAL_SUBJECT = "share.invitation.external.subject";
	private static final String KEY_SHARE_EXTERNAL_TEXT = "share.invitation.external.text";
	private static final String KEY_SHARE_EXTERNAL_HTML = "share.invitation.external.html";
	private static final String KEY_SHARE_INTERNAL_SUBJECT = "share.invitation.internal.subject";
	private static final String KEY_SHARE_INTERNAL_TEXT = "share.invitation.internal.text";
	private static final String KEY_SHARE_INTERNAL_HTML = "share.invitation.internal.html";

	private static ResourceBundle bundle;

	private Mailer mailer;

	@Override
	public boolean processSharingInvitation(final Note note) throws NoteException {
    final ProcessorType pt = getProcessorStatus(note);

    if (processed(pt)) {
      return true;
    }
    final InviteNotificationType invite =
            (InviteNotificationType)note.getNotificationContent();

    final EmailMessage email = new EmailMessage(stripMailTo(invite.getOrganizer().getHref()), null);

		email.addTo(stripMailTo(invite.getHref()));

    final ResourceBundle bundle = getResourceBundle();

		if (note.isRegisteredRecipient()) {
			info("Inviting registered user " + invite.getHref() + ":\n" + invite);
			email.setSubject(bundle.getString(KEY_SHARE_INTERNAL_SUBJECT));
			if (bundle.containsKey(KEY_SHARE_INTERNAL_TEXT)) {
				email.addBody(EmailMessage.CONTENT_TYPE_PLAIN,
                      bundle.getString(KEY_SHARE_INTERNAL_TEXT));
			}
			if (bundle.containsKey(KEY_SHARE_INTERNAL_HTML)) {
				email.addBody(EmailMessage.CONTENT_TYPE_HTML,
                      bundle.getString(KEY_SHARE_INTERNAL_HTML));
			}
		} else {
//			info("Inviting unregistered user " + invite.getHref() + ":\n" + invite);
			email.setSubject(bundle.getString(KEY_SHARE_EXTERNAL_SUBJECT));
			if (bundle.containsKey(KEY_SHARE_EXTERNAL_TEXT)) {
				email.addBody(EmailMessage.CONTENT_TYPE_PLAIN,
                      bundle.getString(KEY_SHARE_EXTERNAL_TEXT));
			}
			if (bundle.containsKey(KEY_SHARE_EXTERNAL_HTML)) {
				email.addBody(EmailMessage.CONTENT_TYPE_HTML,
                      bundle.getString(KEY_SHARE_EXTERNAL_HTML));
			}
		}

		getMailer().send(email);

    pt.setDtstamp(getDtstamp());
    pt.setStatus(HttpUtil.makeOKHttpStatus());

		return true;
	}

	@Override
	public boolean processSubscribeInvitation(final Note note) throws NoteException {
		info("Call to processSubscribeInvitation: " + note);
		return false;
	}

	@Override
	public boolean processResourceChange(final Note note) throws NoteException {
		info("Call to processResourceChange: " + note);
		return false;
	}

	public EmailAdaptorConfig getConfig() {
		return (EmailAdaptorConfig)super.getConfig();
	}

	private ResourceBundle getResourceBundle() throws NoteException {
    if (bundle != null) {
      return bundle;
    }

    try {
      bundle = NotifyEngine.getConfigStore().
              getResource(BUNDLE_NAME,
                          getConfig().getLocale());
    } catch (final Throwable t) {
      throw new NoteException(t);
    }

    return bundle;
	}

  private Mailer getMailer() throws NoteException {
		if (mailer == null) {
			mailer = new Mailer(getConfig());
		}
		return mailer;
	}

  /* Not sure if this can work now we have updates
	public static void main(final String[] args) {
    final InviteNotificationType invite = new InviteNotificationType();
		invite.setHref("mailto:gallen@mycalet.com");
    final OrganizerType organizer = new OrganizerType();
		organizer.setHref("mailto:support@mycalet.com");
		invite.setOrganizer(organizer);

    final EmailAdaptorConfig config = new EmailAdaptorConfig();
		config.setLocale("en_US");
		config.setProtocol("smtp");
		config.setServerPassword("c@lend@r");
		config.setServerPort("587");
		config.setServerUri("mail.mycalet.com");
		config.setServerUsername("root");
		config.setProtocolClass("com.sun.mail.smtp.SMTPTransport");

    final NotificationType notification = new NotificationType();
    notification.setNotification(invite);

    final AppleNotification note = new AppleNotification(notification);
    final EmailAdaptor adapter = new EmailAdaptor();
		try {
			adapter.setConf(config);
			adapter.process(note);
		} catch (final NoteException e) {
			//  Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/
}
