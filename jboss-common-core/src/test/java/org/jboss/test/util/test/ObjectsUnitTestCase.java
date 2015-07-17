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
package org.jboss.test.util.test;

import java.util.Date;

import junit.framework.TestCase;
import org.jboss.util.Objects;

/**
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class ObjectsUnitTestCase extends TestCase
{
   public void testEquals() throws Exception
   {
      assertTrue(Objects.equals("123", "123"));

      Object first = new String[]{"123", "321"};
      Object second = new String[]{"123", "321"};
      assertTrue(Objects.equals(first, second));
      first = new String[][]{{"1", "2"}, {"1", "2"}};
      second = new String[][]{{"1", "2"}, {"1", "2"}};
      assertTrue(Objects.equals(first, second));

      assertFalse(Objects.equals("129", "123"));
      first = new String[]{"123", "324"};
      second = new String[]{"123", "321"};
      assertFalse(Objects.equals(first, second));
      first = new String[][]{{"1", "6"}, {"1", "2"}};
      second = new String[][]{{"1", "2"}, {"1", "2"}};
      assertFalse(Objects.equals(first, second));

      first = new int[]{1, 2, 3};
      second = new int[]{1, 2, 3};
      assertTrue(Objects.equals(first, second));
      first = new int[][]{{1, 2}, {1, 2}};
      second = new int[][]{{1, 2}, {1, 2}};
      assertTrue(Objects.equals(first, second));

      first = new int[]{1, 2, 4};
      second = new int[]{1, 2, 3};
      assertFalse(Objects.equals(first, second));
      first = new int[][]{{1, 6}, {1, 2}};
      second = new int[][]{{1, 2}, {1, 2}};
      assertFalse(Objects.equals(first, second));

      assertFalse(Objects.equals("123", new int[]{1, 2, 3}));
      assertFalse(Objects.equals(new int[]{1, 2, 3}, "123"));
      assertFalse(Objects.equals(new Date(), 123));
   }
}
