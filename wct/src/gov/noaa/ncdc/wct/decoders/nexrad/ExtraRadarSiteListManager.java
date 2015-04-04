package gov.noaa.ncdc.wct.decoders.nexrad;

import gov.noaa.ncdc.wct.WCTConstants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class ExtraRadarSiteListManager {

	private static ExtraRadarSiteListManager instance;

	public final static String DEFAULT_SITE_LIST_FILE = "/config/radarExtraSiteList.txt";
	
	private ExtraRadarSiteListManager() {
	}

	
	public static ExtraRadarSiteListManager getInstance() {
		if (instance == null) {
			instance = new ExtraRadarSiteListManager();
		}
		return instance;
	}

	
	
	public void parseExtraSiteList(RadarHashtables hashtables) throws IOException {
        
        
        // read custom radar sites defined in ${wct.cache}/config/radarExtraSiteList.txt
        File extraSiteFile = new File(WCTConstants.getInstance().getCacheLocation()+
        		File.separator + DEFAULT_SITE_LIST_FILE);
        
        if (extraSiteFile.exists()) {
        
        	// Read all the text returned by the server
        	BufferedReader in = new BufferedReader(new FileReader(extraSiteFile));
        	String str;
        	while ((str = in.readLine()) != null) {
        		if (str.trim().length() > 0 && ! str.startsWith("#")) {
        			String[] cols = str.split(",");
        			//  ID (4 chars), Location, State
        			if (cols.length == 3) {
        				hashtables.addEntry(cols[0], cols[1], cols[2], -999, -999, -999);
        			}
        			//  ID (4 chars), Lat, Lon, Elev, Location, State
        			else if (cols.length == 6) {
        				hashtables.addEntry(
        						cols[0], cols[4], cols[5], 
        						Double.parseDouble(cols[1]),
        						Double.parseDouble(cols[2]),
        						Double.parseDouble(cols[3]));
        			}
        		}
        	}
        	in.close();

        }
	}
	
	
	
	public void addSiteToList(String id, String location, String state, double lat, double lon, double elevInFeet) 
		throws IOException {

        // read custom radar sites defined in ${wct.cache}/config/radarExtraSiteList.txt
        File extraSiteFile = new File(WCTConstants.getInstance().getCacheLocation()+
        		File.separator + DEFAULT_SITE_LIST_FILE);

        BufferedWriter bw = new BufferedWriter(new FileWriter(extraSiteFile, extraSiteFile.exists()));
        
        if (! extraSiteFile.exists()) {
        	bw.write("#");
        	bw.newLine();
        	bw.write("# Config file for appending additional radar sites to the internal");
        	bw.newLine();
        	bw.write("# Radar site list.");
        	bw.newLine();
        	bw.write("#");
        	bw.newLine();
        	bw.write("# Columns are defined as follows:");
        	bw.newLine();
        	bw.write("# 1) if lat/lon/elev is not in the file");
        	bw.newLine();
        	bw.write("# ID (4 chars), Lat, Lon, ElevInFeet, Location, State (optional)");
        	bw.newLine();
        	bw.write("#");
        	bw.newLine();
        	bw.write("# 2) lat/lon/elev is in the file (PREFERRED)");
        	bw.newLine();
        	bw.write("# ID (4 chars), Location, State (optional)");
        	bw.newLine();
        	bw.write("#");
        	bw.newLine();
        	bw.write("#");
        	bw.newLine();
        }

        bw.newLine();
        bw.write("# Entry added on: "+new Date());
        bw.newLine();
        if (Double.isNaN(lat) || lat == -999) {
        	bw.write(id+", "+location+", "+state);
        	bw.newLine();
        }
        else {
        	bw.write(id+", "+lat+", "+lon+", "+elevInFeet+", "+location+", "+state);
        	bw.newLine();
        }
        
        bw.close();
        
		RadarHashtables.getSharedInstance().addEntry(id, location, state, lat, lon, elevInFeet);
	}
	
	
}
