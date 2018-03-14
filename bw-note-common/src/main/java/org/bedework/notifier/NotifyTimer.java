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
import org.bedework.util.misc.Logged;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/** Subscriptions which are waiting for a period before resynching. These are
 * generally the polled kind but other subscriptions may be made to wait before
 * retrying failed operations.
 *
 *   @author Mike Douglass   douglm   bedework.edu
 */
public class NotifyTimer extends Logged {
  private final NotifyEngine notifier;

  /** This is the class that goes into a wait. The run method MUST only take  a
   * short period or it will hang the timer. Usually it will allocate a noteling
   * then return.
   *
   */
  class NotifyTask extends TimerTask {
    private final Action action;

    NotifyTask(final Action action) {
      this.action = action;

      synchronized (waiting) {
        waiting.put(action.getSub().getSubscriptionId(), this);
        maxWaitingCt = Math.max(maxWaitingCt, waiting.size());
      }
    }

    @Override
    public void run() {
      synchronized (waiting) {
        waiting.remove(action.getSub().getSubscriptionId());
      }

      if (debug){
        debug("About to requeue action for " + 
                      action.getSub().getSubscriptionId());
      }

      try {
        notifier.handleAction(action);
      } catch (final NoteException ne) {
        if (debug) {
          error(ne);
        } else {
          error(ne.getMessage());
        }
      }
    }
  }

  private Timer timer;

  private final Map<String, NotifyTask> waiting = new HashMap<>();

  private long maxWaitingCt;

  /** Start the Timer
   *
   * @param notifier - the engine
   */
  public NotifyTimer(final NotifyEngine notifier){
    this.notifier = notifier;

    timer = new Timer("NotifyTimer", true);
  }

  /** Stop our timer thread.
   *
   */
  public void stop() {
    if (timer == null) {
      return;
    }

    timer.cancel();
    timer = null;
  }

  /** Schedule a subscription for the given time
   *
   * @param action the action
   * @param when the date/time
   * @throws NoteException on error
   */
  public void schedule(final Action action,
                       final Date when) throws NoteException {
    if (debug){
      debug("reschedule " + action.getSub().getSubscriptionId() + " for " + when);
    }

    final NotifyTask st = new NotifyTask(action);
    timer.schedule(st, when);
  }

  /** Schedule a subscription after the given delay
   *
   * @param action the action
   * @param delay - delay in milliseconds before subscription is processed.
   * @throws NoteException on error
   */
  public void schedule(final Action action,
                       final long delay) throws NoteException {
    if (debug){
      debug("reschedule " + action.getSub().getSubscriptionId() +
                    " in " + delay + " millisecs");
    }

    final NotifyTask st = new NotifyTask(action);
    timer.schedule(st, delay);
  }

  /**
   * @return number waiting
   */
  public long getWaitingCt() {
    return waiting.size();
  }

  /**
   * @return number waiting
   */
  public long getMaxWaitingCt() {
    return maxWaitingCt;
  }

  /** Get the current stats
   *
   * @return List of Stat
   */
  public List<Stat> getStats() {
    final List<Stat> stats = new ArrayList<Stat>();

    stats.add(new Stat("waiting", getWaitingCt()));
    stats.add(new Stat("max waiting", getMaxWaitingCt()));

    return stats;
  }
}
