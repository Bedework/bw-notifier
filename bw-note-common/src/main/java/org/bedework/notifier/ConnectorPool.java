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

import org.bedework.notifier.cnctrs.Connector;
import org.bedework.notifier.conf.ConnectorConfig;
import org.bedework.notifier.exception.NoteException;
import org.bedework.notifier.service.NoteConnConf;
import org.bedework.util.misc.ToString;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

/** Manage a pool of connnectors. At the moment there are no limits
 * applied - they are just handed out as needed.
 *
 * @author Mike Douglass
 *
 */
public class ConnectorPool {
  protected transient Logger log;

  private Map<String, Connector> connectorMap = new HashMap<>();

  private NotifyEngine notifier;

  private ArrayBlockingQueue<Noteling> pool = new ArrayBlockingQueue<Noteling>(100);

  private Map<Long, Noteling> active = new HashMap<>();

  private long timeout; // millisecs timeout wait

  private long waitTimes;

  private long gets;

  private long getConnectorFailures;

  /** Create a pool
   *
   * @param notifier
   * @param timeout - millisecs
   * @throws org.bedework.notifier.exception.NoteException
   */
  public ConnectorPool(final NotifyEngine notifier,
                       final long timeout) throws NoteException {
    this.notifier = notifier;
    this.timeout = timeout;
  }

  /**
   * @return collection of connectors
   */
  public Collection<Connector> getConnectors() {
    return connectorMap.values();
  }

  /** Return a registered connector with the given id.
   *
   * @param id
   * @return connector or null.
   */
  public Connector getConnector(final String id) {
    return connectorMap.get(id);
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
  public long getGetConnectorFailures() {
    return getConnectorFailures;
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

  /** Put a connector back in the pool if there's room else discard it
   *
   * @param connector
   * @throws NoteException
   */
  public void add(final Connector connector) throws NoteException {
//    synchronized (active) {
//      active.remove(s.getNotelingId());
//    }
//    getPool().offer(s);
  }

  public void registerConnectors() throws NoteException {
    final List<NoteConnConf> connectorConfs = notifier.getConfig().getConnectorConfs();
    final String callbackUriBase = notifier.getConfig().getCallbackURI();

      /* Register the connectors and start them */
    for (final NoteConnConf scc: connectorConfs) {
      final ConnectorConfig conf = (ConnectorConfig)scc.getConfig();
      final String cnctrId = conf.getName();
      info("Register and start connector " + cnctrId);

      registerConnector(cnctrId, conf);

      final Connector conn = getConnector(cnctrId);
      scc.setConnector(conn);

      conn.start(cnctrId,
                 conf,
                 callbackUriBase + cnctrId + "/",
                 notifier);

      while (!conn.isStarted()) {
          /* Wait for it to start */
        synchronized (this) {
          try {
            this.wait(250);
          } catch (final InterruptedException e) {
            throw new NoteException(e);
          }
        }

        if (conn.isFailed()) {
          error("Connector " + cnctrId + " failed to start");
          break;
        }
      }
    }
  }

  private void registerConnector(final String id,
                                 final ConnectorConfig conf) throws NoteException {
    try {
      Class cl = Class.forName(conf.getConnectorClassName());

      if (connectorMap.containsKey(id)) {
        throw new NoteException("Connector " + id + " already registered");
      }

      final Connector c = (Connector)cl.newInstance();
      connectorMap.put(id, c);
    } catch (final Throwable t) {
      throw new NoteException(t);
    }
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

    stats.add(new Stat("connector get timeout", getTimeout()));
    stats.add(new Stat("connector active", getActiveCt()));
    stats.add(new Stat("connector gets", getGets()));
    stats.add(new Stat("connector waitTimes", getWaitTimes()));
    stats.add(new Stat("connector get failures", getGetConnectorFailures()));
    stats.add(new Stat("connector currentMaxSize", getCurrentMaxSize()));
    stats.add(new Stat("connector currentAvailable", getCurrentAvailable()));

    return stats;
  }

  @Override
  public String toString() {
    final ToString ts= new ToString(this);

    ts.append("timeout", getTimeout());
    ts.append("gets", getGets());
    ts.append("waitTimes", getWaitTimes());
    ts.append("getConnectorFailures", getGetConnectorFailures());
    ts.append("currentMaxSize", getCurrentMaxSize());
    ts.append("currentAvailable", getCurrentAvailable());

    return ts.toString();
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

  private void error(final String msg) {
    getLogger().error(msg);
  }
}
