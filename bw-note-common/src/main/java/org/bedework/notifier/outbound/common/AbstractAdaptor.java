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

import org.bedework.caldav.util.notifications.NotificationType;
import org.bedework.caldav.util.notifications.ProcessorType;
import org.bedework.caldav.util.notifications.ProcessorsType;
import org.bedework.notifier.Action;
import org.bedework.notifier.conf.NotifyConfig;
import org.bedework.notifier.exception.NoteException;
import org.bedework.notifier.notifications.Note;
import org.bedework.util.http.HttpUtil;
import org.bedework.util.misc.Util;
import org.bedework.util.xml.NsContext;
import org.bedework.util.xml.XmlUtil;
import org.bedework.util.xml.tagdefs.BedeworkServerTags;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import net.fortuna.ical4j.model.property.DtStamp;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

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

  public String applyTemplate(final QName noteType,
                              final Note.DeliveryMethod handlerType,
                              final NotificationType note,
                              final Map extraValues) throws NoteException {
    try {
      String prefix = nsContext.getPrefix(noteType.getNamespaceURI());

      if (prefix == null) {
        prefix = "default";
      }

      final String abstractPath = Util.buildPath(false,
                                                 handlerType
                                                         .toString(),
                                                 "/",
                                                 prefix,
                                                 "/",
                                                 noteType.getLocalPart() + ".ftl");

//      Element el = parsedNote.getDocumentElement();
//      NodeModel.simplify(parsedNote);
//      NodeModel nm = NodeModel.wrap(parsedNote);
//       String xml = note.toXml(true);

//      NodeModel.useJaxenXPathSupport();
//      final NodeModel nm = NodeModel.parse(new InputSource(new StringReader(xml)));
      Map<String, Object> root = new HashMap();

      toMap(note.getParsed().getDocumentElement(), root);
//      root.put("notification", nm);
      if (extraValues != null) {
        prefix = nsContext.getPrefix(BedeworkServerTags.notifyValues.getNamespaceURI());
        if (prefix == null) {
          prefix = "";
        }

        Set<String> keys = extraValues.keySet();
        for (final String key: keys) {
          Object val = extraValues.get(key);

          if (val instanceof Document) {
            /* Convert the value to a map */
            Map docMap = new HashMap();
            toMap(((Document)val).getDocumentElement(),
                  docMap);
            extraValues.put(key, docMap);
          }
        }

        root.put(prefix + BedeworkServerTags.notifyValues.getLocalPart(),
                 extraValues);
      }

      Template template = fmConfig.getTemplate(abstractPath);

      Writer out = new StringWriter();
      template.process(root, out);

      return out.toString();
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

  private void toMap(final Element el,
                     final Map map) throws NoteException {
    try {
      String prefix = nsContext.getPrefix(el.getNamespaceURI());

      final String name;
      if (prefix == null) {
        name = el.getLocalName();
      } else {
        name = prefix + el.getLocalName();
      }

      try {
        if (!XmlUtil.hasChildren(el) && XmlUtil.hasContent(el)) {
          map.put(name, XmlUtil.getElementContent(el));
          return;
        }
      } catch (final Throwable ignored) {}

      Map childMap = new HashMap();

      map.put(name, childMap);

      for (final Element ch: XmlUtil.getElements(el)) {
        toMap(ch, childMap);
      }
    } catch (final Throwable t) {
      throw new NoteException(t);
    }
  }
}
