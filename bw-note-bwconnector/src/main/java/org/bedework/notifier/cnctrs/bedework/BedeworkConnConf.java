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

import org.bedework.notifier.service.NoteConnConf;
import org.bedework.util.config.ConfigurationStore;

/** This configuration mbean is registered at startup by the main
 * configuration bean NotifyConf.
 *
 * @author douglm
 *
 */
public class BedeworkConnConf extends NoteConnConf<BedeworkConnectorConfig>
    implements BedeworkConnConfMBean {
  public BedeworkConnConf(final String serviceName,
                          final ConfigurationStore store,
                          final String configName) {
    super(serviceName, store, configName);
  }

  /* ==============================================================
   * Conf properties
   * ============================================================== */

  @Override
  public void setSystemNotificationHref(final String val) {
    cfg.setSystemNotificationHref(val);
  }

  @Override
  public String getSystemNotificationHref() {
    return cfg.getSystemNotificationHref();
  }

  @Override
  public void setSystemUrl(final String val) {
    cfg.setSystemUrl(val);
  }

  @Override
  public String getSystemUrl() {
    return cfg.getSystemUrl();
  }

  @Override
  public void setToken(final String val) {
    cfg.setToken(val);
  }

  @Override
  public String getToken() {
    return cfg.getToken();
  }

  @Override
  public void setRetryInterval(final int val) {
    cfg.setRetryInterval(val);
  }

  @Override
  public int getRetryInterval() {
    return cfg.getRetryInterval();
  }

  @Override
  public void setKeepAliveInterval(final int val) {
    cfg.setKeepAliveInterval(val);
  }

  @Override
  public int getKeepAliveInterval() {
    return cfg.getKeepAliveInterval();
  }
}
