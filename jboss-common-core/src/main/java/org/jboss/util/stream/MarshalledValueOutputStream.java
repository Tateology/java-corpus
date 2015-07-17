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
package org.jboss.util.stream;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.rmi.Remote;
import java.rmi.server.RemoteObject;
import java.rmi.server.RemoteStub;

/**
 * An ObjectOutputStream subclass used by the MarshalledValue class to
 * ensure the classes and proxies are loaded using the thread context
 * class loader. Currently this does not do anything as neither class or
 * proxy annotations are used.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 37406 $
 */
public class MarshalledValueOutputStream
   extends ObjectOutputStream
{
   /** Creates a new instance of MarshalledValueOutputStream
    If there is a security manager installed, this method requires a
    SerializablePermission("enableSubstitution") permission to ensure it's
    ok to enable the stream to do replacement of objects in the stream.
    * @param os 
    * @throws IOException 
    */
   public MarshalledValueOutputStream(OutputStream os) throws IOException
   {
      super(os);
      enableReplaceObject(true);
   }

   /**
    * @throws java.io.IOException   Any exception thrown by the underlying OutputStream.
    */
   protected void annotateClass(Class<?> cl) throws IOException
   {
      super.annotateClass(cl);
   }
   
   /**
    * @throws java.io.IOException   Any exception thrown by the underlying OutputStream.
    */
   protected void annotateProxyClass(Class<?> cl) throws IOException
   {
      super.annotateProxyClass(cl);
   }

   /** Override replaceObject to check for Remote objects that are
    not RemoteStubs.
   */
   protected Object replaceObject(Object obj) throws IOException
   {
      if( (obj instanceof Remote) && !(obj instanceof RemoteStub) )
      {
         Remote remote = (Remote) obj;
         try
         {
            obj = RemoteObject.toStub(remote);
         }
         catch(IOException ignore)
         {
            // Let the Serialization layer try with the orignal obj
         }
      }
      return obj;
   }
}
