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

import org.bedework.notifier.exception.NoteException;

import org.apache.log4j.Logger;
import org.oasis_open.docs.ws_calendar.ns.soap.StatusType;

/** The noteling handles the processing of a single notification
 *
 * <p>These will be multi-threaded as sending an invitation can be a
 * length process. We have a pool of notelings to limit the max.
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
  private boolean debug;

  protected transient Logger log;

  private static volatile Object notelingIdLock = new Object();

  private static volatile long lastNotelingid;
  private long notelingId;

  private NotifyEngine notifier;

  /** Constructor
   *
   * @param notifier
   * @throws org.bedework.notifier.exception.NoteException
   */
  public Noteling(final NotifyEngine notifier) throws NoteException {
    debug = getLogger().isDebugEnabled();

    this.notifier = notifier;

    synchronized (notelingIdLock) {
      lastNotelingid++;
      notelingId = lastNotelingid;
    }
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
   * @param note
   * @return OK for all handled fine. ERROR - discard. WARN - retry.
   * @throws org.bedework.notifier.exception.NoteException
   */
  public StatusType handleNotification(final Notification note) throws NoteException {
    StatusType st;

    // TODO - the meat of it

    return StatusType.OK;
  }

  /* ====================================================================
   *                        Notification methods
   * ==================================================================== */


  private StatusType invite(final InviteNotification invite) throws NoteException {
    if (debug) {
      trace("invite " + invite);
    }

    // TODO - the meat of it

    return StatusType.OK;
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

  private void warn(final String msg) {
    getLogger().warn(msg);
  }

  @SuppressWarnings("unused")
  private void error(final Throwable t) {
    getLogger().error(this, t);
  }

  private void info(final String msg) {
    getLogger().info(msg);
  }
}
