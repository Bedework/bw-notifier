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

import org.bedework.notifier.NotifyRegistry;
import org.bedework.notifier.exception.NoteException;

import java.util.Map;

/** Serializable form of information about the subscription. May be
 * subclassed for specific connectors.
 *
 * @author douglm
 */
public class SubscriptionInfo extends SerializableProperties {
  /* ====================================================================
   *                   Deserialization methods
   * ==================================================================== */

  public static SubscriptionInfo getInfo(final String type,
                                         final Map vals) throws NoteException {
    final NotifyRegistry.Info info = NotifyRegistry.getInfo(type);

    if (info == null) {
      throw new NoteException("Unhandled type " + type);
    }

    SubscriptionInfo subInfo;
    try {
      subInfo = info.getSubscriptionInfoClass().newInstance();
    } catch (final Throwable t) {
      throw new NoteException(t);
    }

    subInfo.init(vals);

    return subInfo;
  }

  /* ====================================================================
   *                   Convenience methods
   * ==================================================================== */

  /* ====================================================================
   *                   Object methods
   * ==================================================================== */

}
