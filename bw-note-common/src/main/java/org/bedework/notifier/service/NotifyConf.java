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

import org.bedework.notifier.NotifyEngine;
import org.bedework.notifier.Stat;
import org.bedework.notifier.conf.ConnectorConfig;
import org.bedework.notifier.conf.NotifyConfig;
import org.bedework.notifier.outbound.common.AdaptorConf;
import org.bedework.notifier.outbound.common.AdaptorConfig;
import org.bedework.util.config.ConfigurationStore;
import org.bedework.util.jmx.ConfBase;
import org.bedework.util.jmx.ConfigHolder;
import org.bedework.util.jmx.InfoLines;

import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.management.ObjectName;

/**
 * @author douglm
 *
 */
public class NotifyConf extends ConfBase<NotifyConfig> implements
        NotifyConfMBean, ConfigHolder<NotifyConfig> {
  /* Name of the property holding the location of the config data */
  private static final String confuriPname = "org.bedework.notify.confuri";

  List<String> connectorNames;

  List<String> adaptorNames;

  private boolean running;

  private NotifyEngine notifier;

  /* Be safe - default to false */
  private boolean export;

  private String schemaOutFile;

  private Configuration hibCfg;

  private class SchemaThread extends Thread {
    InfoLines infoLines = new InfoLines();

    SchemaThread() {
      super("BuildSchema");
    }

    @Override
    public void run() {
      try {
        infoLines.addLn("Started export of schema");

        long startTime = System.currentTimeMillis();

        SchemaExport se = new SchemaExport(getHibConfiguration());

//      if (getDelimiter() != null) {
//        se.setDelimiter(getDelimiter());
//      }

        se.setFormat(true);       // getFormat());
        se.setHaltOnError(false); // getHaltOnError());
        se.setOutputFile(getSchemaOutFile());
        /* There appears to be a bug in the hibernate code. Everybody initialises
        this to /import.sql. Set to null causes an NPE
        Make sure it refers to a non-existant file */
        //se.setImportFile("not-a-file.sql");

        se.execute(false, // script - causes write to System.out if true
                   getExport(),
                   false,   // drop
                   true);   //   getCreate());

        long millis = System.currentTimeMillis() - startTime;
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        seconds -= (minutes * 60);

        infoLines.addLn("Elapsed time: " + minutes + ":" +
                                twoDigits(seconds));
      } catch (Throwable t) {
        error(t);
        infoLines.exceptionMsg(t);
      } finally {
        infoLines.addLn("Schema build completed");
        export = false;
      }
    }
  }

  private SchemaThread buildSchema = new SchemaThread();

  private class ProcessorThread extends Thread {
    boolean showedTrace;

    final Object locker = new Object();

    /**
     * @param name - for the thread
     */
    public ProcessorThread(final String name) {
      super(name);
    }

    @Override
    public void run() {
      while (running) {
        try {
          if (notifier == null) {
            // Starting the service

            notifier = NotifyEngine.getNotifier();
            notifier.start();
          }
        } catch (final Throwable t) {
          if (!showedTrace) {
            error(t);
            showedTrace = true;
          } else {
            error(t.getMessage());
          }
        }

        if (running) {
          // Wait a bit before restarting
          try {
            synchronized (locker) {
              locker.wait (10 * 1000);
            }
          } catch (final Throwable t) {
            error(t.getMessage());
          }
        }
      }
    }
  }

  private ProcessorThread processor;

  /**
   */
  public NotifyConf() {
    super("org.bedework.notify:service=NotifyConf");
    setConfigPname(confuriPname);
    setPathSuffix("conf");

    NotifyEngine.setConfigHolder(this);
  }

  /* ========================================================================
   * Schema attributes
   * ======================================================================== */

  @Override
  public void setExport(final boolean val) {
    export = val;
  }

  @Override
  public boolean getExport() {
    return export;
  }

  @Override
  public void setSchemaOutFile(final String val) {
    schemaOutFile = val;
  }

  @Override
  public String getSchemaOutFile() {
    return schemaOutFile;
  }

  /* ========================================================================
   * Attributes
   * ======================================================================== */

  @Override
  public void setNotelingPoolSize(final int val) {
    getConfig().setNotelingPoolSize(val);
  }

  /**
   * @return current size of noteling pool
   */
  @Override
  public int getNotelingPoolSize() {
    return getConfig().getNotelingPoolSize();
  }

  /**
   * @param val timeout in millisecs
   */
  @Override
  public void setNotelingPoolTimeout(final long val) {
    getConfig().setNotelingPoolTimeout(val);
  }

  /**
   * @return timeout in millisecs
   */
  @Override
  public long getNotelingPoolTimeout() {
    return getConfig().getNotelingPoolTimeout();
  }

  /** How often we retry when a target is missing
   *
   * @param val count
   */
  @Override
  public void setMissingTargetRetries(final int val) {
    getConfig().setMissingTargetRetries(val);
  }

  /**
   * @return How often we retry when a target is missing
   */
  @Override
  public int getMissingTargetRetries() {
    return getConfig().getMissingTargetRetries();
  }

  /** web service push callback uri - null for no service
   *
   * @param val    String
   */
  @Override
  public void setCallbackURI(final String val) {
    getConfig().setCallbackURI(val);
  }

  /** web service push callback uri - null for no service
   *
   * @return String
   */
  @Override
  public String getCallbackURI() {
    return getConfig().getCallbackURI();
  }

  /** Timezone server location
   *
   * @param val    String
   */
  @Override
  public void setTimezonesURI(final String val) {
    getConfig().setTimezonesURI(val);
  }

  /** Timezone server location
   *
   * @return String
   */
  @Override
  public String getTimezonesURI() {
    return getConfig().getTimezonesURI();
  }

  @Override
  public void setTemplatesPath(final String val) {
    getConfig().setTemplatesPath(val);
  }

  @Override
  public String getTemplatesPath() {
    return getConfig().getTemplatesPath();
  }

  /** Path to keystore - null for use default
   *
   * @param val    String
   */
  @Override
  public void setKeystore(final String val) {
    getConfig().setKeystore(val);
  }

  /** Path to keystore - null for use default
   *
   * @return String
   */
  @Override
  public String getKeystore() {
    return getConfig().getKeystore();
  }

  /**
   *
   * @param val    String
   */
  @Override
  public void setPrivKeys(final String val) {
    getConfig().setPrivKeys(val);
  }

  /**
   *
   * @return String
   */
  @Override
  public String getPrivKeys() {
    return getConfig().getPrivKeys();
  }

  /**
   *
   * @param val    String
   */
  @Override
  public void setPubKeys(final String val) {
    getConfig().setPubKeys(val);
  }

  /**
   *
   * @return String
   */
  @Override
  public String getPubKeys() {
    return getConfig().getPubKeys();
  }

  /**
   *
   * @param val    String
   */
  @Override
  public void setCardDAVHost(final String val) {
    getConfig().setCardDAVHost(val);
  }

  /**
   *
   * @return String
   */
  @Override
  public String getCardDAVHost() {
    return getConfig().getCardDAVHost();
  }

  /**
   *
   * @param val    String
   */
  @Override
  public void setCardDAVPort(final int val) {
    getConfig().setCardDAVPort(val);
  }

  /**
   *
   * @return String
   */
  @Override
  public int getCardDAVPort() {
    return getConfig().getCardDAVPort();
  }

  /**
   *
   * @param val    String
   */
  @Override
  public void setCardDAVContextPath(final String val) {
    getConfig().setCardDAVContextPath(val);
  }

  /**
   *
   * @return String
   */
  @Override
  public String getCardDAVContextPath() {
    return getConfig().getCardDAVContextPath();
  }

  /**
   *
   * @param val    String
   */
  @Override
  public void setCardDAVPrincipalsPath(final String val) {
    getConfig().setCardDAVPrincipalsPath(val);
  }

  /**
   *
   * @return String
   */
  @Override
  public String getCardDAVPrincipalsPath() {
    return getConfig().getCardDAVPrincipalsPath();
  }

  /**
   *
   * @param val    String
   */
  @Override
  public void setVCardContentType(final String val) {
    getConfig().setVCardContentType(val);
  }

  /**
   *
   * @return String
   */
  @Override
  public String getVCardContentType() {
    return getConfig().getVCardContentType();
  }

  @Override
  public List<String> getConnectorNames() {
    return connectorNames;
  }

  @Override
  public List<Stat> getStats() {
    if (notifier == null) {
      return new ArrayList<>();
    }

    return notifier.getStats();
  }

  /* ========================================================================
   * Operations
   * ======================================================================== */

  @Override
  public String schema() {
    try {
//      buildSchema = new SchemaThread();

      buildSchema.start();

      return "OK";
    } catch (Throwable t) {
      error(t);

      return "Exception: " + t.getLocalizedMessage();
    }
  }

  @Override
  public synchronized List<String> schemaStatus() {
    if (buildSchema == null) {
      InfoLines infoLines = new InfoLines();

      infoLines.addLn("Schema build has not been started");

      return infoLines;
    }

    return buildSchema.infoLines;
  }

  @Override
  public void setHibernateDialect(final String value) {
    getConfig().setHibernateDialect(value);
  }

  @Override
  public String getHibernateDialect() {
    return getConfig().getHibernateDialect();
  }

  @Override
  public String listHibernateProperties() {
    StringBuilder res = new StringBuilder();

    List<String> ps = getConfig().getHibernateProperties();

    for (String p: ps) {
      res.append(p);
      res.append("\n");
    }

    return res.toString();
  }

  @Override
  public String displayHibernateProperty(final String name) {
    String val = getConfig().getHibernateProperty(name);

    if (val != null) {
      return val;
    }

    return "Not found";
  }

  @Override
  public void removeHibernateProperty(final String name) {
    getConfig().removeHibernateProperty(name);
  }

  @Override
  public void addHibernateProperty(final String name,
                                   final String value) {
    getConfig().addHibernateProperty(name, value);
  }

  @Override
  public void setHibernateProperty(final String name,
                                   final String value) {
    getConfig().setHibernateProperty(name, value);
  }

  /* ========================================================================
   * Lifecycle
   * ======================================================================== */

  @Override
  public void start() {
    if (processor != null) {
      error("Already started");
      return;
    }

    info("************************************************************");
    info(" * Starting notifier");
    info("************************************************************");

    running = true;

    processor = new ProcessorThread(getServiceName());
    processor.start();
  }

  @Override
  public void stop() {
    if (processor == null) {
      error("Already stopped");
      return;
    }

    info("************************************************************");
    info(" * Stopping notifier");
    info("************************************************************");

    running = false;

    notifier.stop();

    processor.interrupt();
    try {
      processor.join(20 * 1000);
    } catch (final InterruptedException ignored) {
    } catch (final Throwable t) {
      error("Error waiting for processor termination");
      error(t);
    }

    processor = null;

    notifier = null;

    info("************************************************************");
    info(" * Notifier terminated");
    info("************************************************************");
  }

  @Override
  public boolean isStarted() {
    return true;
  }

  @Override
  public String loadConfig() {
    try {
      /* Load up the config */

      final String res = loadOnlyConfig(NotifyConfig.class);

      if (res != null) {
        return res;
      }

      if (!loadConnectors() || !loadAdaptors()) {
        return "failed";
      }


      return "OK";
    } catch (final Throwable t) {
      error("Failed to start management context: " + t.getLocalizedMessage());
      error(t);
      return "failed";
    }
  }

  @Override
  public NotifyConfig getConfig() {
    return cfg;
  }

  /** Save the configuration.
   *
   */
  @Override
  public void putConfig() {
    saveConfig();
  }

  /* ====================================================================
   *                   Private methods
   * ==================================================================== */

  private synchronized Configuration getHibConfiguration() {
    if (hibCfg == null) {
      try {
        hibCfg = new Configuration();

        StringBuilder sb = new StringBuilder();

        List<String> ps = getConfig().getHibernateProperties();

        for (String p: ps) {
          sb.append(p);
          sb.append("\n");
        }

        Properties hprops = new Properties();
        hprops.load(new StringReader(sb.toString()));

        hibCfg.addProperties(hprops).configure();
      } catch (Throwable t) {
        // Always bad.
        error(t);
      }
    }

    return hibCfg;
  }

  /**
   * @param val to convert
   * @return 2 digit val
   */
  private static String twoDigits(final long val) {
    if (val < 10) {
      return "0" + val;
    }

    return String.valueOf(val);
  }

  private boolean loadConnectors() {
    try {
      final ConfigurationStore cs = getStore().getStore("connectors");

      connectorNames = cs.getConfigs();

      final List<NoteConnConf> nccs = new ArrayList<>();
      cfg.setConnectorConfs(nccs);

      for (final String cn: connectorNames) {
        final ObjectName objectName = createObjectName("connector", cn);

        /* Read the config so we can get the mbean class name. */

        final ConnectorConfig connCfg = (ConnectorConfig)cs.getConfig(cn);

        if (connCfg == null) {
          error("Unable to read connector configuration " + cn);
          continue;
        }

        String mbeanClassName = connCfg.getMbeanClassName();

        if (connCfg.getMbeanClassName() == null) {
          error("Must set the mbean class name for connector " + cn);
          error("Falling back to base class for " + cn);

          mbeanClassName = NoteConnConf.class.getCanonicalName();
        }

        @SuppressWarnings("unchecked")
        final NoteConnConf<ConnectorConfig> ncc =
                (NoteConnConf<ConnectorConfig>)makeObject(mbeanClassName);
        ncc.init(cs, objectName.toString(), connCfg);

        nccs.add(ncc);
        register("connector", cn, ncc);
      }

      return true;
    } catch (final Throwable t) {
      error("Failed to start management context: " + t.getLocalizedMessage());
      error(t);
      return false;
    }
  }

  private boolean loadAdaptors() {
    try {
      final ConfigurationStore cs = getStore().getStore("adaptors");

      adaptorNames = cs.getConfigs();

      final List<AdaptorConf> nccs = new ArrayList<>();
      cfg.setAdaptorConfs(nccs);

      for (final String cn: adaptorNames) {
        final ObjectName objectName = createObjectName("adaptor", cn);

        /* Read the config so we can get the mbean class name. */

        final AdaptorConfig aCfg = (AdaptorConfig)cs.getConfig(cn);

        if (aCfg == null) {
          error("Unable to read adaptor configuration " + cn);
          continue;
        }

        String mbeanClassName = aCfg.getMbeanClassName();

        if (aCfg.getMbeanClassName() == null) {
          error("Must set the mbean class name for adaptor " + cn);
          error("Falling back to base class for " + cn);

          mbeanClassName = AdaptorConf.class.getCanonicalName();
        }

        @SuppressWarnings("unchecked")
        final AdaptorConf<AdaptorConfig> ncc =
                (AdaptorConf<AdaptorConfig>)makeObject(mbeanClassName);
        ncc.init(cs, objectName.toString(), aCfg);

        nccs.add(ncc);
        register("adaptor", cn, ncc);
      }

      return true;
    } catch (final Throwable t) {
      error("Failed to start management context: " + t.getLocalizedMessage());
      error(t);
      return false;
    }
  }

  /* ====================================================================
   *                   Protected methods
   * ==================================================================== */
}
