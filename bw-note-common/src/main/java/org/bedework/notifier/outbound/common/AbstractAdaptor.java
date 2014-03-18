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

import org.bedework.notifier.outbound.Adaptor;

import org.apache.log4j.Logger;

import java.util.concurrent.atomic.AtomicLong;

/** Some useful methods..
 *
 * @author Mike Douglass
 *
 */
public abstract class AbstractAdaptor implements Adaptor {
  private transient Logger log;

  private static AtomicLong nextId = new AtomicLong();

  private Long id;

  protected AdaptorConfig conf;

  protected AbstractAdaptor() {
    id = nextId.incrementAndGet();
  }

  public long getId() {
    return id;
  }

  public void setConf(AdaptorConfig conf) {
    this.conf = conf;
  }

  public AdaptorConfig getConf() {
    return conf;
  }

  public String getType() {
    return conf.getType();
  }

  /* ====================================================================
   *                   Protected methods
   * ==================================================================== */

  protected void info(final String msg) {
    getLogger().info(msg);
  }

  protected void trace(final String msg) {
    getLogger().debug(msg);
  }

  protected void error(final Throwable t) {
    getLogger().error(this, t);
  }

  protected void error(final String msg) {
    getLogger().error(msg);
  }

  protected void warn(final String msg) {
    getLogger().warn(msg);
  }

  /* Get a logger for messages
   */
  protected Logger getLogger() {
    if (log == null) {
      log = Logger.getLogger(this.getClass());
    }

    return log;
  }
}
