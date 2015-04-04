/*
 * @(#)JeksTableModel.java   05/02/99
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

import java.io.IOException;
import java.util.Hashtable;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

/**
 * A table model storing its values in a hashtable with keys of <code>JeksCell</code> class.
 * This allows to have very big tables with a lot of <code>null</code> values.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 */
public class JeksTableModel extends AbstractTableModel
{
	private int    rowCount;
	private int    columnCount;
	private String[] columnNames;

	private Hashtable cellValues;

	/**
	 * Creates a table model with <code>Short.MAX_VALUE</code> rows and columns.
	 */
	public JeksTableModel ()
	{
		this (Short.MAX_VALUE, Short.MAX_VALUE, null);
	}
	
	/**
	 * Creates a table model with <code>rowCount</code> rows and
	 * <code>columnCount</code> columns.
	 */
	public JeksTableModel (int rowCount, int columnCount)
	{
		this (rowCount, columnCount, null);
	}

	/**
	 * Creates a table model with <code>rowCount</code> rows and
	 * <code>columnCount</code> columns.
	 */
	public JeksTableModel (int rowCount, int columnCount, String[] columnNames)
	{
		this.rowCount     = rowCount;
		this.columnCount  = columnCount;
		this.columnNames  = columnNames;

		cellValues = new Hashtable ();
	}

	public int getRowCount ()
	{
		return rowCount;
	}

	public int getColumnCount ()
	{
		return columnCount;
	}

	public Object getValueAt (int row, int column)
	{
		// row and column index are checked but storing in a Hashtable
		// won't cause real problems
		if (row >= getRowCount ())
			throw new ArrayIndexOutOfBoundsException (row);
		if (column >= getColumnCount ())
			throw new ArrayIndexOutOfBoundsException (column);
		return cellValues.get (new JeksCell (row, column));
	}

	public boolean isCellEditable (int row, int column)
	{
		return true;
	}

	public void setValueAt (Object value, int row, int column)
	{
		// row and column index are checked but storing in a Hashtable
		// won't cause real problems
		if (row >= getRowCount ())
			throw new ArrayIndexOutOfBoundsException (row);
		if (column >= getColumnCount ())
			throw new ArrayIndexOutOfBoundsException (column);
		JeksCell cell = new JeksCell (row, column);
		if (   value == null
				|| "".equals (value))
			cellValues.remove (cell);
		else
			cellValues.put (cell, value);

		fireTableChanged (new TableModelEvent (this, row, row, column));
	}

	private void writeObject (java.io.ObjectOutputStream out) throws IOException
	{
		out.writeInt (rowCount);
		out.writeInt (columnCount);
		out.writeObject (cellValues);
		// Don't record listeners
	}

	private void readObject (java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		rowCount    = in.readInt ();
		columnCount = in.readInt ();
		cellValues  = (Hashtable)in.readObject ();
	}

	public void setColumnNames(String[] columnNames) {
		this.columnNames = columnNames;
	}

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}
}
