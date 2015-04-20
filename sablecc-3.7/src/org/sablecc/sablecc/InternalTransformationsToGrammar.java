/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * This file is part of SableCC.                             *
 * See the file "LICENSE" for copyright information and the  *
 * terms and conditions for copying, distribution and        *
 * modification of SableCC.                                  *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/*
* Last Modification date : October, the 11th 2003
* Goal of the modification : addition of specifier to
* the generated production($prod) to handle list of element.
*
*
*/

package org.sablecc.sablecc;

import java.util.*;

import org.sablecc.sablecc.analysis.*;
import org.sablecc.sablecc.node.*;

@SuppressWarnings({"rawtypes","unchecked"})
public class InternalTransformationsToGrammar extends DepthFirstAdapter
{

  private ResolveIds ids;
  private ResolveAltIds altIds;
  private ResolveTransformIds transformIds;
  private String currentProd;
//  private int currentAlt;
  private String currentAltName;
  private boolean processingAst;
//  private boolean processingProdTransform;

  static final int NONE = 0;
  static final int STAR = 1;
  static final int QMARK = 2;
  static final int PLUS = 3;

  int count;
  int elem;

  private LinkedList listSimpleTermTransform;

  public final Map simpleTermTransform;
  Map mapProductionTransformations;
  Map simpleTermOrsimpleListTermTypes;

  private Map isElementIsAlist = new TypedTreeMap(
                                   StringComparator.instance,
                                   StringCast.instance,
                                   StringCast.instance);

  private LinkedList listProd;

  public InternalTransformationsToGrammar(ResolveIds ids, ResolveAltIds altIds,
                                          ResolveTransformIds transformIds,
                                          LinkedList listSimpleTermTransform,
                                          Map simpleTermTransform,
                                          Map mapProductionTransformations,
                                          Map simpleTermOrsimpleListTermTypes)
  {
    this.ids = ids;
    this.altIds = altIds;
    this.transformIds = transformIds;
    this.listSimpleTermTransform = listSimpleTermTransform;
    this.simpleTermTransform = simpleTermTransform;
    this.mapProductionTransformations = mapProductionTransformations;
    this.simpleTermOrsimpleListTermTypes = simpleTermOrsimpleListTermTypes;
  }

  @Override
  public void inAProductions(AProductions node)
  {
    listProd = node.getProds();
  }

  private LinkedList listOfAlts;

  @Override
  public void inAAst(AAst node)
  {
    processingAst = true;
  }

  @Override
  public void outAAst(AAst node)
  {
    processingAst = false;
  }

  @Override
  public void caseAProd(AProd node)
  {
    currentProd = (String) ids.names.get(node);
    listOfAlts = new LinkedList();

    Object[] list_alt = (Object[])node.getAlts().toArray();
    for(int i=0; i<list_alt.length; i++)
    {
      ((PAlt) list_alt[i]).apply(this);
    }

    node.setAlts(listOfAlts);
  }

  private LinkedList listElems;
  private AAlt aParsedAlt;
  private LinkedList listElemsAltTransform;
  private String currentNewAltName;

  boolean countElementNecessary;

  LinkedList listOfAlternativeElemsWHaveName;

  @Override
  public void caseAAlt(AAlt node)
  {
    count = 1;
    currentAltName = (String) ids.names.get(node);

    AAltTransform currentAltTransform = (AAltTransform)node.getAltTransform();

    listOfAlternativeElemsWHaveName = new LinkedList();

    node.apply(new DepthFirstAdapter()
               {
                 @Override
                 public void inAElem(AElem node)
                 {
                   InternalTransformationsToGrammar.this.setOut(node, new Integer(NONE));
                 }

                 @Override
                 public void caseAStarUnOp(AStarUnOp node)
                 {
                   count *= 2;
                   InternalTransformationsToGrammar.this.setOut(node.parent(), new Integer(STAR));
                 }

                 @Override
                 public void caseAQMarkUnOp(AQMarkUnOp node)
                 {
                   count *= 2;
                   InternalTransformationsToGrammar.this.setOut(node.parent(), new Integer(QMARK));
                 }

                 @Override
                 public void caseAPlusUnOp(APlusUnOp node)
                 {
                   InternalTransformationsToGrammar.this.setOut(node.parent(), new Integer(PLUS));
                 }
               }
              );

    if(count == 1)
    {
      listElems = new LinkedList();
      listElemsAltTransform = new LinkedList();

      countElementNecessary = false;

      Object temp[] = node.getElems().toArray();
      for(int i = 0; i < temp.length; i++)
      {
        Object obj = temp[i];

        if( ((AElem)obj).getUnOp() != null &&
            ( ((AElem)obj).getUnOp() instanceof AQMarkUnOp ||
              ((AElem)obj).getUnOp() instanceof AStarUnOp )
          )
        {
          if(!countElementNecessary)
          {
            countElementNecessary = true;
          }
        }
      }

      for(int i = 0; i < temp.length; i++)
      {
        ((PElem)temp[i]).apply(this);
      }

      TId nameOfAlt = null;

      if(node.getAltName() != null)
      {
        nameOfAlt = (TId)node.getAltName().clone();
      }

      currentNewAltName = currentProd + "." + currentAltName.toLowerCase();
      altIds.alts_elems.put(currentNewAltName, listElemsAltTransform);
      altIds.alts_elems_list_elemName.put(currentNewAltName, listOfAlternativeElemsWHaveName);

      AAltTransform altTransform = (AAltTransform)currentAltTransform.clone();

      AltTransformAdapter altTransformAdapter =
        new AltTransformAdapter(simpleTermTransform, listSimpleTermTransform,
                                currentNewAltName, altIds,
                                isElementIsAlist,
                                simpleTermOrsimpleListTermTypes);

      altTransform.apply(altTransformAdapter);

      aParsedAlt = new AAlt(nameOfAlt, listElems, altTransform);

      ids.names.put(aParsedAlt, ids.names.get(node));

      listOfAlts.add(aParsedAlt);
    }
    else
    {
      int max = count;
      AAltTransform altTransform;

      for(count = 0; count < max; count++)
      {
        listElems = new LinkedList();
        listElemsAltTransform = new LinkedList();

        elem = 0;

        currentNewAltName = currentProd + "." + currentAltName.toLowerCase()+(count + 1);

        countElementNecessary = false;

        Object temp[] = node.getElems().toArray();
        for(int i = 0; i < temp.length; i++)
        {
          Object obj = temp[i];

          if( ((AElem)obj).getUnOp() != null &&
              ( ((AElem)obj).getUnOp() instanceof AQMarkUnOp ||
                ((AElem)obj).getUnOp() instanceof AStarUnOp )
            )
          {
            if(!countElementNecessary)
            {
              countElementNecessary = true;
            }
          }
        }

        for(int i = 0; i < temp.length; i++)
        {
          ((PElem)temp[i]).apply(this);
        }

        altIds.alts_elems.put(currentNewAltName, listElemsAltTransform);
        altIds.alts_elems_list_elemName.put(currentNewAltName, listOfAlternativeElemsWHaveName);

        altTransform = (AAltTransform)currentAltTransform.clone();

        AltTransformAdapter altTransformAdapter =
          new AltTransformAdapter(simpleTermTransform, listSimpleTermTransform,
                                  currentNewAltName, altIds,
                                  isElementIsAlist,
                                  simpleTermOrsimpleListTermTypes);

        altTransform.apply(altTransformAdapter);
        aParsedAlt = new AAlt(new TId(currentAltName.toLowerCase()+(count + 1)), listElems, altTransform);

        String currentAltInlining;
        currentAltInlining = "A" + ResolveIds.name(aParsedAlt.getAltName().getText()) + currentProd;
        ids.names.put(aParsedAlt, currentAltInlining);

        listOfAlts.add(aParsedAlt);
      }
    }
  }

  LinkedList checkCreationOfXElem = new TypedLinkedList(StringCast.instance);

  //It's also available for Ignored alternatives
  @Override
  public void caseAElem(AElem node)
  {
    if(!processingAst)
    {
      int op = ((Integer) getOut(node)).intValue();
      String name = (String) ids.elemTypes.get(node);
      String numero = (countElementNecessary == true ? ""+(count+1) : "" );
      String qMarkOrPlusElemType;
      String elemNameOfElem = null;

      TId aElemName = null;
      PSpecifier specifier = null;

      if(node.getElemName() != null)
      {
        elemNameOfElem = node.getElemName().getText();
        aElemName = new TId(elemNameOfElem);
      }

      if(node.getSpecifier() != null)
      {
        if(node.getSpecifier() instanceof ATokenSpecifier)
        {
          specifier = new ATokenSpecifier();
        }
        else
        {
          specifier = new AProductionSpecifier();
        }
      }

      AElem aElem = null;
      String elemName = node.getId().getText();
      boolean ok = false;
      boolean oklist = false;

      switch(op)
      {
      case NONE:
        {
          aElem = new AElem(aElemName, specifier, new TId(elemName), null);
          if(elemNameOfElem != null)
          {
            ids.names.put(aElem, ResolveIds.name(elemNameOfElem));
          }
          else
          {
            ids.names.put(aElem, ResolveIds.name(elemName));
          }
          ok = true;
        }
        break;
      case STAR:
        {

          if((count & (1 << elem)) != 0)
          {
            qMarkOrPlusElemType = (String)ids.elemTypes.get(node);
            LinkedList tmpProdTransform = (LinkedList)mapProductionTransformations.get(qMarkOrPlusElemType);

            if(!checkCreationOfXElem.contains("$" + elemName))
            {
              checkCreationOfXElem.add("$" + elemName);
              listProd.add( createXelemProduction("$" + elemName, qMarkOrPlusElemType,
                                                  name, tmpProdTransform) );
            }

            elemName = "$" + elemName;
            aElem = new AElem(aElemName, new AProductionSpecifier(), new TId(elemName), null);

            if(elemNameOfElem != null)
            {
              ids.names.put(aElem, ResolveIds.name(elemNameOfElem));
            }
            else
            {
              ids.names.put(aElem, ResolveIds.name(elemName));
            }

            ok = true;
            oklist = true;
          }

          elem++;
        }
        break;
      case QMARK:
        {
          if((count & (1 << elem)) != 0)
          {
            aElem = new AElem(aElemName, specifier, new TId(elemName), null);

            if(elemNameOfElem != null)
            {
              ids.names.put(aElem, ResolveIds.name(elemNameOfElem));
            }
            else
            {
              ids.names.put(aElem, ResolveIds.name(elemName));
            }

            ok = true;
          }

          elem++;
        }
        break;
      case PLUS:
        {
          qMarkOrPlusElemType = (String)ids.elemTypes.get(node);
          LinkedList tmpProdTransform = (LinkedList)mapProductionTransformations.get(qMarkOrPlusElemType);

          if(!checkCreationOfXElem.contains("$" + elemName))
          {
            checkCreationOfXElem.add("$" + elemName);
            listProd.add( createXelemProduction("$" + elemName, qMarkOrPlusElemType,
                                                name, tmpProdTransform) );
          }

          elemName = "$" + elemName;
          aElem = new AElem(aElemName, new AProductionSpecifier(), new TId(elemName), null);

          if(elemNameOfElem != null)
          {
            ids.names.put(aElem, ResolveIds.name(elemNameOfElem));
          }
          else
          {
            ids.names.put(aElem, ResolveIds.name(elemName));
          }

          ok = true;
          oklist = true;
        }
        break;
      }

      if(ok)
      {
        if(aElemName != null)
        {
          listElemsAltTransform.add(aElemName.getText());
          if(oklist)
          {
            if(elemNameOfElem != null)
            {
              listOfAlternativeElemsWHaveName.add(elemNameOfElem);
            }
            isElementIsAlist.put(currentProd+"."+currentAltName.toLowerCase()+numero+aElemName.getText(),
                                 node.getId().getText());
          }
        }
        else
        {
          listElemsAltTransform.add(elemName);
          if(oklist)
          {
            if(elemNameOfElem != null)
            {
              listOfAlternativeElemsWHaveName.add(elemNameOfElem);
            }
            isElementIsAlist.put(currentProd+"."+currentAltName.toLowerCase()+numero+node.getId().getText(),
                                 node.getId().getText());
          }
        }
      }

      if(aElem != null)
      {
        listElems.add(aElem);
      }
    }
  }

  /*
    This method creates the production for star(*) and plus(+) substitution in the grammar
    elem* -> $elem |
             elem
    This creates the production ::
                                   $elem                                   {-> elem* } 

  		          = {nonTerminal} $elem elem       {-> [$elem.elem elem] }
  			  | {terminal}    elem             {-> [elem] }
  			  ;
  */
  public AProd createXelemProduction(final String name, final String elemTypeName,
                                     String XproductionName,
                                     LinkedList nodeProdTransform)
  {
    final String rname = name.substring(1);
    LinkedList listOfAltsXelem = new LinkedList();

    if(nodeProdTransform != null)
    {
      nodeProdTransform = (LinkedList)cloneList(nodeProdTransform);

      //Creation of the production transformation for Xelem
      //if the production transformation is introduced by the software
      if(nodeProdTransform.size() == 1)
      {
        AElem elem = (AElem)nodeProdTransform.get(0);
        if(elem.getUnOp() == null && elem.getId().getText().equals(rname))
        {
          LinkedList elemsProdTransform = new LinkedList();
          elemsProdTransform.add( new AElem( null, new AProductionSpecifier(), new TId(rname), new AStarUnOp() ) );
          nodeProdTransform = elemsProdTransform;
        }
      }

    }
    //That means elem is token type
    else
    {
//      String name_resolved = ResolveIds.name(name);

      LinkedList elemsProdTransform = new LinkedList();
      elemsProdTransform.add( new AElem( null, new ATokenSpecifier(), new TId(rname), new AStarUnOp() ) );
      nodeProdTransform = elemsProdTransform;
    }

    final LinkedList listProdTransformationOfXelem = new LinkedList();

    AElem []temp_listProdTransform = (AElem[])nodeProdTransform.toArray(new AElem[0]);
    for(int i=0; i<temp_listProdTransform.length; i++)
    {
      temp_listProdTransform[i].apply( new DepthFirstAdapter()
                                       {
                                         @Override
                                         public void caseAElem(AElem node)
                                         {
                                           //The production transformation needs to have a star operator.
                                           node.setUnOp(new AStarUnOp(new TStar()));
                                           if(node.getElemName() != null)
                                           {
                                             listProdTransformationOfXelem.add( node.getElemName().getText() );
                                           }
                                           else
                                           {
                                             listProdTransformationOfXelem.add( node.getId().getText() );
                                           }
                                         }
                                       }
                                     );
    }

    //creation of the first AltTransform node
    AElem[] prodTransformElems = (AElem[]) nodeProdTransform.toArray(new AElem[0]);

    final LinkedList listTerms_first = new LinkedList();

    for(int i = 0; i < prodTransformElems.length; i++)
    {
      prodTransformElems[i].apply(new AnalysisAdapter()
                                  {
                                    @Override
                                    public void caseAElem(AElem node)
                                    {
                                      String tmpNodeName = ( (node.getElemName() == null) ? node.getId().getText() :
                                                             node.getElemName().getText() );
                                      LinkedList listAListTerm_first = new LinkedList();

                                      if(elemTypeName.startsWith("T"))
                                      {
                                        listAListTerm_first.add(new ASimpleListTerm(new ATokenSpecifier(),
                                                                new TId(rname), null ));
                                      }
                                      else
                                      {
                                        listAListTerm_first.add(new ASimpleListTerm(new AProductionSpecifier(),
                                                                new TId(rname),new TId(tmpNodeName) ) );
                                      }
                                      listTerms_first.add( new AListTerm(new TLBkt(), listAListTerm_first) );
                                    }
                                  }
                                 );
    }

    AAltTransform aAltTransform = new AAltTransform(new TLBrace(), listTerms_first, new TRBrace());

    //create the first list of elems  of an alternative
    LinkedList elems = new LinkedList();
    AElem aElemFirstTobeAdded;
    //the elem is a token
    if(elemTypeName.startsWith("T"))
    {
      aElemFirstTobeAdded = new AElem(null, new ATokenSpecifier(), new TId(rname), null);
    }
    else
    {
      aElemFirstTobeAdded = new AElem(null, new AProductionSpecifier(), new TId(rname), null);
    }
    elems.add(aElemFirstTobeAdded);

    //creation of the first alternative
    AAlt aParsedAlt = new AAlt(new TId("terminal"), elems, aAltTransform);
//    String terminal_altName = "ATerminal" + ResolveIds.name(name);

    listOfAltsXelem.add(aParsedAlt);

    //create the second AltTransform node
    prodTransformElems = (AElem[]) nodeProdTransform.toArray(new AElem[0]);

    final LinkedList listTerms_second = new LinkedList();

    for(int i = 0; i < prodTransformElems.length; i++)
    {
      prodTransformElems[i].apply(new AnalysisAdapter()
                                  {
                                    @Override
                                    public void caseAElem(AElem node)
                                    {
                                      String tmpNodeName = ( (node.getElemName() == null) ? node.getId().getText() :
                                                             node.getElemName().getText() );

                                      LinkedList listAListTerm_second = new LinkedList();

                                      listAListTerm_second.add(new ASimpleListTerm(null, new TId(name),
                                                               new TId(tmpNodeName)) );

                                      if(elemTypeName.startsWith("T"))
                                      {
                                        listAListTerm_second.add(new ASimpleListTerm(new ATokenSpecifier(),
                                                                 new TId(rname), null ));
                                      }
                                      else
                                      {
                                        listAListTerm_second.add(new ASimpleListTerm(new AProductionSpecifier(),
                                                                 new TId(rname),
                                                                 new TId(tmpNodeName) ) );
                                      }
                                      listTerms_second.add(new AListTerm(new TLBkt(), listAListTerm_second));
                                    }
                                  }
                                 );
    }

    aAltTransform = new AAltTransform(new TLBrace(), listTerms_second, new TRBrace());

    //creation of the second list of elems of an alternative :: two elems
    elems = new LinkedList();

    //first elem
    AElem aElemSecondTobeAdded = new AElem(null, new AProductionSpecifier(), new TId(name), null);
    elems.add(aElemSecondTobeAdded);

    //second elem
    if(elemTypeName.startsWith("T"))
    {
      aElemSecondTobeAdded = new AElem(null, new ATokenSpecifier(), new TId(rname), null);
    }
    else
    {
      aElemSecondTobeAdded = new AElem(null, new AProductionSpecifier(),  new TId(rname), null);
    }
    elems.add(aElemSecondTobeAdded);

    aParsedAlt = new AAlt(new TId("non_terminal"), elems, aAltTransform);

//    String nonTerminal_altName = "ANonTerminal" + ResolveIds.name(name);

    listOfAltsXelem.add(aParsedAlt);

    AProd prodToReturn = new AProd(new TId(name), new TArrow(), nodeProdTransform, listOfAltsXelem);
    prodToReturn.apply(ids);
    prodToReturn.apply(transformIds.getProdTransformIds());

    return prodToReturn;
  }

  private List cloneList(List list)
  {
    List clone = new LinkedList();

    for(Iterator i = list.iterator(); i.hasNext();)
    {
      clone.add(((Node) i.next()).clone());
    }
    return clone;
  }
/*
  private String xproductionType(String name)
  {
    return "P$" + name.substring(1).toLowerCase();
  }
*/
}
