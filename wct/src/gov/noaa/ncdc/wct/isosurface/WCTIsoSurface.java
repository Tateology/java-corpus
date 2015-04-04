package gov.noaa.ncdc.wct.isosurface;

import gov.noaa.ncdc.gis.kml.KMLUtils;
import gov.noaa.ncdc.gis.kml.TourGenerator;
import gov.noaa.ncdc.nexrad.NexradEquations;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.cdm.DecodeRadialDatasetSweepHeader;
import gov.noaa.ncdc.wct.decoders.cdm.RadialDatasetSweepRemappedRaster;
import gov.noaa.ncdc.wct.decoders.cdm.RadialDatasetSweepRemappedRaster.CAPPIType;
import gov.noaa.ncdc.wct.event.DataDecodeEvent;
import gov.noaa.ncdc.wct.event.DataDecodeListener;
import gov.noaa.ncdc.wct.export.raster.WCTRasterExport;
import gov.noaa.ncdc.wct.isosurface.SurfaceGen.TriangleListener;
import gov.noaa.ncdc.wct.ui.ViewerKmzUtilities;

import java.awt.Desktop;
import java.awt.geom.Rectangle2D;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.media.jai.RasterFactory;

import org.geotools.ct.MathTransform;
import org.geotools.gc.GridCoverage;
import org.geotools.pt.CoordinatePoint;
import org.geotools.pt.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.RadialDatasetSweep;
import ucar.nc2.dt.grid.GridDataset;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.geoloc.Projection;
import ucar.unidata.geoloc.ProjectionPointImpl;
import ucar.unidata.geoloc.ProjectionRect;

public class WCTIsoSurface {

	public static enum KMLShapeType { POLYGON, MESH };
	
	
	private double maxZVal = -9999999;
	private double minZVal = 9999999;
	private int elementIndex;
	
	private int radialExportGridSmoothFactor = 5;
	private int radialNumCappis = 8;
	private double radialCappiSpacingInMeters = 1500;
	private double radialRemappedGridCellsize = 0.005;
	private int radialRemappedGridSizeMax = 300;
	private CAPPIType radialCappiType = CAPPIType.INVERSE_HEIGHT_DIFFERENCE_WEIGHTED_SQUARED;
	
	private int gridSizeMax = 300;

	private KMLShapeType shapeType = KMLShapeType.POLYGON;
	private boolean isTourSelected = true;
	
	private Vector<DataDecodeListener> listeners = new Vector<DataDecodeListener>();
	
	
	int vertexIndex = 0;
	

	public static void main(String[] args) {

		try {

//			String gridDatasetSource = "E:\\work\\isosurface\\testdata\\ruc2_130_20110309_1700_000.grb2.nc";
//			String gridName = "Temperature";
//			File outKmlFile = new File("isosurface-test1.kml");
//
//			GridDataset gds = GridDataset.open(gridDatasetSource);
//			List<GridDatatype> gridList = gds.getGrids();
//			int gridIndex = -1;
//			for (int n=0; n<gridList.size(); n++) {
//				if (gridList.get(n).getName().equals(gridName)) {
//					gridIndex = n;
//				}
//			}
//
//			
//			WCTIsoSurface iso = new WCTIsoSurface();
//			iso.generateIsosurfaceKML(gridDatasetSource, gridIndex, 60, new float[]{ 250.0f }, new String[]{ "7f000fff" }, outKmlFile);
//			Desktop.getDesktop().open(outKmlFile);
			
			
			
			
			String radialDatasetSource = "E:\\work\\tornado\\joplin\\data\\KSGF20110522_224348_V03.gz";
			File outKmlFile = new File("E:\\work\\tornado\\obj\\isosurface-joplin-test.kml");
			File outObjFile = new File("E:\\work\\tornado\\obj\\isosurface-joplin-test.obj");
			WCTIsoSurface iso = new WCTIsoSurface();
			Rectangle2D.Double extent = new Rectangle2D.Double(-94.6, 37.0, .15, .15);
			iso.generateIsosurfaceKML(radialDatasetSource, "Reflectivity", extent, new float[]{ 60.0f }, new String[]{ "7fffffff" }, outKmlFile);
			iso.generateIsosurfaceOBJ(radialDatasetSource, "Reflectivity", extent, new float[]{ 60.0f }, new String[]{ "7fffffff" }, outObjFile);
			Desktop.getDesktop().open(outKmlFile);
			
			
			
			
			
			
			
			
			
			

//			displayIsosurface(gridDatasetSource, gridIndex, 0.0005, 250.0f, "7f000fff");
			
			
			
			
			
//			Rectangle2D.Double extent = new Rectangle2D.Double(-91.9, 34.2, .5, .5);
//			String datasetSource = "E:\\work\\isosurface\\testdata\\Level2_KLZK_20110101_0301.ar2v";
//			String datasetSource = "C:\\work\\isosurface\\Level2_KLZK_20110101_0301.ar2v";
//			String varName = "Reflectivity";
//			File outKmlFile = new File("isosurface-nexradtest1.kml");

//			generateIsosurfaceKML(datasetSource, varName, extent, 50.0f, "7f000fff", outKmlFile);

			

			
			
			
//			String gridDatasetSource = "isosurface-nexradtest1.kml.nc";
//			String gridName = "Reflectivity";
//			File outKmlFile = new File("isosurface-nexradtest1.kml");
////
//			GridDataset gds = GridDataset.open(gridDatasetSource);
//			List<GridDatatype> gridList = gds.getGrids();
//			int gridIndex = -1;
//			for (int n=0; n<gridList.size(); n++) {
//				if (gridList.get(n).getName().equals(gridName)) {
//					gridIndex = n;
//				}
//			}
//			generateIsosurfaceKML(gridDatasetSource, gridIndex, 40.0f, "ff000fff", outKmlFile);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}



	public void generateIsosurfaceKML(String gridDatasetSource, int gridIndex, 
			int elevationExaggeration,
			float[] isoValues, String[] kmlColors, File outKmlFile) throws IOException, InvalidRangeException {

		generateIsosurfaceKML(gridDatasetSource, gridIndex, null, elevationExaggeration, isoValues, kmlColors, outKmlFile);
	}


	public void generateIsosurfaceKML(String gridDatasetSource, int gridIndex, 
			Rectangle2D.Double bounds, int elevationExaggeration, 
			float[] isoValues, String[] kmlColors, File outKmlFile) throws IOException, InvalidRangeException {

		GridDataset gds = GridDataset.open(gridDatasetSource);

		try {

			//			GeoGrid grid = gds.findGridByName("Temperature");
			GridDatatype grid = gds.getGrids().get(gridIndex);

			// subset optionally
			if (bounds != null) {
				LatLonRect bbox = new LatLonRect(new LatLonPointImpl(
						bounds.getMinY(), bounds.getMinX()), 
						new LatLonPointImpl(bounds.getMaxY(), bounds.getMaxX()));

				grid = grid.makeSubset(null, null, bbox, 1, 1, 1);
			}

			
			// apply some stride if the grid size is large
			int xLength = grid.getCoordinateSystem().getXHorizAxis().getDimension(0).getLength();
			int yLength = grid.getCoordinateSystem().getYHorizAxis().getDimension(0).getLength();
			if (xLength > getGridSizeMax() || yLength > getGridSizeMax()) {
				System.out.println("grid stride enabled: "+
						((int)(xLength/getGridSizeMax()))+1+" , "+
						((int)(yLength/getGridSizeMax()))+1);
				
				grid = grid.makeSubset(null, null, null, 1, 
						((int)(yLength/getGridSizeMax()))+1, 
						((int)(xLength/getGridSizeMax()))+1);
			}

			
			
			if (grid.getCoordinateSystem().getVerticalAxis() == null) {
				throw new IOException("This dataset does not have a vertical axis.");
			}
			

			final double[] zVals = grid.getCoordinateSystem().getVerticalAxis().getCoordValues();
			//    		System.out.println(Arrays.toString(zVals));
			if (grid.getCoordinateSystem().getVerticalAxis().getUnitsString().equals("Pa")) {
				for (int n=0; n<zVals.length; n++) {
					zVals[n] = NexradEquations.getAltitudeFromPressureInMeters(zVals[n]);
				}
			}
			else if (! grid.getCoordinateSystem().isZPositive()) {
				elevationExaggeration *= -1;
			}

			for (int n=0; n<zVals.length; n++) {
				zVals[n] *= elevationExaggeration;
			}

			
			
			


			Array volDataArray = grid.readVolumeData(0);
			float[] volData = (float[]) (volDataArray.get1DJavaArray(Float.class));

			System.out.println("volData.length="+volData.length);

			// should be order of z, y, x
			System.out.println(Arrays.toString(volDataArray.getIndex().getShape()));


			//		int numX = 50;
			//		int numY = 50;
			//		int numZ = 10;

			//		short[] data = new short[numX*numY*numZ];

			int numZ = volDataArray.getIndex().getShape(0);
			int numY = volDataArray.getIndex().getShape(1);
			int numX = volDataArray.getIndex().getShape(2);

			//			short[] data = new short[volData.length];
			//			for (int n=0; n<data.length; n++) {
			//				data[n] = (short)(volData[n]*10);
			//			}


			//		for (int n=0; n<data.length; n++) {
			//			data[n] = (short)Math.round(Math.random()*4);
			//			data[n] = (short)n;
			//		}




			final BufferedWriter bw = new BufferedWriter(new FileWriter(outKmlFile));
			bw.write(getKMLBegin(grid.getName()+" Isosurfaces"));





			//		System.out.println("WRITING KML");
			//			float[] vertices = sg.getVertices();


			//		System.out.println("Vertices: "+Arrays.toString(vertices));
			//		System.out.println("Normals:  "+Arrays.toString(sg.getNormals()));

			// scale = cellsize
			// offset = llCorner
			//		double xScale = 0.1;
			//		double xOffset = -100.0;
			//		double yScale = 0.1;
			//		double yOffset = 30.0;
//			final double zScale = 30000.0;
			final double zScale = Math.abs(zVals[1]-zVals[0]);
			//		double zOffset = 0;

			
//System.out.println("zScale array: "+Arrays.toString(zVals));			

			Array yAxisDataTest = grid.getCoordinateSystem().getYHorizAxis().read("1:2:1");
			Array xAxisDataTest = grid.getCoordinateSystem().getXHorizAxis().read("1:2:1");
			int yAxisOrder = (yAxisDataTest.getDouble(0) > yAxisDataTest.getDouble(1)) ? -1 : 1;
			int xAxisOrder = (xAxisDataTest.getDouble(0) > xAxisDataTest.getDouble(1)) ? -1 : 1;
			
//			if (yAxisDataTest.getDouble(0) > yAxisDataTest.getDouble(1)) {
//				System.out.println(yAxisDataTest.toString());
//				yCellSize *= -1;
//			}

			
			final ProjectionRect bbox = grid.getCoordinateSystem().getBoundingBox();
			final double xCellSize = bbox.getWidth()/grid.getXDimension().getLength()*xAxisOrder;
			final double yCellSize = bbox.getHeight()/grid.getYDimension().getLength()*yAxisOrder;

			final Projection proj = grid.getCoordinateSystem().getProjection();
			//		System.out.println(xCellSize+" / "+yCellSize);


			

			SurfaceGen sg = new SurfaceGen();

			final ArrayList<String> bottomCoordList = new ArrayList<String>();

			
			sg.addTriangleListener(new TriangleListener() {
				
				@Override
				public void processTriangle(float[][] vertices, float[][] normals) {
					try {
						// store z=0 line strings so we can manually extrude to the surface
						if (vertices[0][2] == 1.0 && vertices[1][2] == 1.0) {
							bottomCoordList.add(vertices[1][0]+","+vertices[1][1]+","+vertices[0][0]+","+vertices[0][1]);
//							System.out.println("1: "+Arrays.deepToString(vertices));
						}
						if (vertices[0][2] == 1.0 && vertices[2][2] == 1.0) {
							bottomCoordList.add(vertices[0][0]+","+vertices[0][1]+","+vertices[2][0]+","+vertices[2][1]);
//							System.out.println("2: "+Arrays.deepToString(vertices));
						}
						if (vertices[1][2] == 1.0 && vertices[2][2] == 1.0) {
							bottomCoordList.add(vertices[2][0]+","+vertices[2][1]+","+vertices[1][0]+","+vertices[1][1]);
//							System.out.println("3: "+Arrays.deepToString(vertices));
						}
						
						writeTriangleKML(bw, vertices, proj, bbox, xCellSize, yCellSize, zVals);

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});

			


			DataDecodeEvent dde = new DataDecodeEvent(this);
			for (int n=0; n<isoValues.length; n++) {
				
				dde.setProgress((int)(WCTUtils.progressCalculator(new int[] { n }, new int[] { isoValues.length })*100));
				dde.setStatus("Generating Isosurface: "+
						WCTUtils.DECFMT_0D00.format(isoValues[n])+" "+
						grid.getUnitsString());
				for (DataDecodeListener l : listeners) {
					l.decodeProgress(dde);
				}
				
				String startTime = null;
				String endTime = null;
				try {
					if (grid.getCoordinateSystem().getDateRange() != null) {
						System.out.println("------------: "+grid.getCoordinateSystem().getDateRange().getStart().getDate());
						startTime =  ViewerKmzUtilities.ISO_DATE_FORMATTER.format( grid.getCoordinateSystem().getDateRange().getStart().getDate() );
						endTime =  ViewerKmzUtilities.ISO_DATE_FORMATTER.format( 
								new Date(grid.getCoordinateSystem().getCalendarDateRange().getStart().getMillis()+(1000*60*5L)) );
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				bw.write(getTriangleKMLBegin(kmlColors[n], 
						"Isosurface: "+WCTUtils.DECFMT_0D00.format(isoValues[n])+" "+
						grid.getUnitsString(), startTime, endTime));
				sg.apply(volData, numX, numY, numZ, isoValues[n], 1.0f, 1.0f, 1.0f);
				
				
				
				
//				System.out.println(bottomCoordList);
				writeBaseExtrudeKML(bw, bottomCoordList, proj, bbox, xCellSize, yCellSize, zVals);
				bottomCoordList.clear();
				
				
				
				bw.write(getTriangleKMLEnd());

			}
			
			
			for (DataDecodeListener l : listeners) {
				l.decodeEnded(dde);
			}
			
			

			//			System.out.println("num vertices = " + sg.getVertices().length);
			//			System.out.println("num normals  = " + sg.getNormals().length);

			String[] zLevels = new String[zVals.length];
			for (int n=0; n<zLevels.length; n++) {
				zLevels[n] = WCTUtils.DECFMT_0.format(zVals[n]) + " "+ grid.getCoordinateSystem().getVerticalAxis().getUnitsString();
			}


			LatLonRect llRect = grid.getCoordinateSystem().getLatLonBoundingBox();
			Rectangle2D.Double extent = new Rectangle2D.Double(llRect.getLonMin(), llRect.getLatMin(), llRect.getWidth(), llRect.getHeight());

			try {
				bw.write(addKML3DOutlineBoxFolder(extent, zLevels, zScale));
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if (this.isTourSelected) {
				bw.write(TourGenerator.generateTourKML(extent));
			}
			

			bw.write(getKMLEnd());
			bw.close();

			
			
			
			System.out.println("z min/max vals: "+minZVal+" / "+maxZVal);
			minZVal = 9999999;
			maxZVal = -9999999;
			
			gds.close();
			
		} catch (IOException e) {
			gds.close();
			throw e;
		} catch (InvalidRangeException e) {
			gds.close();
			throw e;
		}

	}





	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	


	/**
	 * Export a Radial dataset file to KML output file.
	 * 
	 * @param radialDatasetSource is path to local location, or OPeNDAP URL for remote access which will work but will be slower.
	 * @param variableName - 'Reflectivity' is what you likely want
	 * @param bounds - lat/lon extent of interest
	 * @param isoValues - array of values which indicates which isosurfaces should be created
	 * @param kmlColors - hex codes as defined in the KML spec
	 * @param outKmlFile - local output location for KML file
	 * @throws Exception
	 */
	public void generateIsosurfaceKML(String radialDatasetSource, 
			String variableName, final Rectangle2D.Double bounds,
			float[] isoValues, String[] kmlColors, File outKmlFile) throws Exception {


		
		double[] cappiHeights = new double[radialNumCappis];
		
//		float[] volData = null;
		
		
//		RadialDatasetSweepRemappedRaster[] raster = new RadialDatasetSweepRemappedRaster[numCappis];
//		
//		for (int n=0; n<numCappis; n++) {
//			cappiHeights[n] = n*1500;
//			raster[n] = new RadialDatasetSweepRemappedRaster();
//			raster[n].setWidth(400);
//			raster[n].setHeight(400);
//			raster[n].setVariableName(variableName);
//			System.out.println("processing cappi...");
//			raster[n].processCAPPI(radialDatasetSource, bounds, cappiHeights[n]);
//			// lazy init
//			if (volData == null) {
//				volData = new float[raster[n].getWidth()*raster[n].getHeight()*numCappis]; 
//			}
//			// smooth
//            if (exportGridSmoothFactor > 0) {
//                GridCoverage gc = raster[n].getGridCoverage(0, exportGridSmoothFactor);
//                java.awt.image.RenderedImage renderedImage = gc.getRenderedImage();
//                raster[n].setWritableRaster((WritableRaster)(renderedImage.getData()));
//            }
//            
//			System.out.println("copying data...");
//			float[] cappiData = new float[raster[n].getWidth()*raster[n].getHeight()];
//			cappiData = raster[n].getWritableRaster().getSamples(0, 0, raster[n].getWidth(), raster[n].getHeight(), 0, cappiData);
//			System.arraycopy(cappiData, 0, volData, n*cappiData.length, cappiData.length);
//			
//			
//		}
		
		
		
		RadialDatasetSweepRemappedRaster raster = new RadialDatasetSweepRemappedRaster();
		for (int n=0; n<radialNumCappis; n++) {
			cappiHeights[n] = n*radialCappiSpacingInMeters;
		}
		
		int rasterWidth = (int)Math.round(bounds.getWidth()/radialRemappedGridCellsize);
		int rasterHeight = (int)Math.round(bounds.getHeight()/radialRemappedGridCellsize);
		System.out.println("raster width/height: "+rasterWidth+" / "+rasterHeight);
		if (rasterWidth > radialRemappedGridSizeMax || rasterHeight > radialRemappedGridSizeMax) {
			rasterWidth = radialRemappedGridSizeMax;
			rasterHeight = radialRemappedGridSizeMax;
		}
		System.out.println("raster width/height: "+rasterWidth+" / "+rasterHeight);
			
//		raster.setWidth(300);
//		raster.setHeight(300);
		raster.setWidth(rasterWidth);
		raster.setHeight(rasterHeight);
		System.out.println("raster width/height: "+rasterWidth+" / "+rasterHeight);
		
		
		for (DataDecodeListener l : listeners) {
			raster.addDataDecodeListener(l);
		}
		
		
		
		raster.setVariableName(variableName);
		System.out.println("processing cappi...");
		raster.processCAPPI(radialDatasetSource, bounds, cappiHeights, radialCappiType);
		// lazy init
//		if (volData == null) {
//			volData = new float[raster.getWidth()*raster.getHeight()*numCappis]; 
//		}
		
		// smooth
		if (radialExportGridSmoothFactor > 0) {
			
			WritableRaster multiBandRaster = raster.getWritableRaster();			
			for (int n=0; n<radialNumCappis; n++) {
				WritableRaster tmpRaster = RasterFactory.createBandedRaster(
						DataBuffer.TYPE_FLOAT, multiBandRaster.getWidth(), 
						multiBandRaster.getHeight(), 1, null);

				float[] tmpData = new float[raster.getWidth()*raster.getHeight()];
				tmpData = multiBandRaster.getSamples(0, 0, raster.getWidth(), raster.getHeight(), n, tmpData);
				tmpRaster.setSamples(0, 0, raster.getWidth(), raster.getHeight(), 0, tmpData);
				raster.setWritableRaster(tmpRaster);				
				
				raster.setSmoothingFactor(radialExportGridSmoothFactor);
				GridCoverage gc = raster.getGridCoverage(0);
				java.awt.image.RenderedImage renderedImage = gc.getRenderedImage();
//				raster.setWritableRaster((WritableRaster)(renderedImage.getData()));
				
				// set smoothed data in original multiband raster
				tmpData = ((WritableRaster)(renderedImage.getData())).getSamples(
						0, 0, raster.getWidth(), raster.getHeight(), 0, tmpData);
				multiBandRaster.setSamples(0, 0, raster.getWidth(), raster.getHeight(), n, tmpData);
				
				System.out.println("smoothed data for band "+n);
				
			}
			raster.setWritableRaster(multiBandRaster);
		}
			
		

		WCTRasterExport rasterExport = new WCTRasterExport();
		File outFile = new File(outKmlFile+".nc");
		// add site elevation to cappi heights
		// let the first height always be '0' to extend surface to ground surface
		for (int n=0; n<radialNumCappis; n++) {
			// TODO - uncomment after base extrude is fixed
			cappiHeights[n] = cappiHeights[n] + raster.getLastDecodedSweepHeader().getAlt() * 0.3048;
		}

		rasterExport.saveNetCDF(outFile, raster, cappiHeights);
		
		
		
		GridDataset gds = GridDataset.open(outKmlFile+".nc");
		List<GridDatatype> gridList = gds.getGrids();
//		int gridIndex = -1;
//		for (int n=0; n<gridList.size(); n++) {
//			if (gridList.get(n).getName().equals("Reflectivity")) {
//				gridIndex = n;
//			}
//		}
		// grid index is always 0 because the output netcdf file is for just a single variable
		int gridIndex = 0;
		
		
		int holdValue = getGridSizeMax();
		setGridSizeMax(radialRemappedGridSizeMax);
		generateIsosurfaceKML(outKmlFile+".nc", gridIndex, 1, isoValues, kmlColors, outKmlFile);
		setGridSizeMax(holdValue);
		

		
		
		for (DataDecodeListener l : listeners) {
			raster.removeDataDecodeListener(l);
		}

		

//		final BufferedWriter bw = new BufferedWriter(new FileWriter(outKmlFile));
//		bw.write(getKMLBegin());
//
//		final Projection proj = new LatLonProjection();
//		final ProjectionRect bbox = new ProjectionRect(raster[0].getBounds());
//		final double xCellSize = bbox.getWidth()/raster[0].getWidth();
//		final double yCellSize = bbox.getHeight()/raster[0].getHeight();
//		final double zScale = 1000;
//		
//		SurfaceGen sg = new SurfaceGen();
//		sg.addTriangleListener(new TriangleListener() {
//			@Override
//			public void processTriangle(float[][] vertices, float[][] normals) {
//				try {
//
//					//						writeTriangleKML(bw, vertices, proj, bbox, xCellSize, yCellSize, zScale);
//					writeTrianglePolyKML(bw, vertices, proj, bbox, xCellSize, yCellSize, zScale);
//
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		});
//		bw.write(getTriangleKMLBegin(kmlColor));
//		sg.apply(volData, raster[0].getWidth(), raster[0].getHeight(), numCappis, isoValue, 1.0f, 1.0f, 1.0f);
//		bw.write(getTriangleKMLEnd());
//
//		try {
//			bw.write(addKML3DOutlineBoxFolder(raster[0].getBounds(), 
//					new String[] { "0km", "1km", "2km", "3km", "4km", "5km" }, zScale));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		bw.write(getKMLEnd());
//		bw.close();


		
		
	}
	
	
	
	
	
	
	






//
//
//	public static void generateIsosurfaceKML(String radialDatasetSource, 
//			String variableName, int sweepIndex, final Rectangle2D.Double bounds,
//			float isoValue, String kmlColor, File outKmlFile) throws Exception {
//
//        StringBuilder errlog = new StringBuilder();
//        final RadialDatasetSweep radialDataset = (RadialDatasetSweep) TypedDatasetFactory.open(
//            ucar.nc2.constants.FeatureType.RADIAL, 
//            radialDatasetSource, WCTUtils.getEmptyCancelTask(), errlog);
//
//        try {
//        
//        final DecodeRadialDatasetSweepHeader sweepHeader = new DecodeRadialDatasetSweepHeader();
//        sweepHeader.setRadialDatasetSweep(radialDataset);
//
//        final RadialVariable radialVar = 
//        	(RadialDatasetSweep.RadialVariable) radialDataset.getDataVariable(variableName);
//        final RadialDatasetSweep.Sweep sweep = radialVar.getSweep(sweepIndex);
//        
//        
//        
//        
//        String id = radialDataset.getRadarID();
//        if (id.equals("XXXX")) {
//        	FileScanner fileScanner = new FileScanner();
//        	fileScanner.scanURL(new URL(radialDatasetSource));
//        	id = fileScanner.getLastScanResult().getSourceID();
//        }
//        double siteLat = RadarHashtables.getSharedInstance().getLat(id);
//        double siteLon = RadarHashtables.getSharedInstance().getLon(id);
//        double siteAlt = RadarHashtables.getSharedInstance().getElev(id);
//        if (siteLat == -999 || siteLon == -999) {
//            siteLat = radialDataset.getCommonOrigin().getLatitude();
//            siteLon = radialDataset.getCommonOrigin().getLongitude();
//            siteAlt = radialDataset.getCommonOrigin().getAltitude()*3.28083989501312;
//        }
//        if (siteLat == 0 && siteLon == 0) {
//        	throw new DecodeException("No site location lat/lon found in lookup tables or file for "+id+".  " +
//        			"Please report this to ncdc.info@noaa.gov .", new URL(radialDatasetSource));
//        }
//
//        WCTProjections radarProjection = new WCTProjections();
//        final MathTransform radarTransform = radarProjection.getRadarTransform(siteLon, siteLat).inverse();
//
//        
//        
//        
//        
////		double[] zVals = radialVar.get
//		//    		System.out.println(Arrays.toString(zVals));
//
//
//
//
//        // sweeps, rays, gates
//		float[] volData = radialVar.readAllData();
//
//		System.out.println("volData.length="+volData.length);
//
//		// should be order of z, y, x
//		System.out.println(Arrays.toString(radialVar.getShape()));
//
//
//		//		int numX = 50;
//		//		int numY = 50;
//		//		int numZ = 10;
//
//		//		short[] data = new short[numX*numY*numZ];
//
//		
//		
////		int numZ = radialVar.getShape()[0];
////		int numY = radialVar.getShape()[1];
////		int numX = radialVar.getShape()[2];
//
//		int numZ = radialVar.getNumSweeps();
//		int numY = radialVar.getSweep(0).getRadialNumber();
//		int numX = radialVar.getSweep(0).getGateNumber();
//
//		//			short[] data = new short[volData.length];
//		//			for (int n=0; n<data.length; n++) {
//		//				data[n] = (short)(volData[n]*10);
//		//			}
//
//
//		//		for (int n=0; n<data.length; n++) {
//		//			data[n] = (short)Math.round(Math.random()*4);
//		//			data[n] = (short)n;
//		//		}
//
//
//
//
//		final BufferedWriter bw = new BufferedWriter(new FileWriter(outKmlFile));
//		bw.write(getKMLBegin());
//
//
//
//
//		final double elevExaggeration = 3;
//		final float[] azimuthArray = sweep.getAzimuth();
//
//		SurfaceGen sg = new SurfaceGen();
//
//		sg.addTriangleListener(new TriangleListener() {
//			@Override
//			public void processTriangle(float[][] vertices, float[][] normals) {
//				try {
//
//					writeRadialTrianglePolyKML(bw, vertices, bounds, sweep, radarTransform, sweepHeader, azimuthArray, elevExaggeration);
//
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
//
//		bw.write(getTriangleKMLBegin(kmlColor));
//
//		sg.apply(volData, numX, numY, numZ, isoValue, 1.0f, 1.0f, 1.0f);
//
//		bw.write(getTriangleKMLEnd());
//
//
//
////		String[] zLevels = new String[zVals.length];
////		for (int n=0; n<zLevels.length; n++) {
////			zLevels[n] = WCTUtils.DECFMT_00.format(zVals[n]) + " "+ grid.getCoordinateSystem().getVerticalAxis().getUnitsString();
////		}
//
//
////		LatLonRect llRect = grid.getCoordinateSystem().getLatLonBoundingBox();
////		Rectangle2D.Double extent = new Rectangle2D.Double(llRect.getLonMin(), llRect.getLatMin(), llRect.getWidth(), llRect.getHeight());
////
////		try {
////			bw.write(addKML3DOutlineBoxFolder(extent, zLevels, zScale));
////		} catch (Exception e) {
////			e.printStackTrace();
////		}
//
//		bw.write(getKMLEnd());
//		bw.close();
//
//		
//
//        } catch (Exception e) {
//        	radialDataset.close();
//        	throw e;
//        }
//	}











	private static String getKMLBegin(String documentName) {
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>       \n");
		sb.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\"    \n");
		sb.append(" xmlns:gx=\"http://www.google.com/kml/ext/2.2\"> \n");
		sb.append(" \n");
		sb.append("	<Document> \n");
		sb.append("  <name>"+documentName+"</name> \n");

		return sb.toString();
	}

	private static String getTriangleKMLBegin(String lineColor, String title) {
		return getTriangleKMLBegin(lineColor, title, null, null);
	}
	
	private static String getTriangleKMLBegin(String lineColor, String title, 
			String kmlBeginTimeString, String kmlEndTimeString) {
		
		StringBuffer sb = new StringBuffer();
		sb.append("	<Placemark> \n");
		sb.append("  <name>"+title+"</name> \n");
		sb.append("  <Style> \n");
		sb.append("  <LineStyle> \n");
		sb.append("   <color>"+lineColor+"</color> \n");
		sb.append("   <width>1</width> \n");
		sb.append("  </LineStyle> \n");
		sb.append("  <PolyStyle> \n");
		sb.append("   <color>"+lineColor+"</color> \n");
		sb.append("   <outline>0</outline> \n");
		sb.append("  </PolyStyle> \n");
		sb.append("  </Style> \n");
		
        if (kmlBeginTimeString != null && kmlEndTimeString != null) {
            sb.append("  <TimeSpan> \n");
            sb.append("     <begin>"+kmlBeginTimeString+"</begin> \n");
            sb.append("     <end>"+kmlEndTimeString+"</end> \n");
            sb.append("  </TimeSpan> \n");
        }

		
		sb.append("  <MultiGeometry> \n");

		return sb.toString();
	}

	private static String getTriangleKMLEnd() {
		StringBuffer sb = new StringBuffer();
		sb.append(" </MultiGeometry> \n");
		sb.append(" </Placemark> \n");

		return sb.toString();
	}

	private static String getKMLEnd() {
		StringBuffer sb = new StringBuffer();
		sb.append("	</Document> \n");
		sb.append("</kml>\n");

		return sb.toString();
	}

	
	
	
//	private static void writeTriangleKML(BufferedWriter bw, float[][] vertices, 
//			Projection proj, ProjectionRect bbox, double xCellSize, double yCellSize, double zScale) throws IOException {
//
//
//
//
//		//-77.05788457660967,38.87253259892824,100  
//		// all triangles - 3 vertices with xyz
//		//	    for (int n=0; n<18; n=n+9) {
//		//			bw.write((vertices[n]*xScale+xOffset)+","+(vertices[n+1]*yScale+yOffset)+","+(vertices[n+2]*zScale+zOffset)+" \n");
//
//		//			System.out.println((bbox.getMinX()+vertices[n]*xCellSize) + " / " +
//		//					(bbox.getMinY()+vertices[n+1]*yCellSize) + " / " +
//		//					vertices[n+2]);
//
//		bw.write("  <LineString> \n");
//		//			bw.write("   <extrude>0</extrude> \n");
//		bw.write("   <altitudeMode>relativeToGround</altitudeMode> \n");
//		bw.write("   <coordinates> \n");
//
//		ProjectionPointImpl projPoint = new ProjectionPointImpl(
//				bbox.getMinX()+vertices[0][0]*xCellSize,
//				(bbox.getMinY()+vertices[0][1]*yCellSize));
//		LatLonPointImpl llPoint = new LatLonPointImpl();
//		proj.projToLatLon(projPoint, llPoint);
//
//		//			System.out.println(llPoint.getLongitude()+" / "+
//		//					llPoint.getLatitude()+" / "+					
//		//					vertices[n+2]*zScale);
//
//		bw.write(WCTUtils.DECFMT_0D0000.format(llPoint.getLongitude())+","+
//				WCTUtils.DECFMT_0D0000.format(llPoint.getLatitude())+","+			
//				vertices[0][2]*zScale + "\n");	
//
//
//
//
//		projPoint = new ProjectionPointImpl(
//				bbox.getMinX()+vertices[1][0]*xCellSize,
//				(bbox.getMinY()+vertices[1][1]*yCellSize));
//		llPoint = new LatLonPointImpl();
//		proj.projToLatLon(projPoint, llPoint);
//
//		bw.write(WCTUtils.DECFMT_0D0000.format(llPoint.getLongitude())+","+
//				WCTUtils.DECFMT_0D0000.format(llPoint.getLatitude())+","+					
//				vertices[1][2]*zScale + "\n");
//
//
//
//		projPoint = new ProjectionPointImpl(
//				bbox.getMinX()+vertices[2][0]*xCellSize,
//				(bbox.getMinY()+vertices[2][1]*yCellSize));
//		llPoint = new LatLonPointImpl();
//		proj.projToLatLon(projPoint, llPoint);
//
//		bw.write(WCTUtils.DECFMT_0D0000.format(llPoint.getLongitude())+","+
//				WCTUtils.DECFMT_0D0000.format(llPoint.getLatitude())+","+				
//				vertices[2][2]*zScale + "\n");
//
//
//
//		bw.write("   </coordinates> \n");
//		bw.write("  </LineString> \n");
//
//
//	}







	
	
	
	private void writeTriangleKML(BufferedWriter bw, float[][] vertices, 
			Projection proj, ProjectionRect bbox, double xCellSize, double yCellSize, double[] zVals) throws IOException {

		// check for NaN in vertex list
		for (float[] xyz : vertices) {
			for (float x: xyz) {
				if (Float.isNaN(x)) {
					return;
				}
		
			}
		}
		
		
		for (float[] xyz : vertices) {
			double val = calcZValue(xyz[2]-1, zVals);
			minZVal = Math.min(val, minZVal);
			maxZVal = Math.max(val, maxZVal);
		}



		//-77.05788457660967,38.87253259892824,100  
		// all triangles - 3 vertices with xyz

		if (shapeType == KMLShapeType.POLYGON) {
			bw.write("  <Polygon> \n");
			bw.write("   <altitudeMode>absolute</altitudeMode> \n");
			bw.write("   <outerBoundaryIs> \n");
			bw.write("    <LinearRing> \n");
		}
		else {
			bw.write("  <LineString> \n");
			bw.write("   <altitudeMode>absolute</altitudeMode> \n");
		}
		bw.write("     <coordinates> \n");
		
		
		double startX = (xCellSize > 0) ? bbox.getMinX() : bbox.getMaxX();
		double startY = (yCellSize > 0) ? bbox.getMinY() : bbox.getMaxY();
		

		ProjectionPointImpl projPoint = new ProjectionPointImpl(
				startX+vertices[0][0]*xCellSize,
				startY+vertices[0][1]*yCellSize);
		LatLonPointImpl llPoint = new LatLonPointImpl();
		proj.projToLatLon(projPoint, llPoint);

//		if (vertices[0][2]<2) {
//			System.out.println(Arrays.toString(vertices[0]));
//					System.out.println(llPoint.getLongitude()+" / "+
//							llPoint.getLatitude()+" / "+					
//							vertices[0][2]*zScale);
//		}

					if (Double.isNaN(llPoint.getLatitude())) {
						System.out.println("######################## "+llPoint);
					}
					if (WCTUtils.DECFMT_0D0000.format(llPoint.getLatitude()).equals("?")) {
						System.out.println("######################## "+llPoint);
					}

					if (String.valueOf(llPoint.getLongitude()).equals("?")) {
						System.out.println("######################## "+llPoint);
					}
					if (WCTUtils.DECFMT_0D0000.format(llPoint.getLongitude()).equals("?")) {
						System.out.println("######################## "+llPoint);
					}

		
		bw.write(WCTUtils.DECFMT_0D0000.format(llPoint.getLongitude())+","+
				WCTUtils.DECFMT_0D0000.format(llPoint.getLatitude())+","+			
				calcZValue(vertices[0][2]-1, zVals) + "\n");	



		int vertexIndex = (yCellSize > 0) ? 1 : 2;

		projPoint = new ProjectionPointImpl(
				startX+vertices[vertexIndex][0]*xCellSize,
				startY+vertices[vertexIndex][1]*yCellSize);
		llPoint = new LatLonPointImpl();
		proj.projToLatLon(projPoint, llPoint);

		bw.write(WCTUtils.DECFMT_0D0000.format(llPoint.getLongitude())+","+
				WCTUtils.DECFMT_0D0000.format(llPoint.getLatitude())+","+					
				calcZValue(vertices[vertexIndex][2]-1, zVals) + "\n");


		vertexIndex = (yCellSize > 0) ? 2 : 1;

		projPoint = new ProjectionPointImpl(
				startX+vertices[vertexIndex][0]*xCellSize,
				startY+vertices[vertexIndex][1]*yCellSize);
		llPoint = new LatLonPointImpl();
		proj.projToLatLon(projPoint, llPoint);

		bw.write(WCTUtils.DECFMT_0D0000.format(llPoint.getLongitude())+","+
				WCTUtils.DECFMT_0D0000.format(llPoint.getLatitude())+","+				
				calcZValue(vertices[vertexIndex][2]-1, zVals) + "\n");




		projPoint = new ProjectionPointImpl(
				startX+vertices[0][0]*xCellSize,
				startY+vertices[0][1]*yCellSize);
		llPoint = new LatLonPointImpl();
		proj.projToLatLon(projPoint, llPoint);

		bw.write(WCTUtils.DECFMT_0D0000.format(llPoint.getLongitude())+","+
				WCTUtils.DECFMT_0D0000.format(llPoint.getLatitude())+","+			
				calcZValue(vertices[0][2]-1, zVals) + "\n");	




		bw.write("     </coordinates> \n");
		if (shapeType == KMLShapeType.POLYGON) {
			bw.write("    </LinearRing> \n");
			bw.write("   </outerBoundaryIs> \n");
			bw.write("  </Polygon> \n");
		}
		else {
			bw.write("  </LineString> \n");
		}



	}


	
	
	
	
	/**
	 * The KML extrude tag is not used, because when transparency is applied, the effect is different between the 
	 * extruded area and the isosurface triangle polygons.
	 * @param bw
	 * @param vertices  ArrayList of 'x,y' comma delimited strings (25.0,3.4) in grid coordinates (not geographic)
	 * @param proj
	 * @param bbox
	 * @param xCellSize
	 * @param yCellSize
	 * @param zVals
	 * @throws IOException
	 */
	private void writeBaseExtrudeKML(BufferedWriter bw, ArrayList<String> vertices, 
			Projection proj, ProjectionRect bbox, double xCellSize, double yCellSize, double[] zVals) throws IOException {

		for (int n=0; n<vertices.size(); n++) {
			processBaseExtrudeShape(bw, vertices.get(n), 
					proj, bbox, xCellSize, yCellSize, zVals);
		}
	
	}
	
	
	
	/**
	 * 
	 * @param shapeType
	 * @param bw
	 * @param vertexString  x1,y1,x2,y2 format
	 * @param proj
	 * @param bbox
	 * @param xCellSize
	 * @param yCellSize
	 * @param zVals
	 * @throws IOException
	 */
	private void processBaseExtrudeShape(BufferedWriter bw, String vertexString,  
			Projection proj, ProjectionRect bbox, double xCellSize, double yCellSize, double[] zVals) throws IOException {
		

			// write manual extrude polygon from z[0] to 0.0 absolute altitude

			String[] xy = vertexString.split(",");
			if (vertexString.contains("NaN")) {
				return;
			}
			double vertX1 = Double.parseDouble(xy[0]);
			double vertY1 = Double.parseDouble(xy[1]);
			double vertX2 = Double.parseDouble(xy[2]);
			double vertY2 = Double.parseDouble(xy[3]);

			if (yCellSize < 0) {
				vertX1 = Double.parseDouble(xy[2]);
				vertY1 = Double.parseDouble(xy[3]);
				vertX2 = Double.parseDouble(xy[0]);
				vertY2 = Double.parseDouble(xy[1]);
			}
			

			if (shapeType == KMLShapeType.POLYGON) {
				bw.write("  <Polygon> \n");
				bw.write("   <altitudeMode>absolute</altitudeMode> \n");
				bw.write("   <outerBoundaryIs> \n");
				bw.write("    <LinearRing> \n");
			}
			else {
				bw.write("  <LineString> \n");
				bw.write("   <altitudeMode>absolute</altitudeMode> \n");
			}
			bw.write("     <coordinates> \n");
		

			
			
			double startX = (xCellSize > 0) ? bbox.getMinX() : bbox.getMaxX();
			double startY = (yCellSize > 0) ? bbox.getMinY() : bbox.getMaxY();

			// right side			
			ProjectionPointImpl projPoint = new ProjectionPointImpl(
					startX+vertX2*xCellSize,
					startY+vertY2*yCellSize);
			LatLonPointImpl llPoint1 = new LatLonPointImpl();
			proj.projToLatLon(projPoint, llPoint1);

			// upper right corner 
			bw.write(WCTUtils.DECFMT_0D0000.format(llPoint1.getLongitude())+","+
					WCTUtils.DECFMT_0D0000.format(llPoint1.getLatitude())+","+			
					zVals[0] + "\n");	

			// lower right corner 
			bw.write(WCTUtils.DECFMT_0D0000.format(llPoint1.getLongitude())+","+
					WCTUtils.DECFMT_0D0000.format(llPoint1.getLatitude())+","+			
					"0.0\n");	

			
			
			// left side			
			projPoint = new ProjectionPointImpl(
					startX+vertX1*xCellSize,
					startY+vertY1*yCellSize);
			LatLonPointImpl llPoint2 = new LatLonPointImpl();
			proj.projToLatLon(projPoint, llPoint2);
			
			// lower left corner
			bw.write(WCTUtils.DECFMT_0D0000.format(llPoint2.getLongitude())+","+
					WCTUtils.DECFMT_0D0000.format(llPoint2.getLatitude())+","+			
					"0.0\n");
			
			// upper left corner
			bw.write(WCTUtils.DECFMT_0D0000.format(llPoint2.getLongitude())+","+
					WCTUtils.DECFMT_0D0000.format(llPoint2.getLatitude())+","+			
					zVals[0] + "\n");	

			// upper right corner
			bw.write(WCTUtils.DECFMT_0D0000.format(llPoint1.getLongitude())+","+
					WCTUtils.DECFMT_0D0000.format(llPoint1.getLatitude())+","+			
					zVals[0] + "\n");	
	
			bw.write("     </coordinates> \n");
			if (shapeType == KMLShapeType.POLYGON) {
				bw.write("    </LinearRing> \n");
				bw.write("   </outerBoundaryIs> \n");
				bw.write("  </Polygon> \n");
			}
			else {
				bw.write("  </LineString> \n");
			}

			
			
			
			
			
			
//String ll1 = WCTUtils.DECFMT_0D0000.format(llPoint1.getLongitude())+","+
//	WCTUtils.DECFMT_0D0000.format(llPoint1.getLatitude());	
//String ll2 = WCTUtils.DECFMT_0D0000.format(llPoint2.getLongitude())+","+
//	WCTUtils.DECFMT_0D0000.format(llPoint2.getLatitude());	
	
//if (ll1.startsWith("?") || ll2.startsWith("?")) {
//	System.out.println(ll1);
//	System.out.println(ll2);
//}
//
//			
//			if (Double.isNaN(llPoint1.getLatitude())) {
//				System.out.println("######################## "+llPoint1);
//			}
//			if (WCTUtils.DECFMT_0D0000.format(llPoint1.getLatitude()).equals("?")) {
//				System.out.println("######################## "+llPoint1);
//			}
//
//			if (String.valueOf(llPoint1.getLongitude()).equals("?")) {
//				System.out.println("######################## "+llPoint1);
//			}
//			if (WCTUtils.DECFMT_0D0000.format(llPoint1.getLongitude()).equals("?")) {
//				System.out.println("######################## "+llPoint1);
//			}
//			if (Double.isNaN(llPoint2.getLatitude())) {
//				System.out.println("######################## "+llPoint2);
//			}
//			if (WCTUtils.DECFMT_0D0000.format(llPoint2.getLatitude()).equals("?")) {
//				System.out.println("######################## "+llPoint2);
//			}
//
//			if (String.valueOf(llPoint2.getLongitude()).equals("?")) {
//				System.out.println("######################## "+llPoint2);
//			}
//			if (WCTUtils.DECFMT_0D0000.format(llPoint2.getLongitude()).equals("?")) {
//				System.out.println("######################## "+llPoint2);
//			}

	}


	
	
	

	private static double calcZValue(float zIndex, double[] zVals) {
		double zValDiff = ( (int)zIndex == zVals.length-1 ) ? 0 : zVals[(int)zIndex+1]-zVals[(int)zIndex];
		double percentFromVertex = (zIndex*10000 % 10000)/10000.0;
		double zValue = zVals[(int)zIndex]+percentFromVertex*zValDiff;
		return zValue;
	}

	
	
	
	
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

//
//	public static void displayIsosurface(String gridDatasetSource, int gridIndex, 
//			double elevationExaggeration,
//			float isoValue, String kmlColor) throws IOException, InvalidRangeException {
//
//		displayIsosurface(gridDatasetSource, gridIndex, null, elevationExaggeration, isoValue, kmlColor);
//	}
//
//
//	public static void displayIsosurface(String gridDatasetSource, int gridIndex, 
//			Rectangle2D.Double bounds, double elevationExaggeration, 
//			float isoValue, String kmlColor) throws IOException, InvalidRangeException {
//
//		GridDataset gds = GridDataset.open(gridDatasetSource);
//
//		try {
//
//			//			GeoGrid grid = gds.findGridByName("Temperature");
//			GridDatatype grid = gds.getGrids().get(gridIndex);
//
//			// subset optionally
//			if (bounds != null) {
//				LatLonRect bbox = new LatLonRect(new LatLonPointImpl(
//						bounds.getMinY(), bounds.getMinX()), 
//						new LatLonPointImpl(bounds.getMaxY(), bounds.getMaxX()));
//
//				grid = grid.makeSubset(null, null, bbox, 1, 1, 1);
//			}
//
//
//			final double[] zVals = grid.getCoordinateSystem().getVerticalAxis().getCoordValues();
//			//    		System.out.println(Arrays.toString(zVals));
////			if (grid.getCoordinateSystem().getVerticalAxis().getUnitsString().equals("Pa")) {
////				for (int n=0; n<zVals.length; n++) {
////					zVals[n] = NexradEquations.getAltitudeFromPressureInMeters(zVals[n]);
////				}
////			}
//
//			for (int n=0; n<zVals.length; n++) {
//				zVals[n] *= elevationExaggeration;
//			}
//
//
//
//			Array volDataArray = grid.readVolumeData(0);
//			float[] volData = (float[]) (volDataArray.get1DJavaArray(Float.class));
//
//			System.out.println("volData.length="+volData.length);
//
//			// should be order of z, y, x
//			System.out.println(Arrays.toString(volDataArray.getIndex().getShape()));
//
//
//			//		int numX = 50;
//			//		int numY = 50;
//			//		int numZ = 10;
//
//			//		short[] data = new short[numX*numY*numZ];
//
//			int numZ = volDataArray.getIndex().getShape(0);
//			int numY = volDataArray.getIndex().getShape(1);
//			int numX = volDataArray.getIndex().getShape(2);
//
//			//			short[] data = new short[volData.length];
//			//			for (int n=0; n<data.length; n++) {
//			//				data[n] = (short)(volData[n]*10);
//			//			}
//
//
//			//		for (int n=0; n<data.length; n++) {
//			//			data[n] = (short)Math.round(Math.random()*4);
//			//			data[n] = (short)n;
//			//		}
//
//
//
//
//
//
//			//		System.out.println("WRITING KML");
//			//			float[] vertices = sg.getVertices();
//
//
//			//		System.out.println("Vertices: "+Arrays.toString(vertices));
//			//		System.out.println("Normals:  "+Arrays.toString(sg.getNormals()));
//
//			// scale = cellsize
//			// offset = llCorner
//			//		double xScale = 0.1;
//			//		double xOffset = -100.0;
//			//		double yScale = 0.1;
//			//		double yOffset = 30.0;
////			final double zScale = 30000.0;
//			final double zScale = Math.abs(zVals[1]-zVals[0]);
//			//		double zOffset = 0;
//
//			
//System.out.println("zScale array: "+Arrays.toString(zVals));			
//			
//			final ProjectionRect bbox = grid.getCoordinateSystem().getBoundingBox();
//			final double xCellSize = bbox.getWidth()/grid.getXDimension().getLength();
//			final double yCellSize = bbox.getHeight()/grid.getYDimension().getLength();
//
//			final Projection proj = grid.getCoordinateSystem().getProjection();
//			//		System.out.println(xCellSize+" / "+yCellSize);
//
//
//
//			final Display3DFrame frame = new Display3DFrame("Isosurface Display");
//		    frame.setPreferredMinMax(-120, -80, 10, 50, 0, 1000);
//		    final double[][][] elementData = new double[20000][4][3];
//		    elementIndex = 0;
//			
//			SurfaceGen sg = new SurfaceGen();
//
//			sg.addTriangleListener(new TriangleListener() {
//				@Override
//				public void processTriangle(float[][] vertices, float[][] normals) {
//					try {
//
//						//						writeTriangleKML(bw, vertices, proj, bbox, xCellSize, yCellSize, zScale);
//						displayTrianglePoly(elementData, elementIndex++, vertices, proj, bbox, xCellSize, yCellSize, zVals);
//
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}
//			});
//
//			sg.apply(volData, numX, numY, numZ, isoValue, 1.0f, 1.0f, 1.0f);
//
//			System.out.println("isosurface done... visualizing in osp...");
//			ElementTessellation elem = new ElementTessellation();
//		    elem.setTiles(elementData);
//		    
//		    frame.addElement(elem);
//		    
//
//		    frame.setAllowQuickRedraw(true);
////		    frame.setPreferredMinMax(-10, 10, -10, 10, -10, 10);		    
//		    frame.setVisible(true);
//		    
//		    
//		    
//
//			//			System.out.println("num vertices = " + sg.getVertices().length);
//			//			System.out.println("num normals  = " + sg.getNormals().length);
//
//			String[] zLevels = new String[zVals.length];
//			for (int n=0; n<zLevels.length; n++) {
//				zLevels[n] = WCTUtils.DECFMT_00.format(zVals[n]) + " "+ grid.getCoordinateSystem().getVerticalAxis().getUnitsString();
//			}
//
//
//			LatLonRect llRect = grid.getCoordinateSystem().getLatLonBoundingBox();
//			Rectangle2D.Double extent = new Rectangle2D.Double(llRect.getLonMin(), llRect.getLatMin(), llRect.getWidth(), llRect.getHeight());
//
//
//			
//			
//			
//			System.out.println("z min/max vals: "+minZVal+" / "+maxZVal);
//			minZVal = 9999999;
//			maxZVal = -9999999;
//			
//		} catch (IOException e) {
//			gds.close();
//			throw e;
//		} catch (InvalidRangeException e) {
//			gds.close();
//			throw e;
//		}
//
//	}





//	private static void displayTrianglePoly(double[][][] elementData, int elementIndex, float[][] vertices, 
//			Projection proj, ProjectionRect bbox, double xCellSize, double yCellSize, double[] zVals) throws IOException {
//
//		
//		if (elementIndex >= elementData.length) {
//			return;
//		}
//		
//		
//		// check for NaN in vertex list
//		for (float[] xy : vertices) {
//			for (float x: xy) {
//				if (Float.isNaN(x)) {
//					return;
//				}
//		
//			}
//		}
//		
//		
//		for (float[] xy : vertices) {
//			double val = calcZValue(vertices[0][2]-1, zVals);
//			minZVal = Math.min(val, minZVal);
//			maxZVal = Math.max(val, maxZVal);
//		}
//
//
//
//		//-77.05788457660967,38.87253259892824,100  
//		// all triangles - 3 vertices with xyz
//
//		ProjectionPointImpl projPoint = new ProjectionPointImpl(
//				bbox.getMinX()+vertices[0][0]*xCellSize,
//				(bbox.getMinY()+vertices[0][1]*yCellSize));
//		LatLonPointImpl llPoint = new LatLonPointImpl();
//		proj.projToLatLon(projPoint, llPoint);
//
////		if (vertices[0][2]<2) {
////			System.out.println(Arrays.toString(vertices[0]));
////					System.out.println(llPoint.getLongitude()+" / "+
////							llPoint.getLatitude()+" / "+					
////							vertices[0][2]*zScale);
////		}
//
////					if (Double.isNaN(llPoint.getLatitude())) {
////						System.out.println(llPoint);
////					}
////					if (WCTUtils.DECFMT_0D0000.format(llPoint.getLatitude()).equals("?")) {
////						System.out.println(llPoint);
////					}
////
////					if (String.valueOf(llPoint.getLongitude()).equals("?")) {
////						System.out.println(llPoint);
////					}
////					if (WCTUtils.DECFMT_0D0000.format(llPoint.getLongitude()).equals("?")) {
////						System.out.println(llPoint);
////					}
//
//		
//		double[][] geoVertices = new double[4][3]; 
//		
//		geoVertices[0] = new double[] {
//				llPoint.getLongitude(),
//				llPoint.getLatitude(),			
//				calcZValue(vertices[0][2]-1, zVals) };	
//
//
//		
//		
//
//
//		projPoint = new ProjectionPointImpl(
//				bbox.getMinX()+vertices[1][0]*xCellSize,
//				(bbox.getMinY()+vertices[1][1]*yCellSize));
//		llPoint = new LatLonPointImpl();
//		proj.projToLatLon(projPoint, llPoint);
//
//		geoVertices[1] = new double[] {
//				llPoint.getLongitude(),
//				llPoint.getLatitude(),			
//				calcZValue(vertices[1][2]-1, zVals) };	
//
//
//		
//		
//		
//		
//
//		projPoint = new ProjectionPointImpl(
//				bbox.getMinX()+vertices[2][0]*xCellSize,
//				(bbox.getMinY()+vertices[2][1]*yCellSize));
//		llPoint = new LatLonPointImpl();
//		proj.projToLatLon(projPoint, llPoint);
//
//		geoVertices[2] = new double[] {
//				llPoint.getLongitude(),
//				llPoint.getLatitude(),			
//				calcZValue(vertices[2][2]-1, zVals) };	
//
//
//		
//		
//		
//		
//
//
//		projPoint = new ProjectionPointImpl(
//				bbox.getMinX()+vertices[0][0]*xCellSize,
//				(bbox.getMinY()+vertices[0][1]*yCellSize));
//		llPoint = new LatLonPointImpl();
//		proj.projToLatLon(projPoint, llPoint);
//
//		geoVertices[3] = new double[] {
//				llPoint.getLongitude(),
//				llPoint.getLatitude(),			
//				calcZValue(vertices[0][2]-1, zVals) };	
//
//
//
//
//	    elementData[elementIndex] = geoVertices;
//	    
//
//
//
//	}


	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
//
//
//	private static void writeTrianglePolyKML(BufferedWriter bw, float[][] vertices, 
//			Rectangle2D.Double bbox, double xCellSize, double yCellSize, double zScale) throws IOException {
//
//
//
//
//		//-77.05788457660967,38.87253259892824,100  
//		// all triangles - 3 vertices with xyz
//
//		bw.write("  <Polygon> \n");
//		//			bw.write("   <extrude>0</extrude> \n");
//		bw.write("   <altitudeMode>relativeToGround</altitudeMode> \n");
//		bw.write("   <outerBoundaryIs> \n");
//		bw.write("    <LinearRing> \n");
//		bw.write("     <coordinates> \n");
//
//		ProjectionPointImpl projPoint = new ProjectionPointImpl(
//				bbox.getMinX()+vertices[0][0]*xCellSize,
//				(bbox.getMinY()+vertices[0][1]*yCellSize));
//		LatLonPointImpl llPoint = new LatLonPointImpl();
//		proj.projToLatLon(projPoint, llPoint);
//
//		//			System.out.println(llPoint.getLongitude()+" / "+
//		//					llPoint.getLatitude()+" / "+					
//		//					vertices[n+2]*zScale);
//
//		bw.write(WCTUtils.DECFMT_0D0000.format(llPoint.getLongitude())+","+
//				WCTUtils.DECFMT_0D0000.format(llPoint.getLatitude())+","+			
//				vertices[0][2]*zScale + "\n");	
//
//
//
//
//		projPoint = new ProjectionPointImpl(
//				bbox.getMinX()+vertices[1][0]*xCellSize,
//				(bbox.getMinY()+vertices[1][1]*yCellSize));
//		llPoint = new LatLonPointImpl();
//		proj.projToLatLon(projPoint, llPoint);
//
//		bw.write(WCTUtils.DECFMT_0D0000.format(llPoint.getLongitude())+","+
//				WCTUtils.DECFMT_0D0000.format(llPoint.getLatitude())+","+					
//				vertices[1][2]*zScale + "\n");
//
//
//
//		projPoint = new ProjectionPointImpl(
//				bbox.getMinX()+vertices[2][0]*xCellSize,
//				(bbox.getMinY()+vertices[2][1]*yCellSize));
//		llPoint = new LatLonPointImpl();
//		proj.projToLatLon(projPoint, llPoint);
//
//		bw.write(WCTUtils.DECFMT_0D0000.format(llPoint.getLongitude())+","+
//				WCTUtils.DECFMT_0D0000.format(llPoint.getLatitude())+","+				
//				vertices[2][2]*zScale + "\n");
//
//
//
//
//		projPoint = new ProjectionPointImpl(
//				bbox.getMinX()+vertices[0][0]*xCellSize,
//				(bbox.getMinY()+vertices[0][1]*yCellSize));
//		llPoint = new LatLonPointImpl();
//		proj.projToLatLon(projPoint, llPoint);
//
//		bw.write(WCTUtils.DECFMT_0D0000.format(llPoint.getLongitude())+","+
//				WCTUtils.DECFMT_0D0000.format(llPoint.getLatitude())+","+			
//				vertices[0][2]*zScale + "\n");	
//
//
//
//
//		bw.write("     </coordinates> \n");
//		bw.write("    </LinearRing> \n");
//		bw.write("   </outerBoundaryIs> \n");
//		bw.write("  </Polygon> \n");
//
//
//	}
//
//
//
//
//
//	
	
	
	
	
	
	
	
	
	
	
	
	

	private static void writeRadialTrianglePolyKML(
			BufferedWriter bw, float[][] vertices, 
			Rectangle2D.Double bounds,
			RadialDatasetSweep.Sweep sweep, MathTransform radarTransform, 
			DecodeRadialDatasetSweepHeader sweepHeader, float[] azimuthArray, double zScale) 
	throws IOException, MismatchedDimensionException, TransformException {




		//-77.05788457660967,38.87253259892824,100  
		// all triangles - 3 vertices with xyz
		

		float[] latLonAltASL0 = getLatLonAltASLFromRadial(vertices[0], sweep, radarTransform, sweepHeader, azimuthArray);
		float[] latLonAltASL1 = getLatLonAltASLFromRadial(vertices[1], sweep, radarTransform, sweepHeader, azimuthArray);
		float[] latLonAltASL2 = getLatLonAltASLFromRadial(vertices[2], sweep, radarTransform, sweepHeader, azimuthArray);

		if (! (bounds.contains(latLonAltASL0[1], latLonAltASL0[0]) && 
				bounds.contains(latLonAltASL1[1], latLonAltASL1[0]) &&
				bounds.contains(latLonAltASL2[1], latLonAltASL2[0])) ) {
			return;
		}

		bw.write("  <Polygon> \n");
		//			bw.write("   <extrude>0</extrude> \n");
		bw.write("   <altitudeMode>relativeToGround</altitudeMode> \n");
		bw.write("   <outerBoundaryIs> \n");
		bw.write("    <LinearRing> \n");
		bw.write("     <coordinates> \n");

		
		
		bw.write(WCTUtils.DECFMT_0D0000.format(latLonAltASL0[0])+","+
				WCTUtils.DECFMT_0D0000.format(latLonAltASL0[1])+","+			
				latLonAltASL0[2]*zScale + "\n");	
		
		
		bw.write(WCTUtils.DECFMT_0D0000.format(latLonAltASL1[0])+","+
				WCTUtils.DECFMT_0D0000.format(latLonAltASL1[1])+","+			
				latLonAltASL1[2]*zScale + "\n");	
		
		
		bw.write(WCTUtils.DECFMT_0D0000.format(latLonAltASL2[0])+","+
				WCTUtils.DECFMT_0D0000.format(latLonAltASL2[1])+","+			
				latLonAltASL2[2]*zScale + "\n");	

		
		bw.write(WCTUtils.DECFMT_0D0000.format(latLonAltASL0[0])+","+
				WCTUtils.DECFMT_0D0000.format(latLonAltASL0[1])+","+			
				latLonAltASL0[2]*zScale + "\n");	

		

		bw.write("     </coordinates> \n");
		bw.write("    </LinearRing> \n");
		bw.write("   </outerBoundaryIs> \n");
		bw.write("  </Polygon> \n");


	}



	private static float[] getLatLonAltASLFromRadial(float[] vertex, 
				RadialDatasetSweep.Sweep sweep, MathTransform radarTransform, 
				DecodeRadialDatasetSweepHeader sweepHeader, float[] azimuthArray) 
		throws IOException, MismatchedDimensionException, TransformException {



		int gateNum = sweep.getGateNumber();
		int radialNum = sweep.getRadialNumber();
		float gateSize = sweep.getGateSize();
		float rangeToFirstGate = sweep.getRangeToFirstGate();
		float sweepElev = sweep.getMeanElevation();

		// vertex array in x,y,z order = range,azimuth,height in index space
		// need to convert to lat/lon/alt units

		// 1. Get Range
		double range =  rangeToFirstGate + gateSize/2 + Math.cos(Math.toRadians(sweepElev))*vertex[0];

		// 2. Get Azimuth
		int azimuthIndexBegin = (int)vertex[1];
		int azimuthIndexEnd = (azimuthIndexBegin == azimuthArray.length+1) ? azimuthIndexBegin : azimuthIndexBegin+1;
		double azDiff = azimuthArray[azimuthIndexEnd] - azimuthArray[azimuthIndexBegin];
		double percentOfAz = vertex[1]-((int)vertex[1]);
		double azimuth = azimuthArray[azimuthIndexBegin]+percentOfAz*azDiff;

		// 3. Get Height
		double rangeInAir = rangeToFirstGate + gateSize/2 + vertex[0];
		double height = NexradEquations.getRelativeBeamHeight(
				Math.cos(Math.toRadians(sweepElev)), 
				Math.sin(Math.toRadians(sweepElev)), rangeInAir);
		// add height of radar site in meters
		double heightASL = height + sweepHeader.getAlt()/3.28083989501312; 


		// 4. Convert Range/Azimuth/Height to lat/lon/alt
		double xpos = range * Math.sin(Math.toRadians(azimuth));
		double ypos = range * Math.cos(Math.toRadians(azimuth));
		double[] geoXY = (radarTransform.transform(new CoordinatePoint(xpos, ypos), null)).getCoordinates();
		
		
		return new float[] { (float)geoXY[1], (float)geoXY[0], (float)heightASL };
	}

	
	
	
	
	
	
	
	
	
	
	
	






	/**
	 * Add a line 3D cube bounding box with labels
	 * @param bounds
	 * @throws Exception
	 */
	private static String addKML3DOutlineBoxFolder(Rectangle2D bounds, String[] zLevels, double zScale) throws Exception {

		StringBuilder kmlString = new StringBuilder();

		kmlString.append("   <Folder> \n");
		kmlString.append("   <name>Exported Data Extent - 3D Cube Outline</name> \n");
		kmlString.append("   <visibility>0</visibility> \n");
		kmlString.append("      <description><![CDATA[" +
				"This represents the extent of the exported data:  " +
				"To adjust transparency of just the legend and logo, " +
		"select this folder and adjust transparency slider. ]]></description> \n");


		
        // set to zero because we are doing absolute height from sea level, not radar elevation
        double nexradAltInMeters = 0;

//		for (int n=0; n<zLevels.length; n++) {
        for (int n=4; n<16+1; n=n+4) {

//			String labelCoords = bounds.getMinX()+","+bounds.getMinY()+","+((n)*zScale);

        	String label = n+" km";
        	if (n == 0) {
        		label += " Above Sea Level";
        	}
        	else {
        		label += " ASL";
        	}

        	String labelCoords = bounds.getMinX()+","+bounds.getMinY()+","+(n*1000 + nexradAltInMeters);
        	String kmlGeomOutline = KMLUtils.getExtentLineKML(bounds, (n*1000 + nexradAltInMeters));
			

//			String kmlGeomOutline = KMLUtils.getExtentLineKML(bounds, (n)*zScale);
			kmlString.append("    <Placemark>\n");
			kmlString.append("      <name>Outline</name>\n");
			kmlString.append("      <visibility>0</visibility>\n");
			kmlString.append("      <styleUrl>#box3dStyle</styleUrl>\n");
			kmlString.append(kmlGeomOutline);
			kmlString.append("    </Placemark>\n");
			kmlString.append("    <Placemark>\n");
//			kmlString.append("      <name>"+zLevels[n]+"</name>\n");
			kmlString.append("      <name>"+label+"</name>\n");
			kmlString.append("      <visibility>0</visibility>\n");
			kmlString.append("      <styleUrl>#pointStyle</styleUrl>\n");
			kmlString.append("      <Point>\n");
			kmlString.append("          <altitudeMode>absolute</altitudeMode>\n");
			kmlString.append("          <coordinates>"+labelCoords+"</coordinates>\n");        	
			kmlString.append("      </Point>\n");
			kmlString.append("    </Placemark>\n");
		}
		kmlString.append("    <Placemark>\n");
		kmlString.append("      <name>Outline</name>\n");
		kmlString.append("      <visibility>0</visibility>\n");
		kmlString.append("      <styleUrl>#box3dStyle</styleUrl>\n");
		kmlString.append("      <LineString>\n");
		kmlString.append("           <altitudeMode>absolute</altitudeMode>\n");
//		kmlString.append("           <coordinates>"+bounds.getMinX()+","+bounds.getMinY()+","+ 
//				0*zScale);        	
//		kmlString.append("                        "+bounds.getMinX()+","+bounds.getMinY()+","+ 
//				((zLevels.length-1)*zScale)+"</coordinates>\n");
    	kmlString.append("           <coordinates>"+bounds.getMinX()+","+bounds.getMinY()+","+ 
    					(0*1000 + nexradAltInMeters));        	
    	kmlString.append("                        "+bounds.getMinX()+","+bounds.getMinY()+","+ 
						(16*1000 + nexradAltInMeters)+"</coordinates>\n");        			
		kmlString.append("      </LineString>\n");
		kmlString.append("    </Placemark>\n");
		kmlString.append("    <Placemark>\n");
		kmlString.append("      <name>Outline</name>\n");
		kmlString.append("      <visibility>0</visibility>\n");
		kmlString.append("      <styleUrl>#box3dStyle</styleUrl>\n");
		kmlString.append("      <LineString>\n");
		kmlString.append("           <altitudeMode>absolute</altitudeMode>\n");
//		kmlString.append("           <coordinates>"+bounds.getMaxX()+","+bounds.getMinY()+","+ 
//				0*zScale);        	
//		kmlString.append("                        "+bounds.getMaxX()+","+bounds.getMinY()+","+ 
//				((zLevels.length-1)*zScale)+"</coordinates>\n");        	
    	kmlString.append("           <coordinates>"+bounds.getMaxX()+","+bounds.getMinY()+","+ 
    					(0*1000 + nexradAltInMeters));        	
    	kmlString.append("                        "+bounds.getMaxX()+","+bounds.getMinY()+","+ 
    					(16*1000 + nexradAltInMeters)+"</coordinates>\n");        	
		kmlString.append("      </LineString>\n");
		kmlString.append("    </Placemark>\n");
		kmlString.append("    <Placemark>\n");
		kmlString.append("      <name>Outline</name>\n");
		kmlString.append("      <visibility>0</visibility>\n");
		kmlString.append("      <styleUrl>#box3dStyle</styleUrl>\n");
		kmlString.append("      <LineString>\n");
		kmlString.append("           <altitudeMode>absolute</altitudeMode>\n");
//		kmlString.append("           <coordinates>"+bounds.getMaxX()+","+bounds.getMaxY()+","+ 
//				0*zScale);        	
//		kmlString.append("                        "+bounds.getMaxX()+","+bounds.getMaxY()+","+
//				((zLevels.length-1)*zScale)+"</coordinates>\n");
    	kmlString.append("           <coordinates>"+bounds.getMaxX()+","+bounds.getMaxY()+","+ 
    					(0*1000 + nexradAltInMeters));        	
    	kmlString.append("                        "+bounds.getMaxX()+","+bounds.getMaxY()+","+
						(16*1000 + nexradAltInMeters)+"</coordinates>\n");        			
		kmlString.append("      </LineString>\n");
		kmlString.append("    </Placemark>\n");
		kmlString.append("    <Placemark>\n");
		kmlString.append("      <name>Outline</name>\n");
		kmlString.append("      <visibility>0</visibility>\n");
		kmlString.append("      <styleUrl>#box3dStyle</styleUrl>\n");
		kmlString.append("      <LineString>\n");
		kmlString.append("           <altitudeMode>absolute</altitudeMode>\n");
//		kmlString.append("           <coordinates>"+bounds.getMinX()+","+bounds.getMaxY()+","+ 
//				0*zScale);        	
//		kmlString.append("                        "+bounds.getMinX()+","+bounds.getMaxY()+","+
//				((zLevels.length-1)*zScale)+"</coordinates>\n");
    	kmlString.append("           <coordinates>"+bounds.getMinX()+","+bounds.getMaxY()+","+ 
    					(0*1000 + nexradAltInMeters));        	
    	kmlString.append("                        "+bounds.getMinX()+","+bounds.getMaxY()+","+
						(16*1000 + nexradAltInMeters)+"</coordinates>\n");        	
		kmlString.append("      </LineString>\n");
		kmlString.append("    </Placemark>\n");



		kmlString.append("    <Style id=\"box3dStyle\"> \n");
		kmlString.append("      <LineStyle> \n");
		kmlString.append("        <width>1.0</width> \n");
		kmlString.append("        <color>ddffffff</color> \n");
		kmlString.append("      </LineStyle> \n");
		kmlString.append("      <PolyStyle> \n");
		kmlString.append("        <color>01000000</color> \n");
		kmlString.append("      </PolyStyle> \n");
		kmlString.append("    </Style> \n");
		
        kmlString.append("    <Style id=\"pointStyle\"> \n");
        kmlString.append("   	<IconStyle> \n");
        kmlString.append("   		<scale>0.5</scale> \n");
        kmlString.append("   		<Icon>  \n");
        kmlString.append("   			<href>http://maps.google.com/mapfiles/kml/shapes/shaded_dot.png</href> \n");
        kmlString.append("   		</Icon> \n");
        kmlString.append("      </IconStyle> \n");
        kmlString.append("    </Style> \n");


		kmlString.append("   </Folder> \n");


		return kmlString.toString();
	}



	
	
	public void setRadialExportGridSmoothFactor(int exportGridSmoothFactor) {
		this.radialExportGridSmoothFactor = exportGridSmoothFactor;
	}
	public int getRadialExportGridSmoothFactor() {
		return radialExportGridSmoothFactor;
	}
	public void setRadialNumCappis(int numCappis) {
		this.radialNumCappis = numCappis;
	}
	public int getRadialNumCappis() {
		return radialNumCappis;
	}
	public void setRadialCappiSpacingInMeters(double cappiSpacingInMeters) {
		this.radialCappiSpacingInMeters = cappiSpacingInMeters;
	}
	public double getRadialCappiSpacingInMeters() {
		return radialCappiSpacingInMeters;
	}
	public void setRadialRemappedGridCellsize(double cellsize) {
		this.radialRemappedGridCellsize = cellsize;
	}
	public double getRadialRemappedGridCellsize() {
		return radialRemappedGridCellsize;
	}
	public void setRadialRemappedGridSizeMax(int radialRemappedGridSizeMax) {
		this.radialRemappedGridSizeMax = radialRemappedGridSizeMax;
	}
	public int getRadialRemappedGridSizeMax() {
		return radialRemappedGridSizeMax;
	}
	public CAPPIType getRadialCappiType() {
		return radialCappiType;
	}



	public void setRadialCappiType(CAPPIType radialCappiType) {
		this.radialCappiType = radialCappiType;
	}



	public void setGridSizeMax(int gridSizeMax) {
		this.gridSizeMax = gridSizeMax;
	}
	public int getGridSizeMax() {
		return gridSizeMax;
	}

	public void setShapeType(KMLShapeType shapeType) {
		this.shapeType = shapeType;
	}

	public KMLShapeType getShapeType() {
		return shapeType;
	}

	


	public void clearListeners() {
		this.listeners.removeAllElements();
	}
	
	public void addDataDecodeListener(DataDecodeListener l) {
		this.listeners.add(l);
	}



	public void setTourSelected(boolean isTourSelected) {
		this.isTourSelected = isTourSelected;
	}



	public boolean isTourSelected() {
		return isTourSelected;
	}


	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	



	public void generateIsosurfaceOBJ(String gridDatasetSource, int gridIndex, 
			int elevationExaggeration,
			float[] isoValues, String[] kmlColors, File outObjFile) throws IOException, InvalidRangeException {

		generateIsosurfaceOBJ(gridDatasetSource, gridIndex, null, elevationExaggeration, isoValues, kmlColors, outObjFile);
	}


	public void generateIsosurfaceOBJ(String gridDatasetSource, int gridIndex, 
			Rectangle2D.Double bounds, int elevationExaggeration, 
			float[] isoValues, String[] kmlColors, File outObjFile) throws IOException, InvalidRangeException {

		GridDataset gds = GridDataset.open(gridDatasetSource);

		try {

			//			GeoGrid grid = gds.findGridByName("Temperature");
			GridDatatype grid = gds.getGrids().get(gridIndex);

			// subset optionally
			if (bounds != null) {
				LatLonRect bbox = new LatLonRect(new LatLonPointImpl(
						bounds.getMinY(), bounds.getMinX()), 
						new LatLonPointImpl(bounds.getMaxY(), bounds.getMaxX()));

				grid = grid.makeSubset(null, null, bbox, 1, 1, 1);
			}

			
			// apply some stride if the grid size is large
			int xLength = grid.getCoordinateSystem().getXHorizAxis().getDimension(0).getLength();
			int yLength = grid.getCoordinateSystem().getYHorizAxis().getDimension(0).getLength();
			if (xLength > getGridSizeMax() || yLength > getGridSizeMax()) {
				System.out.println("grid stride enabled: "+
						((int)(xLength/getGridSizeMax()))+1+" , "+
						((int)(yLength/getGridSizeMax()))+1);
				
				grid = grid.makeSubset(null, null, null, 1, 
						((int)(yLength/getGridSizeMax()))+1, 
						((int)(xLength/getGridSizeMax()))+1);
			}

			
			
			if (grid.getCoordinateSystem().getVerticalAxis() == null) {
				throw new IOException("This dataset does not have a vertical axis.");
			}
			

			final double[] zVals = grid.getCoordinateSystem().getVerticalAxis().getCoordValues();
			//    		System.out.println(Arrays.toString(zVals));
			if (grid.getCoordinateSystem().getVerticalAxis().getUnitsString().equals("Pa")) {
				for (int n=0; n<zVals.length; n++) {
					zVals[n] = NexradEquations.getAltitudeFromPressureInMeters(zVals[n]);
				}
			}
			else if (! grid.getCoordinateSystem().isZPositive()) {
				elevationExaggeration *= -1;
			}

			for (int n=0; n<zVals.length; n++) {
				zVals[n] *= elevationExaggeration;
			}

			
			
			


			Array volDataArray = grid.readVolumeData(0);
			float[] volData = (float[]) (volDataArray.get1DJavaArray(Float.class));

			System.out.println("volData.length="+volData.length);

			// should be order of z, y, x
			System.out.println(Arrays.toString(volDataArray.getIndex().getShape()));


			int numZ = volDataArray.getIndex().getShape(0);
			int numY = volDataArray.getIndex().getShape(1);
			int numX = volDataArray.getIndex().getShape(2);

			final BufferedWriter bw = new BufferedWriter(new FileWriter(outObjFile));
//			bw.write(getKMLBegin(grid.getName()+" Isosurfaces"));

			final double zScale = Math.abs(zVals[1]-zVals[0]);

			Array yAxisDataTest = grid.getCoordinateSystem().getYHorizAxis().read("1:2:1");
			Array xAxisDataTest = grid.getCoordinateSystem().getXHorizAxis().read("1:2:1");
			int yAxisOrder = (yAxisDataTest.getDouble(0) > yAxisDataTest.getDouble(1)) ? -1 : 1;
			int xAxisOrder = (xAxisDataTest.getDouble(0) > xAxisDataTest.getDouble(1)) ? -1 : 1;
			
			
			final ProjectionRect bbox = grid.getCoordinateSystem().getBoundingBox();
			final double xCellSize = bbox.getWidth()/grid.getXDimension().getLength()*xAxisOrder;
			final double yCellSize = bbox.getHeight()/grid.getYDimension().getLength()*yAxisOrder;

			final Projection proj = grid.getCoordinateSystem().getProjection();

			

			SurfaceGen sg = new SurfaceGen();

			final ArrayList<String> bottomCoordList = new ArrayList<String>();

			vertexIndex = 0;
			sg.addTriangleListener(new TriangleListener() {
				
				@Override
				public void processTriangle(float[][] vertices, float[][] normals) {
					try {
						// store z=0 line strings so we can manually extrude to the surface
						if (vertices[0][2] == 1.0 && vertices[1][2] == 1.0) {
							bottomCoordList.add(vertices[1][0]+","+vertices[1][1]+","+vertices[0][0]+","+vertices[0][1]);
//							System.out.println("1: "+Arrays.deepToString(vertices));
						}
						if (vertices[0][2] == 1.0 && vertices[2][2] == 1.0) {
							bottomCoordList.add(vertices[0][0]+","+vertices[0][1]+","+vertices[2][0]+","+vertices[2][1]);
//							System.out.println("2: "+Arrays.deepToString(vertices));
						}
						if (vertices[1][2] == 1.0 && vertices[2][2] == 1.0) {
							bottomCoordList.add(vertices[2][0]+","+vertices[2][1]+","+vertices[1][0]+","+vertices[1][1]);
//							System.out.println("3: "+Arrays.deepToString(vertices));
						}
						
						writeTriangleOBJ(bw, vertices, proj, bbox, xCellSize, yCellSize, zVals);
						vertexIndex += 3;

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});

			


			DataDecodeEvent dde = new DataDecodeEvent(this);
			for (int n=0; n<isoValues.length; n++) {
				
				dde.setProgress((int)(WCTUtils.progressCalculator(new int[] { n }, new int[] { isoValues.length })*100));
				dde.setStatus("Generating Isosurface: "+
						WCTUtils.DECFMT_0D00.format(isoValues[n])+" "+
						grid.getUnitsString());
				for (DataDecodeListener l : listeners) {
					l.decodeProgress(dde);
				}
				
				String startTime = null;
				String endTime = null;
				try {
					if (grid.getCoordinateSystem().getDateRange() != null) {
						System.out.println("------------: "+grid.getCoordinateSystem().getDateRange().getStart().getDate());
						startTime =  ViewerKmzUtilities.ISO_DATE_FORMATTER.format( grid.getCoordinateSystem().getDateRange().getStart().getDate() );
						endTime =  ViewerKmzUtilities.ISO_DATE_FORMATTER.format( 
								new Date(grid.getCoordinateSystem().getDateRange().getStart().getDate().getTime()+(1000*60*5L)) );
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
//				bw.write(getTriangleKMLBegin(kmlColors[n], 
//						"Isosurface: "+WCTUtils.DECFMT_0D00.format(isoValues[n])+" "+
//						grid.getUnitsString(), startTime, endTime));
				sg.apply(volData, numX, numY, numZ, isoValues[n], 1.0f, 1.0f, 1.0f);
				
				
				
				
//				System.out.println(bottomCoordList);
//				writeBaseExtrudeKML(bw, bottomCoordList, proj, bbox, xCellSize, yCellSize, zVals);
//				bottomCoordList.clear();
				
				
				
//				bw.write(getTriangleKMLEnd());
				writeOBJFaceList(bw, vertexIndex);

			}
			
			
			for (DataDecodeListener l : listeners) {
				l.decodeEnded(dde);
			}
			
			

			bw.close();

			
			
			
			System.out.println("z min/max vals: "+minZVal+" / "+maxZVal);
			minZVal = 9999999;
			maxZVal = -9999999;
			
			gds.close();
			
		} catch (IOException e) {
			gds.close();
			throw e;
		} catch (InvalidRangeException e) {
			gds.close();
			throw e;
		}

	}


	
	

	private void writeTriangleOBJ(BufferedWriter bw, float[][] vertices, 
			Projection proj, ProjectionRect bbox, double xCellSize, double yCellSize, double[] zVals) throws IOException {

		// check for NaN in vertex list
		for (float[] xyz : vertices) {
			for (float x: xyz) {
				if (Float.isNaN(x)) {
					return;
				}
		
			}
		}
		
		
		for (float[] xyz : vertices) {
			double val = calcZValue(xyz[2]-1, zVals);
			minZVal = Math.min(val, minZVal);
			maxZVal = Math.max(val, maxZVal);
		}



		//-77.05788457660967,38.87253259892824,100  
		// all triangles - 3 vertices with xyz

		if (shapeType == KMLShapeType.POLYGON) {
//			bw.write("  <Polygon> \n");
//			bw.write("   <altitudeMode>absolute</altitudeMode> \n");
//			bw.write("   <outerBoundaryIs> \n");
//			bw.write("    <LinearRing> \n");
		}
		else {
//			bw.write("  <LineString> \n");
//			bw.write("   <altitudeMode>absolute</altitudeMode> \n");
		}
//		bw.write("     <coordinates> \n");
		
		
		double startX = (xCellSize > 0) ? bbox.getMinX() : bbox.getMaxX();
		double startY = (yCellSize > 0) ? bbox.getMinY() : bbox.getMaxY();
		

		ProjectionPointImpl projPoint = new ProjectionPointImpl(
				startX+vertices[0][0]*xCellSize,
				startY+vertices[0][1]*yCellSize);

		bw.write("v "+WCTUtils.DECFMT_0D0000.format(projPoint.getX())+" "+
				WCTUtils.DECFMT_0D0000.format(projPoint.getY())+" "+			
				calcZValue(vertices[0][2]-1, zVals));	
		bw.newLine();



		int vertexIndex = (yCellSize > 0) ? 1 : 2;

		projPoint = new ProjectionPointImpl(
				startX+vertices[vertexIndex][0]*xCellSize,
				startY+vertices[vertexIndex][1]*yCellSize);

		bw.write("v "+WCTUtils.DECFMT_0D0000.format(projPoint.getX())+" "+
				WCTUtils.DECFMT_0D0000.format(projPoint.getY())+" "+			
				calcZValue(vertices[vertexIndex][2]-1, zVals));
		bw.newLine();


		vertexIndex = (yCellSize > 0) ? 2 : 1;

		projPoint = new ProjectionPointImpl(
				startX+vertices[vertexIndex][0]*xCellSize,
				startY+vertices[vertexIndex][1]*yCellSize);

		bw.write("v "+WCTUtils.DECFMT_0D0000.format(projPoint.getX())+" "+
				WCTUtils.DECFMT_0D0000.format(projPoint.getY())+" "+			
				calcZValue(vertices[vertexIndex][2]-1, zVals));
		bw.newLine();





//		bw.write("     </coordinates> \n");
		if (shapeType == KMLShapeType.POLYGON) {
//			bw.write("    </LinearRing> \n");
//			bw.write("   </outerBoundaryIs> \n");
//			bw.write("  </Polygon> \n");
		}
		else {
//			bw.write("  </LineString> \n");
		}


	}

	
	
	private void writeOBJFaceList(BufferedWriter bw, int vertexCount) throws IOException {
		for (int i=0; i<vertexCount; i=i+3) {
			bw.write("f "+(i+1)+" "+(i+2)+" "+(i+3));
			bw.newLine();
		}
	}
	
	

	public void generateIsosurfaceOBJ(String radialDatasetSource, 
			String variableName, final Rectangle2D.Double bounds,
			float[] isoValues, String[] kmlColors, File outObjFile) throws Exception {


		
		double[] cappiHeights = new double[radialNumCappis];
		
		RadialDatasetSweepRemappedRaster raster = new RadialDatasetSweepRemappedRaster();
		for (int n=0; n<radialNumCappis; n++) {
			cappiHeights[n] = n*radialCappiSpacingInMeters;
		}
		
		int rasterWidth = (int)Math.round(bounds.getWidth()/radialRemappedGridCellsize);
		int rasterHeight = (int)Math.round(bounds.getHeight()/radialRemappedGridCellsize);
		System.out.println("raster width/height: "+rasterWidth+" / "+rasterHeight);
		if (rasterWidth > radialRemappedGridSizeMax || rasterHeight > radialRemappedGridSizeMax) {
			rasterWidth = radialRemappedGridSizeMax;
			rasterHeight = radialRemappedGridSizeMax;
		}
		System.out.println("raster width/height: "+rasterWidth+" / "+rasterHeight);
			
		raster.setWidth(rasterWidth);
		raster.setHeight(rasterHeight);
		System.out.println("raster width/height: "+rasterWidth+" / "+rasterHeight);
		
		
		for (DataDecodeListener l : listeners) {
			raster.addDataDecodeListener(l);
		}
		
		
		
		raster.setVariableName(variableName);
		System.out.println("processing cappi...");
		raster.processCAPPI(radialDatasetSource, bounds, cappiHeights, radialCappiType);
		
		// smooth
		if (radialExportGridSmoothFactor > 0) {
			
			WritableRaster multiBandRaster = raster.getWritableRaster();			
			for (int n=0; n<radialNumCappis; n++) {
				WritableRaster tmpRaster = RasterFactory.createBandedRaster(
						DataBuffer.TYPE_FLOAT, multiBandRaster.getWidth(), 
						multiBandRaster.getHeight(), 1, null);

				float[] tmpData = new float[raster.getWidth()*raster.getHeight()];
				tmpData = multiBandRaster.getSamples(0, 0, raster.getWidth(), raster.getHeight(), n, tmpData);
				tmpRaster.setSamples(0, 0, raster.getWidth(), raster.getHeight(), 0, tmpData);
				raster.setWritableRaster(tmpRaster);				
				
				raster.setSmoothingFactor(radialExportGridSmoothFactor);
				GridCoverage gc = raster.getGridCoverage(0);
				java.awt.image.RenderedImage renderedImage = gc.getRenderedImage();
//				raster.setWritableRaster((WritableRaster)(renderedImage.getData()));
				
				// set smoothed data in original multiband raster
				tmpData = ((WritableRaster)(renderedImage.getData())).getSamples(
						0, 0, raster.getWidth(), raster.getHeight(), 0, tmpData);
				multiBandRaster.setSamples(0, 0, raster.getWidth(), raster.getHeight(), n, tmpData);
				
				System.out.println("smoothed data for band "+n);
				
			}
			raster.setWritableRaster(multiBandRaster);
		}
			
		

		WCTRasterExport rasterExport = new WCTRasterExport();
		File outFile = new File(outObjFile+".nc");
		// add site elevation to cappi heights
		for (int n=0; n<radialNumCappis; n++) {
			cappiHeights[n] = cappiHeights[n] + raster.getLastDecodedSweepHeader().getAlt() * 0.3048;
		}

		rasterExport.saveNetCDF(outFile, raster, cappiHeights);
		
		
		
		GridDataset gds = GridDataset.open(outObjFile+".nc");
		List<GridDatatype> gridList = gds.getGrids();
		int gridIndex = -1;
		for (int n=0; n<gridList.size(); n++) {
			if (gridList.get(n).getName().equals("Reflectivity")) {
				gridIndex = n;
			}
		}
		
		int holdValue = getGridSizeMax();
		setGridSizeMax(radialRemappedGridSizeMax);
		generateIsosurfaceOBJ(outObjFile+".nc", gridIndex, 1, isoValues, kmlColors, outObjFile);
		setGridSizeMax(holdValue);
		
		
		for (DataDecodeListener l : listeners) {
			raster.removeDataDecodeListener(l);
		}

		
	}
	


}
