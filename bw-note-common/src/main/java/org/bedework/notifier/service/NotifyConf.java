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
import org.bedework.util.config.ConfigurationStore;
import org.bedework.util.jmx.ConfBase;
import org.bedework.util.jmx.ConfigHolder;

import java.util.ArrayList;
import java.util.List;

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

  private boolean running;

  private NotifyEngine notifier;

  /* Be safe - default to false */
  private boolean export;

  private class ProcessorThread extends Thread {
    boolean showedTrace;

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
        } catch (Throwable t) {
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
            Object o = new Object();
            synchronized (o) {
              o.wait (10 * 1000);
            }
          } catch (Throwable t) {
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
   * @param val
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

  @Override
  public List<String> getConnectorNames() {
    return connectorNames;
  }

  @Override
  public List<Stat> getStats() {
    if (notifier == null) {
      return new ArrayList<Stat>();
    }

    return notifier.getStats();
  }

  /* ========================================================================
   * Operations
   * ======================================================================== */

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
    } catch (InterruptedException ie) {
    } catch (Throwable t) {
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

      String res = loadOnlyConfig(NotifyConfig.class);

      if (res != null) {
        return res;
      }

      /* Load up the connectors */

      ConfigurationStore cs = getStore().getStore("connectors");

      connectorNames = cs.getConfigs();

      List<NoteConnConf> sccs = new ArrayList<NoteConnConf>();
      cfg.setConnectorConfs(sccs);

      for (String cn: connectorNames) {
        ObjectName objectName = createObjectName("connector", cn);

        /* Read the config so we can get the mbean class name. */

        ConnectorConfig connCfg = (ConnectorConfig)cs.getConfig(cn);

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
        NoteConnConf<ConnectorConfig> scc = (NoteConnConf<ConnectorConfig>)makeObject(mbeanClassName);
        scc.init(cs, objectName.toString(), connCfg);

        scc.saveConfig();
        sccs.add(scc);
        register("connector", cn, scc);
      }

      return "OK";
    } catch (Throwable t) {
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

  /* ====================================================================
   *                   Protected methods
   * ==================================================================== */
}
