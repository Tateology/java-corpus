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


// $Id: ItemDocument.java 214 2003-06-25 22:52:42Z niko_schmuck $

package de.nava.informa.search;

import de.nava.informa.core.ItemIF;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.DateField;

/**
 * A utility class for making a Lucene Document from a news Item
 * object.
 *
 * @author Niko Schmuck (niko@nava.de)
 */
public class ItemDocument implements ItemFieldConstants {

  private ItemDocument() {
  }

  /**
   * Makes a document for a ItemIF object.
   * <p>
   * The document has five fields:
   * <ul>
   *   <li><code>title</code>--containing the title of the item,
   *       as a stored, tokenized field;
   *   <li><code>description</code>--containing the description of the
   *       item, as a stored, tokenized field;
   *   <li><code>titledesc</code>--containing the combination of the
   *       title and the description of the item, as a stored, tokenized
   *       field;
   *   <li><code>found</code>--containing the last modified date of
   *       the item as a keyword field as encoded by DateField;
   *   <li><code>id</code>--containing the identifier of
   *       the item as a unindexed field (for later retrieval).
   * </ul>
   */
  public static Document makeDocument(ItemIF item) {
    Document doc = new Document();
    doc.add(Field.Text(TITLE, item.getTitle()));
    doc.add(Field.Text(DESCRIPTION, item.getDescription()));
    doc.add(Field.Text(TITLE_AND_DESC, item.getTitle() + " " +
                       item.getDescription()));
    // Keyword field is not tokenized for not breaking the date
    if (item.getFound() != null) {
      doc.add(Field.Keyword(DATE_FOUND,
                            DateField.dateToString(item.getFound())));
    }
    // Unindexed field for later retrieval of the item
    doc.add(Field.UnIndexed(ITEM_ID, Long.toString(item.getId())));
    // Unindexed field for later retrieval of the channel
    if (item.getChannel() != null) {
      doc.add(Field.UnIndexed(CHANNEL_ID,
                              Long.toString(item.getChannel().getId())));
    }
    return doc;
  }
  
}
