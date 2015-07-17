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
 * The listener interface for receiving bound property events (as well as
 * property events).
 *
 * <p>Classes that are interested in processing a bound property event 
 *    implement this interface, and register instance objects with a given
 *    {@link PropertyMap} or via
 *    {@link PropertyManager#addPropertyListener(PropertyListener)}.
 *
 * <p>Note that this is not the typical listener interface, as it extends
 *    from {@link PropertyListener}, and defines {@link #getPropertyName()}
 *    which is not an event triggered method.  This method serves to instruct
 *    the {@link PropertyMap} the listener is registered with, which property
 *    it will bind to.
 *
 * @version <tt>$Revision$</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public interface BoundPropertyListener
   extends PropertyListener
{
   /**
    * Get the property name which this listener is bound to.
    *
    * @return  Property name.
    */
   String getPropertyName();

   /**
    * Notifies that this listener was bound to a property.
    *
    * @param map     <tt>PropertyMap</tt> which contains property bound to.
    */
   void propertyBound(PropertyMap map);

   /**
    * Notifies that this listener was unbound from a property.
    *
    * @param map     <tt>PropertyMap</tt> which contains property bound to.
    */
   void propertyUnbound(PropertyMap map);
}
