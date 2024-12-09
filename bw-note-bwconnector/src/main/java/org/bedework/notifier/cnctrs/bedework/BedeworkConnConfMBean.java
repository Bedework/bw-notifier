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

/** Configure a connector for the Bedework notification service
 *
 * @author douglm
 */
public interface BedeworkConnConfMBean extends NoteConnConfMBean {
  /**
   * @param val system notification principal
   */
  void setSystemNotificationHref(String val);

  /**
   * @return system notification principal
   */
  @MBeanInfo("system notification principal")
  String getSystemNotificationHref();

  /**
   * @param val system url
   */
  void setSystemUrl(String val);

  /**
   * @return system notification principal
   */
  @MBeanInfo("system notification principal")
  String getSystemUrl();

  /**
   * @param val token for authentication
   */
  void setToken(String val);

  /**
   * @return token
   */
  @MBeanInfo("token for authentication")
  String getToken();

  /** retryInterval - seconds
   *
   * @param val    int seconds
   */
  void setRetryInterval(int val);

  /** retryInterval - seconds
   *
   * @return int seconds
   */
  @MBeanInfo("retryInterval - seconds")
  int getRetryInterval();

  /** KeepAliveInterval - seconds
   *
   * @param val    int seconds
   */
  void setKeepAliveInterval(int val);

  /** KeepAliveInterval - seconds
   *
   * @return int seconds
   */
  @MBeanInfo("KeepAliveInterval - seconds")
  int getKeepAliveInterval();
}
