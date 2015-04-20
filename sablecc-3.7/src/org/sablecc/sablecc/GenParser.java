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

/*
 * GenParser
 * 
 * This class is the responsible of generation of the parser.
 * It calls another classes which will do internal transformations
 * to the grammar in order to made it support by BNF parser generator
 * algorithm whereas SableCC3.x.x grammars can be written with some 
 * operators of the EBNF form.
 * It can also call an appropriate Tree-Walker which in case of conflict
 * will try to inline productions involved in the conflict with the aim of
 * resolving it.
 */

@SuppressWarnings({"rawtypes","unchecked"})
public class GenParser extends DepthFirstAdapter
{
  //This is the tree-walker field which made internal transformations("EBNF"->BNF) to the grammar
  InternalTransformationsToGrammar bnf_and_CST_AST_Transformations;

  //tree-walker field which construct data structures for generating of parsing tables.
  ConstructParserGenerationDatas genParserAdapter;

  private MacroExpander macros;
  private ResolveIds ids;
  private ResolveAltIds altIds;
  private ResolveTransformIds transformIds;

  // This class reference variable fills the map "altsElemTypes" from class AlternativeElementTypes. It associates
  // the name of elements with its types
  private AlternativeElementTypes AET;

  //This computes variables declarations position and type for parser generation.
  //In fact it helps to determine how many elements are needed to pop from the stack
  private ComputeCGNomenclature CG;

  //This helps to compute alternative transformations code generation.
  private ComputeSimpleTermPosition CTP;

  private File pkgDir;
  private String pkgName;
  private boolean hasProductions;
  private String firstProductionName;
  private boolean processInlining;
  private boolean prettyPrinting;
  private boolean grammarHasTransformations;

  // This boolean is used to check weither the filter() method in class Parser.java
  // should be present or not.
  private boolean activateFilter = true;

  //This tree-walker field generate the code of parsing and construction of the AST.
  GenerateAlternativeCodeForParser aParsedAltAdapter;

  private LinkedList listSimpleTermTransform = new LinkedList();

  public final Map simpleTermTransform =
    new TypedHashMap(NodeCast.instance,
                     StringCast.instance);

  //This map contains Productions which were explicitely transformed in the grammar
  //Those transformations was specified by the grammar-writer.
  private final Map mapProductionTransformations =
    new TypedHashMap(StringCast.instance,
                     ListCast.instance);

  private Map alts;

  public GenParser(ResolveIds ids, ResolveAltIds altIds, ResolveTransformIds transformIds,
                   String firstProductionName, boolean processInlining, boolean prettyPrinting,
                   boolean grammarHasTransformations)
  {
    this.ids = ids;
    this.altIds = altIds;
    this.transformIds = transformIds;
    this.processInlining = processInlining;
    this.prettyPrinting = prettyPrinting;
    this.grammarHasTransformations = grammarHasTransformations;

    AET = new AlternativeElementTypes(ids);
    CG = new ComputeCGNomenclature(ids, transformIds.getProdTransformIds());
    CTP = new ComputeSimpleTermPosition(ids);
    this.firstProductionName = firstProductionName;

    try
    {
      macros = new MacroExpander(
                 new InputStreamReader(
                   getClass().getResourceAsStream("parser.txt")));
    }
    catch(IOException e)
    {
      throw new RuntimeException("unable to open parser.txt.");
    }

    pkgDir = new File(ids.pkgDir, "parser");
    pkgName = ids.pkgName.equals("") ? "parser" : ids.pkgName + ".parser";

    if(!pkgDir.exists())
    {
      if(!pkgDir.mkdir())
      {
        throw new RuntimeException("Unable to create " + pkgDir.getAbsolutePath());
      }
    }
  }

  @Override
  public void caseStart(Start tree)
  {
    tree.getPGrammar().apply(new DepthFirstAdapter()
                             {
                               @Override
                               public void caseAProd(AProd node)
                               {
                                 hasProductions = true;
                                 if(node.getProdTransform() != null)
                                 {
                                   mapProductionTransformations.put("P"+ResolveIds.name(node.getId().getText()),
                                                                    node.getProdTransform().clone() );
                                 }
                               }
                             }
                            );

    if(!hasProductions)
    {
      return;
    }

    //Performing internal transformations
    bnf_and_CST_AST_Transformations =
      new InternalTransformationsToGrammar(ids, altIds, transformIds,
                                           listSimpleTermTransform,
                                           simpleTermTransform,
                                           mapProductionTransformations,
                                           transformIds.simpleTermOrsimpleListTermTypes);

    //apply internal transformations to the grammar.
    tree.getPGrammar().apply(bnf_and_CST_AST_Transformations);

    if(prettyPrinting)
    {
      tree.apply(new PrettyPrinter());
      return;
    }

    ConstructProdsMap mapOfProds = new ConstructProdsMap();
    tree.apply(mapOfProds);

    boolean computeLALR = false;

    //This do-while loop is managing the inlining process.
    do
    {
      //Initialization of parsing tables and some symbol tables
      //names and elemTypes from ResolveIds.
      reinit();
      reConstructSymbolTables(tree);

      tree.apply(new DepthFirstAdapter()
                 {
                   private boolean hasAlternative;

                   @Override
                   public void caseATokenDef(ATokenDef node)
                   {
                     String name = (String) ids.names.get(node);
                     String errorName = (String) ids.errorNames.get(node);

                     if(!ids.ignTokens.containsKey(name))
                     {
                       Grammar.addTerminal(name, errorName);
                     }
                   }

                   @Override
                   public void inAProd(AProd node)
                   {
                     hasAlternative = false;
                   }

                   @Override
                   public void inAAlt(AAlt node)
                   {
                     hasAlternative = true;
                   }

                   @Override
                   public void outAProd(AProd node)
                   {
                     if(hasAlternative)
                     {
                       Grammar.addNonterminal((String) ids.names.get(node));
                     }
                   }
                 }
                );

      //Construct all necessary informations for generation of the parser.
      //This map contains all the alternatives of the transformed final grammar
      alts = new TypedHashMap(StringCast.instance, NodeCast.instance);

      tree.getPGrammar().apply(new ConstructParserGenerationDatas(ids, alts));

      try
      {
        //Generation of parsing symbol tables
        Grammar.computeLALR();
        computeLALR = true;
      }
      catch(ConflictException ce)
      {
        if(activateFilter)
        {
          activateFilter = false;
        }

        //Here, we are trying to inline the grammar with production imply in the conflict.
        if(processInlining)
        {
          ComputeInlining grammarToBeInlinedWithConflictualProductions = new ComputeInlining(ce.getConflictualProductions(),
              mapOfProds.productionsMap,
              tree);
          if(!grammarToBeInlinedWithConflictualProductions.computeInlining())
          {
            System.out.println("\nA previous conflict that we've tried to solve by inline some productions inside the grammars cannot be solved that way. The transformed grammar is : ");
            tree.apply(new PrettyPrinter());
            throw new RuntimeException(ce.getMessage());
          }

          System.out.println();
          System.out.println("Inlining.");
        }
        else
        {
          throw new RuntimeException(ce.getMessage());
        }
      }
    }
    while(!computeLALR);

    tree.getPGrammar().apply(AET);
    CG.setAltElemTypes(AET.getMapOfAltElemType());
    tree.getPGrammar().apply(CG);
    tree.getPGrammar().apply(CTP);

    createParser();
    createParserException();
    createState();
    createTokenIndex();
  }

  public void reinit()
  {
    // re-initialize all static structures in the engine
    LR0Collection.reinit();
    Symbol.reinit();
    Production.reinit();
    Grammar.reinit();
    ids.reinit();
  }

  private String currentProd;
  private String currentAlt;

  //reconstruction of map names of class ResolveIds
  public void reConstructSymbolTables(Start tree)
  {
    tree.apply(new DepthFirstAdapter()
               {
                 @Override
                 public void caseAProd(AProd node)
                 {
                   currentProd = ResolveIds.name(node.getId().getText());
                   String name = "P" + currentProd;

                   ids.names.put(node, name);

                   //list of inAAlt code.
                   Object []list_alt = (Object [])node.getAlts().toArray();
                   for(int i = 0; i< list_alt.length; i++)
                   {
                     ((PAlt)list_alt[i]).apply(this);
                   }
                 }

                 @Override
                 public void outAHelperDef(AHelperDef node)
                 {
                   String name = node.getId().getText();
                   ids.names.put(node, name);
                 }

                 @Override
                 public void outATokenDef(ATokenDef node)
                 {
                   String name = "T" + ResolveIds.name(node.getId().getText());

                   ids.names.put(node, name);
                 }

                 @Override
                 public void caseAAlt(final AAlt alt)
                 {
                   if(alt.getAltName() != null)
                   {
                     currentAlt =
                       "A" +
                       ResolveIds.name(alt.getAltName().getText()) +
                       currentProd;

                     ids.names.put(alt, currentAlt);
                   }
                   else
                   {
                     currentAlt = "A" + currentProd;
                     ids.names.put(alt, currentAlt);
                   }

                   AElem list_elem[] = (AElem[]) alt.getElems().toArray(new AElem[0]);
                   for(int i=0; i<list_elem.length;i++)
                   {
                     list_elem[i].apply(this);
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
                     ids.names.put(elem, ResolveIds.name(elem.getElemName().getText()) );
                   }
                   else
                   {
                     ids.names.put(elem, ResolveIds.name(elem.getId().getText()));
                   }
                 }
               }
              );
  }

  //Parser.java Generation
  private void createParser()
  {
    BufferedWriter file;

    try
    {
      file = new BufferedWriter(
               new FileWriter(
                 new File(pkgDir, "Parser.java")));
    }
    catch(IOException e)
    {
      throw new RuntimeException("Unable to create " + new File(pkgDir, "Parser.java").getAbsolutePath());
    }

    try
    {
//      Symbol[] terminals = Symbol.terminals();
      Symbol[] nonterminals = Symbol.nonterminals();
      Production[] productions = Production.productions();

      macros.apply(file, "ParserHeader", new String[] {pkgName,
                   ids.pkgName.equals("") ? "lexer" : ids.pkgName + ".lexer",
                   ids.pkgName.equals("") ? "node" : ids.pkgName + ".node",
                   ids.pkgName.equals("") ? "analysis" : ids.pkgName + ".analysis"});

      if(activateFilter && !grammarHasTransformations)
      {
        macros.apply(file, "ParserNoInliningPushHeader");
        macros.apply(file, "ParserCommon", new String[] {", true", ", false"});
      }
      else
      {
        macros.apply(file, "ParserInliningPushHeader");
        macros.apply(file, "ParserCommon", new String[] {"", ""});
      }

      for(int i = 500; i < (productions.length - 1); i += 500) {
          macros.apply(file, "ParseReduceElseIf", new String[] {"" + (i + 500), "" + i});
      }
      
      macros.apply(file, "ParserParseTail", new String[] {firstProductionName});

      macros.apply(file, "ParserReduceHead", new String[] {"0"});
      
      //this loop generates the code for all possible reductions and the type of
      //the node needed to be created at a local point.
      for(int i = 0; i < (productions.length - 1); i++)
      {
//        Node node = (Node) alts.get(productions[i].name);
          
        if(i % 500 == 0 && i != 0) {
            macros.apply(file, "ParserReduceTail", new String[] {});
            macros.apply(file, "ParserReduceHead", new String[] {"" + i});            
        }

        if(activateFilter && !grammarHasTransformations)
        {
          macros.apply(file, "ParserNoInliningReduce", new String[] {
                         "" + productions[i].index,
                         "" + productions[i].leftside,
                         "" + (productions[i].name.startsWith("ANonTerminal$") ||
                               productions[i].name.startsWith("ATerminal$")),
                         productions[i].name});
        }
        else
        {
          macros.apply(file, "ParserInliningReduce", new String[] {
                         "" + productions[i].index,
                         "" + productions[i].leftside,
                         productions[i].name});
        }
      }

      macros.apply(file, "ParserReduceTail", new String[] {});

      //the node creation code. Reduce methods definitions are done here
      for(int i = 0; i < (productions.length - 1); i++)
      {
        macros.apply(file, "ParserNewHeader", new String[] {
                       "" + productions[i].index,
                       productions[i].name});

        final Node node = (Node) alts.get(productions[i].name);

//        final BufferedWriter finalFile = file;
        final LinkedList stack = new LinkedList();

        node.apply(new DepthFirstAdapter()
                   {
                     private int current;

                     @Override
                     public void caseAElem(AElem elem)
                     {
                       current++;

                       stack.addFirst(new Element("ParserNewBodyDecl",
                                                  new String[] {"" + current}));
                     }
                   }
                  );

        try
        {
          for(Iterator it = stack.iterator(); it.hasNext();)
          {
            Element e = (Element) it.next();
            macros.apply(file, e.macro, e.arguments);
          }
        }
        catch(IOException e)
        {
          throw new RuntimeException("An error occured while writing to " +
                                     new File(pkgDir, "Parser.java").getAbsolutePath());
        }

        String nodeName = (String)ids.names.get(node);
        String realnodeName = (String)ids.names.get(node);
        aParsedAltAdapter =
          new GenerateAlternativeCodeForParser(pkgDir, nodeName, realnodeName,
                                               file, transformIds, CG, CTP,
                                               simpleTermTransform, macros,
                                               listSimpleTermTransform,
                                               transformIds.simpleTermOrsimpleListTermTypes);
        node.apply(aParsedAltAdapter);
      }

      macros.apply(file, "ParserActionHeader");

      StringBuffer table = new StringBuffer();

      DataOutputStream out = new DataOutputStream(
                               new BufferedOutputStream(
                                 new FileOutputStream(
                                   new File(pkgDir, "parser.dat"))));

      Vector outerArray = new Vector();
      //Generating of paring tables
      for(int i = 0; i < Grammar.action_.length; i++)
      {
        Vector innerArray = new Vector();

        String mostFrequentAction = "ERROR";
        int mostFrequentDestination = i;
        int frequence = 0;
        Map map = new TreeMap(IntegerComparator.instance);

        for(int j = 0; j < Grammar.action_[i].length; j++)
        {
          if(Grammar.action_[i][j] != null)
          {
            if(Grammar.action_[i][j][0] == 1)
            {
              Integer index = new Integer(Grammar.action_[i][j][1]);
              Integer count = (Integer) map.get(index);
              int freq = count == null ? 0 : count.intValue();
              map.put(index, new Integer(++freq));
              if(freq > frequence)
              {
                frequence = freq;
                mostFrequentAction = "REDUCE";
                mostFrequentDestination = Grammar.action_[i][j][1];
              }
            }
          }
        }
        table.append("\t\t\t{");

        table.append("{" + -1 + ", " +
                     mostFrequentAction + ", " +
                     mostFrequentDestination + "}, ");
        innerArray.addElement(
          new int[] {-1,
                     mostFrequentAction.equals("ERROR") ? 3 : 1,
                     mostFrequentDestination});

        for(int j = 0; j < Grammar.action_[i].length; j++)
        {
          if(Grammar.action_[i][j] != null)
          {
            switch(Grammar.action_[i][j][0])
            {
            case 0:
              table.append("{" + j + ", SHIFT, " + Grammar.action_[i][j][1] + "}, ");
              innerArray.addElement(new int[] {j, 0, Grammar.action_[i][j][1]});
              break;
            case 1:
              if(Grammar.action_[i][j][1] != mostFrequentDestination)
              {
                table.append("{" + j + ", REDUCE, " + Grammar.action_[i][j][1] + "}, ");
                innerArray.addElement(new int[] {j, 1, Grammar.action_[i][j][1]});
              }
              break;
            case 2:
              table.append("{" + j + ", ACCEPT, -1}, ");
              innerArray.addElement(new int[] {j, 2, -1});
              break;
            }
          }
        }

        table.append("}," + System.getProperty("line.separator"));
        outerArray.addElement(innerArray);
      }

      file.write("" + table);

      out.writeInt(outerArray.size());
      for(Enumeration e = outerArray.elements(); e.hasMoreElements();)
      {
        Vector innerArray = (Vector) e.nextElement();
        out.writeInt(innerArray.size());
        for(Enumeration n = innerArray.elements(); n.hasMoreElements();)
        {
          int[] array = (int[]) n.nextElement();

          for(int i = 0; i < 3; i++)
          {
            out.writeInt(array[i]);
          }
        }
      }

      macros.apply(file, "ParserActionTail");

      macros.apply(file, "ParserGotoHeader");

      table = new StringBuffer();
      outerArray = new Vector();

      for(int j = 0; j < nonterminals.length - 1; j++)
      {
        Vector innerArray = new Vector();

        int mostFrequent = -1;
        int frequence = 0;
        Map map = new TreeMap(IntegerComparator.instance);

        for(int i = 0; i < Grammar.goto_.length; i++)
        {
          if(Grammar.goto_[i][j] != -1)
          {
            Integer index = new Integer(Grammar.goto_[i][j]);
            Integer count = (Integer) map.get(index);
            int freq = count == null ? 0 : count.intValue();
            map.put(index, new Integer(++freq));
            if(freq > frequence)
            {
              frequence = freq;
              mostFrequent = Grammar.goto_[i][j];
            }
          }
        }

        table.append("\t\t\t{");

        table.append("{" + (-1) + ", " + mostFrequent + "}, ");
        innerArray.addElement(new int[] {-1, mostFrequent});

        for(int i = 0; i < Grammar.goto_.length; i++)
        {
          if((Grammar.goto_[i][j] != -1) &&
              (Grammar.goto_[i][j] != mostFrequent))
          {
            table.append("{" + i + ", " + Grammar.goto_[i][j] + "}, ");
            innerArray.addElement(new int[] {i, Grammar.goto_[i][j]});
          }
        }

        table.append("}," + System.getProperty("line.separator"));

        outerArray.addElement(innerArray);
      }

      file.write("" + table);

      out.writeInt(outerArray.size());
      for(Enumeration e = outerArray.elements(); e.hasMoreElements();)
      {
        Vector innerArray = (Vector) e.nextElement();
        out.writeInt(innerArray.size());
        for(Enumeration n = innerArray.elements(); n.hasMoreElements();)
        {
          int[] array = (int[]) n.nextElement();

          for(int i = 0; i < 2; i++)
          {
            out.writeInt(array[i]);
          }
        }
      }

      macros.apply(file, "ParserGotoTail");

      macros.apply(file, "ParserErrorsHeader");

      table = new StringBuffer();
      StringBuffer index = new StringBuffer();
      int nextIndex = 0;

      Map errorIndex = new TypedTreeMap(
                         StringComparator.instance,
                         StringCast.instance,
                         IntegerCast.instance);

      outerArray = new Vector();
      Vector indexArray = new Vector();

      index.append("\t\t\t");
      for(int i = 0; i < Grammar.action_.length; i++)
      {
        StringBuffer s = new StringBuffer();
        s.append("expecting: ");

        boolean comma = false;
        for(int j = 0; j < Grammar.action_[i].length; j++)
        {
          if(Grammar.action_[i][j] != null)
          {
            if(comma)
            {
              s.append(", ");
            }
            else
            {
              comma = true;
            }

            s.append(Symbol.symbol(j, true).errorName);
          }
        }

        if(errorIndex.containsKey(s.toString()))
        {
          index.append(errorIndex.get(s.toString()) + ", ");
          indexArray.addElement(errorIndex.get(s.toString()));
        }
        else
        {
          table.append("\t\t\t\"" + s + "\"," + System.getProperty("line.separator"));
          outerArray.addElement(s.toString());
          errorIndex.put(s.toString(), new Integer(nextIndex));
          indexArray.addElement(new Integer(nextIndex));
          index.append(nextIndex++ + ", ");
        }
      }

      file.write("" + table);

      out.writeInt(outerArray.size());
      for(Enumeration e = outerArray.elements(); e.hasMoreElements();)
      {
        String s = (String) e.nextElement();
        out.writeInt(s.length());
        int length = s.length();
        for(int i = 0; i < length; i++)
        {
          out.writeChar(s.charAt(i));
        }
      }

      out.writeInt(indexArray.size());
      for(Enumeration e = indexArray.elements(); e.hasMoreElements();)
      {
        Integer n = (Integer) e.nextElement();
        out.writeInt(n.intValue());
      }

      out.close();

      macros.apply(file, "ParserErrorsTail");

      macros.apply(file, "ParserErrorIndexHeader");
      file.write("" + index);
      macros.apply(file, "ParserErrorIndexTail");

      macros.apply(file, "ParserTail");
    }
    catch(IOException e)
    {
      throw new RuntimeException("An error occured while writing to " +
                                 new File(pkgDir, "Parser.java").getAbsolutePath());
    }

    try
    {
      file.close();
    }
    catch(IOException e)
    {}
  }

  private void createTokenIndex()
  {
    BufferedWriter file;

    try
    {
      file = new BufferedWriter(
               new FileWriter(
                 new File(pkgDir, "TokenIndex.java")));
    }
    catch(IOException e)
    {
      throw new RuntimeException("Unable to create " + new File(pkgDir, "TokenIndex.java").getAbsolutePath());
    }

    try
    {
      Symbol[] terminals = Symbol.terminals();

      macros.apply(file, "TokenIndexHeader", new String[] {pkgName,
                   ids.pkgName.equals("") ? "node" : ids.pkgName + ".node",
                   ids.pkgName.equals("") ? "analysis" : ids.pkgName + ".analysis"});

      for(int i = 0; i < (terminals.length - 2); i++)
      {
        macros.apply(file, "TokenIndexBody", new String[] {terminals[i].name, "" + i});
      }

      macros.apply(file, "TokenIndexTail", new String[] {"" + (terminals.length - 2)});
    }
    catch(IOException e)
    {
      throw new RuntimeException("An error occured while writing to " +
                                 new File(pkgDir, "TokenIndex.java").getAbsolutePath());
    }

    try
    {
      file.close();
    }
    catch(IOException e)
    {}
  }

  private void createParserException()
  {
    BufferedWriter file;

    try
    {
      file = new BufferedWriter(
               new FileWriter(
                 new File(pkgDir, "ParserException.java")));
    }
    catch(IOException e)
    {
      throw new RuntimeException("Unable to create " + new File(pkgDir, "ParserException.java").getAbsolutePath());
    }

    try
    {
      macros.apply(file, "ParserException", new String[] {pkgName,
                   ids.pkgName.equals("") ? "node" : ids.pkgName + ".node"});
    }
    catch(IOException e)
    {
      throw new RuntimeException("An error occured while writing to " +
                                 new File(pkgDir, "ParserException.java").getAbsolutePath());
    }

    try
    {
      file.close();
    }
    catch(IOException e)
    {}
  }

  private void createState()
  {
    BufferedWriter file;

    try
    {
      file = new BufferedWriter(
               new FileWriter(
                 new File(pkgDir, "State.java")));
    }
    catch(IOException e)
    {
      throw new RuntimeException("Unable to create " + new File(pkgDir, "State.java").getAbsolutePath());
    }

    try
    {
      macros.apply(file, "State", new String[] {pkgName});
    }
    catch(IOException e)
    {
      throw new RuntimeException("An error occured while writing to " +
                                 new File(pkgDir, "State.java").getAbsolutePath());
    }

    try
    {
      file.close();
    }
    catch(IOException e)
    {}
  }
/*
  private int count(String name)
  {
    if(name.charAt(0) != 'X')
    {
      return 0;
    }

    StringBuffer s = new StringBuffer();
    int i = 1;

    while((i < name.length()) &&
          (name.charAt(i) >= '0') &&
          (name.charAt(i) <= '9'))
    {
      s.append(name.charAt(i++));
    }

    return Integer.parseInt(s.toString());
  }
*/
/*
  private String name(String name)
  {
    if(name.charAt(0) != 'X')
    {
      return name;
    }

    int i = 1;
    while((i < name.length()) &&
          (name.charAt(i) >= '0') &&
          (name.charAt(i) <= '9'))
    {
      i++;
    }

    return name.substring(i);
  }
*/
  static class Element
  {
    String macro;
    String[] arguments;

    Element(String macro, String[] arguments)
    {
      this.macro = macro;
      this.arguments = arguments;
    }
  }
}
