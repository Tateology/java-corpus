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
import java.lang.reflect.InvocationHandler;
import java.io.Serializable;
import java.util.Random;
import java.rmi.Remote;

/**
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public class RMIMultiSocketClient implements InvocationHandler, Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -945837789475428529L;
   protected Remote[] stubs;
   protected Random random;
   public RMIMultiSocketClient(Remote[] stubs)
   {
      this.stubs = stubs;
      random = new Random();
   }

   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      if (method.getName().equals("hashCode"))
      {
         return new Integer(stubs[0].hashCode());
      }
      if (method.getName().equals("equals"))
      {
         return new Boolean(stubs[0].equals(args[0]));
      }
      int i = random.nextInt(stubs.length);
      long hash = MethodHash.calculateHash(method);
      RMIMultiSocket target = (RMIMultiSocket)stubs[i];
      return target.invoke(hash, args);
   }   
}
