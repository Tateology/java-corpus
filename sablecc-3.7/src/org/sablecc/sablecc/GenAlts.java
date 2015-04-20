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
public class GenAlts extends DepthFirstAdapter
{
  private MacroExpander macros;
  private ResolveAstIds ast_ids;
  private File pkgDir;
  private String pkgName;
  private List elemList;

  private String currentProd;
  ElemInfo info;
  //    final GenAlts instance = this;

  public GenAlts(ResolveAstIds ast_ids)
  {
    this.ast_ids = ast_ids;
    try
    {
      macros = new MacroExpander(
                 new InputStreamReader(
                   getClass().getResourceAsStream("alternatives.txt")));
    }
    catch(IOException e)
    {
      throw new RuntimeException("unable to open alternatives.txt.");
    }

    pkgDir = new File(ast_ids.astIds.pkgDir, "node");
    pkgName = ast_ids.astIds.pkgName.equals("") ? "node" : ast_ids.astIds.pkgName + ".node";

    if(!pkgDir.exists())
    {
      if(!pkgDir.mkdir())
      {
        throw new RuntimeException("Unable to create " + pkgDir.getAbsolutePath());
      }
    }
  }

  @Override
  public void inAAstProd(AAstProd node)
  {
    currentProd = (String) ast_ids.ast_names.get(node);
  }

  @Override
  public void inAAstAlt(AAstAlt node)
  {
    elemList = new TypedLinkedList(ElemInfoCast.instance);
  }

  @Override
  public void caseAProductions(AProductions node)
  {}

  @Override
  public void inAElem(AElem node)
  {
    info = new ElemInfo();
    info.name = (String) ast_ids.ast_names.get(node);
    info.type = (String) ast_ids.ast_elemTypes.get(node);
    info.operator = ElemInfo.NONE;

    if(node.getUnOp() != null)
    {
      node.getUnOp().apply(new DepthFirstAdapter()
                           {
                             @Override
                             public void caseAStarUnOp(AStarUnOp node)
                             {
                               info.operator = ElemInfo.STAR;
                             }

                             @Override
                             public void caseAQMarkUnOp(AQMarkUnOp node)
                             {
                               info.operator = ElemInfo.QMARK;
                             }

                             @Override
                             public void caseAPlusUnOp(APlusUnOp node)
                             {
                               info.operator = ElemInfo.PLUS;
                             }
                           }
                          );
    }
    elemList.add(info);
    info = null;
  }

  @Override
  public void outAAstAlt(AAstAlt node)
  {
    String name = (String) ast_ids.ast_names.get(node);

    BufferedWriter file;

    try
    {
      file = new BufferedWriter(
               new FileWriter(
                 new File(pkgDir, name + ".java")));
    }
    catch(IOException e)
    {
      throw new RuntimeException("Unable to create " + new File(pkgDir, name + ".java").getAbsolutePath());
    }

    try
    {
//        boolean hasOperator = false;
        boolean hasList = false;

      for(Iterator i = elemList.iterator(); i.hasNext();)
      {
        ElemInfo info = (ElemInfo) i.next();
        if(info != null)
          switch(info.operator)
          {
          case ElemInfo.STAR:
          case ElemInfo.PLUS:
            {
              hasList = true;
            }
            break;
          }
      }

      macros.apply(file, "AlternativeHeader1", new String[] {pkgName});

      if(hasList)
      {
          macros.apply(file, "AlternativeHeaderList", new String[] {});
      }

      macros.apply(file, "AlternativeHeader2", new String[] {
              ast_ids.astIds.pkgName.equals("") ? "analysis" : ast_ids.astIds.pkgName + ".analysis",
              name, currentProd});

      for(Iterator i = elemList.iterator(); i.hasNext();)
      {
        ElemInfo info = (ElemInfo) i.next();
        if(info != null)
          switch(info.operator)
          {
          case ElemInfo.QMARK:
          case ElemInfo.NONE:
            {
              macros.apply(file, "NodeElement",
                           new String[] {info.type,
                                         nodeName(info.name)});
            }
            break;
          case ElemInfo.STAR:
          case ElemInfo.PLUS:
            {
//              hasOperator = true;
              macros.apply(file, "ListElement",
                           new String[] {info.type, nodeName(info.name)});
            }
            break;
          }
      }

      macros.apply(file, "ConstructorHeader",
                   new String[] {name});
      macros.apply(file, "ConstructorBodyHeader", null);
      macros.apply(file, "ConstructorBodyTail", null);

      if(elemList.size() > 0)
      {
        macros.apply(file, "ConstructorHeader",
                     new String[] {name});

        for(Iterator i = elemList.iterator(); i.hasNext();)
        {
          ElemInfo info = (ElemInfo) i.next();
          if(info != null)
            switch(info.operator)
            {
            case ElemInfo.QMARK:
            case ElemInfo.NONE:
              {
                macros.apply(file, "ConstructorHeaderDeclNode",
                             new String[] {info.type, nodeName(info.name), i.hasNext() ? "," : ""});
              }
              break;
            case ElemInfo.STAR:
            case ElemInfo.PLUS:
              {
                macros.apply(file, "ConstructorHeaderDeclList",
                             new String[] {info.type, nodeName(info.name), i.hasNext() ? "," : ""});
              }
              break;
            }
        }

        macros.apply(file, "ConstructorBodyHeader", null);

        for(Iterator i = elemList.iterator(); i.hasNext();)
        {
          ElemInfo info = (ElemInfo) i.next();

          if(info != null )
            switch(info.operator)
            {
            case ElemInfo.QMARK:
            case ElemInfo.NONE:
              {
                macros.apply(file, "ConstructorBodyNode",
                             new String[] {info.name, nodeName(info.name)});
              }
              break;
            case ElemInfo.STAR:
            case ElemInfo.PLUS:
              {
                macros.apply(file, "ConstructorBodyList",
                             new String[] {info.name, nodeName(info.name)});
              }
              break;
            }
        }

        macros.apply(file, "ConstructorBodyTail", null);
      }

      //****************
      macros.apply(file, "CloneHeader",
                   new String[] {name});

      for(Iterator i = elemList.iterator(); i.hasNext();)
      {
        ElemInfo info = (ElemInfo) i.next();
        if(info != null)
          switch(info.operator)
          {
          case ElemInfo.QMARK:
          case ElemInfo.NONE:
            {
              macros.apply(file, "CloneBodyNode",
                           new String[] {info.type, nodeName(info.name), i.hasNext() ? "," : ""});
            }
            break;
          case ElemInfo.STAR:
          case ElemInfo.PLUS:
            {
              macros.apply(file, "CloneBodyList",
                           new String[] {nodeName(info.name), i.hasNext() ? "," : ""});
            }
            break;
          }
      }

      macros.apply(file, "CloneTail", null);

      macros.apply(file, "Apply", new String[] {name});

      for(Iterator i = elemList.iterator(); i.hasNext();)
      {
        ElemInfo info = (ElemInfo) i.next();

        if(info != null)
          switch(info.operator)
          {
          case ElemInfo.QMARK:
          case ElemInfo.NONE:
            {
              macros.apply(file, "GetSetNode",
                           new String[] {info.type, info.name, nodeName(info.name)});
            }
            break;
          case ElemInfo.STAR:
          case ElemInfo.PLUS:
            {
              macros.apply(file, "GetSetList",
                           new String[] {info.name, nodeName(info.name), info.type});
            }
            break;
          }
      }

      macros.apply(file, "ToStringHeader", null);
      for(Iterator i = elemList.iterator(); i.hasNext();)
      {
        ElemInfo info = (ElemInfo) i.next();

        if(info != null)
          switch(info.operator)
          {
          case ElemInfo.QMARK:
          case ElemInfo.NONE:
            {
              macros.apply(file, "ToStringBodyNode",
                           new String[] {nodeName(info.name)});
            }
            break;
          case ElemInfo.STAR:
          case ElemInfo.PLUS:
            {
              macros.apply(file, "ToStringBodyList",
                           new String[] {nodeName(info.name)});
            }
            break;
          }
      }
      macros.apply(file, "ToStringTail", null);

      macros.apply(file, "RemoveChildHeader", null);
      for(Iterator i = elemList.iterator(); i.hasNext();)
      {
        ElemInfo info = (ElemInfo) i.next();

        if(info != null)
          switch(info.operator)
          {
          case ElemInfo.QMARK:
          case ElemInfo.NONE:
            {
              macros.apply(file, "RemoveChildNode",
                           new String[] {nodeName(info.name)});
            }
            break;
          case ElemInfo.STAR:
          case ElemInfo.PLUS:
            {
              macros.apply(file, "RemoveChildList",
                           new String[] {nodeName(info.name)});
            }
            break;
          }
      }
      macros.apply(file, "RemoveChildTail", null);

      macros.apply(file, "ReplaceChildHeader", null);
      for(Iterator i = elemList.iterator(); i.hasNext();)
      {
        ElemInfo info = (ElemInfo) i.next();

        if(info != null)
          switch(info.operator)
          {
          case ElemInfo.QMARK:
          case ElemInfo.NONE:
            {
              macros.apply(file, "ReplaceChildNode",
                           new String[] {nodeName(info.name), info.name, info.type});
            }
            break;
          case ElemInfo.STAR:
          case ElemInfo.PLUS:
            {
              macros.apply(file, "ReplaceChildList",
                           new String[] {nodeName(info.name), info.type});
            }
            break;
          }
      }
      macros.apply(file, "ReplaceChildTail", null);

      macros.apply(file, "AlternativeTail", null);
    }
    catch(IOException e)
    {
      throw new RuntimeException("An error occured while writing to " +
                                 new File(pkgDir, name + ".java").getAbsolutePath());
    }

    try
    {
      file.close();
    }
    catch(IOException e)
    {}

    elemList = null;
  }

  public static String nodeName(String s)
  {
    StringBuffer result = new StringBuffer(s);

    if(result.length() > 0)
    {
      result.setCharAt(0, Character.toLowerCase(result.charAt(0)));
    }

    return result.toString();
  }

  private static class ElemInfo
  {
    final static int NONE = 0;
    final static int STAR = 1;
    final static int QMARK = 2;
    final static int PLUS = 3;

    String name;
    String type;
    int operator;
  }

  private static class ElemInfoCast implements Cast
  {
    public final static ElemInfoCast instance = new ElemInfoCast();

    private ElemInfoCast()
    {}

    @Override
    public    Object cast(Object o)
    {
      return (ElemInfo) o;
    }
  }
}
