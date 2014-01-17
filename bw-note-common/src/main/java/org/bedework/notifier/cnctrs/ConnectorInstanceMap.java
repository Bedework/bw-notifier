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

import org.bedework.notifier.db.Subscription;
import org.bedework.notifier.exception.NoteException;
import org.bedework.util.misc.ToString;

import java.util.HashMap;
import java.util.Map;


/** A map for use by Connectors.
 *
 * @author Mike Douglass
 *
 * @param <CI>
 */
public class ConnectorInstanceMap<CI extends ConnectorInstance> {
  static class Key {
    Subscription sub;

    Key(final Subscription sub) {
      this.sub = sub;
    }

    @Override
    public int hashCode() {
      return sub.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
      Key that = (Key)o;

      return sub.equals(that.sub);
    }

    @Override
    public String toString() {
      ToString ts = new ToString(this);

      ts.append("sub", sub);

      return ts.toString();
    }
  }

  private Map<Key, CI> theMap = new HashMap<Key, CI>();

  /** Add a connector
   *
     * @param sub
     * @param cinst
   * @throws org.bedework.notifier.exception.NoteException
   */
  public synchronized void add(final Subscription sub,
                               final CI cinst) throws NoteException {
    Key key = new Key(sub);

    if (theMap.containsKey(key)) {
      throw new NoteException("instance already in map for " + key);
    }

    theMap.put(key, cinst);
  }

  /** Find a connector
   *
   * @param sub
   * @return CI or null
   * @throws org.bedework.notifier.exception.NoteException
   */
  public synchronized CI find(final Subscription sub) throws NoteException {
    return theMap.get(new Key(sub));
  }


  /** Remove a connector
   *
   * @param sub
   * @throws org.bedework.notifier.exception.NoteException
   */
  public synchronized void remove(final Subscription sub) throws NoteException {
    theMap.remove(new Key(sub));
  }
}
