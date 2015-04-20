/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * This file is part of SableCC.                             *
 * See the file "LICENSE" for copyright information and the  *
 * terms and conditions for copying, distribution and        *
 * modification of SableCC.                                  *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * Last Modification date : 03-11-2003
 * Remove the checking of question mark and + operator
 * for production tranformations
 * I've commented out the method
 * public void caseAProd(AProd node) { ... }
 * Date : 15-01-2003 : 
 * The method is now removed because it is not used anymore
 */

package org.sablecc.sablecc;

import java.util.*;

import org.sablecc.sablecc.analysis.*;
import org.sablecc.sablecc.node.*;

/*
 * ResolveAltIds
 * 
 * This class computes semantic verifications for AST alternatives
 * section. The same thing is done by ResolveIds class for Productions
 * section.
 */

@SuppressWarnings({"rawtypes","unchecked"})
public class ResolveAltIds extends DepthFirstAdapter
{
  public ResolveIds ids;

  //Map of alternatives elements which are not list :
  // ie not followed by * or + operator.
  public Map alts_elems = new TypedTreeMap(
                            StringComparator.instance,
                            StringCast.instance,
                            ListCast.instance);

  //Map of only alternatives elements which are list :
  //followed by * or + operator.
  public Map alts_elems_list = new TypedTreeMap(
                                 StringComparator.instance,
                                 StringCast.instance,
                                 ListCast.instance);

  //Map of all alternatives elements. Elements name are stored
  //if it is specified otherwise, it is its id.
  //(elem = elem_name? specifier? id un_op?)
  public Map alts_elemsGlobal = new TypedTreeMap(
                                  StringComparator.instance,
                                  StringCast.instance,
                                  ListCast.instance);

  //Map of all alternatives elements which have explicit name.
  public Map alts_elems_list_elemName = new TypedTreeMap(
                                          StringComparator.instance,
                                          StringCast.instance,
                                          ListCast.instance);

  private LinkedList listElemsGlobal;
  private LinkedList listElems;
  private LinkedList listElemslist;

  String currentAlt;

  //This is true if the current elem is a list and false otherwise
  private boolean blist;

  public ResolveAltIds(ResolveIds ids)
  {
    this.ids = ids;
  }

  /*
   * This method is checking if there is QMark or Plus Operator in in the 
   * list of productions transformations elements
   */

  @Override
  public void caseAProd(AProd node)
  {
//    AElem []temp = (AElem[]) node.getProdTransform().toArray(new AElem[0]);

    Object []list_alts = node.getAlts().toArray();
    for(int j=0; j<list_alts.length; j++)
    {
      ((PAlt)list_alts[j]).apply(this);
    }
  }

  /*
   * Here, a map which associate the current alternative with the list of elems
   * is created.
   */
  @Override
  public void caseAAlt(AAlt alt)
  {
    //contains all the elements in the alternative, no matter if they are list or not
    listElemsGlobal = new LinkedList();

    //contains only single (without operator * or +) element of the alternative.
    listElems = new LinkedList();

    //contains only element of the alternative which are list(operator * or +).
    listElemslist = new LinkedList();

    currentAlt = (String)ids.names.get(alt);

    AElem[] list_elems = (AElem[])alt.getElems().toArray(new AElem[0]);
    for(int i=0; i<list_elems.length; i++)
    {
      list_elems[i].apply(this);
    }

    alts_elemsGlobal.put(currentAlt, listElemsGlobal);
    alts_elems.put(currentAlt, listElems);
    alts_elems_list.put(currentAlt, listElemslist);
  }

  @Override
  public void caseAElem(final AElem elem)
  {
    blist = false;
    if( (elem.getUnOp() != null) &&
        ((elem.getUnOp() instanceof AStarUnOp) || (elem.getUnOp() instanceof APlusUnOp)) )
    {
      blist = true;
    }

    String elem_name = (elem.getElemName() != null ? elem.getElemName().getText() : elem.getId().getText() );
    if(!blist)
    {
      listElems.add(elem_name);
    }
    else
    {
      listElemslist.add(elem_name);
    }

    listElemsGlobal.add(elem_name);
  }

  //This method is overriding in order to not allow ASt traversal to visit
  //AST elements.
  @Override
  public void caseAAst(AAst node)
  {}

  @Override
  public String toString()
  {
    StringBuffer s = new StringBuffer();
    String nl = System.getProperty("line.separator");

    s.append("Alternative elements : ");
    s.append(nl);
    s.append(alts_elems);
    s.append(nl);

    return s.toString();
  }
}
