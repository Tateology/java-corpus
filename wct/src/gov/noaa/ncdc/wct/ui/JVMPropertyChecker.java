package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.common.RiverLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

public class JVMPropertyChecker {

//	public void JVMPropertyChecker() {
//		checkUserHome();
//	}
	
	
	
	public void checkUserHome() {
		
		String userHomeCacheDir = System.getProperty("user.home") + File.separator + ".wct-cache";
		String userHomeDir = System.getProperty("user.home");
		
		
//		JOptionPane.showMessageDialog(null, userHomeCacheDir);
		

		try {
			

            // First check if .wct-cache dir already exists, if so, then return
            if (new File(userHomeCacheDir).exists()) {
                   return;
            }


			boolean canWrite = new File(userHomeDir).canWrite();
			boolean canMkDir = new File(userHomeCacheDir).mkdir();
//			boolean canCreateFile = new File(userHomeCacheDir+File.separator+"test").createNewFile();
		

			// 1. user.home not writable, then try to guess based on user name for Windows 7 (common issue for NWS)
			if (! canMkDir) {
						
				String userName = System.getProperty("user.name");
				if (System.getProperty("os.name").equals("Windows 7")) {
					File realUserHomeDir = new File("\\Users\\"+userName);
					if (realUserHomeDir.exists()) {
						System.setProperty("user.home", realUserHomeDir.toString());
						return;
					}
				}
				
				// 2. if #1 doesn't work, then use the current working directory.				
				JOptionPane.showMessageDialog(null, 
						"<html>The default user.home property ("+userHomeDir+"), is not writable.<br>  " +
						"Using the current working directory instead: ("+System.getProperty("user.dir")+").<br>" +
						"This directory will hold application history, configuration files and the data cache.</html>");
				
				System.setProperty("user.home", System.getProperty("user.dir"));
				userHomeCacheDir = System.getProperty("user.home") + File.separator + ".wct-cache";
				canMkDir = new File(userHomeCacheDir).mkdir() || new File(userHomeCacheDir).exists();
				
				// 3. if user.dir is for some reason not available, then ask user for input
				if (! canMkDir) {
					PropertyInputDialog pid = new PropertyInputDialog();
					String selectedLocation = pid.getSelectedLocation();
					System.setProperty("user.home", selectedLocation);
				}			
				
			}
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
			
		}
	}
	
	
	

	public class PropertyInputDialog extends JDialog {
		
		private TextFieldWithBrowse tfb = new TextFieldWithBrowse("<html>The current 'user.home' java property is not writable.  " +
				"Please select a new user.home location which <br>will be used as the default cache location</html>", "propInputDialog", true);

		public PropertyInputDialog() {
			super();
			setTitle("Select Location");
			createGUI();
			setModal(true);
			pack();
			setLocationRelativeTo(null);
			setVisible(true);
		}
		
		private void createGUI() {
		
			JButton submitButton = new JButton("Submit");
			submitButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
			
			this.setLayout(new RiverLayout());
			this.add(tfb);
			this.add("br center", submitButton);
		}
		
		public String getSelectedLocation() {
			return tfb.getSelectedLocation();
		}
		
	}
	
	
}
