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
package org.bedework.notifier.service;

import org.bedework.notifier.Stat;

import org.bedework.util.jmx.ConfBaseMBean;
import org.bedework.util.jmx.MBeanInfo;

import java.util.List;

/** Configure the Bedework notification engine service
 *
 * @author douglm
 */
public interface NotifyConfMBean extends ConfBaseMBean {
  /* ========================================================================
   * Config properties
   * ======================================================================== */

  /**
   * @param val current size of ling pool
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

  /** How often we retry when a target is missing
   *
   * @param val
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

  /** Timezone server location
   *
   * @param val    String
   */
  void setTimezonesURI(final String val);

  /** Timezone server location
   *
   * @return String
   */
  @MBeanInfo("Timezone server location.")
  String getTimezonesURI();

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

  /** List connector names
   *
   * @return list of names
   */
  @MBeanInfo("List the connector names.")
  List<String> getConnectorNames();

  /** Get the current stats
   *
   * @return List of Stat
   */
  @MBeanInfo("Get the current stats.")
  List<Stat> getStats();

  /* ========================================================================
   * Operations
   * ======================================================================== */

  /* ========================================================================
   * Lifecycle
   * ======================================================================== */

  /** Lifecycle
   *
   */
  void start();

  /** Lifecycle
   *
   */
  void stop();

  /** Lifecycle
   *
   * @return true if started
   */
  boolean isStarted();

  /** (Re)load the configuration
   *
   * @return status
   */
  @MBeanInfo("(Re)load the configuration")
  String loadConfig();
}
