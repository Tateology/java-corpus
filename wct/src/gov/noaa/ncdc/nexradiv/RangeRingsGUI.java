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

import gov.noaa.ncdc.wct.export.vector.WCTVectorExport;
import gov.noaa.ncdc.wct.ui.WCTUiInterface;
import gov.noaa.ncdc.wct.ui.WCTViewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class RangeRingsGUI extends JDialog implements ActionListener {

   
   private JTabbedPane tabPane;
   private JComboBox ringMinDistCombo, ringMaxDistCombo;
   private JComboBox spokeMinAzimuthCombo, spokeMaxAzimuthCombo;
   private JComboBox ringIncrementCombo, spokeIncrementCombo;
   private JComboBox ringUnitsCombo, spokeUnitsCombo;
   private JComboBox jcomboSize;
   private JButton jbColor;
   private JCheckBox jcbLabel;

   private JButton jbLoad, jbClear, jbExport;
   
   private boolean isVisible = false;
   
   private WCTViewer viewer;
   
   public RangeRingsGUI(Frame parent, WCTViewer viewer) {
      this(parent, "Range Rings", viewer);   
   }  
   
   public RangeRingsGUI(Frame parent, String title) {
      this(parent, title, null);
   }      
   
   public RangeRingsGUI(Frame parent, String title, WCTViewer viewer) {      
      super(parent, title, false);
      this.viewer = viewer;
      createGUI();
   }

   private void createGUI() {
      tabPane = new JTabbedPane();
      
      //------------------------------------------------------------------------------
      // Range Rings Tab
      //------------------------------------------------------------------------------
      
      JPanel ringPanel = new JPanel();
      ringPanel.setLayout(new BorderLayout());
      ringPanel.add(new JLabel("Range Rings", JLabel.CENTER), BorderLayout.NORTH);
      ringMinDistCombo = new JComboBox(new Object[] { "10.0", "25.0", "50.0" });
      ringMinDistCombo.setEditable(true);
      ringMaxDistCombo = new JComboBox(new Object[] { "100.0", "150.0", "230.0" });
      ringMaxDistCombo.setEditable(true);
      ringMaxDistCombo.setSelectedItem("230.0");
      ringIncrementCombo = new JComboBox(new Object[] { "5.0", "10.0", "25.0", "50.0" });
      ringIncrementCombo.setEditable(true);
      ringIncrementCombo.setSelectedItem("25.0");
      ringUnitsCombo = new JComboBox(new Object[] { "Kilometers", "Miles", "Nautical Mi." });

      JPanel ringLabelPanel = new JPanel();
      ringLabelPanel.setLayout(new GridLayout(4, 1));
      ringLabelPanel.add(new JLabel("Minimum Range: "));
      ringLabelPanel.add(new JLabel("Maximum Range: "));
      ringLabelPanel.add(new JLabel("Ring Increment: "));
      ringLabelPanel.add(new JLabel("Units: "));

      JPanel ringComboPanel = new JPanel();
      ringComboPanel.setLayout(new GridLayout(4, 1));
      ringComboPanel.add(ringMinDistCombo);
      ringComboPanel.add(ringMaxDistCombo);
      ringComboPanel.add(ringIncrementCombo);
      ringComboPanel.add(ringUnitsCombo);
      
      ringPanel.add(ringLabelPanel, BorderLayout.WEST);
      ringPanel.add(ringComboPanel, BorderLayout.EAST);

      //------------------------------------------------------------------------------
      // Azimuth Spokes Tab
      //------------------------------------------------------------------------------

      JPanel spokePanel = new JPanel();
      spokePanel.setLayout(new BorderLayout());
      spokePanel.add(new JLabel("Azimuth Spokes", JLabel.CENTER), BorderLayout.NORTH);

      spokeMinAzimuthCombo = new JComboBox(new Object[] { "0.0", "90.0", "135.0" });
      spokeMinAzimuthCombo.setEditable(true);
      spokeMaxAzimuthCombo = new JComboBox(new Object[] { "360.0", "270.0", "225.0" });
      spokeMaxAzimuthCombo.setEditable(true);
      spokeIncrementCombo = new JComboBox(new Object[] { "10.0", "15.0", "22.5", "30.0", "45.0" });
      spokeIncrementCombo.setEditable(true);
      spokeIncrementCombo.setSelectedItem("30.0");
      spokeUnitsCombo = new JComboBox(new Object[] { "Degrees" });

      JPanel spokeLabelPanel = new JPanel();
      spokeLabelPanel.setLayout(new GridLayout(4, 1));
      spokeLabelPanel.add(new JLabel("Start Azimuth: "));
      spokeLabelPanel.add(new JLabel("End Azimuth: "));
      spokeLabelPanel.add(new JLabel("Spoke Increment: "));
      spokeLabelPanel.add(new JLabel("Units: "));

      JPanel spokeComboPanel = new JPanel();
      spokeComboPanel.setLayout(new GridLayout(4, 1));
      spokeComboPanel.add(spokeMinAzimuthCombo);
      spokeComboPanel.add(spokeMaxAzimuthCombo);
      spokeComboPanel.add(spokeIncrementCombo);
      spokeComboPanel.add(spokeUnitsCombo);
      
      spokePanel.add(spokeLabelPanel, BorderLayout.WEST);
      spokePanel.add(spokeComboPanel, BorderLayout.EAST);


      //------------------------------------------------------------------------------
      // Common stuff
      //------------------------------------------------------------------------------
      JPanel commonPanel = new JPanel();
      commonPanel.setLayout(new BorderLayout());
      jbColor = new JButton(" ");
      jbColor.setBackground(new Color(150, 150, 150));
      jbColor.addActionListener(this);
      jcomboSize = new JComboBox(new Object[] { "1", "2", "3", "4" });
      jcbLabel = new JCheckBox();
      
      JPanel commonColorPanel = new JPanel();
      commonColorPanel.add(new JLabel("Color: "));
      commonColorPanel.add(jbColor);
      
      JPanel commonSizePanel = new JPanel();
      commonSizePanel.add(new JLabel("Size: "));
      commonSizePanel.add(jcomboSize);
      
      JPanel commonLabelPanel = new JPanel();
      commonLabelPanel.add(new JLabel("Label: "));
      commonLabelPanel.add(jcbLabel);

      JPanel commonOptionPanel = new JPanel();
      commonOptionPanel.setLayout(new GridLayout(3, 1));
      commonOptionPanel.add(commonColorPanel);
      commonOptionPanel.add(commonSizePanel);
      commonOptionPanel.add(commonLabelPanel);
      
      commonPanel.add(commonOptionPanel, BorderLayout.CENTER);
      
      //------------------------------------------------------------------------------
      // Finish it up!
      //------------------------------------------------------------------------------

      jbExport = new JButton("Export");
      jbLoad = new JButton("Load");
      jbClear = new JButton("Clear");
      if (viewer != null && viewer.getWCTType() == WCTUiInterface.VIEWER) {
         jbExport.addActionListener(this);
         jbLoad.addActionListener(this);
         jbClear.addActionListener(this);
      }
      JPanel mainButtonPanel = new JPanel();
      mainButtonPanel.add(jbExport);
      mainButtonPanel.add(jbLoad);
      mainButtonPanel.add(jbClear);

      if (viewer != null && viewer.getWCTType() == WCTUiInterface.VIEWER) {
         commonPanel.add(mainButtonPanel, BorderLayout.SOUTH);
      }
      
      
      tabPane.addTab("Rings", ringPanel);
      tabPane.addTab("Spokes", spokePanel);
      getContentPane().setLayout(new BorderLayout());
      getContentPane().add(tabPane, "Center");
      getContentPane().add(commonPanel, BorderLayout.SOUTH);
      
      
   }
   
   
   public void loadRangeRings() {
      try {
         if (! isVisible) {
            return;
         }
         
         // convert to selected units
         double minDistance = Double.parseDouble(((String)ringMinDistCombo.getSelectedItem()));
         double maxDistance = Double.parseDouble(((String)ringMaxDistCombo.getSelectedItem()));
         double ringIncrement = Double.parseDouble(((String)ringIncrementCombo.getSelectedItem()));
         double startAzimuth = Double.parseDouble(((String)spokeMinAzimuthCombo.getSelectedItem()));
         double endAzimuth = Double.parseDouble(((String)spokeMaxAzimuthCombo.getSelectedItem()));
         double spokeIncrement = Double.parseDouble(((String)spokeIncrementCombo.getSelectedItem()));
         int size = Integer.parseInt((String)(jcomboSize.getSelectedItem()));
         boolean label = jcbLabel.isSelected();
         int distUnits = getDistanceUnits();

         
         ((WCTViewer)viewer).setRangeRings(minDistance, maxDistance, ringIncrement, distUnits, 
            startAzimuth, endAzimuth, spokeIncrement, jbColor.getBackground(), size, label);
      } catch (Exception e) {
         javax.swing.JOptionPane.showMessageDialog(null, "Range Rings Loading Error: "+e, 
            "RANGE RING ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);         
      }
   }
   
   
   public void actionPerformed(ActionEvent evt) {
      Object source = evt.getSource();
      if (source == jbLoad) {
    
         isVisible = true;
         loadRangeRings();

      }
      else if (source == jbClear) {
         isVisible = false;
         viewer.setRangeRings(-1.0, -1.0, 0.0, RangeRings.KM, jbColor.getBackground(), 1, false);
      }
      else if (source == jbExport) {
         // First load RangeRings in viewer
         
         // convert to selected units
         double minDistance = Double.parseDouble(((String)ringMinDistCombo.getSelectedItem()));
         double maxDistance = Double.parseDouble(((String)ringMaxDistCombo.getSelectedItem()));
         double ringIncrement = Double.parseDouble(((String)ringIncrementCombo.getSelectedItem()));
         double startAzimuth = Double.parseDouble(((String)spokeMinAzimuthCombo.getSelectedItem()));
         double endAzimuth = Double.parseDouble(((String)spokeMaxAzimuthCombo.getSelectedItem()));
         double spokeIncrement = Double.parseDouble(((String)spokeIncrementCombo.getSelectedItem()));
         int size = Integer.parseInt((String)(jcomboSize.getSelectedItem()));
         boolean label = jcbLabel.isSelected();
         int distUnits = getDistanceUnits();

         
         viewer.setRangeRings(minDistance, maxDistance, ringIncrement, distUnits, 
            startAzimuth, endAzimuth, spokeIncrement, jbColor.getBackground(), size, label);
            
         exportRangeRings();
      }
      
      
      
      else if (source == jbColor) {
         Color newColor = JColorChooser.showDialog(RangeRingsGUI.this,
                          "Choose Map Layer Color",
                          jbColor.getBackground());
         if (newColor != null) {
            jbColor.setBackground(newColor);
            //nexview.setBackgroundColor(jbBackground.getBackground());
         }
         return;
      }
   }
   
   
   public void setRangeRingsVisible(boolean isVisible) {
      this.isVisible = isVisible;
   }
   
   public boolean isRangeRingsVisible() {
      return isVisible;
   }
   
   
   private int getDistanceUnits() {
      if (((String)ringUnitsCombo.getSelectedItem()).equals("Kilometers")) {
         return RangeRings.KM;
      }
      else if (((String)ringUnitsCombo.getSelectedItem()).equals("Miles")) {
         return RangeRings.MILES;
      }
      else if (((String)ringUnitsCombo.getSelectedItem()).equals("Nautical Mi.")) {
         return RangeRings.NAUTICAL_MI;
      }
      else {
         javax.swing.JOptionPane.showMessageDialog(null, 
         "Incorrect units specified: "+ ((String)ringUnitsCombo.getSelectedItem()), 
         "RANGE RING UNITS ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);
         return RangeRings.KM;
      }
   }
   
   

   private void exportRangeRings() {
      try {
         // Set up File Chooser
         JFileChooser fc = new JFileChooser();
         //fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
         fc.setDialogTitle("Save Range Rings");
         OpenFileFilter shpFilter = new OpenFileFilter("shp", true, "Shapefile");
         //OpenFileFilter wktFilter = new OpenFileFilter("wkt", true, "Well-Known Text");
         fc.addChoosableFileFilter(shpFilter);
         //fc.addChoosableFileFilter(wktFilter);
         fc.setAcceptAllFileFilterUsed(false);
         fc.setFileFilter(shpFilter);
            
         int returnVal = fc.showSaveDialog(viewer);
         File file = null;
         if (returnVal == JFileChooser.APPROVE_OPTION) {
            int choice = JOptionPane.YES_OPTION;
            
            // intialize to YES!
            file = fc.getSelectedFile();
            File extfile = null;
            String fstr = file.toString();

         /*
         // Add extension if needed
         if (!fstr.substring((int) fstr.length() - 4, (int) fstr.length()).equals(".jnx")) {
            extfile = new File(file + ".jnx");
         }
         else {
            extfile = file;
            file = new File(fstr.substring(0, (int) fstr.length() - 4));
         }
         */
         
            // Check for existing file
            if (file.exists()) {
               String message = "<html><font color=red>" + file + "</font></html>\n" +
                  "already exists.\n\n" +
                  "Do you want to proceed and OVERWRITE?";
               choice = JOptionPane.showConfirmDialog(null, (Object) message,
                  "OVERWRITE PROJECT FILE", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
               fstr = file.toString();
               file = new File(fstr.substring(0, (int) fstr.length() - 4));
            }
            // END f(file.exists())
            // Check for existing file without extension
            /*
            else if (extfile.exists()) {
               String message = "<html><font color=red>" + extfile + "</font></html>\n" +
                  "already exists.\n\n" +
                  "Do you want to proceed and OVERWRITE?";
               choice = JOptionPane.showConfirmDialog(null, (Object) message,
                  "OVERWRITE PROJECT FILE", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
            }
            // END else if(extfile.exists())
            */
            
            
            if (choice == JOptionPane.YES_OPTION) {
            
               if (fc.getFileFilter() == shpFilter) {
                  try {
                     WCTVectorExport vectorExport = new WCTVectorExport();
                     vectorExport.saveShapefile(file, viewer.getRangeRings(), RangeRings.getRangeRingFeatureType());
                  } catch (Exception e) {
                     e.printStackTrace();
                     String message = "Error writing \n" +
                        "<html><font color=red>" + file + "</font></html>";
                     JOptionPane.showMessageDialog(null, (Object) message,
                        "RANGE RING EXPORT ERROR", JOptionPane.ERROR_MESSAGE);
                  }
               }
               //else if (fc.getFileFilter() == wktFilter) {
                  //NexradVectorExport.saveWKT
               //}
            }
            // END if(choice == YES_OPTION)
         }
            
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
   
   
   
   public static void main(String[] args) {
   }

}
