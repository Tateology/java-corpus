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

@SuppressWarnings("rawtypes")
public class GenerateAlternativeCodeForParser extends DepthFirstAdapter
{
  String currentAlt;
  String realcurrentAlt;
  BufferedWriter file;
  private File pkgDir;

  private ResolveTransformIds transformIds;
  private ComputeCGNomenclature CG;
  private ComputeSimpleTermPosition CTP;
  private MacroExpander macros;
  private Map simpleTermTransformMap;
  private LinkedList listSimpleTermTransform;
  private Map simpleTermOrsimpleListTermTypes;

  GenerateAlternativeCodeForParser(File pkgDir, String aParsedAltName,
                                   String raParsedAltName,
                                   BufferedWriter file,
                                   ResolveTransformIds transformIds,
                                   ComputeCGNomenclature CG,
                                   ComputeSimpleTermPosition CTP,
                                   Map simpleTermTransformMap,
                                   MacroExpander macros,
                                   LinkedList listSimpleTermTransform,
                                   Map simpleTermOrsimpleListTermTypes)
  {
    this.pkgDir = pkgDir;
    this.file = file;
    currentAlt = aParsedAltName;
    realcurrentAlt = raParsedAltName;
    this.transformIds = transformIds;
    this.CG = CG;
    this.CTP = CTP;
    this.simpleTermTransformMap = simpleTermTransformMap;
    this.macros = macros;
    this.listSimpleTermTransform = listSimpleTermTransform;
    this.simpleTermOrsimpleListTermTypes = simpleTermOrsimpleListTermTypes;
  }

  @Override
  public void inAAltTransform(AAltTransform node)
  {
    Object temp[] = node.getTerms().toArray();
    String type_name;
    int position;

    for(int i = 0; i < temp.length; i++)
    {
      if(simpleTermTransformMap.get(temp[i]) != null)
      {
        type_name = (String)simpleTermTransformMap.get(temp[i]);
      }
      else
      {
        type_name = (String)CG.getAltTransformElemTypes().get(temp[i]);
      }

      position = ((Integer)CG.getTermNumbers().get(temp[i])).intValue();

      try
      {
        if(type_name.startsWith("L"))
        {
          macros.apply(file, "ParserListVariableDeclaration", new String[] {"" + position});
        }
        else if(type_name.equals("null"))
        {
          macros.apply(file, "ParserNullVariableDeclaration", new String[] {"" + position});
        }
        else
        {
          macros.apply(file, "ParserSimpleVariableDeclaration", new String[] {type_name, type_name.toLowerCase(), "" + position});
        }
      }
      catch(IOException e)
      {
        throw new RuntimeException("An error occured while writing to " +
                                   new File(pkgDir, "Parser.java").getAbsolutePath());
      }
    }
  }

  @Override
  public void outAAltTransform(AAltTransform node)
  {
    Object temp[] = node.getTerms().toArray();
    String type_name;
    int position;

    try
    {
      for(int i = 0; i < temp.length; i++)
      {
        if(simpleTermTransformMap.get(temp[i]) != null)
        {
          type_name = (String)simpleTermTransformMap.get(temp[i]);
        }
        else
        {
          type_name = (String)CG.getAltTransformElemTypes().get(temp[i]);
        }

        position = ((Integer)CG.getTermNumbers().get(temp[i])).intValue();

        if(type_name.startsWith("L"))
        {
          type_name = "list";
        }
        else if(type_name.equals("null"))
        {
          type_name = "null";
        }
        else
        {
          type_name = type_name.toLowerCase();
        }
        macros.apply(file, "ParserNewBodyListAdd", new String[] {type_name, "" + position});

      }
      macros.apply(file, "ParserNewTail");
    }
    catch(IOException e)
    {
      throw new RuntimeException("An error occured while writing to " +
                                 new File(pkgDir, "Parser.java").getAbsolutePath());
    }
  }

  public void inAParams(LinkedList list_param)
  {
    String type_name;
    int position;

    Object temp[] = list_param.toArray();

    for(int i = 0; i < temp.length; i++)
    {
      if(simpleTermTransformMap.get(temp[i]) != null)
      {
        type_name = (String)simpleTermTransformMap.get(temp[i]);
      }
      else
      {
        type_name = (String)CG.getAltTransformElemTypes().get(temp[i]);
      }
      position = ((Integer)CG.getTermNumbers().get(temp[i])).intValue();

      try
      {
        if(type_name.startsWith("L"))
        {
          macros.apply(file, "ParserListVariableDeclaration", new String[] {"" + position});
        }
        else if(type_name.equals("null"))
        {
          macros.apply(file, "ParserNullVariableDeclaration", new String[] {"" + position});
        }
        else
        {
          macros.apply(file, "ParserSimpleVariableDeclaration", new String[] {type_name, type_name.toLowerCase(), "" + position});
        }
      }
      catch(IOException e)
      {
        throw new RuntimeException("An error occured while writing to " +
                                   new File(pkgDir, "Parser.java").getAbsolutePath());
      }
    }
  }

  @Override
  public void inASimpleTerm(ASimpleTerm node)
  {
    try
    {
      String type_name;
      if(simpleTermTransformMap.get(node) != null)
      {
        type_name = (String)simpleTermTransformMap.get(node);
      }
      else
      {
        type_name = (String)CG.getAltTransformElemTypes().get(node);
      }
      int position = ((Integer)CG.getTermNumbers().get(node)).intValue();
      String termKey = currentAlt+"."+node.getId().getText();
      int elemPosition = ((Integer)CTP.elems_position.get(termKey)).intValue();
      int positionMap = 0;

      if(node.getSimpleTermTail() != null )
      {
        if( !listSimpleTermTransform.contains(node.getId().getText() ) )
        {
          String type = (String)CTP.positionsMap.get( realcurrentAlt+"."+node.getId().getText() );
          LinkedList list = (LinkedList)transformIds.getProdTransformIds().prod_transforms.get(type);
          if( list.indexOf( node.getSimpleTermTail().getText() ) >= 0 )
          {
            positionMap = list.indexOf( node.getSimpleTermTail().getText() );
          }
        }

        if(simpleTermOrsimpleListTermTypes.get(node) != null)
        {
          String type = (String)simpleTermOrsimpleListTermTypes.get(node);
          LinkedList list = (LinkedList)transformIds.getProdTransformIds().prod_transforms.get(type);
          if( list.indexOf( node.getSimpleTermTail().getText() ) >= 0 )
          {
            positionMap = list.indexOf( node.getSimpleTermTail().getText() );
          }
        }
      }

      String type;
      if(type_name.startsWith("L"))
      {
        type_name = "list";
        type = "LinkedList";
      }
      else if(type_name.equals("null"))
      {
        type_name = "null";
        type = "Object";
      }
      else
      {
        type = type_name;
      }

      macros.apply(file, "ParserSimpleTerm", new String[]
                   {
                     type_name.toLowerCase(), ""+position,
                     type, ""+elemPosition, ""+positionMap
                   }
                  );
    }
    catch(IOException e)
    {
      throw new RuntimeException("An error occured while writing to " +
                                 new File(pkgDir, "Parser.java").getAbsolutePath());
    }
  }

  @Override
  public void inASimpleListTerm(ASimpleListTerm node)
  {
    try
    {
      String type_name;
      if(simpleTermTransformMap.get(node) != null)
      {
        type_name = (String)simpleTermTransformMap.get(node);
      }
      else
      {
        type_name = (String)CG.getAltTransformElemTypes().get(node);
      }

      String termKey = currentAlt+"."+node.getId().getText();
      int position = ((Integer)CG.getTermNumbers().get(node)).intValue();

      int elemPosition = ((Integer)CTP.elems_position.get(termKey)).intValue();

      int positionMap = 0;

      if(node.getSimpleTermTail() != null )
      {
        if( !listSimpleTermTransform.contains(node.getId().getText()) )
        {
          String type = (String)CTP.positionsMap.get( realcurrentAlt+"."+node.getId().getText() );
          LinkedList list = (LinkedList)transformIds.getProdTransformIds().prod_transforms.get(type);
          if( list.indexOf( node.getSimpleTermTail().getText() ) >= 0 )
          {
            positionMap = list.indexOf( node.getSimpleTermTail().getText() );
          }
        }

        if(simpleTermOrsimpleListTermTypes.get(node) != null)
        {
          String type = (String)simpleTermOrsimpleListTermTypes.get(node);
          LinkedList list = (LinkedList)transformIds.getProdTransformIds().prod_transforms.get(type);
          if( list.indexOf( node.getSimpleTermTail().getText() ) >= 0 )
          {
            positionMap = list.indexOf( node.getSimpleTermTail().getText() );
          }
        }
      }

      String type;
      if(type_name.startsWith("L"))
      {
        type_name = "list";
        type = "LinkedList";
      }
      else if(type_name.equals("null"))
      {
        type_name = "null";
        type = "Object";
      }
      else
      {
        type = type_name;
      }

      macros.apply(file, "ParserSimpleTerm", new String[]
                   {
                     type_name.toLowerCase(), ""+position,
                     type, ""+elemPosition, ""+positionMap
                   }
                  );
    }
    catch(IOException e)
    {
      throw new RuntimeException("An error occured while writing to " +
                                 new File(pkgDir, "Parser.java").getAbsolutePath());
    }
  }

  @Override
  public void inANewTerm(ANewTerm node)
  {
    try
    {
      macros.apply(file, "ParserBraceOpening");
    }
    catch(IOException e)
    {
      throw new RuntimeException("An error occured while writing to " +
                                 new File(pkgDir, "Parser.java").getAbsolutePath());
    }
    inAParams(node.getParams());
  }

  @Override
  public void inANewListTerm(ANewListTerm node)
  {
    try
    {
      macros.apply(file, "ParserBraceOpening");
    }
    catch(IOException e)
    {
      throw new RuntimeException("An error occured while writing to " +
                                 new File(pkgDir, "Parser.java").getAbsolutePath());
    }
    inAParams(node.getParams());
  }

  @Override
  public void inAListTerm(AListTerm node)
  {
    try
    {
      macros.apply(file, "ParserBraceOpening");
      Object temp[] = node.getListTerms().toArray();

      for(int i = 0; i < temp.length; i++)
      {
        String type_name;
        if(simpleTermTransformMap.get(temp[i]) != null)
        {
          type_name = (String)simpleTermTransformMap.get(temp[i]);
        }
        else
        {
          type_name = (String)CG.getAltTransformElemTypes().get(temp[i]);
        }
        int position = ((Integer)CG.getTermNumbers().get(temp[i])).intValue();

        if(type_name.startsWith("L"))
        {
          macros.apply(file, "ParserListVariableDeclaration", new String[] {"" + position});
        }
        else if(type_name.equals("null"))
        {
          macros.apply(file, "ParserNullVariableDeclaration", new String[] {"" + position});
        }
        else
        {
          macros.apply(file, "ParserSimpleVariableDeclaration", new String[] {type_name, type_name.toLowerCase(), "" + position});
        }
      }
    }
    catch(IOException e)
    {
      throw new RuntimeException("An error occured while writing to " +
                                 new File(pkgDir, "Parser.java").getAbsolutePath());
    }
  }

  @Override
  public void outAListTerm(AListTerm node)
  {
    try
    {
      Object temp[] = node.getListTerms().toArray();
      int listPosition = ((Integer)CG.getTermNumbers().get(node)).intValue();

      for(int i = 0; i < temp.length; i++)
      {
        String type_name;
        if(simpleTermTransformMap.get(temp[i]) != null)
        {
          type_name = (String)simpleTermTransformMap.get(temp[i]);
        }
        else
        {
          type_name = (String)CG.getAltTransformElemTypes().get(temp[i]);
        }
        int position = ((Integer)CG.getTermNumbers().get(temp[i])).intValue();

        if(!type_name.equals("null"))
        {
          if(type_name.startsWith("L"))
          {
            macros.apply(file, "ParserTypedLinkedListAddAll", new String[] {"list", ""+listPosition, "list", ""+ position});
          }
          else
          {
            macros.apply(file, "ParserTypedLinkedListAdd", new String[] {"list", ""+listPosition, type_name.toLowerCase(), ""+ position});
          }
        }
      }
      macros.apply(file, "ParserBraceClosing");
    }
    catch(IOException e)
    {
      throw new RuntimeException("An error occured while writing to " +
                                 new File(pkgDir, "Parser.java").getAbsolutePath());
    }
  }

  @Override
  public void outANewTerm(ANewTerm node)
  {
    String type_name = (String)CG.getAltTransformElemTypes().get(node);
    if(simpleTermTransformMap.get(node) != null)
    {
      type_name = (String)simpleTermTransformMap.get(node);
    }
    else
    {
      type_name = (String)CG.getAltTransformElemTypes().get(node);
    }
    int position = ((Integer)CG.getTermNumbers().get(node)).intValue();
    String newAltName = name((AProdName)node.getProdName());

    try
    {
      if(type_name.startsWith("L"))
      {
        type_name = "list";
      }
      else
      {
        type_name = type_name.toLowerCase();
      }
      macros.apply(file, "ParserNewBodyNew", new String[] {type_name, ""+position, newAltName});

      if(node.getParams().size() > 0)
      {
        Object temp[] = node.getParams().toArray();
        String isNotTheFirstParam = "";

        for(int i = 0; i < temp.length; i++)
        {
          if(simpleTermTransformMap.get(temp[i]) != null)
          {
            type_name = (String)simpleTermTransformMap.get(temp[i]);
          }
          else
          {
            type_name = (String)CG.getAltTransformElemTypes().get(temp[i]);
          }
          position = ((Integer)CG.getTermNumbers().get(temp[i])).intValue();

          if(i != 0)
          {
            isNotTheFirstParam = ", ";
          }

          if(type_name.equals("null"))
          {
            macros.apply(file, "ParserNew&ListBodyParamsNull", new String[] {isNotTheFirstParam+"null"});
          }
          else
          {
            if(type_name.startsWith("L"))
            {
              type_name = "list";
            }
            else
            {
              type_name = type_name.toLowerCase();
            }
            macros.apply(file, "ParserNew&ListBodyParams", new String[] {isNotTheFirstParam+type_name, ""+position});
          }

        }
      }
      macros.apply(file, "ParserNewBodyNewTail");
      macros.apply(file, "ParserBraceClosing");
    }
    catch(IOException e)
    {
      throw new RuntimeException("An error occured while writing to " +
                                 new File(pkgDir, "TokenIndex.java").getAbsolutePath());
    }
  }

  @Override
  public void outANewListTerm(ANewListTerm node)
  {
    String type_name;
    if(simpleTermTransformMap.get(node) != null)
    {
      type_name = (String)simpleTermTransformMap.get(node);
    }
    else
    {
      type_name = (String)CG.getAltTransformElemTypes().get(node);
    }
    int position = ((Integer)CG.getTermNumbers().get(node)).intValue();
    String newAltName = name((AProdName)node.getProdName());
    try
    {
      if(type_name.startsWith("L"))
      {
        type_name = "list";
      }
      else
      {
        type_name = type_name.toLowerCase();
      }
      macros.apply(file, "ParserNewBodyNew", new String[] {type_name, ""+position, newAltName});

      if(node.getParams().size() > 0)
      {
        Object temp[] = node.getParams().toArray();
        String isNotTheFirstParam = "";

        for(int i = 0; i < temp.length; i++)
        {
          if(simpleTermTransformMap.get(temp[i]) != null)
          {
            type_name = (String)simpleTermTransformMap.get(temp[i]);
          }
          else
          {
            type_name = (String)CG.getAltTransformElemTypes().get(temp[i]);
          }
          position = ((Integer)CG.getTermNumbers().get(temp[i])).intValue();

          if(i != 0)
          {
            isNotTheFirstParam = ", ";
          }

          if(type_name.equals("null"))
          {
            macros.apply(file, "ParserNew&ListBodyParamsNull", new String[] {isNotTheFirstParam+"null"});
          }
          else
          {
            if(type_name.startsWith("L"))
            {
              type_name = "list";
            }
            else
            {
              type_name = type_name.toLowerCase();
            }
            macros.apply(file, "ParserNew&ListBodyParams", new String[] {isNotTheFirstParam+type_name, ""+position});
          }
        }
      }
      macros.apply(file, "ParserNewBodyNewTail");
      macros.apply(file, "ParserBraceClosing");
    }
    catch(IOException e)
    {
      throw new RuntimeException("An error occured while writing to " +
                                 new File(pkgDir, "TokenIndex.java").getAbsolutePath());
    }
  }

  public String name(AProdName node)
  {
    if(node.getProdNameTail() != null)
    {
      return "A" +
             ResolveIds.name(node.getProdNameTail().getText()) +
             ResolveIds.name(node.getId().getText());
    }
    return "A" + ResolveIds.name(node.getId().getText());
  }

}
