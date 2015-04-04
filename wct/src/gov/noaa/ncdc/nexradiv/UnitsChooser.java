/**
 * NOAA's National Climatic Data Center
 * NOAA/NESDIS/NCDC
 * 151 Patton Ave, Asheville, NC  28801
 * 
 * THIS SOFTWARE AND ITS DOCUMENTATION ARE CONSIDERED TO BE IN THE 
 * PUBLIC DOMAIN AND THUS ARE AVAILABLE FOR UNRESTRICTED PUBLIC USE.  
 * THEY ARE FURNISHED "AS IS." THE AUTHORS, THE UNITED STATES GOVERNMENT, ITS
 * INSTRUMENTALITIES, OFFICERS, EMPLOYEES, AND AGENTS MAKE NO WARRANTY,
 * EXPRESS OR IMPLIED, AS TO THE USEFULNESS OF THE SOFTWARE AND
 * DOCUMENTATION FOR ANY PURPOSE. THEY ASSUME NO RESPONSIBILITY (1)
 * FOR THE USE OF THE SOFTWARE AND DOCUMENTATION; OR (2) TO PROVIDE
 * TECHNICAL SUPPORT TO USERS.
 */

package gov.noaa.ncdc.nexradiv;

// J2SE dependencies
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.MutableComboBoxModel;

import org.geotools.resources.SwingUtilities;
import org.geotools.resources.Utilities;



/**
 */
public class UnitsChooser extends JPanel {


   public final static String[] units = new String[] { 
      "Kilometers", 
      "Meters", 
      "Miles", 
      "Nautical Mi.",
      "Feet",
      "Kilofeet"
   };
   public final static String[] unitsAbbreviation = new String[] { 
      "km", 
      "m", 
      "mi", 
      "nmi",
      "ft",
      "kft"
   };

   private static String defaultUnits = "Kilometers";

   private final JComboBox choices = new JComboBox();


   public UnitsChooser() throws IllegalArgumentException {
      this(defaultUnits);
   }

   public UnitsChooser(String defaultUnits) throws IllegalArgumentException {
        super(new GridBagLayout());
        this.defaultUnits = defaultUnits;
        if (units != null) {
            final MutableComboBoxModel model = (MutableComboBoxModel) choices.getModel();
            for (int i=0; i<units.length; i++) {
                model.addElement(units[i]);
            }
        }
        choices.setEditable(true); // Must be invoked before 'setFormat'.
        choices.setSelectedItem(defaultUnits);

        final GridBagConstraints c = new GridBagConstraints();
        c.gridx=0; c.insets.right=6;
        c.gridy=0; add(new JLabel("Units"), c);
        c.insets.right=0; c.gridx++; c.weightx=1; c.fill=c.HORIZONTAL;
        c.gridy=0; add(choices, c);
        choices.getEditor().getEditorComponent().requestFocus();
        choices.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent event) {
               ;
            }
        });
    }

    
    public String getUnits() {
       return (String)(choices.getSelectedItem());
    }
    
    public String getUnitsAbbreviation() {
       int index = choices.getSelectedIndex();
       return unitsAbbreviation[index];
    }
    
    
    /**
     * Shows a dialog box requesting input from the user. The dialog box will be
     * parented to <code>owner</code>. If <code>owner</code> is contained into a
     * {@link javax.swing.JDesktopPane}, the dialog box will appears as an internal
     * frame. This method can be invoked from any thread (may or may not be the
     * <i>Swing</i> thread).
     *
     * @param  owner The parent component for the dialog box,
     *         or <code>null</code> if there is no parent.
     * @param  title The dialog box title.
     * @return <code>true</code> if user pressed the "Ok" button, or
     *         <code>false</code> otherwise (e.g. pressing "Cancel"
     *         or closing the dialog box from the title bar).
     */
    public boolean showDialog(final Component owner, final String title) {
        while (SwingUtilities.showOptionDialog(owner, this, title)) {
           return true;
        }
        return false;
    }

    /**
     * Show this component. This method is used mostly in order
     * to check the look of this widget from the command line.
     */
    public static void main(final String[] args) {
        new UnitsChooser()
            .showDialog(null, Utilities.getShortName(UnitsChooser.class));
        System.exit(0);
    }
}

