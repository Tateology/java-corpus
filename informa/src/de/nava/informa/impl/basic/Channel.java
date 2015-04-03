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

package de.nava.informa.impl.basic;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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
 * In-Memory implementation of the ChannelIF interface.
 *
 * @author Niko Schmuck (niko@nava.de)
 */
public class Channel implements ChannelIF {

  private static final long serialVersionUID = 8793309902338066823L;

  private long id;

  private String title;

  private String description;

  private URL location;

  private URL site;

  private String creator;

  private String publisher;

  private String language;

  private ChannelFormat format;

  private Map<Long, ItemIF> items;

  private ImageIF image;

  private CloudIF cloud;

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
  //  private String updatePeriod = UPDATE_DAILY;
  //  private int updateFrequency = 1;
  private ChannelUpdatePeriod updatePeriod = null;

  private int updateFrequency = -1;

  private Date updateBase;

  private transient Collection<ChannelObserverIF> observers;

  public Channel() {
    this((String) null);
  }

  public Channel(String title) {
    this(null, title);
  }

  public Channel(Element channelElement) {
    this(channelElement, "Unnamed channel");
  }

  public Channel(Element channelElement, String title) {
    this.id = IdGenerator.getInstance().getId();
    this.channelElement = channelElement;
    this.title = title;
    this.items = Collections.synchronizedMap((new LinkedHashMap<Long, ItemIF>()));
    this.categories = new ArrayList<CategoryIF>();
    this.observers = new ArrayList<ChannelObserverIF>();
    this.format = ChannelFormat.UNKNOWN_CHANNEL_FORMAT;
    this.lastUpdated = new Date();
  }

  // --------------------------------------------------------------
  // implementation of ChannelIF interface
  // --------------------------------------------------------------

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public URL getLocation() {
    return location;
  }

  public void setLocation(URL location) {
    this.location = location;
  }

  public URL getSite() {
    return site;
  }

  public void setSite(URL site) {
    this.site = site;
  }

  public String getCreator() {
    return creator;
  }

  public void setCreator(String creator) {
    this.creator = creator;
  }

  public String getPublisher() {
    return publisher;
  }

  public void setPublisher(String publisher) {
    this.publisher = publisher;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getRating() {
    return rating;
  }

  public void setRating(String rating) {
    this.rating = rating;
  }

  public CloudIF getCloud() {
    return cloud;
  }

  public void setCloud(CloudIF cloud) {
    this.cloud = cloud;
  }

  public String getGenerator() {
    return generator;
  }

  public void setGenerator(String generator) {
    this.generator = generator;
  }

  public String getDocs() {
    return docs;
  }

  public void setDocs(String docs) {
    this.docs = docs;
  }

  public int getTtl() {
    return ttl;
  }

  public void setTtl(int ttl) {
    this.ttl = ttl;
  }

  public ChannelFormat getFormat() {
    return format;
  }

  public void setFormat(ChannelFormat format) {
    this.format = format;
  }

  public Set<ItemIF> getItems() {
    synchronized (items) {
      return Collections.synchronizedSet(new LinkedHashSet<ItemIF>(items
          .values()));
    }
  }

  public void addItem(ItemIF item) {
    items.put(new Long(item.getId()), item);
    item.setChannel(this);
    notifyObserversItemAdded(item);
  }

  public void removeItem(ItemIF item) {
    items.remove(new Long(item.getId()));
  }

  public ItemIF getItem(long anId) {
    return (ItemIF) items.get(new Long(anId));
  }

  public ImageIF getImage() {
    return image;
  }

  public void setImage(ImageIF image) {
    this.image = image;
  }

  public TextInputIF getTextInput() {
    return textInput;
  }

  public void setTextInput(TextInputIF textInput) {
    this.textInput = textInput;
  }

  public String getCopyright() {
    return copyright;
  }

  public void setCopyright(String copyright) {
    this.copyright = copyright;
  }

  public Collection getCategories() {
    return categories;
  }

  public void setCategories(Collection<CategoryIF> categories) {
    this.categories = categories;
  }

  public void addCategory(CategoryIF category) {
    categories.add(category);
  }

  public void removeCategory(CategoryIF category) {
    categories.remove(category);
  }

  public Date getLastUpdated() {
    return lastUpdated;
  }

  public void setLastUpdated(Date lastUpdated) {
    this.lastUpdated = lastUpdated;
    notifyObserversChannelUpdated();
  }

  public Date getLastBuildDate() {
    return lastBuild;
  }

  public void setLastBuildDate(Date date) {
    this.lastBuild = date;
  }

  public Date getPubDate() {
    return pubDate;
  }

  public void setPubDate(Date pubDate) {
    this.pubDate = pubDate;
  }

  /**
   * setAllProperties - Set all the properties in this Channel by copying them
   * from a source Channel.
   * N.B. This includes all properties, but not children such as items etc.
   * N.B. Location and Format are also not copied.
   *
   * @param sourceChan - ChannelIF that will supply new values
   */
  public void setAllProperties(ChannelIF sourceChan) {
    setTitle(sourceChan.getTitle());
    setDescription(sourceChan.getDescription());
    setSite(sourceChan.getSite());
    setCreator(sourceChan.getCreator());
    setCopyright(sourceChan.getCopyright());
    setPublisher(sourceChan.getPublisher());
    setLanguage(sourceChan.getLanguage());
    setImage(sourceChan.getImage());
    setTextInput(sourceChan.getTextInput());
    setRating(sourceChan.getRating());
    setGenerator(sourceChan.getGenerator());
    setDocs(sourceChan.getDocs());
    setTtl(sourceChan.getTtl());
    setCloud(sourceChan.getCloud());
    setLastBuildDate(sourceChan.getLastBuildDate());
    setUpdateBase(sourceChan.getUpdateBase());
    setUpdateFrequency(sourceChan.getUpdateFrequency());
    setUpdatePeriod(sourceChan.getUpdatePeriod());
    setPubDate(sourceChan.getPubDate());
  }

  // RSS 1.0 Syndication Module methods

  public ChannelUpdatePeriod getUpdatePeriod() {
    return updatePeriod;
  }

  public void setUpdatePeriod(ChannelUpdatePeriod updatePeriod) {
    this.updatePeriod = updatePeriod;
  }

  public int getUpdateFrequency() {
    return updateFrequency;
  }

  public void setUpdateFrequency(int updateFrequency) {
    this.updateFrequency = updateFrequency;
  }

  public Date getUpdateBase() {
    return updateBase;
  }

  public void setUpdateBase(Date updateBase) {
    this.updateBase = updateBase;
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
   * Indicates whether some other object is "equal to" this one.
   *
   * @param   o the reference object with which to compare.
   *
   * @return  <code>true</code> if this object is the same as the obj
   *          argument; <code>false</code> otherwise.
   *
   * @see     #hashCode()
   */
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof ChannelIF))
      return false;

    final ChannelIF channel = (ChannelIF) o;

    // Comparison of links uses synchronized code of Java-NET.
    // This may hurt multi-threaded applications. So, please think twice
    // before using direct comparison of links.
    final URL channelLocation = channel.getLocation();
    if (location != null ? channelLocation == null
        || !location.toString().equalsIgnoreCase(channelLocation.toString())
        : channelLocation != null)
      return false;

    final String channelTitle = channel.getTitle();
    if (title != null ? !title.equals(channelTitle) : channelTitle != null)
      return false;

    final String channelDescription = channel.getDescription();
    if (description != null ? !description.equals(channelDescription)
        : channelDescription != null)
      return false;

    return true;
  }

  /**
   * Returns a hash code value for the object. This method is
   * supported for the benefit of hashtables such as those provided by
   * <code>java.util.Hashtable</code>.
   *
   * @return a hash code value for this object.
   */
  public int hashCode() {
    StringBuffer sb = new StringBuffer(64);
    sb.append(title).append(description).append(location);
    return sb.toString().hashCode();
  }

  /**
   * Returns a string representation of the object.
   *
   * @return  a string representation of the object.
   */
  public String toString() {
    return "[Basic Channel (" + id + "): " + title + " (" + location + " )]";
  }

  // --------------------------------------------------------------
  // internal helper method(s)
  // --------------------------------------------------------------

  /**
   * Loops through and notifies each observer if a new item was
   * detected.
   * @param newItem the item
   */
  private void notifyObserversItemAdded(ItemIF newItem) {
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
  private void notifyObserversChannelUpdated() {
    Iterator it = observers.iterator();
    while (it.hasNext()) {
      ChannelObserverIF o = (ChannelObserverIF) it.next();
      o.channelRetrieved(this);
    }
  }

}
