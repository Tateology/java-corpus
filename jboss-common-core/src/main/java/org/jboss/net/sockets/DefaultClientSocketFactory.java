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
import java.rmi.server.RMIClientSocketFactory;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * A RMIClientSocketFactory that adds a bind address override of the server
 * host to control what the address the client uses.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 30203 $
 */
public class DefaultClientSocketFactory
   implements RMIClientSocketFactory, Serializable
{
   private static final long serialVersionUID = -920483051658660269L;
   /** An override of the server address */
   private InetAddress bindAddress;

   public DefaultClientSocketFactory()
   {
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
    * @exception java.io.IOException if an I/O error occurs during server socket
    * creation
    * @since 1.2
    */
   public Socket createSocket(String host, int port) throws IOException
   {
      InetAddress addr = null;
      if( bindAddress != null )
         addr = bindAddress;
      else
         addr = InetAddress.getByName(host);
      Socket s = new Socket(addr, port);
      return s;
   }

   public boolean equals(Object obj)
   {
      boolean equals = obj instanceof DefaultClientSocketFactory;
      if( equals && bindAddress != null )
      {
         DefaultClientSocketFactory dcsf = (DefaultClientSocketFactory) obj;
         InetAddress dcsfa = dcsf.bindAddress;
         if( dcsfa != null )
            equals = bindAddress.equals(dcsfa);
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
