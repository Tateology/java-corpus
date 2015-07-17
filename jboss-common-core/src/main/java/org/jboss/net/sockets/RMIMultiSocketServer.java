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

import java.rmi.server.UnicastRemoteObject;
import java.rmi.Remote;
import java.lang.reflect.Proxy;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.RemoteException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.rmi.NoSuchObjectException;
/**
 *
 * @author bill@jboss.org
 * @version $Revision$
 */
@SuppressWarnings("unchecked")
public class RMIMultiSocketServer
{
   private static HashMap handlermap = new HashMap();
   private static HashMap stubmap = new HashMap();

   public static Remote exportObject(Remote obj,
                                     int port,
                                     RMIClientSocketFactory csf, 
                                     RMIServerSocketFactory ssf,
                                     Class[] interfaces,
                                     int numSockets)
      throws RemoteException
   {
      Remote[] stubs = new Remote[numSockets];

      Method[] methods = obj.getClass().getMethods();
      
      HashMap invokerMap = new HashMap();
      for (int i = 0; i < methods.length; i++) {
         Long methodkey = new Long(MethodHash.calculateHash(methods[i]));
         invokerMap.put(methodkey, methods[i]);
      }
      
      RMIMultiSocketHandler[] handlers = new RMIMultiSocketHandler[numSockets];
      for (int i = 0; i < numSockets; i++)
      {
         int theport = (port == 0) ? 0 : port + i;
         handlers[i] = new RMIMultiSocketHandler(obj, invokerMap);
         stubs[i] = UnicastRemoteObject.exportObject(handlers[i], theport, csf, ssf);
      }

      Remote remote = (Remote)Proxy.newProxyInstance(
         obj.getClass().getClassLoader(),
         interfaces,
         new RMIMultiSocketClient(stubs));
      stubmap.put(remote, stubs);
      handlermap.put(remote, handlers);
      return remote;
   }

   public static Remote exportObject(Remote obj,
                                     int port,
                                     RMIClientSocketFactory csf, 
                                     RMIServerSocketFactory ssf,
                                     int numSockets)
      throws RemoteException
   {
      return exportObject(obj, port, csf, ssf, obj.getClass().getInterfaces(), numSockets);
   }

   public static boolean unexportObject(Remote obj, boolean force)
      throws NoSuchObjectException
   {
      handlermap.remove(obj);
      Remote[] stubs = (Remote[])stubmap.remove(obj);
      for (int i = 0; i < stubs.length; i++)
      {
         UnicastRemoteObject.unexportObject(stubs[i], force);
      }
      
      return true;
   }
}
