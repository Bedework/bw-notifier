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
import org.bedework.notifier.outbound.common.Adaptor;
import org.bedework.notifier.outbound.common.AdaptorConf;
import org.bedework.notifier.outbound.common.AdaptorConfig;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;
import org.bedework.base.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/** Manage a pool of adaptors. At the moment there are no limits
 * applied - they are just handed out as needed.
 *
 * @author Mike Douglass
 *
 */
public class AdaptorPool implements Logged {
  private static class AdaptorState {
    AdaptorConf conf;

    ArrayBlockingQueue<Adaptor> pool ;

    Map<Long, Adaptor> active = new HashMap<>();
  }

  private final Map<String, AdaptorState> adaptorMap = new HashMap<>();

  private final NotifyEngine notifier;

  private long timeout; // millisecs timeout wait

  private long waitTimes;

  private long gets;

  private long getAdaptorFailures;

  /** Create a pool
   *
   * @param notifier the engine
   * @param timeout - millisecs
   */
  public AdaptorPool(final NotifyEngine notifier,
                     final long timeout) {
    this.notifier = notifier;
    this.timeout = timeout;
  }

  /**
   * discard everything
   */
  public void close() {
  }

  /** Return a registered adaptor with the given type.
   *
   * @param type of adaptor
   * @return adaptor or null.
   */
  public Adaptor getAdaptor(final String type) {
    try {
      final AdaptorState as = adaptorMap.get(type);

      if (as == null) {
        return null;
      }

      final Adaptor a;

      if ((as.pool.size() + as.active.size()) < as.conf.getMaxInstances()) {
        final Class<?> cl = Class.forName(as.conf.getAdaptorClassName());

        a = (Adaptor)cl.newInstance();

        a.setConf(NotifyEngine.getConfig(),
                  as.conf);

        return a;
      }

      gets++;
      final long st = System.currentTimeMillis();

      try {
        a = as.pool.poll(getTimeout(), TimeUnit.MILLISECONDS);
      } catch (final Throwable t) {
        throw new NoteException(t);
      }

      waitTimes += System.currentTimeMillis() - st;

      if (a == null) {
        getAdaptorFailures++;

      } else {
        synchronized (as.active) {
          as.active.put(a.getId(), a);
        }
      }

      return a;
    } catch (final Throwable t) {
      throw new NoteException(t);
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
    long active = 0;

    for (final AdaptorState as: adaptorMap.values()) {
      active += as.active.size();
    }

    return active;
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
  public long getGetAdaptorFailures() {
    return getAdaptorFailures;
  }

  /**
   * @return current size of pool
   */
  public int getCurrentMaxSize() {
    int size = 0;

    for (final AdaptorState as: adaptorMap.values()) {
      size += as.pool.size();
    }

    return size;
  }

  /** Return approximate number of available adaptors
   *
   * @return current avail
   */
  public int getCurrentAvailable() {
    int avail = 0;

    for (final AdaptorState as: adaptorMap.values()) {
      avail += (as.conf.getMaxInstances() - as.pool.size());
    }

    return avail;
  }

  /** Put a adaptor back in the pool if there's room else discard it
   *
   * @param adaptor the adaptor to return
   */
  public void add(final Adaptor adaptor) {
    AdaptorState as = adaptorMap.get(adaptor.getType());

    if (as == null) {
      error("Bad adaptor with type " + adaptor.getType());
      return;
    }

    synchronized (as) {
      as.active.remove(adaptor.getId());
    }

    as.pool.offer(adaptor);
  }

  public void registerAdaptors() {
    final List<AdaptorConf> adaptorConfs = NotifyEngine.getConfig().getAdaptorConfs();

    /* Register the adaptors */
    for (final AdaptorConf ac: adaptorConfs) {
      final AdaptorConfig conf = (AdaptorConfig)ac.getConfig();
      final String name = conf.getName();
      info("Register adaptor " + name + " with type " + conf.getType());

      registerAdaptor(ac);
    }
  }

  private void registerAdaptor(final AdaptorConf conf) {
    try {
      final String type = conf.getType();

      if (adaptorMap.containsKey(type)) {
        throw new NoteException("Adaptor " + type + " already registered");
      }

      final AdaptorState as = new AdaptorState();

      as.conf = conf;
      as.pool = new ArrayBlockingQueue<>(conf.getMaxInstances());

      adaptorMap.put(type, as);
    } catch (final Throwable t) {
      throw new NoteException(t);
    }
  }

  /** Get the current stats
   *
   * @return List of Stat
   */
  public List<Stat> getStats() {
    final List<Stat> stats = new ArrayList<>();

    stats.add(new Stat("adaptor get timeout", getTimeout()));
    stats.add(new Stat("adaptor active", getActiveCt()));
    stats.add(new Stat("adaptor gets", getGets()));
    stats.add(new Stat("adaptor waitTimes", getWaitTimes()));
    stats.add(new Stat("adaptor get failures", getGetAdaptorFailures()));
    stats.add(new Stat("adaptor currentMaxSize", getCurrentMaxSize()));
    stats.add(new Stat("adaptor currentAvailable", getCurrentAvailable()));

    return stats;
  }

  @Override
  public String toString() {
    final ToString ts= new ToString(this);

    ts.append("timeout", getTimeout());
    ts.append("gets", getGets());
    ts.append("waitTimes", getWaitTimes());
    ts.append("getAdaptorFailures", getGetAdaptorFailures());
    ts.append("currentMaxSize", getCurrentMaxSize());
    ts.append("currentAvailable", getCurrentAvailable());

    return ts.toString();
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
