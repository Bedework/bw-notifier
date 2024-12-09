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
package org.bedework.notifier.cnctrs.manager;

import org.bedework.notifier.cnctrs.AbstractConnectorInstance;
import org.bedework.notifier.cnctrs.Connector;
import org.bedework.notifier.db.NotifyDb;
import org.bedework.notifier.exception.NoteException;
import org.bedework.notifier.notifications.Note;

import org.oasis_open.docs.ws_calendar.ns.soap.BaseResponseType;

/** A null connector instance
 *
 * @author Mike Douglass
 */
public class NotifyConnectorInstance extends AbstractConnectorInstance {
  private final NotifyConnector cnctr;

  NotifyConnectorInstance(final NotifyConnector cnctr){
    super(null);

    this.cnctr = cnctr;
  }

  @Override
  public Connector getConnector() {
    return cnctr;
  }

  @Override
  public BaseResponseType open() {
    return null;
  }

  @Override
  public boolean changed() {
    return false;
  }

  @Override
  public boolean check(final NotifyDb db,
                       final String resource) {
    throw new NoteException("Uncallable");
  }

  @Override
  public Note nextItem(final NotifyDb db) {
    throw new NoteException("Uncallable");
  }

  @Override
  public boolean completeItem(final NotifyDb db,
                              final Note item) {
    throw new NoteException("Uncallable");
  }
}
