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

package org.jboss.test.util.test.stream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import junit.framework.TestCase;

import org.jboss.util.stream.MarshalledValueInputStream;
import org.jboss.util.stream.MarshalledValueOutputStream;

/**
 * Test MarshalledValueInput/OutputStream 
 *
 * @see org.jboss.util.propertyeditor.PropertyEditors
 * 
 * @author Jason.Greene@jboss.org 
 * @version $Revision$
 */
public class MarshallValueStreamTestCase extends TestCase
{
   public void testArrayMarshall() throws Exception
   {
      Byte[] bytes = new Byte[] {1, 2, 3};
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      MarshalledValueOutputStream os = new MarshalledValueOutputStream(baos);
      os.writeObject(bytes);
      os.flush();
      MarshalledValueInputStream is = new MarshalledValueInputStream(new ByteArrayInputStream(baos.toByteArray())); 
      assertTrue(Arrays.equals(bytes, (Byte[]) is.readObject()));
   }

   public void testPrimitive() throws Exception
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(int.class);
      oos.close();
      baos.close();
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      MarshalledValueInputStream mvis = new MarshalledValueInputStream(bais);
      Object o = mvis.readObject();
      assertNotNull(o);
      assertEquals(o, int.class);
   }
}
