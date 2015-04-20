/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * This file is part of SableCC.                             *
 * See the file "LICENSE" for copyright information and the  *
 * terms and conditions for copying, distribution and        *
 * modification of SableCC.                                  *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package org.sablecc.sablecc;

import java.io.*;
import java.util.*;

import org.sablecc.sablecc.analysis.*;
import org.sablecc.sablecc.node.*;

@SuppressWarnings({"rawtypes","unchecked"})
public class ResolveIds extends DepthFirstAdapter
{
  public final Map helpers = new TypedTreeMap(
                               StringComparator.instance,
                               StringCast.instance,
                               NodeCast.instance);
  public final Map states = new TypedTreeMap(
                              StringComparator.instance,
                              StringCast.instance,
                              NodeCast.instance);
  public final Map tokens = new TypedTreeMap(
                              StringComparator.instance,
                              StringCast.instance,
                              NodeCast.instance);
  public final Map ignTokens = new TypedTreeMap(
                                 StringComparator.instance,
                                 StringCast.instance,
                                 NodeCast.instance);
  public final Map prods = new TypedTreeMap(
                             StringComparator.instance,
                             StringCast.instance,
                             NodeCast.instance);

  public final Map alts = new TypedHashMap(
                            StringCast.instance,
                            NodeCast.instance);

  public final Map elems = new TypedHashMap(
                             StringCast.instance,
                             NodeCast.instance);

  public final Map names = new TypedHashMap(
                             NodeCast.instance,
                             StringCast.instance);

  public final Map errorNames = new TypedHashMap(
                                  NodeCast.instance,
                                  StringCast.instance);
  public final Map elemTypes = new TypedHashMap(
                                 NodeCast.instance,
                                 StringCast.instance);

  public final Map altsElemNameTypes = new TypedHashMap(
                                         StringCast.instance,
                                         StringCast.instance);

  // This map will serve for simpleTerm and simplelistTerm type within an altTransform
  // Inside an altTransform, one would look at this map to know its type. (P... or T...)
  public final Map altsElemTypes = new TypedHashMap(
                                     StringCast.instance,
                                     StringCast.instance);

  public final Map fixedTokens = new TypedHashMap(
                                   NodeCast.instance,
                                   BooleanCast.instance);

  public final List tokenList = new TypedLinkedList(StringCast.instance);
  public final LinkedList stateList = new TypedLinkedList(StringCast.instance);
  public File pkgDir;
  public String pkgName = "";

//  private boolean processingStates;
//  private boolean processingIgnTokens;

  String currentProd;
  String currentAlt;
  private int lastLine;
  private int lastPos;

  public ResolveIds(File currentDir)
  {
    pkgDir = currentDir;
  }

  @Override
  public void inAGrammar(AGrammar node)
  {
    TPkgId[] temp = (TPkgId []) node.getPackage().toArray(new TPkgId[0]);
    if(temp.length > 0)
    {
      pkgName = temp[0].getText();
      pkgDir = new File(pkgDir, temp[0].getText());

      for(int i=1; i<temp.length; i++)
      {
        pkgName += "." + temp[i].getText();
        pkgDir = new File(pkgDir, temp[i].getText());
      }

      if(!pkgDir.exists())
      {
        if(!pkgDir.mkdirs())
        {
          throw new RuntimeException("Unable to create " + pkgDir.getAbsolutePath());
        }
      }
    }
  }

  @Override
  public void caseAProd(AProd node)
  {
    //inAProd code.
    currentProd = name(node.getId().getText());
    String name = "P" + currentProd;

    if(prods.put(name, node) != null)
    {
      error(node.getId(), name);
    }

    names.put(node, name);

    //list of inAAlt code.
    Object []list_alt = (Object [])node.getAlts().toArray();
    for(int i = 0; i< list_alt.length; i++)
    {
      ((PAlt)list_alt[i]).apply(this);
    }
  }

  @Override
  public void caseAIdBasic(AIdBasic node)
  {
    String name = node.getId().getText();

    // Only helpers can be used inside tokens definition
    if(helpers.get(name) == null)
    {
      error2(node.getId(), name);
    }
  }

  @Override
  public void outAHelperDef(AHelperDef node)
  {
    String name = node.getId().getText();

    // If another helper is used within the current helper,
    // it should have been defined before the current one
    if(helpers.put(name, node) != null)
    {
      error(node.getId(), name);
    }

    names.put(node, name);
  }

  @Override
  public void outATokenDef(ATokenDef node)
  {
    String name = "T" + name(node.getId().getText());
    String errorName = errorName(node.getId().getText());

    //We are making sure that this token is not yet defined.
    if(tokens.put(name, node) != null)
    {
      error(node.getId(), name);
    }

    names.put(node, name);
    errorNames.put(node, errorName);
    tokenList.add(name);

    if(node.getLookAhead() != null)
    {
      Token token = (Token) node.getSlash();
      throw new RuntimeException(
        "[" + token.getLine() + "," + token.getPos() + "] " +
        "Look ahead not yet supported.");
    }
  }

  @Override
  public void inAStates(AStates node)
  {
    Object [] list_id = (Object[]) node.getListId().toArray();
    String name;

    for(int i=0; i<list_id.length; i++)
    {
      name = ((TId)list_id[i]).getText().toUpperCase();

      if(states.put(name, list_id[i]) != null)
      {
        error((TId)list_id[i], name);
      }

      names.put(list_id[i], name);
      stateList.add(name);
    }
  }

  @Override
  public void inAIgnTokens(AIgnTokens node)
  {
    Object [] list_id = (Object[]) node.getListId().toArray();
    String name;

    for(int i=0; i<list_id.length; i++)
    {
      name = "T" + name(((TId)list_id[i]).getText());

      if(tokens.get(name) == null)
      {
        error2((TId)list_id[i], name);
      }

      if(ignTokens.put(name, list_id[i]) != null)
      {
        error((TId)list_id[i], name);
      }
      names.put(list_id[i], name);
    }
  }

  private Map stateMap;

  @Override
  public void inAStateList(AStateList node)
  {
    stateMap = new TypedTreeMap(
                 StringComparator.instance,
                 StringCast.instance,
                 NodeCast.instance);

    String name = node.getId().getText().toUpperCase();

    if(states.get(name) == null)
    {
      error2(node.getId(), name);
    }

    if(stateMap.put(name, node) != null)
    {
      error(node.getId(), name);
    }
  }

  @Override
  public void outAStateList(AStateList node)
  {
    stateMap = null;
  }

  @Override
  public void inAStateListTail(AStateListTail node)
  {
    String name = node.getId().getText().toUpperCase();

    if(states.get(name) == null)
    {
      error2(node.getId(), name);
    }

    if(stateMap.put(name, node) != null)
    {
      error(node.getId(), name);
    }
  }

  @Override
  public void inATransition(ATransition node)
  {
    String name = node.getId().getText().toUpperCase();

    if(states.get(name) == null)
    {
      error2(node.getId(), name);
    }
  }

  @Override
  public void caseAAlt(final AAlt alt)
  {
    if(alt.getAltName() != null)
    {
      currentAlt =
        "A" +
        name(alt.getAltName().getText()) +
        currentProd;

      if(alts.put(currentAlt, alt) != null)
      {
        error(alt.getAltName(), currentAlt);
      }

      names.put(alt, currentAlt);
    }
    else
    {

      currentAlt = "A" + currentProd;

      if(alts.put(currentAlt, alt) != null)
      {
        error(currentAlt);
      }
      names.put(alt, currentAlt);
    }

    AElem list_elem[] = (AElem[]) alt.getElems().toArray(new AElem[0]);
    for(int i=0; i<list_elem.length;i++)
    {
      list_elem[i].apply(this);
    }

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

  @Override
  public void caseAAst(AAst node)
  {}

  @Override
  public void caseAElem(final AElem elem)
  {
    if(elem.getElemName() != null)
    {
      String name = currentAlt + "." +
                    name(elem.getElemName().getText());

      if(elems.put(name, elem) != null)
      {
        error(elem.getElemName(), name);
      }

      if(elem.getElemName().getText().equals("class"))
      {
        error5(elem.getElemName());
      }

      names.put(elem, name(elem.getElemName().getText()) );
    }
    else
    {
      String name = currentAlt + "." +
                    name(elem.getId().getText());

      if(elems.put(name, elem) != null)
      {
        error(elem.getId(), name);
      }

      if(elem.getId().getText().equals("class"))
      {
        error5(elem.getId());
      }

      names.put(elem, name(elem.getId().getText()));
    }
  }

  @Override
  public void outAProductions(AProductions prod)
  {
    prod.apply(new DepthFirstAdapter()
               {
                 @Override
                 public void caseAProd(AProd node)
                 {
                   //inAProd code.
                   currentProd = name(node.getId().getText());

                   //list of inAAlt code.
                   Object []list_alt = (Object [])node.getAlts().toArray();
                   for(int i = 0; i< list_alt.length; i++)
                   {
                     ((PAlt)list_alt[i]).apply(this);
                   }
                 }

                 @Override
                 public void caseAAlt(final AAlt alt)
                 {
                   if(alt.getAltName() != null)
                   {
                     currentAlt = "A" + name(alt.getAltName().getText()) + currentProd;
                   }
                   else
                   {
                     currentAlt = "A" + currentProd;
                   }

                   AElem[] list_elem = (AElem[]) alt.getElems().toArray(new AElem[0]);
                   for(int i=0; i<list_elem.length;i++)
                   {
                     list_elem[i].apply(this);
                   }
                 }

                 @Override
                 public void caseAElem(AElem node)
                 {
                   String name = name(node.getId().getText());
                   String nameOfElem;

                   if(node.getElemName() != null)
                   {
                     nameOfElem = node.getElemName().getText();
                   }
                   else
                   {
                     nameOfElem = node.getId().getText();
                   }

                   if(node.getSpecifier() != null)
                   {
                     if(node.getSpecifier() instanceof ATokenSpecifier)
                     {
                       if(tokens.get("T" + name) == null)
                       {
                         error2(node.getId(), "T" + name);
                       }

                       if(ignTokens.get("T" + name) != null)
                       {
                         error3(node.getId(), "T" + name);
                       }

                       elemTypes.put(node, "T" + name);

                       if(node.getElemName() != null)
                       {
                         altsElemNameTypes.put(currentAlt+"." + node.getElemName().getText(), "T" + name);
                       }

                       String type_name = name;
                       if(node.getUnOp() instanceof AStarUnOp || node.getUnOp() instanceof AQMarkUnOp)
                       {
                         type_name += "?";
                       }
                       altsElemTypes.put(currentAlt+"." + nameOfElem, "T" + type_name);
                     }
                     else
                     {
                       if(prods.get("P" + name) == null)
                       {
                         error2(node.getId(), "P" + name);
                       }

                       elemTypes.put(node, "P" + name);

                       if(node.getElemName() != null)
                       {
                         altsElemNameTypes.put(currentAlt+"." + node.getElemName().getText(), "P" + name);
                       }
                       //altsElemTypes.put(currentAlt+"." + nameOfElem, "P" + name);
                       String type_name = name;
                       if(node.getUnOp() instanceof AStarUnOp || node.getUnOp() instanceof AQMarkUnOp)
                       {
                         type_name += "?";
                       }
                       altsElemTypes.put(currentAlt+"." + nameOfElem, "P" + type_name);
                     }
                   }
                   else
                   {
                     Object token = tokens.get("T" + name);
                     Object ignToken = ignTokens.get("T" + name);
                     Object production = prods.get("P" + name);

                     if((token == null) && (production == null))
                     {
                       error2(node.getId(), "P" + name + " and T" + name);
                     }

                     if(token != null)
                     {
                       if(production != null)
                       {
                         error4(node.getId(), "P" + name + " and T" + name);
                       }

                       if(ignToken != null)
                       {
                         error3(node.getId(), "T" + name);
                       }

                       elemTypes.put(node, "T" + name);

                       if(node.getElemName() != null)
                       {
                         altsElemNameTypes.put(currentAlt+"." + node.getElemName().getText(), "T" + name);
                       }
                       String type_name = name;
                       if(node.getUnOp() instanceof AStarUnOp || node.getUnOp() instanceof AQMarkUnOp)
                       {
                         type_name += "?";
                       }
                       altsElemTypes.put(currentAlt+"." + nameOfElem, "T" + type_name);
                     }
                     else
                     {
                       elemTypes.put(node, "P" + name);

                       if(node.getElemName() != null)
                       {
                         altsElemNameTypes.put(currentAlt+"." + node.getElemName().getText(), "P" + name);
                       }
                       String type_name = name;
                       if(node.getUnOp() instanceof AStarUnOp || node.getUnOp() instanceof AQMarkUnOp)
                       {
                         type_name += "?";
                       }
                       altsElemTypes.put(currentAlt+"." + nameOfElem, "P" + type_name);
                     }
                   }
                 }
               }
              );
  }

  public static String name(String s)
  {
    StringBuffer result = new StringBuffer();
    boolean upcase = true;
    int length = s.length();
    char c;

    for(int i = 0; i < length; i++)
    {
      c = s.charAt(i);
      switch(c)
      {
      case '_':
        upcase = true;
        break;
      case '$':
        result.append(c);
        upcase = true;
        break;
      default:
        if(upcase)
        {
          result.append(Character.toUpperCase(c));
          upcase = false;
        }
        else
        {
          result.append(c);
        }
        break;
      }
    }

    return result.toString();
  }

  public static String errorName(String s)
  {
    StringBuffer result = new StringBuffer();
    int length = s.length();
    char c;

    for(int i = 0; i < length; i++)
    {
      c = s.charAt(i);
      switch(c)
      {
      case '_':
        {
          result.append(' ');
        }
        break;
      default:
        {
          result.append(c);
        }
        break;
      }
    }

    return result.toString();
  }

  public void reinit()
  {
    names.clear();
    elemTypes.clear();
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

    s.append("Helpers:");
    s.append(nl);
    s.append(helpers);
    s.append(nl);

    s.append("States:");
    s.append(nl);
    s.append(states);
    s.append(nl);

    s.append("Tokens:");
    s.append(nl);
    s.append(tokens);
    s.append(nl);

    s.append("Ignored Tokens:");
    s.append(nl);
    s.append(ignTokens);
    s.append(nl);

    s.append("Productions:");
    s.append(nl);
    s.append(prods);
    s.append(nl);

    s.append("Alternatives:");
    s.append(nl);
    s.append(alts);
    s.append(nl);

    s.append("Elements:");
    s.append(nl);
    s.append(elems);
    s.append(nl);

    return s.toString();
  }
}
