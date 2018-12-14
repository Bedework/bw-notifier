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
import org.bedework.notifier.exception.NoteTimeout;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;
import org.bedework.util.misc.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/** manage a pool of notelings.
 *
 * @author Mike Douglass
 *
 */
public class NotelingPool implements Logged {
  private NotifyEngine notifier;

  private ArrayBlockingQueue<Noteling> pool;

  private Map<Long, Noteling> active = new HashMap<>();

  private long timeout; // millisecs

  private long waitTimes;

  private long gets;

  private long getSynchlingFailures;

  /** Create a pool with the given size
   *
   * @param notifier the engine
   * @param size of pool
   * @param timeout - millisecs
   * @throws NoteException on error
   */
  public void start(final NotifyEngine notifier,
                    final int size,
                    final long timeout) throws NoteException {
    this.notifier = notifier;
    this.timeout = timeout;
    resize(size);
  }

  /** Shut down active notelings
   */
  public void stop() {
    long maxWait = 1000 * 90; // 90 seconds - needs to be longer than longest poll interval
    long startTime = System.currentTimeMillis();
    long delay = 1000 * 5; // 5 sec delay

    while (getActiveCt() > 0) {
      if ((System.currentTimeMillis() - startTime) > maxWait) {
        warn("**************************************************");
        warn("Synch shutdown completed with " +
            getActiveCt() + " active notelings");
        warn("**************************************************");

        break;
      }

      info("**************************************************");
      info("Synch shutdown - " +
           getActiveCt() + " active notelings");
      info("**************************************************");

      try {
        wait(delay);
      } catch (InterruptedException ie) {
        maxWait = 0; // Force exit
      }
    }
  }

  /** Resize the pool
   *
   * @param size of pool
   * @throws NoteException on error
   */
  public void resize(final int size) throws NoteException {
    ArrayBlockingQueue<Noteling> oldPool = getPool();
    pool = new ArrayBlockingQueue<Noteling>(size);
    int oldSize = 0;

    if (oldPool != null) {
      oldSize = oldPool.size();
      pool.drainTo(oldPool, Math.max(size, oldSize));
    }

    while (size > oldSize) {
      pool.add(new Noteling(notifier));
      oldSize++;
    }
  }

  /**
   * @param val timeout in millisecs
   */
  public void setTimeout(final long val) {
    timeout = val;
  }

  /**
   * @return timeout in millisecs
   */
  public long getTimeout() {
    return timeout;
  }

  /**
   * @return number active
   */
  public long getActiveCt() {
    return active.size();
  }

  /**
   * @return total waitTimes in millisecs
   */
  public long getWaitTimes() {
    return waitTimes;
  }

  /**
   * @return number of gets
   */
  public long getGets() {
    return gets;
  }

  /**
   * @return number of get failures
   */
  public long getGetSynchlingFailures() {
    return getSynchlingFailures;
  }

  /**
   * @return current size of pool
   */
  public int getCurrentMaxSize() {
    ArrayBlockingQueue<Noteling> thePool = pool;
    if (thePool == null) {
      return 0;
    }

    return thePool.size();
  }

  /** Return approximate number of available notelings
   *
   * @return current avail
   */
  public int getCurrentAvailable() {
    ArrayBlockingQueue<Noteling> thePool = pool;
    if (thePool == null) {
      return 0;
    }

    return thePool.remainingCapacity();
  }

  /** Put a noteling back in the pool if there's room else discard it
   *
   * @param s Noteling
   * @throws NoteException on error
   */
  public void add(final Noteling s) throws NoteException {
    synchronized (active) {
      active.remove(s.getNotelingId());
    }
    getPool().offer(s);
  }

  /** Get a noteling from the pool if possible
   *
   * @return a Noteling
   * @throws NoteException if none available
   */
  public Noteling get() throws NoteException {
    return get(true);
  }

  /** Get a noteling from the pool if possible. Return null if timed out
   *
   * @return a Noteling or null
   * @throws NoteException on error
   */
  public Noteling getNoException() throws NoteException {
    return get(false);
  }

  private Noteling get(final boolean throwOnFailure) throws NoteException {
    Noteling s = null;
    gets++;
    final long st = System.currentTimeMillis();

    try {
      s = getPool().poll(getTimeout(), TimeUnit.MILLISECONDS);
    } catch (final Throwable t) {
      throw new NoteException(t);
    }

    waitTimes += System.currentTimeMillis() - st;

    if (s == null) {
      getSynchlingFailures++;

      if (throwOnFailure) {
        throw new NoteTimeout("Synchling pool wait");
      }
    } else {
      synchronized (active) {
        active.put(s.getNotelingId(), s);
      }
    }

    return s;
  }

  private synchronized ArrayBlockingQueue<Noteling> getPool() {
    return pool;
  }

  /** Get the current stats
   *
   * @return List of Stat
   */
  public List<Stat> getStats() {
    List<Stat> stats = new ArrayList<Stat>();

    stats.add(new Stat("noteling get timeout", getTimeout()));
    stats.add(new Stat("noteling active", getActiveCt()));
    stats.add(new Stat("noteling gets", getGets()));
    stats.add(new Stat("noteling waitTimes", getWaitTimes()));
    stats.add(new Stat("noteling get failures", getGetSynchlingFailures()));
    stats.add(new Stat("noteling currentMaxSize", getCurrentMaxSize()));
    stats.add(new Stat("noteling currentAvailable", getCurrentAvailable()));

    return stats;
  }

  @Override
  public String toString() {
    final ToString ts= new ToString(this);

    ts.append("timeout", getTimeout());
    ts.append("gets", getGets());
    ts.append("waitTimes", getWaitTimes());
    ts.append("getNotelingFailures", getGetSynchlingFailures());
    ts.append("currentMaxSize", getCurrentMaxSize());
    ts.append("currentAvailable", getCurrentAvailable());

    return ts.toString();
  }

  /* ====================================================================
   *                   Logged methods
   * ==================================================================== */

  private BwLogger logger = new BwLogger();

  @Override
  public BwLogger getLogger() {
    if ((logger.getLoggedClass() == null) && (logger.getLoggedName() == null)) {
      logger.setLoggedClass(getClass());
    }

    return logger;
  }
}
