/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * This file is part of SableCC.                             *
 * See the file "LICENSE" for copyright information and the  *
 * terms and conditions for copying, distribution and        *
 * modification of SableCC.                                  *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/*
 * Last Modification date :: 04-February-2004
 * Add termtail to simple term and simple listterm
 * in order to support scripting generation for parser by 
 * the new scripting engine.
 *
 * Fix bug related to code generation. 
 * Method #public void caseASimpleTerm(ASimpleTerm node)#
 * and #public void caseASimpleTerm(ASimpleTerm node)#
 * were rewrite. The type of simpleTerm was the problem.
*/

package org.sablecc.sablecc;

import java.util.*;

import org.sablecc.sablecc.analysis.*;
import org.sablecc.sablecc.node.*;

@SuppressWarnings({"rawtypes","unchecked"})
public class ComputeCGNomenclature extends DepthFirstAdapter
{
  private String currentProd;
  private String currentAlt;
  private int counter;
  private ResolveIds ids;
  private ResolveProdTransformIds prodTransformIds;
  private Map altElemTypes;

  private final Map altTransformElemTypes = new TypedHashMap(
        NodeCast.instance,
        StringCast.instance);

  private final Map termNumbers = new TypedHashMap(NodeCast.instance,
                                  IntegerCast.instance);

  public ComputeCGNomenclature(ResolveIds ids, ResolveProdTransformIds prodTransformIds)
  {
    this.ids = ids;
    this.prodTransformIds = prodTransformIds;
  }

  public void setAltElemTypes(Map aMap)
  {
    this.altElemTypes = aMap;
  }

  public Map getAltTransformElemTypes()
  {
    return altTransformElemTypes;
  }

  public Map getTermNumbers()
  {
    return termNumbers;
  }

  @Override
  public void caseAProd(final AProd production)
  {
    currentProd = "P" + ResolveIds.name(production.getId().getText());
    Object []temp = production.getAlts().toArray();
    for(int i = 0; i<temp.length; i++)
    {
      ((PAlt)temp[i]).apply(this);
    }
  }

  @Override
  public void inAAlt(AAlt nodeAlt)
  {
    counter = 0;

    if(nodeAlt.getAltName() != null)
    {
      currentAlt = "A"+
                   ResolveIds.name( nodeAlt.getAltName().getText() )+
                   currentProd.substring(1);
    }
    else
    {
      currentAlt = "A" + currentProd.substring(1);
    }

    counter = 0;
  }

  @Override
  public void caseAAst(AAst node)
  {}

  @Override
  public void inAElem(AElem node)
  {
    String elemType = (String)ids.elemTypes.get(node);

    if(node.getElemName() != null)
    {
      ids.altsElemNameTypes.put(currentAlt+"."+node.getElemName().getText(), elemType );
    }
  }

  @Override
  public void inANewTerm(ANewTerm node)
  {
    AProdName aProdName = (AProdName)node.getProdName();
    String type = "P" + ResolveIds.name(aProdName.getId().getText());

    altTransformElemTypes.put(node, type);
    termNumbers.put(node, new Integer(++counter));
  }

  @Override
  public void inANewListTerm(ANewListTerm node)
  {
    AProdName aProdName = (AProdName)node.getProdName();
    String type = "P" + ResolveIds.name(aProdName.getId().getText());

    altTransformElemTypes.put(node, type);
    termNumbers.put(node, new Integer(++counter));
  }

  @Override
  public void outAListTerm(AListTerm node)
  {
    if( node.getListTerms().size() > 0 )
    {
      Object[] temp = node.getListTerms().toArray();

      String firstTermType = (String)altTransformElemTypes.get(temp[0]);

      if(firstTermType != null)
      {
        if(!firstTermType.startsWith("L"))
        {
          altTransformElemTypes.put(node, "L" + firstTermType);
        }
        else
        {
          altTransformElemTypes.put(node, firstTermType);
        }
      }
    }
    else
    {
      altTransformElemTypes.put(node, "Lnull");
    }
    termNumbers.put(node, new Integer(++counter));
  }

  @Override
  public void caseASimpleTerm(ASimpleTerm node)
  {
    String name;
    String elemType = (String) this.altElemTypes.get( currentAlt+"."+node.getId().getText() );

    if(node.getSimpleTermTail() == null)
    {
      name = elemType;
      if(name.startsWith("P") )
      {
        //add termtail to the simpleterm
        node.setSimpleTermTail( (TId)node.getId().clone() );
      }
    }
    else
    {
      String termTail = node.getSimpleTermTail().getText();
      name = (String)prodTransformIds.prodTransformElemTypesString.get(elemType+"."+termTail);
    }

    if(name.endsWith("?"))
    {
      name = name.substring(0, name.length()-1);
    }

    altTransformElemTypes.put(node, name);
    termNumbers.put(node, new Integer(++counter));
  }

  /*
  public void caseASimpleTerm(ASimpleTerm node)
  {
  String name;
  String elemType = (String)altElemTypes.get( currentAlt+"."+node.getId().getText() );

  if( ( (elemType != null) && elemType.startsWith("T") ) ||
  ( (elemType == null) && ids.tokens.get("T" + ids.name(node.getId().getText())) != null ) )
  {
    if(elemType != null)
    {
      name = elemType;
    }
    else
    {
      name = "T" + ids.name(node.getId().getText());
    }
  }
  else
  {
    if(node.getSimpleTermTail() == null)
    {
      if(elemType != null)
  {
        name = elemType;
  }
  else
  {
  name = "P" + ids.name(node.getId().getText());
  }

  //add termtail to the simpleterm
  node.setSimpleTermTail( (TId)node.getId().clone() );
    }
    else
    {
      String prodType;
  if(elemType != null)
  {
        prodType = elemType;
  }
  else
  {
        prodType = "P" + ids.name(node.getId().getText());
  }

  String termTail = node.getSimpleTermTail().getText();
  name = (String)prodTransformIds.prodTransformElemTypesString.get(prodType+"."+termTail);
    }
  }

  if(name.endsWith("?"))
  {
    name = name.substring(0, name.length()-1);
  }
  altTransformElemTypes.put(node, name);
  termNumbers.put(node, new Integer(++counter));
  }
  */
  @Override
  public void caseANullTerm(ANullTerm node)
  {
    altTransformElemTypes.put(node, "null");
    termNumbers.put(node, new Integer(++counter));
  }

  @Override
  public void caseASimpleListTerm(ASimpleListTerm node)
  {
    String name;
    String elemType = (String)altElemTypes.get( currentAlt+"."+node.getId().getText() );

    if(node.getSimpleTermTail() == null)
    {
      name = elemType;
      if( name.startsWith("P") )
      {
        //add termtail to the simpleterm
        node.setSimpleTermTail( (TId)node.getId().clone() );
      }
    }
    else
    {
      String termTail = node.getSimpleTermTail().getText();
      name = (String)prodTransformIds.prodTransformElemTypesString.get(elemType+"."+termTail);
    }

    if(name.endsWith("?"))
    {
      name = name.substring(0, name.length()-1);
    }
    altTransformElemTypes.put(node, name);
    termNumbers.put(node, new Integer(++counter));
  }
  /*
  public void caseASimpleListTerm(ASimpleListTerm node)
  {
  String name;
  String elemType = (String)altElemTypes.get( currentAlt+"."+node.getId().getText() );

  if( ( (elemType != null) && elemType.startsWith("T") ) ||
  ( (elemType == null) && ids.tokens.get("T" + ids.name(node.getId().getText())) != null ) )
  {
    if(elemType != null)
    {
      name = elemType;
    }
    else
    {
  name = "T" + ids.name(node.getId().getText());
    }
  }
  //it seems to be a production without a specifier
  else
  {
    if(node.getSimpleTermTail() == null)
    {
      if(elemType != null)
  {
        name = elemType;
  }
  else
  {
  name = "P" + ids.name(node.getId().getText());
  }

  //add termtail to the simpleterm
  node.setSimpleTermTail( (TId)node.getId().clone() );
    }
    else
    {
      String prodType;
  if(elemType != null)
      {
        prodType = elemType;
  }
  else
      {
        prodType = "P" + ids.name(node.getId().getText());
  }
  String termTail = node.getSimpleTermTail().getText();
  name = (String)prodTransformIds.prodTransformElemTypesString.get(prodType+"."+termTail);
    }
  }
  if(name.endsWith("?"))
  {
    name = name.substring(0, name.length()-1);
  }
  altTransformElemTypes.put(node, name);
  termNumbers.put(node, new Integer(++counter));
  }
  */
}
