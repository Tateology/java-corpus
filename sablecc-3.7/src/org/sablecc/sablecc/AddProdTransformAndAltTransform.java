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
 * AddProdTransformAndAltTransform
 * 
 * This class provide the second part of the support by SableCC3.x.x 
 * for SableCC2.x.x grammars.
 * Its role is to add Productions and Alternatives transformations within
 * Productions section.
 * Assuming this is run after the eventual AddAstProductions it also add
 * default transformations to productions and alternatives which have not
 * not specified them.
 */

@SuppressWarnings({"rawtypes","unchecked"})
public class AddProdTransformAndAltTransform extends DepthFirstAdapter
{

  private String currentProdId;
//  private String currentAlt;

  @Override
  public void inAProd(final AProd production)
  {
    currentProdId = production.getId().getText();

    if(production.getArrow() == null)
    {
      AElem elem = new AElem(null, new AProductionSpecifier(), new TId(currentProdId), null);
      LinkedList listOfProdTransformElem = new LinkedList();
      listOfProdTransformElem.add(elem);
      production.setProdTransform(listOfProdTransformElem);
      production.setArrow(new TArrow());
    }
  }

  private int i;
  private LinkedList list;

  @Override
  public void inAAlt(AAlt alt)
  {
    if(alt.getAltTransform() == null)
    {
//      currentAlt = currentProdId;
      list = new LinkedList();
      AProdName aProdName = new AProdName(new TId(currentProdId), null);

      if(alt.getAltName() != null)
      {
        aProdName.setProdNameTail( new TId(alt.getAltName().getText()) );
      }

      if( alt.getElems().size() > 0 )
      {
        Object temp[] = alt.getElems().toArray();

        for(i = 0; i < temp.length; i++)
        {
          ((PElem) temp[i]).apply(new DepthFirstAdapter()
                                  {
                                    @Override
                                    public void caseAElem(AElem elem)
                                    {
                                      PTerm term;
                                      String termId;
                                      boolean elemNameExplicitelySpecified = false;

                                      if(elem.getElemName() != null)
                                      {
                                        termId = elem.getElemName().getText();
                                        elemNameExplicitelySpecified = true;
                                      }
                                      else
                                      {
                                        termId = elem.getId().getText();
                                      }

                                      if( (elem.getUnOp() != null) &&
                                          ( (elem.getUnOp() instanceof AStarUnOp) || (elem.getUnOp() instanceof APlusUnOp) ) )
                                      {
                                        LinkedList listP = new LinkedList();
                                        if( !elemNameExplicitelySpecified && (elem.getSpecifier()!= null) )
                                        {
                                          if(elem.getSpecifier() instanceof ATokenSpecifier)
                                          {
                                            listP.add( new ASimpleListTerm(new ATokenSpecifier(), new TId(termId), null ) );
                                            term = new AListTerm(new TLBkt(), listP);
                                          }
                                          else
                                          {
                                            listP.add( new ASimpleListTerm(new AProductionSpecifier(), new TId(termId), null ) );
                                            term = new AListTerm(new TLBkt(), listP);
                                          }
                                        }
                                        else
                                        {
                                          listP.add( new ASimpleListTerm(null, new TId(termId), null) );
                                          term = new AListTerm(new TLBkt(), listP);
                                        }
                                      }
                                      else
                                      {
                                        if( !elemNameExplicitelySpecified && (elem.getSpecifier()!= null) )
                                        {

                                          if(elem.getSpecifier() instanceof ATokenSpecifier)
                                          {
                                            term = new ASimpleTerm( new ATokenSpecifier(), new TId(termId), null);
                                          }
                                          else
                                          {
                                            term = new ASimpleTerm( new AProductionSpecifier(), new TId(termId), null);
                                          }
                                        }
                                        else
                                        {
                                          term = new ASimpleTerm( null, new TId(termId), null);
                                        }
                                      }

                                      list.add(term);
                                    }
                                  }
                                 );
        }
      }

      ANewTerm newTerm = new ANewTerm(aProdName, new TLPar(), list);
      LinkedList lst = new LinkedList();
      lst.add(newTerm);

      alt.setAltTransform(new AAltTransform(new TLBrace(), lst, new TRBrace()));
    }
  }
}
