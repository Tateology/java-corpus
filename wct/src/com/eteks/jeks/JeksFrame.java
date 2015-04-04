/*
 * @(#)JeksFrame.java   05/02/99
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import com.eteks.parser.CompilationException;
import com.eteks.parser.CompiledFunction;
import com.eteks.tools.awt.SplashScreenWindow;
import com.eteks.tools.swing.HTMLDocumentViewer;

/**
 * Main class of Jeks.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 */
public class JeksFrame extends JFrame
{
  private JTable      table;
  private JTextField  textField;
  private JScrollPane scrollPane;

  private final static String TITLE_BASE = "Jeks";
  private String      currentDirectory = System.getProperty ("user.dir");
  private File        currentFile;
  private boolean     tableChanged;

  private ResourceBundle resourceBundle = ResourceBundle.getBundle ("com.eteks.jeks.resources.jeks");

  private HTMLDocumentViewer helpViewer;

  private static class ToolBarButton extends JButton
  {
    public ToolBarButton (Icon icon)
    {
      super (icon);
      setBorder (new EtchedBorder (EtchedBorder.LOWERED));
    }
  }

  private class JeksToolBar extends JToolBar
  {
    JButton newButton;
    JButton openButton;
    JButton saveButton;

    JButton cutButton;
    JButton copyButton;
    JButton pasteButton;

    public JeksToolBar ()
    {
      Icon newIcon  = new ImageIcon (getClass ().getResource("/toolbarButtonGraphics/general/New16.gif"));
      Icon openIcon = new ImageIcon (getClass ().getResource("/toolbarButtonGraphics/general/Open16.gif"));
      Icon saveIcon = new ImageIcon (getClass ().getResource("/toolbarButtonGraphics/general/Save16.gif"));

      Icon cutIcon   = new ImageIcon (getClass ().getResource("/toolbarButtonGraphics/general/Cut16.gif"));
      Icon copyIcon  = new ImageIcon (getClass ().getResource("/toolbarButtonGraphics/general/Copy16.gif"));
      Icon pasteIcon = new ImageIcon (getClass ().getResource("/toolbarButtonGraphics/general/Paste16.gif"));

      newButton  = new ToolBarButton (newIcon);
      openButton = new ToolBarButton (openIcon);
      saveButton = new ToolBarButton (saveIcon);

      cutButton   = new ToolBarButton (cutIcon);
      copyButton  = new ToolBarButton (copyIcon);
      pasteButton = new ToolBarButton (pasteIcon);

      newButton.addActionListener (newAction);
      openButton.addActionListener (openAction);
      saveButton.addActionListener (saveAction);
      cutButton.addActionListener (cutAction);
      copyButton.addActionListener (copyAction);
      pasteButton.addActionListener (pasteAction);

      add (newButton);
      add (openButton);
      add (saveButton);
      addSeparator ();
      add (cutButton);
      add (copyButton);
      add (pasteButton);

      // TODO : To delete ???
      new java.util.Properties ();
    }
  }

  // Menu bar for Mac OS classic
  private class JeksMenuBar extends MenuBar
  {
    MenuItem newMenuItem    = new MenuItem ();
    MenuItem openMenuItem   = new MenuItem ();
    MenuItem saveMenuItem   = new MenuItem ();
    MenuItem saveAsMenuItem = new MenuItem ();
    MenuItem quitMenuItem   = new MenuItem ();

    MenuItem cutMenuItem       = new MenuItem ();
    MenuItem copyMenuItem      = new MenuItem ();
    MenuItem pasteMenuItem     = new MenuItem ();
    MenuItem eraseMenuItem     = new MenuItem ();

    MenuItem newFunctionMenuItem  = new MenuItem ();

    MenuItem helpMenuItem      = new MenuItem ();
    MenuItem aboutMenuItem     = new MenuItem ();

    public JeksMenuBar ()
    {
      loadMenuLabel (newMenuItem, "FILE_NEW_MENU");
      loadMenuLabel (openMenuItem, "FILE_OPEN_MENU");
      loadMenuLabel (saveMenuItem, "FILE_SAVE_MENU");
      loadMenuLabel (saveAsMenuItem, "FILE_SAVE_AS_MENU");
      loadMenuLabel (quitMenuItem, "FILE_QUIT_MENU");
      loadMenuLabel (cutMenuItem, "EDIT_CUT_MENU");
      loadMenuLabel (copyMenuItem, "EDIT_COPY_MENU");
      loadMenuLabel (pasteMenuItem, "EDIT_PASTE_MENU");
      loadMenuLabel (eraseMenuItem, "EDIT_DELETE_MENU");
      loadMenuLabel (newFunctionMenuItem, "TOOLS_NEW_FUNCTION_MENU");
      loadMenuLabel (helpMenuItem, "HELP_HELP_MENU");
      loadMenuLabel (aboutMenuItem, "HELP_ABOUT_MENU");

      newMenuItem.setShortcut (new MenuShortcut (KeyEvent.VK_N));
      openMenuItem.setShortcut (new MenuShortcut (KeyEvent.VK_O));
      saveMenuItem.setShortcut (new MenuShortcut (KeyEvent.VK_S));
      quitMenuItem.setShortcut (new MenuShortcut (KeyEvent.VK_Q));
      cutMenuItem.setShortcut (new MenuShortcut (KeyEvent.VK_X));
      copyMenuItem.setShortcut (new MenuShortcut (KeyEvent.VK_C));
      pasteMenuItem.setShortcut (new MenuShortcut (KeyEvent.VK_V));

      Menu fileMenu = new Menu ();
      loadMenuLabel (fileMenu, "FILE_MENU");
      fileMenu.add (newMenuItem);
      fileMenu.add (openMenuItem);
      fileMenu.addSeparator ();
      fileMenu.add (saveMenuItem);
      fileMenu.add (saveAsMenuItem);
      fileMenu.addSeparator ();
      fileMenu.add (quitMenuItem);

      Menu editMenu = new Menu ();
      loadMenuLabel (editMenu, "EDIT_MENU");
      editMenu.add (cutMenuItem);
      editMenu.add (copyMenuItem);
      editMenu.add (pasteMenuItem);
      editMenu.add (eraseMenuItem);

      Menu toolsMenu = new Menu ();
      loadMenuLabel (toolsMenu, "TOOLS_MENU");
      toolsMenu.add (newFunctionMenuItem);

      Menu helpMenu = new Menu ();
      loadMenuLabel (helpMenu, "HELP_MENU");
      helpMenu.add (helpMenuItem);
      helpMenu.add (aboutMenuItem);

      add (fileMenu);
      add (editMenu);
      add (toolsMenu);
      add (helpMenu);

      newMenuItem.addActionListener (newAction);
      openMenuItem.addActionListener (openAction);
      saveMenuItem.addActionListener (saveAction);
      saveAsMenuItem.addActionListener (saveAsAction);
      quitMenuItem.addActionListener (quitAction);
      cutMenuItem.addActionListener (cutAction);
      copyMenuItem.addActionListener (copyAction);
      pasteMenuItem.addActionListener (pasteAction);
      eraseMenuItem.addActionListener (eraseAction);
      newFunctionMenuItem.addActionListener (newFunctionAction);
      helpMenuItem.addActionListener (helpAction);
      aboutMenuItem.addActionListener (aboutAction);
    }

    private void loadMenuLabel (MenuItem menuItem, String resource)
    {
      String menuString = resourceBundle.getString (resource);
      int mnemonicIndex = menuString.indexOf ('&');
      if (mnemonicIndex >= 0)
        menuItem.setLabel (menuString.substring (0, mnemonicIndex) + menuString.substring (mnemonicIndex + 1));
      else
        menuItem.setLabel (menuString);
    }
  }

  // Menu bar for other OS (MacOS X should set the property com.apple.macos.useScreenMenuBar=true
  // to use a screen menu bar)
  private class JeksJMenuBar extends JMenuBar
  {
    JMenuItem newMenuItem    = new JMenuItem ();
    JMenuItem openMenuItem   = new JMenuItem ();
    JMenuItem saveMenuItem   = new JMenuItem ();
    JMenuItem saveAsMenuItem = new JMenuItem ();
    JMenuItem quitMenuItem   = new JMenuItem ();

    JMenuItem cutMenuItem       = new JMenuItem ();
    JMenuItem copyMenuItem      = new JMenuItem ();
    JMenuItem pasteMenuItem     = new JMenuItem ();
    JMenuItem eraseMenuItem     = new JMenuItem ();

    JMenuItem newFunctionMenuItem  = new JMenuItem ();

    JMenuItem helpMenuItem      = new JMenuItem ();
    JMenuItem aboutMenuItem     = new JMenuItem ();

    public JeksJMenuBar ()
    {
      loadMenuLabel (newMenuItem, "FILE_NEW_MENU");
      loadMenuLabel (openMenuItem, "FILE_OPEN_MENU");
      loadMenuLabel (saveMenuItem, "FILE_SAVE_MENU");
      loadMenuLabel (saveAsMenuItem, "FILE_SAVE_AS_MENU");
      loadMenuLabel (quitMenuItem, "FILE_QUIT_MENU");
      loadMenuLabel (cutMenuItem, "EDIT_CUT_MENU");
      loadMenuLabel (copyMenuItem, "EDIT_COPY_MENU");
      loadMenuLabel (pasteMenuItem, "EDIT_PASTE_MENU");
      loadMenuLabel (eraseMenuItem, "EDIT_DELETE_MENU");
      loadMenuLabel (newFunctionMenuItem, "TOOLS_NEW_FUNCTION_MENU");
      loadMenuLabel (helpMenuItem, "HELP_HELP_MENU");
      loadMenuLabel (aboutMenuItem, "HELP_ABOUT_MENU");

      // v1.0.2 Added preferred shortcut mask
      int keyMask = Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask();
      newMenuItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_N, keyMask));
      openMenuItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_O, keyMask));
      saveMenuItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_S, keyMask));
      quitMenuItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_Q, keyMask));
      cutMenuItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_X, keyMask));
      copyMenuItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_C, keyMask));
      pasteMenuItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_V, keyMask));
      helpMenuItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_F1, 0));

      JMenu fileMenu = new JMenu ();
      loadMenuLabel (fileMenu, "FILE_MENU");
      fileMenu.add (newMenuItem);
      fileMenu.add (openMenuItem);
      fileMenu.addSeparator ();
      fileMenu.add (saveMenuItem);
      fileMenu.add (saveAsMenuItem);
      fileMenu.addSeparator ();
      fileMenu.add (quitMenuItem);

      JMenu editMenu = new JMenu ();
      loadMenuLabel (editMenu, "EDIT_MENU");
      editMenu.add (cutMenuItem);
      editMenu.add (copyMenuItem);
      editMenu.add (pasteMenuItem);
      editMenu.add (eraseMenuItem);

      JMenu toolsMenu = new JMenu ();
      loadMenuLabel (toolsMenu, "TOOLS_MENU");
      toolsMenu.add (newFunctionMenuItem);

      JMenu helpMenu = new JMenu ();
      loadMenuLabel (helpMenu, "HELP_MENU");
      helpMenu.add (helpMenuItem);
      helpMenu.add (aboutMenuItem);

      add (fileMenu);
      add (editMenu);
      add (toolsMenu);
      add (helpMenu);

      newMenuItem.addActionListener (newAction);
      openMenuItem.addActionListener (openAction);
      saveMenuItem.addActionListener (saveAction);
      saveAsMenuItem.addActionListener (saveAsAction);
      quitMenuItem.addActionListener (quitAction);
      cutMenuItem.addActionListener (cutAction);
      copyMenuItem.addActionListener (copyAction);
      pasteMenuItem.addActionListener (pasteAction);
      eraseMenuItem.addActionListener (eraseAction);
      newFunctionMenuItem.addActionListener (newFunctionAction);
      helpMenuItem.addActionListener (helpAction);
      aboutMenuItem.addActionListener (aboutAction);
    }

    private void loadMenuLabel (JMenuItem menuItem, String resource)
    {
      String menuString = resourceBundle.getString (resource);
      int mnemonicIndex = menuString.indexOf ('&');
      if (mnemonicIndex >= 0)
      {
        menuItem.setText (menuString.substring (0, mnemonicIndex) + menuString.substring (mnemonicIndex + 1));
        menuItem.setMnemonic (menuString.charAt (mnemonicIndex + 1));
      }
      else
        menuItem.setText (menuString);
    }
  }

  private ActionListener newAction = new ActionListener ()
    {
      public void actionPerformed (ActionEvent event)
      {
        if (confirmCloseDocument ())
        {
          setTable (new JeksTable ());
          currentFile = null;
          setTitle (TITLE_BASE);
        }
      }
    };

  private javax.swing.filechooser.FileFilter jeksFileFilter = new javax.swing.filechooser.FileFilter ()
    {
      public boolean accept (File f)
      {
        return    f.isDirectory()
               || f.getName ().toLowerCase ().endsWith (".jks");
      }

      public String getDescription ()
      {
        return "Jeks spreadsheet (*.jks)";
      }
    };

  private ActionListener openAction = new ActionListener ()
    {
      public void actionPerformed (ActionEvent event)
      {
        openDocument ();
      }
    };

  private ActionListener saveAction = new ActionListener ()
    {
      public void actionPerformed (ActionEvent event)
      {
        saveDocument ();
      }
    };

  private ActionListener saveAsAction = new ActionListener ()
    {
      public void actionPerformed (ActionEvent event)
      {
        saveAsDocument ();
      }
    };

  private ActionListener quitAction = new ActionListener ()
    {
      public void actionPerformed (ActionEvent event)
      {
        if (confirmCloseDocument ())
          System.exit (0);
      }
    };

  private ActionListener cutAction = new ActionListener ()
    {
      public void actionPerformed (ActionEvent event)
      {
        if (table instanceof JeksTable)
          ((JeksTable)table).cutSelectedCells ();
      }
    };

  private ActionListener copyAction = new ActionListener ()
    {
      public void actionPerformed (ActionEvent event)
      {
        if (table instanceof JeksTable)
          ((JeksTable)table).copySelectedCells ();
      }
    };

  private ActionListener pasteAction = new ActionListener ()
    {
      public void actionPerformed (ActionEvent event)
      {
        SwingUtilities.invokeLater (new Runnable ()
          {
            public void run ()
            {
              setEnabled (false);
              setCursor (Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR));
              if (   table instanceof JeksTable
                  && ((JeksTable)table).pasteCopiedCells ())
              {
                JeksExpressionSyntax syntax = (JeksExpressionSyntax)((JeksTable)table).getExpressionParser ().getSyntax ();
                JOptionPane.showMessageDialog (null,
                                               syntax.getMessage (JeksExpressionSyntax.MESSAGE_CIRCULARITY_ERROR_INFO),
                                               syntax.getMessage (JeksExpressionSyntax.MESSAGE_CIRCULARITY_ERROR_TITLE),
                                               JOptionPane.INFORMATION_MESSAGE);
              }
              setCursor (Cursor.getDefaultCursor ());
              setEnabled (true);
            }
          });
      }
    };

  private ActionListener eraseAction = new ActionListener ()
    {
      public void actionPerformed (ActionEvent event)
      {
        if (table instanceof JeksTable)
          ((JeksTable)table).deleteSelectedCells ();
      }
    };

  private ActionListener newFunctionAction = new ActionListener ()
    {
      public void actionPerformed (ActionEvent event)
      {
        createNewFunction ();
      }
    };

  private ActionListener aboutAction = new ActionListener ()
    {
      public void actionPerformed (ActionEvent event)
      {
        JOptionPane.showMessageDialog (JeksFrame.this,
                                       resourceBundle.getString ("ABOUT_DIALOG_TEXT"),
                                       resourceBundle.getString ("ABOUT_DIALOG_TITLE"),
                                       JOptionPane.INFORMATION_MESSAGE);
      }
    };

  private ActionListener helpAction = new ActionListener ()
    {
      public void actionPerformed (ActionEvent event)
      {
        if (helpViewer == null)
        {
          helpViewer = new HTMLDocumentViewer ();
          helpViewer.setPage (ClassLoader.getSystemResource (resourceBundle.getString ("HELP_FILE")));
          Rectangle jeksBounds = getBounds ();
          jeksBounds.x += 20;
          jeksBounds.y += 20;
          helpViewer.setBounds (jeksBounds);
          helpViewer.setIconImage (getIconImage ());
        }
        helpViewer.setVisible (true);
      }
    };

  private WindowListener windowListener = new WindowAdapter ()
    {
      public void windowClosing (WindowEvent event)
      {
        if (confirmCloseDocument ())
          System.exit (0);
      }
    };

  private TableModelListener changeListener = new TableModelListener ()
    {
      public void tableChanged (TableModelEvent event)
      {
        tableChanged = true;
        table.getModel ().removeTableModelListener (this);
      }
    };

  /**
   * Creates a frame with an empty table.
   */
  public JeksFrame ()
  {
    super (TITLE_BASE);

    addWindowListener (windowListener);

    scrollPane = new JScrollPane ();

    // Create a text field to edit and modify table values
    textField = new JTextField  ();

    // Create a jeks table
    setTable (new JeksTable ());

    // Add textField and table to a panel
    JPanel panel = new JPanel (new BorderLayout ());
    panel.add (textField, BorderLayout.NORTH);
    panel.add (scrollPane, BorderLayout.CENTER);

    // Add textField changes listener
    textField.addActionListener (new ActionListener ()
      {
        public void actionPerformed (ActionEvent e)
        {
          JTable table = getTable ();
          String text = textField.getText ();
          // TODO Find a better way than using cell editor
          if (table.getCellEditor (0, 0) instanceof JeksCellEditor)
          {
            int editedRow = table.getSelectedRow ();
            Object value = ((JeksCellEditor)table.getCellEditor (0, 0)).getModelValue (text, table.getModel (),
                                                        new JeksCell (editedRow,
                                                                      table.convertColumnIndexToModel (table.getSelectedColumn ())));
            if (value != null)
            {
              table.setValueAt (value, table.getSelectedRow (), table.getSelectedColumn ());
              table.setRowSelectionInterval (editedRow + 1, editedRow + 1);
            }
          }
        }
      });

    getContentPane ().add (panel, BorderLayout.CENTER);
    getContentPane ().add (new JeksToolBar (), BorderLayout.NORTH);

    setDefaultCloseOperation (DO_NOTHING_ON_CLOSE);
    String os = System.getProperty ("os.name").toLowerCase ();
    if (   os.indexOf ("mac") >= 0
        && os.indexOf ("mac os x") < 0)
      setMenuBar (new JeksMenuBar ());
    else
      setJMenuBar (new JeksJMenuBar ());

    URL iconURL = ClassLoader.getSystemResource ("com/eteks/jeks/resources/jeksicon.gif");
    setIconImage (Toolkit.getDefaultToolkit ().getImage (iconURL));

    pack ();
  }

  /**
   * Returns the table displayed by this frame.
   */
  public JTable getTable ()
  {
    return table;
  }

  /**
   * Sets the displayed table.
   */
  public void setTable (final JTable table)
  {
    this.table = table;

    // Put the table in a scrollPane
    scrollPane.setViewportView (table);

    // Add a row header to the scrollPane
    TableModel rowHeaderModel = new AbstractTableModel ()
      {
        public int getRowCount()
        {
          return table.getRowCount ();
        }

        public int getColumnCount()
        {
          return 1;
        }

        public Object getValueAt (int row, int column)
        {
          return table instanceof JeksTable
                   ? ((JeksExpressionSyntax)((JeksTable)table).getExpressionParser ().getSyntax ()).getRowName (row)
                   : String.valueOf (row + 1);
        }
      };

    JTable rowHeaderTable = new JTable (rowHeaderModel);
    // Set the same renderer for row header and main table column header
    // rowHeaderTable.getColumn (rowHeaderTable.getColumnName (0)).setCellRenderer (table.getTableHeader ().getDefaultRenderer ());
    // rowHeaderTable.getColumn (rowHeaderTable.getColumnName (0)).setCellRenderer (table.getTableHeader ().getColumnModel ().getColumn (0).getHeaderRenderer ());
    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer()
      {
        public Component getTableCellRendererComponent (JTable table, Object value,
                         boolean isSelected, boolean hasFocus, int row, int column)
        {
          setForeground (JeksFrame.this.table.getTableHeader ().getForeground());
          setBackground (JeksFrame.this.table.getTableHeader ().getBackground());
          setFont (JeksFrame.this.table.getTableHeader ().getFont());
          setHorizontalAlignment (CENTER);
          setText (value == null ? "" : value.toString ());
          setBorder (UIManager.getBorder ("TableHeader.cellBorder"));
          return this;
        }
      };

    rowHeaderTable.getColumn (rowHeaderTable.getColumnName (0)).setCellRenderer (renderer);
    rowHeaderTable.getColumn (rowHeaderTable.getColumnName (0)).setPreferredWidth (50);
    // Set a text to the renderer to have a valid preferred size
    renderer.setText ("1");
    rowHeaderTable.setPreferredScrollableViewportSize (rowHeaderTable.getPreferredSize ());
    rowHeaderTable.setRowHeight (renderer.getPreferredSize ().height);
    table.setRowHeight (renderer.getPreferredSize ().height);

    scrollPane.setRowHeaderView (rowHeaderTable);

    // Select a default cell
    table.setRowSelectionInterval (0, 0);
    table.setColumnSelectionInterval (0, 0);

    tableChanged = false;
    table.getModel ().addTableModelListener (changeListener);

    // Add selection listeners
    final ListSelectionListener selectionListener = new ListSelectionListener ()
      {
        public void valueChanged (ListSelectionEvent e)
        {
          int row    = table.getSelectedRow ();
          int column = table.getSelectedColumn ();
          Object value  = table.getValueAt (row, column);
          if (table instanceof JeksTable)
            value = ((JeksTable)table).getExpressionParser ().getEditedValue (value);
          textField.setText (value == null ? "" : value.toString ());
        }
      };
    table.getSelectionModel ().addListSelectionListener (selectionListener);
    table.getColumnModel ().addColumnModelListener (new TableColumnModelListener ()
      {
        public void columnAdded (TableColumnModelEvent e)
        { }

        public void columnRemoved (TableColumnModelEvent e)
        { }

        public void columnMoved (TableColumnModelEvent e)
        { }

        public void columnMarginChanged (ChangeEvent e)
        { }

        public void columnSelectionChanged (ListSelectionEvent e)
        {
          selectionListener.valueChanged (e);
        }
      });
  }

  private boolean confirmCloseDocument ()
  {
    if (!tableChanged)
      return true;
    switch (JOptionPane.showConfirmDialog (this, resourceBundle.getString ("MODIFIED_DOCUMENT_DIALOG_TEXT"),
                                           resourceBundle.getString ("MODIFIED_DOCUMENT_DIALOG_TITLE"),
                                           JOptionPane.YES_NO_CANCEL_OPTION))
    {
      case JOptionPane.YES_OPTION :
        saveDocument ();
      case JOptionPane.NO_OPTION :
        return true;
      default :
        return false;
    }
  }

  private void openDocument ()
  {
    if (confirmCloseDocument ())
    {
      final JFileChooser chooser = new JFileChooser (currentDirectory);
      chooser.setFileFilter (jeksFileFilter);
      if (chooser.showOpenDialog (JeksFrame.this) == JFileChooser.APPROVE_OPTION)
        SwingUtilities.invokeLater (new Runnable ()
          {
            public void run ()
            {
              InputStream  in = null;
              try
              {
                setEnabled (false);
                setCursor (Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR));
                currentDirectory = chooser.getSelectedFile().getParent();
                in = new FileInputStream (chooser.getSelectedFile ());

                // Read in a new table
                setTable (new JeksCodec ().decode (in));

                currentFile = chooser.getSelectedFile ();
                setTitle (TITLE_BASE + " - " + currentFile.getName ());
              }
              catch (IOException e)
              {
                JOptionPane.showMessageDialog (JeksFrame.this, e.getMessage (), resourceBundle.getString ("FILE_OPEN_MENU"),
                                               JOptionPane.ERROR_MESSAGE);
              }
              finally
              {
                try
                {
                  if (in != null)
                    in.close ();
                }
                catch (IOException e)
                { }
                setCursor (Cursor.getDefaultCursor ());
                setEnabled (true);
              }
            }
          });
    }
  }

  private void saveDocument ()
  {
    if (currentFile == null)
      saveAsDocument ();
    else
        SwingUtilities.invokeLater (new Runnable ()
          {
            public void run ()
            {
              OutputStream out = null;
              try
              {
                setEnabled (false);
                setCursor (Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR));
                out = new FileOutputStream (currentFile);
                JTable table = getTable ();
                new JeksCodec ().encode (out, table);
                setTitle (TITLE_BASE + " - " + currentFile.getName ());
                if (tableChanged)
                  table.getModel ().addTableModelListener (changeListener);
                tableChanged = false;
              }
              catch (IOException e)
              {
                JOptionPane.showMessageDialog (JeksFrame.this, e.getMessage (), resourceBundle.getString ("FILE_SAVE_MENU"), JOptionPane.ERROR_MESSAGE);
              }
              finally
              {
                try
                {
                  if (out != null)
                    out.close ();
                }
                catch (IOException e)
                { }
                setCursor (Cursor.getDefaultCursor ());
                setEnabled (true);
              }
            }
          });
  }

  private void saveAsDocument ()
  {
    JFileChooser chooser = new JFileChooser (currentDirectory);
    chooser.setFileFilter (jeksFileFilter);
    if (chooser.showSaveDialog (JeksFrame.this) == JFileChooser.APPROVE_OPTION)
    {
      // Check extension
      File savedFile = chooser.getSelectedFile ();
      currentDirectory = savedFile.getParent();
      if (!savedFile.getName ().toLowerCase ().endsWith (".jks"))
         savedFile = new File (savedFile.getParent (), savedFile.getName () + ".jks");
      currentFile = savedFile;
      currentDirectory = savedFile.getParent();
      saveDocument ();
    }
  }

  private void createNewFunction ()
  {
    JTable           table  = getTable ();
    String           functionDef = "";
    CompiledFunction function = null;

    if (table instanceof JeksTable)
      while (function == null)
        try
        {
          if ((functionDef = (String)JOptionPane.showInputDialog (JeksFrame.this, resourceBundle.getString ("NEW_FUNCTION_DIALOG_TEXT"), resourceBundle.getString ("NEW_FUNCTION_DIALOG_TITLE"), JOptionPane.PLAIN_MESSAGE,
                                                                  null, null, functionDef)) == null)
            return;
          function = ((JeksTable)table).getExpressionParser ().getFunctionParser ().compileFunction (functionDef);
          ((JeksTable)table).getExpressionParser ().addUserFunction (function);
          tableChanged = true;
        }
        catch (CompilationException ex)
        {
          JeksFunctionSyntax syntax = (JeksFunctionSyntax)((JeksTable)table).getExpressionParser ().getFunctionParser ().getSyntax ();
          String errorMessage = syntax.getExceptionMessage (ex);
          JOptionPane.showMessageDialog (JeksFrame.this, errorMessage, syntax.getMessage (JeksExpressionSyntax.MESSAGE_COMPILATION_ERROR_TITLE), JOptionPane.ERROR_MESSAGE);
        }
  }

  /**
   * Main method to launch Jeks.
   */
  public static void main (String args []) throws Exception
  {
    // First display splash screen
    URL splashImageURL = ClassLoader.getSystemResource ("com/eteks/jeks/resources/jeksscreen.jpg");
    SplashScreenWindow splashScreen = new SplashScreenWindow (Toolkit.getDefaultToolkit ().getImage (splashImageURL), 5000);

    // Change Look and Feel to current platform
    try
    {
      // UIManager.setLookAndFeel (UIManager.getCrossPlatformLookAndFeelClassName());
      UIManager.setLookAndFeel (UIManager.getSystemLookAndFeelClassName());
    }
    catch (Exception e)
    {
      System.err.println ("Error loading L&F: " + e);
    }

    // Create frame and wait splash screen end
    JFrame frame = new JeksFrame ();
    splashScreen.join ();

    frame.setVisible (true);
  }
}
