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
package org.jboss.util.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.jboss.logging.Logger;
import org.jboss.util.xml.catalog.CatalogManager;
import org.jboss.util.xml.catalog.Resolver;
import org.xml.sax.InputSource;

/**
 * 
 * A ThreadSpecificCatalogs class maintains all catalogfiles <code>catolog.xml</code> found in the
 * <code>Thread.currentThread().getContextClassLoader()</code>.  
 * 
 * @author <a href="wiesed@gmail.com">Daniel Wiese</a>
 * @version $Revision$
 */
public class CatalogLocation
{

   private static Logger log = Logger.getLogger(CatalogLocation.class);

   /** 
    * The catalog is assembled by taking into account all accessible resources whose name is 
    * META-INF/jax-ws-catalog.xml. Each resource MUST be a valid entity catalog according to the XML Catalogs
    */
   private static final String[] catalogFilesNames =
   {"META-INF/jax-ws-catalog.xml", "WEB-INF/jax-ws-catalog.xml", "jax-ws-catalog.xml"};

   private final Resolver catologResolver;
   
   private final URL location;

   private boolean isLastEntityResolved = false;

   static
   {
      // If the source document contains "oasis-xml-catalog" processing instructions,
      // should they be used?
      System.setProperty("xml.catalog.allowPI", "true");
      //Which identifier is preferred, "public" or "system"?
      System.setProperty("xml.catalog.prefer", "public");
      //If non-zero, the Catalog classes will print informative and debugging messages.
      //The higher the number, the more messages.
      System.setProperty("xml.catalog.verbosity", "0");
   }

   /**
    * Create a new CatalogLocation.
    * @param url - the location of the catalog xml file
    * @throws IOException if the catalog files cannot be loaded
    */
   public CatalogLocation(URL url) throws IOException
   {
      catologResolver = new Resolver();
      catologResolver.setCatalogManager(CatalogManager.getStaticManager());
      catologResolver.setupReaders();
      catologResolver.parseCatalog(url);
      this.location=url;
   }

   /**
    * Tries to resolve the entity using the thread specific catolog resolvers
    * 
    * @param publicId - Public ID of DTD, or null if it is a schema
    * @param systemId - the system ID of DTD or Schema
    * @return InputSource of entity
    * @throws MalformedURLException - if the url is wrong
    * @throws IOException - error reading the local file
    */
   public InputSource resolveEntity(String publicId, String systemId) throws MalformedURLException, IOException
   {
      String resolvedURI = catologResolver.resolveSystem(systemId);

      if (resolvedURI == null)
      {
         resolvedURI = catologResolver.resolvePublic(publicId, systemId);
      }

      if (resolvedURI != null)
      {
         final InputSource is = new InputSource();
         is.setPublicId(publicId);
         is.setSystemId(systemId);
         is.setByteStream(this.loadResource(resolvedURI));
         this.isLastEntityResolved = true;
         return is;
      }
      else
      {
         //resource could�t be resloved
         this.isLastEntityResolved = false;
         return null;
      }
   }

   /**
    * Seach the path for oasis catalog files. The classpath of
    * <code>Thread.currentThread().getContextClassLoader()</code>
    * is used for the lookup.
    * @return the url where the <code>jax-ws-catalog.xml</code> is located
    * @throws IOException if the catalog files cannot be loaded
    *
    */
   public static URL lookupCatalogFiles() throws IOException
   {
      URL url = null;
      //JAXWS-2.-0 spec, Line 27:the current context class loader MUST be used to
      //retrieve all the resources with the specified name
      ClassLoader loader = Thread.currentThread().getContextClassLoader();

      for (int i = 0; i < catalogFilesNames.length; i++)
      {
         url = loader.getResource(catalogFilesNames[i]);
         //use the first hit
         if (url != null)
         {
            break;
         }
      }

      return url;
   }

   /**
    * Returns the boolean value to inform id DTD was found in the XML file or not
    *
    *
    * @return boolean - true if DTD was found in XML
    */
   public boolean isEntityResolved()
   {
      return this.isLastEntityResolved;
   }

   /**
    * 
    * Loads the resolved resource.
    * 
    * @param resolvedURI - the full qualified URI of the resoved local ressource
    * @return - the inputstram represnting this resource
    * @throws IOException - if the resource cannot be opened
    */
   private InputStream loadResource(String resolvedURI) throws IOException
   {
      try
      {
         final URI toLoad = new URI(resolvedURI);
         InputStream inputStream = null;
         if (toLoad != null)
         {
            try
            {
               inputStream = new FileInputStream(new File(toLoad));
            }
            catch (IOException e)
            {
               log.error("Failed to open url stream", e);
               throw e;
            }
         }
         return inputStream;
      }
      catch (URISyntaxException e)
      {
         log.error("The URI (" + resolvedURI + ") is malfomed");
         throw new IOException("The URI (" + resolvedURI + ") is malfomed");
      }
   }

   /**
    * To catalog locations are qual if the location is equal.
    * @param other - the catlog location to compare
    * @return true if equal
    */
   public boolean equals(Object other)
   {
     boolean back=false;
      if (other!=null && other instanceof CatalogLocation){
         final CatalogLocation otherC=(CatalogLocation)other;
         back=this.location.equals(otherC.location);
      }
      
      return back;
   }

   /**
    * Two catalog locations have the same hash code if the location is equal.
    * @return - the hash code
    */
   public int hashCode()
   {
      return this.location.hashCode();
   }
   
   
}
