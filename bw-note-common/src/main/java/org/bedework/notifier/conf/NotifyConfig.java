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

import org.bedework.notifier.NotifyConfPropertiesImpl;
import org.bedework.notifier.db.IpAddrInfo;
import org.bedework.notifier.outbound.common.AdaptorConf;
import org.bedework.notifier.service.NoteConnConf;
import org.bedework.util.config.ConfInfo;

import java.util.List;
import java.util.SortedSet;

/** This class defines the various properties we need for the notification engine
 *
 * @author Mike Douglass
 */
@ConfInfo(elementName = "note-confinfo")
public class NotifyConfig extends NotifyConfPropertiesImpl {
  private List<NoteConnConf> connectorConfs;

  private List<AdaptorConf> adaptorConfs;

  private SortedSet<IpAddrInfo> ipInfo;

  /**
   * @param val set of IpAddrInfo
   */
  public void setIpInfo(final SortedSet<IpAddrInfo> val) {
    ipInfo = val;
  }

  /**
   * @return ip info
   */
  public SortedSet<IpAddrInfo> getIpInfo() {
    return ipInfo;
  }

  /** Map of (name, className)
   *
   * @param val list of NoteConnConf
   */
  public void setConnectorConfs(final List<NoteConnConf> val) {
    connectorConfs = val;
  }

  /**
   *
   * @return list of NoteConnConf
   */
  public List<NoteConnConf> getConnectorConfs() {
    return connectorConfs;
  }

  /**
   *
   * @param val list of AdaptorConfig
   */
  public void setAdaptorConfs(final List<AdaptorConf> val) {
    adaptorConfs = val;
  }

  /**
   *
   * @return list of AdaptorConfig
   */
  @ConfInfo(dontSave = true)
  public List<AdaptorConf> getAdaptorConfs() {
    return adaptorConfs;
  }
}
