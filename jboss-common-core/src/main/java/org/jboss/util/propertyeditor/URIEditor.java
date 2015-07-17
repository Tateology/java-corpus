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
package org.jboss.util.propertyeditor;

import java.net.URISyntaxException;

import org.jboss.util.NestedRuntimeException;
import org.jboss.util.Strings;

/**
 * A property editor for {@link java.net.URI}.
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 */
public class URIEditor extends TextPropertyEditorSupport
{
   /**
    * Returns a URI for the input object converted to a string.
    *
    * @return a URI object
    *
    * @throws NestedRuntimeException   An MalformedURLException occured.
    */
   public Object getValue()
   {
      try
      {
         return Strings.toURI(getAsText());
      }
      catch (URISyntaxException e)
      {
         throw new NestedRuntimeException(e);
      }
   }
}
