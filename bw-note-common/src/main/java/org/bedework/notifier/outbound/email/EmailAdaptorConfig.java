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

import org.bedework.notifier.outbound.common.AdaptorConfig;
import org.bedework.util.config.ConfInfo;
import org.bedework.util.jmx.MBeanInfo;
import org.bedework.util.misc.ToString;

/** Bedework dummy adaptor config
 *
 * @author douglm
 */
@ConfInfo(elementName = "notify-adaptor")
public class EmailAdaptorConfig extends AdaptorConfig {
  private String protocol;

  private String protocolClass;

  private String serverUri;

  private String serverPort;

  private String from;

  /** valid protocol for which an implementation exists, e.g "imap", "smtp"
   *
   * @param val
   */
  void setProtocol(String val)  {
    protocol  = val;
  }

  /**
   * @return String
   */
  @MBeanInfo("valid protocol for which an implementation exists, e.g \"imap\", \"smtp\".")
  String getProtocol()  {
    return protocol;
  }

  /** Implementation for the selected protocol
   *
   * @param val
   */
  void setProtocolClass(String val)  {
    protocolClass  = val;
  }

  /**
   * @return String
   */
  @MBeanInfo("Implementation for the selected protocol.")
  String getProtocolClass()  {
    return protocolClass;
  }

  /** Where we send it.
   *
   * @param val
   */
  void setServerUri(String val)  {
    serverUri  = val;
  }

  /**
   * @return String
   */
  @MBeanInfo("Location of server.")
  String getServerUri()  {
    return serverUri;
  }

  /**
   * @param val
   */
  void setServerPort(String val)  {
    serverPort  = val;
  }

  /**
   * @return String
   */
  @MBeanInfo("The server port.")
  String getServerPort()  {
    return serverPort;
  }

  /** Address we use when none supplied
   *
   * @param val
   */
  void setFrom(String val)  {
    from = val;
  }

  /**
   * @return String
   */
  @MBeanInfo("Address we use when none supplied.")
  String getFrom()  {
    return from;
  }

  @Override
  public void toStringSegment(final ToString ts) {
    super.toStringSegment(ts);

    ts.append("protocol", getProtocol());
    ts.append("protocolClass", getProtocolClass());
    ts.append("serverUri", getServerUri());
    ts.append("serverPort", getServerPort());
    ts.append("from", getFrom());
  }
}
