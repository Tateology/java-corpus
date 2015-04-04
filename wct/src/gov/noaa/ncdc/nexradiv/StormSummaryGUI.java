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

import gov.noaa.ncdc.wct.ui.WCTFrame;
import gov.noaa.ncdc.wct.ui.WCTViewer;
import gov.noaa.ncdc.wct.ui.filter.WCTFilterGUI;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.geotools.feature.FeatureCollection;
import org.geotools.gui.swing.tables.FeatureTableModel;
import org.geotools.map.DefaultMapLayer;
import org.geotools.styling.StyleBuilder;

public class StormSummaryGUI extends WCTFrame implements ActionListener {
   
   private JButton runButton, filterButton, cancelButton;
   private JPanel tablePanel;
   private JProgressBar progressBar = new JProgressBar(0, 100);
   private boolean cancel = false;
   
   private StyleBuilder sb = new StyleBuilder();
   
   private WCTViewer nexview;
   
   public StormSummaryGUI(WCTViewer nexview) {
      super("NEXRAD Storm Summary");
      this.nexview = nexview;
      createGUI();
   }
   
   private void createGUI() {
      getContentPane().setLayout(new BorderLayout());
      
      filterButton = new JButton("NEXRAD Filter");
      filterButton.addActionListener(this);
      runButton = new JButton("Run Summary");
      runButton.addActionListener(this);
      cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(this);
      
      JPanel buttonPanel = new JPanel();
      buttonPanel.add(filterButton);
      buttonPanel.add(runButton);
      buttonPanel.add(cancelButton);
      
      JPanel mainPanel = new JPanel();
      mainPanel.setLayout(new BorderLayout());
      mainPanel.add(buttonPanel, BorderLayout.NORTH);
      mainPanel.add(progressBar, BorderLayout.SOUTH);
      
      progressBar.setStringPainted(true);
      
      
      tablePanel = new JPanel();
      tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
      
      getContentPane().add(mainPanel, BorderLayout.NORTH);
      getContentPane().add(new JScrollPane(tablePanel), BorderLayout.CENTER);
   }
   

   public void actionPerformed(ActionEvent evt) {
      Object source = evt.getSource();
      if (source == runButton) {
         foxtrot.Worker.post(new foxtrot.Job() {
            public Object run() {

               runSummary();
               
               return "DONE";
            }
         });
      }
      else if (source == filterButton) {
         WCTFilterGUI nxfilterGUI = nexview.getFilterGUI();
         nxfilterGUI.setVisible(true);
         nxfilterGUI.setLocation(10, 20);
         nxfilterGUI.pack();
      }
      else if (source == cancelButton) {
         progressBar.setString("CANCELING...");
         cancel = true;
      }
   }
   
   
   protected void runSummary() {
      
      long starttime = System.currentTimeMillis();
      
      //Style style = sb.createStyle(sb.createPolygonSymbolizer(Color.black, Color.black, 1));
      //DefaultMapLayer nexradLayer = new DefaultMapLayer(nexview.getNexradFeatures(), style);
      Vector baseMapLayers = nexview.getBaseMapLayers();

      tablePanel.removeAll();
      
         
      for (int n=0; n<baseMapLayers.size(); n++) {
      //for (int n=0; n<5; n++) {
         DefaultMapLayer baseLayer = (DefaultMapLayer)(baseMapLayers.elementAt(n));
         
System.out.println("PROCESSING "+baseLayer.getTitle()+" ("+n+")");      


         long elapsedTime = ((System.currentTimeMillis()-starttime)/1000);


         progressBar.setString("PROCESSING: "+baseLayer.getTitle()+" ("+(n+1)+" / "+baseMapLayers.size()+")  Elapsed Time: "+elapsedTime);

         if ( (nexview.getLayerShapeType(n) == SimpleShapefileLayer.POINT || 
               nexview.getLayerShapeType(n) == SimpleShapefileLayer.MULTIPOINT ) &&
         
            baseLayer != null && baseLayer.isVisible()) {
               
               
            //FeatureCollection features = StormSummary.getIntersectingFeatures(nexview.getNexradFeatures(), baseLayer);
            //FeatureCollection features = StormSummary.getIntersectingFeatures(nexview.getCurrentExtent(), nexview.getNexradFeatures(), baseLayer);
            FeatureCollection features = StormSummary.getIntersectingJoinFeatures(
               nexview.getCurrentExtent(), nexview.getNexradFeatures(), baseLayer, progressBar.getModel());
            
System.out.println(nexview.getCurrentExtent());               
               
            if (features.size() > 0) {
            
               FeatureTableModel featureTableModel = new FeatureTableModel();
               featureTableModel.setFeatureCollection(features);
            
               JTable table = new JTable();
               table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
               table.setModel(featureTableModel);
               JScrollPane scrollPane = new JScrollPane(table);      
               scrollPane.setPreferredSize(new java.awt.Dimension(250, 130));
            
               
               tablePanel.add(new JLabel(baseLayer.getTitle()));
               tablePanel.add(scrollPane);
      //pack();
            }
         }
         
         if (cancel) {
            n = baseMapLayers.size();
            cancel = false;
         }
         
      }
      
      
      progressBar.setString("PROCESS TIME: "+((System.currentTimeMillis()-starttime)/1000)+" SEC");
      pack();
      setSize(800, 420);      
      validate();
   }

}

