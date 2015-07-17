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
 * Interface that gives synchronization semantic to implementors
 *
 * @see Semaphore
 * 
 * @author <a href="mailto:simone.bordet@compaq.com">Simone Bordet</a>
 * @version $Revision$
 */
public interface Sync 
{
   /**
    * Acquires this sync
    * 
    * @see #release
    * @throws InterruptedException 
    */
   void acquire() throws InterruptedException;
   
   /**
    * Releases this sync
    * 
    * @see #acquire
    */
   void release();
}
