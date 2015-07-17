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
package org.jboss.test.util.test.xml.resolver;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;

import org.jboss.util.xml.JBossEntityResolver;
import org.xml.sax.InputSource;

import junit.framework.TestCase;


/**
 * A JBossEntityResolverUnitTestCase.
 * 
 * @author <a href="alex@jboss.com">Alexey Loubyansky</a>
 * @version $Revision: 1.1 $
 */
public class JBossEntityResolverUnitTestCase
   extends TestCase
{
   public JBossEntityResolverUnitTestCase(String arg0)
   {
      super(arg0);
   }

   /**
    * The spcial thing about the resolution of xsd:redefine is that
    * the parser passes the namespace of the redefining schema as publicId
    * and the schema location of the redefined schema as systemId. Now, if
    * the redefining schema's namespace has already been mapped
    * to a schema location of the redefining schema then schema location
    * argument is ignored and the redefining schema is returned instead of the
    * redefined schema.
    * 
    * @throws Exception
    */
   public void testResolveRedefine() throws Exception
   {
      String baseName = getRootName() + "_" + getName() + "_";
      String redefiningName = baseName + "redefining.xsd";
      InputStream redefiningStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(redefiningName);
      assertNotNull("Expected to find " + redefiningName + " in the classpath", redefiningStream);
      int redefiningSize = bytesTotal(redefiningStream);

      String redefinedName = baseName + "redefined.xsd";
      InputStream redefinedStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(redefinedName);
      assertNotNull("Expected to find " + redefinedName + " in the classpath", redefinedStream);
      int redefinedSize = bytesTotal(redefinedStream);
      
      assertTrue(redefiningSize != redefinedSize);

      JBossEntityResolver resolver = new JBossEntityResolver();
      resolver.registerLocalEntity("urn:jboss:xml:test", redefiningName);
      InputSource resolvedSource = resolver.resolveEntity("urn:jboss:xml:test", redefinedName);
      assertNotNull(resolvedSource);
      InputStream resolvedStream = resolvedSource.getByteStream();
      assertNotNull(resolvedStream);
      int resolvedSize = bytesTotal(resolvedStream);
      assertEquals("Schema sizes: redefined=" + redefinedSize + ", redefining=" + redefiningSize, redefinedSize, resolvedSize);
   }

   public void testSystemPropertyInSystemID()
      throws Exception
   {
      JBossEntityResolver resolver = new JBossEntityResolver();
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      URL tstConfig = loader.getResource("tst-config_5.xsd");
      assertNotNull(tstConfig);
      System.setProperty("tst.config_5.xsd", tstConfig.toExternalForm());
      InputSource resolvedSource = resolver.resolveEntity("urn:jboss:xml:test", "${tst.config_5.xsd}");
      assertNotNull(resolvedSource);
      InputStream resolvedStream = resolvedSource.getByteStream();
      assertNotNull(resolvedStream);
      int resolvedSize = bytesTotal(resolvedStream);
      assertEquals(324, resolvedSize);
   }

   public void testEmptyFilenameResolution()
      throws Exception
   {
      String rootDir = "file:///";
      URL rootUrl = new URL(rootDir);
      JBossEntityResolver resolver = new JBossEntityResolver();
      InputSource resolvedSource = resolver.resolveEntity(rootDir, rootDir);

      InputStream resolverStream = resolvedSource.getByteStream();
      InputStream rootDirStream = rootUrl.openStream();

      int resolverByte,rootDirByte;
      while((resolverByte = resolverStream.read()) != -1)
      {
         rootDirByte = rootDirStream.read();
         if(rootDirByte != resolverByte)
            assertTrue("Empty filename resolution failed. URL: " + rootUrl, false);
      }

      if(rootDirStream.read() != -1)
         assertTrue("Empty filename resolution failed. URL: " + rootUrl, false);
   }

   private int bytesTotal(InputStream redefinedStream) throws IOException
   {
      byte[] bytes = new byte[1024];
      int redefinedSize = 0;
      try
      {
         for(int i = 0; (i = redefinedStream.read(bytes)) > 0; redefinedSize += i);
      }
      finally
      {
         redefinedStream.close();
      }
      return redefinedSize;
   }

   protected String getRootName()
   {
      String longName = getClass().getName();
      int dot = longName.lastIndexOf('.');
      if (dot != -1)
         return longName.substring(dot + 1);
      return longName;
   }
}
