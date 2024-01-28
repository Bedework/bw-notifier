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

import org.bedework.util.config.ConfInfo;
import org.bedework.util.config.ConfigBase;
import org.bedework.util.misc.ToString;

/** Common connector config properties
 *
 * @author douglm
 */
@ConfInfo(elementName = "notifier-connector")
public class ConnectorConfig extends ConfigBase<ConnectorConfig> implements ConnectorConfigI  {
  private String connectorClassName;

  private String mbeanClassName;

  private boolean readOnly;

  private boolean trustLastmod;

  @Override
  public void setConnectorClassName(final String val) {
    connectorClassName = val;
  }

  @Override
  public String getConnectorClassName() {
    return connectorClassName;
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
  public void setReadOnly(final boolean val) {
    readOnly = val;
  }

  @Override
  public boolean getReadOnly() {
    return readOnly;
  }

  @Override
  public void setTrustLastmod(final boolean val) {
    trustLastmod = val;
  }

  @Override
  public boolean getTrustLastmod() {
    return trustLastmod;
  }

  @Override
  public void toStringSegment(final ToString ts) {
    super.toStringSegment(ts);

    ts.append("connectorClassName", getConnectorClassName());
    ts.append("mbeanClassName", getMbeanClassName());
    ts.append("readOnly", getReadOnly());
    ts.append("trustLastmod", getTrustLastmod());
  }

  @Override
  public int hashCode() {
    return getName().hashCode();
  }

  @Override
  public String toString() {
    ToString ts = new ToString(this);

    toStringSegment(ts);

    return ts.toString();
  }
}
