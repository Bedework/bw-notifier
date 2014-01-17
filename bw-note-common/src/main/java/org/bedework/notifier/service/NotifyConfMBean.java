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
package org.bedework.notifier.service;

import org.bedework.notifier.Stat;

import org.bedework.util.jmx.ConfBaseMBean;
import org.bedework.util.jmx.MBeanInfo;

import java.util.List;

/** Configure the Bedework notification engine service
 *
 * @author douglm
 */
public interface NotifyConfMBean extends ConfBaseMBean {
  /* ========================================================================
   * Config properties
   * ======================================================================== */

  /**
   * @param val current size of ling pool
   */
  void setNotelingPoolSize(final int val);

  /**
   * @return current size of noteling pool
   */
  @MBeanInfo("size of noteling pool.")
  int getNotelingPoolSize();

  /**
   * @param val timeout in millisecs
   */
  void setNotelingPoolTimeout(final long val);

  /**
   * @return timeout in millisecs
   */
  @MBeanInfo("timeout in millisecs.")
  long getNotelingPoolTimeout();

  /** How often we retry when a target is missing
   *
   * @param val
   */
  void setMissingTargetRetries(final int val);

  /**
   * @return How often we retry when a target is missing
   */
  @MBeanInfo("How often we retry when a target is missing.")
  int getMissingTargetRetries();

  /** web service push callback uri - null for no service
   *
   * @param val    String
   */
  void setCallbackURI(final String val);

  /** web service push callback uri - null for no service
   *
   * @return String
   */
  @MBeanInfo("web service push callback uri - null for no service.")
  String getCallbackURI();

  /** Timezone server location
   *
   * @param val    String
   */
  void setTimezonesURI(final String val);

  /** Timezone server location
   *
   * @return String
   */
  @MBeanInfo("Timezone server location.")
  String getTimezonesURI();

  /** Path to keystore - null for use default
   *
   * @param val    String
   */
  void setKeystore(final String val);

  /** Path to keystore - null for use default
   *
   * @return String
   */
  @MBeanInfo("Path to keystore - null for use default.")
  String getKeystore();

  /**
   *
   * @param val    String
   */
  void setPrivKeys(final String val);

  /**
   *
   * @return String
   */
  @MBeanInfo("Name of private keys file.")
  String getPrivKeys();

  /**
   *
   * @param val    String
   */
  void setPubKeys(final String val);

  /**
   *
   * @return String
   */
  @MBeanInfo("Name of public keys file.")
  String getPubKeys();

  /** List connector names
   *
   * @return list of names
   */
  @MBeanInfo("List the connector names.")
  List<String> getConnectorNames();

  /** Get the current stats
   *
   * @return List of Stat
   */
  @MBeanInfo("Get the current stats.")
  List<Stat> getStats();

  /** Export schema to database?
   *
   * @param val
   */
  public void setExport(boolean val);

  /**
   * @return true for export schema
   */
  @MBeanInfo("Export (write) schema to database?")
  public boolean getExport();

  /** Output file name - full path
   *
   * @param val
   */
  public void setSchemaOutFile(String val);

  /**
   * @return Output file name - full path
   */
  @MBeanInfo("Full path of schema output file")
  public String getSchemaOutFile();

  /* ========================================================================
   * Operations
   * ======================================================================== */

  /** Create or dump new schema. If export and drop set will try to drop tables.
   * Export and create will create a schema in the db and export, drop, create
   * will drop tables, and try to create a new schema.
   *
   * The export and drop flags will all be reset to false after this,
   * whatever the result. This avoids accidental damage to the db.
   *
   * @return Completion message
   */
  @MBeanInfo("Start build of the database schema. Set export flag to write to db.")
  public String schema();

  /** Returns status of the schema build.
   *
   * @return Completion messages
   */
  @MBeanInfo("Status of the database schema build.")
  public List<String> schemaStatus();

  /**
   * @param value
   */
  @MBeanInfo("Set the hibernate dialect")
  void setHibernateDialect(@MBeanInfo("value: a valid hibernate dialect class") final String value);

  /**
   * @return Completion messages
   */
  @MBeanInfo("Get the hibernate dialect")
  String getHibernateDialect();

  /** List the hibernate properties
   *
   * @return properties
   */
  @MBeanInfo("List the hibernate properties")
  String listHibernateProperties();

  /** Display the named property
   *
   * @param name
   * @return value
   */
  @MBeanInfo("Display the named hibernate property")
  String displayHibernateProperty(@MBeanInfo("name") final String name);

  /** Remove the named property
   *
   * @param name
   */
  @MBeanInfo("Remove the named hibernate property")
  void removeHibernateProperty(@MBeanInfo("name") final String name);

  /**
   * @param name
   * @param value
   */
  @MBeanInfo("Add a hibernate property")
  void addHibernateProperty(@MBeanInfo("name") final String name,
                              @MBeanInfo("value") final String value);

  /**
   * @param name
   * @param value
   */
  @MBeanInfo("Set a hibernate property")
  void setHibernateProperty(@MBeanInfo("name") final String name,
                            @MBeanInfo("value") final String value);

  /* ========================================================================
   * Lifecycle
   * ======================================================================== */

  /** Lifecycle
   *
   */
  void start();

  /** Lifecycle
   *
   */
  void stop();

  /** Lifecycle
   *
   * @return true if started
   */
  boolean isStarted();

  /** (Re)load the configuration
   *
   * @return status
   */
  @MBeanInfo("(Re)load the configuration")
  String loadConfig();
}
