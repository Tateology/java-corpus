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

// $Id: PersistChanGrpMgr.java 817 2006-12-04 23:43:29Z italobb $

package de.nava.informa.utils;

import java.util.*;

import org.hibernate.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.nava.informa.core.ChannelBuilderException;
import de.nava.informa.impl.hibernate.*;

/**
 * PersistChanGrpMgr - Controls and Manages a single Hibernate based Informa 
 * ChannelGroup. Provides for threaded Updating of the Channel Group, 
 * persistence management, session management etc.
 *
 * N O T   T H R E A D S A F E
 *
 */
public class PersistChanGrpMgr
{

  private static final int DEFAULT_STARTDELAY = 1 * 1000; // ms
  private static final int DEFAULT_PERIOD = 10 * 60 * 1000; // ms
  private static final int DEFAULT_ACCEPTERRORS = 10;

  private static final int DBG_DEFAULT_STARTDELAY = 100; // ms
  private static final int DBG_DEFAULT_PERIOD = 20000; // ms
  private static final int DBG_DEFAULT_ACCEPTERRORS = 10;

  private static Log logger = LogFactory.getLog(PersistChanGrpMgr.class);
  private ChannelBuilder builder;
  private ChannelGroup group;
  private SessionHandler handler;
  private PersistChanGrpMgrObserverIF globalChannelObserver;
  private boolean activated = false;
  private PersistChanGrpMgrTask task;
  private int pollingCounter;

  int taskStartDelay;
  int taskPeriod;
  int acceptNrErrors;

  /**
   * Constructor.
   *
   * @param handler - SessionHandler to use. This needs to have been built by caller.
   *
   * @param debug - - true will run this in debug mode, which basically means that threads are run
   *        with no delays thereby revealing threading bugs.
   */
  public PersistChanGrpMgr(SessionHandler handler, boolean debug)
  {
    if (handler == null) throw new IllegalStateException("Invalid handler");
    this.handler = handler;
    builder = new ChannelBuilder(handler);
    pollingCounter = 0;

    if (debug)
    {
      taskStartDelay = DBG_DEFAULT_STARTDELAY;
      taskPeriod = DBG_DEFAULT_PERIOD;
      acceptNrErrors = DBG_DEFAULT_ACCEPTERRORS;
    } else
    {
      taskStartDelay = DEFAULT_STARTDELAY;
      taskPeriod = DEFAULT_PERIOD;
      acceptNrErrors = DEFAULT_ACCEPTERRORS;
    }
  }

  /**
   * Called to create a Group.
   *
   * @param name - Text name of the group
   * @return - Channel Group being managed by this PersistChanGrpMgr
   */
  public ChannelGroup createGroup(String name)
  {
    logger.debug("Creating Persistent Group: " + name);
    if (group != null) throw new IllegalStateException("Can't call createGroup twice in a row.");
    if (activated) throw new IllegalStateException("Can't create groups while activated.");

    ChannelGroup result = null;
    synchronized (builder)
    {
      result = findChannelGroup(name);
      if (result == null)
      {
        try
        {
          builder.beginTransaction();
          result = (ChannelGroup) builder.createChannelGroup(name);
          builder.endTransaction();
        } catch (ChannelBuilderException e)
        {
          try
          {
            builder.endTransaction();
          } catch (ChannelBuilderException e1)
          {
            e1.printStackTrace();
          }
          e.printStackTrace();
        }
      }
      group = result;
    }
    logger.info("createGroup(\"" + name + "\" yielded: " + result);
    return result;
  }

  /**
   * Deletes persistent group.
   */
  public void deleteGroup()
  {
    if (group == null) return;
    logger.debug("Deleting Persistent Group: " + group.getTitle());

    synchronized (builder)
    {
      try
      {
        builder.beginTransaction();
        builder.reload(group);

        // Remove group from links with channels
        Channel[] chans = (Channel[])group.getChannels().toArray(new Channel[0]);
        for (int i = 0; i < chans.length; i++)
        {
          Channel chan = chans[i];

          final Set<ChannelGroup> grps = chan.getGroups();
          grps.remove(group);
          group.getChannels().remove(chan);

          // Delete channel if it was the last group it was assigned to
          if (grps.size() == 0) builder.delete(chan);
        }

        builder.delete(group);

        builder.endTransaction();
        group = null;
      } catch (ChannelBuilderException e)
      {
        logger.error("Unable to delete Persistent Group: " + e.getMessage());
        builder.resetTransaction();
      }
    }
  }

  /**
   * Check if this PersistChanGrp has specified CHannel as a member already
   *
   * @param achannel - candidate channel to check
   * @return TRUE = yes
   */
  public boolean hasChannel(final Channel achannel)
  {
    return group.getChannels().contains(achannel);
  }

  /**
   * Add a channel to this Persisten Channel Group. If Channel already exists then just add it,
   * if it doesn't then create it and add it.
   *
   * @param url the url of the rss feed
   * @return Channel so created or located
   */
  public Channel addChannel(String url)
  {
    if (activated) throw new IllegalStateException("can't add Channels while activated.");
    Channel achannel = null;
    synchronized (builder)
    {
      try
      {
        builder.beginTransaction();
        builder.reload(group);

        achannel = findChannel(url);
        if (achannel == null)
        { // Channel is not in the database
          achannel = newChannel(url);
          logger.debug("Added New Channel: " + url);
        } else
        { // Channel is in the database, but it may not already be in this group
          if (!hasChannel(achannel))
          {
            logger.debug("Loaded existing channel" + url);
            group.add(achannel);
            achannel.getGroups().add(group);
          }
        }
        builder.endTransaction();
      } catch (Exception e)
      {
        e.printStackTrace();
        builder.resetTransaction();
      }
      return achannel;
    }
  }

  /**
   * Move a Channel from this PersistentChannelGroup to a different one
   *
   * @param channel channel in this PersistentChannelGroup that is being moved.
   * @param destGrp destination where the Channel is going to
   */
  public void moveChannelTo(Channel channel, PersistChanGrpMgr destGrp)
  {
    if (activated || destGrp.isActivated())
        throw new IllegalStateException("can't move Channels while activated.");
    synchronized (builder)
    {
      try
      {
        builder.beginTransaction();
        builder.reload(group);
        builder.reload(channel);
        ChannelGroup dstGroup = builder.reload(destGrp.getChannelGroup());

        group.remove(channel);
        channel.getGroups().remove(group);

        dstGroup.add(channel);
        channel.getGroups().add(dstGroup);

        builder.endTransaction();
      } catch (Exception e)
      {
        e.printStackTrace();
        builder.resetTransaction();
      }
    }
  }

  /**
   * Delete specified channel from this PersistChanGrpMgr. Status indicates whether Channel was
   * previously part of this group.
   *
   * @param channel - Channel being removed from the Group.
   * @return true if channel was deleted, false if channel was not a member to begin with
   */
  public boolean deleteChannel(Channel channel)
  {
    boolean result = false;
    if (activated) throw new IllegalStateException("can't delete Channels while activated.");
    synchronized (builder)
    {
      try
      {
        builder.beginTransaction();
        builder.reload(group);
        builder.reload(channel);

        if (hasChannel(channel))
        {
          group.remove(channel);
          channel.getGroups().remove(group);
          builder.delete(channel);

          result = true;
        }

        builder.endTransaction();
      } catch (Exception e)
      {
        e.printStackTrace();
        builder.resetTransaction();
      }
    }

    return result;
  }

  /**
   * Delete specified item from specified Channel
   *
   * @param channel - Channel to delete from
   * @param item - Item to delete from that channel
   *
   * @return number of items left in the channel AFTER the deletion.
   */
  public int deleteItemFromChannel(Channel channel, Item item)
  {
    if (activated) throw new IllegalStateException("can't delete Items while activated");
    int result = 0;
    synchronized (builder)
    {
      try
      {
        builder.beginTransaction();
        builder.reload(channel);
        builder.reload(item);

        channel.removeItem(item);
        builder.delete(item);

        result = channel.getItems().size();

        builder.endTransaction();
      } catch (ChannelBuilderException e)
      {
        e.printStackTrace();
        builder.resetTransaction();
      }
    }
    return result;
  }

  /**
   * Return number of Items currently in specified Channel
   *
   * @param channel Channel to query
   * @return number of Items
   */
  public int getItemCount(Channel channel) {
    if (activated) throw new IllegalStateException("can't count Items while activated");
    int result = 0;
    synchronized (builder)
    {
      try
      {
        builder.beginTransaction();
        builder.reload(channel);

        result = channel.getItems().size();

        builder.endTransaction();
      } catch (ChannelBuilderException e)
      {
        e.printStackTrace();
        builder.resetTransaction();
      }
    }
    return result;
  }
  /*
   * Notification handlers.
   *
   * With persistent Channels we have an alternate notification mechanism because keeping the
   * observer in the ChannelIF doesn't work because that doesn't get persisted so the setting is
   * lost between sessions. We might want to consider rearchitecting and not storing the observers
   * in the ChannelIFs at all. Same goes for the Items.
   */

  /**
   * notifyChannelsAndItems - Notify both item and channel listeners for a channel and all its
   * items. This is useful if the client wants to treat a Channel that was recently read in by
   * hibernate in a consistent way with listeners.
   *
   * @param channel - Relevant channel.
   */
  public void notifyChannelsAndItems(Channel channel)
  {
    synchronized (builder)
    {
      try
      {
        builder.beginTransaction();
        builder.reload(channel);

        notifyChannelRetrieved(channel);
        notifyItems(channel);

        builder.endTransaction();
      } catch (ChannelBuilderException e)
      {
        e.printStackTrace();
        builder.resetTransaction();
      }
    }
  }

  /**
   * Send notifications for all the items of this channel that they have been added.
   *
   * @param channelHandle -
   */
  public void notifyItems(Channel channelHandle)
  {
    if (globalChannelObserver != null)
    {
      Iterator iterChan = channelHandle.getItems().iterator();
      while (iterChan.hasNext())
      {
        notifyItemAdded((Item) iterChan.next());
      }
    }
  }

  /**
   * notifyChannelsAndItems - Call notifyChannelAndItems(channels) across all channels in this
   * PersistentChanGrpMgr.
   */
  public void notifyChannelsAndItems()
  {
    Iterator chanIter = group.getChannels().iterator();
    while (chanIter.hasNext())
    {
      notifyChannelsAndItems((Channel) chanIter.next());
    }
  }

  /**
   * Send notifications about all Channels in this group (but not their items) -
   */
  public void notifyChannels()
  {
    //   Iterator chanIter = group.getChannels().iterator();
    Iterator chanIter = channelIterator();
    while (chanIter.hasNext())
    {
      notifyChannelRetrieved((Channel) chanIter.next());
    }
  }

  /**
   * Send notification that specified channel was retrieved.
   *
   * @param chan -
   */
  public void notifyChannelRetrieved(Channel chan)
  {
    if (globalChannelObserver != null)
    {
      try
      {
        globalChannelObserver.channelRetrieved(chan);
      } catch (Exception e)
      {
        // We don't need any troubles with observer exceptions.
        logger.error(e.getMessage(), e);
      }
    }
  }

  /**
   * Send notification that specified item was retrieved.
   *
   * @param newItem -
   */
  public void notifyItemAdded(Item newItem)
  {
    if (globalChannelObserver != null)
    {
      try
      {
        globalChannelObserver.itemAdded(newItem);
      } catch (Exception e)
      {
        // We don't need any troubles with observer exceptions.
        logger.error(e.getMessage(), e);
      }
    }
  }

  /**
   * Notify that the PersistChanGrpMgrTask is currently in the middle of its 'run()' method.
   *
   * @param isPolling true - start polling, false- end
   */
  public void notifyPolling(boolean isPolling)
  {
    if (globalChannelObserver != null)
    {
      try
      {
        globalChannelObserver.pollingNow(group.getTitle(), pollingCounter, isPolling);
      } catch (Exception e)
      {
        // We don't need any troubles with observer interrupt our polling.
        logger.error(e.getMessage(), e);
      }
    }
  }

  /**
   * Setup the one and only Global observer. Note this is not an observer chain, but just a single
   * one.
   *
   * @param obser Observer to register
   */
  public void setGlobalObserver(PersistChanGrpMgrObserverIF obser)
  {
    globalChannelObserver = obser;
  }

  /**
   * activate -
   *  -
   */
  public synchronized void activate()
  {
    if (activated) return;

    task = new PersistChanGrpMgrTask(this, taskPeriod);
    task.start();

    activated = true;
  }

  /**
   * Simply return whether we are currently activated (that is, running the tasks that download and
   * process RSS. Will also return true if the PersistChanGrpMgrTask is still in the middle of
   * finishing.
   *
   * @return true = activated
   */
  public boolean isActivated()
  {
    return activated || (task != null && task.isRunning());
  }

  /**
   * Bump up polling counter by one.
   *
   */
  public void incrPollingCounter()
  {
    pollingCounter++;
  }

  /**
   * Return how many times the task has polled the feed since this PersistChanGrp was built
   *
   * @return polling count so far
   */
  public int getPollingCounter()
  {
    return pollingCounter;
  }

  /**
   * Interrupts the update task and return immediately. Do not waits for task to stop.
   */
  public synchronized void deActivate()
  {
    deActivate(false);
  }

  /**
   * Interrupts the update task and return immediately. Waits for task to finish
   * if <code>waitForFinish</code> argument set.
   *
   * @param waitForFinish TRUE to wait until task actually finishes.
   */
  public synchronized void deActivate(final boolean waitForFinish)
  {
    if (task != null)
        logger.debug("deActivate(" + task.getName() + ") " + activated);
    else
        logger.debug("deActivate task = null");

    if (!activated) return;

    task.interrupt(waitForFinish);

    activated = false;
  }

  /**
   * Change parameters of how this PersistChanGrpMgr works. Only allowed when this PersistChanGrp is
   * inactive.
   *
   * @param startDel ms before starting (-1 means don't change.)
   * @param period ms between iterations (-1 means don't change.)
   * @param acceptErr number of errors before putting a channel offline (-1 means don't change)
   */
  public void setParams(final int startDel, final int period, final int acceptErr)
  {
    if (activated) throw new IllegalStateException("can't setParams while activated");
    if (startDel != -1) taskStartDelay = startDel;
    if (period != -1) taskPeriod = period;
    if (acceptErr != -1) acceptNrErrors = acceptErr;
  }

  /**
   * Create an iterator to iterate across all the channels in this group.
   * @return the iterator
   */
  public Iterator channelIterator()
  {
    Iterator ret = null;
    synchronized (builder)
    {
      try
      {
        builder.beginTransaction();
        builder.reload(group);

        ret = group.getAll().iterator();

        builder.endTransaction();
      } catch (Exception e)
      {
        e.printStackTrace();
      }
    }
    return ret;
  }

  /**
   * Get currently associated ChannelBuilder
   *
   * @return the current cb
   */
  public ChannelBuilder getBuilder()
  {
    return builder;
  }

  /**
   * Get currently assocaited ChannelGrouo
   *
   * @return the cg
   */
  public ChannelGroup getChannelGroup()
  {
    return group;
  }

  /**
   * Get currently assocaited SessionHandler
   *
   * @return the sh
   */
  public SessionHandler getHandler()
  {
    return handler;
  }

  /**
   * @return acceptable number of errors
   */
  public int getAcceptNrErrors()
  {
    return acceptNrErrors;
  }

  /**
   * Return nicely formatted string for this object
   *
   * @return - the string
   */
  public String toString()
  {
    String result = "";
    synchronized (builder)
    {
      try
      {
        builder.beginTransaction();
        builder.reload(group);

        result = group.getTitle() + "[" + group.getChannels().size() + "]";

        builder.endTransaction();
      } catch (Exception e)
      {
        e.printStackTrace();
      }
    }
    return result;
  }

  /**
   * newChannel -
   *
   * @param url
   * @return -
   */
  private Channel newChannel(String url)
  {
    Channel channel;
    synchronized (builder)
    {
      channel = (Channel) builder.createChannel("[uninitialized channel]");
    }
    channel.setLocationString(url);

    group.add(channel);
    channel.getGroups().add(group);

    return channel;
  }

  /**
   * Search for a Channel for the indicated url (i.e. the site url or xml feed url)
   *
   * N.B: This method assumes that a builder.beginTransaction() has been performed.
   *
   * @param url
   * @return - Channel or null if none found
   */
  private Channel findChannel(String url)
  {
    Channel achan = null;
    synchronized (builder)
    {
      Session sess = builder.getSession();
      try
      {
      	Query q = sess.createQuery("from Channel chan where chan.locationString = :url order by chan.id desc");
      	q.setParameter("url", url, Hibernate.STRING);
        final List channels = q.list();

        // List channels will contain 0 or more Channels in the database for said url.

        final int size = channels.size();
        if (size > 0)
        {
          if (size > 1)
          {
            logger.error("Multiple Channels for " + url + " found.");
          }
          achan = (Channel) channels.get(0);
        }
      } catch (HibernateException e)
      {
        achan = null;
        e.printStackTrace();
      }
    }

    logger.info("findChannel: " + url + "->" + achan);
    return achan;
  }

  /**
   * Search for the indicated Channel Group.
   *
   * @param name - Name of ChannelGroup to locate in database
   * @return - ChannelGroup or null if none found by that name
   */
  private ChannelGroup findChannelGroup(String name)
  {
    ChannelGroup result = null;
    synchronized (builder)
    {
      try
      {
        builder.beginTransaction();

        final Session sess = builder.getSession();
        final Query q = sess.createQuery("from ChannelGroup as grp where grp.title = :title");
        q.setParameter("title", name, Hibernate.STRING);
        final List results = q.list();

        final int size = results.size();
        if (size > 0)
        {
          if (size > 1)
          {
            logger.error("Multiple Channel Groups called " + name + " found.");
          }
          result = (ChannelGroup) results.get(0);
        }

        builder.endTransaction();
      } catch (Exception e)
      {
        e.printStackTrace();
        builder.resetTransaction();
      }
    }

    logger.info("findChannelGroup: " + name + "->" + result);
    return result;
  }
}
