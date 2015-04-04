package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.nexradiv.legend.CategoryLegendImageProducer;
import gov.noaa.ncdc.wct.WCTException;
import gov.noaa.ncdc.wct.WCTFilter;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradSampleDimensionFactory;
import gov.noaa.ncdc.wct.event.GeneralProgressEvent;
import gov.noaa.ncdc.wct.event.GeneralProgressListener;
import gov.noaa.ncdc.wct.export.BatchKmzUtilities;
import gov.noaa.ncdc.wct.export.raster.WCTGridCoverageSupport;
import gov.noaa.ncdc.wct.morph.WCTMorphOperation;
import gov.noaa.ncdc.wct.morph.WCTMorphOperation.MorphGridCoverageHandler;
import gov.noaa.ncdc.wct.morph.WCTMorphOperation.MorphVectorSource;
import gov.noaa.ncdc.wct.ui.AbstractKmzUtilities.AltitudeMode;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import org.apache.commons.lang.time.DateUtils;
import org.geotools.gc.GridCoverage;
import org.geotools.pt.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;


public class TimeMorphKmzExport {

	private String variableToProcess = "Reflectivity";
	private WCTFilter filter = new WCTFilter(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 
			20.0, Double.POSITIVE_INFINITY,
			Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
			Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
			null);
	private int smoothing = 3;
	
	private List<GeneralProgressListener> progressListeners = new ArrayList<GeneralProgressListener>();
	private GeneralProgressEvent progressEvent = new GeneralProgressEvent(this);
	public void addProgressListener(GeneralProgressListener listener) {
		progressListeners.add(listener);
	}
	public void removeProgressListener(GeneralProgressListener listener) {
		progressListeners.remove(listener);
	}
	
	private Dimension legendDim = null;
	
	
	public void process(final URL[] urls, final File outputKmzFile, final Rectangle2D.Double displayExtent,
			final MorphSettings morphSettings) {

		try {

			WCTMorphOperation morph = new WCTMorphOperation();

//			final Rectangle2D.Double decodeExtent = new Rectangle2D.Double(
//					displayExtent.getMinX()-displayExtent.getWidth()/2,
//					displayExtent.getMinY()-displayExtent.getHeight()/2,
//					displayExtent.getWidth()*2, displayExtent.getHeight()*2);
			final Rectangle2D.Double decodeExtent = displayExtent;
			final WCTFilter decodeFilter = new WCTFilter();
			decodeFilter.setExtentFilter(decodeExtent);
//			filter.setMinValue(25);
			
			morph.setFilter(decodeFilter);
			morph.setDateBufferPercentForSWDIQuery(500);
			morph.setNumberOfMorphVectorGridCells(1);
//			morph.setSmoothFactor(5);
//			morph.set
			
			final int numberOfMorphSteps = 25;
			morphSettings.setNumMorphSteps(numberOfMorphSteps);
			final ZipOutputStream kmzOut = new ZipOutputStream(new FileOutputStream(outputKmzFile));
//			final Dimension size = new Dimension(800, 800);
			final Dimension size = WCTUtils.getEqualDimensions(displayExtent, 1000, 1000);
			final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			

			final BatchKmzUtilities kmzUtils = new BatchKmzUtilities();
			kmzUtils.initKML();

			
			for (int n=0; n<urls.length-1; n++) {
				URL firstUrl = urls[n].toURI().toURL();
				URL lastUrl = urls[n+1].toURI().toURL();
				final int idx = n;
				
				progressEvent.setProgress(((double)n)/urls.length);
				progressEvent.setStatus("Processing: "+ urls[n]);
				for (GeneralProgressListener l : progressListeners) {
					l.progress(progressEvent);
				}
				
				
				MorphGridCoverageHandler gcHandler = new MorphGridCoverageHandler() {
					@Override
					public void processGridCoverage(GridCoverage gc, Date date, int step, long durationInMillis) {
						
						// don't process the last step for 'middle' URLs in the list - this will 
						// cause repeat frames between URLs.
						if (idx < urls.length-1 && step == numberOfMorphSteps) {
							return;
						}

						System.out.println(step + " of " + numberOfMorphSteps + " morph steps");
						
						// smooth data
						double baseline = filter.getMinValue()[0];
						if (Double.isNaN(baseline) || Double.isInfinite(baseline)) {
							baseline = 15.0;
						}
						gc = WCTGridCoverageSupport.applyAlpha2(gc, 255);
						gc = WCTGridCoverageSupport.smooth(gc, smoothing, (float)baseline);

						
						try {
							gc.prefetch(displayExtent);
							
							String filename = urls[idx].toString().substring(urls[idx].toString().lastIndexOf("/")+1);
							
//							date = DateUtils.round(date, Calendar.SECOND);
							
							kmzUtils.addFrameToKMZ(kmzOut, 
									BatchKmzUtilities.getBufferedImage(gc, displayExtent, size), 
//									BatchKmzUtilities.createImage(gc, size, displayExtent), 
									filename+"_morph"+step, 
									filename+"_morph"+step+".png", 
									DateUtils.round(date, Calendar.SECOND), 
									DateUtils.round(new Date(date.getTime()+durationInMillis-1000), Calendar.SECOND),
									displayExtent, AltitudeMode.CLAMPED_TO_GROUND, 0.0, false);
							
							CategoryLegendImageProducer legend = new CategoryLegendImageProducer();
							legend.setSampleDimensionAndLabels(
									NexradSampleDimensionFactory.getSampleDimensionAndLabels(variableToProcess, false));
							date = DateUtils.round(date, Calendar.SECOND);
							legend.setDateTimeInfo(sdf.format(date));
							legend.setLegendTitle(new String[] { 
									sdf.format(date).substring(0, 10),
									sdf.format(date).substring(11) + " GMT"
								});
							legend.setForegroundColor(Color.WHITE);
							legend.setBackgroundColor(new Color(0, 0, 0, 190));
							legend.setDrawBorder(true);
							BufferedImage bimage = null;
							if (legendDim == null) {
								bimage = legend.createSmallVertLegendImage();
								legendDim = new Dimension(bimage.getWidth(), bimage.getHeight());
							}
							else {
								bimage = legend.createSmallVertLegendImage(legendDim);
							}
							
							
//							kmzUtils.addScreenOverlay(kmzUtils.getMetadataOverlay("meta-"+filename+"_morph"+step+".png", 
//									BatchKmzUtilities.ISO_DATE_FORMATTER.format(date), 
//									BatchKmzUtilities.ISO_DATE_FORMATTER.format(new Date(date.getTime()+durationInMillis)),
//									step+idx*numberOfMorphSteps, "meta-"+filename+"_morph"+step+".png"));
							kmzUtils.addKMZImageEntry("meta-"+filename+"_morph"+step+".png", bimage, kmzOut);
							
						} catch (MismatchedDimensionException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (FactoryException e) {
							e.printStackTrace();
						} catch (TransformException e) {
							e.printStackTrace();
						} catch (WCTException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
				
				
				GeneralProgressListener progressListener = new GeneralProgressListener() {
					@Override
					public void started(GeneralProgressEvent event) {
					}
					@Override
					public void ended(GeneralProgressEvent event) {
					}
					@Override
					public void progress(GeneralProgressEvent event) {

						progressEvent.setProgress(event.getProgress());
						progressEvent.setStatus(event.getStatus());
						for (GeneralProgressListener l : progressListeners) {
							l.progress(progressEvent);
						}
					}				
				};
				
				morph.setVariableName(variableToProcess);
				morph.processMorph(numberOfMorphSteps, progressListener, 
						firstUrl, lastUrl, morphSettings.getMorphVectorSource(), morphSettings, gcHandler);

			}
			

			kmzUtils.addKMLBoundsFolder(displayExtent);
			
		      // Copy NOAA logo to KMZ
//            kmzOut.putNextEntry(new ZipEntry("noaalogo.gif"));
//            // Transfer bytes from the file to the ZIP file
//            URL logoURL = ExportKMZThread.class.getResource("/images/noaa_logo_50x50.png");
//            InputStream in = logoURL.openStream();
//            int len;
//            while ((len = in.read(buf)) > 0) {
//                kmzOut.write(buf, 0, len);
//            }
//            // Complete the entry
//            kmzOut.closeEntry();
//            in.close();
			kmzUtils.addKMZImageEntry("logo.gif", 
					ImageIO.read(TimeMorphKmzExport.class.getResource("/images/noaa_logo_50x50.png")), kmzOut);
			
			kmzUtils.finishKML();
			
			System.out.println(kmzUtils.getKML());
			
			kmzUtils.writeKML(kmzOut);
			kmzOut.close();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void testProcess() {
		
		try {

			WCTMorphOperation morph = new WCTMorphOperation();

			final Rectangle2D.Double displayExtent = new Rectangle2D.Double(-94.895, 36.776, .6, .6);
			final Rectangle2D.Double decodeExtent = new Rectangle2D.Double(
					displayExtent.getMinX()-displayExtent.getWidth()/2,
					displayExtent.getMinY()-displayExtent.getHeight()/2,
					displayExtent.getWidth()*2, displayExtent.getHeight()*2);
			final WCTFilter filter = new WCTFilter();
			filter.setExtentFilter(decodeExtent);
			filter.setMinValue(54);
			
			morph.setFilter(filter);
			morph.setDateBufferPercentForSWDIQuery(500);
			morph.setNumberOfMorphVectorGridCells(1);
//			morph.setSmoothFactor(5);
			
			final int numberOfMorphSteps = 20;
//			File outfile = new File("E:\\work\\morph\\output\\joplin-5files-m30-f54dbz.kmz");
			File outfile = new File("C:\\work\\morph\\output\\moore-5files-f54dbz.kmz");
			final ZipOutputStream kmzOut = new ZipOutputStream(new FileOutputStream(outfile));
			final Dimension size = new Dimension(800, 800);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			
			
			final File[] fileArray = new File[] {
					new File("E:\\work\\tornado\\joplin\\data\\KSGF20110522_223858_V03.gz"),
					new File("E:\\work\\tornado\\joplin\\data\\KSGF20110522_224348_V03.gz"),
					new File("E:\\work\\tornado\\joplin\\data\\KSGF20110522_224838_V03.gz"),
					new File("E:\\work\\tornado\\joplin\\data\\KSGF20110522_225328_V03.gz"),
					new File("E:\\work\\tornado\\joplin\\data\\KSGF20110522_225818_V03.gz"),
			};

			final BatchKmzUtilities kmzUtils = new BatchKmzUtilities();
			kmzUtils.initKML();

			
			for (int n=0; n<fileArray.length-1; n++) {
				URL firstUrl = fileArray[n].toURI().toURL();
				URL lastUrl = fileArray[n+1].toURI().toURL();
				final Date startDate = sdf.parse(fileArray[n].getName().substring(4, 21));
				final Date endDate = sdf.parse(fileArray[n+1].getName().substring(4, 21));
				final long dateDiffPerStepInMillis = (endDate.getTime()-startDate.getTime())/numberOfMorphSteps;
				final int idx = n;
				
				MorphGridCoverageHandler gcHandler = new MorphGridCoverageHandler() {
					@Override
					public void processGridCoverage(GridCoverage gc, Date date, int step, long durationInMillis) {
						
						if (step == numberOfMorphSteps) {
							return;
						}
						
						// smooth data
						double baseline = filter.getMinValue()[0];
						if (Double.isNaN(baseline) || Double.isInfinite(baseline)) {
							baseline = 15.0;
						}
						gc = WCTGridCoverageSupport.applyAlpha2(gc, 255);
						gc = WCTGridCoverageSupport.smooth(gc, 3, (float)baseline);

						
						
						try {
							gc.prefetch(displayExtent);
							
							kmzUtils.addFrameToKMZ(kmzOut, 
//									BatchKmzUtilities.getBufferedImage(gc), 
									BatchKmzUtilities.createImage(gc, size, displayExtent), 
									fileArray[idx].getName()+"_morph"+step, 
									fileArray[idx].getName()+"_morph"+step+".png", 
									new Date(startDate.getTime()+(step*dateDiffPerStepInMillis)),
									new Date(startDate.getTime()+((step+1)*dateDiffPerStepInMillis)),
									displayExtent, AltitudeMode.CLAMPED_TO_GROUND, 0.0, false);
							
						} catch (MismatchedDimensionException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (FactoryException e) {
							e.printStackTrace();
						} catch (TransformException e) {
							e.printStackTrace();
						} catch (WCTException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
				
				
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
				
				
				morph.processMorph(numberOfMorphSteps, progressListener, 
						firstUrl, lastUrl, MorphVectorSource.NCDC_SWDI, new MorphSettings(), gcHandler);

			}
			
			
			
			kmzUtils.finishKML();
			
			kmzUtils.writeKML(kmzOut);
			kmzOut.close();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	
	

	public static void main(String[] args) {
		
		TimeMorphKmzExport tmke = new TimeMorphKmzExport();
		tmke.testProcess();
		
	}

































	public String getVariableToProcess() {
		return variableToProcess;
	}

































	public void setVariableToProcess(String variableToProcess) {
		this.variableToProcess = variableToProcess;
	}

































	public WCTFilter getFilter() {
		return filter;
	}

































	public void setFilter(WCTFilter filter) {
		this.filter = filter;
	}

































	public int getSmoothing() {
		return smoothing;
	}

































	public void setSmoothing(int smoothing) {
		this.smoothing = smoothing;
	}
	
	
}
