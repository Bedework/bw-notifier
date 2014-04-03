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

import java.util.Locale;
import java.util.ResourceBundle;

import org.bedework.caldav.util.sharing.InviteNotificationType;
import org.bedework.caldav.util.sharing.OrganizerType;
import org.bedework.notifier.exception.NoteException;
import org.bedework.notifier.notifications.AppleNotification;
import org.bedework.notifier.notifications.Notification;
import org.bedework.notifier.outbound.common.AbstractAdaptor;
import org.bedework.notifier.outbound.common.AdaptorConfig;

/** The interface implemented by destination adaptors. A destination
 * may be an email address or sms.
 *
 * @author Mike Douglass
 *
 */
public class EmailAdaptor extends AbstractAdaptor {
	
	private static final String BUNDLE_NAME = "org.bedework.notifier.outbound.email.notifications";
	private static final String KEY_SHARE_EXTERNAL_SUBJECT = "share.invitation.external.subject";
	private static final String KEY_SHARE_EXTERNAL_TEXT = "share.invitation.external.text";
	private static final String KEY_SHARE_EXTERNAL_HTML = "share.invitation.external.html";
	private static final String KEY_SHARE_INTERNAL_SUBJECT = "share.invitation.internal.subject";
	private static final String KEY_SHARE_INTERNAL_TEXT = "share.invitation.internal.text";
	private static final String KEY_SHARE_INTERNAL_HTML = "share.invitation.internal.html";
	
	private static ResourceBundle bundle;
	
	private Mailer mailer;
	
	@Override
	public void setConf(AdaptorConfig config) {
		super.setConf(config);
		getResourceBundle();
	}

	@Override
	public boolean processSharingInvitation(Notification note) throws NoteException {
		InviteNotificationType invite = (InviteNotificationType) note.getNotification();
		EmailMessage email = new EmailMessage(stripMailTo(invite.getOrganizer().getHref()), null);
		
		email.addTo(stripMailTo(invite.getHref()));
		if (note.isRegisteredRecipient()) {
			info("Inviting registered user " + invite.getHref() + ":\n" + invite);
			email.setSubject(bundle.getString(KEY_SHARE_INTERNAL_SUBJECT));
			if (bundle.containsKey(KEY_SHARE_INTERNAL_TEXT)) {
				email.addBody(EmailMessage.CONTENT_TYPE_PLAIN, bundle.getString(KEY_SHARE_INTERNAL_TEXT));
			}
			if (bundle.containsKey(KEY_SHARE_INTERNAL_HTML)) {
				email.addBody(EmailMessage.CONTENT_TYPE_HTML, bundle.getString(KEY_SHARE_INTERNAL_HTML));
			}
		} else {
//			info("Inviting unregistered user " + invite.getHref() + ":\n" + invite);			
			email.setSubject(bundle.getString(KEY_SHARE_EXTERNAL_SUBJECT));
			if (bundle.containsKey(KEY_SHARE_EXTERNAL_TEXT)) {
				email.addBody(EmailMessage.CONTENT_TYPE_PLAIN, bundle.getString(KEY_SHARE_EXTERNAL_TEXT));
			}
			if (bundle.containsKey(KEY_SHARE_EXTERNAL_HTML)) {
				email.addBody(EmailMessage.CONTENT_TYPE_HTML, bundle.getString(KEY_SHARE_EXTERNAL_HTML));
			}
		}
		
		getMailer().send(email);
		return false;
	}

	@Override
	public boolean processSubscribeInvitation(Notification note) throws NoteException {
		info("Call to processSubscribeInvitation: " + note);
		return false;
	}

	@Override
	public boolean processResourceChange(Notification note) throws NoteException {
		info("Call to processResourceChange: " + note);
		return false;
	}
	
	public EmailAdaptorConfig getConf() {
		return (EmailAdaptorConfig)super.getConf();
	}
	
	private ResourceBundle getResourceBundle() {
		if (bundle == null) {
			EmailAdaptorConfig conf = (EmailAdaptorConfig)getConf();
			if (conf.getLocale() == null) {
				bundle = ResourceBundle.getBundle(BUNDLE_NAME);
			} else {
				bundle = ResourceBundle.getBundle(BUNDLE_NAME, new Locale(conf.getLocale()));
			}
		}
		return bundle;
	}
	
	private Mailer getMailer() {
		if (mailer == null) {
			mailer = new Mailer(getConf());
		}
		return mailer;
	}
	
	public static void main(String[] args) {
		InviteNotificationType invite = new InviteNotificationType();
		invite.setHref("mailto:gallen@mycalet.com");
		OrganizerType organizer = new OrganizerType();
		organizer.setHref("mailto:support@mycalet.com");
		invite.setOrganizer(organizer);
		
		EmailAdaptorConfig config = new EmailAdaptorConfig();
		config.setLocale("en_US");
		config.setProtocol("smtp");
		config.setServerPassword("c@lend@r");
		config.setServerPort("587");
		config.setServerUri("mail.mycalet.com");
		config.setServerUsername("root");
		config.setProtocolClass("com.sun.mail.smtp.SMTPTransport");
		
		AppleNotification note = new AppleNotification(invite);
		EmailAdaptor adapter = new EmailAdaptor();
		try {
			adapter.setConf(config);
			adapter.process(note);
		} catch (NoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
