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
 * Last Modification date : 23 07 2004
 * fix bug : when an inlining of a production does not resolve a conflict
 * the associated alternatives transformation should not be transformed.
 * The bug was about transforming this instead.
*/

/*
 * ComputeInlining
 * This class takes SableCC grammar represented by the tree
 * and a list of production to inline within this grammar and
 * try to inline those productions.
 */

@SuppressWarnings({"rawtypes","unchecked"})
public class ComputeInlining
{
  //Productions implied in a conflict
  private Set setOfProdToBeInline;

  //Map of all productions in the grammar
  private Map productionsMap;
  private Start tree;

  public ComputeInlining(Set set
                           ,
                           Map productionsMap,
                           Start tree)
  {
    this.setOfProdToBeInline = set
                                 ;
    this.productionsMap = productionsMap;
    this.tree = tree;
  }

  /**
   * This method compute the inline of a all productions implied in a conflict
   * in the grammar.
   * It returns :
   *    -- true if at least one production is inlined with success 
   *    -- and false otherwise.
   */
  public boolean computeInlining()
  {
    final BooleanEx atLeastOneProductionInlined = new BooleanEx(false);
    String []nameOfProds = (String[])setOfProdToBeInline.toArray(new String[0]);

    for(int i=0; i<nameOfProds.length; i++)
    {
      if(nameOfProds[i].equals("Start")) {
          continue;
      }

      final AProd prod = (AProd)productionsMap.get(nameOfProds[i]);

      //We proceed inlining only if the production to inline is not recursive
      //and if it doesn't have more than SableCC.inliningMaxAlts alternatives.
      if( prod.getAlts().size() <= SableCC.inliningMaxAlts && !isProductionRecursive(prod) )
      {
        //This class construct a special data structure for the production to inline.
        final In_Production in_production = new In_Production((AProd)prod.clone());

        tree.apply(new DepthFirstAdapter()
                   {
                     @Override
                     public void caseAProd(AProd node)
                     {
                       //We do not inline the production itself.
                       if(node.getId().getText().equals(prod.getId().getText()))
                       {
                         return;
                       }

                       Inlining inliningClass = new Inlining(node, in_production);

                       //The proper inlining is done here(method inlineProduction)
                       if( inliningClass.inlineProduction() && !atLeastOneProductionInlined.getValue())
                       {
                         atLeastOneProductionInlined.setValue(true);
                       }
                     }
                   }
                  );
      }
    }

    LinkedList listOfGrammarProds = ((AProductions)((AGrammar)tree.getPGrammar()).getProductions()).getProds();

    //Once the production is inlined, we do not need it anymore, so we remove it from the grammar.
    String[] inlinedProductionsToRemove = (String[])Inlining.productionsToBeRemoved.toArray(new String[0]);
    for(int i=0; i<inlinedProductionsToRemove.length; i++)
    {
      listOfGrammarProds.remove(productionsMap.get(inlinedProductionsToRemove[i]) );
    }

    Inlining.productionsToBeRemoved.clear();
    return atLeastOneProductionInlined.getValue();
  }

  /*
   * A production is recursive if one of its alternatives contains an occurrence
   * of itself.
   */
  public boolean isProductionRecursive(final AProd production)
  {
    final BooleanEx recursive = new BooleanEx(false);
    final String currentProdName = production.getId().getText();

    production.apply(new DepthFirstAdapter()
                     {
                       @Override
                       public void caseAProd(AProd node)
                       {
                         Object temp[] = node.getAlts().toArray();
                         for(int i = 0; i < temp.length; i++)
                         {
                           ((PAlt) temp[i]).apply(this);
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

                       @Override
                       public void caseAElem(AElem node)
                       {
                         if(node.getId().getText().equals(currentProdName))
                         {
                           if(node.getSpecifier() != null && node.getSpecifier() instanceof ATokenSpecifier)
                           {
                             return;
                           }
                           recursive.setValue(true);
                         }
                       }
                     }
                    );
    return recursive.getValue();
  }

  /* This class is used to simulate final Boolean.
   * Since final variable cannot be assigned value more than
   * one time, we need another class which boolean value field
   * can be changed so often as necessary.
   */
  class BooleanEx
  {
    boolean value;

    BooleanEx(boolean value)
    {
      this.value = value;
    }

    void setValue(boolean value)
    {
      this.value = value;
    }

    boolean getValue()
    {
      return value;
    }
  }
}
