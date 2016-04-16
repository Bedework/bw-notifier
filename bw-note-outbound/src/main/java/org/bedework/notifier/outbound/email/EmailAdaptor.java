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
package org.bedework.notifier.outbound.email;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.bedework.caldav.util.notifications.NotificationType;
import org.bedework.caldav.util.notifications.ProcessorType;
import org.bedework.caldav.util.notifications.ResourceChangeType;
import org.bedework.notifier.Action;
import org.bedework.notifier.cnctrs.bedework.BedeworkConnector;
import org.bedework.notifier.cnctrs.bedework.BedeworkConnectorConfig;
import org.bedework.notifier.cnctrs.bedework.BedeworkSubscription;
import org.bedework.notifier.exception.NoteException;
import org.bedework.notifier.notifications.Note;
import org.bedework.notifier.outbound.common.AbstractAdaptor;
import org.bedework.util.http.BasicHttpClient;
import org.bedework.util.http.HttpUtil;
import org.bedework.util.misc.Util;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import freemarker.core.Environment;
import freemarker.ext.dom.NodeModel;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.Template;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModelException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

/* The interface implemented by destination adaptors. A destination
 * may be an email address or sms.
 *
 * @author Greg Allen
 * @author Mike Douglass
 *
 */
public class EmailAdaptor extends AbstractAdaptor<EmailConf> {

  private static final Integer SC_MULTISTATUS = 207;

  private static final String VCARD_SUFFIX = ".vcf";
  private static final String MAILTO = "mailto:";
  private static final Pattern pMailto = Pattern.compile("^" + MAILTO, Pattern.CASE_INSENSITIVE);
  private static final SimpleDateFormat recurIdFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
  private static final SimpleDateFormat jsonIdFormatUTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
  private static final SimpleDateFormat jsonIdFormatTZ = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

  private static final String HEADER_ACCEPT = "ACCEPT";
  private static final String HEADER_DEPTH = "DEPTH";
  private static final String CALENDAR_JSON_CONTENT_TYPE = "application/calendar+json";
  private static final String CALENDAR_PROPFIND =
          "<D:propfind xmlns:D='DAV:' xmlns:C='urn:ietf:params:xml:ns:caldav'>" +
             "<D:prop><D:displayname/><D:resourcetype/><C:calendar-description/></D:prop>" +
          "</D:propfind>";
  private static final XPath xPath = XPathFactory.newInstance().newXPath();

  private static final FilenameFilter templateFilter = new FilenameFilter() {
    public boolean accept(File dir, String name) {
      return name.endsWith(".ftl");
    }
  };

  final static String processorType = "email";

  private Mailer mailer;

  @Override
  public boolean process(final Action action) throws NoteException {
    final Note note = action.getNote();
    final NotificationType nt = note.getNotification();
    final EmailSubscription sub = EmailSubscription.rewrap(action.getSub());
    final ProcessorType pt = getProcessorStatus(note, processorType);

    if (processed(pt)) {
      return true;
    }

    final EmailMessage email = new EmailMessage(conf.getFrom(), null);

    // if (note.isRegisteredRecipient()) {
    //   do one thing
    // ? else { ... }

    final QName elementName =  nt.getNotification().getElementName();
    String prefix = nt.getParsed().getDocumentElement().getPrefix();

    if (prefix == null) {
      prefix = "default";
    }

    String subject = getConfig().getSubject(prefix + "-" + elementName.getLocalPart());
    if (subject == null) {
      subject = getConfig().getDefaultSubject();
    }
    email.setSubject(subject);

    List<TemplateResult> results = applyTemplates(action);
    for (TemplateResult result : results) {
      String from = result.getStringVariable("from");
      if (from != null) {
        email.setFrom(from);
      }

      for (final String to: result.getListVariable("to")) {
        email.addTo(to);
      }

      for (final String cc: result.getListVariable("cc")) {
        email.addCc(cc);
      }

      for (final String bcc: result.getListVariable("bcc")) {
        email.addBcc(bcc);
      }

      subject = result.getStringVariable("subject");
      if (subject != null) {
        email.setSubject(subject);
      }

      String contentType = result.getStringVariable("contentType");
      if (contentType == null) {
        contentType = EmailMessage.CONTENT_TYPE_PLAIN;
      }
      email.addBody(contentType, result.getValue());
    }

    if (email.getTos().size() == 0) {
            /* The subscription will define one or more recipients */
      for (final String emailAddress: sub.getEmails()) {
        email.addTo(stripMailTo(emailAddress));
      }

    }
    try {
      if (email.getBodies().keySet().size() > 0) {
        // No template results returned, don't email but still return success.
        getMailer().send(email);

        pt.setDtstamp(getDtstamp());
        pt.setStatus(HttpUtil.makeOKHttpStatus());
      }
      return true;
    } catch (final NoteException ne) {
      if (debug) {
        error(ne);
      }
    }
    return false;
  }

  private Mailer getMailer() throws NoteException {
    if (mailer == null) {
      mailer = new Mailer(getConfig());
    }
    return mailer;
  }

  private List<TemplateResult> applyTemplates(final Action action) throws NoteException {
    final Note note = action.getNote();
    final NotificationType nt = note.getNotification();
    final EmailSubscription sub = EmailSubscription.rewrap(action.getSub());

    List<TemplateResult> results = new ArrayList<TemplateResult>();
    try {
      String prefix = nt.getParsed().getDocumentElement().getPrefix();

      if (prefix == null) {
        prefix = "default";
      }

      final String abstractPath = Util.buildPath(false, Note.DeliveryMethod.email.toString(), "/", prefix, "/", nt.getNotification().getElementName().getLocalPart());

      File templateDir = new File(Util.buildPath(false, globalConfig.getTemplatesPath(), "/", abstractPath));
      if (templateDir.isDirectory()) {

        Map<String, Object> root = new HashMap<String, Object>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(nt.toXml(true))));
        Element el = doc.getDocumentElement();
        NodeModel.simplify(el);
        NodeModel.useJaxenXPathSupport();
        root.put("notification", el);

        HashSet<String> recipients = new HashSet<String>();
        for (String email : sub.getEmails()) {
            recipients.add(MAILTO + email);
        }
        root.put("recipients", recipients);

        if (globalConfig.getCardDAVHost() != null && globalConfig.getCardDAVPort() != 0 && globalConfig.getCardDAVContextPath() != null) {
          HashMap<String, Object> vcards = new HashMap<String, Object>();
          BasicHttpClient client;
          try {
            ArrayList<Header> hdrs = new ArrayList<Header>();
            BasicHeader h = new BasicHeader(HEADER_ACCEPT, globalConfig.getVCardContentType());
            hdrs.add(h);

            client = new BasicHttpClient(globalConfig.getCardDAVHost(), globalConfig.getCardDAVPort(), null, 15 * 1000);

            XPathExpression exp = xPath.compile("//*[local-name() = 'href']");
            NodeList nl = (NodeList)exp.evaluate(doc, XPathConstants.NODESET);

            HashSet<String> vcardLookups = new HashSet<String>();
            for (int i = 0; i < nl.getLength(); i++) {
              Node n = nl.item(i);
              String text = n.getTextContent();

              text = pMailto.matcher(text).replaceFirst(MAILTO);
              if (text.startsWith(MAILTO) || text.startsWith(globalConfig.getCardDAVPrincipalsPath())) {
                  vcardLookups.add(text);
              }
            }

            // Get vCards for recipients too. They may not be referenced in the notification.
            vcardLookups.addAll(recipients);

            for (String lookup : vcardLookups) {
              String path = Util.buildPath(false, globalConfig.getCardDAVContextPath() + "/" + lookup.replace(':', '/'));

              final InputStream is = client.get(path + VCARD_SUFFIX, "application/text", hdrs);
              if (is != null) {
                ObjectMapper om = new ObjectMapper();
                @SuppressWarnings("unchecked")
                ArrayList<Object> hm = om.readValue(is, ArrayList.class);
                vcards.put(lookup, hm);
              }
            }
            root.put("vcards", vcards);
          } catch (final Throwable t) {
            error(t);
          }
        }

        if (nt.getNotification() instanceof ResourceChangeType) {
          ResourceChangeType chg = (ResourceChangeType)nt.getNotification();
          BedeworkConnectorConfig cfg = ((BedeworkConnector)action.getConn()).getConnectorConfig();
          BasicHttpClient cl = getClient(cfg);
          List<Header> hdrs = getHeaders(cfg, new BedeworkSubscription(action.getSub()));

          String href = null;
          if (chg.getCreated() != null) {
            href = chg.getCreated().getHref();
          } else if (chg.getDeleted() != null) {
            href = chg.getDeleted().getHref();
          } else if (chg.getUpdated() != null && chg.getUpdated().size() > 0) {
            href = chg.getUpdated().get(0).getHref();
          }

          if (href != null) {
            if (chg.getCreated() != null || chg.getUpdated() != null) {
              // We only have an event for the templates on a create or update, not on delete.
              ObjectMapper om = new ObjectMapper();
              final InputStream is = cl.get(cfg.getSystemUrl() + href, null, hdrs);
              JsonNode a = om.readValue(is, JsonNode.class);
              
              // Check for a recurrence ID on this notification.
              XPathExpression exp = xPath.compile("//*[local-name() = 'recurrenceid']/text()");
              String rid = exp.evaluate(doc);
              if (rid != null && !rid.isEmpty()) {
                Calendar rcal = Calendar.getInstance();
                recurIdFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                rcal.setTime(recurIdFormat.parse(rid));
                
                // Find the matching recurrence ID in the JSON, and make that the only vevent object.
                Calendar c = Calendar.getInstance();
                ArrayNode vevents = (ArrayNode) a.get(2);
                for (JsonNode vevent : vevents) {
                  if (vevent.size() > 1 && vevent.get(1).size() > 1) {
                    JsonNode n = vevent.get(1).get(0);
                    if (n.get(0).asText().equals("recurrence-id")) {
                      if (n.get(1).size() > 0 && n.get(1).get("tzid") != null) {
                        jsonIdFormatTZ.setTimeZone(TimeZone.getTimeZone(n.get(1).get("tzid").asText()));
                        c.setTime(jsonIdFormatTZ.parse(n.get(3).asText()));
                      } else {
                        jsonIdFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
                        c.setTime(jsonIdFormatUTC.parse(n.get(3).asText()));
                      }
                      if (rcal.compareTo(c) == 0) {
                        vevents.removeAll();
                        vevents.add(vevent);
                        break;
                      }
                    }
                  }
                }
              }

              root.put("vevent", (ArrayList<Object>)om.convertValue(a, ArrayList.class));                
            }

            // TODO: Provide some calendar information to the templates. This is currently the publisher's
            // calendar, but needs to be fixed to be the subscriber's calendar.
            String chref = href.substring(0, href.lastIndexOf("/"));
            hdrs.add(new BasicHeader(HEADER_DEPTH, "0"));
            int rc = cl.sendRequest("PROPFIND", cfg.getSystemUrl() + chref, hdrs, "text/xml", CALENDAR_PROPFIND.length(), CALENDAR_PROPFIND.getBytes());
            if (rc == HttpServletResponse.SC_OK || rc == SC_MULTISTATUS) {
              Document d = builder.parse(new InputSource(cl.getResponseBodyAsStream()));
              HashMap<String, String> hm = new HashMap<String, String>();
              XPathExpression exp = xPath.compile("//*[local-name() = 'href']/text()");
              hm.put("href", exp.evaluate(d));
              exp = xPath.compile("//*[local-name() = 'displayname']/text()");
              hm.put("name", (String)exp.evaluate(d));
              exp = xPath.compile("//*[local-name() = 'calendar-description']/text()");
              hm.put("description", (String)exp.evaluate(d));
              root.put("calendar", hm);
            }
          }
        }

        if (note.getExtraValues() != null) {
          root.putAll(note.getExtraValues());
        }

        DefaultObjectWrapper wrapper = new DefaultObjectWrapperBuilder(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS).build();
        root.put("timezone", (TemplateHashModel) wrapper.getStaticModels().get("java.util.TimeZone"));

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

  private BasicHttpClient getClient(BedeworkConnectorConfig config) throws NoteException {
    try {
      BasicHttpClient client = new BasicHttpClient(30 * 1000, false);  // followRedirects
      client.setBaseURI(new URI(config.getSystemUrl()));
      return client;
    } catch (final Throwable t) {
      throw new NoteException(t);
    }
  }

  protected List<Header> getHeaders(BedeworkConnectorConfig config, BedeworkSubscription sub) {
    List<Header> hdrs = new ArrayList<>(1);
    hdrs.add(new BasicHeader("X-BEDEWORK-NOTEPR", sub.getPrincipalHref()));
    hdrs.add(new BasicHeader("X-BEDEWORK-PT", sub.getUserToken()));
    hdrs.add(new BasicHeader("X-BEDEWORK-NOTE", config.getName() + ":" + config.getToken()));
    hdrs.add(new BasicHeader("X-BEDEWORK-EXTENSIONS", "true"));
    hdrs.add(new BasicHeader(HEADER_ACCEPT, CALENDAR_JSON_CONTENT_TYPE));
    return hdrs;
  }

  protected String stripMailTo(final String address) {
    return pMailto.matcher(address).replaceFirst("");
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
