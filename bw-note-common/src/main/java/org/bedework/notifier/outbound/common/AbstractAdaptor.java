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

import com.fasterxml.jackson.databind.ObjectMapper;

import org.bedework.caldav.util.notifications.NotificationType;
import org.bedework.caldav.util.notifications.ProcessorType;
import org.bedework.caldav.util.notifications.ProcessorsType;
import org.bedework.notifier.Action;
import org.bedework.notifier.conf.NotifyConfig;
import org.bedework.notifier.exception.NoteException;
import org.bedework.notifier.notifications.Note;
import org.bedework.util.http.BasicHttpClient;
import org.bedework.util.http.HttpUtil;
import org.bedework.util.misc.Util;
import org.bedework.util.xml.NsContext;

import freemarker.core.Environment;
import freemarker.ext.dom.NodeModel;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateModelException;
import net.fortuna.ical4j.model.property.DtStamp;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

/** Some useful methods..
 *
 * @author Mike Douglass
 *
 */
public abstract class AbstractAdaptor<Conf extends AdaptorConf> implements Adaptor<Conf> {
  private transient Logger log;

  protected boolean debug;

  private final static AtomicLong nextId = new AtomicLong();

  private final Long id;

  protected NotifyConfig globalConfig;

  protected Conf conf;

  protected Configuration fmConfig;

  protected NsContext nsContext = new NsContext(null);

  protected AbstractAdaptor() {
    debug = getLogger().isDebugEnabled();
    id = nextId.incrementAndGet();
  }

  @Override
  public long getId() {
    return id;
  }

  @Override
  public void setConf(final NotifyConfig globalConfig,
                      final Conf conf) throws NoteException {
    this.globalConfig = globalConfig;
    this.conf = conf;

    try {
      // Init freemarker
      fmConfig = new Configuration(Configuration.getVersion());
      fmConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
      fmConfig.setDefaultEncoding("UTF-8");

      File templateDir = new File(globalConfig.getTemplatesPath());
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
  public abstract boolean process(final Action action) throws NoteException;

    /* ====================================================================
     *                   Protected methods
     * ==================================================================== */

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

  protected void info(final String msg) {
    getLogger().info(msg);
  }

  protected void trace(final String msg) {
    getLogger().debug(msg);
  }

  protected void error(final Throwable t) {
    getLogger().error(this, t);
  }

  protected void error(final String msg) {
    getLogger().error(msg);
  }

  protected void warn(final String msg) {
    getLogger().warn(msg);
  }

  /* Get a logger for messages
   */
  protected Logger getLogger() {
    if (log == null) {
      log = Logger.getLogger(this.getClass());
    }

    return log;
  }
}
