package gov.noaa.ncdc.wct.decoders.nexrad;


import gov.noaa.ncdc.wct.ResourceUtils;
import gov.noaa.ncdc.wct.WCTConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;

import org.geotools.data.shapefile.dbf.DbaseFileReader;

/**
 *  Creates hashtables to represent lat, lon and elevation of ~160 nexrad sites.
 *  The included wsr.dbf attribute table for the wsr.shp shapefile is read to produce the hashtables.
 *  The dbffile wsr.dbf must be in the /shapefiles directory.<BR><BR>
 *  The ICAO (WSR ID) is used as the Hashtable key for all hashtables (must be all upper case).
 *
 * @author     steve.ansari
 * @created    October 5, 2004
 */
public class RadarHashtables {

    private Hashtable<String, String> nexhashLat;
    private Hashtable<String, String> nexhashLon;
    private Hashtable<String, String> nexhashElevInFeet;
    private Hashtable<String, String> nexhashLocation;
    private Hashtable<String, String> nexhashState;


    private static RadarHashtables nxhash;

    /**
     * Creates the hashtables for lat, lon and elev by reading
     * /shapefiles/wsr.dbf
     */
    private RadarHashtables() {
        loadHashtables();
    }


    public static RadarHashtables getSharedInstance() {
        if (nxhash == null) {
            nxhash = new RadarHashtables();
        }
        return nxhash;
    }


    public static void reloadHashtables() {
    	nxhash = null;
    	getSharedInstance();
    }
    
    public void addEntry(String id, String location, String state, 
    		double lat, double lon, double elevInFeet) {

    	
		nexhashLocation.put(id, location);
		nexhashState.put(id, state);
        nexhashLat.put(id, String.valueOf(lat));
        nexhashLon.put(id, String.valueOf(lon));
        nexhashElevInFeet.put(id, String.valueOf(elevInFeet));

    }



    private void loadHashtables() {

        try {

            nexhashLat = new Hashtable<String, String>();
            nexhashLon = new Hashtable<String, String>();
            nexhashElevInFeet = new Hashtable<String, String>();
            nexhashLocation = new Hashtable<String, String>();
            nexhashState = new Hashtable<String, String>();

            {
                // READ NEXRAD
                URL url = ResourceUtils.getInstance().getJarResource(
                        new URL(WCTConstants.MAP_DATA_JAR_URL), ResourceUtils.RESOURCE_CACHE_DIR, "/shapefiles/wsr.dbf", null);

                
                ReadableByteChannel in = getReadChannel(url);
                DbaseFileReader r = new DbaseFileReader( in );
                Object[] fields = new Object[r.getHeader().getNumFields()];
                while (r.hasNext()) {
                    r.readEntry(fields);
                    // do stuff
                    //for (int i=0; i<fields.length; i++) {
                    //   System.out.println(i+" ::: "+fields[i].toString());
                    //}

                    nexhashLat.put(fields[0].toString(), fields[1].toString());
                    nexhashLon.put(fields[0].toString(), fields[2].toString());
                    nexhashElevInFeet.put(fields[0].toString(), fields[3].toString());
                    nexhashLocation.put(fields[0].toString(), fields[4].toString());
                    nexhashState.put(fields[0].toString(), fields[5].toString());

                }
                r.close();
            }
            {
                // READ TDWR
                URL url = ResourceUtils.getInstance().getJarResource(
                        new URL(WCTConstants.MAP_DATA_JAR_URL), ResourceUtils.RESOURCE_CACHE_DIR, "/shapefiles/tdwr.dbf", null);
                ReadableByteChannel in = getReadChannel(url);
                DbaseFileReader r = new DbaseFileReader( in );
                Object[] fields = new Object[r.getHeader().getNumFields()];
                while (r.hasNext()) {
                    r.readEntry(fields);
                    // do stuff
                    //for (int i=0; i<fields.length; i++) {
                    //   System.out.println(i+" ::: "+fields[i].toString());
                    //}

                    nexhashLocation.put(fields[0].toString(), fields[1].toString());
                    nexhashLat.put(fields[0].toString(), fields[2].toString());
                    nexhashLon.put(fields[0].toString(), fields[3].toString());

                }
                r.close();
            }
            
            String[] abbreviations = new String[] {
                    "AB","CB","CN","LM","MA","MB","NC","NE","NW","OH","SE","WG"
            };
            for (int i=0; i<abbreviations.length; i++) {
                nexhashLat.put(abbreviations[i], "-999");
                nexhashLon.put(abbreviations[i], "-999");
                nexhashElevInFeet.put(abbreviations[i], "-999");
            }
            nexhashLocation.put("AB", "ARKANSAS-RED BASIN");
            nexhashLocation.put("CB", "COLORADO BASIN");
            nexhashLocation.put("CN", "CALIFORNIA NEVADA REGION");
            nexhashLocation.put("LM", "LOWER MISSISSIPPI REGION");
            nexhashLocation.put("MA", "MIDDLE ATLANTIC REGION");
            nexhashLocation.put("MB", "MISSOURI BASIN");
            nexhashLocation.put("NC", "NORTH CENTRAL REGION");
            nexhashLocation.put("NE", "NORTHEAST REGION");
            nexhashLocation.put("NW", "NORTHWEST REGION");
            nexhashLocation.put("OH", "OHIO REGION");
            nexhashLocation.put("SE", "SOUTHEASTERN REGION");
            nexhashLocation.put("WG", "WEST GULF REGION");

            //System.out.println("JNX HASHTABLES CREATED SUCCESSFULLY");



            // ADD EXTRA SITE: NOP3 - dual pole stuff from KTLX
//          nexhashLat.put("NOP3", nexhashLat.get("KTLX"));
//          nexhashLon.put("NOP3", nexhashLat.get("KTLX"));
//          nexhashElev.put("NOP3", nexhashLat.get("KTLX"));
//          nexhashLocation.put("NOP3", nexhashLat.get("KTLX"));
//          nexhashState.put("NOP3", nexhashLat.get("KTLX"));


            nexhashElevInFeet.put("CWKR", "-999");
            nexhashLocation.put("CWKR", "King City");
            nexhashState.put("CWKR", "ON, CA");

            
            ExtraRadarSiteListManager.getInstance().parseExtraSiteList(this);
            
            
            
            
            

            
            
            
        } catch (Exception e) {
            System.out.println("WCT NEXRAD HASHTABLE ERROR!");
            e.printStackTrace();
        }

    }


    /**
     * Obtain a ReadableByteChannel from the given URL. If the url protocol is
     * file, a FileChannel will be returned. Otherwise a generic channel will
     * be obtained from the urls input stream.
     */
    private ReadableByteChannel getReadChannel(URL url) throws IOException {
        ReadableByteChannel channel = null;
        if (url.getProtocol().equals("file")) {
            File file = new File(java.net.URLDecoder.decode(url.getFile(), "UTF-8"));
            if (! file.exists() || !file.canRead()) {
                throw new IOException("File either doesn't exist or is unreadable : " + file);
            }
            FileInputStream in = new FileInputStream(file);
            channel = in.getChannel();
        } else {
            InputStream in = url.openConnection().getInputStream();
            channel = Channels.newChannel(in);
        }
        return channel;
    }




    public double getLat(String icao) {
        try {
            if (nexhashLat.containsKey(icao.trim().toUpperCase())) {
                return Double.parseDouble(nexhashLat.get(icao.trim().toUpperCase()).toString());
            }
            else {
                return -999.0;
            }
        } catch (Exception e) {
            return -999.0;
        }
    }

    public double getLon(String icao) {
        try {
            if (nexhashLon.containsKey(icao.trim().toUpperCase())) {
                return Double.parseDouble(nexhashLon.get(icao.trim().toUpperCase()).toString());
            }
            else {
                return -999.0;
            }
        } catch (Exception e) {
            return -999.0;
        }
    }

    public double getElev(String icao) {
        try {
            if (nexhashElevInFeet.containsKey(icao.trim().toUpperCase())) {
                return Double.parseDouble(nexhashElevInFeet.get(icao.trim().toUpperCase()).toString());
            }
            else {
                return -999.0;
            }
        } catch (Exception e) {
            return -999.0;
        }
    }

    public String getLocation(String icao) {
        try {           
            if (nexhashLocation.containsKey(icao.trim().toUpperCase())) {
                return nexhashLocation.get(icao.trim().toUpperCase()).toString();
            }
            else {
                return "";
            }
        } catch (Exception e) {
            return "";
        }
    }

    public String getState(String icao) {
        try {
            if (nexhashState.containsKey(icao.trim().toUpperCase())) {
                return nexhashState.get(icao.trim().toUpperCase()).toString();
            }
            else {
                return "";
            }
        } catch (Exception e) {
            return "";
        }
    }








    /**
     * Returns null if distance between specified lat/lon exceeds the 
     * specified maxRange.
     * 
     * @param lat
     * @param lon
     * @param maxRange (in decimal degrees)
     * @return
     */
    public String getClosestICAO(double lat, double lon, double maxRange) {

        double matchingDist = 9999999;
        String matchingIcao = null;

        Enumeration keyEnum = nexhashLat.keys();
        while (keyEnum.hasMoreElements()) {
            Object key = keyEnum.nextElement();

            double keyLat = Double.parseDouble(nexhashLat.get(key).toString());
            double keyLon = Double.parseDouble(nexhashLon.get(key).toString());
            double dist = Math.sqrt(Math.pow(keyLat-lat, 2) + Math.pow(keyLon-lon, 2));

            if (dist < matchingDist) {
                matchingDist = dist;
                matchingIcao = key.toString();
            }

        }

        if (matchingDist > maxRange) {
        	return null;
        }
        else {
        	return matchingIcao;
        }
    }



    /**
     * Get alphabetically sorted list of loaded IDs.
     * @return
     * @throws IOException
     */
    public ArrayList<String> getIdList() throws IOException {

    	Enumeration<String> keyEnum = nexhashLocation.keys();
    	ArrayList<String> list = new ArrayList<String>(200);
        while (keyEnum.hasMoreElements()) {
    		list.add(keyEnum.nextElement());
    	}
        
        // sort alphabetically
        Collections.sort(list);

        return list;
    }








    public static void main(String[] args) {

        System.out.println("TESTING NexradHashtables Object");
        RadarHashtables nxhash = new RadarHashtables();
        String icao;
        if (args.length > 0) {
            icao = args[0].toUpperCase();
        }
        else {
            icao = "KGSP";
        }
        System.out.println(icao + "  :::  " + nxhash.getLat(icao) + " , " + nxhash.getLon(icao) + 
                " , " + nxhash.getElev(icao) + " , " + nxhash.getLocation(icao) + " , " + nxhash.getState(icao));
        System.out.println("FINISHED TESTING");
        
        
        try {
			for (String id : nxhash.getIdList()) {
				if (id.length() != 4) {
					continue;
				}
				String str = id +" - "+ nxhash.getLocation(id) +", "+ nxhash.getState(id);
				if (str.trim().endsWith(",")) {
					str = str.trim();
					str = str.substring(0, str.length()-1);
				}
				System.out.println("\""+id+"\", \""+ str.toUpperCase() +"\"" );
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

}

