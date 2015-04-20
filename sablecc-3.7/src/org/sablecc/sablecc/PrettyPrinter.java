/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * This file is part of SableCC.                             *
 * See the file "LICENSE" for copyright information and the  *
 * terms and conditions for copying, distribution and        *
 * modification of SableCC.                                  *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package org.sablecc.sablecc;

import org.sablecc.sablecc.analysis.*;
import org.sablecc.sablecc.node.*;

public class PrettyPrinter extends DepthFirstAdapter
{
  public static String production_INDENT = "    " ;
  public static String prod_transform_INDENT = "         ";
  public static String alternative_INDENT = "     " ;
  public static String alt_transform_INDENT = "         " ;

  @Override
  public void caseAProductions(AProductions node)
  {
    System.err.println("Productions \n");
    AProd [] prods =
      (AProd [])node.getProds().toArray(new AProd[0]);
    for(int i = 0; i < prods.length; i++)
    {
      prods[i].apply(this);
    }
  }

  @Override
  public void caseAProd(AProd node)
  {
    System.err.print(production_INDENT + node.getId().getText());
    String hasProdTransform = "=";
    if(node.getArrow() == null)
    {
      hasProdTransform = "";
      System.err.println(" = ");
    }
    System.err.println();

    AElem[] elems = (AElem [])node.getProdTransform().toArray(new AElem[0]);

    //if(node.getArrow() != null)
    if(elems.length > 0)
    {
      System.err.print(prod_transform_INDENT + "{-> ");

      for(int i=0; i<elems.length; i++)
      {
        //System.err.print(elems[i] + " ");
        elems[i].apply(this);
        System.err.print(" ");
      }
      System.err.println(" } " + hasProdTransform);
    }

    Object[] alts = (Object[])node.getAlts().toArray();
    for(int i=0; i<alts.length-1; i++)
    {
      ((PAlt)alts[i]).apply(this);
      System.err.println( " |");
    }
    ((PAlt)alts[alts.length-1]).apply(this);

    System.err.println("\n" + alternative_INDENT + ";\n");
  }

  @Override
  public void caseAAlt(AAlt node)
  {
    System.err.print("\n" + alternative_INDENT);

    if(node.getAltName() != null)
    {
      System.err.print("{" + node.getAltName().getText()+"} ");
    }

    AElem[] listElems = (AElem[])node.getElems().toArray(new AElem[0]);
    for(int i=0; i<listElems.length; i++)
    {
      //System.err.print(listElems[i]);
      listElems[i].apply(this);
      System.err.print(" ");
    }

    if(node.getAltTransform() != null)
    {
      node.getAltTransform().apply(this);
    }
  }

  @Override
  public void caseAAltTransform(AAltTransform node)
  {
    System.err.print("\n" + alt_transform_INDENT + "{-> ");

    Object []terms = (Object[]) node.getTerms().toArray();
    for(int i=0; i<terms.length; i++)
    {
      ((PTerm)terms[i]).apply(this);
      System.err.print(" ");
    }

    System.err.print(" }  ");
  }

  @Override
  public void caseAProdName(AProdName node)
  {
    System.err.print(node.getId().getText());
    if(node.getProdNameTail() != null)
    {
      System.err.print("." + node.getProdNameTail().getText());
    }
  }

  @Override
  public void caseANewTerm(ANewTerm node)
  {
    System.err.print("New ");
    node.getProdName().apply(this);
    System.err.print(" (" );

    Object []params = node.getParams().toArray();
    if(params.length > 0)
    {
      for(int i=0; i<params.length-1; i++)
      {
        ((PTerm)params[i]).apply(this);
        System.err.print(", ");
      }
      ((PTerm)params[params.length-1]).apply(this);
    }
    System.err.print(" )");
  }

  @Override
  public void caseAListTerm(AListTerm node)
  {
    System.err.print("[ ");
    Object []list_terms = node.getListTerms().toArray();

    for(int i=0; i<list_terms.length; i++)
    {
      ((PListTerm)list_terms[i]).apply(this);
    }
    System.err.print(" ]");
  }

  @Override
  public void caseASimpleTerm(ASimpleTerm node)
  {
    if(node.getSpecifier() != null)
    {
      if(node.getSpecifier() instanceof ATokenSpecifier)
      {
        System.err.print("T.");
      }
      else
      {
        System.err.print("P.");
      }
    }
    System.err.print(node.getId().getText() );
    if(node.getSimpleTermTail() != null)
    {
      System.err.print("." + node.getSimpleTermTail().getText());
    }
    System.err.print(" ");
  }

  @Override
  public void caseANullTerm(ANullTerm node)
  {
    System.err.print("Null ");
  }

  @Override
  public void caseANewListTerm(ANewListTerm node)
  {
    System.err.print("New ");
    node.getProdName().apply(this);
    System.err.print(" (" );

    Object []params = node.getParams().toArray();
    if(params.length > 0)
    {
      for(int i=0; i<params.length-1; i++)
      {
        ((PTerm)params[i]).apply(this);
        System.err.print(", ");
      }
      ((PTerm)params[params.length-1]).apply(this);
    }
    System.err.print(" )");
  }

  @Override
  public void caseASimpleListTerm(ASimpleListTerm node)
  {
    if(node.getSpecifier() != null)
    {
      if(node.getSpecifier() instanceof ATokenSpecifier)
      {
        System.err.print("T.");
      }
      else
      {
        System.err.print("P.");
      }
    }
    System.err.print(node.getId().getText() );
    if(node.getSimpleTermTail() != null)
    {
      System.err.print("." + node.getSimpleTermTail().getText());
    }
    System.err.print(" ");
  }

  @Override
  public void caseAAst(AAst node)
  {
    System.err.print("Abstract Syntax Tree\n");

    AAstProd [] prods =
      (AAstProd [])node.getProds().toArray(new AAstProd[0]);
    for(int i = 0; i < prods.length; i++)
    {
      prods[i].apply(this);
    }
  }

  @Override
  public void caseAAstProd(AAstProd node)
  {
    System.err.println(production_INDENT + node.getId().getText() + " =");

    AAstAlt[] alts = (AAstAlt[])node.getAlts().toArray(new AAstAlt[0]);
    for(int i=0; i<alts.length-1; i++)
    {
      alts[i].apply(this);
      System.err.println( "| ");
    }
    alts[alts.length-1].apply(this);

    System.err.println("\n" + alternative_INDENT + ";\n");
  }

  @Override
  public void caseAAstAlt(AAstAlt node)
  {
    System.err.print(alternative_INDENT);

    if(node.getAltName() != null)
    {
      System.err.print("{" + node.getAltName().getText()+"} ");
    }

    AElem[] listElems = (AElem[])node.getElems().toArray(new AElem[0]);
    for(int i=0; i<listElems.length; i++)
    {
      //System.err.print(listElems[i]);
      listElems[i].apply(this);
      System.err.print(" ");
    }
  }

  @Override
  public void caseAElem(AElem node)
  {
    if(node.getElemName() != null)
    {
      System.err.print("[" + node.getElemName().getText() + "]: ");
    }

    if(node.getSpecifier() != null)
    {
      if(node.getSpecifier() instanceof ATokenSpecifier)
      {
        System.err.print("T.");
      }
      else
      {
        System.err.print("P.");
      }
    }

    System.err.print(node.getId().getText());
    if(node.getUnOp() != null)
    {
      node.getUnOp().apply(new DepthFirstAdapter()
                           {
                             @Override
                             public void caseAStarUnOp(AStarUnOp node)
                             {
                               System.err.print("*");
                             }

                             @Override
                             public void caseAQMarkUnOp(AQMarkUnOp node)
                             {
                               System.err.print("?");
                             }

                             @Override
                             public void caseAPlusUnOp(APlusUnOp node)
                             {
                               System.err.print("+");
                             }
                           }
                          );
    }
  }
}
