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
package freecs.util;

import freecs.Server;

/**
 * FIFO-Buffer for objects whicht automatically removes Objects retrieved via
 * get(). Programmers have to verify if value has bin put to this ObjectBuffer.
 * If buffer is full, put-method returns false, on success it returns true.
 */
public class ObjectBuffer {
   private Object elements[];
   private volatile int    nextAdd;
   private volatile int    nextRemove;
   private volatile int    capacity;
   private volatile int    counter=0;

   public ObjectBuffer (int cap) throws IllegalArgumentException {
      if (cap < 2)
         throw new IllegalArgumentException("Capacity must be higher than 1 to make sense");
      this.capacity = cap;
      this.nextAdd=0;
      this.nextRemove=0;
      elements = new Object[capacity];
      if (Server.TRACE_CREATE_AND_FINALIZE)
          Server.log (this, "++++++++++++++++++++++++++++++++++++++++CREATE", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
   }

   public int size () {
      return counter;
   }

   public int capacity () {
      return capacity;
   }

   public boolean isEmpty () {
      return counter==0;
   }

   public boolean isFull () {
      return counter==capacity;
   }

   public boolean put (Object o) {
       if (o==null) 
           throw new NullPointerException ("ObjectBuffer unable to store null");
      if (this.isFull ()) 
          return false;
      if (counter == 0  && elements == null){
          elements = new Object[capacity];
      }
      if (elements == null)
          return false;
      if (nextAdd >= capacity){
          nextAdd = 0;
          resizeTo(capacity);
          Server.log(this, "resize to capacity", Server.MSG_STATE, Server.LVL_MAJOR);
      }
      elements[nextAdd] = o;
      nextAdd++;
      if (nextAdd >= capacity){
          nextAdd = 0;       
          resizeTo(capacity);
      }
      counter++;
      return true;
   }
	
	public boolean contains (Object o) {
		for (int i = 0, j=nextRemove; i < counter; i++, j++) {
			if (j >= capacity) j = 0;
			if (elements[j].equals (o)) return true;
		}
		return false;
	}

   public Object get () {
      if (this.isEmpty ()) return null;
      return elements[nextRemove];
   }

   public Object pop () {
      if (this.isEmpty ()) return null;
      Object retObj = elements[nextRemove];
      elements[nextRemove]=null;
      nextRemove++;
      if (nextRemove >= capacity)
         nextRemove = 0;
      counter--;
      if (counter == 0){
          elements = null;
      }
      return (retObj);
   }
    
    public void inhale (ObjectBuffer ob) {
        elements = new Object[ob.capacity()];
        while (!ob.isEmpty())
            this.put(ob.pop());
    }
    
    public void clear(){
        elements = null;
        nextRemove = 0;
    }

    public void resizeTo (int size) throws IllegalArgumentException {
        if (size < this.size())
            throw new IllegalArgumentException("new capacity may not be lower than current size");
        if (size == elements.length)
            return;
        Object newelements[] = new Object[size];
        synchronized (this) {
            if (isEmpty()) {
                elements = newelements;
                return;
            }
            if (nextAdd <= nextRemove) {
                System.arraycopy(elements,nextRemove,newelements,0,this.capacity()-nextRemove);
                System.arraycopy(elements, 0, newelements, this.capacity()-nextRemove, nextAdd);
            } else {
                System.arraycopy(elements,nextRemove,newelements,0,nextAdd+1);
            }
            nextAdd = this.size();
            nextRemove=0;
            elements = newelements;
        } 
    }

    public void finalize() {
        if (Server.TRACE_CREATE_AND_FINALIZE)
            Server.log(this, "----------------------------------------FINALIZED", Server.MSG_STATE, Server.LVL_VERY_VERBOSE);
    }
}