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
package org.jboss.net.sockets;

import java.lang.reflect.Method;
import java.util.Map;
/**
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
@SuppressWarnings("unchecked")
public class RMIMultiSocketHandler implements RMIMultiSocket
{
   Object target;
   Map invokerMap;
   public RMIMultiSocketHandler(Object target, Map invokerMap)
   {
      this.target = target;
      this.invokerMap = invokerMap;
   }

   public Object invoke (long methodHash, Object[] args) throws Exception
   {
      Method method = (Method)invokerMap.get(new Long(methodHash));
      return method.invoke(target, args);
   }
}
