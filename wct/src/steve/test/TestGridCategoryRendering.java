package steve.test;

import gov.noaa.ncdc.nexradiv.legend.CategoryLegendImageProducer;
import gov.noaa.ncdc.wct.ResourceUtils;
import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.WCTFilter;
import gov.noaa.ncdc.wct.WCTIospManager;
import gov.noaa.ncdc.wct.decoders.ColorLutReaders;
import gov.noaa.ncdc.wct.decoders.ColorsAndValues;
import gov.noaa.ncdc.wct.decoders.SampleDimensionAndLabels;
import gov.noaa.ncdc.wct.decoders.cdm.RadialDatasetSweepRemappedRaster;
import gov.noaa.ncdc.wct.export.WCTExport;
import gov.noaa.ncdc.wct.export.WCTExport.ExportFormat;
import gov.noaa.ncdc.wct.ui.Viewer;
import gov.noaa.ncdc.wct.ui.WCTMapPane;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.JFrame;

import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.cv.SampleDimension;
import org.geotools.gc.GridCoverage;
import org.geotools.gui.swing.WCTStatusBar;
import org.geotools.pt.Envelope;
import org.geotools.renderer.j2d.RenderedGridCoverage;
import org.geotools.renderer.j2d.RenderedLogo;
import org.geotools.renderer.j2d.StyledMapRenderer;

public class TestGridCategoryRendering {

	
	public static void main(String[] args) {
		try {
			

			// needed to get RF values to be handled correctly
	        try {
	            WCTIospManager.getInstance().registerIosp(ucar.nc2.iosp.nexrad2.WCTNexrad2IOServiceProvider.class);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }


			
			
			final URL url = new File("testdata\\KLIX20050829_135451.Z").toURI().toURL();
			
			
			final WCTFilter filter = new WCTFilter();
			Rectangle2D.Double bounds = new Rectangle2D.Double(-91.3, 28.6, 3, 3);
			filter.setExtentFilter(bounds);
			
			final WCTExport exporter1 = new WCTExport();
			exporter1.setExportVariable("RadialVelocity");
//			exporter1.setExportL3Filter(filter);			
			exporter1.setExportRadialFilter(filter);		
			exporter1.setExportGridSize(2200);
//			exporter1.setExportGridSatelliteFilter(filter);			
//			exporter1.setExportGridSmoothFactor(5);
			exporter1.setOutputFormat(ExportFormat.WCT_RASTER_OBJECT_ONLY);
//			exporter1.setExportGridNoData(-25.0f);
			exporter1.exportData(url, new File("startImage.wctobj"));
			
			WritableRaster raster = exporter1.getLastProcessedRadialRemappedRaster().getWritableRaster();
			
			
			
//			double[] minvals = new double[] {
//					0, 20.000001, 40.000001
//			};
//			double[] maxvals = new double[] {
//					20, 40, 60
//			};
//			Color[][] colors = new Color[][] {
//					new Color[] { Color.WHITE, Color.BLUE },
//					new Color[] { Color.GREEN, Color.RED },
//					new Color[] { Color.DARK_GRAY, Color.MAGENTA },
//			};
//			
//			Category[] catArray = new Category[minvals.length];
//			for (int n=0; n<catArray.length; n++) {
//				catArray[n] = new Category("cat"+n, 
//						colors[n], new NumberRange(n*255/catArray.length, (n+1)*255/catArray.length-1), 
//						new NumberRange(minvals[n], maxvals[n])).geophysics(true);
//			}
//			SampleDimension sd = new SampleDimension(catArray, null);

			
			
			URL palUrl = ResourceUtils.getInstance().getJarResource(
	                new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
	                "/config/colormaps/nexrad_dp_z-test.wctpal", null);			
	        BufferedReader br = new BufferedReader(new InputStreamReader(palUrl.openStream()));
	        ColorsAndValues[] cav = ColorLutReaders.parseWCTPal(br);
	   		br.close();	   		
	   		
	   		SampleDimensionAndLabels sd = ColorLutReaders.convertToSampleDimensionAndLabels(cav[0], cav[1]);
			
			GridCoverage gc = new GridCoverage("test", raster, GeographicCoordinateSystem.WGS84, null,
					new Envelope(bounds), new SampleDimension[] { sd.getSampleDimension() });
			
//			GridCoverage gc = new GridCoverage("test", raster, GeographicCoordinateSystem.WGS84, 
//					new Envelope(bounds), minvals, maxvals, null, colors, null);
			
			
			
			CategoryLegendImageProducer clip = new CategoryLegendImageProducer();
			clip.setSampleDimensionAndLabels(sd);
			clip.setLegendTitle( new String[]{ "Title" });
			clip.setDrawBorder(true);
//			clip.s
			
			
			
			
			
			Dimension size = new Dimension(600, 600);

//	        BufferedImage bimage = new BufferedImage(
//	                size.width, size.height, 
//	                BufferedImage.TYPE_INT_ARGB);
//	        Graphics2D g = bimage.createGraphics();
	        
	        WCTMapPane wctMapPane = new WCTMapPane();
//	        wctMapPane.setBackground(new Color(0, 0, 0, 0));
	        wctMapPane.setMagnifierGlass(wctMapPane.getBackground());
	        wctMapPane.setDoubleBuffered(true);
	        wctMapPane.setBounds(new Rectangle(size.width, size.height));
	        
//	        g.setColor(new Color(0, 0, 0, 0));
//	        g.fillRect(0, 0, bimage.getWidth(), bimage.getHeight());      
	        
	        
	        RenderedGridCoverage rgc = new RenderedGridCoverage(gc);
	        rgc.setZOrder(0.1f);
	        ((StyledMapRenderer) wctMapPane.getRenderer()).addLayer(rgc);

	        RenderedLogo rlogo = new RenderedLogo(clip.createLargeLegendImage(new Dimension(140, 350)));
	        rlogo.setZOrder(400f);
	        ((StyledMapRenderer) wctMapPane.getRenderer()).addLayer(rlogo);
	        

	        
	        
	        
	        
			Viewer viewer = new Viewer() {
				@Override
				public RadialDatasetSweepRemappedRaster getRadialRemappedRaster() {
					return exporter1.getLastProcessedRadialRemappedRaster();
				}
				@Override
				public SampleDimension getSampleDimension() {
					return null;
				}
			};
			WCTStatusBar statusBar = new WCTStatusBar(wctMapPane, viewer);
			

			
			
			
	        
	        JFrame frame = new JFrame();
	        frame.getContentPane().setLayout(new BorderLayout());
	        frame.getContentPane().add(wctMapPane, BorderLayout.CENTER);
	        frame.getContentPane().add(statusBar, BorderLayout.SOUTH);
	        frame.pack();
	        frame.setVisible(true);
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
