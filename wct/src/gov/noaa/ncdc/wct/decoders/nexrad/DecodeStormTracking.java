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

import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.DecodeHintNotSupportedException;
import gov.noaa.ncdc.wct.decoders.MaxGeographicExtent;
import gov.noaa.ncdc.wct.decoders.StreamingProcess;
import gov.noaa.ncdc.wct.decoders.StreamingProcessException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import org.geotools.ct.MathTransform;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.pt.CoordinatePoint;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

/**
 * Decodes NST NEXRAD Level-III Storm Tracking alphanumeric product.
 *  
 * From 2620003J.pdf 18.1:
 *  
 *  "This product shall provide information concerning the past, present 
 *  and future positions of each identified storm cell. This product shall 
 *  be generated from the output of the Storm Cell Tracking and Storm 
 *  Position Forecast algorithms. It shall be produced in a tabular format 
 *  of alphanumeric values, as a stand alone graphic product, and in a 
 *  format for generating graphic overlays to other products. This product 
 *  shall be updated once per volume scan time. Each product shall include a 
 *  standard set of total annotations and number of identified storm cells 
 *  for which tracking is available. Upon user request, all site adaptable 
 *  parameters identified as inputs to the algorithm(s) used to generate data 
 *  for this product shall be available at the alphanumeric display."
 *  
 * @author     steve.ansari
 * @created    September 20, 2004
 */
public class DecodeStormTracking implements DecodeL3Alpha {

    private static final Logger logger = Logger.getLogger(DecodeStormTracking.class.getName());

    private String[] metaLabelString = new String[3];
    private FeatureCollection features = FeatureCollections.newCollection();
    private FeatureCollection lineFeatures = FeatureCollections.newCollection();
    private FeatureType schema = null;
    private FeatureType lineschema = null;
    private GeometryFactory geoFactory = new GeometryFactory();
    private java.awt.geom.Rectangle2D.Double wsrBounds;

    private HashMap<String, Object> decodeHints = new HashMap<String, Object>();

    private DecodeL3Header header;
    private String[] supplementalData = new String[2];

    private String numStorms;
    private String avgStormSpeed;
    private String avgStormDirection;

    private WCTProjections nexradProjection = new WCTProjections();

    private String datetime;



    /**
     * Constructor
     *
     * @param  header  Description of the Parameter
     * @throws IOException 
     */
    public DecodeStormTracking(DecodeL3Header header) throws DecodeException, IOException {
        this.header = header;
        this.wsrBounds = getExtent();
        decodeData();
    }


    /**
     * Returns the feature types used for these features
     * 
     * @return The featureType value
     */
    public FeatureType[] getFeatureTypes() {
        return new FeatureType[] {schema};
    }

    /**
     * Returns the LineFeatureType
     *
     * @return    The lineFeatureType value
     */
    public FeatureType getLineFeatureType() {
        return lineschema;
    }


    /**
     * Returns Rectangle.Double Bounds for the NEXRAD Site calculated during decode. (unique to product)
     * Could be 248, 124 or 32 nmi.
     *
     * @return    The nexradExtent value
     */
    public java.awt.geom.Rectangle2D.Double getNexradExtent() {
        return wsrBounds;
    }


    /**
     * Returns default display symbol type
     *
     * @return    The symbol type
     */
    public String getDefaultSymbol() {
        return org.geotools.styling.StyleBuilder.MARK_CROSS;
    }


    /** 
     * Returns the specified supplemental text data (index=1 == Block2, index2 == Block3, etc...)  
     */
    public String getSupplementalData(int index) {
        if (supplementalData[index] != null) {           
            return supplementalData[index];
        }
        else {
            return new String("NO DATA");
        }
    }

    /** 
     * Returns the supplemental text data array  
     */
    public String[] getSupplementalDataArray() {
        return supplementalData;
    }

    /**
     *  Gets the metaLabel attribute of the DecodeStormTracking object
     *
     * @param  index  Description of the Parameter
     * @return        The metaLabel value
     */
    public String getMetaLabel(int index) {
        if (metaLabelString[index] == null) {
            return "";
        }
        else {
            return metaLabelString[index];
        }
    }


    /**
     *  Description of the Method
     */
    private void makeMetaLabelStrings() {

        FeatureIterator fi = features.features();
        if (fi.hasNext()) {
            // only use first and thus strongest reading
            Feature f = fi.next();
            metaLabelString[0] = numStorms + " STORM CELLS";
            metaLabelString[1] = "AVG SPEED: " + avgStormSpeed + " KTS";
            metaLabelString[2] = "AVG DIRECTION: " + avgStormDirection + " DEG";
        }
        else {
            metaLabelString[0] = "NO STORM CELLS PRESENT";
        }

    }


//    @Override
    public Map<String, Object> getDecodeHints() {
        return decodeHints;
    }

//    @Override
    public void setDecodeHint(String hintKey, Object hintValue)
    throws DecodeHintNotSupportedException {
        throw new DecodeHintNotSupportedException("DecodeStormTracking", hintKey, decodeHints);

    }


    /**
     * Decodes data and stores with in-memory FeatureCollection
     * @return
     * @throws DecodeException
     */
//    @Override
    public void decodeData() throws DecodeException, IOException {
        features.clear();

        StreamingProcess process = new StreamingProcess() {
            public void addFeature(Feature feature)
            throws StreamingProcessException {
                features.add(feature);
            }
            public void close() throws StreamingProcessException {
                logger.info("STREAMING PROCESS close() ::: features.size() = "+features.size());
            }           
        };

        decodeData(new StreamingProcess[] { process } );    
    }


//    @Override
    public void decodeData(StreamingProcess[] processArray)
    throws DecodeException, IOException {

        decodeData(processArray, true);

    }





//    @Override
    public void decodeData(StreamingProcess[] processArray, boolean autoClose)
    throws DecodeException, IOException {


        datetime = header.getDate()+header.getHourString()+header.getMinuteString()+header.getSecondString();


        try {

//          Set up attribute table points
            {
                AttributeType geom = AttributeTypeFactory.newAttributeType("geom", Point.class);
                AttributeType wsrid = AttributeTypeFactory.newAttributeType("wsrid", String.class, true, 5);
                AttributeType datetime = AttributeTypeFactory.newAttributeType("datetime", String.class, true, 15);
                AttributeType lat = AttributeTypeFactory.newAttributeType("lat", Double.class, true, 10);
                AttributeType lon = AttributeTypeFactory.newAttributeType("lon", Double.class, true, 10);
                AttributeType id = AttributeTypeFactory.newAttributeType("id", String.class, true, 3);
                AttributeType time = AttributeTypeFactory.newAttributeType("time", Integer.class, true, 5);
                AttributeType range = AttributeTypeFactory.newAttributeType("range", Double.class, true, 7);
                AttributeType azim = AttributeTypeFactory.newAttributeType("azim", Double.class, true, 7);
                AttributeType movedeg = AttributeTypeFactory.newAttributeType("movedeg", String.class, true, 5);
                AttributeType movekts = AttributeTypeFactory.newAttributeType("movekts", String.class, true, 5);

                AttributeType[] attTypes = {geom, wsrid, datetime, lat, lon, id, time, range, azim, movedeg, movekts};
                schema = FeatureTypeFactory.newFeatureType(attTypes, "Storm Tracking Point Data");
            }
//          Set up attribute table for lines
            {
                AttributeType geom = AttributeTypeFactory.newAttributeType("geom", LineString.class);
                AttributeType wsrid = AttributeTypeFactory.newAttributeType("wsrid", String.class, true, 5);
                AttributeType datetime = AttributeTypeFactory.newAttributeType("datetime", String.class, true, 15);
                AttributeType lat = AttributeTypeFactory.newAttributeType("lat", Double.class, true, 10);
                AttributeType lon = AttributeTypeFactory.newAttributeType("lon", Double.class, true, 10);
                AttributeType id = AttributeTypeFactory.newAttributeType("id", String.class, true, 3);
                AttributeType time = AttributeTypeFactory.newAttributeType("time", Integer.class, true, 5);
                AttributeType range = AttributeTypeFactory.newAttributeType("range", Double.class, true, 7);
                AttributeType azim = AttributeTypeFactory.newAttributeType("azim", Double.class, true, 7);
                AttributeType movedeg = AttributeTypeFactory.newAttributeType("movedeg", String.class, true, 5);
                AttributeType movekts = AttributeTypeFactory.newAttributeType("movekts", String.class, true, 5);

                AttributeType[] attTypes = {geom, wsrid, datetime, lat, lon, id, time, range, azim, movedeg, movekts};
                lineschema = FeatureTypeFactory.newFeatureType(attTypes, "Storm Tracking Line Data");
            }


            // Decode the text blocks (block 2 and 3)
            DecodeL3AlphaGeneric decoder = new DecodeL3AlphaGeneric();
//          decoder.setVerbose(verbose);
            decoder.decode(header);

            logger.info("----------- VERSION: "+header.getVersion()+" ------------ \n");
            logger.info("----------- BLOCK 2 ----------- \n"+decoder.getBlock2Text()+"\n------ END BLOCK 2 ------ \n");
            logger.info("----------- BLOCK 3 ----------- \n"+decoder.getBlock3Text()+"\n------ END BLOCK 3 ------ \n");

            StringBuffer sb = new StringBuffer();

            // Lets make a custom legend for this block
            sb.append("  STORM TRACKING SUPPLEMENTAL DATA 1\n\n");
            sb.append("  ABBREVIATIONS:\n");
            sb.append("  AZ       = Azimuth Angle From Radar \n");
            sb.append("             (In Degrees where 0 deg = North, 90 = East, 180 = South, etc...)\n");
            sb.append("  RAN      = Range (Distance) From Radar (In Nautical Miles (nmi))\n");
            sb.append("  FCST MVT = Predicted Movement Direction/Speed (deg/kts)\n");
            sb.append("  ERR      = Forecast Error (nmi)\n");
            sb.append("  MEAN     = Mean Error (nmi)\n");
            sb.append("  DBZM     = Max Reflectivity of Storm Cell (dBZ)\n\n");

            sb.append(decoder.getBlock2Text());
            supplementalData[0] = sb.toString();
            sb.append("\n\n");

            // Build text for block 3 data
            sb = new StringBuffer();
            sb.append("  STORM TRACKING SUPPLEMENTAL DATA 2\n\n");                  
            //sb.append("    NOT APPLICABLE\n\n");
            sb.append(decoder.getBlock3Text());
            sb.append("\n\n");
            supplementalData[1] = sb.toString();

            // Convert the text to features
            convertSupplementalDataToFeatures(decoder.getBlock3Text());

            makeMetaLabelStrings();
            // END try
        } catch (Exception e) {
            logger.info("CAUGHT EXCEPTION:  " + e);
            //return null;
            e.printStackTrace();
        }
    }




    private void convertSupplementalDataToFeatures(String block3Text) 
    throws DecodeException, TransformException, IllegalAttributeException {



        double[] geoXY;
        double[] azim = new double[5];
        double[] range = new double[5];
        int linenum = 0;
        int lineIndex = 0;
        String movedeg;
        String movekts;
        Vector pointVector = new Vector();
        int geoIndex = 0;

        // New GeoTools based projection transformations
        MathTransform nexradTransform;
        try {
            // Use Geotools Proj4 implementation to get MathTransform object
            nexradTransform = nexradProjection.getRadarTransform(header);
        } catch (Exception e) {
            throw new DecodeException("PROJECTION TRANSFORM ERROR", header.getDataURL());
        }


        String[] lines = block3Text.split("\n");

        if (lines.length == 0) {
            metaLabelString[0] = "NO STORMS PRESENT";
            return;      
        }

        logger.info("FOUND VERSION "+header.getVersion()+" DATA");

        // FOR NOW, WE ARE NOT SUPPORTED THE VERSION 0 STORM TRACKING PRODUCT
        if (header.getVersion() != 1.0) {
            throw new DecodeException("UNSUPPORTED NEXRAD STORM TRACKING FILE VERSION: " + header.getVersion()+
                    ".  CURRENTLY ONLY VERSION 0 IS SUPPORTED.", header.getDataURL());
        }


        for (int x = 0; x < lines.length; x++) {

            String str = lines[x];
            // advance past empty lines
            if (str.trim().length() == 0) {
                continue;
            }


//          if (str.startsWith("PAGE ")) {
//          //0123456
//          // PAGE 1
//          pageNum = Integer.parseInt(str.substring(5, 6));  
//          }

            logger.info(linenum + " ::: " + str);



            if (header.getVersion() == 0) {

                // FOR NOW, WE ARE NOT SUPPORTED THE VERSION 0 STORM TRACKING PRODUCT


                if (str.trim().startsWith("RADAR ID")) {
                    numStorms = str.substring(74, 76);
                    avgStormSpeed = "N/A";
                    avgStormDirection = "N/A";
                }

//              ----------------- VERSION 0 ---------------------
//              RADAR ID: 313      DATE/TIME 06:09:95/00:02:20     NUMBER OF STORMS   5               
//              CURRENT POSITION    SPEED       FORECAST POSITIONS       FORCAST TRACKVAR 
//              STM    AZRAN  MOVEMENT     X/Y    15 MIN 30 MIN 45 MIN 60 MIN  ERR/MEAN   X/Y   
//              ID  (DEG-NM) (DEG-KT)     (KT)    (X/Y)  (X/Y)  (X/Y)  (X/Y)    (NM)    (NM) 
//              0         1         2         3         4         5         6         7         8
//              01234567890123456789012345678901234567890123456789012345678901234567890123456789012345
//              28    74/ 63  198/ 15      5       62     63     64     65    1.2/ 0.9   3.6   
//              15       21     25     28     32               0.5   
//              63    56/139  198/ 19      6     NO DAT NO DAT NO DAT NO DAT  0.0/ 0.0   0.0   
//              18     NO DAT NO DAT NO DAT NO DAT             0.0 

                if (str.charAt(9) == '/' && str.charAt(18) == '/') {
                    //logger.info(hitCount+" "+new String(data));

                    // Get next line
                    String str2 = lines[++x];

                    // Do current position
                    azim[0] = Double.parseDouble(str.substring(6, 9));
                    range[0] = Double.parseDouble(str.substring(10, 13));
                    if (str.substring(21, 24).equals("NEW")) {
                        movedeg = "NEW";
                        movekts = "NEW";
                    }
                    else {
                        movedeg = str.substring(19, 22);
                        movekts = str.substring(23, 26);
                    }

                    if (str.substring(31, 38).equals("NO DATA")) {
                        azim[1] = -999.9;
                        range[1] = -999.9;
                    }
                    else {
                        azim[1] = Double.parseDouble(str.substring(31, 34));
                        range[1] = Double.parseDouble(str.substring(35, 38));
                    }

                    if (str.substring(41, 48).equals("NO DATA")) {
                        azim[2] = -999.9;
                        range[2] = -999.9;
                    }
                    else {
                        azim[2] = Double.parseDouble(str.substring(41, 44));
                        range[2] = Double.parseDouble(str.substring(45, 48));
                    }

                    if (str.substring(51, 58).equals("NO DATA")) {
                        azim[3] = -999.9;
                        range[3] = -999.9;
                    }
                    else {
                        azim[3] = Double.parseDouble(str.substring(51, 54));
                        range[3] = Double.parseDouble(str.substring(55, 58));
                    }

                    if (str.substring(61, 68).equals("NO DATA")) {
                        azim[4] = -999.9;
                        range[4] = -999.9;
                    }
                    else {
                        azim[4] = Double.parseDouble(str.substring(61, 64));
                        range[4] = Double.parseDouble(str.substring(65, 68));
                    }

                    String id = str.substring(2, 4);
                    // Create point features
                    pointVector.clear();
                    for (int n = 0; n < 5; n++) {

                        if (azim[n] != -999.9 && range[n] != -999.9) {

                            // Correct for an azim of 0
                            if (azim[n] == 0.0 || azim[n] == 180.0 || azim[n] == 360.0) {
                                azim[n] += 0.000001;
                            }
                            // Convert from nautical mi to meters
                            geoXY = (nexradTransform.transform(
                                    new CoordinatePoint(
                                            range[n] * Math.sin(Math.toRadians(azim[n])) * 1852.0,
                                            range[n] * Math.cos(Math.toRadians(azim[n])) * 1852.0 
                                    ), null)).getCoordinates();

                            pointVector.addElement(new Coordinate(geoXY[0], geoXY[1]));    

                            // create the feature
                            Feature feature = schema.create(
                                    new Object[]{
                                            geoFactory.createPoint((Coordinate)pointVector.elementAt(n)),
                                            header.getICAO(),
                                            datetime,
                                            new Double(geoXY[1]),
                                            new Double(geoXY[0]),
                                            id.trim(),
                                            new Integer(n*15), 
                                            new Double(range[n]),
                                            new Double(azim[n]),
                                            movedeg.trim(),
                                            movekts.trim()
                                    },
                                    new Integer(geoIndex++).toString());
                            // add to collection
                            features.add(feature);

                        }
                    }
                    // Create Line Feature
                    if (pointVector.size() > 1) { 
                        Coordinate[] lineCoords = new Coordinate[pointVector.size()];
                        Feature feature = lineschema.create(
                                new Object[]{
                                        geoFactory.createLineString((Coordinate[])(pointVector.toArray(lineCoords))),
                                        header.getICAO(),
                                        datetime,
                                        new Double(((Coordinate)pointVector.elementAt(0)).y),
                                        new Double(((Coordinate)pointVector.elementAt(0)).x),
                                        id,
                                        new Integer(-1),
                                        new Double(range[0]),
                                        new Double(azim[0]),
                                        movedeg,
                                        movekts
                                }, 
                                new Integer(lineIndex++).toString());

                        // add to collection
                        lineFeatures.add(feature);
                    }
                }




            }
            else if (header.getVersion() == 1) {

                if (str.trim().startsWith("RADAR ID")) {
                    numStorms = str.substring(70, str.length()).trim();
                }
                if (str.trim().startsWith("AVG SPEED")) {
                    avgStormSpeed = str.substring(28, 32).trim();
                    avgStormDirection = str.substring(52, 57).trim();
                }



//              ----------------- VERSION 1 ---------------------
//              RADAR ID 340  DATE/TIME 11:10:02/22:41:34   NUMBER OF STORM CELLS  54
//              AVG SPEED 43 KTS    AVG DIRECTION 223 DEG
//              ID     AZRAN     MOVEMENT    15 MIN    30 MIN    45 MIN    60 MIN    FCST/MEAN
//              0         1         2         3         4         5         6         7         8
//              01234567890123456789012345678901234567890123456789012345678901234567890123456789012345
//              U6     235/ 70   240/ 45     234/ 59   233/ 48   231/ 37   226/ 25    0.9/ 0.8
//              P3     257/ 41   243/ 46     262/ 30   NO DATA   NO DATA   NO DATA    4.0/ 1.2

                if (str.charAt(2) != ' ' && str.charAt(12) == '/') {
                    //logger.info(hitCount+" "+new String(data));

                    // Do current position
                    azim[0] = Double.parseDouble(str.substring(9, 12));
                    range[0] = Double.parseDouble(str.substring(13, 16));
                    if (str.substring(21, 24).equals("NEW")) {
                        movedeg = "NEW";
                        movekts = "NEW";
                    }
                    else {
                        movedeg = str.substring(19, 22);
                        movekts = str.substring(23, 26);
                    }

                    if (str.substring(31, 38).equals("NO DATA")) {
                        azim[1] = -999.9;
                        range[1] = -999.9;
                    }
                    else {
                        azim[1] = Double.parseDouble(str.substring(31, 34));
                        range[1] = Double.parseDouble(str.substring(35, 38));
                    }

                    if (str.substring(41, 48).equals("NO DATA")) {
                        azim[2] = -999.9;
                        range[2] = -999.9;
                    }
                    else {
                        azim[2] = Double.parseDouble(str.substring(41, 44));
                        range[2] = Double.parseDouble(str.substring(45, 48));
                    }

                    if (str.substring(51, 58).equals("NO DATA")) {
                        azim[3] = -999.9;
                        range[3] = -999.9;
                    }
                    else {
                        azim[3] = Double.parseDouble(str.substring(51, 54));
                        range[3] = Double.parseDouble(str.substring(55, 58));
                    }

                    if (str.substring(61, 68).equals("NO DATA")) {
                        azim[4] = -999.9;
                        range[4] = -999.9;
                    }
                    else {
                        azim[4] = Double.parseDouble(str.substring(61, 64));
                        range[4] = Double.parseDouble(str.substring(65, 68));
                    }

                    String id = str.substring(2, 4);

                    // Create point features
                    pointVector.clear();
                    for (int n = 0; n < 5; n++) {

                        if (azim[n] != -999.9 && range[n] != -999.9) {

                            // Correct for an azim of 0
                            if (azim[n] == 0.0 || azim[n] == 180.0 || azim[n] == 360.0) {
                                azim[n] += 0.000001;
                            }
                            // Convert from nautical mi to meters
                            geoXY = (nexradTransform.transform(
                                    new CoordinatePoint(
                                            range[n] * Math.sin(Math.toRadians(azim[n])) * 1852.0,
                                            range[n] * Math.cos(Math.toRadians(azim[n])) * 1852.0 
                                    ), null)).getCoordinates();

                            pointVector.addElement(new Coordinate(geoXY[0], geoXY[1]));    

                            // create the feature
                            Feature feature = schema.create(
                                    new Object[]{
                                            geoFactory.createPoint((Coordinate)pointVector.elementAt(n)),
                                            header.getICAO(),
                                            datetime,
                                            new Double(geoXY[1]),
                                            new Double(geoXY[0]),
                                            id.trim(),
                                            new Integer(n*15), 
                                            new Double(range[n]),
                                            new Double(azim[n]),
                                            movedeg.trim(),
                                            movekts.trim()
                                    },
                                    new Integer(geoIndex++).toString());
                            // add to collection
                            features.add(feature);

                        }
                    }
                    // Create Line Feature
                    if (pointVector.size() > 1) { 
                        Coordinate[] lineCoords = new Coordinate[pointVector.size()];
                        Feature feature = lineschema.create(
                                new Object[]{
                                        geoFactory.createLineString((Coordinate[])(pointVector.toArray(lineCoords))),
                                        header.getICAO(),
                                        datetime,
                                        new Double(((Coordinate)pointVector.elementAt(0)).y),
                                        new Double(((Coordinate)pointVector.elementAt(0)).x),
                                        id,
                                        new Integer(-1),
                                        new Double(range[0]),
                                        new Double(azim[0]),
                                        movedeg,
                                        movekts
                                }, 
                                new Integer(lineIndex++).toString());

                        // add to collection
                        lineFeatures.add(feature);
                    }
                }


                linenum++;

            }

        }
    }




















    // END METHOD decodeData

    /**
     *  Gets the features attribute of the DecodeStormTracking object
     *
     * @return    The features value
     */
    public FeatureCollection getFeatures() {
        return features;
    }

    /**
     *  Gets the line features attribute of the DecodeStormTracking object
     *
     * @return    The features value
     */
    public FeatureCollection getLineFeatures() {
        return lineFeatures;
    }

    /**
     * Implementation of NexradDecoder
     */
    public void setFeatures(FeatureCollection features) {
        this.features = features;
    }


    private java.awt.geom.Rectangle2D.Double getExtent() {
        return (MaxGeographicExtent.getNexradExtent(header.getLat(), header.getLon()));
    }


}

