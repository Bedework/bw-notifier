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

import org.bedework.caldav.util.notifications.BaseNotificationType;
import org.bedework.caldav.util.notifications.NotificationType;
import org.bedework.caldav.util.notifications.admin.AdminNoteParsers;
import org.bedework.caldav.util.notifications.admin.AdminNotificationType;
import org.bedework.caldav.util.notifications.eventreg.EventregBaseNotificationType;
import org.bedework.caldav.util.notifications.eventreg.EventregParsers;
import org.bedework.caldav.util.notifications.parse.Parser;
import org.bedework.caldav.util.notifications.suggest.SuggestBaseNotificationType;
import org.bedework.caldav.util.notifications.suggest.SuggestParsers;
import org.bedework.notifier.cnctrs.AbstractConnectorInstance;
import org.bedework.notifier.cnctrs.Connector;
import org.bedework.notifier.db.NotifyDb;
import org.bedework.notifier.db.Subscription;
import org.bedework.notifier.exception.NoteException;
import org.bedework.notifier.notifications.Note;
import org.bedework.notifier.notifications.Note.DeliveryMethod;
import org.bedework.util.dav.DavUtil;
import org.bedework.util.dav.DavUtil.DavChild;
import org.bedework.util.dav.DavUtil.DavProp;
import org.bedework.util.http.Headers;
import org.bedework.util.http.HttpUtil;
import org.bedework.util.http.PooledHttpClient;
import org.bedework.util.http.PooledHttpClient.ResponseHolder;
import org.bedework.util.misc.Util;
import org.bedework.util.timezones.Timezones;
import org.bedework.util.timezones.TimezonesImpl;
import org.bedework.util.xml.XmlUtil;
import org.bedework.util.xml.tagdefs.AppleServerTags;
import org.bedework.util.xml.tagdefs.WebdavTags;
import org.bedework.webdav.servlet.shared.UrlHandler;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Summary;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.w3c.dom.Element;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

import static org.apache.http.HttpStatus.SC_OK;

/** Handles bedework notifier interactions.
 *
 * @author Mike Douglass
 */
public class BedeworkConnectorInstance extends AbstractConnectorInstance {
  @SuppressWarnings("unused")
  private final BedeworkConnectorConfig config;

  private final BedeworkConnector cnctr;

  private PooledHttpClient client;

  private DavUtil dav;

  private static final Collection<QName> noteTypeProps =
          new ArrayList<>();
  static {
    noteTypeProps.add(AppleServerTags.notificationtype);

    // Force registration of parsers
    new SuggestParsers();
    new EventregParsers();
    new AdminNoteParsers();
  }

  protected static Timezones timezones = new TimezonesImpl();

  static {
    try {
      Timezones.initTimezones("http://localhost:8080/tzsvr");
    } catch (final Throwable t) {
      throw new RuntimeException(t);
    }
  }

  BedeworkConnectorInstance(final BedeworkConnectorConfig config,
                            final BedeworkConnector cnctr,
                            final Subscription sub) {
    super(sub);
    this.config = config;
    this.cnctr = cnctr;
  }

  @Override
  public Connector getConnector() {
    return cnctr;
  }

  @Override
  public boolean changed() {
    /* This implementation needs to at least check the change token for the
     * collection and match it against the stored token.
     */
    return false;
  }

  @Override
  public boolean check(final NotifyDb db, final String resource) {
    /* Will do a query on the configured resource directory and add
       a list of hrefs for notifications.

       This could be a filtered query to only return the resource types
       we want. For the moment just hope the collection is small.
     */

    try {
      /* The URI stored in the info is located by doing a PROPFIND on
       * the user principal. If we already fetched it we'll use it.
       *
       * TODO We should allow for it changing
       */

      if (sub.getUri() == null) {
        if (!getUri()) {
          return false;
        }
      }

      final String syncToken = getBwsub().getSynchToken();

      final Collection<DavChild> chs = getDav().
              syncReport(getClient(), sub.getUri(), syncToken, noteTypeProps);

      if (Util.isEmpty(chs)) {
        return false;
      }

      boolean found = false;
      String newSyncToken = null;

      getBwsub().getNoteHrefs().clear(); // Clear out hrefs in case we are retrying.
      for (final DavChild ch: chs) {
        if (ch.status != HttpServletResponse.SC_OK) {
          // Notification deleted. Don't think we care.
          continue;
        }

        if ((ch.propVals.size() == 1) &&
                ch.propVals.get(0).name.equals(WebdavTags.syncToken)) {
          newSyncToken = XmlUtil.getElementContent(ch.propVals.get(0).element);
          continue;
        }

        final DavProp dp = ch.findProp(AppleServerTags.notificationtype);

        if (dp == null) {
          continue;
        }

        getBwsub().getNoteHrefs().add(ch.uri);
        found = found || (resource != null && !resource.isEmpty() && ch.uri.endsWith(resource));
      }

      if (found && newSyncToken != null) {
        getBwsub().setSynchToken(newSyncToken);
      }
      db.update(sub);

      return found;
    } catch (final NoteException ne ) {
      throw ne;
    } catch (final Throwable t) {
      throw new NoteException(t);
    } finally {
      if (getClient() != null){
        getClient().release();
      }
    }
  }

  private UrlHandler urlHandler;

  private UrlHandler getUrlHandler() {
    if (urlHandler != null) {
      return urlHandler;
    }

    final URL sysUrl;
    try {
      sysUrl = new URL(config.getSystemUrl());
    } catch (final Throwable t) {
      error(t);
      throw new RuntimeException(t);
    }

    final String context = sysUrl.getPath();
    String urlPrefix = sysUrl.toString();
    if (context != null) {
      urlPrefix = urlPrefix.substring(0,
                                      urlPrefix.length() - context
                                              .length());
    }

    urlHandler = new UrlHandler(urlPrefix, context, false);
    return urlHandler;
  }

  @Override
  public Note nextItem(final NotifyDb db) {
    if (Util.isEmpty(getBwsub().getNoteHrefs())) {
      return null;
    }

    final String noteHref = getBwsub().getNoteHrefs().get(0);

    if (debug()) {
      debug("Fetch item " + noteHref);
    }

    try {
      final ResponseHolder<?> resp =
              getClient().get(noteHref,
                              "application/xml",
                              this::processGetItem);

      if (resp.failed) {
        return null;
      }

      /* TODO At this stage we should move the href to a pending list and a
         later action will remove it from that list.

         At restart we put the pending list back on the unprocessed list.

         For the moment just remove it.
       */

      getBwsub().getNoteHrefs().remove(0);
      db.update(sub);

      return (Note)resp.response;
    } catch (final Throwable t) {
      throw new NoteException(t);
    } finally {
      if (getClient() != null){
        getClient().release();
      }
    }
  }

  @Override
  public boolean completeItem(final NotifyDb db,
                              final Note note) {
    /* Because we do a sync-report on teh collection - we won't see the
     * notification unless it changes.
     *
     * Sharing invites will be removed when processed.
     *
     * Sharing responses could be removed.
     *
     * Change notifications we might want to remove.
     *
     */
    final NotificationType notification = note.getNotification();
    final QName noteType = notification.getNotification().getElementName();

    if (noteType.equals(AppleServerTags.resourceChange)) {
      deleteItem(note);
    } else {
      replaceItem(note);
    }

    return true;
  }

  /* ==============================================================
   *                   Private methods
   * ============================================================== */

  private Map<String, Object> checkExtraValues(final NotificationType nt) {
    final BaseNotificationType bnt = nt.getNotification();

    String href = null;

    getHref:
    {
      if (bnt instanceof final SuggestBaseNotificationType sbnt) {
        href = sbnt.getHref();
        break getHref;
      }

      if (bnt instanceof final AdminNotificationType adnt) {
        href = adnt.getHref();
        break getHref;
      }

      if (bnt instanceof final EventregBaseNotificationType ebnt) {
        href = ebnt.getHref();
        break getHref;
      }

      /*
      if (bnt instanceof ResourceChangeType) {
        final ResourceChangeType rct = (ResourceChangeType)bnt;

        if (rct.getCreated() != null) {
          final ResourceInfo ri = new ResourceInfo(rct.getCreated().getHref());
          ri.created = true;
          resourcesInfo.add(ri);
        } else if (rct.getDeleted() != null) {
          final ResourceInfo ri = new ResourceInfo(rct.getDeleted().getHref());
          ri.deleted = true;

          if (rct.getDeleted().getDeletedDetails() != null) {
            ri.summary = rct.getDeleted().getDeletedDetails()
                            .getDeletedSummary();
          }
          resourcesInfo.add(ri);
        } else if (!Util.isEmpty(rct.getUpdated())) {
          for (final UpdatedType ut: rct.getUpdated()) {
            resourcesInfo.add(new ResourceInfo(ut.getHref()));
          }
        }
        break getHref;
      }
        */
    }

    if (href == null) {
      // No event(s).
      return null;
    }

    try {
      final ResponseHolder<?> resp =
              getClient().get(href,
                              "text/calendar",
                              this::processGetEvent);

      if (resp.failed) {
        return null;
      }

      final Component comp = (Component)resp.response;
      final Map<String, Object> extraValues = new HashMap<>();

      extraValues.put("event", comp);

      final DtStart dtstart = comp.getProperty(Property.DTSTART);

      if (dtstart != null) {
        extraValues.put("dtstart", dtstart.getDate());
      }

      final DtEnd dtend = comp.getProperty(Property.DTEND);

      if (dtend != null) {
        extraValues.put("dtend", dtend.getDate());
      }

      final Summary summary = comp.getProperty(Property.SUMMARY);

      if (summary != null) {
        extraValues.put("summary", summary.getValue());
      }

      return extraValues;
    } catch (final Throwable t) {
      error(t);
      return null;
    }
  }

  final ResponseHolder<?> processGetItem(final String path,
                                      final CloseableHttpResponse resp) {
    try {
      final int status = HttpUtil.getStatus(resp);

      if (status != SC_OK) {
        return new ResponseHolder<>(status,
                                    "Failed response from server");
      }

      if (resp.getEntity() == null) {
        return new ResponseHolder<>(status,
                                    "No content in response from server");
      }

      final InputStream is = resp.getEntity().getContent();

      NotificationType nt = Parser.fromXml(is);

      final var extraValues = checkExtraValues(nt);

      nt.getNotification().unprefixHrefs(getUrlHandler());

      /* TODO try not to have to do this...
         Save it as XML and reparse it
       */

      final String strNote = nt.toXml(true);

      nt = Parser.fromXml(strNote);

      // TODO use nt.getDtstamp()?

      final ItemInfo item = new ItemInfo(path, null);
      final Note note = new Note(item, nt);

      note.setExtraValues(extraValues);

      // TODO temp - until we set this at the other end.
      note.addDeliveryMethod(DeliveryMethod.email);

      return new ResponseHolder<>(note);
    } catch (final Throwable t) {
      return new ResponseHolder<>(t);
    }
  }

  final ResponseHolder<?> processGetEvent(final String path,
                                       final CloseableHttpResponse resp) {
    try {
      final int status = HttpUtil.getStatus(resp);

      if (status != SC_OK) {
        return new ResponseHolder<>(status,
                                    "Failed response from server");
      }

      if (resp.getEntity() == null) {
        return new ResponseHolder<>(status,
                                    "No content in response from server");
      }

      final InputStream is = resp.getEntity().getContent();


      final CalendarBuilder cb =
              new CalendarBuilder(Timezones.getTzRegistry());

      final Calendar cal = cb.build(is);

      return new ResponseHolder<>(cal.getComponent(VEvent.VEVENT));
    } catch (final Throwable t) {
      return new ResponseHolder<>(t);
    }
  }

  public boolean deleteItem(final Note note) {
    final ItemInfo item = note.getItemInfo();

    try {
      final ResponseHolder<?> response = getClient().delete(item.href);

      return !response.failed;
    } catch (final Throwable t) {
      error(t);
      return false;
    } finally {
      if (getClient() != null){
        getClient().release();
      }
    }
  }

  public boolean replaceItem(final Note note) {
    try {
      final ItemInfo item = note.getItemInfo();

      final String xml = note.getNotification().toXml(true);

      final int response = getClient().put(item.href,
                                           xml,
                                           "application/xml");

      return true;
    } catch (final Throwable t) {
      error(t);
      return false;
    } finally {
      if (getClient() != null){
        getClient().release();
      }
    }
  }

  private BedeworkSubscription getBwsub() {
    return (BedeworkSubscription)sub;
  }

  private static final Collection<QName> notificationURLProps =
          Arrays.asList(WebdavTags.notificationURL,
                        AppleServerTags.notificationURL);

  private boolean getUri() {
    try {
      //cl.setBaseURI(new URI(config.getSystemUrl()));

      // Make the principal href relative
      /* We're doing this because we have a base uri that we reolve against.
         If the uri were absolute we would lose the path part of
         the base uri. Everything has to be relative to that.
       */
      final DavChild dc = getDav().getProps(getClient(),
                                            sub.getPrincipalHref().substring(1),
                                            notificationURLProps);

      if (dc == null) {
        if (debug()) {
          debug("No response getting notification collection");
        }
        // Could delete but might be dangerous - cnctr.getNotifier().deleteSubscription(sub);
        return false;
      }

      DavProp dp = dc.findProp(WebdavTags.notificationURL);

      if ((dp == null) || (dp.status != HttpServletResponse.SC_OK)) {
        dp = dc.findProp(AppleServerTags.notificationURL);
      }

      if ((dp == null) || (dp.status != HttpServletResponse.SC_OK)) {
        if (debug()) {
          debug("No notification collection");
        }
        // Could delete but might be dangerous - cnctr.getNotifier().deleteSubscription(sub);
        return false;
      }

      final Element href = XmlUtil.getOnlyElement(dp.element);
      sub.setUri(XmlUtil.getElementContent(href));

/*      try {
        cl.setBaseURI(new URI(sub.getUri()));
        return true;
      } catch (final Throwable ignored) {
        if (debug()) {
          debug("Bad uri returned: " + sub.getUri());
        }
        // Could delete but might be dangerous - cnctr.getNotifier().deleteSubscription(sub);
        return false;
      }*/
      return true;
    } catch (final Throwable t) {
      throw new NoteException(t);
    }
  }

  private PooledHttpClient getClient() {
    if (client != null) {
      return client;
    }

    try {
      client = new PooledHttpClient(new URI(config.getSystemUrl()));
      client.setHeadersFetcher(this::getAuthHeaders);

      return client;
    } catch (final Throwable t) {
      throw new NoteException(t);
    }
  }

  Headers getAuthHeaders() {
    final String userToken = getBwsub().getUserToken();

    final Headers authheaders = new Headers();

    authheaders.addAll(cnctr.getAuthHeaders());

    if (userToken == null) {
      return authheaders;
    }
    authheaders.add(new BasicHeader("X-BEDEWORK-NOTEPR", sub.getPrincipalHref()));
    authheaders.add(new BasicHeader("X-BEDEWORK-PT", userToken));

    return authheaders;
  }

  private DavUtil getDav() {
    if (dav != null) {
      return dav;
    }

    try {
      dav = new DavUtil(getAuthHeaders());
      return dav;
    } catch (final Throwable t) {
      throw new NoteException(t);
    }
  }
}
