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
 * A directed, weighted edge in a graph
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 * @param <T> 
 */
public class Edge<T>
{
   private Vertex<T> from;
   private Vertex<T> to;
   private int cost;
   private boolean mark;

   /**
    * Create a zero cost edge between from and to
    * @param from the starting vertex
    * @param to the ending vertex 
    */
   public Edge(Vertex<T> from, Vertex<T> to)
   {
      this(from, to, 0);
   }

   /**
    * Create an edge between from and to with the given cost.
    * 
    * @param from the starting vertex
    * @param to the ending vertex
    * @param cost the cost of the edge 
    */
   public Edge(Vertex<T> from, Vertex<T> to, int cost)
   {
      this.from = from;
      this.to = to;
      this.cost = cost;
      mark = false;
   }

   /**
    * Get the ending vertex
    * @return ending vertex
    */
   public Vertex<T> getTo()
   {
      return to;
   }

   /**
    * Get the starting vertex
    * @return starting vertex
    */
   public Vertex<T> getFrom()
   {
      return from;
   }

   /**
    * Get the cost of the edge
    * @return cost of the edge
    */
   public int getCost()
   {
      return cost;
   }

   /**
    * Set the mark flag of the edge
    *
    */
   public void mark()
   {
      mark = true;
   }

   /**
    * Clear the edge mark flag
    *
    */
   public void clearMark()
   {
      mark = false;
   }

   /**
    * Get the edge mark flag
    * @return edge mark flag
    */
   public boolean isMarked()
   {
      return mark;
   }

   /**
    * String rep of edge
    * @return string rep with from/to vertex names and cost
    */
   public String toString()
   {
      StringBuffer tmp = new StringBuffer("Edge[from: ");
      tmp.append(from.getName());
      tmp.append(",to: ");
      tmp.append(to.getName());
      tmp.append(", cost: ");
      tmp.append(cost);
      tmp.append("]");
      return tmp.toString();
   }
}
