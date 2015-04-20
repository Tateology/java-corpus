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

@SuppressWarnings("rawtypes")
public class GenProds extends DepthFirstAdapter
{
  private MacroExpander macros;
  private ResolveAstIds ast_ids;
  private File pkgDir;
  private String pkgName;
//  private Map hiddenProds = new TypedTreeMap(
//                              StringComparator.instance,
//                              StringCast.instance,
//                              NodeCast.instance);

  public GenProds(ResolveAstIds ast_ids)
  {
    this.ast_ids = ast_ids;

    try
    {
      macros = new MacroExpander(
                 new InputStreamReader(
                   getClass().getResourceAsStream("productions.txt")));
    }
    catch(IOException e)
    {
      throw new RuntimeException("unable to open productions.txt.");
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
    String name = (String) ast_ids.ast_names.get(node);

    createProduction(name);
  }

  private void createProduction(String name)
  {
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
      macros.apply(file, "Production", new String[] {pkgName, name});
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
  }
/*
  private void createAlternative(String name, String macro, String[] arg)
  {
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
      macros.apply(file, macro, arg);
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
  }
*/
}
