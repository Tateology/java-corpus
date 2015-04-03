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


// $Id: FeedRefreshDaemon.java 566 2004-06-28 19:33:44Z niko_schmuck $

package de.nava.informa.utils;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Use a Timer and TimerTask to periodically refresh feeds added by
 * FeedManager.
 * 
 * @author jga 
 */
public class FeedRefreshDaemon {

  private static Log logger = LogFactory.getLog(FeedRefreshDaemon.class);

  private Timer refreshTimer = null;

  public FeedRefreshDaemon() {
    logger.info("FeedRefresh Daemon instancied");
    this.refreshTimer = new Timer(true);
  }

  public void addFeed(FeedManagerEntry feed) {
    FeedRefreshTask refreshTask = new FeedRefreshTask();
    refreshTask.setFeedME(feed);
    // verifying every 5 minutes
    logger.info("scheduling new feed ");
    Date fiveMinuteLater = new Date(new Date().getTime() + (5 * 60 * 1000));

    this.refreshTimer.schedule(refreshTask, fiveMinuteLater, (5 * 60 * 1000));
  }

  private class FeedRefreshTask extends TimerTask {

    FeedManagerEntry f = null;

    int nbError = 0;

    public void setFeedME(FeedManagerEntry feedME) {
      f = feedME;
    }

    public void run() {
      try {
        f.getFeed(); // call isOutofDate and optionnaly fetch new feed
        logger.debug("feed refreshed" + f.getFeed().getLocation());
      } catch (Exception e) {
        this.nbError++;
        logger.warn("Error retrieving feed" + f.toString() + " " + e);
      }

    }
  }

}