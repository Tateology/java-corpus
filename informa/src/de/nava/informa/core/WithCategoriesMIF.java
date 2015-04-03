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

import java.util.Collection;

/**
 *  Meta-, or markerinterface, specifying objects, having <strong>categories 
 *      collection</strong> behind.
 *
 *  @author <a href="mailto:alexei@matiouchkine.de">Alexei Matiouchkine</a>
 *  @version $Id: WithCategoriesMIF.java 817 2006-12-04 23:43:29Z italobb $
 */
public interface WithCategoriesMIF {
  
	/**
   * Gets the assigned category objects for this channel.
   *
   * @return A collection of CategoryIF objects, in the case of no
   * assigned categories an empty collection is returned..
   */
  Collection getCategories();

  void setCategories(Collection<CategoryIF> categories);

  void addCategory(CategoryIF category);

  void removeCategory(CategoryIF category);
  
}
