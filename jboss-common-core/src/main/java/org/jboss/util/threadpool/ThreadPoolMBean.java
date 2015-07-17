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
package org.jboss.util.threadpool;

/**
 * Management interface for the thread pool.
 *
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision$
 */
public interface ThreadPoolMBean
{
   // Constants -----------------------------------------------------

   // Public --------------------------------------------------------

   /**
    * Get the thread pool name
    *
    * @return the thread pool name
    */
   String getName();

   /**
    * Set the thread pool name
    *
    * @param name the name
    */
   void setName(String name);

   /**
    * Get the internal pool number
    *
    * @return the internal pool number
    */
   int getPoolNumber();

   /**
    * Get the minimum pool size
    *
    * @return the minimum pool size
    */
   int getMinimumPoolSize();

   /**
    * Set the minimum pool size
    *
    * @param size the minimum pool size
    */
   void setMinimumPoolSize(int size);

   /**
    * Get the maximum pool size
    *
    * @return the maximum pool size
    */
   int getMaximumPoolSize();

   /**
    * Set the maximum pool size
    *
    * @param size the maximum pool size
    */
   void setMaximumPoolSize(int size);

   /**
    * Get the current pool size
    *
    * @return the current pool size
    */
   int getPoolSize();

   /**
    * @return the instance
    */
   ThreadPool getInstance();

   /**
    * Stop the thread pool
    */
   void stop();

   // Inner classes -------------------------------------------------
}
