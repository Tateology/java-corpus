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
package org.jboss.test.util.test.graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import junit.framework.TestCase;

import org.jboss.util.graph.Edge;
import org.jboss.util.graph.Graph;
import org.jboss.util.graph.Vertex;
import org.jboss.util.graph.Visitor;

/**
 * Tests of the graph package
 *
 * @author Scott.Stark@jboss.org
 * @author Ales.Justin@jboss.org
 * @version $Revision: 1.1 $
 */
@SuppressWarnings("unchecked")
public class GraphTestCase extends TestCase
{
   Vertex<String> a = new Vertex<String>("A", "x.ear");
   Vertex b = new Vertex("B");
   Vertex c = new Vertex("C");
   Vertex d = new Vertex("D");
   Vertex e = new Vertex("E");
   Vertex f = new Vertex("F");
   Vertex g = new Vertex("G");
   Vertex h = new Vertex("H");
   Vertex i = new Vertex("I");
   Vertex<String> ear = new Vertex<String>("x", "x.ear");
   Vertex<String> e1 = new Vertex<String>("e1", "e1.jar");
   Vertex<String> e2 = new Vertex<String>("e2", "e2.jar");
   Vertex<String> w1 = new Vertex<String>("w1", "w1.war");

   public GraphTestCase(String name)
   {
      super(name);
   }

   public void testBasicOps() throws Exception
   {
      Graph graph = new Graph();

      Vertex v1 = new Vertex("1");
      graph.addVertex(v1);
      Vertex v2 = new Vertex("2");
      graph.addVertex(v2);

      graph.addEdge(v1, v2, 0);
      List edges = graph.getEdges();
      assertNotNull(edges);
      assertEquals(1, edges.size());
      Edge e = (Edge)edges.get(0);
      List outgoing1 = v1.getOutgoingEdges();
      assertEquals(1, outgoing1.size());
      assertEquals(e, outgoing1.get(0));
      List incoming1 = v1.getIncomingEdges();
      assertTrue(incoming1 == null || incoming1.isEmpty());
      List outgoing2 = v2.getOutgoingEdges();
      assertTrue(outgoing2 == null || outgoing2.isEmpty());
      List incoming2 = v2.getIncomingEdges();
      assertEquals(1, incoming2.size());
      assertEquals(e, incoming2.get(0));

      graph.removeEdge(v1, v2);
      edges = graph.getEdges();
      assertNotNull(edges);
      assertEquals(0, edges.size());
      outgoing1 = v1.getOutgoingEdges();
      assertTrue(outgoing1 == null || outgoing1.isEmpty());
      incoming1 = v1.getIncomingEdges();
      assertTrue(incoming1 == null || incoming1.isEmpty());
      outgoing2 = v2.getOutgoingEdges();
      assertTrue(outgoing2 == null || outgoing2.isEmpty());
      incoming2 = v2.getIncomingEdges();
      assertTrue(incoming2 == null || incoming2.isEmpty());
   }

   /** Depth first search of digraph1
    * @throws Exception
    */
   public void testDFS() throws Exception
   {
      Graph graph = buildGraph1();

      final ArrayList order = new ArrayList();
      Visitor visitor = new Visitor()
      {
         public void visit(Graph g, Vertex v)
         {
            System.out.println("visit "+v.getName());
            order.add(v);
         }
      };
      graph.depthFirstSearch(a, visitor);

      assertTrue("Visted count == 8("+order.size()+")", order.size() == 8);
      // Validate the expected order and visited state
      for(int n = 0; n < order.size(); n ++)
      {
         Vertex v = (Vertex) order.get(n);
         System.out.println(v);
         assertTrue(v.getName()+" is visited", v.visited());
      }
      String[] names = {"A", "B", "C", "H", "G", "I", "E", "F"};
      for(int n = 0; n < order.size(); n ++)
      {
         Vertex v = (Vertex) order.get(n);
         assertTrue("#"+n+" is "+names[n]+"("+v.getName()+")",
            v.getName().equals(names[n]));
      }
   }

   /** Breadth first search of digraph1
    * @throws Exception
    */
   public void testBFS()
      throws Exception
   {
      Graph graph = buildGraph1();

      final ArrayList<Vertex> order = new ArrayList<Vertex>();
      Visitor visitor = new Visitor()
      {
         public void visit(Graph g, Vertex v)
         {
            System.out.println("visit "+v.getName());
            order.add(v);
         }
      };
      graph.breadthFirstSearch(a, visitor);

      assertTrue("Visted count == 8("+order.size()+")", order.size() == 8);
      // Validate the expected order and visited state
      for(int n = 0; n < order.size(); n ++)
      {
         Vertex v = order.get(n);
         System.out.println(v);
         assertTrue(v.getName()+" is visited", v.visited());
      }
      String[] names = {"A", "B", "F", "C", "G", "E", "I", "H"};
      for(int n = 0; n < order.size(); n ++)
      {
         Vertex v = order.get(n);
         assertTrue("#"+n+" is "+names[n]+"("+v.getName()+")",
            v.getName().equals(names[n]));
      }
   }

   public void testCycleDection()
   {
      Graph g = new Graph();
      Vertex v0 = new Vertex("V0");
      Vertex v1 = new Vertex("V1");
      Vertex v2 = new Vertex("V2");
      g.addVertex(v0);
      g.addVertex(v1);
      g.addVertex(v2);

      g.addEdge(v0, v1, 0);
      g.addEdge(v1, v2, 0);
      g.addEdge(v2, v0, 0);

      Edge[] cycleEdges = g.findCycles();
      assertTrue("There is a cycle", cycleEdges.length == 1);
      for(int n = 0; n < cycleEdges.length; n ++)
         System.out.println(cycleEdges[n]);

      Edge e = cycleEdges[0];
      Vertex vx = e.getFrom();
      Vertex vy = e.getTo();
      assertTrue("Cycle edge is V2->V0", vx.getName().equals("V2")
         && vy.getName().equals("V0"));
   }

   public void testEdges()
   {
      Graph<String> g = new Graph<String>();
      assertTrue(g.addVertex(ear));
      assertFalse(g.addVertex(ear));
      assertEquals("size == 1", 1, g.size());

      try
      {
         g.addEdge(ear, e1, 0);
         fail("Should have seen IllegalArgumentException");
      }
      catch(IllegalArgumentException e)
      {
         System.out.println("Saw IAE as expected");
      }
      assertTrue("Added e1", g.addVertex(e1));
      assertTrue("Added E(ear, e1)", g.addEdge(ear, e1, 0));
      assertFalse("Did not readded E(ear, e1)", g.addEdge(ear, e1, 0));
      assertTrue("Added e2", g.addVertex(e2));
      assertTrue("Added w1", g.addVertex(w1));
      assertTrue("Added E(ear, e2)", g.addEdge(ear, e2, 0));
      assertTrue("Added E(ear, w1)", g.addEdge(ear, w1, 0));

      Edge<String>[] cycles = g.findCycles();
      assertEquals("cycles.length = 0", 0, cycles.length);
   }

   public void testFindByName()
   {
      Graph<String> g = buildEarGraph();
      Vertex<String> v = g.findVertexByName("x");
      assertEquals("x", ear, v);
      v = g.findVertexByName("e1");
      assertEquals("e1", e1, v);
      v = g.findVertexByName("e2");
      assertEquals("e2", e2, v);
      v = g.findVertexByName("w1");
      assertEquals("w1", w1, v);

      v = g.findVertexByName("w1.war");
      assertTrue("w1.war not found", v == null);
   }

   public void testFindByData()
   {
      Graph<String> g = buildEarGraph();
      Comparator<String> SC = new Comparator<String>()
      {
         public int compare(String s1, String s2)
         {
            int compare;
            if( s1 == s2 )
               compare = 0;
            else if( s1 == null )
               compare = 1;
            else if( s2 == null )
               compare = -1;
            else
               compare = s1.compareTo(s2);
            return compare;
         }
      };
      Vertex<String> v = g.findVertexByData("x.ear", SC);
      assertEquals("x.ear", ear, v);
      v = g.findVertexByData("e1.jar", SC);
      assertEquals("e1.jar", e1, v);
      v = g.findVertexByData("e2.jar", SC);
      assertEquals("e2.jar", e2, v);
      v = g.findVertexByData("w1.war", SC);
      assertEquals("w1.war", w1, v);

      v = g.findVertexByData("w1", SC);
      assertTrue("w1 not found", v == null);
   }

   /** Build the following digraph:
    A : B, F
    B : C, G
    C : H
    D : E, H
    E :
    F : E, I
    G : A
    H : G, I
    I : E, G
    */
   private Graph buildGraph1()
   {
      Graph graph = new Graph();
      assertTrue("Added A", graph.addVertex(a));
      assertTrue("Added B", graph.addVertex(b));
      assertTrue("Added C", graph.addVertex(c));
      assertTrue("Added D", graph.addVertex(d));
      assertTrue("Added E", graph.addVertex(e));
      assertTrue("Added F", graph.addVertex(f));
      assertTrue("Added G", graph.addVertex(g));
      assertTrue("Added H", graph.addVertex(h));
      assertTrue("Added I", graph.addVertex(i));

      // A : B, F
      assertTrue("Added A -> B", graph.addEdge(a, b, 0));
      assertTrue("Added A -> F", graph.addEdge(a, f, 0));
      // B : C, G
      assertTrue("Added B -> C", graph.addEdge(b, c, 0));
      assertTrue("Added B -> G", graph.addEdge(b, g, 0));
      // C : H
      assertTrue("Added C -> H", graph.addEdge(c, h, 0));
      // D : E, H
      assertTrue("Added D -> E", graph.addEdge(d, e, 0));
      assertTrue("Added D -> H", graph.addEdge(d, h, 0));
      // F : E, I
      assertTrue("Added F -> E", graph.addEdge(f, e, 0));
      assertTrue("Added F -> I", graph.addEdge(f, i, 0));
      // G : A
      assertTrue("Added G -> A", graph.addEdge(g, a, 0));
      // H : G, I
      assertTrue("Added H -> G", graph.addEdge(h, g, 0));
      assertTrue("Added H -> I", graph.addEdge(h, i, 0));
      // I : E, G
      assertTrue("Added I -> E", graph.addEdge(i, e, 0));
      assertTrue("Added I -> G", graph.addEdge(i, g, 0));
      assertTrue("Graph has 9 verticies", graph.size() == 9);
      return graph;
   }

   /** Build the following digraph:
    x.ear : e1.jar, e2.jar, w1.war
    */
   private Graph<String> buildEarGraph()
   {
      Graph<String> g = new Graph<String>();
      assertTrue(g.addVertex(ear));
      assertFalse(g.addVertex(ear));
      assertTrue("Added e1", g.addVertex(e1));
      assertTrue("Added E(ear, e1)", g.addEdge(ear, e1, 0));
      assertTrue("Added e2", g.addVertex(e2));
      assertTrue("Added w1", g.addVertex(w1));
      assertTrue("Added E(ear, e2)", g.addEdge(ear, e2, 0));
      assertTrue("Added E(ear, w1)", g.addEdge(ear, w1, 0));
      return g;
   }
}
