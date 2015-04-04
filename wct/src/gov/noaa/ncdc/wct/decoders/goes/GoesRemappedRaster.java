package gov.noaa.ncdc.wct.decoders.goes;

import edu.wisc.ssec.mcidas.AreaFile;
import edu.wisc.ssec.mcidas.AreaFileException;
import gov.noaa.ncdc.wct.WCTException;
import gov.noaa.ncdc.wct.WCTIospManager;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.ColorsAndValues;
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.event.DataDecodeEvent;
import gov.noaa.ncdc.wct.event.DataDecodeListener;
import gov.noaa.ncdc.wct.export.raster.WCTGridCoverageSupport;
import gov.noaa.ncdc.wct.export.raster.WCTRaster;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.Vector;
import java.util.logging.Logger;

import javax.media.jai.RasterFactory;

import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.gc.GridCoverage;
import org.geotools.pt.Envelope;

import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.iosp.mcidas.McIDASAreaProjection;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.Projection;
import ucar.unidata.geoloc.ProjectionPointImpl;


public class GoesRemappedRaster implements WCTRaster {

	private static final Logger logger = Logger.getLogger(GoesRemappedRaster.class.getName());


	public static final int ROW_CACHE_SIZE = 500;
	public static final java.awt.geom.Rectangle2D.Double GOES_DEFAULT_EXTENT = 
		new java.awt.geom.Rectangle2D.Double(-135.0, 12.0, 78.0, 55.0);

	public static final int STARTING_MIN_VALUE = 999999999;
	public static final int STARTING_MAX_VALUE = -999999999;

	private int numFileReads = 0;
	private int numCacheReads = 0;
	private int numRepeatRows = 0;

	private int lastRowRead = -1;
	private int[] rowData = null;

	private java.awt.geom.Rectangle2D.Double bounds;
	private GridCoverage gc;
	private WritableRaster raster;
	private String colorTableAlias = "Default";

	private double cellsize = -999;
	private boolean forceEqualXYCellsize = true;
	private int width = 800;
	private int height = 800;

	private double minValue = STARTING_MIN_VALUE;
	private double maxValue = STARTING_MAX_VALUE; 

	private Envelope envelope;

	private Vector<DataDecodeListener> listeners = new Vector<DataDecodeListener>();


	public static enum Band { BAND1, BAND2, BAND3, BAND4, BAND5, BAND6 };
	private Band lastBandDecoded;

	private int currentAlpha;

	private HashMap<Integer, int[]> rowDataMap = new HashMap<Integer, int[]>();
	private ArrayList<Integer> rowCacheOrder = new ArrayList<Integer>();


	private Variable var;
	private int imgWidth; 
	private int imgHeight;
	private Projection proj;

	private Date datetime;
	private String longName = "GOES GVAR Remapped Satellite Imagery";
	private String units = "";
    private String standardName = "";
    private String variableName = "";
	
	final static double NO_DATA_VALUE = 99999;

    private double minValueFilter = Double.NEGATIVE_INFINITY;
    private double maxValueFilter = Double.POSITIVE_INFINITY;
    
	
	public GoesRemappedRaster() throws IllegalAccessException, InstantiationException {
        WCTIospManager.getInstance().registerIosp(gov.noaa.ncdc.iosp.area.AreaIosp.class);
	}


	
    public void scan(String source) throws DecodeException, WCTException, IOException, InvalidRangeException, AreaFileException, ParseException {
        process(source, true);
    }

	public void process(String source) throws DecodeException, WCTException, IOException, InvalidRangeException, AreaFileException, ParseException {
		process(source, GOES_DEFAULT_EXTENT);
	}

	public void process(String source, double smoothingFactor) 
	throws DecodeException, WCTException, IOException, InvalidRangeException, AreaFileException, ParseException {
		process(source, GOES_DEFAULT_EXTENT, smoothingFactor);
	}

	public void process(String source, java.awt.geom.Rectangle2D.Double bounds) 
		throws DecodeException, WCTException, IOException, InvalidRangeException, AreaFileException, ParseException {
		process(source, bounds, 0.0);
	}

	public void process(String source, java.awt.geom.Rectangle2D.Double bounds, double smoothingFactor) 
		throws DecodeException, WCTException, IOException, InvalidRangeException, AreaFileException, ParseException {
		
		process(source, bounds, smoothingFactor, false);
	}
    private void process(String source, boolean scanOnly) throws DecodeException, WCTException, IOException, InvalidRangeException, AreaFileException, ParseException {
        process(source, null, 0.0, scanOnly);
    }
    private void process(String source, java.awt.geom.Rectangle2D.Double bounds, double smoothingFactor, boolean scanOnly) 
    	throws DecodeException, WCTException, IOException, InvalidRangeException, AreaFileException, ParseException {
	
		this.bounds = bounds;
		String lineSectionString = "empty";
		minValue = 999999999;
		maxValue = -999999999;         


		logger.fine("GOES REMAP BOUNDS: "+bounds);



		if (source.contains(".BAND_01")) {
			lastBandDecoded = Band.BAND1;
		}
		else if (source.contains(".BAND_02")) {
			lastBandDecoded = Band.BAND2;
		}
		else if (source.contains(".BAND_03")) {
			lastBandDecoded = Band.BAND3;
		}
		else if (source.contains(".BAND_04")) {
			lastBandDecoded = Band.BAND4;
		}
		else if (source.contains(".BAND_05")) {
			lastBandDecoded = Band.BAND5;
		}
		else if (source.contains(".BAND_06")) {
			lastBandDecoded = Band.BAND6;
		}
		else {
			// default to BAND1
			lastBandDecoded = Band.BAND1;
		}





		DataDecodeEvent event = new DataDecodeEvent(this);
		try {

		    NetcdfFile ncfile = null;
		    
			// Start decode
			// --------------
			for (int i = 0; i < listeners.size(); i++) {
				event.setProgress(0);
				listeners.get(i).decodeStarted(event);
			}



//			if (source.contains(".BAND_")) {

				AreaFile areaFile = new AreaFile(source);
				this.proj = new McIDASAreaProjection(areaFile);
				logger.fine("BEFORE FILE OPEN");
				ncfile = NetcdfFile.open(source);
//				logger.fine(ncfile.toString());
//				String variableName = "image";
				this.variableName = "calibratedData";
				if (ncfile.findVariable("bandNum").readScalarInt() == 1) {
				    this.variableName = "image";
				}
				        
				this.var = ncfile.findVariable(variableName);
				this.imgWidth = var.getDimension(var.findDimensionIndex("GeoX")).getLength(); 
				this.imgHeight = var.getDimension(var.findDimensionIndex("GeoY")).getLength(); 
				this.units = var.findAttribute("units").getStringValue();
				
//				Variable timeVar = ncfile.findVariable("time");
//				this.datetime = new Date(timeVar.readScalarLong());

				DecimalFormat fmtDate = new DecimalFormat("00000");
				SimpleDateFormat sdfDate = new SimpleDateFormat("yyDDD");
				sdfDate.setTimeZone(TimeZone.getTimeZone("GMT"));
				DecimalFormat fmtTime = new DecimalFormat("000000");
				SimpleDateFormat sdfTime = new SimpleDateFormat("HHmmss");
				sdfTime.setTimeZone(TimeZone.getTimeZone("GMT"));

				System.out.println("********* GOES DATE/TIME INFO ***********");
				System.out.println("actualImgDate: "+ncfile.findVariable("actualImgDate").readScalarInt());				
				System.out.println("actualImgTime: "+ncfile.findVariable("actualImgTime").readScalarInt());				
				System.out.println("creationDate: "+ncfile.findVariable("creationDate").readScalarInt());				
				System.out.println("creationTime: "+ncfile.findVariable("creationTime").readScalarInt());				
				System.out.println("imageDate: "+ncfile.findVariable("imageDate").readScalarInt());				
				System.out.println("imageTime: "+ncfile.findVariable("imageTime").readScalarInt());				
				System.out.println("********* GOES DATE/TIME INFO ***********");
				
				long dateMillis = sdfDate.parse(fmtDate.format(ncfile.findVariable("actualImgDate").readScalarInt()).substring(1)).getTime();
				long timeMillis = sdfTime.parse(fmtTime.format(ncfile.findVariable("actualImgTime").readScalarInt())).getTime();

//                this.imgWidth = var.getDimension(var.findDimensionIndex("elements")).getLength(); 
//                this.imgHeight = var.getDimension(var.findDimensionIndex("lines")).getLength(); 
//				long timeMillis = 0;
//				long dateMillis = ncfile.findVariable("time").readScalarLong();
				
				
				
//				System.out.println(String.valueOf(ncfile.findVariable("imageDate").readScalarInt()));
//				System.out.println(String.valueOf(ncfile.findVariable("imageTime").readScalarInt()));
//				System.out.println(String.valueOf(ncfile.findVariable("creationDate").readScalarInt()));
//				System.out.println(String.valueOf(ncfile.findVariable("creationTime").readScalarInt()));
//				System.out.println(String.valueOf(ncfile.findVariable("actualImgDate").readScalarInt()));
//				System.out.println(String.valueOf(ncfile.findVariable("actualImgTime").readScalarInt()));

				this.datetime = new Date(dateMillis+timeMillis);

//			}
			logger.fine("AFTER VAR OPEN");

			if (bounds == null || bounds.equals(GOES_DEFAULT_EXTENT)) {
			
				// get default bounds
				if (bounds == null) {
					bounds = new Rectangle2D.Double();
				}
				
				double minLat = 999;
				double minLon = 999;
				double maxLat = -999;
				double maxLon = -999;
				
				ProjectionPointImpl pp = new ProjectionPointImpl(0, 0);
				LatLonPointImpl ll = new LatLonPointImpl();
				ll = (LatLonPointImpl) proj.projToLatLon(pp, ll);
				minLat = Math.min(minLat, ll.getLatitude());
				minLon = Math.min(minLon, ll.getLongitude());
				maxLat = Math.max(maxLat, ll.getLatitude());
				maxLon = Math.max(maxLon, ll.getLongitude());
				
				pp.setLocation(0, imgHeight-1);
				ll = (LatLonPointImpl) proj.projToLatLon(pp, ll);
				minLat = Math.min(minLat, ll.getLatitude());
				minLon = Math.min(minLon, ll.getLongitude());
				maxLat = Math.max(maxLat, ll.getLatitude());
				maxLon = Math.max(maxLon, ll.getLongitude());

				pp.setLocation(imgWidth-1, imgHeight-1);
				ll = (LatLonPointImpl) proj.projToLatLon(pp, ll);
				minLat = Math.min(minLat, ll.getLatitude());
				minLon = Math.min(minLon, ll.getLongitude());
				maxLat = Math.max(maxLat, ll.getLatitude());
				maxLon = Math.max(maxLon, ll.getLongitude());

				pp.setLocation(imgWidth-1, 0);
				ll = (LatLonPointImpl) proj.projToLatLon(pp, ll);
				minLat = Math.min(minLat, ll.getLatitude());
				minLon = Math.min(minLon, ll.getLongitude());
				maxLat = Math.max(maxLat, ll.getLatitude());
				maxLon = Math.max(maxLon, ll.getLongitude());

				bounds.setRect(minLon, minLat, maxLon-minLon, maxLat-minLat);
//				bounds = adjustGeographicBounds(new Dimension(width, height), bounds);
				this.bounds = bounds;
			}

            if (scanOnly) {
    			ncfile.close();
                return;
            }

			
			if (forceEqualXYCellsize) {
				Dimension dim = WCTUtils.getEqualDimensions(bounds, width, height);
				width = (int)dim.getWidth();
				height = (int)dim.getHeight();  


				logger.fine("Un-Adjusted Bounds: "+bounds);

				bounds = WCTUtils.adjustGeographicBounds(new Dimension(width, height), bounds);
				this.bounds = bounds;
				
				logger.fine("Adjusted Bounds: "+bounds);

				double cellSizeX = bounds.width/(double)width;
				double cellSizeY = bounds.height/(double)height;

				if (Math.abs(cellSizeX - cellSizeY) > .00001) {
					throw new DecodeException("X/Y Cellsizes do not match - this is required for ASCII Grid Export");
				}

				this.cellsize = cellSizeX;

			}




			this.raster = RasterFactory.createBandedRaster(
					DataBuffer.TYPE_FLOAT, width, height, 1, null);

			// clear raster
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					this.raster.setSample(i, j, 0, NO_DATA_VALUE);
				}
			}

			this.envelope = new Envelope(bounds);


			double cellSizeX = envelope.getLength(0)/(double)width;
			double cellSizeY = envelope.getLength(1)/(double)height;

			System.out.println("height="+height+" width="+width+" cellSizeX="+cellSizeX+" , cellSizeY="+cellSizeY);
//			logger.fine("height="+height+" width="+width+" cellSizeX="+cellSizeX+" , cellSizeY="+cellSizeY);



			// ------------------------------------


			logger.fine("REMAPPING DATA TO WGS84");


			for (int n = 0; n < listeners.size(); n++) {
				event.setStatus("Resampling...");
				listeners.get(n).decodeProgress(event);
			}


			ProjectionPointImpl projPoint = new ProjectionPointImpl();
			LatLonPointImpl llPoint = new LatLonPointImpl();


			//-----------------------------------------------------------
			// Now conduct nearest neighbor resampling reproject data
			//-----------------------------------------------------------

			for (int y=0; y<height; y++) {

	        	if (WCTUtils.getSharedCancelTask().isCancel()) {
	        		throw new IOException("Operation canceled");
	        	}
	        	
				logger.fine(y+" OF "+height);
//                System.out.println(y+" OF "+height);

				double lat = envelope.getMinimum(1)+y*cellSizeY+cellSizeY/2.0;

				for (int x=0; x<width; x++) {

					double lon = envelope.getMinimum(0)+x*cellSizeX+cellSizeX/2.0;

					llPoint.set(lat, lon);
					projPoint = (ProjectionPointImpl) proj.latLonToProj(llPoint, projPoint);

					
//					System.out.println(llPoint+" , "+projPoint);

//					int row = (int)projPoint.getY();
//					int col = (int)projPoint.getX();
					int row = (int)Math.round(projPoint.getY());
					int col = (int)Math.round(projPoint.getX());

					if (col >= imgWidth || row >= imgHeight ||
							col < 0 || row < 0 
							|| Double.isNaN(projPoint.getX())) {

						continue;
					}



//					System.out.println("GETTING DATA FOR ("+col+" , "+row+")");
//					logger.fine("GETTING DATA FOR ("+col+" , "+row+")");
					int[] rowData = getRowData(row);

					if (! ((imgHeight-row-1) < 0 || (imgHeight-row-1) >= imgHeight ||
							col < 0 || col >= imgWidth)) {


						int value = rowData[col];


						//                        if (raster.getSample(x, height-y-1, 0) < value) { 
						if (value > this.raster.getSample(x, height-y-1, 0) ||
								this.raster.getSample(x, height-y-1, 0) == NO_DATA_VALUE) { 

							if (value == 99999) {
								value = (int)NO_DATA_VALUE;
							}
							
							if (value < minValueFilter || value > maxValueFilter) {
							    value = (int)NO_DATA_VALUE;
							}

							this.raster.setSample(x, height-y-1, 0, value);


							if (value != NO_DATA_VALUE && value < minValue) {
								minValue = value;
							}
							if (value != NO_DATA_VALUE && value > maxValue) {
//								if (! (lastBandDecoded == Band.BAND1 && value == 99999)) {
	maxValue = value;
	//                                    logger.fine("SETTING MAX VALUE TO: "+value);
//	}
							}

						}
					}
				}


				//                logger.fine(y);

				for (int n = 0; n < listeners.size(); n++) {
					event.setProgress((int) ((((double) y) / height) * 100.0));
					listeners.get(n).decodeProgress(event);
				}

			}


			// Do smoothing if needed
			if (smoothingFactor > 0) {
				WCTGridCoverageSupport gcSupport = new WCTGridCoverageSupport();
				this.raster = gcSupport.getSmoothedRaster(this, smoothingFactor, new double[0]);
			}

			ncfile.close();



		} catch (IOException e) {
			e.printStackTrace();
			logger.fine("\n\nSubset String: "+lineSectionString);
			throw e;
		} finally {

//			logger.fine("NUM FILE READS: "+numFileReads+"  NUM REPEAT ROWS: "+numRepeatRows+"  NUM CACHE READS: "+numCacheReads);
			logger.info("NUM FILE READS: "+numFileReads+"  NUM CACHE READS: "+numCacheReads);
            System.out.println("NUM FILE READS: "+numFileReads+"  NUM CACHE READS: "+numCacheReads);
			numFileReads = 0;
			numCacheReads = 0;
			numRepeatRows = 0;

			// End of decode
			rowDataMap.clear();
			rowCacheOrder.clear();

			// --------------
			for (int i = 0; i < listeners.size(); i++) {
				event.setProgress(0);
				listeners.get(i).decodeEnded(event);
			}
		}

	}


	/**
	 * Read a row of data from disk.  Cache the row to a HashMap.  If the number
	 * of cached rows exceeds the ROW_CACHE_SIZE value, remove the oldest row from
	 * cache.
	 * @param row
	 * @return
	 * @throws IOException
	 * @throws InvalidRangeException
	 */
	private int[] getRowData(int row) throws IOException, InvalidRangeException {

//		if (row == lastRowRead) {
//		numRepeatRows++;
//		return rowData;
//		}

		lastRowRead = row;

		if (! rowDataMap.containsKey(row)) {
		    
			String lineSectionString = (imgHeight-row-1)+":"+(imgHeight-row-1)+",0:"+(imgWidth-1);
			for (int n=2; n<var.getRank(); n++) {
				lineSectionString = "0:0,"+lineSectionString;
			}
			rowData = ((int[]) (var.read(lineSectionString).get1DJavaArray(Integer.class)));
			rowDataMap.put(row, rowData);
			rowCacheOrder.add(row);
//			logger.fine("ADDED DATA (array length "+width+") FOR ROW "+row+ "  CACHE SIZE="+rowDataMap.size());
//			System.out.println("ADDED DATA (array length "+rowData.length+") FOR ROW "+row+ "  CACHE SIZE="+rowDataMap.size());

			
			// clear oldest if needed
			if (rowDataMap.size() > ROW_CACHE_SIZE) {				                   
			    logger.fine("CACHE SIZE REACHED, REMOVING ROW "+rowCacheOrder.get(0));
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


















	public void setColorTableAlias(String colorTableAlias) {
		this.colorTableAlias = colorTableAlias;
	}

	public String getColorTableAlias() {
		return this.colorTableAlias;
	}

	public int getCurrentAlpha() {
		return currentAlpha;
	}


	public GridCoverage getGridCoverage(int alpha) throws WCTException, IOException {
		this.currentAlpha = alpha;
		
		// no data has been processed yet
		if (raster == null) {
			throw new WCTException("no data has been processed yet.  call process(...) first.");
		}

		GoesColorFactory gcf = GoesColorFactory.getInstance();
		ColorsAndValues colorTable = gcf.getColorsAndValues(this);
		Color[] colors = colorTable.getColors();
		Double[] values = colorTable.getValues();

//		System.out.println("size of color array: "+colors.length+"  values: "+values.length+" : values "+values[0]+" to "+values[values.length-1]);
		
		if (alpha != -1) {
			colors = WCTUtils.applyAlphaFactor(colors, alpha);
		}

		double[] minValues = new double[] { values[0] };
		double[] maxValues = new double[] { values[values.length-1] };

//        if (gds.getNetcdfFile().getIosp().getClass().toString().endsWith("Giniiosp")) {
//            minValues = new double[] { 0 };
//            maxValues = new double[] { 254 };
//            WCTUtils.flipArray(colors);
//        }

		this.gc = new GridCoverage("GOES Remapped Image", this.raster, 
				GeographicCoordinateSystem.WGS84, envelope, minValues, 
				maxValues, null, new Color[][]{colors}, null);

		return gc;
	}



	public GridCoverage getGridCoverage() throws Exception {
		return getGridCoverage(255);
	}

	public double getMinValue() {
		return minValue;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public Band getLastBandDecoded() {
		return lastBandDecoded;
	}




	/**
	 * Returns bounds of last processed data
	 */
	public java.awt.geom.Rectangle2D.Double getBounds() {
		return bounds;
	}


	public double getCellWidth() {
		return cellsize;
	}
	public double getCellHeight() {
		return cellsize;
	}



	public long getDateInMilliseconds() {
		return datetime.getTime();
	}

	public String getLongName() {
		return longName;
	}

	public double getNoDataValue() {
		return NO_DATA_VALUE;
	}

	public String getUnits() {
		return units;
	}

	public WritableRaster getWritableRaster() {
		return raster;
	}

	public boolean isEmptyGrid() {
		return false;
	}

	/**
	 * Always returns false because this data has been remapped.
	 */
	 public boolean isNative() {
		 return false;
	 }

	 public void setDateInMilliseconds(long dateInMillis) {
		 datetime = new Date(dateInMillis);
	 }

	 public void setLongName(String longName) {
		 this.longName = longName;
	 }

	 public void setUnits(String units) {
		 this.units = units;
	 }

	 public void setWritableRaster(WritableRaster raster) {
		 this.raster = raster;
	 }






	 public int getWidth() {
		 return width;
	 }


	 public void setWidth(int width) {
		 this.width = width;
	 }


	 public int getHeight() {
		 return height;
	 }


	 public void setHeight(int height) {
		 this.height = height;
	 }





	 public String toString() {
		 return "GOES REMAPPED RASTER: \n"+
		 "Height="+getHeight()+" Width="+getWidth()+
		 "Cell Width="+getCellWidth()+" Cell Height"+getCellHeight()+"\n"+
		 "Bounds="+getBounds()+"\n\n";
	 }





	 /**
	  * Adds a DataDecodeListener to the list.
	  *
	  * @param  listener  The feature to be added to the DataDecodeListener attribute
	  */
	 public void addDataDecodeListener(DataDecodeListener listener) {

		 if (!listeners.contains(listener)) {
			 listeners.add(listener);
		 }

	 }


	 /**
	  * Removes a DataDecodeListener from the list.
	  *
	  * @param  listener  DataDecodeListener to remove.
	  */
	 public void removeDataDecodeListener(DataDecodeListener listener) {

		 listeners.remove(listener);

	 }

	 
	 
	 
	 

	    @Override
	    public String getStandardName() {
	        return this.standardName;
	    }


	    @Override
	    public String getVariableName() {
	        return this.variableName;
	    }


	    @Override
	    public void setStandardName(String standardName) {
	        this.standardName = standardName;        
	    }


	    @Override
	    public void setVariableName(String variableName) {
	        this.variableName = variableName;        
	    }

	 

	    public void setMinValueFilter(double minValueFilter) {
	        this.minValueFilter = minValueFilter;
	    }


	    public double getMinValueFilter() {
	        return minValueFilter;
	    }


	    public void setMaxValueFilter(double maxValueFilter) {
	        this.maxValueFilter = maxValueFilter;
	    }


	    public double getMaxValueFilter() {
	        return maxValueFilter;
	    }

	 
}
