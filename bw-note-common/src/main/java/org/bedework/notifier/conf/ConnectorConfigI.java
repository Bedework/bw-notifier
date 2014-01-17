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
package org.bedework.notifier.conf;


/** Common connector config properties
 *
 * @author douglm
 */
public interface ConnectorConfigI {
  /** Connector class name
   *
   * @param val    String
   */
  void setConnectorClassName(final String val);

  /** Class name
   *
   * @return String
   */
  String getConnectorClassName();

  /** Mbean class name
   *
   * @param val    String
   */
  void setMbeanClassName(final String val);

  /** Class name
   *
   * @return String
   */
  String getMbeanClassName();

  /** Read only?
   *
   * @param val    int seconds
   */
  void setReadOnly(final boolean val);

  /** Read only?
   *
   * @return int seconds
   */
  boolean getReadOnly();

  /** Can we trust the lastmod from this connector?
   *
   * @param val    boolean
   */
  void setTrustLastmod(final boolean val);

  /** Can we trust the lastmod from this connector?
   *
   * @return boolean
   */
  boolean getTrustLastmod();
}
