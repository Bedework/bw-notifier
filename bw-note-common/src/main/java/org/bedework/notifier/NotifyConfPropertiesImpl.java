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
import org.bedework.util.config.ConfigBase;

/**
 * @author douglm
 *
 */
@ConfInfo(elementName = "note-confinfo")
public class NotifyConfPropertiesImpl extends ConfigBase<NotifyConfPropertiesImpl>
        implements NotifyConfProperties {
  /* Size of noteling pool */
  private int notelingPoolSize;

  /* millisecs */
  private long notelingPoolTimeout;

  /* How often we retry when a target is missing */
  private int missingTargetRetries;

  /* web service push callback uri - null for no service */
  private String callbackURI;

  /* Timezone server location */
  private String timezonesURI;

  /* Path to keystore - null for use default */
  private String keystore;

  /* Path to keystores  */
  private String privKeys;
  /* Path to keystores  */
  private String pubKeys;

  /**
   * @param val current size of noteling pool
   */
  public void setNotelingPoolSize(final int val) {
    notelingPoolSize = val;
  }

  /**
   * @return current size of noteling pool
   */
  public int getNotelingPoolSize() {
    return notelingPoolSize;
  }

  /**
   * @param val timeout in millisecs
   */
  public void setNotelingPoolTimeout(final long val) {
    notelingPoolTimeout = val;
  }

  /**
   * @return timeout in millisecs
   */
  public long getNotelingPoolTimeout() {
    return notelingPoolTimeout;
  }

  /** How often we retry when a target is missing
   *
   * @param val number of retries
   */
  public void setMissingTargetRetries(final int val) {
    missingTargetRetries = val;
  }

  /**
   * @return How often we retry when a target is missing
   */
  public int getMissingTargetRetries() {
    return missingTargetRetries;
  }

  /** web service push callback uri - null for no service
   *
   * @param val    String
   */
  public void setCallbackURI(final String val) {
    callbackURI = val;
  }

  /** web service push callback uri - null for no service
   *
   * @return String
   */
  public String getCallbackURI() {
    return callbackURI;
  }

  /** Timezone server location
   *
   * @param val    String
   */
  public void setTimezonesURI(final String val) {
    timezonesURI = val;
  }

  /** Timezone server location
   *
   * @return String
   */
  public String getTimezonesURI() {
    return timezonesURI;
  }

  /** Path to keystore - null for use default
   *
   * @param val    String
   */
  public void setKeystore(final String val) {
    keystore = val;
  }

  /** Path to keystore - null for use default
   *
   * @return String
   */
  public String getKeystore() {
    return keystore;
  }

  /**
   *
   * @param val    String
   */
  public void setPrivKeys(final String val) {
    privKeys = val;
  }

  /**
   *
   * @return String
   */
  public String getPrivKeys() {
    return privKeys;
  }

  /**
   *
   * @param val    String
   */
  public void setPubKeys(final String val) {
    pubKeys = val;
  }

  /**
   *
   * @return String
   */
  public String getPubKeys() {
    return pubKeys;
  }
}
