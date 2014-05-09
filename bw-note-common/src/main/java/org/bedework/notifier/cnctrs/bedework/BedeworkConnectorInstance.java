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

import org.bedework.caldav.util.notifications.NotificationType;
import org.bedework.caldav.util.notifications.parse.Parser;
import org.bedework.notifier.cnctrs.AbstractConnectorInstance;
import org.bedework.notifier.cnctrs.Connector;
import org.bedework.notifier.db.Subscription;
import org.bedework.notifier.exception.NoteException;
import org.bedework.notifier.notifications.AppleNotification;
import org.bedework.notifier.notifications.Note;
import org.bedework.notifier.notifications.Note.DeliveryMethod;
import org.bedework.util.dav.DavUtil;
import org.bedework.util.dav.DavUtil.DavChild;
import org.bedework.util.dav.DavUtil.DavProp;
import org.bedework.util.http.BasicHttpClient;
import org.bedework.util.misc.Util;
import org.bedework.util.xml.tagdefs.AppleServerTags;

import org.apache.http.HttpException;
import org.oasis_open.docs.ws_calendar.ns.soap.DeleteItemResponseType;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

/** Handles bedework synch interactions.
 *
 * @author Mike Douglass
 */
public class BedeworkConnectorInstance extends AbstractConnectorInstance {
  @SuppressWarnings("unused")
  private final BedeworkConnectorConfig config;

  private final BedeworkConnector cnctr;

  private final BedeworkSubscriptionInfo info;

  private BasicHttpClient client;

  private static final Collection<QName> noteTypeProps =
          new ArrayList<>();
  static {
    noteTypeProps.add(AppleServerTags.notificationtype);
  }

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
    /* Will do a query on the configured resource directory and return
       a list of hrefs for notifications.

       This could be a filtered query to only return the resource types
       we want. For the moment just hope the collection is small.
     */

    final BasicHttpClient cl = getClient();

    try {
      final Collection<DavChild> chs = new DavUtil(cnctr.getAuthHeaders()).
              getChildrenUrls(cl, info.getUri(), noteTypeProps);

      if (chs == null) {
        return null;
      }

      final NotifyItemsInfo nii = new NotifyItemsInfo();
      nii.items = new ArrayList<>(chs.size());

      if (Util.isEmpty(chs)) {
        return nii;
      }

      for (final DavChild ch: chs) {
        DavProp dp = ch.findProp(AppleServerTags.notificationtype);

        if (dp == null) {
          continue;
        }

        final ItemInfo ii = new ItemInfo(ch.uri, null);
        nii.items.add(ii);
      }

      return nii;
    } catch (final NoteException ne ) {
      throw ne;
    } catch (final Throwable t) {
      throw new NoteException(t);
    } finally {
      if (cl != null){
        try {
          cl.release();
        } catch (final HttpException e) {
          error(e);
        }
        cl.close();
      }
    }
  }

  @Override
  public DeleteItemResponseType deleteItem(final ItemInfo item) throws NoteException {

    return null;
  }

  @Override
  public Note fetchItem(final ItemInfo item) throws NoteException {
    if (debug) {
      trace("Fetch item " + item.href);
    }

    final BasicHttpClient cl = getClient();

    try {
      final InputStream is = cl.get(item.href,
                                    "application/xml",
                                    cnctr.getAuthHeaders());

      final NotificationType nt = Parser.fromXml(is);

      // TODO use nt.getDtstamp()?

      Note note = new AppleNotification(item, nt);

      // TODO temp - until we set this at the other end.
      note.setDeliveryMethod(DeliveryMethod.email);

      return note;
    } catch (final Throwable t) {
      throw new NoteException(t);
    } finally {
      if (cl != null){
        try {
          cl.release();
        } catch (final HttpException e) {
          error(e);
        }
        cl.close();
      }
    }
  }

  @Override
  public List<Note> fetchItems(final List<ItemInfo> items) throws NoteException {
    // XXX this should be a search for multiple uids - need to reimplement caldav search

    final List<Note> firs = new ArrayList<>();

    /* Set the base uri here so it's only done once per batch */
    getClient().setBaseURIValue(info.getUri());

    for (final ItemInfo item: items) {
      firs.add(fetchItem(item));
    }

    return firs;
  }

  @Override
  public boolean updateItem(final Note note) throws NoteException {
    final ItemInfo item = note.getItemInfo();

    final NotificationType notification = note.getNotification();

    notification.setDtstamp(getDtstamp());

    final BasicHttpClient cl = getClient();

    try {
      String s = notification.toXml(true);

      cl.putObject(item.href, s,
                   "application/xml");

      return true;
    } catch (final Throwable t) {
      throw new NoteException(t);
    } finally {
      if (cl != null){
        try {
          cl.release();
        } catch (final HttpException e) {
          error(e);
        }
        cl.close();
      }
    }
  }

  /* ====================================================================
   *                   Private methods
   * ==================================================================== */

  private BasicHttpClient getClient() throws NoteException {
    if (client != null) {
      return client;
    }

    try {
      client = new BasicHttpClient(30 * 1000,
                                   false);  // followRedirects
      return client;
    } catch (final Throwable t) {
      throw new NoteException(t);
    }
  }
}
