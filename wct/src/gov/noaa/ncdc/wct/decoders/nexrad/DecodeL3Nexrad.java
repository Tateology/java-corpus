/**
 * NOAA's National Climatic Data Center
 * NOAA/NESDIS/NCDC
 * 151 Patton Ave, Asheville, NC  28801
 * 
 * THIS SOFTWARE AND ITS DOCUMENTATION ARE CONSIDERED TO BE IN THE 
 * PUBLIC DOMAIN AND THUS ARE AVAILABLE FOR UNRESTRICTED PUBLIC USE.  
 * THEY ARE FURNISHED "AS IS." THE AUTHORS, THE UNITED STATES GOVERNMENT, ITS
 * INSTRUMENTALITIES, OFFICERS, EMPLOYEES, AND AGENTS MAKE NO WARRANTY,
 * EXPRESS OR IMPLIED, AS TO THE USEFULNESS OF THE SOFTWARE AND
 * DOCUMENTATION FOR ANY PURPOSE. THEY ASSUME NO RESPONSIBILITY (1)
 * FOR THE USE OF THE SOFTWARE AND DOCUMENTATION; OR (2) TO PROVIDE
 * TECHNICAL SUPPORT TO USERS.
 */

package gov.noaa.ncdc.wct.decoders.nexrad;

import gov.noaa.ncdc.common.Hex;
import gov.noaa.ncdc.projections.HRAPProjection;
import gov.noaa.ncdc.wct.WCTFilter;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.DecodeHintNotSupportedException;
import gov.noaa.ncdc.wct.decoders.MaxGeographicExtent;
import gov.noaa.ncdc.wct.decoders.StreamingProcess;
import gov.noaa.ncdc.wct.decoders.StreamingProcessException;
import gov.noaa.ncdc.wct.decoders.cdm.DecodeRadialDatasetSweep;
import gov.noaa.ncdc.wct.decoders.cdm.DecodeRadialDatasetSweepHeader;
import gov.noaa.ncdc.wct.event.DataDecodeEvent;
import gov.noaa.ncdc.wct.event.DataDecodeListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import org.acplt.oncrpc.OncRpcException;
import org.geotools.ct.MathTransform;
import org.geotools.data.shapefile.shp.JTSUtilities;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.pt.CoordinatePoint;
import org.opengis.referencing.operation.TransformException;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dt.RadialDatasetSweep;
import ucar.nc2.dt.RadialDatasetSweep.RadialVariable;
import ucar.nc2.dt.TypedDatasetFactory;
import ucar.unidata.io.InMemoryRandomAccessFile;
import uk.ac.starlink.util.Compression;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;


/**
 *  Decodes NEXRAD Level-III Data into OGC Geotools Features.  Data is stored in memory
 *  as a FeatureCollection of polygons.
 *
 * @author     steve.ansari
 * @created    July 23, 2004
 */
public class DecodeL3Nexrad implements StreamingRadialDecoder {

/*    
    1) Add product code constant to NexradHeader
    2) Add product type determination to 'getProductType' in DecodeL3Header
    3) Implement the value-threshold mapping for 8-bit products if needed.  'get8bitValue' in DecodeL3Nexrad.
    4) Add custom max bounds value in MaxGeographicExtent if needed.
    5) Set color mappings in NexradColorFactory.
    6) Set max/min value range in NexradValueFactory.
    7) Update legend category values in LegendCategoryFactory.
    8) Add product code in: Level3ProductCheck
    9) Add product code in: ext/config/nexrad-level3-prodlist.txt
    10) Update legend listings in NexradLegendLabelFactory
    
    For 8-bit conversions, refer to 3-34 in 2620001N.pdf - ROC ICD
    
    
    
*/
    
    private static final Logger logger = Logger.getLogger(DecodeL3Nexrad.class.getName());

    /**
     *  Description of the Field
     */
    //	public final static int MM = 0, INCH = 1;
    /**
     *  Description of the Field
     */
    //	public final static int LATLON = 1, ALBERS = 2, STEREO = 3, HRAP = 4;

    private FeatureCollection features = FeatureCollections.newCollection();
    private FeatureType schema = null;
    private GeometryFactory geoFactory = new GeometryFactory();

    private Map<String, Object> hintsMap;



    private NexradHeader header;

    private Vector<Polygon>[] polyVector = new Vector[16];
    private Vector<Coordinate> coordinates = new Vector<Coordinate>();

    private DPARaster dpaRaster = null;

    private WCTProjections nexradProjection = new WCTProjections();



    // The list of event listeners.
    private Vector<DataDecodeListener> listeners = new Vector<DataDecodeListener>();


    // Global variables   
    private ucar.unidata.io.RandomAccessFile f;
    private MathTransform nexradTransform;
    private DataDecodeEvent event;
    private WCTFilter nxfilter;
    private int geoIndex;   

    private String[] supplementalData;


    /**
     *Constructor for the DecodeL3Nexrad object
     *
     * @param  header  A DecodeL3Header object
     */
    public DecodeL3Nexrad(DecodeL3Header header) {
        this(header, null);
    }


    /**
     *Constructor for the DecodeL3Nexrad object with supplied FeatureCollection
     *
     * @param  header    A DecodeL3Header object
     * @param  features  FeatureCollection to populate
     */
    public DecodeL3Nexrad(DecodeL3Header header, FeatureCollection features) {
        this.header = header;
        this.features = features;
        if (features == null) {
            features = FeatureCollections.newCollection();
        }
        init();
    }


    /**
     *  Description of the Method
     */
    private void init() {
        try {


            hintsMap = new HashMap<String, Object>();

            // TODO: instead of using the NexradFilter object, use the hints map
            // to define the attributes managed by the NexradFilter class 
            hintsMap.put("nexradFilter", new WCTFilter());

            // Use JTS Geometry.buffer(0.0) to combine adjacent polygons
            hintsMap.put("reducePolygons", new Boolean(false));



            AttributeType geom = AttributeTypeFactory.newAttributeType("geom", Geometry.class);
            AttributeType value = AttributeTypeFactory.newAttributeType("value", Float.class, true, 5);
            AttributeType colorIndex = AttributeTypeFactory.newAttributeType("colorIndex", Integer.class, true, 4);
            AttributeType[] attTypes = {geom, value, colorIndex};
            schema = FeatureTypeFactory.newFeatureType(attTypes, "Nexrad Attributes");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // initialize the dpa raster object
        dpaRaster = new DPARaster();

    }


    /**
     * Returns the FeatureType
     *
     * @return    The featureType value
     */
    public FeatureType[] getFeatureTypes() {

        return new FeatureType[] {schema};
    }


    /**
     * Returns Rectangle.Double Bounds for the NEXRAD Site calculated during decode. (unique to product)
     * Could be 248, 124 or 32 nmi.
     *
     * @return    The bounds value
     */
    public java.awt.geom.Rectangle2D.Double getBounds() {
        return (MaxGeographicExtent.getNexradExtent(header));
        //Envelope env = features.getBounds();
        //return (new java.awt.geom.Rectangle2D.Double(env.getMinX(), env.getMinY(), env.getWidth(), env.getHeight()));
    }





    /**
     * Get the key-value pairs for the current decode hints.  
     * If no hints have been set, this will return the supported
     * hints with default values.
     * @return
     */
    public Map<String, Object> getDecodeHints() {
        return hintsMap;
    }

    /**
     * Set a decodeHint.  To get a list of supported hints and default values,
     * use 'getDecodeHints()'.  The currently supported hints are as follows: <br><br>
     * <ol>
     *  <li> <b>nexradFilter</b>: 
     *  		NexradFilter object that defines filtering options on range, azimuth, 
     *  		height and geographic bounds.
     *  <li> <b>reducePolys</b>: 
     *  		Reduce polygons for Level-III classification groups using a JTS buffer(0.0) command.
     * @param hintsMap
     */
    public void setDecodeHint(String hintKey, Object hintValue) throws DecodeHintNotSupportedException {
        if (! hintsMap.keySet().contains(hintKey)) {
            throw new DecodeHintNotSupportedException(this.getClass().toString(), hintKey, hintsMap);
        }
        hintsMap.put(hintKey, hintValue);
    }



    /**
     * Decodes data and stores with in-memory FeatureCollection
     * @return
     * @throws DecodeException
     * @throws IOException 
     */
    public void decodeData() throws DecodeException, IOException {
        if (features == null) {
            features = FeatureCollections.newCollection();
        }
        features.clear();

        StreamingProcess process = new StreamingProcess() {
            public void addFeature(Feature feature)	throws StreamingProcessException {
                //                System.out.println(feature);
                features.add(feature);
            }
            public void close() throws StreamingProcessException {
                logger.info("STREAMING PROCESS close() ::: fc.size() = "+features.size());
            }    		
        };

        decodeData(new StreamingProcess[] { process } );

    }












    /**
     * Decodes data.  Each decoded feature is passed to a StreamingProcess object as the file is decoded.
     * @param processArray  Array of StreamingProcess objects.  
     */
    public void decodeData(StreamingProcess[] processArray) throws DecodeException, IOException {
        decodeData(processArray, true);
    }


    /**
     * Decodes data.  Each decoded feature is passed to a StreamingProcess object as the file is decoded.
     * @param processArray  Array of StreamingProcess objects.  
     * @param autoClose  Do we call the .close() method for each StreamingProcess object when we are finished? 
     */
    public void decodeData(StreamingProcess[] processArray, boolean autoClose)
    throws DecodeException, IOException {


        this.nxfilter = (WCTFilter)hintsMap.get("nexradFilter");
        boolean reducePolys = (Boolean)hintsMap.get("reducePolygons");

        event = new DataDecodeEvent(this);

        // Start decode
        // --------------
        for (int i = 0; i < listeners.size(); i++) {
            event.setProgress(0);
            listeners.get(i).decodeStarted(event);
        }





        features = FeatureCollections.newCollection();
        //features.clear();

        for (int i = 0; i < 16; i++) {
            polyVector[i] = new Vector<Polygon>();
        }

        // Reset index counter
        geoIndex = 0;


        try {
            // Use Geotools Proj4 implementation to get MathTransform object
            nexradTransform = nexradProjection.getRadarTransform(header);
        } catch (Exception e) {
            throw new DecodeException("PROJECTION TRANSFORM ERROR", header.getDataURL());
        }



        // Initiate binary buffered read
        f = header.getRandomAccessFile();

        int pcode = header.getProductCode();
        try {










            logger.fine("======== DECODE DATA 0 =========: pcode="+pcode);

            // Check for type of data: radial, raster, dpa
            if (header.getProductType() == NexradHeader.L3RADIAL) {
                logger.info("DECODING LEVEL-III RADIAL DATA TO WGS84");

                decodeRadial();


            }
            else if (header.getProductType() == NexradHeader.L3RADIAL_8BIT &&
            		header.getProductCode() == NexradHeader.L3PC_DIGITAL_INSTANTANEOUS_PRECIP_RATE) {
            	logger.info("DECODING LEVEL-III GENERIC RADIAL DATA TO WGS84");
            	
            	decodeGenericXDRRadial(processArray);
            	
            	// for generic radial products, we don't do polygon reduction or classifications so we return
            	return;
            }
            // Check for type of data: radial, raster, dpa
            else if (header.getProductType() == NexradHeader.L3RADIAL_8BIT) {
                logger.info("DECODING LEVEL-III 8-BIT RADIAL DATA TO WGS84");

                decodeRadial8bit(processArray);

                // For 8 bit products, we don't do polygon reduction or organize polygons into classification list
                return;
            }
            // END if(pcode == RADIAL)
            else if (header.getProductType() == NexradHeader.L3RASTER) {
                logger.info("DECODING LEVEL-III RASTER DATA TO WGS84");

                decodeRaster();

            }
            // END else if == RASTER loop
            else if (header.getProductType() == NexradHeader.L3DPA) {
                logger.info("DECODING LEVEL-III DPA DATA TO WGS84");

                decodeDPA(processArray);

                // For DPA, we don't do polygon reduction or organize polygons into classification list
                return;
            }















            //logger.fine("======== DECODE DATA 2 =========");


            f.close();
            // Close connection;

            if (reducePolys) {
                logger.info("REDUCING POLYGONS!");
                // Reduce number of polygons by applying a 0 distance buffer to each vector of polygons
                GeometryCollection[] polyCollections = new GeometryCollection[16];
                for (int i = 0; i < 16; i++) {
                    if (polyVector[i].size() > 0) {
                        Polygon[] polyArray = new Polygon[polyVector[i].size()];
                        polyCollections[i] = geoFactory.createGeometryCollection((Polygon[]) (polyVector[i].toArray(polyArray)));
                        Geometry union = polyCollections[i].buffer(0.0);

                        polyCollections[i] = null;
                        //Geometry union = (Geometry)polyCollections[i];

                        Float value = getFloatDataThreshold(i);
                        Integer color = new Integer(i + 1);

                        try {
                            // create the feature
                            Feature feature = schema.create(
                                    new Object[]{
                                            (Geometry) union,
                                            value,
                                            color
                                    }, new Integer(geoIndex++).toString());
                            for (int n=0; n<processArray.length; n++) {
                                processArray[n].addFeature(feature);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
            else {
                //GeometryCollection[] polyCollections = new GeometryCollection[16];
                for (int i = 0; i < polyVector.length; i++) {
                    Integer color = new Integer(i);
                    Float value = getFloatDataThreshold(i);

                    logger.fine(((DecodeL3Header)header).getDataThresholdString(i) +
                            "   " + value);

                    for (int j = 0; j < polyVector[i].size(); j++) {
                        try {
                            //							logger.fine(color);
                            // create the feature
                            Feature feature = schema.create(
                                    new Object[]{
                                            (Geometry) polyVector[i].elementAt(j),
                                            value,
                                            color
                                    }, new Integer(geoIndex++).toString());
                            for (int n=0; n<processArray.length; n++) {
                                processArray[n].addFeature(feature);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }


        } catch (IOException e) {
        	throw e;

        } catch (Exception e) {
            logger.severe("CAUGHT EXCEPTION:  " + e);
            e.printStackTrace();

            try {
                f.close();
            } catch (Exception eee) {
                e.printStackTrace();
            }

            e.printStackTrace();
            throw new DecodeException("CAUGHT EXCEPTION:  \n" + e + "\n--- THIS DATA IS POSSIBLY CORRUPT ---", header.getDataURL());

        } finally {

            for (int n=0; n<processArray.length; n++) {
                try {
                    processArray[n].close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            // End decode
            // --------------
            for (int i = 0; i < listeners.size(); i++) {
                event.setProgress(100);
                listeners.get(i).decodeEnded(event);
            }


        }

    }










    private double get8bitValue(int level, int[] thresholdHalfwords, float[] scaleOffset) {
    	
    	
    	
        if (header.getProductCode() == 186) {
            // hard code because of error in threshold byte values for TZL (186)
            //hw31: -320
            //hw32: 5
            //hw33: 256
            thresholdHalfwords = new int[] { -320, 5, 256 };
        }


        if (header.getProductCode() == NexradHeader.L3PC_DIGITAL_HYBRID_SCAN || 
                header.getProductCode() == NexradHeader.L3PC_LONG_RANGE_BASE_REFLECTIVITY_8BIT ||
                header.getProductCode() == NexradHeader.L3PC_TDWR_BASE_REFLECTIVITY_8BIT || 
                header.getProductCode() == NexradHeader.L3PC_TDWR_LONG_RANGE_BASE_REFLECTIVITY_8BIT) {
        	
        	
            // 0 = below threshold, 1 = missing
            if (level == 0 || level == 1) {
                return -999;
            }
            //            System.out.println("level="+level+" pcode="+pcode+" "+Arrays.toString(thresholdHalfwords));

            return thresholdHalfwords[0]/10.0 + level*thresholdHalfwords[1]/10.0;

        }
        else if (header.getProductCode() == NexradHeader.L3PC_TDWR_BASE_VELOCITY_8BIT ||
                header.getProductCode() == NexradHeader.L3PC_LONG_RANGE_BASE_VELOCITY_8BIT) {

            // 0 = below threshold, 1 = range folded
            if (level == 0) {
                return -999;
            }
            else if (level == 1) {
                return 800.0;
//                return -999;
            }


            //            System.out.println("level="+level+" pcode="+pcode+" "+Arrays.toString(thresholdHalfwords));

            // convert to knots
            return 1.94384*(thresholdHalfwords[0]/10.0 + level*thresholdHalfwords[1]/10.0);

        }
        else if (header.getProductCode() == NexradHeader.L3PC_DIGITAL_HYBRID_PRECIP) {
            if (level == 0) {
                return -999;
            }

            return thresholdHalfwords[1]*0.01*level;
        }
        else if (header.getProductCode() == NexradHeader.L3PC_ENHANCED_ECHO_TOPS) {
            // 0 = below threshold, 1 = bad data
            if (level == 0) {
                return -999;
            }
            else if (level == 1) {
                //                return -200.0;
                return -999;
            }
            else {
//                System.out.println((float)( level & thresholdHalfwords[0])/ (float) thresholdHalfwords[1] - (float) thresholdHalfwords[2]);
//                System.out.println("("+level +" & "+ thresholdHalfwords[0]+") / "+ thresholdHalfwords[1] + " - " + thresholdHalfwords[2]);
                return (float)((level & thresholdHalfwords[0])/ (float) thresholdHalfwords[1]) - (float) thresholdHalfwords[2];   
            }
        }
        else if (header.getProductCode() == NexradHeader.L3PC_DIGITAL_VERT_INT_LIQUID) {
            // 0 = below threshold, 1 = flagged data
            if (level == 0) {
                return -999;
            }
            else if (level == 1) {
                //                return -200.0;
                return -999;
            }
            else {
                float a = getHexDecodeValue((short)thresholdHalfwords[0]);
                float b = getHexDecodeValue((short)thresholdHalfwords[1]);
                float c = getHexDecodeValue((short)thresholdHalfwords[3]);
                float d = getHexDecodeValue((short)thresholdHalfwords[4]);
                int ival = unsignedByteToInt((byte)level);
                if (ival < 20) {
//                    System.out.println(""+((float)(ival - b)/a));
                    return (float)(ival - b)/a;
                }
                else {
                    float t =  (float)(ival - d)/c;
//                    System.out.println((float) Math.exp(t));
                    return (float) Math.exp(t);
                }                
            }
        }
        else if (header.getProductCode() == NexradHeader.L3PC_DIGITAL_DIFFERENTIAL_REFLECTIVITY) {
        	if (level == 0 || level == 2) {
//        		return -999;
        		return Double.NaN;
        	}
        	else if (level == 1) {
        		// RF flag
        		return 800.0;
        	}
        	else {
//        		System.out.println(scaleOffset[0] + " , "+ scaleOffset[1]);
//        		return (level-128)/16.0;
            	return (level == 0) ? Double.NaN : (((float)level-scaleOffset[1])/scaleOffset[0]);
        	}
        }
        else if (header.getProductCode() == NexradHeader.L3PC_DIGITAL_CORRELATION_COEFFICIENT) {
        	if (level == 0 || level == 2) {
//        		return -999;
//        		return 800.0;
        		return Double.NaN;
        	}
        	else if (level == 1) {
        		// RF flag
        		return 800.0;
        	}
        	else {
//        		System.out.println(scaleOffset[0] + " , "+ scaleOffset[1]);
//        		return (level+60.5)/300.0;
            	return (level == 0) ? Double.NaN : (((float)level-scaleOffset[1])/scaleOffset[0]);
        	}
        }
        else if (header.getProductCode() == NexradHeader.L3PC_DIGITAL_SPECIFIC_DIFFERENTIAL_PHASE) {
        	if (level == 0 || level == 2) {
//        		return -999;
//        		return 800;
        		return Double.NaN;
        	}
        	else if (level == 1) {
        		// RF flag
        		return 800.0;
        	}
        	else {
//        		System.out.println(scaleOffset[0] + " , "+ scaleOffset[1]);
//        		return (level-43)/20.0;
            	return (level == 0) ? Double.NaN : (((float)level-scaleOffset[1])/scaleOffset[0]);
        	}
        }
        else if (header.getProductCode() == NexradHeader.L3PC_DIGITAL_ONE_HOUR_ACCUMULATION) {
//    		System.out.println(scaleOffset[0] + " , "+ scaleOffset[1]);
//        	return (level == 0) ? Double.NaN : level*0.01;
        	return (level == 0) ? Double.NaN : (((float)level-scaleOffset[1])/scaleOffset[0])*(0.01);
        }
        else if (header.getProductCode() == NexradHeader.L3PC_DIGITAL_HYDROMETEOR_CLASSIFICATION) {
        	return (level == 0) ? Double.NaN : level*0.1;
        }
        else if (header.getProductCode() == NexradHeader.L3PC_DIGITAL_HYBRID_HYDROMETEOR_CLASSIFICATION) {
        	return (level == 0) ? Double.NaN : level*0.1;
        }
        else if (header.getProductCode() == NexradHeader.L3PC_DIGITAL_STORM_TOTAL_ACCUMULATION) {
//    		System.out.println(scaleOffset[0] + " , "+ scaleOffset[1]);
//        	return (level == 0) ? Double.NaN : (((float)level*0.01)-scaleOffset[1])/scaleOffset[0];
        	return (level == 0) ? Double.NaN : (((float)level-scaleOffset[1])/scaleOffset[0])*(0.01);
        }
        else if (header.getProductCode() == NexradHeader.L3PC_DIGITAL_USER_SELECTABLE_ACCUMULATION) {
//    		System.out.println(scaleOffset[0] + " , "+ scaleOffset[1]);
//        	return (level == 0) ? Double.NaN : level*0.01;
        	return (level == 0) ? Double.NaN : (((float)level-scaleOffset[1])/scaleOffset[0])*(0.01);
        }
        else if (header.getProductCode() == NexradHeader.L3PC_DIGITAL_ONE_HOUR_ACCUMULATION_DIFFERENCE) {
//    		System.out.println(scaleOffset[0] + " , "+ scaleOffset[1]);
//        	return (level == 0 || level == 128) ? Double.NaN : (level-128)*0.01;
        	return (level == 0) ? Double.NaN : (((float)level-scaleOffset[1])/scaleOffset[0])*(0.01);
        }
        else if (header.getProductCode() == NexradHeader.L3PC_DIGITAL_STORM_TOTAL_ACCUMULATION_DIFFERENCE) {
//    		System.out.println(scaleOffset[0] + " , "+ scaleOffset[1]);
//        	return (level == 0 || level == 128) ? Double.NaN : (level-128)*0.01;
        	return (level == 0) ? Double.NaN : (((float)level-scaleOffset[1])/scaleOffset[0])*(0.01);
        }
        else {
            return level;
        }
        
        
        
        
        
        
        
//    }  else if (vName.startsWith("EnhancedEchoTop")) {
//        int[] levels = vinfo.len;
//        int iscale = vinfo.code;
//        float[] fdata = new float[npixel];
//        for (int i = 0; i < npixel; i++) {
//          int ival = unsignedByteToInt(pdata[i]);
//          if (ival == 0 && ival == 1)
//            fdata[i] = Float.NaN;
//          else
//            fdata[i] = (float)( ival & levels[0])/ (float) levels[1] + (float) levels[2];
//        }
//        return fdata;
//
//      } else if (vName.startsWith("DigitalIntegLiquid")) {
//        int[] levels = vinfo.len;
//        int iscale = vinfo.code;
//        float[] fdata = new float[npixel];
//        float a = getHexDecodeValue((short)levels[0]);
//        float b = getHexDecodeValue((short)levels[1]);
//        float c = getHexDecodeValue((short)levels[3]);
//        float d = getHexDecodeValue((short)levels[4]);
//        for (int i = 0; i < npixel; i++) {
//          int ival = unsignedByteToInt(pdata[i]);
//          if(ival == 0 || ival ==1)
//            fdata[i] = Float.NaN;
//          else if (ival < 20)
//            fdata[i] = (float)(ival - b)/a;
//          else {
//            float t =  (float)(ival - d)/c;
//            fdata[i] = (float) Math.exp(t);
//          }
//        }
//        return fdata;
//
//      }
        

    }




    private void decodeRadial8bit(StreamingProcess[] processArray) throws TransformException, IOException, DecodeException {


        // rewind 
        f.seek(0);
        // ADVANCE PAST WMO HEADER
        while (f.readShort() != -1) {
            ;
        }




        /*
         *  / Decode Date and Time
         *  hour = (short)(uniqueInfo[4]/60.0);
         *  minute = (short)(uniqueInfo[4]%60.0);
         *  yyyymmdd = convertJulianDate(uniqueInfo[3]);
         *  logger.fine("LAT: "+lat);
         *  logger.fine("LON: "+lon);
         *  logger.fine("ALT: "+alt);
         *  logger.fine("MAX: "+uniqueInfo[0]);
         *  logger.fine("BIAS: "+uniqueInfo[1]);
         *  logger.fine("ERR.VAR.: "+uniqueInfo[2]);
         *  logger.fine(yyyymmdd+" "+hour+":"+minute);
         */
        //-------------------------------*
        // Decode the actual RLE data
        //-------------------------------*
        // dataHeader[0] = PACKET CODE FOR BASE REFLECTIVITY -- SHOULD == ?
        // dataHeader[1] = INDEX OF FIRST RANGE BIN
        // dataHeader[2] = NUMBER OF RANGE  BINS
        // dataHeader[3] = I CENTER OF SWEEP
        // dataHeader[4] = J CENTER OF SWEEP
        // dataHeader[5] = RANGE SCALE FACTOR
        // dataHeader[6] = NUMBER OF RADIALS



        f.skipBytes(100);

        byte[] magic = new byte[3];
        f.read(magic);
        Compression compression = Compression.getCompression(magic);
        //        System.out.println(compression.toString());
        f.skipBytes(-3);

        long compressedFileSize = f.length()-f.getFilePointer();
        byte[] buf = new byte[(int)compressedFileSize];
        f.read(buf);
        f.close();
        InputStream decompStream = compression.decompress(new ByteArrayInputStream(buf));
        DataInputStream dis = new DataInputStream(decompStream);

        // ADVANCE TO BEGINNING OF PRODUCT SYMBOLOGY BLOCK (BLOCK DIVIDER)
        short blockDivider = dis.readShort();
        short blockID = dis.readShort();
        int blockLen = dis.readInt();
        short numLayers = dis.readShort();

//                System.out.println("blockDivider="+blockDivider);
//                System.out.println("blockID="+blockID);
//                System.out.println("blockLen="+blockLen);
//                System.out.println("numLayers="+numLayers);

        // advance past next divider
        while (dis.readShort() != -1);
        int layerLen = dis.readInt();
//        System.out.println("layerLen="+layerLen);
        short packetCode = dis.readShort();
        if (packetCode == 1) {
//        	int blen = dis.readShort();
//        	int byteCount = 0;
//    		int i = dis.readShort();
//    		int j = dis.readShort();
//    		byteCount = 4;
//        	while (byteCount < blen) {
//        		System.out.println(new String(new byte[] { dis.readByte() }));
//        		byteCount++;
//        	}
        	f.seek(0);
        	return;
        }
        else if (packetCode != 16) {
        	f.seek(0);
            throw new DecodeException("8-bit packet type=16 NOT FOUND.  Found packet code of: "+packetCode, header.getDataURL());
        }

        InMemoryRandomAccessFile inRaf = new InMemoryRandomAccessFile("Threshold bytes", 
                ((DecodeL3Header)header).getDataThresholdBytes());
        inRaf.order(ucar.unidata.io.RandomAccessFile.BIG_ENDIAN);

        inRaf.seek(0);
        int hw31 = inRaf.readShort(); // halfword 31
        int hw32 = inRaf.readShort(); // halfword 32
        int hw33 = inRaf.readShort(); // halfword 33
        int hw34 = inRaf.readShort(); // halfword 34
        int hw35 = inRaf.readShort(); // halfword 35

        inRaf.seek(0);
//        inRaf.order(ucar.unidata.io.RandomAccessFile.LITTLE_ENDIAN);
        float scale = inRaf.readFloat(); // halfwords 31 & 32
        float offset = inRaf.readFloat(); // halfwords 33 & 34
        inRaf.close();

        //        System.out.println("hw31: "+hw31);
        //        System.out.println("hw32: "+hw32);
        //        System.out.println("hw33: "+hw33);
        //        System.out.println("hw34: "+hw34);
        //        System.out.println("hw35: "+hw35);

        int[] thresholdHalfwords = new int[] { hw31, hw32, hw33, hw34, hw35 };
        float[] scaleOffset = new float[] { scale, offset };


        if (header.getProductCode() == 32 || header.getProductCode() == 94 ||
                header.getProductCode() == 180 || header.getProductCode() == 186) {

            //            System.out.println("minDbz: "+hw31+"  dbzInc: "+hw32+"  numLevels: "+hw33);
        }


        short[] dataHeader = new short[6];
        for (int i = 0; i < 6; i++) {
            dataHeader[i] = dis.readShort();
//                        System.out.println("dataHeader["+i+"]="+dataHeader[i]);
            logger.fine("dataHeader["+i+"]="+dataHeader[i]);
        }




        double binSpacing = dataHeader[4];

        if (header.getProductCode() == NexradHeader.L3PC_TDWR_LONG_RANGE_BASE_REFLECTIVITY_8BIT) {
            binSpacing = 300;
        }
        else if (header.getProductCode() == NexradHeader.L3PC_TDWR_BASE_REFLECTIVITY_8BIT ||
                header.getProductCode() == NexradHeader.L3PC_TDWR_BASE_VELOCITY_8BIT) {
            binSpacing = 150;
        }
        else if (header.getProductCode() == NexradHeader.L3PC_LONG_RANGE_BASE_VELOCITY_8BIT ||
        		header.getProductCode() == NexradHeader.L3PC_DIGITAL_DIFFERENTIAL_REFLECTIVITY ||
        		header.getProductCode() == NexradHeader.L3PC_DIGITAL_CORRELATION_COEFFICIENT ||
        		header.getProductCode() == NexradHeader.L3PC_DIGITAL_SPECIFIC_DIFFERENTIAL_PHASE ||
        		header.getProductCode() == NexradHeader.L3PC_DIGITAL_ONE_HOUR_ACCUMULATION ||
        		header.getProductCode() == NexradHeader.L3PC_DIGITAL_HYDROMETEOR_CLASSIFICATION ||
        		header.getProductCode() == NexradHeader.L3PC_DIGITAL_HYBRID_HYDROMETEOR_CLASSIFICATION) {
            binSpacing = 250;
        }
        else if (header.getProductCode() == NexradHeader.L3PC_ENHANCED_ECHO_TOPS ||
                header.getProductCode() == NexradHeader.L3PC_DIGITAL_VERT_INT_LIQUID) {
            binSpacing = 1000;
        }

        //        System.out.println("BIN SPACING: "+binSpacing);







        int numRadials = dataHeader[5];
        int numRangeBins = dataHeader[1];
        //        int numBytes = (numRangeBins % 2 == 1) ? numRangeBins++ : numRangeBins;


        double xpos, ypos, angle1_sin, angle1_cos, angle2_sin, angle2_cos;
        double[] geoXY;


        // represents the LOWER LEFT CORNER of the range bin
        Coordinate[][] indexedCoordArray = new Coordinate[numRadials+1][numRangeBins+1];

//        maxValue = -999;

        for (int n=0; n<numRadials; n++) {
        	
        	if (WCTUtils.getSharedCancelTask().isCancel()) {
        		throw new IOException("Operation canceled");
        	}

        	
        	
            // read number of bytes in radial
            short numBytesInRadial = dis.readShort();
            short radialStartAngle = dis.readShort();
            short radialDeltaAngle = dis.readShort();

            //            System.out.println("bytes: "+numBytesInRadial+" start: "+radialStartAngle+" : "+radialDeltaAngle+"  ");
            angle1_sin = Math.sin(Math.toRadians(radialStartAngle/10.0));
            angle1_cos = Math.cos(Math.toRadians(radialStartAngle/10.0));
            angle2_sin = Math.sin(Math.toRadians(radialStartAngle/10.0 + radialDeltaAngle/10.0));
            angle2_cos = Math.cos(Math.toRadians(radialStartAngle/10.0 + radialDeltaAngle/10.0));

            //            System.out.println(" "+n);

            for (int i=0; i<numBytesInRadial; i++) {
                int value = dis.readUnsignedByte();

                double dval;

                //                if (i<startbin || i>endbin) {
                //                    continue;
                //                }

                dval = get8bitValue(value, thresholdHalfwords, scaleOffset);
//                if (! Double.isNaN(dval) && dval != -999 && dval != 800) {
//                	maxValue = Math.max(dval, maxValue);
//                }
                
                if (dval != -999 && nxfilter.accept(dval)) {
                    //                                    System.out.print(dataHeader[0]+" ["+i+"] "+dval+" ");

                    coordinates.clear();
                    int binNum = dataHeader[0]+i;

                    if (indexedCoordArray[n][binNum] == null) {
                        xpos = i*binSpacing * angle1_sin;
                        ypos = i*binSpacing * angle1_cos;
                        geoXY = (nexradTransform.transform(new CoordinatePoint(xpos, ypos), null)).getCoordinates();
                        //                      geoXY = (nexradTransform.transform(new DirectPosition2D(xpos, ypos), null)).getCoordinates();
                        indexedCoordArray[n][binNum] = new Coordinate(geoXY[0], geoXY[1]);
                    }
                    coordinates.addElement(indexedCoordArray[n][binNum]);

                    if (indexedCoordArray[n][binNum+1] == null) {
                        xpos = (i*binSpacing+binSpacing) * angle1_sin;
                        ypos = (i*binSpacing+binSpacing) * angle1_cos;
                        geoXY = (nexradTransform.transform(new CoordinatePoint(xpos, ypos), null)).getCoordinates();
                        //                      geoXY = (nexradTransform.transform(new DirectPosition2D(xpos, ypos), null)).getCoordinates();
                        indexedCoordArray[n][binNum+1] = new Coordinate(geoXY[0], geoXY[1]);
                    }
                    coordinates.addElement(indexedCoordArray[n][binNum+1]);

                    if (indexedCoordArray[n+1][binNum+1] == null) {
                        xpos = (i*binSpacing+binSpacing) * angle2_sin;
                        ypos = (i*binSpacing+binSpacing) * angle2_cos;
                        geoXY = (nexradTransform.transform(new CoordinatePoint(xpos, ypos), null)).getCoordinates();
                        //                      geoXY = (nexradTransform.transform(new DirectPosition2D(xpos, ypos), null)).getCoordinates();
                        indexedCoordArray[n+1][binNum+1] = new Coordinate(geoXY[0], geoXY[1]);
                    }
                    coordinates.addElement(indexedCoordArray[n+1][binNum+1]);

                    if (indexedCoordArray[n+1][binNum] == null) {
                        xpos = i*binSpacing * angle2_sin;
                        ypos = i*binSpacing * angle2_cos;
                        geoXY = (nexradTransform.transform(new CoordinatePoint(xpos, ypos), null)).getCoordinates();
                        //                      geoXY = (nexradTransform.transform(new DirectPosition2D(xpos, ypos), null)).getCoordinates();
                        indexedCoordArray[n+1][binNum] = new Coordinate(geoXY[0], geoXY[1]);
                    }
                    coordinates.addElement(indexedCoordArray[n+1][binNum]);
                    coordinates.addElement(indexedCoordArray[n][binNum]);



                    // Create polygon
                    try {
                        Coordinate[] cArray = new Coordinate[coordinates.size()];
                        LinearRing lr = geoFactory.createLinearRing((Coordinate[]) (coordinates.toArray(cArray)));
                        Polygon poly = geoFactory.createPolygon(lr, null);

                        if (nxfilter == null || nxfilter.accept(poly)) {

                            if (nxfilter != null) {
                                poly = (Polygon) (nxfilter.clipToExtentFilter(poly));
                            }

                            if (poly != null) {

                                // create the feature and convert values to inch
                                Feature feature = schema.create(
                                        new Object[]{poly,
                                                new Float(dval),
                                                // find colorIndex value based on original value thresholds
                                                new Integer((int)(((double)value/256.0)*16))
                                        }, new Integer(geoIndex++).toString());

                                // send to processess
                                for (int x=0; x<processArray.length; x++) {
                                    processArray[x].addFeature(feature);
                                }

//                                                            System.out.println(feature);

                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }


                // Decode progress
                // --------------
                for (int x=0; x<listeners.size(); x++) {
                    event.setProgress( (int)( ( ((double)n) / numRadials ) * 100.0) );
                    listeners.get(x).decodeProgress(event);
                }














            }

            // read extra 'filler' byte if needed 
            if (numBytesInRadial % 2 == 1) {
                dis.readUnsignedByte();
            }


        }


        //        byte[] b = new byte[decompStream.available()];
        //        dis.read(b);
        //        System.out.println(b.length+" BBBBBBB:::::::: "+new String(b));
        //        
        //        dis.readShort(); // -1 divider
        //        System.out.println(dis.readShort());
        //        System.out.println(dis.readInt());
        //        
        ////        while (dis.readShort() != -1);
        //        
        //        System.out.println(dis.readShort());
        //        System.out.println(dis.readShort());
        //        System.out.println(dis.readInt());

        //        System.out.println(dis.readInt());
        //        System.out.println(dis.readInt());

        //        blockID = dis.readShort();
        //        blockLen = dis.readInt();
        //
        //        System.out.println("blockID="+blockID);
        //        System.out.println("blockLen="+blockLen);
        //        
        //        // advance past next divider
        //        while (dis.readShort() != -1);
        //        System.out.println(dis.readShort());
        //        System.out.println(dis.readShort());
        //
        //        
        //        
        //        int numChars = dis.readShort();
        //        byte[] text = new byte[numChars];
        //        dis.read(text);
        //        System.out.println(new String(text));

    }





    
    
    
    
    
    
    
    
    


    /**
     * Use Unidata decoders for this product
     * @param processArray
     * @throws TransformException
     * @throws IOException
     * @throws DecodeException
     * @throws OncRpcException
     */
    private void decodeGenericXDRRadial(StreamingProcess[] processArray) 
    	throws TransformException, IOException, DecodeException, OncRpcException {

        
        RadialDatasetSweep radialDataset = null;
        try {
        	
			radialDataset = (RadialDatasetSweep) TypedDatasetFactory.open(ucar.nc2.constants.FeatureType.RADIAL, 
					header.getDataURL().toString(), null, new StringBuilder());
			System.out.println(radialDataset.getDataVariables().toString());
			
			DecodeRadialDatasetSweepHeader sweepHeader = new DecodeRadialDatasetSweepHeader();
			sweepHeader.setRadialDatasetSweep(radialDataset);
			DecodeRadialDatasetSweep sweepDecoder = new DecodeRadialDatasetSweep(sweepHeader);
			for (DataDecodeListener listener : listeners) {
				sweepDecoder.addDataDecodeListener(listener);
			}
			sweepDecoder.setRadialVariable((RadialVariable) radialDataset.getDataVariable("DigitalInstantaneousPrecipitationRate"));
			sweepDecoder.setDecodeHint("nexradFilter", hintsMap.get("nexradFilter"));
			
			
//			StreamingProcess process2 = new StreamingProcess() {
//						double maxvalue = -9999;
//						@Override
//						public void addFeature(Feature feature)
//								throws StreamingProcessException {
//							
//							System.out.println(feature);
//							maxvalue = Math.max(maxvalue, Double.parseDouble(feature.getAttribute("value").toString()));
//						}
//
//						@Override
//						public void close() throws StreamingProcessException {
//							System.out.println("MAX VALUE DECODED FROM DPR: "+maxvalue);
//						}
//					
//			};
			
			
			sweepDecoder.decodeData(processArray, true);
//			sweepDecoder.decodeData(new StreamingProcess[] { processArray[0], process2 }, true);
        	

			for (DataDecodeListener listener : listeners) {
				sweepDecoder.removeDataDecodeListener(listener);
			}
			radialDataset.close();

			
			
		} catch (Exception e1) {
			e1.printStackTrace();
			try {
				if (radialDataset != null) {
					radialDataset.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

    }




    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    


    private void decodeRadial() throws TransformException, IOException {

        // rewind 
        f.seek(0);
        // ADVANCE PAST WMO HEADER
        while (f.readShort() != -1) {
            ;
        }


        // ADVANCE TO BEGINNING OF PRODUCT SYMBOLOGY BLOCK (BLOCK DIVIDER)
        while (f.readShort() != -1) {
            ;
        }

        //        f.seek(152);

        logger.fine("FILE POS: "+f.getFilePointer());
        short blockID = f.readShort();
        int blockLen = f.readInt();
        short numLayers = f.readShort();
        
        if (blockID < 0 || blockID > 20 || numLayers < 0 || numLayers > 100) {
            // we must be one divider short
            // this sometimes occurs with N*S files
            while (f.readShort() != -1) {
                ;
            }
            blockID = f.readShort();
            blockLen = f.readInt();
            numLayers = f.readShort();
        }
        
        
        // ADVANCE TO LAYER DIVIDER
        while (f.readShort() != -1) {
            ;
        }
        int layerLen = f.readInt();
//        System.out.println("BLOCK ID: "+blockID);
//        System.out.println("BLOCK LEN: "+blockLen);
//        System.out.println("NUM LAYERS: "+numLayers);

        /*
         *  / Decode Date and Time
         *  hour = (short)(uniqueInfo[4]/60.0);
         *  minute = (short)(uniqueInfo[4]%60.0);
         *  yyyymmdd = convertJulianDate(uniqueInfo[3]);
         *  logger.fine("LAT: "+lat);
         *  logger.fine("LON: "+lon);
         *  logger.fine("ALT: "+alt);
         *  logger.fine("MAX: "+uniqueInfo[0]);
         *  logger.fine("BIAS: "+uniqueInfo[1]);
         *  logger.fine("ERR.VAR.: "+uniqueInfo[2]);
         *  logger.fine(yyyymmdd+" "+hour+":"+minute);
         */
        //-------------------------------*
        // Decode the actual RLE data
        //-------------------------------*
        // dataHeader[0] = PACKET CODE FOR BASE REFLECTIVITY -- SHOULD == ?
        // dataHeader[1] = INDEX OF FIRST RANGE BIN
        // dataHeader[2] = NUMBER OF RANGE  BINS
        // dataHeader[3] = I CENTER OF SWEEP
        // dataHeader[4] = J CENTER OF SWEEP
        // dataHeader[5] = RANGE SCALE FACTOR
        // dataHeader[6] = NUMBER OF RADIALS


        short[] dataHeader = new short[7];
        for (int i = 0; i < 7; i++) {
            dataHeader[i] = f.readShort();
//                        System.out.println("dataHeader["+i+"]="+dataHeader[i]);
            logger.fine("dataHeader["+i+"]="+dataHeader[i]);
        }

        // correction for gate spacing bad values - this should always be 1000
        // - this accounts for a bug in Build 10 CFC (NCF - Clutter Filter) products
        if (header.getProductCode() == NexradHeader.L3PC_CLUTTER_FILTER_CONTROL) {
            dataHeader[5] = 1000;
        }


        String packetCodeHex = Hex.toHex(dataHeader[0]);
        logger.fine("RADIAL: dataHeader[0] HEX: "+packetCodeHex);
        if (packetCodeHex.equalsIgnoreCase("BA0F") || packetCodeHex.equalsIgnoreCase("BA07")) {

            logger.fine("SENDING TO RASTER DECODER");

            f.seek(0);
            decodeRaster();
            return;
        }
        else if (dataHeader[0] == 1) {
//        	int blen = f.readShort();
//        	int byteCount = 0;
//    		int i = f.readShort();
//    		int j = f.readShort();
//    		byteCount = 4;
//        	while (byteCount < blen) {
//        		System.out.println(new String(new byte[] { f.readByte() }));
//        		byteCount++;
//        	}

        	f.seek(0);
        	return;
        }

        for (int i = 0; i < 7; i++) {
            logger.fine("RADIAL: dataHeader[" + i + "] = " + dataHeader[i]);
        }

        double binSpacing = dataHeader[5];

        if (header.getProductCode() == 25 || header.getProductCode() == 28) {
            //			range *= 2;
            //			startingX *= 2;
            //			startingY *= 2;
            //startingY += 4000;
            binSpacing /= 3.875;
            //			dataHeader[7] *= 2;
        }

        if (header.getProductCode() == 20) {
            binSpacing *= 2;
        }

        if (header.getProductCode() == 181) {
            binSpacing = 150;
        }

        //		if (true) return (CalcNexradExtent.getNexradExtent(header));


        short numHalfwords;
        short startAngle;
        short deltaAngle;
        int data;
        int numBins;
        int colorCode;
        short runTotal;
        double[] geoXY;
        boolean first = true;
        double savedStartAngle = 0.0;
        double angle1;
        double angle2;
        double[] albX;
        double[] albY;

        //logger.fine("======== DECODE DATA 1 =========");

        // Add distance filter - find starting and ending bin numbers
        boolean colorCodeTest;
        int startbin;
        int endbin;
        int startRun;
        if (nxfilter != null) {
            startbin = (int) (((nxfilter.getMinDistance() * 1000 - binSpacing) / binSpacing) + 0.01);
            endbin = (int) (((nxfilter.getMaxDistance() * 1000 - binSpacing) / binSpacing) + 0.01);
            if (startbin < 0 || nxfilter.getMinDistance() == WCTFilter.NO_MIN_DISTANCE) {
                startbin = 0;
            }
            if (nxfilter.getMaxDistance() == WCTFilter.NO_MAX_DISTANCE) {
                endbin = 1000000;
            }
            startbin++;
        }
        else {
            startbin = 0;
            endbin = 1000000;
        }

        //		logger.fine("XXXXXXXXXXXXXXXXXXXX: "+startbin+"  YYYYYYYYYYYYY: "+endbin);

        //		Begin OUTER LOOP for RADIALS
        for (int y = 0; y < dataHeader[6]; y++) {

        	if (WCTUtils.getSharedCancelTask().isCancel()) {
        		throw new IOException("Operation canceled");
        	}
        	
        	
        	
            numHalfwords = f.readShort();
            startAngle = f.readShort();
            deltaAngle = f.readShort();

            if (first) {
                first = false;
                savedStartAngle = 90.0 - (double) startAngle / 10.0;
                if (savedStartAngle < 0) {
                    savedStartAngle += 360;
                }
            }

//            			System.out.println("numHalfwords="+numHalfwords);
//            			System.out.println("startAngle="+startAngle);
//            			System.out.println("deltaAngle="+deltaAngle);

            //			double angle1 = 90.0-((double)startAngle/10.0-(double)deltaAngle/20.0);
            //			double angle2 = 90.0-((double)startAngle/10.0+(double)deltaAngle/20.0);
            angle1 = 90.0 - (double) startAngle / 10.0;
            angle2 = 90.0 - ((double) startAngle / 10.0 + (double) deltaAngle / 10.0);

            if (angle1 < 0) {
                angle1 += 360;
            }
            if (angle2 < 0) {
                angle2 += 360;
            }

            // When we are done with 360 degrees STOP! (Files keep going sometimes)
            if (angle2 < savedStartAngle && y > 300
                    && Math.abs(angle2 - savedStartAngle) < 100) {
                angle2 = savedStartAngle;
                y = 10000000;
            }

            // Add .00000001 to any 0, 90, 180, 270, 360 values to prevent sin or cos error
            if (angle1 == 0.0 || angle1 == 90.0 || angle1 == 180.0
                    || angle1 == 270.0 || angle1 == 360.0) {
                angle1 += 0.00001;
            }
            if (angle2 == 0.0 || angle2 == 90.0 || angle2 == 180.0
                    || angle2 == 270.0 || angle2 == 360.0) {
                angle2 += 0.00001;
            }

            angle1 = Math.toRadians(angle1);
            angle2 = Math.toRadians(angle2);

            runTotal = 0;
            for (int n = 0; n < 2 * numHalfwords; n++) {

                // Read a byte
                data = f.readUnsignedByte();
                //========================================================================/
                // Extract the 4-bit Run and Code values (0-15) from the 8-bit data values
                //========================================================================/
                //*** Example *****************************************/
                //*** 1010 0011 ***************************************/
                //*** numBins = 1010 = 10                           ***/
                //*** colorCode = 0011 = 3                          ***/
                //*** data[n] >> 4 == 1010 == numBins               ***/
                //*** numBins << 4 == 1010 0000                     ***/
                //*** 1010 0000 - 1010 0011                         ***/
                //*** == 0000 0011 == colorCode == 3                ***/
                //========================================================================/
                // Isolate the first 4 bits by knocking off the last 4
                numBins = (int) data >> 4;
                // Isolate the last 4 bits by adding 4 blank bits and subtracting
                colorCode = (int) data - (numBins << 4);



            // Ignore the first bin from the wsr
            if (runTotal == 0) {
                numBins--;
                runTotal = 1;
            }

            //-------------- FILTER STUFF -----------------------
            // set the minimum distance limit
            if (runTotal < startbin && (runTotal + numBins) >= startbin) {
                startRun = startbin - runTotal;
                //runTotal = (short)startbin;
                //				logger.fine("startRun: "+startRun);
            }
            else {
                startRun = 0;
            }
            //----------------------------------------------------


            // Only create polygons if colorCode is > 0
            //if (colorCode > 0 && numBins > 0) {

            if (nxfilter == null) {
                colorCodeTest = (colorCode > 0);
            }
            else {
                colorCodeTest = false;
                int[] categoryIndices = nxfilter.getValueIndices();
                if (categoryIndices == null || categoryIndices.length == 0) {
                    colorCodeTest = (colorCode > 0);
                }
                else {
                    for (int z = 0; z < categoryIndices.length; z++) {
                        // add 1 because colorCode(0) is not in pop list
                        if (categoryIndices[z] == colorCode) {
                            colorCodeTest = true;
                        }
                    }
                }
            }

            if (colorCodeTest && numBins > 0 && (runTotal + numBins) >= startbin
                    && runTotal < endbin && numBins - startRun > 0) {

                coordinates.clear();

                // set the maximum distance limit
                if (runTotal + numBins > endbin) {
                    numBins = endbin - runTotal;
                }



                //				logger.fine("numBins= "+numBins+" ***** colorCode= "+colorCode);

                // If xrun > 1 then create polygon from to encircle each grid cell during run
                // 2 grid cells = 6 points ; 3 grid cells = 8 points ; 4 gc = 10 pnts ; etc...
                // This eliminates empty slivers caused from the projection transformation between >1 cell polygons
                // Add points in this order:    2  3  4  5  (if xrun==4)
                //                              1  8  7  6

                albX = new double[2 + (numBins - startRun) * 2];
                albY = new double[2 + (numBins - startRun) * 2];

                albX[0] = (runTotal + startRun) * binSpacing * Math.cos(angle2);
                albY[0] = (runTotal + startRun) * binSpacing * Math.sin(angle2);
                albX[1] = (runTotal + startRun) * binSpacing * Math.cos(angle1);
                albY[1] = (runTotal + startRun) * binSpacing * Math.sin(angle1);

                for (int nr = 0; nr < numBins - startRun; nr++) {
                    albX[2 + nr] = (runTotal + startRun + nr + 1) * binSpacing * Math.cos(angle1);
                    albY[2 + nr] = (runTotal + startRun + nr + 1) * binSpacing * Math.sin(angle1);
                }
                for (int nr = numBins - startRun - 1; nr >= 0; nr--) {
                    albX[2 + numBins - startRun + (numBins - startRun - 1 - nr)] = (runTotal + startRun + nr + 1) * binSpacing * Math.cos(angle2);
                    albY[2 + numBins - startRun + (numBins - startRun - 1 - nr)] = (runTotal + startRun + nr + 1) * binSpacing * Math.sin(angle2);
                }

                for (int nr = 0; nr < 2 + (numBins - startRun) * 2; nr++) {
                    // Convert to Lat/Lon and add to vector list
                    // Geotools implementation
                    //                    try {
                    geoXY = (nexradTransform.transform(new CoordinatePoint(albX[nr], albY[nr]), null)).getCoordinates();
                    Coordinate coord = new Coordinate(geoXY[0], geoXY[1]);
                    coordinates.addElement(coord);
                    //                    } catch (Exception e) {
                    //                        System.err.println("PROJ TRANSFORM ERROR: n="+nr+"  "+albX[nr]+","+albY[nr]);
                    //                        System.err.println("runTotal="+runTotal+"  : startRun="+startRun+"  : numBins="+numBins+"  : endbin="+endbin);
                    //                        System.err.println("numRadials="+dataHeader[6]+"  : numHalfwords="+numHalfwords+"  : binSpacing="+binSpacing);
                    //                        System.err.println("startAngle="+startAngle+"  : deltaAngle="+deltaAngle);
                    //                        
                    //                        return;
                    //                    }
                    //                    System.out.println("runTotal="+runTotal+"  : startRun="+startRun+"  : numBins="+numBins+"  : endbin="+endbin);
                    //                    System.out.println("numRadials="+dataHeader[6]+"  : numHalfwords="+numHalfwords+"  : binSpacing="+binSpacing);
                }
                // Add the first point again to close polygon
                geoXY = (nexradTransform.transform(new CoordinatePoint(albX[0], albY[0]), null)).getCoordinates();

                Coordinate coord = new Coordinate(geoXY[0], geoXY[1]);
                coordinates.addElement(coord);

                // Create polygon
                try {
                    Coordinate[] cArray = new Coordinate[coordinates.size()];
                    LinearRing lr = geoFactory.createLinearRing((Coordinate[]) (coordinates.toArray(cArray)));
                    Polygon poly = JTSUtilities.makeGoodShapePolygon(geoFactory.createPolygon(lr, null));

                    if (nxfilter == null || nxfilter.accept(poly)) {

                        if (nxfilter != null) {
                            poly = (Polygon) (nxfilter.clipToExtentFilter(poly));
                        }

                        if (poly != null) {
                            polyVector[colorCode].addElement(poly);
                        }

                    }
                    coordinates.clear();
                    cArray = null;
                } catch (Exception e) {
                    //e.printStackTrace();
                    logger.severe(e.toString());
                    //y=100000000;
                }

            }
            // END if (colorCode > 0)
            runTotal += numBins;
            }
            // END Bin Loop
            //y=100000000;

            if (y > dataHeader[6]) {
                y = dataHeader[6];
            }
            // Decode progress
            // --------------
            for (int n = 0; n < listeners.size(); n++) {
                event.setProgress( (int)( ( ((double)y) / dataHeader[6] ) * 100.0) );
                listeners.get(n).decodeProgress(event);
            }


        }
        //		END Radial loop
    } 

    private void decodeRaster() throws TransformException, IOException {

        // rewind 
        f.seek(0);
        // ADVANCE PAST WMO HEADER
        while (f.readShort() != -1) {
            ;
        }

        // ADVANCE TO BEGINNING OF PRODUCT SYMBOLOGY BLOCK (BLOCK DIVIDER)
        while (f.readShort() != -1) {
            ;
        }

        logger.info("FILE POS: "+f.getFilePointer());


        short blockID = f.readShort();
        int blockLen = f.readInt();
        short numLayers = f.readShort();
        // ADVANCE TO LAYER DIVIDER
        while (f.readShort() != -1) {
            ;
        }
        int layerLen = f.readInt();

        /*
         *  / Decode Date and Time
         *  hour = (short)(uniqueInfo[4]/60.0);
         *  minute = (short)(uniqueInfo[4]%60.0);
         *  yyyymmdd = convertJulianDate(uniqueInfo[3]);
         *  logger.fine("LAT: "+lat);
         *  logger.fine("LON: "+lon);
         *  logger.fine("ALT: "+alt);
         *  logger.fine("MAX: "+uniqueInfo[0]);
         *  logger.fine("BIAS: "+uniqueInfo[1]);
         *  logger.fine("ERR.VAR.: "+uniqueInfo[2]);
         *  logger.fine(yyyymmdd+" "+hour+":"+minute);
         */
        //-------------------------------*
        // Decode the actual RLE data
        //-------------------------------*
        // dataHeader[0] = PACKET CODE  -- SHOULD == ?
        // dataHeader[1] = PACKET CODE FOR OP FLAG 1
        // dataHeader[2] = PACKET CODE FOR OP FLAG 2
        // dataHeader[3] = I START COORDINATE
        // dataHeader[4] = J START COORDINATE
        // dataHeader[5] = X SCALE (INT)
        // dataHeader[6] = X SCALE (FRACTION)
        // dataHeader[7] = Y SCALE (INT)
        // dataHeader[8] = Y SCALE (FRACTION)
        // dataHeader[9] = NUMBER OF ROWS
        // dataHeader[10] = PACKING DESCRIPTOR (Always == 2)
        short[] dataHeader = new short[11];
        for (int i = 0; i < 11; i++) {
            dataHeader[i] = f.readShort();
        }

        String packetCodeHex = Hex.toHex(dataHeader[0]);
        logger.fine("RASTER: dataHeader[0] HEX: "+packetCodeHex);
        logger.fine("RASTER: dataHeader[1] HEX: "+Hex.toHex(dataHeader[1]));
        logger.fine("RASTER: dataHeader[2] HEX: "+Hex.toHex(dataHeader[2]));

        for (int i = 3; i < 11; i++) {
            logger.fine("RASTER dataHeader["+i+"] = " + dataHeader[i]);
        }

        int range = 230000;
        //		int startingX = dataHeader[3]*4*1000; // put in meters
        //		int startingY = dataHeader[4]*4*1000;


        //int startingX = dataHeader[3] * 1000 * 2;
        //int startingY = dataHeader[4] * 1000 * 2;
        //int startingX = dataHeader[3] * 1000 + (dataHeader[3]*1000);
        //int startingY = dataHeader[4] * 1000 + (dataHeader[4]*1000);
        double startingX;
        //		int startingX = dataHeader[3]*4*1000; // put in meters
        //		int startingY = dataHeader[4]*4*1000;


        //int startingX = dataHeader[3] * 1000 * 2;
        //int startingY = dataHeader[4] * 1000 * 2;
        //int startingX = dataHeader[3] * 1000 + (dataHeader[3]*1000);
        //int startingY = dataHeader[4] * 1000 + (dataHeader[4]*1000);
        double startingY;
        if (dataHeader[5] == 4) {
            startingX = ((double) dataHeader[3] + 0.5) * 1000.0 * (double) dataHeader[5];
        }
        else {
            startingX = ((double) dataHeader[3] + 1.0) * 1000.0 * (double) dataHeader[5];
        }
        if (dataHeader[7] == 4) {
            startingY = ((double) dataHeader[4] + 0.5) * 1000.0 * (double) dataHeader[7];
        }
        else {
            startingY = ((double) dataHeader[4] + 1.0) * 1000.0 * (double) dataHeader[7];
        }

        if (header.getProductCode() == 38) {
            range *= 2;
            //startingX *= 2;
            //startingY *= 2;
            //startingY += 4000;
            dataHeader[5] *= 2;
            dataHeader[7] *= 2;
        }
        //logger.fine("startingX = " + startingX);
        //logger.fine("startingY = " + startingY);

        //startingX += 1000; // I don't know why this is needed - something fishy with Level-3 products.  This will visually match GEMPAK.
        //startingY += 1000;
        /*
            if (startingX == 0) {
               startingX += dataHeader[5]*1000/dataHeader[5];
            }
            else {
               startingX += dataHeader[5]*1000/2.0;
            }
            if (startingY == 0) {
               startingY += dataHeader[7]*1000/1.0;
            }
            else {
               startingY += dataHeader[7]*1000/2.0;
            }
         */

        short numBytesInRow;
        int data;
        int numBins;
        int colorCode;
        short runTotal;
        double[] geoXY;
        double[] albX;
        double[] albY;
        // Begin OUTER LOOP for ROWS
        for (int y = 0; y < dataHeader[9]; y++) {

        	if (WCTUtils.getSharedCancelTask().isCancel()) {
        		throw new IOException("Operation canceled");
        	}

        	
            numBytesInRow = f.readShort();

            //			logger.fine("numBytesInRow = "+numBytesInRow);

            runTotal = 0;
            for (int n = 0; n < numBytesInRow; n++) {

                // Read a byte
                data = f.readUnsignedByte();
                //========================================================================/
                // Extract the 4-bit Run and Code values (0-15) from the 8-bit data values
                //========================================================================/
                //*** Example *****************************************
                //*** 1010 0011 ***************************************
                //*** numBins = 1010 = 10                           ***
                //*** colorCode = 0011 = 3                          ***
                //*** data[n] >> 4 == 1010 == numBins               ***
                //*** numBins << 4 == 1010 0000                     ***
                //*** 1010 0000 - 1010 0011                         ***
                //*** == 0000 0011 == colorCode == 3                ***
                //========================================================================/
                // Isolate the first 4 bits by knocking off the last 4
                numBins = (int) data >> 4;
            // Isolate the last 4 bits by adding 4 blank bits and subtracting
            colorCode = (int) data - (numBins << 4);

            boolean colorCodeTest;
            // Check category filter from NexradFilter
            if (nxfilter == null) {
                colorCodeTest = (colorCode > 0);
            }
            else {
                colorCodeTest = false;
                int[] categoryIndices = nxfilter.getValueIndices();
                if (categoryIndices == null || categoryIndices.length == 0) {
                    colorCodeTest = (colorCode > 0);
                }
                else {
                    for (int z = 0; z < categoryIndices.length; z++) {
                        // add 1 because colorCode(0) is not in pop list
                        if (categoryIndices[z] == colorCode) {
                            colorCodeTest = true;
                        }
                    }
                }
            }

            // Only create polygons if colorCode is > 0
            if (colorCodeTest) {
                //if (colorCode > 0) {

                //Vector coordinates = new Vector();
                coordinates.clear();

                //				logger.fine("numBins= "+numBins+" ***** colorCode= "+colorCode);

                // If xrun > 1 then create polygon from to encircle each grid cell during run
                // 2 grid cells = 6 points ; 3 grid cells = 8 points ; 4 gc = 10 pnts ; etc...
                // This eliminates empty slivers caused from the projection transformation between >1 cell polygons
                // Add points in this order:    2  3  4  5  (if xrun==4)
                //                              1  8  7  6

                albX = new double[2 + numBins * 2];
                albY = new double[2 + numBins * 2];

                albX[0] = runTotal * 1000 * dataHeader[5] - range - startingX;
                albY[0] = range + startingY - (y + 1) * 1000 * dataHeader[7];
                albX[1] = runTotal * 1000 * dataHeader[5] - range - startingX;
                albY[1] = range + startingY - y * 1000 * dataHeader[7];

                for (int nr = 0; nr < numBins; nr++) {
                    albX[2 + nr] = (runTotal + nr + 1) * 1000 * dataHeader[5] - range - startingX;
                    albY[2 + nr] = range + startingY - y * 1000 * dataHeader[7];
                }
                for (int nr = numBins - 1; nr >= 0; nr--) {
                    albX[2 + numBins + (numBins - 1 - nr)] = (runTotal + nr + 1) * 1000 * dataHeader[5] - range - startingX;
                    albY[2 + numBins + (numBins - 1 - nr)] = range + startingY - (y + 1) * 1000 * dataHeader[7];
                }

                for (int nr = 0; nr < 2 + numBins * 2; nr++) {
                    // Avoid NaN errors
                    if (Math.abs(albY[nr]) < .05) {
                        albY[nr] = .05;
                    }
                    if (Math.abs(albX[nr]) < .05) {
                        albX[nr] = .05;
                    }
                    // Convert to Lat/Lon and add to vector list
                    //geoXY = customAlbers.convert(albX[nr], albY[nr]);
                    geoXY = (nexradTransform.transform(new CoordinatePoint(albX[nr], albY[nr]), null)).getCoordinates();

                    Coordinate coord = new Coordinate(geoXY[0], geoXY[1]);
                    coordinates.addElement(coord);
                }
                // Add the first point again to close polygon
                //geoXY = customAlbers.convert(albX[0], albY[0]);
                geoXY = (nexradTransform.transform(new CoordinatePoint(albX[0], albY[0]), null)).getCoordinates();

                Coordinate coord = new Coordinate(geoXY[0], geoXY[1]);
                coordinates.addElement(coord);

                try {

                    Coordinate[] cArray = new Coordinate[coordinates.size()];
                    LinearRing lr = geoFactory.createLinearRing((Coordinate[]) (coordinates.toArray(cArray)));
                    Polygon poly = geoFactory.createPolygon(lr, null);

                    if (nxfilter == null || nxfilter.accept(poly)) {

                        if (nxfilter != null) {
                            poly = (Polygon) (nxfilter.clipToExtentFilter(poly));
                        }

                        if (poly != null) {
                            polyVector[colorCode].addElement(poly);
                        }

                    }


                    // create the feature
                    //Feature feature = schema.create(new Object[] {poly, new Short((short)colorCode)}, new Integer(geoIndex++).toString());
                    // add to collection
                    //features.add(feature);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //y+=1000000; // END LOOP AFTER DRAWING ONE ROW

            }
            // END if (colorCode > 0)
            runTotal += numBins;
            }
            // END Bin Loop
            //			y=100000000;



            // Decode progress
            // --------------
            for (int n = 0; n < listeners.size(); n++) {
                event.setProgress( (int)( ( ((double)y) / dataHeader[9] ) * 100.0) );
                listeners.get(n).decodeProgress(event);
            }



        }
        // END Raster loop

    }

    private void decodeDPA(StreamingProcess[] processArray) throws TransformException, IOException, DecodeException {


        // rewind 
        f.seek(0);
        // ADVANCE PAST WMO HEADER
        while (f.readShort() != -1) {
            ;
        }


        double maxVal = 0.0;

        ((DecodeL3Header) header).setDataThresholdStringArray(
                new String[]{"0", "< 0.10", "0.25", "0.50", "0.75", "1.00",
                        "1.50", "2.00", "2.50", "3.00", "4.00", "> 4.00", "", "", "", ""});

        // reinitialize dpaRaster object
        dpaRaster.init((DecodeL3Header) header);

        // Set up units conversion
        //double unitsFactor = 1.0; // Default for MM
        //if(units == INCH)
        //   unitsFactor = (1.0/25.4);
        double unitsFactor = (1.0 / 25.4);

        // New HRAP Converter
        HRAPProjection hrapProj = new HRAPProjection();
        double[] dwork;
        double[] hrapXY = hrapProj.forward(header.getLat(), header.getLon());
        double wsrHrapX = hrapXY[0];
        double wsrHrapY = hrapXY[1];

        /*
            //logger.fine("--------------------------------------------------");
            //logger.fine("NAD83 LAT: "+header.getLat());
            //logger.fine("NAD83 LON: "+header.getLon());
            //logger.fine("--------------------------------------------------");

            // Convert NAD83 WSR Lat/Lon to Spherical Lat/Lon
            MathTransform nad83ToSphericalTransform = nexradProjection.getNAD83ToSphericalTransform(6371007.0);
            double[] dwork = (nad83ToSphericalTransform.transform(new CoordinatePoint(header.getLon(), header.getLat()), null)).getCoordinates();

            // Convert from Spherical to WGS84 Datum (for DPA Grid Cells)
            //MathTransform sphericalToWGS84Transform = nexradProjection.getSphericalToWGS84Transform(6371007.0);


            double lat = dwork[1];
            double lon = dwork[0];
            //logger.fine("--------------------------------------------------");
            //logger.fine("NEW SPHERICAL LAT: "+lat);
            //logger.fine("NEW SPHERICAL LON: "+lon);
            //logger.fine("--------------------------------------------------");


            // Find Stereographic Coordinates of WSR Site
            //LatLon2Stereo cvt = new LatLon2Stereo(header.getLat(),header.getLon(),60.0,-105.0,1.0);



//--------- //LatLon2Stereo cvt = new LatLon2Stereo(lat, lon,60.0,-105.0,1.0);
            //double wsrX = cvt.getX(0);
            //double wsrY = cvt.getY(0);

            //logger.fine("--------------------------------------------------");
            //logger.fine("OLD STEREO X: "+wsrX);
            //logger.fine("OLD STEREO Y: "+wsrY);
            //logger.fine("--------------------------------------------------");

            // Adjust x,y coordinate to fixed HRAP grid.  Since we have found the
            //   stereographic coordinates of radar site now we must find the center
            //   sterographic coordinate of HRAP cell that this radar site falls in.
            // Find HRAP coordinate
            double hrapX = wsrX/4762.5 + 400.5;
            double hrapY = wsrY/4762.5 + 1600.5;
            // Find the integer grid cell coordinates then center.  This allows us to find our location on the fixed HRAP ConUS grid.
            hrapX = (int)hrapX; // strip of decimal
            hrapY = (int)hrapY;
            hrapX = (hrapX < 0) ? hrapX-0.5 : hrapX+0.5; // add .5 to put in center of grid cell
            hrapY = (hrapY < 0) ? hrapY-0.5 : hrapY+0.5;
            // Convert this HRAP center coordinate to stereographic
//hrapX-=65;
//hrapY-=65;
            wsrX = 4762.5*(hrapX - 400.5);
            wsrY = 4762.5*(hrapY - 1600.5);

         */
        //logger.fine("--------------------------------------------------");
        //logger.fine("FIXED HRAP X: "+hrapX+"  LL STEREO X: "+(wsrX-4762.5*65.0));
        //logger.fine("FIXED HRAP Y: "+hrapY+"  LL STEREO Y: "+(wsrY-4762.5*65.0));
        //logger.fine("--------------------------------------------------");


        /*
            MathTransform nad83ToStereoTransform, stereoToWGS84Transform;
            try {
               // Use Geotools Proj4 implementation to get MathTransform object
               nad83ToStereoTransform = nexradProjection.getNAD83ToStereoTransform();
               stereoToWGS84Transform = nexradProjection.getStereoToWGS84Transform();
            } catch (Exception e) {
               javax.swing.JOptionPane.showMessageDialog(null, "Nexrad Transform Init Error",
                  "TRANSFORM ERROR", javax.swing.JOptionPane.ERROR_MESSAGE);
               e.printStackTrace();
               return;
            }
            double[] stereoXY = (nad83ToStereoTransform.transform(new CoordinatePoint(header.getLon(), header.getLat()), null)).getCoordinates();
            wsrX = stereoXY[0];
            wsrY = stereoXY[1];

            logger.fine("--------------------------------------------------");
            logger.fine("NEW STEREO X: "+wsrX);
            logger.fine("NEW STEREO Y: "+wsrY);
            logger.fine("--------------------------------------------------");
         */




        // ADVANCE TO BEGINNING OF PRODUCT SYMBOLOGY BLOCK (BLOCK DIVIDER)
        while (f.readShort() != -1) {
            ;
        }
        short blockID = f.readShort();
        int blockLen = f.readInt();
        short numLayers = f.readShort();
        // ADVANCE TO LAYER DIVIDER
        while (f.readShort() != -1) {
            ;
        }
        int layerLen = f.readInt();

        // dataHeader[0] = PACKET CODE FOR DPA -- SHOULD == 17 (NOT PRODUCT CODE!)
        // dataHeader[1 & 2] = SPARE
        // dataHeader[3] = NUMBER OF LFM BOXES IN ROW
        // dataHeader[4] = NUMBER OF ROWS
        short[] dataHeader = new short[5];
        for (int i = 0; i < 5; i++) {
            dataHeader[i] = f.readShort();
        }

        //-------------------------------*
        // Decode the actual RLE data
        //-------------------------------*
        int ncellx;
        for (int y = 0; y < dataHeader[4]; y++) {

            ncellx = 0;

            // Read number of bytes in DPA data packet
            int nbytes = f.readShort();
            if (nbytes < 2 || nbytes > 32000) {
                throw new DecodeException("DATA CORRUPTION ENCOUNTERED", header.getDataURL());
            }

            // Read in the 1-byte (0-255) data
            short[] rawData = new short[nbytes];
            for (int i = 0; i < nbytes; i++) {
                rawData[i] = (short) f.readUnsignedByte();
            }

            // Decode the Run Length Encoded Data
            short nruns = (short) (nbytes / 2.0);
            if (nruns > 131 || nruns < 1) {
                throw new DecodeException("VALUE OF nruns > 131 (=" + nruns + ") -- UNABLE TO DECODE", header.getDataURL());
            }
            for (int j = 0; j < nruns; j++) {
                short xrun = (short) rawData[j * 2];
                if (xrun > 131 || xrun < 1) {
                    throw new DecodeException("VALUE OF xrun > 131 (=" + xrun + ") -- UNABLE TO DECODE", header.getDataURL());
                }
                short level = (short) rawData[1 + j * 2];

                if (level == 0) {
                    for (int nr = 0; nr < xrun; nr++) {
                        dpaRaster.setCellValue(ncellx + nr, y, 0);
                    }
                }



                // Extract actual value from 1-byte encoded value
                float value = (float) (unitsFactor * Math.pow(10, (((level * .125) + (-6.125)) / 10.0)));

                // Keep track of max value decoded
                if (value > maxVal) {
                    if (level < 255 && level > 0) {
                        maxVal = value;
                    }
                }



                boolean colorCodeTest;
                int colorCode = NexradDPAThresholdFactory.getColorIndex(value);
                // Check category filter from NexradFilter
                if (nxfilter == null) {
                    colorCodeTest = (colorCode > 0);
                }
                else {
                    colorCodeTest = false;
                    int[] categoryIndices = nxfilter.getValueIndices();
                    if (categoryIndices == null || categoryIndices.length == 0) {
                        colorCodeTest = (colorCode > 0);
                    }
                    else {
                        for (int z = 0; z < categoryIndices.length; z++) {
                            // add 1 because colorCode(0) is not in pop list
                            if (categoryIndices[z] == colorCode) {
                                colorCodeTest = true;
                            }
                        }
                    }
                }


                //				for (int n=0; n<xrun; n++) {
                if (level < 255 && level > 0 && colorCodeTest) {

                    // Extract actual value from 1-byte encoded value
                    //					float value = (float) (unitsFactor * Math.pow(10,(((level*.125)+(-6.125))/10.0)));



                    //Vector coordinates = new Vector();
                    coordinates.clear();

                    /* Try two methods:  just a straight equal-area geometry approximation -OR-
                     convert back to lat/lon then to albers (from each corner of polygon)
                     */
                    // Add value to DPARaster object
                    for (int nr = 0; nr < xrun; nr++) {
                        dpaRaster.setCellValue(ncellx + nr, y, value);
                    }


                    // If xrun > 1 then create polygon from to encircle each grid cell during run
                    // 2 grid cells = 6 points ; 3 grid cells = 8 points ; 4 gc = 10 pnts ; etc...
                    // This eliminates empty slivers caused from the projection transformation between >1 cell polygons
                    // Add points in this order:    2  3  4  5  (if xrun==4)
                    //                              1  8  7  6
                    //					+1(fix for zero based), -0.5(go to edge of gridcell), -66(cell offset from center WSR), xrun-1(0.5 does right side)

                    /*
                      double[] stereoX = new double[2+xrun*2];
                      double[] stereoY = new double[2+xrun*2];
                      stereoX[0] = wsrX + 4762.5*(ncellx+1-0.5-66);
                      stereoY[0] = wsrY - 4762.5*(y+1-0.5-66);
                      stereoX[1] = wsrX + 4762.5*(ncellx+1-0.5-66);
                      stereoY[1] = wsrY - 4762.5*(y+1+0.5-66);
                      for (int nr=0; nr<xrun; nr++) {
                         stereoX[2+nr] = wsrX + 4762.5*(ncellx+nr+1+0.5-66);
                         stereoY[2+nr] = wsrY - 4762.5*(y+1+0.5-66);
                      }
                      for (int nr=xrun-1; nr>=0; nr--) {
                         stereoX[2+xrun+(xrun-1-nr)] = wsrX + 4762.5*(ncellx+nr+1+0.5-66);
                         stereoY[2+xrun+(xrun-1-nr)] = wsrY - 4762.5*(y+1-0.5-66);
                      }
                      // Convert from Polar Stereographic Spherical to Unprojected (Lon, Lat) Spherical
                      Stereo2LatLon cvt2 = new Stereo2LatLon(stereoX, stereoY, 60.0, -105.0);

                      for(int n=0;n<2+xrun*2;n++) {
                         coordinates.addElement((Object)new Coordinate(cvt2.getLon(n), cvt2.getLat(n)));
//---------              //dwork = (sphericalToWGS84Transform.transform(new CoordinatePoint(cvt2.getLon(n), cvt2.getLat(n)), null)).getCoordinates();
//---------              //coordinates.addElement((Object)new Coordinate(dwork[0], dwork[1]));
                      }
                      // Add the first point again to close polygon
                      coordinates.addElement((Object)new Coordinate(cvt2.getLon(0), cvt2.getLat(0)));
//---------           //dwork = (sphericalToWGS84Transform.transform(new CoordinatePoint(cvt2.getLon(0), cvt2.getLat(0)), null)).getCoordinates();
//---------           //coordinates.addElement((Object)new Coordinate(dwork[0], dwork[1]));
                     */
                    double[] hrapX = new double[2 + xrun * 2];
                    double[] hrapY = new double[2 + xrun * 2];
                    hrapX[0] = wsrHrapX + (ncellx - 65);
                    hrapY[0] = wsrHrapY - (y - 66);
                    // -66 because DPA 131x131 grid starts in upper left while HRAP starts in lower left
                    hrapX[1] = wsrHrapX + (ncellx - 65);
                    hrapY[1] = wsrHrapY - (y + 1 - 66);
                    for (int nr = 0; nr < xrun; nr++) {
                        hrapX[2 + nr] = wsrHrapX + (ncellx + nr + 1 - 65);
                        hrapY[2 + nr] = wsrHrapY - (y + 1 - 66);
                    }
                    for (int nr = xrun - 1; nr >= 0; nr--) {
                        hrapX[2 + xrun + (xrun - 1 - nr)] = wsrHrapX + (ncellx + nr + 1 - 65);
                        hrapY[2 + xrun + (xrun - 1 - nr)] = wsrHrapY - (y - 66);
                    }

                    // Convert from Polar Stereographic Spherical to Unprojected (Lon, Lat) Spherical
                    for (int n = 0; n < 2 + xrun * 2; n++) {
                        dwork = hrapProj.reverse((int) hrapX[n], (int) hrapY[n]);
                        coordinates.addElement(new Coordinate(dwork[0], dwork[1]));
                    }
                    // Add the first point again to close polygon
                    dwork = hrapProj.reverse((int) hrapX[0], (int) hrapY[0]);
                    coordinates.addElement(new Coordinate(dwork[0], dwork[1]));




                    // Create polygon
                    try {
                        Coordinate[] cArray = new Coordinate[coordinates.size()];
                        LinearRing lr = geoFactory.createLinearRing((Coordinate[]) (coordinates.toArray(cArray)));
                        Polygon poly = geoFactory.createPolygon(lr, null);

                        if (nxfilter == null || nxfilter.accept(poly)) {

                            if (nxfilter != null) {
                                poly = (Polygon) (nxfilter.clipToExtentFilter(poly));
                            }

                            if (poly != null) {

                                // create the feature and convert values to inch
                                Feature feature = schema.create(
                                        new Object[]{poly,
                                                new Float(value),
                                                // find colorIndex value based on DPA value thresholds
                                                new Integer(NexradDPAThresholdFactory.getColorIndex(value))
                                        }, new Integer(geoIndex++).toString());

                                // send to processess
                                for (int n=0; n<processArray.length; n++) {
                                    processArray[n].addFeature(feature);
                                }

                            }


                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                for (int n = 0; n < xrun; n++) {
                    ncellx++;
                }
                //				} // END for (xrun)
            }
            // END for (nrun)



            // Decode progress
            // --------------
            for (int n = 0; n < listeners.size(); n++) {
                event.setProgress( (int)( ( ((double)y) / dataHeader[4] ) * 100.0) );
                listeners.get(n).decodeProgress(event);
            }




        }
        // END for (dataHeader) (NUMBER OF ROWS)

        // IF DECODING DPA DATA WE DON'T NEED TO SPLIT UP FEATURES OR REDUCE POLYGONS



        logger.info(" MAX VALUE DECODED: "+maxVal);
        logger.info(" DPA --- OVERRIDING HEADER MAX VAL: "+maxVal);
        ((DecodeL3Header)header).setDPAMaxValue((short)Math.round(maxVal*100));




        // End decode
        // --------------
        for (int i = 0; i < listeners.size(); i++) {
            event.setProgress(100);
            listeners.get(i).decodeEnded(event);
        }

    }
    // END else if == DPA loop







    /**
     * Adds a DataDecodeListener to the list.
     *
     * @param  listener  The feature to be added to the DataDecodeListener attribute
     */
    public void addDataDecodeListener(DataDecodeListener listener) {

        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }

    }


    /**
     * Removes a DataDecodeListener from the list.
     *
     * @param  listener   DataDecodeListener to remove.
     */
    public void removeDataDecodeListener(DataDecodeListener listener) {

        listeners.remove(listener);

    }
























    /**
     *  Gets the features for the last decoded data (point or polygon)
     *
     * @return    The FeatureCollection object
     */
    public FeatureCollection getFeatures() {
        return features;
    }


    /**
     *  Sets the collection for which the features of the next decoded data (point or polygon) will be filled
     *
     * @param  features  The new features value
     */
    public void setFeatures(FeatureCollection features) {

        this.features = features;
    }



    /**
     *  Gets the dPARaster attribute of the DecodeL3Nexrad object
     *
     * @return    The dPARaster value
     */
    public DPARaster getDPARaster() {
        return dpaRaster;
    }




    /**
     *  Gets the floatDataThreshold attribute of the DecodeL3Nexrad object
     *
     * @param  i  Description of the Parameter
     * @return    The floatDataThreshold value
     */
    private Float getFloatDataThreshold(int i) {

        Float value = null;
        try {
            String dataThreshold = ((DecodeL3Header) header).getDataThresholdString(i);
            if (dataThreshold == null) {
                return new Float(-9999);            
            }
            dataThreshold.trim();
            if (dataThreshold.length() > 0) {
                if (dataThreshold.equals("RF")) {
                    value = new Float(-100);
                }
                else if (dataThreshold.equals("TH")) {
                    value = new Float(-200);
                }
                else if (dataThreshold.equals("ND")) {
                    value = new Float(-999);
                }
                else if (dataThreshold.charAt(0) == '-') {
                    value = new Float("-" + dataThreshold.substring(2, dataThreshold.length()));
                }
                else if (dataThreshold.charAt(0) == '+') {
                    value = new Float("+" + dataThreshold.substring(2, dataThreshold.length()));
                }
                else {
                    value = new Float(dataThreshold);
                }
            }
        } catch (Exception e) {
            // catch exception if value is not a number or "RF" or "ND"
            value = new Float(-9999);

            //e.printStackTrace();
        }
        return value;
    }



    public String[] getSupplementalDataArray() throws IOException {

        logger.info("LOADING SUPPLEMENTAL DATA... URL="+header.getDataURL().toString());

        NetcdfFile ncfile = NetcdfFile.open(header.getDataURL().toString());
        try {

            Variable var = ncfile.findVariable("TabMessagePage");
            if (var == null) {
                return null;
            }
            Array data = var.read();
            Index index = data.getIndex();
            int[] shape = data.getShape();
            //		logger.fine("Data Array Dimensions: ");
            for (int n=0; n<shape.length; n++) {      
                logger.fine("TabMessagePage Dimension["+n+"] " + shape[n]);
            }

            supplementalData = new String[shape[0]];

            for (int n=0; n<shape[0]; n++) {
                //			logger.fine("-------------- n="+n);
                String pageString = data.getObject(index.set(n)).toString();
                //			logger.fine(pageString);

                supplementalData[n] = pageString;
            }

        } finally {
            //            System.out.println("CLOSING NETCDF SUP DATA READ");
            ncfile.close();
        }

        return supplementalData;
    }





    

    private static int unsignedByteToInt(byte b) {
      return (int) b & 0xFF;
    }
    
    private static float getHexDecodeValue(short val) {
        float deco;

        int s = (val >> 15) & 1;
        int e = (val >> 10) & (31);
        int f = (val) & (1023);

        if( e== 0) {
             deco =(float) Math.pow(-1, s) * 2 * (0.f +(float) (f/1024.f)) ;
        } else {
             deco = (float) (Math.pow(-1, s) *Math.pow(2, e-16)*(1 + (f/1024.f)));
        }

        return deco;
    }


    
}

