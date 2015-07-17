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
package org.jboss.util.timeout;

/**
 * TimeoutPriorityQueueImpl.
 *
 *  This is a balanced binary tree. If nonempty, the root is at index 1,
 *  and all nodes are at indices 1..size. Nodes with index greater than
 *  size are null. Index 0 is never used.
 *  Children of the node at index <code>j</code> are at <code>j*2</code>
 *  and <code>j*2+1</code>. The children of a node always fire the timeout
 *  no earlier than the node.
 *
 *
 *  Or, more formally:
 *
 *  Only indices <code>1</code>..<code>size</code> of this array are used.
 *  All other indices contain the null reference.
 *  This array represent a balanced binary tree.
 *
 *  If <code>size</code> is <code>0</code> the tree is empty, otherwise
 *  the root of the tree is at index <code>1</code>.
 *
 *  Given an arbitrary node at index <code>n</code> that is not the root
 *  node, the parent node of <code>n</code> is at index <code>n/2</code>.
 *
 *  Given an arbitrary node at index <code>n</code>; if
 *  <code>2*n <= size</code> the node at <code>n</code> has its left child
 *  at index <code>2*n</code>, otherwise the node at <code>n</code> has
 *  no left child.
 *
 *  Given an arbitrary node at index <code>n</code>; if
 *  <code>2*n+1 <= size</code> the node at <code>n</code> has its right child
 *  at index <code>2*n+1</code>, otherwise the node at <code>n</code> has
 *  no right child.
 *
 *  The priority function is called T. Given a node <code>n</code>,
 *  <code>T(n)</code> denotes the absolute time (in milliseconds since
 *  the epoch) that the timeout for node <code>n</code> should happen.
 *  Smaller values of <code>T</code> means higher priority.
 *
 *  The tree satisfies the following invariant:
 *  <i>
 *  For any node <code>n</code> in the tree:
 *  If node <code>n</code> has a left child <code>l</code>,
 *  <code>T(n) <= T(l)</code>.
 *  If node <code>n</code> has a right child <code>r</code>,
 *  <code>T(n) <= T(r)</code>.
 *  </i>
 *
 *
 *  The invariant may be temporarily broken while executing synchronized
 *  on <code>this</code> instance, but is always reestablished before
 *  leaving the synchronized code.
 *
 *  The node at index <code>1</code> is always the first node to timeout,
 *  as can be deduced from the invariant.
 *
 *  For the following algorithm pseudocode, the operation
 *  <code>swap(n,m)</code> denotes the exchange of the nodes at indices
 *  <code>n</code> and <code>m</code> in the tree.
 *
 *  Insertion of a new node happend as follows:
 *  <pre>
 *    IF size = q.length THEN
 *      "expand q array to be larger";
 *    ENDIF
 *    size <- size + 1;
 *    q[size] <- "new node";
 *    n <- size;
 *    WHILE n > 1 AND T(n/2) > T(n) DO
 *      swap(n/2, n);
 *      n <- n/2;
 *    ENDWHILE
 *  </pre>
 *  Proof that this insertion algorithm respects the invariant is left to
 *  the interested reader.
 *
 *  The removal algorithm is a bit more complicated. To remove the node
 *  at index <code>n</code>:
 *  <pre>
 *    swap(n, size);
 *    size <- size - 1;
 *    IF n > 1 AND T(n/2) > T(n) THEN
 *      WHILE n > 1 AND T(n/2) > T(n) DO
 *        swap(n/2, n);
 *        n <- n/2;
 *      ENDWHILE
 *    ELSE
 *      WHILE 2*n <= size DO
 *        IF 2*n+1 <= size THEN
 *          // Both children present
 *          IF T(2*n) <= T(2*n+1) THEN
 *            IF T(n) <= T(2*n) THEN
 *              EXIT;
 *            ENDIF
 *            swap(n, 2*n);
 *            n <- 2*n;
 *          ELSE
 *            IF T(n) <= T(2*n+1) THEN
 *              EXIT;
 *            ENDIF
 *            swap(n, 2*n+1);
 *            n <- 2*n+1;
 *          ENDIF
 *        ELSE
 *          // Only left child, right child not present.
 *          IF T(n) <= T(2*n) THEN
 *            EXIT;
 *          ENDIF
 *          swap(n, 2*n);
 *          n <- 2*n;
 *        ENDIF
 *      ENDWHILE
 *    ENDIF
 *  </pre>
 *  Proof that this removal algorithm respects the invariant is left to
 *  the interested reader. Really, I am not going to prove it here.
 *
 *  If you are interested, you can find this data structure and its
 *  associated operations in most textbooks on algorithmics.
 * 
 * @author <a href="osh@sparre.dk">Ole Husgaard</a>
 * @author <a href="dimitris@jboss.org">Dimitris Andreadis</a>
 * @author <a href="genman@maison-otaku.net">Elias Ross</a>  
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision$
 */
public class TimeoutPriorityQueueImpl implements TimeoutPriorityQueue
{
   //  Code commented out with the mark "INV:" are runtime checks
   //  of invariants that are not needed for a production system.
   //  For problem solving, you can remove these comments.
   //  Multithreading notes:
   //
   //  While a TimeoutImpl is enqueued, its index field contains the index
   //  of the instance in the queue; that is, for 1 <= n <= size,
   //  q[n].index = n.
   //  Modifications of an enqueued TimeoutImpl instance may only happen
   //  in code synchronized on the TimeoutFactory instance that has it
   //  enqueued.
   //  Modifications on the priority queue may only happen while running in
   //  code synchronized on the TimeoutFactory instance that holds the queue.
   //  When a TimeoutImpl instance is no longer enqueued, its index field
   //  changes to one of the negative constants declared in the TimeoutImpl
   //  class.
   //
   //  Cancellation may race with the timeout.
   //  To avoid problems with this, the TimeoutImpl index field is set to
   //  TimeoutImpl.TIMEOUT when the TimeoutImpl is taken out of the queue.
   //  Finally the index field is set to TimeoutImpl.DONE, and
   //  the TimeoutImpl instance is discarded.

   /** The lock object */
   private Object lock = new Object();

   /** The size of the timeout queue. */
   private int size;

   /** The timeouts */
   private TimeoutExtImpl[] queue;

   /**
    * Create a new TimeoutPriorityQueueImpl.
    */
   public TimeoutPriorityQueueImpl()
   {
      queue = new TimeoutExtImpl[16];
      size = 0;
   }

   public TimeoutExt offer(long time, TimeoutTarget target)
   {
      if (queue == null)
         throw new IllegalStateException("TimeoutPriorityQueue has been cancelled");
      if (time < 0)
         throw new IllegalArgumentException("Negative time");
      if (target == null)
         throw new IllegalArgumentException("Null timeout target");

      synchronized (lock)
      {
         // INV: checkTree();
         // INV: assertExpr(size < queue.length);
         if (++size == queue.length)
         {
            TimeoutExtImpl[] newQ = new TimeoutExtImpl[2 * queue.length];
            System.arraycopy(queue, 0, newQ, 0, queue.length);
            queue = newQ;
         }
         // INV: assertExpr(size < queue.length);
         // INV: assertExpr(queue[size] == null);
         TimeoutExtImpl timeout;
         timeout = queue[size] = new TimeoutExtImpl();
         timeout.index = size;
         timeout.time = time;
         timeout.target = target;
         normalizeUp(size);
         if (timeout.index == 1)
            lock.notify();
         // INV: checkTree();
         return timeout;
      }
   }

   public TimeoutExt take()
   {
      return poll(-1);
   }

   public TimeoutExt poll()
   {
      return poll(1);
   }

   public TimeoutExt poll(long wait)
   {
      long endWait = -1;
      if (wait > 0)
         endWait = System.currentTimeMillis() + wait;
      // Look for work
      synchronized (lock)
      {
         while (queue != null && (wait >= 0 || endWait == -1))
         {
            if (size == 0)
            {
               try
               {
                  if (endWait == -1)
                     lock.wait();
                  else
                     lock.wait(wait);
               }
               catch (InterruptedException ex)
               {
               }
            }
            else
            {
               long now = System.currentTimeMillis();
               if (queue[1].time > now)
               {
                  long waitForFirst = queue[1].time - now;
                  if (endWait != -1 && waitForFirst > wait)
                     waitForFirst = wait;
                  try
                  {
                     lock.wait(waitForFirst);
                  }
                  catch (InterruptedException ex)
                  {
                  }
               }
               if (size > 0 && queue != null && queue[1].time <= System.currentTimeMillis())
               {
                  TimeoutExtImpl result = removeNode(1);
                  result.index = TimeoutExtImpl.TIMEOUT;
                  return result;
               }
            }
            if (endWait != -1)
               wait = endWait - System.currentTimeMillis();
         }
      }
      return null;
   }

   public TimeoutExt peek()
   {
      synchronized (lock)
      {
         if (size > 0)
            return queue[1];
         else
            return null;
      }
   }

   public boolean remove(TimeoutExt timeout)
   {
      TimeoutExtImpl timeoutImpl = (TimeoutExtImpl) timeout;
      synchronized (lock)
      {
         if (timeoutImpl.index > 0)
         {
            // Active timeout, remove it.
            // INV: assertExpr(queue[timeoutImpl.index] == timeout);
            // INV: checkTree();
            removeNode(timeoutImpl.index);
            // INV: checkTree();
            timeoutImpl.index = TimeoutExtImpl.DONE;

            // execution cancelled
            return true;
         }
         else
         {
            // has already been executed (DONE) or
            // is currently executing (TIMEOUT)
            return false;
         }
      }
   }

   public void clear()
   {
      synchronized (lock)
      {
         if (queue == null)
            return;

         // cleanup queue
         for (int i = 1; i <= size; ++i)
            queue[i] = cleanupTimeoutExtImpl(queue[i]);
      }
   }

   public void cancel()
   {
      synchronized (lock)
      {
         if (queue == null)
            return;
         clear();
         queue = null;
         size = 0;
         lock.notifyAll();
      }
   }

   public int size()
   {
      return size;
   }
   
   /**
    * Whether the queue is cancelled
    * 
    * @return true when cancelled
    */
   public boolean isCancelled()
   {
      return queue == null;
   }

   /**
    *  A new node has been added at index <code>index</code>.
    *  Normalize the tree by moving the new node up the tree.
    *
    *  @return true if the tree was modified.
    */
   private boolean normalizeUp(int index)
   {
      // INV: assertExpr(index > 0);
      // INV: assertExpr(index <= size);
      // INV: assertExpr(queue[index] != null);
      if (index == 1)
         return false; // at root
      boolean ret = false;
      long t = queue[index].time;
      int p = index >> 1;
      while (queue[p].time > t)
      {
         // INV: assertExpr(queue[index].time == t);
         swap(p, index);
         ret = true;
         if (p == 1)
            break; // at root
         index = p;
         p >>= 1;
      }
      return ret;
   }

   /**
    * Swap two nodes in the tree.
    * 
    * @param a the first index
    * @param b the second index
    */
   private void swap(int a, int b)
   {
      // INV: assertExpr(a > 0);
      // INV: assertExpr(a <= size);
      // INV: assertExpr(b > 0);
      // INV: assertExpr(b <= size);
      // INV: assertExpr(queue[a] != null);
      // INV: assertExpr(queue[b] != null);
      // INV: assertExpr(queue[a].index == a);
      // INV: assertExpr(queue[b].index == b);
      TimeoutExtImpl temp = queue[a];
      queue[a] = queue[b];
      queue[a].index = a;
      queue[b] = temp;
      queue[b].index = b;
   }

   /**
    * Remove a node from the tree and normalize.
    *
    * @param index the index in the queue
    * @return the removed node.
    */
   private TimeoutExtImpl removeNode(int index)
   {
      // INV: assertExpr(index > 0);
      // INV: assertExpr(index <= size);
      TimeoutExtImpl res = queue[index];
      // INV: assertExpr(res != null);
      // INV: assertExpr(res.index == index);
      if (index == size)
      {
         --size;
         queue[index] = null;
         return res;
      }
      swap(index, size); // Exchange removed node with last leaf node
      --size;
      // INV: assertExpr(res.index == size + 1);
      queue[res.index] = null;
      if (normalizeUp(index))
         return res; // Node moved up, so it shouldn't move down
      long t = queue[index].time;
      int c = index << 1;
      while (c <= size)
      {
         // INV: assertExpr(q[index].time == t);
         TimeoutExtImpl l = queue[c];
         // INV: assertExpr(l != null);
         // INV: assertExpr(l.index == c);
         if (c + 1 <= size)
         {
            // two children, swap with smallest
            TimeoutExtImpl r = queue[c + 1];
            // INV: assertExpr(r != null);
            // INV: assertExpr(r.index == c+1);
            if (l.time <= r.time)
            {
               if (t <= l.time)
                  break; // done
               swap(index, c);
               index = c;
            }
            else
            {
               if (t <= r.time)
                  break; // done
               swap(index, c + 1);
               index = c + 1;
            }
         }
         else
         { // one child
            if (t <= l.time)
               break; // done
            swap(index, c);
            index = c;
         }
         c = index << 1;
      }
      return res;
   }

   /**
    * Recursive cleanup of a TimeoutImpl
    * 
    * @return null
    */
   private TimeoutExtImpl cleanupTimeoutExtImpl(TimeoutExtImpl timeout)
   {
      if (timeout != null)
         timeout.target = null;
      return null;
   }

   /**
    * Check invariants of the queue.
    */
   void checkTree()
   {
      assertExpr(size >= 0);
      assertExpr(size < queue.length);
      assertExpr(queue[0] == null);
      if (size > 0)
      {
         assertExpr(queue[1] != null);
         assertExpr(queue[1].index == 1);
         for (int i = 2; i <= size; ++i)
         {
            assertExpr(queue[i] != null);
            assertExpr(queue[i].index == i);
            assertExpr(queue[i >> 1].time <= queue[i].time); // parent fires first
         }
         for (int i = size + 1; i < queue.length; ++i)
            assertExpr(queue[i] == null);
      }
   }

   /**
    * Debugging helper.
    */
   private void assertExpr(boolean expr)
   {
      if (!expr)
         throw new IllegalStateException("***** assert failed *****");
   }

   /**
    *  Our private Timeout implementation.
    */
   private class TimeoutExtImpl implements TimeoutExt
   {
      /** Done */
      static final int DONE = -1;

      /** In timeout */
      static final int TIMEOUT = -2;

      /** Index in the queue */
      int index;

      /** Time of the timeout */
      long time;

      /** The timeout target */
      TimeoutTarget target;

      public long getTime()
      {
         return time;
      }

      public TimeoutTarget getTimeoutTarget()
      {
         return target;
      }

      public void done()
      {
         index = DONE;
      }
      
      public boolean cancel()
      {
         return remove(this);
      }
   }
}
