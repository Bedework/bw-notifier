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
package org.bedework.notifier.cnctrs;

import org.bedework.notifier.db.Subscription;
import org.bedework.notifier.exception.NoteException;

import org.apache.log4j.Logger;
import org.oasis_open.docs.ws_calendar.ns.soap.BaseResponseType;

/** Abstract connector instance to handle some trivia.
 *
 * @author Mike Douglass
 */
public abstract class AbstractConnectorInstance implements ConnectorInstance {
  private transient Logger log;

  protected boolean debug;

  protected Subscription sub;

  protected AbstractConnectorInstance(final Subscription sub) {
    this.sub = sub;

    debug = getLogger().isDebugEnabled();
  }

  /* (non-Javadoc)
   * @see org.bedework.synch.ConnectorInstance#open()
   */
  @Override
  public BaseResponseType open() throws NoteException {
    return null;
  }

  /* ====================================================================
   *                   Protected methods
   * ==================================================================== */

  /*

  private String decryptPw(final BwCalendar val) throws CalFacadeException {
    try {
      return getSvc().getEncrypter().decrypt(val.getRemotePw());
    } catch (Throwable t) {
      throw new CalFacadeException(t);
    }
  }
   *
   */

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
