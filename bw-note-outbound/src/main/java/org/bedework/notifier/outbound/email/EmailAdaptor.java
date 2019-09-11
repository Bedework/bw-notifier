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
import org.bedework.util.http.Headers;
import org.bedework.util.http.HttpUtil;
import org.bedework.util.http.PooledHttpClient;
import org.bedework.util.http.PooledHttpClient.ResponseHolder;
import org.bedework.util.misc.Util;

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
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
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

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import static org.apache.http.HttpStatus.SC_OK;

/** The interface implemented by destination adaptors. A destination
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
  private DocumentBuilder builder;

  public EmailAdaptor() {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    try {
      builder = factory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      error(e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean process(final Action action) throws NoteException {
    final Note note = action.getNote();
    final NotificationType nt = note.getNotification();
    final EmailSubscription sub = EmailSubscription.rewrap(action.getSub());
    final ProcessorType pt = getProcessorStatus(note, processorType);

    if (debug()) {
        debug("EmailAdaptor: processing notifications for " + sub.getPrincipalHref());
    }
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
        getMailer().send(email);

        pt.setDtstamp(getDtstamp());
        pt.setStatus(HttpUtil.makeOKHttpStatus());
      } else {
        // No template results returned, don't email but still return success.
        if (debug()) {
          debug("EmailAdaptor: no emails to be sent for " + sub.getPrincipalHref());
        }
      }
      return true;
    } catch (final NoteException ne) {
      if (debug()) {
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

    List<TemplateResult> results = new ArrayList<>();
    try {
      String prefix = nt.getParsed().getDocumentElement().getPrefix();

      if (prefix == null) {
        prefix = "default";
      }

      final String abstractPath = Util.buildPath(false, Note.DeliveryMethod.email.toString(), "/", prefix, "/", nt.getNotification().getElementName().getLocalPart());

      File templateDir = new File(Util.buildPath(false, globalConfig.getTemplatesPath(), "/", abstractPath));
      if (templateDir.isDirectory()) {

        Map<String, Object> root = new HashMap<>();
        Document doc = builder.parse(new InputSource(new StringReader(nt.toXml(true))));
        Element el = doc.getDocumentElement();
        NodeModel.simplify(el);
        NodeModel.useJaxenXPathSupport();
        root.put("notification", el);

        HashSet<String> recipients = new HashSet<>();
        for (String email : sub.getEmails()) {
            recipients.add(MAILTO + email);
        }
        root.put("recipients", recipients);

        if (globalConfig.getCardDAVURI() != null) {
          HashMap<String, Object> vcards = new HashMap<>();

          PooledHttpClient client;
          try {
            client = new PooledHttpClient(new URI(globalConfig.getCardDAVURI()));

            XPathExpression exp = xPath.compile("//*[local-name() = 'href']");
            NodeList nl = (NodeList)exp.evaluate(doc, XPathConstants.NODESET);

            HashSet<String> vcardLookups = new HashSet<>();
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
              String path = Util.buildPath(false, "/", lookup.replace(':', '/'));

              final ResponseHolder resp =
                      client.get(path + VCARD_SUFFIX,
                                 "application/text",
                                 this::processGetJsonVcard);
              if (!resp.failed) {
                ArrayList<Object> hm = (ArrayList<Object>)resp.response;
                vcards.put(lookup, hm);
              }
            }
            root.put("vcards", vcards);
          } catch (final Throwable t) {
            error(t);
          }
        }

        doResourceChange: {
          if (!(nt.getNotification() instanceof ResourceChangeType)) {
            break doResourceChange;
          }
          ResourceChangeType chg = (ResourceChangeType)nt.getNotification();
          BedeworkConnectorConfig cfg =
                  ((BedeworkConnector)action.getConn()).getConnectorConfig();
          PooledHttpClient cl = getSystemClient(cfg);
          cl.setHeadersFetcher(
                  new SubHeadersFetcher(cfg,
                                        new BedeworkSubscription(action.getSub())));

          String href = null;
          if (chg.getCreated() != null) {
            href = chg.getCreated().getHref();
          } else if (chg.getDeleted() != null) {
            href = chg.getDeleted().getHref();
          } else if (chg.getUpdated() != null && chg.getUpdated().size() > 0) {
            href = chg.getUpdated().get(0).getHref();
          }

          if (href == null) {
            break doResourceChange;
          }

          doCreateUpdate: {
            if (chg.getDeleted() != null) {
              // We only have an event for the templates on a
              // create or update, not on delete.
              break doCreateUpdate;
            }
            final ResponseHolder resp = cl.get(href, null,
                                               this::processGetJsonNode);
            if (resp.failed) {
              break doCreateUpdate;
            }
            JsonNode a = (JsonNode)resp.response;
              
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

            final ObjectMapper om = new ObjectMapper();
            root.put("vevent", (ArrayList<Object>)om.convertValue(a, ArrayList.class));
          } // doCreateUpdate

          // TODO: Provide some calendar information to the templates. This is currently the publisher's
          // calendar, but needs to be fixed to be the subscriber's calendar.
          String chref = href.substring(0, href.lastIndexOf("/"));

          final ResponseHolder resp =
                  cl.propfind(chref, "0",
                              CALENDAR_PROPFIND,
                              this::processCalendarPropfind);
          if (resp.failed) {
            break doResourceChange;
          }

          root.put("calendar", resp.response);
        } // doResourceChange

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

  final ResponseHolder processGetJsonVcard(final String path,
                                           final CloseableHttpResponse resp) {
    try {
      final int status = HttpUtil.getStatus(resp);

      if (status != SC_OK) {
        return new ResponseHolder(status,
                                  "Failed response from server");
      }

      if (resp.getEntity() == null) {
        return new ResponseHolder(status,
                                  "No content in response from server");
      }

      final InputStream is = resp.getEntity().getContent();

      ObjectMapper om = new ObjectMapper();
      @SuppressWarnings("unchecked")
      ArrayList<Object> hm = om.readValue(is, ArrayList.class);

      return new ResponseHolder(hm);
    } catch (final Throwable t) {
      return new ResponseHolder(t);
    }
  }

  final ResponseHolder processGetJsonNode(final String path,
                                          final CloseableHttpResponse resp) {
    try {
      final int status = HttpUtil.getStatus(resp);

      if (status != SC_OK) {
        return new ResponseHolder(status,
                                  "Failed response from server");
      }

      if (resp.getEntity() == null) {
        return new ResponseHolder(status,
                                  "No content in response from server");
      }

      final InputStream is = resp.getEntity().getContent();

      ObjectMapper om = new ObjectMapper();

      JsonNode a = om.readValue(is, JsonNode.class);

      return new ResponseHolder(a);
    } catch (final Throwable t) {
      return new ResponseHolder(t);
    }
  }

  final ResponseHolder processCalendarPropfind(final String path,
                                               final CloseableHttpResponse resp) {
    try {
      final int status = HttpUtil.getStatus(resp);

      if (status != SC_OK) {
        return new ResponseHolder(status,
                                  "Failed response from server");
      }

      if (resp.getEntity() == null) {
        return new ResponseHolder(status,
                                  "No content in response from server");
      }

      final InputStream is = resp.getEntity().getContent();

      Document d = builder.parse(new InputSource(is));

      HashMap<String, String> hm = new HashMap<>();

      XPathExpression exp =
              xPath.compile("//*[local-name() = 'href']/text()");
      hm.put("href", exp.evaluate(d));
      exp = xPath.compile("//*[local-name() = 'displayname']/text()");
      hm.put("name", (String)exp.evaluate(d));
      exp = xPath.compile("//*[local-name() = 'calendar-description']/text()");
      hm.put("description", exp.evaluate(d));

      return new ResponseHolder(hm);
    } catch (final Throwable t) {
      return new ResponseHolder(t);
    }
  }

  private PooledHttpClient getSystemClient(BedeworkConnectorConfig config) throws NoteException {
    try {
      final PooledHttpClient client = new PooledHttpClient(new URI(config.getSystemUrl()));

      return client;
    } catch (final Throwable t) {
      throw new NoteException(t);
    }
  }

  private class SubHeadersFetcher
          implements HttpUtil.HeadersFetcher {
    private Headers hdrs = new Headers();

    SubHeadersFetcher(final BedeworkConnectorConfig config,
                      final BedeworkSubscription sub) {
      hdrs.add(new BasicHeader("X-BEDEWORK-NOTEPR", sub.getPrincipalHref()));
      hdrs.add(new BasicHeader("X-BEDEWORK-PT", sub.getUserToken()));
      hdrs.add(new BasicHeader("X-BEDEWORK-NOTE", config.getName() + ":" + config.getToken()));
      hdrs.add(new BasicHeader("X-BEDEWORK-EXTENSIONS", "true"));
      hdrs.add(new BasicHeader(HEADER_ACCEPT, CALENDAR_JSON_CONTENT_TYPE));
      hdrs.add(new BasicHeader(HEADER_ACCEPT, globalConfig.getVCardContentType()));

    }

    public Headers get() {
      return hdrs;
    }
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
      List<String> result = new ArrayList<>();
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
