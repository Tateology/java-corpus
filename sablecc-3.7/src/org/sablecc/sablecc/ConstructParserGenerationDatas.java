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
public class ConstructParserGenerationDatas extends DepthFirstAdapter
{
  private ResolveIds ids;
  private int currentAlt;
//  private boolean processingAst;
  private String currentProd;

  private Map alts;

  public ConstructParserGenerationDatas(ResolveIds ids, Map alts)
  {
    this.ids = ids;
    this.alts = alts;
  }

  @Override
  public void caseAAst(AAst node)
  {}

  @Override
  public void caseAProd(AProd node)
  {
    currentProd = (String) ids.names.get(node);
    AAlt[] alts = (AAlt[])node.getAlts().toArray(new AAlt[0]);
    for(int i=0; i<alts.length; i++)
    {
      alts[i].apply(this);
    }
  }

  @Override
  public void caseAAlt(AAlt node)
  {
    currentAlt = Grammar.addProduction(currentProd, (String) ids.names.get(node));
    alts.put(ids.names.get(node), node);

    AElem[] temp = (AElem[])node.getElems().toArray(new AElem[0]);
    for(int i = 0; i < temp.length; i++)
    {
      temp[i].apply(this);
    }
  }

  @Override
  public void caseAElem(AElem node)
  {
    String name = ResolveIds.name(node.getId().getText());

    if(node.getSpecifier() != null)
    {
      if(node.getSpecifier() instanceof ATokenSpecifier)
      {
        ids.elemTypes.put(node, "T" + name);
      }
      else
      {
        ids.elemTypes.put(node, "P" + name);
      }
    }
    else
    {
      Object token = ids.tokens.get("T" + name);
//      Object production = ids.prods.get("P" + name);

      if(token != null)
      {
        ids.elemTypes.put(node, "T" + name);
      }
      else
      {
        ids.elemTypes.put(node, "P" + name);
      }
    }
    name = (String) ids.elemTypes.get(node);
    Grammar.addSymbolToProduction(name, currentAlt);
  }
}
