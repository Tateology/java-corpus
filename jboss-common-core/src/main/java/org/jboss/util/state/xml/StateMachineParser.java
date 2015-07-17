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
package org.jboss.util.state.xml;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.jboss.logging.Logger;
import org.jboss.util.state.State;
import org.jboss.util.state.StateMachine;
import org.jboss.util.state.Transition;
import org.jboss.util.xml.DOMUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Parse an xml representation of a state machine. A sample document is:
 
<state-machine description="JACC PolicyConfiguration States">
   <state name="open">
      <transition name="inService" target="open" />
      <transition name="getContextID" target="open" />
      <transition name="getPolicyConfiguration" target="open" />
      <transition name="addToRole" target="open" />
      <transition name="removeRole" target="open" />
      <transition name="addToExcludedPolicy" target="open" />
      <transition name="removeExcludedPolicy" target="open" />
      <transition name="addToUncheckedPolicy" target="open" />
      <transition name="removeUncheckedPolicy" target="open" />
      <transition name="linkConfiguration" target="open" />
      <transition name="commit" target="inService" />
      <transition name="delete" target="deleted" />
   </state>
   <state name="inService">
      <transition name="getPolicyConfiguration" target="open" />
      <transition name="getContextID" target="inService" />
      <transition name="inService" target="inService" />
      <transition name="delete" target="deleted" />
   </state>
   <state name="deleted" isStartState="true">
      <transition name="getPolicyConfiguration" target="open" />
      <transition name="delete" target="deleted" />      
      <transition name="inService" target="deleted" />
      <transition name="getContextID" target="deleted" />
   </state>
</state-machine>

 @author Scott.Stark@jboss.org
 @author Dimitris.Andreadis@jboss.org
 @version $Revision$
 */
@SuppressWarnings("unchecked")
public class StateMachineParser
{
   private static Logger log = Logger.getLogger(StateMachineParser.class);

   public StateMachine parse(URL source) throws Exception
   {
      // parse the XML document into a DOM structure
      InputStream in = source.openConnection().getInputStream();
      Element root = DOMUtils.parse(in);

      String description = root.getAttribute("description");
      HashMap nameToStateMap = new HashMap();
      HashMap nameToTransitionsMap = new HashMap();
      HashSet states = new HashSet();
      State startState = null;

      // parse states
      NodeList stateList = root.getChildNodes();
      for (int i = 0; i < stateList.getLength(); i++)
      {
         Node stateNode = stateList.item(i);
         if (stateNode.getNodeName().equals("state"))
         {
            Element stateElement = (Element)stateNode;
            String stateName = stateElement.getAttribute("name");
            State s = new State(stateName);
            states.add(s);
            nameToStateMap.put(stateName, s);
            HashMap transitions = new HashMap();
            
            // parse transitions
            NodeList transitionList = stateElement.getChildNodes();
            for (int j = 0; j < transitionList.getLength(); j++)
            {
               Node transitionNode = transitionList.item(j);
               if (transitionNode.getNodeName().equals("transition"))
               {
                  Element transitionElement = (Element)transitionNode;
                  String name = transitionElement.getAttribute("name");
                  String targetName = transitionElement.getAttribute("target");
                  transitions.put(name, targetName);
               }
            }
            nameToTransitionsMap.put(stateName, transitions);
            if (Boolean.valueOf(stateElement.getAttribute("isStartState")) == Boolean.TRUE)
               startState = s;
         }
      }
      
      // Resolve all transition targets
      Iterator transitions = nameToTransitionsMap.keySet().iterator();
      StringBuffer resolveFailed = new StringBuffer();
      while (transitions.hasNext())
      {
         String stateName = (String)transitions.next();
         State s = (State)nameToStateMap.get(stateName);
         HashMap stateTransitions = (HashMap)nameToTransitionsMap.get(stateName);
         Iterator it = stateTransitions.keySet().iterator();
         while (it.hasNext())
         {
            String name = (String)it.next();
            String targetName = (String)stateTransitions.get(name);
            State target = (State)nameToStateMap.get(targetName);
            if (target == null)
            {
               String msg = "Failed to resolve target state: " + targetName + " for transition: " + name;
               resolveFailed.append(msg);
               log.debug(msg);
            }
            Transition t = new Transition(name, target);
            s.addTransition(t);
         }
      }

      if (resolveFailed.length() > 0)
         throw new Exception("Failed to resolve transition targets: " + resolveFailed);

      StateMachine sm = new StateMachine(states, startState, description);
      return sm;
   }
}
