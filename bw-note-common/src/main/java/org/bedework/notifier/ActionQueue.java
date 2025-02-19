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

import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.bedework.notifier.Noteling.StatusType;

/** Handle processing of actions. These are messages indicating new
 * notificatiosn and the resulting actions.
 *
 *   @author Mike Douglass   douglm   bedework.org
 */
public class ActionQueue extends Thread implements Logged {
  /* Some counts */

  private final StatLong actionsCt;

  private long lastTrace;

  private final BlockingQueue<Action> actionQueue;

  private final NotifyEngine notifier;

  private final NotelingPool notelingPool;

  private boolean stopping;

  /**
   */
  public ActionQueue(final NotifyEngine notifier,
                     final String name,
                     final NotelingPool notelingPool) {
    super(name);

    this.notifier = notifier;
    this.notelingPool = notelingPool;
    actionQueue = new ArrayBlockingQueue<>(100);
    actionsCt = new StatLong(name + ".actions");
  }

  /**
   * @param action to take
   */
  public void queueAction(final Action action) {
    try {
      while (!stopping) {
        if (actionQueue.offer(action, 5, TimeUnit.SECONDS)) {
          if (debug()) {
            debug("Action accepted");
          }
          break;
        }
        if (debug()) {
          debug("Action queue timedout: retrying");
        }
      }
    } catch (final InterruptedException ignored) {
    }
  }

  /**
   * @param stats for notify service bean
   */
  public void getStats(final List<Stat> stats) {
    stats.add(actionsCt);
  }

  @Override
  public void run() {
    int exceptions = 0;

    while (true) {
      if (debug()) {
        debug("About to wait for action");
      }

      try {
        final Action action = actionQueue.take();
        if (action == null) {
          continue;
        }

        if (debug()) {
          debug("Received action");
        }

          /*
          if ((note.getSub() != null) && note.getSub().getDeleted()) {
            // Drop it

            if (debug()) {
              debug("Dropping deleted notification");
            }

            continue;
          }*/

        actionsCt.inc();
        Noteling noteling = null;

        try {
            /* Get a noteling from the pool */
          while (true) {
            if (stopping) {
              return;
            }

            noteling = notelingPool.getNoException();
            if (noteling != null) {
              break;
            }
          }

            /* TODO The noteling needs to be running it's own thread. */
          final StatusType st = noteling.handleAction(action);

          if (st == StatusType.Reprocess) {
              /* Delay then eventually back on the queue to be processed again.
               */

            notifier.reschedule(action);
            continue;
          }

          if (st == StatusType.Warning) {
              /* Delay then eventually back on the queue to be processed again.
               */

            notifier.reschedule(action);
          }

          exceptions = 0; // somethings working
        } finally {
          notelingPool.add(noteling);
        }

          /* If this is a poll kind then we should add it to a poll queue
           */
        // XXX Add it to poll queue
      } catch (final InterruptedException ie) {
        info("Notification handler shutting down");
        break;
      } catch (final Throwable t) {
        exceptions++;
        if (debug()) {
          error(t);
        } else {
          // Try not to flood the log with error traces
          final long now = System.currentTimeMillis();
          if ((now - lastTrace) > (30 * 1000)) {
            error(t);
            lastTrace = now;
          } else {
            error(t.getMessage());
          }
        }

        if (exceptions > 10) {
          break; // Nothings working apparently.
        }
      }
    }
  }

  public void shutdown() {
    stopping = true;
    interrupt();
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
