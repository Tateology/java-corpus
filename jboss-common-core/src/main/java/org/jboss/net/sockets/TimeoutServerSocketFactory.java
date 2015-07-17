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
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.rmi.server.RMIServerSocketFactory;

/**
 * A RMIServerSocketFactory that installs a InterruptableInputStream to be
 * responsive to thead interruption events.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public class TimeoutServerSocketFactory
   implements RMIServerSocketFactory, Serializable
{
   /** @since 3.2.6 */
   static final long serialVersionUID = 7006964274840965634L;
   protected transient InetAddress bindAddress;
   protected int backlog = 200;

   /**
    * Create a socket factory that binds on any address with a default
    * backlog of 200
    */
   public TimeoutServerSocketFactory()
   {
      this(null, 200);
   }

   /**
    * Create a socket factory with the given bind address
    * @param bindAddress 
    */
   public TimeoutServerSocketFactory(InetAddress bindAddress)
   {
      this(bindAddress, 200);
   }

   /**
    * Create a socket factory with the given backlog
    * @param backlog 
    */
   public TimeoutServerSocketFactory(int backlog)
   {
      this(null, backlog);
   }

   /**
    * Create a socket factory with the given bind address and backlog
    * @param bindAddress 
    * @param backlog 
    */
   public TimeoutServerSocketFactory(InetAddress bindAddress, int backlog)
   {
      this.bindAddress = bindAddress;
      this.backlog = backlog;
   }

   public String getBindAddress()
   {
      String address = null;
      if (bindAddress != null)
         address = bindAddress.getHostAddress();
      return address;
   }

   public void setBindAddress(String host) throws UnknownHostException
   {
      bindAddress = InetAddress.getByName(host);
   }
   public void setBindAddress(InetAddress bindAddress)
   {
      this.bindAddress = bindAddress;
   }

   /**
    * Create a server socket on the specified port (port 0 indicates
    * an anonymous port).
    *
    * @param port the port number
    * @return the server socket on the specified port
    * @throws java.io.IOException if an I/O error occurs during server socket
    *    creation
    * @since 1.2
    */
   public ServerSocket createServerSocket(int port) throws IOException
   {
      ServerSocket activeSocket = new TimeoutServerSocket(port, backlog, bindAddress);
      return activeSocket;
   }

   public boolean equals(Object obj)
   {
      return obj instanceof TimeoutServerSocketFactory;
   }

   public int hashCode()
   {
      return getClass().getName().hashCode();
   }
}