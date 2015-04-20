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
public class ConstructProdsMap extends DepthFirstAdapter
{
  public Map productionsMap =
    new TypedTreeMap(StringCast.instance,
                     NodeCast.instance);

  private String currentProd;

  @Override
  public void caseAProd(AProd node)
  {
    currentProd = ResolveIds.name(node.getId().getText());
    productionsMap.put("P" + currentProd, node);
  }
}
