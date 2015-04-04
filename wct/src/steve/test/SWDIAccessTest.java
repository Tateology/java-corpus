package steve.test;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.geotools.cs.GeodeticCalculator;

public class SWDIAccessTest {


	static boolean firstTime = true;

	public static void main(String[] args) {
		try {
			process(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void process(String[] args) throws Exception {
		
		// example URL:
		// http://www.ncdc.noaa.gov/swdiws/xml/nx3hail/20110404:20110405?tile=-95.303650,32.337040
		
		double lat = 32.337040;
		double lon = -95.303650;
		double radiusInMiles = 15;
		
		// 1. find n,s,e,w extent at 15 miles
		GeodeticCalculator gcalc = new GeodeticCalculator();
		gcalc.setAnchorPoint(lon, lat);
		gcalc.setDirection(0, radiusInMiles*1609.344);
		Point2D north = gcalc.getDestinationPoint();
		gcalc.setDirection(90, radiusInMiles*1609.344);
		Point2D east = gcalc.getDestinationPoint();
		gcalc.setDirection(180, radiusInMiles*1609.344);
		Point2D south = gcalc.getDestinationPoint();
		gcalc.setDirection(-90, radiusInMiles*1609.344);
		Point2D west = gcalc.getDestinationPoint();
		
		System.out.println(north+" , "+east+" , "+south+" , "+west);
		
		// 2. find min/max lat/lon value 0.1 tiles to form extent
		int minLat = (int)Math.round(south.getY()*10);
		int maxLat = (int)Math.round(north.getY()*10);
		int minLon = (int)Math.round(west.getX()*10);
		int maxLon = (int)Math.round(east.getX()*10);
		
		System.out.println(minLat+" to "+maxLat+"  "+minLon+" to "+maxLon);
		for (int y=minLat; y<=maxLat; y++) {
			for (int x=minLon; x<=maxLon; x++) {
				URL tileURL = new URL("http://www.ncdc.noaa.gov/swdiws/csv/nx3hail/20110404:20110405?tile="+(x/10.0)+","+(y/10.0));
				readTile(tileURL, lat, lon, radiusInMiles);
			}
		}
	}
	
    public static void readTile(URL tileURL, double originLat, double originLon, double radiusInMiles) throws Exception {
        URLConnection yc = tileURL.openConnection();
        BufferedReader in = new BufferedReader(
                                new InputStreamReader(
                                yc.getInputStream()));
        String inputLine;

		GeodeticCalculator gcalc = new GeodeticCalculator();

        while ((inputLine = in.readLine()) != null) {
        	if (inputLine.startsWith("ZTIME")) {
        		if (firstTime) {
        			firstTime = false;
        			System.out.println(inputLine+",DISTANCE[miles]");
        		}
        	}
        	else if (! (inputLine.startsWith("summary") || inputLine.startsWith("total"))) {
        		
        		String[] cols = inputLine.split(",");
        		double lat = Double.parseDouble(cols[cols.length-2]);
        		double lon = Double.parseDouble(cols[cols.length-1]);
        		
        		gcalc.setAnchorPoint(originLon, originLat);
        		gcalc.setDestinationPoint(lon, lat);
        		
        		double dist = gcalc.getOrthodromicDistance();
        		
        		if (dist/1609.344 < radiusInMiles) {
        			System.out.println(inputLine+","+dist/1609.344);
        		}
        	}
        }
        in.close();
    }

}
