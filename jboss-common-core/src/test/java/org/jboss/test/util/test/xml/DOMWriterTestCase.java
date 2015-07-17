/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors as indicated
 * by the @authors tag.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.test.util.test.xml;

// $Id$

import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.jboss.util.xml.DOMUtils;
import org.jboss.util.xml.DOMWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test the DOMWriter
 *
 * @author Thomas.Diesler@jboss.org
 * @since 10-Aug-2006
 */
public class DOMWriterTestCase extends TestCase
{
   /** The element does not contain the required ns declaration.
    */
   public void testNamespaceCompletionOne() throws Exception
   {
      String inStr = 
         "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
          "<env:Body>" +
           "<env:Fault>" +
            "<faultcode>env:Client</faultcode>" +
            "<faultstring>Endpoint {http://webmethod.jsr181.ws.test.jboss.org/jaws}TestEndpointPort does not contain operation meta data for: {http://webmethod.jsr181.ws.test.jboss.org/jaws}noWebMethod</faultstring>" +
           "</env:Fault>" +
          "</env:Body>" +
         "</env:Envelope>";
      
      Element env = DOMUtils.parse(inStr);
      Element body = DOMUtils.getFirstChildElement(env);
      Element fault = DOMUtils.getFirstChildElement(body);
      
      String expStr = 
         "<env:Fault xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
          "<faultcode>env:Client</faultcode>" +
          "<faultstring>Endpoint {http://webmethod.jsr181.ws.test.jboss.org/jaws}TestEndpointPort does not contain operation meta data for: {http://webmethod.jsr181.ws.test.jboss.org/jaws}noWebMethod</faultstring>" +
         "</env:Fault>";
      
      String wasStr = DOMWriter.printNode(fault, false);
      assertEquals(expStr, wasStr);
   }
   
   /** The element already contains the required ns declaration.
    */
   public void testNamespaceCompletionTwo() throws Exception
   {
      String inStr = 
         "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
          "<env:Body>" +
           "<ns1:rpc xmlns:ns1='http://somens'>" +
            "<ns1:param1/>" +
            "<ns1:param2/>" +
           "</ns1:rpc>" +
          "</env:Body>" +
         "</env:Envelope>";
      
      Element env = DOMUtils.parse(inStr);
      Element body = DOMUtils.getFirstChildElement(env);
      Element rpc = DOMUtils.getFirstChildElement(body);
      
      String expStr = 
         "<ns1:rpc xmlns:ns1='http://somens'>" +
          "<ns1:param1/>" +
          "<ns1:param2/>" +
         "</ns1:rpc>";
      
      String wasStr = DOMWriter.printNode(rpc, false);
      assertEquals(expStr, wasStr);
   }
   
   /** The element does not contain the required ns declaration, the child does.
    */
   public void testNamespaceCompletionThree() throws Exception
   {
      String inStr = 
         "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
          "<env:Body>" +
           "<ns1:rpc xmlns:ns1='http://somens'>" +
            "<ns1:param1/>" +
            "<ns1:param2/>" +
           "</ns1:rpc>" +
          "</env:Body>" +
         "</env:Envelope>";
      
      Element env = DOMUtils.parse(inStr);
      Element body = DOMUtils.getFirstChildElement(env);
      
      String expStr = 
         "<env:Body xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
          "<ns1:rpc xmlns:ns1='http://somens'>" +
           "<ns1:param1/>" +
           "<ns1:param2/>" +
          "</ns1:rpc>" +
         "</env:Body>";
      
      String wasStr = DOMWriter.printNode(body, false);
      assertEquals(expStr, wasStr);
   }
   
   /** The envelope defines a default namespace
    */
   public void testNamespaceCompletionDefault() throws Exception
   {
      String inStr = 
         "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/' xmlns='http://somens'>" +
          "<env:Body>" +
           "<rpc>" +
            "<param1/>" +
            "<param2/>" +
           "</rpc>" +
          "</env:Body>" +
         "</env:Envelope>";
      
      Element env = DOMUtils.parse(inStr);
      Element body = DOMUtils.getFirstChildElement(env);
      Element rpc = DOMUtils.getFirstChildElement(body);
      
      String expStr = 
         "<rpc xmlns='http://somens'>" +
          "<param1/>" +
          "<param2/>" +
         "</rpc>";
      
      String wasStr = DOMWriter.printNode(rpc, false);
      assertEquals(expStr, wasStr);
   }
   
   /** The element does not contain the required attribute ns declaration.
    */
   public void testNamespaceCompletionAttribute() throws Exception
   {
      String inStr = 
         "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
          "<env:Header>" +
           "<someHeader env:mustUnderstand='1' xml:lang='en'/>" +
          "</env:Header>" +
          "<env:Body/>" +
         "</env:Envelope>";
      
      Element env = DOMUtils.parse(inStr);
      Element header = DOMUtils.getFirstChildElement(env);
      Element headerElement = DOMUtils.getFirstChildElement(header);
      
      String expStr = 
         "<someHeader env:mustUnderstand='1' xml:lang='en' xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'/>";
      
      String wasStr = DOMWriter.printNode(headerElement, false);
      assertEquals(expStr, wasStr);
   }
   
   public void testEntity() throws Exception
   {
      String expStr = 
         "<xsd:simpleType name='MailRelayConfiguration' xmlns:xsd='http://www.w3.org/2001/XMLSchema'>" +
          "<xsd:restriction base='xsd:string'>" +
           "<xsd:enumeration value='Incoming &amp; Outgoing'/>" +
           "<xsd:enumeration value='None'/>" +
          "</xsd:restriction>" +
         "</xsd:simpleType>";

      Element domEl = DOMUtils.parse(expStr);
      String wasStr = DOMWriter.printNode(domEl, false);
      
      assertEquals(expStr, wasStr);
   }

   // [JBWS-762] DOMUtils.parse skips peer comments on Document node
   public void testDocumentComments() throws Exception
   {
      String expStr = 
         "<?xml version='1.0' encoding='UTF-8'?>" +
         "<!-- Some root comment -->" +
         "<root>" +
          "<!-- Some element comment -->" +
          "<element>some value</element>" +
         "</root>";


      Document doc = DOMUtils.parse(expStr).getOwnerDocument();
      StringWriter strwr = new StringWriter();
      new DOMWriter(strwr, "UTF-8").print(doc);
      String wasStr = strwr.toString();
      
      assertEquals(expStr, wasStr);
   }

   public void testElementNamespaceURIElementNS() throws Exception
   {
      String xmlIn = "<Hello xmlns='http://somens'><Sub>World</Sub></Hello>";
      
      Element root = DOMUtils.createElement(new QName("http://somens", "Hello"));
      assertEquals("http://somens", root.getNamespaceURI());
      Element child = (Element)root.appendChild(DOMUtils.createElement(new QName("Sub")));
      child.appendChild(DOMUtils.createTextNode("World"));

      String xmlOut = DOMWriter.printNode(root, false);
      assertEquals(xmlIn, xmlOut);
   }

   public void testElementNamespaceURIDocumentParse() throws Exception
   {
      String xmlIn = "<Hello xmlns='http://somens'><Sub>World</Sub></Hello>";
      
      Element root = DOMUtils.parse(xmlIn);
      assertEquals("http://somens", root.getNamespaceURI());

      String xmlOut = DOMWriter.printNode(root, false);
      assertEquals(xmlIn, xmlOut);
   }

   public void testElementNamespaceURITransformer() throws Exception
   {
      String xmlIn = "<Hello xmlns='http://somens'><Sub>World</Sub></Hello>";
      StreamSource source = new StreamSource(new ByteArrayInputStream(xmlIn.getBytes()));

      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      DOMResult result = new DOMResult();
      transformer.transform(source, result);
      
      Element root = ((Document)result.getNode()).getDocumentElement();
      assertEquals("http://somens", root.getNamespaceURI());

      String xmlOut = DOMWriter.printNode(root, false);
      assertEquals(xmlIn, xmlOut);
   }
 
   public void testWhiteSpaceRemove() throws Exception
   {
      String xmlIn = "<Hello> <Sub>World</Sub> </Hello>";
      Element root = DOMUtils.parse(xmlIn);
      root.normalize();
      
      StringWriter strwr = new StringWriter();
      new DOMWriter(strwr).setIgnoreWhitespace(true).print(root);
      String xmlOut = strwr.toString();
      assertEquals("<Hello><Sub>World</Sub></Hello>", xmlOut);
   }
}
