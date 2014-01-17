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
package org.bedework.notifier.cnctrs.bedework;

import org.bedework.notifier.cnctrs.AbstractConnectorInstance;
import org.bedework.notifier.cnctrs.Connector;
import org.bedework.notifier.db.Subscription;
import org.bedework.notifier.exception.NoteException;

import org.oasis_open.docs.ws_calendar.ns.soap.DeleteItemResponseType;
import org.oasis_open.docs.ws_calendar.ns.soap.FetchItemResponseType;
import org.oasis_open.docs.ws_calendar.ns.soap.UpdateItemResponseType;
import org.oasis_open.docs.ws_calendar.ns.soap.UpdateItemType;

import java.util.ArrayList;
import java.util.List;

/** Handles bedework synch interactions.
 *
 * @author Mike Douglass
 */
public class BedeworkConnectorInstance extends AbstractConnectorInstance {
  @SuppressWarnings("unused")
  private BedeworkConnectorConfig config;

  private final BedeworkConnector cnctr;

  private BedeworkSubscriptionInfo info;

  BedeworkConnectorInstance(final BedeworkConnectorConfig config,
                            final BedeworkConnector cnctr,
                            final Subscription sub,
                            final BedeworkSubscriptionInfo info) {
    super(sub);
    this.config = config;
    this.cnctr = cnctr;
    this.info = info;
  }

  @Override
  public Connector getConnector() {
    return cnctr;
  }

  /* (non-Javadoc)
   * @see org.bedework.synch.ConnectorInstance#changed()
   */
  @Override
  public boolean changed() throws NoteException {
    /* This implementation needs to at least check the change token for the
     * collection and match it against the stored token.
     */
    return false;
  }

  @Override
  public NotifyItemsInfo getItemsInfo() throws NoteException {
    /* Will do a query on the configurd resource directory and return
       a list of hrefs.
     */

    return null;
  }

  @Override
  public DeleteItemResponseType deleteItem(final String uid) throws NoteException {

    return null;
  }

  @Override
  public FetchItemResponseType fetchItem(final String uid) throws NoteException {
    return null;
  }

  @Override
  public List<FetchItemResponseType> fetchItems(final List<String> hrefs) throws NoteException {
    // XXX this should be a search for multiple uids - need to reimplement caldav search

    List<FetchItemResponseType> firs = new ArrayList<>();

    for (String href: hrefs) {
      firs.add(fetchItem(href));
    }

    return firs;
  }

  @Override
  public UpdateItemResponseType updateItem(final UpdateItemType updates) throws NoteException {
    return null;
  }

  /* ====================================================================
   *                   Private methods
   * ==================================================================== */
}
