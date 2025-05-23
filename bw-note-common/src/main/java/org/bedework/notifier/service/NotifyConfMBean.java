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

import org.bedework.notifier.NotifyConfProperties;
import org.bedework.notifier.Stat;

import org.bedework.util.jmx.ConfBaseMBean;
import org.bedework.util.jmx.MBeanInfo;

import java.util.List;

/** Configure the Bedework notification engine service
 *
 * @author douglm
 */
public interface NotifyConfMBean
        extends ConfBaseMBean, NotifyConfProperties {

  /** Export schema to database?
   *
   * @param val true for export
   */
  void setExport(boolean val);

  /**
   * @return true for export schema
   */
  @MBeanInfo("Export (write) schema to database?")
  boolean getExport();

  /** Output file name - full path
   *
   * @param val path
   */
  void setSchemaOutFile(String val);

  /**
   * @return Output file name - full path
   */
  @MBeanInfo("Full path of schema output file")
  String getSchemaOutFile();

  /* ========================================================================
   * Operations
   * ======================================================================== */

  /** Create or dump new schema. If export and drop set will try to drop tables.
   * Export and create will create a schema in the db and export, drop, create
   * will drop tables, and try to create a new schema.
   * <p>
   * The export and drop flags will all be reset to false after this,
   * whatever the result. This avoids accidental damage to the db.
   *
   * @return Completion message
   */
  @MBeanInfo("Start build of the database schema. Set export flag to write to db.")
  String schema();

  /** Returns status of the schema build.
   *
   * @return Completion messages
   */
  @MBeanInfo("Status of the database schema build.")
  List<String> schemaStatus();

  /**
   * @param value dialect
   */
  @MBeanInfo("Set the hibernate dialect")
  void setHibernateDialect(@MBeanInfo("value: a valid hibernate dialect class") String value);

  /**
   * @return Completion messages
   */
  @MBeanInfo("Get the hibernate dialect")
  String getHibernateDialect();

  /** List the orm properties
   *
   * @return properties
   */
  @MBeanInfo("List the orm properties")
  String listOrmProperties();

  /** Display the named property
   *
   * @param name property name
   * @return value
   */
  @MBeanInfo("Display the named orm property")
  String displayOrmProperty(@MBeanInfo("name") String name);

  /** Remove the named property
   *
   * @param name property name
   */
  @MBeanInfo("Remove the named orm property")
  void removeOrmProperty(@MBeanInfo("name") String name);

  /**
   * @param name property name
   * @param value property value
   */
  @MBeanInfo("Add an orm property")
  void addOrmProperty(@MBeanInfo("name") String name,
                      @MBeanInfo("value") String value);

  /**
   * @param name property name
   * @param value property value
   */
  @MBeanInfo("Set an orm property")
  void setOrmProperty(@MBeanInfo("name") String name,
                      @MBeanInfo("value") String value);

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
