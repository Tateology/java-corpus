package steve.test;

import gov.noaa.ncdc.nexrad.NexradEquations;
import gov.noaa.ncdc.wct.decoders.MaxGeographicExtent;
import gov.noaa.ncdc.wct.decoders.nexrad.WCTProjections;

import java.awt.geom.Rectangle2D;

import org.geotools.ct.MathTransform;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

public class CAPPICalculator {

	
	public static void main(String[] args) {
		try {
			
			testCAPPI();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void testCAPPI() throws FactoryException, TransformException {
		
		double originLat = 35;
		double originLon = -90;
		double originAltInMeters = 100;
		
		
		double[] elevAngles = new double[] { .5, .8, 1.3, 1.5, 2.5, 3.5, 4.5, 5.8, 7, 10, 13 };
		
		
		double cappiHeightInMeters = 5000;
		
		WCTProjections wctProj = new WCTProjections();
		// instead of x,y to lat/lon, we need lat/lon to x/y
		MathTransform transform = wctProj.getRadarTransform(originLon, originLat).inverse();
		
		
		
		Rectangle2D.Double extent = MaxGeographicExtent.getNexradExtent(originLat, originLon);
		int height = 1200;
		int width = 1200;

		double[] srcPoints = new double[2];
		double[] dstPoints = new double[2];

		double cellSizeX = extent.getWidth()/width;
		double cellSizeY = extent.getHeight()/height;
			
		 
		for (int j=0; j<height; j++) {
			for (int i=0; i<width; i++) {
				
				// 1. get lat/lon at this segment
				double lon = extent.getMinX() + i*cellSizeX;
				double lat = extent.getMinY() + j*cellSizeY;
				
				// 2. find x and y in meters using local equal area projection for radar
                srcPoints[0] = lon;
                srcPoints[1] = lat;
                transform.transform(srcPoints, 0, dstPoints, 0, 1);
				

                double x = dstPoints[0];
                double y = dstPoints[1];
                double range = Math.sqrt(x*x + y*y);
                
//                double elevAngle = Math.toDegrees(Math.atan(cappiHeightInMeters/range));
//                double slantRange = range*Math.cos(Math.toRadians(elevAngle));
                
//            	http://www.wdtb.noaa.gov/courses/dloc/topic3/lesson1/Section5/Section5-4.html
//                H = SR*sin PHI + (SR*SR)/(2*IR*RE)
//                	  	
//                Equation (12)
//
//                      where
//
//                      H = height of the beam centerline above radar level in km
//                      SR = slant range in km
//                      PHI = angle of elevation in degrees
//                      IR = refractive index, 1.21
//                      RE = radius of earth, 6371 km
                
                double minDiff = Double.POSITIVE_INFINITY;
                int minDiffIndex = 0;
                for (int n=0; n<elevAngles.length; n++) {
                	double slantRange = range*Math.cos(Math.toRadians(elevAngles[n]));
                    double altARL = NexradEquations.getRelativeBeamHeight(elevAngles[n], slantRange);

//                    System.out.println("x,y="+x+","+y+"  range="+range+"  elevAngle="+elevAngles[n] + "  slantRange="+slantRange + "  altARL="+altARL);
                    
                    double diff = Math.abs(altARL-cappiHeightInMeters);
                    if (diff < minDiff) {
                    	minDiff = diff;
                    	minDiffIndex = n;
                    }

                }
                

                
            	double slantRange = range*Math.cos(Math.toRadians(elevAngles[minDiffIndex]));
                double altARL = NexradEquations.getRelativeBeamHeight(elevAngles[minDiffIndex], slantRange);
//                System.out.println("x,y="+x+","+y+"  range="+range+"  elevAngle="+elevAngles[minDiffIndex] + "  slantRange="+slantRange + "  altARL="+altARL);

                
                
			}
		}
		
	}
}
