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
package org.bedework.notifier.cnctrs;

import org.bedework.notifier.NotifyEngine;
import org.bedework.notifier.conf.ConnectorConfig;
import org.bedework.notifier.db.JsonUtil;
import org.bedework.notifier.db.NotifyDb;
import org.bedework.notifier.notifications.Note;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;

import java.util.List;

/** A special connector to handle calls to the notifier engine via the web context.
 *
 * <p>This is the way to call the system to add subscriptions, to unsubscribe etc.
 *
 * @author Mike Douglass
 *
 * @param <T> Connector subclass
 * @param <TI> Connector instance subclass
 * @param <TN> Notification subclass
 * @param <Tconf> Configuration class
 */
public abstract class AbstractConnector<T,
                                        TI extends AbstractConnectorInstance,
                                        TN extends Note,
                                        Tconf extends ConnectorConfig>
        extends JsonUtil
        implements Logged, Connector<TI, TN, Tconf> {
  protected Tconf config;

  protected String callbackUri;

  private String connectorName;

  protected NotifyEngine notifier;

  protected boolean running;

  protected boolean stopped;

  /**
   * @return the connector id
   */
  public String getConnectorName() {
    return connectorName;
  }

  /**
   * @return the connector config
   */
  public Tconf getConnectorConfig() {
    return config;
  }

  @Override
  public void init(final String name,
                      final Tconf config) {
    connectorName = name;
    this.config = config;
  }

  @Override
  public void start(final NotifyDb db,
                    final String callbackUri,
                    final NotifyEngine notifier) {
    this.notifier = notifier;
    this.callbackUri = callbackUri;
  }

  @Override
  public String getStatus() {
    final StringBuilder sb = new StringBuilder();

    if (isManager()) {
      sb.append("(Manager): ");
    }

    if (isStarted()) {
      sb.append("Started: ");
    }

    if (isFailed()) {
      sb.append("Failed: ");
    }

    if (isStopped()) {
      sb.append("Stopped: ");
    }

    return sb.toString();
  }

  @Override
  public boolean isStarted() {
    return running;
  }

  @Override
  public boolean isFailed() {
    return false;
  }

  @Override
  public boolean isStopped() {
    return stopped;
  }

  @Override
  public String getId() {
    return connectorName;
  }

  @Override
  public String getCallbackUri() {
    return callbackUri;
  }

  @Override
  public NotifyEngine getNotifier() {
    return notifier;
  }

  @Override
  public List<Object> getSkipList() {
    return null;
  }

  @Override
  public void stop() {
    running = false;
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
