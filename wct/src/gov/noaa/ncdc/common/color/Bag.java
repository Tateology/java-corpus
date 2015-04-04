/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package gov.noaa.ncdc.common.color;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

/** Maintains a simple array (objs) of Objects and the number of objects (numObjs) in the array
    (the array can be bigger than this number).  Unlike Vector or ArrayList, Bag is designed
    to encourage direct access of the array.  If you access the objects directly, they are
    stored in positions [0 ... numObjs-1].  If you wish to extend the array, you should call
    the resize method.
    
    <p>By providing direct access to the array, Bags are about three and a half times faster than ArrayLists
    (whose get/set methods unfortunately at present contain un-inlinable range bounds checks) and four times faster 
    than Vectors (whose methods additionally are synchronized).  Even Bag's built-in get() and set() methods, 
    complete with range bounds checks, are twice the speed of ArrayLists.  To get faster 
    than a Bag, you'd have to go to a raw fixed-length array of the specific class type of your objects.
    Accessing a Bag's Object array and casting its Objects into the appropriate class is about 50% slower
    than accessing a fixed-length array of that class in the first place.
    
    <p>Bag is not synchronized, and so should not be accessed from different threads without locking on it
    or some appropriate lock object first.  Bag also has an unusual, fast method for removing objects
    called remove(...), which removes the object simply by swapping the topmost object into its
    place.  This means that after remove(...) is called, the Bag may no longer have the same order
    (hence the reason it's called a "Bag" rather than some variant on "Vector" or "Array" or "List").  You can
    guarantee order by calling removeNondestructively(...) instead if you wish, but this is O(n) in the worst case.

    <p>Bags provide iterators but you are strongly encouraged to just access the array instead.  Iterators
    are slow.  Bag's iterator performs its remove operation by calling removeNondestructively().  
    Like array access, iterator usage is undefined if objects are placed into the Bag or 
    removed from the Bag in the middle of the iterator usage (except by using the iterator's remove 
    operation of course).
*/

public class Bag implements java.util.Collection, java.io.Serializable, Cloneable, Indexed
    {
    public Object[] objs;
    public int numObjs;
    
    public Bag() { numObjs = 0; objs = new Object[1]; }
    
    /** Creates a Bag with a given initial capacity. */
    public Bag(int capacity) { numObjs = 0; objs = new Object[capacity]; }
        
    /** Adds the objects from the other Bag without copying them.  The size of the
        new Bag is the minimum necessary size to hold the objects. */
    public Bag(final Bag other)
        {
        if (other==null) { numObjs = 0; objs = new Object[1]; }
        numObjs = other.numObjs;
        objs = new Object[numObjs];
        System.arraycopy(other.objs,0,objs,0,numObjs);
        }
    
    public int size()
        {
        return numObjs;
        }
    
    public boolean isEmpty()
        {
        return (numObjs<= 0);
        }
    
    public boolean addAll(final Collection other) 
        { 
        if (other instanceof Bag) return addAll((Bag)other);  // avoid an array build
        return addAll(numObjs, other.toArray()); 
        }

    public boolean addAll(final int index, final Collection other)
        {
        if (other instanceof Bag) return addAll(index, (Bag)other);  // avoid an array build
        return addAll(index, other.toArray());
        }

    public boolean addAll(final int index, final Object[] other)
        {
        // throws NullPointerException if other == null,
        // ArrayIndexOutOfBoundsException if index < 0,
        // and IndexOutOfBoundsException if index > numObjs
        if (index > numObjs) { throwIndexOutOfBoundsException(index); }
        if (other.length == 0) return false;
        // make Bag big enough
        if (numObjs+other.length > objs.length)
            resize(numObjs+other.length);
        if (index != numObjs)   // make room
            System.arraycopy(objs,index,objs,index+other.length,other.length);
        System.arraycopy(other,0,objs,index,other.length);
        numObjs += other.length;
        return true;
        }
    
    public boolean addAll(final Bag other) { return addAll(numObjs,other); }

    public boolean addAll(final int index, final Bag other)
        {
        // throws NullPointerException if other == null,
        // ArrayIndexOutOfBoundsException if index < 0,
        // and IndexOutOfBoundsException if index > numObjs
        if (index > numObjs) { throwIndexOutOfBoundsException(index); }
        if (other.numObjs <= 0) return false;
        // make Bag big enough
        if (numObjs+other.numObjs > objs.length)
            resize(numObjs+other.numObjs);
        if (index != numObjs)    // make room
            System.arraycopy(objs,index,objs,index+other.numObjs,other.numObjs);
        System.arraycopy(other.objs,0,objs,index,other.numObjs);
        numObjs += other.numObjs;
        return true;
        }

    public Object clone() throws CloneNotSupportedException
        {
        Bag b = (Bag)(super.clone());
        b.objs = (Object[]) objs.clone();
        return b;
        }
    
    /** Resizes the internal array to at least the requested size. */
    public void resize(int toAtLeast)
        {
        if (objs.length >= toAtLeast)  // already at least as big as requested
            return;

        if (objs.length * 2 > toAtLeast)  // worth doubling
            toAtLeast = objs.length * 2;

        // now resize
        Object[] newobjs = new Object[toAtLeast];
        System.arraycopy(objs,0,newobjs,0,numObjs);
        objs=newobjs;
        }
        
    /** Resizes the objs array to max(numObjs, desiredLength), unless that value is greater than or equal to objs.length,
        in which case no resizing is done (this operation only shrinks -- use resize() instead).
        This is an O(n) operation, so use it sparingly. */
    public void shrink(int desiredLength)
        {
        if (desiredLength < numObjs) desiredLength = numObjs;
        if (desiredLength >= objs.length) return;  // no reason to bother
        Object[] newobjs = new Object[desiredLength];
        System.arraycopy(objs,0,newobjs,0,numObjs);
        objs = newobjs;
        }
    
    /** Returns null if the Bag is empty, else returns the topmost object. */
    public Object top()
        {
        if (numObjs<= 0) return null;
        else return objs[numObjs-1];
        }
    
    /** Returns null if the Bag is empty, else removes and returns the topmost object. */
    public Object pop()
        {
        // this curious arrangement makes me small enough to be inlined (35 bytes; right at the limit)
        int numObjs = this.numObjs;
        if (numObjs<= 0) return null;
        Object ret = objs[--numObjs];
        objs[numObjs] = null; // let GC
        this.numObjs = numObjs;
        return ret;
        }
    
    /** Synonym for add(obj) -- stylistically, you should add instead unless you
        want to think of the Bag as a stack. */
    public boolean push(final Object obj)
        {
        // this curious arrangement makes me small enough to be inlined (35 bytes)
        int numObjs = this.numObjs;
        if (numObjs >= objs.length) doubleCapacityPlusOne();
        objs[numObjs] = obj;
        this.numObjs = numObjs+1;
        return true;
        }
        
    public boolean add(final Object obj)
        {
        // this curious arrangement makes me small enough to be inlined (35 bytes)
        int numObjs = this.numObjs;
        if (numObjs >= objs.length) doubleCapacityPlusOne();
        objs[numObjs] = obj;
        this.numObjs = numObjs+1;
        return true;
        }
        
    // private function used by add and push in order to get them below
    // 35 bytes -- always doubles the capacity and adds one
    void doubleCapacityPlusOne()
        {
        Object[] newobjs = new Object[numObjs*2+1];
        System.arraycopy(objs,0,newobjs,0,numObjs);
        objs=newobjs;
        }

    public boolean contains(final Object o)
        {
        int numObjs = this.numObjs;
        Object[] objs = this.objs;
        for(int x=0;x<numObjs;x++)
            if (o==null ?  objs[x]==null :  o==objs[x] || o.equals(objs[x])) return true;
        return false;
        }
        
    public boolean containsAll(final Collection c)
        {
        Iterator iterator = c.iterator();
        while(iterator.hasNext())
            if (!contains(iterator.next())) return false;
        return true;
        }

    public Object get(final int index)
        {
        if (index>=numObjs) // || index < 0)
            throwIndexOutOfBoundsException(index);
        return objs[index];
        }

    /** identical to get(index) */
    public Object getValue(final int index)
        {
        if (index>=numObjs) // || index < 0)
            throwIndexOutOfBoundsException(index);
        return objs[index];
        }

    public Object set(final int index, final Object element)
        {
        if (index>=numObjs) // || index < 0)
            throwIndexOutOfBoundsException(index);
        Object returnval = objs[index];
        objs[index] = element;
        return returnval;
        }

    /** identical to set(index, element) */
    public Object setValue(final int index, final Object element)
        {
        if (index>=numObjs) // || index < 0)
            throwIndexOutOfBoundsException(index);
        Object returnval = objs[index];
        objs[index] = element;
        return returnval;
        }

    public boolean removeAll(final Collection c)
        {
        boolean flag = false;
        Iterator iterator = c.iterator();
        while(iterator.hasNext())
            if (remove(iterator.next())) flag = true;
        return flag;
        }

    public boolean retainAll(final Collection c)
        {
        boolean flag = false;
        for(int x=0;x<numObjs;x++)
            if (!c.contains(objs[x]))
                {
                flag = true;
                remove(x);
                x--; // consider the newly-swapped-in item
                }
        return flag;
        }

    /** Removes the object at the given index, shifting the other objects down. */
    public Object removeNondestructively(final int index)
        {
        if (index>=numObjs) // || index < 0)
            throwIndexOutOfBoundsException(index);
        Object ret = objs[index];
        if (index < numObjs - 1)  // it's not the topmost object, must swap down
            System.arraycopy(objs, index+1, objs, index, numObjs - index - 1);
        objs[numObjs-1] = null;  // let GC
        numObjs--;
        return ret;
        }
    
    public boolean remove(final Object o)
        {
        for(int x=0;x<numObjs;x++)
            if (o==null ?  objs[x]==null :  o==objs[x] || o.equals(objs[x])) 
                {
                remove(x);
                return true;
                }
        return false;
        }
        
    /** Removes multiple instantiations of an object */
    public boolean removeMultiply(final Object o)
        {
        boolean flag = false;
        for(int x=0;x<numObjs;x++)
            if (o==null ?  objs[x]==null :  o==objs[x] || o.equals(objs[x])) 
                {
                flag = true;
                remove(x);
                x--;  // to check the next item swapped in...
                }
        return flag;
        }

    /** Removes the object at the given index, moving the topmost object into its position. */
    public Object remove(final int index)
        {
        if (index>=numObjs) // || index < 0)
            throwIndexOutOfBoundsException(index);
        Object ret = objs[index];
        objs[index] = objs[numObjs-1];
        objs[numObjs-1] = null;  // let GC
        numObjs--;
        return ret;
        }
        
    protected void throwIndexOutOfBoundsException(final int index)
        {
        throw new IndexOutOfBoundsException(""+index);
        }
                        
    // does NOT allow the objects to GC
    public void clear()
        {
        numObjs = 0;
        }
        
    public Object[] toArray()
        {
        Object[] o = new Object[numObjs];
        System.arraycopy(objs,0,o,0,numObjs);
        return o;
        }
        
    public Object[] toArray(Object[] o)
        {
        if (o.length < numObjs)
            // make a new array of same type
            o = (Object[]) java.lang.reflect.Array.newInstance(o.getClass().getComponentType(), numObjs);
        // load it up
        System.arraycopy(objs,0,o,0,numObjs);
        // insert null if we need it
        if (o.length > numObjs)
            o[numObjs] = null;
        return null;
        }

    /** NOT fail-fast.  Use this method only if you're
        concerned about accessing numObjs and objs directly.  */
    public Iterator iterator()
        {
        return new BagIterator(this);
        }
    
    /** Always returns null.  This method is to adhere to Indexed. */
    public Class componentType()
        {
        return null;
        }

    /** Sorts the bag according to the provided comparator */
    public void sort(Comparator c) 
        {
        Arrays.sort(objs, 0, numObjs, c);
        }

    /** Replaces all elements in the bag with the provided object. */
    public void fill(Object o)
        {
        // teeny bit faster
        Object[] objs = this.objs;
        int numObjs = this.numObjs;
        
        for(int x=0; x < numObjs; x++)
            objs[x] = o;
        }

    /** Shuffles (randomizes the order of) the Bag */
    public void shuffle(Random random)
        {
        // teeny bit faster
        Object[] objs = this.objs;
        int numObjs = this.numObjs;
        Object obj;
        int rand;
        
        for(int x=numObjs-1; x > 1 ; x--)
            {
            rand = random.nextInt(x+1);
            obj = objs[x];
            objs[x] = objs[rand];
            objs[rand] = obj;
            }
        }
    
    /** Shuffles (randomizes the order of) the Bag */
    public void shuffle(MersenneTwisterFast random)
        {
        // teeny bit faster
        Object[] objs = this.objs;
        int numObjs = this.numObjs;
        Object obj;
        int rand;
        
        for(int x=numObjs-1; x > 1 ; x--)
            {
            rand = random.nextInt(x+1);
            obj = objs[x];
            objs[x] = objs[rand];
            objs[rand] = obj;
            }
        }
    
    /** Reverses order of the elements in the Bag */
    public void reverse()
        {
        // teeny bit faster
        Object[] objs = this.objs;
        int numObjs = this.numObjs;
        int l = numObjs / 2;
        Object obj;
        for(int x=0; x < l; x++)
            {
            obj = objs[x];
            objs[x] = objs[numObjs - x - 1];
            objs[numObjs - x - 1] = obj;
            }
        }

    static class BagIterator implements Iterator, java.io.Serializable
        {
        int obj = 0;
        Bag bag;
        boolean canRemove = false;
        
        public BagIterator(Bag bag) { this.bag = bag; }
        
        public boolean hasNext()
            {
            return (obj < bag.numObjs);
            }
        public Object next()
            {
            if (obj >= bag.numObjs) throw new NoSuchElementException("No More Elements");
            canRemove = true;
            return bag.objs[obj++];
            }
        public void remove()
            {
            if (!canRemove) throw new IllegalStateException("remove() before next(), or remove() called twice");
            // more consistent with the following line than 'obj > bag.numObjs' would be...
            if (obj - 1 >=  bag.numObjs) throw new NoSuchElementException("No More Elements");
            bag.removeNondestructively(obj-1);
            canRemove = false;
            }
        // static inner class -- no need to add a serialVersionUID
        }
    }





/*
Academic Free License ("AFL") v. 3.0

This Academic Free License (the "License") applies to any original work
of authorship (the "Original Work") whose owner (the "Licensor") has
placed the following licensing notice adjacent to the copyright notice
for the Original Work:

Licensed under the Academic Free License version 3.0

1) Grant of Copyright License. Licensor grants You a worldwide,
royalty-free, non-exclusive, sublicensable license, for the duration of
the copyright, to do the following:

    a) to reproduce the Original Work in copies, either alone or as
       part of a collective work;

    b) to translate, adapt, alter, transform, modify, or arrange the
       Original Work, thereby creating derivative works ("Derivative
       Works") based upon the Original Work;

    c) to distribute or communicate copies of the Original Work and
       Derivative Works to the public, UNDER ANY LICENSE OF YOUR
       CHOICE THAT DOES NOT CONTRADICT THE TERMS AND CONDITIONS,
       INCLUDING LICENSOR'S RESERVED RIGHTS AND REMEDIES, IN THIS
       ACADEMIC FREE LICENSE;

    d) to perform the Original Work publicly; and

    e) to display the Original Work publicly.

2) Grant of Patent License. Licensor grants You a worldwide,
royalty-free, non-exclusive, sublicensable license, under patent claims
owned or controlled by the Licensor that are embodied in the Original
Work as furnished by the Licensor, for the duration of the patents, to
make, use, sell, offer for sale, have made, and import the Original Work
and Derivative Works.

3) Grant of Source Code License. The term "Source Code" means the
preferred form of the Original Work for making modifications to it and
all available documentation describing how to modify the Original Work.
Licensor agrees to provide a machine-readable copy of the Source Code of
the Original Work along with each copy of the Original Work that
Licensor distributes. Licensor reserves the right to satisfy this
obligation by placing a machine-readable copy of the Source Code in an
information repository reasonably calculated to permit inexpensive and
convenient access by You for as long as Licensor continues to distribute
the Original Work.

4) Exclusions From License Grant. Neither the names of Licensor, nor the
names of any contributors to the Original Work, nor any of their
trademarks or service marks, may be used to endorse or promote products
derived from this Original Work without express prior permission of the
Licensor. Except as expressly stated herein, nothing in this License
grants any license to Licensor's trademarks, copyrights, patents, trade
secrets or any other intellectual property. No patent license is granted
to make, use, sell, offer for sale, have made, or import embodiments of
any patent claims other than the licensed claims defined in Section 2.
No license is granted to the trademarks of Licensor even if such marks
are included in the Original Work. Nothing in this License shall be
interpreted to prohibit Licensor from licensing under terms different
from this License any Original Work that Licensor otherwise would have a
right to license.

5) External Deployment. The term "External Deployment" means the use,
distribution, or communication of the Original Work or Derivative Works
in any way such that the Original Work or Derivative Works may be used
by anyone other than You, whether those works are distributed or
communicated to those persons or made available as an application
intended for use over a network. As an express condition for the grants
of license hereunder, You must treat any External Deployment by You of
the Original Work or a Derivative Work as a distribution under section
1(c).

6) Attribution Rights. You must retain, in the Source Code of any
Derivative Works that You create, all copyright, patent, or trademark
notices from the Source Code of the Original Work, as well as any
notices of licensing and any descriptive text identified therein as an
"Attribution Notice." You must cause the Source Code for any Derivative
Works that You create to carry a prominent Attribution Notice reasonably
calculated to inform recipients that You have modified the Original
Work.

7) Warranty of Provenance and Disclaimer of Warranty. Licensor warrants
that the copyright in and to the Original Work and the patent rights
granted herein by Licensor are owned by the Licensor or are sublicensed
to You under the terms of this License with the permission of the
contributor(s) of those copyrights and patent rights. Except as
expressly stated in the immediately preceding sentence, the Original
Work is provided under this License on an "AS IS" BASIS and WITHOUT
WARRANTY, either express or implied, including, without limitation, the
warranties of non-infringement, merchantability or fitness for a
particular purpose. THE ENTIRE RISK AS TO THE QUALITY OF THE ORIGINAL
WORK IS WITH YOU. This DISCLAIMER OF WARRANTY constitutes an essential
part of this License. No license to the Original Work is granted by this
License except under this disclaimer.

8) Limitation of Liability. Under no circumstances and under no legal
theory, whether in tort (including negligence), contract, or otherwise,
shall the Licensor be liable to anyone for any indirect, special,
incidental, or consequential damages of any character arising as a
result of this License or the use of the Original Work including,
without limitation, damages for loss of goodwill, work stoppage,
computer failure or malfunction, or any and all other commercial damages
or losses. This limitation of liability shall not apply to the extent
applicable law prohibits such limitation.

9) Acceptance and Termination. If, at any time, You expressly assented
to this License, that assent indicates your clear and irrevocable
acceptance of this License and all of its terms and conditions. If You
distribute or communicate copies of the Original Work or a Derivative
Work, You must make a reasonable effort under the circumstances to
obtain the express assent of recipients to the terms of this License.
This License conditions your rights to undertake the activities listed
in Section 1, including your right to create Derivative Works based upon
the Original Work, and doing so without honoring these terms and
conditions is prohibited by copyright law and international treaty.
Nothing in this License is intended to affect copyright exceptions and
limitations (including "fair use" or "fair dealing"). This License shall
terminate immediately and You may no longer exercise any of the rights
granted to You by this License upon your failure to honor the conditions
in Section 1(c).

10) Termination for Patent Action. This License shall terminate
automatically and You may no longer exercise any of the rights granted
to You by this License as of the date You commence an action, including
a cross-claim or counterclaim, against Licensor or any licensee alleging
that the Original Work infringes a patent. This termination provision
shall not apply for an action alleging patent infringement by
combinations of the Original Work with other software or hardware.

11) Jurisdiction, Venue and Governing Law. Any action or suit relating
to this License may be brought only in the courts of a jurisdiction
wherein the Licensor resides or in which Licensor conducts its primary
business, and under the laws of that jurisdiction excluding its
conflict-of-law provisions. The application of the United Nations
Convention on Contracts for the International Sale of Goods is expressly
excluded. Any use of the Original Work outside the scope of this License
or after its termination shall be subject to the requirements and
penalties of copyright or patent law in the appropriate jurisdiction.
This section shall survive the termination of this License.

12) Attorneys' Fees. In any action to enforce the terms of this License
or seeking damages relating thereto, the prevailing party shall be
entitled to recover its costs and expenses, including, without
limitation, reasonable attorneys' fees and costs incurred in connection
with such action, including any appeal of such action. This section
shall survive the termination of this License.

13) Miscellaneous. If any provision of this License is held to be
unenforceable, such provision shall be reformed only to the extent
necessary to make it enforceable.

14) Definition of "You" in This License. "You" throughout this License,
whether in upper or lower case, means an individual or a legal entity
exercising rights under, and complying with all of the terms of, this
License. For legal entities, "You" includes any entity that controls, is
controlled by, or is under common control with you. For purposes of this
definition, "control" means (i) the power, direct or indirect, to cause
the direction or management of such entity, whether by contract or
otherwise, or (ii) ownership of fifty percent (50%) or more of the
outstanding shares, or (iii) beneficial ownership of such entity.

15) Right to Use. You may use the Original Work in all ways not
otherwise restricted or conditioned by this License or by law, and
Licensor promises not to interfere with or be responsible for such uses
by You.

16) Modification of This License. This License is Copyright (c) 2005
Lawrence Rosen. Permission is granted to copy, distribute, or
communicate this License without modification. Nothing in this License
permits You to modify this License as applied to the Original Work or to
Derivative Works. However, You may modify the text of this License and
copy, distribute or communicate your modified version (the "Modified
License") and apply it to other original works of authorship subject to
the following conditions: (i) You may not indicate in any way that your
Modified License is the "Academic Free License" or "AFL" and you may not
use those names in the name of your Modified License; (ii) You must
replace the notice specified in the first paragraph above with the
notice "Licensed under <insert your license name here>" or with a notice
of your own that is not confusingly similar to the notice in this
License; and (iii) You may not claim that your original works are open
source software unless your Modified License has been approved by Open
Source Initiative (OSI) and You comply with its license review and
certification process.


*/