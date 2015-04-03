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
// $Id: PollerWorkerThread.java 817 2006-12-04 23:43:29Z italobb $
//

package de.nava.informa.utils.poller;

import de.nava.informa.core.ChannelBuilderIF;
import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.core.ParseException;
import de.nava.informa.core.UnsupportedFormatException;
import de.nava.informa.impl.basic.ChannelBuilder;
import de.nava.informa.parsers.FeedParser;
import de.nava.informa.utils.FormatDetector;
import de.nava.informa.utils.HttpHeaderUtils;
import de.nava.informa.utils.InformaUtils;
import de.nava.informa.utils.toolkit.ChannelRecord;
import de.nava.informa.utils.toolkit.WorkerThread;
import org.xml.sax.InputSource;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.EOFException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;

/**
 * Worker thread is the main processing unit. Worker thread is not dedicated to single channel,
 * group or whatever else. It represents a single thread, which is capable of doing well-defined
 * and unified job. <code>WorkersManager</code> submits jobs for free <code>WorkerThread</code>'s.
 * Workers simply do their jobs. This architecture allows to scale to as much threads as necessary.
 *
 * @author Aleksey Gureev (spyromus@noizeramp.com)
 */
public class PollerWorkerThread extends WorkerThread {
  private static final ChannelBuilderIF BUILDER = new ChannelBuilder();

  /** Scanning for new items in channel finishes when existing item detected. */
  public static final int POLICY_SKIP_AFTER_EXISTING = 1;
  
  /** Scanning for new items performed fully. */
  public static final int POLICY_SCAN_ALL            = 2;

  private static int        seq = 1;

  private PollerObserverIF  observer;
  private PollerApproverIF  approver;
  
  private int               itemScanningPolicy;

  private String            userAgent;

  private InputSourceProviderIF inputSourceProvider;
  private InputStreamProviderIF inputStreamProvider;

  /**
   * Creates worker thread for poller with given observer and approver.
   *
   * @param observer            observer object.
   * @param approver            approver object.
   * @param itemScanningPolicy  policy for item scanning.
   * @param inputSourceProvider provider of <code>InputSource</code> for feeds' streams.
   * @param inputStreamProvider provider of <code>InputStream</code> for feeds.
   */
  public PollerWorkerThread(PollerObserverIF observer, PollerApproverIF approver,
                            int itemScanningPolicy, InputSourceProviderIF inputSourceProvider,
                            InputStreamProviderIF inputStreamProvider) {
    super("Poller " + (seq++));
    
    this.observer = observer;
    this.approver = approver;
    this.itemScanningPolicy = itemScanningPolicy;
    this.inputSourceProvider = inputSourceProvider;
    this.inputStreamProvider = inputStreamProvider;
  }

  /**
   * Sets specific user-agent to use when using HTTP protocol.
   *
   * @param userAgent user agent or NULL to use default.
   */
  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  /**
   * Processes record.
   *
   * @param record record to process.
   */
  protected final void processRecord(ChannelRecord record) {
    final ChannelIF channel = record.getChannel();

    // Notify observer
    observer.pollStarted(channel);

    try {
      // Resolve format of channel it it isn't resolved yet.
      if (!record.isFormatResolved()) {
        resolveFormat(record);
      }

      // Read the contents and check for possible new items.
      checkContents(record);

      // Notify observer
      observer.pollFinished(channel);

    } catch (Exception e) {
      observer.channelErrored(channel, e);
    }
  }

  /**
   * Resolve channel format.
   *
   * @param record channel record to process.
   * @throws IOException
   * @throws UnsupportedFormatException
   */
  private void resolveFormat(ChannelRecord record)
    throws IOException, UnsupportedFormatException {
    // Resolve channel format.
    final ChannelIF channel = record.getChannel();

    InputStream in = getInputStream(channel, "Detecting format");
    if (in != null) {
      try {
        channel.setFormat(FormatDetector.getFormat(in));
        record.setFormatResolved(true);
      } catch (EOFException e) {
        // It can happen if the file stream is empty (from the start or as result of
        // ifModifiedSince server check) -- not an error in our case.
      }
    }
  }

  /**
   * Returns input stream for URL. If connection, opened for the URL is using HTTP protocol
   * then make additional effort to install user-agent (if any set).
   *
   * @param channel   to get input stream for.
   * @param activity  name of activity.
   *
   * @return input stream.
   *
   * @throws IOException if connection cannot be opened.
   */
  private InputStream getInputStream(ChannelIF channel, String activity) throws IOException {
    return inputStreamProvider != null
      ? inputStreamProvider.getInputStreamFor(channel, activity)
      : getInputStream(channel.getLocation());
  }
  
  /**
   * Returns input stream for URL. If connection, opened for the URL is using HTTP protocol
   * then make additional effort to install user-agent (if any set).
   *
   * @param location  location to get stream for.
   *
   * @return input stream.
   *
   * @throws IOException if connection cannot be opened.
   */
  private InputStream getInputStream(URL location) throws IOException {
    
    if (location == null) return null;
    
    URLConnection connection = location.openConnection();
    if (userAgent != null && connection instanceof HttpURLConnection) {
      HttpHeaderUtils.setUserAgent((HttpURLConnection)connection, userAgent);
    }
    return new BufferedInputStream(connection.getInputStream());
  }

  /**
   * Check for item updates.
   *
   * @param record channel record to process.
   * @throws IOException
   * @throws ParseException
   */
  private void checkContents(ChannelRecord record)
    throws IOException, ParseException {
    // Read channel from URL.
    final ChannelIF channel = record.getChannel();
    URL baseUrl = channel.getLocation();

    InputStream in = getInputStream(channel, "Fetching");
    if (in != null) {
      try {
        ChannelIF tempChannel = FeedParser.parse(BUILDER, createInputSource(in), baseUrl);

        // Copy channel information from newly retreived instance.
        if (!record.isCanceled() && channelHasChanged(channel, tempChannel)) {
          InformaUtils.copyChannelProperties(tempChannel, channel);
          observer.channelChanged(channel);
        }

        // Walk through the items list and check if we have newcomers.
        if (!record.isCanceled()) checkItems(tempChannel, record);
      } catch (EOFException e) {
        // It can happen if the file stream is empty (from the start or as result of
        // ifModifiedSince server check) -- not an error in our case.
      }
    }
  }

  /**
   * Create <code>InputSource</code> object from stream which will be
   * used in parsing.
   *
   * @param feedInputStream stream.
   *
   * @return input source.
   *
   * @throws IOException in case of errors.
   */
  private InputSource createInputSource(InputStream feedInputStream)
    throws IOException {

    return inputSourceProvider == null
      ? new InputSource(feedInputStream)
      : inputSourceProvider.getInputSourceFor(feedInputStream);
  }

  /**
   * Walks through the items and checks if new items present.
   *
   * @param newChannel      new channel taken from web.
   * @param record          record about currently existing channel to match against.
   */
  final void checkItems(ChannelIF newChannel, ChannelRecord record) {
    ChannelIF existingChannel = record.getChannel();
    Collection<ItemIF> items = newChannel.getItems();
    final ItemIF[] newItems = (ItemIF[]) items.toArray(new ItemIF[items.size()]);
    final Collection currentItems = existingChannel.getItems();

    boolean finish = false;
    for (int i = 0; !record.isCanceled() && !finish && i < newItems.length; i++) {
      final ItemIF newItem = newItems[i];

      // If current list of item has no this item and approver allows to add item then proceed.
      if (!record.isCanceled() && !currentItems.contains(newItem)) {
        doAdditionIfApproved(newItem, existingChannel);
      } else if (itemScanningPolicy == POLICY_SKIP_AFTER_EXISTING) {
        finish = true;
      }
    }
  }

  private void doAdditionIfApproved(final ItemIF newItem, ChannelIF existingChannel) {
    boolean approved = false;

    try {
      approved = approver.canAddItem(newItem, existingChannel);
    } catch (Exception e) {
      // For the case when approver fails.
    }
        
    if (approved) {
      // Notify observer
      try {
        observer.itemFound(newItem, existingChannel);
      } catch (RuntimeException e) {
        // For the case when observer fails.
      }
    }
  }

  /**
   * Checks if channel has changed.
   *
   * @param o old channel.
   * @param n new channel.
   * @return result of the check.
   */
  static boolean channelHasChanged(ChannelIF o, ChannelIF n) {
    // Note that this list has no LastUpdated and LastBuildDate. We don't need to compare them.
    // LastUpdated changes each time we create new channel.
    // LastBuildDate is generated by the server-side software during build operation
    // each time we ask for a channel.

    return o == null
      || differ(o.getTitle(), n.getTitle())
      || differ(o.getDescription(), n.getDescription())
      || differ(o.getSite(), n.getSite())
      || differ(o.getCreator(), n.getCreator())
      || differ(o.getCopyright(), n.getCopyright())
      || differ(o.getPublisher(), n.getPublisher())
      || differ(o.getLanguage(), n.getLanguage())
      || differ(o.getRating(), n.getRating())
      || differ(o.getGenerator(), n.getGenerator())
      || differ(o.getDocs(), n.getDocs())
      || (o.getTtl() != n.getTtl())
      || differ(o.getUpdateBase(), n.getUpdateBase())
      || (o.getUpdateFrequency() != n.getUpdateFrequency())
      || (o.getUpdatePeriod() != n.getUpdatePeriod())
      || differ(o.getPubDate(), n.getPubDate())
      || differ(o.getFormat(), n.getFormat());
  }

  /**
   * Checks if two objects differ. NULL isn't different from other NULL.
   *
   * @param a object.
   * @param b object.
   * @return result of the check.
   */
  static boolean differ(Object a, Object b) {
    return !((a == null && b == null)
      || (a instanceof URL && b instanceof URL && (a.toString().equals(b.toString())))
      || (a != null && a.equals(b)));
  }
}
