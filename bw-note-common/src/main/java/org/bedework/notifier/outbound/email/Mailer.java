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

import org.bedework.notifier.exception.NoteException;

import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class Mailer {
	EmailAdaptorConfig config;
	Session session;

  public Mailer(final EmailAdaptorConfig config) throws NoteException {
    this.config = config;

    final Properties props = new Properties();

    setNonNull(props, "mail.transport.protocol", config.getProtocol());
    setNonNull(props, "mail." + config.getProtocol() + ".class",
               config.getProtocolClass());
    setNonNull(props, "mail." + config.getProtocol() + ".host",               config.getServerUri());
    if (config.getServerPort() != null) {
      props.put("mail." + config.getProtocol() + ".port",
                config.getServerPort());
    }

    //  add handlers for main MIME types
    final MailcapCommandMap mc = (MailcapCommandMap)CommandMap.getDefaultCommandMap();
    mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
    mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
    mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
    mc.addMailcap("text/calendar;; x-java-content-handler=com.sun.mail.handlers.text_html");
    mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
    mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
    CommandMap.setDefaultCommandMap(mc);

    if (config.getServerUsername() != null) {
      // Authentication required.
      final MailerAuthenticator authenticator =
              new MailerAuthenticator(config.getServerUsername(), config.getServerPassword());
      props.put("mail." + config.getProtocol() + ".auth", "true");
      session = Session.getInstance(props, authenticator);
    } else {
      session = Session.getInstance(props);
    }
  }

  public void send(final BaseEmailMessage email) throws NoteException {
    final MimeMessage msg = new MimeMessage(session);
    try {
      if (email.getFrom() == null) {
        msg.setFrom(new InternetAddress(config.getFrom()));
      } else {
        msg.setFrom(new InternetAddress(email.getFrom()));
      }

      for (final String to : email.getTos()) {
        msg.addRecipient(RecipientType.TO, new InternetAddress(to));
      }
      msg.setSubject(email.getSubject());

      final MimeMultipart multipart = new MimeMultipart("alternative");
      for (final String type : email.getBodies().keySet()) {
        final MimeBodyPart part = new MimeBodyPart();
        part.setContent(email.getBodies().get(type), type);
        multipart.addBodyPart(part);
      }
      msg.setContent(multipart);

      Transport.send(msg);
    } catch (final MessagingException e) {
      throw new NoteException(e);
    }
  }

  private void setNonNull(final Properties props,
                          final String name,
                          final String val) throws NoteException {
    if (val == null) {
      throw new NoteException("Null property value for " + name);
    }

    props.setProperty(name, val);
  }

  private class MailerAuthenticator extends Authenticator {
    private final PasswordAuthentication authentication;

    MailerAuthenticator(final String user, final String password) {
      authentication = new PasswordAuthentication(user, password);
    }

    protected PasswordAuthentication getPasswordAuthentication() {
      return authentication;
    }
  }
}
