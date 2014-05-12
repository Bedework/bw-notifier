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

import org.bedework.notifier.outbound.common.AdaptorConfig;
import org.bedework.util.config.ConfInfo;
import org.bedework.util.jmx.MBeanInfo;
import org.bedework.util.misc.ToString;

/** Bedework dummy adaptor config
 *
 * @author douglm
 */
@ConfInfo(elementName = "notify-adaptor")
public class EmailAdaptorConfig extends AdaptorConfig {
	private String protocol;
	private String protocolClass;
	private String serverUri;
	private String serverPort;
	private String serverUsername;
	private String serverPassword;
	private String from;
	private String locale;

	/** valid protocol for which an implementation exists, e.g "imap", "smtp"
	 *
	 * @param val the protocol
	 */
	public void setProtocol(final String val)  {
		protocol = val;
	}

	/**
	 * @return String
	 */
	@MBeanInfo("valid protocol for which an implementation exists, e.g \"imap\", \"smtp\".")
	public String getProtocol()  {
		return protocol;
	}

	/** Implementation for the selected protocol
	 *
	 * @param val class
	 */
	public void setProtocolClass(final String val)  {
		protocolClass  = val;
	}

	/**
	 * @return String
	 */
	@MBeanInfo("Implementation for the selected protocol.")
	public String getProtocolClass()  {
		return protocolClass;
	}

	/** Where we send it.
	 *
	 * @param val uri
	 */
	public void setServerUri(final String val)  {
		serverUri  = val;
	}

	/**
	 * @return String
	 */
	@MBeanInfo("Location of server.")
	public String getServerUri()  {
		return serverUri;
	}

	/**
	 * @param val port
	 */
	public void setServerPort(final String val)  {
		serverPort  = val;
	}

	/**
	 * @return String
	 */
	@MBeanInfo("The server port.")
	public String getServerPort()  {
		return serverPort;
	}

  /**
   * @param val server Username
   */
  public void setServerUsername(final String val) {
    serverUsername = val;
  }

	@MBeanInfo("User name if authentication is required.")
	public String getServerUsername() {
		return serverUsername;
	}

  /**
   * @param val server Password
   */
  public void setServerPassword(final String val) {
    serverPassword = val;
  }

  /**
   * @return password
   */
	@MBeanInfo("User password if authentication is required.")
	public String getServerPassword() {
		return serverPassword;
	}

	/** Address we use when none supplied
	 *
	 * @param val from for message
	 */
	void setFrom(final String val)  {
		from = val;
	}

	/**
	 * @return String
	 */
	@MBeanInfo("Address we use when none supplied.")
	String getFrom()  {
		return from;
	}

  /**
   * @param val for messages
   */
	public void setLocale(final String val) {
		locale = val;
	}

	/**
	 * @return String
	 */
	@MBeanInfo("Local to user for adaptor emails.")
	public String getLocale() {
		return locale;
	}

	@Override
	public void toStringSegment(final ToString ts) {
		super.toStringSegment(ts);

		ts.append("protocol", getProtocol());
		ts.append("protocolClass", getProtocolClass());
		ts.append("serverUri", getServerUri());
		ts.append("serverPort", getServerPort());
		ts.append("from", getFrom());
    ts.append("locale", getLocale());
	}
}
