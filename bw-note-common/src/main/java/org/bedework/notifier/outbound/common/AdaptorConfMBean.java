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
package org.bedework.notifier.outbound.common;

import org.bedework.util.jmx.ConfBaseMBean;
import org.bedework.util.jmx.MBeanInfo;

/** Configure an adaptor for the Bedework synch engine service
 *
 * @author douglm
 */
public interface AdaptorConfMBean extends ConfBaseMBean,
        AdaptorConfigI {
  /* ========================================================================
   * Status
   * ======================================================================== */

  /**
   * @return status message
   */
  String getStatus();

  /* ========================================================================
   * Config properties
   * ======================================================================== */

  /** Class name
   *
   * @param val    String
   */
  void setAdaptorClassName(final String val);

  /** Class name
   *
   * @return String
   */
  @MBeanInfo("The adaptor class.")
  String getAdaptorClassName();

  /** Mbean class name
   *
   * @param val    String
   */
  void setMbeanClassName(final String val);

  /** Class name
   *
   * @return String
   */
  @MBeanInfo("The mbean class.")
  String getMbeanClassName();

  /* ========================================================================
   * Operations
   * ======================================================================== */
}
