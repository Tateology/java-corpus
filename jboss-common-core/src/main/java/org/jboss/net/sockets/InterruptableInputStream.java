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

import java.io.InputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;

/** An InputStream that uses the SocketTimeoutException thrown during read
 * timeouts to check if the thread has been interrupted.
 *  
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public class InterruptableInputStream extends InputStream
{
   private InputStream is;

   public InterruptableInputStream(InputStream is)
   {
      this.is = is;
   }

   public int read() throws IOException
   {
      byte[] b = {};
      int count = internalRead(b, 0, 1);
      return count > 0 ? b[0] : -1;
   }

   public int read(byte[] b) throws IOException
   {
      return internalRead(b, 0, b.length);
   }

   public int read(byte[] b, int off, int len) throws IOException
   {
      return internalRead(b, off, len);
   }

   public long skip(long n) throws IOException
   {
      return is.skip(n);
   }

   public int available() throws IOException
   {
      return is.available();
   }

   public void close() throws IOException
   {
      is.close();
   }

   public synchronized void mark(int readlimit)
   {
      is.mark(readlimit);
   }

   public synchronized void reset() throws IOException
   {
      is.reset();
   }

   public boolean markSupported()
   {
      return is.markSupported();
   }

   private int internalRead(byte[] b, int off, int len) throws IOException
   {
      int n = -1;
      while( true )
      {
         try
         {
            n = is.read(b, off, len);
            return n;
         }
         catch(SocketTimeoutException e)
         {
            // Test for thread interrupt
            if( Thread.interrupted() )
               throw e;
         }
      }
   }
}
