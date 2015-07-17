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
 * Wait exclusive semaphore with wait - notify primitives
 *
 * @author <a href="mailto:simone.bordet@compaq.com">Simone Bordet</a>
 * @version $Revision$
 */
public class WaitSemaphore
   extends Semaphore
   implements WaitSync
{
   // Constants -----------------------------------------------------
   private final static int MAX_USERS_ALLOWED = 1;

   // Attributes ----------------------------------------------------
   private int m_waiters;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public WaitSemaphore()
   {
      super(MAX_USERS_ALLOWED);
   }

   // Public --------------------------------------------------------
   public void doWait() throws InterruptedException
   {
      synchronized (this)
      {
         release();
         ++m_waiters;
         waitImpl(this);
         --m_waiters;
         acquire();
      }
   }

   public void doNotify() throws InterruptedException
   {
      synchronized (this)
      {
         if (getWaiters() > 0)
         {
            acquire();
            notify();
            release();
         }
      }
   }

   public int getWaiters()
   {
      synchronized (this)
      {
         return m_waiters;
      }
   }

   // Object overrides ---------------------------------------------------
   public String toString()
   {
      return super.toString() + " - " + m_waiters;
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
