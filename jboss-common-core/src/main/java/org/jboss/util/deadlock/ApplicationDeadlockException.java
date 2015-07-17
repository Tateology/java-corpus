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
package org.jboss.util.deadlock;

/**
 * This exception class is thrown when application deadlock is detected when trying to lock an entity bean
 * This is probably NOT a result of a jboss bug, but rather that the application is access the same entity
 * beans within 2 different transaction in a different order.  Remember, with a PessimisticEJBLock, 
 * Entity beans are locked until the transaction commits or is rolled back.
 *
 * @author <a href="bill@burkecentral.com">Bill Burke</a>
 *
 * @version $Revision$
 *
 * <p><b>Revisions:</b><br>
 * <p><b>2002/02/13: billb</b>
 *  <ol>
 *  <li>Initial revision
 *  </ol>
 */
public class ApplicationDeadlockException extends RuntimeException
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -908428091244600395L;
   protected boolean retry = false;


   public ApplicationDeadlockException()
   {
      super();
   }

   public ApplicationDeadlockException(String msg, boolean retry)
   {
      super(msg);
      this.retry = retry;
   }

   public boolean retryable()
   {
      return retry;
   }

   
   /**
    * Detects exception contains is or a ApplicationDeadlockException.
    * @param t 
    * @return  true when it is a deadlock
    */
   public static ApplicationDeadlockException isADE(Throwable t)
   {
      while (t!=null)
      {
         if (t instanceof ApplicationDeadlockException)
         {
            return (ApplicationDeadlockException)t;
         }
         else
         {
            t = t.getCause();
         }
      }
      return null;
   }

}

