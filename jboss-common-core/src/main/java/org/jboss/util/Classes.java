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
package org.jboss.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Array;
import java.lang.NoSuchMethodException;
import java.net.URL;

import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.*;

/**
 * A collection of <code>Class</code> utilities.
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author  <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis<a/>
 */
@SuppressWarnings("unchecked")
public final class Classes
{
   /** The string used to separator packages */
   public static final String PACKAGE_SEPARATOR = ".";

   /** The characther used to separator packages */
   public static final char PACKAGE_SEPARATOR_CHAR = '.';

   /** The default package name. */
   public static final String DEFAULT_PACKAGE_NAME = "<default>";

   /** Format a string buffer containing the Class, Interfaces, CodeSource,
    and ClassLoader information for the given object clazz.

    @param clazz the Class
    @param results - the buffer to write the info to
    */
   public static void displayClassInfo(Class clazz, StringBuffer results)
   {
      // Print out some codebase info for the clazz
      ClassLoader cl = clazz.getClassLoader();
      results.append("\n");
      results.append(clazz.getName());
      results.append("(");
      results.append(Integer.toHexString(clazz.hashCode()));
      results.append(").ClassLoader=");
      results.append(cl);
      ClassLoader parent = cl;
      while( parent != null )
      {
         results.append("\n..");
         results.append(parent);
         URL[] urls = getClassLoaderURLs(parent);
         int length = urls != null ? urls.length : 0;
         for(int u = 0; u < length; u ++)
         {
            results.append("\n....");
            results.append(urls[u]);
         }
         if( parent != null )
            parent = parent.getParent();
      }
      CodeSource clazzCS = clazz.getProtectionDomain().getCodeSource();
      if( clazzCS != null )
      {
         results.append("\n++++CodeSource: ");
         results.append(clazzCS);
      }
      else
         results.append("\n++++Null CodeSource");

      results.append("\nImplemented Interfaces:");
      Class[] ifaces = clazz.getInterfaces();
      for(int i = 0; i < ifaces.length; i ++)
      {
         Class iface = ifaces[i];
         results.append("\n++");
         results.append(iface);
         results.append("(");
         results.append(Integer.toHexString(iface.hashCode()));
         results.append(")");
         ClassLoader loader = ifaces[i].getClassLoader();
         results.append("\n++++ClassLoader: ");
         results.append(loader);
         ProtectionDomain pd = ifaces[i].getProtectionDomain();
         CodeSource cs = pd.getCodeSource();
         if( cs != null )
         {
            results.append("\n++++CodeSource: ");
            results.append(cs);
         }
         else
            results.append("\n++++Null CodeSource");
      }
   }

   /** Use reflection to access a URL[] getURLs or URL[] getClasspath method so
    that non-URLClassLoader class loaders, or class loaders that override
    getURLs to return null or empty, can provide the true classpath info.
    * @param cl 
    * @return the urls
    */
   public static URL[] getClassLoaderURLs(ClassLoader cl)
   {
      URL[] urls = {};
      try
      {
         Class returnType = urls.getClass();
         Class[] parameterTypes = {};
         Class clClass = cl.getClass();
         Method getURLs = clClass.getMethod("getURLs", parameterTypes);
         if( returnType.isAssignableFrom(getURLs.getReturnType()) )
         {
            Object[] args = {};
            urls = (URL[]) getURLs.invoke(cl, args);
         }
         if( urls == null || urls.length == 0 )
         {
            Method getCp = clClass.getMethod("getClasspath", parameterTypes);
            if( returnType.isAssignableFrom(getCp.getReturnType()) )
            {
               Object[] args = {};
               urls = (URL[]) getCp.invoke(cl, args);
            }
         }
      }
      catch(Exception ignore)
      {
      }
      return urls;
   }

   /**
    * Describe the class of an object
    *
    * @param object the object
    * @return the description
    */
   public static String getDescription(Object object)
   {
      StringBuffer buffer = new StringBuffer();
      describe(buffer, object);
      return buffer.toString();
   }

   /**
    * Describe the class of an object
    *
    * @param buffer the string buffer 
    * @param object the object
    */
   public static void describe(StringBuffer buffer, Object object)
   {
      if (object == null)
         buffer.append("**null**");
      else
         describe(buffer, object.getClass());
   }

   /**
    * Describe the class
    *
    * @param buffer the string buffer 
    * @param clazz the clazz
    */
   public static void describe(StringBuffer buffer, Class clazz)
   {
      if (clazz == null)
         buffer.append("**null**");
      else
      {
         buffer.append("{class=").append(clazz.getName());
         Class[] intfs = clazz.getInterfaces();
         if (intfs.length > 0)
         {
            buffer.append(" intfs=");
            for (int i = 0; i < intfs.length; ++i)
            {
               buffer.append(intfs[i].getName());
               if (i < intfs.length-1)
                  buffer.append(", ");
            }
         }
         buffer.append("}");
      }
   }

   /**
    * Get the short name of the specified class by striping off the package
    * name.
    *
    * @param classname  Class name.
    * @return           Short class name.
    */
   public static String stripPackageName(final String classname)
   {
      int idx = classname.lastIndexOf(PACKAGE_SEPARATOR);

      if (idx != -1)
         return classname.substring(idx + 1, classname.length());
      return classname;
   }

   /**
    * Get the short name of the specified class by striping off the package
    * name.
    *
    * @param type    Class name.
    * @return        Short class name.
    */
   public static String stripPackageName(final Class type)
   {
      return stripPackageName(type.getName());
   }

   /**
    * Get the package name of the specified class.
    *
    * @param classname  Class name.
    * @return           Package name or "" if the classname is in the
    *                   <i>default</i> package.
    *
    * @throws EmptyStringException     Classname is an empty string.
    */
   public static String getPackageName(final String classname)
   {
      if (classname.length() == 0)
         throw new EmptyStringException();

      int index = classname.lastIndexOf(PACKAGE_SEPARATOR);
      if (index != -1)
         return classname.substring(0, index);
      return "";
   }

   /**
    * Get the package name of the specified class.
    *
    * @param type    Class.
    * @return        Package name.
    */
   public static String getPackageName(final Class type)
   {
      return getPackageName(type.getName());
   }

   /**
    * Force the given class to be loaded fully.
    *
    * <p>This method attempts to locate a static method on the given class
    *    the attempts to invoke it with dummy arguments in the hope that
    *    the virtual machine will prepare the class for the method call and
    *    call all of the static class initializers.
    *
    * @param type    Class to force load.
    *
    * @throws NullArgumentException    Type is <i>null</i>.
    */
   public static void forceLoad(final Class type)
   {
      if (type == null)
         throw new NullArgumentException("type");

      // don't attempt to force primitives to load
      if (type.isPrimitive()) return;

      // don't attempt to force java.* classes to load
      String packageName = Classes.getPackageName(type);
      // System.out.println("package name: " + packageName);

      if (packageName.startsWith("java.") ||
         packageName.startsWith("javax."))
      {
         return;
      }

      // System.out.println("forcing class to load: " + type);

      try
      {
         Method methods[] = type.getDeclaredMethods();
         Method method = null;
         for (int i = 0; i < methods.length; i++)
         {
            int modifiers = methods[i].getModifiers();
            if (Modifier.isStatic(modifiers))
            {
               method = methods[i];
               break;
            }
         }

         if (method != null)
         {
            method.invoke(null, (Object[]) null);
         }
         else
         {
            type.newInstance();
         }
      }
      catch (Exception ignore)
      {
         ThrowableHandler.add(ignore);
      }
   }


   /////////////////////////////////////////////////////////////////////////
   //                               Primitives                            //
   /////////////////////////////////////////////////////////////////////////

   /** Primitive type name -> class map. */
   private static final Map PRIMITIVE_NAME_TYPE_MAP = new HashMap();

   /** Setup the primitives map. */
   static
   {
      PRIMITIVE_NAME_TYPE_MAP.put("boolean", Boolean.TYPE);
      PRIMITIVE_NAME_TYPE_MAP.put("byte", Byte.TYPE);
      PRIMITIVE_NAME_TYPE_MAP.put("char", Character.TYPE);
      PRIMITIVE_NAME_TYPE_MAP.put("short", Short.TYPE);
      PRIMITIVE_NAME_TYPE_MAP.put("int", Integer.TYPE);
      PRIMITIVE_NAME_TYPE_MAP.put("long", Long.TYPE);
      PRIMITIVE_NAME_TYPE_MAP.put("float", Float.TYPE);
      PRIMITIVE_NAME_TYPE_MAP.put("double", Double.TYPE);
   }

   /**
    * Get the primitive type for the given primitive name.
    *
    * <p>
    * For example, "boolean" returns Boolean.TYPE and so on...
    *
    * @param name    Primitive type name (boolean, int, byte, ...)
    * @return        Primitive type or null.
    *
    * @exception IllegalArgumentException    Type is not a primitive class
    */
   public static Class getPrimitiveTypeForName(final String name)
   {
      return (Class) PRIMITIVE_NAME_TYPE_MAP.get(name);
   }

   /** Map of primitive types to their wrapper classes */
   private static final Class[] PRIMITIVE_WRAPPER_MAP = {
      Boolean.TYPE, Boolean.class,
      Byte.TYPE, Byte.class,
      Character.TYPE, Character.class,
      Double.TYPE, Double.class,
      Float.TYPE, Float.class,
      Integer.TYPE, Integer.class,
      Long.TYPE, Long.class,
      Short.TYPE, Short.class,
   };

   /**
    * Get the wrapper class for the given primitive type.
    *
    * @param type    Primitive class.
    * @return        Wrapper class for primitive.
    *
    * @exception IllegalArgumentException    Type is not a primitive class
    */
   public static Class getPrimitiveWrapper(final Class type)
   {
      if (!type.isPrimitive())
      {
         throw new IllegalArgumentException("type is not a primitive class");
      }

      for (int i = 0; i < PRIMITIVE_WRAPPER_MAP.length; i += 2)
      {
         if (type.equals(PRIMITIVE_WRAPPER_MAP[i]))
            return PRIMITIVE_WRAPPER_MAP[i + 1];
      }

      // should never get here, if we do then PRIMITIVE_WRAPPER_MAP
      // needs to be updated to include the missing mapping
      throw new UnreachableStatementException();
   }

   /**
    * Populates a list with all the interfaces implemented by the argument
    * class c and all its superclasses.
    * 
    * @param allIfaces - the list to populate with the interfaces 
    * @param c - the class to start scanning for interfaces
    */
   public static void getAllInterfaces(List allIfaces, Class c)
   {
      while (c != null)
      {
         Class[] ifaces = c.getInterfaces();
         for (int n = 0; n < ifaces.length; n ++)
         {
            allIfaces.add(ifaces[n]);
         }
         c = c.getSuperclass();
      }
   }

   /**
    * Returns an array containing all the unique interfaces implemented
    * by the argument class c and all its superclasses. Interfaces that
    * appear multiple times through inheritence are only accounted for once.
    * 
    * @param c - the class to start scanning for interfaces
    * @return the interfaces
    */   
   public static Class[] getAllUniqueInterfaces(Class c)
   {
      Set uniqueIfaces = new HashSet();
      while (c != null )
      {
         Class[] ifaces = c.getInterfaces();
         for (int n = 0; n < ifaces.length; n ++)
         {
            uniqueIfaces.add(ifaces[n]);
         }
         c = c.getSuperclass();
      }
      return (Class[])uniqueIfaces.toArray(new Class[uniqueIfaces.size()]);
   }

   /**
    * Check if the given class is a primitive wrapper class.
    *
    * @param type    Class to check.
    * @return        True if the class is a primitive wrapper.
    */
   public static boolean isPrimitiveWrapper(final Class type)
   {
      for (int i = 0; i < PRIMITIVE_WRAPPER_MAP.length; i += 2)
      {
         if (type.equals(PRIMITIVE_WRAPPER_MAP[i + 1]))
         {
            return true;
         }
      }

      return false;
   }

   /**
    * Check if the given class is a primitive class or a primitive 
    * wrapper class.
    *
    * @param type    Class to check.
    * @return        True if the class is a primitive or primitive wrapper.
    */
   public static boolean isPrimitive(final Class type)
   {
      if (type.isPrimitive() || isPrimitiveWrapper(type))
      {
         return true;
      }

      return false;
   }
   /** Check type against boolean, byte, char, short, int, long, float, double.
    * @param type  The java type name
    * @return true if this is a primative type name.
    */
   public static boolean isPrimitive(final String type)
   {
      return PRIMITIVE_NAME_TYPE_MAP.containsKey(type);
   }

   /**
    * @param wrapper  a primitive wrapper type
    * @return  primitive type the passed in wrapper type corresponds to
    */
   public static Class getPrimitive(Class wrapper)
   {
      Class primitive;
      if(Integer.class == wrapper)
      {
         primitive = int.class;
      }
      else if(Long.class == wrapper)
      {
         primitive = long.class;
      }
      else if(Double.class == wrapper)
      {
         primitive = double.class;
      }
      else if(Boolean.class == wrapper)
      {
         primitive = boolean.class;
      }
      else if(Short.class == wrapper)
      {
         primitive = short.class;
      }
      else if(Float.class == wrapper)
      {
         primitive = float.class;
      }
      else if(Byte.class == wrapper)
      {
         primitive = byte.class;
      }
      else if(Character.class == wrapper)
      {
         primitive = char.class;
      }
      else
      {
         throw new IllegalArgumentException("The class is not a primitive wrapper type: " + wrapper);
      }
      return primitive;
   }

   /**
    * Instantiate a java class object
    * 
    * @param expected the expected class type
    * @param property the system property defining the class
    * @param defaultClassName the default class name
    * @return the instantiated object
    */
   public static Object instantiate(Class expected, String property, String defaultClassName)
   {
      String className = getProperty(property, defaultClassName);
      Class clazz = null;
      try
      {
         clazz = loadClass(className);
      }
      catch (ClassNotFoundException e)
      {
         throw new NestedRuntimeException("Cannot load class " + className, e);
      }
      Object result = null;
      try
      {
         result = clazz.newInstance();
      }
      catch (InstantiationException e)
      {
         throw new NestedRuntimeException("Error instantiating " + className, e);
      }
      catch (IllegalAccessException e)
      {
         throw new NestedRuntimeException("Error instantiating " + className, e);
      }
      if (expected.isAssignableFrom(clazz) == false)
         throw new NestedRuntimeException("Class " + className + " from classloader " + clazz.getClassLoader() +
            " is not of the expected class " + expected + " loaded from " + expected.getClassLoader());
      return result;
   }

   /////////////////////////////////////////////////////////////////////////
   //                            Class Loading                            //
   /////////////////////////////////////////////////////////////////////////

   /**
    * This method acts equivalently to invoking
    * <code>Thread.currentThread().getContextClassLoader().loadClass(className);</code> but it also
    * supports primitive types and array classes of object types or primitive types.
    *
    * @param className    the qualified name of the class or the name of primitive type or
    *                     array in the same format as returned by the
    *                     <code>java.lang.Class.getName()</code> method.
    * @return             the Class object for the requested className
    *
    * @throws ClassNotFoundException when the <code>classLoader</code> can not find the requested class
    */
   public static Class loadClass(String className) throws ClassNotFoundException
   {
      return loadClass(className, Thread.currentThread().getContextClassLoader());
   }

   /**
    * This method acts equivalently to invoking classLoader.loadClass(className)
    * but it also supports primitive types and array classes of object types or
    * primitive types.
    *
    * @param className the qualified name of the class or the name of primitive
    * type or array in the same format as returned by the
    * java.lang.Class.getName() method.
    * @param classLoader  the ClassLoader used to load classes
    * @return             the Class object for the requested className
    *
    * @throws ClassNotFoundException when the <code>classLoader</code> can not
    * find the requested class
    */
   public static Class loadClass(String className, ClassLoader classLoader)
      throws ClassNotFoundException
   {
      // ClassLoader.loadClass() does not handle primitive types:
      //
      //   B            byte
      //   C            char
      //   D            double
      //   F            float
      //   I            int
      //   J            long
      //   S            short
      //   Z            boolean
      //   V	         void
      //
      if (className.length() == 1)
      {
         char type = className.charAt(0);
         if (type == 'B') return Byte.TYPE;
         if (type == 'C') return Character.TYPE;
         if (type == 'D') return Double.TYPE;
         if (type == 'F') return Float.TYPE;
         if (type == 'I') return Integer.TYPE;
         if (type == 'J') return Long.TYPE;
         if (type == 'S') return Short.TYPE;
         if (type == 'Z') return Boolean.TYPE;
         if (type == 'V') return Void.TYPE;
         // else throw...
         throw new ClassNotFoundException(className);
      }

      // Check for a primative type
      if( isPrimitive(className) == true )
         return (Class) Classes.PRIMITIVE_NAME_TYPE_MAP.get(className);

      // Check for the internal vm format: Lclassname;
      if (className.charAt(0) == 'L' && className.charAt(className.length() - 1) == ';')
         return classLoader.loadClass(className.substring(1, className.length() - 1));

      // first try - be optimistic
      // this will succeed for all non-array classes and array classes that have already been resolved
      //
      try
      {
         return classLoader.loadClass(className);
      }
      catch (ClassNotFoundException e)
      {
         // if it was non-array class then throw it
         if (className.charAt(0) != '[')
            throw e;
      }

      // we are now resolving array class for the first time

      // count opening braces
      int arrayDimension = 0;
      while (className.charAt(arrayDimension) == '[')
         arrayDimension++;

      // resolve component type - use recursion so that we can resolve primitive types also
      Class componentType = loadClass(className.substring(arrayDimension), classLoader);

      // construct array class
      return Array.newInstance(componentType, new int[arrayDimension]).getClass();
   }

   /**
    * Convert a list of Strings from an Interator into an array of
    * Classes (the Strings are taken as classnames).
    *
    * @param it A java.util.Iterator pointing to a Collection of Strings
    * @param cl The ClassLoader to use
    *
    * @return Array of Classes
    *
    * @throws ClassNotFoundException When a class could not be loaded from
    *         the specified ClassLoader
    */
   public final static Class<?>[] convertToJavaClasses(Iterator<String> it,
                                                    ClassLoader cl)
      throws ClassNotFoundException
   {
      ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
      while (it.hasNext())
      {
         classes.add(convertToJavaClass(it.next(), cl));
      }
      return classes.toArray(new Class[classes.size()]);
   }

   /**
    * Returns attribute's getter method. If the method not found then NoSuchMethodException will be thrown.
    * @param cls  the class the attribute belongs too
    * @param attr  the attribute's name
    * @return  attribute's getter method
    * @throws NoSuchMethodException  if the getter was not found
    */
   public final static Method getAttributeGetter(Class cls, String attr) throws NoSuchMethodException
   {
      StringBuffer buf = new StringBuffer(attr.length() + 3);
      buf.append("get");
      if(Character.isLowerCase(attr.charAt(0)))
      {
         buf.append(Character.toUpperCase(attr.charAt(0)))
            .append(attr.substring(1));
      }
      else
      {
         buf.append(attr);
      }

      try
      {
         return cls.getMethod(buf.toString(), (Class[]) null);
      }
      catch (NoSuchMethodException e)
      {
         buf.replace(0, 3, "is");
         return cls.getMethod(buf.toString(), (Class[]) null);
      }
   }

   /**
    * Returns attribute's setter method. If the method not found then NoSuchMethodException will be thrown.
    * @param cls   the class the attribute belongs to
    * @param attr  the attribute's name
    * @param type  the attribute's type
    * @return  attribute's setter method
    * @throws NoSuchMethodException  if the setter was not found
    */
   public final static Method getAttributeSetter(Class cls, String attr, Class type) throws NoSuchMethodException
   {
      StringBuffer buf = new StringBuffer(attr.length() + 3);
      buf.append("set");
      if(Character.isLowerCase(attr.charAt(0)))
      {
         buf.append(Character.toUpperCase(attr.charAt(0)))
            .append(attr.substring(1));
      }
      else
      {
         buf.append(attr);
      }

      return cls.getMethod(buf.toString(), new Class[]{type});
   }

   /**
    * Convert a given String into the appropriate Class.
    *
    * @param name Name of class
    * @param cl ClassLoader to use
    *
    * @return The class for the given name
    *
    * @throws ClassNotFoundException When the class could not be found by
    *         the specified ClassLoader
    */
   private final static Class convertToJavaClass(String name,
                                                 ClassLoader cl)
      throws ClassNotFoundException
   {
      int arraySize = 0;
      while (name.endsWith("[]"))
      {
         name = name.substring(0, name.length() - 2);
         arraySize++;
      }

      // Check for a primitive type
      Class c = (Class) PRIMITIVE_NAME_TYPE_MAP.get(name);

      if (c == null)
      {
         // No primitive, try to load it from the given ClassLoader
         try
         {
            c = cl.loadClass(name);
         }
         catch (ClassNotFoundException cnfe)
         {
            throw new ClassNotFoundException("Parameter class not found: " +
               name);
         }
      }

      // if we have an array get the array class
      if (arraySize > 0)
      {
         int[] dims = new int[arraySize];
         for (int i = 0; i < arraySize; i++)
         {
            dims[i] = 1;
         }
         c = Array.newInstance(c, dims).getClass();
      }

      return c;
   }

   /**
    * Get a system property
    * 
    * @param name the property name
    * @param defaultValue the default value
    */
   private static String getProperty(final String name, final String defaultValue)
   {
      return (String) AccessController.doPrivileged(
      new PrivilegedAction()
      {
         public Object run()
         {
            return System.getProperty(name, defaultValue);
         }

      });
   }
}
