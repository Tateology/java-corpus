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

import gov.noaa.ncdc.nexradexport.alpha.NexradAlphaExport;
import gov.noaa.ncdc.wct.WCTProperties;
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.io.FileScanner;
import gov.noaa.ncdc.wct.io.ScanResults;
import gov.noaa.ncdc.wct.io.SupportedDataType;
import gov.noaa.ncdc.wct.ui.DataSelector;
import gov.noaa.ncdc.wct.ui.WCTTextDialog;
import gov.noaa.ncdc.wct.ui.WCTViewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.geotools.styling.StyleBuilder;
import org.xml.sax.SAXException;

/**
 *  Description of the Class
 *
 * @author     Steve.Ansari
 * @created    April 14, 2004
 */
public class AlphaProperties extends JDialog implements ActionListener {

   private WCTViewer viewer;
   private DataSelector dataSelector;

   private JPanel leftPanel, rightPanel;
   private JLabel jlDrawGraphic, jlColor, jlSize, jlSymbol, jlID, jlTrans, jlHalo;
   private JCheckBox jcbID, jcbHalo;
   private JComboBox jcomboSize, jcomboSymbol, jcomboTrans;
   private JButton jbLoad, jbColor, jbTable, jbTextP1, jbTextP2, jbExport;
   private JList fileList;
   private DefaultListModel listModel;
   private Vector<String> matchingFiles;

   private String[] symbols = {StyleBuilder.MARK_CIRCLE,
         StyleBuilder.MARK_CROSS,
         StyleBuilder.MARK_SQUARE,
         StyleBuilder.MARK_TRIANGLE,
         StyleBuilder.MARK_X,
         StyleBuilder.MARK_STAR
         };

   private DecimalFormat fmt3 = new DecimalFormat("0.000");


   /**
    *Constructor for the AlphaProperties object
    *
    * @param  viewer       Description of the Parameter
    * @param  dataSelector  Description of the Parameter
    */
   public AlphaProperties(WCTViewer viewer, DataSelector dataSelector) {
      //super("Alphanumeric Properties");
      super(viewer, "Alphanumeric Properties", false);

      this.viewer = viewer;
      this.dataSelector = dataSelector;
      createGUI();
      //      pack();
      //      setVisible(true);
   }


   /**
    *  Description of the Method
    */
   private void createGUI() {

      jlDrawGraphic = new JLabel("Draw Radar Background", JLabel.CENTER);
      jlColor = new JLabel("Color", JLabel.CENTER);
      jlSize = new JLabel("Size", JLabel.CENTER);
      jlSymbol = new JLabel("Symbol", JLabel.CENTER);
      jlHalo = new JLabel("Halo", JLabel.CENTER);
      jlTrans = new JLabel("Transparency (%)", JLabel.CENTER);
      jlID = new JLabel("Show Storm ID", JLabel.CENTER);

      jcbID = new JCheckBox("", true);
      jcbID.setHorizontalAlignment(SwingConstants.CENTER);
      jcbID.addActionListener(this);
      String[] options = new String[5];
      options[0] = "1";
      options[1] = "2";
      options[2] = "3";
      options[3] = "4";
      options[4] = "5";
      jcomboSize = new JComboBox(options);
      jcomboSize.setSelectedIndex(viewer.getAlphanumericLineWidth() - 1);
      jcomboSize.addActionListener(this);
      options = new String[6];
      options[0] = StyleBuilder.MARK_CIRCLE;
      options[1] = StyleBuilder.MARK_CROSS;
      options[2] = StyleBuilder.MARK_SQUARE;
      options[3] = StyleBuilder.MARK_TRIANGLE;
      options[4] = StyleBuilder.MARK_X;
      options[5] = StyleBuilder.MARK_STAR;
      jcomboSymbol = new JComboBox(options);
      jcomboSymbol.setSelectedItem(viewer.getAlphanumericSymbol());
//System.out.println("ALPHA SYMBOL =====  "+viewer.getAlphanumericSymbol());
      jcomboSymbol.addActionListener(this);
      
      jcbHalo = new JCheckBox("", true);
      jcbHalo.setHorizontalAlignment(SwingConstants.CENTER);
      jcbHalo.addActionListener(this);      
      
      
      jbColor = new JButton("   ");
      jbColor.setBackground(viewer.getAlphanumericLineColor());
      jbColor.addActionListener(this);
      options = new String[10];
      options[0] = "0";
      options[1] = "10";
      options[2] = "20";
      options[3] = "30";
      options[4] = "40";
      options[5] = "50";
      options[6] = "60";
      options[7] = "70";
      options[8] = "80";
      options[9] = "90";
      jcomboTrans = new JComboBox(options);
      jcomboTrans.setEditable(true);
      jcomboTrans.setSelectedItem((int) (viewer.getAlphanumericTransparency() * 100) + "");
      jcomboTrans.addActionListener(this);
      jcomboTrans.setEnabled(false);
      jbTable = new JButton("Show Attribute Table");
      jbTable.addActionListener(this);
      jbTextP1 = new JButton("Show Supplemental Data 1");
      jbTextP1.addActionListener(this);
      jbTextP2 = new JButton("Show Supplemental Data 2");
      jbTextP2.addActionListener(this);
      jbExport = new JButton("Export Attribute Table");
      jbExport.addActionListener(this);
      jbLoad = new JButton("Load");
      jbLoad.addActionListener(this);

      JPanel idPanel = new JPanel();
      idPanel.setLayout(new GridLayout(1, 2));
      idPanel.add(jlID);
      idPanel.add(jcbID);

      JPanel colorPanel = new JPanel();
      colorPanel.setLayout(new GridLayout(1, 2));
      colorPanel.add(jlColor);
      colorPanel.add(jbColor);

      JPanel sizePanel = new JPanel();
      sizePanel.setLayout(new GridLayout(1, 2));
      sizePanel.add(jlSize);
      sizePanel.add(jcomboSize);

      JPanel symbolPanel = new JPanel();
      symbolPanel.setLayout(new GridLayout(1, 2));
      symbolPanel.add(jlSymbol);
      symbolPanel.add(jcomboSymbol);

      JPanel haloPanel = new JPanel();
      haloPanel.setLayout(new GridLayout(1, 2));
      haloPanel.add(jlHalo);
      haloPanel.add(jcbHalo);
      
      JPanel transPanel = new JPanel();
      transPanel.setLayout(new GridLayout(1, 2));
      transPanel.add(jlTrans);
      transPanel.add(jcomboTrans);

      leftPanel = new JPanel();
      leftPanel.setLayout(new GridLayout(8, 1));
      leftPanel.add(idPanel);
      //leftPanel.add(symbolPanel);
      leftPanel.add(haloPanel);
      leftPanel.add(colorPanel);
      leftPanel.add(sizePanel);
      //leftPanel.add(transPanel);
      leftPanel.add(jbTable);
      leftPanel.add(jbTextP1);
      leftPanel.add(jbTextP2);
      leftPanel.add(jbExport);

      // Build JList
      ScanResults[] scanResults = dataSelector.getScanResults();
      listModel = new DefaultListModel();
      fileList = new JList(listModel);
      fileList.addMouseListener(new MouseAdapter(){
         public void mouseClicked(MouseEvent e){
            if (e.getClickCount() == 2){
               if (jbLoad.isEnabled()) {
                   loadGraphicBackground();
               }
            }
         }
      } );
      
      
      
      
      
      matchingFiles = new Vector<String>();
      try {
         refreshMatchingFileList();
      } catch (Exception e) {
         e.printStackTrace();
         javax.swing.JOptionPane.showMessageDialog(null, "Error reading matching Level-III files", 
            "ATTRIBUTE TABLE ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);            
      }


      rightPanel = new JPanel();
      rightPanel.setLayout(new BorderLayout());
      rightPanel.add(jlDrawGraphic, "North");
      rightPanel.add(new JScrollPane(fileList), "Center");
      rightPanel.add(jbLoad, "South");

      getContentPane().setLayout(new GridLayout(1, 2));
      getContentPane().add(leftPanel);
      getContentPane().add(rightPanel);

      // Do the colors
      //leftPanel.setBackground(new Color(143, 188, 143));
      //rightPanel.setBackground(new Color(189, 183, 107));

      /*
       *  jle.setBackground(new Color(60, 179, 113));
       *  jlProj.setBackground(new Color(189, 183, 107));
       *  jlGeoUnits.setBackground(new Color(60, 179, 113));
       *  jlDistUnits.setBackground(new Color(189, 183, 107));
       *  jcbXY.setBackground(new Color(143, 188, 143));
       *  jcbValue.setBackground(new Color(143, 188, 143));
       *  jcbLegend.setBackground(new Color(143, 188, 143));
       *  jcbTools.setBackground(new Color(143, 188, 143));
       *  jrbLatLon.setBackground(new Color(189, 183, 107));
       *  jrbAlbers.setBackground(new Color(189, 183, 107));
       *  jrbRadar.setBackground(new Color(189, 183, 107));
       *  jrbStereo.setBackground(new Color(189, 183, 107));
       *  jrbHRAP.setBackground(new Color(189, 183, 107));
       *  jrbDMS.setBackground(new Color(143, 188, 143));
       *  jrbDeci.setBackground(new Color(143, 188, 143));
       *  jrbKM.setBackground(new Color(189, 183, 107));
       *  jrbMiles.setBackground(new Color(189, 183, 107));
       *  jrbNauticalMi.setBackground(new Color(189, 183, 107));
       */
   }


   
   public void setSelectedSymbol(String symbolName) {
      jcomboSymbol.setSelectedItem(symbolName);
   }
   
   
   
   /**
    * Set the current file and refresh the listModel
 * @throws SQLException 
 * @throws ParserConfigurationException 
 * @throws IOException 
 * @throws SAXException 
 * @throws XPathExpressionException 
 * @throws NumberFormatException 
 * @throws ParseException 
    */
   public void refreshMatchingFileList() 
       throws DecodeException, SQLException, NumberFormatException, XPathExpressionException, SAXException, IOException, ParserConfigurationException, ParseException {

      FileScanner nexradFile = new FileScanner();
      // Refresh JList
      ScanResults[] scanResults = dataSelector.getScanResults();
      listModel.clear();
      matchingFiles.clear();

      int count = 0;
      listModel.add(count++, " None");
      matchingFiles.add("None");
      //String curFileTimeStamp = NexradFile.quickScan(viewer.getCurrentNexradFilename())[NexradFile.RETURN_ARRAY_ELEMENT_TIMESTAMP];
      String[] fileInfo;

      nexradFile.scanURL(viewer.getCurrentDataURL());
      String curFileTimeStamp = nexradFile.getLastScanResult().getTimestamp();

      
      int fileType = 0;
      
      for (int i = 0; i < scanResults.length; i++) {


//         try {
//            if (scanResults[i].getFileType() == FileScanner.NEXRAD_LEVEL3_NWS) {                              
//               nexradFile.scanFile(dataSelector.getDataSelectorPanel().getSelectedURL(i));
//               fileInfo = nexradFile.getReturnArray();                 
//            }
//         } catch (Exception e) {
//            e.printStackTrace();
//            javax.swing.JOptionPane.showMessageDialog(null, "Error reading matching Level-III files", 
//               "ATTRIBUTE TABLE ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);            
//         }


//System.out.println("CUR TIME STAMP:"+curFileTimeStamp+"---- NX FILE TS:"+fileInfo[NexradFile.RETURN_ARRAY_ELEMENT_TIMESTAMP]);
    	  if (scanResults[i].getDataType() == SupportedDataType.NEXRAD_LEVEL3_NWS) {  
    		  return;
    	  }
    	  
         if (curFileTimeStamp.equals(scanResults[i].getTimestamp())) {
            String product = scanResults[i].getProductID();
            if (!(product.equals("NHI") || product.equals("NME") || product.equals("NTV") ||
                  product.equals("NSS") || product.equals("NWP") || product.equals("RCM") ||
                  product.equals("SPD") || product.equals("IRM") || product.equals("NST") ||
                  product.equals("NVW") || product.equals("GSM") || product.equals("NMD") ||
                  product.equals("RSL"))) {
               matchingFiles.add(scanResults[i].getUrl().toString());
               String name = scanResults[i].getDisplayName();
               listModel.add(count++, name);
            }
         }
      }

   }


   // Implementation of ActionListener interface.
   /**
    *  Description of the Method
    *
    * @param  event  Description of the Parameter
 * @throws IOException 
    */
   public void actionPerformed(ActionEvent event) {

      Object source = event.getSource();

      if (source == jcbID) {
         viewer.setShowAlphanumericLabels(jcbID.isSelected());

      }
      else if (source == jbColor) {
         Color newColor = JColorChooser.showDialog(AlphaProperties.this,
               "Choose Alphanumeric Color", jbColor.getBackground());
         if (newColor != null) {
            jbColor.setBackground(newColor);
            if (jcomboTrans.getSelectedIndex() >= 0) {
               Color c = new Color(newColor.getRed(), newColor.getGreen(), newColor.getBlue());
               //,
               //(((100-(10*jcomboTrans.getSelectedIndex()))*255)/100));
               viewer.setAlphanumericLineColor(c);
               viewer.setAlphanumericFillColor(c);
            }
            else {
               viewer.setAlphanumericLineColor(newColor);
               viewer.setAlphanumericFillColor(newColor);
            }
         }
      }
      else if (source == jcomboSize) {
         viewer.setAlphanumericLineWidth(jcomboSize.getSelectedIndex() + 1);
         viewer.setAlphanumericLabelSize(jcomboSize.getSelectedIndex() + 1);
         viewer.setShowAlphanumericPoints(true);
         if (jcbID.isSelected()) {
            viewer.setShowAlphanumericLabels(true);
         }
      }
      else if (source == jcomboSymbol) {
         viewer.setAlphanumericSymbol(symbols[jcomboSymbol.getSelectedIndex()]);
         viewer.setShowAlphanumericPoints(true);
         if (jcbID.isSelected()) {
            viewer.setShowAlphanumericLabels(true);
         }
      }
      else if (source == jcomboTrans) {
         viewer.setAlphanumericTransparency(Double.parseDouble((String) jcomboTrans.getSelectedItem()) / 100.0);
         viewer.setShowAlphanumericPoints(true);
         if (jcbID.isSelected()) {
            viewer.setShowAlphanumericLabels(true);
         }
      }
      else if (source == jcbHalo) {
         viewer.setAlphaHalo(jcbHalo.isSelected());
      }
      
      else if (source == jbTable) {
         AlphaAttributeTable attributeTable = new AlphaAttributeTable(viewer);
      }
      else if (source == jbTextP1) {
    	  try {
    		  WCTTextDialog suppframe1 = new WCTTextDialog(viewer, viewer.getAlphanumericSupplementalArray()[0]);
    	  } catch (Exception e) {
    		  JOptionPane.showMessageDialog(this, "Error getting supplemental data...\n " + e.toString(),
    		  		"Data Load Error", JOptionPane.ERROR_MESSAGE);
    	  }
      }
      else if (source == jbTextP2) {
    	  try {
    		  WCTTextDialog suppframe1 = new WCTTextDialog(viewer, viewer.getAlphanumericSupplementalArray()[1]);
    	  } catch (Exception e) {
    		  JOptionPane.showMessageDialog(this, "Error getting supplemental data...\n " + e.toString(),
    		  		"Data Load Error", JOptionPane.ERROR_MESSAGE);
    	  }
      }
      else if (source == jbLoad) {
          loadGraphicBackground();
      }
      else if (source == jbExport) {
         exportAlphanumeric();
      }
   }

   
   private void loadGraphicBackground() {
       try {
         if (fileList.getSelectedIndex() == -1) {
            return;
         }
         else if (fileList.getSelectedIndex() == 0) {
            viewer.clearNexradAlphaBackground();
         }
         else {
            int index = fileList.getSelectedIndex();
            //viewer.loadNexradFile((String) matchingFiles.elementAt(index), true, false, true);
            jbLoad.setEnabled(false);
            //LoadDataThread loadData = new LoadDataThread(viewer, (String) matchingFiles.elementAt(index),
            LoadDataThread loadData = new LoadDataThread(viewer, new URL((String) matchingFiles.elementAt(index)),
               false, new Component[] { jbLoad }, false, true);
            loadData.start();
            jbLoad.setEnabled(true);

            viewer.validateAll();
         }
       } catch (Exception e) {
           e.printStackTrace();
           javax.swing.JOptionPane.showMessageDialog(this, "Error loading background image: \n"+e, 
                   "DISPLAY ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);                  
       }

   }
   
   // END actionPerformed

   /**
    *  Description of the Method
    */
   private void exportAlphanumeric() {

      String saveDirectory = WCTProperties.getWCTProperty("jne_export_dir");
      // Set up File Chooser
      JFileChooser fc = new JFileChooser(saveDirectory);
      OpenFileFilter shpFilter = new OpenFileFilter("shp", true, "'.shp' ESRI Shapefile");
      OpenFileFilter gmlFilter = new OpenFileFilter("gml", true, "'.gml' GML (Geographic Markup Language)");
      OpenFileFilter wktFilter = new OpenFileFilter("txt", true, "'.txt' WKT (Well Known Text)");
      OpenFileFilter csvFilter = new OpenFileFilter("csv", true, "'.csv' CSV (Comma Delimited)");
      fc.addChoosableFileFilter(shpFilter);
//      fc.addChoosableFileFilter(gmlFilter);
      fc.addChoosableFileFilter(wktFilter);
      fc.addChoosableFileFilter(csvFilter);
      fc.setAcceptAllFileFilterUsed(false);
      fc.setFileFilter(shpFilter);
      
      int returnVal = fc.showSaveDialog(viewer);
      File file = null;
      if (returnVal == JFileChooser.APPROVE_OPTION) {
         int choice = JOptionPane.YES_OPTION;
         // intialize to YES!
         file = fc.getSelectedFile();
         String fstr = file.toString();
         File extfile = null;
         String extension;
         // Check the typed or selected file for an extension
         if (fc.getFileFilter() == shpFilter) {
            extension = ".shp";
         }
         else if (fc.getFileFilter() == wktFilter) {
             extension = ".txt";
         }
         else if (fc.getFileFilter() == gmlFilter) {
             extension = ".gml";
         }
         else {
            extension = ".csv";
         }

         // Add extension if not present
         if (!fstr.substring((int) fstr.length() - 4, (int) fstr.length()).equals(extension)) {
            extfile = new File(file + extension);
         }
         else {
            // If extension is present...
            extfile = new File(file.toString());
            file = new File(fstr.substring(0, (int) fstr.length() - 4));
         }

         // Check to see if file exists
         if (extfile.exists()) {
            String message = "The output file \n" +
                  "<html><font color=red>" + extfile + "</font></html>\n" +
                  "already exists.\n\n" +
                  "Do you want to proceed and OVERWRITE?";
            choice = JOptionPane.showConfirmDialog(null, (Object) message,
                  "OVERWRITE FILE", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
            fstr = file.toString();
            //file = new File(fstr.substring(0, (int) fstr.length() - 4));
         }

         if (choice == JOptionPane.YES_OPTION) {
            
            saveDirectory = file.getParent().toString();
            WCTProperties.setWCTProperty("jne_export_dir", saveDirectory);
      
            // Save as shapefile
            if (fc.getFileFilter() == shpFilter) {
               System.out.println("Saving: " + extfile);
               try {               
                  NexradAlphaExport.saveShapefile(file, viewer.getAlphanumericDecoder());
                  NexradAlphaExport.saveLineShapefile(file, viewer.getAlphanumericDecoder());
               } catch (Exception e) {
                   e.printStackTrace();
                   javax.swing.JOptionPane.showMessageDialog(this, "No Alphanumeric Data Present", 
                     "EXPORT ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);                  
               }
            }
            else if (fc.getFileFilter() == wktFilter) {
                System.out.println("Saving: " + extfile);
                try {               
                   NexradAlphaExport.saveWKT(file, viewer.getAlphanumericDecoder());
                   NexradAlphaExport.saveLineWKT(file, viewer.getAlphanumericDecoder());
                } catch (Exception e) {
                    e.printStackTrace();
                    javax.swing.JOptionPane.showMessageDialog(this, "No Alphanumeric Data Present", 
                      "EXPORT ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);                  
                }
            }
            else if (fc.getFileFilter() == gmlFilter) {
                System.out.println("Saving: " + extfile);
                try {               
                   NexradAlphaExport.saveGML(file, viewer.getAlphanumericDecoder());
                   NexradAlphaExport.saveLineGML(file, viewer.getAlphanumericDecoder());
                } catch (Exception e) {
                    e.printStackTrace();
                    javax.swing.JOptionPane.showMessageDialog(this, "No Alphanumeric Data Present", 
                      "EXPORT ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);                  
                }
            }
            else {
               // Do the CSV file
               System.out.println("Saving: " + extfile);
               try {               
                   NexradAlphaExport.saveCSV(file, viewer.getAlphanumericDecoder());
               } catch (Exception e) {
                   e.printStackTrace();
                   javax.swing.JOptionPane.showMessageDialog(this, " - No Alphanumeric Data Present -", 
                     "EXPORT ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);                  
               }
            }
         }
         // END if(choice == YES_OPTION)
      }
   }
   // END exportAlphanumeric()

}

