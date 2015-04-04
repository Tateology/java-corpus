package gov.noaa.ncdc.wct.ui.plugins;


import gov.noaa.ncdc.wct.ui.GenericViewerSplash;

/**
 * Launches the ImpactTool
 *
 * @author steve.ansari
 */
public class ImpactToolLauncher {
    public static void main(String[] args) {
    	
    	final String[] finalArgs = args;
//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
				GenericViewerSplash.startApplication(
						new String[] { "CDR-Impact-Tool" }, 
						"gov.noaa.ncdc.wct.ui.WCTViewer", 
						"/images/impact-splash-image.jpg");
//			}
//		});

    }
    
}


