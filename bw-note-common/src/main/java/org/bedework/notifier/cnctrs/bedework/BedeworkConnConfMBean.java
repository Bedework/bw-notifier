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

import org.bedework.notifier.service.NoteConnConfMBean;

import org.bedework.util.jmx.MBeanInfo;

/** Configure a file connector for the Bedework synch engine service
 *
 * @author douglm
 */
public interface BedeworkConnConfMBean extends NoteConnConfMBean {
  /** Min poll - seconds
   *
   * @param val    int seconds
   */
  void setBwWSDLURI(final String val);

  /** Min poll - seconds
   *
   * @return int seconds
   */
  @MBeanInfo("WSDL for remote service")
  String getBwWSDLURI();

  /** retryInterval - seconds
   *
   * @param val    int seconds
   */
  public void setRetryInterval(final int val);

  /** retryInterval - seconds
   *
   * @return int seconds
   */
  @MBeanInfo("retryInterval - seconds")
  public int getRetryInterval();

  /** KeepAliveInterval - seconds
   *
   * @param val    int seconds
   */
  public void setKeepAliveInterval(final int val);

  /** KeepAliveInterval - seconds
   *
   * @return int seconds
   */
  @MBeanInfo("KeepAliveInterval - seconds")
  public int getKeepAliveInterval();
}
