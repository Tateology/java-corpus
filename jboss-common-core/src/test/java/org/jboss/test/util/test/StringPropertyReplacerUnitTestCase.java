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

import java.util.Properties;

import junit.framework.TestCase;
import static org.jboss.util.StringPropertyReplacer.replaceProperties;

/**
 * A StringPropertyReplacerUnitTestCase.
 * 
 * @author Brian Stansberry
 * @version $Revision$
 */
public class StringPropertyReplacerUnitTestCase extends TestCase
{
   private static final String PROP_A = "string.prop.replace.test.a";
   private static final String PROP_B = "string.prop.replace.test.b";
   private static final String PROP_C = "string.prop.replace.test.c";
   private static final String PROP_D = "string.prop.replace.test.d";
   private static final String DEFAULT = "DEFAULT";
   private static final String VALUE = "VALUE";
   private static final String WRAPPER = "wrapper";
   
   
   @Override
   protected void tearDown() throws Exception
   {
      super.tearDown();
      
      System.clearProperty(PROP_A);      
      System.clearProperty(PROP_B);
      System.clearProperty(PROP_C);
      System.clearProperty(PROP_D);
   }
   
   private static Properties setupProperties()
   {
      Properties props = new Properties();
      props.put(PROP_A, VALUE);
      props.put(PROP_C, VALUE);
      return props;
   }
   
   private static void setupSystemProperties()
   {
      System.setProperty(PROP_A, VALUE);
      System.setProperty(PROP_C, VALUE);
   }
   
   public void testNullInput()
   {
      try
      {
         assertNull(replaceProperties(null));
         fail("NPE expected with null input");
      }
      catch (NullPointerException good) {}
      
      try
      {
         assertNull(replaceProperties(null, setupProperties()));
         fail("NPE expected with null input");
      }
      catch (NullPointerException good) {}
   }
   
   public void testBasicReplacement()
   {
      basicReplacementTest(false);
   }
   
   public void testBasicReplacementFromSystemProps()
   {
      basicReplacementTest(true);
   }
   
   private void basicReplacementTest(boolean useSysProps)
   {
      String input = "${"+PROP_A+"}";
      String output = null;
      if (useSysProps)
      {
         setupSystemProperties();
         output = replaceProperties(input);
      }
      else
      {
         output = replaceProperties(input, setupProperties());
      }
      
      assertEquals(VALUE, output);
   }
   
   public void testWrappedReplacement()
   {
      wrappedReplacementTest(false);
   }
   
   public void testWrappedReplacementFromSystemProps()
   {
      wrappedReplacementTest(true);
   }
   
   private void wrappedReplacementTest(boolean useSysProps)
   {
      String input = WRAPPER+"${"+PROP_A+"}";
      String output = null;
      if (useSysProps)
      {
         setupSystemProperties();
         output = replaceProperties(input);
      }
      else
      {
         output = replaceProperties(input, setupProperties());
      }
      
      assertEquals(WRAPPER+VALUE, output);
      
      input = "${"+PROP_A+"}"+WRAPPER;
      output = null;
      if (useSysProps)
      {
         output = replaceProperties(input);
      }
      else
      {
         output = replaceProperties(input, setupProperties());
      }
      
      assertEquals(VALUE+WRAPPER, output);
      
      input = WRAPPER+"${"+PROP_A+"}"+WRAPPER;
      output = null;
      if (useSysProps)
      {
         setupSystemProperties();
         output = replaceProperties(input);
      }
      else
      {
         output = replaceProperties(input, setupProperties());
      }
      
      assertEquals(WRAPPER+VALUE+WRAPPER, output);
   }
   
   public void testMissingProperty()
   {
      missingPropertyTest(false);
   }
   
   public void testMissingPropertyFromSystemProps()
   {
      missingPropertyTest(true);
   }
   
   private void missingPropertyTest(boolean useSysProps)
   {
      String input = WRAPPER+"${"+PROP_B+"}"+WRAPPER;
      String output = null;
      if (useSysProps)
      {
         setupSystemProperties();
         output = replaceProperties(input);
      }
      else
      {
         output = replaceProperties(input, setupProperties());
      }
      
      assertEquals(input, output);
   }
   
   public void testWrappedMissingProperty()
   {
      wrappedMissingPropertyTest(false);
   }
   
   public void testWrappedMissingPropertyFromSystemProps()
   {
      wrappedMissingPropertyTest(true);
   }
   
   private void wrappedMissingPropertyTest(boolean useSysProps)
   {
      String input = WRAPPER+"${"+PROP_B+"}"+WRAPPER;
      String output = null;
      if (useSysProps)
      {
         setupSystemProperties();
         output = replaceProperties(input);
      }
      else
      {
         output = replaceProperties(input, setupProperties());
      }
      
      assertEquals(input, output);
   }
   
   public void testDefaultValue()
   {
      defaultValueTest(false);
   }
   
   public void testDefaultValueFromSystemProps()
   {
      defaultValueTest(true);
   }
   
   private void defaultValueTest(boolean useSysProps)
   {
      String input = "${"+PROP_B+":"+DEFAULT+"}";
      String output = null;
      if (useSysProps)
      {
         setupSystemProperties();
         output = replaceProperties(input);
      }
      else
      {
         output = replaceProperties(input, setupProperties());
      }
      
      assertEquals(DEFAULT, output);
   }
   
   public void testSecondaryProperty()
   {
      secondaryPropertyTest(false);
   }
   
   public void testSecondaryPropertyFromSystemProps()
   {
      secondaryPropertyTest(true);
   }
   
   private void secondaryPropertyTest(boolean useSysProps)
   {
      String input = "${"+PROP_B+","+PROP_C+"}";
      String output = null;
      if (useSysProps)
      {
         setupSystemProperties();
         output = replaceProperties(input);
      }
      else
      {
         output = replaceProperties(input, setupProperties());
      }
      
      assertEquals(VALUE, output);
   }
   
   public void testSecondaryPropertyAndDefault()
   {
      secondaryPropertyAndDefaultTest(false);
   }
   
   public void testSecondaryPropertyAndDefaultFromSystemProps()
   {
      secondaryPropertyAndDefaultTest(true);
   }
   
   private void secondaryPropertyAndDefaultTest(boolean useSysProps)
   {
      String input = "${"+PROP_B+","+PROP_D+":"+DEFAULT+"}";
      String output = null;
      if (useSysProps)
      {
         setupSystemProperties();
         output = replaceProperties(input);
      }
      else
      {
         output = replaceProperties(input, setupProperties());
      }
      
      assertEquals(DEFAULT, output);
   }
   
   public void testSecondaryPropertyAndMissing()
   {
      secondaryPropertyAndMissingTest(false);
   }
   
   public void testSecondaryPropertyAndMissingFromSystemProps()
   {
      secondaryPropertyAndMissingTest(true);
   }
   
   private void secondaryPropertyAndMissingTest(boolean useSysProps)
   {
      String input = "${"+PROP_B+","+PROP_D+"}";
      String output = null;
      if (useSysProps)
      {
         setupSystemProperties();
         output = replaceProperties(input);
      }
      else
      {
         output = replaceProperties(input, setupProperties());
      }
      
      assertEquals(input, output);
   }
   
   public void testMultipleReplacements()
   {
      multipleReplacementTest(false);
   }
   
   public void testMultipleReplacementsFromSystemProps()
   {
      multipleReplacementTest(true);
   }
   
   private void multipleReplacementTest(boolean useSysProps)
   {
      String input = "${"+PROP_A+"}${"+PROP_C+"}";
      String output = null;
      if (useSysProps)
      {
         setupSystemProperties();
         output = replaceProperties(input);
      }
      else
      {
         output = replaceProperties(input, setupProperties());
      }
      
      assertEquals(VALUE+VALUE, output);
   }
   
   public void testPartialMissing()
   {
      partialMissingTest(false);
   }
   
   public void testPartialMissingFromSystemProps()
   {
      partialMissingTest(true);
   }
   
   private void partialMissingTest(boolean useSysProps)
   {
      String badinput = "${"+PROP_B+"}";
      String input = WRAPPER+"${"+PROP_A+"}"+badinput+"${"+PROP_C+"}"+WRAPPER;
      String output = null;
      if (useSysProps)
      {
         setupSystemProperties();
         output = replaceProperties(input);
      }
      else
      {
         output = replaceProperties(input, setupProperties());
      }
      
      assertEquals(WRAPPER+VALUE+badinput+VALUE+WRAPPER, output);
   }

}
