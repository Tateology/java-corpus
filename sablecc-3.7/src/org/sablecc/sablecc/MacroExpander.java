/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * This file is part of SableCC.                             *
 * See the file "LICENSE" for copyright information and the  *
 * terms and conditions for copying, distribution and        *
 * modification of SableCC.                                  *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package org.sablecc.sablecc;

import java.io.*;
import java.util.*;

@SuppressWarnings({"rawtypes","unchecked"})
public class MacroExpander
{
  private static final String MACRO = "Macro:";
//  private static final String lineSeparator = System.getProperty("line.separator");

  private Map macros = new TypedTreeMap(
                         StringComparator.instance,
                         StringCast.instance,
                         ListCast.instance);

  public MacroExpander(Reader in) throws IOException
  {
    BufferedReader br = new BufferedReader(in);
    while(readInMacro(br))
      ;
    in.close();
  }

  private boolean readInMacro(BufferedReader in) throws IOException
  {
    String line;
    while((line = in.readLine()) != null)
    {
      if(line.startsWith(MACRO))
      {
        String name = line.substring(MACRO.length());
        List macro = new TypedLinkedList(StringCast.instance);

        while((line = in.readLine()) != null)
        {
          if(line.equals("$"))
          {
            macros.put(name, macro);
            return true;
          }

          macro.add(line);
        }

        macros.put(name, macro);
        return false;
      }
    }

    return false;
  }

  @Override
  public String toString()
  {
    return this.getClass().getName() + macros;
  }

  public void apply(BufferedWriter out, String macroName) throws IOException
  {
    apply(out, macroName, null);
  }

  public void apply(BufferedWriter out, String macroName, String[] arguments) throws IOException
  {
    List macro = (List) macros.get(macroName);

    for(ListIterator li = macro.listIterator(); li.hasNext();)
    {
      if(li.nextIndex() != 0)
      {
        out.newLine();
      }

      String line = (String) li.next();
      char c;

      for(int i = 0; i < line.length(); i++)
      {
        if((c = line.charAt(i)) == '$')
        {
          StringBuffer index = new StringBuffer();

          while((c = line.charAt(++i)) != '$')
          {
            index.append(c);
          }

          if(index.length() == 0)
          {
            out.write('$');
          }
          else
          {
            out.write(arguments[Integer.parseInt(index.toString())]);
          }
        }
        else
        {
          out.write(c);
        }
      }
    }
  }
}
