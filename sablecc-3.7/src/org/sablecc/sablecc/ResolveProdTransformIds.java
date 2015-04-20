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

/*
 * ResolveProdTransformIds
 * 
 * This class computes semantic verifications for production transformations
 * in Production section. It makes sure that all transformations specified
 * for a production(prod {-> prod_transform1 prod_transform2 prod_transform3...}) 
 * are defined as production in the section Abstract Syntax Tree.
 */

@SuppressWarnings({"rawtypes","unchecked"})
public class ResolveProdTransformIds extends DepthFirstAdapter
{
  private ResolveAstIds transformIds;

  private LinkedList listElems;

  private String prod_name;

  public LinkedList listProdTransformList = new LinkedList();
  private LinkedList listProdTransformContainsList;

  //Map of production transformation element type. The key of this map
  //is the node of this element in the AST.
  public final Map prodTransformElemTypes = new TypedHashMap(
        NoCast.instance,
        StringCast.instance);

  //This map contains the same information as the other one just above.
  //But the keys for this map are String ("ProdName.ElemTransformationName")
  public final Map prodTransformElemTypesString = new TypedHashMap(
        StringCast.instance,
        StringCast.instance);

  //Map of Productions which transformations contains list elements.
  public Map mapProdTransformContainsList = new TypedTreeMap(
        StringComparator.instance,
        StringCast.instance,
        ListCast.instance);

  //Map of all Production transformations elements.
  public final Map prod_transforms = new TypedTreeMap(
                                       StringComparator.instance,
                                       StringCast.instance,
                                       NoCast.instance);

  public ResolveProdTransformIds(ResolveAstIds ids)
  {
    transformIds = ids;
  }

  @Override
  public void caseAProd(final AProd production)
  {
    prod_name = (String)transformIds.astIds.names.get(production);

    AElem temp[] =
      (AElem [])production.getProdTransform().toArray(new AElem[0]);

    listProdTransformContainsList = new LinkedList();

    listElems = new LinkedList();

    if( temp.length > 1 )
    {
      listProdTransformList.add(prod_name);
    }

    for(int i=0; i<temp.length; i++)
    {
      ((PElem) temp[i]).apply(new DepthFirstAdapter()
                              {
                                @Override
                                public void caseAElem(AElem node)
                                {
                                  String rname = node.getId().getText();
                                  String name = ResolveIds.name(rname);
                                  String elemName = null;

                                  if(node.getElemName() != null)
                                  {
                                    elemName = node.getElemName().getText();
                                    if( listElems.contains(elemName) )
                                    {
                                      error(node.getElemName(), elemName);
                                    }
                                    listElems.add(elemName);
                                  }
                                  else
                                  {
                                    if(listElems.contains(rname))
                                    {
                                      error(node.getId(), rname);
                                    }

                                    listElems.add(rname);
                                  }

                                  if( (node.getUnOp() != null) &&
                                      ( (node.getUnOp() instanceof AStarUnOp) ||
                                        (node.getUnOp() instanceof APlusUnOp) ) )
                                  {

                                    listProdTransformList.add(prod_name);

                                    if(node.getElemName() != null)
                                    {
                                      listProdTransformContainsList.add(elemName);
                                    }
                                    else
                                    {
                                      listProdTransformContainsList.add(node.getId().getText());
                                    }
                                  }

                                  if(node.getSpecifier() != null)
                                  {
                                    if(node.getSpecifier() instanceof ATokenSpecifier)
                                    {
                                      if(transformIds.astIds.tokens.get("T" + name) == null)
                                      {
                                        error2(node.getId(), "T" + name);
                                      }

                                      if(transformIds.astIds.ignTokens.get("T" + name) != null)
                                      {
                                        error3(node.getId(), "T" + name);
                                      }

                                      /*****************************************************/
                                      String type_name = name;
                                      if( (node.getUnOp() instanceof AStarUnOp) ||
                                          (node.getUnOp() instanceof AQMarkUnOp) )
                                      {
                                        type_name += "?";
                                      }

                                      if( (node.getUnOp() instanceof AStarUnOp) ||
                                          (node.getUnOp() instanceof APlusUnOp) )
                                      {
                                        prodTransformElemTypes.put(node, "LT" + type_name);
                                        if(elemName != null)
                                        {
                                          prodTransformElemTypesString.put(prod_name+"."+elemName, "LT" + type_name);
                                        }
                                        else
                                        {
                                          prodTransformElemTypesString.put(prod_name+"."+rname, "LT" + type_name);
                                        }
                                      }
                                      else
                                      {
                                        prodTransformElemTypes.put(node, "T" + type_name);
                                        if(elemName != null)
                                        {
                                          prodTransformElemTypesString.put(prod_name+"."+elemName, "T" + type_name);
                                        }
                                        else
                                        {
                                          prodTransformElemTypesString.put(prod_name+"."+rname, "T" + type_name);
                                        }
                                      }
                                    }
                                    else
                                    {
                                      if(transformIds.ast_prods.get("P" + name) == null)
                                      {
                                        error5(node.getId(), "P" + name);
                                      }

                                      /*****************************************************/
                                      String type_name = name;
                                      if( (node.getUnOp() instanceof AStarUnOp) ||
                                          (node.getUnOp() instanceof AQMarkUnOp) )
                                      {
                                        type_name += "?";
                                      }

                                      if( (node.getUnOp() instanceof AStarUnOp) ||
                                          (node.getUnOp() instanceof APlusUnOp) )
                                      {
                                        prodTransformElemTypes.put(node, "LP" + type_name);
                                        if(elemName != null)
                                        {
                                          prodTransformElemTypesString.put(prod_name+"."+elemName, "LP" + type_name);
                                        }
                                        else
                                        {
                                          prodTransformElemTypesString.put(prod_name+"."+rname, "LP" + type_name);
                                        }
                                      }
                                      else
                                      {
                                        prodTransformElemTypes.put(node, "P" + type_name);
                                        if(elemName != null)
                                        {
                                          prodTransformElemTypesString.put(prod_name+"."+elemName, "P" + type_name);
                                        }
                                        else
                                        {
                                          prodTransformElemTypesString.put(prod_name+"."+rname, "P" + type_name);
                                        }
                                      }
                                    }
                                  }
                                  else
                                  {
                                    Object token = transformIds.astIds.tokens.get("T" + name);
//                                    Object ignToken = transformIds.astIds.ignTokens.get("T" + name);
                                    Object production = transformIds.astIds.prods.get("P" + name);
                                    Object ast_production = transformIds.ast_prods.get("P" + name);

                                    if((token == null) && (ast_production == null) && (production == null))
                                    {
                                      error2(node.getId(), "P" + name + " and T" + name );
                                    }

                                    //it seems to be a token
                                    if(token != null)
                                    {
//                                      boolean bast_production = false;
                                      //if it's also a ast_production
                                      if(ast_production != null)
                                      {
                                        error4(node.getId(), "P" + name + " and T" + name);
                                      }

                                      /*****************************************************/
                                      String type_name = name;
                                      if( (node.getUnOp() instanceof AStarUnOp) ||
                                          (node.getUnOp() instanceof AQMarkUnOp) )
                                      {
                                        type_name += "?";
                                      }

                                      if( (node.getUnOp() instanceof AStarUnOp) ||
                                          (node.getUnOp() instanceof APlusUnOp) )
                                      {
                                        prodTransformElemTypes.put(node, "LT" + type_name);
                                        if(elemName != null)
                                        {
                                          prodTransformElemTypesString.put(prod_name+"."+elemName, "LT" + type_name);
                                        }
                                        else
                                        {
                                          prodTransformElemTypesString.put(prod_name+"."+rname, "LT" + type_name);
                                        }
                                      }
                                      else
                                      {
                                        prodTransformElemTypes.put(node, "T" + type_name);
                                        if(elemName != null)
                                        {
                                          prodTransformElemTypesString.put(prod_name+"."+elemName, "T" + type_name);
                                        }
                                        else
                                        {
                                          prodTransformElemTypesString.put(prod_name+"."+rname, "T" + type_name);
                                        }
                                      }
                                    }
                                    // The element is supposed to be a Production in section AST
                                    else
                                    {
                                      if(ast_production == null)
                                      {
                                        error5(node.getId(), node.getId().getText());
                                      }

                                      /*****************************************************/
                                      String type_name = name;
                                      if( (node.getUnOp() instanceof AStarUnOp) ||
                                          (node.getUnOp() instanceof AQMarkUnOp) )
                                      {
                                        type_name += "?";
                                      }

                                      if( (node.getUnOp() instanceof AStarUnOp) ||
                                          (node.getUnOp() instanceof APlusUnOp) )
                                      {
                                        prodTransformElemTypes.put(node, "LP" + type_name);
                                        if(elemName != null)
                                        {
                                          prodTransformElemTypesString.put(prod_name+"."+elemName, "LP" + type_name);
                                        }
                                        else
                                        {
                                          prodTransformElemTypesString.put(prod_name+"."+rname, "LP" + type_name);
                                        }
                                      }
                                      else
                                      {
                                        prodTransformElemTypes.put(node, "P" + type_name);
                                        if(elemName != null)
                                        {
                                          prodTransformElemTypesString.put(prod_name+"."+elemName, "P" + type_name);
                                        }
                                        else
                                        {
                                          prodTransformElemTypesString.put(prod_name+"."+rname, "P" + type_name);
                                        }
                                      }
                                    }
                                  }
                                }
                              }
                             );
    }
    prod_transforms.put(prod_name, listElems);
    mapProdTransformContainsList.put(prod_name, listProdTransformContainsList);
  }

  private static void error(Token token, String name)
  {
    throw new RuntimeException(
      "[" + token.getLine() + "," + token.getPos() + "] " +
      "Redefinition of " + name + ".");
  }

  private static void error2(Token token, String name)
  {
    throw new RuntimeException(
      "[" + token.getLine() + "," + token.getPos() + "] " +
      name + " undefined.");
  }

  private static void error3(Token token, String name)
  {
    throw new RuntimeException(
      "[" + token.getLine() + "," + token.getPos() + "] " +
      name + " is ignored.");
  }

  private static void error4(Token token, String name)
  {
    throw new RuntimeException(
      "[" + token.getLine() + "," + token.getPos() + "] " +
      "ambiguous " + name + ".");
  }

  private static void error5(Token token, String name)
  {
    throw new RuntimeException(
      "[" + token.getLine() + "," + token.getPos() + "] " +
      name+ " must be a production defined in section AST.");
  }
}
