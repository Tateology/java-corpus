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
public class GenAnalyses extends DepthFirstAdapter
{
  private MacroExpander macros;
  private ResolveAstIds ast_ids;
  private File pkgDir;
  private String pkgName;
  private List elemList;
  private List altList = new TypedLinkedList(AltInfoCast.instance);
  private List tokenList = new TypedLinkedList(StringCast.instance);
  private String mainProduction;

  ElemInfo info;
  //    final GenAnalyses instance = this;

  public GenAnalyses(ResolveAstIds ast_ids)
  {
    this.ast_ids = ast_ids;
    try
    {
      macros = new MacroExpander(
                 new InputStreamReader(
                   getClass().getResourceAsStream("analyses.txt")));
    }
    catch(IOException e)
    {
      throw new RuntimeException("unable to open analyses.txt.");
    }

    pkgDir = new File(ast_ids.astIds.pkgDir, "analysis");
    pkgName = ast_ids.astIds.pkgName.equals("") ? "analysis" : ast_ids.astIds.pkgName + ".analysis";

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
    if(mainProduction == null)
    {
      mainProduction = (String) ast_ids.ast_names.get(node);
    }
  }

  @Override
  public void inATokenDef(ATokenDef node)
  {
    tokenList.add(ast_ids.astIds.names.get(node));
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
    AltInfo info = new AltInfo();

    info.name = (String) ast_ids.ast_names.get(node);
    info.elems.addAll(elemList);
    elemList = null;

    altList.add(info);
  }

  @Override
  public void outStart(Start node)
  {
    createAnalysis();
    createAnalysisAdapter();

    if(mainProduction != null)
    {
      createDepthFirstAdapter();
      createReversedDepthFirstAdapter();
    }
  }

  public void createAnalysis()
  {
    BufferedWriter file;

    try
    {
      file = new BufferedWriter(
               new FileWriter(
                 new File(pkgDir, "Analysis.java")));
    }
    catch(IOException e)
    {
      throw new RuntimeException("Unable to create " + new File(pkgDir, "Analysis.java").getAbsolutePath());
    }

    try
    {
      macros.apply(file, "AnalysisHeader", new String[] {pkgName,
                   ast_ids.astIds.pkgName.equals("") ? "node" : ast_ids.astIds.pkgName + ".node"});

      if(mainProduction != null)
      {
        macros.apply(file, "AnalysisStart", null);

        for(Iterator i = altList.iterator(); i.hasNext();)
        {
          AltInfo info = (AltInfo) i.next();

          macros.apply(file, "AnalysisBody",
                       new String[] {info.name});
        }

        file.newLine();
      }

      for(Iterator i = tokenList.iterator(); i.hasNext();)
      {
        macros.apply(file, "AnalysisBody",
                     new String[] {(String) i.next()});
      }

      macros.apply(file, "AnalysisTail", null);
    }
    catch(IOException e)
    {
      throw new RuntimeException("An error occured while writing to " +
                                 new File(pkgDir, "Analysis.java").getAbsolutePath());
    }

    try
    {
      file.close();
    }
    catch(IOException e)
    {}
  }

  public void createAnalysisAdapter()
  {
    BufferedWriter file;

    try
    {
      file = new BufferedWriter(
               new FileWriter(
                 new File(pkgDir, "AnalysisAdapter.java")));
    }
    catch(IOException e)
    {
      throw new RuntimeException("Unable to create " + new File(pkgDir, "AnalysisAdapter.java").getAbsolutePath());
    }

    try
    {
      macros.apply(file, "AnalysisAdapterHeader", new String[] {pkgName,
                   ast_ids.astIds.pkgName.equals("") ? "node" : ast_ids.astIds.pkgName + ".node"});

      if(mainProduction != null)
      {
        macros.apply(file, "AnalysisAdapterStart", null);

        for(Iterator i = altList.iterator(); i.hasNext();)
        {
          AltInfo info = (AltInfo) i.next();

          macros.apply(file, "AnalysisAdapterBody",
                       new String[] {info.name});
        }
      }

      for(Iterator i = tokenList.iterator(); i.hasNext();)
      {
        macros.apply(file, "AnalysisAdapterBody",
                     new String[] {(String) i.next()});
      }

      macros.apply(file, "AnalysisAdapterTail", null);
    }
    catch(IOException e)
    {
      throw new RuntimeException("An error occured while writing to " +
                                 new File(pkgDir, "AnalysisAdapter.java").getAbsolutePath());
    }

    try
    {
      file.close();
    }
    catch(IOException e)
    {}
  }

  public void createDepthFirstAdapter()
  {
    BufferedWriter file;

    try
    {
      file = new BufferedWriter(
               new FileWriter(
                 new File(pkgDir, "DepthFirstAdapter.java")));
    }
    catch(IOException e)
    {
      throw new RuntimeException("Unable to create " + new File(pkgDir, "DepthFirstAdapter.java").getAbsolutePath());
    }

    try
    {
      macros.apply(file, "DepthFirstAdapterHeader", new String[] {pkgName,
                   ast_ids.astIds.pkgName.equals("") ? "node" : ast_ids.astIds.pkgName + ".node",
                   mainProduction});

      for(Iterator i = altList.iterator(); i.hasNext();)
      {
        AltInfo info = (AltInfo) i.next();

        macros.apply(file, "DepthFirstAdapterInOut",
                     new String[] {info.name});

        macros.apply(file, "DepthFirstAdapterCaseHeader",
                     new String[] {info.name});

        for(Iterator j = info.elems.iterator(); j.hasNext();)
        {
          ElemInfo eInfo = (ElemInfo) j.next();

          switch(eInfo.operator)
          {
          case ElemInfo.QMARK:
          case ElemInfo.NONE:
            {
              macros.apply(file, "DepthFirstAdapterCaseBodyNode",
                           new String[] {eInfo.name});
            }
            break;
          case ElemInfo.STAR:
          case ElemInfo.PLUS:
            {
              macros.apply(file, "DepthFirstAdapterCaseBodyList",
                           new String[] {eInfo.name, eInfo.type});
            }
            break;
          }
        }

        macros.apply(file, "DepthFirstAdapterCaseTail",
                     new String[] {info.name});

      }

      macros.apply(file, "DepthFirstAdapterTail", null);
    }
    catch(IOException e)
    {
      throw new RuntimeException("An error occured while writing to " +
                                 new File(pkgDir, "DepthFirstAdapter.java").getAbsolutePath());
    }

    try
    {
      file.close();
    }
    catch(IOException e)
    {}
  }

  public void createReversedDepthFirstAdapter()
  {
    BufferedWriter file;

    try
    {
      file = new BufferedWriter(
               new FileWriter(
                 new File(pkgDir, "ReversedDepthFirstAdapter.java")));
    }
    catch(IOException e)
    {
      throw new RuntimeException("Unable to create " + new File(pkgDir, "ReversedDepthFirstAdapter.java").getAbsolutePath());
    }

    try
    {
      macros.apply(file, "ReversedDepthFirstAdapterHeader", new String[] {pkgName,
                   ast_ids.astIds.pkgName.equals("") ? "node" : ast_ids.astIds.pkgName + ".node",
                   mainProduction});

      for(Iterator i = altList.iterator(); i.hasNext();)
      {
        AltInfo info = (AltInfo) i.next();

        macros.apply(file, "DepthFirstAdapterInOut",
                     new String[] {info.name});

        macros.apply(file, "DepthFirstAdapterCaseHeader",
                     new String[] {info.name});

        for(ListIterator j = info.elems.listIterator(info.elems.size()); j.hasPrevious();)
        {
          ElemInfo eInfo = (ElemInfo) j.previous();

          switch(eInfo.operator)
          {
          case ElemInfo.QMARK:
          case ElemInfo.NONE:
            {
              macros.apply(file, "DepthFirstAdapterCaseBodyNode",
                           new String[] {eInfo.name});
            }
            break;
          case ElemInfo.STAR:
          case ElemInfo.PLUS:
            {
              macros.apply(file, "ReversedDepthFirstAdapterCaseBodyList",
                           new String[] {eInfo.name, eInfo.type});
            }
            break;
          }
        }

        macros.apply(file, "DepthFirstAdapterCaseTail",
                     new String[] {info.name});

      }

      macros.apply(file, "DepthFirstAdapterTail", null);
    }
    catch(IOException e)
    {
      throw new RuntimeException("An error occured while writing to " +
                                 new File(pkgDir, "ReversedDepthFirstAdapter.java").getAbsolutePath());
    }

    try
    {
      file.close();
    }
    catch(IOException e)
    {}
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
    final static ElemInfoCast instance = new ElemInfoCast();

    private ElemInfoCast()
    {}

    @Override
    public    Object cast(Object o)
    {
      return (ElemInfo) o;
    }
  }

  private static class AltInfo
  {
    String name;
    final List elems = new TypedLinkedList(ElemInfoCast.instance);
  }

  private static class AltInfoCast implements Cast
  {
    final static AltInfoCast instance = new AltInfoCast();

    private AltInfoCast()
    {}

    @Override
    public    Object cast(Object o)
    {
      return (AltInfo) o;
    }
  }
}
