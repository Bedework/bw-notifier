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
package org.bedework.notifier;

import org.bedework.notifier.Action.ActionType;
import org.bedework.notifier.cnctrs.ConnectorInstance;
import org.bedework.notifier.cnctrs.ConnectorInstance.NotifyItemsInfo;
import org.bedework.notifier.db.Subscription;
import org.bedework.notifier.exception.NoteException;
import org.bedework.notifier.notifications.Notification;
import org.bedework.notifier.outbound.Destination;
import org.bedework.notifier.outbound.DestinationFactory;
import org.bedework.util.misc.Util;

import org.apache.log4j.Logger;
import org.oasis_open.docs.ws_calendar.ns.soap.StatusType;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/** The noteling handles the processing of a single notification
 *
 * <p>These will be multi-threaded as sending an invitation can be a
 * lengthy process. We have a pool of notelings to limit the max.
 *
 * <p>The notification engine will combine and schedule notifications
 * and will be signalled by the noteling when it has done it's job.
 * </p>.
 *
 * <p>A single noteling will have it's own queue of outbound
 * notifications - possibly to the same destination or to a subset
 * of destinations</p>
 *
 * @author Mike Douglass
 */
public class Noteling {
  private final boolean debug;

  protected transient Logger log;

  private static final AtomicLong lastNotelingid = new AtomicLong(0);

  private final long notelingId;

  private final NotifyEngine notifier;

  /** Constructor
   *
   * @param notifier the notifier engine
   * @throws NoteException
   */
  public Noteling(final NotifyEngine notifier) throws NoteException {
    debug = getLogger().isDebugEnabled();

    this.notifier = notifier;

    notelingId = lastNotelingid.getAndIncrement();
  }

  /**
   * @return unique id
   */
  public long getNotelingId() {
    return notelingId;
  }

  /** This might add a notification to a queue or handle it directly
   * in line. We'll assume the Noteling is running on its own thread
   * and that when this method is called it could be already handling
   * a notification.
   *
   * <p>A possible response might be busy - in which case the caller
   * should either wait or try another noteling</p>
   *
   * @param action - the action to take
   * @return OK for all handled fine. ERROR - discard. WARN - retry.
   * @throws NoteException
   */
  public StatusType handleAction(final Action action) throws NoteException {
    switch (action.getType()) {
      case fetchItems:
        final Subscription sub = action.getSub();
        try {
          notifier.setConnectors(sub);
          fetchItems(sub);
        } finally {
          sub.updateLastRefresh();
          notifier.reschedule(sub);
        }
        break;

      case processOutbound:
        doOutBound(action);
        break;
    }

    return StatusType.OK;
  }

  /* ====================================================================
   *                        private Notification methods
   * ==================================================================== */

  private void fetchItems(final Subscription sub) throws NoteException {
    final ConnectorInstance ci = notifier.getConnectorInstance(sub);

    final NotifyItemsInfo nii = ci.getItemsInfo();

    if ((nii == null) || nii.items.isEmpty()) {
      return;
    }

    final List<Notification> notes = ci.fetchItems(nii.items);

    if (Util.isEmpty(notes)) {
      return;
    }

    for (final Notification note: notes) {
      if (debug) {
        trace("Got notification " + note);
      }

      Action act = new Action(ActionType.processOutbound,
                              sub, note);

      notifier.handleAction(act);
    }
  }

  private void doOutBound(Action action) throws NoteException {
    List<Destination> dests =
            DestinationFactory.getDestination(action.getNote());

    if (Util.isEmpty(dests)) {
      warn("No destination for " + action);
      // TODO - delete it?
      return;
    }

    for (Destination dest: dests) {
      dest.send(action.getNote());
    }

    // TODO - now what do we do with the notification?
  }

  /* ====================================================================
   *                        private methods
   * ==================================================================== */


  private Logger getLogger() {
    if (log == null) {
      log = Logger.getLogger(this.getClass());
    }

    return log;
  }

  private void trace(final String msg) {
    getLogger().debug(msg);
  }

  @SuppressWarnings("unused")
  private void warn(final String msg) {
    getLogger().warn(msg);
  }

  @SuppressWarnings("unused")
  private void error(final Throwable t) {
    getLogger().error(this, t);
  }

  @SuppressWarnings("unused")
  private void info(final String msg) {
    getLogger().info(msg);
  }
}
