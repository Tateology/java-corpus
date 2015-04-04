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

import gov.noaa.ncdc.nexradiv.legend.JNXLegend;
import gov.noaa.ncdc.nexradiv.legend.Q2Legend;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeQ2;
import gov.noaa.ncdc.wct.ui.WCTFrame;
import gov.noaa.ncdc.wct.ui.WCTViewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;


public class Q2Properties extends WCTFrame {

   private JList heightList = new JList();
   private JList variableList = new JList();
   private DefaultListModel heightListModel = new DefaultListModel();
   private DefaultListModel variableListModel = new DefaultListModel();
   private JButton loadButton = new JButton("Load");
   
   private float[] heightData;
   private String[] variables;
   
   private WCTViewer nexview;
   private URL q2Url;
   private String units;
   
   private DecodeQ2 decodeQ2;
   
   private JPanel metaPanel;
   private JLabel heightLevelLabel = new JLabel();
   private JLabel heightDataLabel = new JLabel();
   private JLabel variableLabel = new JLabel();
   private JNXLegend keyPanel;
  
   private JPanel levelPanel;
   private JPanel variablePanel;
   private JSplitPane splitPane;
         
   
   public Q2Properties(WCTViewer nexview) {
      super("Q2 Properties");
      createGUI();
      setSize(275, 300);
      splitPane.setDividerLocation(0.5);
      
      this.nexview = nexview;
      
   }


   private void createGUI() {
      
      levelPanel = new JPanel();
      variablePanel = new JPanel();
      splitPane = new JSplitPane();
      
      loadButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            nexview.loadFile(q2Url);            
         }
      });
      
      levelPanel.setLayout(new BorderLayout());
      levelPanel.add(new JLabel("Choose Height Level", JLabel.CENTER), BorderLayout.NORTH);
      levelPanel.add(new JScrollPane(heightList), BorderLayout.CENTER);
      variablePanel.setLayout(new BorderLayout());
      variablePanel.add(new JLabel("Choose Grid Variable", JLabel.CENTER), BorderLayout.NORTH);
      variablePanel.add(new JScrollPane(variableList), BorderLayout.CENTER);
      
      splitPane.setLeftComponent(new JScrollPane(levelPanel));
      splitPane.setRightComponent(new JScrollPane(variablePanel));
      splitPane.setDividerLocation(0.5);
      
      this.getContentPane().setLayout(new BorderLayout());
      this.getContentPane().add(splitPane, BorderLayout.CENTER);
      this.getContentPane().add(loadButton, BorderLayout.SOUTH);
      
      
   }
   
   
   public void refreshHeightList(DecodeQ2 q2) throws IOException {
      
      System.out.println("REFRESH HEIGHT LIST");
      
      q2Url = q2.getUrl();
      this.decodeQ2 = q2;
      
      int selectedIndex = heightList.getSelectedIndex();
      float[] newHeightData = q2.getHeightData();
      boolean changed = false;

      // Height data only applies to 3D products
      if (newHeightData == null) {
         heightData = null;
         heightListModel.clear();  
         heightLevelLabel.setText("   HEIGHT LEVEL: N/A");
         heightDataLabel.setText("   HEIGHT: N/A");
         return;
      }

      //for (int n=0; n<newHeightData.length; n++) {
      //   System.out.println("newHeightData["+n+"] = "+newHeightData[n]);
      //}
      
      if (heightData == null) {
         heightData = newHeightData;
         changed = true;
      }
      else {
         //for (int n=0; n<heightData.length; n++) {
         //   System.out.println("heightData["+n+"] = "+heightData[n]);
         //}
      }
      
      
      for (int n=0; n<newHeightData.length; n++) {
         if (newHeightData[n] != heightData[n]) {
            changed = true;
         }
      }
      
      if (changed) {
         System.out.println("CHANGED LEVELS!!!");
         heightListModel.clear();
         for (int n=0; n<heightData.length; n++) {
            heightListModel.add(n, "   "+n+" :: "+heightData[n]+" m");
         }
         heightList.setModel(heightListModel);
      }
      if (selectedIndex < 0) {
         selectedIndex = 0;
      }
      heightList.setSelectedIndex(selectedIndex);
      

      heightLevelLabel.setText("   HEIGHT LEVEL: "+selectedIndex);
      heightDataLabel.setText("   HEIGHT: "+newHeightData[selectedIndex]+" meters");


      levelPanel.setPreferredSize(new Dimension(50, 50));
      splitPane.setDividerLocation(0.5);
      
   }

   
   public void refreshVariableList(DecodeQ2 q2) throws IOException {
      
      System.out.println("REFRESH VARIABLE LIST");
      
      q2Url = q2.getUrl();
      this.decodeQ2 = q2;
      
      int selectedIndex = variableList.getSelectedIndex();
      String[] newVariables = q2.getVariableNames();
      boolean changed = false;
      

      //for (int n=0; n<newVariables.length; n++) {
      //   System.out.println("newVariables["+n+"] = "+newVariables[n]);
      //}

      if (variables == null) {
         variables = newVariables;
         changed = true;
      }
      else {
         for (int n=0; n<variables.length; n++) {
            System.out.println("variables["+n+"] = "+variables[n]);
         }
      }
      
      
      
      if (variables.length != newVariables.length) {
         //System.out.println("LENGTH DOESN'T MATCH");
         changed = true;   
      }
      else {
         for (int n=0; n<newVariables.length; n++) {
            if (! newVariables[n].equals(variables[n])) {
               changed = true;
            }
         }
      }
      
      if (changed) {
         System.out.println("CHANGED VARIABLES!!!");
         variableListModel.clear();
         for (int n=0; n<newVariables.length; n++) {
            variableListModel.add(n, newVariables[n]);
         }
         variableList.setModel(variableListModel);
         selectedIndex = 0;
         variables = newVariables;
      }
      
      if (selectedIndex < 0) {
         selectedIndex = 0;
      }
      variableList.setSelectedIndex(selectedIndex);
      
      variableListModel.removeElement("Height");

      variableLabel.setText("   VARIABLE: "+newVariables[selectedIndex]);


      levelPanel.setPreferredSize(new Dimension(50, 50));
      splitPane.setDividerLocation(0.5);

      
   }


   
   
   public int getSelected3DHeightIndex() {
      return heightList.getSelectedIndex();
   }
   
   public String getSelectedVariableName() {
      return variableList.getSelectedValue().toString();
   }

   public void pack() {   
      //super.pack();
      this.setSize(new Dimension(275, 300));
      splitPane.setDividerLocation(0.5);
   }
   
   
   
   
   
   
   
   
   
   
   
   
      
//   public JPanel getMetaPanel() {
//      if (metaPanel == null) {
//         makeMetaPanel();
//      }
//      return this.metaPanel;      
//   }
   
   
//   private void makeMetaPanel() {
//      
//      DecimalFormat fmt02 = new DecimalFormat("00");
//      Calendar cal = decodeQ2.getCreationDate();
//      //cal.setTimeInMillis(1146096000L*1000L);
//      String timestamp = (cal.get(Calendar.MONTH)+1) + "/" +
//         fmt02.format(cal.get(Calendar.DATE)) + "/" +
//         fmt02.format(cal.get(Calendar.YEAR)) + " " +
//         fmt02.format(cal.get(Calendar.HOUR)) + ":" +
//         fmt02.format(cal.get(Calendar.MINUTE)) + " GMT";
//      
//      int heightLevel = decodeQ2.getLastDecodedHeightLevel();
//      float[] heightData = decodeQ2.getHeightData();
//      
//      if (heightLevelLabel == null) {
//         heightLevelLabel = new JLabel("   HEIGHT LEVEL: "+heightLevel);
//      }
//      if (heightDataLabel == null) {
//         heightDataLabel = new JLabel("   HEIGHT: "+heightData[heightLevel]);
//      }
//         
//      
//      JPanel panel = new JPanel();
//      panel.setLayout(new GridLayout(11, 1));
//      panel.add(new JLabel("  NSSL NATIONAL MOSAIC"));
//      panel.add(new JLabel(" "));
//      panel.add(new JLabel("   "+timestamp));
//      panel.add(new JLabel(" "));
//      panel.add(variableLabel);
//      panel.add(heightLevelLabel);
//      panel.add(heightDataLabel);
//      panel.add(new JLabel(" "));
//      panel.add(new JLabel(" "));
//      panel.add(new JLabel(" "));
//      panel.add(new JLabel(" "));
//      
//      panel.setBackground(new Color(220, 220, 220));
//
//      metaPanel = panel;
//   }
   
   
   public JNXLegend getKeyPanel() {
      if (keyPanel == null) {
         makeKeyPanel();
      }
      return this.keyPanel;      
   }
   
   
   private void makeKeyPanel() {
      //keyPanel = new JPanel() {
      //};
      
      keyPanel = new Q2Legend();
      
      
      keyPanel.setBackground(new Color(220, 220, 220));
      keyPanel.setDoubleBuffered(true);
   }   
   
}

