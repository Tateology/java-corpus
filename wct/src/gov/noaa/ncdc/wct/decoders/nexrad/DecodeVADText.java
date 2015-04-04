package gov.noaa.ncdc.wct.decoders.nexrad;

import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.DecodeHintNotSupportedException;
import gov.noaa.ncdc.wct.decoders.StreamingProcess;
import gov.noaa.ncdc.wct.decoders.StreamingProcessException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Map;

import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.SchemaException;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;


public class DecodeVADText implements DecodeL3Alpha {

    private DecodeL3Header header;
    private FeatureType schema;
    private FeatureCollection features = FeatureCollections.newCollection();
    private URL url;
    private String[] supplementalData;
    
    public DecodeVADText(DecodeL3Header header) throws IOException, DecodeException {
        this.header = header;
        this.url = header.getDataURL();
        decodeData();
    }
    
    
    
    

//	@Override
	public Map<String, Object> getDecodeHints() {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
	public void setDecodeHint(String hintKey, Object hintValue)
			throws DecodeHintNotSupportedException {
		// TODO Auto-generated method stub
		
	}
    
    /**
     * Decodes data and stores with in-memory FeatureCollection
     * @return
     * @throws DecodeException
     */
//	@Override
    public void decodeData() throws DecodeException, IOException {
    	features.clear();
    	
    	StreamingProcess process = new StreamingProcess() {
			public void addFeature(Feature feature)
					throws StreamingProcessException {
				features.add(feature);
			}
			public void close() throws StreamingProcessException {
			    System.out.println("STREAMING PROCESS close() ::: fc.size() = "+features.size());
			}    		
    	};
    	
    	decodeData(new StreamingProcess[] { process } ); 	
    }
   
    
//	@Override
	public void decodeData(StreamingProcess[] processArray)
			throws DecodeException, IOException {
		
		decodeData(processArray, true);		
	}

//	@Override
	public void decodeData(StreamingProcess[] processArray, boolean autoClose)
			throws DecodeException, IOException {
		        
        features.clear();
        
        System.out.println("VERSION = "+header.getVersion()+" PCODE = "+header.getProductCode());
        
        String datetimeString = header.getDate()+header.getHourString()+header.getMinuteString()+header.getSecondString();

        int geoIndex = 0;
        GeometryFactory geoFactory = new GeometryFactory();
        
        try {

            // Set up attribute table
            AttributeType geom = AttributeTypeFactory.newAttributeType("geom", Point.class);
            AttributeType wsrid = AttributeTypeFactory.newAttributeType("wsrid", String.class, true, 5);
            AttributeType datetime = AttributeTypeFactory.newAttributeType("datetime", String.class, true, 15);
            AttributeType lat = AttributeTypeFactory.newAttributeType("lat", Double.class, true, 7);
            AttributeType lon = AttributeTypeFactory.newAttributeType("lon", Double.class, true, 7);
            AttributeType alt = AttributeTypeFactory.newAttributeType("alt", Double.class, true, 5);
            AttributeType u = AttributeTypeFactory.newAttributeType("u", Double.class, true, 5);
            AttributeType v = AttributeTypeFactory.newAttributeType("v", Double.class, true, 5);
            AttributeType w = AttributeTypeFactory.newAttributeType("w", Double.class, true, 5);
            AttributeType dir = AttributeTypeFactory.newAttributeType("dir", Double.class, true, 5);
            AttributeType spd = AttributeTypeFactory.newAttributeType("spd", Double.class, true, 5);
            AttributeType rms = AttributeTypeFactory.newAttributeType("rms", Double.class, true, 5);
            AttributeType div = AttributeTypeFactory.newAttributeType("div", Double.class, true, 5);
            AttributeType srng = AttributeTypeFactory.newAttributeType("srng", String.class, true, 5);
            AttributeType elev = AttributeTypeFactory.newAttributeType("elev", String.class, true, 5);
            AttributeType[] attTypes = {geom, wsrid, datetime, lat, lon, alt, u, v, w, dir, spd, rms, div, srng, elev};
            schema = FeatureTypeFactory.newFeatureType(attTypes, "Storm Structure Data");

        } catch (SchemaException se) {
            throw new IOException("CAUGHT SCHEMA EXCEPTION - ERROR PROCESSING: "+url);
        }
        
        
        NetcdfFile ncfile = NetcdfFile.open(url.toString());
        Variable var = ncfile.findVariable("TabMessagePage");
        Array data = var.read();
        Index index = data.getIndex();
        int[] shape = data.getShape();
        System.out.println("Data Array Dimensions: ");
        for (int n=0; n<shape.length; n++) {      
           System.out.println("Dimension["+n+"] " + shape[n]);
        }
        
        supplementalData = new String[shape[0]];
        
        for (int n=0; n<shape[0]; n++) {
            System.out.println("-------------- n="+n);
            String pageString = data.getObject(index.set(n)).toString();
            System.out.println(pageString);

            supplementalData[n] = pageString;
            
            if (! pageString.contains("VAD Algorithm Output")) {
                continue;
            }

            
            BufferedReader pageReader = new BufferedReader(new StringReader(pageString));
            // skip first three lines
            pageReader.readLine();
            pageReader.readLine();
            pageReader.readLine();

            String str = null;
            while ( (str = pageReader.readLine()) != null) {
                
                System.out.println("str: "+str);
                
        
//        VAD Algorithm Output  08/29/05  09:17                       
//        ALT      U       V       W    DIR   SPD   RMS     DIV     SRNG    ELEV      
//       100ft    m/s     m/s    cm/s   deg   kts   kts    E-3/s     nm      deg      
//        006   -19.4    -3.0     NA    081   038   3.0      NA      5.67    0.5
//    0         1         2         3         4         5         6         7         8        
//    012345678901234567890123456789012345678901234567890123456789012345678901234567890
                
                

                try {
                    // AttributeType[] attTypes = {geom, wsrid, datetime, lat, lon, alt, u, v, w, dir, spd, rms, div, srng, elev};
                    Feature feature = schema.create(
                            new Object[]{
                                    geoFactory.createPoint(new Coordinate(header.getLon(), header.getLat())), // geom
                                    header.getICAO(), // wsrid
                                    datetimeString, 
                                    new Double(header.getLon()),
                                    new Double(header.getLat()),
                                    new Double(NexradUtilities.stripCharsDouble(str.substring( 3,  7))), // alt
                                    new Double(NexradUtilities.stripCharsDouble(str.substring( 9, 15))), // u
                                    new Double(NexradUtilities.stripCharsDouble(str.substring(16, 23))), // v
                                    new Double(NexradUtilities.stripCharsDouble(str.substring(24, 32))), // w
                                    new Double(NexradUtilities.stripCharsDouble(str.substring(33, 38))), // dir
                                    new Double(NexradUtilities.stripCharsDouble(str.substring(38, 44))), // spd
                                    new Double(NexradUtilities.stripCharsDouble(str.substring(44, 49))), // rms
                                    new Double(NexradUtilities.stripCharsDouble(str.substring(50, 58))), // div
                                    new Double(NexradUtilities.stripCharsDouble(str.substring(60, 69))), // srng
                                    new Double(NexradUtilities.stripCharsDouble(str.substring(69, 75))) // elev
                            },
                            new Integer(geoIndex++).toString());
                    // add to streaming processes
                    for (int s=0; s<processArray.length; s++) {
                        processArray[s].addFeature(feature);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new DecodeException("DECODE EXCEPTION IN VAD FILE - CODE 1 ", header.getDataURL());
                }


                
            }
        }

    }
    
    
    
    public FeatureCollection getFeatures() { 
        return features;
    }
    
    
    
    
    
    
    
    
    /** 
     * Returns the supplemental text data array  
     */
    public String[] getSupplementalDataArray() {
        return supplementalData;
    }
    
    
    public java.awt.geom.Rectangle2D.Double getNexradExtent() { 
        return header.getNexradBounds(); 
    }
    
    /**
     * Returns the feature types used for these features
     * 
     * @return The featureType value
     */
    public FeatureType[] getFeatureTypes() {
        return new FeatureType[] {schema};
    }
    
    
    public FeatureCollection getLineFeatures() { return null; }
    
    public FeatureType getLineFeatureType() { return null; }
    public String getMetaLabel(int index) { return null; }
    public String getDefaultSymbol() { return null; }
    
  

}
