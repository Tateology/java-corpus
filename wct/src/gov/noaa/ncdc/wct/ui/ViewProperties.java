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

package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.WCTProperties;
import gov.noaa.ncdc.wct.decoders.nexrad.ExtraRadarSiteListManager;
import gov.noaa.ncdc.wct.decoders.nexrad.WCTProjections;
import gov.noaa.ncdc.wct.io.WCTTransfer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.io.FileUtils;
import org.geotools.cs.CoordinateSystemFactory;
import org.jdesktop.swingx.JXHyperlink;
import org.jfree.ui.FontChooserDialog;


public class ViewProperties extends JDialog implements ActionListener, ChangeListener {

   private JPanel choicePanel, rasterPanel, projPanel, geoPanel, distPanel, displayPanel, legendPanel, smoothPanel, cachePanel;
   private JTabbedPane tabbedPane;
   private WCTViewer viewer;     
   private JRadioButton jrbLatLon, jrbAlbers, jrbStereo, jrbHRAP, jrbRadar, jrbDMS, jrbDeci;
   private JRadioButton jrbMiles, jrbKM, jrbNauticalMi;
   
   private JCheckBox jcbAddLogo, jcbAutoResolution, jcbEngageSmoothing, jcbEngagePowerSmoothing, jcbUseCache;
   private JButton jbManualRefresh;
   private JLabel jlCacheSize;
   private JButton jbClearCache;
   
   private JButton jbCancel, jbOK, jbApply;
   private JButton jbLegendFont, jbLegendFGColor, jbLegendBGColor;
   private JComboBox jcomboGridSize, jcomboLegendFont;
   private JSpinner smoothFactorSpinner, downsampleRaysSpinner, downsampleGatesSpinner;

   private FontChooserDialog fontChooser;
   
   private JCheckBox jcbRasterVariableRes, jcbAnimationVariable;
   private JTextField jtfRasterNoData;

   private final JTextField jtfCacheLocation = new JTextField(WCTConstants.getInstance().getDataCacheLocation());
   private final JTextField jtfMaxCacheSize = new JTextField(String.valueOf((long)(WCTConstants.getInstance().getDataCacheSizeLimit()/(1024L*1024L))));

   
   // Factory to create coordinate systems from WKT strings
   private CoordinateSystemFactory csFactory = CoordinateSystemFactory.getDefault();
   private WCTProjections wCTProjections = new WCTProjections();
   
   
   private boolean displayChange = false;
   private boolean rasterChange = false;
   private boolean legendChange = false;
   private boolean projChange = false;
   private boolean geoChange = false;
   private boolean distChange = false;
   private boolean smoothChange = false;
   private boolean cacheChange = false;
   private boolean downsampleChange = false;
   
   public ViewProperties(WCTViewer nexview) {
      super(nexview, "View Properties", false);
      this.viewer=nexview;
      createGUI();
//      pack();
//      setVisible(true);
   } 

   private void createGUI() {

      jcbAutoResolution = new JCheckBox("Data Auto-Refresh", true);
      jcbAutoResolution.addActionListener(this);
      jbManualRefresh = new JButton("Manual Data Refresh");
      jbManualRefresh.addActionListener(this);      
      jcbAddLogo = new JCheckBox("Add NOAA Logo", true);
      jcbAddLogo.addActionListener(this);
      
      

      jcbEngageSmoothing = new JCheckBox("NEXRAD Smoothing", false);
      jcbEngageSmoothing.addActionListener(this);
      jcbEngagePowerSmoothing = new JCheckBox("Use Reflectivity Power (not dBZ)", false);
      jcbEngagePowerSmoothing.addActionListener(this);
      smoothFactorSpinner = new JSpinner(new SpinnerNumberModel(10, 0, 50, 1));
      smoothFactorSpinner.addChangeListener(this);
      
      // set up raster properties defaults
      jcomboGridSize = new JComboBox(new Object[] {"400", "600", "800", "1000", "1200", "1400", "1600"});
      jcbRasterVariableRes = new JCheckBox();
      jtfRasterNoData = new JTextField();
      jcbAnimationVariable = new JCheckBox();
      jcomboGridSize.setSelectedItem("800");
      jcomboGridSize.setEditable(true);
      jcbRasterVariableRes.setSelected(true);
      jtfRasterNoData.setText("-999.0");
      
      // Add action listeners      
      jcomboGridSize.addActionListener(this);           
      jcbRasterVariableRes.addActionListener(this);           
      jtfRasterNoData.addActionListener(this);  
      jcbAnimationVariable.addActionListener(this);
   
      JPanel rasterPanel1 = new JPanel();
      JPanel rasterPanel2 = new JPanel();
      JPanel rasterPanel3 = new JPanel();
      JPanel rasterPanel4 = new JPanel();
      rasterPanel1.setLayout(new GridLayout(1, 2));
      rasterPanel1.add(new JLabel("Square Grid Size: "));
      rasterPanel1.add(jcomboGridSize);
      rasterPanel2.setLayout(new GridLayout(1, 2));
      rasterPanel2.add(new JLabel("Variable Resolution: "));
      rasterPanel2.add(jcbRasterVariableRes);
      rasterPanel3.setLayout(new GridLayout(1, 2));
      rasterPanel3.add(new JLabel("No Data Value: "));
      rasterPanel3.add(jtfRasterNoData);           
      rasterPanel4.setLayout(new GridLayout(1, 2));
      //rasterPanel4.add(new JLabel("Animation Auto-Resolution: "));
      //rasterPanel4.add(jcbAnimationVariable);
      rasterPanel1.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
      rasterPanel2.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
      rasterPanel3.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
      rasterPanel4.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            
      // set up legend properties defaults
      jcomboLegendFont = new JComboBox(new Object[] {"9", "10", "11", "12", "13"});
      jbLegendFont = new JButton("Edit Font");
      jbLegendFGColor = new JButton();
      jbLegendBGColor = new JButton();
      jbLegendFGColor.setBackground(viewer.getLegendFGColor());
      jbLegendBGColor.setBackground(viewer.getLegendBGColor());
      jcomboLegendFont.setSelectedItem("11");
      jcomboLegendFont.setEditable(true);
      
      // BELOW -- FONTCHOOSER FROM SALSA
      //String[] names = new String[] { "Times", "Courier", "Serif", "Sans" };
      //String[] sizes = new String[] { "8", "9", "10", "11", "12", "13", "14" };
      //fontChooser = new FontChooser(this, names, sizes);
      
      fontChooser = new FontChooserDialog(this, "Choose Legend Font", false, new Font("Default", Font.PLAIN, 11)); 
      
      // Add action listeners      
      //jcomboLegendFont.addActionListener(this);
      
      jbLegendFont.addActionListener(this);           
      jbLegendFGColor.addActionListener(this);           
      jbLegendBGColor.addActionListener(this);
      
      JPanel legendPanel1 = new JPanel();
      JPanel legendPanel2 = new JPanel();
      JPanel legendPanel3 = new JPanel();
      legendPanel1.setLayout(new GridLayout(1, 2));
      legendPanel1.add(new JLabel("Font: "));
      //legendPanel1.add(new JLabel("Font Size: "));
      //legendPanel1.add(jcomboLegendFont);
      legendPanel1.add(jbLegendFont);
      legendPanel2.setLayout(new GridLayout(1, 2));
      legendPanel2.add(new JLabel("Text Color: "));
      legendPanel2.add(jbLegendFGColor);
      legendPanel3.setLayout(new GridLayout(1, 2));
      legendPanel3.add(new JLabel("Background Color: "));
      legendPanel3.add(jbLegendBGColor);           
      legendPanel1.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
      legendPanel2.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
      legendPanel3.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
      


      jcbUseCache = new JCheckBox("Use Data File Cache?", true);
      jcbUseCache.addActionListener(this);
//      jlCacheSize = new JLabel("Current Cache Size = ");
      jbClearCache = new JButton("Clear Cache");
      jbClearCache.addActionListener(new ActionListener() {
//        @Override
        public void actionPerformed(ActionEvent e) {
            WCTTransfer.clearTempDirectory();
        }
      });
      
      
      
      
      jrbLatLon = new JRadioButton("None (Lat/Lon)", true);
      jrbLatLon.addActionListener(this);           
      jrbAlbers = new JRadioButton("Albers Equal Area");
      jrbAlbers.addActionListener(this);           
      jrbAlbers.setEnabled(false);
      jrbRadar = new JRadioButton("Radar Projection");
      jrbRadar.addActionListener(this);           
      jrbRadar.setEnabled(false);
      jrbStereo = new JRadioButton("Polar Stereographic");
      jrbStereo.addActionListener(this);           
      jrbStereo.setEnabled(false);
      jrbHRAP = new JRadioButton("HRAP");
      jrbHRAP.addActionListener(this);           
      jrbHRAP.setEnabled(false);

      jrbDMS = new JRadioButton("Deg Min Sec", true);
      jrbDMS.addActionListener(this);           
      jrbDeci = new JRadioButton("Decimal Degrees");
      jrbDeci.addActionListener(this);           

      jrbKM = new JRadioButton("Kilometers");
      jrbKM.addActionListener(this);           
      jrbKM.setEnabled(false);
      jrbMiles = new JRadioButton("Miles", true);
      jrbMiles.addActionListener(this);           
      jrbMiles.setEnabled(false);
      jrbNauticalMi = new JRadioButton("Nautical Mi", true);
      jrbNauticalMi.addActionListener(this);           
      jrbNauticalMi.setEnabled(false);
      
      
      // Add to button group
      ButtonGroup projButtonGroup = new ButtonGroup();
      projButtonGroup.add(jrbLatLon);
      projButtonGroup.add(jrbAlbers);
      projButtonGroup.add(jrbRadar);
      projButtonGroup.add(jrbStereo);
      projButtonGroup.add(jrbHRAP);

      // Add to button group
      ButtonGroup geoButtonGroup = new ButtonGroup();
      geoButtonGroup.add(jrbDMS);
      geoButtonGroup.add(jrbDeci);
     
      // Add to button group
      ButtonGroup distButtonGroup = new ButtonGroup();
      distButtonGroup.add(jrbMiles);
      distButtonGroup.add(jrbKM);
      distButtonGroup.add(jrbNauticalMi);


      
      
      
      
      
      
      
      displayPanel = new JPanel();
      displayPanel.setBorder(BorderFactory.createEmptyBorder(2, 15, 2, 15));
      displayPanel.setLayout(new RiverLayout());
      displayPanel.add(new JLabel("Display Properties", JLabel.CENTER), "p center");
      displayPanel.add(jcbAddLogo, "br left");
      displayPanel.add(new JLabel("(This controls automatic NOAA logo addition in all animations"), "br left");
      displayPanel.add(new JLabel("  and screen captures)"), "br left");
      displayPanel.add(new JLabel("Change Display Units: Right-click on the Status Bar."), "br br br br br left vfill");
//      displayPanel.add(jcbAutoResolution);
//      displayPanel.add(jbManualRefresh);

      
      smoothPanel = new JPanel();
      smoothPanel.setBorder(BorderFactory.createEmptyBorder(2, 35, 2, 35));
      smoothPanel.setLayout(new GridLayout(6,1));
      smoothPanel.add(new JLabel("Smoothing Properties", JLabel.CENTER));
      smoothPanel.add(new JLabel(" "));
      smoothPanel.add(jcbEngageSmoothing);
      smoothPanel.add(jcbEngagePowerSmoothing);
      JPanel spinnerPanel = new JPanel();
      spinnerPanel.add(new JLabel("Smoothing Factor: "));      
      spinnerPanel.add(smoothFactorSpinner);
      smoothPanel.add(spinnerPanel);
      smoothPanel.add(new JLabel(" "));
      
      
      
      rasterPanel = new JPanel();
      rasterPanel.setLayout(new GridLayout(6,1));
      rasterPanel.add(new JLabel("Raster Properties", JLabel.CENTER));
      rasterPanel.add(new JLabel("<html><b>-- Must Reload After Change --</b></html>", JLabel.CENTER));
      rasterPanel.add(rasterPanel1);
      rasterPanel.add(rasterPanel2);
      rasterPanel.add(rasterPanel3);
      rasterPanel.add(rasterPanel4);

      legendPanel = new JPanel();
      legendPanel.setLayout(new GridLayout(6,1));
      legendPanel.add(new JLabel("Legend Properties", JLabel.CENTER));
//      legendPanel.add(new JLabel(" (Currently applies to Radar data only) "));
      legendPanel.add(new JLabel(" "));
      legendPanel.add(legendPanel1);
      legendPanel.add(legendPanel2);
      legendPanel.add(legendPanel3);
      legendPanel.add(new JLabel(""));
      
      projPanel = new JPanel();
      projPanel.setLayout(new GridLayout(6,1));
      projPanel.add(new JLabel("Choose Map Projection", JLabel.CENTER));
      projPanel.add(jrbLatLon);
      projPanel.add(jrbAlbers);
      projPanel.add(jrbRadar);
      projPanel.add(jrbStereo);
      projPanel.add(jrbHRAP);

      geoPanel = new JPanel();
      geoPanel.setLayout(new GridLayout(6,1));
      geoPanel.add(new JLabel("Geographic Units", JLabel.CENTER));
      geoPanel.add(jrbDMS);
      geoPanel.add(jrbDeci);
      geoPanel.add(new JLabel(""));
      geoPanel.add(new JLabel(""));
      geoPanel.add(new JLabel(""));

      distPanel = new JPanel();
      distPanel.setLayout(new GridLayout(6,1));
      distPanel.add(new JLabel("Distance Units", JLabel.CENTER));
      distPanel.add(jrbKM);
      distPanel.add(jrbMiles);
      distPanel.add(jrbNauticalMi);
      distPanel.add(new JLabel(""));
      distPanel.add(new JLabel(""));

      
      
      JButton jbCacheChooser = new JButton("Browse");
      final JDialog finalThis = this;
      jbCacheChooser.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
            JFileChooser jfc = new JFileChooser();
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            jfc.showSaveDialog(finalThis);
            File newCacheDir = jfc.getSelectedFile();
            if (newCacheDir == null) {
                return;
            }
            else {
                cacheChange = true;
                try {            
                  jtfCacheLocation.setText(newCacheDir.toString());
              } catch (Exception e) {
                  JOptionPane.showMessageDialog(finalThis, e.getMessage(), "Cache Error", JOptionPane.WARNING_MESSAGE);
              }
            }
        }
      });
      jtfCacheLocation.getDocument().addDocumentListener(new DocumentListener() {
        public void changedUpdate(DocumentEvent e) {
            cacheChange = true;
//            System.out.println("setting cache change to true 1");
        }
        public void insertUpdate(DocumentEvent e) {
            cacheChange = true;
//            System.out.println("setting cache change to true 2");
        }
        public void removeUpdate(DocumentEvent e) {
            cacheChange = true;
//            System.out.println("setting cache change to true 3");
        }
      });
      JButton jbResetCacheToDefault = new JButton("Reset to Default Location");
      jbResetCacheToDefault.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            jtfCacheLocation.setText(WCTConstants.DEFAULT_DATA_CACHE_LOCATION);
            cacheChange = true;
        }
      });
      
      
      jtfMaxCacheSize.getDocument().addDocumentListener(new DocumentListener() {
          public void changedUpdate(DocumentEvent e) {
              cacheChange = true;
          }
          public void insertUpdate(DocumentEvent e) {
              cacheChange = true;
          }
          public void removeUpdate(DocumentEvent e) {
              cacheChange = true;
          }
        });
      
      
      

      
      cachePanel = new JPanel();
      cachePanel.setBorder(BorderFactory.createEmptyBorder(2, 15, 2, 15));
      cachePanel.setLayout(new RiverLayout());
      cachePanel.add(jcbUseCache, "left br");
      cachePanel.add(new JLabel("Cache Location", JLabel.CENTER), "p left");
      cachePanel.add(jtfCacheLocation, "br left hfill");
      cachePanel.add(jbCacheChooser, "br left");
      cachePanel.add(jbResetCacheToDefault);
      cachePanel.add(new JLabel("Maximum Cache Size (in MB)"), "p left");
      cachePanel.add(jtfMaxCacheSize, "hfill");
      
//      cachePanel.add(jbClearCache, "br");

      
      
      
      JPanel miscPanel = new JPanel(new RiverLayout());

      final JXHyperlink setStatusBarUnitsLink = new JXHyperlink();
      setStatusBarUnitsLink.setText("Edit");
      setStatusBarUnitsLink.addMouseListener(new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent event) {
			viewer.getStatusBar().showPopup(finalThis, 
					setStatusBarUnitsLink.getX()+5, setStatusBarUnitsLink.getY()+25);
		}
      });
      
      JXHyperlink editExtraSiteConfigLink = new JXHyperlink();
      editExtraSiteConfigLink.setText("Edit (in Default Text Editor)");
      editExtraSiteConfigLink.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			editExtraSiteList();
		}
      });

      JXHyperlink editCustomFilenamesConfigLink = new JXHyperlink();
      editCustomFilenamesConfigLink.setText("Edit (in Default Text Editor)");
      editCustomFilenamesConfigLink.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			editCustomFilenamesList();
		}
      });
      
      JXHyperlink showSystemOutLogger = new JXHyperlink();
      showSystemOutLogger.setText("Show");
      showSystemOutLogger.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			WCTSystemOutputDialog.getSharedSystemOutputDialog(viewer).setVisible(true);
		}
      });

      

      
      miscPanel.add("p", new JLabel("1) Status Bar Units and Format: "));
      miscPanel.add("tab", setStatusBarUnitsLink);
      miscPanel.add("p", new JLabel("2) Extra Radar Site List: "));
      miscPanel.add("tab", editExtraSiteConfigLink);
      miscPanel.add("p", new JLabel("3) Custom Filename Patterns: "));
      miscPanel.add("tab", editCustomFilenamesConfigLink);
      miscPanel.add("p", new JLabel("4) WCT Output Log: "));
      miscPanel.add("tab", showSystemOutLogger);
      
      
      
      
      JPanel downsamplePanel = new JPanel();
      downsamplePanel.setBorder(BorderFactory.createEmptyBorder(2, 15, 2, 15));
      downsamplePanel.setLayout(new RiverLayout());

      
      
      downsampleGatesSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 4, 1));
      downsampleGatesSpinner.addChangeListener(this);
      downsampleRaysSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 4, 1));
      downsampleRaysSpinner.addChangeListener(this);
      
      downsamplePanel.add("center", new JLabel("Downsample Radial Data"));
      downsamplePanel.add("br left", new JLabel("Reduces resolution of Radial data for increased performance"));
      downsamplePanel.add("p left tab", new JLabel("Downsample Gates"));
      downsamplePanel.add("tab", downsampleGatesSpinner);
      downsamplePanel.add("br left tab", new JLabel("Downsample Rays"));
      downsamplePanel.add("tab", downsampleRaysSpinner);

      
//      downsamplePanel.add(new JLabel(" Downsample Radial Data ", JLabel.CENTER));
//      downsamplePanel.add(new JLabel("  ", JLabel.CENTER));
//      downsamplePanel.add(new JLabel(" "));
//      downsamplePanel.add(new JLabel("Downsample Gates"));
//      downsamplePanel.add(downsampleGatesSpinner);
//      downsamplePanel.add(new JLabel(" "));
//      downsamplePanel.add(new JLabel("Downsample Rays"));
//      downsamplePanel.add(downsampleRaysSpinner);
//      downsamplePanel.add(new JLabel(" "));
      

      

      JCheckBox jcbEnableLogo = new JCheckBox("Disable all logos");
      JComboBox jcomboLogoTransparency = new JComboBox(new Object[] {
              "  0 %", " 10 %", " 20 %", " 30 %", " 40 %", " 50 %", 
              " 60 %", " 70 %", " 80 %", " 90 %", "100 %"
      });      

      TextFieldWithBrowse iconChooser1 = new TextFieldWithBrowse("icon1");
      TextFieldWithBrowse iconChooser2 = new TextFieldWithBrowse("icon2");
      TextFieldWithBrowse iconChooser3 = new TextFieldWithBrowse("icon3");
      JPanel logoPanel = new JPanel(new RiverLayout());
      logoPanel.add(new JLabel("Choose custom logo images to add to saved images and animations"), "p center");
      logoPanel.add(jcbEnableLogo, "left p");
      logoPanel.add(new JLabel("Transparency: "));
      logoPanel.add(jcomboLogoTransparency);
      logoPanel.add(new JLabel("Icon 1: "), "br");
      logoPanel.add(iconChooser1, "hfill");
      logoPanel.add(new JLabel("Icon 2: "), "br");
      logoPanel.add(iconChooser2, "hfill");
      logoPanel.add(new JLabel("Icon 3: "), "br");
      logoPanel.add(iconChooser3, "hfill");
      
      

      
      tabbedPane = new JTabbedPane();
      tabbedPane.add("Display", displayPanel);
//      tabbedPane.add("Logo", logoPanel);
//      tabbedPane.add("Raster", rasterPanel);
      tabbedPane.add("Legend", legendPanel);
      //tabbedPane.add("Projection", projPanel);
      //tabbedPane.add("XY Units", geoPanel);
      //tabbedPane.add("Distance Units", distPanel);
//      tabbedPane.add("Smoothing", smoothPanel);
      tabbedPane.add("Cache", cachePanel);
//      tabbedPane.add("Downsample", downsamplePanel);
      tabbedPane.add("Misc.", miscPanel);
            
      // Action buttons
      jbOK = new JButton("OK");
      jbOK.addActionListener(this);
      jbCancel = new JButton("Cancel");
      jbCancel.addActionListener(this);
      jbApply = new JButton("Apply");
      jbApply.addActionListener(this);
      
      choicePanel = new JPanel();
      choicePanel.setLayout(new GridLayout(1,3));
      choicePanel.add(jbOK);
      choicePanel.add(jbCancel);
      choicePanel.add(jbApply);
      
      
      getContentPane().setLayout(new BorderLayout());
      getContentPane().add(tabbedPane, "Center");
      getContentPane().add(choicePanel, "South");

         
      JRootPane rootPane = this.getRootPane();
      InputMap iMap = rootPane.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
      iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");

      ActionMap aMap = rootPane.getActionMap();
      aMap.put("escape", new AbstractAction()
          {
              public void actionPerformed(ActionEvent e)
              {
                  dispose();
              }
          });

      
   }

   public int getDownsampleGates() {
       return Integer.parseInt(downsampleGatesSpinner.getValue().toString());
   }
   public int getDownsampleRays() {
       return Integer.parseInt(downsampleRaysSpinner.getValue().toString());
   }

   
   
   
   public void setAutoRefresh(boolean autoRefresh) {
      jcbAutoResolution.setSelected(autoRefresh);
      viewer.getMapPaneZoomChange().setRadarActive(autoRefresh);
   }
   public boolean isAutoRefresh() {
      return jcbAutoResolution.isSelected();
   }
   
   public boolean isAutoResolution() {
      return jcbAutoResolution.isSelected();
   }
   
   public void setIsAutoResolution(boolean isAuto) {
      jcbAutoResolution.setSelected(isAuto);
      displayChange = true;      
   }
   
   public int getRasterGridSize() {
      return Integer.parseInt((String)jcomboGridSize.getSelectedItem());
   }
 
   public void setRasterGridSize(int size) {
      jcomboGridSize.setSelectedItem(new String(""+size));
      rasterChange = true;
   }
   
   public boolean isRasterVariableResolution() {
      return jcbRasterVariableRes.isSelected();
   }
   
   public void setRasterVariableResolution(boolean variableRes) {
      jcbRasterVariableRes.setSelected(variableRes);
      rasterChange = true;
   }
   
   public String getRasterNoDataString() {
      return jtfRasterNoData.getText();
   }
   
   public void setRasterNoDataString(String noData) {
      jtfRasterNoData.setText(noData);
      rasterChange = true;
   }
   
   public void setMetaLabelFontSize(int size) {
      jcomboLegendFont.setSelectedItem(new Integer(size).toString());            
   }
   
   public void setLegendFGColor(Color c) {
      jbLegendFGColor.setBackground(c);
   }
   public void setLegendBGColor(Color c) {
      jbLegendBGColor.setBackground(c);
   }
   
      
   
   public void setDistanceUnitsEnabled (boolean enable) {
      jrbMiles.setEnabled(enable);  
      jrbKM.setEnabled(enable);  
      jrbNauticalMi.setEnabled(enable);  
   }
   
   public void setGeographicUnitsEnabled (boolean enable) {
      jrbDMS.setEnabled(enable);  
      jrbDeci.setEnabled(enable);  
   }
   
   public void setProjectionChangeEnabled (boolean enable) {
      jrbAlbers.setEnabled(enable);  
      jrbRadar.setEnabled(enable);  
      jrbStereo.setEnabled(enable);  
      jrbHRAP.setEnabled(enable);  
   }
   
   public boolean isNexradHighlightVisible() {
      return jcbRasterVariableRes.isSelected();
   }


   // Implementation of ChangeListener interface.
   public void stateChanged(ChangeEvent event) {
      Object source = event.getSource();
      if (source == smoothFactorSpinner) {
          smoothChange = true;
      }
      else if (source == downsampleGatesSpinner || source == downsampleRaysSpinner) {
          downsampleChange = true;
      }
      
   }
   
   
   // Implementation of ActionListener interface.
   public void actionPerformed(ActionEvent event) {

      Object source = event.getSource();
   
      if (source == jbManualRefresh) {
//         nexview.refreshNexrad();
          viewer.getMapPaneZoomChange().zoomChanged(new org.geotools.gui.swing.event.ZoomChangeEvent("MANUAL REFRESH", null));

      }
      else if (source == jbLegendFont) {
         fontChooser.pack();
         fontChooser.setVisible(true);
      }
      else if (source == jbLegendBGColor) {
                  
         Color newColor = JColorChooser.showDialog(ViewProperties.this,
                          "Choose Map Layer Color",
                          jbLegendBGColor.getBackground());
         if (newColor != null) {
            jbLegendBGColor.setBackground(newColor);
         }
      }
      else if (source == jbLegendFGColor) {
                  
         Color newColor = JColorChooser.showDialog(ViewProperties.this,
                          "Choose Map Layer Color",
                          jbLegendFGColor.getBackground());
         if (newColor != null) {
            jbLegendFGColor.setBackground(newColor);
         }
      }
         

      
      // Register any changes made
      
      if (source == jbCancel) 
         dispose();
      else if (source == jbOK) {
         makeChanges();
         dispose();
      }
      else if (source == jbApply) {
         makeChanges();
      }
      else if (source == jcbAddLogo) {
         displayChange = true;
      }
      else if (source == jcomboGridSize ||
                  source == jcbRasterVariableRes ||
                  source == jtfRasterNoData) {
         rasterChange = true;
      }
      else if (source == jbLegendFont ||
                  source == jbLegendFGColor ||
                  source == jbLegendBGColor) {
         legendChange = true;
      }
      else if (source == jrbLatLon ||
                  source == jrbAlbers ||
                  source == jrbStereo ||
                  source == jrbHRAP ||
                  source == jrbRadar) {
         projChange = true;
      }
      else if (source == jrbDMS || source == jrbDeci) {
         geoChange = true;
      }
      else if (source == jrbKM || source == jrbMiles || source == jrbNauticalMi) {
         distChange = true;
      }
      else if (source == jcbEngageSmoothing || source == jcbEngagePowerSmoothing) {
         smoothChange = true;
      }
      else if (source == jcbUseCache) {
         cacheChange = true;
      }
      
      
   } // actionPerformed
   
   public void makeChanges() {
      
      // Display options
      if (displayChange) {
//         nexview.getNexradMapPaneZoomChange().setNexradActive(jcbAutoResolution.isSelected());
          viewer.setNOAALogoPainted(jcbAddLogo.isSelected());
         displayChange = false;
      }
      
      // Raster options
      if (rasterChange) {      
         int gridSize = Integer.parseInt((String)jcomboGridSize.getSelectedItem());
         boolean gridVariableRes = jcbRasterVariableRes.isSelected();
         float gridNoData = -999.0f;
         try {
            gridNoData = Float.parseFloat((String)jtfRasterNoData.getText());
         } catch (Exception e) { // check for non-numbers
            JOptionPane.showMessageDialog(this, "The NO_DATA value must be a number!\nYou entered: " + 
                         jtfRasterNoData.getText(), "NO DATA NUMBER ERROR", JOptionPane.ERROR_MESSAGE);
            return;
         }
//         viewer.setRasterizer(gridSize, gridVariableRes, gridNoData);
         JOptionPane.showMessageDialog(this, "Not implemented..." + 
                 jtfRasterNoData.getText(), "ERROR", JOptionPane.ERROR_MESSAGE);
         rasterChange = false;
      }
      
      if (legendChange) {
         int fontSize = Integer.parseInt((String)jcomboLegendFont.getSelectedItem());
         //nexview.setMetaLabelFont(new Font("Default", Font.PLAIN, fontSize));
         try {
             viewer.setLegendFont(fontChooser.getSelectedFont());         
             viewer.setLegendFGColor(jbLegendFGColor.getBackground());
             viewer.setLegendBGColor(jbLegendBGColor.getBackground());
         } catch (Exception e) {
             javax.swing.JOptionPane.showMessageDialog(this, "Unable to change font, background color or foreground color", 
                     "VIEW PROPERTIES LEGEND ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);               
             e.printStackTrace();
         }
         legendChange = false;
      }
      
      if (projChange) {
         // Projection Settings
         if (jrbLatLon.isSelected()) {
            //nexview.setProjection(NexradIAViewer.LATLON);
            try {
               viewer.setCoordinateSystem(csFactory.createFromWKT(WCTProjections.WGS84_WKT));
            } catch (Exception e) {
               e.printStackTrace();
               javax.swing.JOptionPane.showMessageDialog(this, "Projection Transform Error", 
                  "VIEW PROPERTIES PROJECTION ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);               
            }
         }
         else if (jrbAlbers.isSelected()) {
            //nexview.setProjection(NexradIAViewer.ALBERS);
            try {
               viewer.setCoordinateSystem(csFactory.createFromWKT(WCTProjections.ALBERS_EQUALAREA_CONUS_NAD83_WKT));
            } catch (Exception e) {
               e.printStackTrace();
               javax.swing.JOptionPane.showMessageDialog(this, "Projection Transform Error", 
                  "VIEW PROPERTIES PROJECTION ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);               
            }
         }
         else if (jrbRadar.isSelected()) {
            //nexview.setProjection(NexradIAViewer.RADAR);
            try {
               viewer.setCoordinateSystem(wCTProjections.getRadarCoordinateSystem(viewer.getNexradHeader()));
            } catch (Exception e) {
               e.printStackTrace();
               javax.swing.JOptionPane.showMessageDialog(this, "Projection Transform Error", 
                  "VIEW PROPERTIES PROJECTION ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);               
            }
         }
         else if (jrbStereo.isSelected()) {
            //nexview.setProjection(NexradIAViewer.STEREO);
            try {
               viewer.setCoordinateSystem(csFactory.createFromWKT(WCTProjections.HRAPSTEREO_WKT));
            } catch (Exception e) {
               e.printStackTrace();
               javax.swing.JOptionPane.showMessageDialog(this, "Projection Transform Error", 
                  "VIEW PROPERTIES PROJECTION ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);               
            }
         }
         else if (jrbHRAP.isSelected()) {
            //nexview.setProjection(NexradIAViewer.HRAP);
         }
         
         projChange = false;
      }
      
      if (geoChange) {
         // Geographic Units Settings
         //if (jrbDMS.isSelected()) {
            //nexview.setGeographicUnits(NexradIAViewer.DMS);
         //}
         //else if (jrbDeci.isSelected()) { 
            //nexview.setGeographicUnits(NexradIAViewer.DECI);
         //}
         
         geoChange = false;
      }
      
      if (distChange) {
         // Geographic Units Settings
         if (jrbMiles.isSelected()) 
            viewer.setDistanceUnits(WCTViewer.MILES);
         else if (jrbKM.isSelected()) 
            viewer.setDistanceUnits(WCTViewer.KM);
         else if (jrbNauticalMi.isSelected()) 
            viewer.setDistanceUnits(WCTViewer.NAUTICAL_MI);
         
         distChange = false;
      }
      
      
      if (smoothChange) {
         if (jcbEngageSmoothing.isSelected()) {
             viewer.setRadarSmoothFactor(Integer.parseInt(smoothFactorSpinner.getValue().toString()));
         }
         else {
             viewer.setRadarSmoothFactor(0);
         }
         viewer.setRadarPowerSmoothing(jcbEngagePowerSmoothing.isSelected());
         
         //if (jcbCustomKernel.isSelected()) {
         //   nexview.getNexradRasterizer().setSmoothingKernel(
         //}
         
//         nexview.reloadNexrad();
         viewer.refreshRadarData();
         
      }      
      
      if (cacheChange) {
         viewer.setUseWctCache(jcbUseCache.isSelected());
         
         try {
             Long.parseLong(jtfMaxCacheSize.getText());
         } catch (Exception e) {
             JOptionPane.showMessageDialog(this, "Error reading the max cache size value.\nUse only whole integer numbers.", 
                     "Cache Size Error", JOptionPane.WARNING_MESSAGE);
             return;
         }
         
         try {            
             
             if (! (WCTProperties.getWCTProperty("dataCacheLocation") != null && 
                     jtfCacheLocation.getText().trim().equals(WCTProperties.getWCTProperty("dataCacheLocation"))
                    ) || 
                ! (WCTProperties.getWCTProperty("dataCacheSizeLimit") != null &&                     
                     jtfCacheLocation.getText().trim().equals(WCTProperties.getWCTProperty("dataCacheSizeLimit")) ) ) {
                 
             
//           WCTConstants.getInstance().setCacheLocation(newCacheDir.toString());
                 WCTProperties.setWCTProperty("dataCacheLocation", jtfCacheLocation.getText());
                 WCTProperties.setWCTProperty("dataCacheSizeLimit", String.valueOf(Long.parseLong(jtfMaxCacheSize.getText())*1024L*1024L));
           
                 JOptionPane.showMessageDialog(this, "Data Cache settings have been changed " +
                   "to:\nLocation: "+jtfCacheLocation.getText()+
                   "\nSize: "+jtfMaxCacheSize.getText()+" MB\nA restart of the Weather and Climate Toolkit is REQUIRED " +
                           "to use the new cache settings (location & size).", "Cache Location", JOptionPane.INFORMATION_MESSAGE);
           
             }
         } catch (Exception e) {
             JOptionPane.showMessageDialog(this, e.getMessage(), "Cache Error", JOptionPane.WARNING_MESSAGE);
         }
         
         
//         try {            
//             // restart example
//             nexview.dispose();
//             nexview = null;
//             Class.forName("gov.noaa.ncdc.nexradiv.NexradIAViewerSplash")
//                 .getMethod("main", new Class[] {String[].class})
//                 .invoke(null, new Object[] {new String[]{}});
//         } catch (Exception e) {
//             e.printStackTrace();
//             JOptionPane.showMessageDialog(this, e.getMessage(), "Restart Error", JOptionPane.WARNING_MESSAGE);
//         }

         
      }
      
      
      if (downsampleChange) {
          viewer.reloadData();
      }
      
      
   }
   
   
   
   
   
   
   
   private void editCustomFilenamesList() {
		try {
			File customFilenamesFile = new File(WCTConstants.getInstance().getCacheLocation()+
					File.separator+"config"+File.separator+"customFilenamePatterns.xml");
			if (! customFilenamesFile.exists()) {
				createDefaultCustomFilenamesFile(customFilenamesFile);
			}
			
            javax.swing.JOptionPane.showMessageDialog(this, "Any changes require a restart of the Toolkit to take effect.", 
                    "Configuration Change Notice", javax.swing.JOptionPane.INFORMATION_MESSAGE);               
			Desktop.getDesktop().open(customFilenamesFile);
		} catch (IOException e1) {
			e1.printStackTrace();
             javax.swing.JOptionPane.showMessageDialog(this, "Error opening custom filename patterns file. \n"+e1.getMessage(), 
                     "VIEW PROPERTIES ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);               
		}
   }

   private void createDefaultCustomFilenamesFile(File customFilenamesFile) throws IOException {
	   
	   StringBuilder sb = new StringBuilder();
	   
	   sb.append("<?xml version=\"1.0\"?>                                                                                 "+System.getProperty("line.separator"));
	   sb.append("<filenamePatterns>                                                                                      "+System.getProperty("line.separator"));
	   sb.append("	                                                                                                      "+System.getProperty("line.separator"));
	   sb.append("   <!--                                                                                                 "+System.getProperty("line.separator"));
	   sb.append("    Level-3 from Texas A&M LDM Feed                                                                     "+System.getProperty("line.separator"));
	   sb.append("    012345678901234567890                                                                               "+System.getProperty("line.separator"));
	   sb.append("    N0R_20050721_2025                                                                                   "+System.getProperty("line.separator"));
	   sb.append("   -->                                                                                                  "+System.getProperty("line.separator"));
	   sb.append("   <!--                                                                                                 "+System.getProperty("line.separator"));
	   sb.append("   <filename>                                                                                           "+System.getProperty("line.separator"));
	   sb.append("      <dataType> NEXRAD_LEVEL3 </dataType>                                                              "+System.getProperty("line.separator"));
	   sb.append("      <category> NEXRAD Radar </category>                                                               "+System.getProperty("line.separator"));
	   sb.append("      <description> NEXRAD Level-III </description>                                                     "+System.getProperty("line.separator"));
	   sb.append("      <filePattern> ^[a-zA-Z0-9]{3}_[0-9]{8}_[0-9]{4}$ </filePattern>                                   "+System.getProperty("line.separator"));
	   sb.append("      <fileTimestampLocation> yyyy:4-8; MM:8-10; dd:10-12; HH:13-15; mm:15-17 </fileTimestampLocation>  "+System.getProperty("line.separator"));
	   sb.append("      <productCode> 0-3 </productCode>                                                                  "+System.getProperty("line.separator"));
	   sb.append("   </filename>                                                                                          "+System.getProperty("line.separator"));
	   sb.append("   -->                                                                                                  "+System.getProperty("line.separator"));
	   sb.append("                                                                                                        "+System.getProperty("line.separator"));
	   sb.append("   <!--                                                                                                 "+System.getProperty("line.separator"));
	   sb.append("    Level-II Super Res NCDC super-res format                                                            "+System.getProperty("line.separator"));
	   sb.append("     0123456789012345678901234567890                                                                    "+System.getProperty("line.separator"));
	   sb.append("     KPAH20080330_130339_V03.gz                                                                         "+System.getProperty("line.separator"));
	   sb.append("   -->                                                                                                  "+System.getProperty("line.separator"));
	   sb.append("   <!--                                                                                                 "+System.getProperty("line.separator"));
	   sb.append("   <filename>                                                                                           "+System.getProperty("line.separator"));
	   sb.append("      <dataType> RADIAL </dataType>                                                                     "+System.getProperty("line.separator"));
	   sb.append("      <category> NEXRAD Radar </category>                                                               "+System.getProperty("line.separator"));
	   sb.append("      <description>Level-II NEXRAD - Super Resolution (0.5 deg x 0.25 km) </description>                "+System.getProperty("line.separator"));
	   sb.append("      <filePattern> ^[a-zA-Z]{4}[0-9]{8}_[0-9]{6}_V03(|.Z|.z|.GZ|.gz)$ </filePattern>                   "+System.getProperty("line.separator"));
	   sb.append("      <fileTimestampLocation> yyyy:4-8; MM:8-10; dd:10-12; HH:13-15; mm:15-17; ss:17-19 </fileTimestampL"+System.getProperty("line.separator"));
	   sb.append("      <sourceID> 0-4 </sourceID>                                                                        "+System.getProperty("line.separator"));
	   sb.append("      <productCode> 20-23 </productCode>                                                                "+System.getProperty("line.separator"));
	   sb.append("      <info href=\"http://www.ncdc.noaa.gov/oa/radar/radarproducts.html\">                              "+System.getProperty("line.separator"));
	   sb.append("         <![CDATA[                                                                                      "+System.getProperty("line.separator"));
	   sb.append("         Level-II NEXRAD Data contains the three base moments, or variables, for NEXRAD data.  These    "+System.getProperty("line.separator"));
	   sb.append("         moments are Reflectivity, Radial Velocity and Spectrum Width.  Level-II data contains all      "+System.getProperty("line.separator"));
	   sb.append("         elevation angles within the Radar's volume scan.  The version 3 (V03) files contain data at the"+System.getProperty("line.separator"));
	   sb.append("         'super' resolution (.25 km x .5 deg.).                                                         "+System.getProperty("line.separator"));
	   sb.append("         ]]>                                                                                            "+System.getProperty("line.separator"));
	   sb.append("      </info>                                                                                           "+System.getProperty("line.separator"));
	   sb.append("   </filename>                                                                                          "+System.getProperty("line.separator"));
	   sb.append("   -->                                                                                                  "+System.getProperty("line.separator"));
	   sb.append("	                                                                                                      "+System.getProperty("line.separator"));
	   sb.append("</filenamePatterns>                                                                                     "+System.getProperty("line.separator"));

	   
	   FileUtils.writeStringToFile(customFilenamesFile, sb.toString());
	   
   }

   
   

   
   
   private void editExtraSiteList() {
		try {
			File extraSiteListFile = new File(WCTConstants.getInstance().getCacheLocation()+ExtraRadarSiteListManager.DEFAULT_SITE_LIST_FILE);
			if (! extraSiteListFile.exists()) {
				createDefaultSiteListFile(extraSiteListFile);
			}

            javax.swing.JOptionPane.showMessageDialog(this, "Any changes require a restart of the Toolkit to take effect.", 
                    "Configuration Change Notice", javax.swing.JOptionPane.INFORMATION_MESSAGE);               
			Desktop.getDesktop().open(extraSiteListFile);
		} catch (IOException e1) {
			e1.printStackTrace();
             javax.swing.JOptionPane.showMessageDialog(this, "Error opening extra site list file. \n"+e1.getMessage(), 
                     "VIEW PROPERTIES ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);               
		}
   }
   
   private void createDefaultSiteListFile(File extraSiteListFile) throws IOException {
	   
	   StringBuilder sb = new StringBuilder();
	   
	   sb.append("#                                                                  "+System.getProperty("line.separator"));
	   sb.append("# Config file for appending additional radar sites to the internal "+System.getProperty("line.separator"));
	   sb.append("# Radar site list.                                                 "+System.getProperty("line.separator"));
	   sb.append("#                                                                  "+System.getProperty("line.separator"));
	   sb.append("# Columns are defined as follows:                                  "+System.getProperty("line.separator"));
	   sb.append("# 1) if lat/lon/elev is not in the file                            "+System.getProperty("line.separator"));
	   sb.append("# ID (4 chars), Lat, Lon, Elev, Location, State (optional)         "+System.getProperty("line.separator"));
	   sb.append("#                                                                  "+System.getProperty("line.separator"));
	   sb.append("# 2) lat/lon/elev is in the file (PREFERRED)                       "+System.getProperty("line.separator"));
	   sb.append("# ID (4 chars), Location, State (optional)                         "+System.getProperty("line.separator"));
	   sb.append("                                                                   "+System.getProperty("line.separator"));
	   sb.append("# Entry added on: Thu Sep 08 16:51:50 EDT 2011                     "+System.getProperty("line.separator"));
	   sb.append("#KLGX, 47.1158, -124.1069, 366.0, GRAYS HARBOR COUNTY, WA          "+System.getProperty("line.separator"));
	   sb.append("                                                                   "+System.getProperty("line.separator"));
	   sb.append("# Entry added on: Fri Jan 27 12:24:38 EST 2012                     "+System.getProperty("line.separator"));
	   sb.append("#ROP4, 35.238056, -97.46, 1200.0, KCRI test 1, OK                  "+System.getProperty("line.separator"));
	   
	   FileUtils.writeStringToFile(extraSiteListFile, sb.toString());
	   
   }
   


} // END CLASS
