package gov.noaa.ncdc.wct.morph;

import gov.noaa.ncdc.wct.WCTFilter;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.DecodeHintNotSupportedException;
import gov.noaa.ncdc.wct.decoders.SampleDimensionAndLabels;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradColorFactory;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradSampleDimensionFactory;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradValueFactory;
import gov.noaa.ncdc.wct.event.DataExportEvent;
import gov.noaa.ncdc.wct.event.DataExportListener;
import gov.noaa.ncdc.wct.event.GeneralProgressEvent;
import gov.noaa.ncdc.wct.event.GeneralProgressListener;
import gov.noaa.ncdc.wct.export.WCTExport;
import gov.noaa.ncdc.wct.export.WCTExport.ExportFormat;
import gov.noaa.ncdc.wct.export.WCTExportException;
import gov.noaa.ncdc.wct.export.WCTExportNoDataException;
import gov.noaa.ncdc.wct.export.raster.FeatureRasterizerException;
import gov.noaa.ncdc.wct.io.FileScanner;
import gov.noaa.ncdc.wct.io.SupportedDataType;
import gov.noaa.ncdc.wct.morph.MorphOperator.MorphStepHandler;
import gov.noaa.ncdc.wct.ui.MorphSettings;
import gov.noaa.ncdc.wct.ui.WCTMapPane;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.KernelJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.TileCache;

import org.geotools.cs.GeodeticCalculator;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.cv.SampleDimension;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.gc.GridCoverage;
import org.geotools.pt.Envelope;
import org.geotools.renderer.j2d.RenderedGridCoverage;
import org.geotools.renderer.j2d.StyledMapRenderer;

import ucar.ma2.InvalidRangeException;

import com.madgag.gif.fmsware.AnimatedGifEncoder;


public class WCTMorphOperation {

	public static final int DEFAULT_DATE_BUFFER_PERCENT = 260; // % of time difference
	public static final int DEFAULT_NUMBER_OF_MORPH_VECTOR_GRID_CELLS = 10;

	public static final int SQUARE_GRID_SIZE = 800;

	//	private ArrayList<GridCoverage> morphedGridCoverages = new ArrayList<GridCoverage>();

	//	public static void main(String[] args) {
	//		testMorphWithSWDI(args);
	//		testMorphWithTracking(args);
	//	}

	private WCTExport exporter1 = null;			
	private WCTExport exporter2 = null;			

	private ArrayList<MorphGeoFeaturePair> lastProcessedGeoFeaturePairList = new ArrayList<MorphGeoFeaturePair>();
	private StringBuilder operationLog = new StringBuilder();


	private WCTFilter filter = new WCTFilter();
	private String variableName = null;
	private int dateBufferPercentForSWDIQuery = DEFAULT_DATE_BUFFER_PERCENT;
	private int numberOfMorphVectorGridCells = DEFAULT_NUMBER_OF_MORPH_VECTOR_GRID_CELLS;
	private int smoothFactor = 0;
	private SampleDimension sampleDimension;

	public enum MorphVectorSource { NONE, NCDC_SWDI, NCDC_SWDI_TVS, NCDC_RUC, WCT_MARKERS };

	public static void testMorphWithTracking(String[] args) {

		try {

			int numberOfMorphSteps = 16;

			Rectangle2D.Double extent = new Rectangle2D.Double(-95.35, 36.40, 1.5, 1.5);
			//			Rectangle2D.Double extent = new Rectangle2D.Double(-84, 32.9, 3.5, 3.5);


			//			URL firstUrl = new URL("ftp://tgftp.nws.noaa.gov/SL.us008001/DF.of/DC.radar/DS.p94r0/SI.kgsp/sn.0102");
			//			URL firstUrl = new File("E:\\work\\morph\\testdata\\l3-p94r1\\KGSP_N1Q_20110615_2234").toURI().toURL();
			URL firstUrl = new File("E:\\work\\morph\\testdata\\joplin\\KSGF20110522\\KSGF_SDUS53_N0QSGF_201105222243").toURI().toURL();

			URL trackingUrl = new File("E:\\work\\morph\\testdata\\joplin\\KSGF20110522\\KSGF_SDUS33_NSTSGF_201105222243").toURI().toURL();


			MorphGridCoverageHandler gcHandler = new MorphGridCoverageHandler() {
				@Override
				public void processGridCoverage(GridCoverage gc, Date date, int step, long durationInMillis) {
				}
			};


			WCTMorphOperation morph = new WCTMorphOperation();
			WCTFilter filter = new WCTFilter();
			filter.setExtentFilter(extent);
			morph.setFilter(filter);
			morph.processWithStormTracking(numberOfMorphSteps, firstUrl, trackingUrl, gcHandler);


		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	public static void testMorphWithSWDI(String[] args) {

		try {

			int numberOfMorphSteps = 16;

			//			Rectangle2D.Double extent = new Rectangle2D.Double(-95.35, 36.40, 1.5, 1.5);
			Rectangle2D.Double extent = new Rectangle2D.Double(-84, 32.9, 3.5, 3.5);


			//			URL firstUrl = new URL("ftp://tgftp.nws.noaa.gov/SL.us008001/DF.of/DC.radar/DS.p94r0/SI.kgsp/sn.0102");
			URL firstUrl = new File("E:\\work\\morph\\testdata\\l3-p94r1\\KGSP_N1Q_20110615_2234").toURI().toURL();
			//			URL firstUrl = new File("E:\\work\\morph\\testdata\\joplin\\KSGF20110522\\KSGF_SDUS53_N0QSGF_201105222243").toURI().toURL();


			//			lastUrl = new URL("ftp://tgftp.nws.noaa.gov/SL.us008001/DF.of/DC.radar/DS.p94r0/SI.kgsp/sn.0110");
			//			lastUrl = new File("E:\\work\\morph\\testdata\\l3-p94r0\\sn.0110.161110").toURI().toURL();
			URL lastUrl = new File("E:\\work\\morph\\testdata\\l3-p94r1\\KGSP_N1Q_20110615_2303").toURI().toURL();
			//			lastUrl = new File("E:\\work\\morph\\testdata\\joplin\\KSGF20110522\\KSGF_SDUS53_N0QSGF_201105222248").toURI().toURL();

			MorphGridCoverageHandler gcHandler = new MorphGridCoverageHandler() {
				@Override
				public void processGridCoverage(GridCoverage gc, Date date, int step, long durationInMillis) {
				}
			};


			WCTMorphOperation morph = new WCTMorphOperation();
			WCTFilter filter = new WCTFilter();
			GeneralProgressListener progressListener = new GeneralProgressListener() {
				@Override
				public void started(GeneralProgressEvent event) {
				}
				@Override
				public void ended(GeneralProgressEvent event) {
				}
				@Override
				public void progress(GeneralProgressEvent event) {
				}				
			};
			filter.setExtentFilter(extent);
			morph.setFilter(filter);
			morph.processMorph(numberOfMorphSteps, progressListener, 
					firstUrl, lastUrl, MorphVectorSource.NCDC_SWDI, new MorphSettings(), gcHandler);
			//					WCTMorphVectors.SWDI_DATE_FORMAT.parse(startDate), WCTMorphVectors.SWDI_DATE_FORMAT.parse(endDate));


		} catch (Exception e) {
			e.printStackTrace();
		}

	}



	/**
	 * Morph data URL into the future using the provided Storm Tracking product.
	 * The timestamps of both products should be identical.  The data image will
	 * be morphed in time using the tracking vectors, but will not morph into 
	 * another future data image.
	 * @param extent
	 * @param numberOfMorphSteps
	 * @param firstUrl
	 * @param stormTrackingL3ProductUrl
	 */
	public void processWithStormTracking(int numberOfMorphSteps,
			URL firstUrl, URL stormTrackingL3ProductUrl, 
			final MorphGridCoverageHandler gcHandler) {

		processWithStormTracking(numberOfMorphSteps, firstUrl, null, 
				stormTrackingL3ProductUrl, gcHandler);
	}


	/**
	 * Morph first data image into second data image using supplied storm tracking
	 * product to supply the motion vectors.
	 * @param extent
	 * @param numberOfMorphSteps
	 * @param firstUrl
	 * @param lastURL
	 * @param stormTrackingL3ProductUrl
	 */
	public void processWithStormTracking(final int numberOfMorphSteps,
			URL firstUrl, URL lastURL, URL stormTrackingL3ProductUrl, 
			final MorphGridCoverageHandler gcHandler) {


		try {

			final long durationInMillis = 1000L*60*25;

			// config JAI
			TileCache cache = JAI.getDefaultInstance().getTileCache();
			cache.setMemoryCapacity(200000000L);
			cache.setMemoryThreshold(.75f);
			JAI.getDefaultInstance().setTileCache(cache);
			JAI.getDefaultInstance().getTileScheduler().setParallelism(0);



			Rectangle2D.Double extentFilter = filter.getExtentFilter();
			Rectangle2D.Double newFilter = new Rectangle2D.Double(
					extentFilter.getX()-extentFilter.getWidth()*0.25,
					extentFilter.getY()-extentFilter.getHeight()*0.25,
					extentFilter.getWidth()+extentFilter.getWidth()*2*0.25,
					extentFilter.getHeight()+extentFilter.getHeight()*2*0.25);
			filter.setExtentFilter(newFilter);



			final WCTExport exporter1 = new WCTExport();
			if (variableName != null) exporter1.setExportVariable(variableName);
			exporter1.setExportGridSize(1200);
			exporter1.setExportL3Filter(filter);			
			exporter1.setExportRadialFilter(filter);			
			exporter1.setExportGridSatelliteFilter(filter);			
			//			exporter1.setExportGridSmoothFactor(5);
			exporter1.setOutputFormat(ExportFormat.WCT_RASTER_OBJECT_ONLY);
			exporter1.setExportGridNoData(-25.0f);
			try {
				exporter1.exportData(firstUrl, new File("startImage.wctobj"));
			} catch (Exception e) {
				exporter1.exportData(firstUrl, new File("startImage.wctobj"), SupportedDataType.NEXRAD_LEVEL3, false);
			}

			WritableRaster startImage = exporter1.getLastProcessedRaster().getWritableRaster();
			Date firstDate = new Date(exporter1.getLastProcessedRaster().getDateInMilliseconds());
			changeValue(startImage, Double.NaN, -25.0);


			Date lastDate = null;
			WritableRaster lastImage = null;
			if (lastURL != null) {

				final WCTExport exporter2 = new WCTExport();

				if (variableName != null) exporter2.setExportVariable(variableName);
				exporter2.setExportGridSize(1200);
				exporter2.setExportL3Filter(filter);			
				exporter2.setExportRadialFilter(filter);			
				exporter2.setExportGridSatelliteFilter(filter);			
				//				exporter2.setExportGridSmoothFactor(5);
				exporter2.setOutputFormat(ExportFormat.WCT_RASTER_OBJECT_ONLY);
				exporter2.setExportGridNoData(-25.0f);
				try {
					exporter2.exportData(lastURL, new File("endImage.wctobj"));
				} catch (Exception e) {
					exporter2.exportData(lastURL, new File("endImage.wctobj"), SupportedDataType.NEXRAD_LEVEL3, false);
				}

				lastImage = exporter2.getLastProcessedRaster().getWritableRaster();
				lastDate = new Date(exporter2.getLastProcessedRaster().getDateInMilliseconds());
				changeValue(lastImage, Double.NaN, -25.0);

			}




			ArrayList<MorphGeoFeaturePair> mgfPairList = null;
			if (lastDate != null) {
				mgfPairList = WCTMorphVectors.queryL3StormTracking(stormTrackingL3ProductUrl, filter.getExtentFilter(), 
						lastDate.getTime()-firstDate.getTime());
			}
			else {
				mgfPairList = WCTMorphVectors.queryL3StormTracking(stormTrackingL3ProductUrl, filter.getExtentFilter(), durationInMillis);
			}

			final ArrayList<Point[]> pairList = WCTMorphVectors.getMorphImageCoordinatePairList(
					mgfPairList, filter.getExtentFilter(), 
					new Dimension(startImage.getWidth(), startImage.getHeight()));




			final ArrayList<Point[]> pairListTvs = new ArrayList<Point[]>();

			System.out.println(Arrays.toString(getMinMax(startImage)));

			//			System.exit(1);


			final Rectangle2D.Double bounds = exporter1.getLastProcessedRaster().getBounds();
			final Dimension size = WCTUtils.getEqualDimensions(bounds, startImage.getWidth(), startImage.getHeight());

			Color[] c = null;
			double[] v = null;
			SampleDimensionAndLabels sd = null;
			if (exporter1.getLastProcessedDataType() == SupportedDataType.NEXRAD_LEVEL3 || 
					exporter1.getLastProcessedDataType() == SupportedDataType.NEXRAD_LEVEL3_NWS) {
				c = NexradColorFactory.getColors(exporter1.getLevel3Header().getProductCode());
				v = NexradValueFactory.getProductMaxMinValues(exporter1.getLevel3Header());
				sd = NexradSampleDimensionFactory.getSampleDimensionAndLabels(exporter1.getLevel3Header().getProductCode(), false);

			}
			else if (exporter1.getLastProcessedDataType() == SupportedDataType.RADIAL) {
				c = NexradColorFactory.getColors(
						exporter1.getLastProcessedRadialRemappedRaster().getVariableName(), false);
				v = NexradValueFactory.getProductMaxMinValues(
						exporter1.getLastProcessedRadialRemappedRaster().getVariableName(), 12, false);
				sd = NexradSampleDimensionFactory.getSampleDimensionAndLabels(
						exporter1.getLastProcessedRadialRemappedRaster().getVariableName(), false);
			}


			final Color[] colors = c;
			final double[] vals = v;
			final SampleDimensionAndLabels sdAndLabels = sd;

			final AnimatedGifEncoder gif = new AnimatedGifEncoder();
			gif.setDelay(10);
			gif.setRepeat(100000);
			gif.setFrameRate(25);
			gif.setQuality(1);



			MorphStepHandler handler = new MorphStepHandler() {

				@Override
				public void processRaster(WritableRaster raster, int step) {

					changeValue(raster, -25.0, -999.0);
					filterLessThanValue(raster, 0.0, -999.0);

					System.out.println(Arrays.toString(getMinMax(raster)));


					GridCoverage gc = null;
					if (sdAndLabels != null) {
						gc = new GridCoverage("morph"+step, raster, GeographicCoordinateSystem.WGS84, 
								null, new Envelope(bounds), new SampleDimension[] { sdAndLabels.getSampleDimension() });
					}
					else {
						gc = new GridCoverage(
								"morph"+step, raster, GeographicCoordinateSystem.WGS84, 
								new Envelope(bounds), 
								new double[] { vals[0] },
								new double[] { vals[1] }, 
								null, new Color[][] { colors }, null);
					}

					//					exporter1.getLastProcessedRaster().setWritableRaster(raster);
					//					Date firstDate = new Date(exporter1.getLastProcessedRaster().getDateInMilliseconds());
					//					Date lastDate = new Date(exporter2.getLastProcessedRaster().getDateInMilliseconds());
					//					final long dateDiffPerStepInMillis = (lastDate.getTime() - firstDate.getTime())/numberOfMorphSteps;

					Date date = new Date(Math.round(exporter1.getLastProcessedRaster().getDateInMilliseconds()+
							durationInMillis*((double)step/numberOfMorphSteps)));

					gcHandler.processGridCoverage(gc, date, step, Math.round(durationInMillis/(double)numberOfMorphSteps));


					//					try {
					//						
					//			            KernelJAI kernel = new KernelJAI(1, 1, new float[]{ 1f });        
					//			            ParameterBlock pb = new ParameterBlock();
					//			            pb.addSource(gc.getRenderedImage());
					//			            pb.add(kernel);      
					//			            PlanarImage output = JAI.create("convolve", pb, null);
					//
					//			            Thread.sleep(10);
					//						
					////						BufferedImage image = output.getAsBufferedImage();						
					//			            WritableRaster newRaster = (WritableRaster)(output.getData());
					//						GridCoverage gc2= new GridCoverage(
					//								"morph"+step, newRaster, GeographicCoordinateSystem.WGS84, 
					//								new Envelope(bounds), 
					//								new double[] { vals[0] },
					//								new double[] { vals[1] }, 
					//								null, new Color[][] { colors }, null);
					//
					//			            Thread.sleep(10);
					//
					////						morphedGridCoverages.add(gc2);
					//			            gcHandler.processGridCoverage(gc2, step);
					//
					////						ImageIO.write(image, "png", new File("E:\\work\\morph\\output\\morph"+(step+1)+".png"));
					//						
					////						gif.addFrame(image);
					//						
					//					} catch (Exception e) {
					//						e.printStackTrace();
					//					}


				}				
			};

			MorphStepHandler handler2 = new MorphStepHandler() {
				@Override
				public void processRaster(WritableRaster raster, int step) {

					changeValue(raster, -25.0, -999.0);
					filterLessThanValue(raster, 0.0, -999.0);

					System.out.println(Arrays.toString(getMinMax(raster)));


					GridCoverage gc = new GridCoverage(
							"morph"+step, raster, GeographicCoordinateSystem.WGS84, 
							new Envelope(bounds), 
							new double[] { vals[0] },
							new double[] { vals[1] }, 
							null, new Color[][] { colors }, null);

					exporter1.getLastProcessedRaster().setWritableRaster(raster);


					try {

						KernelJAI kernel = new KernelJAI(1, 1, new float[]{ 1f });        
						ParameterBlock pb = new ParameterBlock();
						pb.addSource(gc.getRenderedImage());
						pb.add(kernel);      
						PlanarImage output = JAI.create("convolve", pb, null);

						Thread.sleep(50);

						//						BufferedImage image = output.getAsBufferedImage();						
						WritableRaster newRaster = (WritableRaster)(output.getData());
						GridCoverage gc2= new GridCoverage(
								"morph"+step, newRaster, GeographicCoordinateSystem.WGS84, 
								new Envelope(bounds), 
								new double[] { vals[0] },
								new double[] { vals[1] }, 
								null, new Color[][] { colors }, null);

						Thread.sleep(50);


						BufferedImage image = createImage(gc2, size, pairList, pairListTvs);



						//						Thread.sleep(10);
						ImageIO.write(image, "png", new File("E:\\work\\morph\\output\\morph"+(step+1)+".png"));

						gif.addFrame(image);

					} catch (Exception e) {
						e.printStackTrace();
					}
				}				
			};



			//			{	
			//				GridCoverage gc = new GridCoverage(
			//					"morph-start", startImage, GeographicCoordinateSystem.WGS84, 
			//					new Envelope(bounds), 
			//					new double[] { vals[0] },
			//					new double[] { vals[1] }, 
			//					null, new Color[][] { colors }, null);
			//				try {
			//				
			//					BufferedImage image = createImage(gc, size);
			//					Thread.sleep(10);
			//					ImageIO.write(image, "png", new File("morph/morph0-start.png"));
			//				} catch (Exception e) {
			//					e.printStackTrace();
			//				}
			//			}			
			//			
			//			{	
			//				GridCoverage gc = new GridCoverage(
			//					"morph-end", endImage, GeographicCoordinateSystem.WGS84, 
			//					new Envelope(bounds), 
			//					new double[] { vals[0] },
			//					new double[] { vals[1] }, 
			//					null, new Color[][] { colors }, null);
			//				try {
			//				
			//					BufferedImage image = createImage(gc, size);
			//					Thread.sleep(10);
			//					ImageIO.write(image, "png", new File("morph/morph"+(numberOfMorphSteps+2)+"-end.png"));
			//				} catch (Exception e) {
			//					e.printStackTrace();
			//				}
			//			}			





			Vector<Point> lMeshList = new Vector<Point>();
			Vector<Point> lPolyList = new Vector<Point>();

			Vector<Point> rMeshList = new Vector<Point>();
			Vector<Point> rPolyList = new Vector<Point>();

			for (Point[] pointPair : pairList) {
				//				if (! (pointPair[0].x == pointPair[1].x && pointPair[0].y == pointPair[1].y)) {
				lMeshList.add(pointPair[0]);
				rMeshList.add(pointPair[1]);
				//				}
			}

			for (Point[] pointPair : pairListTvs) {
				//				if (! (pointPair[0].x == pointPair[1].x && pointPair[0].y == pointPair[1].y)) {
				lMeshList.add(pointPair[0]);
				rMeshList.add(pointPair[1]);
				//				}
			}


			Point[] ulPair = new Point[] { 
					new Point(0, 0), 
					new Point(0, 0) };
			Point[] urPair = new Point[] {
					new Point(startImage.getWidth(), 0), 
					new Point(startImage.getWidth(), 0) };
			Point[] lrPair = new Point[] {
					new Point(startImage.getWidth(), startImage.getHeight()), 
					new Point(startImage.getWidth(), startImage.getHeight()) };
			Point[] llPair = new Point[] {
					new Point(0, startImage.getHeight()), 
					new Point(0, startImage.getHeight()) };

			// if we have 3 or fewer points, then average all points
			if (pairList.size() > 0) {
				//			if (pairList.size() < 4 && pairList.size() > 0) {
				int xDiffTotal = 0;
				int yDiffTotal = 0;
				for (Point[] pointPair : pairList) {
					xDiffTotal += pointPair[1].x-pointPair[0].x;
					yDiffTotal += pointPair[1].y-pointPair[0].y;
				}

				int avgX = Math.round(xDiffTotal/(float)pairList.size());
				int avgY = Math.round(yDiffTotal/(float)pairList.size());

				System.out.println("AVG X/Y :::::::::: "+avgX+" / "+avgY);

				ulPair[1].setLocation(ulPair[1].getX()+avgX, ulPair[1].getY()+avgY);
				urPair[1].setLocation(urPair[1].getX()+avgX, urPair[1].getY()+avgY);
				lrPair[1].setLocation(lrPair[1].getX()+avgX, lrPair[1].getY()+avgY);
				llPair[1].setLocation(llPair[1].getX()+avgX, llPair[1].getY()+avgY);
			}




			lMeshList.add(ulPair[0]);
			lMeshList.add(urPair[0]);
			lMeshList.add(lrPair[0]);
			lMeshList.add(llPair[0]);

			rMeshList.add(ulPair[1]);
			rMeshList.add(urPair[1]);
			rMeshList.add(lrPair[1]);
			rMeshList.add(llPair[1]);





			lPolyList.add(new Point(0, 0));
			lPolyList.add(new Point(0, startImage.getHeight()));
			lPolyList.add(new Point(startImage.getWidth(), startImage.getHeight()));
			lPolyList.add(new Point(startImage.getWidth(), 0));

			rPolyList.add(new Point(0, 0));
			rPolyList.add(new Point(0, startImage.getHeight()));
			rPolyList.add(new Point(startImage.getWidth(), startImage.getHeight()));
			rPolyList.add(new Point(startImage.getWidth(), 0));








			MorphInput startInput = new MorphInput(lMeshList, lPolyList, startImage);
			MorphInput endInput = new MorphInput(rMeshList, rPolyList, (lastImage == null) ? null : lastImage);

			Config config = new Config(startInput, endInput, numberOfMorphSteps);

			Triangulation triangulation = new Triangulation(config);
			triangulation.triangulate();

			MorphOperator op = new MorphOperator(config);


			gif.start("E:\\work\\morph\\output\\morph-tracking-anim.gif");

			//			morphedGridCoverages.clear();
			op.run(handler);

			gif.finish();




		} catch (Exception e) {
			e.printStackTrace();
		}
	}




























































	public void processMorph(final Date[] dateTimesToProcess, final GeneralProgressListener progressListener, 
			URL firstUrl, URL lastUrl, MorphVectorSource mvSource, final MorphSettings morphSettings,
			final MorphGridCoverageHandler gcHandler) 
					throws WCTExportNoDataException, WCTExportException, DecodeException, FeatureRasterizerException, 
					IllegalAttributeException, InvalidRangeException, DecodeHintNotSupportedException, 
					URISyntaxException, ParseException, Exception {

		processMorph(-1, dateTimesToProcess, progressListener, firstUrl, lastUrl, mvSource, morphSettings, gcHandler);

	}


//	public void processMorph(final double[] morphRatioArray, final GeneralProgressListener progressListener, 
//			URL firstUrl, URL lastUrl, MorphVectorSource mvSource, final MorphGridCoverageHandler gcHandler) 
//					throws WCTExportNoDataException, WCTExportException, DecodeException, FeatureRasterizerException, 
//					IllegalAttributeException, InvalidRangeException, DecodeHintNotSupportedException, 
//					URISyntaxException, ParseException, Exception {
//
//
//
//	}

	
	public void processMorph(final int numberOfMorphSteps, final GeneralProgressListener progressListener, 
			URL firstUrl, URL lastUrl, MorphVectorSource mvSource, final MorphSettings morphSettings,
			final MorphGridCoverageHandler gcHandler) 
					throws WCTExportNoDataException, WCTExportException, DecodeException, FeatureRasterizerException, 
					IllegalAttributeException, InvalidRangeException, DecodeHintNotSupportedException, 
					URISyntaxException, ParseException, Exception {

		processMorph(numberOfMorphSteps, null, progressListener, firstUrl, lastUrl, mvSource, morphSettings, gcHandler);

	}
	
	
	
	
	
	private void processMorph(final int numberOfMorphSteps, final Date[] dateTimesToProcess,   
			final GeneralProgressListener progressListener, 
			URL firstUrl, URL lastUrl, MorphVectorSource mvSource, final MorphSettings morphSettings,
			final MorphGridCoverageHandler gcHandler) 
					throws WCTExportNoDataException, WCTExportException, DecodeException, FeatureRasterizerException, 
					IllegalAttributeException, InvalidRangeException, DecodeHintNotSupportedException, 
					URISyntaxException, ParseException, Exception {


		//		try {

		//		final GeneralProgressEvent progressEvent = new GeneralProgressEvent(this);

		GeneralProgressListener pl = new GeneralProgressListener() {

			@Override
			public void started(GeneralProgressEvent event) {
				// TODO Auto-generated method stub

			}

			@Override
			public void ended(GeneralProgressEvent event) {
				// TODO Auto-generated method stub

			}

			@Override
			public void progress(GeneralProgressEvent event) {
				System.out.println(event.getStatus()+" : "+event.getProgress());

			}

		};

		final GeneralProgressEvent gpe = new GeneralProgressEvent(this);
		DataExportListener dl = new DataExportListener() {

			@Override
			public void exportStarted(DataExportEvent event) {
				// TODO Auto-generated method stub

			}

			@Override
			public void exportEnded(DataExportEvent event) {
				// TODO Auto-generated method stub

			}

			@Override
			public void exportProgress(DataExportEvent event) {
				//				System.out.println(event.getProgress());
				gpe.setProgress(event.getProgress());
				gpe.setStatus(event.getStatus());
				progressListener.progress(gpe);
			}

			@Override
			public void exportStatus(DataExportEvent event) {
				//				System.out.println("status: "+event.getStatus());
				gpe.setProgress(event.getProgress());
				gpe.setStatus(event.getStatus());
				progressListener.progress(gpe);
			}

		};




		// config JAI
		TileCache cache = JAI.getDefaultInstance().getTileCache();
		cache.setMemoryCapacity(200000000L);
		cache.setMemoryThreshold(.75f);
		JAI.getDefaultInstance().setTileCache(cache);
		JAI.getDefaultInstance().getTileScheduler().setParallelism(0);



		gpe.setStatus("Decoding files to determing start/end dates...");
		pl.progress(gpe);



		// do a quick decode so we can get the date/time extracted from the file

		WCTFilter bogusFilter = new WCTFilter();
		bogusFilter.setExtentFilter(new Rectangle2D.Double(-90, 40, 0.0001, 0.0001));




		FileScanner fs = new FileScanner();
		fs.scanURL(firstUrl);
		String basename = fs.getLastScanResult().getFileName();
		basename = basename.substring(0, basename.length()-fs.getLastScanResult().getExtension().length());
		if (exporter2 != null &&
				exporter2.getFileScanner() != null &&
				exporter2.getFileScanner().getLastScanResult() != null &&
				exporter2.getFileScanner().getLastScanResult().getFileName().equals(basename)) {

			exporter1 = exporter2;
			exporter2 = new WCTExport();
		}
		else {
			exporter1 = new WCTExport();
			exporter1.addGeneralProgressListener(pl);
			exporter1.addDataExportListener(dl);
			exporter1.setExportGridSize(8);
			exporter1.setExportL3Filter(bogusFilter);
			exporter1.setOutputFormat(ExportFormat.WCT_RASTER_OBJECT_ONLY);
			try {
				exporter1.exportData(firstUrl, new File("startImage.wctobj"));
			} catch (Exception e) {
				exporter1.exportData(firstUrl, new File("startImage.wctobj"), SupportedDataType.NEXRAD_LEVEL3, false);
			}
		}


		exporter2 = new WCTExport();
		exporter2.addGeneralProgressListener(pl);
		exporter2.addDataExportListener(dl);
		exporter2.setExportGridSize(8);
		exporter2.setExportL3Filter(bogusFilter);
		exporter2.setOutputFormat(ExportFormat.WCT_RASTER_OBJECT_ONLY);
		try {
			exporter2.exportData(lastUrl, new File("endImage.wctobj"));
		} catch (Exception e) {
			exporter2.exportData(lastUrl, new File("endImage.wctobj"), SupportedDataType.NEXRAD_LEVEL3, false);
		}

		Date firstDate = new Date(exporter1.getLastProcessedRaster().getDateInMilliseconds());
		Date lastDate = new Date(exporter2.getLastProcessedRaster().getDateInMilliseconds());
		long dateBufferInMillis = Math.round((lastDate.getTime()-firstDate.getTime())*(0.01*dateBufferPercentForSWDIQuery)); 
		WritableRaster startImage = exporter1.getLastProcessedRaster().getWritableRaster();

		
		
		
		



		gpe.setStatus("Querying for motion vectors...");
		pl.progress(gpe);


		// get motion vectors used for morphing
		operationLog.setLength(0);
		operationLog.append("[General Morph Settings]:\n");
		operationLog.append("  Morph Vector Source: "+mvSource+"\n");
		operationLog.append("  Number of Morph Vector Grid Cells: "+getNumberOfMorphVectorGridCells()+"\n");
		operationLog.append("  Date Buffer Percent for SWDI Query: "+getDateBufferPercentForSWDIQuery()+"\n\n");

		ArrayList<MorphVector> mvList = null;
		if (mvSource == MorphVectorSource.NCDC_SWDI) {
			mvList = WCTMorphVectors.querySWDI(
					"nx3structure", 
					new Date(firstDate.getTime()-dateBufferInMillis), 
					new Date(lastDate.getTime()+dateBufferInMillis), 
					filter.getExtentFilter(), numberOfMorphVectorGridCells, operationLog);
		}
		else if (mvSource == MorphVectorSource.NCDC_SWDI_TVS) {
			mvList = WCTMorphVectors.querySWDI(
					"nx3tvs", 
					new Date(firstDate.getTime()-dateBufferInMillis), 
					new Date(lastDate.getTime()+dateBufferInMillis), 
					filter.getExtentFilter(), numberOfMorphVectorGridCells, operationLog);
		}
		else if (mvSource == MorphVectorSource.NCDC_RUC) {
			mvList = WCTMorphVectors.queryRUCStormMotion(
					new Date(firstDate.getTime()-dateBufferInMillis), 
					new Date(lastDate.getTime()+dateBufferInMillis), 
					filter.getExtentFilter(), operationLog);
		}
		else if (mvSource == MorphVectorSource.WCT_MARKERS) {
			mvList = WCTMorphVectors.queryMarkers(
					new Date(firstDate.getTime()), new Date(lastDate.getTime()), 
					filter.getExtentFilter(), operationLog);
		}
		else {
			operationLog.append("No motion vectors used. \n");
			mvList = new ArrayList<MorphVector>();
		}


		if (mvSource != MorphVectorSource.NONE && mvList.size() == 0) {
			throw new Exception("No data available to create morph vectors.\n" +
					"Try zooming out or using a different morph vector source.");
		}





		long dateDiffInSeconds = (lastDate.getTime()-firstDate.getTime())/1000;
		double[] latLonDiffs = calculateAdjustedExtent(mvList, dateDiffInSeconds);
		Rectangle2D.Double extentFilter = filter.getExtentFilter();
		double xCellSize = extentFilter.getWidth()/SQUARE_GRID_SIZE;
		double yCellSize = extentFilter.getHeight()/SQUARE_GRID_SIZE;

		Rectangle2D.Double newFilter = new Rectangle2D.Double(
				extentFilter.getX()-latLonDiffs[1],
				extentFilter.getY()-latLonDiffs[0],
				extentFilter.getWidth()+latLonDiffs[1]*2,
				extentFilter.getHeight()+latLonDiffs[0]*2);

		int newWidth = (int)Math.round(newFilter.getWidth()/xCellSize);
		int newHeight = (int)Math.round(newFilter.getHeight()/yCellSize);
		int squareGridSize = Math.max(newWidth, newHeight);
		// cap at 1400
		squareGridSize = Math.min(squareGridSize, 1400);
		double ratioWidthToHeight = (double)newWidth/newHeight;


		System.out.println("before smoothing adjustment: "+newFilter);
		//			SmoothingOperation smop = new SmoothingOperation();
		//			newFilter = smop.adjustSmoothingExtent(newFilter, squareGridSize, squareGridSize, smoothFactor*8);
		if (smoothFactor > 0) {
			newFilter = new Rectangle2D.Double(
					newFilter.getX()-newFilter.getWidth()/2.0,
					newFilter.getY()-newFilter.getHeight()/2.0,
					newFilter.getWidth()*2,
					newFilter.getHeight()*2);
		}
		System.out.println(" after smoothing adjustment: "+newFilter);

		filter.setExtentFilter(newFilter);



		// if URLs are reversed, then flip
		if (lastDate.before(firstDate)) {
			URL tmp = firstUrl;
			firstUrl = lastUrl;
			lastUrl = tmp;
		}


		if (variableName != null) exporter1.setExportVariable(variableName);
		exporter1.setExportCut(Integer.parseInt(morphSettings.getExtraInfoMap().get(MorphSettings.INFO_RADIAL_SWEEP)));
		exporter1.setExportGridSize(squareGridSize);
		exporter1.setExportL3Filter(filter);			
		exporter1.setExportRadialFilter(filter);			
		exporter1.setExportGridSatelliteFilter(filter);			
		//			exporter1.setExportGridSmoothFactor(smoothFactor);
		exporter1.setOutputFormat(ExportFormat.WCT_RASTER_OBJECT_ONLY);
		exporter1.setExportGridNoData(-25.0f);
		try {
			exporter1.exportData(firstUrl, new File("startImage.wctobj"));
		} catch (Exception e) {
			exporter1.exportData(firstUrl, new File("startImage.wctobj"), SupportedDataType.NEXRAD_LEVEL3, false);
		}


		//			WritableRaster startImage = exporter1.getLastProcessedRaster().getWritableRaster();
		//			Date firstDate = new Date(exporter1.getLastProcessedRaster().getDateInMilliseconds());
		startImage = exporter1.getLastProcessedRaster().getWritableRaster();
		firstDate = new Date(exporter1.getLastProcessedRaster().getDateInMilliseconds());
		changeValue(startImage, Double.NaN, -25.0);



		//			exporter2 = new WCTExport();
		//			exporter2.addGeneralProgressListener(pl);
		//			exporter2.addDataExportListener(dl);
		if (variableName != null) exporter2.setExportVariable(variableName);
		exporter2.setExportCut(Integer.parseInt(morphSettings.getExtraInfoMap().get(MorphSettings.INFO_RADIAL_SWEEP)));
		exporter2.setExportGridSize(squareGridSize);
		exporter2.setExportL3Filter(filter);
		exporter2.setExportRadialFilter(filter);
		exporter2.setExportGridSatelliteFilter(filter);			
		//			exporter2.setExportGridSmoothFactor(smoothFactor);
		exporter2.setOutputFormat(ExportFormat.WCT_RASTER_OBJECT_ONLY);
		exporter2.setExportGridNoData(-25.0f);
		try {
			exporter2.exportData(lastUrl, new File("endImage.wctobj"));
		} catch (Exception e) {
			exporter2.exportData(lastUrl, new File("endImage.wctobj"), SupportedDataType.NEXRAD_LEVEL3, false);
		}
		WritableRaster endImage = exporter2.getLastProcessedRaster().getWritableRaster();
		//			Date lastDate = new Date(exporter2.getLastProcessedRaster().getDateInMilliseconds());
		lastDate = new Date(exporter2.getLastProcessedRaster().getDateInMilliseconds());
		changeValue(endImage, Double.NaN, -25.0);







		// expand extent filter to eliminate weird edge effects from morphing
		//			if (mvSource != MorphVectorSource.NONE) {
		//				Rectangle2D.Double extentFilter = filter.getExtentFilter();
		//				Rectangle2D.Double newFilter = new Rectangle2D.Double(
		//						extentFilter.getX()-extentFilter.getWidth()*0.25,
		//						extentFilter.getY()-extentFilter.getHeight()*0.25,
		//						extentFilter.getWidth()+extentFilter.getWidth()*2*0.25,
		//						extentFilter.getHeight()+extentFilter.getHeight()*2*0.25);
		//				filter.setExtentFilter(newFilter);
		//			}






		//			FileScanner fs = new FileScanner();
		//			fs.scanURL(firstUrl);
		//			String basename = fs.getLastScanResult().getFileName();
		//			basename = basename.substring(0, basename.length()-fs.getLastScanResult().getExtension().length());
		//			if (exporter2 != null &&
		//					exporter2.getFileScanner() != null &&
		//					exporter2.getFileScanner().getLastScanResult() != null &&
		//					exporter2.getFileScanner().getLastScanResult().getFileName().equals(basename)) {
		//				
		//				exporter1 = exporter2;
		//				exporter2 = new WCTExport();
		//			}
		//			else {
		//				exporter1 = new WCTExport();
		//				exporter1.addGeneralProgressListener(pl);
		//				exporter1.addDataExportListener(dl);
		//				if (variableName != null) exporter1.setExportVariable(variableName);
		//				exporter1.setExportGridSize(squareGridSize);
		//				exporter1.setExportL3Filter(filter);			
		//				exporter1.setExportRadialFilter(filter);			
		//				exporter1.setExportGridSatelliteFilter(filter);			
		//				//			exporter1.setExportGridSmoothFactor(smoothFactor);
		//				exporter1.setOutputFormat(ExportFormat.WCT_RASTER_OBJECT_ONLY);
		//				exporter1.setExportGridNoData(-5.0f);
		//				try {
		//					exporter1.exportData(firstUrl, new File("startImage.wctobj"));
		//				} catch (Exception e) {
		//					exporter1.exportData(firstUrl, new File("startImage.wctobj"), SupportedDataType.NEXRAD_LEVEL3, false);
		//				}
		//			}
		//			
		//			
		////			WritableRaster startImage = exporter1.getLastProcessedRaster().getWritableRaster();
		////			Date firstDate = new Date(exporter1.getLastProcessedRaster().getDateInMilliseconds());
		//			startImage = exporter1.getLastProcessedRaster().getWritableRaster();
		//			firstDate = new Date(exporter1.getLastProcessedRaster().getDateInMilliseconds());
		//			changeValue(startImage, Double.NaN, -5.0);
		//
		//			
		//			
		//			exporter2 = new WCTExport();
		//			exporter2.addGeneralProgressListener(pl);
		//			exporter2.addDataExportListener(dl);
		//			if (variableName != null) exporter2.setExportVariable(variableName);
		//			exporter2.setExportGridSize(squareGridSize);
		//			exporter2.setExportL3Filter(filter);
		//			exporter2.setExportRadialFilter(filter);
		//			exporter2.setExportGridSatelliteFilter(filter);			
		////			exporter2.setExportGridSmoothFactor(smoothFactor);
		//			exporter2.setOutputFormat(ExportFormat.WCT_RASTER_OBJECT_ONLY);
		//			exporter2.setExportGridNoData(-5.0f);
		//			try {
		//				exporter2.exportData(lastUrl, new File("endImage.wctobj"));
		//			} catch (Exception e) {
		//				exporter2.exportData(lastUrl, new File("endImage.wctobj"), SupportedDataType.NEXRAD_LEVEL3, false);
		//			}
		//			WritableRaster endImage = exporter2.getLastProcessedRaster().getWritableRaster();
		////			Date lastDate = new Date(exporter2.getLastProcessedRaster().getDateInMilliseconds());
		//			lastDate = new Date(exporter2.getLastProcessedRaster().getDateInMilliseconds());
		//			changeValue(endImage, Double.NaN, -5.0);


		//			long dateBufferInMillis = Math.round((lastDate.getTime()-firstDate.getTime())*(0.01*dateBufferPercentForSWDIQuery)); 
		//			
		//
		//			operationLog.setLength(0);
		//			operationLog.append("[General Morph Settings]:\n");
		//			operationLog.append("  Morph Vector Source: "+mvSource+"\n");
		//			operationLog.append("  Number of Morph Vector Grid Cells: "+getNumberOfMorphVectorGridCells()+"\n");
		//			operationLog.append("  Date Buffer Percent for SWDI Query: "+getDateBufferPercentForSWDIQuery()+"\n\n");
		//			
		//			ArrayList<MorphVector> mvList = null;
		//			if (mvSource == MorphVectorSource.NCDC_SWDI) {
		//				mvList = WCTMorphVectors.querySWDI(
		//					"nx3structure", 
		//					new Date(firstDate.getTime()-dateBufferInMillis), 
		//					new Date(lastDate.getTime()+dateBufferInMillis), 
		//					filter.getExtentFilter(), numberOfMorphVectorGridCells, operationLog);
		//			}
		//			else if (mvSource == MorphVectorSource.NCDC_RUC) {
		//				mvList = WCTMorphVectors.queryRUCStormMotion(
		//						new Date(firstDate.getTime()-dateBufferInMillis), 
		//						new Date(lastDate.getTime()+dateBufferInMillis), 
		//						filter.getExtentFilter(), operationLog);
		//			}
		//			else {
		//				operationLog.append("No motion vectors used. \n");
		//				mvList = new ArrayList<MorphVector>();
		//			}
		//

		ArrayList<MorphGeoFeaturePair> mgfPairList = new ArrayList<MorphGeoFeaturePair>(); 
		mgfPairList.addAll(WCTMorphVectors.processMorphGeoFeatureList(mvList, firstDate, lastDate)); 
		this.lastProcessedGeoFeaturePairList = mgfPairList;

		final ArrayList<Point[]> pairList = WCTMorphVectors.getMorphImageCoordinatePairList(
				mgfPairList,
				filter.getExtentFilter(), 
				new Dimension(startImage.getWidth(), startImage.getHeight()));



		System.out.println(Arrays.toString(getMinMax(startImage)));
		System.out.println(Arrays.toString(getMinMax(endImage)));










		final Rectangle2D.Double bounds = exporter1.getLastProcessedRaster().getBounds();
		final Dimension size = WCTUtils.getEqualDimensions(bounds, endImage.getWidth(), endImage.getHeight());

		Color[] c = null;
		double[] v = null;
		if (exporter1.getLastProcessedDataType() == SupportedDataType.NEXRAD_LEVEL3 || 
				exporter1.getLastProcessedDataType() == SupportedDataType.NEXRAD_LEVEL3_NWS) {
			c = NexradColorFactory.getColors(exporter1.getLevel3Header().getProductCode());
			v = NexradValueFactory.getProductMaxMinValues(exporter1.getLevel3Header());
			SampleDimensionAndLabels sdAndLabels = NexradSampleDimensionFactory.getSampleDimensionAndLabels(exporter1.getLevel3Header().getProductCode(), false);
			if (sdAndLabels != null) {
				sampleDimension = sdAndLabels.getSampleDimension();
			}

		}
		else if (exporter1.getLastProcessedDataType() == SupportedDataType.RADIAL) {
			c = NexradColorFactory.getColors(
					exporter1.getLastProcessedRadialRemappedRaster().getVariableName(), false);
			v = NexradValueFactory.getProductMaxMinValues(
					exporter1.getLastProcessedRadialRemappedRaster().getVariableName(), 12, false);
			SampleDimensionAndLabels sdAndLabels = NexradSampleDimensionFactory.getSampleDimensionAndLabels(
					exporter1.getLastProcessedRadialRemappedRaster().getVariableName(), false);

			if (sdAndLabels != null) {
				sampleDimension = sdAndLabels.getSampleDimension();
			}

		}
		else if (exporter1.getLastProcessedDataType() == SupportedDataType.GRIDDED ||
				exporter1.getLastProcessedDataType() == SupportedDataType.GOES_SATELLITE_AREA_FORMAT) {
			//				sampleDimension = sampleDimension;
		}
		else {
			throw new Exception("Time Morphing is not supported for this data type ("+exporter1.getLastProcessedDataType()+")");
		}

		final Color[] colors = c;
		final double[] vals = v;


		//			final AnimatedGifEncoder gif = new AnimatedGifEncoder();
		//			gif.setDelay(20);
		//			gif.setRepeat(100000);
		//			gif.setFrameRate(15);
		//			gif.setQuality(100);



		MorphStepHandler handler = new MorphStepHandler() {
			@Override
			public void processRaster(WritableRaster raster, int step) {

				//					changeValue(raster, -25.0, -999.0);
				//					filterLessThanValue(raster, -25.0, -999.0);

				System.out.println(Arrays.toString(getMinMax(raster)));


				GridCoverage gc = null;
				if (sampleDimension != null) {
					gc = new GridCoverage("morph"+step, raster, GeographicCoordinateSystem.WGS84, 
							null, new Envelope(bounds), new SampleDimension[] { sampleDimension });
				}
				else {
					gc = new GridCoverage(
							"morph"+step, raster, GeographicCoordinateSystem.WGS84, 
							new Envelope(bounds), 
							new double[] { vals[0] },
							new double[] { vals[1] }, 
							null, new Color[][] { colors }, null);
				}

				exporter1.getLastProcessedRaster().setWritableRaster(raster);
				Date firstDate = new Date(exporter1.getLastProcessedRaster().getDateInMilliseconds());
				Date lastDate = new Date(exporter2.getLastProcessedRaster().getDateInMilliseconds());
				final double dateDiffPerStepInMillis = (lastDate.getTime() - firstDate.getTime())/(double)numberOfMorphSteps;


				gcHandler.processGridCoverage(gc, new Date(firstDate.getTime()+Math.round(step*dateDiffPerStepInMillis)), step, Math.round(dateDiffPerStepInMillis));

			}				
		};





		Vector<Point> lMeshList = new Vector<Point>();
		Vector<Point> lPolyList = new Vector<Point>();

		Vector<Point> rMeshList = new Vector<Point>();
		Vector<Point> rPolyList = new Vector<Point>();

		for (Point[] pointPair : pairList) {
			//				if (! (pointPair[0].x == pointPair[1].x && pointPair[0].y == pointPair[1].y)) {
			lMeshList.add(pointPair[0]);
			rMeshList.add(pointPair[1]);
			//				}
		}

		//			for (Point[] pointPair : pairListTvs) {
		////				if (! (pointPair[0].x == pointPair[1].x && pointPair[0].y == pointPair[1].y)) {
		//					lMeshList.add(pointPair[0]);
		//					rMeshList.add(pointPair[1]);
		////				}
		//			}






		Point[] ulPair = new Point[] { 
				new Point(0, 0), 
				new Point(0, 0) };
		Point[] urPair = new Point[] {
				new Point(startImage.getWidth(), 0), 
				new Point(startImage.getWidth(), 0) };
		Point[] lrPair = new Point[] {
				new Point(startImage.getWidth(), startImage.getHeight()), 
				new Point(startImage.getWidth(), startImage.getHeight()) };
		Point[] llPair = new Point[] {
				new Point(0, startImage.getHeight()), 
				new Point(0, startImage.getHeight()) };

		// if we have 3 or fewer points, then average all points
		if (pairList.size() > 0) {
			//			if (pairList.size() < 4 && pairList.size() > 0) {
			int xDiffTotal = 0;
			int yDiffTotal = 0;
			for (Point[] pointPair : pairList) {
				xDiffTotal += pointPair[1].x-pointPair[0].x;
				yDiffTotal += pointPair[1].y-pointPair[0].y;
			}

			int avgX = Math.round(xDiffTotal/(float)pairList.size());
			int avgY = Math.round(yDiffTotal/(float)pairList.size());

			System.out.println("AVG X/Y :::::::::: "+avgX+" / "+avgY);

			ulPair[1].setLocation(ulPair[1].getX()+avgX, ulPair[1].getY()+avgY);
			urPair[1].setLocation(urPair[1].getX()+avgX, urPair[1].getY()+avgY);
			lrPair[1].setLocation(lrPair[1].getX()+avgX, lrPair[1].getY()+avgY);
			llPair[1].setLocation(llPair[1].getX()+avgX, llPair[1].getY()+avgY);
		}




		lMeshList.add(ulPair[0]);
		lMeshList.add(urPair[0]);
		lMeshList.add(lrPair[0]);
		lMeshList.add(llPair[0]);

		rMeshList.add(ulPair[1]);
		rMeshList.add(urPair[1]);
		rMeshList.add(lrPair[1]);
		rMeshList.add(llPair[1]);





		lPolyList.add(new Point(0, 0));
		lPolyList.add(new Point(0, startImage.getHeight()));
		lPolyList.add(new Point(startImage.getWidth(), startImage.getHeight()));
		lPolyList.add(new Point(startImage.getWidth(), 0));

		rPolyList.add(new Point(0, 0));
		rPolyList.add(new Point(0, endImage.getHeight()));
		rPolyList.add(new Point(endImage.getWidth(), endImage.getHeight()));
		rPolyList.add(new Point(endImage.getWidth(), 0));








		MorphInput startInput = new MorphInput(lMeshList, lPolyList, startImage);
		MorphInput endInput = new MorphInput(rMeshList, rPolyList, endImage);

		Config config = new Config(startInput, endInput, numberOfMorphSteps);

		Triangulation triangulation = new Triangulation(config);
		triangulation.triangulate();

		MorphOperator op = new MorphOperator(config);


		if (dateTimesToProcess != null) {
			long dateDiffMillis = lastDate.getTime() - firstDate.getTime();
			double[] ratioArray = new double[dateTimesToProcess.length];
			for (int n=0; n<dateTimesToProcess.length; n++) {
				ratioArray[n] = (dateTimesToProcess[n].getTime()-firstDate.getTime()) / dateDiffMillis;
			}
			op.run(handler, ratioArray);
		}
		else {
			op.run(handler);
		}	



	}




	/**
	 * 
	 * @param mvList
	 * @param durationInSeconds
	 * @return [0] = maxLatDiff , [1] = maxLonDiff
	 */
	private double[] calculateAdjustedExtent(ArrayList<MorphVector> mvList, long durationInSeconds) {
		double maxLatDiff = -99999;
		double maxLonDiff = -99999;

		if (mvList.size() == 0) {
			return new double[] { 0, 0 };
		}



		GeodeticCalculator gcalc = new GeodeticCalculator();

		for (MorphVector mv : mvList) {

			System.out.println("processing max shift - mv: "+mv);
			try {

				gcalc.setAnchorPoint(mv.getLon(), mv.getLat());
				double angle = mv.getDirectionAngle();
				System.out.println(mv.getDirectionAngle()+" : "+mv.getSpeed());
				gcalc.setDirection(angle, mv.getSpeed()*durationInSeconds);

				Point2D point = gcalc.getDestinationPoint();
				double lat = point.getY();
				double lon = point.getX();

				double latDiff = Math.abs(lat-mv.getLat());
				double lonDiff = Math.abs(lon-mv.getLon());

				maxLatDiff = (maxLatDiff < latDiff) ? latDiff : maxLatDiff;
				maxLonDiff = (maxLonDiff < lonDiff) ? lonDiff : maxLonDiff;

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (maxLatDiff < 0 || maxLonDiff < 0) {
			maxLatDiff = 0;
			maxLonDiff = 0;
		}

		System.out.println("max lat/lon diffs: "+maxLatDiff +" / "+maxLonDiff);
		return new double[] { maxLatDiff , maxLonDiff };
	}




	public static double[] getMinMax(WritableRaster raster) {
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		for (int i=0; i<raster.getWidth(); i++) {
			for (int j=0; j<raster.getHeight(); j++) {
				min = Math.min(min, raster.getSampleDouble(i, j, 0));
				max = Math.max(max, raster.getSampleDouble(i, j, 0));
			}
		}
		return new double[] { min, max };
	}

	public static void changeValue(WritableRaster raster, double oldValue, double newValue) {
		for (int i=0; i<raster.getWidth(); i++) {
			for (int j=0; j<raster.getHeight(); j++) {
				if (Double.isNaN(oldValue)) {
					if (Double.isNaN(raster.getSampleDouble(i, j, 0))) {
						raster.setSample(i, j, 0, newValue);
					}					
				}
				else if (Double.isNaN(newValue)) {
					if (Double.isNaN(raster.getSampleDouble(i, j, 0))) {
						raster.setSample(i, j, 0, Double.NaN);
					}					
				}
				else {
					if (raster.getSampleDouble(i, j, 0) == oldValue) {
						raster.setSample(i, j, 0, newValue);
					}
				}
			}
		}
	}

	public static void filterLessThanValue(WritableRaster raster, double value, double noDataValue) {
		for (int i=0; i<raster.getWidth(); i++) {
			for (int j=0; j<raster.getHeight(); j++) {
				if (raster.getSampleDouble(i, j, 0) < value) {
					raster.setSample(i, j, 0, noDataValue);
				}
			}
		}
	}


	public static BufferedImage createImage(GridCoverage gc, Dimension size) throws Exception {
		return createImage(gc, size, null, null);
	}

	public static BufferedImage createImage(GridCoverage gc, Dimension size, ArrayList<Point[]> pairList, ArrayList<Point[]> pairListTvs) throws Exception {


		BufferedImage bimage = new BufferedImage(
				size.width, size.height, 
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bimage.createGraphics();


		WCTMapPane wctMapPane = new WCTMapPane();
		wctMapPane.setBackground(new Color(0, 0, 0, 0));
		wctMapPane.setMagnifierGlass(wctMapPane.getBackground());
		wctMapPane.setDoubleBuffered(true);
		wctMapPane.setBounds(new Rectangle(size.width, size.height));

		g.setColor(new Color(0, 0, 0, 0));
		g.fillRect(0, 0, bimage.getWidth(), bimage.getHeight());      


		RenderedGridCoverage rgc = new RenderedGridCoverage(gc);
		rgc.setZOrder(0.1f);
		((StyledMapRenderer) wctMapPane.getRenderer()).addLayer(rgc);



		Thread.sleep(100);

		wctMapPane.paint(g);        


		if (pairList != null) {
			for (Point[] pointPair : pairList) {
				//        		System.out.println("drawing pair: "+Arrays.toString(pointPair));
				g.setColor(Color.CYAN);
				g.fillRect(pointPair[0].x-4, pointPair[0].y-4, 8, 8);
				g.setColor(Color.GRAY);
				g.fillRect(pointPair[1].x-3, pointPair[1].y-3, 6, 6);

				g.setColor(Color.BLACK);
				drawArrow(g, pointPair[0].x, pointPair[0].y, pointPair[1].x, pointPair[1].y);
			}
		}



		if (pairList != null) {
			for (Point[] pointPair : pairListTvs) {
				//        		System.out.println("drawing tvs pair: "+Arrays.toString(pointPair));
				g.setColor(Color.GREEN);
				g.fillRect(pointPair[0].x-4, pointPair[0].y-4, 8, 8);
				g.setColor(Color.GREEN);
				g.fillRect(pointPair[1].x-3, pointPair[1].y-3, 6, 6);

				g.setColor(Color.BLUE);
				drawArrow(g, pointPair[0].x, pointPair[0].y, pointPair[1].x, pointPair[1].y);
			}
		}



		//        AWTImageExport.saveImage(bimage, new File("batchout-kmz.png").getCanonicalFile(), AWTImageExport.Type.PNG);

		Thread.sleep(100);

		g.dispose();
		rgc.dispose();
		gc.dispose();

		return bimage;

	}


	public static void drawArrow(Graphics g1, int x1, int y1, int x2, int y2) {
		Graphics2D g = (Graphics2D) g1.create();

		int ARR_SIZE = 8;

		double dx = x2 - x1, dy = y2 - y1;
		double angle = Math.atan2(dy, dx);
		int len = (int) Math.sqrt(dx*dx + dy*dy);
		AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
		at.concatenate(AffineTransform.getRotateInstance(angle));
		g.setTransform(at);

		// Draw horizontal arrow starting in (0, 0)
		g.drawLine(0, 0, (int) len, 0);
		g.fillPolygon(new int[] {len, len-ARR_SIZE, len-ARR_SIZE, len},
				new int[] {0, -ARR_SIZE, ARR_SIZE, 0}, 4);
	}


	//	public void setMorphedGridCoverages(ArrayList<GridCoverage> morphedGridCoverages) {
	//		this.morphedGridCoverages = morphedGridCoverages;
	//	}


	//	public ArrayList<GridCoverage> getMorphedGridCoverages() {
	//		return morphedGridCoverages;
	//	}




	public ArrayList<MorphGeoFeaturePair> getLastProcessedGeoFeaturePairList() {
		return lastProcessedGeoFeaturePairList;
	}


	public void setFilter(WCTFilter filter) {
		this.filter = filter;
	}


	public WCTFilter getFilter() {
		return filter;
	}


	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}


	public String getVariableName() {
		return variableName;
	}


	public void setDateBufferPercentForSWDIQuery(
			int dateBufferPercentForSWDIQuery) {
		this.dateBufferPercentForSWDIQuery = dateBufferPercentForSWDIQuery;
	}


	public int getDateBufferPercentForSWDIQuery() {
		return dateBufferPercentForSWDIQuery;
	}


	public void setNumberOfMorphVectorGridCells(int numberOfMorphVectorGridCells) {
		this.numberOfMorphVectorGridCells = numberOfMorphVectorGridCells;
	}


	public int getNumberOfMorphVectorGridCells() {
		return numberOfMorphVectorGridCells;
	}


	//	public void setSmoothFactor(int smoothFactor) {
	//		this.smoothFactor = smoothFactor;
	//	}
	//
	//
	//	public int getSmoothFactor() {
	//		return smoothFactor;
	//	}


	public void setSampleDimension(SampleDimension sampleDimension) {
		this.sampleDimension = sampleDimension;
	}


	public SampleDimension getSampleDimension() {
		return sampleDimension;
	}


	public StringBuilder getOperationLog() {
		return operationLog;
	}


	public void setSmoothFactor(int smoothFactor) {
		this.smoothFactor = smoothFactor;
	}


	public int getSmoothFactor() {
		return smoothFactor;
	}


	public interface MorphGridCoverageHandler {
		public void processGridCoverage(GridCoverage gc, Date date, int step, long durationInMillis);
	}
}
