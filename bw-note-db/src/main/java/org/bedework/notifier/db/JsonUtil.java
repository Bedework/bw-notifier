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

import java.util.List;
import java.util.Map;

/** Useful Json stuff.
 *
 * @author douglm
 *
 */
public class JsonUtil {
  public static String must(final String name,
                            final Map theVals) throws NoteException {
    Object val = theVals.get(name);

    if (val == null) {
      throw new NoteException("missing value: " + name);
    }
    try {
      return (String)val;
    } catch (final Throwable t) {
      throw new NoteException(t);
    }
  }

  public static String may(final String name,
                           final Map theVals) throws NoteException {
    Object val = theVals.get(name);

    if (val == null) {
      return null;
    }
    try {
      return (String)val;
    } catch (final Throwable t) {
      throw new NoteException(t);
    }
  }

  public static List mustList(final String name,
                              final Map theVals) throws NoteException {
    Object val = theVals.get(name);

    if (val == null) {
      throw new NoteException("missing value: " + name);
    }
    try {
      return (List)val;
    } catch (final Throwable t) {
      throw new NoteException(t);
    }
  }

  public static List mayList(final String name,
                             final Map theVals) throws NoteException {
    Object val = theVals.get(name);

    if (val == null) {
      return null;
    }
    try {
      return (List)val;
    } catch (final Throwable t) {
      throw new NoteException(t);
    }
  }
}
