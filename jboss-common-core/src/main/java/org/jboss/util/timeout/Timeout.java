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
 * The public interface of timeouts.
 *   
 * @author <a href="osh@sparre.dk">Ole Husgaard</a>
 * @version $Revision$
*/
public interface Timeout
{
   /**
    * Cancel this timeout.
    *
    * It is guaranteed that on return from this method this timer is
    * no longer active. This means that either it has been cancelled and
    * the timeout will not happen, or (in case of late cancel) the
    * timeout has happened and the timeout callback function has returned.
    *
    * On return from this method this instance should no longer be
    * used. The reason for this is that an implementation may reuse
    * cancelled timeouts, and at return the instance may already be
    * in use for another timeout.
    * 
    * @return true when cancelled
    */
   public boolean cancel();
}