/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.notifier;

import org.bedework.notifier.cnctrs.Connector;
import org.bedework.notifier.conf.ConnectorConfig;
import org.bedework.notifier.conf.NotifyConfig;
import org.bedework.notifier.db.NotifyDb;
import org.bedework.notifier.db.SubscriptionWrapper;
import org.bedework.notifier.exception.NoteException;
import org.bedework.notifier.service.NoteConnConf;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: mike Date: 8/4/15 Time: 19:41
 */
public class NotifyRegistry implements Logged {
  private NotifyConfig config;

  public interface Authenticator {
    /**
     *
     * @param token an opaque token
     * @return true for ok
     * @throws NoteException on error
     */
    boolean authenticate(final String token) throws NoteException;
  }

  public static class ConnectorEntry {
    private final Connector connector;

    private final Info info;

    public ConnectorEntry(final Connector connector,
                          final Info info) {
      this.connector = connector;
      this.info = info;
    }

    public Connector getConnector() {
      return connector;
    }

    public Info getInfo() {
      return info;
    }
  }

  public static class Info {
    private final String type;

    private final Class<? extends SubscriptionWrapper> subscriptionClass;

    private final Authenticator authenticator;

    public Info(final String type,
                final Class<? extends SubscriptionWrapper> subscriptionClass,
                final Authenticator authenticator) {
      this.type = type;
      this.subscriptionClass = subscriptionClass;
      this.authenticator = authenticator;
    }

    public String getType() {
      return type;
    }

    public Class<? extends SubscriptionWrapper> getSubscriptionClass() {
      return subscriptionClass;
    }

    public Authenticator getAuthenticator() {
      return authenticator;
    }
  }

  private static final Map<String, ConnectorEntry> registry = new HashMap<>();

  public static Info getInfo(final String type) {
    final ConnectorEntry ce = registry.get(type);

    if (ce == null) {
      return null;
    }

    return ce.getInfo();
  }

  public static Connector getConnector(final String type) {
    return registry.get(type).getConnector();
  }

  public static List<Connector> getConnectors() {
    List<Connector> conns = new ArrayList<>();

    for (final ConnectorEntry ce: registry.values()) {
      conns.add(ce.getConnector());
    }
    return conns;
  }

  public void registerConnectors(final NotifyConfig config) throws NoteException {
    this.config = config;
    final List<NoteConnConf> connectorConfs = config.getConnectorConfs();

    /* Register the connectors */
    for (final NoteConnConf ncc: connectorConfs) {
      final ConnectorConfig conf = (ConnectorConfig)ncc.getConfig();
      final String cnctrName = conf.getName();
      info("Register and start connector " + cnctrName);

      registerConnector(cnctrName, conf);

      final Connector conn = getConnector(cnctrName);
      ncc.setConnector(conn);
    }
  }

  public void startConnectors(final NotifyDb db,
                              final NotifyEngine notifier) throws NoteException {
    final String callbackUriBase = config.getCallbackURI();

    for (final String id: registry.keySet()) {
      Connector conn = getConnector(id);

      conn.start(db,
                 callbackUriBase + id + "/",
                 notifier);

      while (!conn.isStarted()) {
          /* Wait for it to start */
        synchronized (this) {
          try {
            this.wait(250);
          } catch (final InterruptedException e) {
            throw new NoteException(e);
          }
        }

        if (conn.isFailed()) {
          error("Connector " + id + " failed to start");
          break;
        }
      }
    }
  }

  private void registerConnector(final String name,
                                 final ConnectorConfig conf) throws NoteException {
    try {
      Class cl = Class.forName(conf.getConnectorClassName());

      if (registry.containsKey(name)) {
        throw new NoteException("Connector " + name + " already registered");
      }

      final Connector c = (Connector)cl.newInstance();

      c.init(name, conf);

      final ConnectorEntry ce = new ConnectorEntry(c, c.getInfo());

      registry.put(name, ce);
    } catch (final Throwable t) {
      throw new NoteException(t);
    }
  }

  /* ====================================================================
   *                   Logged methods
   * ==================================================================== */

  private BwLogger logger = new BwLogger();

  @Override
  public BwLogger getLogger() {
    if ((logger.getLoggedClass() == null) && (logger.getLoggedName() == null)) {
      logger.setLoggedClass(getClass());
    }

    return logger;
  }
}
