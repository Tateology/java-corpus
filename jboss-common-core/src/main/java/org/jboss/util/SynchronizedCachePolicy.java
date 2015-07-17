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
package org.jboss.util;


/**
 * A synchronized cache policy wrapper.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision$
 * @see CachePolicy
 */
public final class SynchronizedCachePolicy
   implements CachePolicy
{

   // Attributes ----------------------------------------------------

   private final CachePolicy delegate;

   // Constructors --------------------------------------------------

   public SynchronizedCachePolicy(CachePolicy delegate)
   {
      this.delegate = delegate;
   }

   // CachePolicy implementation ------------------------------------

   synchronized public Object get(Object key)
   {
      return delegate.get(key);
   }

   synchronized public Object peek(Object key)
   {
      return delegate.get(key);
   }

   synchronized public void insert(Object key, Object object)
   {
      delegate.insert(key, object);
   }

   synchronized public void remove(Object key)
   {
      delegate.remove(key);
   }

   synchronized public void flush()
   {
      delegate.flush();
   }

   synchronized public int size()
   {
      return delegate.size();
   }

   synchronized public void create() throws Exception
   {
      delegate.create();
   }

   synchronized public void start() throws Exception
   {
      delegate.start();
   }

   synchronized public void stop()
   {
      delegate.stop();
   }

   synchronized public void destroy()
   {
      delegate.destroy();
   }
}
