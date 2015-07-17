/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors as indicated
 * by the @authors tag.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.util.naming;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.OperationNotSupportedException;

/**
 A JNDI context wrapper implementation that delegates read-only methods to
 its delegate Context, and throws OperationNotSupportedException for any
 method with a side-effect.

 @author Scott.Stark@jboss.org
 @version $Revision$
 */
@SuppressWarnings("unchecked")
public class ReadOnlyContext implements Context
{
   /** The actual context we impose the read-only behavior on */
   private Context delegate;

   public ReadOnlyContext(Context delegate)
   {
      this.delegate = delegate;
   }

// Supported methods -----------------------------------------------------------
   public void close()
      throws NamingException
   {
      delegate.close();
   }

   public String composeName(String name, String prefix)
      throws NamingException
   {
      return delegate.composeName(name, prefix);
   }

   public Name composeName(Name name, Name prefix)
      throws NamingException
   {
      return delegate.composeName(name, prefix);
   }

   public String getNameInNamespace()
      throws NamingException
   {
      return delegate.getNameInNamespace();
   }

   public Hashtable getEnvironment()
      throws NamingException
   {
      return delegate.getEnvironment();
   }

   public Object lookup(String name)
      throws NamingException
   {
      return delegate.lookup(name);
   }

   public Object lookupLink(String name)
      throws NamingException
   {
      return delegate.lookupLink(name);
   }

   public Object lookup(Name name)
      throws NamingException
   {
      return delegate.lookup(name);
   }

   public Object lookupLink(Name name)
      throws NamingException
   {
      return delegate.lookupLink(name);
   }

   public NameParser getNameParser(String name)
      throws NamingException
   {
      return delegate.getNameParser(name);
   }

   public NameParser getNameParser(Name name)
      throws NamingException
   {
      return delegate.getNameParser(name);
   }

   public NamingEnumeration list(String name)
      throws NamingException
   {
      return delegate.list(name);
   }

   public NamingEnumeration listBindings(String name)
      throws NamingException
   {
      return delegate.listBindings(name);
   }

   public NamingEnumeration list(Name name)
      throws NamingException
   {
      return delegate.list(name);
   }

   public NamingEnumeration listBindings(Name name)
      throws NamingException
   {
      return delegate.listBindings(name);
   }

// Unsupported methods ---------------------------------------------------------

   public Object addToEnvironment(String propName, Object propVal)
      throws NamingException
   {
      throw new OperationNotSupportedException("This is a read-only Context");
   }

   public void bind(String name, Object obj)
      throws NamingException
   {
      throw new OperationNotSupportedException("This is a read-only Context");
   }
   public void bind(Name name, Object obj)
      throws NamingException
   {
      throw new OperationNotSupportedException("This is a read-only Context");
   }

   public Context createSubcontext(String name)
      throws NamingException
   {
      throw new OperationNotSupportedException("This is a read-only Context");
   }

   public Context createSubcontext(Name name)
      throws NamingException
   {
      throw new OperationNotSupportedException("This is a read-only Context");
   }

   public void destroySubcontext(String name)
      throws NamingException
   {
      throw new OperationNotSupportedException("This is a read-only Context");
   }

   public void destroySubcontext(Name name)
      throws NamingException
   {
      throw new OperationNotSupportedException("This is a read-only Context");
   }

   public Object removeFromEnvironment(String propName)
      throws NamingException
   {
      throw new OperationNotSupportedException("This is a read-only Context");
   }

   public void rebind(String name, Object obj)
      throws NamingException
   {
      throw new OperationNotSupportedException("This is a read-only Context");
   }

   public void rebind(Name name, Object obj)
      throws NamingException
   {
      throw new OperationNotSupportedException("This is a read-only Context");
   }

   public void rename(String oldName, String newName)
      throws NamingException
   {
      throw new OperationNotSupportedException("This is a read-only Context");
   }

   public void rename(Name oldName, Name newName)
      throws NamingException
   {
      throw new OperationNotSupportedException("This is a read-only Context");
   }

   public void unbind(String name)
      throws NamingException
   {
      throw new OperationNotSupportedException("This is a read-only Context");
   }
   public void unbind(Name name)
      throws NamingException
   {
      throw new OperationNotSupportedException("This is a read-only Context");
   }

}
