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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// $Id: FeedManagerEntry.java 817 2006-12-04 23:43:29Z italobb $

package de.nava.informa.utils;

import de.nava.informa.core.ChannelBuilderIF;
import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ChannelUpdatePeriod;
import de.nava.informa.core.FeedIF;
import de.nava.informa.core.ParseException;
import de.nava.informa.impl.basic.Feed;
import de.nava.informa.parsers.FeedParser;

import java.io.IOException;

import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLConnection;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Holder class for feeds held in the manager. The purpose of this class is to
 * store the last time we loaded the feed, and determine if the feed needs to be
 * reread. Whilst we use the data provided by the feed where possible, if this
 * is not present defaults will be used.
 * <p>
 * Its also important to note that we do oversimply things a bit. We ignore the
 * updateBase even if specified by the feed.
 * </p>
 * 
 * @author Sam Newman
 * @see FeedManager
 */
public class FeedManagerEntry {

  public static final long MILLISECONDS_IN_HOUR = 3600000L;

  public static final long MILLISECONDS_IN_DAY = 86400000L;

  public static final long MILLISECONDS_IN_MONTH = 2419200000L;

  /** Over simplificatin here - assuming a non-leap year */
  public static final long MILLISECONDS_IN_YEAR = 31536000000L;

  /* logger handler */
  private static Log logger = LogFactory.getLog(FeedManagerEntry.class);

  private ChannelUpdatePeriod defaultUpdatePeriod;

  private int defaultUpdateFrequency;

  /**
   * Stores the number of milliseconds since the last update after which the
   * feed is out of date
   */
  private long timeToExpire;

  /** The channel we hold */
  private FeedIF feed;

  /** The last time we updated a feed */
  private long lastUpdate;

  /** The URI for the feed */
  private String feedUri;

  private ChannelBuilderIF channelBuilder;

  /** the wantedTtl for the feed * */
  private long wantedTtl = -1;

  /** stores the values necessary to make conditionnale GET * */
  private ConditionalGetValues httpHeaders = new ConditionalGetValues();

	/**
   * Creates a new FeedManagerEntry object.
   * 
   * @param feedUri
   * @param builder
   * @param defaultUpdatePeriod2
   * @param defaultUpdateFrequency
   * @throws FeedManagerException
   */
  public FeedManagerEntry(String feedUri, ChannelBuilderIF builder,
      ChannelUpdatePeriod defaultUpdatePeriod2, int defaultUpdateFrequency)
      throws FeedManagerException {
    this.feedUri = feedUri;
    this.channelBuilder = builder;
    this.defaultUpdatePeriod = defaultUpdatePeriod2;
    this.defaultUpdateFrequency = defaultUpdateFrequency;
    this.feed = retrieveFeed(feedUri);
    this.lastUpdate = System.currentTimeMillis();
  }

  public ChannelUpdatePeriod getDefaultUpdatePeriod() {
    return defaultUpdatePeriod;
  }

  public void setDefaultUpdatePeriod(ChannelUpdatePeriod defaultUpdatePeriod) {
    this.defaultUpdatePeriod = defaultUpdatePeriod;
  }

  public int getDefaultUpdateFrequency() {
    return defaultUpdateFrequency;
  }

  public void setDefaultUpdateFrequency(int defaultUpdateFrequency) {
    this.defaultUpdateFrequency = defaultUpdateFrequency;
  }
	
  /**
   * Loads the channel and sets up the time to expire
   * 
   * @param uri
   *          The location for the rss file
   * @return The Channel
   * @throws FeedManagerException
   *           If the feed specified by <code>uri</code> is invalid
   */
  private FeedIF retrieveFeed(String uri) throws FeedManagerException {
    try {
      URL urlToRetrieve = new URL(uri);

      URLConnection conn = null;
      try {
        conn = urlToRetrieve.openConnection();

        if (conn instanceof HttpURLConnection) {

          HttpURLConnection httpConn = (HttpURLConnection) conn;

          httpConn.setInstanceFollowRedirects(true); // not needed, default ?

          //	 Hack for User-Agent : problem for
          // http://www.diveintomark.org/xml/rss.xml
          HttpHeaderUtils.setUserAgent(httpConn, "Informa Java API");

          logger.debug("retr feed at url " + uri + ": ETag"
              + HttpHeaderUtils.getETagValue(httpConn) + " if-modified :"
              + HttpHeaderUtils.getLastModified(httpConn));

          // get initial values for cond. GET in updateChannel
          this.httpHeaders.setETag(HttpHeaderUtils.getETagValue(httpConn));
          this.httpHeaders.setIfModifiedSince(HttpHeaderUtils
              .getLastModified(httpConn));
        }
      } catch (java.lang.ClassCastException e) {
        conn = null;
        logger.warn("problem cast to HttpURLConnection " + uri, e);
        throw new FeedManagerException(e);
      } catch (NullPointerException e) {
        logger.error("problem NPE " + uri + " conn=" + conn, e);
        conn = null;
        throw new FeedManagerException(e);
      }

      ChannelIF channel = null;
      /*
       * if ( conn == null ) { channel = FeedParser.parse(getChannelBuilder(),
       * uri); } else {
       */
      channel = FeedParser.parse(getChannelBuilder(), conn.getInputStream());
      //}

      this.timeToExpire = getTimeToExpire(channel);
      this.feed = new Feed(channel);

      Date currDate = new Date();
      this.feed.setLastUpdated(currDate);
      this.feed.setDateFound(currDate);
      this.feed.setLocation(urlToRetrieve);
      logger.info("feed retrieved " + uri);

    } catch (IOException e) {
      logger.error("IOException " + feedUri + " e=" + e);
      e.printStackTrace();
      throw new FeedManagerException(e);
    } catch (ParseException e) {
      e.printStackTrace();
      throw new FeedManagerException(e);
    }

    return this.feed;
  }

  /**
   * Updates the channel associated with this feed use conditional get stuff.
   * http://fishbowl.pastiche.org/2002/10/21/http_conditional_get_for_rss_hackers
   * 
   * @throws FeedManagerException
   */
  private synchronized void updateChannel() throws FeedManagerException {
    try {
      String feedUrl = this.feed.getLocation().toString();

      URL aURL = null;
      try {
        aURL = new URL(feedUrl);
      } catch (java.net.MalformedURLException e) {
        logger.error("Could not create URL for " + feedUrl);
      }

      URLConnection conn = null;
      try {
        conn = aURL.openConnection();

        if (conn instanceof HttpURLConnection) {

          HttpURLConnection httpConn = (HttpURLConnection) conn;

          httpConn.setInstanceFollowRedirects(true);
          //	 Hack for User-Agent : problem for
          // http://www.diveintomark.org/xml/rss.xml
          HttpHeaderUtils.setUserAgent(httpConn, "Informa Java API");
          HttpHeaderUtils.setETagValue(httpConn, this.httpHeaders.getETag());
          HttpHeaderUtils.setIfModifiedSince(httpConn, this.httpHeaders
              .getIfModifiedSince());
          httpConn.connect();
          if (httpConn.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {

            logger.info("cond. GET for feed at url " + feedUrl + ": no change");
            this.feed.setLastUpdated(new Date());
            // TODO : add a property in FeedIF interface for lastGet ?
            this.lastUpdate = System.currentTimeMillis();
            return;
          }
          logger.info("cond. GET for feed at url " + feedUrl + ": changed");
          logger.debug("feed at url " + feedUrl + " new values : ETag"
              + HttpHeaderUtils.getETagValue(httpConn) + " if-modified :"
              + HttpHeaderUtils.getLastModified(httpConn));

          this.httpHeaders.setETag(HttpHeaderUtils.getETagValue(httpConn));
          this.httpHeaders.setIfModifiedSince(HttpHeaderUtils
              .getLastModified(httpConn));
        }

      } catch (java.lang.ClassCastException e) {
        logger.warn("problem cast to HttpURLConnection (reading from a file?) "
            + feedUrl, e);
      }

      ChannelIF channel = null;
      if (conn == null) {
        channel = FeedParser.parse(getChannelBuilder(), feedUrl);
      } else {
        channel = FeedParser.parse(getChannelBuilder(), conn.getInputStream());
      }

      this.feed.setChannel(channel);
      this.feed.setLastUpdated(new Date());
      this.lastUpdate = System.currentTimeMillis();
      logger.info("feed updated " + feedUrl);
    } catch (IOException e) {
      throw new FeedManagerException(e);
    } catch (ParseException e) {
      throw new FeedManagerException(e);
    }
  }

  /**
   * Checks to see if the feed is out of date - if it is the feed is reloaded
   * from the URI, otherwise the cached version is returned.
   * 
   * @return The up todate feed
   * @throws FeedManagerException
   */
  public FeedIF getFeed() throws FeedManagerException {
    if (isOutOfDate()) {
      updateChannel();
    }
    return this.feed;
  }

  public void setWantedTtl(long ms) {
    this.wantedTtl = ms;
    //recalculate the timeToExpire
    this.timeToExpire = this.getTimeToExpire(this.feed.getChannel());
  }

  /**
   * Based on the update period and update frequceny and on the optional
   * wantedTtl for the feed, calculate how many milliseconds after the
   * <code>lastUpdate</code> before this feed is considered out of date
   * 
   * @param channel
   * @return The number of milliseconds before we can consider the feed invalid
   * @throws IllegalArgumentException
   */
  private long getTimeToExpire(ChannelIF channel) {
    long temp = (new CacheSettings()).getTtl(channel, this.wantedTtl);
    return temp;
  }

  /**
   * Determines if the feed is out of date.
   * 
   * @return false if the feed is up to date, else true
   */
  private boolean isOutOfDate() {
    boolean outOfDate = false;
    logger.info(this + " isOutOfDate " + this.feedUri + "lupdt: " + lastUpdate
        + ",tte=" + timeToExpire + "<?"
        + (System.currentTimeMillis() - lastUpdate));
    if ((lastUpdate + timeToExpire) < System.currentTimeMillis()) {
      outOfDate = true;
    }
    return outOfDate;
  }

  private ChannelBuilderIF getChannelBuilder() {
    return channelBuilder;
  }

}