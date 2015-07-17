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
package org.jboss.util.xml;

// $Id$

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Traverse a DOM tree in order to print a document that is parsed.
 *
 * @author Andy Clark, IBM
 * @author Thomas.Diesler@jboss.org
 */
@SuppressWarnings("unchecked")
public class DOMWriter
{
   // Print writer
   private PrintWriter out;
   // True, if canonical output
   private boolean canonical;
   // True, if pretty printing should be used
   private boolean prettyprint;
   // True, if the XML declaration should be written
   private boolean writeXMLDeclaration;
   // True, if whitespace should be ignored
   private boolean ignoreWhitespace;
   // Explicit character set encoding
   private String charsetName;
   // indent for the pretty printer
   private int prettyIndent;
   // True, if the XML declaration has been written
   private boolean wroteXMLDeclaration;
   // The node that started the write
   private Node rootNode;
   // True if we want namespace completion
   private boolean completeNamespaces = true;
   // The current default namespace
   private String currentDefaultNamespace;

   public DOMWriter(Writer w)
   {
      this.out = new PrintWriter(w);
   }

   public DOMWriter(Writer w, String charsetName)
   {
      this.out = new PrintWriter(w);
      this.charsetName = charsetName;
      this.writeXMLDeclaration = true;
   }

   public DOMWriter(OutputStream stream)
   {
      try
      {
         this.out = new PrintWriter(new OutputStreamWriter(stream, "UTF-8"));
      }
      catch (UnsupportedEncodingException e)
      {
         // ignore, UTF-8 should be available
      }
   }

   public DOMWriter(OutputStream stream, String charsetName)
   {
      try
      {
         this.out = new PrintWriter(new OutputStreamWriter(stream, charsetName));
         this.charsetName = charsetName;
         this.writeXMLDeclaration = true;
      }
      catch (UnsupportedEncodingException e)
      {
         throw new IllegalArgumentException("Unsupported encoding: " + charsetName);
      }
   }

   /** 
    * Print a node with explicit prettyprinting.
    * The defaults for all other DOMWriter properties apply. 
    *  
    */
   public static String printNode(Node node, boolean prettyprint)
   {
      StringWriter strw = new StringWriter();
      new DOMWriter(strw).setPrettyprint(prettyprint).print(node);
      return strw.toString();
   }

   public boolean isCanonical()
   {
      return canonical;
   }

   /** 
    * Set wheter entities should appear in their canonical form.
    * The default is false.
    */
   public DOMWriter setCanonical(boolean canonical)
   {
      this.canonical = canonical;
      return this;
   }

   public boolean isIgnoreWhitespace()
   {
      return ignoreWhitespace;
   }

   /**
    * Set whether whitespace should be ignored.
    * The default is false.
    */
   public DOMWriter setIgnoreWhitespace(boolean ignoreWhitespace)
   {
      this.ignoreWhitespace = ignoreWhitespace;
      return this;
   }
   
   /**
    * Set wheter subelements should have their namespaces completed.
    * Setting this to false may lead to invalid XML fragments.
    * The default is true.
    */
   public DOMWriter setCompleteNamespaces(boolean complete)
   {
      this.completeNamespaces = complete;
      return this;
   }

   public boolean isPrettyprint()
   {
      return prettyprint;
   }

   /** 
    * Set wheter element should be indented.
    * The default is false.
    */
   public DOMWriter setPrettyprint(boolean prettyprint)
   {
      this.prettyprint = prettyprint;
      return this;
   }

   public boolean isWriteXMLDeclaration()
   {
      return writeXMLDeclaration;
   }

   /** 
    * Set wheter the XML declaration should be written.
    * The default is false.
    */
   public DOMWriter setWriteXMLDeclaration(boolean flag)
   {
      this.writeXMLDeclaration = flag;
      return this;
   }

   public void print(Node node)
   {
      if (prettyprint && ignoreWhitespace)
         throw new IllegalStateException("Cannot pretty print and ignore whitespace");
      
      rootNode = node;
      printInternal(node, false);
   }

   private void printInternal(Node node, boolean indentEndMarker)
   {
      // is there anything to do?
      if (node == null)
      {
         return;
      }

      // JBAS-2117 - Don't skip the DOCUMENT_NODE
      // if (node instanceof Document) node = ((Document)node).getDocumentElement();

      if (wroteXMLDeclaration == false && writeXMLDeclaration == true && canonical == false)
      {
         out.print("<?xml version='1.0'");
         if (charsetName != null)
            out.print(" encoding='" + charsetName + "'");

         out.print("?>");
         if (prettyprint)
            out.println();

         wroteXMLDeclaration = true;
      }

      int type = node.getNodeType();
      boolean hasChildNodes = node.getChildNodes().getLength() > 0;

      String nodeName = node.getNodeName();
      switch (type)
      {
         // print document
         case Node.DOCUMENT_NODE:
         {
            NodeList children = node.getChildNodes();
            for (int iChild = 0; iChild < children.getLength(); iChild++)
            {
               printInternal(children.item(iChild), false);
            }
            out.flush();
            break;
         }

            // print element with attributes
         case Node.ELEMENT_NODE:
         {
            Element element = (Element)node;
            if (prettyprint)
            {
               for (int i = 0; i < prettyIndent; i++)
               {
                  out.print(' ');
               }
               prettyIndent++;
            }

            out.print('<');
            out.print(nodeName);

            Map nsMap = new HashMap();
            String elPrefix = node.getPrefix();
            String elNamespaceURI = node.getNamespaceURI();
            if (elPrefix != null)
            {
               String nsURI = getNamespaceURI(elPrefix, element, rootNode);
               nsMap.put(elPrefix, nsURI);
            }

            Attr attrs[] = sortAttributes(node.getAttributes());
            for (int i = 0; i < attrs.length; i++)
            {
               Attr attr = attrs[i];
               String atPrefix = attr.getPrefix();
               String atName = attr.getNodeName();
               String atValue = normalize(attr.getNodeValue(), canonical);

               if (atName.equals("xmlns"))
                  currentDefaultNamespace = atValue;

               if (atPrefix != null && !atPrefix.equals("xmlns") && !atPrefix.equals("xml"))
               {
                  String nsURI = getNamespaceURI(atPrefix, element, rootNode);
                  nsMap.put(atPrefix, nsURI);
                  // xsi:type='ns1:SubType', xsi:type='xsd:string'
                  if (atName.equals(atPrefix + ":type") && atValue.indexOf(":") > 0)
                  {
                     // xsi defined on the envelope
                     if (nsURI == null)
                        nsURI = getNamespaceURI(atPrefix, element, null);

                     if ("http://www.w3.org/2001/XMLSchema-instance".equals(nsURI))
                     {
                        String typePrefix = atValue.substring(0, atValue.indexOf(":"));
                        String typeURI = getNamespaceURI(typePrefix, element, rootNode);
                        nsMap.put(typePrefix, typeURI);
                     }
                  }
               }

               out.print(" " + atName + "='" + atValue + "'");
            }

            // Add namespace declaration for prefixes 
            // that are defined further up the tree
            if (completeNamespaces)
            {
               Iterator itPrefix = nsMap.keySet().iterator();
               while (itPrefix.hasNext())
               {
                  String prefix = (String)itPrefix.next();
                  String nsURI = (String)nsMap.get(prefix);
                  if (nsURI == null)
                  {
                     nsURI = getNamespaceURI(prefix, element, null);
                     out.print(" xmlns:" + prefix + "='" + nsURI + "'");
                  }
               }
            }

            // The SAX ContentHandler will by default not add the namespace declaration 
            // <Hello xmlns='http://somens'>World</Hello>
            if (elPrefix == null && elNamespaceURI != null)
            {
               String defaultNamespace = element.getAttribute("xmlns");
               if (defaultNamespace.length() == 0 && !elNamespaceURI.equals(currentDefaultNamespace))
               {
                  out.print(" xmlns='" + elNamespaceURI + "'");
                  currentDefaultNamespace = elNamespaceURI;
               }
            }

            if (hasChildNodes)
            {
               out.print('>');
            }

            // Find out if the end marker is indented
            indentEndMarker = isEndMarkerIndented(node);

            if (indentEndMarker)
            {
               out.print('\n');
            }

            NodeList childNodes = node.getChildNodes();
            int len = childNodes.getLength();
            for (int i = 0; i < len; i++)
            {
               Node childNode = childNodes.item(i);
               printInternal(childNode, false);
            }
            break;
         }

            // handle entity reference nodes
         case Node.ENTITY_REFERENCE_NODE:
         {
            if (canonical)
            {
               NodeList children = node.getChildNodes();
               if (children != null)
               {
                  int len = children.getLength();
                  for (int i = 0; i < len; i++)
                  {
                     printInternal(children.item(i), false);
                  }
               }
            }
            else
            {
               out.print('&');
               out.print(nodeName);
               out.print(';');
            }
            break;
         }

            // print cdata sections
         case Node.CDATA_SECTION_NODE:
         {
            if (canonical)
            {
               out.print(normalize(node.getNodeValue(), canonical));
            }
            else
            {
               out.print("<![CDATA[");
               out.print(node.getNodeValue());
               out.print("]]>");
            }
            break;
         }

            // print text
         case Node.TEXT_NODE:
         {
            String text = normalize(node.getNodeValue(), canonical);
            if (text.trim().length() > 0)
            {
               out.print(text);
            }
            else if (prettyprint == false && ignoreWhitespace == false)
            {
               out.print(text);
            }
            break;
         }

            // print processing instruction
         case Node.PROCESSING_INSTRUCTION_NODE:
         {
            out.print("<?");
            out.print(nodeName);
            String data = node.getNodeValue();
            if (data != null && data.length() > 0)
            {
               out.print(' ');
               out.print(data);
            }
            out.print("?>");
            break;
         }

            // print comment
         case Node.COMMENT_NODE:
         {
            for (int i = 0; i < prettyIndent; i++)
            {
               out.print(' ');
            }

            out.print("<!--");
            String data = node.getNodeValue();
            if (data != null)
            {
               out.print(data);
            }
            out.print("-->");

            if (prettyprint)
            {
               out.print('\n');
            }

            break;
         }
      }

      if (type == Node.ELEMENT_NODE)
      {
         if (prettyprint)
            prettyIndent--;

         if (hasChildNodes == false)
         {
            out.print("/>");
         }
         else
         {
            if (indentEndMarker)
            {
               for (int i = 0; i < prettyIndent; i++)
               {
                  out.print(' ');
               }
            }

            out.print("</");
            out.print(nodeName);
            out.print('>');
         }

         if (prettyIndent > 0)
         {
            out.print('\n');
         }
      }
      out.flush();
   }

   private String getNamespaceURI(String prefix, Element element, Node stopNode)
   {
      Node parent = element.getParentNode();
      String nsURI = element.getAttribute("xmlns:" + prefix);
      if (nsURI.length() == 0 && element != stopNode && parent instanceof Element)
         return getNamespaceURI(prefix, (Element)parent, stopNode);

      return (nsURI.length() > 0 ? nsURI : null);
   }

   private boolean isEndMarkerIndented(Node node)
   {
      if (prettyprint)
      {
         NodeList childNodes = node.getChildNodes();
         int len = childNodes.getLength();
         for (int i = 0; i < len; i++)
         {
            Node children = childNodes.item(i);
            if (children.getNodeType() == Node.ELEMENT_NODE)
            {
               return true;
            }
         }
      }
      return false;
   }

   /** Returns a sorted list of attributes. */
   private Attr[] sortAttributes(NamedNodeMap attrs)
   {

      int len = (attrs != null) ? attrs.getLength() : 0;
      Attr array[] = new Attr[len];
      for (int i = 0; i < len; i++)
      {
         array[i] = (Attr)attrs.item(i);
      }
      for (int i = 0; i < len - 1; i++)
      {
         String name = array[i].getNodeName();
         int index = i;
         for (int j = i + 1; j < len; j++)
         {
            String curName = array[j].getNodeName();
            if (curName.compareTo(name) < 0)
            {
               name = curName;
               index = j;
            }
         }
         if (index != i)
         {
            Attr temp = array[i];
            array[i] = array[index];
            array[index] = temp;
         }
      }
      return (array);
   }

   /** Normalizes the given string. */
   public static String normalize(String s, boolean canonical)
   {
      StringBuffer str = new StringBuffer();

      int len = (s != null) ? s.length() : 0;
      for (int i = 0; i < len; i++)
      {
         char ch = s.charAt(i);
         switch (ch)
         {
            case '<':
            {
               str.append("&lt;");
               break;
            }
            case '>':
            {
               str.append("&gt;");
               break;
            }
            case '&':
            {
               str.append("&amp;");
               break;
            }
            case '"':
            {
               str.append("&quot;");
               break;
            }
            case '\'':
            {
               str.append("&apos;");
               break;
            }
            case '\r':
            case '\n':
            {
               if (canonical)
               {
                  str.append("&#");
                  str.append(Integer.toString(ch));
                  str.append(';');
                  break;
               }
               // else, default append char
            }
            default:
            {
               str.append(ch);
            }
         }
      }
      return (str.toString());
   }
}
