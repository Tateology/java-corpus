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

package org.jboss.test.util.test.collection;

import java.util.Iterator;
import java.util.Set;

/**
 * Unit tests for WeakSet
 *
 * @author <a href="mailto:sven@meiers.net">Sven Meier</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @version $Revision: 43534 $
 */
@SuppressWarnings("unchecked")
public abstract class AbstractNullableSetUnitTest extends AbstractSetUnitTest
{
   protected abstract Set createSet();

   public void testNullElement()
   {
      Set set = createSet();

      set.add(null);

      assertEquals(1, set.size());

      Iterator iterator = set.iterator();
      assertTrue(iterator.hasNext());
      iterator.next();
      assertFalse(iterator.hasNext());
   }
}