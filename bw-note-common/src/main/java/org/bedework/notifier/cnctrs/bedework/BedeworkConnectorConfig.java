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
package org.bedework.notifier.cnctrs.bedework;

import org.bedework.notifier.conf.ConnectorConfig;
import org.bedework.util.config.ConfInfo;
import org.bedework.util.misc.ToString;

/** Bedework synch connector config
 *
 * @author douglm
 */
@ConfInfo(elementName = "notify-connector")
public class BedeworkConnectorConfig extends ConnectorConfig {
  private String notificationDirHref;

  /** WSDL for remote service */
  private String bwWSDLURI;

  /** seconds before retry on failure  */
  private int retryInterval;

  /** seconds before we ping just to say we're still around  */
  private int keepAliveInterval;

  /**
   * @param val path to notifications directory
   */
  public void setNotificationDirHref(final String val) {
    notificationDirHref = val;
  }

  /**
   * @return path to notifications directory
   */
  public String getNotificationDirHref() {
    return notificationDirHref;
  }

  /** bedework web service WSDL uri
   *
   * @param val    String
   */
  public void setBwWSDLURI(final String val) {
    bwWSDLURI = val;
  }

  /** Bedework web service WSDL uri
   *
   * @return String
   */
  public String getBwWSDLURI() {
    return bwWSDLURI;
  }

  /** retryInterval - seconds
   *
   * @param val    int seconds
   */
  public void setRetryInterval(final int val) {
    retryInterval = val;
  }

  /** retryInterval - seconds
   *
   * @return int seconds
   */
  public int getRetryInterval() {
    return retryInterval;
  }

  /** KeepAliveInterval - seconds
   *
   * @param val    int seconds
   */
  public void setKeepAliveInterval(final int val) {
    keepAliveInterval = val;
  }

  /** KeepAliveInterval - seconds
   *
   * @return int seconds
   */
  public int getKeepAliveInterval() {
    return keepAliveInterval;
  }

  @Override
  public void toStringSegment(final ToString ts) {
    super.toStringSegment(ts);

    ts.append("notificationDirHref", getNotificationDirHref()).
            append("bwWSDLURI", getBwWSDLURI()).
            append("retryInterval", getRetryInterval()).
            append("keepAliveInterval", getKeepAliveInterval());
  }

  @Override
  public String toString() {
    ToString ts = new ToString(this);

    toStringSegment(ts);

    return ts.toString();
  }
}
