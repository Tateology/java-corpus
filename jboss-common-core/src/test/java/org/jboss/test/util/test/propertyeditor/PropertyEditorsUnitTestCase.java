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
package org.jboss.test.util.test.propertyeditor;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import junit.framework.TestCase;
import org.jboss.logging.Logger;
import org.jboss.util.propertyeditor.DateEditor;
import org.jboss.util.propertyeditor.DocumentEditor;
import org.jboss.util.propertyeditor.ElementEditor;
import org.jboss.util.propertyeditor.PropertyEditors;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Unit tests for the custom JBoss property editors
 *
 * @see org.jboss.util.propertyeditor.PropertyEditors
 * 
 * @author Scott.Stark@jboss.org
 * @author Dimitris.Andreadis@jboss.org
 * @author Ales.Justin@jboss.org
 * @version $Revision: 43534 $
 */
@SuppressWarnings("unchecked")
public class PropertyEditorsUnitTestCase extends TestCase
{
   private static Logger log = Logger.getLogger(PropertyEditorsUnitTestCase.class);
   Calendar calendar = Calendar.getInstance();
   private Locale locale;
   private static final boolean hasJDK7;

   /** Augment the PropertyEditorManager search path to incorporate the JBoss
    specific editors. This simply references the PropertyEditors.class to
    invoke its static initialization block.
    */
   static
   {
      PropertyEditors.init();
      String[] paths = PropertyEditorManager.getEditorSearchPath();
      log.info(Arrays.asList(paths));
      hasJDK7 = hasClass("java.util.concurrent.ForkJoinPool");
   }

   private static boolean hasClass(final String name)
   {
      try
      {
         Class.forName(name);
         return true;
      }
      catch (ClassNotFoundException e)
      {
         return false;
      }
   }

   private static boolean hasJDK7()
   {
      return hasJDK7;
   }

   static class StringArrayComparator implements Comparator
   {
      public int compare(Object o1, Object o2)
      {
         String[] a1 = (String[]) o1;
         String[] a2 = (String[]) o2;
         int compare = a1.length - a2.length;
         for(int n = 0; n < a1.length; n ++)
            compare += a1[n].compareTo(a2[n]);
         return compare;
      }
   }
   static class ClassArrayComparator implements Comparator
   {
      public int compare(Object o1, Object o2)
      {
         Class<?>[] a1 = (Class[]) o1;
         Class<?>[] a2 = (Class[]) o2;
         int compare = a1.length - a2.length;
         for(int n = 0; n < a1.length; n ++)
         {
            int hash1 = a1[n].hashCode();
            int hash2 = a2[n].hashCode();
            compare += hash1 - hash2;
         }
         return compare;
      }
   }
   static class IntArrayComparator implements Comparator
   {
      public int compare(Object o1, Object o2)
      {
         int[] a1 = (int[]) o1;
         int[] a2 = (int[]) o2;
         int compare = a1.length - a2.length;
         for(int n = 0; n < a1.length; n ++)
            compare += a1[n] - a2[n];
         return compare;
      }
   }
   static class NumberComparator implements Comparator<Number>
   {
      public int compare(Number o1, Number o2)
      {
         return o1.intValue() - o2.intValue();
      }
   }
   static class ToStringComparator implements Comparator
   {
      public int compare(Object o1, Object o2)
      {
         String s1 = o1.toString();
         String s2 = o2.toString();
         return s1.compareTo(s2);
      }
   }

   public PropertyEditorsUnitTestCase(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      locale = Locale.getDefault();
      Locale.setDefault(Locale.US);

      super.setUp();
   }

   protected void tearDown() throws Exception
   {
      super.tearDown();

      Locale.setDefault(locale);
   }

   public void testEditorSearchPath()
      throws Exception
   {
      log.debug("+++ testEditorSearchPath");
      String[] searchPath = PropertyEditorManager.getEditorSearchPath();
      boolean foundJBossPath = false;
      for(int p = 0; p < searchPath.length; p ++)
      {
         String path = searchPath[p];
         log.debug("path["+p+"]="+path);
         foundJBossPath |= path.equals("org.jboss.util.propertyeditor");
      }
      assertTrue("Found org.jboss.util.propertyeditor in search path", foundJBossPath);
   }

   /** The mechanism for mapping java.lang.* variants of the primative types
    misses editors for java.lang.Boolean and java.lang.Integer. Here we test
    the java.lang.* variants we expect editors for.
    * @throws Exception 
    **/
   public void testJavaLangEditors()
      throws Exception
   {
      log.debug("+++ testJavaLangEditors");
      // The supported java.lang.* types
      Class[] types = {
         Boolean.class,
         Byte.class,
         Short.class,
         Integer.class,
         Long.class,
         Float.class,
         Double.class,
         Byte.class,
         Character.class,
      };
      // The input string data for each type
      String[][] inputData = {
         {"true", "false", "TRUE", "FALSE", "tRuE", "FaLsE", null},
         {"1", "-1", "0", "0x1A"},
         {"1", "-1", "0", "0xA0"},
         {"1", "-1", "0", "0xA0"},
         {"1", "-1", "0", "1000"},
         {"1", "-1", "0", "1000.1"},
         {"1", "-1", "0", "1000.1"},
         {"0x1", "-#1", "0"},
         {"A", "a", "Z", "z"},
      };
      // The expected java.lang.* instance for each inputData value
      Object[][] expectedData = {
         {Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, null},
         {Byte.valueOf("1"), Byte.valueOf("-1"), Byte.valueOf("0"), Byte.decode("0x1A")},
         {Short.valueOf("1"), Short.valueOf("-1"), Short.valueOf("0"), Short.decode("0xA0")},
         {Integer.valueOf("1"), Integer.valueOf("-1"), Integer.valueOf("0"), Integer.decode("0xA0")},
         {Long.valueOf("1"), Long.valueOf("-1"), Long.valueOf("0"), Long.valueOf("1000")},
         {Float.valueOf("1"), Float.valueOf("-1"), Float.valueOf("0"), Float.valueOf("1000.1")},
         {Double.valueOf("1"), Double.valueOf("-1"), Double.valueOf("0"), Double.valueOf("1000.1")},
         {Byte.valueOf("1"), Byte.valueOf("-1"), Byte.valueOf("0")},
         {new Character('A'), new Character('a'), new Character('Z'), new Character('z')},
      };
      // The expected string output from getAsText()
      String[][] expectedStringData = {
         {"true", "false", "true", "false", "true", "false", hasJDK7() ? null : "null"},
         {"1", "-1", "0", "26"},
         {"1", "-1", "0", "160"},
         {"1", "-1", "0", "160"},
         {"1", "-1", "0", "1000"},
         {"1.0", "-1.0", "0.0", "1000.1"},
         {"1.0", "-1.0", "0.0", "1000.1"},
         {"1", "-1", "0"},
         {"A", "a", "Z", "z"},            
      };
      Comparator[] comparators = new Comparator[types.length];

      doTests(types, inputData, expectedData, expectedStringData, comparators);
   }

   /** Test custom JBoss property editors.
    * @throws Exception 
    */
   public void testJBossEditors()
      throws Exception
   {
      log.debug("+++ testJBossEditors");
      Class[] types = {
         java.io.File.class,
         java.net.URL.class,
         java.net.URI.class,
         java.lang.String.class,         
         java.lang.Class.class,
         InetAddress.class,
         String[].class,
         Class[].class,
         int[].class,
         Date.class,
         java.util.Properties.class,
         Locale.class,
         AtomicInteger.class,
         AtomicLong.class,
         AtomicBoolean.class,
      };
      // The input string data for each type
      String[][] inputData = {
         // java.io.File.class
         {"/tmp/test1", "/tmp/subdir/../test2"},
         // java.net.URL.class
         {"http://www.jboss.org", "file:/path with space/tst.xml"},
         // java.net.URI.class
         {"http://www.jboss.org", "file:/path with space/tst.xml"},
         // java.lang.String.class
         {"JBoss, Home of Professional Open Source"},
         // java.lang.Class.class
         {"java.util.Arrays"},
         // InetAddress.class, localhost must be defined for this to work
         {"127.0.0.1", "localhost"},
         // String[].class
         {"1,2,3", "a,b,c", "", "#,%,\\,,.,_$,\\,v"},
         // Class[].class
         {"java.lang.Integer,java.lang.Float"},
         // int[].class
         {"0,#123,-123"},
         // Date.class
         {"Jan 4, 2005", "Tue Jan  4 23:38:21 PST 2005", "Tue, 04 Jan 2005 23:38:48 -0800"},
         // java.util.Properties.class
         {"prop1=value1\nprop2=value2\nprop3=value3\nprop32=${prop3}\nprop4=${user.home}\nprop5=${some.win32.path}"},
         {Locale.getDefault().toString(), "ja_JP"},
         {"-1", "0", "1"},
         {"-1", "0", "1"},
         {"true", "false"},
      };
      // The expected instance for each inputData value
      calendar.set(2005, 0, 4, 0, 0, 0);
      calendar.set(Calendar.MILLISECOND, 0);
      Date date1 = calendar.getTime();
      calendar.setTimeZone(TimeZone.getTimeZone("PST"));      
      calendar.set(2005, 0, 4, 23, 38, 21);
      Date date2 = calendar.getTime();
      calendar.set(2005, 0, 4, 23, 38, 48);
      Date date3 = calendar.getTime();
      Properties props = new Properties();
      props.setProperty("prop1", "value1");
      props.setProperty("prop2", "value2");
      props.setProperty("prop3", "value3");
      props.setProperty("prop32", "value3");
      props.setProperty("prop4", System.getProperty("user.home"));
      System.setProperty("some.win32.path", "C:\\disk1\\root\\");
      props.setProperty("prop5", "C:\\disk1\\root\\");
      
      Object[][] expectedData = {
         {new File("/tmp/test1").getCanonicalFile(), new File("/tmp/test2").getCanonicalFile()},
         {new URL("http://www.jboss.org"), new File("/path with space/tst.xml").getCanonicalFile().toURI().toURL()},
         {new URI("http://www.jboss.org"), new File("/path with space/tst.xml").getCanonicalFile().toURI()},
         {new String("JBoss, Home of Professional Open Source")},
         {java.util.Arrays.class},
         {InetAddress.getByName("127.0.0.1"), InetAddress.getByName("localhost")},
         {new String[]{"1", "2", "3"}, new String[] {"a", "b", "c"},
            new String[]{}, new String[]{"#","%",",",".","_$", ",v"}},
         {new Class[]{Integer.class, Float.class}},
         {new int[]{0, 0x123, -123}},
         {date1, date2, date3},
         {props},
         {Locale.getDefault(), Locale.JAPAN},
         {new AtomicInteger(-1), new AtomicInteger(0), new AtomicInteger(1)},
         {new AtomicLong(-1), new AtomicLong(0), new AtomicLong(1)},
         {new AtomicBoolean(true), new AtomicBoolean(false)},
      };
      // The expected string output from getAsText()
      String[][] expectedStringData = {
         // java.io.File.class
         {"/tmp/test1", "/tmp/subdir/../test2"},
         // java.net.URL.class
         {"http://www.jboss.org", "file:/path with space/tst.xml"},
         // java.net.URI.class
         {"http://www.jboss.org", "file:/path with space/tst.xml"},
         // java.lang.String.class
         {"JBoss, Home of Professional Open Source"},
         // java.lang.Class.class
         {"java.util.Arrays"},
         // InetAddress.class, localhost must be defined for this to work
         {"127.0.0.1", "localhost"},
         // String[].class
         {"1,2,3", "a,b,c", "", "#,%,\\,,.,_$,,v"},
         // Class[].class
         {"java.lang.Integer,java.lang.Float"},
         // int[].class
         {"0,291,-123"},
         // Date.class
         {"Jan 4, 2005", "Tue Jan  4 23:38:21 PST 2005", "Tue, 04 Jan 2005 23:38:48 -0800"},
         // java.util.Properties.class
         {props.toString()},
         {Locale.getDefault().toString(), Locale.JAPAN.toString()},
         {"-1", "0", "1"},
         {"-1", "0", "1"},
         {"true", "false"},
      };
      // The Comparator for non-trival types
      Comparator[] comparators = {
         null, // File
         null, // URL
         null, // URI
         null, // String
         null, // Class
         null, // InetAddress
         new StringArrayComparator(), // String[]
         new ClassArrayComparator(), // Class[]
         new IntArrayComparator(), // int[]
         null, // Date
         null, // Properties
         null, // Locale
         new NumberComparator(),
         new NumberComparator(),
         new ToStringComparator(),
      };

      doTests(types, inputData, expectedData, expectedStringData, comparators);
   }
   
   public void testDateEditor() throws Exception
   {
      log.debug("+++ testDateEditor");
      
      Locale locale = Locale.getDefault();
      
      try
      {
         // Use the default locale
         log.debug("Current Locale: " + Locale.getDefault());
      
         // An important date
         String text = "Fri, 25 Jun 1971 00:30:00 +0200";
         DateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
         Date date = format.parse(text);      
         
         PropertyEditor editor = new DateEditor();
         editor.setAsText(text);
         log.debug("setAsText('" + text + "') --> getValue() = '" + editor.getValue() + "'");
         assertTrue("Compare date1: " + date + ", date2: " + editor.getValue(),
               date.compareTo((Date)editor.getValue()) == 0);
         
         editor.setValue(date);
         log.debug("setValue('" + date + "') --> getAsText() - '" + editor.getAsText() + "'");
         Date date2 = format.parse(editor.getAsText());
         assertTrue("Compare date1: " + date + ", date2: " + date2, date.compareTo(date2) == 0);
         
         // Try in French
         Locale.setDefault(Locale.FRENCH);
         log.debug("Current Locale: " + Locale.getDefault());
         DateEditor.initialize();
         
         // An important date
         text = "ven., 25 juin 1971 00:30:00 +0200";
         format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
         date = format.parse(text);      
         
         editor = new DateEditor();
         editor.setAsText(text);
         log.debug("setAsText('" + text + "') --> getValue() = '" + editor.getValue() + "'");
         assertTrue("Compare date1: " + date + ", date2: " + editor.getValue(), date.compareTo((Date)editor.getValue()) == 0);
         
         editor.setValue(date);
         log.debug("setValue('" + date + "') --> getAsText() = '" + editor.getAsText() + "'");
         date2 = format.parse(editor.getAsText());
         assertTrue("Compare date1: " + date + ", date2: " + date2, date.compareTo(date2) == 0);
      }
      finally
      {
         // reset locale
         Locale.setDefault(locale);
         DateEditor.initialize();
      }
   }

   /** 
    * Tests the DOM Document and Element editors.
    */
   public void testDocumentElementEditors()
   {
      log.debug("+++ testDocumentElementEditors");
      DocumentEditor de = new DocumentEditor();
      // Comments can appear outside of a document
      String s = "<!-- header comment --><doc name='whatever'/><!-- footer comment -->";
      log.debug("setAsText '" + s + "'");
      de.setAsText(s);
      log.debug("Parsed XML document:");
      log((Node)de.getValue(), "  ");
      log.debug("getAsText '" + de.getAsText() + "'");
      assertTrue("Document :\n" + de.getAsText(), de.getAsText().trim().endsWith(s));
      assertTrue(de.getValue() instanceof org.w3c.dom.Document);
      // Test whitespace preservation
      s = "<element>\n\n<e2/> testing\n\n</element>";
      de.setAsText(s);
      assertTrue("Document :\n" + de.getAsText() + "\nvs\n" + s, de.getAsText().trim().endsWith(s));

      ElementEditor ee = new ElementEditor();
      s = "<element>text</element>";
      ee.setAsText(s);
      assertEquals(s, ee.getAsText());
      assertTrue(ee.getValue() instanceof org.w3c.dom.Element);
   }
   
   private void doTests(Class[] types, String[][] inputData, Object[][] expectedData,
         String[][] expectedStringData, Comparator[] comparators)
   {
      for(int t = 0; t < types.length; t ++)
      {
         Class type = types[t];
         log.debug("Checking property editor for: "+type);
         PropertyEditor editor = PropertyEditorManager.findEditor(type);
         assertTrue("Found property editor for: "+type, editor != null);
         log.debug("Found property editor for: "+type+", editor="+editor.getClass().getName());
         assertEquals(editor+" input length", inputData[t].length, expectedData[t].length);
         for(int i = 0; i < inputData[t].length; i ++)
         {
            String input = inputData[t][i];
            editor.setAsText(input);
            Object expected = expectedData[t][i];
            Object output = editor.getValue();
            Comparator c = comparators[t];
            boolean equals = false;
            if (c == null)
            {
               equals = output != null ? output.equals(expected) : expected == null;
            }
            else
            {
               equals = c.compare(output, expected) == 0;
            }
            if( equals == false )
               System.err.println(output+" != "+input);
            assertTrue("Transform("+editor+") of "+input+" equals "+expected+", output="+output, equals);
               
            String expectedStringOutput = expectedStringData[t][i];
            String stringOutput = editor.getAsText();
            log.debug("setAsText '" + logString(input) + "'");
            log.debug("getAsText '" + logString(stringOutput) + "'");
            if( type != Properties.class )
            {
               // We can't meaningfully compare the PropertiesEditor string output
               String msg = "PropertyEditor: " + editor.getClass().getName() + ", getAsText() == expectedStringOutput '";
               assertEquals(msg, expectedStringOutput, stringOutput);
            }
         }
      }
   }
   
   /**
    * Log a Node hierarchy
    */
   private void log(Node node, String indent)
   {
      String name = node.getNodeName();
      String value = node.getNodeValue();
      log.debug(indent + "Name=" + name + ", Value=" + value);
      NodeList list = node.getChildNodes();
      for (int i = 0; i < list.getLength(); i++)
         log(list.item(i), indent + indent);
   }
   
   private static String logString(String s)
   {
      return s != null ? s : "<null>";
   }
}
