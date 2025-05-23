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

/** Some definitions for the notification service
 *
 *   @author Mike Douglass   douglm  rpi.edu
 */
public interface NotifyDefs {
  /** and does it tell us when something changes or do we have to ask?
   */
  enum NotifyKind {
    /** we have to ask */
    poll,

    /** the other end will tell us via a callback */
    notify
  }

  String namespace = "oeg.bedework.namespace.notify";
}
