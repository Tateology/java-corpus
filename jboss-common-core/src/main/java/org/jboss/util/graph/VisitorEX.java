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
package org.jboss.util.graph;

/**
 * A graph visitor interface that can throw an exception during
 * a visit callback.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 * @param <T> 
 * @param <E> 
 */
public interface VisitorEX<T, E extends Exception>
{   
   /**
    * Called by the graph traversal methods when a vertex is first visited.
    * 
    * @param g - the graph
    * @param v - the vertex being visited.
    * @throws E exception for any error
    */
   public void visit(Graph<T> g, Vertex<T> v) throws E;
}
