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

package steve.test;

import org.geotools.ct.MathTransform;
import org.geotools.pt.CoordinatePoint;

import com.vividsolutions.jts.geom.CoordinateFilter;

/**
 * @version $Id:
 * @author rschulz
 */
public class TransformationCoordinateFilter implements CoordinateFilter{
    /* Transform to apply to each coordinate*/
    private MathTransform transform;
    
    /** Creates a new instance of TransformationCoordinateFilter */
    public TransformationCoordinateFilter(MathTransform transform) {
        this.transform = transform;
    }
    
    /*performs a transformation on a coordinate*/
    public void filter(com.vividsolutions.jts.geom.Coordinate coordinate) {
        CoordinatePoint point = new CoordinatePoint(coordinate.x, coordinate.y);
        try {
            point = transform.transform(point, point);
        }
        catch (org.opengis.referencing.operation.TransformException e) {
            System.out.println("Error in transformation: " + e);
        }
        
        coordinate.x = point.ord[0];
        coordinate.y = point.ord[1];
    }
    
}
