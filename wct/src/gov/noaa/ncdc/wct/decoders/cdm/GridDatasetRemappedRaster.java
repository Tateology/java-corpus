package gov.noaa.ncdc.wct.decoders.cdm;

import gov.noaa.ncdc.wct.WCTException;
import gov.noaa.ncdc.wct.WCTFilter;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.event.DataDecodeEvent;
import gov.noaa.ncdc.wct.event.DataDecodeListener;
import gov.noaa.ncdc.wct.export.raster.WCTGridCoverageSupport;
import gov.noaa.ncdc.wct.export.raster.WCTRaster;
import gov.noaa.ncdc.wct.ui.WCTNoGridsFoundException;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Logger;

import javax.media.jai.RasterFactory;

import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.gc.GridCoverage;
import org.geotools.pt.Envelope;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath.MinMax;
import ucar.ma2.Range;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.geoloc.Projection;
import uk.ac.rdg.resc.ncwms.coords.LonLatPositionImpl;
import uk.ac.rdg.resc.ncwms.coords.TwoDCoordSys;


public class GridDatasetRemappedRaster implements WCTRaster {

	private static final Logger logger = Logger.getLogger(GridDatasetRemappedRaster.class.getName());

	public static final int DEFAULT_REMAPPED_GRID_WIDTH = 800;
	public static final int DEFAULT_REMAPPED_GRID_HEIGHT = 800;


	public static final int ROW_CACHE_SIZE = 500;
	public static final java.awt.geom.Rectangle2D.Double GOES_DEFAULT_EXTENT = 
			new java.awt.geom.Rectangle2D.Double(-135.0, 12.0, 78.0, 55.0);

	public static final int STARTING_MIN_VALUE = 999999999;
	public static final int STARTING_MAX_VALUE = -999999999;


	private static final long NATIVE_GRID_CACHE_SIZE_LIMIT = 2000*2000;

	private int numFileReads = 0;
	private int numCacheReads = 0;
	private int numRepeatRows = 0;

	private int lastRowRead = -1;
	private double[] rowData = null;

	private java.awt.geom.Rectangle2D.Double bounds;
	private GridCoverage gc;
	private WritableRaster raster;
	private String colorTableAlias = "Default";

	private double cellsize = -999;
	private boolean forceEqualXYCellsize = true;
	private int width = DEFAULT_REMAPPED_GRID_WIDTH;
	private int height = DEFAULT_REMAPPED_GRID_HEIGHT;

	private double minValue = STARTING_MIN_VALUE;
	private double maxValue = STARTING_MAX_VALUE; 
	private double gridMinValue = STARTING_MIN_VALUE;
	private double gridMaxValue = STARTING_MAX_VALUE;
	private boolean hasValidRange = false;
	private double validRangeMinValue = STARTING_MIN_VALUE;
	private double validRangeMaxValue = STARTING_MAX_VALUE;
	private double displayMinValue = Double.NaN;
	private double displayMaxValue = Double.NaN;
	private Color[] displayColors = null;


	private int maxXIndex = -1;
	private int minXIndex = 100000000;
	private int maxYIndex = -1;
	private int minYIndex = 100000000;


	private Envelope envelope;

	private Vector<DataDecodeListener> listeners = new Vector<DataDecodeListener>();

	private int currentAlpha;

	private HashMap<Integer, double[]> rowDataMap = new HashMap<Integer, double[]>();
	private ArrayList<Integer> rowCacheOrder = new ArrayList<Integer>();
	private Array dataCache = null;

	private int imgWidth; 
	private int imgHeight;
	private Projection proj;
	private boolean isNative = false;
	private boolean isRegularLatLon = false;

	private Date datetime;
	private String longName = " ";
	private String units = " ";
	private String standardName = "";

	private ucar.nc2.dt.GridDataset gds = null;
	private GridDatatype grid = null;
	private GridCoordSystem coordSys = null;
	private String fileTypeDesc = null;

	private int gridIndex = 0;
	private String gridVariableName = null;
	private int timeIndex = 0;
	private int runtimeIndex = 0;
	private int zIndex = 0;

	private double[] zCoordVals = null;


	private String lastDecodedFile = null;
	private boolean isNewGrid = true;

	private String iospClassString = "";

	private boolean isAutoMinMax = true;

	private boolean isYAxisFlipped = false;
	private long yAxisSize;

	//    private double minValueFilter = Double.NEGATIVE_INFINITY;
	//    private double maxValueFilter = Double.POSITIVE_INFINITY;
	private WCTFilter wctFilter = new WCTFilter();

	private DataDecodeEvent event = new DataDecodeEvent(this);

	private boolean isEmptyGrid;

	private boolean forceResample = false;

//	private CentroidLookUpTable swathLut = new CentroidLookUpTable();




	public GridDatasetRemappedRaster() {
//		    	System.out.println("new instance");
//		    	JOptionPane.showMessageDialog(null,  "new instance");

	}


	public void scan(String source) throws DecodeException, WCTException, IOException, WCTNoGridsFoundException {
		process(source, true);
	}

	public void process(String source) throws DecodeException, WCTException, IOException, WCTNoGridsFoundException {
		process(source, null);
	}
	public void process(String source, java.awt.geom.Rectangle2D.Double bounds) throws DecodeException, WCTException, IOException, WCTNoGridsFoundException {
		process(source, bounds, 0.0);
	}
	public void process(String source, java.awt.geom.Rectangle2D.Double bounds, double smoothingFactor) throws DecodeException, WCTException, IOException, WCTNoGridsFoundException {
		process(source, bounds, smoothingFactor, false);
	}
	private void process(String source, boolean scanOnly) throws DecodeException, WCTException, IOException, WCTNoGridsFoundException {
		process(source, null, 0.0, scanOnly);
	}
	private void process(String source, java.awt.geom.Rectangle2D.Double bounds, double smoothingFactor, boolean scanOnly) 
			throws DecodeException, WCTException, IOException, WCTNoGridsFoundException {




		if (lastDecodedFile == null || ! lastDecodedFile.equals(source) || isNewGrid) {
			//            calculateStatistics(source);
			//            isNewGrid = false;
			//            lastDecodedFile = source;
			dataCache = null;
		}

		//        this.bounds = bounds;
		String lineSectionString = "empty";
		minValue = 999999999;
		maxValue = -999999999;         
		maxXIndex = -1;
		minXIndex = 100000000;
		maxYIndex = -1;
		minYIndex = 100000000;


		logger.fine("REMAP BOUNDS: "+bounds);




		try {

			// Start decode
			// --------------
			for (int i = 0; i < listeners.size(); i++) {
				event.setProgress(0);
				listeners.get(i).decodeStarted(event);
			}

//			System.out.print("Opening Grid... ");



			StringBuilder errlog = new StringBuilder();
			this.gds = GridDatasetUtils.openGridDataset(source, errlog);
			if (this.gds.getGrids().size() == 0) {
				//            	this.gds.close();
				throw new WCTNoGridsFoundException("No Grids found in file.  Check CF-compliance for gridded data");
			}
			gridIndex = (getGridIndex() < 0) ? 0 : getGridIndex();
			if (gridVariableName != null) {
				gridIndex = gds.getGrids().indexOf( gds.findGridDatatype(gridVariableName) );
			}
			timeIndex = (getTimeIndex() < 0) ? 0 : getTimeIndex();
			runtimeIndex = (getRuntimeIndex() < 0) ? 0 : getRuntimeIndex();
			zIndex = (getZIndex() < 0) ? 0 : getZIndex();

			//            JOptionPane.showMessageDialog(null,  timeIndex);



			gridIndex = (gridIndex > gds.getGrids().size()) ? gds.getGrids().size()-1 : gridIndex;

			if (gridIndex < 0) {
				throw new WCTException("Could not find variable named '"+gridVariableName+"'");
			}

			this.grid = gds.getGrids().get(gridIndex);

//			System.out.println("finished!!!");









			// special testing of npp
//			if (source.contains("_npp_d2")) {
//
//				if (scanOnly) {
//					try {
//						System.out.print("start extent scan...  ");
//						this.bounds = CentroidLookUpTable.scanExtent(this.grid);
//						System.out.println("  finished!");
//					} catch (InvalidRangeException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					return;
//				}
//				try {
//					if (bounds == null) {
//						// just something for testing
//						bounds = GOES_DEFAULT_EXTENT;
//					}
//					if (lastDecodedFile != null && (! lastDecodedFile.equals(source) || isNewGrid)) {
//						System.out.println("clearing swath lut cache!");
//						swathLut.clearCache();
//					}
//
//
//					//            		swathLut.clearCache();
//
//
//
//
//					swathLut.process(this.grid, bounds);
//
//					this.raster = swathLut.getDataRaster();
//					this.minValue = swathLut.getDataMinMax().min;
//					this.maxValue = swathLut.getDataMinMax().max;
//					this.displayMinValue = swathLut.getDataMinMax().min;
//					this.displayMaxValue = swathLut.getDataMinMax().max;
//					this.gridMinValue = swathLut.getDataMinMax().min;
//					this.gridMaxValue = swathLut.getDataMinMax().max;
//					this.bounds = bounds;
//					this.envelope = new Envelope(bounds);
//
//
//					this.proj = grid.getProjection();
//					this.imgWidth = grid.getXDimension().getLength();
//					this.imgHeight = grid.getYDimension().getLength();
//					this.coordSys = grid.getCoordinateSystem();
//					this.longName = grid.getDescription();
//					this.gridVariableName = grid.getName();
//					this.units = grid.getUnitsString();
//
//					//    	            this.fileTypeDesc = "custom read of npp";
//					//    	            this.longName = "npp long name";
//					//    	            this.units = "npp units";
//
//
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//				lastDecodedFile = source;
//
//				return;
//			}




			// ANSARI TEST
			//            this.grid = this.grid.makeSubset(null, null, null, null, 
			//            		new Range(0, grid.getYDimension().getLength()/6, 1),
			//            		new Range(0, grid.getXDimension().getLength()/6, 1)
			//            );






			this.proj = grid.getProjection();
			this.imgWidth = grid.getXDimension().getLength();
			this.imgHeight = grid.getYDimension().getLength();
			this.coordSys = grid.getCoordinateSystem();
			this.longName = grid.getDescription();
			this.gridVariableName = grid.getName();
			this.units = grid.getUnitsString();

			if (grid.getTimeDimension() != null) {
				this.timeIndex = (getTimeIndex() < 0 || getTimeIndex() >= grid.getTimeDimension().getLength()) ? 0 : getTimeIndex();
			}
			if (grid.getRunTimeDimension() != null) {
				this.runtimeIndex = (getRuntimeIndex() < 0 || 
						getRuntimeIndex() >= grid.getRunTimeDimension().getLength()) ? 0 : getRuntimeIndex();
			}
			if (grid.getZDimension() != null) {
				this.zIndex = (getZIndex() < 0 || getZIndex() >= grid.getZDimension().getLength()) ? 0 : getZIndex();

				this.zCoordVals = grid.getCoordinateSystem().getVerticalAxis().getCoordValues();
			}

			if (coordSys.hasTimeAxis1D()) {
				CoordinateAxis1DTime timeAxis = grid.getCoordinateSystem().getTimeAxis1D();
				this.datetime = timeAxis.getTimeDate(timeIndex);
			}
			else {
				datetime = null;
			}


			fileTypeDesc = gds.getNetcdfFile().getFileTypeDescription();

			try {
				iospClassString = gds.getNetcdfFile().getIosp().getClass().toString();

				//                System.out.println(grid.getName()+" :: DATE: "+datetime+"\n :: PROJ: "+proj.paramsToString()+
						//                        "\n :: "+proj.getClassName()+"\n :: BBOX: "+gds.getBoundingBox().toString2());

			} catch (Exception e) {}

			if (bounds == null || this.bounds == null) {
				//                if (this.grid.getCoordinateSystem().isRegularSpatial()) {
				if (this.grid.getCoordinateSystem().getXHorizAxis().getRank() == 1 &&
						this.grid.getCoordinateSystem().getXHorizAxis().getRank() == 1) {       

					Array yAxisArray = this.grid.getCoordinateSystem().getYHorizAxis().read("0:1");
					Array xAxisArray = this.grid.getCoordinateSystem().getXHorizAxis().read("0:1");
					double yCellSize = Math.abs(yAxisArray.getDouble(0) - yAxisArray.getDouble(1)); 
					double xCellSize = Math.abs(xAxisArray.getDouble(0) - xAxisArray.getDouble(1)); 

					LatLonRect bbox = this.grid.getCoordinateSystem().getLatLonBoundingBox();
					bounds = new Rectangle2D.Double(
							bbox.getLowerLeftPoint().getLongitude() - xCellSize/2.0, 
							bbox.getLowerLeftPoint().getLatitude() - yCellSize/2.0, 
							bbox.getWidth() + xCellSize, bbox.getHeight() + yCellSize);
					this.bounds = bounds;

				}
				else {

					double minX = this.grid.getCoordinateSystem().getXHorizAxis().getMinValue();
					double minY = this.grid.getCoordinateSystem().getYHorizAxis().getMinValue();
					double maxX = this.grid.getCoordinateSystem().getXHorizAxis().getMaxValue();
					double maxY = this.grid.getCoordinateSystem().getYHorizAxis().getMaxValue();

					Array yAxisArray = this.grid.getCoordinateSystem().getYHorizAxis().read("0:1,0:0");
					Array xAxisArray = this.grid.getCoordinateSystem().getXHorizAxis().read("0:0,0:1");
					double yCellSize = Math.abs(yAxisArray.getDouble(0) - yAxisArray.getDouble(1)); 
					double xCellSize = Math.abs(xAxisArray.getDouble(0) - xAxisArray.getDouble(1)); 

					//                    System.out.println(minX+"-"+maxX+" , "+minY+"-"+maxY);
					//                    System.out.println("xCellsize="+xCellSize+" , yCellsize="+yCellSize);



					LatLonRect bbox = this.grid.getCoordinateSystem().getLatLonBoundingBox();
					bounds = new Rectangle2D.Double(
							bbox.getLowerLeftPoint().getLongitude() - xCellSize/2.0, 
							bbox.getLowerLeftPoint().getLatitude() - yCellSize/2.0, 
							bbox.getWidth() + xCellSize, bbox.getHeight() + yCellSize);
					this.bounds = bounds;
				}
			}

			boolean doResample = forceResample;
			//            System.out.println("grid max/min longitude values: "+ grid.getCoordinateSystem().getXHorizAxis().getMinValue() + 
			//                    " / " + grid.getCoordinateSystem().getXHorizAxis().getMaxValue() );
			if (grid.getCoordinateSystem().getXHorizAxis().getMaxValue() > 180) {
				doResample = true;
			}

			// override for lat/lon non-projected data
			if ((! doResample) && this.coordSys.isLatLon() && this.coordSys.isRegularSpatial() && checkGridCacheSizeLimit()) {
				// subset grid from given bounds
				//                LatLonRect subsetBbox = new LatLonRect(new LatLonPointImpl(bounds.getMinY(), bounds.getMinX()), 
				//                        new LatLonPointImpl(bounds.getMaxY(), bounds.getMaxX()));
				//                this.grid = grid.makeSubset(null, null, subsetBbox, 1, 1, 1);

				//                System.out.println("subsetBbox: "+subsetBbox);

				this.raster = RasterFactory.createBandedRaster(
						DataBuffer.TYPE_FLOAT, 
						this.grid.getDimension(this.grid.getXDimensionIndex()).getLength(), 
						this.grid.getDimension(this.grid.getYDimensionIndex()).getLength(), 
						1, null);

				//                    // clear raster
				//                    for (int i=0; i<this.raster.getWidth(); i++) {
				//                        for (int j=0; j<this.raster.getHeight(); j++) {
				//                            this.raster.setSample(i, j, 0, Float.NaN);
				//                        }
				//                    }



				Array yAxisArray = this.grid.getCoordinateSystem().getYHorizAxis().read("0:1");
				Array xAxisArray = this.grid.getCoordinateSystem().getXHorizAxis().read("0:1");
				double yCellSize = Math.abs(yAxisArray.getDouble(0) - yAxisArray.getDouble(1)); 
				double xCellSize = Math.abs(xAxisArray.getDouble(0) - xAxisArray.getDouble(1)); 
				boolean yFlipped = yAxisArray.getDouble(0) > yAxisArray.getDouble(1);
				//                boolean xFlipped = xAxisArray.getDouble(0) > xAxisArray.getDouble(1);


				for (int i=0; i<this.raster.getWidth(); i++) {
					for (int j=0; j<this.raster.getHeight(); j++) {
						if (yFlipped) {
							this.raster.setSample(i, j, 0, getCellValue(i, j));
						}
						else {
							this.raster.setSample(i, this.raster.getHeight()-j-1, 0, getCellValue(i, j));
						}
					}
				}

				LatLonRect bbox = this.grid.getCoordinateSystem().getLatLonBoundingBox();
				bounds = new Rectangle2D.Double(
						bbox.getLowerLeftPoint().getLongitude() - xCellSize/2.0, 
						bbox.getLowerLeftPoint().getLatitude() - yCellSize/2.0, 
						bbox.getWidth() + xCellSize, bbox.getHeight() + yCellSize);

				this.proj = grid.getProjection();
				this.imgWidth = grid.getXDimension().getLength();
				this.imgHeight = grid.getYDimension().getLength();
				this.width = grid.getXDimension().getLength();
				this.height = grid.getYDimension().getLength();
				this.bounds = bounds;
				this.envelope = new Envelope(bounds);
				this.cellsize = this.bounds.getWidth() / this.raster.getWidth();
				this.longName = grid.getDescription();
				this.gridVariableName = grid.getName();
				this.units = grid.getUnitsString();
				this.isNative = true;
				this.isRegularLatLon = true;

				//                System.out.println("bbox: "+bbox);
				//                System.out.println("x cell size: "+this.bounds.getWidth() / this.raster.getWidth());
				//                System.out.println("y cell size: "+this.bounds.getHeight() / this.raster.getHeight());

				//                this.gds.close();
				return;
			}
			else {
//				this.width = DEFAULT_REMAPPED_GRID_WIDTH;
//				this.height = DEFAULT_REMAPPED_GRID_HEIGHT;
			}


			if (scanOnly) {
				//                this.gds.close();
				return;
			}




			if (forceEqualXYCellsize) {
				Dimension dim = WCTUtils.getEqualDimensions(bounds, width, height);
				width = (int)dim.getWidth();
				height = (int)dim.getHeight();  


				logger.fine("Un-Adjusted Bounds: "+bounds);

				bounds = WCTUtils.adjustGeographicBounds(new Dimension(width, height), bounds);

				logger.fine("Adjusted Bounds: "+bounds);

				double cellSizeX = bounds.width/(double)width;
				double cellSizeY = bounds.height/(double)height;

				if (Math.abs(cellSizeX - cellSizeY) > .00001) {
					throw new Exception("X/Y Cellsizes do not match - this is required for ASCII Grid Export");
				}

				this.cellsize = cellSizeX;

			}

			//System.out.println("width, height, bounds: "+width+","+height+","+bounds);

			this.bounds = bounds;
			//            TODO perhaps only create a new raster if needed?
			//            if (this.width != this.lastWidth || this.height != this.lastHeight) {

			this.raster = RasterFactory.createBandedRaster(
					DataBuffer.TYPE_FLOAT, width, height, 1, null);

			//            }



			// clear raster
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					this.raster.setSample(i, j, 0, Float.NaN);
				}
			}

			this.envelope = new Envelope(bounds);


			double cellSizeX = envelope.getLength(0)/(double)width;
			double cellSizeY = envelope.getLength(1)/(double)height;

			//            System.out.println("height="+height+" width="+width+" cellSizeX="+cellSizeX+" , cellSizeY="+cellSizeY);
			//          logger.fine("height="+height+" width="+width+" cellSizeX="+cellSizeX+" , cellSizeY="+cellSizeY);












			// ------------------------------------


			// ANSARI TEST

			int numYTiles = 1;
			int numXTiles = 1;
			int stride = 1;
			for (int zy=0; zy<numYTiles; zy++) {
				for (int zx=0; zx<numXTiles; zx++) {


					GridDatatype subsetGrid = this.grid.makeSubset(null, null, null, null, 
							new Range(zy*grid.getYDimension().getLength()/numYTiles, ((zy+1)*grid.getYDimension().getLength()/numYTiles)-1, stride),
							new Range(zx*grid.getXDimension().getLength()/numXTiles, ((zx+1)*grid.getXDimension().getLength()/numXTiles)-1, stride)
							);
					//            GridDatatype subsetGrid = this.grid.makeSubset(null, null, null, 1, 1, stride);

					GridCoordSystem subsetCoordSys = subsetGrid.getCoordinateSystem();




					//--------------------------------------------------------------------
					// If we have 2-D coordinate axes (i.e. 'swath' or 'curvilinear grid',
					// use code from ncwms project to build fast lookup table.
					//--------------------------------------------------------------------
					TwoDCoordSys coordSys2D = null;
					if (subsetGrid.getCoordinateSystem().getXHorizAxis().getRank() == 2 ||
							subsetGrid.getCoordinateSystem().getYHorizAxis().getRank() == 2) {


						//                System.out.println("generating 2-d coord system and cached lookup table");
						for (int n = 0; n < listeners.size(); n++) {
							event.setStatus("Generating Coordinate Cache...");
							listeners.get(n).decodeProgress(event);
						}
						coordSys2D = TwoDCoordSys.generate(subsetGrid.getCoordinateSystem());

						//                JOptionPane.showMessageDialog(null, "using lat/lon, not projection...");


					}





					logger.fine("REMAPPING DATA TO WGS84");

					for (int n = 0; n < listeners.size(); n++) {
						event.setStatus("Resampling...");
						listeners.get(n).decodeProgress(event);
					}


					//            ProjectionPointImpl projPoint = new ProjectionPointImpl();
					//            LatLonPointImpl llPoint = new LatLonPointImpl();
					int[] result = new int[2];





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
						if (lat > 90 || lat < -90) {
							continue;
						}

						for (int x=0; x<width; x++) {

							double lon = envelope.getMinimum(0)+x*cellSizeX+cellSizeX/2.0;

							if (coordSys2D == null) {
								// simple 1-D coordinate axes
								subsetCoordSys.findXYindexFromLatLon(lat, lon, result);
							}
							else {
								result = coordSys2D.lonLatToGrid(new LonLatPositionImpl(lon, lat));
								// null means that lat/lon is outside grid
								if (result == null) {
									continue;
								}
							}

							int row = result[1];
							int col = result[0];


							if (col >= imgWidth || row >= imgHeight ||
									col < 0 || row < 0) {

								continue;
							}

							minXIndex = (col < minXIndex) ? col : minXIndex; 
							minYIndex = (row < minYIndex) ? row : minYIndex; 
							maxXIndex = (col > maxXIndex) ? col : maxXIndex; 
							maxYIndex = (row > maxYIndex) ? row : maxYIndex; 


							//                  System.out.println("GETTING DATA FOR ("+col+" , "+row+") dims="+imgWidth+"x"+imgHeight);
							//                  logger.fine("GETTING DATA FOR ("+col+" , "+row+")");
							//                    double[] rowData = getRowData(row);

							if (! ((imgHeight-row-1) < 0 || (imgHeight-row-1) >= imgHeight ||
									col < 0 || col >= imgWidth)) {


								//                        double value = rowData[col];                        
								//                        double value = getCellValue(col, row);
								double value = getCellValue(col, row);


								if (Double.isNaN(value)) {
									continue;
								}

								if (Float.isNaN(this.raster.getSampleFloat(x, height-y-1, 0)) ||
										//                                this.raster.getSampleFloat(x, height-y-1, 0) == Float.NaN ||
										value > this.raster.getSampleFloat(x, height-y-1, 0) ) { 

									//                            if (value > 0) {
									//                               System.out.println(value + " :-: " + this.raster.getSampleFloat(x, height-y-1, 0));
									//                            }

									this.raster.setSample(x, height-y-1, 0, value);

									if (! Double.isNaN(value) && value < minValue) {
										minValue = value;
									}
									if (! Double.isNaN(value) && value > maxValue) {
										maxValue = value;
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


					// end ANSARI test loop
				}
			}




			hasValidRange = grid.getVariable().hasInvalidData();
			validRangeMinValue = grid.getVariable().getValidMin();
			validRangeMaxValue = grid.getVariable().getValidMax();

//			System.out.println("      histo max/min: "+gridMinValue+" / "+gridMaxValue);
//			System.out.println("valid_range max/min: "+hasValidRange+" -- "+validRangeMinValue+" / "+validRangeMaxValue);





			// Do smoothing if needed
			if (smoothingFactor > 0) {
				WCTGridCoverageSupport gcSupport = new WCTGridCoverageSupport();
				this.raster = gcSupport.getSmoothedRaster(this, smoothingFactor, new double[0]);
			}

			//            this.gds.close();

		} catch (WCTNoGridsFoundException e) {
			throw e;            
		} catch (Exception e) {
			e.printStackTrace();
			logger.fine("\n\nSubset String: "+lineSectionString);
			throw new IOException(e.getMessage());
		} finally {
			//        	if (this.gds != null) {
			//        		this.gds.close();
			//        	}

			//          logger.fine("NUM FILE READS: "+numFileReads+"  NUM REPEAT ROWS: "+numRepeatRows+"  NUM CACHE READS: "+numCacheReads);
			logger.info("NUM FILE READS: "+numFileReads+"  NUM CACHE READS: "+numCacheReads);
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



		isNewGrid = false;
		lastDecodedFile = source;

	}


	/**
	 * Returns true if native grid size is under the limit, which allows caching.
	 * @return
	 */
	private boolean checkGridCacheSizeLimit() {        
		return ! (this.coordSys.getXHorizAxis().getSize() * 
				this.coordSys.getYHorizAxis().getSize() > NATIVE_GRID_CACHE_SIZE_LIMIT);        
	}


	public void calculateStatistics(String source) throws Exception {

		this.gridMaxValue = STARTING_MAX_VALUE;
		this.gridMinValue = STARTING_MIN_VALUE;

		StringBuilder errlog = new StringBuilder();
		this.gds = GridDatasetUtils.openGridDataset(source, errlog);
		if (this.gds == null) { 
			throw new Exception("Can't open Grid Dataset at location= "+source+"; error message= "+errlog);
		}
		if (this.gds.getGrids().size() == 0) { 
			throw new Exception("No Grids found in file: "+source);
		}

		gridIndex = (getGridIndex() < 0) ? 0 : getGridIndex();
		runtimeIndex = (getRuntimeIndex() < 0) ? 0 : getRuntimeIndex();
		timeIndex = (getTimeIndex() < 0) ? 0 : getTimeIndex();
		zIndex = (getZIndex() < 0) ? 0 : getZIndex();

		this.grid = gds.getGrids().get(gridIndex);

		calculateStatistics();
	}



	public void calculateStatistics() throws Exception {


		int ncols = grid.getXDimension().getLength();
		int nrows = grid.getYDimension().getLength();

		int cellNum = nrows * ncols;
		int rowsRead = 0;
		int maxCellRead = 4*500000;
		int rowsToRead = (int) maxCellRead/ncols;
		System.out.println("rowsToRead: "+rowsToRead);
		while (rowsRead < nrows) {
			if (rowsRead+rowsToRead > nrows) {
				rowsToRead = nrows-rowsRead;
			}

			System.out.println("rowsRead: "+rowsRead);
			GridDatatype subsetGrid = grid.makeSubset(null, null, 
					new Range(timeIndex, timeIndex), 
					new Range(zIndex, zIndex), 
					new Range(rowsRead, rowsRead+rowsToRead-1), 
					null);

			MinMax minMax = subsetGrid.getMinMaxSkipMissingData( subsetGrid.readDataSlice(-1, -1, -1, -1) );
			System.out.println("chunk ("+rowsRead+" to "+(rowsRead+rowsToRead)+") min/max: "+minMax.min + " / " + minMax.max);
			if (gridMaxValue < minMax.max) {
				gridMaxValue = minMax.max;
			}
			if (gridMinValue > minMax.min) {
				gridMinValue = minMax.min;
			}

			//            double[] data = (double[]) grid.readDataSlice(timeIndex, zIndex, row, -1).get1DJavaArray(Double.class);

			rowsRead += rowsToRead; 
		}


		hasValidRange = grid.getVariable().hasInvalidData();
		validRangeMinValue = grid.getVariable().getValidMin();
		validRangeMaxValue = grid.getVariable().getValidMax();

		System.out.println("      histo max/min: "+gridMinValue+" / "+gridMaxValue);
		System.out.println("valid_range max/min: "+hasValidRange+" -- "+validRangeMinValue+" / "+validRangeMaxValue);

		//        this.gds.close();
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
	private double[] getRowData(int row) throws IOException, InvalidRangeException {

		lastRowRead = row;

		if (! rowDataMap.containsKey(row)) {

			rowData = (double[]) grid.readDataSlice(timeIndex, zIndex, row, -1).get1DJavaArray(Double.class);
			rowDataMap.put(row, rowData);
			rowCacheOrder.add(row);

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




	/**
	 * Read a chunk of rows of data from disk.  Cache the rows to a HashMap.  If the number
	 * of cached rows exceeds the ROW_CACHE_SIZE value, remove the oldest row from
	 * cache.
	 * @param row
	 * @return
	 * @throws Exception 
	 */
	private double[] getRowData(int row, int rowChunkSize) throws Exception {

		lastRowRead = row;

		if (! rowDataMap.containsKey(row)) {

			GridDatatype subsetGrid = grid.makeSubset(
					null, // runtime 
					null, // e
					new Range(timeIndex, timeIndex), 
					new Range(zIndex, zIndex), 
					new Range(row, row+rowChunkSize-1), 
					null // x
					);


			rowData = (double[]) subsetGrid.readDataSlice(-1, -1, -1, -1).get1DJavaArray(Double.class);
			if (rowData.length != rowChunkSize*grid.getXDimension().getLength()) {
				throw new Exception("Data array size read from disk ( "+rowData.length+" ) doesn't match expected: "+rowChunkSize*grid.getXDimension().getLength());
			}

			for (int n=0; n<rowChunkSize; n++) {
				double[] singleRowData = new double[grid.getXDimension().getLength()];
				System.arraycopy(rowData, n*grid.getXDimension().getLength(), singleRowData, 0, grid.getXDimension().getLength());
				rowDataMap.put(row, singleRowData);
				rowCacheOrder.add(row);

				//  clear oldest if needed
				if (rowDataMap.size() > ROW_CACHE_SIZE) {
					logger.fine("CACHE SIZE REACHED, REMOVING ROW "+rowCacheOrder.get(0));
					rowDataMap.remove(rowCacheOrder.get(0));
					rowCacheOrder.remove(0);
				}
			}

			numFileReads++;
			return rowData;
		}
		else {
			numCacheReads++;
			return rowDataMap.get(new Integer(row));
		}

	}


	private void populateDataCache() throws InvalidRangeException, IOException {

		Range rtRange = (runtimeIndex >= 0) ? new Range(runtimeIndex, runtimeIndex) : new Range(0, 0);
		Range timeRange = (timeIndex >= 0) ? new Range(timeIndex, timeIndex) : new Range(0, 0);
		Range zRange = (zIndex >= 0) ? new Range(zIndex, zIndex) : new Range(0, 0);

		GridDatatype subsetGrid = null;
		if (grid.getRank() == 2) {
			subsetGrid = grid;
		}
		else {
			subsetGrid = grid.makeSubset(
					rtRange, // runtime
					null, // e 
					timeRange, 
					zRange, 
					null, 
					null);
		}

		//        System.out.println("is lat/lon: "+subsetGrid.getCoordinateSystem().isLatLon());
		//        System.out.println("index x: "+subsetGrid.getCoordinateSystem().getProjection().INDEX_X);
		//        System.out.println("index y: "+subsetGrid.getCoordinateSystem().getProjection().INDEX_Y);
		//        System.out.println("y axis: "+subsetGrid.getCoordinateSystem().getYHorizAxis().toString());

		// Thought we might need to check for flip but I don't think we need to...
		//        yAxisSize = subsetGrid.getCoordinateSystem().getYHorizAxis().getSize();
		//        if (yAxisSize > 1) {
		//            Array yAxisArray = subsetGrid.getCoordinateSystem().getYHorizAxis().read("0:1");
		//                        System.out.println("y[0]: "+yAxisArray.getDouble(0));
		//                        System.out.println("y[1]: "+yAxisArray.getDouble(1));
		//            isYAxisFlipped = yAxisArray.getDouble(0) > yAxisArray.getDouble(1);
		//        }

		//        System.out.println("\n\n\nOriginal Variable:");
		//        System.out.println(grid.getVariable().writeCDL("  ", true, false));
		//        System.out.println("\nSubset Variable");
		//        System.out.println(subsetGrid.getVariable().writeCDL("  ", true, false));


		for (int n = 0; n < listeners.size(); n++) {
			event.setStatus("Reading Data Into Cache...");
			listeners.get(n).decodeProgress(event);
		}
		//        System.out.print("READING GRID INTO CACHE");
		if (grid.getRank() == 2) {
			dataCache = subsetGrid.getVariable().read();
		}
		else {
			dataCache = subsetGrid.readDataSlice(-1, -1, -1, -1).reduce();
		}
		//        System.out.println(" ... DONE!");

		for (int n = 0; n < listeners.size(); n++) {
			event.setStatus("Scanning Grid for Statistics...");
			listeners.get(n).decodeProgress(event);
		}
		//        System.out.print("SCANNING GRID FOR STATISTICS");
		MinMax minMax = subsetGrid.getMinMaxSkipMissingData(dataCache);
		gridMinValue = minMax.min;
		gridMaxValue = minMax.max;
		//        System.out.print("  min/max="+minMax.min+"/"+minMax.max);
		//        System.out.println(" ... DONE!");

		isEmptyGrid = (gridMinValue > gridMaxValue);

		hasValidRange = grid.getVariable().hasInvalidData();
		validRangeMinValue = grid.getVariable().getValidMin();
		validRangeMaxValue = grid.getVariable().getValidMax();


		for (int n = 0; n < listeners.size(); n++) {
			event.setStatus("Resampling...");
			listeners.get(n).decodeProgress(event);
		}
	}


	private double getCellValue(int x, int y) throws Exception {

		boolean cacheGrid = true;
		double value;

		if (cacheGrid) {

			if (dataCache == null) {
				populateDataCache();
			}     


			//        if (isYAxisFlipped) {
			//            value = dataCache.getDouble(dataCache.getIndex().set((int)yAxisSize-y-1, x));
			//        }
			//        else {
			value = dataCache.getDouble(dataCache.getIndex().set(y, x));
			//        }



		}
		else {                  
			value = getRowData(y)[x];
		}






		//        if (value < minValueFilter || value > maxValueFilter) {
		if (! wctFilter.accept(value)) {
			return Double.NaN;
		}
		else {
			return value;
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


	public GridCoverage getGridCoverage(int alpha) {
		this.currentAlpha = alpha;


		if (minValue >= maxValue) {
			//            throw new Exception("All data is undefined for this geographic extent.");
			maxValue = 0.000001;
			minValue = -0.000001;
			//            isEmptyGrid = true;
		}
		if (gridMinValue >= gridMaxValue) {

			//          throw new Exception("All data is undefined for this geographic extent.");
			gridMaxValue = 0.000001;
			gridMinValue = -0.000001;
			displayMinValue = -1;
			displayMaxValue = 1;
			//            isEmptyGrid = true;
		}
		else {
			//            isEmptyGrid = false;
			if (isAutoMinMax) {
				displayMinValue = gridMinValue;
				displayMaxValue = gridMaxValue;
			}
		}


		//        System.out.println("size of value array: "+values.length+" : values "+values[0]+" to "+values[values.length-1]);

		if (displayColors == null) {
			displayColors = new Color[] { Color.BLUE.darker().darker(), Color.BLUE, Color.BLUE.brighter().brighter(), Color.GREEN, 
					Color.YELLOW, Color.ORANGE, Color.RED };
		}
		Double[] values = new Double[] { displayMinValue, displayMaxValue };

		if (alpha != -1) {
			displayColors = WCTUtils.applyAlphaFactor(displayColors, alpha);
		}

		double[] minValues = new double[] { values[0] };
		double[] maxValues = new double[] { values[values.length-1] };

		//        if (iospClassString.endsWith("Giniiosp")) {
		//            minValues = new double[] { 0 };
		//            maxValues = new double[] { 254 };
		//            WCTUtils.flipArray(colors);
		//        }

		//        System.out.println("gc width/height: "+this.raster.getWidth()+"/"+this.raster.getHeight());
		//        System.out.println("gc bounds: "+envelope);
		//        System.out.println("gc values min: "+minValue+"  max: "+maxValue);
		//        System.out.println("gc indices minX: "+minXIndex+"  maxX: "+maxXIndex);
		//        System.out.println("gc indices minY: "+minYIndex+"  maxY: "+maxYIndex);


		// for npp testing
		if (this.coordSys == null) {
			this.gc = new GridCoverage("Remapped Swath Image", this.raster, 
					GeographicCoordinateSystem.WGS84, envelope, minValues, 
					maxValues, null, new Color[][]{ displayColors }, null);
		}        

		// override for lat/lon non-projected data
		else if (this.coordSys.isLatLon() && checkGridCacheSizeLimit()) {
			//        if (this.coordSys.isLatLon() && checkGridCacheSizeLimit()) {
			//            if (this.gc != null && this.gc.getName(null).equals("Native Lat/Lon Grid Image")) {
			//                return this.gc;
			//            }
			this.gc = new GridCoverage("Native Lat/Lon Grid Image", this.raster, 
					GeographicCoordinateSystem.WGS84, envelope, minValues, 
					maxValues, null, new Color[][]{ displayColors }, null);
		}
		else {
			this.gc = new GridCoverage("Remapped Grid Image", this.raster, 
					GeographicCoordinateSystem.WGS84, envelope, minValues, 
					maxValues, null, new Color[][]{ displayColors }, null);

		}

		return gc;
	}

	public boolean hasValidRangeAttributes() {
		//    	return validRangeMaxValue != STARTING_MAX_VALUE &&
		//    			validRangeMinValue != STARTING_MIN_VALUE;
		return hasValidRange;
	}

	public GridCoverage getGridCoverage() {
		return getGridCoverage(255);
	}

	public double getMinValue() {
		return minValue;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public double getGridMinValue() {
		return gridMinValue;
	}

	public double getGridMaxValue() {
		return gridMaxValue;
	}

	public double getValidRangeMinValue() {
		return validRangeMinValue;
	}

	public double getValidRangeMaxValue() {
		return validRangeMaxValue;
	}




	public void setDisplayMinValue(double displayMinValue) {
		this.displayMinValue = displayMinValue;
	}


	public double getDisplayMinValue() {
		return displayMinValue;
	}


	public void setDisplayMaxValue(double displayMaxValue) {
		this.displayMaxValue = displayMaxValue;
	}


	public double getDisplayMaxValue() {
		return displayMaxValue;
	}


	/**
	 * Returns bounds of last processed data
	 */
	 public java.awt.geom.Rectangle2D.Double getBounds() {
		return bounds;
	 }

	 /**
	  * Resets the bounds (extent) of the raster, so it is automatically 
	  * calculated from the extent of the grid during the next decode.
	  */
	 public void resetBounds() {
		 this.bounds = null;
	 }


	 public double getCellWidth() {
		 return cellsize;
	 }
	 public double getCellHeight() {
		 return cellsize;
	 }



	 public long getDateInMilliseconds() {
		 if (datetime != null) {
			 return datetime.getTime();
		 } 
		 else {
			 return Long.MIN_VALUE;
		 }
	 }

	 public String getLongName() {
		 return longName;
	 }

	 public double getNoDataValue() {
		 return Float.NaN;
	 }

	 public String getUnits() {
		 return units;
	 }

	 public WritableRaster getWritableRaster() {
		 return raster;
	 }

	 public boolean isEmptyGrid() {
		 return isEmptyGrid;
	 }

	 /**
	  * Always returns false if this data has been remapped.
	  */
	  public boolean isNative() {
		  return isNative;
	  }

	  public boolean isRegularLatLon() {
		  return isRegularLatLon;
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

	  
	  /**
	   * Will reset the minValue and maxValue values for the underlying resampled raster.
	   */
	  public void setWritableRaster(WritableRaster raster) {
		  this.raster = raster;
		  resetMinMaxFromRaster();
	  }
	  
	  private void resetMinMaxFromRaster() {
		  minValue = Double.MAX_VALUE;
		  maxValue = Double.MIN_VALUE;
		  for (int j=0; j<this.raster.getHeight(); j++) {
			  for (int i=0; i<this.raster.getWidth(); i++) {
				  minValue = (this.raster.getSampleDouble(i, j, 0) < minValue) 
						  ? this.raster.getSampleDouble(i, j, 0) : minValue;
				  maxValue = (this.raster.getSampleDouble(i, j, 0) < maxValue) 
						  ? this.raster.getSampleDouble(i, j, 0) : maxValue;
			  }
		  }
	  }

	  public void setBounds(Rectangle2D.Double bounds) {
		  this.bounds = bounds;
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
		  return "GridDatasetRemappedRaster: \n"+
				  "Height="+getHeight()+" Width="+getWidth()+
				  "\nCell Width="+getCellWidth()+" Cell Height="+getCellHeight()+"\n"+
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



	  public void setRuntimeIndex(int runtimeIndex) {
		  if (this.runtimeIndex != runtimeIndex && runtimeIndex >= 0) {
			  isNewGrid = true;
			  System.out.println("runtimeIndex change ("+this.runtimeIndex+" to "+runtimeIndex+"): SET isNewGrid to true");
		  }
		  this.runtimeIndex = runtimeIndex;
	  }


	  public int getRuntimeIndex() {
		  return runtimeIndex;
	  }


	  public void setTimeIndex(int timeIndex) {
		  if (this.timeIndex != timeIndex && timeIndex >= 0) {
			  isNewGrid = true;
			  System.out.println("timeIndex change ("+this.timeIndex+" to "+timeIndex+"): SET isNewGrid to true");
		  }
		  this.timeIndex = timeIndex;
	  }


	  public int getTimeIndex() {
		  return timeIndex;
	  }


	  public void setZIndex(int zIndex) {
		  if (this.zIndex != zIndex && zIndex >= 0) {
			  isNewGrid = true;
			  System.out.println("zIndex change ("+this.zIndex+" to "+zIndex+"): SET isNewGrid to true");
		  }
		  this.zIndex = zIndex;
	  }


	  public int getZIndex() {
		  return zIndex;
	  }


	  public void setGridIndex(int gridIndex) {
		  this.setVariableName(null);
		  if (this.gridIndex != gridIndex && gridIndex >= 0) {
			  isNewGrid = true;
			  System.out.println("gridIndex change ("+this.gridIndex+" to "+gridIndex+"): SET isNewGrid to true");
		  }
		  this.gridIndex = gridIndex;
	  }


	  public int getGridIndex() {
		  return gridIndex;
	  }


	  //    public void setGridVariableName(String gridVariableName) {
	  //        this.gridVariableName = gridVariableName;
	  //    }
	  //
	  //
	  //    public String getGridVariableName() {
	  //        return gridVariableName;
	  //    }


	  public ucar.nc2.dt.GridDataset getLastProcessedGridDataset() {
		  return gds;
	  }

	  public GridDatatype getLastProcessedGridDatatype() {
		  return grid;
	  }

	  public GridCoordSystem getLastProcessedGridCoordSystem() {
		  return coordSys;
	  }

	  public String getLastProcessedFileTypeDescription() {
		  return fileTypeDesc;
	  }

	  public Date getLastProcessedDateTime() {
		  if (this.timeIndex < 0) {
			  return null;
		  }
		  try {
			  return this.coordSys.getTimeAxis1D().getTimeDate(this.timeIndex);
		  } catch (Exception e) {
			  return null;
		  }
	  }

	  public Date getLastProcessedRuntime() {
		  if (this.runtimeIndex < 0) {
			  return null;
		  }
		  try {
			  return this.coordSys.getRunTimeAxis().getTimeDate(this.runtimeIndex);
		  } catch (Exception e) {
			  return null;
		  }
	  }

	  public double getLastProcessedHeight() {
		  if (this.zIndex < 0) {
			  return Double.NaN;
		  }
		  if (this.coordSys.getVerticalAxis() == null) {
			  return Double.NaN;
		  }
		  try {

			  //        	CoordinateAxis1D ca1d = this.coordSys.getVerticalAxis();
			  //        	System.out.println("size= "+ca1d.getSize());
			  //        	System.out.println(" val= "+ca1d.getCoordValue(0));

			  //        	return this.coordSys.getVerticalAxis().getCoordValue(this.zIndex);


			  return zCoordVals[zIndex];



		  } catch (Exception e) {
			  return Double.NaN;
		  }
	  }


	  public void setAutoMinMaxValues(boolean isAutoMinMax) {
		  this.isAutoMinMax = isAutoMinMax;        
	  }
	  public boolean isAutoMinMaxValues() {
		  return this.isAutoMinMax;
	  }


	  public void setDisplayColors(Color[] displayColors) {
		  this.displayColors = displayColors;
	  }


	  public Color[] getDisplayColors() {
		  return displayColors;
	  }



	  @Override
	  public String getStandardName() {
		  return this.standardName;
	  }


	  @Override
	  public String getVariableName() {
		  return this.gridVariableName;
	  }


	  @Override
	  public void setStandardName(String standardName) {
		  this.standardName = standardName;        
	  }


	  @Override
	  public void setVariableName(String variable) {
		  if (variable == null || gridVariableName == null) {
			  dataCache = null;
		  }
		  else if (! variable.equals(gridVariableName)) {
			  dataCache = null;
		  }
		  this.gridVariableName = variable;        
	  }


	  public void setMinValueFilter(double[] minValueFilter) {
		  this.wctFilter.setMinValue(minValueFilter);
	  }


	  public double[] getMinValueFilter() {
		  return wctFilter.getMinValue();
	  }


	  public void setMaxValueFilter(double[] maxValueFilter) {
		  this.wctFilter.setMaxValue(maxValueFilter);
	  }


	  public double[] getMaxValueFilter() {
		  return wctFilter.getMaxValue();
	  }

	  public void setForceResample(boolean forceResample) {
		  this.forceResample = forceResample;
	  }

	  public boolean isForceResample() {
		  return forceResample;
	  }

}
