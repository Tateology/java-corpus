package steve.test.grass;

import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.wct.ui.TextFieldWithBrowse;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

public class GEGrassInitDialog extends JDialog {

	private TextFieldWithBrowse tfbGrassInstallDir;
	private TextFieldWithBrowse tfbGrassGISDB;
	private TextFieldWithBrowse tfbGrassLocation;
	private TextFieldWithBrowse tfbGrassMapset; 

	
	private Frame parent = null;
	
	public GEGrassInitDialog(Frame parent) {
		super(parent, "GE-GRASS Initialization", true);
		this.parent = parent;
		createUI();
		pack();
		setLocationRelativeTo(parent);
//		setVisible(true);
	}
	
	private void createUI() {
		tfbGrassInstallDir = new TextFieldWithBrowse("GRASS Install Directory (ex: C:\\Program Files\\GRASS64)", 
						"gegrass-grassdir");
		tfbGrassGISDB = new TextFieldWithBrowse("GRASS Database Directory (GISDB)", "gegrass-gisdb");
		tfbGrassLocation = new TextFieldWithBrowse("GRASS Location", "gegrass-location");
		tfbGrassMapset = new TextFieldWithBrowse("GRASS Mapset", "gegrass-mapset");
		
		JButton goButton = new JButton("Ok");
		goButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (validateFields()) {
					setVisible(false);
				}
			}			
		});
		
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (validateFields()) {
					setVisible(false);
				}
			}			
		});
		
		this.getContentPane().setLayout(new RiverLayout());
		this.getContentPane().add(tfbGrassInstallDir, "hfill br");
		this.getContentPane().add(tfbGrassGISDB, "hfill br");
		this.getContentPane().add(tfbGrassLocation, "hfill br");
		this.getContentPane().add(tfbGrassMapset, "hfill br");
		this.getContentPane().add(goButton, "center br br");
	}
	
	private boolean validateFields() {
		StringBuilder message = new StringBuilder();
		if (tfbGrassInstallDir.getSelectedLocation().trim().length() == 0) {
			message.append("Please select the GRASS Installation Directory\n");
		}
		if (! new File(tfbGrassInstallDir.getSelectedLocation()).exists()) {
			message.append("The GRASS Installation Directory Does Not Exist\n");
		}
		if (tfbGrassGISDB.getSelectedLocation().trim().length() == 0) {
			message.append("Please select the GRASS Database (GISDB) Directory\n");
		}
		if (! new File(tfbGrassGISDB.getSelectedLocation()).exists()) {
			message.append("The GRASS Database Directory (GISDB) Does Not Exist\n");
		}
		if (tfbGrassLocation.getSelectedLocation().trim().length() == 0) {
			message.append("Please select the GRASS Location\n");
		}
		if (tfbGrassMapset.getSelectedLocation().trim().length() == 0) {
			message.append("Please select the GRASS Mapset\n");
		}
		if (message.toString().length() > 0) {
			JOptionPane.showMessageDialog(parent, message.toString(), "Initialization Errors", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
	public void configureGRASS(GRASSUtils grass) {
		if (validateFields()) {
			grass.setGrassINSTALL_DIR(tfbGrassInstallDir.getSelectedLocation());
			grass.setGrassGISDBASE(tfbGrassGISDB.getSelectedLocation());
			grass.setGrassLOCATION_NAME(tfbGrassLocation.getSelectedLocation());
			grass.setGrassMAPSET(tfbGrassMapset.getSelectedLocation());
		}
	}
	
	
	
}
