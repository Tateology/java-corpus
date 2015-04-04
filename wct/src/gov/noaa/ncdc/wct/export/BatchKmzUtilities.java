package gov.noaa.ncdc.wct.export;

import gov.noaa.ncdc.nexradiv.legend.CategoryLegendImageProducer;
import gov.noaa.ncdc.nexradiv.legend.LegendCategoryFactory;
import gov.noaa.ncdc.nexradiv.legend.NexradLegendLabelFactory;
import gov.noaa.ncdc.wct.ResourceUtils;
import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.WCTFilter;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.ColorLutReaders;
import gov.noaa.ncdc.wct.decoders.ColorsAndValues;
import gov.noaa.ncdc.wct.decoders.SampleDimensionAndLabels;
import gov.noaa.ncdc.wct.decoders.cdm.DecodeRadialDatasetSweepHeader;
import gov.noaa.ncdc.wct.decoders.cdm.GridDatasetRemappedRaster;
import gov.noaa.ncdc.wct.decoders.cdm.RadialDatasetSweepRemappedRaster;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Header;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradHeader;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradSampleDimensionFactory;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradUtilities;
import gov.noaa.ncdc.wct.export.WCTExport.ExportFormat;
import gov.noaa.ncdc.wct.export.raster.WCTGridCoverageSupport;
import gov.noaa.ncdc.wct.export.raster.WCTRaster;
import gov.noaa.ncdc.wct.io.SupportedDataType;
import gov.noaa.ncdc.wct.ui.AbstractKmzUtilities;
import gov.noaa.ncdc.wct.ui.WCTMapPane;
import gov.noaa.ncdc.wct.ui.animation.ExportKMZThread;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import org.geotools.gc.GridCoverage;
import org.geotools.renderer.j2d.RenderedGridCoverage;
import org.geotools.renderer.j2d.StyledMapRenderer;

public class BatchKmzUtilities extends AbstractKmzUtilities {

	private int alpha = 255;
	
	private Rectangle2D.Double extent;
	private Dimension size;
	
//	private double radialElevAngle = 0;
	
	public static void main(String[] args) {
		try {
			
			URL url = new URL("ftp://tgftp.nws.noaa.gov/SL.us008001/DF.of/DC.radar/DS.p94r0/SI.kamx/sn.last");
			WCTExport exporter = new WCTExport();
			exporter.setOutputFormat(ExportFormat.KMZ);
			exporter.exportData(url, new File("batchout-kmz.kmz"));
			
			
			
//			URL url = new File("testdata\\KLIX20050829_135451.Z").toURI().toURL();
//			Rectangle2D.Double extent = MaxGeographicExtent.getNexradExtent(
//					RadarHashtables.getSharedInstance().getLat("KLIX"), 
//					RadarHashtables.getSharedInstance().getLon("KLIX"));
//			
//			BatchKmzUtilities utils = new BatchKmzUtilities();
//			utils.setOriginAltInMeters(RadarHashtables.getSharedInstance().getElev("KLIX")/3.28083989501312);
//			utils.setOriginLat(RadarHashtables.getSharedInstance().getLat("KLIX"));
//			utils.setOriginLon(RadarHashtables.getSharedInstance().getLon("KLIX"));
//			utils.setElevationExaggeration(3);
//			
//			
//			utils.setExtent(extent);
//			utils.setSize(new Dimension(600, 600));
//			utils.setAlpha(180);
//			utils.exportKMZ(url, new File("batchout-kmz.kmz").getCanonicalFile(), SupportedDataType.RADIAL);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	/** 
	 * Utility method to export Level-III NEXRAD
	 * @param outFile
	 * @param raster
	 * @param header
	 * @throws Exception 
	 */
	public void saveKmz(File outFile, WCTRaster raster, 
			DecodeL3Header header, ExportKmlOptions kmlOptions) throws Exception {
		WCTGridCoverageSupport gcSupport = new WCTGridCoverageSupport();
		
//		URL url = ResourceUtils.getInstance().getJarResource(
//                new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
//                "/config/colormaps/nexrad_dp_z.wctpal", null);
		
		gcSupport.setPaletteOverrideURL(kmlOptions.getPaletteURL());

		
		GridCoverage gc = gcSupport.getGridCoverage(raster, header, false);
		
		
		
		
		boolean classify = header.getProductType() != NexradHeader.L3RADIAL_8BIT;
		CategoryLegendImageProducer radLegendProducer = new CategoryLegendImageProducer();
		NexradLegendLabelFactory.setStandardLegendLabels(radLegendProducer, header, 
				header.getNCDCFilename(), NexradLegendLabelFactory.DMS);
		NexradLegendLabelFactory.setSpecialLevel3LegendLabels(radLegendProducer, header, false);
		radLegendProducer.setInterpolateBetweenCategories(! classify);
		radLegendProducer.setCategoryColors(LegendCategoryFactory.getCategoryColors(header, classify));
		radLegendProducer.setCategoryLabels(LegendCategoryFactory.getCategoryStrings(header, classify));
		if (radLegendProducer.isDrawColorMap()) {
			radLegendProducer.setLegendTitle(new String[] {LegendCategoryFactory.getLegendTitle(header, classify)});
		}                    
		radLegendProducer.setDrawBorder(true);
		
		
		
		
		
		HashMap<String, String> exportHints = new HashMap<String, String>();
		exportHints.put("radialOriginLat", String.valueOf(header.getLat()));
		exportHints.put("radialOriginLon", String.valueOf(header.getLon()));
		exportHints.put("radialOriginAltInMeters", String.valueOf(header.getAlt()/3.2808399));
		exportHints.put("radialElevAngle", String.valueOf(NexradUtilities.getElevationAngle(header)));
		
		exportHints.put("elevationExaggeration", String.valueOf(kmlOptions.getElevationExaggeration()));
		exportHints.put("kmzAltType", kmlOptions.getAltMode().toString());
		exportHints.put("legendLocationKML", kmlOptions.getLegendLocationKML());
		exportHints.put("logoLocationKML", kmlOptions.getLogoLocationKML());
		exportHints.put("logoURL", kmlOptions.getLogoURL().toString());
		
		
		saveKmz(outFile, gc, exportHints, radLegendProducer);
	}

	
	
	
	/** 
	 * Utility method to export Level-II NEXRAD
	 * @param outFile
	 * @param raster
	 * @param header
	 * @throws Exception 
	 */
	public void saveKmz(File outFile, RadialDatasetSweepRemappedRaster raster, 
			DecodeRadialDatasetSweepHeader header, ExportKmlOptions kmlOptions) throws Exception {
		
		// use default palette if none is specified
		if (kmlOptions.getPaletteURL() == null) {
			String paletteName = NexradSampleDimensionFactory.getDefaultPaletteName(header.getProductCode());
			kmlOptions.setPaletteURL(
					ResourceUtils.getInstance().getJarResource(
                            new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, 
                            "/config/colormaps/"+paletteName, null)
           );					
		}

    	BufferedReader br = new BufferedReader(new InputStreamReader(kmlOptions.getPaletteURL().openStream()));
		ColorsAndValues[] cav = ColorLutReaders.parseWCTPal(br);
		br.close();
		SampleDimensionAndLabels sd = ColorLutReaders.convertToSampleDimensionAndLabels(cav[0], cav[1]);
		GridCoverage gc = raster.getGridCoverage(255, sd.getSampleDimension());
		
		HashMap<String, String> exportHints = new HashMap<String, String>();
		exportHints.put("radialOriginLat", String.valueOf(header.getLat()));
		exportHints.put("radialOriginLon", String.valueOf(header.getLon()));
		exportHints.put("radialOriginAltInMeters", String.valueOf(header.getAlt()/3.2808399));
		exportHints.put("radialElevAngle", String.valueOf(raster.getLastDecodedElevationAngle()));
		
		exportHints.put("elevationExaggeration", String.valueOf(kmlOptions.getElevationExaggeration()));
		exportHints.put("kmzAltType", kmlOptions.getAltMode().toString());
		exportHints.put("legendLocationKML", kmlOptions.getLegendLocationKML());
		exportHints.put("logoLocationKML", kmlOptions.getLogoLocationKML());
		exportHints.put("logoURL", kmlOptions.getLogoURL().toString());
		
		
		
		
		
		
		CategoryLegendImageProducer radLegendProducer = new CategoryLegendImageProducer();
		String url = header.getDataURL().toString();
		String filename = url.substring(url.lastIndexOf("/")+1);
		NexradLegendLabelFactory.setStandardLegendLabels(radLegendProducer, header, filename,
				NexradLegendLabelFactory.DMS);

		NexradLegendLabelFactory.setSpecialLevel2LegendLabels(radLegendProducer, header, raster);
		
		radLegendProducer.setSampleDimensionAndLabels(sd);
		
		if (radLegendProducer.isDrawColorMap()) {
			radLegendProducer.setLegendTitle(new String[]{ LegendCategoryFactory.getLegendTitle(header, false) });
		}                    

		radLegendProducer.setDrawBorder(true);
		
		
		
		
		
		
		
		saveKmz(outFile, gc, exportHints, radLegendProducer);
	}
	
	
	
	/** 
	 * Utility method to export Grid Datasets
	 * @param outFile
	 * @param raster
	 * @param header
	 * @throws Exception 
	 */
	public void saveKmz(File outFile, GridDatasetRemappedRaster raster, 
			ExportKmlOptions kmlOptions) throws Exception {
		
		GridCoverage gc = null;
		if (kmlOptions.getPaletteURL() == null) {
			gc = raster.getGridCoverage();
		}
		else {
			gc = WCTGridCoverageSupport.getGridCoverage(raster, kmlOptions.getPaletteURL());
		}
		
		// no collada support for grids
		if (kmlOptions.getAltMode() == AltitudeMode.DRAPE_ON_MODEL) {
			kmlOptions.setAltMode(AltitudeMode.CLAMPED_TO_GROUND);
		}
		
		
		
		HashMap<String, String> exportHints = new HashMap<String, String>();
		exportHints.put("kmzAltType", kmlOptions.getAltMode().toString());
		exportHints.put("legendLocationKML", kmlOptions.getLegendLocationKML());
		exportHints.put("logoLocationKML", kmlOptions.getLogoLocationKML());
		exportHints.put("logoURL", kmlOptions.getLogoURL().toString());
		
		
		CategoryLegendImageProducer legendProducer = new CategoryLegendImageProducer();
		String title = raster.getLongName();
		if (title == null || title.trim().length() == 0) {
			title = raster.getVariableName();
		}
		legendProducer.setLegendTitle(new String[]{ 
				title + " ("+raster.getUnits()+")", 
				 });
		legendProducer.setDataDescription(new String[]{ raster.getLongName() });
		
		
		
		
		
		
		if (kmlOptions.getPaletteURL() == null) {
			Color[] catColors = LegendCategoryFactory.getCategoryColors(raster);
			Double[] catValues = LegendCategoryFactory.getCategoryValues(raster);

			ColorsAndValues cav1 = new ColorsAndValues(catColors, catValues);
			if (cav1.getValues().length > 1 && cav1.getValues()[1] > cav1.getValues()[0]) {
				cav1.flip();
			}
			ColorsAndValues cav2 = ColorsAndValues.calculateEqualColorsAndValues( cav1, 11);
			String[] catLabels = cav2.getLabels(new DecimalFormat("0.00"));

			legendProducer.setCategoryColors(WCTUtils.flipArray( cav2.getColors() ));        
			legendProducer.setCategoryLabels(catLabels);
		}
		else {
	    	BufferedReader br = new BufferedReader(new InputStreamReader(kmlOptions.getPaletteURL().openStream()));
			ColorsAndValues[] cav = ColorLutReaders.parseWCTPal(br);
			br.close();
			SampleDimensionAndLabels sd = ColorLutReaders.convertToSampleDimensionAndLabels(cav[0], cav[1]);
			legendProducer.setSampleDimensionAndLabels(sd);

			legendProducer.setCategoryColors(cav[0].getColors());
			legendProducer.setCategoryLabels(cav[0].getLabels());
			legendProducer.setSupplementalCategoryColors(cav[1].getColors());
			legendProducer.setSupplementalCategoryLabels(cav[1].getLabels());

		}		
		legendProducer.setDrawBorder(true);
		
		if (raster.isEmptyGrid()) {
			legendProducer.setLabelOverride(" No Data ");
		}
		else {
			legendProducer.setLabelOverride("");
		}
		
		
		
		legendProducer.setInterpolateBetweenCategories(true);
		legendProducer.setDrawColorMapTransparentBackgroundPattern(kmlOptions.isDrawColorMapTransparentBackgroundPattern());
		exportHints.put("legendType", "MEDIUM");
		exportHints.put("createShadowLayer", "false");
		
		
		
		
		
		saveKmz(outFile, gc, exportHints, legendProducer);
	}
	


	
	
	
	
	/**
	 * 
	 * exportHints:
	 *  radialOriginLat, radialOriginLon, radialOriginAltInMeters, radialElevAngle,
	 *  kmzAltType[drapeOnModel, clampToGround, absolute]
	 *  
	 * 
	 * @param outFile
	 * @param gc
	 * @param exportHints
	 * @throws Exception
	 */
	public static void saveKmz(File outFile, GridCoverage gc, 
			HashMap<String, String> exportHints, 
			CategoryLegendImageProducer legendProducer) throws Exception {
		
		BatchKmzUtilities utils = new BatchKmzUtilities();
		
		if (exportHints.get("legendLocationKML") != null) {
			utils.setLegendLocationKML(exportHints.get("legendLocationKML"));
		}
		if (exportHints.get("logoLocationKML") != null) {
			utils.setLogoLocationKML(exportHints.get("logoLocationKML"));
		}
		
		
		
		
		if (exportHints.get("radialOriginLat") != null) {
			utils.setOriginLat(Double.parseDouble(exportHints.get("radialOriginLat")));
		}
		if (exportHints.get("radialOriginLon") != null) {
			utils.setOriginLon(Double.parseDouble(exportHints.get("radialOriginLon")));
		}
		if (exportHints.get("radialOriginAltInMeters") != null) {
			utils.setOriginAltInMeters(Double.parseDouble(exportHints.get("radialOriginAltInMeters")));
		}
		if (exportHints.get("radialElevAngle") != null) {
			utils.setRadialElevAngle(Double.parseDouble(exportHints.get("radialElevAngle")));
		}
		if (exportHints.get("elevationExaggeration") != null) {
			utils.setElevationExaggeration(Integer.parseInt(exportHints.get("elevationExaggeration")));
		}
		if (exportHints.get("kmzAltType") != null) {
			String altType = exportHints.get("kmzAltType").trim();
			if (altType.equalsIgnoreCase("DRAPE_ON_MODEL")) {
				utils.setAltMode(AltitudeMode.DRAPE_ON_MODEL);
			}
			else if (altType.equalsIgnoreCase("CLAMPED_TO_GROUND")) {
				utils.setAltMode(AltitudeMode.CLAMPED_TO_GROUND);
			}
			else if (altType.equalsIgnoreCase("ABSOLUTE")) {
				utils.setAltMode(AltitudeMode.ABSOLUTE);
			}
		}
		else {
			utils.setAltMode(AltitudeMode.CLAMPED_TO_GROUND);
		}
		
		
		utils.setSize(new Dimension(gc.getRenderedImage().getWidth(), gc.getRenderedImage().getHeight()));
		utils.setExtent(new Rectangle2D.Double(
				gc.getEnvelope().getMinimum(0),
				gc.getEnvelope().getMinimum(1),
				gc.getEnvelope().getLength(0),
				gc.getEnvelope().getLength(1))
		);
		
		utils.initKML();
		
//		utils.addKMLBoundsFolder(utils.getExtent());
		
//		utils.addKML3DOutlineBoxFolder(utils.getExtent());
		
		// Create the ZIP file
        ZipOutputStream kmzOut = new ZipOutputStream(new FileOutputStream(outFile));
      
//        BufferedImage image = createImage(gc, utils.getSize());
//        BufferedImage image = getBufferedImage(gc);
        BufferedImage image = getBufferedImage(gc);
        
        //ImageIO.write(image, "png", new File("imageout.png"));
        
        String filename = outFile.getName().substring(0, outFile.getName().length()-4);
		if (exportHints.get("legendType") != null && exportHints.get("legendType").equals("MEDIUM")) {
			utils.addKMZImageEntry("meta-"+filename+".png", 
					legendProducer.createMediumLegendImage(null), kmzOut);
//					legendProducer.createLargeLegendImage(), kmzOut);
//					legendProducer.createSmallVertLegendImage(), kmzOut);
		}
		else {
			utils.addKMZImageEntry("meta-"+filename+".png", 
    			legendProducer.createLargeLegendImage(new Dimension(180, 500)), kmzOut);
		}
        
        
        boolean createShadows = exportHints.get("createShadowLayer") != null && exportHints.get("createShadowLayer").equals("true");
        utils.addFrameToKMZ(kmzOut, image,
				filename+".png", filename+".png", null, null, utils.getExtent(), 
				utils.getAltMode(), 0.0, createShadows);
		
		
//		if (exportHints.get("legendType") != null && exportHints.get("legendType").equals("MEDIUM")) {
//			utils.addKMZImageEntry("meta-"+filename+".png", 
//	    			legendProducer.createMediumLegendImage(new Dimension(700, 100)), kmzOut);
//		}
//		else {
//			utils.addKMZImageEntry("meta-"+filename+".png", 
//    			legendProducer.createLargeLegendImage(new Dimension(180, 500)), kmzOut);
//		}
			
			
//		addFrameToKMZ(kmzOut, createImage(url, dataType),
//				outFile.getName()+"-draped", outFile.getName()+"-draped", null, null, extent, 
//				AltitudeMode.CLAMPED_TO_GROUND, 0.0, true);
		
		
		utils.finishKML();
		
		utils.writeKML(kmzOut);
		


        // Copy NOAA logo to KMZ
        if (exportHints.get("logoURL") != null) {
        	kmzOut.putNextEntry(new ZipEntry("logo.gif"));
        	// Transfer bytes from the file to the ZIP file
            URL logoURL = new URL(exportHints.get("logoURL"));
        	InputStream in = logoURL.openStream();
        	int len;
        	// Create a buffer for reading the files
        	byte[] buf = new byte[1024];

        	while ((len = in.read(buf)) > 0) {
        		kmzOut.write(buf, 0, len);
        	}
        	// Complete the entry
        	kmzOut.closeEntry();
        	in.close();
        }
        
        kmzOut.close();

//        kmlString.setLength(0); // clear
//        kmlMetaString.setLength(0); // clear
	}
	
	
	
//	public void exportKMZ(URL url, File outFile, SupportedDataType dataType) throws Exception {
//		
//		GridCoverage gc = getGridCoverage(url, dataType, extent, size);
//		HashMap<String, String> exportHints = new HashMap<String, String>();
//		exportHints.put("radialOriginLat", String.valueOf(getOriginLat()));
//		exportHints.put("radialOriginLon", String.valueOf(getOriginLon()));
//		exportHints.put("radialOriginAltInMeters", String.valueOf(getOriginAltInMeters()));
//		exportHints.put("radialElevAngle", String.valueOf(getRadialElevAngle()));
//		exportHints.put("elevationExaggeration", String.valueOf(getElevationExaggeration()));
//		
//		saveKmz(outFile, gc, exportHints);
//		
//	}
	
	
	public BufferedImage createImage(URL url, SupportedDataType dataType) throws Exception {
		
		return getBufferedImage(getGridCoverage(url, dataType, extent, size));
	}
	
	
	public static BufferedImage createImage(GridCoverage gc, Dimension size) throws IllegalStateException, Exception {
		return createImage(gc, size, gc.getEnvelope().toRectangle2D());
	}
	public static BufferedImage createImage(GridCoverage gc, Dimension size,
			Rectangle2D displayExtent) throws Exception {

		
		
		
//        WritableRenderedImageAdapter img = (WritableRenderedImageAdapter)(gc.getRenderedImage());        
//        RenderedImage rimg = img.wrapRenderedImage(img);
        
//        RenderedImage rimg = gc.getRenderedImage();
//        

		{
        RenderedGridCoverage rgc = new RenderedGridCoverage(gc);
        BufferedImage bimage = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bimage.createGraphics();
        rgc.paint(g, displayExtent);
			
			
////        AffineTransform xform = AffineTransform.getScaleInstance(1.0, 1.0);
////        g.drawRenderedImage(rimg, xform);
//        g.dispose();
//        g = null;
			
			
        if (true) return bimage;
		}
		
		
		
		
		
		
		
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

        wctMapPane.setPreferredArea(displayExtent);
        wctMapPane.setVisibleArea(displayExtent);

        RenderedGridCoverage rgc = new RenderedGridCoverage(gc);
        ((StyledMapRenderer) wctMapPane.getRenderer()).addLayer(rgc);

//        gc.prefetch(displayExtent);
//        rgc.repaint();

        
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        wctMapPane.setVisibleArea(displayExtent);

        
        
        
        
//        try {
//        	Thread.sleep(200);
//		} catch (Exception e) {
//		}
        
        
        wctMapPane.paint(g);
//		wctMapPane.paintAll(g);

        
//        try {
//        	Thread.sleep(200);
//		} catch (Exception e) {
//		}
//
//        
////        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
////        wctMapPane.setVisibleArea(gc.getEnvelope().toRectangle2D());
////        AffineTransform t = AffineTransform.getScaleInstance(1.0, 1.0);
////        ((StyledMapRenderer) wctMapPane.getRenderer()).paint(g, 
////                wctMapPane.getWCTZoomableBounds(null), t, true);
//
//        
//        
////        AWTImageExport.saveImage(bimage, new File("batchout-kmz.png").getCanonicalFile(), AWTImageExport.Type.PNG);
//        
        g.dispose();
        rgc.dispose();
        gc.dispose();
        
        return bimage;
        
	}
	
	
	
	
	
	public GridCoverage getGridCoverage(
			URL url, SupportedDataType dataType, 
			Rectangle2D.Double extent, Dimension size) 
		throws Exception {
		
		this.size = WCTUtils.getEqualDimensions(this.extent, this.size.width, this.size.height);

		
		GridCoverage gc = null;
		
		if (dataType == SupportedDataType.RADIAL) {
			RadialDatasetSweepRemappedRaster radialRaster = 
				new RadialDatasetSweepRemappedRaster();
//			radialRaster.get
			
			WCTFilter wctFilter = new WCTFilter();
			wctFilter.setValueRange(20.0, 100.0);
			
			radialRaster.setVariableName("Reflectivity");
			radialRaster.setWidth(size.width);
			radialRaster.setHeight(size.height);
			radialRaster.setWctFilter(wctFilter);
			radialRaster.process(url.toString(), extent);
			radialRaster.setSmoothingFactor(10);
			gc = radialRaster.getGridCoverage(alpha);
			
			setRadialElevAngle( radialRaster.getLastDecodedElevationAngle() );
		}
//		else if (dataType == SupportedDataType.NEXRAD_LEVEL3 ||
//				dataType == SupportedDataType.NEXRAD_LEVEL3_NWS) {
//			WCTGridCoverageSupport gcSupport = new WCTGridCoverageSupport();
//			gcSupport.
//		}
		
		
		return gc;
	}

	
	

	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}
	public int getAlpha() {
		return alpha;
	}


	public void setExtent(Rectangle2D.Double extent) {
		this.extent = extent;
	}


	public Rectangle2D.Double getExtent() {
		return extent;
	}


	/**
	 * This size will be adjusted to preserve the aspect ratio
	 * based on the extent during the getGridCoverage() method.
	 * @param size
	 */
	public void setSize(Dimension size) {
		this.size = size;
	}


	public Dimension getSize() {
		return size;
	}
	
	
	
	
	
	

}