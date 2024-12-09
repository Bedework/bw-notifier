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

import org.bedework.util.jmx.MBeanInfo;

/** Common adaptor config properties
 *
 * @author douglm
 */
public interface AdaptorConfigI {
  /** Adaptor class name
   *
   * @param val    String
   */
  void setAdaptorClassName(String val);

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
  void setMbeanClassName(String val);

  /** Class name
   *
   * @return String
   */
  @MBeanInfo("The mbean class.")
  String getMbeanClassName();

  /**
   * @param val the type of adaptor
   */
  void setType(String val);

  /**
   * @return the type of adaptor
   */
  @MBeanInfo("The type of adaptor.")
  String getType();

  /**
   * @param val the max number of instances
   */
  void setMaxInstances(int val);

  /**
   * @return the max number of instances
   */
  @MBeanInfo("The max number of instances.")
  int getMaxInstances();
}
