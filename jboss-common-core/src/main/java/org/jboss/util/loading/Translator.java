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
package org.jboss.util.loading;

import java.security.ProtectionDomain;

/** An interface for transforming byte code before Class creation. This is
 * compatible with the JDK1.5 java.lang.instrument.ClassFileTransformer
 * proposal.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public interface Translator
{
   /** Optionally transform the supplied class file and return a new replacement
    * class file.
    *
    * <P> If a transformer has been registered with the class loading layer,
    * the transformer will be called for every new class definition.
    * The request for a new class definition is made with defineClass
    * The transformer is called during the processing of the request, before
    * the class file bytes have been verified or applied.
    *
    * <P>
    * If the implementing method determines that no transformations are needed,
    * it should return <code>null</code>. Otherwise, it should create a new
    * byte[] array and copy the input <code>classfileBuffer</code> into it,
    * along with all desired transformations. The input <code>classfileBuffer</code>
    * must not be modified.
    *
    * @param loader - the defining loader of the class to be transformed, may
    *    be <code>null</code> if the bootstrap loader
    * @param className - the fully-qualified name of the class
    * @param classBeingRedefined - if this is a redefine, the class being
    *    redefined, otherwise <code>null</code>
    * @param protectionDomain - the protection domain of the class being
    *    defined or redefined
    * @param classfileBuffer - the input byte buffer in class file format - must
    *    not be modified
    *
    * @throws Exception - if the input does not represent a well-formed class file
    * @return a well-formed class file buffer (the result of the transform), 
    * or <code>null</code> if no transform is performed.
    */
   public byte[] transform(ClassLoader loader,
      String className,
      Class<?> classBeingRedefined,
      ProtectionDomain protectionDomain,
      byte[] classfileBuffer)
      throws Exception;

   /** Called to indicate that the ClassLoader is being discarded by the server.
    * 
    * @param loader - a class loader that has possibly been used previously
    *    as an argument to transform.
    */ 
   public void unregisterClassLoader(ClassLoader loader);
}
