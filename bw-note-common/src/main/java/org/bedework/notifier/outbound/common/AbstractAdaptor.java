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
package org.bedework.notifier.outbound.common;

import org.bedework.caldav.util.notifications.ProcessorType;
import org.bedework.caldav.util.notifications.ProcessorsType;
import org.bedework.notifier.Action;
import org.bedework.notifier.conf.NotifyConfig;
import org.bedework.notifier.exception.NoteException;
import org.bedework.notifier.notifications.Note;
import org.bedework.util.http.HttpUtil;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;
import org.bedework.util.xml.NsContext;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import net.fortuna.ical4j.model.property.DtStamp;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletResponse;

/** Some useful methods..
 *
 * @author Mike Douglass
 *
 */
public abstract class AbstractAdaptor<Conf extends AdaptorConf>
        implements Logged, Adaptor<Conf> {
  private final static AtomicLong nextId = new AtomicLong();

  private final Long id;

  protected NotifyConfig globalConfig;

  protected Conf conf;

  protected Configuration fmConfig;

  protected NsContext nsContext = new NsContext(null);

  protected AbstractAdaptor() {
    id = nextId.incrementAndGet();
  }

  @Override
  public long getId() {
    return id;
  }

  @Override
  public void setConf(final NotifyConfig globalConfig,
                      final Conf conf) {
    this.globalConfig = globalConfig;
    this.conf = conf;

    try {
      // Init freemarker
      fmConfig = new Configuration(Configuration.getVersion());
      fmConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
      fmConfig.setDefaultEncoding("UTF-8");

      final File templateDir = new File(globalConfig.getTemplatesPath());
      fmConfig.setDirectoryForTemplateLoading(templateDir);
    } catch (final Throwable t) {
      throw new NoteException(t);
    }
  }

  @Override
  public Conf getConfig() {
    return conf;
  }

  public String getType() {
    return conf.getType();
  }

  @Override
  public abstract boolean process(final Action action);

    /* ============================================================
     *                   Protected methods
     * ============================================================ */

  protected String getDtstamp() {
    return new DtStamp().getValue();
  }

  /** Get the processor status information from the notification. If
   * one does not exist for the given type we add one.
   *
   * @param note the notification
   * @param processor type
   * @return processor information
   */
  protected ProcessorType getProcessorStatus(final Note note,
                                             final String processor) {
    ProcessorsType pst = note.getNotification().getProcessors();
    ProcessorType pt = null;

    if (pst != null) {
      for (final ProcessorType notePt: pst.getProcessor()) {
        // TODO Define standard types?
        if (notePt.getType().equals(processor)) {
          pt = notePt;
          break;
        }
      }
    }

    if (pt != null) {
      return pt;
    }

    if (pst == null) {
      pst = new ProcessorsType();
      note.getNotification().setProcessors(pst);
    }
    pt = new ProcessorType();
    pt.setType(processor);

    pst.getProcessor().add(pt);

    return pt;
  }

  protected boolean processed(final ProcessorType pt) {
    if (pt.getStatus() == null) {
      return false;
    }

    try {
      final int scode = HttpUtil.getHttpStatus(pt.getStatus()).getStatusCode();

      return scode == HttpServletResponse.SC_OK;
    } catch (final Throwable t) {
      warn("Bad status: " + pt.getStatus());
      return false;
    }
  }

  /* ==============================================================
   *                   Logged methods
   * ============================================================== */

  private final BwLogger logger = new BwLogger();

  @Override
  public BwLogger getLogger() {
    if ((logger.getLoggedClass() == null) && (logger.getLoggedName() == null)) {
      logger.setLoggedClass(getClass());
    }

    return logger;
  }
}
