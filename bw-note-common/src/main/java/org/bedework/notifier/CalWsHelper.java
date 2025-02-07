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
package org.bedework.notifier;

import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;
import org.bedework.util.xml.XmlUtil;

import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.io.Writer;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;


/** Help for CalWS
 *
 *   @author Mike Douglass   douglm bedework.edu
 */
public class CalWsHelper implements Logged {
  private MessageFactory soapMsgFactory;

  /** Trace a calws SOAP message
   *
   * @param o object
   * @throws Throwable on error
   */
  public void traceSoap(final Object o) throws Throwable {
    final SOAPMessage msg =
            marshal(o, "org.oasis_open.docs.ns.wscal.calws_soap");

    /* SOAP marshaller doesn't appear to form
    msg.writeTo(pstate.traceOut);
    */
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    msg.writeTo(baos);

    final var bais = new ByteArrayInputStream(baos.toByteArray());

    final StreamSource src = new StreamSource(bais);

    final Transformer serializer =
            SAXTransformerFactory.newInstance().newTransformer();
    serializer.setOutputProperty(OutputKeys.INDENT, "yes");

    //serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    //serializer.setOutputProperty("{http://xml.customer.org/xslt}indent-amount", "2");

    final Writer wtr = new StringWriter();
    final StreamResult res =  new StreamResult(wtr);
    serializer.transform(src, res);

    trace(wtr.toString());
  }

  /**
   * @param o the object
   * @param jaxbContextPath path
   * @return SOAPMessage
   * @throws Throwable on error
   */
  public SOAPMessage marshal(final Object o,
                                final String jaxbContextPath) throws Throwable {
    if (soapMsgFactory == null) {
      soapMsgFactory = MessageFactory.newInstance();
    }

    final JAXBContext jc = JAXBContext.newInstance(jaxbContextPath);

    final Marshaller marshaller = jc.createMarshaller();

    final Document doc = XmlUtil.safeNewDocument(true);

    final SOAPMessage msg = soapMsgFactory.createMessage();
    msg.getSOAPBody().addDocument(doc);

    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    marshaller.marshal(o,
                       msg.getSOAPBody());

    return msg;
  }

  /* ===============================================================
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
