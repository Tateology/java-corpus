package gov.noaa.ncdc.wct.ui;

import javax.swing.JDialog;

public class ServicesMenuDialog extends JDialog {

	private WCTViewer viewer = null;
	private static ServicesMenuDialog dialog = null;
	
	
	private ServicesMenuDialog(WCTViewer viewer) {
		super(viewer, "Dataset Information");
		this.viewer = viewer;

		createUI();
		pack();
		setLocation(viewer.getX()+25, viewer.getY()+25);
	}

	public static ServicesMenuDialog getInstance(WCTViewer viewer) {
		if (dialog == null) {
			dialog = new ServicesMenuDialog(viewer);
		}
		return dialog;
	}

	
	private void createUI() {
		
	}
}
