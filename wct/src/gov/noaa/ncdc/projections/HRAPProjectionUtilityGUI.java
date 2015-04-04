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

//***********************************************************
// HRAPProjection.java     Author: Steve Ansari
/**
 * Converts Geographic Latitude and Longitude to and from
 * Polar Stereographic HRAP Coordinates. <br><br>
 *
 * All transformations assume Spherical Earth Datum.  (This is ok
 * for NEXRAD calculations because NEXRAD Precipitation Processing
 * also uses Spherical Earth model)
 *
 * All input angles must be in decimal degrees.<br>
 * Output Polar Stereographic Coordinates are in HRAP grid cell units.<br>
 * Earth Radius is defined as 6372000 meters.<br>
 *
 * @author    Steve Ansari
 */

package gov.noaa.ncdc.projections;

import gov.noaa.ncdc.common.OpenFileFilter;
import gov.noaa.ncdc.wct.ui.WCTFrame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;

//************************************************************
/**
 *  Description of the Class
 *
 * @author    steve.ansari
 */
public class HRAPProjectionUtilityGUI implements ActionListener {

   private HRAPUtility util = new HRAPUtility();
   
   
   private JTextField jtfOutputFile;
   private DefaultListModel listModel;
   private JList jlistInputFiles;
   private JButton inputBrowseButton, outputBrowseButton, submitButton;
   private JPanel inputPanel;
   private JFrame frame;
   private JProgressBar progress = new JProgressBar(0, 100);
   
               


   public HRAPProjectionUtilityGUI() {
      createGUI();
   }
   
   
   private void createGUI() {
      JFrame frame = new WCTFrame("HRAP Projection Utility");
      
      JPanel mainPanel = new JPanel();
      mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
      
      inputPanel = new JPanel();
      JPanel browseInputPanel = new JPanel();
      inputBrowseButton = new JButton("Browse");
      inputBrowseButton.addActionListener(this);
      listModel = new DefaultListModel();
      jlistInputFiles = new JList(listModel);
      JScrollPane inputFilesPane = new JScrollPane(jlistInputFiles);
      inputPanel.add(inputBrowseButton);
      inputPanel.add(inputFilesPane);
      inputPanel.setBorder(myTitledBorder("Input Shapefile(s)"));

      JPanel outputPanel = new JPanel();
      outputBrowseButton = new JButton("Browse");
      outputBrowseButton.addActionListener(this);
      jtfOutputFile = new JTextField(25);
      outputPanel.add(outputBrowseButton);
      outputPanel.add(jtfOutputFile);
      outputPanel.setBorder(myTitledBorder("Output Directory"));
      
      String message = "<html>Directions:<br><br> "+
         "This will generate a WGS84, NAD83, HRAP or Polar Stereographic <br> "+
         "Polygon shapefile that represents the grid cells of the HRAP Grid.<br>"+
         "<br>"+
         "<br>"+
         "";
      JLabel jlInstructions = new JLabel(message);
      JPanel instructionsPanel = new JPanel();
      instructionsPanel.add(jlInstructions);
      
      
      progress.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
      progress.setStringPainted(true);
      progress.setString("Progress");
      
      submitButton = new JButton("Submit");
      submitButton.addActionListener(this);
      JPanel submitPanel = new JPanel();
      submitPanel.add(submitButton);
      
      mainPanel.add(instructionsPanel);
      mainPanel.add(inputPanel);
      mainPanel.add(outputPanel);
      mainPanel.add(progress);
      mainPanel.add(submitPanel);
   
      
      frame.getContentPane().add(mainPanel);
      frame.pack();
      frame.setSize(480, 550);
      frame.show();
      

   }      
    
 // We can do this because the same border object can be reused.
    
    private Border myTitledBorder(String title) {
       Border empty10Border = BorderFactory.createEmptyBorder(10,10,10,10);
       Border etchedBorder  = BorderFactory.createEtchedBorder();
       
       return BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(etchedBorder, title), 
            empty10Border);
               
    }//end myTitledBorder  
   
   
    
   public void actionPerformed(ActionEvent evt) {
      Object source = evt.getSource();
      if (source == submitButton) {
            
         try {
            
            submitButton.setEnabled(false);
            
            progress.setMinimum(0);
            progress.setMaximum(listModel.size());
            
            foxtrot.Worker.post(new foxtrot.Task() {
               public Object run() {
                  
                  try {
                     
                     for (int n=0; n<listModel.size(); n++) {
                        progress.setValue(n+1);
                        System.out.println("PROCESSING: "+listModel.get(n));
                        File inFile = new File(listModel.get(n).toString());
                        File outDir = new File(jtfOutputFile.getText());
                        util.convertShpFile(inFile, outDir, util.HRAP);
                     }
                     
                     progress.setString("Completed Export of " + listModel.size() + " files");
                     try {
                        Thread.sleep(1500);
                     } catch (Exception e) {}
                     submitButton.setEnabled(true);
                     progress.setValue(0);
                     progress.setString("Conversion Progress");
               
                        
                  } catch (Exception e) {
                     e.printStackTrace();
                  }
               
                  return "DONE";
               }
            });
      
      //Shp2KML.process(new File(jtfInputFile.getText()), new File(jtfOutputFile.getText()), 
            //   jtfTitle.getText(), textArea.getText());
               
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
      else if (source == inputBrowseButton) {

         // Set up File Chooser
         JFileChooser fc = new JFileChooser();
         fc.setDialogTitle("Choose Input Shapefile");
         fc.addChoosableFileFilter(new OpenFileFilter("shp", true, "ESRI Shapefiles (.shp)"));
         fc.setMultiSelectionEnabled(true);
         
         int returnVal = fc.showOpenDialog(inputBrowseButton);
         if (returnVal == JFileChooser.APPROVE_OPTION) {
            File[] files = fc.getSelectedFiles();
            
            for (int i = 0; i < files.length; i++) {
               listModel.addElement(files[i].toString());
            }
            
            // Set default output directory to the same as the input files
            jtfOutputFile.setText(files[0].getParent());
         }
         
      }
      else if (source == outputBrowseButton) {

         // Set up File Chooser
         JFileChooser fc = new JFileChooser();
         fc.setDialogTitle("Choose Output Directory");
         fc.addChoosableFileFilter(new OpenFileFilter("lkajsdf;lkejrfasf", true, "Directories Only"));
         fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

         int returnVal = fc.showSaveDialog(inputBrowseButton);
         if (returnVal == JFileChooser.APPROVE_OPTION) {
            String outFile = fc.getSelectedFile().toString();
            jtfOutputFile.setText(outFile);
         }
      }
   }

    
    
   
   
   /**
    *  Description of the Method
    */
   public void showDialog() {

      try {
         // get the shapefile URL by either loading it from the file system
         // or from the classpath
         URL shapeURL = null;
         JFileChooser fileChooser = new JFileChooser();
         fileChooser.setDialogTitle("Choose a WGS84/NAD83 shapefile to convert.");
         fileChooser.setFileFilter(new OpenFileFilter("shp", true, "Shapefile"));
         fileChooser.setMultiSelectionEnabled(true);

         int result = fileChooser.showOpenDialog(null);

         if (result == JFileChooser.APPROVE_OPTION) {
            File[] f = fileChooser.getSelectedFiles();

            for (int i = 0; i < f.length; i++) {

               System.out.println("PROCESSING " + i);

               util.convertShpFile(f[i], util.HRAP);
                        
            }

         }

      } catch (Exception e) {
         e.printStackTrace();
      }

   }
   



   /**
    *  The main program for the HRAPProjectionUtility class
    *
    * @param  args  The command line arguments
    */
   public static void main(String[] args) {
      HRAPProjectionUtilityGUI util = new HRAPProjectionUtilityGUI();

      //util.showDialog();

   }

}

