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

import java.util.List;

/** Bedework dummy adaptor config
 *
 * @author douglm
 */
@ConfInfo(elementName = "notify-adaptor")
public class EmailAdaptorConfig extends AdaptorConfig
        implements EmailAdaptorConfigI {
	private String protocol;
	private String serverUri;
	private String serverPort;
  private boolean starttls;
	private String serverUsername;
	private String serverPassword;
	private String from;
	private String locale;
  private String defaultSubject;
  private List<String> subjects;

  @Override
	public void setProtocol(final String val)  {
		protocol = val;
	}

  @Override
	@MBeanInfo("valid protocol for which an implementation exists, e.g \"imap\", \"smtp\".")
	public String getProtocol()  {
		return protocol;
	}

  @Override
	public void setServerUri(final String val)  {
		serverUri  = val;
	}

  @Override
	@MBeanInfo("Location of server.")
	public String getServerUri()  {
		return serverUri;
	}

  @Override
	public void setServerPort(final String val)  {
		serverPort  = val;
	}

  @Override
	@MBeanInfo("The server port.")
	public String getServerPort()  {
		return serverPort;
	}

  @Override
  public void setStarttls(final boolean val) {
    starttls = val;
  }

  @Override
  public boolean getStarttls() {
    return starttls;
  }

  @Override
  public void setServerUsername(final String val) {
    serverUsername = val;
  }

  @Override
	@MBeanInfo("User name if authentication is required.")
	public String getServerUsername() {
		return serverUsername;
	}

  @Override
  public void setServerPassword(final String val) {
    serverPassword = val;
  }

  @Override
	@MBeanInfo("User password if authentication is required.")
	public String getServerPassword() {
		return serverPassword;
	}

  @Override
	public void setFrom(final String val)  {
		from = val;
	}

	@MBeanInfo("Address we use when none supplied.")
	public String getFrom()  {
		return from;
	}

  @Override
	public void setLocale(final String val) {
		locale = val;
	}

  @Override
	@MBeanInfo("Local to user for adaptor emails.")
	public String getLocale() {
		return locale;
	}

  @Override
  public void setDefaultSubject(final String val) {
    defaultSubject = val;
  }

  @Override
  public String getDefaultSubject() {
    return defaultSubject;
  }

  @Override
  public void setSubjects(final List<String> val) {
    subjects = val;
  }

  @Override
  @ConfInfo(collectionElementName = "subject",
          elementType = "java.lang.String")
  public List<String> getSubjects() {
    return subjects;
  }

  @Override
  public void addSubject(final String name,
                                   final String val) {
    setSubjects(addListProperty(getSubjects(),
                                name, val));
  }

  @Override
  @ConfInfo(dontSave = true)
  public String getSubject(final String name) {
    return getProperty(getSubjects(), name);
  }

  @Override
  public void removeSubject(final String name) {
    removeProperty(getSubjects(), name);
  }

  @Override
  @ConfInfo(dontSave = true)
  public void setSubject(final String name,
                                   final String val) {
    setSubjects(setListProperty(getSubjects(),
                                name, val));
  }

	@Override
	public void toStringSegment(final ToString ts) {
		super.toStringSegment(ts);

		ts.append("protocol", getProtocol());
		ts.append("serverUri", getServerUri());
		ts.append("serverPort", getServerPort());
    ts.append("starttls", getStarttls());
		ts.append("from", getFrom());
    ts.append("locale", getLocale());
    ts.append("defaultSubject", getDefaultSubject());
    ts.append("subjects", getSubjects());
	}
}
