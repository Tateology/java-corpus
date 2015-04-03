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

// $Id: ChannelBuilder.java 817 2006-12-04 23:43:29Z italobb $

package de.nava.informa.impl.hibernate;

import java.net.URL;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jdom.Element;

import de.nava.informa.core.CategoryIF;
import de.nava.informa.core.ChannelBuilderException;
import de.nava.informa.core.ChannelBuilderIF;
import de.nava.informa.core.ChannelGroupIF;
import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.CloudIF;
import de.nava.informa.core.ImageIF;
import de.nava.informa.core.ItemEnclosureIF;
import de.nava.informa.core.ItemGuidIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.core.ItemSourceIF;
import de.nava.informa.core.TextInputIF;

/**
 * Factory for the creation of the channel object model with the hibernate
 * persistent store. 
 * <p/> 
 * NOT THREAD SAFE 
 * <p/> 
 * Hibernate Multi-threading notes:
 * ChannelBuilder has some subtleties as it relates to threading. The specifics
 * of the way it is supported still need to be proven. Certainly the error
 * handling here and in UpdateChannelTask and in ChannelRegistry is incomplete.
 * It seems to work, but I would consider it incomplete still. 
 * <p/> 
 * The key facts are 
 * (1) Sessions are not thread safe and 
 * (2) Sessions should have relatively short lifespans. 
 * <p/> 
 * To support this, there is a mode of using
 * ChannelBuilder where it holds on to a SessionHandler and manages the creation
 * and destruction of Sessions on behalf of the caller. When you supply a
 * SessionHandler to ChannelBuilder, you may use the beginTransaction() and
 * endTransaction() calls to take all the steps needed before and after a
 * transaction. At the end of endTransaction() the transaction will be closed
 * and the session will be flushed and closed. To use this mode, you should 
 * (1) Create a SessionHandler , 
 * (2) Create a JDBC Connection to the database, 
 * (3) sessionHandler.setConnection(connection), and 
 * (4) use new ChannelBuilder(sessionHandler).
 * 
 * @author Niko Schmuck (niko@nava.de)
 */
public class ChannelBuilder implements ChannelBuilderIF {

  private static Log logger = LogFactory.getLog(ChannelBuilder.class);

  private Session session;

  private SessionHandler handler;

  private Transaction transaction;

  /**
   * ChannelBuilder constructor. Caller is responsible for managing sessions and
   * transactions.
   */
  public ChannelBuilder(Session session) {
    logger.info("New Channel Builder for: " + session);
    this.session = session;
    this.handler = null;
  }

  /**
   * ChannelBuilder constructor. ChannelBuilder will manage sessions and
   * transactions. Supplied SessionHandler needs to have a live JDBC connection
   * available.
   */
  public ChannelBuilder(SessionHandler handler) {
    logger.debug("New Channel Builder for: " + handler);
    this.handler = handler;
    this.session = null;
  }

  // --------------------------------------------------------------
  // Hibernate Specific Methods
  // --------------------------------------------------------------

  /**
   * Processing needed at the start of a transaction. - creating a session -
   * beginning the transaction
   */
  public void beginTransaction() throws ChannelBuilderException {
    logger.info("beginTransaction");
    if (session != null || handler == null)
      throw new IllegalStateException("Session != null || handler == null");
    try {
      session = handler.getSession();
      transaction = session.beginTransaction();
    } catch (HibernateException e) {
      e.printStackTrace();
      transaction = null;
      throw new ChannelBuilderException(e);
    }
  }

  /**
   * Processing needed at the end of a transaction. - commit the transaction -
   * flush the session - close the session TODO: catch the exception so this
   * method doesn't have any throws.
   */
  public void endTransaction() throws ChannelBuilderException {
    logger.info("endTransaction");
    if (handler == null || transaction == null || session == null)
      throw new IllegalStateException(
          "handler == null || transaction == null || session == null");
    try {
      transaction.commit();
      session.flush();
      session.close();
      session = null;
      transaction = null;

    } catch (HibernateException he) {
      if (transaction != null)
        try {
          he.printStackTrace();
          transaction.rollback();
          transaction = null;
          if (session.isOpen()) {
            session.close();
            session = null;
          }
        } catch (HibernateException e) {
          if (session.isOpen()) {
            session = null;
          }
          e.printStackTrace();
          throw new ChannelBuilderException(e);
        }
      throw new ChannelBuilderException(he);
    }
  }

  /**
   * Check if we are already in the middle of a transaction. This is needed
   * because as of now begin/endTransactions cannot be nested and in fact give
   * assert errors if you try.
   * 
   * @return - boolean indicating whether we are currently in a transaction.
   */
  public boolean inTransaction() {
    return session != null && transaction != null;
  }

  /**
   * resetTransaction - Used during error handling. If in a catch block there is
   * a potentially still open transaction (i.e. beginTransaction() was called)
   * then call this method to reset the state of the ChannelBuilder and clean up
   * the transaction.
   * 
   */
  public void resetTransaction() {
    logger.debug("Transaction being reset.");
    if (transaction != null) {
      try {
        transaction.commit();
        transaction = null;
      } catch (HibernateException e) {
        transaction = null;
        e.printStackTrace();
      }
    }
    if (session != null) {
      try {
        session.flush();
        session.close();
        session = null;
      } catch (HibernateException e) {
        e.printStackTrace();
        session = null;
      }
    }
  }

  /**
   * Certain Hibernate calls require the session. Note that this call should
   * only be made between a beginTransaction and endTransaction call which is
   * why we throw an IllegalStateException otherwise.
   */
  public Session getSession() {
    if (handler == null || session == null)
      throw new IllegalStateException(
          "getSession must be bracketed by begin/endTransaction");
    if (!handler.isSessionOpen())
      throw new IllegalStateException("Hibernate Handler must be open");
    return session;
  }

  /**
   * update - Hibernate Update some object
   * 
   * @param o
   * @throws ChannelBuilderException -
   */
  public void update(Object o) throws ChannelBuilderException {
    try {
      session.update(o);
    } catch (HibernateException e) {
      e.printStackTrace();
      throw new ChannelBuilderException("update() Failed");
    }
  }

  /**
   * Hibernate Delete some object
   * 
   * @param o -
   *          Object to Delete
   * @throws ChannelBuilderException -
   *           Translation of Hibernate exception
   */
  public void delete(Object o) throws ChannelBuilderException {
    try {
      session.delete(o);
    } catch (HibernateException e) {
      e.printStackTrace();
      throw new ChannelBuilderException("delete() Failed");
    }
  }

  // --------------------------------------------------------------
  // implementation of ChannelBuilderIF interface
  // --------------------------------------------------------------

  public void init(Properties props) throws ChannelBuilderException {
    logger.debug("initialising channel builder for hibernate backend");
  }

  public ChannelGroupIF createChannelGroup(String title) {
    ChannelGroupIF obj = new ChannelGroup(title);
    save(obj);
    return obj;
  }
  
  public ChannelIF createChannel(String title) {
    return createChannel((Element) null, title);
  }

  public ChannelIF createChannel(Element channelElement, String title) {
    return createChannel(channelElement, title, null);
  }

  public ChannelIF createChannel(String title, String location) {
    return createChannel(null, title, location); 
  }
  
  /**
   * May throw runtime HibernateException
   */
  public ChannelIF createChannel(Element channelElement, String title, String location) {
    ChannelIF obj = null;
    if (location != null) {
      Query query = session.createQuery("from Channel as channel where channel.locationString = ? ");
      query.setString(0, location);
      obj = (ChannelIF) query.uniqueResult();
    }
    if (obj == null) {
      obj = new Channel(channelElement, title, location);
      session.save(obj);
    } else {
      logger.info("Found already existing channel instance with location " + location);
    }
    return obj;
  }
  
  public ItemIF createItem(ChannelIF channel, String title, String description, URL link) {
    return createItem(null, channel, title, description, link);
  }

  public ItemIF createItem(Element itemElement, ChannelIF channel, String title, String description, URL link) {
    // according to RSS 2.0 spec link may be omitted, but need link
    // for unique identifier.  Add channel location for uniqueness?
    //
    if (link == null) {
      throw new RuntimeException("link required for item " + title + " for persistence uniqueness");
    }
    
    Query query = session.createQuery("from Item as item where item.linkString = ? ");
    query.setString(0, link.toString());
    ItemIF obj = (ItemIF) query.uniqueResult();
    if (obj == null) {
      obj = new Item(channel, title, description, link);
      if (channel != null) {
        channel.addItem(obj);
      }
      session.save(obj);
    } else {
      logger.info("Found already existing item instance with location " + link);
    }
    return obj;
  }

  public ItemIF createItem(ChannelIF channel, ItemIF item) {
    throw new RuntimeException("Not implemented yet.");
  }
      
  
  public ImageIF createImage(String title, URL location, URL link) {
    Query query = session.createQuery("from Image as img where img.locationString = ? ");
    query.setString(0, location.toString());
    ImageIF obj = (Image) query.uniqueResult();
    if (obj == null) {
      obj = new Image(title, location, link);
      session.save(obj);
    }
    return obj;
  }
  
  public CloudIF createCloud(String domain, int port, String path, String registerProcedure, String protocol) {
    logger.info("ChannelBuilder is creating a Persistent Cloud");
    // equality by domain, port, and path
    
    Query query = session.createQuery("from Cloud as cld where cld.domain = ? and cld.port = ? and cld.path = ?");
    query.setString(0, domain);
    query.setInteger(1, port);
    query.setString(2, path);
    CloudIF obj = (CloudIF) query.uniqueResult();
    if (obj == null) {
      obj = new Cloud(domain, port, path, registerProcedure, protocol);
      session.save(obj);
    }
    
    return obj;
  }
  
  public TextInputIF createTextInput(String title, String description,
      String name, URL link) {
    Query query = session.createQuery("from TextInput as txt where txt.title = ? and txt.name = ? and txt.linkString = ? ");
    query.setString(0, title);
    query.setString(1, name);
    query.setString(2, link.toString());
    TextInputIF obj = (TextInput) query.uniqueResult();
    if (obj == null) {
      obj = new TextInput(title, description, name, link);
      session.save(obj);
    }
    return obj;
  }
  
  public ItemSourceIF createItemSource(ItemIF item, String name, String location, Date timestamp) {
    // TODO Auto-generated method stub
    return null;
  }
  
  public ItemSourceIF createItemSource(String name, String location, Date timestamp) {
    
    Query query = session.createQuery("from ItemSource as src where src.name = ? and src.location = ? and src.timestamp = ?  ");
    query.setString(0, name);
    query.setString(1, location);
    query.setTimestamp(2, timestamp);
    ItemSourceIF obj = (ItemSourceIF) query.uniqueResult();
    if (obj == null) {
      obj = new ItemSource(null, name, location, timestamp);
      session.save(obj);
    }
    return obj;
  }
  
  public ItemEnclosureIF createItemEnclosure(ItemIF item, URL location,
      String type, int length) {
    Query query = session.createQuery("from ItemEnclosure as enc where enc.item.id = ? ");
    query.setLong(0, item.getId());
    ItemEnclosureIF obj = (ItemEnclosureIF) query.uniqueResult();
    if (obj == null) {
      obj = new ItemEnclosure(item, location, type, length);
      session.save(obj);
    }
    return obj;
  }
  
  public ItemGuidIF createItemGuid(ItemIF item, String location, boolean permaLink) {
    Query query = session.createQuery("from ItemGuid as guid where guid.location = ? ");
    query.setString(0, location);
    ItemGuidIF guid = (ItemGuidIF) query.uniqueResult();
    if (guid == null) {
      guid = new ItemGuid(item, location, permaLink);
      guid.setPermaLink(permaLink);
      session.save(guid);
    }
    return guid;
  }
  
  public CategoryIF createCategory(CategoryIF parent, String title) {
    return createCategory(parent, title, null);
  }
  
  public CategoryIF createCategory(CategoryIF parent, String title, String domain) {
    Query query = session.createQuery("from Category as cat where cat.title = ? and cat.domain = ? ");
    query.setString(0, title);
    query.setString(1, domain);
    CategoryIF cat = (CategoryIF) query.uniqueResult();
    if (cat == null) {
      cat = new Category(title);
      cat.setDomain(domain);
      if (parent != null) {
        parent.addChild(cat);
      }
      session.save(cat);
    }
    return cat;
  }

  public void close() throws ChannelBuilderException {
    logger.debug("closing channel builder for hibernate backend");
  }

  /**
   * Reloads group for use in new session.
   * 
   * @param group
   *          to reload.
   * 
   * @return reloaded group for chaning.
   * 
   * @throws ChannelBuilderException
   *           when unable to reload data.
   */
  public ChannelGroup reload(ChannelGroup group) throws ChannelBuilderException {
    try {
      getSession().load(group, new Long(group.getId()));
    } catch (HibernateException e) {
      throw new ChannelBuilderException("Unable to reload group: "
          + e.getMessage());
    }

    return group;
  }

  /**
   * Reloads channel for use in new session.
   * 
   * @param channel
   *          channel to reload.
   * 
   * @return reloaded channel for chaining.
   * 
   * @throws ChannelBuilderException
   *           when unable to reload data.
   */
  public Channel reload(Channel channel) throws ChannelBuilderException {
    try {
      getSession().load(channel, new Long(channel.getId()));
    } catch (HibernateException e) {
      throw new ChannelBuilderException("Unable to reload channel: "
          + e.getMessage());
    }

    return channel;
  }

  /**
   * Reloads item for use in new session.
   * 
   * @param item
   *          item to reload.
   * 
   * @return reloaded item for chaning.
   * 
   * @throws ChannelBuilderException
   *           when unable to reload data.
   */
  public Item reload(Item item) throws ChannelBuilderException {
    try {
      getSession().load(item, new Long(item.getId()));
    } catch (HibernateException e) {
      throw new ChannelBuilderException("Unable to reload item: "
          + e.getMessage());
    }

    return item;
  }

  // -------------------------------------------------------------
  // internal helper methods
  // -------------------------------------------------------------

  protected void save(Object dataObject) {
    if (session == null)
      throw new IllegalStateException("Session == null");
    try {
      session.save(dataObject);
    } catch (HibernateException he) {
      throw new RuntimeException(he.getMessage());
    }
  }

}
