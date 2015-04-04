/*
 * @(#)JeksCodec.java   08/26/99
 *
 * Copyright (c) 1998-2001 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Visit eTeks web site for up-to-date versions of this file and other
 * Java tools and tutorials : http://www.eteks.com/
 */
package com.eteks.jeks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.TableModel;

import com.eteks.parser.CompilationException;
import com.eteks.parser.CompiledExpression;
import com.eteks.parser.CompiledFunction;
import com.eteks.parser.ExpressionParameter;
import com.eteks.parser.ExpressionParser;

/**
 * Encoder / decoder of streams at Jeks format.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 */
public class JeksCodec
{
  private static final String JEKS_1_0_HEADER = "@#JEKS10#@";
  private static final String JEKS_1_0_FOOTER = "@#END#@";

  private static final Locale SAVED_LOCALE   = new Locale ("", "");

  private static final String STRING_CLASS_SHORT_CUT  = "S";
  private static final String DOUBLE_CLASS_SHORT_CUT  = "N";
  private static final String LONG_CLASS_SHORT_CUT    = "L";
  private static final String DATE_CLASS_SHORT_CUT    = "D";
  private static final String BOOLEAN_CLASS_SHORT_CUT = "B";

  private final JeksFunctionSyntax   savedFunctionSyntax   = new JeksFunctionSyntax (SAVED_LOCALE);
  private final JeksExpressionSyntax savedExpressionSyntax = new JeksExpressionSyntax (SAVED_LOCALE);

  // Inner class to read portable files
  private class PortableReader extends BufferedReader
  {
    public PortableReader (BufferedReader reader)
    {
      super (reader);
    }

    public String readLine () throws IOException
    {
      String s = super.readLine ();
      if (s.indexOf ('\\') >= 0)
      {
        StringBuffer buffer = new StringBuffer (s.length ());
        for (int i = 0; i < s.length (); )
        {
          char c = s.charAt (i++);
          if (c == '\\')
            switch (s.charAt (i++))
            {
              case '\\' : buffer.append ('\\');
                          break;
              case 't'  : buffer.append ('\t');
                          break;
              case 'n'  : buffer.append ('\n');
                          break;
              case 'r'  : buffer.append ('\r');
                          break;
              case 'f'  : buffer.append ('\f');
                          break;
              case 'u'  : buffer.append ((char)Integer.parseInt (s.substring (i, i += 4), 16));
                          break;
            }
          else
            buffer.append (c);
        }
        s = buffer.toString ();
      }
      return s;
    }
  }

  // Inner class to write portable files
  private class PortableWriter extends BufferedWriter
  {
    public PortableWriter (BufferedWriter writer)
    {
      super (writer);
    }

    public void writeLine (String s) throws IOException
    {
      for (int i = 0; i < s.length (); i++)
      {
        char c = s.charAt (i);
        switch (c)
        {
          case '\\' : super.write ("\\\\");
                      break;
          case '\t' : super.write ("\\t");
                      break;
          case '\n' : super.write ("\\n");
                      break;
          case '\r' : super.write ("\\r");
                      break;
          case '\f' : super.write ("\\f");
                      break;
          default :
            if ((c < 0x0020) || (c > 0x007E))
            {
              String hexChar = Integer.toHexString (c);
              super.write ("\\u");
              super.write (Integer.toHexString ((c >> 12) & 0xF));
              super.write (Integer.toHexString ((c >>  8) & 0xF));
              super.write (Integer.toHexString ((c >>  4) & 0xF));
              super.write (Integer.toHexString ( c        & 0xF));
            }
            else
              write (c);
        }
      }

      newLine ();
    }
  }

  /*
  public boolean isInputSupported (InputStream inStream) throws IOException
  {
    return new BufferedReader (new InputStreamReader (inStream)).readLine ().equals (JEKS_1_0_HEADER);
  }
  */

  /**
   * Returns a new instance of <code>JeksTable</code> filled with the functions and
   * the cell values read from the stream <code>inStream</code> at Jeks format.
   */
  public JTable decode (InputStream inStream) throws IOException
  {
    BufferedReader reader = new PortableReader (new BufferedReader (new InputStreamReader (inStream)));

    // Create model and add cells listener only at the end to avoid useless updates
    TableModel tableModel = new JeksTableModel (500, 200);
    Vector     cellSet    = new Vector ();
    JeksExpressionParser expressionParser = new JeksExpressionParser (tableModel);

    JeksFunctionSyntax   functionSyntax = (JeksFunctionSyntax)expressionParser.getFunctionParser ().getSyntax ();
    JeksExpressionSyntax expressionSyntax = (JeksExpressionSyntax)expressionParser.getSyntax ();

    JeksFunctionParser   savedFunctionParser   = new JeksFunctionParser (savedFunctionSyntax);
    JeksExpressionParser savedExpressionParser = new JeksExpressionParser (savedExpressionSyntax, new JeksParameter (savedExpressionSyntax, null, null), null,
                                                                           savedFunctionParser, null);
    TranslationExpressionParser translationExpressionParser = new TranslationExpressionParser (savedExpressionSyntax, savedExpressionParser.getExpressionParameter ());
    TranslationExpressionParser translationFunctionParser   = new TranslationExpressionParser (savedFunctionSyntax, null);

    if (!reader.readLine ().equals (JEKS_1_0_HEADER))
      throw new IOException (functionSyntax.getMessage (JeksFunctionSyntax.MESSAGE_INVALID_JEKS_FILE));
    String     line;
    // Read first user defined functions
    while (   (line = reader.readLine ()) != null
           && line.length () > 0)
      try
      {
        if (!SAVED_LOCALE.equals (functionSyntax.getLocale ()))
        {
          // The first compilation ensures that the read function is correctly written
          CompiledFunction compiledFunction = savedFunctionParser.compileFunction (line);
          savedExpressionParser.addUserFunction (compiledFunction);
          // Translate the function
          line = translateFunctionToSyntax (translationFunctionParser,
                                            compiledFunction, functionSyntax);
        }
        CompiledFunction compiledFunction = expressionParser.getFunctionParser ().compileFunction (line);
        expressionParser.addUserFunction (compiledFunction);
      }
      catch (CompilationException ex)
      {
        // Shouldn't happen
        throw new IOException (functionSyntax.getMessage (JeksFunctionSyntax.MESSAGE_INVALID_JEKS_FILE));
      }

    // Read the cell values
    while (   (line = reader.readLine ()) != null
           && !line.equals (JEKS_1_0_FOOTER))
      try
      {
        // Read the content of the cells
        int firstSpaceIndex  = line.indexOf (" ");
        if (firstSpaceIndex == -1)
          throw new IOException (functionSyntax.getMessage (JeksExpressionSyntax.MESSAGE_INVALID_JEKS_FILE));
        JeksCell cell;
        String   value;
        try
        {
          cell = savedExpressionSyntax.getCellAt (line.substring (0, firstSpaceIndex));
          value = line.substring (firstSpaceIndex + 1);
        }
        catch (RuntimeException e)
        {
          throw new IOException (functionSyntax.getMessage (JeksExpressionSyntax.MESSAGE_INVALID_JEKS_FILE));
        }

        if (value.startsWith (savedExpressionParser.getSyntax ().getAssignmentOperator ()))
        {
          JeksExpression computedCell = null;
          cellSet.addElement (cell);
          try
          {
            if (!SAVED_LOCALE.equals (expressionSyntax.getLocale ()))
            {
              // The first compilation ensures that the read expression is correctly written
              computedCell = (JeksExpression)savedExpressionParser.compileExpression (value);
              value = translateExpressionToSyntax (translationExpressionParser,
                                                   computedCell, expressionSyntax);
            }
            computedCell = (JeksExpression)expressionParser.compileExpression (value);
            computedCell.checkCircularity (tableModel, cell);
          }
          catch (CompilationException ex)
          {
            // Shouldn't happen
            throw new IOException (functionSyntax.getMessage (JeksExpressionSyntax.MESSAGE_INVALID_JEKS_FILE));
          }
          catch (CircularityException ex)
          {
            // Required to avoid infinite loops
            computedCell.invalidateValue (ex);
          }
          catch (IndexOutOfBoundsException ex)
          { }

          tableModel.setValueAt (computedCell, cell.getRow (), cell.getColumn ());
        }
        else
        {
          int spaceIndex = value.indexOf (" ");
          if (spaceIndex == -1)
            throw new IOException (functionSyntax.getMessage (JeksExpressionSyntax.MESSAGE_INVALID_JEKS_FILE));
          try
          {
            // Read the class name of the value and the value
            String  valueClassName = value.substring (0, spaceIndex);
            Class   valueClass;
            if (valueClassName.equals (STRING_CLASS_SHORT_CUT))
              valueClass = String.class;
            else if (valueClassName.equals (DOUBLE_CLASS_SHORT_CUT))
              valueClass = Double.class;
            else if (valueClassName.equals (LONG_CLASS_SHORT_CUT))
              valueClass = Long.class;
            else if (valueClassName.equals (DATE_CLASS_SHORT_CUT))
              valueClass = Date.class;
            else if (valueClassName.equals (BOOLEAN_CLASS_SHORT_CUT))
              valueClass = Boolean.class;
            else
              valueClass = Class.forName (value.substring (0, spaceIndex));
            Object cellValue  = valueClass.getConstructor (new Class [] {String.class})
                                              .newInstance (new String [] {value.substring (spaceIndex + 1)});
            tableModel.setValueAt (cellValue, cell.getRow (), cell.getColumn ());
          }
          catch (Exception e)
          {
            throw new IOException (functionSyntax.getMessage (JeksExpressionSyntax.MESSAGE_INVALID_JEKS_FILE));
          }
        }
      }
      catch (IndexOutOfBoundsException ex)
      { } // Should warn ?

    JeksTable  table = new JeksTable (tableModel, expressionParser, true);
    table.getReferringCellsListener ().tableUpdated (tableModel, cellSet);

    return table;
  }

  /**
   * Writes to <code>outStream</code> the content of <code>table</code> at Jeks format.
   */
  public void encode (OutputStream outStream,
                      JTable       table) throws IOException
  {
    PortableWriter writer = new PortableWriter (new BufferedWriter (new OutputStreamWriter (outStream)));

    TableModel           tableModel = table.getModel ();
    JeksExpressionParser expressionParser = null;
    TranslationExpressionParser translationFunctionParser   = null;
    TranslationExpressionParser translationExpressionParser = null;

    writer.writeLine (JEKS_1_0_HEADER);

    if (table instanceof JeksTable)
    {
      expressionParser = ((JeksTable)table).getExpressionParser ();
      translationFunctionParser   = new TranslationExpressionParser ((JeksFunctionSyntax)expressionParser.getFunctionParser ().getSyntax (), null);
      translationExpressionParser = new TranslationExpressionParser ((JeksExpressionSyntax)expressionParser.getSyntax (),
                                                                     expressionParser.getExpressionParameter ());
      // Write first user defined functions
      Vector userFunctions = expressionParser.getUserFunctions ();
      for (int i = 0; i < userFunctions.size (); i++)
        if (SAVED_LOCALE.equals (((JeksFunctionSyntax)expressionParser.getFunctionParser ().getSyntax ()).getLocale ()))
          writer.writeLine (((CompiledFunction)userFunctions.elementAt (i)).getDefinition ());
        else
          writer.writeLine (translateFunctionToSyntax (translationFunctionParser, (CompiledFunction)userFunctions.elementAt (i),
                                                       savedFunctionSyntax));
    }

    writer.newLine ();
    // Write the content of the filled cells
    for (int row = 0; row < tableModel.getRowCount (); row++)
      for (int column = 0; column < tableModel.getColumnCount (); column++)
      {
        Object value = tableModel.getValueAt (row, column);
        StringBuffer buffer = new StringBuffer ();
        if (value != null)
        {
          buffer.append (savedExpressionSyntax.getColumnName (column));
          buffer.append (savedExpressionSyntax.getRowName (row));
          buffer.append (" ");
          if (value instanceof JeksExpression)
          {
            if (   expressionParser == null
                || SAVED_LOCALE.equals (((JeksExpressionSyntax)expressionParser.getSyntax ()).getLocale ()))
              buffer.append (((JeksExpression)value).getDefinition ());
            else
              buffer.append (translateExpressionToSyntax (translationExpressionParser,
                                                         (JeksExpression)value, savedExpressionSyntax));
          }
          else
          {
            // Write the class of the value and the value
            Class valueClass = value.getClass ();
            if (valueClass.equals (String.class))
              buffer.append (STRING_CLASS_SHORT_CUT);
            else if (valueClass.equals (Double.class))
              buffer.append (DOUBLE_CLASS_SHORT_CUT);
            else if (valueClass.equals (Long.class))
              buffer.append (LONG_CLASS_SHORT_CUT);
            else if (valueClass.equals (Date.class))
              buffer.append (DATE_CLASS_SHORT_CUT);
            else if (valueClass.equals (Boolean.class))
              buffer.append (BOOLEAN_CLASS_SHORT_CUT);
            else
              buffer.append (valueClass.getName ());
            buffer.append (" ");
            buffer.append (String.valueOf (value));
          }
          writer.writeLine (buffer.toString ());
        }
      }
    writer.writeLine (JEKS_1_0_FOOTER);
    writer.flush ();
  }

  /**
   * Translates the definition of <code>expression</code>, using the syntax
   * <code>translationSyntax</code>.
   */
  private String translateExpressionToSyntax (TranslationExpressionParser translationExpressionParser,
                                              CompiledExpression          expression,
                                              JeksExpressionSyntax        translationSyntax)
  {
    return translationExpressionParser
             .translateExpressionDefinitionToSyntax (expression.getDefinition (),
                                                     expression.getParameters (),
                                                     translationSyntax);
  }

  private String translateFunctionToSyntax (TranslationExpressionParser translationFunctionParser,
                                            CompiledFunction            function,
                                            JeksFunctionSyntax          translationSyntax)
  {
    String             functionDefinition = function.getDefinition ();
    JeksFunctionSyntax fromSyntax = (JeksFunctionSyntax)translationFunctionParser.getSyntax ();
    // Translate brackets and parameters separator
    String translatedFunctionStart = functionDefinition.substring (0, functionDefinition.indexOf (fromSyntax.getAssignmentOperator ()));
    translatedFunctionStart = translatedFunctionStart.replace (fromSyntax.getOpeningBracket (), translationSyntax.getOpeningBracket ());
    translatedFunctionStart = translatedFunctionStart.replace (fromSyntax.getClosingBracket (), translationSyntax.getClosingBracket ());
    translatedFunctionStart = translatedFunctionStart.replace (fromSyntax.getParameterSeparator (), translationSyntax.getParameterSeparator ());
    // Translate delimiters unknown in the translation syntax
    String whiteSpaces = fromSyntax.getWhiteSpaceCharacters ();
    for (int i = 0; i < whiteSpaces.length (); i++)
      if (translationSyntax.getWhiteSpaceCharacters ().indexOf (whiteSpaces.charAt (i)) == -1)
        translatedFunctionStart = translatedFunctionStart.replace (whiteSpaces.charAt (i), translationSyntax.getWhiteSpaceCharacters ().charAt (0));

    String expressionDefinition = functionDefinition.substring (functionDefinition.indexOf (fromSyntax.getAssignmentOperator ()));
    String translatedExpression = translationFunctionParser
                                    .translateExpressionDefinitionToSyntax (expressionDefinition, function,
                                                                            translationSyntax);
    return translatedFunctionStart + translatedExpression;
  }

  private static class TranslationExpressionParser extends ExpressionParser
  {
    public TranslationExpressionParser (JeksFunctionSyntax  syntax,
                                        ExpressionParameter expressionParameter)
    {
      super (syntax, expressionParameter);
    }

    public String translateExpressionDefinitionToSyntax (String             expressionDefinition,
                                                         Object             parserData,
                                                         JeksFunctionSyntax toSyntax)
    {
      JeksFunctionSyntax fromSyntax = (JeksFunctionSyntax)getSyntax ();
      // Reuse getLexical () to parse expression and find which cells to change
      StringBuffer translatedExpression = new StringBuffer (toSyntax.getAssignmentOperator ());
      Lexical      lexical = null;
      for (int parserIndex = fromSyntax.getAssignmentOperator ().length ();
           parserIndex < expressionDefinition.length ();
           parserIndex += lexical.getExtractedString ().length ())
      {
        Object key;
        String newLexical = null;
        try
        {
          lexical = getLexical (expressionDefinition, parserIndex, parserData);
          String extractedString = lexical.getExtractedString ();
          switch (lexical.getCode ())
          {
            case LEXICAL_WHITE_SPACE :
              // Translate white spaces
              newLexical = "";
              for (int i = 0; i < extractedString.length (); i++)
                if (toSyntax.getWhiteSpaceCharacters ().indexOf (extractedString.charAt (i)) == -1)
                  newLexical += toSyntax.getWhiteSpaceCharacters ().charAt (0);
                else
                  newLexical += extractedString.charAt (i);
               break;
            case LEXICAL_LITERAL :
              if (extractedString.charAt (0) == fromSyntax.getQuoteCharacter ())
              {
                // Litteral string values
                newLexical = extractedString;
                // Replace quote char and escape characters if different
                if (fromSyntax.getQuoteCharacter () != toSyntax.getQuoteCharacter ())
                  newLexical = newLexical.replace (fromSyntax.getQuoteCharacter (), toSyntax.getQuoteCharacter ());
              }
              else
              {
                // Literal numeric values
                Number number = (Number)fromSyntax.getLiteral (extractedString, new StringBuffer ());
                newLexical = toSyntax.getNumberFormat ().format (number);
              }
              break;
            case LEXICAL_CONSTANT :
              newLexical = toSyntax.getConstant (fromSyntax.getConstantKey (extractedString));
              break;
            case LEXICAL_PARAMETER :
                // Change parameter's constant char and cell set separator
                if (parserData instanceof Hashtable)
                {
                  Hashtable parameters = (Hashtable)parserData;
                  for (Enumeration enu = parameters.keys (); enu.hasMoreElements (); )
                    if (compare ((String)enu.nextElement (), extractedString, fromSyntax.isCaseSensitive ()))
                    {
                      newLexical = extractedString;
                      if (fromSyntax instanceof JeksExpressionSyntax)
                      {
                        JeksExpressionSyntax fromExpressionSyntax = (JeksExpressionSyntax)fromSyntax;
                        JeksExpressionSyntax toExpressionSyntax   = (JeksExpressionSyntax)toSyntax;
                        if (compare (fromExpressionSyntax.getCellError (JeksExpressionSyntax.ERROR_ILLEGAL_CELL),
                                     extractedString, fromSyntax.isCaseSensitive ()))
                          newLexical = toExpressionSyntax.getCellError (JeksExpressionSyntax.ERROR_ILLEGAL_CELL);
                        else
                        {
                          int  separatorIndex = newLexical.indexOf (fromExpressionSyntax.getCellSetSeparator ());
                          if (separatorIndex == -1)
                            newLexical = translateCellToSyntax (newLexical, toExpressionSyntax);
                          else
                            newLexical =   translateCellToSyntax (newLexical.substring (0, separatorIndex), toExpressionSyntax)
                                         + toExpressionSyntax.getCellSetSeparator ()
                                         + translateCellToSyntax (newLexical.substring (separatorIndex + 1), toExpressionSyntax);
                        }
                      }
                      break;
                    }
                }
                else if (parserData instanceof CompiledFunction)
                {
                  String [] parameters = ((CompiledFunction)parserData).getParameters ();
                  for (int i = 0; i < parameters.length; i++)
                    if (compare (parameters [i], extractedString, fromSyntax.isCaseSensitive ()))
                    {
                      newLexical = extractedString;
                      break;
                    }
                }
                else
                  newLexical = extractedString;
              break;
            case LEXICAL_SYNONYMOUS_OPERATOR :
              // Consider that synonimous operators are each time the same in each syntax
            case LEXICAL_UNARY_OPERATOR :
              newLexical = toSyntax.getUnaryOperator (fromSyntax.getUnaryOperatorKey (extractedString));
              break;
            case LEXICAL_BINARY_OPERATOR :
              newLexical = toSyntax.getBinaryOperator (fromSyntax.getBinaryOperatorKey (extractedString));
              break;
            case LEXICAL_COMMON_FUNCTION :
              newLexical = toSyntax.getCommonFunction (fromSyntax.getCommonFunctionKey (extractedString));
              break;
            case LEXICAL_OPENING_BRACKET :
              newLexical = extractedString;
              if (fromSyntax.getOpeningBracket () != toSyntax.getOpeningBracket ())
                newLexical = String.valueOf (toSyntax.getOpeningBracket ());
              break;
            case LEXICAL_CLOSING_BRACKET :
              newLexical = extractedString;
              if (fromSyntax.getClosingBracket () != toSyntax.getClosingBracket ())
                newLexical = String.valueOf (toSyntax.getClosingBracket ());
              break;
            case LEXICAL_FUNCTION :
              key = fromSyntax.getJeksFunctionKey (extractedString);
              // If the key exist it's a Jeks function otherwise it's a user defined function
              if (key != null)
                newLexical = toSyntax.getJeksFunction (key);
              else
                newLexical = extractedString;
              break;
            case LEXICAL_PARAMETER_SEPARATOR :
              newLexical = extractedString;
              if (fromSyntax.getParameterSeparator () != toSyntax.getParameterSeparator ())
                newLexical = String.valueOf (toSyntax.getParameterSeparator ());
              break;
            case LEXICAL_IF :
            case LEXICAL_THEN :
            case LEXICAL_ELSE :
              newLexical = toSyntax.getConditionPart (fromSyntax.getConditionPartKey (extractedString));
              break;
          }
        }
        catch (CompilationException e)
        { } // Can't happen : expression already correctly parsed

        if (newLexical != null)
          translatedExpression.append (newLexical);
        else
          // Otherwise append the read substring (Shouldn't happen if toSyntax has defined the same keys)
          translatedExpression.append (expressionDefinition.substring (parserIndex, parserIndex));
      }

      return translatedExpression.toString ();
    }

    /**
     * Translates the cell <code>cellIdentifier</code>, to the syntax <code>toSyntax</code>.
     */
    private String translateCellToSyntax (String                cellIdentifier,
                                          JeksExpressionSyntax  toSyntax)
    {
      JeksExpressionSyntax fromSyntax = (JeksExpressionSyntax)getSyntax ();
      JeksCell cell = fromSyntax.getCellAt (cellIdentifier);
      return toSyntax.toString (cell.getRow (),    cellIdentifier.indexOf (fromSyntax.getConstantChar (), 1) != -1,
                                cell.getColumn (), cellIdentifier.charAt (0) == fromSyntax.getConstantChar ());
    }
  }
}
