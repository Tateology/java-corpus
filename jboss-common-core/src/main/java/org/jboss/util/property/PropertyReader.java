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
package org.jboss.util.property;

import java.util.Map;

import java.io.IOException;

/**
 * Iterface used to allow a <tt>PropertyMap</tt> to read property definitions 
 * in an implementation independent fashion.
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
@SuppressWarnings("unchecked")
public interface PropertyReader
{
   /**
    * Read a map of properties from this input source.
    *
    * @return  Read properties map.
    *
    * @throws PropertyException    Failed to read properties.
    * @throws IOException          I/O error while reading properties.
    */
   Map readProperties() throws PropertyException, IOException;
}
