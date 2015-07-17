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

/**
 * An abstract adapter class for receiving bound property events.
 *
 * <p>Methods defined in this class are empty.  This class exists as
 *    as convenience for creating listener objects.
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public abstract class BoundPropertyAdapter
   extends PropertyAdapter
   implements BoundPropertyListener
{
   /**
    * Notifies that this listener was bound to a property.
    *
    * @param map     PropertyMap which contains property bound to.
    */
   public void propertyBound(final PropertyMap map) {}

   /**
    * Notifies that this listener was unbound from a property.
    *
    * @param map     PropertyMap which contains property bound to.
    */
   public void propertyUnbound(final PropertyMap map) {}
}
