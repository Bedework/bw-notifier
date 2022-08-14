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
package org.bedework.notifier.outbound.dummy;

import org.bedework.notifier.outbound.common.AdaptorConf;
import org.bedework.util.config.ConfigurationStore;

/** This configuration mbean is registered at startup by the main
 * configuration bean NotifyConf.
 *
 * @author douglm
 *
 */
public class DummyConf extends AdaptorConf<DummyAdaptorConfig>
    implements DummyConfMBean {
  public DummyConf(final String serviceName,
                   final ConfigurationStore store,
                   final String configName) {
    super(serviceName, store, configName);
  }

  /* ========================================================================
   * Conf properties
   * ======================================================================== */
}
