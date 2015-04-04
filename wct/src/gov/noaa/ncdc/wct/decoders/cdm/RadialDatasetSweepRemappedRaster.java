package gov.noaa.ncdc.wct.decoders.cdm;

import gov.noaa.ncdc.nexrad.NexradEquations;
import gov.noaa.ncdc.wct.WCTException;
import gov.noaa.ncdc.wct.WCTFilter;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.DecodeHintNotSupportedException;
import gov.noaa.ncdc.wct.decoders.SampleDimensionAndLabels;
import gov.noaa.ncdc.wct.decoders.cdm.SmoothingOperation.SmoothingInfo;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradColorFactory;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradSampleDimensionFactory;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradValueFactory;
import gov.noaa.ncdc.wct.decoders.nexrad.RadarHashtables;
import gov.noaa.ncdc.wct.decoders.nexrad.WCTProjections;
import gov.noaa.ncdc.wct.event.DataDecodeEvent;
import gov.noaa.ncdc.wct.event.DataDecodeListener;
import gov.noaa.ncdc.wct.export.raster.FeatureRasterizerException;
import gov.noaa.ncdc.wct.export.raster.WCTGridCoverageSupport;
import gov.noaa.ncdc.wct.export.raster.WCTGridCoverageSupport.AlphaInterpolationType;
import gov.noaa.ncdc.wct.export.raster.WCTRaster;
import gov.noaa.ncdc.wct.io.FileScanner;
import gov.noaa.ncdc.wct.io.SupportedDataType;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import javax.media.jai.JAI;
import javax.media.jai.KernelJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RasterFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.ct.MathTransform;
import org.geotools.cv.SampleDimension;
import org.geotools.gc.GridCoverage;
import org.geotools.pt.Envelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.xml.sax.SAXException;

import ucar.nc2.Attribute;
import ucar.nc2.dt.RadialDatasetSweep;
import ucar.nc2.dt.RadialDatasetSweep.RadialVariable;
import ucar.nc2.dt.RadialDatasetSweep.Sweep;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.util.CancelTask;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.geoloc.ProjectionPointImpl;
import ucar.unidata.geoloc.projection.FlatEarth;
import ucar.unidata.io.RandomAccessFile;

import com.vividsolutions.jts.index.strtree.SIRtree;


public class RadialDatasetSweepRemappedRaster implements WCTRaster {

	private static final Logger logger = Logger.getLogger(RadialDatasetSweepRemappedRaster.class.getName());

	public enum CAPPIType { NEAREST_SWEEP, LINEAR_WEIGHTED_AVERAGE, INVERSE_HEIGHT_DIFFERENCE_WEIGHTED_SQUARED, 
		NEAREST_ELEVATION_ANGLE, DIST_TO_SWEEP_CENTER };

	public static final int ROW_CACHE_SIZE = 500;
	public static final java.awt.geom.Rectangle2D.Double GOES_DEFAULT_EXTENT = 
			new java.awt.geom.Rectangle2D.Double(-135.0, 12.0, 78.0, 55.0);

	public static final int STARTING_MIN_VALUE = 999999999;
	public static final int STARTING_MAX_VALUE = -999999999;

	private WCTProjections nexradProjection = new WCTProjections();

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
	private int width = 1200;
	private int height = 1200;

	private double minValue = STARTING_MIN_VALUE;
	private double maxValue = STARTING_MAX_VALUE; 
	private double gridMinValue = STARTING_MIN_VALUE;
	private double gridMaxValue = STARTING_MAX_VALUE; 
	private int maxXIndex = -1;
	private int minXIndex = 100000000;
	private int maxYIndex = -1;
	private int minYIndex = 100000000;


	private Envelope envelope;

	private Vector<DataDecodeListener> listeners = new Vector<DataDecodeListener>();

	private int currentAlpha;
	
	private float[] dataCache = null;

	private Date datetime;
	private String longName = " ";
	private String units = " ";
	private String standardName = "";
	private String variableName = "";


	private RadialDatasetSweep radialDataset = null;
	private RadialDatasetSweep.RadialVariable radialVar = null;
	//    private SIRtree azLookUpTree = null;
	private int[] azLutArray = null;


	private int sweepIndex = 0;
	private WCTFilter wctFilter = null;
	private int smoothingFactor = 0;
	
	private String lastVariableName = "";
	private int lastSweepIndex = -1;
	private double lastElevationAngle = -99;
	private Date lastSweepTime = null;
	private double[] lastCappiHeightsInMeters = null;


	private String lastDecodedFile = null;
	private boolean isNewFile = true;
	private boolean isNewVariable = true;
	private boolean isNewSweep = true;

	//    private Map<String, Object> hintsMap;


	DecodeRadialDatasetSweepHeader sweepHeader; 
	//	DecodeRadialDatasetSweep sweepDecoder;
	//	WCTRasterizer rasterizer = new WCTRasterizer(800, 800);


	private double noDataValue = Double.NaN;

	public RadialDatasetSweepRemappedRaster() throws IllegalAccessException, InstantiationException {
		init();
	}

	private void init() {

		//        hintsMap = new HashMap<String, Object>();
		//        hintsMap.put("startSweep", new Integer(0));
		//        hintsMap.put("endSweep", new Integer(0));
		//
		//        // default NexradFilter for Level-II data
		//        WCTFilter nxfilter = new WCTFilter();
		//        nxfilter.setMinValue(-500.0);
		//        hintsMap.put("nexradFilter", nxfilter);
		//
		//        // TODO: instead of using preset classifications, allow user
		//        // to input custom classification values
		//        hintsMap.put("classify", new Boolean(false));

	}


	public void process(String source) throws Exception {
		process(source, null);
	}


	//    public void process(String source, java.awt.geom.Rectangle2D.Double bounds) 
	//    	throws WCTException, IOException, NumberFormatException, XPathExpressionException, 
	//    	SAXException, ParserConfigurationException, DecodeException, SQLException, 
	//    	ParseException, FactoryException, TransformException {
	//        
	//    	process(source, bounds, 0.0);
	//    }

	public synchronized void process(String source, java.awt.geom.Rectangle2D.Double bounds) 
			throws WCTException, IOException, NumberFormatException, XPathExpressionException, SAXException, 
			ParserConfigurationException, DecodeException, SQLException, ParseException, 
			FactoryException, TransformException {





		RandomAccessFile.setDebugLeaks(true);






		if (this.variableName == null) {
			throw new WCTException("Error: The variableName has not been specified.");
		}

		// set cappi to null
		lastCappiHeightsInMeters = null;


		// something's not right, so force a full read each time
		//        isNewFile = true;
		//        isNewVariable = true;
		//        isNewSweep = true;



		if (lastDecodedFile == null || ! lastDecodedFile.equals(source)) {
			//            calculateStatistics(source);
			isNewFile = true;
			isNewVariable = true;
			isNewSweep = true;

			lastDecodedFile = source;
			dataCache = null;
		}

		this.bounds = bounds;
		String lineSectionString = "empty";
		minValue = 999999999;
		maxValue = -999999999;         
		maxXIndex = -1;
		minXIndex = 100000000;
		maxYIndex = -1;
		minYIndex = 100000000;


		logger.fine("REMAP BOUNDS: "+bounds);




		DataDecodeEvent event = new DataDecodeEvent(this);
		try {

			// Start decode
			// --------------
			for (int i = 0; i < listeners.size(); i++) {
				event.setProgress(0);
				listeners.get(i).decodeStarted(event);
			}








			isNewFile = true;
			isNewSweep = true;
			isNewVariable = true;



			//            isNewSweep = (sweepIndex != lastSweepIndex);
			isNewSweep = true;
			if (isNewFile) {
				//                System.out.println("new file: sweep: "+sweepIndex+" var: "+variableName);

				// Open File
				CancelTask cancelTask = new CancelTask() {
					@Override
					public boolean isCancel() {
						return false;
					}
					@Override
					public void setError(String msg) {
					}
					@Override
					public void setProgress(String arg0, int arg1) {						
					}
				};

				//				System.out.println(RandomAccessFile.getOpenFiles());

				Formatter errlog = new Formatter(new StringBuilder());
				radialDataset = (RadialDatasetSweep) FeatureDatasetFactoryManager.open(
						ucar.nc2.constants.FeatureType.RADIAL, 
						source, cancelTask, errlog);

				//				System.out.println(RandomAccessFile.getOpenFiles());

				sweepHeader = new DecodeRadialDatasetSweepHeader();
				sweepHeader.setRadialDatasetSweep(radialDataset);


				isNewFile = false;
				isNewVariable = true;
				isNewSweep = true;
			}
			if (isNewVariable) {            
				//                System.out.println("new variable: sweep: "+sweepIndex+" var: "+variableName);

				//  Get Radial Variable
				this.radialVar = (RadialDatasetSweep.RadialVariable) radialDataset.getDataVariable(this.variableName);
				if (this.radialVar == null) {
					throw new WCTException("The variable '"+this.variableName+"' was not found.\n" +
							"Available variables are: "+radialDataset.getDataVariables());
				}

				isNewVariable = false;
				isNewSweep = true;
			}











//			Variable v = radialDataset.getNetcdfFile().findVariable(variableName);

			




			this.units = "N/A";
			double rangeFoldedValue = Double.NEGATIVE_INFINITY;
			List<Attribute> attList = radialVar.getAttributes();
			for (Attribute a : attList) {
				if (a.getName().equals("range_folded_value")) {
					rangeFoldedValue = a.getNumericValue().doubleValue();
				}
				if (a.getName().equals("units")) {
					this.units = a.getStringValue();
				}
			}
			//			            System.out.println("attributes read: units="+units+"  range_folded_value="+rangeFoldedValue);





			this.datetime = radialDataset.getStartDate();
			this.longName = radialDataset.getDescription();


			try {
				String iospClassString = radialDataset.getNetcdfFile().getIosp().getClass().toString();

				//                    System.out.println(grid.getName()+" :: DATE: "+datetime+"\n :: PROJ: "+proj.paramsToString()+
				//                        "\n :: "+proj.getClassName()+"\n :: BBOX: "+gds.getBoundingBox().toString2());

			} catch (Exception e) {}

			if (bounds == null) {
				LatLonRect bbox = radialDataset.getBoundingBox();
				bounds = new Rectangle2D.Double(bbox.getLowerLeftPoint().getLongitude(), bbox.getLowerLeftPoint().getLatitude(), bbox.getWidth(), bbox.getHeight());
			}






			if (forceEqualXYCellsize) {
				Dimension dim = getEqualDimensions(bounds, width, height);
				width = (int)dim.getWidth();
				height = (int)dim.getHeight();  


				logger.fine("Un-Adjusted Bounds: "+bounds);

				bounds = adjustGeographicBounds(new Dimension(width, height), bounds);

				logger.fine("Adjusted Bounds: "+bounds);

				double cellSizeX = bounds.width/(double)width;
				double cellSizeY = bounds.height/(double)height;

				if (Math.abs(cellSizeX - cellSizeY) > .00001) {
					throw new WCTException("X/Y Cellsizes do not match - this is required for ASCII Grid Export");
				}

				this.cellsize = cellSizeX;

			}


			// only create a new one if we need it!
			if (this.raster == null || 
					width != this.raster.getWidth() || height != this.raster.getHeight()) {
				this.raster = RasterFactory.createBandedRaster(
						DataBuffer.TYPE_FLOAT, width, height, 1, null);
			}

			// clear raster
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					this.raster.setSample(i, j, 0, Float.NaN);
				}
			}

			this.envelope = new Envelope(bounds);

			// reset bounds
			this.bounds = bounds;


			double cellSizeX = envelope.getLength(0)/(double)width;
			double cellSizeY = envelope.getLength(1)/(double)height;

			//            System.out.println("height="+height+" width="+width+" cellSizeX="+cellSizeX+" , cellSizeY="+cellSizeY);
			logger.fine("height="+height+" width="+width+" cellSizeX="+cellSizeX+" , cellSizeY="+cellSizeY);










			// ------------------------------------


			logger.fine("REMAPPING DATA TO WGS84");


			for (int n = 0; n < listeners.size(); n++) {
				event.setStatus("Resampling...");
				listeners.get(n).decodeProgress(event);
			}


			//            ProjectionPointImpl projPoint = new ProjectionPointImpl();
			//            LatLonPointImpl llPoint = new LatLonPointImpl();
			//            int[] result = new int[2];

			Sweep sweep = radialVar.getSweep(sweepIndex);


			//            System.out.println(sweep.getRadialNumber() + " radials found for sweep index="+sweepIndex);


			this.lastElevationAngle = sweep.getMeanElevation();
			this.lastSweepTime = radialDataset.getTimeUnits().makeDate(sweep.getTime(180));
			this.lastSweepIndex = sweepIndex;
			this.lastVariableName = variableName;
			HashMap<String, String> metadataMap = new HashMap<String, String>();
			metadataMap.put("elevAngle", String.valueOf(this.lastElevationAngle));
			metadataMap.put("sweepIndex", String.valueOf(this.lastSweepIndex));
			metadataMap.put("variableName", String.valueOf(this.lastVariableName));

			for (int i = 0; i < listeners.size(); i++) {
				event.setDataType(SupportedDataType.RADIAL);
				event.setDecodeMetadataMap(metadataMap);
				listeners.get(i).metadataUpdate(event);
			}







			if (isNewSweep) {

				//                System.out.println("new sweep: sweep: "+sweepIndex+" var: "+variableName);

				//-----------------------------------------------------------
				//  Load data from sweep into array
				//-----------------------------------------------------------
				dataCache = sweep.readData();


				float minValue = Float.MAX_VALUE;
				float maxValue = Float.MIN_VALUE;
				for (float val : dataCache) {
					if (val > gridMaxValue) gridMaxValue = val;
					if (val < gridMinValue) gridMinValue = val;
				}
				//                System.out.println("read data complete.  sweep min/max="+gridMinValue+"/"+gridMaxValue);


				//-----------------------------------------------------------
				//  Load possibly unequally spaced azimuth values into SIRtree
				//-----------------------------------------------------------
				//                azLookUpTree = getAzimuthLookupTree(sweep);
				azLutArray = getAzimuthLookupArray(sweep);

				isNewSweep = false;
			}

			//-----------------------------------------------------------
			// Now conduct nearest neighbor resampling reproject data
			//-----------------------------------------------------------
			String id = radialDataset.getRadarID();
			if (id.equals("XXXX")) {
				FileScanner fileScanner = new FileScanner();
				fileScanner.scanURL(new URL(source));
				id = fileScanner.getLastScanResult().getSourceID();
			}
			double siteLat = RadarHashtables.getSharedInstance().getLat(id);
			double siteLon = RadarHashtables.getSharedInstance().getLon(id);
			double siteAlt = RadarHashtables.getSharedInstance().getElev(id);
			if (siteLat == -999 || siteLon == -999) {
				siteLat = radialDataset.getCommonOrigin().getLatitude();
				siteLon = radialDataset.getCommonOrigin().getLongitude();
				siteAlt = radialDataset.getCommonOrigin().getAltitude()*3.28083989501312;
			}

			if (siteLat == 0 && siteLon == 0) {
				id = sweepHeader.getICAO();
				siteLat = sweepHeader.getLat();
				siteLon = sweepHeader.getLon();
				siteAlt = sweepHeader.getAlt();
			}

			if (siteLat == 0 && siteLon == 0) {
				throw new DecodeException("No site location lat/lon found in lookup tables or file for "+id+".  " +
						"Please report this to ncdc.info@noaa.gov .", new URL(source));
			}


			double elevation_cos = Math.cos(Math.toRadians(this.lastElevationAngle));
			double elevation_sin = Math.sin(Math.toRadians(this.lastElevationAngle));

			// if we are smoothing and filtering by value, wait and don't filter by value until after smoothing operation has completed
			double[] holdMinValues = Arrays.copyOf(wctFilter.getMinValue(), wctFilter.getMinValue().length);
			double[] holdMaxValues = Arrays.copyOf(wctFilter.getMaxValue(), wctFilter.getMaxValue().length);
			if (smoothingFactor > 0) {
				wctFilter.setValueRange(WCTFilter.NO_MIN_VALUE, WCTFilter.NO_MAX_VALUE);
			}

			// Use Geotools Proj4 implementation to get MathTransform object 
			// provides lat/lon -> proj coords (inverse of original)
//			MathTransform nexradTransform = nexradProjection.getRadarTransform(siteLon, siteLat).inverse();
			FlatEarth feproj = new FlatEarth(siteLat, siteLon);
			LatLonPointImpl llp = new LatLonPointImpl();
			ProjectionPointImpl pp = new ProjectionPointImpl();




			double rangeToFirstGate = sweep.getRangeToFirstGate();
			double gateSize = sweep.getGateSize();
			int gateNumber = sweep.getGateNumber();


			//System.out.println(variableName+": "+gateSize+"m  "+gateNumber+" gates");



			long timeInProj = 0;
			long timeInIndex = 0;
			long timeInValueCheck = 0;


//			double[] srcPoints = new double[2];
//			double[] dstPoints = new double[2];

			for (int y=0; y<height; y++) {

				if (WCTUtils.getSharedCancelTask().isCancel()) {
					throw new IOException("Operation canceled");
				}



				logger.fine(y+" OF "+height);
				//              System.out.println(y+" OF "+height);

				double lat = envelope.getMinimum(1)+y*cellSizeY+cellSizeY/2.0;

				for (int x=0; x<width; x++) {

					double lon = envelope.getMinimum(0)+x*cellSizeX+cellSizeX/2.0;

					long startTime = System.currentTimeMillis();

					// 1. Get distance from radar origin for this lat/lon
					//                  llPoint.set(lat, lon);
					//                  projPoint = (ProjectionPointImpl) trans.latLonToProj(llPoint, projPoint);
					//                  double distance = projPoint.distance(trans.getOriginLon(), trans.getOriginLat());
					//                  double distance = Math.sqrt((projPoint.x*projPoint.x) + (projPoint.y*projPoint.y));

//					srcPoints[0] = lon;
//					srcPoints[1] = lat;
//					nexradTransform.transform(srcPoints, 0, dstPoints, 0, 1);
					
					llp.set(lat, lon);
					feproj.latLonToProj(llp, pp);
					double distance = Math.sqrt((pp.x*pp.x) + (pp.y*pp.y));
					
					
//					double distance = Math.sqrt((dstPoints[0]*dstPoints[0]) + (dstPoints[1]*dstPoints[1]))/1000;

					timeInProj += System.currentTimeMillis()-startTime;





					// 2. Get azimuth from radar origin for this lat/lon
					//                  double azimuth = Math.toDegrees(Math.atan(projPoint.x/projPoint.y)); // reverse for meteorlogical coord system
					//                  if (projPoint.y < 0) {
					//                      azimuth += 180;
					//                  }
					//                  if (projPoint.y >= 0 && projPoint.x <= 0) {
					//                      azimuth += 360;
					//                  }
					
//					double azimuth = Math.toDegrees(Math.atan(dstPoints[0]/dstPoints[1])); // reverse for meteorlogical coord system
//					if (dstPoints[1] < 0) {
//						azimuth += 180;
//					}
//					if (dstPoints[1] >= 0 && dstPoints[0] <= 0) {
//						azimuth += 360;
//					}
					double azimuth = Math.toDegrees(Math.atan(pp.x/pp.y)); // reverse for meteorlogical coord system
					if (pp.y < 0) {
						azimuth += 180;
					}
					if (pp.y >= 0 && pp.x <= 0) {
						azimuth += 360;
					}



					// 3. Calculate the array index values for this distance/bearing
					startTime = System.currentTimeMillis();                  


					// distance = distIndex*gateSize + rangeToFirstGate - gateSize/2.0 -- center of range bin
					int distIndex = (int)((distance*1000 - rangeToFirstGate + gateSize/2.0) / gateSize);

					//                  List<Integer> azLookupResults = azLookUpTree.query(azimuth);
					//                  if (azLookupResults.size() == 0) {
					//                      // then try +360
					//                      azLookupResults = azLookUpTree.query(azimuth+360);
					//                      if (azLookupResults.size() == 0) {
					//                          System.out.println("none found: "+azimuth);                         
					//                          continue;
					//                      }
					//                  }
					//                  int azIndex = azLookupResults.get(0);
					int azIndex = azLutArray[(int)Math.round(azimuth*100)];

					int arrayIndex = azIndex*gateNumber + distIndex;  

					//                  System.out.println(distIndex+" , "+azIndex+" , "+arrayIndex + " --- dist="+distance+" az="+azimuth+" -- lat="+lat+" lon="+lon);


					// check general 'within bounds' 
					if (azIndex >= sweep.getRadialNumber() || distIndex >= sweep.getGateNumber() ||
							azIndex < 0 || distIndex < 0) {
						continue;
					}

					double value = dataCache[arrayIndex];


					//                  value = (distance*1000 / (double)(gateSize*gateNumber)) * 50;


					// calculate height using radar beam propogation equation
					double beamHeightInM = NexradEquations.getRelativeBeamHeight(elevation_cos, elevation_sin, distance*1000);
					// add height of radar site in meters
					//                  double heightASL = beamHeightInKM*1000 + siteAlt/3.28083989501312;
					double heightASL = beamHeightInM;



					//                  System.out.println(beamHeightInKM+" , "+heightASL);

					// keep going if no data
					if (Double.isNaN(value)) {
						continue;
					}

					// convert velocity to knots
					if (units.equals("m/s") && value != rangeFoldedValue) {
						value *= 1.9438445;
					}   

					// handle rf values 
					if (value == rangeFoldedValue) {
						//                    System.out.println(value);
						value = 800;
						if (wctFilter != null && ! wctFilter.accept(wctFilter.getMinValue()[0]+0.000001, azimuth, distance, heightASL)) {
							continue;
						}
					}
					else {
						// check filter 'within bounds'
						if (wctFilter != null && ! wctFilter.accept(value, azimuth, distance, heightASL)) {
							continue;
						}
					}




					timeInIndex += System.currentTimeMillis()-startTime;
					startTime = System.currentTimeMillis();


					if (Float.isNaN(this.raster.getSampleFloat(x, height-y-1, 0)) ||
							value > this.raster.getSampleFloat(x, height-y-1, 0) ) { 

						//                          if (value == rangeFoldedValue) {
						////                            System.out.println(value);
						//                              value = 800;
						//                          }
						//                          else if (units.equals("m/s")) {
						//                              value *= 1.9438445;
						//                          }   

						this.raster.setSample(x, height-y-1, 0, value);

						if (value < minValue) {
							minValue = value;
						}
						if (value > maxValue) {
							maxValue = value;
						}
					}                        


					timeInValueCheck += System.currentTimeMillis()-startTime;

				}           
				for (int n = 0; n < listeners.size(); n++) {
					event.setProgress((int) ((((double) y) / height) * 100.0));
					listeners.get(n).decodeProgress(event);
				}
			} 




			//          System.out.println("timeInProj: "+timeInProj);
			//          System.out.println("timeInIndex: "+timeInIndex);
			//          System.out.println("timeInValue: "+timeInValueCheck);

			// if we are smoothing and filtering by value, wait and don't filter by value until after smoothing operation has completed
			// fill back in the min/max filters
			if (smoothingFactor > 0) {
				wctFilter.setMinValue(holdMinValues);
				wctFilter.setMaxValue(holdMaxValues);
			}

			sweepHeader.setMinValue(minValue);
			sweepHeader.setMaxValue(maxValue);



			//			System.out.println(RandomAccessFile.getOpenFiles());

			radialDataset.close();

			//			System.out.println(RandomAccessFile.getOpenFiles());


		} catch (IOException e) {
			e.printStackTrace();
			logger.fine("\n\nSubset String: "+lineSectionString);
			throw e;
		} finally {
			//
			////          logger.fine("NUM FILE READS: "+numFileReads+"  NUM REPEAT ROWS: "+numRepeatRows+"  NUM CACHE READS: "+numCacheReads);
			//            logger.info("NUM FILE READS: "+numFileReads+"  NUM CACHE READS: "+numCacheReads);
			//            numFileReads = 0;
			//            numCacheReads = 0;
			//            numRepeatRows = 0;

			// End of decode
			// --------------
			for (int i = 0; i < listeners.size(); i++) {
				event.setProgress(0);
				listeners.get(i).decodeEnded(event);
			}
		}

	}

























































	public synchronized void processCAPPI(String source, java.awt.geom.Rectangle2D.Double bounds, 
			double[] cappiHeightsInMeters, CAPPIType cappiType) 
					throws WCTException, IOException, NumberFormatException, XPathExpressionException, SAXException, 
					ParserConfigurationException, DecodeException, SQLException, ParseException, 
					FactoryException, TransformException {


		if (this.variableName == null) {
			throw new WCTException("Error: The variableName has not been specified.");
		}


		// something's not right, so force a full read each time
		//        isNewFile = true;
		//        isNewVariable = true;
		//        isNewSweep = true;



		if (lastDecodedFile == null || ! lastDecodedFile.equals(source)) {
			//            calculateStatistics(source);
			isNewFile = true;
			isNewVariable = true;
			isNewSweep = true;

			lastDecodedFile = source;
			dataCache = null;
		}

		this.bounds = bounds;
		String lineSectionString = "empty";
		minValue = 999999999;
		maxValue = -999999999;         
		maxXIndex = -1;
		minXIndex = 100000000;
		maxYIndex = -1;
		minYIndex = 100000000;


		logger.fine("REMAP BOUNDS: "+bounds);




		DataDecodeEvent event = new DataDecodeEvent(this);
		try {

			// Start decode
			// --------------
			for (int i = 0; i < listeners.size(); i++) {
				event.setProgress(0);
				listeners.get(i).decodeStarted(event);
			}








			isNewFile = true;
			isNewSweep = true;
			isNewVariable = true;



			//            isNewSweep = (sweepIndex != lastSweepIndex);
			isNewSweep = true;
			if (isNewFile) {
				//                System.out.println("new file: sweep: "+sweepIndex+" var: "+variableName);

				// Open File
				CancelTask cancelTask = new CancelTask() {
					@Override
					public boolean isCancel() {
						return false;
					}
					@Override
					public void setError(String msg) {
					}
					@Override
					public void setProgress(String arg0, int arg1) {			
					}
				};
				Formatter errlog = new Formatter(new StringBuilder());
				radialDataset = (RadialDatasetSweep) FeatureDatasetFactoryManager.open(
						ucar.nc2.constants.FeatureType.RADIAL, 
						source, cancelTask, errlog);

				sweepHeader = new DecodeRadialDatasetSweepHeader();
				sweepHeader.setRadialDatasetSweep(radialDataset);


				isNewFile = false;
				isNewVariable = true;
				isNewSweep = true;
			}
			if (isNewVariable) {            
				//                System.out.println("new variable: sweep: "+sweepIndex+" var: "+variableName);

				//  Get Radial Variable
				this.radialVar = (RadialDatasetSweep.RadialVariable) radialDataset.getDataVariable(this.variableName);
				if (this.radialVar == null) {
					throw new WCTException("The variable '"+this.variableName+"' was not found.");
				}

				isNewVariable = false;
				isNewSweep = true;
			}

















			this.units = "N/A";
			double rangeFoldedValue = Double.NEGATIVE_INFINITY;
			List<Attribute> attList = radialVar.getAttributes();
			for (Attribute a : attList) {
				if (a.getName().equals("range_folded_value")) {
					rangeFoldedValue = a.getNumericValue().doubleValue();
				}
				if (a.getName().equals("units")) {
					this.units = a.getStringValue();
				}
			}
			//            System.out.println("attributes read: units="+units+"  range_folded_value="+rangeFoldedValue);





			this.datetime = radialDataset.getStartDate();
			this.longName = radialDataset.getDescription();


			try {
				String iospClassString = radialDataset.getNetcdfFile().getIosp().getClass().toString();

				//                    System.out.println(grid.getName()+" :: DATE: "+datetime+"\n :: PROJ: "+proj.paramsToString()+
				//                        "\n :: "+proj.getClassName()+"\n :: BBOX: "+gds.getBoundingBox().toString2());

			} catch (Exception e) {}

			if (bounds == null) {
				LatLonRect bbox = radialDataset.getBoundingBox();
				bounds = new Rectangle2D.Double(bbox.getLowerLeftPoint().getLongitude(), bbox.getLowerLeftPoint().getLatitude(), bbox.getWidth(), bbox.getHeight());
			}






			if (forceEqualXYCellsize) {
				Dimension dim = getEqualDimensions(bounds, width, height);
				width = (int)dim.getWidth();
				height = (int)dim.getHeight();  


				logger.fine("Un-Adjusted Bounds: "+bounds);

				bounds = adjustGeographicBounds(new Dimension(width, height), bounds);

				logger.fine("Adjusted Bounds: "+bounds);

				double cellSizeX = bounds.width/(double)width;
				double cellSizeY = bounds.height/(double)height;

				if (Math.abs(cellSizeX - cellSizeY) > .00001) {
					throw new WCTException("X/Y Cellsizes do not match - this is required for ASCII Grid Export");
				}

				this.cellsize = cellSizeX;

			}


			// only create a new one if we need it!
			if (this.raster == null || 
					width != this.raster.getWidth() || height != this.raster.getHeight()) {
				this.raster = RasterFactory.createBandedRaster(
						DataBuffer.TYPE_FLOAT, width, height, cappiHeightsInMeters.length, null);
			}

			// clear raster
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					this.raster.setSample(i, j, 0, Float.NaN);
				}
			}

			this.envelope = new Envelope(bounds);

			// reset bounds
			this.bounds = bounds;


			double cellSizeX = envelope.getLength(0)/(double)width;
			double cellSizeY = envelope.getLength(1)/(double)height;

			//            System.out.println("height="+height+" width="+width+" cellSizeX="+cellSizeX+" , cellSizeY="+cellSizeY);
			logger.fine("height="+height+" width="+width+" cellSizeX="+cellSizeX+" , cellSizeY="+cellSizeY);


			// if we are smoothing and filtering by value, wait and don't filter by value until after smoothing operation has completed
			if (wctFilter == null) {
				wctFilter = new WCTFilter();
			}
			double[] holdMinValues = Arrays.copyOf(wctFilter.getMinValue(), wctFilter.getMinValue().length);
			double[] holdMaxValues = Arrays.copyOf(wctFilter.getMaxValue(), wctFilter.getMaxValue().length);
			if (smoothingFactor > 0) {
				wctFilter.setValueRange(WCTFilter.NO_MIN_VALUE, WCTFilter.NO_MAX_VALUE);
			}



			// ------------------------------------


			logger.fine("REMAPPING DATA TO WGS84");


			for (int n = 0; n < listeners.size(); n++) {
				event.setStatus("Resampling...");
				listeners.get(n).decodeProgress(event);
			}


			//            ProjectionPointImpl projPoint = new ProjectionPointImpl();
			//            LatLonPointImpl llPoint = new LatLonPointImpl();
			//            int[] result = new int[2];

			//            Sweep sweep = radialVar.getSweep(sweepIndex);


			//            System.out.println(sweep.getRadialNumber() + " radials found for sweep index="+sweepIndex);


			//            this.lastElevationAngle = sweep.getMeanElevation();
			this.lastElevationAngle = Float.NaN;  // no elev angle for CAPPI
			this.lastSweepIndex = sweepIndex;
			this.lastVariableName = variableName;
			this.lastCappiHeightsInMeters = cappiHeightsInMeters;
			HashMap<String, String> metadataMap = new HashMap<String, String>();
			metadataMap.put("elevAngle", String.valueOf(this.lastElevationAngle));
			metadataMap.put("sweepIndex", String.valueOf(this.lastSweepIndex));
			metadataMap.put("variableName", String.valueOf(this.lastVariableName));
			metadataMap.put("cappiHeightInMeters", String.valueOf(this.lastCappiHeightsInMeters[0]));

			for (int i = 0; i < listeners.size(); i++) {
				event.setDataType(SupportedDataType.RADIAL);
				event.setDecodeMetadataMap(metadataMap);
				listeners.get(i).metadataUpdate(event);
			}



			//-----------------------------------------------------------
			//  Load data from all sweeps into array
			//-----------------------------------------------------------            
			//            dataCache = sweep.readData();

			//            float minValue = Float.MAX_VALUE;
			//            float maxValue = Float.MIN_VALUE;
			//            for (float val : dataCache) {
			//            	if (val > gridMaxValue) gridMaxValue = val;
			//            	if (val < gridMinValue) gridMinValue = val;
			//            }
			//            System.out.println("read data complete.  sweep min/max="+gridMinValue+"/"+gridMaxValue);



			//                azLutArray = getAzimuthLookupArray(sweep);


			// --------------------------------------------------------------------
			// cache sweep info
			// --------------------------------------------------------------------

			int[][] azLutArrayBySweep = new int[radialVar.getNumSweeps()][];
			double[] elevAngles = new double[radialVar.getNumSweeps()];
			double[] rangeToFirstGate = new double[radialVar.getNumSweeps()];
			double[] gateSize = new double[radialVar.getNumSweeps()];
			int[] gateNumber = new int[radialVar.getNumSweeps()];
			int[] radialNumber = new int[radialVar.getNumSweeps()];
			float[][] sweepData = new float[radialVar.getNumSweeps()][];


			for (int n=0; n<radialVar.getNumSweeps(); n++) {
				Sweep sweep = radialVar.getSweep(n);
				//            	System.out.println("generating azimuth lookup array for sweep "+n);
				//            	azLutArrayBySweep[n] = getAzimuthLookupArray(sweep);
				elevAngles[n] = sweep.getMeanElevation();
				rangeToFirstGate[n] = sweep.getRangeToFirstGate();
				gateSize[n] = sweep.getGateSize();
				gateNumber[n] = sweep.getGateNumber();
				radialNumber[n] = sweep.getRadialNumber();
				//            	sweepData[n] = sweep.readData();
				//            	
				//                for (float val : sweepData[n]) {
				//                	if (val > gridMaxValue) gridMaxValue = val;
				//                	if (val < gridMinValue) gridMinValue = val;
				//                }
				System.out.println(" ....... sweep times[sweep="+n+"]:"+radialDataset.getTimeUnits().makeStandardDateString(sweep.getTime(0)));
				System.out.println(" ....... sweep times[sweep="+n+"]:"+radialDataset.getTimeUnits().makeStandardDateString(sweep.getTime(90)));
				System.out.println(" ....... sweep times[sweep="+n+"]:"+radialDataset.getTimeUnits().makeStandardDateString(sweep.getTime(180)));
				System.out.println(" ....... sweep times[sweep="+n+"]:"+radialDataset.getTimeUnits().makeStandardDateString(sweep.getTime(270)));

			}
			System.out.println("read data complete.  sweep min/max="+gridMinValue+"/"+gridMaxValue);

			//                isNewSweep = false;


			// ---------------------------------------------------------------------
			// calculate the sweeps we want to use.  For NEXRAD, some scan patterns
			// have several sweeps at the same elevation angle.  The first sweep
			// is generally the best for reflectivity, with no range folding.
			// TODO - figure out the best for the doppler moments
			// ---------------------------------------------------------------------
			List<Integer> sweepsToUseList = getSweepsToUseList(radialVar);









			//-----------------------------------------------------------
			// Now conduct nearest neighbor resampling reproject data
			//-----------------------------------------------------------
			String id = radialDataset.getRadarID();
			if (id.equals("XXXX")) {
				FileScanner fileScanner = new FileScanner();
				fileScanner.scanURL(new URL(source));
				id = fileScanner.getLastScanResult().getSourceID();
			}
			double siteLat = RadarHashtables.getSharedInstance().getLat(id);
			double siteLon = RadarHashtables.getSharedInstance().getLon(id);
			double siteAlt = RadarHashtables.getSharedInstance().getElev(id);
			if (siteLat == -999 || siteLon == -999) {
				siteLat = radialDataset.getCommonOrigin().getLatitude();
				siteLon = radialDataset.getCommonOrigin().getLongitude();
				siteAlt = radialDataset.getCommonOrigin().getAltitude()*3.28083989501312;
			}
			if (siteLat == 0 && siteLon == 0) {
				throw new DecodeException("No site location lat/lon found in lookup tables or file for "+id+".  " +
						"Please report this to ncdc.info@noaa.gov .", new URL(source));
			}







			// Subtract Radar site altitude to cappi heights, so heights are ASL (above sea level)
			for (int n=0; n<cappiHeightsInMeters.length; n++) {

			}




			// Use Geotools Proj4 implementation to get MathTransform object 
			// provides lat/lon -> proj coords (inverse of original)
			MathTransform nexradTransform = nexradProjection.getRadarTransform(siteLon, siteLat).inverse();





			//System.out.println(variableName+": "+gateSize+"m  "+gateNumber+" gates");



			long timeInProj = 0;
			long timeInIndex = 0;
			long timeInValueCheck = 0;


			double[] srcPoints = new double[2];
			double[] dstPoints = new double[2];

			double[] heightArrayToReuse = new double[sweepsToUseList.size()];
			int[] cappiCalcReturnArrayToReuse = new int[3];
			double rangeToLookInMeters = Double.POSITIVE_INFINITY;
			if (cappiHeightsInMeters.length > 1) {
				rangeToLookInMeters = Math.abs(
						cappiHeightsInMeters[cappiHeightsInMeters.length-1]-
						cappiHeightsInMeters[cappiHeightsInMeters.length-2] );
			}


			for (int y=0; y<height; y++) {

				logger.fine(y+" OF "+height);
				//              System.out.println(y+" OF "+height);

				double lat = envelope.getMinimum(1)+y*cellSizeY+cellSizeY/2.0;

				for (int x=0; x<width; x++) {

					double lon = envelope.getMinimum(0)+x*cellSizeX+cellSizeX/2.0;

					long startTime = System.currentTimeMillis();

					for (int c=0; c<cappiHeightsInMeters.length; c++) {




						// 1. Get distance from radar origin for this lat/lon
						//                  llPoint.set(lat, lon);
						//                  projPoint = (ProjectionPointImpl) trans.latLonToProj(llPoint, projPoint);
						//                  double distance = projPoint.distance(trans.getOriginLon(), trans.getOriginLat());
						//                  double distance = Math.sqrt((projPoint.x*projPoint.x) + (projPoint.y*projPoint.y));

						srcPoints[0] = lon;
						srcPoints[1] = lat;
						nexradTransform.transform(srcPoints, 0, dstPoints, 0, 1);
						double distance = Math.sqrt((dstPoints[0]*dstPoints[0]) + (dstPoints[1]*dstPoints[1]))/1000;

						timeInProj += System.currentTimeMillis()-startTime;







						// 1.5 Get closest elevation angle / sweep for this CAPPI altitude

						//                http://www.wdtb.noaa.gov/courses/dloc/topic3/lesson1/Section5/Section5-4.html
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
						int[] cappiSweepIndexInfo = getClosestSweepIndexAtHeight(
								elevAngles, sweepsToUseList, distance*1000, cappiHeightsInMeters[c],
								rangeToLookInMeters, heightArrayToReuse, cappiCalcReturnArrayToReuse );


						//						System.out.println(Arrays.toString(cappiSweepIndexInfo));

						// no cappi found within rangeToLook
						if (cappiSweepIndexInfo[0] == -1) {
							//							return;
							continue;
						}

						// wait, rangeToLook is ignored, so try it here
						if (cappiSweepIndexInfo[2] == 0 && cappiSweepIndexInfo[1] > 1000) {
							continue;
						}

						
						// only the closest sweep is needed if we are not interpolating the CAPPI value from upper and lower sweeps						
						int[] closestCappiSweepIndices = null;
						if ((cappiType == CAPPIType.INVERSE_HEIGHT_DIFFERENCE_WEIGHTED_SQUARED ||
								cappiType == CAPPIType.LINEAR_WEIGHTED_AVERAGE ) &&
								cappiSweepIndexInfo[1] > 0 && cappiSweepIndexInfo[2] > 0 && 
								cappiSweepIndexInfo[1] < cappiSweepIndexInfo[2]) {
							
							// then closest sweep is below and second closest is above
							minDiff = cappiSweepIndexInfo[1];
							closestCappiSweepIndices = new int[] { cappiSweepIndexInfo[0], cappiSweepIndexInfo[0]+1 };
						}
						else if ((cappiType == CAPPIType.INVERSE_HEIGHT_DIFFERENCE_WEIGHTED_SQUARED ||
								cappiType == CAPPIType.LINEAR_WEIGHTED_AVERAGE ) &&
								cappiSweepIndexInfo[1] > 0 && cappiSweepIndexInfo[2] > 0 && 
								cappiSweepIndexInfo[1] > cappiSweepIndexInfo[2]) {
							
							// then closest sweep is above and second closest is below
							minDiff = cappiSweepIndexInfo[2];
							closestCappiSweepIndices = new int[] { cappiSweepIndexInfo[0]-1, cappiSweepIndexInfo[0] };
						}
						else {
							// if one of them is '0' then it is either the first or last sweep and we only want that one.
							minDiff = Math.max(cappiSweepIndexInfo[1],  cappiSweepIndexInfo[2]);
							closestCappiSweepIndices = new int[] { cappiSweepIndexInfo[0] };
						}


						// loop over both closest sweeps to find an average
						for (int ci=0; ci<closestCappiSweepIndices.length; ci++) {

							int cappiSweepIndex = closestCappiSweepIndices[ci];


							// 2. Get azimuth from radar origin for this lat/lon
							double azimuth = Math.toDegrees(Math.atan(dstPoints[0]/dstPoints[1])); // reverse for meteorlogical coord system
							if (dstPoints[1] < 0) {
								azimuth += 180;
							}
							if (dstPoints[1] >= 0 && dstPoints[0] <= 0) {
								azimuth += 360;
							}


							// 3. Calculate the array index values for this distance/bearing
							startTime = System.currentTimeMillis();                  


							// distance = distIndex*gateSize + rangeToFirstGate - gateSize/2.0 -- center of range bin
							int distIndex = 
									(int)((distance*1000 - rangeToFirstGate[cappiSweepIndex] + gateSize[cappiSweepIndex]/2.0) / 
											gateSize[cappiSweepIndex]);


							// populate azimuth LUT cache the 'lazy' way
							if (azLutArrayBySweep[cappiSweepIndex] == null) {
								azLutArrayBySweep[cappiSweepIndex] = getAzimuthLookupArray(radialVar.getSweep(cappiSweepIndex));  
								System.out.println("generating azimuth lookup array for sweep "+cappiSweepIndex);
							}



							int azIndex = azLutArrayBySweep[cappiSweepIndex][(int)Math.round(azimuth*100)];

							int arrayIndex = azIndex*gateNumber[cappiSweepIndex] + distIndex;                    

							//                  System.out.println(distIndex+" , "+azIndex+" , "+arrayIndex + " --- dist="+distance+" az="+azimuth+" -- lat="+lat+" lon="+lon);


							// check general 'within bounds' 
							if (azIndex >= radialNumber[cappiSweepIndex] || distIndex >= gateNumber[cappiSweepIndex] ||
									azIndex < 0 || distIndex < 0) {
								continue;
							}


							// populate cache the 'lazy' way
							if (sweepData[cappiSweepIndex] == null) {
								System.out.println("reading data into cache for sweep: "+cappiSweepIndex);

								sweepData[cappiSweepIndex] = radialVar.getSweep(cappiSweepIndex).readData();

								for (float val : sweepData[cappiSweepIndex]) {
									if (val > gridMaxValue) gridMaxValue = val;
									if (val < gridMinValue) gridMinValue = val;
								}
							}



							float value = sweepData[cappiSweepIndex][arrayIndex];


							//                  value = (distance*1000 / (double)(gateSize*gateNumber)) * 50;


							// calculate height using radar beam propogation equation
							double beamHeightInM = NexradEquations.getRelativeBeamHeight(elevAngles[cappiSweepIndex], distance*1000);
							// add height of radar site in meters
							//                  double heightASL = beamHeightInKM*1000 + siteAlt/3.28083989501312;
							double heightASL = beamHeightInM + siteAlt/3.28083989501312;



							//                  System.out.println(beamHeightInKM+" , "+heightASL);

							// keep going if no data
							if (Float.isNaN(value)) {
								continue;
							}

							// convert velocity to knots
							if (units.equals("m/s") && value != rangeFoldedValue) {
								value *= 1.9438445;
							}   

							// handle rf values 
							if (value == rangeFoldedValue) {
								//                    System.out.println(value);
								value = 800;
								if (wctFilter != null && ! wctFilter.accept(wctFilter.getMinValue()[0]+0.000001, azimuth, distance, heightASL)) {
									continue;
								}
							}
							else {
								// check filter 'within bounds'
								if (wctFilter != null && ! wctFilter.accept(value, azimuth, distance, heightASL)) {
									continue;
								}
							}




							timeInIndex += System.currentTimeMillis()-startTime;
							startTime = System.currentTimeMillis();


							// if we have both a sweep above and below, do a distance-weighted average
							if (ci == 1) {
								float firstVal = this.raster.getSampleFloat(x, height-y-1, c);
								if (Float.isNaN(firstVal) || firstVal == 800) {
									this.raster.setSample(x, height-y-1, c, value);
								}
								else if (value == 800) {
									; // do nothing, new value is RF so keep the previous value
								}
								else {
									//								System.out.println("sweep: "+closestCappiSweepIndices[0] + 
									//										" distance to cappi: "+cappiSweepIndexInfo[1] + " value="+firstVal);
									//								System.out.println("   sweep: "+closestCappiSweepIndices[1] + 
									//										" distance to cappi: "+cappiSweepIndexInfo[2] + " value="+value);
									// find weighted average
									//								float lowerSweepWeight = 1/(cappiSweepIndexInfo[1]*cappiSweepIndexInfo[1]);
									//								float upperSweepWeight = 1/(cappiSweepIndexInfo[2]*cappiSweepIndexInfo[2]);
									//								float avg = (firstVal*lowerSweepWeight + value*upperSweepWeight) / 
									//										(lowerSweepWeight + upperSweepWeight);
//									float avg = 0;
									
//									if (firstVal >= rangeFoldedValue - 100 || value >= rangeFoldedValue - 100) {
//										System.out.println("rangefoldedvalue="+rangeFoldedValue+"  firstVal="+firstVal+"  value="+value);
//									}
									
									if (cappiType == CAPPIType.LINEAR_WEIGHTED_AVERAGE) {
										value = (firstVal*cappiSweepIndexInfo[2] + value*cappiSweepIndexInfo[1]) /
												(cappiSweepIndexInfo[1] + cappiSweepIndexInfo[2]);
									}
									else {
										value = (firstVal*cappiSweepIndexInfo[2]*cappiSweepIndexInfo[2] + 
												value*cappiSweepIndexInfo[1]*cappiSweepIndexInfo[1])/
												(cappiSweepIndexInfo[1]*cappiSweepIndexInfo[1] + cappiSweepIndexInfo[2]*cappiSweepIndexInfo[2]);
									}
									
									//								System.out.println("     weighted avg: "+avg);
							
								}							
							}
							
							if (cappiType == CAPPIType.DIST_TO_SWEEP_CENTER) {
								value = (float)minDiff/1000;
							}
							else if (cappiType == CAPPIType.NEAREST_ELEVATION_ANGLE) {
								value = (float)elevAngles[cappiSweepIndex];
							}
							
							// just assign the value
							this.raster.setSample(x, height-y-1, c, value);

							if (value < minValue) {
								minValue = value;
							}
							if (value > maxValue) {
								maxValue = value;
							}



							//						if (Float.isNaN(this.raster.getSampleFloat(x, height-y-1, c)) ||
							//								value > this.raster.getSampleFloat(x, height-y-1, c) ) {                     }   
							//
							//							this.raster.setSample(x, height-y-1, c, value);
							//
							//							if (value < minValue) {
							//								minValue = value;
							//							}
							//							if (value > maxValue) {
							//								maxValue = value;
							//							}
							//						}   


							//						if (Float.isNaN(this.raster.getSampleFloat(x, height-y-1, c)) ||
							//								value > this.raster.getSampleFloat(x, height-y-1, c) ) {                     }   
							//
							//							this.raster.setSample(x, height-y-1, c, value);
							//
							//							if (value < minValue) {
							//								minValue = value;
							//							}
							//							if (value > maxValue) {
							//								maxValue = value;
							//							}
							//						}                        


						}


						timeInValueCheck += System.currentTimeMillis()-startTime;


					}
				}           
				for (int n = 0; n < listeners.size(); n++) {
					event.setProgress((int) ((((double) y) / height) * 100.0));
					listeners.get(n).decodeProgress(event);
				}
			} 


			azLutArrayBySweep = null;
			sweepData = null;



			//          System.out.println("timeInProj: "+timeInProj);
			//          System.out.println("timeInIndex: "+timeInIndex);
			//          System.out.println("timeInValue: "+timeInValueCheck);

			// if we are smoothing and filtering by value, wait and don't filter by value until after smoothing operation has completed
			// fill back in the min/max filters
			if (smoothingFactor > 0) {
				wctFilter.setMinValue(holdMinValues);
				wctFilter.setMaxValue(holdMaxValues);
			}
			
			sweepHeader.setMinValue(minValue);
			sweepHeader.setMaxValue(maxValue);


			radialDataset.close();


		} catch (IOException e) {
			e.printStackTrace();
			logger.fine("\n\nSubset String: "+lineSectionString);
			throw e;
		} finally {
			//
			////          logger.fine("NUM FILE READS: "+numFileReads+"  NUM REPEAT ROWS: "+numRepeatRows+"  NUM CACHE READS: "+numCacheReads);
			//            logger.info("NUM FILE READS: "+numFileReads+"  NUM CACHE READS: "+numCacheReads);
			//            numFileReads = 0;
			//            numCacheReads = 0;
			//            numRepeatRows = 0;

			// End of decode
			// --------------
			for (int i = 0; i < listeners.size(); i++) {
				event.setProgress(0);
				listeners.get(i).decodeEnded(event);
			}
		}

	}





	/**
	 * Determines sweep index of sweep closest to height at a specifed range/distance
	 * in meters from the radar site.  The range is the surface range in an equal 
	 * area projection.  Returns null if there is no sweep within the +- 'rangeToLook'.
	 * Return array contains: [0]=closest sweep index, [1]=meters from sweep below, 
	 * [2]=meters from sweep above
	 * @param sweepElevAngles
	 * @param sweepIndicesToUseList
	 * @param distance
	 * @param cappiHeightInMeters
	 * @param rangeToLookInMeters
	 * @param heightArrayToReuse  This saves space by reusing an existing array.  If null, a new
	 * array will be allocated and used.  The heights don't need to be precalculated, but will be
	 * calculated in this method.  Array size must equal sweepIndicesToUseList.size() .
	 * @param returnArray  This saves space by reusing an existing array.  If null, a new
	 * array will be allocated and returned.  Return array contains: 
	 * [0]=closest sweep index, [1]=meters from sweep below, 
	 * [2]=meters from sweep above
	 * @return
	 */
	public static int[] getClosestSweepIndexAtHeight(
			double[] sweepElevAngles, 
			List<Integer> sweepIndicesToUseList,
			double distanceInMeters, double cappiHeightInMeters, 
			double rangeToLookInMeters, 
			double[] heightArrayToReuse, int[] returnArray) {


		//      http://www.wdtb.noaa.gov/courses/dloc/topic3/lesson1/Section5/Section5-4.html
		//      H = SR*sin PHI + (SR*SR)/(2*IR*RE)
		//      	  	
		//      Equation (12)
		//
		//            where
		//
		//            H = height of the beam centerline above radar level in km
		//            SR = slant range in km
		//            PHI = angle of elevation in degrees
		//            IR = refractive index, 1.21
		//            RE = radius of earth, 6371 km

		double minAbsDiff = Double.POSITIVE_INFINITY;
		int cappiSweepIndex = 0;
		int sweepToUseIndex = 0;


		// 1. Calculate the heights at this x,y for all sweeps
		for (int n=0; n<sweepIndicesToUseList.size(); n++) {
			double slantRange = distanceInMeters*Math.cos(Math.toRadians(sweepElevAngles[sweepIndicesToUseList.get(n)]));
			double altARL = NexradEquations.getRelativeBeamHeight(sweepElevAngles[sweepIndicesToUseList.get(n)], slantRange);
			heightArrayToReuse[n] = altARL;
			//          System.out.println("x,y="+x+","+y+"  range="+range+"  elevAngle="+elevAngles[n] + "  slantRange="+slantRange + "  altARL="+altARL);

		}


		// 2. Find the sweep height that is the closest to our cappi height 
		for (int n=0; n<sweepIndicesToUseList.size(); n++) {
			double diff = heightArrayToReuse[n]-cappiHeightInMeters;
			double absDiff = Math.abs(diff);

			if (absDiff < minAbsDiff) {
				minAbsDiff = absDiff;
				cappiSweepIndex = sweepIndicesToUseList.get(n);
				sweepToUseIndex = n;
			}
		}


		// 3. Calculate the height difference between the cappi height and the sweep below and sweep above
		double diff = heightArrayToReuse[sweepToUseIndex]-cappiHeightInMeters;
		double closestBeamBelowDiff = 0;
		double closestBeamAboveDiff = 0;
		if (diff < 0) {
			// neg. diff, closest beam is below cappi height
			closestBeamBelowDiff = Math.abs(diff);
			if (sweepToUseIndex < sweepIndicesToUseList.size()-1) {
				closestBeamAboveDiff = Math.abs(heightArrayToReuse[sweepToUseIndex+1]-cappiHeightInMeters);
			}
		}
		else {
			// pos. diff, closest beam is above cappi height
			closestBeamAboveDiff = diff;
			if (sweepToUseIndex > 0) {
				closestBeamBelowDiff = Math.abs(heightArrayToReuse[sweepToUseIndex-1]-cappiHeightInMeters);
			}
		}



		if (returnArray == null || returnArray.length != 3) {
			returnArray = new int[3];
		}

		returnArray[0] = cappiSweepIndex;
		returnArray[1] = (int)Math.round(closestBeamBelowDiff);
		returnArray[2] = (int)Math.round(closestBeamAboveDiff);

		return returnArray;

	}


	/**
	 * Copy list of elevation angles into double array
	 * @param radialVar
	 * @return
	 */
	public static double[] getElevAngles(RadialVariable radialVar) {

		double[] elevAngles = new double[radialVar.getNumSweeps()];
		for (int n=0; n<radialVar.getNumSweeps(); n++) {
			Sweep sweep = radialVar.getSweep(n);
			elevAngles[n] = sweep.getMeanElevation();
		}

		return elevAngles;
	}

	/**
	 * 
	 * Calculate the sweeps we want to use.  For NEXRAD, some scan patterns
	 * have several sweeps at the same elevation angle.  The first sweep
	 * is generally the best for reflectivity, with no range folding.
	 * TODO - figure out the best for the doppler moments
	 * @param radialVar
	 * @return
	 */
	public static List<Integer> getSweepsToUseList(RadialVariable radialVar) {
		List<Integer> sweepsToUseList = new ArrayList<Integer>();

		double[] elevAngles = getElevAngles(radialVar);


		int sweepIndex = 0;
		int sweepsProcessed = 0;
		while (sweepsProcessed < elevAngles.length) {

			// check if next elevation angle is the same or close to the same.
			// if so, then skip it
			if (sweepIndex > 0) {
				while (Math.abs(elevAngles[sweepIndex-1]-elevAngles[sweepIndex]) < .2 && sweepIndex < elevAngles.length-1) {
					sweepIndex++;
				}
			}
			sweepsToUseList.add(sweepIndex);

			System.out.println("CAPPI: PROCESSING sweep="+sweepIndex+"  elev="+elevAngles[sweepIndex]);  

			sweepIndex++;

			// break loop if we have advanced to the top sweep
			if (sweepIndex >= elevAngles.length) {
				sweepsProcessed = elevAngles.length;
			}
		}

		return sweepsToUseList;
	}





	//    public void calculateStatistics(String source) throws Exception {
	//        
	//        this.gridMaxValue = STARTING_MAX_VALUE;
	//        this.gridMinValue = STARTING_MIN_VALUE;
	//        
	//        CancelTask cancelTask = new CancelTask() {
	//            public boolean isCancel() {
	//                return false;
	//            }
	//            public void setError(String msg) {
	//            }
	//        };
	//
	//        StringBuilder errlog = new StringBuilder();
	//        this.gds = (ucar.nc2.dt.GridDataset) TypedDatasetFactory.open(ucar.nc2.constants.FeatureType.GRID, source, cancelTask, errlog);
	//        if (this.gds == null) { 
	//            throw new Exception("Can't open Grid Dataset at location= "+source+"; error message= "+errlog);
	//        }
	//        if (this.gds.getGrids().size() == 0) { 
	//            throw new Exception("No Grids found in file: "+source);
	//        }
	//        
	//        gridIndex = (getGridIndex() < 0) ? 0 : getGridIndex();
	//        runtimeIndex = (getRuntimeIndex() < 0) ? 0 : getRuntimeIndex();
	//        timeIndex = (getTimeIndex() < 0) ? 0 : getTimeIndex();
	//        zIndex = (getZIndex() < 0) ? 0 : getZIndex();
	//        
	//        this.grid = gds.getGrids().get(gridIndex);
	//    
	//        calculateStatistics();
	//    }
	//    
	//    
	//    
	//    public void calculateStatistics() throws Exception {
	//    
	//        
	//        int ncols = grid.getXDimension().getLength();
	//        int nrows = grid.getYDimension().getLength();
	//        
	//        int cellNum = nrows * ncols;
	//        int rowsRead = 0;
	//        int maxCellRead = 4*500000;
	//        int rowsToRead = (int) maxCellRead/ncols;
	//        System.out.println("rowsToRead: "+rowsToRead);
	//        while (rowsRead < nrows) {
	//            if (rowsRead+rowsToRead > nrows) {
	//                rowsToRead = nrows-rowsRead;
	//            }
	//            
	//            System.out.println("rowsRead: "+rowsRead);
	//            GridDatatype subsetGrid = grid.makeSubset(null, null, 
	//                    new Range(timeIndex, timeIndex), 
	//                    new Range(zIndex, zIndex), 
	//                    new Range(rowsRead, rowsRead+rowsToRead-1), 
	//                    null);
	//            
	//            MinMax minMax = subsetGrid.getMinMaxSkipMissingData( subsetGrid.readDataSlice(-1, -1, -1, -1) );
	//            System.out.println("chunk ("+rowsRead+" to "+(rowsRead+rowsToRead)+") min/max: "+minMax.min + " / " + minMax.max);
	//            if (gridMaxValue < minMax.max) {
	//                gridMaxValue = minMax.max;
	//            }
	//            if (gridMinValue > minMax.min) {
	//                gridMinValue = minMax.min;
	//            }
	//            
	////            double[] data = (double[]) grid.readDataSlice(timeIndex, zIndex, row, -1).get1DJavaArray(Double.class);
	//            
	//            rowsRead += rowsToRead; 
	//        }
	//        System.out.println("histo max/min: "+gridMinValue+" / "+gridMaxValue);
	//        
	//        this.gds.close();
	//    }





	public void setColorTableAlias(String colorTableAlias) {
		this.colorTableAlias = colorTableAlias;
	}

	public String getColorTableAlias() {
		return this.colorTableAlias;
	}

	public int getCurrentAlpha() {
		return currentAlpha;
	}


	/**
	 * Will use default color map for data
	 * @param alpha 0-255
	 * @return
	 * @throws Exception
	 */
	public GridCoverage getGridCoverage(int alpha) throws Exception {
		if (smoothingFactor > 0) {
			return getGridCoverage(alpha, smoothingFactor);
		}
		else {
			return getGridCoverage(alpha, null);
		}
	}

	/**
	 * Will use custom color map for data, unless customSampleDimension==null, then will use default
	 * @param alpha 0-255
	 * @param customSampleDimension  if null then the default system color map will be used for this dataset
	 * @return
	 * @throws Exception
	 */
	public GridCoverage getGridCoverage(int alpha, SampleDimension customSampleDimension) throws Exception {

//		if (smoothingFactor > 0) {
//			return getGri
//		}
		
		if (this.raster == null) {
			return null;
		}

		this.currentAlpha = alpha;


		if (minValue >= maxValue) {
			//            throw new Exception("All data is undefined for this geographic extent.");
			maxValue = 0.000001;
			minValue = -0.000001;
		}
		if (gridMinValue >= gridMaxValue) {
			//          throw new Exception("All data is undefined for this geographic extent.");
			gridMaxValue = 0.000001;
			gridMinValue = -0.000001;
		}


		//        Color[] colors = new Color[] { Color.BLUE.darker().darker(), Color.BLUE, Color.BLUE.brighter().brighter(), Color.GREEN, 
		//                Color.YELLOW, Color.ORANGE, Color.RED };
		Color[] colors = NexradColorFactory.getColors(variableName, false);
		double[] values = NexradValueFactory.getProductMaxMinValues(variableName, 12, false);

		if (alpha != -1) {
			colors = WCTUtils.applyAlphaFactor(colors, alpha);
		}

		double[] minValues = new double[] { values[0] };
		double[] maxValues = new double[] { values[values.length-1] };


		if (customSampleDimension != null) {			

			SampleDimension[] sdArray = WCTGridCoverageSupport.setSampleDimensionAlpha(
					new SampleDimension[] { customSampleDimension }, alpha);

			this.gc = new GridCoverage("Remapped Grid Image", this.raster, GeographicCoordinateSystem.WGS84, null,
					envelope, sdArray);

		}
		else {
			SampleDimensionAndLabels sd = NexradSampleDimensionFactory.getSampleDimensionAndLabels(variableName, false);
			if (sd != null) {
				//			JOptionPane.showMessageDialog(null, "here", "there", JOptionPane.INFORMATION_MESSAGE);

				SampleDimension[] sdArray = WCTGridCoverageSupport.setSampleDimensionAlpha(
						new SampleDimension[] { sd.getSampleDimension() }, alpha);

				this.gc = new GridCoverage("Remapped Grid Image", this.raster, GeographicCoordinateSystem.WGS84, null,
						envelope, sdArray);
			}
			else {
				this.gc = new GridCoverage("Remapped Grid Image", this.raster, 
						GeographicCoordinateSystem.WGS84, envelope, minValues, 
						maxValues, null, new Color[][]{colors}, null);
			}
		}

		return gc;
	}



	private GridCoverage getGridCoverage(int alpha, int smoothFactor) throws Exception {
		return getGridCoverage(alpha, AlphaInterpolationType.NONE, smoothFactor);
	}    

	private GridCoverage getGridCoverage(int alpha, AlphaInterpolationType alphaInterpType, int smoothFactor) throws Exception {

		System.out.println(envelope);

		if (smoothFactor < 1) {
			return getGridCoverage(alpha);
		} 





		//        double baseline = values[0] - 0.001;
		double baseline = minValue - (maxValue-minValue)/100.0;

		GridCoverage tempGC = new GridCoverage("junk coverage", raster, GeographicCoordinateSystem.WGS84,
				envelope, new double[] {0.0}, new double[] {1.0}, 
				null, new Color[][]{new Color[] { Color.black }}, null);            


		// change NoData values in Raster 
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (raster.getSampleFloat(i, j, 0) == getNoDataValue() || Float.isNaN( raster.getSampleFloat(i, j, 0) )) {
					raster.setSample(i, j, 0, baseline);
				}
			}
		}



		// Dynamic Smoothing?
		SmoothingOperation smoothOp = new SmoothingOperation();
		SmoothingInfo info = smoothOp.getSmoothingInfo(envelope, raster.getWidth(), raster.getHeight(), smoothFactor);

		boolean isPowerSmoothing = variableName.toUpperCase().contains("REFLECTIVITY");
		//        isPowerSmoothing = false;

		// CONVERT dBZ to Gain
		if (isPowerSmoothing) {

			System.out.println("POWER SMOOTHING!");

			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {

					double val = raster.getSampleFloat(i, j, 0);
					val = Math.pow(10, val/10.0);
					raster.setSample(i, j, 0, val);
				}
			}
		}





		//        KernelJAI kernel = new KernelJAI(info.getKernelSize(), info.getKernelSize(), info.getKernelMatrix());        
		//        ParameterBlock pb = new ParameterBlock();
		//        pb.addSource(tempGC.getRenderedImage());
		//        pb.add(kernel);      
		//        PlanarImage output = JAI.create("convolve", pb, null);
		//        WritableRaster smoothedRaster = (WritableRaster)(output.getData());





		//        KernelJAI kernel = new KernelJAI(kernelSize, kernelSize, kernelMatrix);
		//        KernelJAI kernel = new KernelJAI(5, 5, gaussianKernelMatrix);
		//        KernelJAI kernel = new KernelJAI(3, 3, meanKernelMatrix);
		//        RenderedImage img = tempGC.getRenderedImage();
		//        for (int n=0; n<smoothIterations; n++) {
		//        	ParameterBlock pb = new ParameterBlock();
		//        	pb.addSource(img);
		//        	pb.add(kernel);      
		//        	img = JAI.create("convolve", pb, null);
		//        }

		//        WritableRaster smoothedRaster = SmoothingOperation.smoothRaster(raster, new Kernel(5, 5, gaussianKernelMatrix), info.getSmoothingIterations());
		//        WritableRaster smoothedRaster = SmoothingOperation.smoothRaster(raster, new Kernel(3, 3, meanKernelMatrix), info.getSmoothingIterations());
		WritableRaster smoothedRaster = null;
		//        if (info.getSmoothingIterations() > 1) {
		//        	smoothedRaster = smoothOp.smoothRaster(
		//        		raster, 
		//        		new Kernel(info.getKernelSize(), info.getKernelSize(), info.getKernelMatrix()), 
		//        		info.getSmoothingIterations(),
		//        		listeners);
		//        }
		//        else {
		KernelJAI kernel = new KernelJAI(info.getKernelSize(), info.getKernelSize(), info.getKernelMatrix());        
		ParameterBlock pb = new ParameterBlock();
		pb.addSource(tempGC.getRenderedImage());
		pb.add(kernel);      
		PlanarImage output = JAI.create("convolve", pb, null);
		smoothedRaster = (WritableRaster)(output.getData());
		//        }


		// CONVERT back to dBZ
		if (isPowerSmoothing) {
			//            raster = (WritableRaster)(img.getData());

			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {

					double val = smoothedRaster.getSampleFloat(i, j, 0);

					val = (float)(10*(Math.log(val)/Math.log(10)));
					smoothedRaster.setSample(i, j, 0, val);

				}
			}
		}            



		boolean scrubRaster = true;

		double maxVal = -99999;
		double minVal = 99999;

		double baselineThreshold = (maxValue-minValue)/100.0;


		if (scrubRaster) {

			float val;
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {

					val = smoothedRaster.getSampleFloat(i, j, 0);
					
					if (wctFilter != null && wctFilter.getMinValue()[0] != WCTFilter.NO_MIN_VALUE) {
						if (val < wctFilter.getMinValue()[0]) {
							smoothedRaster.setSample(i, j, 0, getNoDataValue());
						}
					}
					if (wctFilter != null && wctFilter.getMaxValue()[0] != WCTFilter.NO_MAX_VALUE) {
						if (val > wctFilter.getMaxValue()[0]) {
							smoothedRaster.setSample(i, j, 0, getNoDataValue());
						}
					}
					
					if (val <= baseline+baselineThreshold) {
						//System.out.println("111 "+(minValues[0] - 5.0)+"   :::   "+val+"  :: "+NO_DATA+"  :: "+kernelSize);
						smoothedRaster.setSample(i, j, 0, getNoDataValue());
					}
					else if (Float.isInfinite(val)) {
						smoothedRaster.setSample(i, j, 0, Float.NaN);
					}

					maxVal = (val > maxVal) ? val : maxVal;
					minVal = (val < minVal) ? val : minVal;

				}
			}

			//			System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			//			System.out.println("MAX VAL: "+maxVal);
			//			System.out.println("MIN VAL: "+minVal);
			//			System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");

		}


		SampleDimensionAndLabels sdl = NexradSampleDimensionFactory.getSampleDimensionAndLabels(variableName, false);
		if (sdl != null) {
			SampleDimension[] sdArray = WCTGridCoverageSupport.setSampleDimensionAlpha(
					new SampleDimension[] { sdl.getSampleDimension() }, alpha);

			this.gc = new GridCoverage("Smoothed Remapped Grid Image", smoothedRaster, 
					GeographicCoordinateSystem.WGS84, null, envelope, sdArray);
		}
		else {
			Color[] colors = NexradColorFactory.getColors(variableName, false);
			double[] values = NexradValueFactory.getProductMaxMinValues(variableName, 12, false);

			if (alpha != -1) {
				colors = WCTUtils.applyAlphaFactor(colors, alpha);
			}

			double[] minValues = new double[] { values[0] };
			double[] maxValues = new double[] { values[values.length-1] };

			this.gc = new GridCoverage("Smoothed Remapped Grid Image", smoothedRaster, 
					GeographicCoordinateSystem.WGS84, envelope, minValues, 
					maxValues, null, new Color[][]{colors}, null);
		}

		//		this.gc = new GridCoverage("Smoothed Grid Coverage", smoothedRaster, GeographicCoordinateSystem.WGS84,
		//				envelope, minValues, maxValues, null, new Color[][]{colors}, null);

		return this.gc;
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

	/**
	 * Gets the min value over entire sweep, not just current geog. extent.
	 * @return
	 */
	public double getGridMinValue() {
		return gridMinValue;
	}

	/**
	 * Gets the max value over entire sweep, not just current geog. extent.
	 * @return
	 */
	public double getGridMaxValue() {
		return gridMaxValue;
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

	public double setNoDataValue(double noDataValue) {
		return this.noDataValue = noDataValue;
	}

	public double getNoDataValue() {
		return noDataValue;
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






	public static Dimension getEqualDimensions(java.awt.geom.Rectangle2D.Double bounds, int width, int height) {
		// All the data should start in the lower left corner.  Don't export what we don't need.
		double ratio = bounds.height / bounds.width;
		int ncols;
		int nrows;
		if (ratio < 1) {
			// wider than tall
			nrows = (int) (ratio * width);
			ncols = width;
		}
		else {
			nrows = height;
			ncols = (int) (ratio * height);
		}

		logger.info("             Original --- WIDTH: " + width + "   HEIGHT: " + height);
		logger.info("Bounds Ratio Adjusted --- WIDTH: " + ncols + "   HEIGHT: " + nrows);
		logger.info("Cellsize -- X: "+bounds.width/ncols+" Y: "+bounds.height/nrows);

		return new Dimension(ncols, nrows);
	}


	public static java.awt.geom.Rectangle2D.Double adjustGeographicBounds(Dimension imageDimension, java.awt.geom.Rectangle2D.Double bounds) {

		double imgRatio = (double)imageDimension.getWidth() / (double)imageDimension.getHeight();
		double geoRatio = bounds.getWidth() / bounds.getHeight();

		double dlon = bounds.getWidth();
		double dlat = bounds.getHeight();
		double geoCenterX = bounds.getMinX() + (dlon / 2.0);
		double geoCenterY = bounds.getMinY() + (dlat / 2.0);

		logger.fine(geoCenterX+","+geoCenterY);

		double x = imgRatio / geoRatio;

		if (x > 1.0) {
			dlon = dlon * x;
		}
		else {
			dlat = dlat / x;
		}

		return new java.awt.geom.Rectangle2D.Double(geoCenterX - dlon / 2.0, 
				geoCenterY - dlat / 2.0, 
				dlon, dlat);

		//        return new ReferencedEnvelope(geoCenterX + dlon / 2.0, 
		//                geoCenterX - dlon / 2.0, 
		//                geoCenterY + dlat / 2.0, 
		//                geoCenterY - dlat / 2.0,
		//                bounds.getCoordinateReferenceSystem());

	}  //  end adjustGeographicBounds()


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
		return "RADIAL REMAPPED RASTER: \n"+
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

















	protected static SIRtree getAzimuthLookupTree(Sweep sweep) throws IOException {

		int nrays = sweep.getRadialNumber();

		SIRtree azTree = new SIRtree(sweep.getRadialNumber());

		for (int n=0; n<sweep.getRadialNumber(); n++) {

			//          double startAz = sweep.getAzimuth(n)-sweep.getBeamWidth()/2.0;
			//          double endAz = sweep.getAzimuth(n)+sweep.getBeamWidth()/2.0;


			double lastAzCenter, nextAzCenter;
			double curAzCenter = sweep.getAzimuth(n);

			if (n == 0 || Double.isNaN(sweep.getAzimuth(n-1))) {
				lastAzCenter = sweep.getAzimuth(n) - sweep.getBeamWidth()*0.75;
			}
			else {
				lastAzCenter = sweep.getAzimuth(n-1);
			}
			if (n == nrays-1 || Double.isNaN(sweep.getAzimuth(n+1))) {
				nextAzCenter = sweep.getAzimuth(n) + sweep.getBeamWidth()*0.75;
			}
			else {
				nextAzCenter = sweep.getAzimuth(n+1);
			}

			if (lastAzCenter > curAzCenter) {
				//              System.out.println(lastAzCenter + " : "+ curAzCenter + " : " + nextAzCenter);
				curAzCenter += 360;
			}
			if (nextAzCenter < curAzCenter) {
				//              System.out.println(lastAzCenter + " : "+ curAzCenter + " : " + nextAzCenter);
				nextAzCenter += 360;
			}



			if (! (Double.isNaN(curAzCenter))) {

				//              System.out.println(lastAzCenter + " ::: "+ curAzCenter + " ::: " + nextAzCenter);

				double startAz = curAzCenter - (curAzCenter - lastAzCenter)/2.0;
				double endAz = curAzCenter + (nextAzCenter - curAzCenter)/2.0;
				//              System.out.println(startAz+" , "+endAz+" , "+n);
				azTree.insert(startAz, endAz, new Integer(n));
			}

		}
		return azTree;
	}





	protected static int[] getAzimuthLookupArray(Sweep sweep) throws IOException {

		int nrays = sweep.getRadialNumber();

		int[] azLut = new int[72000];

		for (int n=0; n<sweep.getRadialNumber(); n++) {

			//          double startAz = sweep.getAzimuth(n)-sweep.getBeamWidth()/2.0;
			//          double endAz = sweep.getAzimuth(n)+sweep.getBeamWidth()/2.0;


			double lastAzCenter, nextAzCenter;
			double curAzCenter = sweep.getAzimuth(n);

			if (n == 0 || Double.isNaN(sweep.getAzimuth(n-1))) {
				lastAzCenter = sweep.getAzimuth(n) - sweep.getBeamWidth()*0.75;
			}
			else {
				lastAzCenter = sweep.getAzimuth(n-1);
			}
			if (n == nrays-1 || Double.isNaN(sweep.getAzimuth(n+1))) {
				nextAzCenter = sweep.getAzimuth(n) + sweep.getBeamWidth()*0.75;
			}
			else {
				nextAzCenter = sweep.getAzimuth(n+1);
			}

			if (lastAzCenter > curAzCenter) {
				//                              System.out.println(lastAzCenter + " : "+ curAzCenter + " : " + nextAzCenter);
				curAzCenter += 360;
			}
			if (nextAzCenter < curAzCenter) {
				//                              System.out.println(lastAzCenter + " : "+ curAzCenter + " : " + nextAzCenter);
				nextAzCenter += 360;
			}



			if (! Double.isNaN(curAzCenter)) {

				//              System.out.println(lastAzCenter + " ::: "+ curAzCenter + " ::: " + nextAzCenter);

				double startAz = curAzCenter - (curAzCenter - lastAzCenter)/2.0;
				double endAz = curAzCenter + (nextAzCenter - curAzCenter)/2.0;

				if (startAz < 0) {
					startAz += 360;
					endAz += 360;
				}

				//                              System.out.println(startAz+" , "+endAz+" , "+n);
				//                azTree.insert(startAz, endAz, new Integer(n));

				if (endAz > 360) {                    
					for (double az = startAz; az <= 360; az=az+0.01) {
						azLut[(int)Math.round(az*100)] = n;
					}                    
					for (double az = 0; az <= endAz-360; az=az+0.01) {
						azLut[(int)Math.round(az*100)] = n;
					}
				}
				else {                
					for (double az = startAz; az <= endAz; az=az+0.01) {
						azLut[(int)Math.round(az*100)] = n;
					}
				}
			}

		}
		return azLut;
	}








	public void setSweepIndex(int sweepIndex) {
		if (sweepIndex != this.sweepIndex) {
			isNewSweep = true;
		}
		this.sweepIndex = sweepIndex;
	}

	public int getLastDecodedSweepIndex() {
		return lastSweepIndex;
	}

	public DecodeRadialDatasetSweepHeader getLastDecodedSweepHeader() {
		return sweepHeader;
	}

	public void setVariableName(String variableName) {
		if (! variableName.equals(this.variableName)) {
			isNewSweep = true;
			isNewVariable = true;
		}
		this.variableName = variableName;
	}

	public String getLastDecodedVariableName() {
		return lastVariableName;
	}

	public double getLastDecodedElevationAngle() {
		return lastElevationAngle;
	}

	public Date getLastDecodedSweepTime() {
		return lastSweepTime;
	}

	/**
	 * Returns null if last decode was NOT a CAPPI
	 * @return
	 */
	public double[] getLastDecodedCappiHeightInMeters() {
		return lastCappiHeightsInMeters;
	}

	/**
	 * Gets last decoded radial variable
	 * @param source
	 * @param bounds
	 * @throws DecodeException
	 * @throws FeatureRasterizerException
	 * @throws ParseException
	 * @throws IOException
	 * @throws DecodeHintNotSupportedException
	 */
	public RadialVariable getLastDecodedRadialVariable() {
		return this.radialVar;
	}







	//	 private void doPolygonRasterize(String source, java.awt.geom.Rectangle2D.Double bounds) 
	//	 throws DecodeException, FeatureRasterizerException, ParseException, IOException, DecodeHintNotSupportedException {
	//
	//
	//		 if (isNewSweep) {
	//			 sweepHeader = new DecodeRadialDatasetSweepHeader();
	//			 sweepHeader.setRadialDatasetSweep(radialDataset);
	//			 sweepDecoder = new DecodeRadialDatasetSweep(sweepHeader);
	//			 sweepDecoder.addDataDecodeListener(new DataDecodeListener() {
	//				 @Override
	//				 public void decodeEnded(DataDecodeEvent event) {
	//				 }
	//				 @Override
	//				 public void decodeProgress(DataDecodeEvent event) {
	//					 for (int n = 0; n < listeners.size(); n++) {
	//						 event.setStatus("Decoding Data");
	//						 event.setProgress(event.getProgress());
	//						 listeners.get(n).decodeProgress(event);
	//					 }
	//				 }
	//				 @Override
	//				 public void decodeStarted(DataDecodeEvent event) {
	//				 }
	//				 @Override
	//				 public void metadataUpdate(DataDecodeEvent event) {
	//				 }
	//			 });
	//			 rasterizer = new WCTRasterizer(800, 800);
	//			 rasterizer.addGeneralProgressListener(new GeneralProgressListener() {
	//				 DataDecodeEvent decodeEvent = new DataDecodeEvent(rasterizer);
	//				 @Override
	//				 public void ended(GeneralProgressEvent event) {
	//				 }
	//				 @Override
	//				 public void progress(GeneralProgressEvent event) {
	//					 for (int n = 0; n < listeners.size(); n++) {
	//						 decodeEvent.setStatus("Resampling Data");
	//						 decodeEvent.setProgress(100+(int)event.getProgress());
	//						 listeners.get(n).decodeProgress(decodeEvent);
	//					 }
	//				 }
	//				 @Override
	//				 public void started(GeneralProgressEvent event) {
	//				 }
	//			 });
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//			 // make a custom header object that responds to different variables
	//			 if (sweepHeader == null) {
	//				 sweepHeader = new DecodeRadialDatasetSweepHeader() {
	//					 public short getProductCode() {                        
	//						 if (variableName.equals("Reflectivity")) {
	//							 return NexradHeader.LEVEL2_REFLECTIVITY;
	//						 }
	//						 else if (variableName.equals("Total_Power")) {
	//							 return NexradHeader.LEVEL2_REFLECTIVITY;
	//						 }
	//						 else if (variableName.equals("RadialVelocity")) {
	//							 return NexradHeader.LEVEL2_VELOCITY;
	//						 }
	//						 else if (variableName.equals("Velocity")) {
	//							 return NexradHeader.LEVEL2_VELOCITY;
	//						 }
	//						 else if (variableName.equals("SpectrumWidth")) {
	//							 return NexradHeader.LEVEL2_SPECTRUMWIDTH;
	//						 }
	//						 else if (variableName.equals("Width")) {
	//							 return NexradHeader.LEVEL2_SPECTRUMWIDTH;
	//						 }
	//						 else {
	//							 return NexradHeader.UNKNOWN;
	//						 }
	//					 }
	//				 };
	//			 }
	//			 sweepHeader.setRadialDatasetSweep(this.radialDataset);
	//
	//			 // if the file does not have lat/lon/site info encoded inside, set it here based on lookup table
	//
	//			 //              System.err.println(radialDatasetHeader.getICAO());
	//			 //              System.exit(1);
	//
	//			 if (sweepHeader.getICAO().equals("XXXX")) {
	//				 int idx = source.lastIndexOf('/');
	//				 String icao = source.substring(idx+1, idx+5);
	//				 if (icao.equals("6500")) {
	//					 icao = source.substring(idx+5, idx+9); 
	//				 }
	//
	//				 System.err.println("SETTING SITE MANUALLY FOR: "+icao);
	//
	//				 RadarHashtables nxhash = RadarHashtables.getSharedInstance();
	//				 sweepHeader.setStationInfo(icao, nxhash.getLat(icao), nxhash.getLon(icao), nxhash.getElev(icao));
	//			 }
	//
	//
	//			 sweepDecoder.setDecodeHint("attributes", DecodeRadialDatasetSweep.DISPLAY_POLY_ATTRIBUTES);
	//			 sweepDecoder.setDecodeHint("classify", new Boolean(false));
	//			 sweepDecoder.setDecodeHint("nexradFilter", new WCTFilter());
	//			 sweepDecoder.setDecodeHint("startSweep", new Integer(sweepIndex));
	//			 sweepDecoder.setDecodeHint("endSweep", new Integer(sweepIndex));
	//			 sweepDecoder.setRadialVariable(this.radialVar);
	//			 sweepDecoder.decodeData();
	//
	//		 }
	//
	//
	//
	//		 rasterizer.rasterize(sweepDecoder.getFeatures(), bounds, "value");
	//
	//		 this.raster = rasterizer.getWritableRaster();
	//
	//		 radialDataset.close();
	//
	//	 }




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

	public void setWctFilter(WCTFilter wctFilter) {
		this.wctFilter = wctFilter;
	}

	public WCTFilter getWctFilter() {
		return wctFilter;
	}

	public int getSmoothingFactor() {
		return smoothingFactor;
	}

	public void setSmoothingFactor(int smoothingFactor) {
		this.smoothingFactor = smoothingFactor;
	}


}
