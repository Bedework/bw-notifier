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
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;

import net.fortuna.ical4j.model.property.DtStamp;
import org.oasis_open.docs.ws_calendar.ns.soap.BaseResponseType;

/** Abstract connector instance to handle some trivia.
 *
 * @author Mike Douglass
 */
public abstract class AbstractConnectorInstance 
        implements Logged, ConnectorInstance {
  protected Subscription sub;

  protected AbstractConnectorInstance(final Subscription sub) {
    super();
    this.sub = sub;
  }

  @Override
  public BaseResponseType open() {
    return null;
  }

  /* ====================================================================
   *                   Protected methods
   * ==================================================================== */

  protected String getDtstamp() {
    return new DtStamp().getValue();
  }

  /* ==============================================================
   *                   Logged methods
   * ============================================================== */

  private final BwLogger logger = new BwLogger();

  @Override
  public BwLogger getLogger() {
    if ((logger.getLoggedClass() == null) && (logger.getLoggedName() == null)) {
      logger.setLoggedClass(getClass());
    }

    return logger;
  }
}
