/**
 * Copyright (C) 2003  Manfred Andres
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package freecs.content;

import freecs.Server;
import freecs.interfaces.*;
import java.nio.ByteBuffer;

public class PersonalizedMessage implements IContainer {
   private ByteBuffer bBuff;
   private boolean closeSocket = false;

   public PersonalizedMessage (ByteBuffer bBuff, boolean closeSocket) {
      this.bBuff = bBuff;
      this.closeSocket = closeSocket;
      if (Server.TRACE_CREATE_AND_FINALIZE)
          Server.log (this, "++++++++++++++++++++++++++++++++++++++++CREATE", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
   }

   public PersonalizedMessage (ByteBuffer bBuff) {
      this.bBuff = bBuff;
      if (Server.TRACE_CREATE_AND_FINALIZE)
          Server.log (this, "++++++++++++++++++++++++++++++++++++++++CREATE", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
   }

   public boolean prepareForSending () { return true; } // has nothing to do... personalized messages are already rendered and encoded

   public ByteBuffer getByteBuffer () {
      return bBuff;
   }

   public boolean hasContent () {
      return (bBuff != null);
   }

   public boolean       closeSocket () {
      return closeSocket;
   }

    public void finalize() {
        if (Server.TRACE_CREATE_AND_FINALIZE)
            Server.log(this, "----------------------------------------FINALIZED", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
    }
}