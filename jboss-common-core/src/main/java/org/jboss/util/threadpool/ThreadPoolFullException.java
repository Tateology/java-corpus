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
 * The thread pool is full.
 *
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision$
 */
public class ThreadPoolFullException extends RuntimeException
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -1044683480627340299L;

   /**
    * Create a new ThreadPoolFullException
    */
   public ThreadPoolFullException()
   {
      super();
   }

   /**
    * Create a new ThreadPoolFullException
    *
    * @param message the message
    */
   public ThreadPoolFullException(String message)
   {
      super(message);
   }
   
   /**
    * Create a new ThreadPoolFullException.
    * 
    * @param message the message
    * @param t the throwable
    */
   public ThreadPoolFullException(String message, Throwable t)
   {
      super(message, t);
   }
}
