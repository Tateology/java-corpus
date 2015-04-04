/*
 * @(#)JeksCalculator.java   03/29/01
 *
 * Copyright (c) 2000 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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
package com.eteks.tools.calculator;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;

import com.eteks.parser.CalculatorParser;
import com.eteks.parser.CompilationException;
import com.eteks.parser.Function;
import com.eteks.parser.Interpreter;
import com.eteks.parser.Syntax;

/**
 * An applet displaying a calculator.
 *
 * @version 1.0.2
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 */
public class JeksCalculator extends Applet
{
  // Calculator's syntax.
  // This syntax doesn't use any key of <code>Syntax</code>, it simply returns
  // the uppercase lexical if it's recognized by the calculator. These strings
  // are used as keys by <code>Interpreter</code> methods to perform the good
  // computation.
  private static final Syntax SYNTAX = new Syntax ()
    {
      public Object getLiteral (String       expression,
                                StringBuffer extractedString)
      {
        int index = 0;
        // A minimal number parser
        while (   index < expression.length ()
               && (  Character.isDigit (expression.charAt (index))
                   || expression.charAt (index) == '.'))
          if (   ++index < expression.length ()
              && Character.toUpperCase (expression.charAt (index)) == 'E')
            index += 2;

        if (index == 0)
          return null;
        extractedString.append (expression.substring (0, index));
        return new Double (extractedString.toString ());
      }

      public Object getConstantKey (String constant)
      {
        return null; // No constant only numbers
      }

      public Object getUnaryOperatorKey (String unaryOperator)
      {
        if (   "+".equals (unaryOperator)
            || "-".equals (unaryOperator))
          return unaryOperator;
        else
          return null;
      }

      public Object getBinaryOperatorKey (String binaryOperator)
      {
        binaryOperator = binaryOperator.toUpperCase ();
        if (   "+".equals (binaryOperator)
            || "-".equals (binaryOperator)
            || "/".equals (binaryOperator)
            || "*".equals (binaryOperator)
            || "POW".equals (binaryOperator))
          return binaryOperator;
        else
          return null;
      }

      public Object getConditionPartKey (String ternaryOperator)
      {
        return null; // No condition
      }

      public int getConditionPartCount ()
      {
        return 0;
      }

      public Object getCommonFunctionKey (String predefinedFunction)
      {
        predefinedFunction = predefinedFunction.toUpperCase ();
        if (   "LN".equals (predefinedFunction)
            || "LOG".equals (predefinedFunction)
            || "EXP".equals (predefinedFunction)
            || "SQR".equals (predefinedFunction)
            || "SQRT".equals (predefinedFunction)
            || "COS".equals (predefinedFunction)
            || "SIN".equals (predefinedFunction)
            || "TAN".equals (predefinedFunction)
            || "ARCCOS".equals (predefinedFunction)
            || "ARCSIN".equals (predefinedFunction)
            || "ARCTAN".equals (predefinedFunction)
            || "INV".equals (predefinedFunction))
          return predefinedFunction;
        else
          return null;
      }

      public Function getFunction (String userFunction)
      {
        return null; // No function
      }

      public int getBinaryOperatorPriority (Object binaryOperatorKey)
      {
        if ("+".equals (binaryOperatorKey))
          return 1;
        else if ("-".equals (binaryOperatorKey))
          return 1;
        else if ("/".equals (binaryOperatorKey))
          return 2;
        else if ("*".equals (binaryOperatorKey))
          return 2;
        else if ("POW".equals (binaryOperatorKey))
          return 3;
        else
          throw new IllegalArgumentException ();
      }

      public String getAssignmentOperator ()
      {
        return null;
      }

      public String getWhiteSpaceCharacters ()
      {
        return " \t\n\r";
      }

      public char getOpeningBracket ()
      {
        return '(';
      }

      public char getClosingBracket ()
      {
        return ')';
      }

      public char getParameterSeparator ()
      {
        return 0;
      }

      public String getDelimiters ()
      {
        return " \t\n\r-+*/().";
      }

      public boolean isCaseSensitive ()
      {
        return false;
      }

      public boolean isShortSyntax ()
      {
        // Supports common function calls without brackets
        return true;
      }

      public boolean isValidIdentifier (String identifier)
      {
        return false; // No identifier
      }
    }; // End of syntax

  // Calculator's interpreter.
  // Only the operators recognized in this class are implemented.
  private static final Interpreter INTERPRETER = new Interpreter ()
    {
      public Object getLiteralValue (Object literal)
      {
        return literal;
      }

      public Object getParameterValue (Object parameter)
      {
        return null; // No parameter
      }

      public Object getConstantValue (Object key)
      {
        return null; // No constant
      }

      public Object getUnaryOperatorValue (Object unaryOperator, Object param)
      {
        if (unaryOperator.equals ("+"))
          return param;
        else if (unaryOperator.equals ("-"))
          return new Double (-((Number)param).doubleValue ());
        else
          throw new IllegalArgumentException ("Not implemented");
      }

      public Object getBinaryOperatorValue (Object binaryOperator, Object param1, Object param2)
      {
        if (binaryOperator.equals ("+"))
          return new Double (((Number)param1).doubleValue () + ((Number)param2).doubleValue ());
        else if (binaryOperator.equals ("-"))
          return new Double (((Number)param1).doubleValue () - ((Number)param2).doubleValue ());
        else if (binaryOperator.equals ("/"))
          return new Double (((Number)param1).doubleValue () / ((Number)param2).doubleValue ());
        else if (binaryOperator.equals ("*"))
          return new Double (((Number)param1).doubleValue () * ((Number)param2).doubleValue ());
        else if (binaryOperator.equals ("POW"))
          return new Double (Math.pow (((Number)param1).doubleValue (), ((Number)param2).doubleValue ()));
        else
          throw new IllegalArgumentException ("Not implemented");
      }

      public Object getCommonFunctionValue (Object predefinedFunction, Object param)
      {
        if (predefinedFunction.equals ("LN"))
          return new Double (Math.log (((Number)param).doubleValue ()));
        else if (predefinedFunction.equals ("LOG"))
          return new Double (Math.log (((Number)param).doubleValue ()) / Math.log (10.));
        else if (predefinedFunction.equals ("EXP"))
          return new Double (Math.exp (((Number)param).doubleValue ()));
        else if (predefinedFunction.equals ("SQR"))
          return new Double (((Number)param).doubleValue () * ((Number)param).doubleValue ());
        else if (predefinedFunction.equals ("SQRT"))
          return new Double (Math.sqrt (((Number)param).doubleValue ()));
        else if (predefinedFunction.equals ("COS"))
          return new Double (Math.cos (((Number)param).doubleValue ()));
        else if (predefinedFunction.equals ("SIN"))
          return new Double (Math.sin (((Number)param).doubleValue ()));
        else if (predefinedFunction.equals ("TAN"))
          return new Double (Math.tan (((Number)param).doubleValue ()));
        else if (predefinedFunction.equals ("ARCCOS"))
          return new Double (Math.acos (((Number)param).doubleValue ()));
        else if (predefinedFunction.equals ("ARCSIN"))
          return new Double (Math.asin (((Number)param).doubleValue ()));
        else if (predefinedFunction.equals ("ARCTAN"))
          return new Double (Math.atan (((Number)param).doubleValue ()));
        else if (predefinedFunction.equals ("INV"))
          return new Double (1. / ((Number)param).doubleValue ());
        else
          throw new IllegalArgumentException ("Not implemented");
      }

      public Object getConditionValue (Object paramIf, Object paramThen,  Object paramElse)
      {
        return null;
      }

      public boolean isTrue (Object param)
      {
        return false; // No condition
      }

      public boolean supportsRecursiveCall ()
      {
        return false;
      }

      public Object getFunctionValue (Function    function,
                                      Object []   parameters,
                                      boolean     recursiveCall)
      {
        return null; // No function
      }
    }; // End of interpreter

  // We need only one parser, so let's instantiate it now to load immediately
  // the classes required for parsing
  private static final CalculatorParser PARSER  = new CalculatorParser (SYNTAX);

  // GUI components
  private TextField expressionTextField  = new TextField ();
  private Label     numberLabel          = new Label ("0", Label.RIGHT);

  // Create function buttons (additional spaces are used in text label for a correct layout on MacOS X)
  private Button    powerButton          = new FuncButton ("     pow     ");
  private Button    squareButton         = new FuncButton ("     x\u00b2      ");
  private Button    squareRootButton     = new FuncButton ("      \u221a      ");
  private Button    invButton            = new FuncButton ("     1/x     ");

  private Button    expButton            = new FuncButton ("     exp     ");
  private Button    lnButton             = new FuncButton ("      ln      ");
  private Button    logButton            = new FuncButton ("     log     ");

  private Button    sinButton            = new FuncButton ("     sin     ");
  private Button    cosButton            = new FuncButton ("     cos     ");
  private Button    tanButton            = new FuncButton ("     tan     ");

  private Button    arcsinButton         = new FuncButton ("  arcsin  ");
  private Button    arccosButton         = new FuncButton ("  arccos  ");
  private Button    arctanButton         = new FuncButton ("  arctan  ");

  private Button    openingBracketButton = new FuncButton ("       (       ");
  private Button    closingBracketButton = new FuncButton ("       )       ");

  private Button    piButton             = new FuncButton ("      \u03C0      ");

  private Button    digit0Button         = new DigButton ("0");
  private Button    digit1Button         = new DigButton ("1");
  private Button    digit2Button         = new DigButton ("2");
  private Button    digit3Button         = new DigButton ("3");
  private Button    digit4Button         = new DigButton ("4");
  private Button    digit5Button         = new DigButton ("5");
  private Button    digit6Button         = new DigButton ("6");
  private Button    digit7Button         = new DigButton ("7");
  private Button    digit8Button         = new DigButton ("8");
  private Button    digit9Button         = new DigButton ("9");

  private Button    decimalButton        = new DigButton (" . ");
  private Button    oppositeButton       = new DigButton (" \u00b1 "); // -/+ symbol

  private Button    addButton            = new DigButton ("+");
  private Button    substractButton      = new DigButton ("-");
  private Button    multiplyButton       = new DigButton ("*");
  private Button    divideButton         = new DigButton ("/");

  private Button    clearButton          = new DigButton ("CE");
  private Button    clearAllButton       = new DigButton ("C");
  private Button    equalButton          = new DigButton("=");

  private boolean   newExpression      = false;
  private boolean   newOperand         = true;
  private String    lastBinaryOperator = null;

  private class DigButton extends Button
  { 
    public DigButton (String label)
    {
      super (label);
    }
    
    // Override preferredSize to get a button with its width = 5/4 height;
    public Dimension preferredSize ()
    {
      Dimension dim = super.preferredSize ();
      dim.width = dim.height * 5 / 4;
      return dim;
    } 
  }; 

  private class FuncButton extends Button
  { 
    public FuncButton (String label)
    {
      super (label);
    }
    
    // Override preferredSize to get a button with its width = 5 / 2 height;
    public Dimension preferredSize ()
    {
      Dimension dim = super.preferredSize ();
      dim.width = dim.height * 5 / 2;
      return dim;
    } 
  }; 

  /**
   * Inits the applet GUI.
   */
  public void init ()
  {
    Font defaultFont = getFont ();
    setFont (new java.awt.Font (defaultFont.getName (), Font.BOLD, defaultFont.getSize ()));
    numberLabel.setFont (new java.awt.Font (defaultFont.getName (), Font.BOLD, defaultFont.getSize () * 2));
    numberLabel.setBackground (Color.white);

    // Disable text field (too complicated to manage text entered by user and entered with the buttons)
    expressionTextField.disable ();
    
    // v1.0.2 : Changed keys layout
    // Build function keys panel
    Panel fctPanel = new Panel ();
    fctPanel.setLayout (new GridLayout (4, 4, 3, 3));
    fctPanel.add (powerButton);
    fctPanel.add (sinButton);
    fctPanel.add (arcsinButton);
    fctPanel.add (expButton);

    fctPanel.add (squareRootButton);
    fctPanel.add (cosButton);
    fctPanel.add (arccosButton);
    fctPanel.add (lnButton);

    fctPanel.add (squareButton);
    fctPanel.add (tanButton);
    fctPanel.add (arctanButton);
    fctPanel.add (logButton);

    fctPanel.add (invButton);
    fctPanel.add (piButton);
    fctPanel.add (openingBracketButton);
    fctPanel.add (closingBracketButton);

    // Build numeric keys panel
    Panel digitPanel = new Panel ();
    digitPanel.setLayout (new GridLayout (4, 4, 3, 3));
    digitPanel.add (digit7Button);
    digitPanel.add (digit8Button);
    digitPanel.add (digit9Button);
    digitPanel.add (divideButton);

    digitPanel.add (digit4Button);
    digitPanel.add (digit5Button);
    digitPanel.add (digit6Button);
    digitPanel.add (multiplyButton);

    digitPanel.add (digit1Button);
    digitPanel.add (digit2Button);
    digitPanel.add (digit3Button);
    digitPanel.add (substractButton);

    digitPanel.add (digit0Button);
    digitPanel.add (decimalButton);
    digitPanel.add (oppositeButton);
    digitPanel.add (addButton);

    // Build control keys panel
    Panel clearPanel = new Panel ();
    clearPanel.setLayout (new GridLayout (2, 1, 3, 3));
    clearPanel.add (clearButton);
    clearPanel.add (clearAllButton);

    Panel controlKeysPanel = new Panel ();
    controlKeysPanel.setLayout (new GridLayout (2, 1, 3, 3));
    controlKeysPanel.add (clearPanel);
    controlKeysPanel.add (equalButton);

    Panel keysPanel = new Panel ();
    GridBagLayout layout = new GridBagLayout ();
    keysPanel.setLayout (layout);
    GridBagConstraints constraints = new GridBagConstraints ();
    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.weightx = 1;
    constraints.weighty = 1;    
    constraints.fill = GridBagConstraints.BOTH;
    constraints.insets = new Insets (0, 0, 0, 5);
    keysPanel.add (fctPanel);
    layout.setConstraints (fctPanel, constraints);
    constraints.insets = new Insets (0, 0, 0, 3);
    constraints.gridx = 1;
    keysPanel.add (digitPanel);
    layout.setConstraints (digitPanel, constraints);
    constraints.insets = new Insets (0, 0, 0, 0);
    constraints.gridx = 2;
    keysPanel.add (controlKeysPanel);
    layout.setConstraints (controlKeysPanel, constraints);

    // Build textField and label panel
    Panel displayPanel = new Panel ();
    displayPanel.setLayout (new BorderLayout (3, 3));
    displayPanel.add ("North", expressionTextField);
    // Add numberLabel to a panel with a border
    Panel numberPanel = new Panel ()
      {
        public Insets insets ()
        {
          return new Insets (1, 1, 1, 1);
        }        
      };
    numberPanel.setBackground (Color.lightGray);
    numberPanel.setLayout (new GridLayout (1, 1));
    numberPanel.add (numberLabel);
    displayPanel.add ("South", numberPanel);

    // Build calculator panel
    Panel calculatorPanel = new Panel ();
    calculatorPanel.setLayout (new BorderLayout (3, 3));
    calculatorPanel.add ("North", displayPanel);
    calculatorPanel.add ("Center", keysPanel);
    setLayout (new BorderLayout ());
    add ("West", calculatorPanel);
  }

  /**
   * Returns an inset of 3 pixels.
   */
  public Insets insets ()
  {
    return new Insets (3, 3, 3, 3);
  }

  /**
   * Maps key events to their matching button.
   */
  public boolean keyDown (Event evt, int key)
  {
    switch (key)
    {
      case '0' : postEvent (new Event (digit0Button, Event.ACTION_EVENT, digit0Button.getLabel()));
                 break;
      case '1' : postEvent (new Event (digit1Button, Event.ACTION_EVENT, digit1Button.getLabel()));
                 break;
      case '2' : postEvent (new Event (digit2Button, Event.ACTION_EVENT, digit2Button.getLabel()));
                 break;
      case '3' : postEvent (new Event (digit3Button, Event.ACTION_EVENT, digit3Button.getLabel()));
                 break;
      case '4' : postEvent (new Event (digit4Button, Event.ACTION_EVENT, digit4Button.getLabel()));
                 break;
      case '5' : postEvent (new Event (digit5Button, Event.ACTION_EVENT, digit5Button.getLabel()));
                 break;
      case '6' : postEvent (new Event (digit6Button, Event.ACTION_EVENT, digit6Button.getLabel()));
                 break;
      case '7' : postEvent (new Event (digit7Button, Event.ACTION_EVENT, digit7Button.getLabel()));
                 break;
      case '8' : postEvent (new Event (digit8Button, Event.ACTION_EVENT, digit8Button.getLabel()));
                 break;
      case '9' : postEvent (new Event (digit9Button, Event.ACTION_EVENT, digit9Button.getLabel()));
                 break;
      case '.' : postEvent (new Event (decimalButton, Event.ACTION_EVENT, decimalButton.getLabel()));
                 break;
      case '+' : postEvent (new Event (addButton, Event.ACTION_EVENT, addButton.getLabel()));
                 break;
      case '-' : postEvent (new Event (substractButton, Event.ACTION_EVENT, substractButton.getLabel()));
                 break;
      case '*' : postEvent (new Event (multiplyButton, Event.ACTION_EVENT, multiplyButton.getLabel()));
                 break;
      case '/' : postEvent (new Event (divideButton, Event.ACTION_EVENT, divideButton.getLabel()));
                 break;
      case '(' : postEvent (new Event (openingBracketButton, Event.ACTION_EVENT, openingBracketButton.getLabel()));
                 break;
      case ')' : postEvent (new Event (closingBracketButton, Event.ACTION_EVENT, closingBracketButton.getLabel()));
                 break;
      case Event.ENTER : postEvent (new Event (equalButton, Event.ACTION_EVENT, equalButton.getLabel()));
                 break;
    }
    return true;
  }

  /**
   * Builds the expression depending on which button was pressed.
   */
  public boolean action (Event evt, Object what)
  {
    if (evt.target instanceof Button)
    {
      String label = ((String)what).trim();

      if (   evt.target == equalButton
          && newExpression)
        return true; // Result already computed

      if (   evt.target == clearAllButton
          || newExpression)
      {
        expressionTextField.setText ("");
        if (   Character.isDigit (label.charAt (0))
            || evt.target == decimalButton
            || evt.target == clearAllButton)
          numberLabel.setText ("0");

        newExpression = false;
      }

      // Build the expression according to the keys
      String expression = expressionTextField.getText ();

      if (   "+-/*".indexOf (label) != -1
          || evt.target == powerButton)
      {
        // Binary operators
        if (newOperand && lastBinaryOperator != null)
          expressionTextField.setText (expression.substring (0, expression.lastIndexOf (lastBinaryOperator)) + label + " ");
        else if (expression.endsWith (")"))
          expressionTextField.setText (expression + " " + label + " ");
        else
          expressionTextField.setText (expression + numberLabel.getText () + " " + label + " ");
        newOperand = true;
        lastBinaryOperator = label;
      }
      else
      {
        lastBinaryOperator = null;

        if (evt.target == equalButton)
        {
          if (!expression.endsWith (")"))
            expressionTextField.setText (expression += numberLabel.getText ());

          // Add closing brackets if needed
          for (int i = getCharCount (expression, '(') - getCharCount (expression, ')');
               i > 0;
               i--)
            expressionTextField.setText (expression += ")");

          try
          {
            // Compute the result with calculator's parser
            numberLabel.setText (PARSER.computeExpression (expressionTextField.getText (),
                                                           INTERPRETER).toString ());
            newOperand = false;
          }
          catch (CompilationException e)
          {
            // Shouldn't happen if the calculator builds a correct expression
            numberLabel.setText (e.getMessage ());
          }

          newExpression = true;
        }
        else if (   label.length () == 1
                 && Character.isDigit (label.charAt (0)))
        {
          // Key is a digit
          if ("0".equals (numberLabel.getText ()))
            numberLabel.setText (label);
          else if ("-0".equals (numberLabel.getText ()))
            numberLabel.setText ("-" + label);
          else
            numberLabel.setText (numberLabel.getText () + label);
          newOperand = false;
        }
        else if (evt.target == decimalButton)
        {
          numberLabel.setText (numberLabel.getText () + (numberLabel.getText ().indexOf (".") == -1 ? "." : ""));
          newOperand = false;
        }
        else if (evt.target == oppositeButton)
        {
          numberLabel.setText (numberLabel.getText ().charAt (0) == '-'
                                     ? numberLabel.getText ().substring (1)
                                     : "-" + numberLabel.getText ());
          newOperand = false;
        }
        else if (evt.target == piButton)
        {
          numberLabel.setText (String.valueOf (Math.PI));
          newOperand = false;
        }
        else if (evt.target == clearButton)
          newOperand = true;
        else if (evt.target == openingBracketButton)
        {
          expressionTextField.setText (expression + "(");
          newOperand = true;
        }
        else if (evt.target == closingBracketButton)
        {
          if (getCharCount (expression, '(') - getCharCount (expression, ')') > 0)
            if (expression.endsWith (")"))
              expressionTextField.setText (expression + ")");
            else
              expressionTextField.setText (expression + numberLabel.getText () + ")");
        }
        else if (evt.target != clearAllButton)
        {
          // Predefined functions
          // Replace the text of some keys
          if (evt.target == squareButton)
            label = "sqr";
          else if (evt.target == squareRootButton)
            label = "sqrt";
          else if (evt.target == invButton)
            label = "inv";
          expressionTextField.setText (expression + label + " ");
          newOperand = true;
        }
      }

      if (newOperand)
        numberLabel.setText ("0");
    }
    return true;
  }

  // Counts the number of c character in str
  private int getCharCount (String str, char c)
  {
    int count  = 0;
    int cIndex = -1;
    do
      if ((cIndex = str.indexOf (c, cIndex + 1)) != -1)
        count++;
    while (cIndex != -1);
    return count;
  }

  public String getAppletInfo()
  {
    return "JeksCalculator applet\nCopyrights (C) 1998-2001 eTeks";
  }

  /**
   * Returns the current value of the number label.
   */
  public double getValue ()
  {
    return new Double (numberLabel.getText ()).doubleValue ();
  }

  /**
   * Main method of this class enabling the use of this applet as an application.
   */
  public static void main (String args [])
  {
    // Creation of a frame displaying this applet
    Frame  appletFrame = new Frame ("JeksCalculator")
      {
        public boolean handleEvent (Event event)
        {
          switch (event.id)
          {
            case Event.WINDOW_DESTROY :
              // Destroy if asked to quit
              dispose ();
              System.exit (0);
              return true;
            default :
              return super.handleEvent (event);
           }
         }
      };
    Applet applet = new JeksCalculator ();

    // Add the applet to the frame and display it
    appletFrame.add ("Center", applet);

    // Start the applet
    appletFrame.addNotify ();
    applet.init ();

    appletFrame.pack ();
    appletFrame.setResizable (false);
    appletFrame.show ();
  }
}
