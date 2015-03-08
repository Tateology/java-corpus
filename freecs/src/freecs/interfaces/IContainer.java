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
package freecs.interfaces;

import java.nio.ByteBuffer;

public interface IContainer {
   /**
    * prepares this container for sending
   public abstract boolean       prepareForSending ();
    */
   /**
    * returns the bytebuffer representing this containers content
    */
   public abstract ByteBuffer    getByteBuffer ();
   /**
    * true if this container has some content
    */
   public abstract boolean       hasContent ();
   /**
    * returns true if this container demands the socket to be closed
    */
   public abstract boolean       closeSocket ();
}