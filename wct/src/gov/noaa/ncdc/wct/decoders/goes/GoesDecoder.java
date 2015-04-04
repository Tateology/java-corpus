package gov.noaa.ncdc.wct.decoders.goes;

import edu.wisc.ssec.mcidas.AreaFile;
import gov.noaa.ncdc.wct.WCTIospManager;
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.StreamingDecoder;
import gov.noaa.ncdc.wct.decoders.StreamingProcess;
import gov.noaa.ncdc.wct.decoders.goes.GoesRemappedRaster.Band;
import gov.noaa.ncdc.wct.event.DataDecodeEvent;
import gov.noaa.ncdc.wct.event.DataDecodeListener;

import java.awt.geom.Rectangle2D.Double;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.Vector;
import java.util.logging.Logger;

import org.geotools.factory.FactoryConfigurationError;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.SchemaException;

import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.iosp.mcidas.McIDASAreaProjection;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.Projection;
import ucar.unidata.geoloc.ProjectionPointImpl;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class GoesDecoder implements StreamingDecoder {

	private static final Logger logger = Logger.getLogger(GoesDecoder.class.getName());

	private String source;
	private java.awt.geom.Rectangle2D.Double bounds = GoesRemappedRaster.GOES_DEFAULT_EXTENT;
	private double minValue, maxValue;

	private int numFileReads = 0;
	private int numCacheReads = 0;
	private int numRepeatRows = 0;

	private int lastRowRead = -1;
	private int[] rowData = null;


	private Vector<DataDecodeListener> listeners = new Vector<DataDecodeListener>();
	private Band lastBandDecoded;

	private HashMap<Integer, int[]> rowDataMap = new HashMap<Integer, int[]>();
	private ArrayList<Integer> rowCacheOrder = new ArrayList<Integer>();

	private Variable var;
	private int imgWidth; 
	private int imgHeight;
	private Projection proj;

	private Date datetime;
	private String longName = "GOES GVAR Remapped Satellite Imagery";
	private String units = "Kelvin";

	private FeatureType schema;

	final static double NO_DATA_VALUE = 99999;


	public final static AttributeType[] POINT_ATTRIBUTES = {
		AttributeTypeFactory.newAttributeType("geom", Geometry.class),
		AttributeTypeFactory.newAttributeType("value", Float.class, true, 5),
	};











	public GoesDecoder() throws IllegalAccessException, InstantiationException, FactoryConfigurationError, SchemaException {
//		NetcdfFile.registerIOProvider(gov.noaa.ncdc.iosp.area.AreaIosp.class);
        WCTIospManager.getInstance().registerIosp(gov.noaa.ncdc.iosp.area.AreaIosp.class);

		schema = FeatureTypeFactory.newFeatureType(POINT_ATTRIBUTES, "GOES Grid Cell Centroid Point Attributes");

	}








	public FeatureType[] getFeatureTypes() {
		return new FeatureType[] { schema };
	}

	public Double getLastDecodedExtent() {
		return bounds;
	}


	public void decodeData(StreamingProcess[] streamingProcessArray) throws DecodeException {


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





		DataDecodeEvent event = new DataDecodeEvent(this);
		try {

			// Start decode
			// --------------
			for (int i = 0; i < listeners.size(); i++) {
				event.setProgress(0);
				listeners.get(i).decodeStarted(event);
			}




			AreaFile areaFile = new AreaFile(source);
			this.proj = new McIDASAreaProjection(areaFile);
			logger.fine("BEFORE FILE OPEN");
			NetcdfFile ncfile = NetcdfFile.open(source);
			//            logger.fine(ncfile.toString());
			//            String variableName = "image";
			String variableName = "calibratedData";
			this.var = ncfile.findVariable(variableName);
			this.imgWidth = var.getDimension(var.findDimensionIndex("GeoX")).getLength(); 
			this.imgHeight = var.getDimension(var.findDimensionIndex("GeoY")).getLength(); 
			Array xCoords = ncfile.findVariable("GeoX").read();
			Array yCoords = ncfile.findVariable("GeoY").read();

			//            Variable timeVar = ncfile.findVariable("time");
			//            this.datetime = new Date(timeVar.readScalarLong());

			DecimalFormat fmtDate = new DecimalFormat("00000");
			SimpleDateFormat sdfDate = new SimpleDateFormat("yyDDD");
			sdfDate.setTimeZone(TimeZone.getTimeZone("GMT"));
			DecimalFormat fmtTime = new DecimalFormat("000000");
			SimpleDateFormat sdfTime = new SimpleDateFormat("HHmmss");
			sdfTime.setTimeZone(TimeZone.getTimeZone("GMT"));

			long dateMillis = sdfDate.parse(fmtDate.format(ncfile.findVariable("actualImgDate").readScalarInt()).substring(1)).getTime();
			long timeMillis = sdfTime.parse(fmtTime.format(ncfile.findVariable("actualImgTime").readScalarInt())).getTime();
			this.datetime = new Date(dateMillis+timeMillis);

			ProjectionPointImpl projPoint = new ProjectionPointImpl();
			LatLonPointImpl llPoint = new LatLonPointImpl();

			GeometryFactory geoFactory = new GeometryFactory();

			//-----------------------------------------------------------
			// Now read the data and export the centroid of each grid cell
			//-----------------------------------------------------------

			double geoX, geoY, value;
			int geoIndex = 0;

			IndexIterator yCoordsIter = yCoords.getIndexIterator();
			

			for (int y=0; y<imgHeight; y++) {

//				System.out.println(y+" OF "+imgHeight);
//				System.out.println(yCoords.getSize()+" , "+yCoords.shapeToString() + " , "+yCoords.getIndex().currentElement());
				
//				logger.fine(y+" OF "+imgHeight);
//				geoY = yCoords.nextDouble();
				geoY = yCoordsIter.getDoubleNext();

				IndexIterator xCoordsIter = xCoords.getIndexIterator();
				for (int x=0; x<imgWidth; x++) {

//					geoX = xCoords.nextDouble();
					geoX = xCoordsIter.getDoubleNext();
					
					
					projPoint.setLocation(geoX, geoY);
					llPoint = (LatLonPointImpl) proj.projToLatLon(projPoint, llPoint);

					// No need to shift, since in NetCDF conventions use center of grid cell for coordinate array

					if (bounds.contains(llPoint.getLongitude(), llPoint.getLatitude())) {

						value = getRowData(y)[x];

						if (value == 99999) {
							value = (int)NO_DATA_VALUE;
						}


						if (value != NO_DATA_VALUE && value < minValue) {
							minValue = value;
						}
						if (value != NO_DATA_VALUE && value > maxValue) {
							maxValue = value;
						}


						if (value != NO_DATA_VALUE) {

							// create the feature
							Feature feature = schema.create(new Object[]{
									geoFactory.createPoint(new Coordinate(llPoint.getLongitude(), llPoint.getLatitude())),
									new Float(value)
							}, new Integer(geoIndex++).toString());

							for (int n=0; n<streamingProcessArray.length; n++) {
								streamingProcessArray[n].addFeature(feature);
							}

						}
					}
				} // end x loop
				for (int i = 0; i < listeners.size(); i++) {
					event.setProgress((int)(100*y/(double)imgHeight));
					listeners.get(i).decodeProgress(event);
				}
			}   

			for (int n=0; n<streamingProcessArray.length; n++) {
				streamingProcessArray[n].close();
			}


		} catch (Exception e) {
			try {
				for (int n=0; n<streamingProcessArray.length; n++) {
					streamingProcessArray[n].close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}

			e.printStackTrace();
			logger.fine("\n\nSubset String: "+lineSectionString);
			throw new DecodeException(e.toString());
		} finally {
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

		//        if (row == lastRowRead) {
		//            numRepeatRows++;
		//            return rowData;
		//        }

		lastRowRead = row;

		if (! rowDataMap.containsKey(row)) {
			String lineSectionString = (imgHeight-row-1)+":"+(imgHeight-row-1)+",0:"+(imgWidth-1);
			for (int n=2; n<var.getRank(); n++) {
				lineSectionString = "0:0,"+lineSectionString;
			}
			rowData = ((int[]) (var.read(lineSectionString).get1DJavaArray(Integer.class)));
			rowDataMap.put(row, rowData);
			rowCacheOrder.add(row);
			//                logger.fine("ADDED DATA (array length "+width+") FOR ROW "+row+ "  CACHE SIZE="+rowDataMap.size());

			// clear oldest if needed
			if (rowDataMap.size() > GoesRemappedRaster.ROW_CACHE_SIZE) {
				//                    logger.fine("CACHE SIZE REACHED, REMOVING ROW "+rowCacheOrder.get(0));
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





	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public java.awt.geom.Rectangle2D.Double getBounds() {
		return bounds;
	}
	public void setBounds(java.awt.geom.Rectangle2D.Double bounds) {
		this.bounds = bounds;
	}



	
	public void addDataDecodeListener(DataDecodeListener l) {
		listeners.add(l);
	}
	public void removeDataDecodeListener(DataDecodeListener l) {
		listeners.remove(l);
	}
}
