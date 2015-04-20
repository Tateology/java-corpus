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
 * AddAstProductions
 * 
 * This class provide a part of the support of SableCC2.x.x grammars by 
 * SableCC3.x.x.
 * Its role is to add the section Abstract Syntax Tree and its productions
 * to the Grammar based on the Production Section.
 * It's the same result if a copy-paste of Productions section was added
 * to the SableCC2.x.x original grammar and renammed Abstract Syntax Tree.
 */

@SuppressWarnings({"rawtypes","unchecked"})
public class AddAstProductions extends DepthFirstAdapter
{

  LinkedList listAstProd = new TypedLinkedList();
//  private boolean firstAlt;

  public AddAstProductions()
  {}

  @Override
  public void caseAProd(AProd node)
  {
//    firstAlt = true;
    listOfAstAlts = new TypedLinkedList();

    /*
     * Here, we assume that if there is no Abstract Syntax Tree Section specified
     * in the grammar, no transformations syntax is allowed in Productions section
     */
    if(node.getArrow() != null)
    {
      error(node.getArrow());
    }

    Object []list_alt = (Object[]) node.getAlts().toArray();
    for(int i=0; i<list_alt.length; i++)
    {
      ((PAlt)list_alt[i]).apply(this);
    }

    AAstProd astProd = new AAstProd(new TId(node.getId().getText()), listOfAstAlts);
    listAstProd.add(astProd);
  }

  @Override
  public void outAGrammar(AGrammar node)
  {
    node.setAst(new AAst(listAstProd));
  }

  @Override
  public void inAAlt(AAlt node)
  {
    listElems = new TypedLinkedList();
    processingParsedAlt = true;
  }

  @Override
  public void inAAltTransform(AAltTransform node)
  {
    if(node.getLBrace() != null)
    {
      error(node.getLBrace());
    }
  }

  @Override
  public void outAAlt(AAlt node)
  {
    TId aAltname = node.getAltName() == null ? null : (TId)node.getAltName().clone();
    AAstAlt astAlt = new AAstAlt(aAltname, listElems);

    listOfAstAlts.add(astAlt);
    processingParsedAlt = false;
  }

  boolean processingParsedAlt;

  @Override
  public void inAElem(AElem node)
  {
    if(processingParsedAlt)
    {
      AElem tmp = (AElem)node.clone();
      listElems.add(tmp);
    }
  }

  LinkedList listElems;
  LinkedList listOfAstAlts;

  public void error(Token token)
  {
    throw new RuntimeException(
      "[" + token.getLine() + "," + token.getPos() + "] " +
      "AST transformations are not allowed because there are no section Abstract Syntax Tree");
  }
}
