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


// $Id: CategoryIF.java 817 2006-12-04 23:43:29Z italobb $

package de.nava.informa.core;

/**
 * This interface is implemented by objects representing categories in
 * which channels could be organised in the news channel object model.</p>
 * 
 * <p>{@link WithChildrenMIF#getChildren()} collection contains here nested 
 *    CategoryIF objects</p>
 *
 * @author Niko Schmuck (niko@nava.de)
 */
public interface CategoryIF extends WithIdMIF, WithTitleMIF, WithChildrenMIF<CategoryIF> {

  String getDomain();
  void setDomain(String domain);

  CategoryIF getParent();
  void setParent(CategoryIF parent);

  void addChild(CategoryIF child);
  void removeChild(CategoryIF child);

}
