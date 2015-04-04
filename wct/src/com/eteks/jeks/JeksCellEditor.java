/*
 * @(#)JeksCellEditor.java   08/23/99
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

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.DefaultCellEditor;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableModel;

import com.eteks.parser.CompilationException;

/**
 * Cell editor for computed and default cells. This editor enables to edit the definition
 * of an expression if the edited value is an instance of <code>JeksExpression</code>.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 */
public class JeksCellEditor extends DefaultCellEditor
{
  private JeksExpressionParser parser;
  private JTable               table;
  private Object               cellValue;

  /**
   * Creates a cell editor.
   * @param parser the parser used to parse and compile the cells starting
   *               with an assignment operator.
   */
  public JeksCellEditor (JeksExpressionParser parser)
  {
    super (new JTextField ()); // No defaut super constructor

    final JTextField textField = new JTextField ();
    editorComponent = textField;
    // Reassign an other delegate that checks stopCellEditing ()
    // before firing stop event
    delegate = new EditorDelegate ()
      {
        public void setValue (Object value)
        {
          textField.setText ((value != null) ? value.toString() : "");
        }

        public Object getCellEditorValue ()
        {
          return textField.getText();
        }

        public void actionPerformed (ActionEvent event)
        {
          if (JeksCellEditor.this.stopCellEditing ())
            super.actionPerformed (event);
        }
      };
    textField.addActionListener(delegate);

    this.parser = parser;
  }

  /**
   * Returns the table model value edited by this editor.
   */
  public Object getCellEditorValue ()
  {
    return cellValue;
  }

  public boolean stopCellEditing ()
  {
    String data = (String)super.getCellEditorValue ();
    cellValue = getModelValue (data,
                               table.getModel (),
                               new JeksCell (table.getEditingRow (),
                                             table.convertColumnIndexToModel (table.getEditingColumn ())));
    if (cellValue == null)
      return false;
    else
      return super.stopCellEditing ();
  }

  /**
   * Returns the component used to edit cell values.
   * Overrides <code>getTableCellEditorComponent ()</code> to use the value returned
   * by <code>getEditedValue (value)</code> in the editor instead of the table model
   * <code>value</code> itself.
   */
  public Component getTableCellEditorComponent (JTable table,
                                                Object value,
                                                boolean isSelected,
                                                int row,
                                                int column)
  {
    cellValue = value;
    value = getEditedValue (value);

    this.table = table;
    return super.getTableCellEditorComponent (table, value, isSelected, row, column);
  }

  /**
   * Returns <code>value</code> as an editable object. If <code>value</code> is an
   * instance of <code>JeksExpression</code> the definition of the expression is returned,
   * if <code>value</code> is a number or a date its localized string representation
   * is returned, otherwise the object <code>value</code> itself is returned.
   * @param value a value of the table model.
   */
  public Object getEditedValue (Object value)
  {
    if (parser != null)
      return parser.getEditedValue (value);
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
   * @param data   a string.
   * @param model  the edited table model.
   * @param editingCell  the current editing cell.
   * @return an instance of either classes : <code>Long</code>, <code>Double</code>,
   *         <code>Date</code>, <code>Boolean</code>, <code>String</code>
   *         or <code>JeksExpression</code>.
   */
  public Object getModelValue (String      data,
                               TableModel  model,
                               JeksCell    editingCell)
  {
    try
    {
      if (parser != null)
      {
        Object modelValue = parser.getModelValue (data);
        if (modelValue instanceof JeksExpression)
          try
          {
            ((JeksExpression)modelValue).checkCircularity (model, editingCell);
          }
          catch (CircularityException ex)
          {
            JeksExpressionSyntax syntax = (JeksExpressionSyntax)parser.getSyntax ();
            if (JOptionPane.showConfirmDialog (null,
                                               syntax.getMessage (JeksExpressionSyntax.MESSAGE_CIRCULARITY_ERROR_CONFIRM),
                                               syntax.getMessage (JeksExpressionSyntax.MESSAGE_CIRCULARITY_ERROR_TITLE),
                                               JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
              return null;
            else
              ((JeksExpression)modelValue).invalidateValue (ex);
          }
        return modelValue;
      }
      else
        return data;
    }
    catch (CompilationException ex)
    {
      JeksExpressionSyntax syntax = (JeksExpressionSyntax)parser.getSyntax ();
      String errorMessage = syntax.getExceptionMessage (ex);
      JOptionPane.showMessageDialog (null, errorMessage, syntax.getMessage (JeksExpressionSyntax.MESSAGE_COMPILATION_ERROR_TITLE),
                                     JOptionPane.ERROR_MESSAGE);
      // TODO find a way to show the error to user
      // ((JTextField)getComponent ()).setCaretPosition (ex.getCharacterIndex ());
      return null;
    }
  }
}
