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
public class RecursiveProductionsDetections extends DepthFirstAdapter
{
  public LinkedList listOfRecursiveProds = new TypedLinkedList(StringCast.instance);
  private String currentProd;

  @Override
  public void caseAProd(AProd node)
  {
    currentProd = node.getId().getText();
    if(!node.getId().getText().startsWith("$"))
    {
      Object []alts = node.getAlts().toArray();

      for(int i=0; i<alts.length; i++)
      {
        ((PAlt)alts[i]).apply(this);
      }
    }
    else
    {
      listOfRecursiveProds.add( ResolveIds.name(currentProd) );
    }
  }

  @Override
  public void caseAAlt(AAlt node)
  {
    Object temp[] = node.getElems().toArray();
    for(int i = 0; i < temp.length; i++)
    {
      ((PElem) temp[i]).apply(this);
    }
  }
  /*
  public void caseAIgnoredAlt(AIgnoredAlt node)
  {
  Object temp[] = node.getElems().toArray();
  for(int i = 0; i < temp.length; i++)
  {
    ((PElem) temp[i]).apply(this);
  }
  }
  */

  @Override
  public void caseAElem(AElem node)
  {
    if(node.getId().getText().equals(currentProd))
    {
      if(node.getSpecifier() != null && node.getSpecifier() instanceof ATokenSpecifier)
      {
        return;
      }
      if( !listOfRecursiveProds.contains(ResolveIds.name(currentProd)) )
      {
        listOfRecursiveProds.add( ResolveIds.name(currentProd) );
      }
    }
  }
}
