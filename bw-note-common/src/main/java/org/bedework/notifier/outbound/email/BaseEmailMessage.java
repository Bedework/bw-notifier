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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseEmailMessage {
	
	public static final String CONTENT_TYPE_PLAIN = "text/plain";
	public static final String CONTENT_TYPE_HTML = "text/html";
	
	private String from;
	private ArrayList<String> tos = new ArrayList<String>();
	private String subject;
	private LinkedHashMap<String, String> bodies = new LinkedHashMap<String, String>();

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public List<String> getTos() {
		return tos;
	}
	
	public void addTo(String to) {
		this.tos.add(to);
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = transform(subject);
	}

	public Map<String, String> getBodies() {
		return bodies;
	}

	public void addBody(String type, String content) {
		this.bodies.put(type, transform(content));
	}

	// Perform some transformation on the subject and message bodies.
	protected abstract String transform(String content); 
}
