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
package org.jboss.util.naming;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.RefAddr;
import javax.naming.LinkRef;
import javax.naming.spi.ObjectFactory;

import org.jboss.logging.Logger;

/**
 * Return a LinkRef based on a ThreadLocal key.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision$
 */
@SuppressWarnings("unchecked")
public class ENCThreadLocalKey
      implements ObjectFactory
{
   private static final Logger log = Logger.getLogger(ENCThreadLocalKey.class);

   // We need all the weak maps to make sure everything is released properly
   // and we don't have any memory leaks

   private final static ThreadLocal key = new ThreadLocal();

   public static void setKey(String tlkey)
   {
      key.set(tlkey);
   }

   public static String getKey()
   {
      return (String) key.get();
   }

   public Object getObjectInstance(Object obj,
         Name name,
         Context nameCtx,
         Hashtable environment)
         throws Exception
   {
      Reference ref = (Reference) obj;
      String reftype = (String) key.get();
      boolean trace = log.isTraceEnabled();

      if (reftype == null)
      {
         if (trace)
            log.trace("using default in ENC");
         reftype = "default";
      }

      RefAddr addr = ref.get(reftype);
      if (addr == null)
      {
         if (trace)
            log.trace("using default in ENC");
         addr = ref.get("default"); // try to get default linking
      }
      if (addr != null)
      {
         String target = (String) addr.getContent();
         if (trace)
            log.trace("found Reference " + reftype + " with content " + target);
         return new LinkRef(target);
      }
      return null;
   }

}
