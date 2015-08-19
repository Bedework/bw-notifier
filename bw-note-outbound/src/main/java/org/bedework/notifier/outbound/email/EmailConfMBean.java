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

import org.bedework.util.jmx.ConfBaseMBean;
import org.bedework.util.jmx.MBeanInfo;

/** Configure an email adaptor for the Bedework notification service
 *
 * @author douglm
 */
public interface EmailConfMBean extends ConfBaseMBean, EmailAdaptorConfigI {
	/**
	 * @param val user name
	 */
	void setTransientUsername(String val);

	/**
	 * @return String
	 */
	@MBeanInfo("Override or supply a transient username if authentication is required.")
	String getTransientUsername();

	/**
	 * @param val a password
	 */
	void setTransientPassword(String val);

	/**
	 * @return String
	 */
	@MBeanInfo("Override or supply a transient password if authentication is required.")
	String getTransientPassword();
}
