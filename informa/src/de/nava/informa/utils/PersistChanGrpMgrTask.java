// Informa -- RSS Library for Java
//Copyright (c) 2002 by Niko Schmuck
//
//Niko Schmuck
//http://sourceforge.net/projects/informa
//mailto:niko_schmuck@users.sourceforge.net
//
//This library is free software.
//
//You may redistribute it and/or modify it under the terms of the GNU
//Lesser General Public License as published by the Free Software Foundation.
//
//Version 2.1 of the license should be included with this distribution in
//the file LICENSE. If the license is not included with this distribution,
//you may find a copy at the FSF web site at 'www.gnu.org' or 'www.fsf.org',
//or you may write to the Free Software Foundation, 675 Mass Ave, Cambridge,
//MA 02139 USA.
//
//This library is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied waranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//Lesser General Public License for more details.
//
//$Id: PersistChanGrpMgrTask.java 817 2006-12-04 23:43:29Z italobb $

package de.nava.informa.utils;

import java.net.URL;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.nava.informa.core.ChannelBuilderIF;
import de.nava.informa.core.ChannelFormat;
import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.impl.hibernate.Channel;
import de.nava.informa.impl.hibernate.ChannelBuilder;
import de.nava.informa.impl.hibernate.Item;
import de.nava.informa.parsers.FeedParser;

/**
 * PersistChanGrpMgrTask - description...
 *  
 */
public class PersistChanGrpMgrTask extends Thread {

  private static Log logger = LogFactory.getLog(PersistChanGrpMgrTask.class);

  private PersistChanGrpMgr mgr;
  private ChannelBuilder builder;
  private ChannelBuilderIF tempBuilder;
  private Map<URL,UpdateChannelInfo> channelInfos;
  private long minChannelUpdateDelay;
  private volatile boolean running = false;

  /**
   * Construct and setup context of the PersistChanGrpMgr
   * 
   * @param mgr
   * @param minChannelUpdateDelay minimum number of millis between channel updates.
   */
  public PersistChanGrpMgrTask(PersistChanGrpMgr mgr, long minChannelUpdateDelay) {
    super("PCGrp: " + mgr.getChannelGroup().getTitle());
    this.minChannelUpdateDelay = minChannelUpdateDelay;
    this.mgr = mgr;
    builder = mgr.getBuilder();
    channelInfos = new HashMap<URL, UpdateChannelInfo>();
    tempBuilder = new de.nava.informa.impl.basic.ChannelBuilder();
  }

  /**
   * Minimum number of milliseconds between updates of channel.
   * 
   * @param minChannelUpdateDelay minimum pause between updates in milliseconds.
   */
  public void setMinChannelUpdateDelay(long minChannelUpdateDelay) {
    this.minChannelUpdateDelay = minChannelUpdateDelay;
  }

  /**
   * run - Called each iteration to process all the Channels in this Group. This will skip inactive
   * channels. -
   */
  public void run() {
    running = true;

    try {
      // We do job and sleep until someone interupts us.
      while (!isInterrupted()) {
        long startedLoop = System.currentTimeMillis();

        performUpdates();

        // Calculate time left to sleep beween updates
        long leftToSleep = minChannelUpdateDelay - (startedLoop - System.currentTimeMillis());
        logger.debug("Going to sleep for " + leftToSleep + " millis");
        if (leftToSleep > 0) Thread.sleep(leftToSleep);
      }
    } catch (InterruptedException e) {
      // Note that the catch looks like it just continues, but at the same time isInterrupted() goes
      // to true and ends the
      logger.warn("Interrupted exception within Run method");
    } catch (Exception ignoredException) // Ignore all Exceptions (assuming that they did their own
    // cleanup and we want to keep on polling.
    {
      ignoredException.printStackTrace();
      // and continue

    } finally {
      running = false;
      synchronized (this) {
        notifyAll();
      }
    }
  }

  /**
   * Returns TRUE if current thread is running.
   * 
   * @return TRUE if running.
   */
  public boolean isRunning() {
    return running;
  }

  /**
   * Interrupt the thread and return.
   * 
   * @see java.lang.Thread#interrupt()
   */
  public void interrupt() {
    interrupt(false);
  }

  /**
   * Interrupts execution of task.
   * 
   * @param wait TRUE to wait for finish of task.
   */
  public void interrupt(boolean wait) {
    super.interrupt();
    if (wait && isRunning()) {
      while (isRunning()) {
        try {
          synchronized (this) {
            wait(1000);
          }
        } catch (InterruptedException e) {
        }
      }
    }
  }

  /**
   * Perform single update cycle for current group.
   */
  public void performUpdates() {
    logger.debug("Starting channel updates loop for " + mgr.getChannelGroup().getTitle());
    mgr.notifyPolling(true);
    Iterator iter = mgr.channelIterator();

    Channel nextChan;
    while (iter.hasNext()) {
      nextChan = (Channel) iter.next();
      logger.info("processing: " + nextChan);
      
// Catch all Exceptions coming out of handleChannel and continue iterating to the next one.
      
      try {
        handleChannel(nextChan, getUpdChanInfo(nextChan));
      } catch (RuntimeException e) {
        logger.error("Error during processing: " + nextChan, e);
      } catch (NoSuchMethodError ignoreNoSuchMethod) // Ignore and continue
      {
        logger
            .error("NoSuchMethodError exception within Run method. Ignoring." + nextChan, ignoreNoSuchMethod);
      }

    }

    // Notify everyone that polling of group finished.
    mgr.notifyPolling(false);
    mgr.incrPollingCounter();
  }

  /**
   * Return (and create if necessary) an UpdateChannelInfo object, which is a parallel object which
   * we use here to keep track of information about a channel.
   * 
   * @param chan - Corresponding Channel.
   */
  private UpdateChannelInfo getUpdChanInfo(Channel chan) {
    UpdateChannelInfo info = channelInfos.get(chan.getLocation());

    if (info == null) // Create a new UpdateChannelInfo object and add it to the Map.
    {
      info = new UpdateChannelInfo(mgr.getAcceptNrErrors());
      channelInfos.put(chan.getLocation(), info);
    }
    return info;
  }

  /**
   * Process the Channel information.
   * 
   * @param chan - Channel to process
   * @param info - UpdateChannelInfo - additional Channel Info object
   */
  private void handleChannel(Channel chan, UpdateChannelInfo info) {
    if (!info.shouldDeactivate()) {
      if (shouldUpdate(info)) {
        synchronized (builder) {
          if (!info.getFormatDetected()) handleChannelHeader(chan, info);
          handleChannelItems(chan, info);
        }

        info.setLastUpdatedTimestamp(System.currentTimeMillis());
      }
    } else {
      // Returns true if more errors happened than threshold.
      logger.info("Not processing channel: " + chan + " because exceeded error threshold.");
      return;
    }
  }

  /**
   * Returns TRUE if the cannel represented by the <code>info</code> should be updated. Decision
   * is basing on the fact of last update. If there's not enough time passed since then we don't
   * need to update this channel.
   * 
   * @param info info object of the channel.
   * 
   * @return result of the check.
   */
  private boolean shouldUpdate(UpdateChannelInfo info) {
    return System.currentTimeMillis() - info.getLastUpdatedTimestamp() > minChannelUpdateDelay;
  }

  /**
   * handleChannelHeader -
   * 
   * @param chan
   * @param info -
   */
  private void handleChannelHeader(Channel chan, UpdateChannelInfo info) {
    if (!info.getFormatDetected()) { // If format has been detected then we've seen this Channel
      // already
      logger.debug("Handling Channel Header. Format not yet detected.");
      try {
        builder.beginTransaction();
        builder.reload(chan);
        ChannelFormat format = FormatDetector.getFormat(chan.getLocation());
        chan.setFormat(format);
        info.setFormatDetected(true);
        chan.setLastUpdated(new Date());
        builder.endTransaction();
      } catch (UnknownHostException e) {
        // Normal situation when user is offline
        logger.debug("Host not found: " + e.getMessage());
      } catch (Exception e) {
        info.increaseProblemsOccurred(e);
        String msg = "Exception in handleChannelHeader for : " + chan;
        logger.fatal(msg + "\n     Continue....");
      } finally {
        // If there was an exception we still will be in transaction.
        if (builder.inTransaction()) builder.resetTransaction();
      }
    }
  }

  /**
   * Process items in the newly parsed Channel. If they are new (i.e. not yet persisted) then add
   * them to the Channel. Note the logXXX variables were put in to do better error reporting in the
   * event of an Exception.
   * 
   * @param chan
   * @param info -
   */
  private void handleChannelItems(Channel chan, UpdateChannelInfo info) {
    ChannelIF tempChannel = null;
    int logHowManySearched = 0;
    int logHowManyAdded = 0;

    // TODO: [Aleksey Gureev] I don't see locking of builder here. Locking of the whole peice will
    // be very
    // great resource consumption. It's necessary to rework whole method to lock builder for a
    // minimal time.

    try {
      builder.beginTransaction();
      builder.reload(chan);
      /*
       * We will now parse the new channel's information into a *memory based* temporary channel. We
       * will then see which items that we received from the feed are already present and add the
       * new ones.
       */
      tempChannel = FeedParser.parse(tempBuilder, chan.getLocation());
      InformaUtils.copyChannelProperties(tempChannel, chan);
      /*
       * Tricky: this channel might have been loaded into memory by Hibernate in a preceding
       * Hibernate Session. We need to make it available in this session so it will be written back
       * to disk when the transaction is committed.
       */
      chan.setLastUpdated(new Date());
      mgr.notifyChannelRetrieved(chan);
      /*
       * Compare with the existing items, and only add new ones. In the future this is where we
       * would put code to diff an item to see how blog author has edited a certain item over time.
       */
      if (!tempChannel.getItems().isEmpty()) {
        Iterator it = tempChannel.getItems().iterator();
        while (it.hasNext()) {
          logHowManySearched++;
          de.nava.informa.impl.basic.Item transientItem = (de.nava.informa.impl.basic.Item) it
              .next();
          if (!chan.getItems().contains(transientItem)) {
            logger.info("Found new item: " + transientItem);
            logHowManyAdded++;
            /*
             * A persistent item is created, using all the state from the memory based item produced
             * by parser.
             */
            ItemIF newItem = builder.createItem(chan, transientItem);
            mgr.notifyItemAdded((Item) newItem);
          }
        } // while it.hasNext()
      }
      builder.endTransaction();
    } catch (UnknownHostException e) {
      // Normal situation when user is offline
      logger.debug("Host not found: " + e.getMessage());
    } catch (Exception e) {
      info.increaseProblemsOccurred(e);
      String msg = "Exception in handleChannelItems. # Potential new items = " + logHowManySearched
          + ", # Items actually added to channel: " + logHowManyAdded + "\n     Stored Chan="
          + chan + "\n     ParsedChan=" + tempChannel;
      logger.fatal(msg + "\n     Continue....");
    } finally {
      // If there was an exception we still will be in transaction.
      if (builder.inTransaction()) builder.resetTransaction();
    }
  }
}