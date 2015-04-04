package gov.noaa.ncdc.common;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;


/**
 * Perform Swing-related operations.
 */
public final class SwingManipulator
{
    /**
    * Private constructor that should never be called.
    */
    private SwingManipulator()
    {}


    /**
    * Add a standard editing popup menu (Cut, Copy, Paste, Select All)
    * to the specified text fields.
    * This method must run on the EDT.
    *
    * @param fields
    *      array of JTextField's for which to add the popup menu
    */
    public static void addStandardEditingPopupMenu(
            final JTextComponent[] fields)
    {
        final JPopupMenu popupMenu = new JPopupMenu();

        /* text fields popup menu: "Cut" */
        final JMenuItem cutMenuItem = new JMenuItem("Cut", 't');
        cutMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final Component c = popupMenu.getInvoker();

                if (c instanceof JTextComponent)
                {
                    ((JTextComponent) c).cut();
                }
            }
        });
        popupMenu.add(cutMenuItem);

        /* text fields popup menu: "Copy" */
        final JMenuItem copyMenuItem = new JMenuItem("Copy", 'C');
        copyMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final Component c = popupMenu.getInvoker();

                if (c instanceof JTextComponent)
                {
                    ((JTextComponent) c).copy();
                }
            }
        });
        popupMenu.add(copyMenuItem);

        /* text fields popup menu: "Paste" */
        final JMenuItem pasteMenuItem = new JMenuItem("Paste", 'P');
        pasteMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final Component c = popupMenu.getInvoker();

                if (c instanceof JTextComponent)
                {
                    ((JTextComponent) c).paste();
                }
            }
        });
        popupMenu.add(pasteMenuItem);
        popupMenu.addSeparator();

        /* text fields popup menu: "Select All" */
        final JMenuItem selectAllMenuItem = new JMenuItem("Select All", 'A');
        selectAllMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final Component c = popupMenu.getInvoker();

                if (c instanceof JTextComponent)
                {
                    ((JTextComponent) c).selectAll();
                }
            }
        });
        popupMenu.add(selectAllMenuItem);

        /* add mouse listeners to the specified fields */
        for (final JTextComponent f : fields)
        {
            f.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mousePressed(MouseEvent e)
                {
                    processMouseEvent(e);
                }

                @Override
                public void mouseReleased(MouseEvent e)
                {
                    processMouseEvent(e);
                }

                private void processMouseEvent(MouseEvent e)
                {
                    if (e.isPopupTrigger())
                    {
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                        popupMenu.setInvoker(f);
                    }
                }
            });
        }
    }


    /**
    * Wrapper for the getText() method of a JTextField that always returns a String.
    * This method must run on the EDT.
    *
    * @param f
    *      JTextField object on which to call getText()
    * @return
    *      String text in the JTextField
    */
    public static String getTextJTextField(
            final JTextField f)
    {
        try
        {
            return f.getText();
        }
        catch (NullPointerException e)
        {
            return "";
        }
    }


    /**
    * Wrapper for the getPassword() method of a JPasswordField that always returns a char array.
    * This method must run on the EDT.
    *
    * @param f
    *      JPasswordField object on which to call getPassword()
    * @return
    *      char array representing the text in the JPasswordField
    */
    public static char[] getPasswordJPasswordField(
            final JPasswordField f)
    {
        try
        {
            return f.getPassword();
        }
        catch (NullPointerException e)
        {
            return new char[0];
        }
    }


    /**
    * Update progress bar.
    * This method can be called on any thread.
    *
    * @param progress
    *     progress bar to be updated
    * @param text
    *     text string on the progress bar
    * @param percent
    *     percentage of the task completed (if less than 0 or more than 100,
    *     then indeterminate mode is used)
    */
    public static void updateProgressBar(
            final JProgressBar progress,
            final String text,
            final int percent)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                if ((percent >= 0) && (percent <= 100))
                {
                    /* determinate mode */
                    progress.setValue(percent);
                    progress.setIndeterminate(false);

                    if (percent < 100)
                    {
                        progress.setString(String.format("%s (%d%%)", text, percent));
                    }
                    else
                    {
                        progress.setString(text);
                    }
                }
                else
                {
                    /* indeterminate mode */
                    progress.setString(text);
                    progress.setIndeterminate(true);
                }
            }
        });
    }


    /**
    * Update label.
    * This method can be called on any thread.
    *
    * @param label
    *     label to be updated
    * @param text
    *     new text on the label
    */
    public static void updateLabel(
            final JLabel label,
            final String text)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                label.setText(text);
            }
        });
    }


    /**
    * Set enabled state of a button.
    * This method can be called on any thread.
    *
    * @param button
    *     button whose state is to be modified
    * @param enabled
    *     new enabled state of the button
    */
    public static void setEnabledButton(
            final AbstractButton button,
            final boolean enabled)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                button.setEnabled(enabled);
            }
        });
    }


    /**
    * Set visible state of a window.
    * This method can be called on any thread.
    *
    * @param window
    *     window whose state is to be modified
    * @param visible
    *     new visible state of the window
    */
    public static void setVisibleWindow(
            final Window window,
            final boolean visible)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                window.setVisible(visible);
            }
        });
    }


    /**
    * Convenience method to display a JOptionPane modal option dialog with
    * a label and text area.
    * This method can be called on any thread.
    *
    * @param parentComponent
    *     Frame in which the dialog is to be displayed; if null, or if the
    *     parentComponent has no Frame, then a default Frame is used
    * @param label
    *     Label String to be displayed
    * @param text
    *     Text String to be displayed in the text area
    * @param rows
    *     Height of the text area in number of rows
    * @param title
    *     Title String for the dialog
    * @param optionType
    *     Options available for the dialog: JOptionPane.DEFAULT_OPTION,
    *     YES_NO_OPTION, YES_NO_CANCEL_OPTION, or OK_CANCEL_OPTION
    * @param messageType
    *     Type of message; primarily used to determine the icon from the
    *     pluggable Look and Feel: JOptionPane.ERROR_MESSAGE,
    *     INFORMATION_MESSAGE, WARNING_MESSAGE, QUESTION_MESSAGE,
    *     or PLAIN_MESSAGE
    * @param icon
    *     Icon to be displayed
    * @param Object[] options
    *     Array of objects indicating the possible choices the user can make;
    *     if the objects are components, they are rendered properly;
    *     non-String objects are rendered using their toString methods;
    *     if this parameter is null, the options are determined by the
    *     Look and Feel
    * @param initialValue
    *      Index of the default selection for the dialog; meaningful
    *      only if options is used; ignored if negative
    * @return
    *      Index of the option chosen by the user, or JOptionPane.CLOSED_OPTION
    *      if the user closed the dialog
    * @throw HeadlessException
    *     If GraphicsEnvironment.isHeadless returns true
    */
    public static int showModalOptionTextDialog(
            final Component parentComponent,
            final String label,
            final String text,
            final int rows,
            final String title,
            final int optionType,
            final int messageType,
            final Icon icon,
            final Object[] options,
            final int initialValue)
            throws HeadlessException
    {
        /* determine intial option */
        final Object initialOption;

        if ((options != null) && (initialValue >= 0) && (initialValue < options.length))
        {
            initialOption = options[initialValue];
        }
        else
        {
            initialOption = null;
        }

        final Debug.ValueCapsule<Integer> choice = new Debug.ValueCapsule<Integer>();

        final Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                final JPanel panel = new JPanel(new BorderLayout());
                panel.add(new JLabel(label + ":"), BorderLayout.NORTH);

                final JTextArea textArea = new JTextArea(text, rows, 50);
                textArea.setEditable(false);
                textArea.setWrapStyleWord(true);
                textArea.setLineWrap(true);
                textArea.setToolTipText(label);
                textArea.setFont(new Font(
                        Font.DIALOG,
                        Font.PLAIN,
                        textArea.getFont().getSize() - 2));

                SwingManipulator.addStandardEditingPopupMenu(new JTextArea[] {textArea});

                panel.add(new JScrollPane(
                        textArea,
                        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),
                        BorderLayout.CENTER);

                choice.set(JOptionPane.showOptionDialog(
                        parentComponent,
                        panel,
                        title,
                        optionType,
                        messageType,
                        icon,
                        options,
                        initialOption));
            }
        };

        if (SwingUtilities.isEventDispatchThread())
        {
            r.run();
        }
        else
        {
            SwingUtilities.invokeLater(r);
        }

        return choice.get();
    }


    /**
    * Convenience method to create a modeless JDialog that wraps a JOptionPane
    * option dialog with a label and text area.
    * This method must be called on the EDT.
    *
    * @param parentComponent
    *     Frame in which the dialog is to be displayed; if null, or if the
    *     parentComponent has no Frame, then a default Frame is used
    * @param label
    *     Label String to be displayed
    * @param text
    *     Text String to be displayed in the text area
    * @param rows
    *     Height of the text area in number of rows
    * @param title
    *     Title String for the dialog
    * @param optionType
    *     Options available for the dialog: JOptionPane.DEFAULT_OPTION,
    *     YES_NO_OPTION, YES_NO_CANCEL_OPTION, or OK_CANCEL_OPTION
    * @param messageType
    *     Type of message; primarily used to determine the icon from the
    *     pluggable Look and Feel: JOptionPane.ERROR_MESSAGE,
    *     INFORMATION_MESSAGE, WARNING_MESSAGE, QUESTION_MESSAGE,
    *     or PLAIN_MESSAGE
    * @param icon
    *     Icon to be displayed
    * @param Object[] options
    *     Array of objects indicating the possible choices the user can make;
    *     if the objects are components, they are rendered properly;
    *     non-String objects are rendered using their toString methods;
    *     if this parameter is null, the options are determined by the
    *     Look and Feel
    * @param initialValue
    *      Index of the default selection for the dialog; meaningful
    *      only if options is used; ignored if negative
    * @return
    *      new modeless JDialog wrapping a JOptionPane option dialog
    * @throw HeadlessException
    *     If GraphicsEnvironment.isHeadless returns true
    */
    public static JDialog createModelessOptionTextDialog(
            final Component parentComponent,
            final String label,
            final String text,
            final int rows,
            final String title,
            final int optionType,
            final int messageType,
            final Icon icon,
            final Object[] options,
            final int initialValue)
            throws HeadlessException
    {
        /* determine intial option */
        final Object initialOption;

        if ((options != null) && (initialValue >= 0) && (initialValue < options.length))
        {
            initialOption = options[initialValue];
        }
        else
        {
            initialOption = null;
        }

        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(label + ":"), BorderLayout.NORTH);

        final JTextArea textArea = new JTextArea(text, rows, 50);
        textArea.setEditable(false);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setToolTipText(label);
        textArea.setFont(new Font(
                Font.DIALOG,
                Font.PLAIN,
                textArea.getFont().getSize() - 2));

        SwingManipulator.addStandardEditingPopupMenu(new JTextArea[] {textArea});

        panel.add(new JScrollPane(
                textArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),
                BorderLayout.CENTER);

        final JDialog d = new JOptionPane(
                panel,
                messageType,
                optionType,
                icon,
                options,
                initialOption)
                .createDialog(parentComponent, title);

        d.setModalityType(ModalityType.MODELESS);
        d.setResizable(true);
        return d;
    }


    /**
    * Display a modal error dialog.
    * This method can be called on any thread.
    *
    * @param parent
    *     Parent component of this dialog
    * @param title
    *     Title of dialog
    * @param message
    *     Error message to be displayed
    */
    public static void showErrorDialog(
                final Component parent,
                final String title,
                final String message)
    {
        showModalOptionTextDialog(
                parent,
                "An error has occurred",
                message,
                5,
                title,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                null,
                0);
    }


    /**
    * Display a modal warning dialog.
    * This method can be called on any thread.
    *
    * @param parent
    *     Parent component of this dialog
    * @param title
    *     Title of dialog
    * @param message
    *     Warning message to be displayed
    */
    public static void showWarningDialog(
                final Component parent,
                final String title,
                final String message)
    {
        showModalOptionTextDialog(
                parent,
                "A warning has been issued",
                message,
                5,
                title,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                null,
                0);
    }


    /**
    * Display a modal information dialog.
    * This method can be called on any thread.
    *
    * @param parent
    *     Parent component of this dialog
    * @param title
    *     Title of dialog
    * @param label
    *     Label string to be displayed
    * @param message
    *     Information message to be displayed
    * @param rows
    *     Height of the text area in number of rows
    */
    public static void showInfoDialog(
                final Component parent,
                final String title,
                final String label,
                final String message,
                final int rows)
    {
        showModalOptionTextDialog(
                parent,
                label,
                message,
                rows,
                title,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                null,
                0);
    }


    /**
    * Create a new modeless information dialog.
    * This method must be called on the EDT.
    *
    * @param parent
    *     Parent component of this dialog
    * @param title
    *     Title of dialog
    * @param label
    *     Label string to be displayed
    * @param message
    *     Information message to be displayed
    * @param rows
    *     Height of the text area in number of rows
    * @return
    *     new modeless information dialog
    */
    public static JDialog createModelessInfoDialog(
                final Component parent,
                final String title,
                final String label,
                final String message,
                final int rows)
    {
        return createModelessOptionTextDialog(
                parent,
                label,
                message,
                rows,
                title,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                null,
                0);
    }
}