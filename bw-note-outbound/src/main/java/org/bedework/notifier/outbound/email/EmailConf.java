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
package org.bedework.notifier.outbound.email;

import org.bedework.notifier.outbound.common.AdaptorConf;

/** This configuration mbean is registered at startup by the main
 * configuration bean NotifyConf.
 *
 * @author douglm
 *
 */
public class EmailConf extends AdaptorConf<EmailAdaptorConfig> implements EmailConfMBean {
  private String transientUsername;
  private String transientPassword;

	/* ========================================================================
	 * Conf properties
	 * ======================================================================== */

	@Override
	public void setProtocol(final String val) {
		cfg.setProtocol(val);
	}

	@Override
	public String getProtocol() {
		return cfg.getProtocol();
	}

	@Override
	public void setServerUri(final String val) {
		cfg.setServerUri(val);
	}

	@Override
	public String getServerUri() {
		return cfg.getServerUri();
	}

	@Override
	public void setServerPort(final String val) {
		cfg.setServerPort(val);
	}

	@Override
	public String getServerPort() {
		return cfg.getServerPort();
	}

  @Override
  public void setStarttls(final boolean val) {
    cfg.setStarttls(val);
  }

  @Override
  public boolean getStarttls() {
    return cfg.getStarttls();
  }

  @Override
	public void setServerUsername(final String val) {
		cfg.setServerUsername(val);
	}

	@Override
	public String getServerUsername() {
		return cfg.getServerUsername();
	}

	@Override
	public void setServerPassword(final String val) {
		cfg.setServerPassword(val);
	}

	@Override
	public String getServerPassword() {
		return cfg.getServerPassword();
	}

	@Override
	public void setFrom(final String val) {
		cfg.setFrom(val);
	}

	@Override
	public String getFrom() {
		return cfg.getFrom();
	}

	@Override
	public void setLocale(final String val) {
		cfg.setLocale(val);
	}

	@Override
	public String getLocale() {
		return cfg.getLocale();
	}

  @Override
  public void setTransientUsername(final String val) {
    transientUsername = val;
  }

  @Override
  public String getTransientUsername() {
    return transientUsername;
  }

  @Override
  public void setTransientPassword(final String val) {
    transientPassword = val;
  }

  @Override
  public String getTransientPassword() {
    return transientPassword;
  }
}
