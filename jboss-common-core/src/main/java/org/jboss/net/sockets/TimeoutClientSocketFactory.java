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
import java.net.Socket;

/**
 * A RMIClientSocketFactory that installs a InterruptableInputStream to be
 * responsive to thead interruption events.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public class TimeoutClientSocketFactory
   implements RMIClientSocketFactory, Serializable
{
   private static final long serialVersionUID = -920483051658660269L;

   public TimeoutClientSocketFactory()
   {
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
      Socket s = new Socket(host, port);
      s.setSoTimeout(1000);
      TimeoutSocket ts = new TimeoutSocket(s);
      return ts;
   }
   
   public boolean equals(Object obj)
   {
      return obj instanceof TimeoutClientSocketFactory;
   }
   public int hashCode()
   {
      return getClass().getName().hashCode();
   }
   
}
