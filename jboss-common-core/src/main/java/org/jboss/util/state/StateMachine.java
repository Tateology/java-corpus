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

import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import org.jboss.logging.Logger;

/** The representation of a finite state machine.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
@SuppressWarnings("unchecked")
public class StateMachine implements Cloneable
{
   private static Logger log = Logger.getLogger(StateMachine.class);
   /** A description of the state machine */
   private String description;
   /** The set of states making up the state machine */
   private HashSet states;
   /** The starting state */
   private State startState;
   /** The current state of the state machine */
   private State currentState;

   /** Create a state machine given its states and start state.
    * 
    * @param states - Set<State> for the state machine
    * @param startState - the starting state
    */ 
   public StateMachine(Set states, State startState)
   {
      this(states, startState, null);
   }
   /** Create a state machine given its states and start state.
    * 
    * @param states - Set<State> for the state machine
    * @param startState - the starting state
    * @param description - an optional description of the state machine
    */ 
   public StateMachine(Set states, State startState, String description)
   {
      this.states = new HashSet(states);
      this.startState = startState;
      this.currentState = startState;
      this.description = description;
   }

   /** Make a copy of the StateMachine maintaining the current state.
    * 
    * @return a copy of the StateMachine.
    */ 
   public Object clone()
   {
      StateMachine clone = new StateMachine(states, startState, description);
      clone.currentState = currentState;
      return clone;
   }

   /** Get the state machine description.
    * @return an possibly null description.
    */ 
   public String getDescription()
   {
      return description;
   }

   /** Get the current state of the state machine.
    * @return the current state.
    */ 
   public State getCurrentState()
   {
      return currentState;
   }

   /** Get the start state of the state machine.
    * @return the start state.
    */ 
   public State getStartState()
   {
      return startState;
   }

   /** Get the states of the state machine.
    * @return the machine states.
    */ 
   public Set getStates()
   {
      return states;
   }

   /** Transition to the next state given the name of a valid transition.
    * @param actionName - the name of transition that is valid for the
    * current state. 
    * @return the next state
    * @throws IllegalTransitionException
    */ 
   public State nextState(String actionName)
      throws IllegalTransitionException
   {
      Transition t = currentState.getTransition(actionName);
      if( t == null )
      {
         String msg = "No transition for action: '" + actionName
            + "' from state: '" + currentState.getName() + "'";
         throw new IllegalTransitionException(msg);
      }
      State nextState = t.getTarget();
      log.trace("nextState("+actionName+") = "+nextState);
      currentState = nextState;
      return currentState;
   }

   /** Reset the state machine back to the start state
    * 
    * @return the start state
    */ 
   public State reset()
   {
      this.currentState = startState;
      return currentState;
   }

   public String toString()
   {
      StringBuffer tmp = new StringBuffer("StateMachine[:\n");
      tmp.append("\tCurrentState: "+currentState.getName());
      Iterator i = states.iterator();
      while( i.hasNext() )
      {
         tmp.append('\n').append(i.next());
      }
      tmp.append(']');
      return tmp.toString();
   }
}
