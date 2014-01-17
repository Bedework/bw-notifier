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
package org.bedework.notifier.cnctrs.bedework;

import org.bedework.notifier.Notification;
import org.bedework.notifier.NotifyDefs.NotifyKind;
import org.bedework.notifier.NotifyEngine;
import org.bedework.notifier.cnctrs.AbstractConnector;
import org.bedework.notifier.cnctrs.ConnectorInstanceMap;
import org.bedework.notifier.conf.ConnectorConfig;
import org.bedework.notifier.db.Subscription;
import org.bedework.notifier.exception.NoteException;

import org.oasis_open.docs.ws_calendar.ns.soap.GetPropertiesResponseType;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** The synch processor connector for connections to bedework.
 *
 * @author Mike Douglass
 */
public class BedeworkConnector
      extends AbstractConnector<BedeworkConnector,
                                BedeworkConnectorInstance,
                                Notification,
                                BedeworkConnectorConfig> {
  /* If non-null this is the token we currently have for bedework */
  private String remoteToken;

  private GetPropertiesResponseType sysInfo;

  private ConnectorInstanceMap<BedeworkConnectorInstance> cinstMap =
      new ConnectorInstanceMap<BedeworkConnectorInstance>();

  /**
   */
  public BedeworkConnector() {
  }

  /** This process will send keep-alive notifications to the remote system.
   * During startup the first notification is sent so this process starts with
   * a wait
   *
   */
  private class PingThread extends Thread {
    boolean showedTrace;

    BedeworkConnector conn;

    /**
     * @param name - for the thread
     * @param conn
     */
    public PingThread(final String name,
                      final BedeworkConnector conn) {
      super(name);
      this.conn = conn;
    }

    @Override
    public void run() {
      while (!conn.isStopped()) {
        if (debug) {
          trace("About to call service - token = " + remoteToken);
        }
        /* First see if we need to reinitialize or ping */

        try {
          if (remoteToken == null) {
//            initConnection();
            if (remoteToken != null) {
              running = true;
            }
          } else {
  //          ping();
          }
        } catch (Throwable t) {
          if (!showedTrace) {
            error(t);
            showedTrace = true;
          } else {
            error(t.getMessage());
          }
        }

        // Wait a bit before trying again

        if (debug) {
          trace("About to pause - token = " + remoteToken);
        }

        try {
          Object o = new Object();
          long waitTime;

          if (remoteToken == null) {
            waitTime = config.getRetryInterval() * 1000;
          } else {
            waitTime = config.getKeepAliveInterval() * 1000;
          }

          synchronized (o) {
            o.wait(waitTime);
          }
        } catch (InterruptedException ie) {
          break;
        } catch (Throwable t) {
          error(t.getMessage());
        }
      }
    }
  }

  private PingThread pinger;

  @Override
  public void start(final String connectorId,
                    final ConnectorConfig conf,
                    final String callbackUri,
                    final NotifyEngine syncher) throws NoteException {
    super.start(connectorId, conf, callbackUri, syncher);

    config = (BedeworkConnectorConfig)conf;

    if (pinger == null) {
      pinger = new PingThread(connectorId, this);
      pinger.start();
    }

    stopped = false;
    running = true;
  }

  @Override
  public boolean isManager() {
    return false;
  }

  @Override
  public NotifyKind getKind() {
    return NotifyKind.notify;
  }

  @Override
  public boolean isReadOnly() {
    return config.getReadOnly();
  }

  @Override
  public boolean getTrustLastmod() {
    return config.getTrustLastmod();
  }

  @Override
  public BedeworkConnectorInstance getConnectorInstance(final Subscription sub) throws NoteException {
    if (!running) {
      return null;
    }

    BedeworkConnectorInstance inst = cinstMap.find(sub);

    if (inst != null) {
      return inst;
    }

    BedeworkSubscriptionInfo info;

    info = new BedeworkSubscriptionInfo(sub.getSourceConnectorInfo());

    inst = new BedeworkConnectorInstance(config,
                                         this, sub, info);
    cinstMap.add(sub, inst);

    return inst;
  }

  class BedeworkNotificationBatch extends NotificationBatch<Notification> {
  }

  @Override
  public BedeworkNotificationBatch handleCallback(final HttpServletRequest req,
                                     final HttpServletResponse resp,
                                     final List<String> resourceUri) throws NoteException {
    return null;
  }

  @Override
  public void respondCallback(final HttpServletResponse resp,
                              final NotificationBatch<Notification> notifications)
                                                    throws NoteException {
  }

  @Override
  public void stop() throws NoteException {
    stopped = true;
    if (pinger != null) {
      pinger.interrupt();
    }

    pinger = null;
  }

  /* ====================================================================
   *                         Package methods
   * ==================================================================== */

  /* ====================================================================
   *                   Private methods
   * ==================================================================== */
}