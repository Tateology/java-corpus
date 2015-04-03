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


// $Id: DemoCategoryHibernate.java 770 2005-09-24 22:35:15Z niko_schmuck $

package de.nava.informa.impl.hibernate;

import java.util.Iterator;
import java.util.List;

import de.nava.informa.core.CategoryIF;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * Class demonstrating the use of the hibernate backend to persist the
 * channel object model to a relational database.
 * 
 * @author Niko Schmuck
 */
public class DemoCategoryHibernate {

  public static void main(String[] args) throws Exception {
    SessionHandler handler = SessionHandler.getInstance();
    Session session = handler.getSession();
    ChannelBuilder builder = new ChannelBuilder(session);
    
    // --- create a new cat
    CategoryIF catA = builder.createCategory(null, "News Category");
    Long catId = null; 
    
    Transaction tx = null;
    try {
      tx = session.beginTransaction();
      session.save(catA);
      tx.commit();
      catId = new Long(catA.getId());
      System.out.println("Saved category with id " + catId + " persistently");
    } 
    catch (HibernateException he) {
      if (tx != null) tx.rollback();
      throw he;
    }
    finally {
      session.close();
    }
    
    // --- update
    session = handler.getSession();
    try {
      tx = session.beginTransaction();
      Category theCat = (Category) session.load(Category.class, catId);
      theCat.setTitle("Another category title");
      tx.commit();
      System.out.println("Updated category title for id: " + catId);
    } catch (HibernateException he) {
      if (tx != null) tx.rollback();
      throw he;
    }
    finally {
      session.close();  
    }
    
    // --- list
    session = handler.getSession();
    try {
      tx = session.beginTransaction();
      // Query q = session.createQuery("select cat.id from Category as cat");
      // List result = q.list();
      Query q = session.createQuery("from Category as cat where cat.title = :title");
      q.setParameter("title", "Another category title", Hibernate.STRING);
      List cats = q.list();
      tx.commit();
      Iterator it = cats.iterator();
      while (it.hasNext()) {
        Category c = (Category) it.next();
        System.out.println("--> " + c.getId());
      }
    } catch (HibernateException he2) {
      if (tx != null) tx.rollback();
      throw he2;
    }
    finally {
      session.close();  
    }
  }

}
