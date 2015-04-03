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


// $Id: ChannelSearcher.java 817 2006-12-04 23:43:29Z italobb $

package de.nava.informa.search;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Hits;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemIF;  
import de.nava.informa.core.ChannelGroupIF;

/**
 * PUBLIC: Utility class for executing searches against the full-text
 * index that does allow to directly obtain the found news items.
 *
 * @author Niko Schmuck (niko@nava.de)
 */
public class ChannelSearcher {

  private static Log logger = LogFactory.getLog(ChannelSearcher.class);

  /**
   * The default maximum number of search results that should be
   * returned by the query.
   */
  public static final int DEFAULT_MAX_RESULTS = 25;
  
  private String indexDir;
  private Analyzer analyzer;
  private Searcher searcher;
  private int nrOfHits;
  
  /**
   * Constructor which allows to specify the index directory.
   * For searching the full-text index the lucene
   * {@link org.apache.lucene.analysis.standard.StandardAnalyzer}
   * is used.
   *
   * @param indexDir - The directory from which the index can be retrieved.
   */
  public ChannelSearcher(String indexDir) throws IOException {
    this.indexDir = indexDir;
    this.analyzer = new StandardAnalyzer();
    this.searcher = new IndexSearcher(indexDir);
    this.nrOfHits = 0;
  }

  /**
   * Performs a query on the full-text index using the field
   * {@link ItemFieldConstants#TITLE_AND_DESC} as the default search
   * field limited to {@link #DEFAULT_MAX_RESULTS} number of search
   * results.
   *
   * @return A List of ItemResult objects ordered by relevance.
   */
  public List<ItemResult> search(ChannelGroupIF channels, String queryString)
    throws QueryParseException, IOException {
    return search(channels, queryString, DEFAULT_MAX_RESULTS);
  }
  
  /**
   * Performs a query on the full-text index using the field
   * {@link ItemFieldConstants#TITLE_AND_DESC} as the default search
   * field.
   *
   * @return A List of ItemResult objects ordered by relevance.
   */
  public List<ItemResult> search(ChannelGroupIF channels,
                     String queryString, int maxResults)
    throws QueryParseException, IOException {

    Query query = null;
    try {
      logger.info("Searching for '" + queryString + "'.");
      query = QueryParser.parse(queryString,
                                ItemFieldConstants.TITLE_AND_DESC,
                                analyzer);
    } catch (ParseException pe) {
      // TODO: wrap into own exception for more independence of lucene
      throw new QueryParseException(pe);
    }
    Hits hits = searcher.search(query);
    nrOfHits = hits.length();
    logger.info("Query returned " + nrOfHits + " hits.");
    List<ItemResult> results = new ArrayList<ItemResult>();
    for (int i = 0; i < hits.length() && i < maxResults; i++) {
      Document doc = hits.doc(i);
      long channelId = Long.parseLong(doc.get(ItemFieldConstants.CHANNEL_ID));
      ChannelIF channel = channels.getById(channelId);
      if (channel == null) {
        throw new UnretrievableException("channel " + channelId);
      }
      // TODO: could this be done in another fashion or using a context?
      long itemId = Long.parseLong(doc.get(ItemFieldConstants.ITEM_ID));
      ItemIF item = channel.getItem(itemId);
      if (item == null) {
        throw new UnretrievableException("item " + itemId);
      }
      results.add(new ItemResult(item, hits.score(i)));
    }
    searcher.close();
    return results;
  }

  /**
   * Returns the number of news items found from the previous
   * full-text search query.
   *
   * Note: Use only directly after the search was performed, otherwise
   * the return value may be wrong.
   */
  public int getNrOfHits() {
    return nrOfHits;
  }
  
  public void setIndexDir(String indexDir) {
    this.indexDir = indexDir;
  }

  public String getIndexDir() {
    return indexDir;
  }
  
}
