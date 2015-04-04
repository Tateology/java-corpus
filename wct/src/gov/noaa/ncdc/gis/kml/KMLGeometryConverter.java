package gov.noaa.ncdc.gis.kml;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Converts JTS Geometry objects to KML Geometry snippets 
 * @author steve.ansari
 *
 */
public class KMLGeometryConverter {
    
    private String supplementalTags = "";
    
    
    
    public String processGeometry(Geometry geom) throws Exception {

        String geomType = geom.getClass().getName();
        if (geomType.equals("com.vividsolutions.jts.geom.Polygon")) {
//            System.out.println("GEOMTYPE=Polygon");
            return processPolygon((Polygon)geom);
        }
        else if (geomType.equals("com.vividsolutions.jts.geom.MultiPolygon")) {
//            System.out.println("GEOMTYPE=MultiPolygon");
            return processMultiPolygon((MultiPolygon)geom);
        }
        else if (geomType.equals("com.vividsolutions.jts.geom.LineString")) {
//            System.out.println("GEOMTYPE=LineString");
            return processLineString((LineString)geom);
        }
        else if (geomType.equals("com.vividsolutions.jts.geom.MultiLineString")) {
//            System.out.println("GEOMTYPE=MultiLineString");
            return processMultiLineString((MultiLineString)geom);
        }
        else if (geomType.equals("com.vividsolutions.jts.geom.Point")) {
//            System.out.println("GEOMTYPE=Point");
            return processPoint((Point)geom);
        }
        else if (geomType.equals("com.vividsolutions.jts.geom.MultiPoint")) {
//            System.out.println("GEOMTYPE=MultiPoint");
            return processMultiPoint((MultiPoint) geom);
        }
        else {
            throw new Exception("Unsupported Geometry Type found: " + geomType);
        }
    }

    public String processMultiPolygon(MultiPolygon multiPoly) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("      <MultiGeometry>\n");
        for (int n = 0; n < multiPoly.getNumGeometries(); n++) {
            sb.append(processPolygon((Polygon)multiPoly.getGeometryN(n)));
        }
        sb.append("      </MultiGeometry>\n");
        return sb.toString();
    }

    public String processMultiLineString(MultiLineString multiLine) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("      <MultiGeometry>\n");
        for (int n = 0; n < multiLine.getNumGeometries(); n++) {
            sb.append(processLineString((LineString)multiLine.getGeometryN(n)));
        }
        sb.append("      </MultiGeometry>\n");
        return sb.toString();
    }

    public String processMultiPoint(MultiPoint multiPoint) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("      <MultiGeometry>\n");
        for (int n = 0; n < multiPoint.getNumGeometries(); n++) {
            sb.append(processPoint((Point)multiPoint.getGeometryN(n)));
        }
        sb.append("      </MultiGeometry>\n");
        return sb.toString();
    }

    
    
    
    
    
    public String processPolygon(Polygon poly) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("      <Polygon>\n");
        sb.append("        "+supplementalTags+"\n");
        sb.append("         <outerBoundaryIs>\n");
        sb.append("           <LinearRing>\n");
        sb.append("             <coordinates>\n");
        LineString extRing = poly.getExteriorRing();
        Coordinate[] extCoords = extRing.getCoordinates();
        for (int n=0; n<extCoords.length; n++) {
            sb.append("              "+getCoordinateString(extCoords[n])+"\n");
        }
        sb.append("             </coordinates>\n");
        sb.append("           </LinearRing>\n");
        sb.append("         </outerBoundaryIs>\n");
        int numInteriorRings = poly.getNumInteriorRing();
        for (int i=0; i<numInteriorRings; i++) {
            sb.append("         <innerBoundaryIs>\n");
            sb.append("           <LinearRing>\n");
            sb.append("             <coordinates>\n");
            LineString intRing = poly.getInteriorRingN(i);
            Coordinate[] intCoords = intRing.getCoordinates();
            for (int n=0; n<intCoords.length; n++) {
                sb.append("              "+getCoordinateString(extCoords[n])+"\n");
            }
            sb.append("             </coordinates>\n");
            sb.append("           </LinearRing>\n");
            sb.append("         </innerBoundaryIs>\n");
        }
        sb.append("      </Polygon>\n");
        return sb.toString();
    }

    public String processLineString(LineString lineString) throws Exception {
        StringBuffer sb = new StringBuffer();
        
        sb.append("      <LineString>\n");
        sb.append("        "+supplementalTags+"\n");
        sb.append("        <coordinates>\n");
        Coordinate[] coords = lineString.getCoordinates();
        for (int n=0; n<coords.length; n++) {
            sb.append("          "+getCoordinateString(coords[n])+"\n");
        }
        sb.append("        </coordinates>\n");
        sb.append("      </LineString>\n");
        
        return sb.toString();
    }

    public String processPoint(Point point) throws Exception {
        StringBuffer sb = new StringBuffer();
        
        Coordinate coord = point.getCoordinate();
        
        sb.append("      <Point>\n");
        sb.append("        "+supplementalTags+"\n");
        sb.append("        <coordinates>");
        sb.append(getCoordinateString(coord));
        sb.append("        </coordinates>\n");
        sb.append("      </Point>\n");
        
        return sb.toString();
    }

    
    
    
    private String getCoordinateString(Coordinate coord) {        
        if (Double.isNaN(coord.z)) {
            return coord.x+","+coord.y+",0";
        }
        else {
            return coord.x+","+coord.y+","+coord.z;
        }        
    }
    
    
    
    
    public String getSupplementalTags() {
        return supplementalTags;
    }

    /**
     * Allows addition of extra tags such as extrude, tessellate 
     * and altitudeMode to geometry section.
     * @param supplementalTags
     */
    public void setSupplementalTags(String supplementalTags) {
        this.supplementalTags = supplementalTags;
    }

    /**
     * Allows addition of extra tags such as extrude, tessellate 
     * and altitudeMode to geometry section.
     * @param supplementalTags
     */
    public void appendSupplementalTags(String supplementalTags) {
        this.supplementalTags = this.supplementalTags + supplementalTags;
    }

}
