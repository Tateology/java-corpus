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

import gov.noaa.ncdc.common.JTableUtils;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Alpha;
import gov.noaa.ncdc.wct.ui.WCTViewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Reader;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;

/**
 *  Creates JTable representation of attributes
 *  <b> IMPORTANT! <b> First 3 objects in FeatureType must be 
 * geom, lat, lon due to skipping the geometry
 * and number formating the lat and lon double values.
 * @author     steve.ansari
 * @created    August 9, 2004
 */
//public class AlphaAttributeTable extends gov.noaa.ncdc.nexrad.NexradFrame {
public class AlphaAttributeTable extends JDialog {
   private WCTViewer nexview;

   private JLabel jlTitle;
   private JButton closeButton, copyButton, copyTextButton;
   private JTable attributeTable;
   private String timestamp, type, siteID;

   DecimalFormat fmt3 = new DecimalFormat("0.000");

   //AtTablePrinter printer = new AtTablePrinter();
   



   /**
    *Constructor for the AlphaAttributeTable object
    *
    * @param  nexview  Description of the Parameter
    */
   public AlphaAttributeTable(WCTViewer nexview) {
      this(nexview, nexview.getCurrentNexradTimestamp(),
            nexview.getCurrentNexradTypeString(), nexview.getCurrentNexradSiteID());
   }


   /**
    *Constructor for the AlphaAttributeTable object
    *
    * @param  nexview    Description of the Parameter
    * @param  timestamp  Description of the Parameter
    * @param  type       Description of the Parameter
    * @param  siteID     Description of the Parameter
    */
   public AlphaAttributeTable(WCTViewer nexview, String timestamp, String type, String siteID) {
       //super("Alphanumeric Attribute Table");
       super(nexview, "Alphanumeric Attribute Table", false);
      this.nexview = nexview;
      this.timestamp = timestamp;
      this.type = type;
      this.siteID = siteID;
      createGUI();

      //printer.setDrawCellBorder(true);
      //printer.setRepeatHeader(true);
      //printer.setHeaderBold(true);
      //printer.setPreviewPaneSize(new Dimension(600, 400));

      pack();
      setVisible(true);
   }


   /**
    *  Description of the Method
    */
   private void createGUI() {

      // Set up title label
      jlTitle = new JLabel(siteID + " " + type + " " + timestamp, JLabel.CENTER);

      DecodeL3Alpha decoder = nexview.getAlphanumericDecoder();
      FeatureType schema = decoder.getFeatureTypes()[0];
      FeatureCollection features = decoder.getFeatures();
      Object[] objNameArray = new Object[schema.getAttributeCount() - 1];
      Object[][] objDataArray = new Object[features.size()][schema.getAttributeCount() - 1];
      for (int n = 0; n < schema.getAttributeCount() - 1; n++) {
         objNameArray[n] = schema.getAttributeType(n + 1).getName();
         // skip geometry attribute
      }
      FeatureIterator fi = features.features();
      Feature f = null;
      int i = 0;
      while (fi.hasNext()) {
         f = fi.next();
         for (int n = 0; n < schema.getAttributeCount() - 1; n++) {
            // skip geometry value
            if (n == 2 || n == 3) {
               objDataArray[i][n] = fmt3.format(Double.parseDouble(f.getAttribute(n + 1).toString()));
            }
            else {
               objDataArray[i][n] = f.getAttribute(n + 1);
            }
         }
         i++;
      }

      attributeTable = new JTable(objDataArray, objNameArray);
      //attributeTable.setRowSelectionAllowed(false);
      attributeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      attributeTable.setPreferredScrollableViewportSize(new Dimension(700, 100));
      //JFrame tableFrame = new JFrame("Attribute Table");

      //tableFrame.getContentPane().setLayout(new BorderLayout());
      getContentPane().setLayout(new BorderLayout());

      JPanel buttonPanel = new JPanel();
      copyTextButton = new JButton("Copy");
      copyTextButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
              Transferable t = new TextSelection(JTableUtils.getText(attributeTable));
              Toolkit.getDefaultToolkit().getSystemClipboard().setContents(t, null);
          }
      });
      
      copyButton = new JButton("Copy Html");
      copyButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
              Transferable t = new HtmlSelection(JTableUtils.getHtml(attributeTable));
              Toolkit.getDefaultToolkit().getSystemClipboard().setContents(t, null);
          }
      });

      closeButton = new JButton("Close");
      closeButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
              dispose();
          }
      });
      
      
      
      buttonPanel.add(copyButton);
      buttonPanel.add(copyTextButton);
      buttonPanel.add(closeButton);

      getContentPane().add(jlTitle, "North");
      getContentPane().add(new JScrollPane(attributeTable), "Center");
      getContentPane().add(buttonPanel, "South");

   }

   
   
   
   private class HtmlSelection implements Transferable {

       private ArrayList htmlFlavors = new ArrayList();
       private String html;

       public HtmlSelection(String html) {
           this.html = html;
           
           
           try {
               htmlFlavors.add(new DataFlavor("text/html;class=java.lang.String"));
               htmlFlavors.add(new DataFlavor("text/html;class=java.io.Reader"));
           } catch (ClassNotFoundException e) {
               e.printStackTrace();
           }
           
       }

       public DataFlavor[] getTransferDataFlavors() {
           return (DataFlavor[]) htmlFlavors.toArray(new DataFlavor[htmlFlavors.size()]);
       }

       public boolean isDataFlavorSupported(DataFlavor flavor) {
           return htmlFlavors.contains(flavor);
       }

       public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {

           if (String.class.equals(flavor.getRepresentationClass())) {
               return html;
           } else if (Reader.class.equals(flavor.getRepresentationClass())) {
               return new StringReader(html);
           }
           throw new UnsupportedFlavorException(flavor);

       }

   } 
   
   private class TextSelection implements Transferable {

       private ArrayList textFlavors = new ArrayList();
       private String text;

       public TextSelection(String text) {
           this.text = text;
           
           
           try {
               textFlavors.add(new DataFlavor("text/plain;class=java.lang.String"));
               textFlavors.add(new DataFlavor("text/plain;class=java.io.Reader"));
           } catch (ClassNotFoundException e) {
               e.printStackTrace();
           }
           
       }

       public DataFlavor[] getTransferDataFlavors() {
           return (DataFlavor[]) textFlavors.toArray(new DataFlavor[textFlavors.size()]);
       }

       public boolean isDataFlavorSupported(DataFlavor flavor) {
           return textFlavors.contains(flavor);
       }

       public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {

           if (String.class.equals(flavor.getRepresentationClass())) {
               return text;
           } else if (Reader.class.equals(flavor.getRepresentationClass())) {
               return new StringReader(text);
           }
           throw new UnsupportedFlavorException(flavor);

       }

   } 

}

