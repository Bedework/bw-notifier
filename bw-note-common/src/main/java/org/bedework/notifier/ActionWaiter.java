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

import org.bedework.notifier.db.Subscription;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;
import org.bedework.util.misc.ToString;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Actions waiting for a subscription to be released.
 *
 * @author douglm
 *
 */
public class ActionWaiter implements Logged {
  private final Map<String, List<Action>> actions = new HashMap<>();

  private static final int maxFree = 20;

  private int waiting;

  private int maxWaiting;

  private Deque<List<Action>> freeLists = new ArrayDeque<>(maxFree);

  public void add(final Action action) {
    final String key = action.getSub().getSubscriptionId();

    List<Action> la = actions.get(key);

    if (la == null) {
      if (freeLists.size() > 0) {
        la = freeLists.pop();
      } else {
        la = new ArrayList<>();
      }

      actions.put(key, la);
    }

    la.add(action);
  }

  public Action get(final Subscription sub) {
    final String key = sub.getSubscriptionId();

    final List<Action> la = actions.get(key);

    if (la == null) {
      return null;
    }

    final Action action = la.get(0);
    la.remove(0);

    if (la.isEmpty()) {
      actions.remove(key);

      if (freeLists.size() < maxFree) {
        freeLists.push(la);
      }
    }

    return action;
  }

  /**
   *
   * @return number of actions waiting
   */
  public int getWaiting() {
    return waiting;
  }

  /**
   *
   * @return max number of actions waiting
   */
  public int getMaxWaiting() {
    return maxWaiting;
  }

  protected void toStringSegment(final ToString ts) {
    ts.append("waiting", getWaiting());
    ts.append("maxWaiting", getMaxWaiting());
  }

  public String toString() {
    final ToString ts = new ToString(this);

    toStringSegment(ts);

    return ts.toString();
  }

  /* ====================================================================
   *                   Logged methods
   * ==================================================================== */

  private BwLogger logger = new BwLogger();

  @Override
  public BwLogger getLogger() {
    if ((logger.getLoggedClass() == null) && (logger.getLoggedName() == null)) {
      logger.setLoggedClass(getClass());
    }

    return logger;
  }
}
