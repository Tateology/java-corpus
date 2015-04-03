//
// Informa -- RSS Library for Java
// Copyright (c) 2002, 2003 by Niko Schmuck
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


// $Id: DemoChannelHibernate.java 779 2005-09-27 22:17:06Z niko_schmuck $

package de.nava.informa.impl.hibernate;

import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * Class demonstrating the use of the hibernate backend to persist the
 * channel object model to a relational database.
 * 
 * @author Niko Schmuck
 */
public class DemoChannelHibernate {

  public static void main(String[] args) throws Exception {
    SessionHandler handler = SessionHandler.getInstance();
    Session session = handler.getSession();
    //ChannelBuilder builder = new ChannelBuilder(session);
    Transaction tx = null;

    // --- list
    try {
      tx = session.beginTransaction();
      // Query q = session.createQuery("select ch.id from Channel as ch");
      // List result = q.list();
      List result = session.createQuery("from Channel").list();
      // List chs = session.find("from Channel as ch where cat.title = ?",
      //                         "Another category title", Hibernate.STRING);
      tx.commit();
      Iterator it = result.iterator();
      while (it.hasNext()) {
        Channel c = (Channel) it.next();
        System.out.println("retrieved channel --> " + c.getId());
        System.out.println("  c: " + c);
        Iterator it_items = c.getItems().iterator();
        while (it_items.hasNext()) {
          Item item = (Item) it_items.next();
          System.out.println("  * " + item);
        }
      }
    } catch (HibernateException he2) {
      if (tx != null) tx.rollback();
      throw he2;
    }
    finally {
      session.close();  
    }
    
    // --- create
    /*
    session = handler.getSession();
    
    try {
      tx = session.beginTransaction();
      ChannelIF chA = builder.createChannel("Channel A");
      chA.setDescription("test channel for hibernate backend");
      System.out.println("created chA: " + chA);
      ItemIF itA = builder.createItem("Simple item", "oh what a desc", 
                                      new URL("http://www.sf.net/"));
      System.out.println("created itA: " + itA);
      chA.addItem(itA);
      System.out.println("itA -> chA assigned");                                    
      session.save(chA);
      System.out.println("saved chA");
      session.save(itA);
      System.out.println("saved itA");
      tx.commit();
      System.out.println("Saved channel with id " + chA.getId());
    } 
    catch (HibernateException he) {
      if (tx != null) tx.rollback();
      throw he;
    }
    finally {
      session.close();
    }
    */
    
    // --- as if nothing happened
    // chA = null;
    // itA = null;
    
  }

}
