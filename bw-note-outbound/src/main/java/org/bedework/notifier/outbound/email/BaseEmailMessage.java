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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public abstract class BaseEmailMessage {

  public static final String CONTENT_TYPE_PLAIN = "text/plain";
  public static final String CONTENT_TYPE_HTML = "text/html";

  private String from;
  private final Set<String> tos = new HashSet<>();
  private final Set<String> ccs = new HashSet<>();
  private final Set<String> bccs = new HashSet<>();
  private String subject;
  private final LinkedHashMap<String, String> bodies = new LinkedHashMap<>();

  public String getFrom() {
    return from;
  }

  public void setFrom(final String from) {
    this.from = from;
  }

  public Set<String> getTos() {
    return tos;
  }

  public void addTo(final String to) {
    this.tos.add(to);
  }

  public Set<String> getCcs() {
    return ccs;
  }

  public void addCc(final String cc) {
    this.ccs.add(cc);
  }

  public Set<String> getBccs() {
    return bccs;
  }

  public void addBcc(final String bcc) {
    this.bccs.add(bcc);
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(final String subject) {
    this.subject = transform(subject);
  }

  public Map<String, String> getBodies() {
    return bodies;
  }

  public void addBody(final String type, final String content) {
    this.bodies.put(type, transform(content));
  }

  // Perform some transformation on the subject and message bodies.
  protected abstract String transform(String content);
}
