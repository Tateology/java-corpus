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
package org.jboss.util.timeout;

/**
 * TimeoutPriorityQueue.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision$
 */
public interface TimeoutPriorityQueue
{
   /**
    * Add a timeout to the queue
    * 
    * @param time the time of the timeout
    * @param target the timeout target
    * @return timeout when it was added to the queue, false otherwise
    */
   TimeoutExt offer(long time, TimeoutTarget target);
   
   /**
    * Take a timeout when it times out
    * 
    * @return the top the queue or null if the queue is cancelled
    */
   TimeoutExt take();
   
   /**
    * Retrieves and removes the top of the queue if it times out
    * or null if there is no such element
    * 
    * @return the top the queue or null if the queue is empty
    */
   TimeoutExt poll();
   
   /**
    * Retrieves and removes the top of the queue if it times out
    * or null if there is no such element
    * 
    * @param wait how to long to wait in milliseconds
    *        if the queue is empty
    * @return the top of the queue or null if the queue is empty
    */
   TimeoutExt poll(long wait);
   
   /**
    * Retrieves but does not remove the top of the queue
    * or null if there is no such element
    * 
    * @return the top of the queue or null if the queue is empty
    */
   TimeoutExt peek();
   
   /**
    * Removes the passed timeout from the queue
    * @param timeout 
    * 
    * @return true when the timeout was removed
    */
   boolean remove(TimeoutExt timeout);
   
   /**
    * Clears the queue
    */
   void clear();
   
   /**
    * Cancels the queue
    */
   void cancel();
   
   /**
    * @return the size of the queue
    */
   int size();
}
