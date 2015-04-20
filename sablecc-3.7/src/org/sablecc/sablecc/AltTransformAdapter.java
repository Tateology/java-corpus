/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * This file is part of SableCC.                             *
 * See the file "LICENSE" for copyright information and the  *
 * terms and conditions for copying, distribution and        *
 * modification of SableCC.                                  *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package org.sablecc.sablecc;

import java.util.*;

import org.sablecc.sablecc.analysis.*;
import org.sablecc.sablecc.node.*;

@SuppressWarnings({"rawtypes","unchecked"})
public class AltTransformAdapter extends DepthFirstAdapter
{
  ResolveAltIds altIds;
  String currentNewAltName;

  private Map isElementIsAlist;

  private LinkedList listSimpleTermTransform;
  private Map simpleTermTransform;
  private Map simpleTermOrsimpleListTermTypes;

  AltTransformAdapter(Map simpleTermTransform,
                      LinkedList listSimpleTermTransform,
                      String currentNewAltName,
                      ResolveAltIds altIds, Map isElementIsAlist,
                      Map simpleTermOrsimpleListTermTypes)
  {
    this.currentNewAltName = currentNewAltName;
    this.altIds = altIds;
    this.isElementIsAlist = isElementIsAlist;
    this.listSimpleTermTransform = listSimpleTermTransform;
    this.simpleTermTransform = simpleTermTransform;
    this.simpleTermOrsimpleListTermTypes = simpleTermOrsimpleListTermTypes;
  }

  @Override
  public void inASimpleTerm(ASimpleTerm node)
  {
    String name = node.getId().getText();

    if( !((LinkedList)altIds.alts_elems.get(currentNewAltName)).contains(name) &&
        !((LinkedList)altIds.alts_elems.get(currentNewAltName)).contains("$"+name) )
    {
      node.replaceBy( new ANullTerm() );
    }

    if( isElementIsAlist.get(currentNewAltName+name) != null )
    {
      TId simpleTermTail;
      if(node.getSimpleTermTail() != null)
      {
        simpleTermTail = node.getSimpleTermTail();
      }
      else
      {
        simpleTermTail = new TId( (String)isElementIsAlist.get(currentNewAltName+name) );
      }

      ASimpleTerm asimpleTerm = new ASimpleTerm( node.getSpecifier(), node.getId(), simpleTermTail);

      if(simpleTermOrsimpleListTermTypes.get(node) != null)
      {
        simpleTermOrsimpleListTermTypes.put(asimpleTerm, (String)simpleTermOrsimpleListTermTypes.get(node));
      }

      node.replaceBy(asimpleTerm);
      simpleTermTransform.put(asimpleTerm, "L"+ResolveIds.name((String)isElementIsAlist.get(currentNewAltName+name)) );

      //Terms are added here only if they were implicitely transformed
      listSimpleTermTransform.add( asimpleTerm );
    }
  }

  @Override
  public void inASimpleListTerm(ASimpleListTerm node)
  {
    String name = node.getId().getText();

    if( !((LinkedList)altIds.alts_elems.get(currentNewAltName)).contains(name) &&
        !((LinkedList)altIds.alts_elems.get(currentNewAltName)).contains("$"+name) )
    {
      node.replaceBy( null );
    }

    if( isElementIsAlist.get(currentNewAltName+name) != null)
    {
      TId simpleTermTail;
      if(node.getSimpleTermTail() != null)
      {
        simpleTermTail = node.getSimpleTermTail();
      }
      else
      {
        simpleTermTail = new TId((String)isElementIsAlist.get(currentNewAltName+name));
      }

      TId tid;
      tid = ( ((LinkedList)altIds.alts_elems_list_elemName.get(currentNewAltName)).contains(name) ?
              node.getId() : new TId( "$" + node.getId().getText() ) );

      ASimpleListTerm asimpleListTerm = new ASimpleListTerm( node.getSpecifier(), tid, simpleTermTail);

      if(simpleTermOrsimpleListTermTypes.get(node) != null)
      {
        simpleTermOrsimpleListTermTypes.put(asimpleListTerm, (String)simpleTermOrsimpleListTermTypes.get(node));
      }

      node.replaceBy(asimpleListTerm);
      simpleTermTransform.put(asimpleListTerm, "L"+ResolveIds.name((String)isElementIsAlist.get(currentNewAltName+name) ));

      //Terms are added here only if they were implicitely transformed
      listSimpleTermTransform.add( asimpleListTerm );
    }
  }

  @Override
  public void outAListTerm(AListTerm node)
  {
    if( (node.getListTerms() != null) && (node.getListTerms().size() > 0) )
    {
      Object[] temp = node.getListTerms().toArray();

      if(simpleTermTransform.get(temp[0]) != null)
      {
        String firstTermType = (String)simpleTermTransform.get(temp[0]);

        if(firstTermType != null)
        {
          if(!firstTermType.startsWith("L"))
          {
            simpleTermTransform.put(node, "L" + firstTermType);
          }
          else
          {
            simpleTermTransform.put(node, firstTermType);
          }
        }
      }
    }
  }

}
