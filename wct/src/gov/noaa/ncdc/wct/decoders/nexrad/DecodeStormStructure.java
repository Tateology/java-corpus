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
import org.geotools.pt.CoordinatePoint;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;


/**
 *  Decodes NSS NEXRAD Level-III Storm Structure alphanumeric product.  
 *  
 *  From 2620003J.pdf 22.1:
 *  
 *  "This product shall provide, for each identified storm cell, 
 *  information regarding the structure of the storm cell. This 
 *  product shall be produced from and contain the values that are 
 *  output by the Storm Cell Centroids Algorithm. This product 
 *  shall be updated once per volume scan time. This product shall 
 *  be produced in a tabular alphanumeric format and shall include 
 *  annotations for the product name, radar ID, time and date of 
 *  volume scan, and the total number of identified storm cells. 
 *  Upon user request, all site adaptable parameters identified as 
 *  inputs to the algorithm(s) used to generate data for this product 
 *  shall be available at the alphanumeric display."
 *  
 *
 * @author     steve.ansari
 * @created    September 20, 2004
 */
public class DecodeStormStructure implements DecodeL3Alpha {

    private static final Logger logger = Logger.getLogger(DecodeStormStructure.class.getName());

    private String[] metaLabelString = new String[3];
    private FeatureCollection features = FeatureCollections.newCollection();
    private FeatureType schema = null;
    private GeometryFactory geoFactory = new GeometryFactory();
    private java.awt.geom.Rectangle2D.Double wsrBounds;

    private HashMap<String, Object> decodeHints = new HashMap<String, Object>();

    private DecodeL3Header header;
    private String[] supplementalData = new String[2];

    private WCTProjections nexradProjection = new WCTProjections();

    private String datetime;



    /**
     * Constructor
     *
     * @param  header  Description of the Parameter
     * @throws IOException 
     */
    public DecodeStormStructure(DecodeL3Header header) throws DecodeException, IOException {
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
     * Returns the LineFeatureType -- Always null in this case
     *
     * @return    Always null for this alphanumeric product
     */
    public FeatureType getLineFeatureType() {
        return null;
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
        return org.geotools.styling.StyleBuilder.MARK_CIRCLE;
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
     *  Gets the metaLabel attribute of the DecodeStormStructure object
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
        if (fi.hasNext()) { // only use first and thus strongest reading
            Feature f = fi.next();
            metaLabelString[0] = "MAX ID: " + f.getAttribute("id").toString().trim();
            metaLabelString[1] = "VIL: " + f.getAttribute("vil").toString().trim() + " KG/M2";
            metaLabelString[2] = "MAX REF: " + f.getAttribute("maxref").toString().trim() + " dBZ";
        }
        else {
            metaLabelString[0] = "NO STORM STRUC. PRESENT";
        }

    }

//    @Override
    public Map<String, Object> getDecodeHints() {
        return decodeHints;
    }

//    @Override
    public void setDecodeHint(String hintKey, Object hintValue)
    throws DecodeHintNotSupportedException {
        throw new DecodeHintNotSupportedException("DecodeStructure", hintKey, decodeHints);

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


        MathTransform nexradTransform;
        try {
            // Use Geotools Proj4 implementation to get MathTransform object
            nexradTransform = nexradProjection.getRadarTransform(header);
        } catch (Exception e) {
            throw new DecodeException("PROJECTION TRANSFORM ERROR", header.getDataURL());
        }


        datetime = header.getDate()+header.getHourString()+header.getMinuteString()+header.getSecondString();

        // Set up attribute table
        try {
            AttributeType geom = AttributeTypeFactory.newAttributeType("geom", Point.class);
            AttributeType wsrid = AttributeTypeFactory.newAttributeType("wsrid", String.class, true, 5);
            AttributeType datetime = AttributeTypeFactory.newAttributeType("datetime", String.class, true, 15);
            AttributeType lat = AttributeTypeFactory.newAttributeType("lat", Double.class, true, 10);
            AttributeType lon = AttributeTypeFactory.newAttributeType("lon", Double.class, true, 10);
            AttributeType id = AttributeTypeFactory.newAttributeType("id", String.class, true, 3);
            AttributeType range = AttributeTypeFactory.newAttributeType("range", Double.class, true, 7);
            AttributeType azim = AttributeTypeFactory.newAttributeType("azim", Double.class, true, 7);
            AttributeType basehgt = AttributeTypeFactory.newAttributeType("basehgt", String.class, true, 5);
            AttributeType tophgt = AttributeTypeFactory.newAttributeType("tophgt", String.class, true, 5);
            AttributeType vil = AttributeTypeFactory.newAttributeType("vil", String.class, true, 5);
            AttributeType maxref = AttributeTypeFactory.newAttributeType("maxref", String.class, true, 5);
            AttributeType height = AttributeTypeFactory.newAttributeType("height", String.class, true, 5);
            AttributeType[] attTypes = {geom, wsrid, datetime, lat, lon, id, range, azim, basehgt, tophgt, vil, maxref, height};
            schema = FeatureTypeFactory.newFeatureType(attTypes, "Storm Structure Data");
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Reset GeoData index counter
        int geoIndex = 0;


        try {

            // Decode the text blocks (block 2 and 3)
            DecodeL3AlphaGeneric decoder = new DecodeL3AlphaGeneric();
            decoder.decode(header);

            logger.info("----------- VERSION: "+header.getVersion()+" ------------ \n");
            logger.info("----------- BLOCK 2 ----------- \n"+decoder.getBlock2Text()+"\n------ END BLOCK 2 ------ \n");
            logger.info("----------- BLOCK 3 ----------- \n"+decoder.getBlock3Text()+"\n------ END BLOCK 3 ------ \n");



            // Build text for block 2 data           
            StringBuffer sb = new StringBuffer();
            sb.append("  STORM STRUCTURE SUPPLEMENTAL DATA 1\n\n");                  
            sb.append("    NOT APPLICABLE\n\n");
            supplementalData[0] = sb.toString();
            sb.append("\n\n");

            // Build text for block 3 data
            sb = new StringBuffer();
            sb.append("  STORM STRUCTURE SUPPLEMENTAL DATA 2\n\n");
            sb.append("  ABBREVIATIONS:\n");
            sb.append("  AZ      = Azimuth Angle From Radar \n");
            sb.append("            (In Degrees where 0 deg = North, 90 = East, 180 = South, etc...)\n");
            sb.append("  RAN     = Range (Distance) From Radar (In Nautical Miles, Max=124)\n");
            sb.append("  BASE    = Elevation of Storm Base (kft)\n");
            sb.append("  TOP     = Elevation of Storm Top (kft)\n");
            sb.append("  VIL     = Vertically Integrated Liquid (kg/m2)\n");
            sb.append("  MAX REF = Max Reflectivity of Storm Cell (dBZ)\n");
            sb.append("  HEIGHT  = Height of Storm (kft)\n\n");

            sb.append(decoder.getBlock3Text());
            sb.append("\n\n");
            supplementalData[1] = sb.toString();

            String block3Text = decoder.getBlock3Text();
            String[] lines = block3Text.split("\n");

            if (lines.length == 0) {
                metaLabelString[0] = "NO STORMS PRESENT";
                return;      
            }

            logger.info("FOUND VERSION "+header.getVersion()+" DATA");

            if (header.getVersion() > 1.0) {
                throw new DecodeException("UNKNOWN NEXRAD STORM STRUCTURE FILE VERSION: " + header.getVersion(), header
                        .getDataURL());
            }

            boolean lineSwitch = false;
            for (int n = 0; n < lines.length; n++) {

                String str = lines[n];
                // advance past empty lines
                if (str.trim().length() == 0) {
                    continue;
                }

                logger.info("n="+n+" STRING DUMP: "+str);

                if (header.getVersion() == 0) {

                    if (lines[n].startsWith("  ID X NM Y NM  KFT")) {
                        lineSwitch = true;
                        continue; // skip to next line
                    }
                    if (lineSwitch && lines[n].trim().length() > 0 && lines[n].trim().length() < 70) {
                        lineSwitch = false;
                        continue; // skip to next line
                    }

                    if (lineSwitch && str.trim().length() > 70) {


                        //0         1         2         3         4         5         6         7         8
                        //012345678901234567890123456789012345678901234567890123456789012345678901234567890
                        // ------------ VERSION 0 -----------------------------
                        // STM CTRD CTRD BASE   TOP  VOL   TILT(DEG)   OVH ORI MAXZ HGT MAX SW HGT LOW V  
                        //  ID X NM Y NM  KFT   KFT  NM*3 TOT   X   Y   NM DEG DBZ  KFT   KT   KFT   KT                                                                                   
                        //  62  148   84 29.2  44.8  4221  65  52  60 -2.0 216  59 29.2    0  29.2    0  

                        double stormCenterX = Double.parseDouble(str.substring(6, 9));
                        double stormCenterY = Double.parseDouble(str.substring(11, 14));
                        double range = Math.sqrt(stormCenterX * stormCenterX + stormCenterY * stormCenterY);
                        double azim = 90 - Math.toDegrees(Math.sin(stormCenterY / range));

                        // Convert from nautical mi to lat/lon
                        double[] geoXY = (nexradTransform.transform(new CoordinatePoint(range
                                * Math.sin(Math.toRadians(azim)) * 1852.0, range * Math.cos(Math.toRadians(azim))
                                * 1852.0), null)).getCoordinates();

                        try {
                            //logger.info(coords[n]);
                            // create the feature
                            //AttributeType[] attTypes = {geom, lat, lon, id, range, azim, basehgt, tophgt, vil, maxref, height};
                            Feature feature = schema.create(new Object[] {
                                    geoFactory.createPoint(new Coordinate(geoXY[0], geoXY[1])), header.getICAO(),
                                    datetime, new Double(geoXY[1]), new Double(geoXY[0]), str.substring(1, 4).trim(),
                                    new Double(range), new Double(azim), str.substring(15, 19).trim(),
                                    str.substring(21, 25).trim(), "N/A", str.substring(53, 56).trim(),
                                    str.substring(57, 61).trim() }, new Integer(geoIndex++).toString());
                            // add to collection
                            features.add(feature);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                //0         1         2         3         4         5         6         7         8
                //012345678901234567890123456789012345678901234567890123456789012345678901234567890
                // ------------ VERSION 1 -----------------------------
                //   STORM      AZRAN      BASE     TOP    CELL BASED VIL    MAX REF    HEIGHT
                //     ID      DEG/NM       KFT     KFT       KG/M**2          DBZ        KFT
                //     U4      183/ 18    < 1.4     8.6          17             55        4.9
                else if (header.getVersion() == 1) {
                    //if (linecnt > 6 && n < numPages - 3 && str.trim().length() > 0) {
                    if (str.charAt(16) == '/' && ! str.substring(13, 19).equals("DEG/NM")) {

//                      logger.info("FOUND LINE OF VERSION 1 DATA");

                        double azim = Double.parseDouble(str.substring(13, 16));
                        double range = Double.parseDouble(str.substring(17, 20));

                        // Correct for an azim of 0
                        if (azim == 0.0 || azim == 180.0 || azim == 360.0) {
                            azim += 0.000001;
                        }
                        // Convert from nautical mi to lat/lon
                        double[] geoXY = (nexradTransform.transform(new CoordinatePoint(range
                                * Math.sin(Math.toRadians(azim)) * 1852.0, range * Math.cos(Math.toRadians(azim))
                                * 1852.0), null)).getCoordinates();

                        try {
                            //logger.info(coords[n]);
                            // create the feature
                            Feature feature = schema.create(new Object[] {
                                    geoFactory.createPoint(new Coordinate(geoXY[0], geoXY[1])), header.getICAO(),
                                    datetime, new Double(geoXY[1]), new Double(geoXY[0]), str.substring(5, 7).trim(),
                                    new Double(range), new Double(azim), str.substring(23, 29).trim(),
                                    str.substring(31, 37).trim(), str.substring(46, 49).trim(),
                                    str.substring(61, 64).trim(), str.substring(70, 75).trim() }, new Integer(
                                            geoIndex++).toString());
                            // add to collection
                            features.add(feature);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
                else {
                    throw new DecodeException("UNKNOWN NEXRAD STORM STRUCTURE FILE VERSION: " + header.getVersion(), header
                            .getDataURL());
                }

            }
            makeMetaLabelStrings();
            return;
        } // END try
        catch (Exception e) {
            e.printStackTrace();
            throw new DecodeException("DECODE EXCEPTION IN STORM STRUCTURE FILE", header.getDataURL());
        }
    }
    // END METHOD decodeData



    /**
     *  Gets the features attribute of the DecodeHail object
     *
     * @return    The features value
     */
    public FeatureCollection getFeatures() {
        return features;
    }



    /**
     *  Gets the line features attribute of the DecodeStormStructure object
     *
     * @return    The features value
     */
    public FeatureCollection getLineFeatures() {
        return null;
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

