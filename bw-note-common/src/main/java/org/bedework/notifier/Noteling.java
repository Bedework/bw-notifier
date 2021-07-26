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
import org.bedework.notifier.db.NotifyDb;
import org.bedework.notifier.db.Subscription;
import org.bedework.notifier.exception.NoteException;
import org.bedework.notifier.notifications.Note;
import org.bedework.notifier.outbound.common.Adaptor;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;
import org.bedework.util.misc.Util;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.bedework.notifier.NotifyEngine.NotificationMsg;

/** The noteling handles the processing of a single notification
 *
 * <p>These will be multi-threaded as sending an invitation can be a
 * lengthy process. We have a pool of notelings to limit the max.
 *
 * <p>The notification engine will combine and schedule notifications
 * and will be signalled by the noteling when it has done it's job.
 * </p>.
 *
 * <p>A single noteling may have it's own queue of outbound
 * notifications - possibly to the same destination or to a subset
 * of destinations</p>
 *
 * @author Mike Douglass
 */
public class Noteling implements Logged {
  private static final AtomicLong lastNotelingid = new AtomicLong(0);

  private final long notelingId;

  private final NotifyEngine notifier;

  private final NotifyDb db;

  public enum StatusType {
    OK,

    Warning,

    Reprocess
  }

  /** Constructor
   *
   * @param notifier the notifier engine
   * @throws NoteException on error
   */
  public Noteling(final NotifyEngine notifier) throws NoteException {
    this.notifier = notifier;
    db = NotifyEngine.getNewDb();

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
   * @throws NoteException on error
   */
  public StatusType handleAction(final Action action) throws NoteException {
    try {
      db.startTransaction();

      switch (action.getType()) {
        case notificationMsg:
          handleNotificationMsg(action);
          break;

        case checkItems:
          return handleCheck(action);

        case processItem:
          handleProcessItem(action);
          break;

        case processOutbound:
          return doOutBound(action);
      }

      return StatusType.OK;
    } finally {
      db.endTransaction();
    }
  }

  /* ====================================================================
   *                        private Notification methods
   * ==================================================================== */

  private void handleNotificationMsg(final Action action) throws NoteException {
    final NotificationMsg msg = action.getMsg();

    final Subscription sub = db.find(msg.getSystem(),
                                     msg.getHref());

    if (sub == null) {
      if (debug()) {
        debug("No subscription for " + msg.getHref() + " (" + msg.getSystem() + "), not processing notification " + msg.getResourceName() + ".");
      }
      // Not one of ours
      return;
    }

    action.setSub(sub);
    notifier.setConnectors(action);
    action.setType(ActionType.checkItems);

    notifier.handleAction(action);
  }

  private StatusType handleCheck(final Action action) throws NoteException {
    try {
      final ConnectorInstance ci = notifier.reserveInstance(db,
                                                            action);
      if (ci == null) {
        // Been queued
        return StatusType.OK;
      }

      if (!ci.check(db, action.getMsg().getResourceName())) {
        // No new notifications
        if (debug()) {
          debug("No new notifications matching resource: " + action.getMsg().getResourceName());
        }
        return StatusType.Reprocess;
      }

      action.setType(ActionType.processItem);
      notifier.handleAction(action);
    } finally {
      notifier.release(action.getSub());
    }
    return StatusType.OK;
  }

  private void handleProcessItem(final Action action) throws NoteException {
    try {
      final ConnectorInstance ci = notifier.reserveInstance(db,
                                                            action);

      final Subscription sub = action.getSub();

      final Note note = ci.nextItem(db);

      if (note == null) {
        // We're done.
        return;
      }

      if (debug()) {
        debug("Got notification " + note);
      }

      final Action act = new Action(ActionType.processOutbound,
                                    sub, note);

      notifier.handleAction(act);

      // Requeue ourself for the next item
      notifier.handleAction(action);
    } finally {
      notifier.release(action.getSub());
    }
  }

  private StatusType doOutBound(final Action action) throws NoteException {
    final List<Adaptor<?>> adaptors =
            notifier.getAdaptors(action);

    if (Util.isEmpty(adaptors)) {
      warn("No adaptor for " + action);
      // TODO - delete it?
      return StatusType.OK;
    }

    /* We attempt to process the action with all the adaptors.
       Each adaptor can signal done or not completed. Done might mean
       success or a permanent error.

       Not completed means we should retry it.

       Each adaptor should put it's status into the processors section
       of the notification. When processing it should check this status
       and just return done if it's already processed.
     */
    boolean allOk = true;

    try {
      for (final Adaptor<?> adaptor: adaptors) {
        if (!adaptor.process(action)) {
          allOk = false;
        }
      }
    } finally {
      notifier.releaseAdaptors(adaptors);
    }

    if (!allOk) {
      return StatusType.Reprocess;
    }

    try {
      final ConnectorInstance ci = notifier.reserveInstance(db,
                                                            action);

      ci.completeItem(db,
                      action.getNote());
    } finally {
      notifier.release(action.getSub());
    }

    return StatusType.OK;
  }

  /* ====================================================================
   *                   Logged methods
   * ==================================================================== */

  private final BwLogger logger = new BwLogger();

  @Override
  public BwLogger getLogger() {
    if ((logger.getLoggedClass() == null) && (logger.getLoggedName() == null)) {
      logger.setLoggedClass(getClass());
    }

    return logger;
  }
}
