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

/**
 * A simple interface for objects that can return pretty (ie.
 * prefixed) string representations of themselves.
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public interface PrettyString
{
   /**
    * Returns a pretty representation of the object.
    *
    * @param prefix  The string which all lines of the output must be prefixed with.
    * @return        A pretty representation of the object.
    */
   String toPrettyString(String prefix);

   /**
    * Interface for appending the objects pretty string onto a buffer.
    */
   interface Appendable
   {
      /**
       * Appends a pretty representation of the object to the given buffer.
       *
       * @param buff    The buffer to use while making pretty.
       * @param prefix  The string which all lines of the output must be prefixed with.
       * @return        The buffer.
       */
      StringBuffer appendPrettyString(StringBuffer buff, String prefix);
   }
}
