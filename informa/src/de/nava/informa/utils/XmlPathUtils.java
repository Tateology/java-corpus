//
// Informa -- RSS Library for Java
// Copyright (c) 2002 by Niko Schmuck
//
// Niko Schmuck
// http://sourceforge.net/projects/informa
// mailto:niko_schmuck@users.sourceforge.net
//
// This library is free software.
//
// You may redistribute it and/or modify it under the terms of the GNU
// Lesser General Public License as published by the Free Software Foundation.
//
// Version 2.1 of the license should be included with this distribution in
// the file LICENSE. If the license is not included with this distribution,
// you may find a copy at the FSF web site at 'www.gnu.org' or 'www.fsf.org',
// or you may write to the Free Software Foundation, 675 Mass Ave, Cambridge,
// MA 02139 USA.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied waranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//

// $Id: XmlPathUtils.java 817 2006-12-04 23:43:29Z italobb $

package de.nava.informa.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.jdom.Element;
import org.jdom.Namespace;

/**
 * Utility class providing methods access XML attributes and elements using
 * a path.
 *
 * @author Michael Harhen
 */
public class XmlPathUtils {

  private XmlPathUtils() {}

  private static final String elementDelim = "/";
  private static final String prefixDelim = ":";

  /**
   * Returns the value of  an element's child element reached by the given path.
   * Traverses the DOM tree from the parent until the child is reached.
   *
   * @param parent the parent <code>Element</code>
   * @param childPath a path to the root of the elements.
   *        Paths are specified as element names, separated by a "/".
   *        Namespaces are allowed. e.g. "aaa:bbb/ccc:ddd/eee".
   *
   * @return the value of the child. <br>If <code>parent</code> is
   *         <code>null</code>, returns <code>null</code>. If
   *         <code>childPath</code> is null, returns the value of the parent.
   */
  public static String getElementValue(final Element parent, final String childPath) {

    if (parent == null) {
      return null;
    } else {
      Element child = getLeafChild(parent, childPath);
      return (child == null) ? null : child.getTextTrim();
    }
  }

  /**
   * Returns the values of the specified sub-elements of the child element
   * reached by the given path. This is useful in cases where a child has
   * several children. Traverses the DOM tree from the parent until the root
   * is reached, then reads the specified elements.
   *
   * @param parent the parent <code>Element</code>
   * @param childPath a path to the root of the elements.
   *        Paths are specified as element names, separated by a "/".
   *        Namespaces are allowed. e.g. "aaa:bbb/ccc:ddd/eee".
   * @param elements An array of element names. May contain namespace specifiers.
   *
   * @return an array containing the value of each element.
   *         <br>If <code>parent</code> or <code>elements</code> is
   *         <code>null</code>, returns <code>null</code>. If
   *         <code>childPath</code> is null, returns the specified sub-elements
   *         of the parent.
   */
  public static String[] getElementValues(final Element parent, final String childPath, final String[] elements) {

    if (parent == null) {
      return null;
    } else {
      Element child = getLeafChild(parent, childPath);
      return getElementValues(child, elements);
    }
  }

  /**
   * Returns the values of the specified sub-elements of the child parent element.
   * This is useful in cases where an element has several children.
   *
   * @param parent the parent <code>Element</code>
   * @param children An array of child element names. May contain namespace specifiers.
   *
   * @return an array containing the value of each child element.
   *         <br>If <code>parent</code> or <code>children</code>
   *         is <code>null</code>, returns <code>null</code>.
   */
  public static String[] getElementValues(final Element parent, final String[] children) {
    if ((parent == null) || (children == null)) {
      return null;
    } else {
      int numValues = children.length;
      String[] elementValues = new String[numValues];
      for (int i = 0; i < numValues; ++i) {
        Element child = getChild(parent, children[i]);
        elementValues[i] = (child == null) ? null : child.getTextTrim();
      }
      return elementValues;
    }
  }

  /**
   * Returns the value of the attribute of the child element reached by the
   * given path. Traverses the DOM tree from the parent until the child is
   * reached, then reads the given attribute.
   *
   * @param parent the parent <code>Element</code>
   * @param childPath a path to the root of the elements.
   *        Paths are specified as element names, separated by a "/".
   * @param attribute the attribute.
   *        May contain a namespace specifier e.g. "rdf:resource".
   *
   * @return the value of the attribute.
   *         <br>If <code>parent</code> or <code>attribute</code> is
   *         <code>null</code>, returns <code>null</code> .
   *         If <code>childPath</code> is null, returns the specified
   *         attribute of the parent.
   *
   */
  public static String getAttributeValue(final Element parent, final String childPath, final String attribute) {

    if ((parent == null) || (attribute == null)) {
      return null;
    } else {
      Element child = getLeafChild(parent, childPath);
      return getAttributeValue(child, attribute);
    }
  }

  /**
   * Returns the value of the child element reached by the given path.
   * This is useful in cases where a child has several attributes.
   * Traverses the DOM tree from the parent until the child is reached.
   *
   * @param parent the parent <code>Element</code>
   * @param childPath a path to the root of the elements. Paths are specified
   *        as element names, separated by a "/". Namespaces are allowed.
   *        e.g. "aaa:bbb/ccc:ddd/eee".
   * @param attributes - An array of element names. May contain namespace
   *        specifiers.
   *
   * @return the value of the child. <br>If <code>parent</code> or
   *         <code>attributes</code> is <code>null</code>, returns
   *         <code>null</code>. If <code>childPath</code> is null, returns
   *         the specified attributes of the parent.
   */
  public static String[] getAttributeValues(final Element parent, final String childPath, final String[] attributes) {

    if ((parent == null) || (attributes == null)) {
      return null;
    } else {
      Element child = getLeafChild(parent, childPath);
      return getAttributeValues(child, attributes);
    }
  }

  /**
   * Returns the values of the attributes of given element.
   * This is useful in cases where an element has several attributes.
   *
   * @param element the <code>Element</code>
   * @param attributes An array of attribute names.
   *        May contain namespace specifiers.
   *
   * @return an array containing the values of the element's attributes.
   *         <br>If <code>element</code> or <code>attributes</code>
   *         is <code>null</code>, returns <code>null</code> .
   */
  public static String[] getAttributeValues(final Element element, final String[] attributes) {

    if ((element == null) || (attributes == null)) {
      return null;
    } else {
      int numAttributes = attributes.length;
      String[] attributeValues = new String[numAttributes];
      for (int i = 0; i < numAttributes; ++i) {
        attributeValues[i] = getAttributeValue(element, attributes[i]);
      }
      return attributeValues;
    }
  }

  /**
   * Returns an <code>Element's</code> child corresponding to the given path.
   * Traverses the DOM tree from the parent until the child is reached.
   *
   * @param parent the parent <code>Element</code>
   * @param childPath a path to the root of the elements.
   *        Paths are specified as element names, separated by a "/".
   * @return the child.
   *         <br>If <code>childPath</code> is null, return <code>parent</code>.
   */
  private static Element getLeafChild(final Element parent, final String childPath) {

    if (childPath == null) return parent;

    List elementNames = getElementNames(childPath);
    Iterator iterator = elementNames.iterator();
    Element nextChild = parent;

    while (iterator.hasNext() && (nextChild != null)) {
      String elementName = (String) iterator.next();
      nextChild = getChild(nextChild, elementName);
    }

    return nextChild;
  }

  /**
   * Returns an <code>Element's</code> child corresponding to the given child
   * name.
   *
   * @param parent the parent <code>Element</code>
   * @param childName the child's name.
   *        May contain a namespace specifier.
   *
   * @return the child.
   */
  private static Element getChild(final Element parent, final String childName) {

    int prefixPos = childName.indexOf(prefixDelim);
    if ( (prefixPos == 0) || (prefixPos >= childName.length() - 1)) {
      return null;
    } else {
      if (prefixPos == -1) {
        return parent.getChild(childName, getNamespace(parent, null));
      } else {
        String prefix = childName.substring(0, prefixPos);
        String childElementName = childName.substring(prefixPos + 1);
        return parent.getChild(childElementName, getNamespace(parent, prefix));
      }
    }
  }

  /**
   * Get the value corresponding to an <code>Element</code> and an attribute.
   *
   * @param element the <code>Element</code>.
   *        <code>null</code> is not acceptable.
   * @param attribute the attribute.
   *        May contain a namespace specifier e.g. "rdf:resource".
   *        <code>null</code> is not acceptable.
   * @return the value. The value of the <code>Element's</code> attribute,
   *         or <code>null</code> if element is <code>null</code>.
   */
  private static String getAttributeValue(Element element, String attribute) {

    if (element == null) return null;
    int prefixPos = attribute.indexOf(prefixDelim);

    if ( (prefixPos == 0) || (prefixPos >= attribute.length() - 1)) {
      return null;
    } else if (prefixPos == -1) { // no prefix
      return element.getAttributeValue(attribute);
    } else {
      String prefix = attribute.substring(0, prefixPos);
      String attributeName = attribute.substring(prefixPos + 1);
      return element.getAttributeValue(attributeName, getNamespace(element, prefix));
    }
  }

  /**
   * Converts a path into a <code>List</code> of element names.
   *
   * @param childPath a path. e.g. "aaa:bbb/ccc:ddd/eee"
   *
   * @return a <code>List</code> of element names.
   *         e.g. "aaa:bbb", "ccc:ddd", "eee".
   */
  private static List getElementNames(final String childPath) {

    List<String> strArray = new ArrayList<String>();
    if (childPath != null) {
      StringTokenizer st = new StringTokenizer(childPath, elementDelim);
      while (st.hasMoreTokens()) {
        String token = st.nextToken();
        if (token.length() > 0) {
          strArray.add(token);
        }
      }
    }
    return strArray;
  }

  /**
   * Returns the Namespace corresponding to an element and a prefix.
   *
   * @param element the element.
   * @param prefix the prefix.
   *
   * @return the Namespace.
   */
  private static Namespace getNamespace(final Element element, final String prefix) {
    Namespace namespace = (prefix == null) ? element.getNamespace("") : element.getNamespace(prefix);
    return (namespace == null) ? Namespace.NO_NAMESPACE : namespace;
  }

}
