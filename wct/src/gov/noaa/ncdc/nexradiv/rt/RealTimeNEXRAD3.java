package gov.noaa.ncdc.nexradiv.rt;

import gov.noaa.ncdc.wct.ui.WCTViewer;

public class RealTimeNEXRAD3 {

	public final static String NWS_ADDRESS = "tgftp.nws.noaa.gov";
	public final static String NWS_FTP_DIR = "SL.us008001/DF.of/DC.radar";

	private WCTViewer viewer = null;

	public RealTimeNEXRAD3(WCTViewer viewer) {
		this.viewer = viewer;
	}
	
	public void displayNEXRAD(String id, String product) {
		
	}
}
