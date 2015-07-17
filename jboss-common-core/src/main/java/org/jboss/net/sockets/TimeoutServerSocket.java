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

import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.io.IOException;

/** A ServerSocket that returns a TimeoutSocket from the overriden accept.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public class TimeoutServerSocket extends ServerSocket
{
   public TimeoutServerSocket(int port)
      throws IOException
   {
      this(port, 50);
   }
   public TimeoutServerSocket(int port, int backlog)
      throws IOException
   {
      this(port, backlog, null);
   }
   public TimeoutServerSocket(int port, int backlog, InetAddress bindAddr)
      throws IOException
   {
      super(port, backlog, bindAddr);
   }

   public Socket accept() throws IOException
   {
      Socket s = super.accept();
      s.setSoTimeout(1000);
      TimeoutSocket ts = new TimeoutSocket(s);
      return ts;
   }
}
