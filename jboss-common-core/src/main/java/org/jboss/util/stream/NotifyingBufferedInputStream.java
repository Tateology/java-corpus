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
package org.jboss.util.stream;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * A buffered input stream that notifies every "chunk"
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:Adrian@jboss.org">Adrian Brock</a>
 */
public class NotifyingBufferedInputStream
   extends BufferedInputStream
{
   /**
    * The number of bytes between notifications
    */
   int chunkSize;

   /**
    * The number of bytes read in the current chunk
    */
   int chunk = 0;

   /**
    * The listener notified every chunk
    */
   StreamListener listener;

   /**
    * Construct a notifying buffered inputstream.
    * The listener is notified once every chunk.
    *
    * @param is the input stream to be buffered
    * @param size the buffer size
    * @param chunkSize the chunk size
    * @param listener the listener to notify
    * @exception IllegalArgumentException for a size <= 0 or chunkSize <= size
    */
   public NotifyingBufferedInputStream(InputStream is, int size, int chunkSize, StreamListener listener)
   {
      super(is, size);
      if (chunkSize <= size)
         throw new IllegalArgumentException("chunkSize must be bigger than the buffer");
      this.chunkSize = chunkSize;
      this.listener = listener;
   }

   public void setStreamListener(StreamListener listener)
   {
      this.listener = listener;
   }

   public int read()
      throws IOException
   {
      int result = super.read();
      if (result == -1)
         return result;
      checkNotification(result);
      return result;
   }

   public int read(byte[] b, int off, int len)
      throws IOException
   {
      int result = super.read(b, off, len);
      if (result == -1)
         return result;
      checkNotification(result);
      return result;
   }

   /**
    * Checks whether a notification is required and
    * notifies as appropriate
    *
    * @param result the number of bytes read
    */
   public void checkNotification(int result)
   {
      // Is a notification required?
      chunk += result;
      if (chunk >= chunkSize)
      {
         if (listener != null)
            listener.onStreamNotification(this, chunk);

         // Start a new chunk
         chunk = 0;
      }
   }
}
