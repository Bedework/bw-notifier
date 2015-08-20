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

import org.bedework.notifier.exception.NoteException;

import java.io.Serializable;
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
public interface NotifyDb extends Serializable {
  /**
   * @return true if we had to open it. False if already open
   * @throws NoteException
   */
  boolean startTransaction() throws NoteException;

  /**
   * @return true for open
   */
  boolean isOpen();

  /**
   * @throws NoteException
   */
  void endTransaction() throws NoteException;

  /* ====================================================================
   *                   Subscription Object methods
   * ==================================================================== */

  /**
   * @return list of subscriptions
   * @throws NoteException
   */
  List<Subscription> getAll() throws NoteException;

  /**
   * @throws NoteException
   */
  void clearTransients() throws NoteException;

  /** The notify engine generates a unique subscription id
   * for each subscription. This is used as a key for each subscription.
   *
   * @param id - unique id
   * @return a matching subscription or null
   * @throws NoteException
   */
  Subscription get(String id) throws NoteException;

  /** Find any subscription that matches this one. There can only be one with
   * the same endpoints
   *
   * @param conName name of connector
   * @param principalHref of subscription
   * @return matching subscriptions
   * @throws NoteException
   */
  Subscription find(String conName,
                    String principalHref) throws NoteException;

  /** Find any subscription that matches this one. There can only be one with
   * the same endpoints
   *
   * @param sub subscription
   * @return matching subscriptions
   * @throws NoteException
   */
  Subscription find(Subscription sub) throws NoteException;

  /** Add the subscription.
   *
   * @param sub subscription
   * @throws NoteException
   */
  void add(Subscription sub) throws NoteException;

  /** Update the persisted state of the subscription.
   *
   * @param sub subscription
   * @throws NoteException
   */
  void update(Subscription sub) throws NoteException;

  /** Delete the subscription.
   *
   * @param sub subscription
   * @throws NoteException
   */
  void delete(Subscription sub) throws NoteException;
}
