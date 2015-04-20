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
 * ResolveAstIds
 * 
 * This class computes basic semantic verifications for AST alternatives
 * section. The same thing is done by ResolveIds class for Productions
 * section. It makes sure that there is no conflictual names and it also
 * constructs few symbol tables necessary in the rest of the code.
 */

/**
 * Last Modification date : 18-10-2004
 * correct AST alternative element error bug (error2()) 
 * Now only tokens and AST section's productions can be used in
 * AST alternatives
 *
 * 15-01-2004
 * Remove comment method error1(...)
 *
 */

@SuppressWarnings({"rawtypes","unchecked"})
public class ResolveAstIds extends DepthFirstAdapter
{
  //Map of AST productions. The AST production node can be obtained
  //by giving the name of this production
  // Example :: PAstProd is the name of the declared the following productions
  //            ast_prod = id equal [alts]:ast_alt* semicolon;
  public final Map ast_prods = new TypedTreeMap(
                                 StringComparator.instance,
                                 StringCast.instance,
                                 NodeCast.instance);
  //Same thing that above for AST alternatives.
  public final Map ast_alts = new TypedTreeMap(
                                StringComparator.instance,
                                StringCast.instance,
                                NodeCast.instance);
  //Same thing that above for AST alternatives elements.
  public final Map ast_elems = new TypedTreeMap(
                                 StringComparator.instance,
                                 StringCast.instance,
                                 NodeCast.instance);
  //Map of all names of AST productions.
  //They are essentially used to generate AST node classes.
  public final Map ast_names = new TypedHashMap(
                                 NodeCast.instance,
                                 StringCast.instance);
  public final Map ast_elemTypes = new TypedHashMap(
                                     NodeCast.instance,
                                     StringCast.instance);
  public ResolveIds astIds;

  private String firstAstProduction;
  private String currentProd;
  private String currentAlt;
  private int lastLine;
  private int lastPos;

  public ResolveAstIds(ResolveIds ids)
  {
    astIds = ids;
  }

  public String getFirstAstProduction()
  {
    return firstAstProduction;
  }

  @Override
  public void inAAst(AAst node)
  {
    LinkedList listProds = node.getProds();
    if(listProds.size() > 0)
    {
      AAstProd firstAstProd = (AAstProd)listProds.getFirst();
      firstAstProduction = "P" + ResolveIds.name(firstAstProd.getId().getText());
    }
  }

  @Override
  public void inAAstProd(AAstProd node)
  {
    currentProd = ResolveIds.name(node.getId().getText());

    String name = "P" + currentProd;

    if(ast_prods.put(name, node) != null)
    {
      error(node.getId(), name);
    }
    ast_names.put(node, name);
  }

  @Override
  public void inAAstAlt(final AAstAlt alt)
  {
    if(alt.getAltName() != null)
    {
      currentAlt =
        "A" +
        ResolveIds.name(alt.getAltName().getText()) +
        currentProd;

      if(ast_alts.put(currentAlt, alt) != null)
      {
        error(alt.getAltName(), currentAlt);
      }
      ast_names.put(alt, currentAlt);
    }
    else
    {
      currentAlt = "A" + currentProd;

      if(ast_alts.put(currentAlt, alt) != null)
      {
        error(currentAlt);
      }
      ast_names.put(alt, currentAlt);
    }
  }

  //Only Abstract Syntax Tree section is concerned by the visitor here.
  @Override
  public void caseAProductions(AProductions node)
  {}

  @Override
  public void caseAElem(final AElem elem)
  {
    String name;
    String elem_name;
    TId tid;
    if(elem.getElemName() != null)
    {
      tid = elem.getElemName();
    }
    else
    {
      tid = elem.getId();
    }

    elem_name = tid.getText();
    name = currentAlt + "." + ResolveIds.name(elem_name);

    if(ast_elems.put(name, elem) != null)
    {
      error(tid, name);
    }

    if(elem_name.equals("class"))
    {
      error5(tid);
    }

    ast_names.put(elem, ResolveIds.name(elem_name));
  }

  @Override
  public void outAAstProd(AAstProd prod)
  {
    prod.apply(new DepthFirstAdapter()
               {
                 @Override
                 public void caseAElem(AElem node)
                 {
                   String name = ResolveIds.name(node.getId().getText());

                   if(node.getSpecifier() != null)
                   {
                     if(node.getSpecifier() instanceof ATokenSpecifier)
                     {
                       ast_elemTypes.put(node, "T" + name);
                     }
                     else
                     {
                       ast_elemTypes.put(node, "P" + name);
                     }
                   }
                   else
                   {
                     Object token = astIds.tokens.get("T" + name);

                     if(token != null)
                     {
                       ast_elemTypes.put(node, "T" + name);
                     }
                     else
                     {
                       ast_elemTypes.put(node, "P" + name);
                     }
                   }
                 }
               }
              );
  }

  @Override
  public void outAAst(AAst prod)
  {
    prod.apply(new DepthFirstAdapter()
               {
                 @Override
                 public void caseAElem(AElem node)
                 {
                   String name = ResolveIds.name(node.getId().getText());

                   if(node.getSpecifier() != null)
                   {
                     if(node.getSpecifier() instanceof ATokenSpecifier)
                     {
                       if(astIds.tokens.get("T" + name) == null)
                       {
                         error2(node.getId(), "T" + name);
                       }

                       if(astIds.ignTokens.get("T" + name) != null)
                       {
                         error3(node.getId(), "T" + name);
                       }

                       ast_elemTypes.put(node, "T" + name);
                     }
                     else
                     {
                       if(ast_prods.get("P" + name) == null)
                       {
                         error2(node.getId(), "P" + name);
                       }
                       ast_elemTypes.put(node, "P" + name);
                     }
                   }
                   else
                   {
                     Object token = astIds.tokens.get("T" + name);
                     Object ignToken = astIds.ignTokens.get("T" + name);
                     //Object production = astIds.prods.get("P" + name);
                     Object ast_production = ast_prods.get("P" + name);
                     //if()
                     if((token == null) && (ast_production == null))
                     {
                       error2(node.getId(), "P" + name + " and T" + name );
                     }

                     //if the alternative element is a token
                     if(token != null)
                     {
                       //and also appears to be a valid production, there is an ambiguity
                       if( ast_production != null )
                       {
                         error4(node.getId(), "P" + name + " and T" + name);
                       }

                       //it should not be an ignored token
                       if(ignToken != null)
                       {
                         error3(node.getId(), "T" + name);
                       }

                       ast_elemTypes.put(node, "T" + name);
                     }
                     //the alternative element is a production and everything is fine
                     else
                     {
                       ast_elemTypes.put(node, "P" + name);
                     }
                   }
                 }
               }
              );
  }

  public void defaultcase(Node node)
  {
    if(node instanceof Token)
    {
      Token t = (Token) node;
      lastLine = t.getLine();
      lastPos = t.getPos() + t.getText().length();
    }
  }

  private static void error(Token token, String name)
  {
    throw new RuntimeException(
      "[" + token.getLine() + "," + token.getPos() + "] " +
      "Redefinition of " + name + ".");
  }

  private void error(String name)
  {
    throw new RuntimeException(
      "[" + lastLine + "," + lastPos + "] " +
      "Redefinition of " + name + ".");
  }

  private static void error2(Token token, String name)
  {
    throw new RuntimeException(
      "[" + token.getLine() + "," + token.getPos() + "] " +
      name + " undefined. If it is a production, It should be defined in AST section");
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

  private static void error5(Token token)
  {
    throw new RuntimeException(
      "[" + token.getLine() + "," + token.getPos() + "] " +
      "class is an invalid element name.");
  }

  @Override
  public String toString()
  {
    StringBuffer s = new StringBuffer();
    String nl = System.getProperty("line.separator");

    s.append("Productions:");
    s.append(nl);
    s.append(ast_prods);
    s.append(nl);

    s.append("Alternatives:");
    s.append(nl);
    s.append(ast_alts);
    s.append(nl);

    s.append("Elements:");
    s.append(nl);
    s.append(ast_elems);
    s.append(nl);

    return s.toString();
  }
}
