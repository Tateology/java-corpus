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
 * Interface that gives wait - notify primitives to implementors.
 *
 * @see Semaphore
 * 
 * @author <a href="mailto:simone.bordet@compaq.com">Simone Bordet</a>
 * @version $Revision$
 */
public interface WaitSync
   extends Sync
{
   /**
    * Pone in wait status this sync, until {@link #doNotify} is called to wake it up.
    * 
    * @see #doNotify
    * @throws InterruptedException 
    */
   void doWait() throws InterruptedException;
   
   /**
    * Wakes up this sync that has been posed in wait status by a {@link #doWait} call.
    * If this sync is not waiting, invoking this method should have no effect.
    * @see #doWait
    * @throws InterruptedException 
    */
   void doNotify() throws InterruptedException;
}
