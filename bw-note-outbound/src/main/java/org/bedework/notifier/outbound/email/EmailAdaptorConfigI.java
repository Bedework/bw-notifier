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

import org.bedework.notifier.outbound.common.AdaptorConfigI;
import org.bedework.util.config.ConfInfo;
import org.bedework.util.jmx.MBeanInfo;

import java.util.List;

/** Bedework mail adaptor config interface
 *
 * @author douglm
 */
@ConfInfo(elementName = "notify-adaptor")
public interface EmailAdaptorConfigI extends AdaptorConfigI {
  /**
   * valid protocol for which an implementation exists, e.g "imap",
   * "smtp"
   *
   * @param val the protocol
   */
  void setProtocol(String val);

  /**
   * @return String
   */
  @MBeanInfo("valid protocol for which an implementation exists, e.g \"imap\", \"smtp\".")
  String getProtocol();

  /**
   * Where we send it.
   *
   * @param val uri
   */
  void setServerUri(String val);

  /**
   * @return String
   */
  @MBeanInfo("Location of server.")
  String getServerUri();

  /**
   * @param val port
   */
  void setServerPort(String val);

  /**
   * @return String
   */
  @MBeanInfo("The server port.")
  String getServerPort();

  /**
   * @param val start tls
   */
  void setStarttls(boolean val);

  /**
   * @return flag
   */
  @MBeanInfo("Starttls?")
  boolean getStarttls();

  /**
   * @param val server Username
   */
  void setServerUsername(String val);

  @MBeanInfo("User name if authentication is required.")
  String getServerUsername();

  /**
   * @param val server Password
   */
  void setServerPassword(String val);

  /**
   * @return password
   */
  @MBeanInfo("User password if authentication is required.")
  String getServerPassword();

  /**
   * Address we use when none supplied
   *
   * @param val from for message
   */
  void setFrom(String val);

  /**
   * @return String
   */
  @MBeanInfo("Address we use when none supplied.")
  String getFrom();

  /**
   * @param val for messages
   */
  void setLocale(String val);

  /**
   * @return String
   */
  @MBeanInfo("Local to user for adaptor emails.")
  String getLocale();

  /**
   * Address we use when none supplied
   *
   * @param val from for message
   */
  void setDefaultSubject(String val);

  /**
   * @return String
   */
  @MBeanInfo("Subject we use when none supplied.")
  String getDefaultSubject();

  void setSubjects(List<String> val);

  List<String> getSubjects();

  /** Add a subject
   *
   * @param name of notification
   * @param val subject
   */
  void addSubject(String name,
                  String val);

  /** Get a subject
   *
   * @param name of notification
   * @return value or null
   */
  @ConfInfo(dontSave = true)
  String getSubject(String name);

  /** Remove a subject
   *
   * @param name of notification
   */
  void removeSubject(String name);

  /** Set a subject
   *
   * @param name of property
   * @param val of property
   */
  @ConfInfo(dontSave = true)
  void setSubject(String name,
                  String val);
}