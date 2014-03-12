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
package org.bedework.notifier.outbound;

import org.bedework.notifier.exception.NoteException;
import org.bedework.notifier.notifications.Notification;

import java.util.ArrayList;
import java.util.List;

/** The interface implemented by destination adaptors. A destination
 * may be an email address or sms.
 *
 * @author Mike Douglass
 *
 */
public class DestinationFactory {
  private static Destination dummy = new DummyDestination();

  /** Get an object to send a notification
   *
   * @param note the notification to send
   * @return one or more objects to send it or null.
   * @throws NoteException
   */
  public static List<Destination> getDestination(Notification note) throws NoteException {
    List<Destination> l = new ArrayList<>(1);

    l.add(dummy);

    return l;
  }
}
