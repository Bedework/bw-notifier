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

import org.bedework.notifier.conf.NotifyConfig;
import org.bedework.notifier.exception.NoteException;
import org.bedework.util.hibernate.HibException;
import org.bedework.util.hibernate.HibSession;
import org.bedework.util.hibernate.HibSessionFactory;
import org.bedework.util.hibernate.HibSessionImpl;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

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
public class NotifyDb implements Serializable {
  private transient Logger log;

  private final boolean debug;

  private NotifyConfig config;

  /** */
  protected boolean open;

  /** When we were created for debugging */
  protected Timestamp objTimestamp;

  /** Current hibernate session - exists only across one user interaction
   */
  protected HibSession sess;

  /**
   * @param config
   *
   */
  public NotifyDb(final NotifyConfig config) {
    debug = getLogger().isDebugEnabled();

    this.config = config;
  }

  /**
   * @return true if we had to open it. False if already open
   * @throws org.bedework.notifier.exception.NoteException
   */
  public boolean open() throws NoteException {
    if (isOpen()) {
      return false;
    }

    openSession();
    open = true;
    return true;
  }

  /**
   * @return true for open
   */
  public boolean isOpen() {
    return open;
  }

  /**
   * @throws org.bedework.notifier.exception.NoteException
   */
  public void close() throws NoteException {
    try {
      endTransaction();
    } catch (NoteException wde) {
      try {
        rollbackTransaction();
      } catch (NoteException wde1) {}
      throw wde;
    } finally {
      try {
        closeSession();
      } catch (NoteException wde1) {}
      open = false;
    }
  }

  /* ====================================================================
   *                   Subscription Object methods
   * ==================================================================== */

  /**
   * @return list of subscriptions
   * @throws org.bedework.notifier.exception.NoteException
   */
  @SuppressWarnings("unchecked")
  public List<Subscription> getAll() throws NoteException {
    StringBuilder sb = new StringBuilder();

    sb.append("from ");
    sb.append(Subscription.class.getName());

    try {
      sess.createQuery(sb.toString());

      return sess.getList();
    } catch (HibException he) {
      throw new NoteException(he);
    }
  }

  /** The synch engine generates a unique subscription id
   * for each subscription. This is used as a key for each subscription.
   *
   * @param id - unique id
   * @return a matching subscription or null
   * @throws org.bedework.notifier.exception.NoteException
   */
  public Subscription get(final String id) throws NoteException {
    try {
      StringBuilder sb = new StringBuilder();

      sb.append("from ");
      sb.append(Subscription.class.getName());
      sb.append(" sub where sub.subscriptionId=:subid");

      sess.createQuery(sb.toString());
      sess.setString("subid", id);

      return (Subscription)sess.getUnique();
    } catch (HibException he) {
      throw new NoteException(he);
    }
  }

  /** Find any subscription that matches this one. There can only be one with
   * the same endpoints
   *
   * @param sub
   * @return matching subscriptions
   * @throws org.bedework.notifier.exception.NoteException
   */
  public Subscription find(final Subscription sub) throws NoteException {
    return null;
  }

  /** Add the subscription.
   *
   * @param sub
   * @throws org.bedework.notifier.exception.NoteException
   */
  public void add(final Subscription sub) throws NoteException {
    try {
      sess.save(sub);
    } catch (HibException he) {
      throw new NoteException(he);
    }
  }

  /** Update the persisted state of the subscription.
   *
   * @param sub
   * @throws org.bedework.notifier.exception.NoteException
   */
  public void update(final Subscription sub) throws NoteException {
    try {
      sess.update(sub);
    } catch (HibException he) {
      throw new NoteException(he);
    }
  }

  /** Delete the subscription.
   *
   * @param sub
   * @throws org.bedework.notifier.exception.NoteException
   */
  public void delete(final Subscription sub) throws NoteException {
    boolean opened = open();

    try {
      sess.delete(sub);
    } catch (HibException he) {
      throw new NoteException(he);
    } finally {
      if (opened) {
        close();
      }
    }
  }

  /* ====================================================================
   *                   Session methods
   * ==================================================================== */

  protected void checkOpen() throws NoteException {
    if (!isOpen()) {
      throw new NoteException("Session call when closed");
    }
  }

  protected synchronized void openSession() throws NoteException {
    if (isOpen()) {
      throw new NoteException("Already open");
    }

    open = true;

    if (sess != null) {
      warn("Session is not null. Will close");
      try {
        close();
      } finally {
      }
    }

    if (sess == null) {
      if (debug) {
        trace("New hibernate session for " + objTimestamp);
      }
      sess = new HibSessionImpl();
      try {
        sess.init(HibSessionFactory.getSessionFactory(
                config.getHibernateProperties()), getLogger());
      } catch (HibException he) {
        throw new NoteException(he);
      }
      trace("Open session for " + objTimestamp);
    }

    beginTransaction();
  }

  protected synchronized void closeSession() throws NoteException {
    if (!isOpen()) {
      if (debug) {
        trace("Close for " + objTimestamp + " closed session");
      }
      return;
    }

    if (debug) {
      trace("Close for " + objTimestamp);
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
//        sess.disconnect();
        sess.close();
        sess = null;
      }
    } catch (Throwable t) {
      try {
        sess.close();
      } catch (Throwable t1) {}
      sess = null; // Discard on error
    } finally {
      open = false;
    }
  }

  protected void beginTransaction() throws NoteException {
    checkOpen();

    if (debug) {
      trace("Begin transaction for " + objTimestamp);
    }
    try {
      sess.beginTransaction();
    } catch (HibException he) {
      throw new NoteException(he);
    }
  }

  protected void endTransaction() throws NoteException {
    checkOpen();

    if (debug) {
      trace("End transaction for " + objTimestamp);
    }

    try {
      if (!sess.rolledback()) {
        sess.commit();
      }
    } catch (HibException he) {
      throw new NoteException(he);
    }
  }

  protected void rollbackTransaction() throws NoteException {
    try {
      checkOpen();
      sess.rollback();
    } catch (HibException he) {
      throw new NoteException(he);
    } finally {
    }
  }

  /**
   * @return Logger
   */
  protected Logger getLogger() {
    if (log == null) {
      log = Logger.getLogger(this.getClass());
    }

    return log;
  }

  /**
   * @param t
   */
  protected void error(final Throwable t) {
    getLogger().error(this, t);
  }

  /**
   * @param msg
   */
  protected void warn(final String msg) {
    getLogger().warn(msg);
  }

  /**
   * @param msg
   */
  protected void trace(final String msg) {
    getLogger().debug(msg);
  }

  /* ====================================================================
   *                   private methods
   * ==================================================================== */

}
