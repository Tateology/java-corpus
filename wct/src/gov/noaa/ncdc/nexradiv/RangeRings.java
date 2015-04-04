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

package gov.noaa.ncdc.nexradiv;

import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.nexrad.WCTProjections;

import java.text.DecimalFormat;

import org.geotools.ct.MathTransform;
import org.geotools.data.shapefile.shp.JTSUtilities;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.SchemaException;
import org.geotools.pt.CoordinatePoint;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

/**
 *  Creates RangeRing Geometries and Features
 *
 * @author    steve.ansari
 */
public class RangeRings {

    public final static int KM = 0;
    public final static int MILES = 1;
    public final static int NAUTICAL_MI = 2;

    public final static String[] unitsString = new String[] {"km", "mi", "nmi"};


    /**
     *  Attributes for Range Rings
     */
    public final static AttributeType[] RANGERING_ATTRIBUTES = {
        AttributeTypeFactory.newAttributeType("geom", Geometry.class),
        AttributeTypeFactory.newAttributeType("distance", Float.class),
        AttributeTypeFactory.newAttributeType("azimuth", Float.class),
        AttributeTypeFactory.newAttributeType("label", String.class)
    };

    /**
     *  Attributes for Range Bins
     */
    public final static AttributeType[] RANGEBIN_ATTRIBUTES = {
        AttributeTypeFactory.newAttributeType("geom", Geometry.class),
        AttributeTypeFactory.newAttributeType("beg_dist", Float.class),
        AttributeTypeFactory.newAttributeType("end_dist", Float.class),
        AttributeTypeFactory.newAttributeType("beg_azim", Float.class),
        AttributeTypeFactory.newAttributeType("end_azim", Float.class),
        AttributeTypeFactory.newAttributeType("label", String.class)
    };


    /**
     *  Description of the Field
     */
    public final static double DEFAULT_SEGMENT_AZIMUTH = 1.0;

    private final static GeometryFactory geoFactory = new GeometryFactory();



    /**
     *  Gets a rangeRing geometry at a specified distance and segment angle deltas (360 degree ring)
     *
     * @param  center          Coordinate value for lon and lat of radar site
     * @param  distance        Distance of ring from center coordinate in meters
     * @return                 The rangeRing geometry
     */
    public static LineString getRingLineString(Coordinate center, double distance) {
        return getRingLineString(center, distance, 0.0, 360.0, DEFAULT_SEGMENT_AZIMUTH);
    }


    /**
     *  Gets a rangeRing geometry at a specified distance and segment angle deltas (360 degree ring)
     *
     * @param  center          Coordinate value for lon and lat of radar site
     * @param  distance        Distance of ring from center coordinate in meters
     * @param  segmentAzimuth  Azimuth delta to use when creating the ring (larger == course ring)
     * @return                 The rangeRing geometry
     */
    public static LineString getRingLineString(Coordinate center, double distance, double segmentAzimuth) {
        return getRingLineString(center, distance, 0.0, 360.0, segmentAzimuth);
    }


    /**
     *  Gets a rangeRing geometry at a specified distance and segment angle deltas
     *
     * @param  center          Coordinate value for lon and lat of radar site
     * @param  distance        Distance of ring from center coordinate in meters
     * @param  segmentAzimuth  Azimuth delta to use when creating the ring (larger == course ring)
     * @param  startAzimuth    Description of the Parameter
     * @param  endAzimuth      Description of the Parameter
     * @return                 The rangeRing geometry
     */
    public static LineString getRingLineString(Coordinate center, double distance,
            double startAzimuth, double endAzimuth, double segmentAzimuth) {

        try {
            Coordinate[] coords = getRingCoordinates(center, distance, startAzimuth, endAzimuth, segmentAzimuth, false);
            LineString ls = geoFactory.createLineString(coords);
            return ls;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     *  Gets a rangeRing geometry at a specified distance and segment angle deltas (360 degree ring)
     *
     * @param  center          Coordinate value for lon and lat of radar site
     * @param  distance        Distance of ring from center coordinate in meters
     * @return                 The rangeRing geometry
     */
    public static LinearRing getLinearRing(Coordinate center, double distance) {
        return getLinearRing(center, distance, DEFAULT_SEGMENT_AZIMUTH);
    }


    /**
     *  Gets a rangeRing geometry at a specified distance and segment angle deltas (360 degree ring)
     *
     * @param  center          Coordinate value for lon and lat of radar site
     * @param  distance        Distance of ring from center coordinate in meters
     * @param  segmentAzimuth  Azimuth delta to use when creating the ring (larger == course ring)
     * @return                 The rangeRing geometry
     */
    public static LinearRing getLinearRing(Coordinate center, double distance, double segmentAzimuth) {

        try {

            Coordinate[] coords = getRingCoordinates(center, distance, 0.0, 360.0, segmentAzimuth, true);
            LinearRing lr = geoFactory.createLinearRing(coords);
            return lr;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     *  Gets the spokeLineString attribute of the RangeRings class
     *
     * @param  center       Description of the Parameter
     * @param  minDistance  Description of the Parameter
     * @param  maxDistance  Description of the Parameter
     * @param  azimuth      Description of the Parameter
     * @return              The spokeLineString value
     */
    public static LineString getSpokeLineString(Coordinate center, double minDistance,
            double maxDistance, double azimuth) {

        try {
            Coordinate[] coords = getSpokeCoordinates(center, minDistance, maxDistance, azimuth);
            LineString ls = geoFactory.createLineString(coords);
            return ls;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


    /**
     *  Gets coordinate array at a specified distance and segment angle deltas
     *
     * @param  center          Coordinate value for lon and lat of radar site
     * @param  distance        Distance of ring from center coordinate in meters
     * @param  segmentAzimuth  Azimuth delta to use when creating the ring (larger == course ring)
     * @param  startAzimuth    Start Azimuth Angle
     * @param  endAzimuth      End Azimuth Angle
     * @param  isRing          Add first point as last to create ring?
     * @return                 The rangeRing geometry
     */
    public static Coordinate[] getRingCoordinates(Coordinate center, double distance,
            double startAzimuth, double endAzimuth, double segmentAzimuth, boolean isRing) {

        // Create a custom Albers Equal Area projection where WSR is at 0,0
        try {
            WCTProjections nexradProjection = new WCTProjections();               
            MathTransform nexradTransform;
            try {
                // Use Geotools Proj4 implementation to get MathTransform object
                nexradTransform = nexradProjection.getRadarTransform(center.x, center.y);
            } catch (Exception e) {
                throw new DecodeException("PROJECTION TRANSFORM ERROR - RANGERINGS ( "+center.x+" , "+center.y+" )", null);
            }





            double deltaAzimuth = Math.abs(startAzimuth - endAzimuth);

            int numberOfSegments = (int) (deltaAzimuth / segmentAzimuth);
            Coordinate[] coords = new Coordinate[numberOfSegments + 1];

            double xpos;

            double ypos;
            double[] geoXY;
            double angle;
            // Loop through 360 degrees with delta == segmentAzimuth
            for (int n = 0; n < numberOfSegments + 1; n++) {
                angle = n * segmentAzimuth + startAzimuth;
                if (angle % 90 == 0) {
                    angle += 0.00001;
                }
                angle = Math.toRadians(angle);
                xpos = distance * Math.sin(angle);
                ypos = distance * Math.cos(angle);
                //geoXY = customAlbers.convert(xpos, ypos);
                geoXY = (nexradTransform.transform(new CoordinatePoint(xpos, ypos), null)).getCoordinates();
                coords[n] = new Coordinate(geoXY[0], geoXY[1]);
            }



            //System.out.println("DELTA AZIMUTH: "+deltaAzimuth);       

            // Add first coordinate to close ring
            // This is commented out because it caused problems when using <360 deg. deltaAzimuth

            //coords[numberOfSegments] = coords[0];

            return coords;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     *  Gets coordinate array for spoke at a specified distances and azimuth
     *
     * @param  center       Coordinate value for lon and lat of radar site
     * @param  minDistance  Min Distance of ring from center coordinate in meters
     * @param  maxDistance  Max Distance of ring from center coordinate in meters
     * @param  azimuth      Azimuth Angle
     * @return              The spoke LineString
     */
    public static Coordinate[] getSpokeCoordinates(Coordinate center, double minDistance,
            double maxDistance, double azimuth) {

        // Create a custom Albers Equal Area projection where WSR is at 0,0
        try {
            //gov.noaa.ncdc.projections.Albers2LatLon customAlbers =
            //      new gov.noaa.ncdc.projections.Albers2LatLon(center.y + 1.0, center.y - 1.0,
            //      center.y, center.x, 1.0);


            WCTProjections nexradProjection = new WCTProjections();               
            MathTransform nexradTransform;
            try {
                // Use Geotools Proj4 implementation to get MathTransform object
                nexradTransform = nexradProjection.getRadarTransform(center.x, center.y);
            } catch (Exception e) {
                throw new DecodeException("PROJECTION TRANSFORM ERROR - RANGERINGS ( "+center.x+" , "+center.y+" )", null);
            }



            Coordinate[] coords = new Coordinate[2];

            double xpos;

            double ypos;
            double[] geoXY;
            double angle;

            if (azimuth % 90 == 0) {
                azimuth += 0.00001;
            }

            azimuth = Math.toRadians(azimuth);
            xpos = minDistance * Math.sin(azimuth);
            ypos = minDistance * Math.cos(azimuth);
            //geoXY = customAlbers.convert(xpos, ypos);
            geoXY = (nexradTransform.transform(new CoordinatePoint(xpos, ypos), null)).getCoordinates();
            coords[0] = new Coordinate(geoXY[0], geoXY[1]);
            xpos = maxDistance * Math.sin(azimuth);
            ypos = maxDistance * Math.cos(azimuth);
            //geoXY = customAlbers.convert(xpos, ypos);
            geoXY = (nexradTransform.transform(new CoordinatePoint(xpos, ypos), null)).getCoordinates();
            coords[1] = new Coordinate(geoXY[0], geoXY[1]);

            return coords;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     *  Gets a rangeRing geometry at a specified distance with DEFAULT_SEGMENT_AZIMUTH
     *
     * @param  center    Coordinate value for lon and lat of radar site
     * @param  distance  Distance of ring from center coordinate in meters
     * @return           The Geometry representing the ring
     */
    public static Polygon getRangeRingFilledPolygon(Coordinate center, double distance) {
        return getRangeRingFilledPolygon(center, distance, DEFAULT_SEGMENT_AZIMUTH);
    }


    /**
     *  Gets a rangeRing geometry at a specified distance and segment angle deltas
     *
     * @param  center          Coordinate value for lon and lat of radar site
     * @param  distance        Distance of ring from center coordinate in meters
     * @param  segmentAzimuth  Azimuth delta to use when creating the ring (larger == course ring)
     * @return                 The rangeRing geometry
     */
    public static Polygon getRangeRingFilledPolygon(Coordinate center, double distance, double segmentAzimuth) {

        try {
            LinearRing lr = getLinearRing(center, distance, segmentAzimuth);
            Polygon poly = JTSUtilities.makeGoodShapePolygon(geoFactory.createPolygon(lr, null));
            return poly;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     *  Gets a rangeRing (single closed polygon) geometry at the specified distances (with hole from 0-minDistance)
     *  with DEFAULT_SEGMENT_AZIMUTH
     *
     * @param  center       Coordinate value for lon and lat of radar site
     * @param  minDistance  Min Distance of ring from center coordinate in meters
     * @param  maxDistance  Max Distance of ring from center coordinate in meters
     * @return              The Geometry representing the ring
     */
    public static Polygon getRangePolygon(Coordinate center, double minDistance, double maxDistance) {
        return getRangePolygon(center, minDistance, maxDistance, DEFAULT_SEGMENT_AZIMUTH);
    }


    /**
     *  Gets a rangeRing (single closed polygon) geometry at the specified distances
     *  and segment angle deltas
     *
     * @param  center          Coordinate value for lon and lat of radar site
     * @param  minDistance     Min Distance of ring from center coordinate in meters
     * @param  maxDistance     Max Distance of ring from center coordinate in meters
     * @param  segmentAzimuth  Azimuth delta to use when creating the ring (larger == course ring)
     * @return                 The rangeRing geometry
     */
    public static Polygon getRangePolygon(Coordinate center, double minDistance,
            double maxDistance, double segmentAzimuth) {

        try {

            LinearRing lrShell = getLinearRing(center, maxDistance, segmentAzimuth);
            LinearRing lrHole = getLinearRing(center, minDistance, segmentAzimuth);
            Polygon poly = JTSUtilities.makeGoodShapePolygon(
                    geoFactory.createPolygon(lrShell, new LinearRing[]{lrHole})
            );

            return poly;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     *  Gets the rangeRingFeatures attribute of the RangeRings class
     *
     * @param  center             Description of the Parameter
     * @param  minDistance        Description of the Parameter
     * @param  maxDistance        Description of the Parameter
     * @param  ringIncrement      Description of the Parameter
     * @param  rangeRingFeatures  Description of the Parameter
     * @return                    The rangeRingFeatures value
     */
    public static FeatureCollection getRangeRingFeatures(
            Coordinate center,
            double minDistance,
            double maxDistance,
            double ringIncrement,
            int distUnits,
            FeatureCollection rangeRingFeatures) {

        return getRangeRingFeatures(center, minDistance, maxDistance,
                ringIncrement, distUnits, rangeRingFeatures, DEFAULT_SEGMENT_AZIMUTH);
    }


    /**
     *  Gets the rangeRingFeatures attribute of the RangeRings class
     *
     * @param  center             Description of the Parameter
     * @param  minDistance        Description of the Parameter
     * @param  maxDistance        Description of the Parameter
     * @param  ringIncrement      Description of the Parameter
     * @param  rangeRingFeatures  Description of the Parameter
     * @param  segmentAzimuth     Description of the Parameter
     * @return                    The rangeRingFeatures value
     */
    public static FeatureCollection getRangeRingFeatures(
            Coordinate center,
            double minDistance,
            double maxDistance,
            double ringIncrement,
            int distUnits,
            FeatureCollection rangeRingFeatures,
            double segmentAzimuth) {

        return getRangeRingFeatures(center, minDistance, maxDistance, ringIncrement,
                distUnits, 0.0, 360.0, -1.0, rangeRingFeatures, segmentAzimuth);
    }


    /**
     *  Gets the rangeRingFeatures attribute of the RangeRings class
     *
     * @param  center             Description of the Parameter
     * @param  minDistance        Description of the Parameter
     * @param  maxDistance        Description of the Parameter
     * @param  ringIncrement      Description of the Parameter
     * @param  startAzimuth       Description of the Parameter
     * @param  endAzimuth         Description of the Parameter
     * @param  spokeIncrement     Description of the Parameter
     * @param  rangeRingFeatures  Description of the Parameter
     * @return                    The rangeRingFeatures value
     */
    public static FeatureCollection getRangeRingFeatures(
            Coordinate center,
            double minDistance,
            double maxDistance,
            double ringIncrement,
            int distUnits,
            double startAzimuth,
            double endAzimuth,
            double spokeIncrement,
            FeatureCollection rangeRingFeatures) {

        return getRangeRingFeatures(center, minDistance, maxDistance, ringIncrement,
                distUnits, startAzimuth, endAzimuth, spokeIncrement,
                rangeRingFeatures, RangeRings.DEFAULT_SEGMENT_AZIMUTH);
    }


    /**
     *  Gets the rangeRingFeatures attribute of the RangeRings class
     *
     * @param  center             Description of the Parameter
     * @param  minDistance        Description of the Parameter
     * @param  maxDistance        Description of the Parameter
     * @param  ringIncrement      Description of the Parameter
     * @param  startAzimuth       Description of the Parameter
     * @param  endAzimuth         Description of the Parameter
     * @param  spokeIncrement     Description of the Parameter
     * @param  rangeRingFeatures  Description of the Parameter
     * @param  segmentAzimuth     Description of the Parameter
     * @return                    The rangeRingFeatures value
     */
    public static FeatureCollection getRangeRingFeatures(
            Coordinate center,
            double minDistance,
            double maxDistance,
            double ringIncrement,
            int distUnits,
            double startAzimuth,
            double endAzimuth,
            double spokeIncrement,
            FeatureCollection rangeRingFeatures,
            double segmentAzimuth) {

        try {
            rangeRingFeatures.clear();

            if (minDistance < 0.0 || maxDistance <= 0.0 || ringIncrement <= 0.0) {
                return rangeRingFeatures;
            }

            // Convert to meters and keep ratio
            double ratio;
            if (distUnits == KM) {
                ratio=1000.0;
            }
            else if (distUnits == MILES) {
                ratio=1609.24;
            }
            else if (distUnits == NAUTICAL_MI) {
                ratio=1853.18;
            }
            else {
                return rangeRingFeatures;
            }


            FeatureType ring_schema = FeatureTypeFactory.newFeatureType(RANGERING_ATTRIBUTES, "Range Ring Attributes");

            DecimalFormat fmt1 = new DecimalFormat("0.0");
            DecimalFormat fmt2 = new DecimalFormat("0.00");
            int geoIndex = 0;
            int numRings = (int) ((maxDistance - minDistance) / ringIncrement);

            System.out.println("RANGERINGS: CREATING " + numRings + " RANGE RINGS");

            Feature feature = null;

            for (int n = 0; n < numRings+1; n++) {
                double distance = (minDistance + (n * ringIncrement))*ratio; // convert to meters
                Geometry ring = getRingLineString(center, distance, startAzimuth, endAzimuth, segmentAzimuth);

                feature = ring_schema.create(new Object[]{
                        ring,
                        new Float((float) distance/ratio),
                        new Float(-1f),
                        new String(fmt2.format(distance/ratio) + " " + unitsString[distUnits])
                }, new Integer(geoIndex++).toString());

                // add to collection
                rangeRingFeatures.add(feature);
            }

            // Add maximum ring
            Geometry ring = getRingLineString(center, maxDistance*ratio, startAzimuth, endAzimuth, segmentAzimuth);
            feature = ring_schema.create(new Object[]{
                    ring,
                    new Float((float) maxDistance),
                    new Float(-1f),
                    new String(fmt2.format(maxDistance) + " " + unitsString[distUnits])
            }, new Integer(geoIndex++).toString());
            rangeRingFeatures.add(feature);

            // Add spokes
            double deltaAzimuth = Math.abs(startAzimuth - endAzimuth);
            int numSpokes;
            if (spokeIncrement <= 0) {
                numSpokes = 1;  
            }
            else {
                numSpokes = (int) (deltaAzimuth / spokeIncrement);
            }

            for (int n = 0; n < numSpokes+1; n++) {

                double curAzimuth = startAzimuth + n * spokeIncrement;

                String label;
                if (curAzimuth == 360.0 && startAzimuth == 0.0) {
                    label = "";
                } else {
                    label = fmt2.format(curAzimuth) + " Degrees";
                }

                Geometry spoke = getSpokeLineString(center, minDistance*ratio,
                        ratio*maxDistance, curAzimuth);
                feature = ring_schema.create(new Object[]{
                        spoke,
                        new Float(-1f),
                        new Float((float) curAzimuth),
                        label
                }, new Integer(geoIndex++).toString());
                rangeRingFeatures.add(feature);
            }
            // Add maximum spoke
            Geometry spoke = getSpokeLineString(center, ratio*minDistance,
                    ratio*maxDistance, endAzimuth);
            // Don't put label on 360.0 -- will overlap 0.0 label               
            String label;
            if (endAzimuth == 360.0) {
                label = "";
            } else {
                label = fmt2.format(endAzimuth) + " Degrees";
            }


            feature = ring_schema.create(new Object[]{
                    spoke,
                    new Float(-1f),
                    new Float((float) endAzimuth),
                    label
            }, new Integer(geoIndex++).toString());
            rangeRingFeatures.add(feature);


        } catch (Exception e) {
            e.printStackTrace();
        }

        return rangeRingFeatures;
    }










    /**
     *  Gets the rangeRingFeatureType attribute of the RangeRings class
     *
     * @return                      The rangeRingFeatureType value
     * @exception  SchemaException  Description of the Exception
     */
    public static FeatureType getRangeRingFeatureType()
    throws SchemaException {

        return FeatureTypeFactory.newFeatureType(RANGERING_ATTRIBUTES, "Range Ring Attributes");
    }


















    /**
     * Gets coordinates for a single polygon range bin.
     * @param center
     * @param minDistance
     * @param maxDistance
     * @param startAzimuth
     * @param endAzimuth
     * @param segmentAzimuth
     * @return
     */
    public static Coordinate[] getRangeBinCoordinates(
            Coordinate center, 
            double minDistance,
            double maxDistance, 
            double startAzimuth, 
            double endAzimuth, 
            double segmentAzimuth) {


        Coordinate[] farRingSegmentCoords = getRingCoordinates(center, maxDistance, startAzimuth, endAzimuth, segmentAzimuth, false);
        Coordinate[] closeRingSegmentCoords = getRingCoordinates(center, minDistance, startAzimuth, endAzimuth, segmentAzimuth, false);

        // reorder and combine coordinates (+1 to close range bin)
        Coordinate[] rangeBinCoords = new Coordinate[farRingSegmentCoords.length + closeRingSegmentCoords.length + 1]; 

        for (int n=0; n<farRingSegmentCoords.length; n++) {
            rangeBinCoords[n] = farRingSegmentCoords[n];
        }
        for (int n=0; n<closeRingSegmentCoords.length; n++) {
            rangeBinCoords[n+farRingSegmentCoords.length] = closeRingSegmentCoords[closeRingSegmentCoords.length-n-1];
        }
        // close range bin
        rangeBinCoords[rangeBinCoords.length-1] = farRingSegmentCoords[0];

        return rangeBinCoords;
    }

    /**
     *  Gets the rangeRingFeatures attribute of the RangeRings class
     *
     * @param  center             Description of the Parameter
     * @param  minDistance        Description of the Parameter
     * @param  maxDistance        Description of the Parameter
     * @param  ringIncrement      Description of the Parameter
     * @param  startAzimuth       Description of the Parameter
     * @param  endAzimuth         Description of the Parameter
     * @param  spokeIncrement     Description of the Parameter
     * @param  rangeRingFeatures  Description of the Parameter
     * @param  segmentAzimuth     Description of the Parameter
     * @return                    The rangeRingFeatures value
     */
    public static FeatureCollection getRangeBinFeatures(
            Coordinate center,
            double minDistance,
            double maxDistance,
            double ringIncrement,
            int distUnits,
            double minAzimuth,
            double maxAzimuth,
            double spokeIncrement,
            FeatureCollection rangeBinFeatures,
            double segmentAzimuth) {

        try {
            rangeBinFeatures.clear();

            if (minDistance < 0.0 || maxDistance <= 0.0 || ringIncrement <= 0.0) {
                return rangeBinFeatures;
            }

            // Convert to meters and keep ratio
            double ratio;
            if (distUnits == KM) {
                ratio=1000.0;
            }
            else if (distUnits == MILES) {
                ratio=1609.24;
            }
            else if (distUnits == NAUTICAL_MI) {
                ratio=1853.18;
            }
            else {
                return rangeBinFeatures;
            }


            //FeatureType ring_schema = FeatureTypeFactory.newFeatureType(RANGEBIN_ATTRIBUTES, "Range Bin Attributes");
            FeatureType ring_schema = FeatureTypeFactory.newFeatureType(RANGERING_ATTRIBUTES, "Range Bin Attributes");

            DecimalFormat fmt1 = new DecimalFormat("0.0");
            DecimalFormat fmt2 = new DecimalFormat("0.00");
            int geoIndex = 0;
            int numRings = (int) ((maxDistance - minDistance) / ringIncrement);

            // Add spokes
            double deltaAzimuth = Math.abs(minAzimuth - maxAzimuth);
            int numSpokes;
            if (spokeIncrement <= 0) {
                numSpokes = 1;  
            }
            else {
                numSpokes = (int) (deltaAzimuth / spokeIncrement);
            }


            System.out.println("RANGERINGS: CREATING " + numRings + "/"+numSpokes+" RANGE RINGS");

            Feature feature = null;



            for (int n = 0; n < numRings+1; n++) {
                double startDistance = (minDistance + (n * ringIncrement))*ratio; // convert to meters
                double endDistance = (minDistance + ((n+1) * ringIncrement))*ratio; // convert to meters
                System.out.println("processing range increment "+n);


                for (int s = 0; s < numSpokes+1; s++) {
                    double startAzimuth = minAzimuth + s * spokeIncrement;
                    double endAzimuth = minAzimuth + (s+1) * spokeIncrement;

//                    System.out.println("processing spoke "+s);
//                  for (int n = 0; n < numRings+1; n++) {
//                  double startDistance = (minDistance + (n * ringIncrement))*ratio; // convert to meters
//                  double endDistance = (minDistance + ((n+1) * ringIncrement))*ratio; // convert to meters
                    //Geometry ring = getRingLineString(center, distance, startAzimuth, endAzimuth, segmentAzimuth);

                    LinearRing ring = geoFactory.createLinearRing(
                            getRangeBinCoordinates(center, startDistance, endDistance, 
                                    startAzimuth, endAzimuth, segmentAzimuth)
                    );

                    Geometry rangeBin = geoFactory.createPolygon(ring, null);

                    //System.out.println("CREATING RANGE RING FOR: "+startDistance+" , "+startAzimuth);

                    feature = ring_schema.create(new Object[]{
                            rangeBin,
                            new Float(fmt2.format(((startDistance+endDistance)/2.0)/ratio)),
                            new Float(fmt2.format((startAzimuth+endAzimuth)/2.0)),
                            new String(fmt2.format(((startDistance+endDistance)/2.0)/ratio) + " " + (startAzimuth+endAzimuth)/2.0)
                    }, new Integer(geoIndex++).toString());

                    // add to collection
                    rangeBinFeatures.add(feature);
                }
            }

            // Add maximum ring
//          Geometry ring = getRingLineString(center, maxDistance*ratio, startAzimuth, endAzimuth, segmentAzimuth);
//          feature = ring_schema.create(new Object[]{
//          ring,
//          new Float((float) maxDistance),
//          new Float(-1f),
//          new String(fmt2.format(maxDistance) + " " + unitsString[distUnits])
//          }, new Integer(geoIndex++).toString());
//          rangeBinFeatures.add(feature);




        } catch (Exception e) {
            e.printStackTrace();
        }

        return rangeBinFeatures;
    }





}

