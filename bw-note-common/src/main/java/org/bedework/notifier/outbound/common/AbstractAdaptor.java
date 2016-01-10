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
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

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
public abstract class AbstractAdaptor<Conf extends AdaptorConf>
        implements Adaptor<Conf> {
  private transient Logger log;

  protected boolean debug;

  private final static AtomicLong nextId = new AtomicLong();

  private final Long id;

  protected NotifyConfig globalConfig;

  protected Conf conf;

  private Configuration fmConfig;

  protected NsContext nsContext = new NsContext(null);

  private static final String VCARD_SUFFIX = ".vcf"; 
  private static final String MAILTO = "mailto:";

  private static final FilenameFilter templateFilter = new FilenameFilter() {
    public boolean accept(File dir, String name) {
      return name.endsWith(".ftl");
    }
  };

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
      fmConfig = new Configuration();
      fmConfig.setTemplateExceptionHandler(
              TemplateExceptionHandler.RETHROW_HANDLER);
      fmConfig.setDefaultEncoding("UTF-8");

      File templateDir = new File(globalConfig.getTemplatesPath());
      fmConfig.setDirectoryForTemplateLoading(templateDir);
    } catch (final Throwable t) {
      throw new NoteException(t);
    }
  }

  public List<TemplateResult> applyTemplates(final QName noteType,
                                             final Note.DeliveryMethod handlerType,
                                             final NotificationType note,
                                             final Map<String, Object> extraValues) throws NoteException {
    List<TemplateResult> results = new ArrayList<TemplateResult>();
    try {
      String prefix = note.getParsed().getDocumentElement().getPrefix();

      if (prefix == null) {
        prefix = "default";
      }

      final String abstractPath = Util.buildPath(false, handlerType.toString(), "/", prefix, "/", noteType.getLocalPart());

      File templateDir = new File(Util.buildPath(false, globalConfig.getTemplatesPath(), "/", abstractPath));
      if (templateDir.isDirectory()) {

        Map<String, Object> root = new HashMap<String, Object>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(note.toXml(true))));
        Element el = doc.getDocumentElement();
        NodeModel.simplify(el);
        NodeModel.useJaxenXPathSupport();
        root.put("notification", el);

        if (globalConfig.getCardDAVHost() != null && globalConfig.getCardDAVPort() != 0 && globalConfig.getCardDAVContextPath() != null) {
          HashMap<String, Object> vcards = new HashMap<String, Object>();
          BasicHttpClient client;
          try {
            ArrayList<Header> hdrs = new ArrayList<Header>();
            BasicHeader h = new BasicHeader("ACCEPT", globalConfig.getVCardContentType());
            hdrs.add(h);

            client = new BasicHttpClient(globalConfig.getCardDAVHost(), globalConfig.getCardDAVPort(), null, 15 * 1000);

            XPath xPath = XPathFactory.newInstance().newXPath();
            XPathExpression exp = xPath.compile("//*[local-name() = 'href']");
            NodeList nl = (NodeList)exp.evaluate(doc, XPathConstants.NODESET);

            for (int i = 0; i < nl.getLength(); i++) {
              Node n = nl.item(i);
              String text = n.getTextContent();

              if ((text.startsWith(MAILTO) || text.startsWith(globalConfig.getCardDAVPrincipalsPath())) && !root.containsKey(text)) {
                String path = Util.buildPath(false, globalConfig.getCardDAVContextPath() + "/" + text.replace(':', '/'));

                final InputStream is = client.get(path + VCARD_SUFFIX, "application/text", hdrs);
                if (is != null) {
                  ObjectMapper om = new ObjectMapper();
                  ArrayList<Object> hm = om.readValue(is, ArrayList.class);
                  vcards.put(text, hm);
                }
              }
            }
            root.put("vcards", vcards);
          } catch (final Throwable t) {
            error(t);
          }
        }

        if (extraValues != null) {
          root.putAll(extraValues);
        }

        // Sort files so the user can control the order of content types/body parts of the email by template file name.
        File[] templates = templateDir.listFiles(templateFilter);
        Arrays.sort(templates);
        for (File f : templates) {
          Template template = fmConfig.getTemplate(Util.buildPath(false, abstractPath, "/", f.getName()));
          Writer out = new StringWriter();
          Environment env = template.createProcessingEnvironment(root, out);
          env.process();

          TemplateResult r = new TemplateResult(f.getName(), out.toString(), env);
          if (!r.getBooleanVariable("skip")) {
            results.add(new TemplateResult(f.getName(), out.toString(), env));
          }
        }
      }
    } catch (final Throwable t) {
      throw new NoteException(t);
    }
    return results;
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

  protected String stripMailTo(final String address) {
    return address.replaceAll("^mailto:", "");
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

  public class TemplateResult {
    String templateName;
    String value;
    Environment env;

    protected TemplateResult(String templateName, String value, Environment env) {
      this.templateName = templateName;
      this.value = value;
      this.env = env;
    }

    public String getValue() {
      return value;
    }

    public String getStringVariable(String key) {
      try {
        Object val = env.getVariable(key);
        if (val != null) {
          return val.toString();
        }
      } catch (TemplateModelException tme) {
      }
      return null;
    }

    public Boolean getBooleanVariable(String key) {
      try {
        Object val = env.getVariable(key);
        if (val != null) {
          return Boolean.valueOf(val.toString());
        }
      } catch (TemplateModelException tme) {
      }
      return false;
    }

    public List<String> getListVariable(String key) {
      List<String> result = new ArrayList<String>();
      try {
        Object val = env.getVariable(key);
        if (val != null) {
          result = Arrays.asList(val.toString().split("\\s*;\\s*"));;
        }
      } catch (TemplateModelException tme) {
      }
      return result;
    }
  }
}
