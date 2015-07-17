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
import javax.net.ServerSocketFactory;

/** An implementation of RMIServerSocketFactory that supports backlog and
 * bind address settings
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public class DefaultSocketFactory extends ServerSocketFactory
   implements RMIServerSocketFactory, Serializable
{
   static final long serialVersionUID = -7626239955727142958L;
   private transient InetAddress bindAddress;
   private int backlog = 200;

   /** Create a socket factory that binds on any address with a default
    * backlog of 200
    */
   public DefaultSocketFactory()
   {
      this(null, 200);
   }
   /** Create a socket factory with the given bind address
    * @param bindAddress 
    */
   public DefaultSocketFactory(InetAddress bindAddress)
   {
      this(bindAddress, 200);
   }
   /** Create a socket factory with the given backlog
    * @param backlog 
    */
   public DefaultSocketFactory(int backlog)
   {
      this(null, backlog);
   }
   /** Create a socket factory with the given bind address and backlog
    * @param bindAddress 
    * @param backlog 
    */
   public DefaultSocketFactory(InetAddress bindAddress, int backlog)
   {
      this.bindAddress = bindAddress;
      this.backlog = backlog;
   }

   public String getBindAddress()
   {
      String address = null;
      if( bindAddress != null )
         address = bindAddress.getHostAddress();
      return address;
   }
   public void setBindAddress(String host) throws UnknownHostException
   {
      bindAddress = InetAddress.getByName(host);
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
    public ServerSocket createServerSocket(int port) throws IOException
    {
      return createServerSocket(port, backlog, bindAddress);
   }

   /**
    * @param port - the port to listen to
    * @param backlog - how many connections are queued
    * @return A ServerSocket
    * @throws IOException
    */ 
   public ServerSocket createServerSocket(int port, int backlog)
      throws IOException
   {
      return createServerSocket(port, backlog, null);
   }

   /**
    * @param port - the port to listen to
    * @param backlog - how many connections are queued
    * @param inetAddress - the network interface address to use
    * @return the server socket
    * @throws IOException
    */ 
   public ServerSocket createServerSocket(int port, int backlog,
      InetAddress inetAddress) throws IOException
   {
        ServerSocket activeSocket = new ServerSocket(port, backlog, bindAddress);
        return activeSocket;
    }

   public boolean equals(Object obj)
   {
      boolean equals = obj instanceof DefaultSocketFactory;
      if( equals && bindAddress != null )
      {
         DefaultSocketFactory dsf = (DefaultSocketFactory) obj;
         InetAddress dsfa = dsf.bindAddress;
         if( dsfa != null )
            equals = bindAddress.equals(dsfa);
         else
            equals = false;
      }
      return equals;
   }
   public int hashCode()
   {
      int hashCode = getClass().getName().hashCode();
      if( bindAddress != null )
         hashCode += bindAddress.toString().hashCode();
      return hashCode;
   }
   public String toString()
   {
      StringBuffer tmp = new StringBuffer(super.toString());
      tmp.append('[');
      tmp.append("bindAddress=");
      tmp.append(bindAddress);
      tmp.append(']');
      return tmp.toString();
   }
}
