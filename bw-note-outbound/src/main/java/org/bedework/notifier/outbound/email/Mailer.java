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
import org.bedework.util.misc.Logged;

import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.Authenticator;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class Mailer extends Logged {
	EmailConf config;

  public Mailer(final EmailConf config) throws NoteException {
    super();
    this.config = config;
  }

  public void send(final BaseEmailMessage email) throws NoteException {
    Session session = getSession();

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

      if (debug) {
        debug("About to get transport");
      }
//      Transport.send(msg);
      Transport transport = session.getTransport(config.getProtocol());
      try {
        if (debug) {
          debug("About to connect");
        }

        transport.connect(/*host, from, pass*/);

        if (debug) {
          debug("About to send message");
        }

        transport.sendMessage(msg, msg.getAllRecipients());

        if (debug) {
          debug("Message sent");
        }
      } finally {
        transport.close();
      }
    } catch (final MessagingException e) {
      throw new NoteException(e);
    }
  }

  private Session getSession() throws NoteException {
    final Properties props = new Properties();

    setNonNull(props, "mail.transport.protocol", config.getProtocol());
//    setNonNull(props, "mail." + config.getProtocol() + ".class",
//               config.getProtocolClass());
    setNonNull(props, "mail." + config.getProtocol() + ".host",
               config.getServerUri());
    if (config.getServerPort() != null) {
      props.put("mail." + config.getProtocol() + ".port",
                config.getServerPort());
    }

    props.put("mail." + config.getProtocol() + ".starttls.enable",
              String.valueOf(config.getStarttls()));
    props.put("mail." + config.getProtocol() + ".connectiontimeout", 10000);
    props.put("mail." + config.getProtocol() + "smtp.timeout", 10000);

    //  add handlers for main MIME types
    final MailcapCommandMap mc = (MailcapCommandMap)CommandMap.getDefaultCommandMap();
    mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
    mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
    mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
    mc.addMailcap("text/calendar;; x-java-content-handler=com.sun.mail.handlers.text_html");
    mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
    mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
    CommandMap.setDefaultCommandMap(mc);

    final String username;
    final String pw;

    if (config.getTransientUsername() != null) {
      username = config.getTransientUsername();
      pw = config.getTransientPassword();
    } else if (config.getServerUsername() != null) {
      username = config.getServerUsername();
      pw = config.getServerPassword();
    } else {
      username = null;
      pw = null;
    }

    if (username != null) {
      // Authentication required.
      final MailerAuthenticator authenticator =
              new MailerAuthenticator(username, pw);
      props.put("mail." + config.getProtocol() + ".auth", "true");
      return Session.getInstance(props, authenticator);
    }

    return Session.getDefaultInstance(props);
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
