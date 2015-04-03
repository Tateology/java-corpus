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

// $Id: Channel.java 817 2006-12-04 23:43:29Z italobb $

package de.nava.informa.impl.hibernate;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;

import de.nava.informa.core.CategoryIF;
import de.nava.informa.core.ChannelFormat;
import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ChannelObserverIF;
import de.nava.informa.core.ChannelUpdatePeriod;
import de.nava.informa.core.CloudIF;
import de.nava.informa.core.ImageIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.core.TextInputIF;
import de.nava.informa.utils.XmlPathUtils;

/**
 * Hibernate implementation of the ChannelIF interface.
 *
 * @author Niko Schmuck (niko@nava.de)
 */
public class Channel implements ChannelIF {

  private static Log logger = LogFactory.getLog(Channel.class);

  private static final long serialVersionUID = 7579933431503905957L;

  private long id = -1;

  private String title;

  private String description;

  private URL location;

  private URL site;

  private String creator;

  private String publisher;

  private String language;

  private ChannelFormat format;

  private Set<ItemIF> items; // Items are ordered in RDF RSS 1.0.

  private Set<ChannelGroup> groups;

  private CloudIF cloud;

  private ImageIF image;

  private TextInputIF textInput;

  private String copyright;

  private Collection<CategoryIF> categories;

  private Date lastUpdated;

  private Date lastBuild;

  private Date pubDate;

  private String rating;

  private String generator;

  private String docs;

  private int ttl = -1;

  private Element channelElement;

  // RSS 1.0 Syndication Module values
  private ChannelUpdatePeriod updatePeriod = ChannelUpdatePeriod.UPDATE_DAILY;

  private int updateFrequency = 1;

  private Date updateBase;

  private transient Collection<ChannelObserverIF> observers;

  public Channel() {
    this(null, null, null);
  }

  public Channel(String title) {
    this(null, title, null);
  }

  public Channel(String title, String location) {
    this(null, title, location);
  }

  public Channel(String title, URL location) {
    this(null, title, location.toExternalForm());
  }

  public Channel(Element channelElement) {
    this(channelElement, "Unnamed channel");
  }

  public Channel(Element channelElement, String title) {
    this(channelElement, title, null);
  }

  public Channel(Element channelElement, String title, String location) {
    this.channelElement = channelElement;
    this.title = title;
    setLocationString(location);
    this.items = new HashSet<ItemIF>();
    this.categories = new ArrayList<CategoryIF>();
    this.observers = new ArrayList<ChannelObserverIF>();
    this.groups = new HashSet<ChannelGroup>();
    this.format = ChannelFormat.UNKNOWN_CHANNEL_FORMAT;
    this.lastUpdated = new Date();
  }

  // --------------------------------------------------------------
  // implementation of ChannelIF interface
  // --------------------------------------------------------------

  /**
   * @return integer representation of identity.
   */
  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  /**
   * @return title.
   */
  public String getTitle() {
    return title;
  }

  public void setTitle(String aTitle) {
    this.title = aTitle;
  }

  /**
   * @return description.
   */
  public String getDescription() {
    return description;
  }

  public void setDescription(String aDescription) {
    this.description = aDescription;
  }

  // We store the Location as a text string in the database, but as a URL in the memory based object.
  // As far as Hibernate is concerned this is a STRING property. However the getter and setter
  // convert to and from text for Informa.

  /**
   * @return location as a string.
   */
  public String getLocationString() {
    return (location == null) ? null : location.toString();
  }

  public void setLocationString(String loc) {
    if (loc == null || loc.trim().length() == 0) {
      location = null;
      return;
    } else {
      try {
        location = new URL(loc);
      } catch (MalformedURLException e) {
        logger.warn("Tried to set location to invalid URL", e);
        location = null;
      }
    }
  }

  /**
   * @return the location
   */
  public URL getLocation() {
    return location;
  }

  /**
   * @param location the location to set
   */
  public void setLocation(URL location) {
    this.location = location;
  }

  /**
   * @return URL of the site.
   */
  public String getSiteString() {
    return (site == null) ? null : site.toString();
  }

  public void setSiteString(String siteUrl) {
    if (siteUrl == null || siteUrl.trim().length() == 0) {
      site = null;
      return;
    } else {
      try {
        site = new URL(siteUrl);
      } catch (MalformedURLException e) {
        logger.warn("Tried to set site to invalid URL", e);
        site = null;
      }
    }
  }

  public URL getSite() {
    return site;
  }

  public void setSite(URL site) {
    this.site = site;
  }

  /**
   * @return name of creator.
   */
  public String getCreator() {
    return creator;
  }

  public void setCreator(String aCreator) {
    this.creator = aCreator;
  }

  /**
   * @return publisher.
   */
  public String getPublisher() {
    return publisher;
  }

  public void setPublisher(String aPublisher) {
    this.publisher = aPublisher;
  }

  /**
   * @return language of channel.
   */
  public String getLanguage() {
    return language;
  }

  public void setLanguage(String aLanguage) {
    this.language = aLanguage;
  }

  /**
   * @return format string.
   */
  public String getFormatString() {
    return format.toString();
  }

  public void setFormatString(String strFormat) {
    // TODO: this could be improved by a format resolver
    if (strFormat.equals(ChannelFormat.RSS_0_90.toString())) {
      format = ChannelFormat.RSS_0_90;
    } else if (strFormat.equals(ChannelFormat.RSS_0_91.toString())) {
      format = ChannelFormat.RSS_0_91;
    } else if (strFormat.equals(ChannelFormat.RSS_0_92.toString())) {
      format = ChannelFormat.RSS_0_92;
    } else if (strFormat.equals(ChannelFormat.RSS_0_93.toString())) {
      format = ChannelFormat.RSS_0_93;
    } else if (strFormat.equals(ChannelFormat.RSS_0_94.toString())) {
      format = ChannelFormat.RSS_0_94;
    } else if (strFormat.equals(ChannelFormat.RSS_1_0.toString())) {
      format = ChannelFormat.RSS_1_0;
    } else if (strFormat.equals(ChannelFormat.RSS_2_0.toString())) {
      format = ChannelFormat.RSS_2_0;
    }
  }

  public ChannelFormat getFormat() {
    return format;
  }

  public void setFormat(ChannelFormat aFormat) {
    this.format = aFormat;
  }

  /**
   * @return set of groups.
   */
  public Set<ChannelGroup> getGroups() {
    return groups;
  }

  public void setGroups(Set<ChannelGroup> aGroups) {
    this.groups = aGroups;
  }

  /**
   * @return items of channel.
   */
  public Set<ItemIF> getItems() {
    return items;
  }

  public void setItems(Set<ItemIF> anItems) {
    this.items = anItems;
  }

  public void addItem(ItemIF item) {
    items.add(item);
    item.setChannel(this);
    notifyObserversItemAdded(item);
  }

  public void removeItem(ItemIF item) {
    items.remove(item);
  }

  public ItemIF getItem(long itemId) {
    // TODO: improve performance
    // hibernate query cannot be used (not possible: no session object)
    // may be use transient map: items.get(new Long(id));
    ItemIF theItem = null;
    Iterator it = items.iterator();
    while (it.hasNext()) {
      ItemIF curItem = (ItemIF) it.next();
      if (curItem.getId() == itemId) {
        theItem = curItem;
        break;
      }
    }
    return theItem;
  }

  /**
   * @return image.
   */
  public ImageIF getImage() {
    return image;
  }

  public void setImage(ImageIF anImage) {
    this.image = anImage;
  }

  /**
   * @return text input.
   */
  public TextInputIF getTextInput() {
    return textInput;
  }

  public void setTextInput(TextInputIF aTextInput) {
    this.textInput = aTextInput;
  }

  /**
   * @return copyright note.
   */
  public String getCopyright() {
    return copyright;
  }

  public void setCopyright(String aCopyright) {
    this.copyright = aCopyright;
  }

  /**
   * @return rating.
   */
  public String getRating() {
    return rating;
  }

  public void setRating(String aRating) {
    this.rating = aRating;
  }

  /**
   * @return cloud.
   */
  public CloudIF getCloud() {
    return cloud;
  }

  public void setCloud(CloudIF aCloud) {
    this.cloud = aCloud;
  }

  /**
   * @return generator.
   */
  public String getGenerator() {
    return generator;
  }

  public void setGenerator(String aGenerator) {
    this.generator = aGenerator;
  }

  /**
   * @return docs.
   */
  public String getDocs() {
    return docs;
  }

  public void setDocs(String aDocs) {
    this.docs = aDocs;
  }

  /**
   * RSS 2.0: ttl stands for time to live. It's a number of minutes that
   * indicates how long a channel can be cached before refreshing from
   * the source
   *
   * @return TTL value.
   */
  public int getTtl() {
    return ttl;
  }

  public void setTtl(int aTtl) {
    this.ttl = aTtl;
  }

  /**
   * @return categories.
   */
  public Collection getCategories() {
    return categories;
  }

  public void setCategories(Collection<CategoryIF> aCategories) {
    this.categories = aCategories;
  }

  public void addCategory(CategoryIF category) {
    categories.add(category);
  }

  public void removeCategory(CategoryIF category) {
    categories.remove(category);
  }

  /**
   * @return date of last update.
   */
  public Date getLastUpdated() {
    return lastUpdated;
  }

  public void setLastUpdated(Date date) {
    this.lastUpdated = date;
    notifyObserversChannelUpdated();
  }

  /**
   * RSS 0.91: The date-time the last time the content of the channel changed.
   * RSS 2.0: 	The last time the content of the channel changed.
   *
   * @return date of last builing.
   */
  public Date getLastBuildDate() {
    return lastBuild;
  }

  public void setLastBuildDate(Date date) {
    this.lastBuild = date;
  }

  /**
   * @return publication date.
   */
  public Date getPubDate() {
    return pubDate;
  }

  public void setPubDate(Date date) {
    this.pubDate = date;
  }

  // RSS 1.0 Syndication Module methods

  /**
   * @see de.nava.informa.core.ChannelIF#getUpdatePeriod()
   */
  public ChannelUpdatePeriod getUpdatePeriod() {
    return updatePeriod;
  }

  public void setUpdatePeriod(ChannelUpdatePeriod anUpdatePeriod) {
    this.updatePeriod = anUpdatePeriod;
  }

  /**
   * Accesses data provided by the Syndication module (will apply only
   * to RSS 1.0+). Returns the number of times during the
   * <code>updatePeriod</code> that a feed should be updated
   * @return The number of times during <code>updatePeriod</code> to update the
   * feed (the update frequency).
   */
  public int getUpdateFrequency() {
    return updateFrequency;
  }

  public void setUpdateFrequency(int anUpdateFrequency) {
    this.updateFrequency = anUpdateFrequency;
  }

  /**
   * @return update base.
   */
  public Date getUpdateBase() {
    return updateBase;
  }

  public void setUpdateBase(Date date) {
    this.updateBase = date;
  }

  public String getElementValue(final String path) {
    return XmlPathUtils.getElementValue(channelElement, path);
  }

  public String[] getElementValues(final String path, final String[] elements) {
    return XmlPathUtils.getElementValues(channelElement, path, elements);
  }

  public String getAttributeValue(final String path, final String attribute) {
    return XmlPathUtils.getAttributeValue(channelElement, path, attribute);
  }

  public String[] getAttributeValues(final String path,
      final String[] attributes) {
    return XmlPathUtils.getAttributeValues(channelElement, path, attributes);
  }

  // --------------------------------------------------------------
  // implementation of ChannelObservableIF interface
  // --------------------------------------------------------------

  public void addObserver(ChannelObserverIF o) {
    observers.add(o);
  }

  public void removeObserver(ChannelObserverIF o) {
    observers.remove(o);
  }

  // --------------------------------------------------------------
  // overwrite default method implementation from Object
  // --------------------------------------------------------------

  /**
   * Returns a string representation of the object.
   *
   * @return  a string representation of the object.
   */
  public String toString() {
    return "[Hibernate Channel (" + id + "): " + title + "("
        + getItems().size() + ")( " + location + " )]";
  }

  /**
   * Compare two Channels for equality. Implementing this method and hashCode
   * correctly is CRITICAL for Hibernate to function correctly. The semantic is
   * that two Channels arensidered the 'same' RSS Channel. They may at one
   * point in time have different values for different properties, but are
   * they the SAME Channel? This is a very subtle Hibernate point, WHICH I
   * AM 90% Sure I got right. In this case, two Channels are equal specifically
   * if their RSS URL are the same. In other words, even if the title is
   * different or the description is different, it's still the same Channel.
   *
   * @param   o the reference object with which to compare.
   *
   * @return  <code>true</code> if this object is the same as the obj
   *          argument; <code>false</code> otherwise.
   */
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof ChannelIF))
      return false;

    final ChannelIF channel = (ChannelIF) o;

    final String channelTitle = channel.getTitle();
    if (title != null ? !title.equals(channelTitle) : channelTitle != null)
      return false;

    // Comparison of links uses synchronized code of Java-NET.
    // This may hurt multi-threaded applications. So, please think twice
    // before using direct comparison of links.
    final URL channelLocation = channel.getLocation();
    if (location != null ? channelLocation == null
        || !location.toString().equalsIgnoreCase(channelLocation.toString())
        : channelLocation != null)
      return false;

    final String channelDescription = channel.getDescription();
    if (description != null ? !description.equals(channelDescription)
        : channelDescription != null)
      return false;

    return true;
  }

  /**
   * Hashcode, like equals, is touchy and critical for proper functioning of
   * Hibernate.
   *
   * @return a hash code value for this object.
   */
  public int hashCode() {
    return location.toString().hashCode();
  }

  /**
   * Loops through and notifies each observer if a new item was
   * detected.
   *
   * @param newItem item added.
   */
  public void notifyObserversItemAdded(ItemIF newItem) {
    Iterator it = observers.iterator();
    while (it.hasNext()) {
      ChannelObserverIF o = (ChannelObserverIF) it.next();
      o.itemAdded(newItem);
    }
  }

  /**
   * Loops through and notifies each observer if a new item was
   * detected.
   */
  public void notifyObserversChannelUpdated() {
    Iterator it = observers.iterator();
    while (it.hasNext()) {
      ChannelObserverIF o = (ChannelObserverIF) it.next();
      o.channelRetrieved(this);
    }
  }
}
