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

import gov.noaa.ncdc.nexradiv.OpenFileFilter;
import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.WCTProperties;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.ui.WCTTextPanel.SearchBarProperties;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.apache.commons.io.FileUtils;

import ucar.nc2.FileWriter2;
import ucar.nc2.FileWriter2.FileWriterProgressListener;
import ucar.nc2.NetcdfFile;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.ncml.NcMLReader;

public class NcMLEditor extends WCTFrame {


	private static final long serialVersionUID = -727929739275254078L;

	private WCTViewer viewer = null;
	
	private WCTTextPanel ncmlPanel = null;
	
	
	
	   
	public NcMLEditor(String title, WCTViewer viewer) {
		super(title);
		this.viewer = viewer;
		
		createGUI();
	}

	
	private void createGUI() {
		JTabbedPane tabPane = new JTabbedPane();
		
		String ncmlString = DataSelectionUtils.getDumpString(gov.noaa.ncdc.wct.ui.DataSelectionUtils.Type.NCML, viewer, 
				viewer.getDataSelector(), viewer.getDataSelector().getSelectedURLs()[0], true);
		ncmlPanel = new WCTTextPanel(viewer, ncmlString, SearchBarProperties.SEARCH_BAR_FULL);
		

		String cdlString = DataSelectionUtils.getDumpString(gov.noaa.ncdc.wct.ui.DataSelectionUtils.Type.CDL, viewer, 
				viewer.getDataSelector(), viewer.getDataSelector().getSelectedURLs()[0], true);
		WCTTextPanel cdlPanel = new WCTTextPanel(viewer, cdlString, SearchBarProperties.SEARCH_BAR_FULL);
		cdlPanel.getTextArea().setEditable(false);
		
		tabPane.addTab("NcML (Editable)", ncmlPanel);
		tabPane.addTab("CDL (View Only)", cdlPanel);
		
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(tabPane, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		JButton jbDisplayNcml = new JButton("Load Data from NcML");
		jbDisplayNcml.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				loadFromNcml();
			}			
		});
		JButton jbSaveToNetCDF = new JButton("Save to NetCDF");
		jbSaveToNetCDF.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				saveToNetCDF();
			}						
		});
		
		JButton jbSaveNcml = new JButton("Save NcML");
		JButton jbBrowseToNcmlDocs = new JButton("Browse NcML Documentation");
		buttonPanel.add(jbDisplayNcml);
		buttonPanel.add(jbSaveNcml);
		buttonPanel.add(jbSaveToNetCDF);
		buttonPanel.add(jbBrowseToNcmlDocs);
		
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
	}
	
	
	private void loadFromNcml() {
		
		try {
//			NetcdfDataset ncd = NcMLReader.readNcML(new StringReader(ncmlPanel.getTextArea().getText()), 
//					WCTUtils.getSharedCancelTask());
			
			String filename = "tmp-ncml-"+System.currentTimeMillis()%1000000+".ncml";
			File tmpNcmlFile = new File(WCTConstants.getInstance().getCacheLocation()+
					File.separator+filename);
			FileUtils.writeStringToFile(tmpNcmlFile, ncmlPanel.getTextArea().getText());
			viewer.loadFile(tmpNcmlFile.toURI().toURL());
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void saveToNetCDF() {
		try {


			String saveDirectory = WCTProperties.getWCTProperty("jne_export_dir");
		      JFileChooser fc = new JFileChooser(saveDirectory);
		      int returnVal = fc.showSaveDialog(viewer);
		      OpenFileFilter ncFilter = new OpenFileFilter(".nc", true, "'.nc' NetCDF 3 format");
		      fc.addChoosableFileFilter(ncFilter);
		      fc.setFileFilter(ncFilter);

		      File file = null;
		      if (returnVal == JFileChooser.APPROVE_OPTION) {
		         int choice = JOptionPane.YES_OPTION;
		         // intialize to YES!
		         file = fc.getSelectedFile();
		         String fstr = file.toString();
		         File extfile = null;
		         String extension;
		         // Check the typed or selected file for an extension
		         if (fc.getFileFilter() == ncFilter) {
		            extension = ".nc";
		         }
		         else {
		            extension = ".nc";
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
		      
		            if (fc.getFileFilter() == ncFilter) {
		               System.out.println("Saving: " + extfile);
		               try {
		            	   saveToNetCDF(extfile);
		               
		               } catch (Exception e) {
		                   e.printStackTrace();
		                   javax.swing.JOptionPane.showMessageDialog(this, "ERROR SAVING TO NETCDF", 
		                     "EXPORT ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);                  
		               }
		            }
		            else {
		               System.out.println("Saving: " + extfile);
		               try {
		            	   
		            	   saveToNetCDF(extfile);
		               
		               } catch (Exception e) {
		                   e.printStackTrace();
		                   javax.swing.JOptionPane.showMessageDialog(this, "ERROR SAVING TO NETCDF", 
		                     "EXPORT ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);                  
		               }
		            }
		         }
		      }
		      // END if(choice == YES_OPTION)

			
			
			
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void saveToNetCDF(File outfile) throws IOException {

	
		NetcdfDataset ncd = NcMLReader.readNcML(new StringReader(ncmlPanel.getTextArea().getText()), WCTUtils.getSharedCancelTask());
		NetcdfFile ncfile = FileWriter2.writeToFile(ncd, outfile.toString(), new ArrayList<FileWriterProgressListener>());
		ncfile.close();
			

	}
	
}


