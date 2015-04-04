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

package gov.noaa.ncdc.wms;

import java.awt.BorderLayout;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

//public class WMSLayerSelector extends NexradFrame {
public class WMSLayerSelector extends JPanel {

   private String title;
   private String[] layers = null;
   private JCheckBox[] jcbLayers = null;
   
   
   public WMSLayerSelector(String title, String server) throws ParserConfigurationException, SAXException, IOException, Exception {
      super();
      this.title = title;
      
      //super(title);
      this.layers = WMSData.getLayerList(server);   
      createGUI();
      //pack();
      setVisible(true);
      setLocation(15, 15);
   }
   
   private void createGUI() {

      JPanel mainPanel = new JPanel();
      mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
      
      jcbLayers = new JCheckBox[layers.length];
      for (int i=0; i<layers.length; i++) {
         
         // Don't add repeated layer names!
         boolean foundMultiple = false;
         for (int n=0; n<i; n++) {
            if (jcbLayers[n] != null && layers[i].equals(jcbLayers[n].getText())) {
               foundMultiple = true;
            }
         }
         if (! foundMultiple) {
            
            
System.out.println("LAYER GUI -- CREATING LAYER "+i+" = "+layers[i]);

            jcbLayers[i] = new JCheckBox(layers[i], false);
            jcbLayers[i].setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
            mainPanel.add(jcbLayers[i]);
         }
                  
      }

      //this.getContentPane().add(mainPanel);
      
      this.setLayout(new BorderLayout());
      this.add(new JLabel(title, JLabel.CENTER), "North");
      this.add(mainPanel, "Center");
   }
   
   
   
   public String getSelectedLayersString() {

      boolean firstTime = true;
      String layersString = "";
      for (int i=0; i<jcbLayers.length; i++) {
         if (jcbLayers[i] != null && jcbLayers[i].isSelected()) {
            if (! firstTime) {
               layersString += ",";
            }
            
            layersString += (jcbLayers[i].getText());
            
            if (firstTime) {         
               firstTime = false;
            }
         }
      }      
      layersString = layersString.replaceAll(" ", "%20");
      
System.out.println("------------------------------- RETURN STRING FROM WMS-LAYER-SELECTOR -------------");
System.out.println("       "+layersString);
System.out.println("------------------------------- RETURN STRING FROM WMS-LAYER-SELECTOR -------------");

      return layersString;
   }
   
   
   public void setSelectedLayers(String selectedLayers) {
      // Remove tailing comma if necessary
      if (selectedLayers.charAt(selectedLayers.length()-1) == ',') {
         selectedLayers = selectedLayers.substring(0, selectedLayers.length()-1); 
      }
      
      String[] layers = selectedLayers.split(",");
      for (int i=0; i<layers.length; i++) {
         layers[i] = layers[i].replaceAll("%20", " ");
         for (int n=0; n<jcbLayers.length; n++) {
          
            if (jcbLayers[n] != null && layers[i].equals(jcbLayers[n].getText())) {
               jcbLayers[n].setSelected(true);
               n = jcbLayers.length; // stop looking
            }
         }
      }
   }
   
}
