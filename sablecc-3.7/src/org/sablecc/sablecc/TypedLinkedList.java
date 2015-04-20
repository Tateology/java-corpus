/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * This file is part of SableCC.                             *
 * See the file "LICENSE" for copyright information and the  *
 * terms and conditions for copying, distribution and        *
 * modification of SableCC.                                  *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package org.sablecc.sablecc;

import java.util.*;

@SuppressWarnings({"rawtypes","unchecked"})
public class TypedLinkedList extends LinkedList
{
  Cast cast;

  public TypedLinkedList()
  {
    super();

    cast = NoCast.instance;
  }

  public TypedLinkedList(Collection c)
  {
    super(c);

    cast = NoCast.instance;
  }

  public TypedLinkedList(Cast cast)
  {
    super();

    this.cast = cast;
  }

  public TypedLinkedList(Collection c, Cast cast)
  {
    super(c);

    this.cast = cast;
  }

  public Cast getCast()
  {
    return cast;
  }

  @Override
  public void addFirst(Object o)
  {
    super.addFirst(cast.cast(o));
  }

  @Override
  public void addLast(Object o)
  {
    super.addLast(cast.cast(o));
  }

  @Override
  public ListIterator listIterator(int index)
  {
    return new TypedLinkedListIterator(super.listIterator(index));
  }

  private class TypedLinkedListIterator implements ListIterator
  {
    ListIterator iterator;

    TypedLinkedListIterator(ListIterator iterator)
    {
      this.iterator = iterator;
    }

    @Override
    public boolean hasNext()
    {
      return iterator.hasNext();
    }

    @Override
    public Object next()
    {
      return iterator.next();
    }

    @Override
    public boolean hasPrevious()
    {
      return iterator.hasPrevious();
    }

    @Override
    public Object previous()
    {
      return iterator.previous();
    }

    @Override
    public int nextIndex()
    {
      return iterator.nextIndex();
    }

    @Override
    public int previousIndex()
    {
      return iterator.previousIndex();
    }

    @Override
    public void remove
      ()
    {
      iterator.remove();
    }

    @Override
    public void set
      (Object o)
    {
      iterator.set(cast.cast(o));
    }

    @Override
    public void add
      (Object o)
    {
      iterator.add(cast.cast(o));
    }
  }
}
