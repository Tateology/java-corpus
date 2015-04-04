package steve.test.swath;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;

import javax.vecmath.Point3d;
import javax.vecmath.Quat4d;

import steve.test.swath.GeoUtils.EarthModel;

import com.eteks.tools.d3d.Point3D;

public class GeolocationInterpolation {

	public static void main(String[] args) {
		
		try {
			
			double[] latArray = new double[60];
			double[] lonArray = new double[60];
			
			for (int n=0; n<60; n++) {
				latArray[n] = 34.0 + n*0.5;
				lonArray[n] = -90 + n*0.5;
			}
			                              
			double[][] xyzArray = new double[latArray.length][3];
			for (int n=0; n<60; n++) {
				GeoUtils.geo2xyz(latArray[n], lonArray[n], 0, xyzArray[n], GeoUtils.EarthModel.WGS84);
//				System.out.println(xyzArray[n][0]+" , "+xyzArray[n][1]+" , "+xyzArray[n][2]);

				
			}
			
			Point3d point1 = new Point3d(xyzArray[0]);
			Point3d point2 = new Point3d(xyzArray[xyzArray.length-1]);
			
			Point3d interpPoint = new Point3d();
			interpPoint.interpolate(point1, point2, .5);
			
			System.out.println("point3d");
			System.out.println(point1);
			System.out.println(point2);
			System.out.println(interpPoint);
			System.out.println(Arrays.toString(xyzArray[29]));
			
			int endIdx = xyzArray.length-1;
			Quat4d quat1 = new Quat4d(xyzArray[0][0], xyzArray[0][1], xyzArray[0][2], 0);
			Quat4d quat2 = new Quat4d(xyzArray[endIdx][0], xyzArray[endIdx][1], xyzArray[endIdx][2], 0);
			
			Quat4d interpQuat = new Quat4d();
			interpQuat.interpolate(quat1, quat2, .5);

			System.out.println("quat3f");
			System.out.println(quat1);
			System.out.println(quat2);
			System.out.println(interpQuat);
			System.out.println(Arrays.toString(xyzArray[29]));
			
			double mag1 = Math.sqrt(
					xyzArray[0][0]*xyzArray[0][0] 
			        + xyzArray[0][1]*xyzArray[0][1]
			        + xyzArray[0][2]*xyzArray[0][2] );
			double mag2 = Math.sqrt(
					xyzArray[endIdx][0]*xyzArray[endIdx][0] 
			        + xyzArray[endIdx][1]*xyzArray[endIdx][1]
			        + xyzArray[endIdx][2]*xyzArray[endIdx][2] );
			double mag3 = Math.sqrt(
					xyzArray[29][0]*xyzArray[29][0] 
			        + xyzArray[29][1]*xyzArray[29][1]
			        + xyzArray[29][2]*xyzArray[29][2] );
			
			System.out.println(mag1);
			System.out.println(mag2);
			System.out.println(mag3);
			
			double interpMag = (mag1+mag2)/2;
			System.out.println(interpMag);
			interpQuat.scale(interpMag);
			
			System.out.println(interpQuat);
			
			double[] geoPos = new double[3];
			GeoUtils.xyz2geo(new double[] { interpQuat.x, interpQuat.y, interpQuat.z }, 
					geoPos, EarthModel.WGS84);
			System.out.println(latArray[29]+","+lonArray[29]);
			System.out.println(Arrays.toString(geoPos));
			
		} catch (Exception e) {
			e.printStackTrace();
		}	
		
	}
	
	
	
	public static void testUvLuts() {
		
		double[][] latArray = new double[3000][3000];
		double[][] lonArray = new double[3000][3000];
		
		for (int j=0; j<latArray.length; j++) {
			for (int i=0; i<latArray[0].length; i++) {
				
			}
		}
		
	}
	
	

}
