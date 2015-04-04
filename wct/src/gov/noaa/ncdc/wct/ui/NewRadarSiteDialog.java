package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.wct.decoders.nexrad.ExtraRadarSiteListManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class NewRadarSiteDialog extends JDialog {

    private WCTViewer viewer = null;

    private String id = null;
    private String location = null;
    private String stateAbbr = null;
    private Double lat;
    private Double lon;
    private Double elevInFeet;
    
    private boolean isError = false;
    private boolean isCancelled = false;
    
    public NewRadarSiteDialog(WCTViewer viewer) {
        super(viewer, "New Radar Site Information", true);
        this.viewer = viewer;
        createUI();
    }

    
    
    private void createUI() {

    	this.setLayout(new RiverLayout());

    	this.add(new JLabel("<html><center><b>Information for this Radar site was not " +
    			"found in the internal <br> Weather and Climate Toolkit look-up tables. <br><br> " +
    			"Please enter new information for this Radar site below. </b></center></html>"), "p");
    	
    	final JTextField jtfSiteID = new JTextField(15);
    	if (viewer.getNexradHeader().getICAO() != null && 
    			viewer.getNexradHeader().getICAO().length() == 4) {

        	jtfSiteID.setText(viewer.getNexradHeader().getICAO());
        	jtfSiteID.setEnabled(false);
    	}
    	
    	final JTextField jtfSiteLocation = new JTextField(15);
    	final JTextField jtfSiteStateAbbr = new JTextField(5);
    	final JTextField jtfSiteLat = new JTextField(15);
    	final JTextField jtfSiteLon = new JTextField(15);
    	final JTextField jtfSiteElevInFeet = new JTextField(15);
    	
    	this.add("p", new JLabel("Site ID (4 Letters/Numbers)"));
    	this.add("tab", jtfSiteID);
    	this.add("br", new JLabel("Site Location Description"));
    	this.add("tab", jtfSiteLocation);
    	this.add("br", new JLabel("Site State Abbreviation"));
    	this.add("tab", jtfSiteStateAbbr);
    	this.add("br", new JLabel("Site Latitude (NAD83 Decimal Degrees)"));
    	this.add("tab", jtfSiteLat);
    	this.add("br", new JLabel("Site Longitude (NAD83 Decimal Degrees)"));
    	this.add("tab", jtfSiteLon);
    	this.add("br", new JLabel("Site Elevation (Feet)"));
    	this.add("tab", jtfSiteElevInFeet);
    	
    	
    	
    	final JButton submitButton = new JButton("Submit");
    	this.add("p", submitButton);
    	final JButton cancelButton = new JButton("Cancel");
    	this.add("", cancelButton);
    	
    	cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setCanceled(true);
				dispose();
			}
		});
    	
    	submitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (! validate()) {
					showValidationErrorMessage();
				}
				else {
					id = jtfSiteID.getText();
					location = jtfSiteLocation.getText();
					stateAbbr = jtfSiteStateAbbr.getText();
					lat = Double.parseDouble(jtfSiteLat.getText());
					lon = Double.parseDouble(jtfSiteLon.getText());
					elevInFeet = Double.parseDouble(jtfSiteElevInFeet.getText());
					try {
						ExtraRadarSiteListManager.getInstance().addSiteToList(id, location, stateAbbr, lat, lon, elevInFeet);
					} catch (IOException e1) {
						showAddSiteErrorMessage(e1);
						setError(true);
					}
					dispose();
				}
			}
			
			private boolean validate() {
				if (jtfSiteID.getText().trim().length() == 0) {
					return false;
				}
				try {
					Double.parseDouble(jtfSiteLat.getText());
				} catch (Exception e) {
					return false;
				}
				try {
					Double.parseDouble(jtfSiteLon.getText());
				} catch (Exception e) {
					return false;
				}
				try {
					Double.parseDouble(jtfSiteElevInFeet.getText());
				} catch (Exception e) {
					return false;
				}
				
				return true;
			}
		});
    	
    }
    
    
    private void showValidationErrorMessage() {
    	JOptionPane.showMessageDialog(viewer, "- Validation Error - \nPlease check input fields", 
    			"Validation Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showAddSiteErrorMessage(Exception e) {
    	JOptionPane.showMessageDialog(viewer, "- Error Adding Radar Site - \n"+e.getMessage(), 
    			"General Error", JOptionPane.ERROR_MESSAGE);
    }


	public String getId() {
		return id;
	}
	public String getLocationDescription() {
		return location;
	}
	public String getStateAbbr() {
		return stateAbbr;
	}
	public Double getLat() {
		return lat;
	}
	public Double getLon() {
		return lon;
	}
	public Double getElevInFeet() {
		return elevInFeet;
	}

	private void setError(boolean isError) {
		this.isError = isError;
	}
	public boolean isError() {
		return isError;
	}


	private void setCanceled(boolean isCancelled) {
		this.isCancelled = isCancelled;
	}
	public boolean isCancelled() {
		return isCancelled;
	}
    
}
