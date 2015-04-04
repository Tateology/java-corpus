package gov.noaa.ncdc.wct.decoders.cdm;

import gov.noaa.ncdc.nexrad.NexradEquations;
import gov.noaa.ncdc.wct.WCTFilter;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.DecodeHintNotSupportedException;
import gov.noaa.ncdc.wct.decoders.StreamingProcess;
import gov.noaa.ncdc.wct.decoders.StreamingProcessException;
import gov.noaa.ncdc.wct.decoders.nexrad.StreamingRadialDecoder;
import gov.noaa.ncdc.wct.decoders.nexrad.WCTProjections;
import gov.noaa.ncdc.wct.event.DataDecodeEvent;
import gov.noaa.ncdc.wct.event.DataDecodeListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Vector;
import java.util.logging.Logger;

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

import ucar.nc2.Attribute;
import ucar.nc2.dt.RadialDatasetSweep;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class DecodeRadialDatasetSweep implements StreamingRadialDecoder {

    private static final Logger logger = Logger.getLogger(DecodeRadialDatasetSweep.class.getName());



    private WCTProjections nexradProjection = new WCTProjections();
    private GeometryFactory geoFactory = new GeometryFactory();
    private FeatureType schema, display_schema, poly_schema, point_schema, all_point_schema;

    private final FeatureCollection fc = FeatureCollections.newCollection();

    private Map<String, Object> hintsMap;





    private double lastDecodedElevationAngle = -999.0;


    /**
     *  Description of the Field
     */
    public final static AttributeType[] DISPLAY_POLY_ATTRIBUTES = {
        AttributeTypeFactory.newAttributeType("geom", Geometry.class),
        AttributeTypeFactory.newAttributeType("value", Float.class, true, 5),
        AttributeTypeFactory.newAttributeType("colorIndex", Integer.class, true, 4)
    };
    /**
     *  Description of the Field
     */
    public final static AttributeType[] EXPORT_POLY_ATTRIBUTES = {
        AttributeTypeFactory.newAttributeType("geom", Geometry.class),
        AttributeTypeFactory.newAttributeType("sweep", Integer.class, true, 3),
        AttributeTypeFactory.newAttributeType("sweepTime", String.class, true, 17),
        AttributeTypeFactory.newAttributeType("elevAngle", Float.class, true, 6),
        AttributeTypeFactory.newAttributeType("value", Float.class, true, 5),
        AttributeTypeFactory.newAttributeType("radialAng", Float.class, true, 8),
        AttributeTypeFactory.newAttributeType("begGateRan", Float.class, true, 9),
        AttributeTypeFactory.newAttributeType("endGateRan", Float.class, true, 9),
        AttributeTypeFactory.newAttributeType("heightRel", Float.class, true, 9),
        AttributeTypeFactory.newAttributeType("heightASL", Float.class, true, 9)
    };
    /**
     *  Description of the Field
     */
    public final static AttributeType[] EXPORT_POINT_ATTRIBUTES = {
        AttributeTypeFactory.newAttributeType("geom", Geometry.class),
        AttributeTypeFactory.newAttributeType("sweep", Integer.class, true, 3),
        AttributeTypeFactory.newAttributeType("sweepTime", String.class, true, 17),
        AttributeTypeFactory.newAttributeType("elevAngle", Float.class, true, 6),
        AttributeTypeFactory.newAttributeType("value", Float.class, true, 5),
        AttributeTypeFactory.newAttributeType("radialAng", Float.class, true, 8),
        AttributeTypeFactory.newAttributeType("surfaceRan", Float.class, true, 8),
        AttributeTypeFactory.newAttributeType("heightRel", Float.class, true, 9),
        AttributeTypeFactory.newAttributeType("heightASL", Float.class, true, 9)
    };



    // The list of event listeners.
    private Vector<DataDecodeListener> listeners = new Vector<DataDecodeListener>();

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");









    private RadialDatasetSweep.RadialVariable radialVar;
    private DecodeRadialDatasetSweepHeader header;

    private Date[] sweepDateTime;

    private double[] classificationValues = new double[] { -200.0 };
    private double lastDecodedMinValue = 99999;
    private double lastDecodedMaxValue = -99999;


    public DecodeRadialDatasetSweep(DecodeRadialDatasetSweepHeader header) {
        this.header = header;
        init();
    }



    /**
     * Sets the radial variable volume that will be decoded.  
     * @param radialVar
     */
    public void setRadialVariable(RadialDatasetSweep.RadialVariable radialVar) {
        this.radialVar = radialVar;

    }




    public FeatureType getFeatureType() {
        return schema;
    }













    /**
     *  Initialize the FeatureType schemas and the hints map
     */
    private void init() {
        
        try {
            display_schema = FeatureTypeFactory.newFeatureType(DISPLAY_POLY_ATTRIBUTES, "Display Radial Attributes");
            poly_schema = FeatureTypeFactory.newFeatureType(EXPORT_POLY_ATTRIBUTES, "Radial Polygon Attributes");
            point_schema = FeatureTypeFactory.newFeatureType(EXPORT_POINT_ATTRIBUTES, "Radial Point Attributes");
        } catch (Exception e) {
            e.printStackTrace();
        }

        hintsMap = new HashMap<String, Object>();
        hintsMap.put("startSweep", new Integer(0));
        hintsMap.put("endSweep", new Integer(0));

        hintsMap.put("attributes", EXPORT_POLY_ATTRIBUTES);

        // TODO: instead of using the NexradFilter object, use the hints map
        // to define the attributes managed by the NexradFilter class

        // default NexradFilter for Level-II data
        WCTFilter nxfilter = new WCTFilter();
        nxfilter.setMinValue(-500.0);
        hintsMap.put("nexradFilter", nxfilter);

        // TODO: instead of using preset classifications, allow user
        // to input custom classification values
        hintsMap.put("classify", new Boolean(false));

        // ignore the range folded values?
        hintsMap.put("ignoreRF", new Boolean(true));


        hintsMap.put("downsample-numGates", new Integer(1));
        hintsMap.put("downsample-numRays", new Integer(1));


        // Use JTS Geometry.buffer(0.0) to combine adjacent polygons
        //      hintsMap.put("reducePolygons", new Boolean(false));

        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));


    }


    /**
     * Set a decodeHint.  To get a list of supported hints and default values,
     * use 'getDecodeHints()'.  The currently supported hints are as follows: <br><br>
     * <ol>
     *  <li> <b>startSweep</b>: 
     *  		integer zero-based sweep number.  If less than 0, it is set to 0.  If
     *  		startSweep > totalNumSweeps, then startSweep is set to the last sweep present.</li>
     *  <li> <b>endSweep</b>: 
     *  		integer zero-based sweep number.  If endSweep < startSweep, then endSweep is
     *  		set to the value of startSweep.  If endSweep = startSweep, then only that one
     *  		sweep is decoded.  If endSweep > totalNumSweeps, then endSweep is set to the 
     *  		last sweep present.  An easy catch-all to decode all sweeps for any NEXRAD VCP 
     *  		would be to set the startSweep = 0 and endSweep = 1000 (for example). </li>
     *  <li> <b>attributes</b>: 
     *  		AttributeType[] object that determines which set of attributes to produce.  
     *  		Use the static arrays in this class - they are the only ones supported.
     *  <li> <b>nexradFilter</b>: 
     *  		WCTFilter object that defines filtering options on range, azimuth, 
     *  		height and geographic bounds.
     *  <li> <b>classify</b>: 
     *  		Boolean object to determine if Level-III NEXRAD classification levels 
     *  		should be used.  Classifying can reduce the rasterization time and number of features because
     *  		range bins with the same classification (ex: 5-10 dBZ) are combined into a single polygon.
     *  <li> <b>ignoreRF</b>: 
     *  		Ignore the Range-Folded values (only present in doppler moments)
     * @param hintsMap
     */
    public void setDecodeHint(String hintKey, Object hintValue) throws DecodeHintNotSupportedException {
        if (! hintsMap.keySet().contains(hintKey)) {
            throw new DecodeHintNotSupportedException(this.getClass().toString(), hintKey, hintsMap);
        }

        hintsMap.put(hintKey, hintValue);
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
     * Get the array of sweep starting date/times - 
     * the size will correspond to the number of sweeps decoded. <br>
     * dateTime[0] = dateTime of 'startSweep' decode hint. <br>
     * dateTime[dateTime.length-1] = dateTime of 'endSweep' decode hint. <br>
     * @return
     */
    public Date[] getSweepDateTime() {
        return sweepDateTime;
    }


    /**
     * Decodes data and stores with in-memory FeatureCollection
     * @return
     * @throws DecodeException
     */
    //    @Override
    public void decodeData() throws DecodeException {
        fc.clear();

        StreamingProcess process = new StreamingProcess() {
            public void addFeature(Feature feature)
            throws StreamingProcessException {
                fc.add(feature);
            }
            public void close() throws StreamingProcessException {
                logger.info("STREAMING PROCESS close() ::: fc.size() = "+fc.size());
            }    		
        };

        decodeData(new StreamingProcess[] { process } );

    }




    /**
     * @param streamingProcessArray Array of StreamingProcess objects.
     */
    public void decodeData(StreamingProcess[] streamingProcessArray) throws DecodeException {
        decodeData(streamingProcessArray, true);
    }

    /**
     * @param streamingProcessArray Array of StreamingProcess objects.
     * @param autoClose Do we call .close() on each StreamingProcess after decoding has finished.  
     * Set to false, if aggregation of many files is needed and the process will be closed outside
     * of this decoder.   
     */
    public void decodeData(StreamingProcess[] streamingProcessArray, boolean autoClose) throws DecodeException {


//        System.out.println(header.getLat()+" "+header.getLon()+" ::: ");



        DataDecodeEvent event = new DataDecodeEvent(this);
        try {

            WCTFilter nxfilter = (WCTFilter)hintsMap.get("nexradFilter");
            boolean classify = (Boolean)hintsMap.get("classify");
            boolean useRFvalues = ! (Boolean)hintsMap.get("ignoreRF");
            AttributeType[] attTypes = (AttributeType[])hintsMap.get("attributes");

            // set up classification values based on Level-III scheme
            setUpClassifications();

            String units = "N/A";
            double rangeFoldedValue = Double.NEGATIVE_INFINITY;
            List<Attribute> attList = radialVar.getAttributes();
            for (Attribute a : attList) {
                if (a.getName().equals("range_folded_value")) {
                    rangeFoldedValue = a.getNumericValue().doubleValue();
                }
                if (a.getName().equals("units")) {
                    units = a.getStringValue();
                }
            }
//            System.out.println("attributes read: units="+units+"  range_folded_value="+rangeFoldedValue);


            double RF_VALUE = -200.0;
            double SNR_VALUE = -999.0;


            // Use Geotools Proj4 implementation to get MathTransform object
            MathTransform nexradTransform = nexradProjection.getRadarTransform(header.getLon(), header.getLat());



            //List attributes = radialVar.getAttributes();





            // Start decode
            // --------------
            for (int i = 0; i < listeners.size(); i++) {
                event.setProgress(0);
                listeners.get(i).decodeStarted(event);
            }



            // set the current schema FeatureType
            if (attTypes == DISPLAY_POLY_ATTRIBUTES) {
                schema = display_schema;
            }
            else if (attTypes == EXPORT_POLY_ATTRIBUTES) {
                schema = poly_schema;
            }
            else if (attTypes == EXPORT_POINT_ATTRIBUTES) {
                schema = point_schema;
            }
            else {
                throw new DecodeException("Supplied AttributeType array doesn't not match");
            }










            int startSweep = (Integer)hintsMap.get("startSweep");
            int endSweep = (Integer)hintsMap.get("endSweep");

            if (startSweep < 0) {
                startSweep = 0;
            }
            if (startSweep >= radialVar.getNumSweeps()) {
                startSweep = radialVar.getNumSweeps()-1;
            }

            if (endSweep < 0) {
                endSweep = 0;
            }
            if (endSweep < startSweep) {
                endSweep = startSweep;
            }
            if (endSweep >= radialVar.getNumSweeps()) {
                endSweep = radialVar.getNumSweeps()-1;
            }


            logger.info("UNITS = '"+units+"'");




            logger.info("DESCRIPTION::::: "+radialVar.getDescription());




            sweepDateTime = new Date[endSweep - startSweep + 1];
            int geoIndex = 0;

            for (int s=startSweep; s<endSweep+1; s++) { 

                RadialDatasetSweep.Sweep sweep = radialVar.getSweep(s);

                
//                System.err.println(sweep.getStartingTime() + " " + sweep.getEndingTime());
                

                //                int numGates = (Integer)hintsMap.get("downsample-numGates");
                //                int numRays = (Integer)hintsMap.get("downsample-numRays");
                //                // only do this if we really need to
                //                if (numGates > 0 || numRays > 0) {
                //                    SweepPyramid sweepPyramid = new SweepPyramid(sweep);
                //                    sweep = sweepPyramid.getDownsampledSweep(numGates, numRays);
                //                }



                for (Attribute att: radialVar.getAttributes()) {
                    logger.fine("ATT LIST: "+att.toString());
                }




                float meanElev = sweep.getMeanElevation();
                int nrays = sweep.getRadialNumber();
                float beamWidth = sweep.getBeamWidth();
                int ngates = sweep.getGateNumber();
                float gateSize = sweep.getGateSize();
                double range_to_first_gate = sweep.getRangeToFirstGate();
                //                sweepDateTime[endSweep-s] = sweep.getStartingTime();
                sweepDateTime[endSweep-s] = new Date(sweep.getStartingTime().getTime() + (int)sweep.getTime(0));

                logger.fine("getTime: "+(int)sweep.getTime(0));



                // represents the LOWER LEFT CORNER of the range bin
                Coordinate[][] indexedCoordArray = new Coordinate[nrays+1][ngates+1];


                logger.fine("startSweep = "+startSweep+"  -----  endSweep = "+endSweep);
                logger.fine("meanElev = "+meanElev);
                logger.fine("nrays = "+nrays);
                logger.fine("beamWidth = "+beamWidth);
                logger.fine("ngates = "+ngates);
                logger.fine("gateSize = "+gateSize);                
                logger.fine("rangeToFirstGate = "+range_to_first_gate);


                this.lastDecodedElevationAngle = meanElev;

                double range_step = sweep.getGateSize();
                // go 1 extra to complete the gate
                double[] range = new double[sweep.getGateNumber()+1];


                // check that each ray has the same elevation angle within the sweep
                double sweepElev = sweep.getElevation(0);
                logger.fine("sweepElev = "+sweepElev);
                for (int i = 1; i < nrays; i++) {
                    if (sweepElev != sweep.getElevation(i)) {
                        //                      throw new DecodeException("ELEVATION ANGLE CHANGES WITHIN SWEEP - THIS DATA STRUCTURE IS NOT SUPPORTED\n"+
                        //                      "VALUES: "+sweepElev+" and "+sweep.getElevation(i)+" at i="+i, null);
                        logger.fine("ELEVATION ANGLE CHANGES WITHIN SWEEP - THIS DATA STRUCTURE MAY NOT BE SUPPORTED\n"+
                                "VALUES: "+sweepElev+" and "+sweep.getElevation(i)+" at i="+i);
                        sweepElev = sweep.getElevation(i);
                    }
                }
                logger.info("sweepElev = "+sweepElev);

                sweepElev = meanElev;
                
                
                // TODO remove this workaround for bug in NCJ
                if (sweep.getsweepVar().getFullNameEscaped().equals("DigitalInstantaneousPrecipitationRate")) {
                	sweepElev = 0;
                }
                
                


                // precalculate the range values in our 3-d Albers coordinate system (simple x-y-z in meters)

                // Adjust range_step for elevation angle
                // find actual distance on surface (instead of range from radar at specified elevation angle)
                range_step = range_step * Math.cos(Math.toRadians(sweepElev));
                range[0] = (range_to_first_gate * Math.cos(Math.toRadians(sweepElev))) - range_step / 2.0;
                // find actual distance on surface (instead of range from radar at specified elevation angle)
                for (int i = 1; i < range.length; i++) {
                    range[i] = (double) (range[i - 1] + range_step);
                }
                // adjust for first bin at 500m with gate size of 1000m
                if (range[0] < 0) {
                    range[0] = 1;
                }

                logger.info(" elevation: " + sweepElev);
                logger.info(" number of range gates: " + sweep.getGateNumber());
                logger.info(" range_step: " + range_step);
                logger.info(" range_to_first_gate: " + range_to_first_gate);
                logger.info(" RANGE[0]: " + range[0]);

                // Add distance filter to decoder - find starting and ending bin numbers
                int startbin;

                // Add distance filter to decoder - find starting and ending bin numbers
                int endbin;

                if (nxfilter != null) {
                    startbin = (int) (((nxfilter.getMinDistance() * 1000.0 - range[0]) / range_step) + 0.01);
                    endbin = (int) (((nxfilter.getMaxDistance() * 1000.0 - range[0]) / range_step) + 0.01);
                    if (startbin < 0 || nxfilter.getMinDistance() == WCTFilter.NO_MIN_DISTANCE) {
                        startbin = 0;
                    }
                    if (endbin > range.length || nxfilter.getMaxDistance() == WCTFilter.NO_MAX_DISTANCE) {
                        endbin = range.length-1;
                    }
                }
                else {
                    startbin = 0;
                    endbin = sweep.getGateNumber();
                }

                //  For each radial in the cut, read all of the bin data
                int value;
                double[] geoXY = new double[2];
                double xpos;
                double ypos;
                double angle1 = 0.0;
                double angle2 = 0.0;

                double range_in_nmi;
                double height;
                double elevation_sin;
                double elevation_cos;

                double last_azimuth;
                double next_azimuth;
                double first_azimuth;
                double last_azimuth_diff;
                double next_azimuth_diff;
                double deg_used = 0.0f;
                double angle1_sin;
                double angle1_cos;
                double angle2_sin;
                double angle2_cos;
                double azimuth_sin;
                double azimuth_cos;
                boolean first = true;
                int startgate;
                float[] data;

                ArrayList<Coordinate> leftCoordinates = new ArrayList<Coordinate>();
                ArrayList<Coordinate> rightCoordinates = new ArrayList<Coordinate>();
                ArrayList<Coordinate> coordinates = new ArrayList<Coordinate>();

                // General estimate for azimuth difference
                //              double hold_last_azimuth_diff = 1.0;
                //              double hold_next_azimuth_diff = 1.0;
                double hold_last_azimuth_diff = sweep.getBeamWidth();
                double hold_next_azimuth_diff = sweep.getBeamWidth();

                double minValue = Double.POSITIVE_INFINITY;
                double maxValue = Double.NEGATIVE_INFINITY;
                

                /* data variable at radial level */
                for (int i = 0; i < nrays; i++) {
                    float azimuth = sweep.getAzimuth(i);
                    float elevation = sweep.getElevation(i);
                    data = sweep.readData(i);

                    //                  System.out.print("azim["+i+"] = "+azimuth);
                    //                  System.out.print("   elev["+i+"] = "+elevation);
                    //                  logger.fine("   data["+i+"].length = "+data.length + " :::  sweep.getGateNumber()="+sweep.getGateNumber());
                    //                  System.exit(1);              







                    try {






                        /*
                         *  Get the azimuth angle from the table created when the file was *
                         *  opened.  A negative azimuth indicates the record did not contain   *
                         *  digital radar data so it should be skipped.
                         */
                        if (i == 0) {
                            last_azimuth = azimuth - hold_last_azimuth_diff;
                        }
                        else {
                            last_azimuth = sweep.getAzimuth(i-1);
                        }

                        if (i == nrays-1) {
                            next_azimuth = azimuth + hold_next_azimuth_diff;
                        }
                        else {
                            next_azimuth = sweep.getAzimuth(i+1);
                        }


                        // Break out of decoder if
                        if (Double.isNaN(next_azimuth) || Double.isNaN(last_azimuth)) {
                            continue;
                        }






                        /*
                         *  Determine the azimuth angles (in 0.5 degree increments) that   *
                         *  define the bounds for the beam.
                         */
                        // add 360 to both angles if we cross 0
                        if (azimuth < last_azimuth) {
                            azimuth += 360;
                            next_azimuth += 360;
                        }
                        // add 360 only to the next_azimuth angle
                        if (next_azimuth < azimuth) {
                            next_azimuth += 360;
                        }

                        last_azimuth_diff = Math.abs(azimuth - last_azimuth);
                        next_azimuth_diff = Math.abs(next_azimuth - azimuth);

                        if (last_azimuth_diff > 2*sweep.getBeamWidth()) {
                            last_azimuth_diff = hold_last_azimuth_diff;
                        }
                        else {
                            hold_last_azimuth_diff = last_azimuth_diff;
                        }

                        if (next_azimuth_diff > 2*sweep.getBeamWidth()) {
                            next_azimuth_diff = hold_next_azimuth_diff;
                        }
                        else {
                            hold_next_azimuth_diff = next_azimuth_diff;
                        }

                        angle1 = azimuth - last_azimuth_diff / 2.0;
                        angle2 = azimuth + next_azimuth_diff / 2.0;

                        if (angle1 % 90 == 0) {
                            angle1 += 0.00001;
                        }
                        if (angle2 % 90 == 0) {
                            angle2 += 0.00001;
                        }


                        //                      logger.fine("bin azimuth extent: "+angle1+" - "+angle2);


                        angle1 = Math.toRadians(angle1);
                        angle2 = Math.toRadians(angle2);

                        angle1_sin = Math.sin(angle1);
                        angle1_cos = Math.cos(angle1);
                        angle2_sin = Math.sin(angle2);
                        angle2_cos = Math.cos(angle2);
                        azimuth_sin = Math.sin(Math.toRadians(azimuth));
                        azimuth_cos = Math.cos(Math.toRadians(azimuth));

                        boolean foundNonZero = false;

                        elevation_sin = Math.sin(Math.toRadians(elevation));
                        elevation_cos = Math.cos(Math.toRadians(elevation));


                        //for (int j = 0; j < bins; j++) {
                        for (int j = startbin; j < endbin; j++) {

                            double dvalue;

                            if (Float.isNaN(data[j])) {
                                dvalue = Double.NaN;                                  
                            }
                            else {
                                dvalue = (double)data[j];
                            }




                            

                            int colorCode;

                            if (useRFvalues && dvalue == RF_VALUE) {
                                ;
                            }
                            else if (nxfilter != null && !nxfilter.accept(dvalue)) {
                                dvalue = SNR_VALUE;
                            }
                            
                            

                            

                            //                            colorCode = getColorCode(dvalue);
                            //                          colorCode = (int)dvalue;

                            // ============================= BEGIN POLYGON CREATION =====================================

                            // value of 0 == below signal to noise ratio
                            // value of 1 == ambiguous



                            //if (dvalue != SNR_VALUE) {
                            //                          if (dvalue != Double.NaN) {
                            if (! Double.isNaN(dvalue) && dvalue > -250.0) {
                                //if (colorCode > 0) {


                                //                                logger.fine(radialVar.getAttributes().g);
                                // convert to knots if units are m/s
                                //                                if (radialVar.getUnitsString().equals("m/s")) {
                                if (dvalue == rangeFoldedValue) {
//                                    System.out.println(dvalue);
                                    dvalue = 800;
                                }
                                else if (units.equals("m/s")) {
                                    dvalue *= 1.9438445;
                                }
                                colorCode = getColorCode(dvalue);

                                //                                System.out.println(colorCode+" ::: "+dvalue);

                                if (dvalue != 800 && dvalue > maxValue) {
                                    maxValue = dvalue;
                                }
                                if (dvalue != 800 && dvalue < minValue) {
                                    minValue = dvalue;
                                }


                                if (attTypes == EXPORT_POINT_ATTRIBUTES) {

                                    xpos = (range[j]+range_step/2) * azimuth_sin;
                                    ypos = (range[j]+range_step/2) * azimuth_cos;
                                    geoXY = (nexradTransform.transform(new CoordinatePoint(xpos, ypos), null)).getCoordinates();

                                    Point point = geoFactory.createPoint(new Coordinate(geoXY[0], geoXY[1]));

//                                    // Using Radar Beam Propagation Equation (Range-Height Equation)
//                                    range_in_nmi = (range[j]+range_step/2) * 0.000539957 / Math.cos(Math.toRadians(elevation));
//                                    // get beam height at current range in nmi
//                                    height = ((Math.pow(range_in_nmi, 2) * Math.pow(elevation_cos, 2)) / 9168.66 + range_in_nmi * elevation_sin) * 6076.115;
//                                    // convert to meters
//                                    height *= 0.3048;
                                    
                                    height = NexradEquations.getRelativeBeamHeight(elevation_cos, elevation_sin, range[j]+range_step/2);
                                    
                                    // add height of radar site in meters
                                    double heightASL = height + header.getAlt()/3.28083989501312; 


                                    if (nxfilter == null ||
                                            //nxfilter.accept(poly, dvalue, azimuth, range[j], range[j] + range_step)) {
                                            nxfilter.accept(point, dvalue, azimuth, height)) {

                                        if (nxfilter != null) {
                                            point = (Point) (nxfilter.clipToExtentFilter(point));
                                        }

                                        if (point != null) {

                                            if (dvalue < lastDecodedMinValue) { lastDecodedMinValue = dvalue; }
                                            if (dvalue > lastDecodedMaxValue) { lastDecodedMaxValue = dvalue; }

                                            // create the feature
                                            Feature feature = point_schema.create(new Object[]{
                                                    point,
                                                    new Integer(s),
                                                    dateFormat.format(sweep.getStartingTime()),
                                                    new Float(elevation),
                                                    new Float((double) dvalue),
                                                    new Float(azimuth),
                                                    new Float(range[j]+range_step/2),
                                                    new Float(height),
                                                    new Float(heightASL)
                                            }, new Integer(geoIndex++).toString());


                                            // add to streaming processes
                                            for (int n=0; n<streamingProcessArray.length; n++) {
                                                streamingProcessArray[n].addFeature(feature);
                                            }
                                        }
                                    }
                                }
                                else {


                                    leftCoordinates.clear();
                                    rightCoordinates.clear();
                                    coordinates.clear();

                                    double dwork;

                                    boolean inRun = false;

                                    int nextColorCode = colorCode;
                                    startgate = j;
                                    while (colorCode == nextColorCode && j < endbin) {


                                        if (indexedCoordArray[i][j] == null) {
                                            xpos = (range[j]) * angle1_sin;
                                            ypos = (range[j]) * angle1_cos;
                                            geoXY = (nexradTransform.transform(new CoordinatePoint(xpos, ypos), null)).getCoordinates();
                                            //                                      geoXY = (nexradTransform.transform(new DirectPosition2D(xpos, ypos), null)).getCoordinates();
                                            indexedCoordArray[i][j] = new Coordinate(geoXY[0], geoXY[1]);
                                        }
                                        //                                  leftCoordinates.add(new Coordinate(geoXY[0], geoXY[1]));
                                        if (! inRun) {
                                            leftCoordinates.add(indexedCoordArray[i][j]);
                                        }

                                        if (indexedCoordArray[i][j+1] == null) {
                                            xpos = (range[j] + range_step) * angle1_sin;
                                            ypos = (range[j] + range_step) * angle1_cos;
                                            geoXY = (nexradTransform.transform(new CoordinatePoint(xpos, ypos), null)).getCoordinates();
                                            indexedCoordArray[i][j+1] = new Coordinate(geoXY[0], geoXY[1]);
                                        }
                                        //                                  leftCoordinates.add(new Coordinate(geoXY[0], geoXY[1]));
                                        leftCoordinates.add(indexedCoordArray[i][j+1]);

                                        if (indexedCoordArray[i+1][j] == null) {                                        
                                            xpos = (range[j]) * angle2_sin;
                                            ypos = (range[j]) * angle2_cos;
                                            geoXY = (nexradTransform.transform(new CoordinatePoint(xpos, ypos), null)).getCoordinates();
                                            indexedCoordArray[i+1][j] = new Coordinate(geoXY[0], geoXY[1]);
                                        }
                                        //                                  rightCoordinates.add(new Coordinate(geoXY[0], geoXY[1]));
                                        if (! inRun) {
                                            rightCoordinates.add(indexedCoordArray[i+1][j]);
                                        }

                                        if (indexedCoordArray[i+1][j+1] == null) {
                                            xpos = (range[j] + range_step) * angle2_sin;
                                            ypos = (range[j] + range_step) * angle2_cos;
                                            geoXY = (nexradTransform.transform(new CoordinatePoint(xpos, ypos), null)).getCoordinates();
                                            indexedCoordArray[i+1][j+1] = new Coordinate(geoXY[0], geoXY[1]);
                                        }
                                        //                                  rightCoordinates.add(new Coordinate(geoXY[0], geoXY[1]));
                                        rightCoordinates.add(indexedCoordArray[i+1][j+1]);

                                        if (classify) {
                                            if (j+1 == endbin) {
                                                break;
                                            }
                                            // Get color code for next bin
                                            dwork = (double)data[j+1];
                                            if (units.equals("m/s")) {
                                                dwork *= 1.9438445;
                                            }

                                            nextColorCode = getColorCode(dwork);

                                            //                                        System.out.println("classifying - start: "+dvalue+"  colorCode="+colorCode+
                                            //                                                "    nextVal="+dwork+"  nextColorCode="+nextColorCode+"   ("+i+","+j+")");

                                            //                                      logger.fine("classifying - start: "+dvalue+"  colorCode="+colorCode+
                                            //                                      "    nextVal="+dwork+"  nextColorCode="+nextColorCode+"  ("+i+","+j+")");



                                            // If color code is same then continue polygon to next bin
                                            if (nextColorCode == colorCode) {
                                                //                                            nextColorCode = colorCode+1;
                                                j++;
                                                inRun = true;
                                            }

                                            // Set bin value to classification value
                                            //                                      if (colorCode == 0) {
                                            //                                      colorCode++;
                                            //                                      }
                                            try {
                                                if (colorCode >= 0 && colorCode < classificationValues.length) {
                                                    dvalue = classificationValues[colorCode];
                                                    //                                              dvalue = colorCode;
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                        }
                                        else {
                                            // Break out of loop if we are not classifying (could also set nextColorCode = colorCode + 1)
                                            break;
                                        }
                                    }

                                    // Add coordinates in this order:
                                    // 1  2  3  4  5  6
                                    // 12 11 10 9  8  7

                                    for (int n = 0; n < leftCoordinates.size(); n++) {
                                        //                                    if (n < leftCoordinates.size()-1 && leftCoordinates.get(n).equals(leftCoordinates.get(n+1))) {
                                        coordinates.add(leftCoordinates.get(n));
                                        //                                    }
                                    }
                                    for (int n = rightCoordinates.size() - 1; n >= 0; n--) {
                                        //                                    if (n > 0 && ! rightCoordinates.get(n).equals(rightCoordinates.get(n-1))) {
                                        coordinates.add(rightCoordinates.get(n));
                                        //                                    }
                                    }
                                    // Add first point to close polygon
                                    coordinates.add(leftCoordinates.get(0));

                                    // Create polygon
                                    Coordinate[] coordArray = null;
                                    try {
                                        //Coordinate[] cArray = new Coordinate[coordinates.size()];
                                        coordArray = new Coordinate[coordinates.size()];
                                        LinearRing lr = geoFactory.createLinearRing((Coordinate[]) (coordinates.toArray(coordArray)));
                                        Polygon poly = JTSUtilities.makeGoodShapePolygon(geoFactory.createPolygon(lr, null));
                                        //                                  Polygon poly = JTSUtilities.makeGoodShapePolygon(geoFactory.createPolygon(lr, null));
                                        //Geometry poly = JTSUtilities.makeGoodShapePolygon(geoFactory.createPolygon(lr, null));

                                        //                                  logger.fine(poly);


                                        leftCoordinates.clear();
                                        rightCoordinates.clear();
                                        coordinates.clear();
                                        coordArray = null;

                                        // Using Radar Beam Propagation Equation (Range-Height Equation)
                                        range_in_nmi = range[startgate] * 0.000539957 / Math.cos(Math.toRadians(elevation));
                                        // get beam height at current range in nmi
                                        height = ((Math.pow(range_in_nmi, 2) * Math.pow(elevation_cos, 2)) / 9168.66 + range_in_nmi * elevation_sin) * 6076.115;
                                        // convert to meters
                                        height *= 0.3048;
                                        //                                  // add height of radar site in meters
                                        double heightASL = height + header.getAlt()/3.28083989501312; 


                                        if (nxfilter == null ||
                                                //nxfilter.accept(poly, dvalue, azimuth, range[j], range[j] + range_step)) {
                                                nxfilter.accept(poly, dvalue, azimuth, height)) {

                                            if (nxfilter != null) {
                                                poly = (Polygon) (nxfilter.clipToExtentFilter(poly));
                                            }

                                            if (poly != null) {

                                                if (dvalue < lastDecodedMinValue) { lastDecodedMinValue = dvalue; }
                                                if (dvalue > lastDecodedMaxValue) { lastDecodedMaxValue = dvalue; }

                                                if (attTypes == DISPLAY_POLY_ATTRIBUTES) {
                                                    // create the feature
                                                    Feature feature = display_schema.create(new Object[]{
                                                            poly,
                                                            new Float((double) dvalue),
                                                            new Integer(colorCode)
                                                    }, new Integer(geoIndex++).toString());

                                                    // add to streaming processes
                                                    for (int n=0; n<streamingProcessArray.length; n++) {
                                                        streamingProcessArray[n].addFeature(feature);
                                                    }
                                                }                                                                                     
                                                else if (attTypes == EXPORT_POLY_ATTRIBUTES) {
                                                    // create the feature
                                                    Feature feature = poly_schema.create(new Object[]{
                                                            poly,
                                                            new Integer(s),
                                                            dateFormat.format(sweep.getStartingTime()),
                                                            new Float(elevation),
                                                            new Float((double) dvalue),
                                                            //  new Integer(colorCode),
                                                            new Float(azimuth),
                                                            new Float(range[startgate]),
                                                            // start range
                                                            new Float(range[j + 1]),
                                                            // end range
                                                            new Float(height),
                                                            new Float(heightASL)
                                                    }, new Integer(geoIndex++).toString());


                                                    // add to streaming processes
                                                    for (int n=0; n<streamingProcessArray.length; n++) {
                                                        streamingProcessArray[n].addFeature(feature);
                                                    }


                                                }
                                            }

                                        }

                                        //polyVector[colorCode].addElement((Object) poly);

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        logger.fine("================================================================");
                                        for (int ii = 0; ii < coordArray.length; ii++) {
                                            logger.fine(coordArray[ii].toString());
                                        }
                                        logger.fine("================================================================");
                                        return;
                                    }


                                }

                            }

                        }

                        // Decode progress
                        // --------------
                        for (int n = 0; n < listeners.size(); n++) {
//                            event.setProgress((int) (((((double) i) / nrays) * 100.0) * 1/(double)(endSweep+1-startSweep)) + (int)(s*100.0/(double)(endSweep+1-startSweep)));
                            
//                        	event.setProgress((int) (((((double) i) / nrays) * 100.0) * s/(double)(endSweep+1-startSweep)) );
                            
                            int percent = (int)Math.round(100*WCTUtils.progressCalculator(new int[] { s, i }, new int[] { endSweep+1-startSweep, nrays }));
                            event.setProgress(percent);
                            
                            listeners.get(n).decodeProgress(event);
                                                       
//                            System.out.println(startSweep+" / "+endSweep+"  "+event.getProgress());
                        }

                        
                        if (WCTUtils.getSharedCancelTask().isCancel()) {
                        	s = endSweep+1;
                        	i = nrays+1;
                        }
                        

                    } catch (Exception e) {
                        System.err.println("LEVEL2 DECODE EXCEPTION: RADIAL NUMBER=" + i + "  AZIMUTH=" + azimuth);
                        e.printStackTrace();
                    }

                }
                
                header.setMinValue(minValue);
                header.setMaxValue(maxValue);

            }


            if (autoClose) {
                for (int i=0; i<streamingProcessArray.length; i++) {
                    streamingProcessArray[i].close();
                }
            }



        } catch (Exception e) {
            e.printStackTrace();
            throw new DecodeException(e.toString(), null);
        } finally {
            // End of decode
            // --------------
            for (int i = 0; i < listeners.size(); i++) {
                event.setProgress(0);
                listeners.get(i).decodeEnded(event);
            }
        }


    }















    public FeatureType[] getFeatureTypes() {
        return new FeatureType[]{ schema };
    }

    public java.awt.geom.Rectangle2D.Double getLastDecodedExtent() {
        // TODO Auto-generated method stub
        return null;
    }










    public FeatureCollection getFeatures() {
        return fc;
    }



    public double getLastDecodedCutElevation() {
        return lastDecodedElevationAngle;
    }
    public String getLastDecodedMoment() {
        return radialVar.getName().toUpperCase();
    }






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
     * @param  listener  DataDecodeListener to remove.
     */
    public void removeDataDecodeListener(DataDecodeListener listener) {

        listeners.remove(listener);

    }










    public String[] getSupplementalDataArray() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }






    /**
     *  Gets the classification value for the raw data based on Level-III classification schemes.
     *
     * @param  dvalue  Description of the Parameter
     * @return         The colorCode value
     */
    private int getColorCode(double dvalue) {

        boolean useRfValues = true;
        //      return (int)dvalue;

        //      logger.fine(dvalue);
        if (dvalue < -900.0 || Double.isNaN(dvalue)) {
            return -1;
            //return 0;
        }



        int colorCode = 0;

        if (radialVar.getName().contains("Reflectivity")) {
            if (header.getVCP() == 31 || header.getVCP() == 32) {
                if (dvalue > 28) {
                    colorCode = 15;
                }
                else {
                    colorCode = (int) ((dvalue + 28) / 4.0) + 1;
                }
            }
            else {
                if (dvalue > 75) {
                    colorCode = 15;
                }
                else if (dvalue < 0) {
                    colorCode = 0;
                }
                else {
                    colorCode = (int) (dvalue / 5.0) + 1;
                }
            }
        }
        else if (radialVar.getName().contains("Velocity")) {
            if (dvalue == 100) {
                if (useRfValues) {
                    colorCode = 16;
                }
                else {
                    colorCode = 0;
                }
            }
            else if (dvalue < -60) {
                colorCode = 1;
            }
            else if (dvalue > 60) {
                colorCode = 15;
            }
            else if (dvalue <= 0) {
                colorCode = (int) ((dvalue + 60) / 10.0) + 2;
            }
            else {
                colorCode = (int) ((dvalue + 60) / 10.0) + 3;
            }
        }
        // Spectrum width
        else {
            if (dvalue == 100) {
                if (useRfValues) {
                    colorCode = 7;
                }
                else {
                    colorCode = 0;
                }
            }
            else if (dvalue > 30) {
                colorCode = 6;
            }
            else {
                colorCode = (int) (dvalue / 6.0) + 1;
            }
        }

        return colorCode;
    }





    private void setUpClassifications() {
        boolean classify = (Boolean)hintsMap.get("classify");

        String[] dataThresholdArray = null;
        if (radialVar.getName().contains("Reflectivity")) {
            if (classify && (header.getVCP() == 31 || header.getVCP() == 32)) {
                dataThresholdArray = new String[]{
                        "SNR", "<= - 28", "- 24", "- 20", "- 16", "- 12", "- 8", "- 4", "  0",
                        "+ 4", "+ 8", "+ 12", "+ 16", "+ 20", "+ 24", ">= + 28"
                };
                this.classificationValues = new double[] {
                        -200.0, -24.0, -20.0, -16.0, -12.0, -8.0, -4.0, 0.0, 4.0, 8.0, 12.0, 16.0, 20.0, 24.0, 28.0, 32.0
                };
            }
            else if (classify) {
                dataThresholdArray = new String[]{
                        "SNR", "<= 0", "+ 5", "+ 10", "+ 15", "+ 20", "+ 25", "+ 30",
                        "+ 35", "+ 40", "+ 45", "+ 50", "+ 55", "+ 60", "+ 65", "+ 70", ">= + 75"
                };
                this.classificationValues = new double[16];
                for (int i = 0; i < 16; i++) {
                    classificationValues[i] = 5.0 * i;
                }
            }
            else {
                dataThresholdArray = new String[]{
                        "SNR", "<= - 20", "- 15", "- 10", "- 5", "  0", "+ 5", "+ 10", "+ 15",
                        "+ 20", "+ 25", "+ 30", "+ 35", "+ 40", "+ 45", "+ 50", "+ 55", "+ 60",
                        "+ 65", "+ 70", ">= + 75"
                };
                this.classificationValues = new double[16];
                for (int i = 0; i < 16; i++) {
                    classificationValues[i] = 5.0 * i;
                }
            }
        }
        else if (radialVar.getName().contains("Velocity")) {
            logger.fine("FOUND VELOCITY ===========");
            dataThresholdArray = new String[]{
                    "SNR", "< - 60", "- 60", "- 50", "- 40", "- 30", "- 20", "- 10", "  0", "+ 10",
                    "+ 20", "+ 30", "+ 40", "+ 50", "+ 60", "> + 60", 
                    //                    "SNR", "< - 50", "- 50", "- 40", "- 30", "- 20", "- 10", "  0", "+ 10",
                    //                    "+ 20", "+ 30", "+ 40", "+ 50", "> 50", "RF"
            };
            this.classificationValues = new double[] {
                    -200.0, -60.0, -50.0, -40.0, -30.0, -20.0, -10.0, 0.0, 10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 200.0
            };
        }
        else if (radialVar.getName().contains("SpectrumWidth")) {

            logger.fine("FOUND SPECTRUM WIDTH ===========");        	 
            dataThresholdArray = new String[]{
                    //                    "SNR", "0", "6", "12", "18", "24", "30", "RF", "", "", "", "", "", ""
                    //                    "SNR", "0", "6", "12", "18", "24", "30", "", "", "", "", "", "", ""
                    "SNR", "0", "6", "12", "18", "24", "30", ""
            };

            this.classificationValues = new double[] {
                    //                    -200.0, 0.0, 6.0, 12.0, 18.0, 24.0, 30.0, 200.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0
                    -200.0, 0.0, 6.0, 12.0, 18.0, 24.0, 30.0, 200.0
            };
            //            classificationValues = new double[16];
            //            for (int i = 0; i < 7; i++) {
            //                classificationValues[i] = (6.0 * i) - 6.0;
            //            }
            //            classificationValues[7] = -200.0;
            //            for (int i = 8; i < 16; i++) {
            //                classificationValues[i] = 0.0;
            //            }
        }
        else {
            String[] categories = new String[16];
            for (int n=0; n<categories.length; n++) {
                categories[n] = String.valueOf( lastDecodedMinValue + 
                        n*(lastDecodedMaxValue - lastDecodedMinValue)/categories.length );
            }
            dataThresholdArray = categories;
        }


        if (classify) {
            for (int n=0; n<dataThresholdArray.length; n++) {
                dataThresholdArray[n] = dataThresholdArray[n]+" ("+n+")";
            }
        }

        header.setDataThresholdStringArray(dataThresholdArray);

    }



}
