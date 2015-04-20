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

@SuppressWarnings({"rawtypes","unchecked"})
public class In_Production
{
  private String name;
  private String[] prodTransformElems;
  private int nbAlts;
  private In_Alternative[] alternatives;

  public In_Production(AProd prod)
  {
    setName(prod.getId().getText());

    AElem[] prodTransforms = (AElem [])prod.getProdTransform().toArray(new AElem[0]);
    prodTransformElems = new String[prodTransforms.length];

    for(int i=0; i<prodTransforms.length; i++)
    {
      if(prodTransforms[i].getElemName() != null)
      {
        prodTransformElems[i] = prodTransforms[i].getElemName().getText();
      }
      else
      {
        prodTransformElems[i] = prodTransforms[i].getId().getText();
      }
    }

    if(prodTransforms.length == 0)
    {
      prodTransformElems = new String[1];
      prodTransformElems[0] = new String("  ");
    }

    AAlt[] alts = (AAlt[])prod.getAlts().toArray(new AAlt[0]);
    alternatives = new In_Alternative[alts.length];

    for(int i=0; i<alts.length; i++)
    {
      addAlternative(i, new In_Alternative(alts[i], prodTransformElems, name));
    }

    nbAlts = alts.length;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }

  public int getNbAlts()
  {
    return nbAlts;
  }

  public void addAlternative(int position, In_Alternative alt)
  {
    alternatives[position] = alt;
  }

  public In_Alternative getAlternative(int position)
  {
    return alternatives[position];
  }

  class In_Alternative
  {
    String name;
    int nbElems;
    AElem[] elements;
    Map prodTransform_altTransform;

    In_Alternative(AAlt alt, String[] prodTransformElems, String prodName)
    {
      setName(alt.getAltName() != null ? alt.getAltName().getText() : "");

      elements = new AElem[alt.getElems().size()];
      AElem[] listOfElems = (AElem[]) alt.getElems().toArray(new AElem[0]);

      final String newElemName = (name.equals("") ? prodName : prodName + "#" + name );

      for(int i=0; i<listOfElems.length; i++)
      {
        AElem tmpElem = (AElem)listOfElems[i].clone();

        if(tmpElem.getElemName() != null)
        {
          tmpElem.setElemName(new TId(newElemName + "#" + tmpElem.getElemName().getText() ) );
        }
        else
        {
          tmpElem.setElemName(new TId(newElemName + "#" + tmpElem.getId().getText() ));
        }

        addElem(i, tmpElem );
      }

      nbElems = listOfElems.length;

      prodTransform_altTransform =
        new TypedHashMap(prodTransformElems.length,
                         StringCast.instance,
                         NodeCast.instance);

      LinkedList list = ((AAltTransform)alt.getAltTransform()).getTerms();
      for(int i=0; i<list.size(); i++)
      {
        PTerm tmpTerm = (PTerm)list.get(i);
        tmpTerm.apply(new DepthFirstAdapter()
                      {
                        @Override
                        public void caseASimpleListTerm(ASimpleListTerm node)
                        {
                          node.setId( new TId(newElemName + "#" + node.getId().getText(), node.getId().getLine(), node.getId().getPos()) );
                        }

                        @Override
                        public void caseASimpleTerm(ASimpleTerm node)
                        {
                          node.setId( new TId(newElemName + "#" + node.getId().getText(), node.getId().getLine(), node.getId().getPos()) );
                        }

                      }
                     );
        prodTransform_altTransform.put(prodTransformElems[i], tmpTerm);
      }
    }

    void setName(String name)
    {
      this.name = name;
    }

    String getName()
    {
      return name;
    }

    int getNbElems()
    {
      return nbElems;
    }

    Map getProdTransform_AlTransformMap()
    {
      return prodTransform_altTransform;
    }

    void addElem(int position, AElem elem)
    {
      elements[position] = elem;
    }

    AElem[] getElems()
    {
      return elements;
    }
  }
}
