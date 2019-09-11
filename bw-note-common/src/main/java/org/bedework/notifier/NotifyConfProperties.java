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
package org.bedework.notifier;

import org.bedework.util.config.ConfInfo;
import org.bedework.util.jmx.MBeanInfo;

/** Configuration properties for the Bedework notification engine service
 *
 * @author douglm
 */
@ConfInfo(elementName = "note-confinfo")
public interface NotifyConfProperties {
  /* ========================================================================
   * Config properties
   * ======================================================================== */

  /**
   * @param val current size of noteling pool
   */
  void setNotelingPoolSize(final int val);

  /**
   * @return current size of noteling pool
   */
  @MBeanInfo("size of noteling pool.")
  int getNotelingPoolSize();

  /**
   * @param val timeout in millisecs
   */
  void setNotelingPoolTimeout(final long val);

  /**
   * @return timeout in millisecs
   */
  @MBeanInfo("timeout in millisecs.")
  long getNotelingPoolTimeout();

  /**
   *
   * @param val How often we retry when a target is missing
   */
  void setMissingTargetRetries(final int val);

  /**
   * @return How often we retry when a target is missing
   */
  @MBeanInfo("How often we retry when a target is missing.")
  int getMissingTargetRetries();

  /** web service push callback uri - null for no service
   *
   * @param val    String
   */
  void setCallbackURI(final String val);

  /** web service push callback uri - null for no service
   *
   * @return String
   */
  @MBeanInfo("web service push callback uri - null for no service.")
  String getCallbackURI();

  /**
   *
   * @param val Timezone server location
   */
  void setTimezonesURI(final String val);

  /** Timezone server location
   *
   * @return String
   */
  @MBeanInfo("Timezone server location.")
  String getTimezonesURI();

  /**
   *
   * @param val path to templates
   */
  void setTemplatesPath(final String val);

  /**
   *
   * @return path to templates
   */
  @MBeanInfo("Path to templates.")
  String getTemplatesPath();

  /** Path to keystore - null for use default
   *
   * @param val    String
   */
  void setKeystore(final String val);

  /** Path to keystore - null for use default
   *
   * @return String
   */
  @MBeanInfo("Path to keystore - null for use default.")
  String getKeystore();

  /**
   *
   * @param val    String
   */
  void setPrivKeys(final String val);

  /**
   *
   * @return String
   */
  @MBeanInfo("Name of private keys file.")
  String getPrivKeys();

  /**
   *
   * @param val    String
   */
  void setPubKeys(final String val);

  /**
   *
   * @return String
   */
  @MBeanInfo("Name of public keys file.")
  String getPubKeys();

  /**
   *
   * @param val    String
   */
  void setCardDAVURI(final String val);

  /**
   *
   * @return String
   */
  @MBeanInfo("CalDAV host.")
  String getCardDAVURI();

  /**
   *
   * @param val    String
   */
  void setCardDAVPrincipalsPath(final String val);

  /**
   *
   * @return String
   */
  @MBeanInfo("Principals path for vCard lookup.")
  String getCardDAVPrincipalsPath();

  /**
   *
   * @param val    String
   */
  void setVCardContentType(final String val);

  /**
   *
   * @return String
   */
  @MBeanInfo("ACCEPT header for vCard retrieval, either 'text/vcard' or 'application/vcard+json'.")
  String getVCardContentType();
}
