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

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: wburke
 * Date: Aug 21, 2003
 * Time: 2:10:46 PM
 * To change this template use Options | File Templates.
 */
@SuppressWarnings("unchecked")
public class DeadlockDetector
{
   // TODO Maybe this should be an MBean in the future
   public static DeadlockDetector singleton = new DeadlockDetector();
   // This following is for deadlock detection
   protected HashMap waiting = new HashMap();

   public void deadlockDetection(Object holder, Resource resource)
           throws ApplicationDeadlockException
   {
      HashSet set = new HashSet();
      set.add(holder);

      Object checkHolder = resource.getResourceHolder();

      synchronized (waiting)
      {
         addWaiting(holder, resource);

         while (checkHolder != null)
         {
            Resource waitingFor = (Resource)waiting.get(checkHolder);
            Object holding = null;
            if (waitingFor != null)
            {
               holding = waitingFor.getResourceHolder();
            }
            if (holding != null)
            {
               if (set.contains(holding))
               {
                  // removeWaiting should be cleaned up in acquire
                  String msg = "Application deadlock detected, resource="+resource
                     +", holder="+holder+", waitingResource="+waitingFor
                     +", waitingResourceHolder="+holding;
                  throw new ApplicationDeadlockException(msg, true);
               }
               set.add(holding);
            }
            checkHolder = holding;
         }
      }
   }

   /**
    * Add a transaction waiting for a lock
    * @param holder 
    * @param resource 
    */
   public void addWaiting(Object holder, Resource resource)
   {
      synchronized (waiting)
      {
         waiting.put(holder, resource);
      }
   }

   /**
    * Remove a transaction waiting for a lock
    * @param holder 
    */
   public void removeWaiting(Object holder)
   {
      synchronized (waiting)
      {
         waiting.remove(holder);
      }
   }

}
