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

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Unit tests for custom maps.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractMapUnitTest extends TestCase
{
   protected abstract Map createEmptyMap();

   @SuppressWarnings("unchecked")
   public void testBasicOperations() throws Exception
   {
      Map map = createEmptyMap();
      assertTrue(map.isEmpty());
      assertEquals(0, map.size());

      String key1 = "date1";
      Date value1 = new Date();
      map.put(key1, value1);

      assertTrue(map.containsKey(key1));
      assertTrue(map.containsValue(value1));
      assertEquals(1, map.size());

      String key2 = "date2";
      Date value2 = new Date();
      map.put(key2, value2);

      assertTrue(map.containsKey(key2));
      assertTrue(map.containsValue(value2));
      assertEquals(2, map.size());

      String key3 = "date3";
      Date value3 = new Date();
      map.put(key3, value3);

      assertTrue(map.containsKey(key1));
      assertTrue(map.containsValue(value1));
      assertEquals(3, map.size());

      map.clear();
      assertTrue(map.isEmpty());

      key1 = "date1";
      value1 = new Date();
      map.put(key1, value1);

      map.remove(key1);
      assertTrue(map.isEmpty());

      map.putAll(Collections.singletonMap(key1, value1));

      assertEquals(value1, map.get(key1));
      assertEquals(Collections.singletonMap(key1, value1), map);
      
      // iterables
      Iterable<String> keys = map.keySet();
      assertIterable(keys, String.class);
      Iterable<Date> values = map.values();
      assertIterable(values, Date.class);
      Iterable<Map.Entry> entries = map.entrySet();
      Map.Entry entry = assertIterable(entries, Map.Entry.class);
      assertEquals(key1, entry.getKey());
      assertEquals(value1, entry.getValue());
   }

   protected <T> T assertIterable(Iterable<T> iter, Class<T> clazz)
   {
      assertTrue(iter.iterator().hasNext());
      T next = iter.iterator().next();
      assertTrue("Next " + next + " is not instance of " + clazz.getName(), clazz.isInstance(next));
      assertNotNull(next);
      return next;
   }
}