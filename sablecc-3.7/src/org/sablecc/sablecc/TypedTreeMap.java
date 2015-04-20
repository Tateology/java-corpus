/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * This file is part of SableCC.                             *
 * See the file "LICENSE" for copyright information and the  *
 * terms and conditions for copying, distribution and        *
 * modification of SableCC.                                  *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package org.sablecc.sablecc;

import java.util.*;

@SuppressWarnings({"rawtypes","unchecked"})
public class TypedTreeMap extends TreeMap
{
  private Cast keyCast;
  private Cast valueCast;
  private Set entries;

  public TypedTreeMap()
  {
    super();

    keyCast = NoCast.instance;
    valueCast = NoCast.instance;
  }

  public TypedTreeMap(Comparator comparator)
  {
    super(comparator);

    keyCast = NoCast.instance;
    valueCast = NoCast.instance;
  }

  public TypedTreeMap(Map map)
  {
    super();

    keyCast = NoCast.instance;
    valueCast = NoCast.instance;

    Map.Entry[] entries = (Map.Entry[])map.entrySet().toArray(new Map.Entry[0]);
    for(int i=0; i < entries.length; i++)
    {
      this.put(entries[i].getKey(),entries[i].getValue());
    }
  }

  /*
  public TypedTreeMap(Map map)
  {
    super(map);

    keyCast = NoCast.instance;
    valueCast = NoCast.instance;
  }
  */

  public TypedTreeMap(SortedMap smap)
  {
    super(smap.comparator());

    keyCast = NoCast.instance;
    valueCast = NoCast.instance;

    Map.Entry[] entries = (Map.Entry[])smap.entrySet().toArray(new Map.Entry[0]);
    for(int i=0; i < entries.length; i++)
    {
      this.put(entries[i].getKey(),entries[i].getValue());
    }

  }
  /*
  public TypedTreeMap(SortedMap smap)
  {
    super(smap);

    keyCast = NoCast.instance;
    valueCast = NoCast.instance;
  }
  */

  public TypedTreeMap(Cast keyCast, Cast valueCast)
  {
    super();

    this.keyCast = keyCast;
    this.valueCast = valueCast;
  }

  public TypedTreeMap(Comparator comparator, Cast keyCast, Cast valueCast)
  {
    super(comparator);

    this.keyCast = keyCast;
    this.valueCast = valueCast;
  }

  @Override
  public Object clone()
  {
    return new TypedTreeMap(this, keyCast, valueCast);
  }

  public TypedTreeMap(Map map, Cast keyCast, Cast valueCast)
  {
    super();

    this.keyCast = keyCast;
    this.valueCast = valueCast;

    Map.Entry[] entries = (Map.Entry[])map.entrySet().toArray(new Map.Entry[0]);
    for(int i=0; i < entries.length; i++)
    {
      this.put(entries[i].getKey(),entries[i].getValue());
    }

  }

  /*
    public TypedTreeMap(Map map, Cast keyCast, Cast valueCast)
    {
      super(map);

      this.keyCast = keyCast;
      this.valueCast = valueCast;
    }
  */

  public TypedTreeMap(SortedMap smap, Cast keyCast, Cast valueCast)
  {
    super(smap.comparator());

    this.keyCast = keyCast;
    this.valueCast = valueCast;

    Map.Entry[] entries = (Map.Entry[])smap.entrySet().toArray(new Map.Entry[0]);
    for(int i=0; i < entries.length; i++)
    {
      this.put(entries[i].getKey(),entries[i].getValue());
    }

  }

  /*
  public TypedTreeMap(SortedMap smap, Cast keyCast, Cast valueCast)
  {
    super(smap);

    this.keyCast = keyCast;
    this.valueCast = valueCast;
  }
  */

  public Cast getKeyCast()
  {
    return keyCast;
  }

  public Cast getValueCast()
  {
    return valueCast;
  }

  @Override
  public Set entrySet()
  {
    if(entries == null)
    {
      entries = new EntrySet(super.entrySet());
    }

    return entries;
  }

  @Override
  public Object put(Object key, Object value)
  {
    return super.put(keyCast.cast(key), valueCast.cast(value));
  }

  private class EntrySet extends AbstractSet
  {
    private Set set
      ;

    EntrySet(Set set
              )
    {
      this.set = set
                   ;
    }

    @Override
    public int size()
    {
      return set.size();
    }

    @Override
    public Iterator iterator()
    {
      return new EntryIterator(set.iterator());
    }
  }

  private class EntryIterator implements Iterator
  {
    private Iterator iterator;

    EntryIterator(Iterator iterator)
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
      return new TypedEntry((Map.Entry) iterator.next());
    }

    @Override
    public void remove
      ()
    {
      iterator.remove();
    }
  }

  private class TypedEntry implements Map.Entry
  {
    private Map.Entry entry;

    TypedEntry(Map.Entry entry)
    {
      this.entry = entry;
    }

    @Override
    public Object getKey()
    {
      return entry.getKey();
    }

    @Override
    public Object getValue()
    {
      return entry.getValue();
    }

    @Override
    public Object setValue(Object value)
    {
      return entry.setValue(valueCast.cast(value));
    }
  }
}
