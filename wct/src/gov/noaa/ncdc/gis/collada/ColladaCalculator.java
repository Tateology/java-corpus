package gov.noaa.ncdc.gis.collada;

import gov.noaa.ncdc.wct.WCTException;

import java.util.ArrayList;
import java.util.Arrays;

import com.vividsolutions.jts.geom.Coordinate;
/**
 * Coordinate calculator for Google Earth COLLADA (.dae) files.
 * 
 * 
 * FROM GOOGLE EARTH DOCS:
 * http://code.google.com/apis/earth/documentation/reference/interface_kml_model.html
 * 
 * A 3D object described in a referenced COLLADA file. COLLADA files have a .dae file extension. 
 * Models are created in their own coordinate space and then located, positioned, and scaled in 
 * Google Earth. Google Earth supports the COLLADA common profile, with the following exceptions:
 *
 *   * Google Earth supports only triangles and lines as primitive types. The maximum number of 
 *   	triangles allowed is 21845.
 *   * Google Earth does not support animation or skinning.
 *   * Google Earth does not support external geometry references. 
 *
 * @author steve.ansari@noaa.gov
 *
 */
public class ColladaCalculator {

	private Coordinate[] coords = null;
	private Coordinate[][] coords2D = null;
	private ArrayList<int[]> triangleList = null;
	private ArrayList<double[]> triangleNormalList = null;
	private double[][] coordVertexNormals = null;
	private double[][] coordVertexUV = null;

	
	public ColladaCalculator() {
	}
	
	
	

		
	/**
	 * Load an array of coordinates and a list of triangles made up of the 
	 * array index values of the corresponding coordinates.
	 * @param coords
	 * @param triangleList
	 * @throws WCTException 
	 */
	public void loadData(Coordinate[] coords, ArrayList<int[]> triangleList) throws WCTException {
 		this.coords = coords;
		this.triangleList = triangleList;
		
		if (triangleList.size() > 21845) {
			throw new WCTException("Maximum number of triangles allowed in COLLADA model is 21845.  Found: "+triangleList.size());
		}
		
		this.triangleNormalList = new ArrayList<double[]>();
		for (int[] vertexIndices : triangleList) {
			triangleNormalList.add(computeTriangleNormal(vertexIndices));
		}
		
		this.coordVertexNormals = computeVertexNormals();
		this.coordVertexUV = computeVertexUVTextureMap();

	}
	
	
	public Coordinate[] getCoordinates() {
		return coords;		
	}
	
	public ArrayList<int[]> getTriangles() {
		return triangleList;
	}
	
	public double[][] getVertexNormalsFromCoordinates() {
		return coordVertexNormals;
	}
	

	public double[][] getCoordVertexNormals() {
		return coordVertexNormals;
	}

	public double[][] getCoordVertexUV() {
		return coordVertexUV;
	}
	
	
	
	
	private double[] computeTriangleNormal(int[] vertexIndices) {
		
//		A face normal can be calculated with the following code:
//
//			normx = (z1-z2)*(y3-y2)-(y1-y2)*(z3-z2);
//			normy = (x1-x2)*(z3-z2)-(z1-z2)*(x3-x2);
//			normz = (y1-y2)*(x3-x2)-(x1-x2)*(y3-y2);
//			normlength = sqrt(sqr(normx)+sqr(normy)+sqr(normz));
//			normx /= normlength;
//			normy /= normlength;
//			normz /= normlength;
		
		double normx = ((coords[vertexIndices[0]].z - coords[vertexIndices[1]].z)*
				        (coords[vertexIndices[2]].y - coords[vertexIndices[1]].y)) -
				       ((coords[vertexIndices[0]].y - coords[vertexIndices[1]].y)*
					    (coords[vertexIndices[2]].z - coords[vertexIndices[1]].z));
		
		double normy = ((coords[vertexIndices[0]].x - coords[vertexIndices[1]].x)*
		        (coords[vertexIndices[2]].z - coords[vertexIndices[1]].z)) -
		       ((coords[vertexIndices[0]].z - coords[vertexIndices[1]].z)*
			    (coords[vertexIndices[2]].x - coords[vertexIndices[1]].x));
		
		double normz = ((coords[vertexIndices[0]].y - coords[vertexIndices[1]].y)*
		        (coords[vertexIndices[2]].x - coords[vertexIndices[1]].x)) -
		       ((coords[vertexIndices[0]].x - coords[vertexIndices[1]].x)*
			    (coords[vertexIndices[2]].y - coords[vertexIndices[1]].y));
		
		double normlength = Math.sqrt(normx*normx + normy*normy + normz*normz);
		normx /= normlength;
		normy /= normlength;
		normz /= normlength;
		
		return new double[] { normx, normy, normz };
	}
	
	
	private double[][] computeVertexNormals() {
		
		// from: http://gmc.yoyogames.com/index.php?showtopic=374068
		//  and: http://www.euclideanspace.com/maths/algebra/vectors/applications/normals/index.htm
		
		double[][] coordVertexNormals = new double[coords.length][3];
		
		// brute force for now
		for (int n=0; n<coords.length; n++) {

			double[] sumNormals = new double[3];
			int hitCount = 0;

			// 	loop through our vertex coordinates and find all triangles which use this vertex
			for(int i=0; i<triangleList.size(); i++) {
				int[] vi = triangleList.get(i);
				
				if (vi[0] == n || vi[1] == n || vi[2] == n) {
					double[] triangleNormal = triangleNormalList.get(i);
					
//					System.out.println("tri normal: "+Arrays.toString(triangleNormal));
					
					sumNormals[0] += triangleNormal[0];
					sumNormals[1] += triangleNormal[1];
					sumNormals[2] += triangleNormal[2];
					hitCount++;
				}
			}
			
			coordVertexNormals[n][0] = sumNormals[0]/hitCount;
			coordVertexNormals[n][1] = sumNormals[1]/hitCount;
			coordVertexNormals[n][2] = sumNormals[2]/hitCount*-1;  
		}
		
		return coordVertexNormals;
	}
	
	
	private double[][] computeVertexUVTextureMap() {
		// from: http://paulyg.f2s.com/uv.htm
		
		double[] range = new double[3];
		double[] offset = new double[3];
		
		double[] maxValues = new double[] { Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY };
		double[] minValues = new double[] { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY };
		
		double[][] coordVertexUV = new double[coords.length][2];
		for (Coordinate coord : coords) {
			if (coord.x > maxValues[0]) maxValues[0] = coord.x;
			if (coord.y > maxValues[1]) maxValues[1] = coord.y;
			if (coord.z > maxValues[2]) maxValues[2] = coord.z;
			if (coord.x < minValues[0]) minValues[0] = coord.x;
			if (coord.y < minValues[1]) minValues[1] = coord.y;
			if (coord.z < minValues[2]) minValues[2] = coord.z;
		}

		range[0] = maxValues[0] - minValues[0]; 
		range[1] = maxValues[1] - minValues[1]; 
		range[2] = maxValues[2] - minValues[2];
		
		offset[0] = 0 - minValues[0];
		offset[1] = 0 - minValues[1];
		offset[2] = 0 - minValues[2];
		
		double minDeltaU = Double.POSITIVE_INFINITY;
		double maxDeltaU = Double.NEGATIVE_INFINITY;
		double minDeltaV = Double.POSITIVE_INFINITY;
		double maxDeltaV = Double.NEGATIVE_INFINITY;
		double minDeltaX = Double.POSITIVE_INFINITY;
		double maxDeltaX = Double.NEGATIVE_INFINITY;
		double minDeltaY = Double.POSITIVE_INFINITY;
		double maxDeltaY = Double.NEGATIVE_INFINITY;
		
		for (int n=0; n<coords.length; n++) {
			coordVertexUV[n][0] = (coords[n].x+offset[0])/range[0];
			coordVertexUV[n][1] = (coords[n].y+offset[1])/range[1];
			
//			coordVertexUV[n][0] /= 2.0;
//			coordVertexUV[n][1] /= 2.0;
			
			for (int i=0; i<2; i++) {
				if (Double.isNaN(coordVertexUV[n][i])) {
					coordVertexUV[n][i] = 0;
				}
			}
			
//			System.out.println("UV (ST): "+Arrays.toString(coordVertexUV[n]));
			
			if (n < coords.length - 1) {
				minDeltaU = Math.min(minDeltaU, Math.abs(coordVertexUV[n+1][0]-coordVertexUV[n][0]));
				maxDeltaU = Math.max(maxDeltaU, Math.abs(coordVertexUV[n+1][0]-coordVertexUV[n][0]));
				minDeltaV = Math.min(minDeltaV, Math.abs(coordVertexUV[n+1][1]-coordVertexUV[n][1]));
				maxDeltaV = Math.max(maxDeltaV, Math.abs(coordVertexUV[n+1][1]-coordVertexUV[n][1]));
				minDeltaX = Math.min(minDeltaX, Math.abs(coords[n+1].x-coords[n].x));
				maxDeltaX = Math.max(maxDeltaX, Math.abs(coords[n+1].x-coords[n].x));
				minDeltaY = Math.min(minDeltaY, Math.abs(coords[n+1].y-coords[n].y));
				maxDeltaY = Math.max(maxDeltaY, Math.abs(coords[n+1].y-coords[n].y));
				
				double dist = Math.sqrt(
						Math.pow(coords[n].x-coords[n+1].x, 2) +
						Math.pow(coords[n].y-coords[n+1].y, 2) +
						Math.pow(coords[n].z-coords[n+1].z, 2)
					);
//				System.out.println(dist);
			}
		}
		

		// u values in 2D array for rows and columns
		double[][] uArray = new double[coords2D.length][coords2D[0].length];
		
		for (int j=0; j<coords2D.length; j++) {
		
			// get total x distance by following coordinates each row
			double xDistSum = 0;
			double[] distArray = new double[coords2D[0].length];
			for (int i=0; i<coords2D[0].length-1; i++) {
				distArray[i] = Math.sqrt(
						Math.pow(coords2D[j][i].x-coords2D[j][i+1].x, 2) +
						Math.pow(coords2D[j][i].y-coords2D[j][i+1].y, 2) +
						Math.pow(coords2D[j][i].z-coords2D[j][i+1].z, 2)
				);
				xDistSum += distArray[i];
			}
			double runningDistRatioSum = 0;
			double firstDistance = distArray[0]/xDistSum;  
			for (int i=0; i<distArray.length-1; i++) {
				runningDistRatioSum += distArray[i]/xDistSum;
				distArray[i] = runningDistRatioSum - firstDistance;  // set so we start at zero
			}
			// set last element to 1 (first should be 0 and last should be 1)
			// uv texture map is ratio from 0 to 1 of the x (for u) and y (for v) ranges
			// to 'tie' the image onto the 3D surface.
			distArray[distArray.length-1] = 1;

			uArray[j] = distArray;
		
		}

		
		// u values in 2D array for rows and columns
		double[][] vArray = new double[coords2D.length][coords2D[0].length];
		for (int i=0; i<coords2D[0].length; i++) {

			// get total y distance by following coordinates each row
			double yDistSum = 0;
			double[] distArray = new double[coords2D.length];
			for (int j=0; j<coords2D.length-1; j++) {
				distArray[j] = Math.sqrt(
						Math.pow(coords2D[j][i].x-coords2D[j+1][i].x, 2) +
						Math.pow(coords2D[j][i].y-coords2D[j+1][i].y, 2) +
						Math.pow(coords2D[j][i].z-coords2D[j+1][i].z, 2)
				);
				yDistSum += distArray[j];
			}
			double runningDistRatioSum = 0;
			double firstDistance = distArray[0]/yDistSum;  
			for (int j=0; j<distArray.length-1; j++) {
				runningDistRatioSum += distArray[j]/yDistSum;
				distArray[j] = runningDistRatioSum - firstDistance;  // set so we start at zero
			}
			// set last element to 1 (first should be 0 and last should be 1)
			// uv texture map is ratio from 0 to 1 of the x (for u) and y (for v) ranges
			// to 'tie' the image onto the 3D surface.
			distArray[distArray.length-1] = 1;

			vArray[i] = distArray;
				
		}
		
		
//		System.out.println(Arrays.deepToString(coordVertexUV));
		
		// now dump uv arrays into 1-D list structure
		// uncomment to allow percentage based uv
		int index = 0;
		for (int j=0; j<uArray.length; j++) {
			for (int i=0; i<uArray[0].length; i++) {
				coordVertexUV[index][0] = uArray[j][i]; 
				coordVertexUV[index][1] = vArray[i][j];
				index++;
			}
		}
		
		
		
		
//		System.out.println(Arrays.deepToString(coordVertexUV));
//		
//		
//		System.out.println(" range: "+Arrays.toString(range));
//		System.out.println("offset: "+Arrays.toString(offset));
//		System.out.println("min/max delta U: " + minDeltaU + " , " + maxDeltaU);
//		System.out.println("min/max delta V: " + minDeltaV + " , " + maxDeltaV);
//		System.out.println("min/max delta X: " + minDeltaX + " , " + maxDeltaX);
//		System.out.println("min/max delta Y: " + minDeltaY + " , " + maxDeltaY);
		
		
		
		return coordVertexUV;
	}
	
	

	/**
	 * Automatically creates triangles from coordinate data.  
	 * The coordinate array must emulate a grid in terms of spacing and layout.
	 * @param coordArray  [rows][columns]
	 * @throws WCTException 
	 */
	public void loadData(Coordinate[][] coordArray) throws WCTException {
		
		
//      x  	    x	    x
//		    x       x
//		x       x       x
//      x   x       x   
//		x   x   x   x   x
//		x / |   x   x   x 
//		x - x   x   x   x

		this.coords2D = coordArray;
		
		Coordinate[] coords1D = new Coordinate[coordArray.length*coordArray[0].length];
		
		this.triangleList = new ArrayList<int[]>();

		int height = coordArray.length;
		
		for (int j=0; j<coordArray.length; j++) {
			
			int width = coordArray[j].length;
			for (int i=0; i<coordArray[j].length; i++) {
				if (j+1 < height && i+1 < width) {
					triangleList.add(new int[] { to1DIndex(i, j, width, height), 
											 to1DIndex(i, j+1, width, height), 
											 to1DIndex(i+1, j, width, height) });
//				x
//				
//				x   x
				
				
					triangleList.add(new int[] { to1DIndex(i, j+1, width, height), 
						 					 to1DIndex(i+1, j+1, width, height), 
						 				     to1DIndex(i+1, j, width, height) });				
//				x   x 
//				
//				    x
				
				}
				
				coords1D[to1DIndex(i, j, width, height)] = coordArray[j][i];
			}
		}

		loadData(coords1D, triangleList);
	}
		
		
	/**
	 * Assumes rectangular array in order of [j][i] 
	 * @param i
	 * @param j
	 * @param width
	 * @param height
	 * @return
	 */
	private static int to1DIndex(int i, int j, int width, int height) {
		return j*width + i;
	}
	
	
	
	
	
	
	
	public static void main(String[] args) {

		try {
		
			Coordinate[] coords = new Coordinate[] {
					new Coordinate(10, 10, 10),
					new Coordinate(20, 10, 10),
					new Coordinate(20, 20, 10),
					new Coordinate(10, 20, 10)
			};		
			//			3  2
			//			0  1

			Coordinate[][] coords2D = new Coordinate[2][2];
			coords2D[0][0] = new Coordinate(10, 10, 10);
			coords2D[0][1] = new Coordinate(20, 10, 10);
			coords2D[1][0] = new Coordinate(10, 20, 10);
			coords2D[1][1] = new Coordinate(20, 20, 10);


			//		ArrayList<int[]> triangleList = new ArrayList<int[]>();
			//		triangleList.add( new int[] { 0 , 1 , 2 } );
			//		triangleList.add( new int[] { 0 , 2 , 3 } );




			ColladaCalculator test = new ColladaCalculator();
			//		test.loadData(coords, triangleList);
			test.loadData(coords2D);



			for (double[] vn : test.getVertexNormalsFromCoordinates()) {
				System.out.println(Arrays.toString(vn));
			}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}







}
