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
 * Meta-, or markerinterface, specifying objects, having <strong>creator</strong>.
 * Creator is the part of Dublin Core Metadata.
 * 
 * @author <a href="mailto:alexei@matiouchkine.de">Alexei Matiouchkine</a>
 * @version $Id: WithCreatorMIF.java 807 2006-09-10 04:47:35Z nileshbansal $
 */
public interface WithCreatorMIF {

  /**
   * @return Creator of the object. Returns <code>null</code> if nothing
   * appropriate found.
   * 
   * For RSS 0.91 and 2.0 feeds: return text under managingEditor element For
   * RSS 1.0 feed: first checks if creator element exists as dublin core
   * metadata, and then falls back to managingEditor element For RSS 2.0
   * entries: checks the author element for each entry or fallback to creator
   * metadata element from dublin core
   * 
   * For Atom 0.3 and 1.0 feeds: checks author element. For Atom 1.0, multiple
   * authors are possible, in which case we return a list of authors delimited
   * by semicolon. Note that the author name itself may contain a semicolon, in
   * which case seperation of author names by splitting at every semicolon will
   * not work.
   */
  String getCreator();

  /**
   * @param creator the creator of the object to be set
   */
  void setCreator(String creator);

}
