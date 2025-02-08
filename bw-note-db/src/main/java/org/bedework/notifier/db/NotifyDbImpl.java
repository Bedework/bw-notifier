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
package org.bedework.notifier.db;

import org.bedework.base.exc.BedeworkException;
import org.bedework.database.db.DbSession;
import org.bedework.database.db.DbSessionFactoryProvider;
import org.bedework.database.db.DbSessionFactoryProviderImpl;
import org.bedework.notifier.exception.NoteException;
import org.bedework.util.config.OrmConfigBase;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/** This class manages the notification database.
 * Not clear what would go in here. Presumably a user could ask
 * for certain kinds of notification in certain ways from specified
 * sources. I guess we store that info here.
 *
 * <p>We can call those subscriptions. Somebody has subscribed (or
 * been subscribed) for notifications.</p>
 *
 * @author Mike Douglass
 */
public class NotifyDbImpl implements NotifyDb, Logged {
  private final OrmConfigBase<?> config;

  /** */
  protected boolean open;

  /** Incremented we were created for debugging */
  private static final AtomicLong globalSessionCt = new AtomicLong();

  private long sessionCt;

  /* Factory used to obtain a session
   */
  private static DbSessionFactoryProvider factoryProvider;

  /** Current database session - exists only across one user interaction
   */
  protected DbSession sess;

  /**
   * @param config configuration
   *
   */
  public NotifyDbImpl(final OrmConfigBase<?> config) {
    this.config = config;
  }

  @Override
  public boolean startTransaction() {
    if (isOpen()) {
      return false;
    }

    openSession();
    open = true;
    return true;
  }

  @Override
  public boolean isOpen() {
    return open;
  }

  @Override
  public void endTransaction() {
    try {
      checkOpen();

      if (debug()) {
        debug("End transaction for " + sessionCt);
      }

      try {
        if (!sess.rolledback()) {
          sess.commit();
        }
      } catch (final BedeworkException e) {
        throw new NoteException(e);
      }
    } catch (final NoteException ne) {
      try {
        rollbackTransaction();
      } catch (final NoteException ignored) {}
      throw ne;
    } finally {
      try {
        closeSession();
      } catch (final NoteException ignored) {}
      open = false;
    }
  }

  /* ====================================================================
   *                   Subscription Object methods
   * ==================================================================== */

  private static final String getAllQuery =
          "select sub from SubscriptionImpl sub";

  @Override
  @SuppressWarnings("unchecked")
  public List<Subscription> getAll() {
    try {
      sess.createQuery(getAllQuery);

      return wrap((List<Subscription>)sess.getList());
    } catch (final BedeworkException e) {
      throw new NoteException(e);
    }
  }

  private static final String clearTransientQuery =
          "delete from SubscriptionImpl sub " +
                  "where sub.transientSub = true";

  @Override
  public void clearTransients() {
    try {
      sess.createQuery(clearTransientQuery);

      sess.executeUpdate();
    } catch (final BedeworkException e) {
      throw new NoteException(e);
    }
  }

  private static final String getSubQuery =
          "select sub from SubscriptionImpl sub " +
                  "where sub.subscriptionId=:subid";

  @Override
  public Subscription get(final String id) {
    try {
      sess.createQuery(getSubQuery);
      sess.setString("subid", id);

      return wrap((Subscription)sess.getUnique());
    } catch (final BedeworkException e) {
      throw new NoteException(e);
    }
  }

  @Override
  public void refresh(final Subscription sub) {
    if (sub == null) {
      return;
    }

    try {
      sess.refresh(unwrap(sub));
    } catch (final BedeworkException e) {
      throw new NoteException(e);
    }
  }

  private static final String findSubQuery =
          "select sub from SubscriptionImpl sub " +
                  "where sub.connectorName=:connName " +
                  "and sub.principalHref=:pref";

  @Override
  public Subscription find(final String conName,
                           final String principalHref) {
    try {
      sess.createQuery(findSubQuery);
      sess.setString("connName", conName);
      sess.setString("pref", principalHref);

      return wrap((Subscription)sess.getUnique());
    } catch (final BedeworkException e) {
      throw new NoteException(e);
    }
  }

  @Override
  public Subscription find(final Subscription sub) {
    return find(sub.getConnectorName(),
                sub.getPrincipalHref());
  }

  @Override
  public void add(final Subscription sub) {
    try {
      sess.add(unwrap(sub));
    } catch (final BedeworkException e) {
      throw new NoteException(e);
    }
  }

  @Override
  public void update(final Subscription sub) {
    try {
      sess.update(unwrap(sub));
    } catch (final BedeworkException e) {
      throw new NoteException(e);
    }
  }

  @Override
  public void delete(final Subscription sub) {
    try {
      sess.delete(unwrap(sub));
    } catch (final BedeworkException e) {
      throw new NoteException(e);
    }
  }

  /* ==============================================================
   *                   Session methods
   * ============================================================== */

  protected void checkOpen() {
    if (!isOpen()) {
      throw new NoteException("Session call when closed");
    }
  }

  protected synchronized void openSession() {
    if (isOpen()) {
      throw new NoteException("Already open");
    }

    try {
      if (factoryProvider == null) {
        factoryProvider =
                new DbSessionFactoryProviderImpl()
                        .init(config.getOrmProperties());
      }

      open = true;

      if (sess != null) {
        warn("Session is not null. Will close");
        try {
          endTransaction();
        } catch (final Throwable ignored) {
        }
      }

      sessionCt = globalSessionCt.incrementAndGet();

      if (sess == null) {
        if (debug()) {
          debug("New orm session for " + sessionCt);
        }
        sess = factoryProvider.getNewSession();

        debug("Open session for " + sessionCt);
      }
    } catch (final BedeworkException e) {
      throw new NoteException(e);
    }

    beginTransaction();
  }

  protected synchronized void closeSession() {
    if (!isOpen()) {
      if (debug()) {
        debug("Close for " + sessionCt + " closed session");
      }
      return;
    }

    if (debug()) {
      debug("Close for " + sessionCt);
    }

    try {
      if (sess != null) {
        if (sess.rolledback()) {
          sess = null;
          return;
        }

        if (sess.transactionStarted()) {
          sess.rollback();
        }

        sess.close();
        sess = null;
      }
    } catch (final Throwable t) {
      try {
        sess.close();
      } catch (final Throwable ignored) {}
      sess = null; // Discard on error
    } finally {
      open = false;
    }
  }

  protected void beginTransaction() {
    checkOpen();

    if (debug()) {
      debug("Begin transaction for " + sessionCt);
    }
    try {
      sess.beginTransaction();
    } catch (final BedeworkException e) {
      throw new NoteException(e);
    }
  }

  protected void rollbackTransaction() {
    try {
      checkOpen();
      sess.rollback();
    } catch (final BedeworkException e) {
      throw new NoteException(e);
    }
  }

  /* ====================================================================
   *                   private methods
   * ==================================================================== */

  private Subscription unwrap(final Subscription val) {
    if (val == null) {
      return null;
    }

    if (val instanceof SubscriptionWrapper) {
      return ((SubscriptionWrapper)val).getSubscription();
    }

    return val;
  }

  private Subscription wrap(final Subscription val) {
    if (val == null) {
      return null;
    }

    return new SubscriptionWrapper(val);
  }

  private List<Subscription> wrap(final List<Subscription> val) {
    if (val == null) {
      return null;
    }

    final List<Subscription> wrapped = new ArrayList<>();

    for (final Subscription sub: val) {
      wrapped.add(wrap(sub));
    }

    return wrapped;
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
