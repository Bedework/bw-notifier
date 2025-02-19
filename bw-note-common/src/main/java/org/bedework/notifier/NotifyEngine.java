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
import org.bedework.notifier.cnctrs.ConnectorInstance;
import org.bedework.notifier.conf.NotifyConfig;
import org.bedework.notifier.db.NotifyDb;
import org.bedework.notifier.db.NotifyDbImpl;
import org.bedework.notifier.db.Subscription;
import org.bedework.notifier.exception.NoteException;
import org.bedework.notifier.notifications.Note;
import org.bedework.notifier.outbound.common.Adaptor;
import org.bedework.util.calendar.XcalUtil.TzGetter;
import org.bedework.util.config.ConfigException;
import org.bedework.util.config.ConfigurationStore;
import org.bedework.util.jmx.ConfigHolder;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;
import org.bedework.util.misc.Util;
import org.bedework.util.security.PwEncryptionIntf;
import org.bedework.util.timezones.Timezones;
import org.bedework.util.timezones.TimezonesImpl;

import net.fortuna.ical4j.model.TimeZone;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Notification processor.
 * <p>The notification processor manages the notification service.
 *
 * <p>There are two ends to a subscription handled by connectors.
 *
 * <p>blah blah</p>
 *
 * @author Mike Douglass
 */
public class NotifyEngine implements Logged, TzGetter {
  static ConfigHolder<NotifyConfig> cfgHolder;

  private transient PwEncryptionIntf pwEncrypt;

  private final ActionWaiter waitingActions = new ActionWaiter();
  private boolean starting;

  private boolean running;

  private boolean stopping;

  //private Configurator config;

  private final static Object getNotifierLock = new Object();

  private static NotifyEngine notifier;

  private Timezones timezones;

  static TzGetter tzgetter;

  private NotelingPool notelingPool;

  private AdaptorPool adaptorPool;

  private NotifyTimer notifyTimer;

  private NotifyDb db;

  /** Queue and process inbound actions.
   *
   */
  private static ActionQueue actionInHandler;

  /** Queue and process outbound actions.
   *
   */
  private static ActionQueue actionOutHandler;

  public static class NotificationMsg {
    private final String system;
    private final String href;
    private final String resourceName;

    public NotificationMsg(final String system,
                           final String href,
                           final String resourceName) {
      this.system = system;
      this.href = href;
      this.resourceName = resourceName;
    }

    public String getSystem() {
      return system;
    }

    public String getHref() {
      return href;
    }

    public String getResourceName() {
      return resourceName;
    }
  }

  /**
   *
   * @param val add some messages to the queue
   */
  public void addNotificationMsg(final NotificationMsg val) {
    try {
      final Action action = new Action(Action.ActionType.notificationMsg,
                                       val);

      handleAction(action);
    } catch (final Throwable t) {
      throw new NoteException(t);
    }
  }

  /** Constructor
   *
   */
  private NotifyEngine() {
    System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump",
                       String.valueOf(debug()));
  }

  /**
   * @return the notification engine
   */
  public static NotifyEngine getNotifier() {
    if (notifier != null) {
      return notifier;
    }

    synchronized (getNotifierLock) {
      if (notifier != null) {
        return notifier;
      }
      notifier = new NotifyEngine();
      return notifier;
    }
  }

  /**
   * @param val the config holder
   */
  public static void setConfigHolder(final ConfigHolder<NotifyConfig> val) {
    cfgHolder = val;
  }

  /**
   * @return current state of the configuration
   */
  public static NotifyConfig getConfig() {
    if (cfgHolder == null) {
      return null;
    }

    return cfgHolder.getConfig();
  }

  public static NotifyDb getNewDb() {
    return new NotifyDbImpl(getConfig());
  }

  public static boolean authenticate(final String system,
                              final String token) {
    final NotifyRegistry.Info info = NotifyRegistry.getInfo(system);

    return info != null &&
            info.getAuthenticator().authenticate(token);
  }

  /**
   * @return configuration store
   */
  public static ConfigurationStore getConfigStore() {
    if (cfgHolder == null) {
      return null;
    }

    try {
      return cfgHolder.getStore();
    } catch (final ConfigException ce) {
      throw new NoteException(ce);
    }
  }

  /**
   */
  public void updateConfig() {
    if (cfgHolder != null) {
      cfgHolder.putConfig();
    }
  }

  /** Get a timezone object given the id. This will return transient objects
   * registered in the timezone directory
   *
   * @param id tzid
   * @return TimeZone with id or null
   * @throws Throwable on error
   */
  @Override
  public TimeZone getTz(final String id) throws Throwable {
     return getNotifier().timezones.getTimeZone(id);
   }

  /** Start notify process.
   *
   */
  public void start() {
    try {
      if (starting || running) {
        warn("Start called when already starting or running");
        return;
      }

      synchronized (this) {
        starting = true;
      }

      db = getNewDb();
      db.startTransaction();
      db.clearTransients();
      db.endTransaction();

      timezones = new TimezonesImpl();
      timezones.init(getConfig().getTimezonesURI());

      tzgetter = this;

      notelingPool = new NotelingPool();
      notelingPool.start(this,
                          getConfig().getNotelingPoolSize(),
                          getConfig().getNotelingPoolTimeout());

      actionInHandler = new ActionQueue(this, "actionIn", notelingPool);
      actionOutHandler = new ActionQueue(this, "actionOut", notelingPool);

      info("**************************************************");
      info("Starting notifier");
      info("      callback URI: " + getConfig().getCallbackURI());
      info("**************************************************");

      if (getConfig().getKeystore() != null) {
        System.setProperty("javax.net.ssl.trustStore", getConfig().getKeystore());
        System.setProperty("javax.net.ssl.trustStorePassword", "bedework");
      }

      info("Register and start connectors");
      final NotifyRegistry registry = new NotifyRegistry();
      registry.registerConnectors(getConfig());
      registry.startConnectors(db, this);

      info("Register and start adaptors");
      adaptorPool = new AdaptorPool(this, 1000 * 60);
      adaptorPool.registerAdaptors();

      notifyTimer = new NotifyTimer(this);

      /* Get the list of subscriptions from our database and process them.
       * While starting, new subscribe requests get added to the list.
       */

      info("Start action handlers");
      actionInHandler.start();
      actionOutHandler.start();

      info("**************************************************");
      info("Notifier started");
      info("**************************************************");
    } catch (final NoteException se) {
      error(se);
      starting = false;
      running = false;
      throw se;
    } catch (final Throwable t) {
      error(t);
      starting = false;
      running = false;
      throw new NoteException(t);
    }
  }

  /* * Reschedule all subscriptions. This might blow things up at the
   * moment. It should do a paged request and space them out.
   *
   * /
  public void reschedule() {
    try {
      db.open();
      List<Subscription> rescheduleList = db.getAll();
      db.close();


      if (Util.isEmpty(rescheduleList)) {
        if (debug()) {
          debug("rescheduleList is empty");
        }
        return;
      }

      if (debug()) {
        debug("rescheduleList has " + rescheduleList.size() + " subscriptions");
      }

      for (final Subscription sub: rescheduleList) {
        setConnectors(sub);

        reschedule(sub);
      }
    } finally {
      if ((db != null) && db.isOpen()) {
        db.close();
      }
    }
  }*/

  /** Reschedule an action for retry.
   *
   * @param act the action
   */
  public void reschedule(final Action act) {
    if (act.getRetries() < 10) {
      act.setRetries(act.getRetries()+1);
      if (debug()) {
        debug("reschedule action after error, attempt #" + act.getRetries() + ": " + act.getSub());
      }
      notifyTimer.schedule(act, 1 * 60 * 1000);  // 1 minute
    } else {
      if (debug()) {
        debug("not rescheduling action after " + act.getRetries() + " attempts: " + act.getSub());
      }
    }
  }

  public void queueNotification(final Note note) {

  }

  /**
   * @return true if we're running
   */
  public boolean getRunning() {
    return running;
  }

  /**
   * @return stats for notify service bean
   */
  public List<Stat> getStats() {
    final List<Stat> stats = new ArrayList<>();

    stats.addAll(notelingPool.getStats());
    stats.addAll(notifyTimer.getStats());

    actionInHandler.getStats(stats);
    actionOutHandler.getStats(stats);

    return stats;
  }

  /** Stop notify process.
   *
   */
  public void stop() {
    if (stopping) {
      return;
    }

    stopping = true;

    /* Call stop on each connector
     */
    for (final Connector<?, ?, ?> conn: NotifyRegistry.getConnectors()) {
      info("Stopping connector " + conn.getId());
      try {
        conn.stop();
      } catch (final Throwable t) {
        if (debug()) {
          error(t);
        } else {
          error(t.getMessage());
        }
      }
    }

    info("Connectors stopped");

    if (actionInHandler != null) {
      actionInHandler.shutdown();
    }
    
    if (actionOutHandler != null) {
      actionOutHandler.shutdown();
    }
    
    if (notelingPool != null) {
      notelingPool.stop();
    }

    notifier = null;

    info("**************************************************");
    info("Notifier shutdown complete");
    info("**************************************************");
  }

  /**
   * @param action to take
   */
  public void handleAction(final Action action) {
    setConnectors(action);
    if (Objects.requireNonNull(
            action.getType()) == Action.ActionType.notificationMsg) {
      actionInHandler.queueAction(action);
    } else {
      actionOutHandler.queueAction(action);
    }
  }

  /**
   * @param val to decrypt
   * @return decrypted string
   */
  public String decrypt(final String val) {
    try {
      return getEncrypter().decrypt(val);
    } catch (final NoteException se) {
      throw se;
    } catch (final Throwable t) {
      throw new NoteException(t);
    }
  }

  /**
   * @return en/decryptor
   */
  public PwEncryptionIntf getEncrypter() {
    if (pwEncrypt != null) {
      return pwEncrypt;
    }

    try {
      final String pwEncryptClass = "org.bedework.util.security.PwEncryptionDefault";
      //String pwEncryptClass = getSysparsHandler().get().getPwEncryptClass();

      pwEncrypt = (PwEncryptionIntf)Util.getObject(pwEncryptClass,
                                                   PwEncryptionIntf.class);

      pwEncrypt.init(getConfig().getPrivKeys(),
                     getConfig().getPubKeys());

      return pwEncrypt;
    } catch (final Throwable t) {
      t.printStackTrace();
      throw new NoteException(t);
    }
  }

  /** Gets an instance and implants it in the subscription object.
   * This method serializes use of a subscription. Once a
   * subscription is in use any further actions will be queued and
   * released when the release method is called.
   *
   * @param db - for db interactions
   * @param action an action
   * @return ConnectorInstance or throws Exception
   */
  public synchronized ConnectorInstance reserveInstance(final NotifyDb db,
                                                        final Action action) {
    ConnectorInstance cinst;
    final Connector<?, ?, ?> conn;

    if (action.getSub().reserved()) {
      waitingActions.add(action);
      return null;
    }

    action.getSub().reserve();

    cinst = action.getConnInst();
    conn = action.getConn();

    if (cinst != null) {
      return cinst;
    }

    if (conn == null) {
      throw new NoteException("No connector for " + action);
    }

    cinst = conn.getConnectorInstance(db,
                                      action.getSub());
    if (cinst == null) {
      throw new NoteException("No connector instance for " + action);
    }

    action.setConnInst(cinst);

    return cinst;
  }

  /** Release a subscription after handling a notification.
   *
   * @param sub the subscription
   */
  public void release(final Subscription sub) {
    if (debug()) {
      debug("release subscription " + sub);
    }

    sub.release();
  }

  public synchronized void release(final Action action) {
    final Action waction = waitingActions.get(action.getSub());

    if (waction != null) {
      handleAction(waction);
    }
  }

  /** When we start up a new subscription we implant a Connector in the object.
   *
   * @param action an action
   */
  public void setConnectors(final Action action) {
    if ((action.getSub() == null) || (action.getConn() != null)) {
      return;
    }

    final String connectorName = action.getSub().getConnectorName();

    final Connector<?, ?, ?> conn =
            NotifyRegistry.getConnector(connectorName);
    if (conn == null) {
      throw new NoteException("No connector for " +
                                      action.getSub() + "(");
    }

    action.setConn(conn);
  }

  /* * Processes a batch of notifications. This must be done in a timely manner
   * as a request is usually hanging on this.
   *
   * @param notes
   * /
  public void handleNotifications(
            final NotificationBatch<Notification> notes) {
    for (Notification note: notes.getNotifications()) {
      db.open();
      Noteling nl = null;

      try {
        if (note.getNotification() != null) {
          nl = notelingPool.get();

          handleAction(nl, note);
        }
      } finally {
        db.close();
        if (nl != null) {
          notelingPool.add(nl);
        }
      }
    }

    return;
  }*/

  /**
   * @param action an action that needs outbound adaptors
   * @return list of adaptors
   */
  public List<Adaptor<?>> getAdaptors(final Action action) {
    final Note note = action.getNote();
    final List<Adaptor<?>> as = new ArrayList<>();
    final List<Note.DeliveryMethod> dms = note.getDeliveryMethods();

    for (final Note.DeliveryMethod dm: dms) {
      final Adaptor<?> a = adaptorPool.getAdaptor(dm.toString());

      if (a == null) {
        continue;
      }

      as.add(a);
    }

    return as;
  }

  /**
   * @param adaptors list of adaptors
   */
  public void releaseAdaptors(final List<Adaptor<?>> adaptors) {
    for (final Adaptor<?> adaptor: adaptors) {
      adaptorPool.add(adaptor);
    }
  }

  /**
   * @param sub a subscription
   */
  public void addNotificationMsg(final Subscription sub) {
    /* Queue a message to process it */
    addNotificationMsg(
            new NotificationMsg(sub.getConnectorName(),
                                sub.getPrincipalHref(),
                                null));
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
