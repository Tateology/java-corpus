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
 * AddEventualEmptyTransformationToProductions
 * 
 * This class provide the second part of the support by SableCC3.x.x 
 * for SableCC2.x.x grammars.
 * Its role is to add Productions and Alternatives transformations within
 * Productions section.
 * Assuming this is run after the eventual AddAstProductions it also add
 * default transformations to productions and alternatives which have not
 * not specified them.
 */

@SuppressWarnings("rawtypes")
public class AddEventualEmptyTransformationToProductions extends DepthFirstAdapter
{
  private String currentProd;
//  private String currentAlt;

  private ResolveIds ids;
  private ResolveAstIds ast_ids;

  public AddEventualEmptyTransformationToProductions(ResolveIds ids, ResolveAstIds ast_ids)
  {
    this.ids = ids;
    this.ast_ids = ast_ids;
  }

  @Override
  public void inAProd(AProd node)
  {
    currentProd = (String)ids.names.get(node);

    /* If there is no transformation specified for the production
     * and there is no AST production which has the same name as the current
     * CST production, this production is transformed into an empty
     */
    if(node.getArrow() == null && ast_ids.ast_prods.get(currentProd) == null )
    {
      node.setArrow(new TArrow(node.getId().getLine(), node.getId().getPos()+node.getId().getText().length() ));
      node.setProdTransform(new LinkedList());

      AAlt []alts = (AAlt[]) node.getAlts().toArray(new AAlt[0]);

      for(int i=0; i<alts.length; i++)
      {
        alts[i].apply( new DepthFirstAdapter()
                       {
                         @Override public void inAAlt(AAlt node)
                         {
                           if(node.getAltTransform() != null && ((AAltTransform)node.getAltTransform()).getTerms().size() > 0)
                           {
                             error(((AAltTransform)node.getAltTransform()).getLBrace());
                           }
                           node.setAltTransform( new AAltTransform(new TLBrace(), new LinkedList(), new TRBrace()) );
                         }
                       }
                     );
      }
    }
  }

  private static void error(Token token)
  {
    throw new RuntimeException(
      "[" + token.getLine() + "," + token.getPos() + "] " +
      "This alternative transformation should be transformed to {-> } " );
  }

}
