/*
 * @(#)JeksExpressionParser.java   08/26/99
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

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.table.TableModel;

import com.eteks.parser.CompilationException;
import com.eteks.parser.CompiledExpression;
import com.eteks.parser.ExpressionParameter;
import com.eteks.parser.ExpressionParser;
import com.eteks.parser.Function;
import com.eteks.parser.Interpreter;
import com.eteks.parser.Syntax;

/**
 * Parser for expressions entered in table cells. This parser stores the interpreter used
 * to compute expressions at runtime and the functions
 * entered by user and available for expressions.<br>
 * This class provides also methods used to shift cells in expressions.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 */
public class JeksExpressionParser extends ExpressionParser
{
  private NumberFormat       numberParser;
  private DateFormat         dateParser;
  private DateFormat         timeParser;
  private DateFormat         dateTimeParser;

  private JeksFunctionParser functionParser;
  private Interpreter        interpreter;
  private Vector             userFunctions = new Vector ();

  /**
   * Creates an expression parser from the table model <code>tableModel</code>,
   * using instances of <code>JeksExpressionSyntax</code> and of <code>JeksFunctionSyntax</code>
   * for expression and function syntaxes, an instance of <code>JeksInterpreter</code> as interpreter
   * and an instance of <code>JeksParameter</code> as expression parameter.
   */
  public JeksExpressionParser (TableModel tableModel)
  {
    this (tableModel, new JeksExpressionSyntax (), new JeksInterpreter ());
  }

  private JeksExpressionParser (TableModel           tableModel,
                                JeksExpressionSyntax syntax,
                                Interpreter          interpreter)
  {
    this (syntax, new JeksParameter (syntax, interpreter, tableModel), interpreter,
          new JeksFunctionParser (), null);
  }

  /**
   * Creates an expression parser with no user function parser.
   */
  public JeksExpressionParser (JeksExpressionSyntax syntax,
                               ExpressionParameter  expressionParameter,
                               Interpreter          interpreter)
  {
    this (syntax, expressionParameter, interpreter, null, null);
  }

  /**
   * Creates an expression parser.
   */
  public JeksExpressionParser (JeksExpressionSyntax syntax,
                               ExpressionParameter  expressionParameter,
                               Interpreter          interpreter,
                               JeksFunctionParser   functionParser,
                               Vector               userFunctions)
  {
    super (syntax, expressionParameter);
    this.functionParser = functionParser;
    this.interpreter    = interpreter;
    if (userFunctions != null)
    {
      for (int i = 0; i < userFunctions.size (); i++)
        addUserFunction ((Function)userFunctions.elementAt (i));
    }
    numberParser   = NumberFormat.getInstance (syntax.getLocale ());
    dateParser     = DateFormat.getDateInstance (DateFormat.SHORT, syntax.getLocale ());
    timeParser     = DateFormat.getTimeInstance (DateFormat.SHORT, syntax.getLocale ());
    dateTimeParser = DateFormat.getDateTimeInstance (DateFormat.SHORT, DateFormat.SHORT, syntax.getLocale ());
  }

  /**
   * Parses and compiles the expression definition <code>expressionDefinition</code>
   * and returns an instance of <code>JeksExpression</code>.
   */
  public CompiledExpression compileExpression (String expressionDefinition) throws CompilationException
  {
    CompiledExpression expression = super.compileExpression (expressionDefinition);
    return new JeksExpression (expression);
  }

  /**
   * Returns the interpreter of this parser.
   */
  public Interpreter getInterpreter ()
  {
    return interpreter;
  }

  /**
   * Returns the parser used to parse user functions.
   */
  public JeksFunctionParser getFunctionParser ()
  {
    return functionParser;
  }

  /**
   * Returns a copy of the user functions set.
   */
  public Vector getUserFunctions ()
  {
    return (Vector)userFunctions.clone ();
  }

  /**
   * Add the user function <code>userFunction</code> to the expression
   * and function syntaxes of this parser.
   */
  public void addUserFunction (Function userFunction)
  {
    userFunctions.addElement (userFunction);
    ((JeksExpressionSyntax)getSyntax ()).addFunction (userFunction);
    if (functionParser != null)
      ((JeksFunctionSyntax)functionParser.getSyntax ()).addFunction (userFunction);
  }

  /**
   * Remove the user function <code>userFunction</code> from the expression
   * and function syntaxes of this parser.
   */
  public void removeUserFunction (Function userFunction)
  {
    userFunctions.removeElement (userFunction);
    ((JeksExpressionSyntax)getSyntax ()).removeFunction (userFunction);
    if (functionParser != null)
      ((JeksFunctionSyntax)functionParser.getSyntax ()).removeFunction (userFunction);
  }

  /**
   * Remove all the user functions from the expression
   * and function syntaxes of this parser.
   */
  public void removeAllUserFunctions ()
  {
    for (int i = 0; i < userFunctions.size (); i++)
    {
      Function userFunction = (Function)userFunctions.elementAt (i);
      ((JeksExpressionSyntax)getSyntax ()).removeFunction (userFunction);
      if (functionParser != null)
        ((JeksFunctionSyntax)functionParser.getSyntax ()).removeFunction (userFunction);
    }
    userFunctions.removeAllElements ();
  }

  /**
   * Returns <code>value</code> as an editable object. If <code>value</code> is an
   * instance of <code>JeksExpression</code> the definition of the expression is returned,
   * if <code>value</code> is a number or a date its localized string representation
   * is returned, otherwise the object <code>value</code> itself is returned.
   * @param value a value stored in the table model.
   */
  public Object getEditedValue (Object value)
  {
    if (value instanceof JeksExpression)
      return ((JeksExpression)value).getDefinition ();
    else if (value instanceof Number)
      return numberParser.format (value);
    else if (value instanceof Date)
    {
      Calendar calendar = Calendar.getInstance ();
      calendar.setTime ((Date)value);
      if (   calendar.get (Calendar.HOUR_OF_DAY) == 0
          && calendar.get (Calendar.MINUTE) == 0
          && calendar.get (Calendar.SECOND) == 0)
        return dateParser.format (value);
      else
        return dateTimeParser.format (value);
    }
    else if (value instanceof Boolean)
      return ((Boolean)value).booleanValue ()
                ? ((JeksExpressionSyntax)getSyntax ()).getConstant (Syntax.CONSTANT_TRUE)
                : ((JeksExpressionSyntax)getSyntax ()).getConstant (Syntax.CONSTANT_FALSE);
    else
      return value;
  }

  /**
   * Returns an object of the same wrapping class as the string
   * <code>data</code>. This method tries to create an instance
   * of one of the following classes in this order: <code>Long</code>,
   * <code>Double</code>, <code>Date</code>, <code>Boolean</code> or the string itself.
   * If the string starts with an assignment operator (equal sign =), it tries
   * to compile the expression to create a <code>JeksExpression</code> instance.
   * @param data  a string.
   * @return an instance of either classes : <code>Long</code>, <code>Double</code>,
   *         <code>Date</code>, <code>Boolean</code>, <code>String</code>
   *         or <code>JeksExpression</code>.
   */
  public Object getModelValue (String data) throws CompilationException
  {
    if (data.startsWith (getSyntax ().getAssignmentOperator ()))
      return compileExpression (data);
    else
      return parseLitteral (data);
  }

  /**
   * Returns an object of the same wrapping class as the string
   * <code>data</code>. This method tries to create an instance
   * of one of the following classes in this order: <code>Long</code>,
   * <code>Double</code>, <code>Date</code>, <code>Boolean</code>.
   * If it fails it returns the string itself.
   * @param data  a string.
   * @return an instance of either classes : <code>Long</code>, <code>Double</code>,
   *         <code>Date</code>, <code>Boolean</code> or <code>String</code>.
   */
  public Object parseLitteral (String data)
  {
    if ("".equals (data))
      return data;
    ParsePosition position = new ParsePosition (0);
    // First try to parse a number (Long or Double)
    Object value = numberParser.parse (data, position);
    if (position.getIndex () < data.length ())
    {
      // Second try to parse a date
      position.setIndex (0);
      value = dateParser.parse (data, position);
      if (position.getIndex () < data.length ())
      {
        // Third try to parse a date time
        position.setIndex (0);
        value = dateTimeParser.parse (data, position);
        if (position.getIndex () < data.length ())
        {
          // Fourth try to parse a time
          position.setIndex (0);
          value = timeParser.parse (data, position);
          if (position.getIndex () > data.length ())
          {
            // As parsed time is a time on the January 1st 1970,
            // change it to be the time of today
            Calendar parsedCalendar = Calendar.getInstance ();
            parsedCalendar.setTime ((Date)value);

            Calendar todayCalendar = Calendar.getInstance ();
            todayCalendar.set (Calendar.HOUR_OF_DAY, parsedCalendar.get (Calendar.HOUR_OF_DAY));
            todayCalendar.set (Calendar.MINUTE,      parsedCalendar.get (Calendar.MINUTE));
            todayCalendar.set (Calendar.SECOND,      parsedCalendar.get (Calendar.SECOND));

            value = todayCalendar.getTime ();
          }
          else
            // Fifth try to parse a boolean
            if (Syntax.CONSTANT_TRUE.equals (getSyntax ().getConstantKey (data)))
              value = Boolean.TRUE;
            else if (Syntax.CONSTANT_FALSE.equals (getSyntax ().getConstantKey (data)))
              value = Boolean.FALSE;
            else
              // Last return the string itself
              value = data;
        }
      }
    }
    return value;
  }

  /**
   * Shifts the cells used in the definition of <code>expression</code> of <code>rowShift</code>
   * rows and <code>columnShift</code> columns and returns the shifted definition.
   * Columns and rows prefixed by the character returned
   * by the <code>getConstantChar ()</code> method are unchanged. The string returned
   * by <code>getCellError (JeksExpressionSyntax.ERROR_ILLEGAL_CELL)</code> of expression
   * syntax replaces any shifted cell with a negative column or row.<br>
   * This method is used to shift copied expressions during a copy / paste operation.
   */
  public String shiftExpression (CompiledExpression expression, int rowShift, int columnShift)
  {
    if (      rowShift == 0
           && columnShift == 0
        || expression.getParameters ().size () == 0)
      return expression.getDefinition ();
    else
    {
      JeksExpressionSyntax syntax = (JeksExpressionSyntax)getSyntax ();
      Hashtable shiftedParameters = new Hashtable (expression.getParameters ().size ());
      for (Enumeration enu = expression.getParameters ().keys ();
           enu.hasMoreElements (); )
      {
        String parameter = (String)enu.nextElement ();
        Object cellOrSet = expression.getParameters ().get (parameter);
        String shiftedParameter = syntax.getCellError (JeksExpressionSyntax.ERROR_ILLEGAL_CELL);
        try
        {
          if (!cellOrSet.equals (IllegalCellException.class))
            if (cellOrSet instanceof JeksCell)
              shiftedParameter = shiftCell (((JeksCell)cellOrSet).getRow (), ((JeksCell)cellOrSet).getColumn (),
                                            rowShift, parameter.indexOf (syntax.getConstantChar (), 1) != -1,
                                            columnShift, parameter.charAt (0) == syntax.getConstantChar (),
                                            false);
            else // JeksCellSet
            {
              int separatorIndex = parameter.indexOf (syntax.getCellSetSeparator ());
              String cell1Identifier = parameter.substring (0, separatorIndex);
              String cell2Identifier = parameter.substring (separatorIndex + 1);
              JeksCell cell1 = syntax.getCellAt (cell1Identifier);
              JeksCell cell2 = syntax.getCellAt (cell2Identifier);
              shiftedParameter =   shiftCell (cell1.getRow (), cell1.getColumn (),
                                              rowShift, cell1Identifier.indexOf (syntax.getConstantChar (), 1) != -1,
                                              columnShift, cell1Identifier.charAt (0) == syntax.getConstantChar (),
                                              false)
                                 + syntax.getCellSetSeparator ()
                                 + shiftCell (cell2.getRow (), cell2.getColumn (),
                                              rowShift, cell2Identifier.indexOf (syntax.getConstantChar (), 1) != -1,
                                              columnShift, cell2Identifier.charAt (0) == syntax.getConstantChar (),
                                              false);
            }
        }
        catch (IllegalCellException e)
        {
          shiftedParameter = syntax.getCellError (JeksExpressionSyntax.ERROR_ILLEGAL_CELL);
        }

        shiftedParameters.put (parameter, shiftedParameter);
      }

      return replaceShiftedParameters (expression, shiftedParameters);
    }
  }

  /**
   * Shifts the cells used in the definition of <code>expression</code> of <code>rowShift</code>
   * rows and <code>columnShift</code> columns and returns the shifted expression.
   * The only cells which are shifted in the returned definition are the ones
   * belonging to the cells set defined by the opposite cells (<code>cellSetFirstRow</code>,
   * <code>cellSetFirstColumn</code>) and (<code>cellSetLastRow</code>, <code>cellSetLastColumn</code>).
   * Columns and rows are shifted even if they are prefixed by the constant character.
   * This method is used to shift cut expressions and their referring cells
   * during a cut / paste operation.
   */
  public String shiftExpression (CompiledExpression expression,
                                 int     rowShift,
                                 int     columnShift,
                                 int     cellSetFirstRow,
                                 int     cellSetFirstColumn,
                                 int     cellSetLastRow,
                                 int     cellSetLastColumn)
  {
    if (      rowShift == 0
           && columnShift == 0
        || expression.getParameters ().size () == 0)
      return expression.getDefinition ();
    else
    {
      JeksExpressionSyntax syntax = (JeksExpressionSyntax)getSyntax ();
      Hashtable shiftedParameters = new Hashtable (expression.getParameters ().size ());
      for (Enumeration enu = expression.getParameters ().keys ();
           enu.hasMoreElements (); )
      {
        String parameter = (String)enu.nextElement ();
        Object cellOrSet = expression.getParameters ().get (parameter);
        String shiftedParameter = syntax.getCellError (JeksExpressionSyntax.ERROR_ILLEGAL_CELL);
        try
        {
          if (!cellOrSet.equals (IllegalCellException.class))
            if (cellOrSet instanceof JeksCell)
            {
              JeksCell cell = (JeksCell)cellOrSet;
              if (   cell.getRow () >= cellSetFirstRow
                  && cell.getRow () <= cellSetLastRow
                  && cell.getColumn () >= cellSetFirstColumn
                  && cell.getColumn () <= cellSetLastColumn)
                shiftedParameter = shiftCell (((JeksCell)cellOrSet).getRow (), ((JeksCell)cellOrSet).getColumn (),
                                              rowShift, parameter.indexOf (syntax.getConstantChar (), 1) != -1,
                                              columnShift, parameter.charAt (0) == syntax.getConstantChar (),
                                              true);
              else
                shiftedParameter = parameter;
            }
            else // JeksCellSet
            {
              int separatorIndex = parameter.indexOf (syntax.getCellSetSeparator ());
              String cell1Identifier = parameter.substring (0, separatorIndex);
              String cell2Identifier = parameter.substring (separatorIndex + 1);
              JeksCell cell1 = syntax.getCellAt (cell1Identifier);
              JeksCell cell2 = syntax.getCellAt (cell2Identifier);
              if (   cell1.getRow () >= cellSetFirstRow
                  && cell1.getRow () <= cellSetLastRow
                  && cell1.getColumn () >= cellSetFirstColumn
                  && cell1.getColumn () <= cellSetLastColumn)
                shiftedParameter = shiftCell (cell1.getRow (), cell1.getColumn (),
                                              rowShift, cell1Identifier.indexOf (syntax.getConstantChar (), 1) != -1,
                                              columnShift, cell1Identifier.charAt (0) == syntax.getConstantChar (),
                                              true);
              else
                shiftedParameter = cell1Identifier;
              shiftedParameter += syntax.getCellSetSeparator ();
              if (   cell2.getRow () >= cellSetFirstRow
                  && cell2.getRow () <= cellSetLastRow
                  && cell2.getColumn () >= cellSetFirstColumn
                  && cell2.getColumn () <= cellSetLastColumn)
                shiftedParameter += shiftCell (cell2.getRow (), cell2.getColumn (),
                                               rowShift, cell2Identifier.indexOf (syntax.getConstantChar (), 1) != -1,
                                               columnShift, cell2Identifier.charAt (0) == syntax.getConstantChar (),
                                               true);
              else
                shiftedParameter += cell2Identifier;
            }
        }
        catch (IllegalCellException e)
        {
          shiftedParameter = syntax.getCellError (JeksExpressionSyntax.ERROR_ILLEGAL_CELL);
        }

        shiftedParameters.put (parameter, shiftedParameter);
      }

      return replaceShiftedParameters (expression, shiftedParameters);
    }
  }

  private String replaceShiftedParameters (CompiledExpression expression,
                                           Hashtable          shiftedParameters)
  {
    // Reuse getLexical () to parse expression and find which cells to change
    JeksExpressionSyntax syntax = (JeksExpressionSyntax)getSyntax ();
    StringBuffer shiftedExpression = new StringBuffer (syntax.getAssignmentOperator ());
    Lexical      lexical = null;
    for (int parserIndex = syntax.getAssignmentOperator ().length ();
         parserIndex < expression.getDefinition ().length ();
         parserIndex += lexical.getExtractedString ().length ())
    {
      String foundParameter = null;
      try
      {
        lexical = getLexical (expression.getDefinition (), parserIndex, expression.getParameters ());
        if (lexical.getCode () == LEXICAL_PARAMETER)
          for (Enumeration enu = expression.getParameters ().keys (); enu.hasMoreElements (); )
          {
            String parameter = (String)enu.nextElement ();
            if (compare (parameter, lexical.getExtractedString (), syntax.isCaseSensitive ()))
            {
              foundParameter = parameter;
              break;
            }
          }
      }
      catch (CompilationException e)
      { } // Can't happen : expression already correctly parsed

      if (foundParameter != null)
        // If found lexical is a parameter, substitute it by the new parameter
        shiftedExpression.append (shiftedParameters.get (foundParameter));
      else
        // Otherwise append the read substring
        shiftedExpression.append (lexical.getExtractedString ());
    }

    return shiftedExpression.toString ();
  }

  private String shiftCell (int row,
                            int column,
                            int rowShift,    boolean rowConstant,
                            int columnShift, boolean columnConstant,
                            boolean shiftConstantCell)
  {
    int shiftedRow    = row    + (shiftConstantCell || !rowConstant    ? rowShift    : 0);
    int shiftedColumn = column + (shiftConstantCell || !columnConstant ? columnShift : 0);
    if (   shiftedRow >= 0
        && shiftedColumn >= 0)
      // TODO : check max limits ?
      return ((JeksExpressionSyntax)getSyntax ()).toString (shiftedRow, rowConstant, shiftedColumn, columnConstant);
    else
      throw new IllegalCellException ();
  }
}
