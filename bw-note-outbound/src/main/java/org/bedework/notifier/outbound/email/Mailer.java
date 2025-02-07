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
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;

import java.util.Properties;

import jakarta.activation.CommandMap;
import jakarta.activation.MailcapCommandMap;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

public class Mailer implements Logged {
  EmailConf config;

  public Mailer(final EmailConf config) {
    super();
    this.config = config;
  }

  public void send(final BaseEmailMessage email) {
    final Session session = getSession();

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
      for (final String cc : email.getCcs()) {
        msg.addRecipient(RecipientType.CC, new InternetAddress(cc));
      }
      for (final String bcc : email.getBccs()) {
        msg.addRecipient(RecipientType.BCC, new InternetAddress(bcc));
      }
      msg.setSubject(email.getSubject());

      final MimeMultipart multipart = new MimeMultipart("alternative");
      for (final String type : email.getBodies().keySet()) {
        final MimeBodyPart part = new MimeBodyPart();
        part.setContent(email.getBodies().get(type), type);
        multipart.addBodyPart(part);
      }
      msg.setContent(multipart);

      if (debug()) {
        debug("About to get transport");
      }
      try (final Transport transport =
                   session.getTransport(config.getProtocol())) {
        if (debug()) {
          debug("About to connect");
        }

        String username = null;
        String pw = null;

        if (config.getTransientUsername() != null) {
          username = config.getTransientUsername();
          pw = config.getTransientPassword();
        } else if (config.getServerUsername() != null) {
          username = config.getServerUsername();
          pw = config.getServerPassword();
        }

        if (username != null && pw != null) {
          if (config.getServerUri() != null) {
            if (config.getServerPort() != null) {
              transport.connect(config.getServerUri(),
                                Integer.parseInt(
                                        config.getServerPort()),
                                username, pw);
            } else {
              transport.connect(config.getServerUri(), username, pw);
            }
          } else {
            transport.connect(username, pw);
          }
        } else {
          // No other connect methods with any arguments.
          transport.connect();
        }

        if (debug()) {
          debug("About to send message");
        }

        transport.sendMessage(msg, msg.getAllRecipients());

        if (debug()) {
          debug("Message sent");
        }
      }
      if (debug()) {
        debug("Message sent");
      }
    } catch (final MessagingException e) {
      throw new NoteException(e);
    }
  }

  private Session getSession() {
    final Properties props = new Properties();

    //  add handlers for main MIME types
    final MailcapCommandMap mc = (MailcapCommandMap)CommandMap.getDefaultCommandMap();
    mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
    mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
    mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
    mc.addMailcap("text/calendar;; x-java-content-handler=com.sun.mail.handlers.text_html");
    mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
    mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
    CommandMap.setDefaultCommandMap(mc);

    return Session.getDefaultInstance(props);
  }

  /* ==============================================================
   *                   Logged methods
   * ============================================================== */

  private final BwLogger logger = new BwLogger();

  @Override
  public BwLogger getLogger() {
    if ((logger.getLoggedClass() == null) && (logger.getLoggedName() == null)) {
      logger.setLoggedClass(getClass());
    }

    return logger;
  }
}
