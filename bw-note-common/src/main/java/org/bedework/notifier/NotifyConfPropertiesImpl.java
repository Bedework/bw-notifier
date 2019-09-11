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
import org.bedework.util.config.HibernateConfigBase;

/**
 * @author douglm
 *
 */
@ConfInfo(elementName = "note-confinfo")
public class NotifyConfPropertiesImpl extends HibernateConfigBase<NotifyConfPropertiesImpl>
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

  private String templatesPath;

  /* Path to keystore - null for use default */
  private String keystore;

  /* Path to keystores  */
  private String privKeys;
  /* Path to keystores  */
  private String pubKeys;

  private String cardDAVURI;
  private String cardDAVPrincipalsPath;
  private String vCardContentType;

  @Override
  public void setNotelingPoolSize(final int val) {
    notelingPoolSize = val;
  }

  @Override
  public int getNotelingPoolSize() {
    return notelingPoolSize;
  }

  @Override
  public void setNotelingPoolTimeout(final long val) {
    notelingPoolTimeout = val;
  }

  @Override
  public long getNotelingPoolTimeout() {
    return notelingPoolTimeout;
  }

  @Override
  public void setMissingTargetRetries(final int val) {
    missingTargetRetries = val;
  }

  @Override
  public int getMissingTargetRetries() {
    return missingTargetRetries;
  }

  @Override
  public void setCallbackURI(final String val) {
    callbackURI = val;
  }

  @Override
  public String getCallbackURI() {
    return callbackURI;
  }

  @Override
  public void setTimezonesURI(final String val) {
    timezonesURI = val;
  }

  @Override
  public String getTimezonesURI() {
    return timezonesURI;
  }

  @Override
  public void setTemplatesPath(final String val) {
    templatesPath = val;
  }

  @Override
  public String getTemplatesPath() {
    return templatesPath;
  }

  @Override
  public void setKeystore(final String val) {
    keystore = val;
  }

  @Override
  public String getKeystore() {
    return keystore;
  }

  @Override
  public void setPrivKeys(final String val) {
    privKeys = val;
  }

  @Override
  public String getPrivKeys() {
    return privKeys;
  }

  @Override
  public void setPubKeys(final String val) {
    pubKeys = val;
  }

  @Override
  public String getPubKeys() {
    return pubKeys;
  }

  @Override
  public String getCardDAVURI() {
    return cardDAVURI;
  }

  @Override
  public void setCardDAVURI(String cardDAVURI) {
    this.cardDAVURI = cardDAVURI;
  }

  @Override
  public String getCardDAVPrincipalsPath() {
    return cardDAVPrincipalsPath;
  }

  @Override
  public void setCardDAVPrincipalsPath(String cardDAVPrincipalsPath) {
    this.cardDAVPrincipalsPath = cardDAVPrincipalsPath;
  }

  @Override
  public String getVCardContentType() {
    return vCardContentType;
  }

  @Override
  public void setVCardContentType(String vCardContentType) {
    this.vCardContentType = vCardContentType;
  }
}
