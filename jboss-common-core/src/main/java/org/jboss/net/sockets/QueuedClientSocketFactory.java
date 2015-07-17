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
package org.jboss.net.sockets;

import java.io.IOException;
import java.rmi.server.RMIClientSocketFactory;
import java.net.Socket;
import java.util.concurrent.Semaphore;

/**
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public class QueuedClientSocketFactory
   implements RMIClientSocketFactory, java.io.Externalizable
{
   private transient Semaphore permits;
   private long numPermits;
   public QueuedClientSocketFactory()
   {
   }

   public QueuedClientSocketFactory(long nPermits)
   {
      permits = new Semaphore((int)nPermits, true);
      numPermits = nPermits;
   }
   /**
    * Create a server socket on the specified port (port 0 indicates
    * an anonymous port).
    * @param  port the port number
    * @return the server socket on the specified port
    * @exception IOException if an I/O error occurs during server socket
    * creation
    * @since 1.2
    */
   public Socket createSocket(String host, int port) throws IOException
   {
      try
      {
         permits.acquire();
         return new Socket(host, port);
      }
      catch (InterruptedException ex)
      {
         throw new IOException("Failed to acquire FIFOSemaphore for ClientSocketFactory");
      }
      finally
      {
         permits.release();
      }
   }
   
   public boolean equals(Object obj)
   {
      return obj instanceof QueuedClientSocketFactory;
   }
   public int hashCode()
   {
      return getClass().getName().hashCode();
   }
   
   public void writeExternal(java.io.ObjectOutput out)
      throws IOException
   {
      out.writeLong(numPermits);
   }
   public void readExternal(java.io.ObjectInput in)
      throws IOException, ClassNotFoundException
   {
      numPermits = in.readLong();
      permits = new Semaphore((int)numPermits, true);
   }
}
