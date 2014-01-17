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

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/** manage a pool of synchlings.
 *
 * @author Mike Douglass
 *
 */
public class NotelingPool {
  protected transient Logger log;

  private NotifyEngine syncher;

  private ArrayBlockingQueue<Noteling> pool;

  private Map<Long, Noteling> active = new HashMap<Long, Noteling>();

  private long timeout; // millisecs

  private long waitTimes;

  private long gets;

  private long getSynchlingFailures;

  /** Create a pool with the given size
   *
   * @param syncher
   * @param size
   * @param timeout - millisecs
   * @throws org.bedework.notifier.exception.NoteException
   */
  public void start(final NotifyEngine syncher,
                    final int size,
                    final long timeout) throws NoteException {
    this.syncher = syncher;
    this.timeout = timeout;
    resize(size);
  }

  /** Shut down active synchlings
   */
  public void stop() {
    long maxWait = 1000 * 90; // 90 seconds - needs to be longer than longest poll interval
    long startTime = System.currentTimeMillis();
    long delay = 1000 * 5; // 5 sec delay

    while (getActiveCt() > 0) {
      if ((System.currentTimeMillis() - startTime) > maxWait) {
        warn("**************************************************");
        warn("Synch shutdown completed with " +
            getActiveCt() + " active synchlings");
        warn("**************************************************");

        break;
      }

      info("**************************************************");
      info("Synch shutdown - " +
           getActiveCt() + " active synchlings");
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
   * @param size
   * @throws org.bedework.notifier.exception.NoteException
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
      pool.add(new Noteling(syncher));
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

  /** Return approximate number of available synchlings
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

  /** Put a synchling back in the pool if there's room else discard it
   *
   * @param s
   * @throws org.bedework.notifier.exception.NoteException
   */
  public void add(final Noteling s) throws NoteException {
    synchronized (active) {
      active.remove(s.getNotelingId());
    }
    getPool().offer(s);
  }

  /** Get a synchling from the pool if possible
   *
   * @return a sychling
   * @throws org.bedework.notifier.exception.NoteException if none available
   */
  public Noteling get() throws NoteException {
    return get(true);
  }

  /** Get a synchling from the pool if possible. Return null if timed out
   *
   * @return a sychling or null
   * @throws org.bedework.notifier.exception.NoteException on error
   */
  public Noteling getNoException() throws NoteException {
    return get(false);
  }

  private Noteling get(final boolean throwOnFailure) throws NoteException {
    Noteling s = null;
    gets++;
    long st = System.currentTimeMillis();

    try {
      s = getPool().poll(getTimeout(), TimeUnit.MILLISECONDS);
    } catch (Throwable t) {
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

    stats.add(new Stat("synchling get timeout", getTimeout()));
    stats.add(new Stat("synchling active", getActiveCt()));
    stats.add(new Stat("synchling gets", getGets()));
    stats.add(new Stat("synchling waitTimes", getWaitTimes()));
    stats.add(new Stat("synchling get failures", getGetSynchlingFailures()));
    stats.add(new Stat("synchling currentMaxSize", getCurrentMaxSize()));
    stats.add(new Stat("synchling currentAvailable", getCurrentAvailable()));

    return stats;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append("{");

    sb.append("timeout=");
    sb.append(getTimeout());

    sb.append(", gets=");
    sb.append(getGets());

    sb.append("\n,     waitTimes=");
    sb.append(getWaitTimes());

    sb.append(", getSynchlingFailures=");
    sb.append(getGetSynchlingFailures());

    sb.append("\n,     currentMaxSize=");
    sb.append(getCurrentMaxSize());

    sb.append(", currentAvailable=");
    sb.append(getCurrentAvailable());

    sb.append("}");

    return sb.toString();
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

  private void info(final String msg) {
    getLogger().info(msg);
  }

  private void warn(final String msg) {
    getLogger().warn(msg);
  }
}
