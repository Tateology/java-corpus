package gov.noaa.ncdc.wct.decoders;


import gov.noaa.ncdc.wct.event.DataDecodeEvent;
import gov.noaa.ncdc.wct.event.DataDecodeListener;
import gov.noaa.ncdc.wct.io.SupportedDataType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class ParseGHCNOrder {

	
    private FeatureType featureType = null;
    private int geographicLocationColIndex = -1;
    private int dateColIndex = -1;
    private StringBuilder errorLog = new StringBuilder();
    
    private ArrayList<DataDecodeListener> listeners = new ArrayList<DataDecodeListener>();
    private DataDecodeEvent dataDecodeEvent = new DataDecodeEvent(this);
    
    private boolean cancel = false;
    
    /**
     * Parse GHCN-D order from NCDC CDO System <br>
     * 1) Geographic Coordinates MUST be added. <br>
     * 2) Output format of 'Custom GHCN-Daily Text' for delivery format. 
     * @param url
     * @param streamingProcessArray
     * @throws IOException
     * @throws ParseGHCNOrderException
     * @throws StreamingProcessException 
     */
	public void parseOrder(URL url, StreamingProcess[] streamingProcessArray) throws IOException, ParseGHCNOrderException, StreamingProcessException {
		
		dataDecodeEvent.setDataType(SupportedDataType.POINT_TIMESERIES);
		dataDecodeEvent.setProgress(-1);
		dataDecodeEvent.setStatus("Connecting to data...");
		for (DataDecodeListener l : listeners) {
			l.decodeStarted(dataDecodeEvent);
		}
		
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

		dataDecodeEvent.setStatus("Parsing data...");
		for (DataDecodeListener l : listeners) {
			l.decodeProgress(dataDecodeEvent);
		}

		
		String str = null;
		String headerLine = null;
		String spacingLine = null;
		
		if ( (headerLine = in.readLine()) == null) {
			throw new ParseGHCNOrderException("Error reading header line");
		}
		if ( (spacingLine = in.readLine()) == null) {
			throw new ParseGHCNOrderException("Error reading spacing line");
		}
		

//		STATION           STATION_NAME                                       GEOGRAPHIC_LOCATION             DATE     TMAX     TMIN     TOBS     
//		----------------- -------------------------------------------------- ------------------------------- -------- -------- -------- -------- 
//		GHCND:USC00163079 FARMERVILLE                                        32.775     -92.4075   54.864    20110914      367      211      239 
//		GHCND:USC00326365 NEW SALEM 5NW                                      46.8925    -101.4897  655.32    20110914      206      -22       56 
//		GHCND:USC00275013 EDWARD MACDOWELL LAKE                              42.8941    -71.9842   295.656   20110914      261      150      178 
//		GHCND:USW00053863 ATLANTA PEACHTREE AP                               33.875     -84.30222  305.296824 20110914      333      156     9999 

		// 1. determine number of columns (special geographic_location column counts as one)
		String[] spacingCols = spacingLine.split(" ");
		int numColumns = spacingCols.length;
		int[] colIndexLocations = getColumnIndexLocation(spacingCols);
		
		String[] columnNames = getFixedWidthColumns(headerLine, colIndexLocations);
		geographicLocationColIndex = getGeographicLocationColIndex(headerLine, spacingLine);
		dateColIndex = getDateColIndex(headerLine, spacingLine);
		
		
		if (geographicLocationColIndex < 0) {
			throw new ParseGHCNOrderException("No 'GEOGRAPHIC_LOCATION' column found");
		}
		
		setupFeatureType(columnNames);
		
		getLastParsedErrorLog().setLength(0);
		
		
		int geoIndex = 0;
		GeometryFactory geoFactory = new GeometryFactory();
		while ( (str = in.readLine()) != null) {
			
			if (cancel) {
				dataDecodeEvent.setStatus("Canceled");
				for (DataDecodeListener l : listeners) {
					l.decodeEnded(dataDecodeEvent);
				}
				cancel = false;
				
		        // close streaming processes
		        for (StreamingProcess process : streamingProcessArray) {
		        	process.close();
		        }
		        in.close();
		        return;
			}

			
			dataDecodeEvent.setStatus("Parsing data: "+geoIndex+" records parsed...");
			for (DataDecodeListener l : listeners) {
				l.decodeProgress(dataDecodeEvent);
			}

			
			
			String[] values = getFixedWidthColumns(str, colIndexLocations);

			String geoLoc = null;
			if (geographicLocationColIndex == colIndexLocations.length-1) {
				geoLoc = str.substring(colIndexLocations[geographicLocationColIndex], str.length());
			}
			else {
				geoLoc = str.substring(colIndexLocations[geographicLocationColIndex], colIndexLocations[geographicLocationColIndex+1]);
			}
			double[] latLonElev = parseGeographicLocation(geoLoc);
			
			
			try {

//				System.out.println(str);
				
				ArrayList<Object> objList = new ArrayList<Object>();
				objList.add(geoFactory.createPoint(new Coordinate(latLonElev[1], latLonElev[0])));
				objList.add(Integer.parseInt(values[getDateColIndex(headerLine, spacingLine)]));
				objList.add(new Double(latLonElev[0]));
				objList.add(new Double(latLonElev[1]));
				objList.add(new Double(latLonElev[2]));

	            for (int n=0; n<columnNames.length; n++) {
	            	if (n != dateColIndex && n != geographicLocationColIndex) {
	            		objList.add(values[n].trim());
	            	}
	            }
				
                Feature feature = featureType.create(
                		objList.toArray(new Object[objList.size()]),
                        new Integer(geoIndex++).toString());
                

                // pass through streaming processes
                for (StreamingProcess process : streamingProcessArray) {
                	process.addFeature(feature);
                }

				
			} catch (Exception e) {
				e.printStackTrace();
				getLastParsedErrorLog().append("[error]: "+e.getMessage()+"\n");
				getLastParsedErrorLog().append(str+"\n");
			}
			
		}
		
		System.err.println(getLastParsedErrorLog().toString());
		
        // close streaming processes
        for (StreamingProcess process : streamingProcessArray) {
        	process.close();
        }
        in.close();
        
		dataDecodeEvent.setStatus("");
		for (DataDecodeListener l : listeners) {
			l.decodeEnded(dataDecodeEvent);
		}

	}
	
	
	
	private void setupFeatureType(String[] columnNames) {
        // Set up attribute table
		try {
			
			ArrayList<AttributeType> attList = new ArrayList<AttributeType>();

            attList.add(AttributeTypeFactory.newAttributeType("geom", Point.class));
            attList.add(AttributeTypeFactory.newAttributeType("datetime", Integer.class, true, 15));
            attList.add(AttributeTypeFactory.newAttributeType("lat", Double.class, true, 10));
            attList.add(AttributeTypeFactory.newAttributeType("lon", Double.class, true, 10));
            attList.add(AttributeTypeFactory.newAttributeType("elev", Double.class, true, 10));
            
            for (int n=0; n<columnNames.length; n++) {
            	
            	if (n != dateColIndex && n != geographicLocationColIndex) {
            		if (columnNames[n].toLowerCase().contains("flag") ||
            				columnNames[n].trim().equalsIgnoreCase("STATION") ||
            				columnNames[n].trim().equalsIgnoreCase("STATION_NAME")) {
            			
            			attList.add(AttributeTypeFactory.newAttributeType(columnNames[n].toLowerCase().trim(), 
            					String.class, true, columnNames[n].length()));
            		}
            		else {
            			attList.add(AttributeTypeFactory.newAttributeType(columnNames[n].toLowerCase().trim(), 
            					Integer.class, true, columnNames[n].length()));
            		}
            	}
            }
                        
            featureType = FeatureTypeFactory.newFeatureType(attList.toArray(new AttributeType[attList.size()]), "GHCN-Daily");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	
	protected double[] parseGeographicLocation(String geoLocString) {

		// ignore whitespace during split
		String[] latLonElevString = geoLocString.split("[\\s]+");
		double[] latLonElev = new double[latLonElevString.length];
		for (int n=0; n<latLonElev.length; n++) {
			latLonElev[n] = Double.parseDouble(latLonElevString[n].trim());
		}
		return latLonElev;
	}
	
	
	protected int getGeographicLocationColIndex(String headerLine, String spacingLine) {
		int[] colIndexLocations = getColumnIndexLocation(spacingLine);
		String[] columnNames = getFixedWidthColumns(headerLine, colIndexLocations);
		int index = -1;
		for (int n=0; n<columnNames.length; n++) {
			if (columnNames[n].trim().equalsIgnoreCase("GEOGRAPHIC_LOCATION")) {
				index = n;
			}
		}
		return index;
	}
	
	protected int getDateColIndex(String headerLine, String spacingLine) {
		int[] colIndexLocations = getColumnIndexLocation(spacingLine);
		String[] columnNames = getFixedWidthColumns(headerLine, colIndexLocations);
		int index = -1;
		for (int n=0; n<columnNames.length; n++) {
			if (columnNames[n].trim().equalsIgnoreCase("DATE")) {
				index = n;
			}
		}
		return index;
	}
	
	
	protected int[] getColumnIndexLocation(String spacingLine) {
		return getColumnIndexLocation(spacingLine.split(" "));
	}
	
	protected int[] getColumnIndexLocation(String[] spacingCols) {
		int cnt = 0;
		int[] colIndexLocations = new int[spacingCols.length];
		for (int n=0; n<spacingCols.length; n++) {
			colIndexLocations[n] = cnt;
			cnt += spacingCols[n].length() + 1;
		}
		return colIndexLocations;
	}
	
	
	
	protected String[] getFixedWidthColumns(String line, int[] colIndexLocations) {
		String[] cols = new String[colIndexLocations.length];
		
		for (int n=0; n<colIndexLocations.length; n++) {
			if (n < colIndexLocations.length-1) {
				cols[n] = line.substring(colIndexLocations[n], colIndexLocations[n+1]-1);
			}
			else {
				cols[n] = line.substring(colIndexLocations[n], line.length());
			}
		}
		
		return cols;
	}

	public FeatureType getLastParsedFeatureType() {
		return featureType;
	}
	
	public StringBuilder getLastParsedErrorLog() {
		return errorLog;
	}

	public void addDataDecodeListener(DataDecodeListener listener) {
		listeners.add(listener);
	}
	public void removeDataDecodeListener(DataDecodeListener listener) {
		listeners.remove(listener);
	}
	public void clearDataDecodeListeners() {
		listeners.clear();
	}	
	public void setCancel(boolean cancel) {
		this.cancel = cancel;
	}
	public boolean isCancel() {
		return cancel;
	}


	public class ParseGHCNOrderException extends Exception {
		public ParseGHCNOrderException(String message) {
			super(message);
		}
	}
}
