package steve.test.swath;

import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.goes.GoesRemappedRaster;
import gov.noaa.ncdc.wct.ui.WCTMapPane;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.media.jai.RasterFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.geotools.gc.GridCoverage;
import org.geotools.gui.swing.StatusBar;
import org.geotools.pt.Envelope;
import org.geotools.renderer.j2d.RenderedGridCoverage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.Index2D;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.ma2.MAMath.MinMax;
import ucar.nc2.Variable;
import ucar.nc2.dataset.CoordinateSystem;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.dt.GridDatatype;
import ucar.unidata.geoloc.LatLonRect;


public class CentroidLookUpTable {


    private static final Logger logger = LoggerFactory.getLogger(CentroidLookUpTable.class);
    
    
    
    /**
     * A {@link DirectColorModel} that holds data as unsigned shorts, ignoring
     * the red and alpha components.  (int value = green &lt;&lt; 8 | blue)
     */
    private static final ColorModel COLOR_MODEL = new DirectColorModel(
        16,           // Store the data as unsigned shorts
        0x00000000,
        0x0000ff00,
        0x000000ff,
        0x00000000
    );

    /** This value in the look-up table means "missing value" */
//    private static final int MISSING_VALUE = 65535;
    private static final int MISSING_VALUE = -1;

    /** This is the maximum index that can be stored in the LUT */
    private static final int MAX_INDEX = 65534;
    
    
    private WritableRaster iRaster = null;
    private WritableRaster jRaster = null;
    private Rectangle2D.Double extent = null;
    private Dimension size = null;
    
    private WritableRaster dataRaster = null;    
	private HashMap<Integer, double[]> rowDataMap = new HashMap<Integer, double[]>();
	private ArrayList<Integer> rowCacheOrder = new ArrayList<Integer>();

	public static final int ROW_CACHE_SIZE = 1000;
	private int numFileReads = 0;
	private int numCacheReads = 0;
	
	private MinMax dataMinMax = new MinMax(99999999, -99999999);
	
	private Array dataCache = null;
	private Index dataIndex = null;
	private Array latArray = null;
	private Array lonArray = null;
	private String varName = null;
	
	
	
	
	public void interpolateMissingScanLines(Array dataCache) throws Exception {
		Index index = dataCache.getIndex();
		if (index.getRank() != 2) {
			throw new Exception("Rank should be 2 for NPP data variables");
		}
		
		for (int j=0; j<index.getShape(0); j++) {
			for (int i=0; i<index.getShape(1); i++) {
				index.set(j, i);
				double val = dataCache.getDouble(index);
				
			}			
		}
	}
    
    public void applyData(Variable dataVar) throws IOException, InvalidRangeException {
    	if (dataRaster == null) {
    		dataRaster = createWritableRaster(size);
    	}
    	else {
    		clearRaster(dataRaster);
    	}
    	
    	int[][] ijMinMax = getIJMinMax();
    	System.out.println("datavar shape: "+Arrays.toString(dataVar.getShape()));
    	System.out.println("min/max i/j: "+Arrays.deepToString(ijMinMax));
    	
    	
    	int[] origin = new int[2];
    	int[] shape = new int[] { 1, 1 };
    	
    	if (dataCache == null) {
    		System.out.println("CACHING DATA VARIABLE: "+dataVar.getFullName());
   			dataCache = dataVar.read();
   			dataIndex = dataCache.getIndex();
    	}
    	
    	for (int j=0; j<dataRaster.getHeight(); j++) {
    		for (int i=0; i<dataRaster.getWidth(); i++) {
    			int iIndex = iRaster.getSample(i, j, 0);
    			if (iIndex < 0) {
    				continue;
    			}
    			int jIndex = jRaster.getSample(i, j, 0);
    			origin[0] = iIndex;
    			origin[1] = jIndex;
//    			double dataValue = dataVar.read(origin, shape).getDouble(0);
    			
//    			System.out.println("getting data value for "+iIndex+" , "+jIndex);
//    			double dataValue = getDataValue(dataVar, jIndex, iIndex);
    			
    			dataIndex.set(iIndex, jIndex);    			
    			double dataValue = dataCache.getDouble(dataIndex);
    			
//    			dataValue = iIndex;
    			
    	    	dataRaster.setSample(i, j, 0, dataValue);
    			
    	    	
    	    	dataMinMax.min = (dataValue < dataMinMax.min) ? dataValue : dataMinMax.min;
    	    	dataMinMax.max = (dataValue > dataMinMax.max) ? dataValue : dataMinMax.max;
    	    	
//    			System.out.println(i+","+j+" "+iIndex+","+jIndex+" : "+dataValue);
    		}
    	}
    	System.out.println(numFileReads + " / " +numCacheReads + "( file reads / cache reads )");
    	System.out.println(this.dataRaster);
    }
    
    private double getDataValue(Variable var, int i, int j) throws IOException, InvalidRangeException {
    	return getRowData(var, j)[i];
    }
    
	private double[] getRowData(Variable var, int row) throws IOException, InvalidRangeException {

//		if (row == lastRowRead) {
//		numRepeatRows++;
//		return rowData;
//		}

//		lastRowRead = row;

		if (! rowDataMap.containsKey(row)) {
		    
			String lineSectionString = (row)+":"+(row)+",0:"+(var.getShape(1)-1);
			for (int n=2; n<var.getRank(); n++) {
				lineSectionString = "0:0,"+lineSectionString;
			}
			double[] rowData = ((double[]) (var.read(lineSectionString).get1DJavaArray(Double.class)));
			rowDataMap.put(row, rowData);
			rowCacheOrder.add(row);
//			logger.fine("ADDED DATA (array length "+width+") FOR ROW "+row+ "  CACHE SIZE="+rowDataMap.size());
//			System.out.println("ADDED DATA (array length "+rowData.length+") FOR ROW "+row+ "  CACHE SIZE="+rowDataMap.size());

			
			// clear oldest if needed
			if (rowDataMap.size() > ROW_CACHE_SIZE) {				                   
//			    System.out.println("CACHE SIZE REACHED, REMOVING ROW "+rowCacheOrder.get(0));
				rowDataMap.remove(rowCacheOrder.get(0));
				rowCacheOrder.remove(0);
			}

			numFileReads++;
			return rowData;
		}
		else {
			numCacheReads++;
			return rowDataMap.get(new Integer(row));
		}

	}

    
    
    public void buildLutRasters(Variable latCoords, Variable lonCoords,
    		Rectangle2D.Double extent, Dimension size) throws Exception {
    	
    	
    	this.extent = extent;
    	this.size = size;
    	
    	
//    	BufferedImage iIm = createBufferedImage(size);
//    	BufferedImage jIm = createBufferedImage(size);
    	
    	double minLat = 99999;
    	double maxLat = -99999;
    	double minLon = 99999;
    	double maxLon = -99999;
    	
    	
    	
    	if (iRaster == null) {
    		iRaster = createWritableRaster(size);
    	}
    	else {
    		clearRaster(iRaster);
    	}
    	
    	if (jRaster == null) {
    		jRaster = createWritableRaster(size);
    	}
    	else {
    		clearRaster(jRaster);
    	}
    	
    	
    	
    	
    	if (latArray == null) {
    		latArray = latCoords.read();
    	}
    	if (lonArray == null) {
    		lonArray = lonCoords.read();
    	}
    	int numDims = latArray.getShape().length;
    	
    	double latScaleFactor = 1.0;
    	if (latCoords.findAttribute("scale_factor") != null) {
    		latScaleFactor = latCoords.findAttribute("scale_factor").getNumericValue().doubleValue();
    	}
    	double latAddOffset = 0;
    	if (latCoords.findAttribute("add_offset") != null) {
    		latAddOffset = latCoords.findAttribute("add_offset").getNumericValue().doubleValue();
    	}
    	
    	double lonScaleFactor = 1.0;
    	if (lonCoords.findAttribute("scale_factor") != null) {
    		lonScaleFactor = lonCoords.findAttribute("scale_factor").getNumericValue().doubleValue();
    	}
    	double lonAddOffset = 0;
    	if (lonCoords.findAttribute("add_offset") != null) {
    		lonAddOffset = lonCoords.findAttribute("add_offset").getNumericValue().doubleValue();
    	}
    	
    	System.out.println(extent);
    	System.out.println(size);
    	
    	double cellSizeX = extent.getWidth()/size.getWidth();
    	double cellSizeY = extent.getHeight()/size.getHeight();
    	
    	if (numDims == 2) {
    		
    		System.out.println(Arrays.toString(latArray.getShape()));
    		
    		Index2D index = new Index2D(latArray.getShape());
    		for (int j=0; j<latArray.getShape()[1]; j++) {
    			for (int i=0; i<latArray.getShape()[0]; i++) {
    				
    				index.set(i, j);
    				double lat = latArray.getDouble(index)*latScaleFactor + latAddOffset;
    				double lon = lonArray.getDouble(index)*lonScaleFactor + lonAddOffset;
    				
    				if (lat <= 90 && lat >= -90) {
    					minLat = Math.min(lat, minLat);
    					maxLat = Math.max(lat, maxLat);
    				}
    				if (lon <= 180 && lon >= 180) {
    					minLon = Math.min(lon, minLon);
    					maxLon = Math.max(lon, maxLon);
    				}
    				
    				int resampledI = (int) ((lon - cellSizeX/2.0 - extent.getMinX() )/cellSizeX);
    				int resampledJ = (int)size.getHeight()-1-(int) ((lat - cellSizeY/2.0 - extent.getMinY() )/cellSizeY);
    				
    				
//    				System.out.println(lat+","+lon+" ::: "+resampledI + " , "+resampledJ+" = ("+i+","+j+")");
					
    				if (resampledI >= 0 && resampledI < size.width &&
    						resampledJ >= 0 && resampledJ < size.height) {
    					
//    					System.out.println(lat+","+lon+" ::: "+resampledI + " , "+resampledJ+" = ("+i+","+j+")");
    					
    					iRaster.setSample(resampledI, resampledJ, 0, i);
    					jRaster.setSample(resampledI, resampledJ, 0, j);
    				}
    				
    			}
    		}
    		
    		System.out.println("min/max lat/lon"+ minLat+" to "+maxLat+"  ::: "+minLon+" to "+maxLon);
    	}
    	else {
    		throw new Exception("this is only supported for 2-D lat/lon axes");
    	}
    	
    }
    
    
    
    /**
     * Returns '-999' if outside grid, returns '-1' if within grid, but native grid entry is missing.
     * @param lat
     * @param lon
     * @param returnIJArray
     * @return
     */
    private int[] findResampledIJ(double lat, double lon, int[] returnIJArray) {
    	if (lat < extent.getMinY() || lat > extent.getMaxY() ||
    			lon < extent.getMinX() || lon > extent.getMaxX()) {
    		
    		System.out.println("outside bounds: "+lat+","+lon+" :: "+extent);
    		returnIJArray[0] = -999;
    		returnIJArray[1] = -999;
    		return returnIJArray;    		
    	}
    	
    	double cellsizeX = extent.getWidth()/iRaster.getWidth();
    	double cellsizeY = extent.getHeight()/iRaster.getHeight();
    	int resampledI = (int)((lon - extent.getMinX())/cellsizeX);
    	int resampledJ = (int)((lat - extent.getMinY())/cellsizeY);
    	returnIJArray[0] = resampledI;
    	returnIJArray[1] = resampledJ;
    	
    	System.out.println(extent);
    	System.out.println(Arrays.toString(returnIJArray));
    	
    	return returnIJArray;
    }
    
    
    private int[][] getIJMinMax() {
    	if (iRaster == null) {
    		return null;
    	}
    	
    	int minI = 999999999;
    	int minJ = 999999999;
    	int maxI = -1;
    	int maxJ = -1;
    	
    	for (int j=0; j<iRaster.getHeight(); j++) {
    		for (int i=0; i<iRaster.getWidth(); i++) {
    			int ival = iRaster.getSample(i, j, 0);
    			int jval = jRaster.getSample(i, j, 0);
    			if (ival >= 0) {
        			minI = Math.min(minI, ival);
        			maxI = Math.max(maxI, ival);
        			minJ = Math.min(minJ, jval);
        			maxJ = Math.max(maxJ, jval);
    			}
    		}
    	}
    	return new int[][] { new int[] { minI, maxI }, new int[] { minJ, maxJ } };
    }
    
    
    private int[] findClosestNativeIJ(double lat, double lon, int[] returnIJArray) {
    	int[] ij = findResampledIJ(lat, lon, returnIJArray);
    	return findNativeIJByIndex(ij[0], ij[1], returnIJArray);
    }
    
    private int[] findNativeIJByIndex(int resampledI, int resampledJ, int[] returnIJArray) {
    	int nativeI = iRaster.getSample(resampledI, resampledJ, 0);
    	int nativeJ = jRaster.getSample(resampledI, resampledJ, 0);
    	
    	if (nativeI == MISSING_VALUE) {
//    		System.out.println("MISSING at: "+resampledI+","+resampledJ);
    		returnIJArray[0] = -1;
    		returnIJArray[1] = -1;
    	}
//    	System.out.println(nativeI+" ~ "+nativeJ);
    	returnIJArray[0] = nativeI;
    	returnIJArray[1] = nativeJ;
    	
    	
    	return returnIJArray;
    	
    }
    
    /**
     * Returns true overlapping cell, or false if search was needed.
     * @param resampledI
     * @param resampledJ
     * @param radius
     * @param closestNativeIJ
     * @return
     */
    private boolean searchForClosestExistingNativeIJ(int resampledI, int resampledJ, int radius, int[] closestNativeIJ) {
    	int cnt = 0;
    	
    	double minDist = Double.POSITIVE_INFINITY;
//    	int[] closestNativeIJ = new int[2];
    	
    	for (int i=0; i<radius; i++) {
    		for (int j=0; j<radius; j++) {
    			cnt++;
    			if (resampledI - i >= 0 && 
    				resampledJ - j >= 0 &&
    				resampledI + i < iRaster.getWidth() &&
    				resampledJ + j < iRaster.getHeight()) {
    				
    				// According to Shepards method, that is done by the
    	            // sum(w*u)/sum(w), where w = 1 / d ^ p, 
    				// where d is the distance and p a positive real number (power used)
    				
    				double dist = Math.sqrt(i*i + j*j);
//    				double weight = (i == 0 && j == 0) ? 1 : 1/Math.pow(dist, 2);
    				int[] nativeIJ = new int[2];
    				
    				findNativeIJByIndex(resampledI-i, resampledJ-j, nativeIJ);    				
    				if (nativeIJ[0] != MISSING_VALUE && dist < minDist) {
    					// this means we are right on top of the grid cell

    					closestNativeIJ[0] = nativeIJ[0];
    					closestNativeIJ[1] = nativeIJ[1];
    					minDist = dist;
    					if (dist == 0) {
//    						System.out.println("dist = 0");
    						return true;
    					}
    					
    					
//    					System.out.println(cnt+" "+(resampledI-i)+" "+(resampledJ-j)+" "+
//    						nativeIJ[0]+" ~ "+nativeIJ[1]+"   "+dist+"|"+weight);
    				}
    				
    				if (! (i == 0 && j == 0)) {
        				findNativeIJByIndex(resampledI+i, resampledJ+j, nativeIJ);
        				if (nativeIJ[0] != MISSING_VALUE && dist < minDist) {
        					closestNativeIJ[0] = nativeIJ[0];
        					closestNativeIJ[1] = nativeIJ[1];
        					minDist = dist;
//            				System.out.println(cnt+" "+(resampledI+i)+" "+(resampledJ+j)+" "+
//            						nativeIJ[0]+" ~ "+nativeIJ[1]+"   "+dist+"|"+weight);
        				}
    				}
    			}
    		}
    	}
    	if (minDist == Double.POSITIVE_INFINITY) {
    		closestNativeIJ[0] = MISSING_VALUE;
    		closestNativeIJ[1] = MISSING_VALUE;
    	}
//    	System.out.println(Arrays.toString(closestNativeIJ));
    	return false;
    }
    
    
    
    
    private void fillInRasterLuts() {
    	
    	int radius = 2;
    	
    	int[] closestNativeIJ = new int[2];
    	
//    	WritableRaster iRasterToFill = iRaster.createWritableChild(
//    			0, 0, iRaster.getWidth(), iRaster.getHeight(), 
//    			0, 0, new int[]{ 0 });
//    	WritableRaster jRasterToFill = jRaster.createWritableChild(
//    			0, 0, jRaster.getWidth(), jRaster.getHeight(), 
//    			0, 0, new int[]{ 0 });
    	WritableRaster iRasterToFill = createWritableRaster(new Dimension(iRaster.getWidth(), iRaster.getHeight()));
    	WritableRaster jRasterToFill = createWritableRaster(new Dimension(jRaster.getWidth(), jRaster.getHeight()));
    	
    	
    	// ideas
    	// 1) keep list of which rows have 'continuous' data ranges so
    	//    we know when we are 'done' with that row forever
    	// 2) keep first and last column info for each row so we don't 
    	//    'bleed' out from each side of sweep
    	// 3) generate polygon of swath extent using first/last indices
    	//    of lat/lon arrays and then use as mask to clip the 
    	//    'bleed' that occurs from repeated nearest neighbor interpolations
    	int[] searchSwitchCount = new int[iRaster.getHeight()];
    	
    	boolean stillFilling = true;
    	for (int n=0; n<200; n++) {
//    	int n=0;
    		
    		System.out.println("filling iteration: "+n);
    		if (! stillFilling) {
    			break;
    		}
    	
    	for (int j=0; j<iRaster.getHeight(); j++) {
    		if (j%100 == 0) System.out.println("progress: "+j);
    		
    		if (n > 0 && searchSwitchCount[j] <= 2) {
    			stillFilling = false;
    			continue;
    		}
//    		System.out.println(j + " : "+ n);
    		stillFilling = true;
    		
    		searchSwitchCount[j] = 0;
    		boolean lastCellNoSearchNeeded = false;
    		for (int i=0; i<iRaster.getWidth(); i++) {
    			boolean noSearchNeeded = searchForClosestExistingNativeIJ(i, j, radius, closestNativeIJ);
    			if (lastCellNoSearchNeeded != noSearchNeeded) {
    				searchSwitchCount[j]++;
    			}
    			lastCellNoSearchNeeded = noSearchNeeded;
    			
//    			System.out.println(i+","+j+" "+Arrays.toString(closestNativeIJ));
    			
    			iRasterToFill.setSample(i, j, 0, closestNativeIJ[0]);
    			jRasterToFill.setSample(i, j, 0, closestNativeIJ[1]);
    			
    		}
    	}
    	
//    	System.out.println(Arrays.toString(searchSwitchCount));
    	
    	
    	
    	iRaster = copyWritableRasterInt(iRasterToFill);
    	jRaster = copyWritableRasterInt(jRasterToFill);
    	
    	}
    }
     
    
    
    
    
    
    /**
     * Creates and returns a new {@link BufferedImage} that stores pixel data
     * as unsigned shorts.  Initializes all pixel values to {@link #MISSING_VALUE}.
     * @return
     */
    private BufferedImage createBufferedImage(Dimension size)
    {
        WritableRaster raster = COLOR_MODEL.createCompatibleWritableRaster(size.width, size.height);
        BufferedImage im = new BufferedImage(COLOR_MODEL, raster, true, null);
        for (int y = 0; y < im.getHeight(); y++)
        {
            for (int x = 0; x < im.getWidth(); x++)
            {
                im.setRGB(x, y, MISSING_VALUE);
            }
        }
        logger.debug("Created BufferedImage of size {},{}, data buffer type {}",
            new Object[]{im.getWidth(), im.getHeight(), im.getRaster().getDataBuffer().getClass()});
        return im;
    }
    
    
    
    private WritableRaster createWritableRaster(Dimension size)
    {
        WritableRaster raster = RasterFactory.createBandedRaster(
                DataBuffer.TYPE_FLOAT, 
                size.width, size.height, 1, null);
        
        for (int j=0; j<size.height; j++) {
        	for (int i=0; i<size.width; i++) {
        		raster.setSample(i, j, 0, MISSING_VALUE);        		
        	}
        }
        
        return raster;
    }
    
    private void clearRaster(WritableRaster raster) {
        for (int j=0; j<size.height; j++) {
        	for (int i=0; i<size.width; i++) {
        		raster.setSample(i, j, 0, MISSING_VALUE);        		
        	}
        }
    }
    
    private WritableRaster copyWritableRasterInt(Raster inRaster)
    {
        WritableRaster raster = RasterFactory.createBandedRaster(
                DataBuffer.TYPE_FLOAT, 
                inRaster.getWidth(), inRaster.getHeight(), 1, null);
        
        for (int j=0; j<inRaster.getHeight(); j++) {
        	for (int i=0; i<inRaster.getWidth(); i++) {
        		raster.setSample(i, j, 0, inRaster.getSample(i, j, 0));        		
        	}
        }
        
        return raster;
    }
    
    public void setILutRaster(WritableRaster iRaster) {
		this.iRaster = iRaster;
	}
	public WritableRaster getILutRaster() {
		return iRaster;
	}
	public void setJLutRaster(WritableRaster jRaster) {
		this.jRaster = jRaster;
	}
	public WritableRaster getJLutRaster() {
		return jRaster;
	}
	public void setDataRaster(WritableRaster dataRaster) {
		this.dataRaster = dataRaster;
	}
	public WritableRaster getDataRaster() {
		return this.dataRaster;
	}

	
	public static MinMax[] getMinMaxLatLon(Variable latCoords, Variable lonCoords) throws IOException {
		// comment below because netcdfdataset takes care of this
//    	double latScaleFactor = 1.0;
//    	if (latCoords.findAttribute("scale_factor") != null) {
//    		latScaleFactor = latCoords.findAttribute("scale_factor").getNumericValue().doubleValue();
//    	}
//    	double latAddOffset = 0;
//    	if (latCoords.findAttribute("add_offset") != null) {
//    		latAddOffset = latCoords.findAttribute("add_offset").getNumericValue().doubleValue();
//    	}
    	double latMissingVal = 0;
    	if (latCoords.findAttribute("missing_value") != null) {
    		latMissingVal = latCoords.findAttribute("missing_value").getNumericValue().doubleValue();
    	}
//    	
//    	double lonScaleFactor = 1.0;
//    	if (lonCoords.findAttribute("scale_factor") != null) {
//    		lonScaleFactor = lonCoords.findAttribute("scale_factor").getNumericValue().doubleValue();
//    	}
//    	double lonAddOffset = 0;
//    	if (lonCoords.findAttribute("add_offset") != null) {
//    		lonAddOffset = lonCoords.findAttribute("add_offset").getNumericValue().doubleValue();
//    	}
    	double lonMissingVal = 0;
    	if (latCoords.findAttribute("missing_value") != null) {
    		lonMissingVal = latCoords.findAttribute("missing_value").getNumericValue().doubleValue();
    	}

		MinMax latMinMax = MAMath.getMinMaxSkipMissingData(latCoords.read(), latMissingVal);
		MinMax lonMinMax = MAMath.getMinMaxSkipMissingData(lonCoords.read(), lonMissingVal);
		
		return new MinMax[] { latMinMax, lonMinMax };
	}
	
//	public static void findCoordinateVariables(NetcdfDataset ncd, Variable dataVar) {
//		
//		if (! dataVar.getClass().getName().equals("ucar.nc2.dataset.VariableDS")) {
//			System.err.println("must be a VariableDS!");
//			return;
//		}
//		
//		
//		
//		System.out.println(ncd.getCoordinateAxes());
//		System.out.println(ncd.getCoordinateSystems());
//	}
	
	public MinMax getDataMinMax() {
		return dataMinMax;
	}

	public void setVarName(String varName) {
		this.varName = varName;
	}

	public String getVarName() {
		return varName;
	}

	public static Rectangle2D.Double scanExtent(GridDatatype grid) throws IOException, InvalidRangeException {

		try {
			
			
			
		System.out.println("opening file...");
//		NetcdfDataset ncfile = NetcdfDataset.openDataset(source);
//		Variable dataVar = ncfile.findVariable(varName);
//		List<CoordinateSystem> coordSysList = ((VariableDS)dataVar).getCoordinateSystems();
//		Variable latVar = ncfile.findVariable(coordSysList.get(0).getLatAxis().getFullName());
//		Variable lonVar = ncfile.findVariable(coordSysList.get(0).getLonAxis().getFullName());  
//		
//		double cornerLat1 = latVar.read(new int[] { 0, 0 }, new int[] { 1, 1} ).getDouble(0);
//		double cornerLat2 = latVar.read(new int[] { latVar.getDimension(0).getLength()-1, 0 }, 
//				new int[] { 1, 1} ).getDouble(0);
//		double cornerLat3 = latVar.read(new int[] { latVar.getDimension(0).getLength()-1, latVar.getDimension(1).getLength()-1 }, 
//				new int[] { 1, 1} ).getDouble(0);
//		double cornerLat4 = latVar.read(new int[] { 0, latVar.getDimension(1).getLength()-1 }, new int[] { 1, 1} ).getDouble(0);
//		
//		double cornerLon1 = lonVar.read(new int[] { 0, 0 }, new int[] { 1, 1} ).getDouble(0);
//		double cornerLon2 = lonVar.read(new int[] { lonVar.getDimension(0).getLength()-1, 0 }, new int[] { 1, 1} ).getDouble(0);
//		double cornerLon3 = lonVar.read(new int[] { lonVar.getDimension(0).getLength()-1, lonVar.getDimension(1).getLength()-1 }, 
//				new int[] { 1, 1} ).getDouble(0);
//		double cornerLon4 = lonVar.read(new int[] { 0, lonVar.getDimension(1).getLength()-1 }, new int[] { 1, 1} ).getDouble(0);
//		
//		double minLat = Math.min(Math.min(Math.min(cornerLat1, cornerLat2), cornerLat3), cornerLat4);
//		double minLon = Math.min(Math.min(Math.min(cornerLon1, cornerLon2), cornerLon3), cornerLon4);
//		double maxLat = Math.max(Math.max(Math.max(cornerLat1, cornerLat2), cornerLat3), cornerLat4);
//		double maxLon = Math.max(Math.max(Math.max(cornerLon1, cornerLon2), cornerLon3), cornerLon4);

//		return new Rectangle2D.Double(minLon, minLat, maxLon-minLon, maxLat-minLat);
		
		
		LatLonRect r = grid.getCoordinateSystem().getLatLonBoundingBox();
		return new Rectangle2D.Double(r.getLonMin(), r.getLatMin(), r.getLonMax()-r.getLonMin(), r.getLatMax()-r.getLatMin());
		
		
		
		
		} catch (Exception e) {
			e.printStackTrace();
			return GoesRemappedRaster.GOES_DEFAULT_EXTENT;
		}
		
	}

	public void clearCache() {
		dataCache = null;
		latArray = null;
		lonArray = null;
		dataMinMax.min = 99999999;
		dataMinMax.max = -99999999;
	}
	
	public void process(GridDatatype grid, Rectangle2D.Double extent) throws Exception {
		
		
		
		this.size = new Dimension(800, 800);
		this.extent = extent;
		
		System.out.println("lon var caching? "+grid.getCoordinateSystem().getXHorizAxis().isCaching());
		System.out.println("lat var caching? "+grid.getCoordinateSystem().getYHorizAxis().isCaching());
		
		
//		System.out.println("opening file...");
//		NetcdfDataset ncfile = NetcdfDataset.openDataset(source);
//		Variable dataVar = (varName == null) ? ncfile.getVariables().get(0) : ncfile.findVariable(varName);
//		
//		List<CoordinateSystem> coordSysList = ((VariableDS)dataVar).getCoordinateSystems();
//		Variable latVar = ncfile.findVariable(coordSysList.get(0).getLatAxis().getFullName());
//		Variable lonVar = ncfile.findVariable(coordSysList.get(0).getLonAxis().getFullName());   		

		
		System.out.print("getting variable...");
		Variable dataVar = grid.getVariable();
		System.out.println("    done.");
		
		List<CoordinateSystem> coordSysList = ((VariableDS)dataVar).getCoordinateSystems();
		Variable lonVar = grid.getCoordinateSystem().getXHorizAxis().getOriginalVariable();
		Variable latVar = grid.getCoordinateSystem().getYHorizAxis().getOriginalVariable();
//		Variable latVar = ncfile.findVariable(coordSysList.get(0).getLatAxis().getFullName());
//		Variable lonVar = ncfile.findVariable(coordSysList.get(0).getLonAxis().getFullName());   		

		
		WritableRaster raster = getDataRaster();
		System.out.println("1: "+raster);
		
		buildLutRasters(latVar, lonVar, this.extent, this.size); 		
		
		raster = getDataRaster();	
		System.out.println("2: "+raster);
		
		fillInRasterLuts();
		
		raster = getDataRaster();	
		System.out.println("3: "+raster);
		
		
		applyData(dataVar);		
		
		raster = getDataRaster();	
		System.out.println("4: "+raster);
		
		System.out.println("5: "+getDataRaster());
//		ncfile.close();
//		System.out.println("6: "+getDataRaster());
	}
	
	public static void main(String[] args) {
		
    	try {
			
//    		String source = "C:\\work\\go-essp\\RSS_V06_SSMI_FCDR_F08_D19870919_S1711_E1857_R01294.nc";
//    		String source = "C:\\work\\npp\\GMTCO-VSSTO_npp_d20120425_t0756391_e0802195_b02553_c20120426143244193439_noaa_ops.nc";
    		String source = "C:\\work\\npp\\GMTCO-VSSTO_npp_d20120426_t0556450_e0602254_b02566_c20120427193220029403_noaa_ops.nc";
    				
    		System.out.println("opening file...");
    		NetcdfDataset ncfile = NetcdfDataset.openDataset(source);
    		
    		System.out.println("getting coord variables...");
//    		Variable latVar = ncfile.findVariable("latitude_hires");
//    		Variable lonVar = ncfile.findVariable("longitude_hires");
//    		Variable dataVar = ncfile.findVariable("antenna_temperature_85H");
    		Variable dataVar = ncfile.findVariable("bulksst");
    		
    		List<CoordinateSystem> coordSysList = ((VariableDS)dataVar).getCoordinateSystems();
    		System.out.println(coordSysList.get(0).getName());
    		
//    		Variable latVar = ncfile.findVariable("lat");
//    		Variable lonVar = ncfile.findVariable("lon");
    		Variable latVar = ncfile.findVariable(coordSysList.get(0).getLatAxis().getFullName());
    		Variable lonVar = ncfile.findVariable(coordSysList.get(0).getLonAxis().getFullName());   		
    		
    		
//    		findCoordinateVariables(ncfile, dataVar);
//    		if (true) return;
    		
    		
    		
    		
    		System.out.print("getting minmax");
    		MinMax[] minMaxLatLon = getMinMaxLatLon(latVar, lonVar);
    		System.out.println("... done");
    		
    		Dimension size = new Dimension(800, 800);
//    		Rectangle2D.Double extent = new Rectangle2D.Double(-20, 25, 60, 60);
    		Rectangle2D.Double extent = new Rectangle2D.Double(
    				minMaxLatLon[1].min, minMaxLatLon[0].min, 
    				Math.abs(minMaxLatLon[1].max-minMaxLatLon[0].min), 
    				Math.abs(minMaxLatLon[0].max-minMaxLatLon[0].min));
    		
    		System.out.println(extent);
    		extent = WCTUtils.adjustGeographicBounds(size, extent);
    		System.out.println(extent);
    		
    		CentroidLookUpTable lut = new CentroidLookUpTable();
    		lut.buildLutRasters(latVar, lonVar, extent, size); 		
    		
    		
    		
    		
//    		int[] resampledIJ = new int[2];
//    		int[] nativeIJ = new int[2];
//    		resampledIJ = lut.findResampledIJ(55.7, 10, resampledIJ);
////    		nativeIJ = lut.findNativeIJByIndex(resampledIJ[0], resampledIJ[1], nativeIJ);
//    		lut.searchForClosestExistingNativeIJ(resampledIJ[0], resampledIJ[1], 4, nativeIJ);
//    		
//    		System.out.println(nativeIJ[0]+" ~ "+nativeIJ[1]);
    		
    		
    		lut.fillInRasterLuts();
    		
//    		if (true) return;
    		
    		
    		lut.applyData(dataVar);
    		

    		ncfile.close();
    		
    		
    		GridCoverage iGc = new GridCoverage("iRaster", lut.getILutRaster(), new Envelope(extent));
    		GridCoverage jGc = new GridCoverage("jRaster", lut.getJLutRaster(), new Envelope(extent));
    		GridCoverage dataGc = new GridCoverage("dataRaster", lut.getDataRaster(), new Envelope(extent));
    		
    		
    		WCTMapPane iMapPane = new WCTMapPane();
    		iMapPane.getRenderer().addLayer(new RenderedGridCoverage(iGc));
    		iMapPane.setSize(size);
    		WCTMapPane jMapPane = new WCTMapPane();
    		jMapPane.getRenderer().addLayer(new RenderedGridCoverage(jGc));
    		jMapPane.setSize(size);
    		WCTMapPane dataMapPane = new WCTMapPane();
    		dataMapPane.getRenderer().addLayer(new RenderedGridCoverage(dataGc));
    		dataMapPane.setSize(size);
    		
    		JFrame frame = new JFrame();
    		frame.getContentPane().setLayout(new BorderLayout());
    		JTabbedPane tabPane = new JTabbedPane();
    		JPanel iPanel = new JPanel(new BorderLayout());
    		iPanel.add(iMapPane, BorderLayout.CENTER);
    		iPanel.add(new StatusBar(iMapPane), BorderLayout.SOUTH);
    		
    		JPanel jPanel = new JPanel(new BorderLayout());
    		jPanel.add(jMapPane, BorderLayout.CENTER);
    		jPanel.add(new StatusBar(jMapPane), BorderLayout.SOUTH);

    		JPanel dataPanel = new JPanel(new BorderLayout());
    		dataPanel.add(dataMapPane, BorderLayout.CENTER);
    		dataPanel.add(new StatusBar(dataMapPane), BorderLayout.SOUTH);
    		
    		frame.getContentPane().add(tabPane, BorderLayout.CENTER);
    		tabPane.addTab("iGc", iPanel);
    		tabPane.addTab("jGc", jPanel);
    		tabPane.addTab("dataGc", dataPanel);
    		
    		frame.pack();
    		frame.setVisible(true);
    		
    		
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
	}
}
