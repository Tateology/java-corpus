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
package org.jboss.util.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * ???
 *      
 * @author ???
 * @version $Revision$
 */
@SuppressWarnings("unchecked")
public class SerializableEnumeration
   extends ArrayList
   implements Enumeration
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 8678951571196067510L;
   private int index;

   public SerializableEnumeration () {
      index = 0;
   }

   public SerializableEnumeration (Collection c) {
      super(c);
      index = 0;
   }
	 
   public SerializableEnumeration (int initialCapacity) {
      super(initialCapacity);
      index = 0;
   }
	 
   public boolean hasMoreElements() {
      return (index < size());
   }
	
   public Object nextElement() throws NoSuchElementException
   {
      try {
         Object nextObj = get(index);
         index++;
         return nextObj;
      }
      catch (IndexOutOfBoundsException e) {
         throw new NoSuchElementException();
      }
   }

   private void writeObject(java.io.ObjectOutputStream out)
      throws java.io.IOException
   {
      // the only thing to write is the index field
      out.defaultWriteObject();
   }
   
   private void readObject(java.io.ObjectInputStream in)
      throws java.io.IOException, ClassNotFoundException
   {
      in.defaultReadObject();
   }
}
