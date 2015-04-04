package gov.noaa.ncdc.wct.morph;

import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.WCTException;
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Header;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeStormTracking;
import gov.noaa.ncdc.wct.ui.MarkerEditor;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.time.DateUtils;
import org.geotools.cs.GeodeticCalculator;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;

public class WCTMorphVectors {

	public final static String SWDI_URL = "http://www.ncdc.noaa.gov/swdiws";
	
	public final static SimpleDateFormat SWDI_DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmm");
	public final static SimpleDateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	static {
		SWDI_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
		ISO_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
	}


	
	public static ArrayList<MorphVector> queryMarkers(Date firstDate, Date lastDate, Rectangle2D.Double extent, StringBuilder operationLog) 
			throws ClassNotFoundException, IllegalAttributeException, IOException, FactoryConfigurationError, SchemaException, WCTException {
		
		final ArrayList<MorphVector> mvList = new ArrayList<MorphVector>();

	    File objFile = new File(WCTConstants.getInstance().getCacheLocation()+File.separator+
	            "objdata"+File.separator+MarkerEditor.class.getName()+".data");

	    long pairDateDiff = lastDate.getTime() - firstDate.getTime();
	    Feature firstDateMatchingMarker = null;
	    Feature lastDateMatchingMarker = null;
	    
	    FeatureCollection markerFeatures = MarkerEditor.loadMarkerObjectData(objFile, FeatureCollections.newCollection());
	    FeatureIterator iter = markerFeatures.features();
		while (iter.hasNext()) {
			Feature f = iter.next();
			if (f.getAttribute("label1").toString().startsWith("morph.")) {
				System.out.println(SWDI_DATE_FORMAT.format(firstDate)+" , "+SWDI_DATE_FORMAT.format(lastDate) + " , "+f);
				// check for matching markers using 'morph.1.hhmm' convention
				
				// 012345678901234567890
				// morph.1.hhmm
				// 201305202016
				if (f.getAttribute("label1").toString().substring(8, 12).equals(
						SWDI_DATE_FORMAT.format(firstDate).substring(8, 12))) {
					firstDateMatchingMarker = f;
				}
				if (f.getAttribute("label1").toString().substring(8, 12).equals(
						SWDI_DATE_FORMAT.format(lastDate).substring(8, 12))) {
					lastDateMatchingMarker = f;
				}
			}
		}
	    
	    if (firstDateMatchingMarker == null || lastDateMatchingMarker == null) {
	    	throw new WCTException("Unable to match markers to times.  \n" +
	    			"Please check that markers match the label convention of morph.1.$hhmm, \n" +
	    			"where $hhmm is the hour and minute in GMT of the corresponding file timestamp.");
	    }

//		// calculate magnitude and speed
		final GeodeticCalculator geoCalc = new GeodeticCalculator();
		geoCalc.setAnchorPoint(firstDateMatchingMarker.getDefaultGeometry().getCoordinate().x, 
				firstDateMatchingMarker.getDefaultGeometry().getCoordinate().y);
		geoCalc.setDestinationPoint(lastDateMatchingMarker.getDefaultGeometry().getCoordinate().x, 
				lastDateMatchingMarker.getDefaultGeometry().getCoordinate().y);
		
		MorphVector mv = new MorphVector();
		mv.setLat(firstDateMatchingMarker.getDefaultGeometry().getCoordinate().y);
		mv.setLon(firstDateMatchingMarker.getDefaultGeometry().getCoordinate().x);
		mv.setSpeed(geoCalc.getOrthodromicDistance()/(pairDateDiff/1000));
		mv.setDirectionAngle(geoCalc.getAzimuth());
		
		mvList.add(mv);
		System.out.println("mv: "+mv+" dist="+geoCalc.getOrthodromicDistance()+" time="+pairDateDiff/1000+" from: "+ISO_DATE_FORMAT.format(firstDate));

		operationLog.append(mv.toString()+"\n");

		
		
		return mvList;
	}
	
	
//	private ArrayList<MorphGeoFeaturePair> morphGeoFeaturePairList = new ArrayList<MorphGeoFeaturePair>();
	

	
	
	// NARR?
	// http://nomads.ncdc.noaa.gov/thredds/ncss/grid/narr/200004/20000425/narr-a_221_20000425_1200_000.grb?var=u-component_of_storm_motion&var=v-component_of_storm_motion&latitude=35&longitude=-90&temporal=all&time_start=2000-04-25T12%3A00%3A00Z&time_end=2000-04-25T15%3A00%3A00Z&time=2000-04-25T12%3A00%3A00Z&vertCoord=&accept=csv&point=true
	
	
	public static ArrayList<MorphVector> queryRUCStormMotion(Date begDate, Date endDate, Rectangle2D.Double extent,
			StringBuilder operationLog) throws MalformedURLException, IOException {
		
		final ArrayList<MorphVector> mvList = new ArrayList<MorphVector>();

		
		
		// OLD: http://nomads.ncdc.noaa.gov/thredds/ncss/grid/ruc/201104/20110427/ruc2_252_20110427_2000_000.grb?var=u-component_of_storm_motion&var=v-component_of_storm_motion&latitude=33.83170974254608&longitude=-86.89009404182434&time_start=2011-04-27T20%3A14%3A55Z&time_end=2011-04-27T20%3A28%3A43Z&temporal=point&time=2011-04-27T20%3A21%3A49Z&vertCoord=&accept=csv&point=true
		// NEW: http://nomads.ncdc.noaa.gov/thredds/ncss/grid/ruc252/201104/20110404/ruc2_252_20110404_0000_004.grb?var=u-component_of_storm_motion&var=v-component_of_storm_motion&latitude=35&longitude=-90&temporal=all&time_start=2011-04-05T04%3A00%3A00Z&time_end=2011-04-05T04%3A00%3A00Z&time=2011-04-05T04%3A00%3A00Z&vertCoord=&accept=csv&point=true
		
		
		
		// 1. find closest 3 hour file to dates in question
		// 1a. find midpoint date
		Date date = new Date(begDate.getTime()+ (endDate.getTime()-begDate.getTime())/2);
		
		System.out.println(begDate+","+endDate+","+date);
		// round to nearest hour		
		Date nearestHour = DateUtils.round(date, Calendar.HOUR);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		String yyyymmddhh = sdf.format(nearestHour);

		if (Integer.parseInt(yyyymmddhh.substring(0, 8)) > 20120516) {

			MorphVector mv = getRAP_252_MV(yyyymmddhh, extent, begDate, endDate, date, operationLog);

			operationLog.append("[rap_252 morph vector]: \n");
			operationLog.append(mv+"\n");
			
			mvList.add(mv);
		}
		else {
		
		try {
			
			//TODO change this to use date ranges to know which to use
			
		
		// build OPeNDAP request URL:  (opendap isn't working right!)
//		String odapURL = "http://nomads.ncdc.noaa.gov/thredds/"
		
			MorphVector mv = getRUC_130_MV(yyyymmddhh, extent, begDate, endDate, date, operationLog);
			
			operationLog.append("[ruc_130 morph vector]: \n");
			operationLog.append(mv+"\n");
			
			mvList.add(mv);
		
		} catch (FileNotFoundException e) {			
			e.printStackTrace();		
			System.out.println("RUC 130 NOT FOUND.  USING RUC 252...");
			MorphVector mv = getRUC_252_MV(yyyymmddhh, extent, begDate, endDate, date, operationLog);

			operationLog.append("[ruc_252 morph vector]: \n");
			operationLog.append(mv+"\n");

			mvList.add(mv);

		}
		
		}
		
		return mvList;
		
	}
	


	private static MorphVector getRAP_252_MV(String yyyymmddhh, Rectangle2D.Double extent, Date begDate, Date endDate, Date date,
			StringBuilder operationLog) throws MalformedURLException, IOException {
		
		SimpleDateFormat sdfIso = new SimpleDateFormat("yyyy-MM-dd'T'HH'%3A'mm'%3A'ss'Z'");
		sdfIso.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		String ncssURL = "http://nomads.ncdc.noaa.gov/thredds/ncss/grid/rap252/"+
		yyyymmddhh.substring(0, 6)+"/"+yyyymmddhh.substring(0, 8)+"/" +
		"rap_252_"+yyyymmddhh.substring(0, 8)+"_"+
		yyyymmddhh.substring(8)+"00_000.grb2" +
		"?var=U-Component_Storm_Motion" +
		"&var=V-Component_Storm_Motion" +
		"&latitude="+extent.getCenterY()+
		"&longitude="+extent.getCenterX() +
		"&time_start="+sdfIso.format(begDate)+
		"&time_end="+sdfIso.format(endDate)+
		"&temporal=point&time="+sdfIso.format(date)+"&vertCoord=&accept=csv&point=true";

		System.out.println(ncssURL);

		operationLog.append("\n[ruc_130 NOMADS NetCDF Subset Service URL]: \n");
		operationLog.append(ncssURL+"\n");


		//http://nomads.ncdc.noaa.gov/thredds/ncss/grid/ruc13/201111/20111103/ruc2_130_20111103_2300_000.grb2?var=u-component_of_storm_motion&var=v-component_of_storm_motion&latitude=34.85343750325636&longitude=-82.21999999999998&time_start=2011-11-03T23%3A07%3A06Z&time_end=2011-11-03T23%3A18%3A02Z&temporal=point&time=2011-11-03T23%3A12%3A34Z&vertCoord=&accept=csv&point=true		
		//http://nomads.ncdc.noaa.gov/thredds/ncss/grid/ruc13/201111/20111103/ruc2_130_20111103_2100_009.grb2?var=U-Component_Storm_Motion&var=V-Component_Storm_Motion&latitude=35&longitude=-80&time_start=2011-11-04T06%3A00%3A00Z&time_end=2011-11-04T06%3A00%3A00Z&temporal=point&time=2011-11-04T06%3A00%3A00Z&vertCoord=&accept=csv&point=true


		BufferedReader in = new BufferedReader(new InputStreamReader(new URL(ncssURL).openStream()));

		// read header line
		in.readLine();
		// read data line
		String[] cols = in.readLine().split(",");
		double lat = Double.parseDouble(cols[1]);
		double lon = Double.parseDouble(cols[2]);
		double u = Double.parseDouble(cols[3]);
		double v = Double.parseDouble(cols[4]);


		double speed = Math.sqrt(u*u + v*v);
		double direction = 90 - Math.toDegrees(Math.atan2(v, u))+0;
		if (direction > 180) {
			direction = direction-360;
		}


		MorphVector mv = new MorphVector();
		mv.setLat(lat);
		mv.setLon(lon);
		mv.setSpeed(speed);
		mv.setDirectionAngle(direction);

		return mv;
	}
	
	

	private static MorphVector getRUC_130_MV(String yyyymmddhh, Rectangle2D.Double extent, Date begDate, Date endDate, Date date,
			StringBuilder operationLog) throws MalformedURLException, IOException {
		
		SimpleDateFormat sdfIso = new SimpleDateFormat("yyyy-MM-dd'T'HH'%3A'mm'%3A'ss'Z'");
		sdfIso.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		String ncssURL = "http://nomads.ncdc.noaa.gov/thredds/ncss/grid/ruc130anl/"+
		yyyymmddhh.substring(0, 6)+"/"+yyyymmddhh.substring(0, 8)+"/" +
		"ruc2anl_130_"+yyyymmddhh.substring(0, 8)+"_"+
		yyyymmddhh.substring(8)+"00_000.grb2" +
		"?var=U-Component_Storm_Motion" +
		"&var=V-Component_Storm_Motion" +
		"&latitude="+extent.getCenterY()+
		"&longitude="+extent.getCenterX() +
		"&time_start="+sdfIso.format(begDate)+
		"&time_end="+sdfIso.format(endDate)+
		"&temporal=point&time="+sdfIso.format(date)+"&vertCoord=&accept=csv&point=true";

		System.out.println(ncssURL);

		operationLog.append("\n[ruc_130 NOMADS NetCDF Subset Service URL]: \n");
		operationLog.append(ncssURL+"\n");


		//http://nomads.ncdc.noaa.gov/thredds/ncss/grid/ruc13/201111/20111103/ruc2_130_20111103_2300_000.grb2?var=u-component_of_storm_motion&var=v-component_of_storm_motion&latitude=34.85343750325636&longitude=-82.21999999999998&time_start=2011-11-03T23%3A07%3A06Z&time_end=2011-11-03T23%3A18%3A02Z&temporal=point&time=2011-11-03T23%3A12%3A34Z&vertCoord=&accept=csv&point=true		
		//http://nomads.ncdc.noaa.gov/thredds/ncss/grid/ruc13/201111/20111103/ruc2_130_20111103_2100_009.grb2?var=U-Component_Storm_Motion&var=V-Component_Storm_Motion&latitude=35&longitude=-80&time_start=2011-11-04T06%3A00%3A00Z&time_end=2011-11-04T06%3A00%3A00Z&temporal=point&time=2011-11-04T06%3A00%3A00Z&vertCoord=&accept=csv&point=true


		BufferedReader in = new BufferedReader(new InputStreamReader(new URL(ncssURL).openStream()));

		// read header line
		in.readLine();
		// read data line
		String[] cols = in.readLine().split(",");
		double lat = Double.parseDouble(cols[1]);
		double lon = Double.parseDouble(cols[2]);
		double u = Double.parseDouble(cols[3]);
		double v = Double.parseDouble(cols[4]);


		double speed = Math.sqrt(u*u + v*v);
		double direction = 90 - Math.toDegrees(Math.atan2(v, u))+0;
		if (direction > 180) {
			direction = direction-360;
		}


		MorphVector mv = new MorphVector();
		mv.setLat(lat);
		mv.setLon(lon);
		mv.setSpeed(speed);
		mv.setDirectionAngle(direction);

		return mv;
	}
	
	private static MorphVector getRUC_252_MV(String yyyymmddhh, Rectangle2D.Double extent, Date begDate, Date endDate, Date date,
			StringBuilder operationLog) throws MalformedURLException, IOException {
		
		SimpleDateFormat sdfIso = new SimpleDateFormat("yyyy-MM-dd'T'HH'%3A'mm'%3A'ss'Z'");
		sdfIso.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		String ncssURL = "http://nomads.ncdc.noaa.gov/thredds/ncss/grid/ruc252anl/"+
		yyyymmddhh.substring(0, 6)+"/"+yyyymmddhh.substring(0, 8)+"/" +
		"ruc2_252_"+yyyymmddhh.substring(0, 8)+"_"+
		yyyymmddhh.substring(8)+"00_000.grb" +
		"?var=u-component_of_storm_motion" +
		"&var=v-component_of_storm_motion" +
		"&latitude="+extent.getCenterY()+
		"&longitude="+extent.getCenterX() +
		"&time_start="+sdfIso.format(begDate)+
		"&time_end="+sdfIso.format(endDate)+
		"&temporal=point&time="+sdfIso.format(date)+"&vertCoord=&accept=csv&point=true";

		System.out.println(ncssURL);

		operationLog.append("\n[ruc_252 NOMADS NetCDF Subset Service URL]: \n");
		operationLog.append(ncssURL+"\n");

		BufferedReader in = new BufferedReader(new InputStreamReader(new URL(ncssURL).openStream()));

		// read header line
		in.readLine();
		// read data line
		String[] cols = in.readLine().split(",");
		double lat = Double.parseDouble(cols[1]);
		double lon = Double.parseDouble(cols[2]);
		double u = Double.parseDouble(cols[3]);
		double v = Double.parseDouble(cols[4]);


		double speed = Math.sqrt(u*u + v*v);
		double direction = 90 - Math.toDegrees(Math.atan2(v, u))+180;
		if (direction > 180) {
			direction = direction-360;
		}


		MorphVector mv = new MorphVector();
		mv.setLat(lat);
		mv.setLon(lon);
		mv.setSpeed(speed);
		mv.setDirectionAngle(direction);
		
		return mv;
	}
	
	
	
	
	
	
	
	
	
	
	

	
	
	
	
	
	public static ArrayList<MorphVector> querySWDI(
			String dataset, Date begDate, Date endDate, 
			Rectangle2D.Double extent, int numberOfMorphVectorGridCells, StringBuilder operationLog) 			
			throws IOException, ParseException {
		
		
		ArrayList<MorphGeoFeaturePair> morphGeoFeaturePairList = new ArrayList<MorphGeoFeaturePair>();

		final GeodeticCalculator geoCalc = new GeodeticCalculator();
		
		final ArrayList<String[]> dateSiteCellList = new ArrayList<String[]>();
		final ArrayList<MorphVector> mvList = new ArrayList<MorphVector>();
			
		SWDIRowHandler rowHandler = new SWDIRowHandler() {
			@Override
			public void processRow(String[] cols) {
				if (cols.length > 3) {
					dateSiteCellList.add(cols);
				}
			}
		};
		
		querySWDI(rowHandler, begDate, endDate, dataset, extent, operationLog);
		
		System.out.println(Arrays.deepToString(dateSiteCellList.toArray()));
		
		
//		                 [0], [1],[2],[3],[4],5,[6],  [7],  [8]
//		2011-05-22T22:43:19Z,KLSX,B1,204,242,83,57,37.040,-94.433

		
		operationLog.append("\n\n[convert to motion vector]:\n");
		
		for (int n=0; n<dateSiteCellList.size()-1; n++) {
			
			String[] row = dateSiteCellList.get(n);
			String output = row[1]+"|"+row[2]+","+row[0]+"|"+row[row.length-2]+"|"+row[row.length-1];

			// for each row, loop through remainders to find cells with same ID in future.
			for (int i=n+1; i<dateSiteCellList.size(); i++) {
				String[] row2 = dateSiteCellList.get(i);
				
//				System.out.println("row2: "+Arrays.toString(row2));
				
				if (row[1].equals(row2[1]) && row[2].equals(row2[2])) {
					output=output+","+row2[0]+"|"+row2[row2.length-2]+"|"+row2[row2.length-1];
				}
			}
			
			// now loop through cell id pairs and calculate motion vectors
			// use only data rows, not 'query summary' rows
			if (output.split(",").length > 2  
//					&& row[1].equals("KBMX") 
//					&& ! row[2].equals("V4")
				) {
				System.out.println(output);
				
				MorphGeoFeaturePair pair = new MorphGeoFeaturePair();
				
				String[] pairInfo = output.split(",");
				String[] firstPairInfo = pairInfo[1].split("\\|");
				String[] lastPairInfo = pairInfo[pairInfo.length-1].split("\\|");
				
				Date firstTime = ISO_DATE_FORMAT.parse(firstPairInfo[0]);
				Date lastTime = ISO_DATE_FORMAT.parse(lastPairInfo[0]);
				
				long pairDateDiff = lastTime.getTime()-firstTime.getTime();

				// calculate magnitude and speed
				geoCalc.setAnchorPoint(Double.parseDouble(firstPairInfo[2]), Double.parseDouble(firstPairInfo[1]));
				geoCalc.setDestinationPoint(Double.parseDouble(lastPairInfo[2]), Double.parseDouble(lastPairInfo[1]));
				MorphVector mv = new MorphVector();
				mv.setLat(Double.parseDouble(firstPairInfo[1]));
				mv.setLon(Double.parseDouble(firstPairInfo[2]));
				mv.setSpeed(geoCalc.getOrthodromicDistance()/(pairDateDiff/1000));
				mv.setDirectionAngle(geoCalc.getAzimuth());
				
				mvList.add(mv);
				System.out.println("mv: "+mv+" dist="+geoCalc.getOrthodromicDistance()+" time="+pairDateDiff/1000+" from: "+output);

				operationLog.append(mv.toString()+"\n");
				
//				long queryDateDiff = endDate.getTime()-begDate.getTime();
//				double diffPercent = ((double)pairDateDiff)/queryDateDiff;
				// track pair must be at least 90% of time coverage from beg to end query date
//				if (diffPercent > 0.70) {

//					System.out.println("using: "+output);
//					
//					pair.setId(pairInfo[0]);
//					pair.setFirstLat(Double.parseDouble(firstPairInfo[1]));
//					pair.setFirstLon(Double.parseDouble(firstPairInfo[2]));
//					pair.setLastLat(Double.parseDouble(lastPairInfo[1]));
//					pair.setLastLon(Double.parseDouble(lastPairInfo[2]));
//					
//					morphGeoFeaturePairList.add(pair);

//				}
					
				
			}
		}
		
//		processMorphGeoFeatureList(mvList, begDate, endDate);
		return createMorphVectorGrid(mvList, extent, numberOfMorphVectorGridCells, operationLog);		
//		return mvList;
	}

	
	
	private static void querySWDI(SWDIRowHandler rowHandler, Date begDate, Date endDate, 
			String dataset, Rectangle2D.Double extent, StringBuilder operationLog) throws IOException {
		
		
		// ex: http://www.ncdc.noaa.gov/swdiws/csv/nx3mda/201105222240:201105222250?bbox=-94.5,37.0,-94.0,37.5
		// 1. assemble URL
		URL url = new URL(SWDI_URL+"/csv/"+dataset+"/"+
				SWDI_DATE_FORMAT.format(begDate)+":"+SWDI_DATE_FORMAT.format(endDate)+"/?bbox="+
				extent.getMinX()+","+extent.getMinY()+","+extent.getMaxX()+","+extent.getMaxY());

		
		System.out.println(url);
		
		operationLog.append("SWDI Query URL: "+url+"\n");
		
		
		// 2. read data and extract vectors
//		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();
		BufferedReader in;
		boolean isError = false;
		if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
			in = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
		}
		else {
			in = new BufferedReader(new InputStreamReader(httpConn.getErrorStream()));
			isError = true;
		}
		String str;
		// read header
		if ((str = in.readLine()) == null) {
			throw new IOException("Could not read header line");
		}
		
		if (str.equals("error")) {
			String message = in.readLine();
			in.close();
			throw new IOException(message);
		}
		
		if (isError) {
			throw new IOException("General connection error to the NCDC SWDI services");
		}

		operationLog.append("[SWDI Query Results]: \n");
		// read data
		while ((str = in.readLine()) != null) {
			operationLog.append(str+"\n");
			
			rowHandler.processRow(str.split(","));			
		}
		in.close();
		
	}
	

	
	
	
	private static ArrayList<MorphVector> createMorphVectorGrid(ArrayList<MorphVector> mvList, 
			Rectangle2D.Double extent, int numberOfMorphGridCells, StringBuilder operationLog) {
		
		ArrayList<MorphVector> mvGridList = new ArrayList<MorphVector>();
		
		int numXCells = numberOfMorphGridCells;
		int numYCells = numberOfMorphGridCells;
		
		double xCellSize = extent.getWidth()/numXCells;
		double yCellSize = extent.getHeight()/numYCells;
		
		operationLog.append("\n\n[createMorphVectorGrid]: \n");
		operationLog.append("   (number of morph grid cells = "+numberOfMorphGridCells+") \n");
		operationLog.append("   (number of input individual motion vectors = "+mvList.size()+") \n");

		
		for (int j=0; j<numYCells; j++) {
			for (int i=0; i<numXCells; i++) {
				
				double totalU = 0;
				double totalV = 0;
				int count = 0;
				
				double minX = extent.getMinX()+(i*xCellSize);
				double minY = extent.getMinY()+(j*yCellSize);
				
				for (MorphVector mv : mvList) {
					if (mv.getLat() > minY && mv.getLat() < minY+yCellSize &&
						mv.getLon() > minX && mv.getLon() < minX+xCellSize) {
					
						totalU += -1*mv.getSpeed()*Math.sin(Math.toRadians(mv.getDirectionAngle()));
						totalV += -1*mv.getSpeed()*Math.cos(Math.toRadians(mv.getDirectionAngle()));
						count++;
					}
				}
				
				if (count > 0) {
				
					double avgU = totalU/count;
					double avgV = totalV/count;

					double speed = Math.sqrt(avgU*avgU + avgV*avgV);
					double direction = 90 - Math.toDegrees(Math.atan2(avgV, avgU))+180;

					if (direction > 180) {
						direction = direction-360;
					}

					MorphVector mv = new MorphVector();
					mv.setLat(minY+yCellSize/2.0);
					mv.setLon(minX+xCellSize/2.0);
					mv.setSpeed(speed);
					mv.setDirectionAngle(direction);

					mvGridList.add(mv);

					operationLog.append("morph vector grid cell: "+mv.toString()+") \n");

				}
				
			}
		}
		
		
		return mvGridList;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
//	public void queryL3StormTracking(URL dataURL, Rectangle2D.Double extent) throws IOException, DecodeException {
		
//		ArrayList<MorphVector> mvList = getL3StormTrackingMorphVectors(dataURL, extent);
//		processMorphGeoFeatureList(mvList, startDate, endDate)
//	}
	
	public static ArrayList<MorphGeoFeaturePair> queryL3StormTracking(URL dataURL, Rectangle2D.Double extent) 
		throws IOException, DecodeException {
		
		return queryL3StormTracking(dataURL, extent, 1000L*60*5);
	}
	
	public static ArrayList<MorphGeoFeaturePair> queryL3StormTracking(URL dataURL, Rectangle2D.Double extent, 
			long durationInMillis) 
		throws IOException, DecodeException {

		ArrayList<MorphVector> morphVectorList = new ArrayList<MorphVector>();
		
		GeometryFactory gfac = new GeometryFactory();
		
		DecodeL3Header header = new DecodeL3Header();
		header.decodeHeader(dataURL);
		DecodeStormTracking decoder = new DecodeStormTracking(header);
		decoder.decodeData();
		FeatureIterator iter = decoder.getFeatures().features();
		while (iter.hasNext()) {
			Feature f = iter.next();
			
			try {
			
				if (f.getAttribute("time").toString().trim().equals("0") && f.getDefaultGeometry().within(
						gfac.toGeometry(
								new Envelope(extent.getMinX(), extent.getMaxX(), extent.getMinY(), extent.getMaxY())
							)
						)
					) {
					
					MorphVector mv = new MorphVector();
					mv.setLat(Double.parseDouble( f.getAttribute("lat").toString() ));
					mv.setLon(Double.parseDouble( f.getAttribute("lon").toString() ));
					double angle = Double.parseDouble( f.getAttribute("movedeg").toString() )-180;
					mv.setDirectionAngle(angle);
					double mag = Double.parseDouble( f.getAttribute("movekts").toString() );
					// convert from knots to m/s
					mv.setSpeed(mag*0.514444444);

					morphVectorList.add(mv);
				}

			} catch (Exception e) {
				
			}
		}
		
		// if no storm cells tracked within bbox, then look for any within range of Radar
		if (morphVectorList.size() == 0) {
			FeatureIterator iter2 = decoder.getFeatures().features();
			while (iter2.hasNext()) {
				Feature f = iter2.next();
				
				try {
						
					MorphVector mv = new MorphVector();
					mv.setLat(Double.parseDouble( f.getAttribute("lat").toString() ));
					mv.setLon(Double.parseDouble( f.getAttribute("lon").toString() ));
					double angle = Double.parseDouble( f.getAttribute("movedeg").toString() )-180;
					mv.setDirectionAngle(angle);
					double mag = Double.parseDouble( f.getAttribute("movekts").toString() );
					// convert from knots to m/s
					mv.setSpeed(mag*0.514444444);

					morphVectorList.add(mv);

				} catch (Exception e) {
					
				}
			}
			
		}
		
		
		
		return processMorphGeoFeatureList(morphVectorList, new Date(header.getMilliseconds()), 
				new Date(header.getMilliseconds()+durationInMillis));
		
		
	}
	
	/**
	 * Populate global morphGeoFeaturePairList with geographic pairs generated from
	 * the supplied morph vector list and start/end dates.
	 * @param morphVectorList
	 * @param startDate
	 * @param endDate
	 */
	public static ArrayList<MorphGeoFeaturePair> processMorphGeoFeatureList(ArrayList<MorphVector> morphVectorList, 
			Date startDate, Date endDate) {
		
		ArrayList<MorphGeoFeaturePair> morphGeoFeaturePairList = new ArrayList<MorphGeoFeaturePair>();
		
		GeodeticCalculator gcalc = new GeodeticCalculator();
		
		long dateDiffInSeconds = (endDate.getTime()-startDate.getTime())/1000;
		
		for (MorphVector mv : morphVectorList) {
			
			try {
			
				gcalc.setAnchorPoint(mv.getLon(), mv.getLat());
				double angle = mv.getDirectionAngle();
				System.out.println(mv.getDirectionAngle()+" : "+mv.getSpeed());
				gcalc.setDirection(angle, mv.getSpeed()*Math.abs(dateDiffInSeconds));

				Point2D point = gcalc.getDestinationPoint();
				double lat = point.getY();
				double lon = point.getX();

				MorphGeoFeaturePair pair = new MorphGeoFeaturePair();
				pair.setFirstLat(mv.getLat());
				pair.setFirstLon(mv.getLon());
				pair.setLastLat(lat);
				pair.setLastLon(lon);

				morphGeoFeaturePairList.add(pair);
			
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return morphGeoFeaturePairList;
		
	}
	
	public static ArrayList<Point[]> getMorphImageCoordinatePairList(
			ArrayList<MorphGeoFeaturePair> morphGeoFeaturePairList, 
			Date startDate, Date endDate,
			Rectangle2D.Double extent, Dimension imageDim) {
		
		ArrayList<Point[]> imageCoordsPairList = new ArrayList<Point[]>();
		
		for (MorphGeoFeaturePair pair : morphGeoFeaturePairList) {
		
			Point[] imageCoordsPair = getImageCoordinatePair(pair, extent, imageDim);
			System.out.println(Arrays.toString(imageCoordsPair));
		
			imageCoordsPairList.add(imageCoordsPair);
		
		}
		
		return imageCoordsPairList;
	}

	
	
	public static ArrayList<Point[]> getMorphImageCoordinatePairList(
			ArrayList<MorphGeoFeaturePair> morphGeoFeaturePairList, 
			Rectangle2D.Double extent, Dimension imageDim) {
		
		ArrayList<Point[]> imagePairList = new ArrayList<Point[]>();
		
		for (MorphGeoFeaturePair pair : morphGeoFeaturePairList) {

			Point[] p = getImageCoordinatePair(pair, extent, imageDim);
			
			imagePairList.add(new Point[] { p[0], p[1] });
			
			System.out.println(p[0] + " , "+p[1]);
		}
		
		return imagePairList;
	}
	
	public static Point[] getImageCoordinatePair(MorphGeoFeaturePair pair, Rectangle2D.Double extent,
			Dimension imageDim) {
		
		double p1x = ((pair.getFirstLon() - extent.getMinX())/extent.getWidth())*imageDim.getWidth();
		double p1y = imageDim.getHeight()-((pair.getFirstLat() - extent.getMinY())/extent.getHeight())*imageDim.getHeight()-1;
		double p2x = ((pair.getLastLon() - extent.getMinX())/extent.getWidth())*imageDim.getWidth();
		double p2y = imageDim.getHeight()-((pair.getLastLat() - extent.getMinY())/extent.getHeight())*imageDim.getHeight()-1;
		
		Point p1 = new Point((int)p1x, (int)p1y);
		Point p2 = new Point((int)p2x, (int)p2y);
		
		return new Point[] { p1, p2 };
	}
	
	
	public interface SWDIRowHandler {
		public void processRow(String[] cols);
	}
	
	

	
	
	

	
	
	
	
	
	
	
	
	
	public static void main(String[] args) {
		try {
            Logger.getLogger("gov.noaa.ncdc").setLevel(Level.WARNING);

			
			
//			WCTMorphVectors mv = new WCTMorphVectors();
//			Rectangle2D.Double extent = new Rectangle2D.Double(-95.35,36.40, 1.5, 1.5); 
			
//			mv.queryL3StormTracking(new File("E:\\work\\morph\\testdata\\joplin\\KSGF20110522\\KSGF_SDUS33_NSTSGF_201105222243").toURI().toURL(), 
//					extent);

//			mv.querySWDI("nx3structure", SWDI_DATE_FORMAT.parse("201105222242"), SWDI_DATE_FORMAT.parse("201105222249"), extent);
			
//			System.out.println(mv.getMorphGeoFeaturePairList());
			
//			mv.getMorphImageCoordinatePairList(extent, new Dimension(500, 500));
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	
}
