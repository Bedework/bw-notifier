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

import org.bedework.util.config.ConfInfo;
import org.bedework.util.config.ConfigBase;
import org.bedework.util.jmx.MBeanInfo;
import org.bedework.util.misc.ToString;

/** Common connector config properties
 *
 * @author douglm
 */
@ConfInfo(elementName = "notify-adaptor")
public class AdaptorConfig extends ConfigBase<AdaptorConfig> implements
        AdaptorConfigI {
  private String adaptorClassName;

  private String mbeanClassName;

  private String type;

  private int maxInstances;

  @Override
  public void setAdaptorClassName(final String val) {
    adaptorClassName = val;
  }

  @Override
  public String getAdaptorClassName() {
    return adaptorClassName;
  }

  @Override
  public void setMbeanClassName(final String val) {
    mbeanClassName = val;
  }

  @Override
  public String getMbeanClassName() {
    return mbeanClassName;
  }

  @Override
  public void setType(final String val) {
    type = val;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public void setMaxInstances(final int val) {
    maxInstances = val;
  }

  @Override
  @MBeanInfo("The type of adaptor.")
  public int getMaxInstances() {
    return maxInstances;
  }

  @Override
  public void toStringSegment(final ToString ts) {
    super.toStringSegment(ts);

    ts.append("adaptorClassName", getAdaptorClassName());
    ts.append("mbeanClassName", getMbeanClassName());
    ts.append("type", getType());
    ts.append("maxInstances", getMaxInstances());
  }

  @Override
  public int hashCode() {
    return getName().hashCode();
  }
}
