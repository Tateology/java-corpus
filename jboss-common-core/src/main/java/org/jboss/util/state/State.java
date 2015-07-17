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
package org.jboss.util.state;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

/** The respresentation of a state in a state machine.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
@SuppressWarnings("unchecked")
public class State
{
   /** The name of the state */
   private String name;
   /** HashMap<String, Transition> */
   private HashMap allowedTransitions = new HashMap();
   /** Arbitrary state data */
   private Object data;

   public State(String name)
   {
      this(name, null);
   }
   public State(String name, Map transitions)
   {
      this.name = name;
      if( transitions != null )
      {
         allowedTransitions.putAll(transitions);
      }
   }

   /** Get the state name.
    * @return the name of the state.
    */ 
   public String getName()
   {
      return name;
   }

   public Object getData()
   {
      return data;
   }
   public void setData(Object data)
   {
      this.data = data;
   }

   /** An accept state is indicated by no transitions
    * @return true if this is an accept state, false otherwise.
    */ 
   public boolean isAcceptState()
   {
      return allowedTransitions.size() == 0;
   }

   /** Add a transition to the allowed transition map.
    * 
    * @param transition
    * @return this to allow chained addTransition calls
    */ 
   public State addTransition(Transition transition)
   {
      allowedTransitions.put(transition.getName(), transition);
      return this;
   }
   
   /** Lookup an allowed transition given its name.
    * 
    * @param name - the name of a valid transition from this state.
    * @return the valid transition if it exists, null otherwise.
    */ 
   public Transition getTransition(String name)
   {
      Transition t = (Transition) allowedTransitions.get(name);
      return t;
   }

   /** Get the Map<String, Transition> of allowed transitions for this state.
    * @return the allowed transitions map.
    */ 
   public Map getTransitions()
   {
      return allowedTransitions;
   }

   public String toString()
   {
      StringBuffer tmp = new StringBuffer("State(name=");
      tmp.append(name);
      Iterator i = allowedTransitions.entrySet().iterator();
      while( i.hasNext() )
      {
         Map.Entry e = (Map.Entry) i.next();
         tmp.append("\n\t on: ");
         tmp.append(e.getKey());
         Transition t = (Transition) e.getValue();
         tmp.append(" go to: ");
         tmp.append(t.getTarget().getName());
      }
      tmp.append(')');
      return tmp.toString();
   }
}
