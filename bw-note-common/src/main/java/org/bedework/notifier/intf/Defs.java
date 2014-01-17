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
package org.bedework.notifier.intf;

/** Definitions for synch classes
 *
 *   @author Mike Douglass   douglm  bedework.edu
 */
public interface Defs {
  /* ========================================================================
   * X-properties we generate
   * ======================================================================== */

  /** Common prefix */
  public static final String xpMSPrefix = "X-BEDEWORK-EXSYNC-";

  /** Exchange tzid */
  public static final String xpMSTzid = xpMSPrefix + "TZID";

  /** Exchange start tzid */
  public static final String xpMSStartTzid = xpMSPrefix + "STARTTZID";

  /** Exchange end tzid */
  public static final String xpMSEndTzid = xpMSPrefix + "ENDTZID";

  /** Exchange end tzid */
  public static final String xpMSLastmod = xpMSPrefix + "LASTMOD";
}
