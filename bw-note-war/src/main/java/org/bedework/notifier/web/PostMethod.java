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
package org.bedework.notifier.web;

import org.bedework.notifier.exception.NoteException;
import org.bedework.util.misc.Util;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Handle POST for exchange synch servlet.
 */
public class PostMethod extends MethodBase {
  @Override
  public void init() throws NoteException {
  }

  @SuppressWarnings({"unchecked"})
  @Override
  public void doMethod(final HttpServletRequest req,
                       final HttpServletResponse resp) throws NoteException {
    try {
      List<String> resourceUri = getResourceUri(req);

      if (Util.isEmpty(resourceUri)) {
        throw new NoteException("Bad resource url - no connector specified");
      }

      /* Find a connector to handle the incoming request.
       */
      //Connector conn = notifier.getConnector(resourceUri.get(0));

      //if (conn == null) {
      //  throw new NoteException("Bad resource url - unknown connector specified");
      //}

      resourceUri.remove(0);
//      NotificationBatch notes = conn.handleCallback(req, resp, resourceUri);

  //    if (notes != null) {
    //    notifier.handleNotifications(notes);
      //  conn.respondCallback(resp, notes);
      //}
    } catch (NoteException se) {
      throw se;
    } catch(Throwable t) {
      throw new NoteException(t);
    }
  }
}

