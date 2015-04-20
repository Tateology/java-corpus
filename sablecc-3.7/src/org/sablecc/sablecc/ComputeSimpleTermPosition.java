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
public class ComputeSimpleTermPosition extends DepthFirstAdapter
{
  String currentAlt;
  String currentProd;
  boolean processingParsedAlt;
  private ResolveIds ids;
  private int counter;

  public final Map positionsMap = new TypedHashMap(
                                    StringCast.instance,
                                    StringCast.instance);

  public final Map elems_position = new TypedHashMap(
                                      StringCast.instance,
                                      IntegerCast.instance);

  public ComputeSimpleTermPosition(ResolveIds ids)
  {
    this.ids = ids;
  }

  @Override
  public void inAProd(AProd node)
  {
    currentProd = ResolveIds.name(node.getId().getText());
    ids.names.put(node, currentProd);
  }

  @Override
  public void inAAlt(AAlt node)
  {
    counter = 0;
    processingParsedAlt = true;

    if(node.getAltName() != null)
    {
      currentAlt = "A" +
                   ResolveIds.name( node.getAltName().getText() ) +
                   currentProd;
    }
    else
    {
      currentAlt = "A" + currentProd;
    }

    ids.names.put(node, currentAlt);
  }

  @Override
  public void inAElem(AElem node)
  {
    if(processingParsedAlt)
    {
      String currentElemName;
      if(node.getElemName() != null)
      {
        currentElemName = currentAlt + "." + node.getElemName().getText();
      }
      else
      {
        currentElemName = currentAlt + "." + node.getId().getText();
      }

      elems_position.put(currentElemName, new Integer(++counter));
    }

    if(node.getSpecifier() != null &&
        node.getSpecifier() instanceof ATokenSpecifier)
    {
      return;
    }

//    String name = ResolveIds.name( node.getId().getText() );

    String elemType = (String)ids.elemTypes.get(node);
    if(processingParsedAlt && elemType.startsWith("P"))
    {
      String elemName;
      if(node.getElemName() != null)
      {
        elemName = node.getElemName().getText();
      }
      else
      {
        elemName = node.getId().getText();
      }

      positionsMap.put(currentAlt+"."+elemName, elemType);
    }
  }

  @Override
  public void outAAlt(AAlt node)
  {
    processingParsedAlt = false;
  }
}
