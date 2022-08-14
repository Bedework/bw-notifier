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

import org.bedework.notifier.cnctrs.Connector;
import org.bedework.util.config.ConfigurationStore;
import org.bedework.util.jmx.ConfBase;

/**
 * @author douglm
 *
 * @param <X> adaptor class
 */
public class AdaptorConf<X extends AdaptorConfig> extends ConfBase<X>
    implements AdaptorConfMBean {
  //private String cname;

  private Connector connector;

  public AdaptorConf(final String serviceName,
                     final ConfigurationStore store,
                     final String configName) {
    super(serviceName, store, configName);
  }

  /**
   * @param cfg - the configuration
   */
  public void setConfig(final X cfg) {
    this.cfg = cfg;
  }

  @Override
  public String loadConfig() {
    return null;
  }

  /** Embed the connector
   *
   * @param val the connector
   */
  public void setConnector(final Connector val) {
    connector = val;
  }

  /**
   * @return the connector
   */
  public Connector getConnector() {
    return connector;
  }

  @Override
  public String getStatus() {
    return connector.getStatus();
  }

  /* ========================================================================
   * Conf properties
   * ======================================================================== */

  @Override
  public void setAdaptorClassName(final String val) {
    cfg.setAdaptorClassName(val);
  }

  @Override
  public String getAdaptorClassName() {
    return cfg.getAdaptorClassName();
  }

  @Override
  public void setMbeanClassName(final String val) {
    cfg.setMbeanClassName(val);
  }

  @Override
  public String getMbeanClassName() {
    if (cfg.getMbeanClassName() == null) {
      return this.getClass().getCanonicalName();
    }

    return cfg.getMbeanClassName();
  }

  @Override
  public void setType(final String val) {
    cfg.setType(val);
  }

  @Override
  public String getType() {
    return cfg.getType();
  }

  @Override
  public void setMaxInstances(final int val) {
    cfg.setMaxInstances(val);
  }

  @Override
  public int getMaxInstances() {
    return cfg.getMaxInstances();
  }

  /* ====================================================================
   *                   Private methods
   * ==================================================================== */

  /* ====================================================================
   *                   Protected methods
   * ==================================================================== */
}
