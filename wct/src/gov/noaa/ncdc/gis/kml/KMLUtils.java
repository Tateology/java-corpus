package gov.noaa.ncdc.gis.kml;

import java.awt.geom.Rectangle2D;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

public class KMLUtils {

    
    public static String getExtentPolyKML(Rectangle2D extent) throws Exception {
        Envelope env = new Envelope(extent.getMinX(), extent.getMaxX(), extent.getMinY(), extent.getMaxY());
        GeometryFactory geoFactory = new GeometryFactory();
        KMLGeometryConverter kmlConv = new KMLGeometryConverter();
        String kmlGeom = kmlConv.processGeometry(geoFactory.toGeometry(env));
        
        return kmlGeom;
    }
    
    
    
    
    public static String getExtentLineKML(Rectangle2D extent) throws Exception {
    	return getExtentLineKML(extent, Double.NaN);
    }
    
    /**
     * 
     * @param extent
     * @param height  - use Double.NaN if there is no height value
     * @return
     * @throws Exception
     */
    public static String getExtentLineKML(Rectangle2D extent, double height) throws Exception {
        
        // if bounds contains latitude values > abs(90), then truncate to 90
//        System.out.println("bounds before: "+extent);
        extent = extent.createIntersection(new Rectangle2D.Double(2*-180.0, -90, 4*360, 180));
//        System.out.println("bounds after: "+extent);
        
        KMLGeometryConverter kmlConv = new KMLGeometryConverter();
        kmlConv.setSupplementalTags("<tessellate>1</tessellate>\n");
        
        if (! Double.isNaN(height)) {
        	kmlConv.appendSupplementalTags("<altitudeMode>absolute</altitudeMode>");
        }
        
        int numXSections = 20;
        if (extent.getWidth() / numXSections > 4) {
            numXSections = (int)Math.round(extent.getWidth() / 4);
        }
        double dx = (double)extent.getWidth()/numXSections;
        
//        Envelope env = new Envelope(extent.getMinX(), extent.getMaxX(), extent.getMinY(), extent.getMaxY());
        GeometryFactory geoFactory = new GeometryFactory();
        
        Coordinate[] coords;
        if (extent.getWidth() == 360 && extent.getMinY() == -90) {
            coords = new Coordinate[numXSections+1]; 
            for (int n=0; n<numXSections; n++) {
                coords[n] = new Coordinate(extent.getMinX()+n*dx, extent.getMaxY(), height);
            } 
            // add first point again to complete circle
            coords[numXSections] = new Coordinate(extent.getMinX(), extent.getMaxY(), height);
        }
        else if (extent.getWidth() == 360 && extent.getMaxY() == 90) {
            coords = new Coordinate[numXSections+1]; 
            for (int n=0; n<numXSections; n++) {
                coords[n] = new Coordinate(extent.getMinX()+n*dx, extent.getMinY(), height);
            } 
            // add first point again to complete circle
            coords[numXSections] = new Coordinate(extent.getMinX(), extent.getMinY(), height);
        }
        else if (extent.getWidth() == 360) {
            StringBuilder sb = new StringBuilder();
            
            coords = new Coordinate[numXSections+1]; 
            for (int n=0; n<numXSections; n++) {
                coords[n] = new Coordinate(extent.getMinX()+n*dx, extent.getMaxY(), height);
            } 
            // add first point again to complete circle
            coords[numXSections] = new Coordinate(extent.getMinX(), extent.getMaxY(), height);

            // add line string for upper ring
            LineString upperLineString = geoFactory.createLineString(coords);
            
            
            coords = new Coordinate[numXSections+1]; 
            for (int n=0; n<numXSections; n++) {
                coords[n] = new Coordinate(extent.getMinX()+n*dx, extent.getMinY(), height);
            } 
            // add first point again to complete circle
            coords[numXSections] = new Coordinate(extent.getMinX(), extent.getMinY(), height);

            // add line string for lower ring
            LineString lowerLineString = geoFactory.createLineString(coords);
            
            sb.append(kmlConv.processGeometry(geoFactory.createMultiLineString(new LineString[] { upperLineString, lowerLineString })));
            
            return sb.toString();

        }
        else {
            
            coords = new Coordinate[2*numXSections+3];
            coords[0] = new Coordinate(scrubXCoord(extent.getMinX()), extent.getMinY(), height);
            for (int n=0; n<numXSections; n++) {
                coords[n+1] = new Coordinate(scrubXCoord(extent.getMinX()+n*dx), extent.getMaxY(), height);
            } 
            coords[numXSections+1] = new Coordinate(scrubXCoord(extent.getMaxX()), extent.getMaxY(), height);
            for (int n=0; n<numXSections; n++) {
                coords[numXSections+n+2] = new Coordinate(scrubXCoord(extent.getMaxX()-n*dx), extent.getMinY(), height);
            }
            coords[2*numXSections+2] = new Coordinate(scrubXCoord(extent.getMinX()), extent.getMinY(), height);
            
        }

        String kmlGeom = kmlConv.processGeometry(geoFactory.createLineString(coords));
        
//        System.out.println(kmlGeom);
        
        return kmlGeom;
    }

    
    private static double scrubXCoord(double x) {
        x = (x < -180) ? x+360 : x;
        x = (x > 180)  ? x-360 : x;
        return x;
    }
}
