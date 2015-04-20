/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * This file is part of SableCC.                             *
 * See the file "LICENSE" for copyright information and the  *
 * terms and conditions for copying, distribution and        *
 * modification of SableCC.                                  *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package org.sablecc.sablecc;

import java.io.*;

import org.sablecc.sablecc.analysis.*;
import org.sablecc.sablecc.node.*;

public class GenUtils extends DepthFirstAdapter
{
  private MacroExpander macros;
  private ResolveAstIds ast_ids;
  private File pkgDir;
  private String pkgName;
  private String mainProduction;

  public GenUtils(ResolveAstIds ast_ids)
  {
    this.ast_ids = ast_ids;

    try
    {
      macros = new MacroExpander(
                 new InputStreamReader(
                   getClass().getResourceAsStream("utils.txt")));
    }
    catch(IOException e)
    {
      throw new RuntimeException("unable to open utils.txt.");
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

  /*
  public void caseAGrammar(AGrammar node)
  {
  AAstProd []temp = (AAstProd[])node.getAst().toArray(new AAstProd[0]);

  for(int i=0; i<temp.length; i++)
  {
    temp[i].apply(this);
  }
  }
  */

  @Override
  public void caseAAstProd(AAstProd node)
  {
    if(mainProduction == null)
    {
      mainProduction = (String) ast_ids.ast_names.get(node);
    }
  }

  @Override
  public void outStart(Start node)
  {
    if(mainProduction != null)
    {
      createStart();
    }

    createEOF();
    createInvalidToken();
    createNode();
    createToken();
    create("Switch");
    create("Switchable");
  }

  public void createStart()
  {
    BufferedWriter file;

    try
    {
      file = new BufferedWriter(
               new FileWriter(
                 new File(pkgDir, "Start.java")));
    }
    catch(IOException e)
    {
      throw new RuntimeException("Unable to create " + new File(pkgDir, "Start.java").getAbsolutePath());
    }

    try
    {
      macros.apply(file, "Start", new String[] {pkgName,
                   ast_ids.astIds.pkgName.equals("") ? "analysis" : ast_ids.astIds.pkgName + ".analysis",
                   mainProduction, GenAlts.nodeName(mainProduction)});
    }
    catch(IOException e)
    {
      throw new RuntimeException("An error occured while writing to " +
                                 new File(pkgDir, "Start.java").getAbsolutePath());
    }

    try
    {
      file.close();
    }
    catch(IOException e)
    {}
  }

  public void createEOF()
  {
    BufferedWriter file;

    try
    {
      file = new BufferedWriter(
               new FileWriter(
                 new File(pkgDir, "EOF.java")));
    }
    catch(IOException e)
    {
      throw new RuntimeException("Unable to create " + new File(pkgDir, "EOF.java").getAbsolutePath());
    }

    try
    {
      macros.apply(file, "EOF", new String[] {pkgName,
                                              ast_ids.astIds.pkgName.equals("") ? "analysis" : ast_ids.astIds.pkgName + ".analysis"});
    }
    catch(IOException e)
    {
      throw new RuntimeException("An error occured while writing to " +
                                 new File(pkgDir, "EOF.java").getAbsolutePath());
    }

    try
    {
      file.close();
    }
    catch(IOException e)
    {}
  }

  public void createInvalidToken()
  {
    BufferedWriter file;

    try
    {
      file = new BufferedWriter(
               new FileWriter(
                 new File(pkgDir, "InvalidToken.java")));
    }
    catch(IOException e)
    {
      throw new RuntimeException("Unable to create " + new File(pkgDir, "InvalidToken.java").getAbsolutePath());
    }

    try
    {
      macros.apply(file, "InvalidToken", new String[] {pkgName,
                                              ast_ids.astIds.pkgName.equals("") ? "analysis" : ast_ids.astIds.pkgName + ".analysis"});
    }
    catch(IOException e)
    {
      throw new RuntimeException("An error occured while writing to " +
                                 new File(pkgDir, "InvalidToken.java").getAbsolutePath());
    }

    try
    {
      file.close();
    }
    catch(IOException e)
    {}
  }

  public void createNode()
  {
    BufferedWriter file;

    try
    {
      file = new BufferedWriter(
               new FileWriter(
                 new File(pkgDir, "Node.java")));
    }
    catch(IOException e)
    {
      throw new RuntimeException("Unable to create " + new File(pkgDir, "Node.java").getAbsolutePath());
    }

    try
    {
      macros.apply(file, "Node", new String[] {pkgName,
                   ast_ids.astIds.pkgName.equals("") ? "analysis" : ast_ids.astIds.pkgName + ".analysis"});
    }
    catch(IOException e)
    {
      throw new RuntimeException("An error occured while writing to " +
                                 new File(pkgDir, "Node.java").getAbsolutePath());
    }

    try
    {
      file.close();
    }
    catch(IOException e)
    {}
  }

  public void createToken()
  {
    BufferedWriter file;

    try
    {
      file = new BufferedWriter(
               new FileWriter(
                 new File(pkgDir, "Token.java")));
    }
    catch(IOException e)
    {
      throw new RuntimeException("Unable to create " + new File(pkgDir, "Token.java").getAbsolutePath());
    }

    try
    {
      macros.apply(file, "Token", new String[] {pkgName});
    }
    catch(IOException e)
    {
      throw new RuntimeException("An error occured while writing to " +
                                 new File(pkgDir, "Token.java").getAbsolutePath());
    }

    try
    {
      file.close();
    }
    catch(IOException e)
    {}
  }

  public void create(String cls)
  {
    BufferedWriter file;

    try
    {
      file = new BufferedWriter(
               new FileWriter(
                 new File(pkgDir, cls + ".java")));
    }
    catch(IOException e)
    {
      throw new RuntimeException("Unable to create " + new File(pkgDir, cls + ".java").getAbsolutePath());
    }

    try
    {
      macros.apply(file, cls, new String[] {pkgName});
    }
    catch(IOException e)
    {
      throw new RuntimeException("An error occured while writing to " +
                                 new File(pkgDir, cls + ".java").getAbsolutePath());
    }

    try
    {
      file.close();
    }
    catch(IOException e)
    {}
  }
}
