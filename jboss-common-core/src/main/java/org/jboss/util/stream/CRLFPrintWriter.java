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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.io.InterruptedIOException;
import java.io.Writer;

/**
 * A <tt>PrintWriter</tt> that ends lines with a carriage return-line feed 
 * (<tt>CRLF</tt>).
 *
 * <h3>Concurrency</h3>
 * This class is <b>as</b> synchronized as <tt>PrintWriter</tt>.
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class CRLFPrintWriter
   extends PrintWriter
{
   protected boolean autoFlush = false;

   public CRLFPrintWriter(final Writer out) {
      super(out);
   }

   public CRLFPrintWriter(final Writer out, final boolean autoFlush) {
      super(out, autoFlush);
      this.autoFlush = autoFlush;
   }

   public CRLFPrintWriter(final OutputStream out) {
      super(out);
   }

   public CRLFPrintWriter(final OutputStream out, final boolean autoFlush) {
      super(out, autoFlush);
      this.autoFlush = autoFlush;
   }

   protected void ensureOpen() throws IOException {
      if (out == null)
         throw new IOException("Stream closed");
   }

   public void println() {
      try {
         synchronized (lock) {
            ensureOpen();

            out.write("\r\n");

            if (autoFlush) {
               out.flush();
            }
         }
      }
      catch (InterruptedIOException e) {
         Thread.currentThread().interrupt();
      }
      catch (IOException e) {
         setError();
      }
   }      
}
