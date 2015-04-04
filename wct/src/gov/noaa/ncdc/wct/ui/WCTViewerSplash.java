package gov.noaa.ncdc.wct.ui;

/**
 * Launches the Toolkit
 *
 * @author steve.ansari
 */
public class WCTViewerSplash {
    public static void main(String[] args) {
    	
    	final String[] finalArgs = args;
//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
				GenericViewerSplash.startApplication(
						finalArgs, 
						"gov.noaa.ncdc.wct.ui.WCTViewer", 
						"/images/splash-homepage.jpg");
//			}
//		});

    }
    
}

