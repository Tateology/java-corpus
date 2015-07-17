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
package org.jboss.test.util.test;

import junit.framework.TestCase;
import org.jboss.test.util.support.A;
import org.jboss.test.util.support.B;

/**
 * Test JBossObject.
 * 
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class JBossObjectTestCase extends TestCase
{
   public void testLogger() throws Exception
   {
      A a1 = new A();
      A a2 = new A();

      assertSame(a1.getLog(), a2.getLog());

      B b = new B();

      assertNotSame(b.getLog(), a1.getLog());
   }
}
