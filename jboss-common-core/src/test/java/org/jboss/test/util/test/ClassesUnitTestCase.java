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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.jboss.util.Classes;

/**
 * Unit tests for org.jboss.util.Classes utility
 * 
 * @author Dimitris.Andreadis@jboss.org
 * @version $Revision: 43534 $
 */
public class ClassesUnitTestCase extends TestCase
{
   public void testGetAllInterfaces()
   {
      List<Class<?>> list = new ArrayList<Class<?>>();
      Classes.getAllInterfaces(list, ExtendedClass.class);
      assertEquals(3, list.size());
      assertEquals(Interface1.class, list.get(0));
      assertEquals(Interface1.class, list.get(1));
      assertEquals(Interface2.class, list.get(2));
   }
   
   public void testGetAllUniqueInterfaces()
   {
      Class<?>[] interfaces = Classes.getAllUniqueInterfaces(ExtendedClass.class);
      assertEquals(2, interfaces.length);
   }
   
   public interface Interface1 {}
   public interface Interface2 {}

   public static class BaseClass implements Interface1, Interface2 {}
   public static class ExtendedClass extends BaseClass implements Interface1 {}
} 