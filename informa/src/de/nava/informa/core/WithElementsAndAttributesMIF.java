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

package de.nava.informa.core;

/**
 * Meta-, or markerinterface, specifying objects, having <strong>nested 
 * elements &amp; attributes</strong>.
 *
 * @author <a href="mailto:alexei@matiouchkine.de">Alexei Matiouchkine</a>
 * @version $Id: WithElementsAndAttributesMIF.java 779 2005-09-27 22:17:06Z niko_schmuck $
 */
public interface WithElementsAndAttributesMIF {
  
	/**
   * Returns the value of the Channel's child element reached by the given path.
   * <br>For example, getElementValue("image/title") will return the title of the Channel's image element.
   *
   * @param path a path to the child. Paths are specified as element names, separated by a "/".
   * Namespaces are allowed. e.g. "aaa:bbb/ccc:ddd/eee".
   *
   * @return the value of the child.
   * Returns <code>null</code> if path is <code>null</code>.
   */
  String getElementValue(final String path);

  /**
   * Returns the values of the specified sub-elements of the Channel's child reached by the given path.
   * This is useful in cases where a child has several children.
   * <br>For example,
   *   getElementValues("image", new String[] {"title", "url", "link", "width", "height", "description"} )
   * will return the specified sub-elements of the "image" element.
   *
   * @param path a path to the child. Paths are specified as element names, separated by a "/".
   * Namespaces are allowed. e.g. "aaa:bbb/ccc:ddd/eee".
   * @param elements An array of element names. May contain namespace specifiers.
   *
   * @return an array containing the value of each element.
   * <br>If <code>path</code> is <code>null</code>, returns the specified sub-elements for the Channel.
   * If <code>elements</code> is <code>null</code>, returns <code>null</code>.
   */
  String[] getElementValues(final String path, final String[] elements);

  /**
   * Returns the value of the attribute of the Channel's child element reached by the given path.
   * <br>For example, getAttributeValue("admin:generatorAgent", "rdf:resource")
   * will return the "rdf:resource" attribute of the Channel's "admin:generatorAgent" element.
   *
   * @param path a path to the root of the elements. Paths are specified as element names, separated by a "/".
   * @param attribute the attribute.  May contain a namespace specifier e.g. "rdf:resource".
   *
   * @return the value of the attribute.
   * <br>If <code>path</code> is <code>null</code>, returns the specified attribute for the Channel.
   * If <code>attribute</code> is <code>null</code>, returns <code>null</code>.
   *
   */
  String getAttributeValue(final String path, final String attribute);

  /**
   * Returns the values of the specified attributes of the Channel's child element reached by the given path.
   * This is useful in cases where a child has several attributes.
   *
   * @param path a path to the child. Paths are specified as element names, separated by a "/".
   * Namespaces are allowed. e.g. "aaa:bbb/ccc:ddd/eee".
   * @param attributes An array of element names. May contain namespace specifiers.
   *
   * @return an array containing the value of each attribute.
   * <br>If <code>path</code> is <code>null</code>, returns the specified attributes for the Channel.
   * If <code>attributes</code> is <code>null</code>, returns <code>null</code>.
   */
  String[] getAttributeValues(final String path, final String[] attributes);
  
}
