package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.gis.kml.KMLUtils;
import gov.noaa.ncdc.wct.WCTException;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.ui.ge.WCTColladaUtils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import org.geotools.gc.GridCoverage;
import org.geotools.pt.MismatchedDimensionException;
import org.geotools.renderer.j2d.RenderedGridCoverage;
import org.geotools.renderer.j2d.RenderedLayer;
import org.geotools.renderer.j2d.StyledMapRenderer;
import org.geotools.resources.geometry.XAffineTransform;
import org.geotools.resources.gui.ResourceKeys;
import org.geotools.resources.renderer.Resources;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;


public abstract class AbstractKmzUtilities {

	public final static String DEFAULT_LEGEND_LOCATION_KML = 
		"        <overlayXY x= \"0\" y= \"1\" xunits= \"fraction\" yunits= \"fraction\" /> \n"+  
		"        <screenXY x= \"0\" y= \"0.875\" xunits= \"fraction\" yunits= \"fraction\" /> \n"+
		"        <rotationXY x= \"0\" y= \"0\" xunits= \"fraction\" yunits= \"fraction\" /> \n"+
		"        <size x= \"-1\" y= \"-1\" xunits= \"pixels\" yunits= \"pixels\" />\n";
	public final static String DEFAULT_LOGO_LOCATION_KML = 
		"        <overlayXY x= \"1\" y= \"0\" xunits= \"fraction\" yunits= \"fraction\" /> \n"+  
		"        <screenXY x= \"1\" y= \"0.125\" xunits= \"fraction\" yunits= \"fraction\" /> \n"+
		"        <rotationXY x= \"0\" y= \"0\" xunits= \"fraction\" yunits= \"fraction\" /> \n"+
		"        <size x= \"-1\" y= \"-1\" xunits= \"pixels\" yunits= \"pixels\" />\n";



	private StringBuffer kmlString = new StringBuffer();
	private StringBuffer kmlMetaString = new StringBuffer();
	private int kmlFrameIndex = 0;

	private AltitudeMode altMode = AltitudeMode.CLAMPED_TO_GROUND;
	private double altitude = Double.NaN;
	private boolean createShadowImages = false;
	private boolean isDrapeOnColladaSelected = false;
	private int elevationExaggeration = 1;
	private double radialElevAngle = 0.0;

	public static final SimpleDateFormat ISO_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	public static final SimpleDateFormat FILE_SCANNER_FORMATTER = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
	static {
		ISO_DATE_FORMATTER.setTimeZone(TimeZone.getTimeZone("GMT"));
		FILE_SCANNER_FORMATTER.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	public enum AltitudeMode { CLAMPED_TO_GROUND, ABSOLUTE, DRAPE_ON_MODEL }

	private ArrayList<KMLGroundOverlay> kmlOverlayList = new ArrayList<KMLGroundOverlay>();
	private ArrayList<KMLModel> kmlModelList = new ArrayList<KMLModel>();

	private double originLat;
	private double originLon;
	private double originAltInMeters;

	private String legendLocationKML = DEFAULT_LEGEND_LOCATION_KML;
	private String logoLocationKML = DEFAULT_LOGO_LOCATION_KML;






	/**
	 * Init the KML string, setting up the folder structure, etc...
	 */
	public void initKML() {
		kmlString.setLength(0); // clear
		kmlMetaString.setLength(0); // clear
		kmlFrameIndex = 0;

		kmlOverlayList.clear();
		kmlModelList.clear();

		kmlString.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n");
		kmlString.append("<kml xmlns=\"http://earth.google.com/kml/2.1\"> \n");
		kmlString.append("<Document> \n");
		kmlString.append(" <Folder> \n");
		kmlString.append("   <name>NOAA Weather and Climate Toolkit Generated Animation</name> \n");
		kmlString.append("      <description><![CDATA[" +
				"Animation Controls: <br>" +
				"To adjust transparency of the image frames and legend/logo, " +
				"select this folder and adjust transparency slider. <br><br>" +
				"This animation was created by NOAA's Weather and Climate Toolkit.  For more " +
				"information, please visit one of the following links. <br><br>" +
				"<a href=\"http://www.ncdc.noaa.gov\">NOAA's National Climatic Data Center (NCDC) </a> <br> " +
				"<a href=\"http://www.ncdc.noaa.gov/wct/\">NOAA Weather and Climate Toolkit</a> <br> " +
				"]]></description> \n");
		kmlString.append("   <open>1</open> \n");
		kmlString.append("   <Style> \n");
		kmlString.append("       <ListStyle> \n");
		kmlString.append("           <bgColor>ddffffff</bgColor> \n");
		kmlString.append("       </ListStyle> \n");
		kmlString.append("   </Style> \n");
		kmlString.append("   <Folder> \n");
		kmlString.append("      <name>Image Frames</name> \n");
		kmlString.append("      <visibility>0</visibility> \n");
		kmlString.append("      <description><![CDATA[" +
				"Image Frame Controls: " +
				"To adjust transparency of just the image frames, " +
				"select this folder and adjust transparency slider. ]]></description> \n");


	}
















	/**
	 * Add a line 3D cube bounding box with labels
	 * @param bounds
	 * @throws Exception
	 */
	public void addKML3DOutlineBoxFolder(Rectangle2D bounds) throws Exception {

		kmlString.append("   <Folder> \n");
		kmlString.append("   <name>Exported Data Extent - 3D Cube Outline</name> \n");
		kmlString.append("   <visibility>0</visibility> \n");
		kmlString.append("      <description><![CDATA[" +
				"This represents the extent of the exported data:  " +
				"To adjust transparency of just the legend and logo, " +
				"select this folder and adjust transparency slider. ]]></description> \n");

		for (int n=0; n<16+1; n=n+4) {

			//        	String label = WCTUtils.DECFMT_0D000.format(bounds.getMinX())+" , "+WCTUtils.DECFMT_0D000.format(bounds.getMinY(), );
			String label = n+" km";
			String labelCoords = bounds.getMinX()+","+bounds.getMinY()+","+n*elevationExaggeration*1000;


			String kmlGeomOutline = KMLUtils.getExtentLineKML(bounds, n*elevationExaggeration*1000);
			kmlString.append("    <Placemark>\n");
			kmlString.append("      <name>Outline</name>\n");
			kmlString.append("      <visibility>0</visibility>\n");
			kmlString.append("      <styleUrl>#box3dStyle</styleUrl>\n");
			kmlString.append(kmlGeomOutline);
			kmlString.append("    </Placemark>\n");
			kmlString.append("    <Placemark>\n");
			kmlString.append("      <name>"+label+"</name>\n");
			kmlString.append("      <visibility>0</visibility>\n");
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
		kmlString.append("           <coordinates>"+bounds.getMinX()+","+bounds.getMinY()+","+ 0*elevationExaggeration*1000);        	
		kmlString.append("                        "+bounds.getMinX()+","+bounds.getMinY()+","+16*elevationExaggeration*1000+"</coordinates>\n");        	
		kmlString.append("      </LineString>\n");
		kmlString.append("    </Placemark>\n");
		kmlString.append("    <Placemark>\n");
		kmlString.append("      <name>Outline</name>\n");
		kmlString.append("      <visibility>0</visibility>\n");
		kmlString.append("      <styleUrl>#box3dStyle</styleUrl>\n");
		kmlString.append("      <LineString>\n");
		kmlString.append("           <altitudeMode>absolute</altitudeMode>\n");
		kmlString.append("           <coordinates>"+bounds.getMaxX()+","+bounds.getMinY()+","+ 0*elevationExaggeration*1000);        	
		kmlString.append("                        "+bounds.getMaxX()+","+bounds.getMinY()+","+16*elevationExaggeration*1000+"</coordinates>\n");        	
		kmlString.append("      </LineString>\n");
		kmlString.append("    </Placemark>\n");
		kmlString.append("    <Placemark>\n");
		kmlString.append("      <name>Outline</name>\n");
		kmlString.append("      <visibility>0</visibility>\n");
		kmlString.append("      <styleUrl>#box3dStyle</styleUrl>\n");
		kmlString.append("      <LineString>\n");
		kmlString.append("           <altitudeMode>absolute</altitudeMode>\n");
		kmlString.append("           <coordinates>"+bounds.getMaxX()+","+bounds.getMaxY()+","+ 0*elevationExaggeration*1000);        	
		kmlString.append("                        "+bounds.getMaxX()+","+bounds.getMaxY()+","+16*elevationExaggeration*1000+"</coordinates>\n");        	
		kmlString.append("      </LineString>\n");
		kmlString.append("    </Placemark>\n");
		kmlString.append("    <Placemark>\n");
		kmlString.append("      <name>Outline</name>\n");
		kmlString.append("      <visibility>0</visibility>\n");
		kmlString.append("      <styleUrl>#box3dStyle</styleUrl>\n");
		kmlString.append("      <LineString>\n");
		kmlString.append("           <altitudeMode>absolute</altitudeMode>\n");
		kmlString.append("           <coordinates>"+bounds.getMinX()+","+bounds.getMaxY()+","+ 0*elevationExaggeration*1000);        	
		kmlString.append("                        "+bounds.getMinX()+","+bounds.getMaxY()+","+16*elevationExaggeration*1000+"</coordinates>\n");        	
		kmlString.append("      </LineString>\n");
		kmlString.append("    </Placemark>\n");



		kmlString.append("    <Style id=\"box3dStyle\"> \n");
		kmlString.append("      <LineStyle> \n");
		kmlString.append("        <width>1.0</width> \n");
		kmlString.append("      </LineStyle> \n");
		kmlString.append("      <PolyStyle> \n");
		kmlString.append("        <color>01000000</color> \n");
		kmlString.append("      </PolyStyle> \n");
		kmlString.append("    </Style> \n");

		kmlString.append("   </Folder> \n");


	}






	/**
	 * Add a line bounding box and filled semi-transparent bounding box representing the extent of the data.
	 * @param bounds
	 * @throws Exception
	 */
	public void addKMLBoundsFolder(Rectangle2D bounds) throws Exception {

		kmlString.append("   <Folder> \n");
		kmlString.append("   <name>Exported Data Extent</name> \n");
		kmlString.append("   <visibility>0</visibility> \n");
		kmlString.append("      <description><![CDATA[" +
				"This represents the extent of the exported data:  " +
				"To adjust transparency of just the legend and logo, " +
				"select this folder and adjust transparency slider. ]]></description> \n");


		String kmlGeomOutline = KMLUtils.getExtentLineKML(bounds);
		kmlString.append("    <Placemark>                            \n");
		kmlString.append("      <name>Outline</name>                 \n");
		kmlString.append("      <visibility>1</visibility>           \n");
		kmlString.append("      <styleUrl>#outlineStyle</styleUrl>   \n");
		kmlString.append(kmlGeomOutline);
		kmlString.append("    </Placemark>                           \n");



		//        String kmlGeomBackground = KMLUtils.getExtentPolyKML(bounds);
		//        kmlString.append("    <Placemark>                            \n");
		//        kmlString.append("      <name>Background</name>              \n");
		//        kmlString.append("      <visibility>0</visibility>           \n");
		//        kmlString.append("      <styleUrl>#transPolyStyle</styleUrl> \n");
		//        kmlString.append(kmlGeomBackground);
		//        kmlString.append("    </Placemark>                           \n");

		kmlString.append("    <Style id=\"outlineStyle\">           \n");
		kmlString.append("      <LineStyle>                         \n");
		kmlString.append("        <width>4.0</width>                \n");
		kmlString.append("      </LineStyle>                        \n");
		kmlString.append("      <PolyStyle>                         \n");
		kmlString.append("        <color>01000000</color>           \n");
		kmlString.append("      </PolyStyle>                        \n");
		kmlString.append("    </Style>                              \n");

		kmlString.append("    <Style id=\"transPolyStyle\">         \n");
		kmlString.append("      <LineStyle>                         \n");
		kmlString.append("        <width>1.5</width>                \n");
		kmlString.append("      </LineStyle>                        \n");
		kmlString.append("      <PolyStyle>                         \n");
		kmlString.append("        <color>7d000000</color>           \n");
		kmlString.append("      </PolyStyle>                        \n");
		kmlString.append("    </Style>                              \n");

		kmlString.append("   </Folder> \n");


	}






	public void addFrameToKMZ(
			ZipOutputStream kmzOut, BufferedImage frameImage, 
			String frameName, String imageName, 
			Date startDate, Date endDate,
			Rectangle2D.Double bounds, AltitudeMode altMode, 
			double alt, boolean createShadowImages) 
					throws IOException, MismatchedDimensionException, FactoryException, TransformException, WCTException {



		addKMZImageEntry(imageName, frameImage, kmzOut);

		// ------------------------------------------
		// 2) Generate Metadata Image for this frame
		// ------------------------------------------
		String frameMetaName = "meta-"+imageName;
		//        addKMZImageEntry(frameMetaName, getMetaBufferedImage(), kmzOut);

		// ------------------------------------------
		// 3) Add reference to image frame in KML
		// ------------------------------------------
		addFrameToKML(frameName, imageName, startDate, endDate, bounds, altMode, alt, createShadowImages, true);


		// 4) Add collada dae model if needed
		if (altMode == AltitudeMode.DRAPE_ON_MODEL) {
			addKMZDaeEntry(frameName+".dae", frameName, kmzOut);
		}
	}




	/**
	 * If endTime is null, then we are at the last frame.
	 * @param frameName
	 * @param startTime
	 * @param endTime
	 * @param bounds
	 * @param alt
	 * @param createShadowImages
	 * @param shadowAlpha
	 */
	public void addFrameToKML(
			String frameName, String imageName, 
			Date startTime, Date endTime, 
			Rectangle2D.Double bounds, AltitudeMode altMode, double alt, 
			boolean createShadowImages, boolean addLegendOverlays) {

		this.altMode = altMode;
		this.altitude = alt;
		this.createShadowImages = createShadowImages;

		kmlFrameIndex++;

		if (altMode == AltitudeMode.DRAPE_ON_MODEL) {
			KMLModel kmlModel = new KMLModel();
			kmlModel.setFrameName(frameName);
			kmlModel.setTimestamp(startTime);
			kmlModel.setEndTimestamp(endTime);
			kmlModel.setImageName(imageName);
			kmlModel.setFrameIndex(kmlFrameIndex);
			kmlModel.setAltitude(alt);
			kmlModel.setBounds(bounds);
			kmlModel.setAddLegendOverlays(addLegendOverlays);
			kmlModel.setOriginLat(this.originLat);
			kmlModel.setOriginLon(this.originLon);
			kmlModel.setOriginAltInMeters(this.originAltInMeters);

			kmlModelList.add(kmlModel);
		}
		else {

			KMLGroundOverlay kmlOverlay = new KMLGroundOverlay();
			kmlOverlay.setFrameName(frameName);
			kmlOverlay.setTimestamp(startTime);
			kmlOverlay.setEndTimestamp(endTime);
			kmlOverlay.setImageName(imageName);
			kmlOverlay.setFrameIndex(kmlFrameIndex);
			kmlOverlay.setAltitude(alt);
			kmlOverlay.setAltitudeMode(altMode);
			kmlOverlay.setBounds(bounds);
			kmlOverlay.setAddLegendOverlays(addLegendOverlays);

			kmlOverlayList.add(kmlOverlay);
		}

	}

	private void processKMLOverlayList() {
		if (kmlOverlayList.size() == 0) {
			return;
		}

		// find average time difference between layers and use that to calculate the last time step
		long avgTimeDiff = calculateAverageTimestep(kmlOverlayList);

		for (int n=0; n<kmlOverlayList.size(); n++) {

			//            System.out.println("kmlOverlayList timestamp:  "+kmlOverlayList.get(n).getTimestamp());


			KMLGroundOverlay overlay = kmlOverlayList.get(n);
			String frameName = overlay.getFrameName();
			int frameIndex = overlay.getFrameIndex();
			String imageName = overlay.getImageName();
			double frameAlt = overlay.getAltitude();
			AltitudeMode frameAltMode = overlay.getAltitudeMode();
			Rectangle2D.Double frameBounds = overlay.getBounds();

			Date startTime = overlay.getTimestamp();
			Date endTime = overlay.getEndTimestamp();
			if ((endTime != null && startTime != null && startTime.equals(endTime)) ||
					(endTime == null && startTime != null)) {

				if (n < kmlOverlayList.size()-1) {
					endTime = kmlOverlayList.get(n+1).getTimestamp();
					if (startTime.equals(endTime)) {
						endTime = new Date(startTime.getTime() + avgTimeDiff);
					}
				}
				else {
					endTime = new Date(startTime.getTime() + avgTimeDiff);
				}
			}





			if (startTime != null) System.out.println("  now: "+ISO_DATE_FORMATTER.format(startTime));
			if (endTime != null) System.out.println(" next: "+ISO_DATE_FORMATTER.format(endTime));

			String kmlTimeString = null;
			String nextKmlTimeString = null;

			if (startTime != null) {

				kmlTimeString = ISO_DATE_FORMATTER.format(startTime);
				nextKmlTimeString = ISO_DATE_FORMATTER.format(endTime);

				// 012345678901234567890
				// 20040812 23:34
				// YYYY-MM-DDThh:mm:ssZ
				// Add ZIP entry to output stream.
			}



			kmlString.append("   <GroundOverlay> \n");
			kmlString.append("       <name>"+frameName+"</name> \n");
			if (kmlTimeString != null) {
				kmlString.append("       <TimeSpan> \n");
				kmlString.append("           <begin>"+kmlTimeString+"</begin> \n");
				kmlString.append("           <end>"+nextKmlTimeString+"</end> \n");
				kmlString.append("       </TimeSpan> \n");
			}
			kmlString.append("       <drawOrder>"+kmlFrameIndex+"</drawOrder> \n");
			kmlString.append("       <Icon> \n");
			kmlString.append("           <href>"+imageName+"</href> \n");
			kmlString.append("           <viewBoundScale>0.75</viewBoundScale> \n");
			kmlString.append("       </Icon> \n");
			if (frameAlt > 0) {
				kmlString.append("       <altitude>"+frameAlt+"</altitude>        \n");
				if (frameAltMode == AltitudeMode.ABSOLUTE) {
					kmlString.append("       <altitudeMode>absolute</altitudeMode>   \n");
				}
				else {
					kmlString.append("       <altitudeMode>relativeToGround</altitudeMode>   \n");
				}
			}
			kmlString.append("       <LatLonBox> \n");
			kmlString.append("           <north>"+frameBounds.getMaxY()+"</north> \n");
			kmlString.append("           <south>"+frameBounds.getMinY()+"</south> \n");
			kmlString.append("           <east>"+frameBounds.getMaxX()+"</east> \n");
			kmlString.append("           <west>"+frameBounds.getMinX()+"</west> \n");
			kmlString.append("       </LatLonBox> \n");
			kmlString.append("   </GroundOverlay> \n");



			// Optional Shadow Images
			if (createShadowImages) {
				kmlFrameIndex++;

				createShadowImageKML(kmlString, frameName, kmlTimeString, nextKmlTimeString, imageName, frameBounds);

			}

			// Metadata
			if (overlay.isAddLegendOverlays()) {
				kmlMetaString.append(getMetadataOverlay(frameName+"-Metadata", kmlTimeString, nextKmlTimeString, kmlFrameIndex, "meta-"+imageName));
			}

			//Legend
			//        String legendOverlayXY = "x= \"0\" y= \"0\" xunits= \"fraction\" yunits= \"fraction\" ";
			//        String legendScreenXY = "x= \"0\" y= \"0.02\" xunits= \"fraction\" yunits= \"fraction\" ";
			//        String legendRotationXY = "x= \"0\" y= \"0\" xunits= \"fraction\" yunits= \"fraction\" ";
			//        String legendSize = "x= \"-1\" y= \"-1\" xunits= \"pixels\" yunits= \"pixels\" ";
			//        kmlMetaString.append("   <ScreenOverlay> \n");
			//        kmlMetaString.append("      <name>"+name+"-Legend</name> \n");
			//        kmlMetaString.append("      <TimeSpan> \n");
			//        kmlMetaString.append("          <begin>"+kmlTimeString+"</begin> \n");
			//        kmlMetaString.append("          <end>"+nextKmlTimeString+"</end> \n");
			//        kmlMetaString.append("      </TimeSpan> \n");
			//        kmlMetaString.append("      <drawOrder>"+kmlFrameIndex+"</drawOrder> \n");
			//        kmlMetaString.append("      <Icon><href> legend-"+frameName+"</href></Icon> \n");
			//        kmlMetaString.append("      <overlayXY " + legendOverlayXY + " /> \n");
			//        kmlMetaString.append("      <screenXY " + legendScreenXY + " /> \n");
			//        kmlMetaString.append("      <rotationXY " + legendRotationXY + " /> \n");
			//        kmlMetaString.append("      <size " + legendSize + " /> \n");
			//        kmlMetaString.append("   </ScreenOverlay>");



			kmlFrameIndex++;
		}
	}



	public String getMetadataOverlay(String frameName, String kmlTimeString, String nextKmlTimeString, int kmlFrameIndex, String imageName) {

		StringBuilder kmlMetaString = new StringBuilder();

		kmlMetaString.append("   <ScreenOverlay> \n");
		kmlMetaString.append("      <name>"+frameName+"</name> \n");
		if (kmlTimeString != null && nextKmlTimeString != null) {
			kmlMetaString.append("      <TimeSpan> \n");
			kmlMetaString.append("          <begin>"+kmlTimeString+"</begin> \n");
			kmlMetaString.append("          <end>"+nextKmlTimeString+"</end> \n");
			kmlMetaString.append("      </TimeSpan> \n");
		}
		kmlMetaString.append("      <drawOrder>"+kmlFrameIndex+"</drawOrder> \n");
		kmlMetaString.append("      <Icon><href>"+imageName+"</href></Icon> \n");
		kmlMetaString.append(legendLocationKML + "\n");
		kmlMetaString.append("   </ScreenOverlay> \n");

		return kmlMetaString.toString();
	}


	private void processKMLModelList() {

		if (kmlModelList.size() == 0) {
			return;
		}

		long avgTimeDiff = calculateAverageTimestep(kmlModelList);

		for (int n=0; n<kmlModelList.size(); n++) {

			//            System.out.println("kmlModelList timestamp:  "+kmlModelList(n).getTimestamp());


			KMLModel model = kmlModelList.get(n);
			String frameName = model.getFrameName();
			int frameIndex = model.getFrameIndex();
			String imageName = model.getImageName();
			double frameAlt = model.getAltitude();
			Rectangle2D.Double frameBounds = model.getBounds();

			double originLat = model.getOriginLat();
			double originLon = model.getOriginLon();
			double originAltInMeters = model.getOriginAltInMeters();

			Date startTime = model.getTimestamp();
			Date endTime = model.getEndTimestamp();
			if ((endTime != null && startTime != null && startTime.equals(endTime)) ||
					(endTime == null && startTime != null)) {

				if (n < kmlModelList.size()-1) {
					endTime = kmlModelList.get(n+1).getTimestamp();
					if (startTime.equals(endTime)) {
						endTime = new Date(startTime.getTime() + avgTimeDiff);
					}
				}
				else {
					endTime = new Date(startTime.getTime() + avgTimeDiff);
				}
			}





			if (startTime != null) System.out.println("  now: "+ISO_DATE_FORMATTER.format(startTime));
			if (endTime != null) System.out.println(" next: "+ISO_DATE_FORMATTER.format(endTime));

			String kmlTimeString = null;
			String nextKmlTimeString = null;

			if (startTime != null) {

				kmlTimeString = ISO_DATE_FORMATTER.format(startTime);
				nextKmlTimeString = ISO_DATE_FORMATTER.format(endTime);

				// 012345678901234567890
				// 20040812 23:34
				// YYYY-MM-DDThh:mm:ssZ
				// Add ZIP entry to output stream.
			}




			kmlString.append(" 	<Placemark> \n");
			kmlString.append(" 	<name>"+frameName+"</name> \n");
			if (kmlTimeString != null) {
				kmlString.append("       <TimeSpan> \n");
				kmlString.append("           <begin>"+kmlTimeString+"</begin> \n");
				kmlString.append("           <end>"+nextKmlTimeString+"</end> \n");
				kmlString.append("       </TimeSpan> \n");
			}
			kmlString.append(" 	<visibility>1</visibility> \n");
			kmlString.append(" 	<Style id=\"default\"></Style> \n");
			kmlString.append(" 	<LookAt> \n");
			kmlString.append(" 	<altitudeMode>absolute</altitudeMode> \n");
			kmlString.append("  	<longitude>"+frameBounds.getCenterX()+"</longitude> \n");
			kmlString.append("  	<latitude>"+frameBounds.getCenterY()+"</latitude> \n");
			kmlString.append(" 	<altitude>"+originAltInMeters+"</altitude> \n");
			kmlString.append(" 	    <range>300000</range> \n");
			kmlString.append(" 	    <tilt>65</tilt> \n");
			kmlString.append(" 	    <heading>0</heading> \n");
			kmlString.append(" 	</LookAt> \n");
			kmlString.append(" 	<Model id=\"model_1\"> \n");
			kmlString.append(" 	<altitudeMode>absolute</altitudeMode> \n");
			kmlString.append(" 	<Location> \n");
			kmlString.append("  	<longitude>"+originLon+"</longitude> \n");
			kmlString.append("  	<latitude>"+originLat+"</latitude> \n");
			kmlString.append(" 	<altitude>"+originAltInMeters+"</altitude> \n");
			kmlString.append(" 	</Location> \n");
			kmlString.append(" 	<Orientation> \n");
			kmlString.append(" 	<heading>0</heading> \n");
			kmlString.append(" 	<tilt>0</tilt> \n");
			kmlString.append(" 	<roll>0</roll> \n");
			kmlString.append(" 	</Orientation> \n");
			kmlString.append(" 	<Scale> \n");
			kmlString.append(" 	<x>1</x> \n");
			kmlString.append(" 	<y>1</y> \n");
			kmlString.append(" 	<z>1</z> \n");
			kmlString.append(" 	</Scale> \n");
			kmlString.append(" 	<Link> \n");
			kmlString.append(" 	<href>"+imageName+".dae</href> \n");
			kmlString.append(" 	</Link> \n");
			kmlString.append(" 	</Model> \n");
			kmlString.append(" </Placemark> \n");


			// Optional Shadow Images
			if (createShadowImages) {
				kmlFrameIndex++;

				createShadowImageKML(kmlString, frameName, kmlTimeString, nextKmlTimeString, imageName, frameBounds);

			}

			// Metadata
			if (model.isAddLegendOverlays()) {
				kmlMetaString.append(getMetadataOverlay(frameName+"-Metadata", kmlTimeString, nextKmlTimeString, frameIndex, "meta-"+imageName));
			}

			//Legend
			//        String legendOverlayXY = "x= \"0\" y= \"0\" xunits= \"fraction\" yunits= \"fraction\" ";
			//        String legendScreenXY = "x= \"0\" y= \"0.02\" xunits= \"fraction\" yunits= \"fraction\" ";
			//        String legendRotationXY = "x= \"0\" y= \"0\" xunits= \"fraction\" yunits= \"fraction\" ";
			//        String legendSize = "x= \"-1\" y= \"-1\" xunits= \"pixels\" yunits= \"pixels\" ";
			//        kmlMetaString.append("   <ScreenOverlay> \n");
			//        kmlMetaString.append("      <name>"+name+"-Legend</name> \n");
			//        kmlMetaString.append("      <TimeSpan> \n");
			//        kmlMetaString.append("          <begin>"+kmlTimeString+"</begin> \n");
			//        kmlMetaString.append("          <end>"+nextKmlTimeString+"</end> \n");
			//        kmlMetaString.append("      </TimeSpan> \n");
			//        kmlMetaString.append("      <drawOrder>"+kmlFrameIndex+"</drawOrder> \n");
			//        kmlMetaString.append("      <Icon><href> legend-"+frameName+"</href></Icon> \n");
			//        kmlMetaString.append("      <overlayXY " + legendOverlayXY + " /> \n");
			//        kmlMetaString.append("      <screenXY " + legendScreenXY + " /> \n");
			//        kmlMetaString.append("      <rotationXY " + legendRotationXY + " /> \n");
			//        kmlMetaString.append("      <size " + legendSize + " /> \n");
			//        kmlMetaString.append("   </ScreenOverlay>");



			kmlFrameIndex++;
		}
	}

	/**
	 * Add a 'ScreenOverlay' kml section to the kml document
	 * @param kmlSection
	 */
	public void addScreenOverlay(String kmlSection) {
		kmlMetaString.append(kmlSection);
	}






	private void createShadowImageKML(StringBuffer kmlString, String frameName, 
			String kmlTimeString, String nextKmlTimeString, String imageName, 
			Rectangle2D.Double frameBounds) {

		kmlString.append("   <GroundOverlay> \n");
		kmlString.append("       <name>"+frameName+"_shadow</name> \n");
		if (kmlTimeString != null) {
			kmlString.append("       <TimeSpan> \n");
			kmlString.append("           <begin>"+kmlTimeString+"</begin> \n");
			kmlString.append("           <end>"+nextKmlTimeString+"</end> \n");
			kmlString.append("       </TimeSpan> \n");
		}
		kmlString.append("       <drawOrder>"+kmlFrameIndex+"</drawOrder> \n");
		kmlString.append("       <color>4b000000</color> \n");
		kmlString.append("       <Icon> \n");
		kmlString.append("           <href>"+imageName+"</href> \n");
		kmlString.append("           <viewBoundScale>0.75</viewBoundScale> \n");
		kmlString.append("       </Icon> \n");
		kmlString.append("       <LatLonBox> \n");
		kmlString.append("           <north>"+frameBounds.getMaxY()+"</north> \n");
		kmlString.append("           <south>"+frameBounds.getMinY()+"</south> \n");
		kmlString.append("           <east>"+frameBounds.getMaxX()+"</east> \n");
		kmlString.append("           <west>"+frameBounds.getMinX()+"</west> \n");
		kmlString.append("       </LatLonBox> \n");
		kmlString.append("   </GroundOverlay> \n");
	}



	/**
	 * Assembles the KML file, linking all metadata, data and bounding box pieces.
	 */
	public void finishKML() {

		processKMLOverlayList();
		processKMLModelList();

		kmlString.append("   </Folder> \n");


		if (kmlOverlayList.size() == 0 && kmlModelList.size() == 0) {
			return;
		}

		try {
			Rectangle2D.Double extent = 
					(kmlOverlayList.size() > 0) ? kmlOverlayList.get(0).getBounds() :
						kmlModelList.get(0).getBounds();

					addKMLBoundsFolder(extent);
					if (isDrapeOnColladaSelected) {
						//            	addKML3DOutlineBoxFolder(extent);
					}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("COULD NOT ADD CURRENT EXTENT POLYGON TO KML");
		}




		kmlString.append(getKMLScreenOverlayFolder());

		kmlString.append("   </Folder> \n");
		kmlString.append(" </Document> \n");
		kmlString.append("</kml> \n");       
	}


	/**
	 * Gets the KML text - should be called after 'finishKML' to get a complete file.
	 * @return
	 */
	public String getKML() {
		return kmlString.toString();
	}



	private String getKMLScreenOverlayFolder() {        
		//Screen Overlay Variables
		String logo = "logo.gif";

		//NOAA logo

		StringBuffer sb = new StringBuffer();

		//Screen Overlays
		sb.append("<Folder> \n");
		sb.append("   <name>Overlays</name> \n");
		sb.append("   <visibility>0</visibility> \n");
		sb.append("      <description><![CDATA[" +
				"Legend and Logo Controls:  " +
				"To adjust transparency of just the legend and logo, " +
				"select this folder and adjust transparency slider. ]]></description> \n");
		sb.append("   <ScreenOverlay> \n");
		sb.append("      <name>Logo</name> \n");
		sb.append("      <Icon><href>"+logo+"</href></Icon> \n");
		sb.append(logoLocationKML + "\n");
		sb.append("   </ScreenOverlay> \n");
		sb.append(kmlMetaString);
		sb.append("</Folder> \n");

		return sb.toString();

	}

	//
	//    /**
	//     * Returns legend image.  Returns null if none available.
	//     * @return
	//     * @throws WCTException 
	//     */
	//    public BufferedImage getMetaBufferedImage() throws WCTException  {
	//    	return getMetaBufferedImage(false);
	//    }
	//
	//    /**
	//     * Returns legend image.  Returns null if none available.
	//     * @return
	//     * @throws WCTException 
	//     */
	//    public BufferedImage getMetaBufferedImage(boolean isRadialVolume) throws WCTException  {
	//
	//        if (viewer.getGridSatelliteGridCoverage() == null && viewer.getRadarGridCoverage() == null) {
	//            return null;
	//        }
	//        
	//        CategoryLegendImageProducer legend = viewer.getLastDecodedLegendImageProducer();
	//        Color tmpColor = legend.getBackgroundColor();
	//        boolean tmpDrawBorder = legend.isDrawBorder();
	//
	//        legend.setBackgroundColor(new Color(tmpColor.getRed(), tmpColor.getGreen(), tmpColor.getBlue(), 230));
	//        legend.setDrawBorder(true);
	//
	//        if (isRadialVolume) {
	//        	legend.setSpecialMetadata(new String[] { "RADIAL VOLUME SCAN", " " });
	//        }
	//        
	//
	//        BufferedImage bimage;
	//        if (viewer.getFileScanner() != null &&
	//        	(viewer.getFileScanner().getLastScanResult().getDataType() == SupportedDataType.GOES_SATELLITE_AREA_FORMAT
	//        	|| viewer.getFileScanner().getLastScanResult().getDataType() == SupportedDataType.GRIDDED)) {
	//            legend.setInterpolateBetweenCategories(true);
	//            bimage = legend.createMediumLegendImage();
	//        }
	//        else {
	//            bimage = legend.createLargeLegendImage(new Dimension(180, 450));
	//        }
	//
	//        legend.setBackgroundColor(tmpColor);
	//        legend.setDrawBorder(tmpDrawBorder);
	//
	//        return bimage;
	//
	//    }



	public void addKMZImageEntry(String frameName, BufferedImage buffImage, ZipOutputStream kmzOut) 
			throws IOException {

		kmzOut.putNextEntry(new ZipEntry(frameName));

		ImageIO.write(buffImage, "png", kmzOut);

		//   Complete the image frame entry
		kmzOut.closeEntry();

		buffImage = null;
	}

	public void addKMZDaeEntry(String daeName, String imageName, ZipOutputStream kmzOut) 
			throws IOException, MismatchedDimensionException, FactoryException, TransformException, WCTException {

		kmzOut.putNextEntry(new ZipEntry(daeName));

		kmzOut.write(WCTColladaUtils.getColladaDAE(
				kmlModelList.get(0).getOriginLat(), kmlModelList.get(0).getOriginLon(),
				radialElevAngle, kmlModelList.get(0).getBounds(),
				imageName, elevationExaggeration).getBytes());

		//   Complete the image frame entry
		kmzOut.closeEntry();
	}


	/**
	 * Writes out the KML as a zip entry called 'wct.kml' in the supplied KMZ ZipOutputStream.
	 * To be called AFTER .finishKML();
	 * @param kmzOut
	 * @throws IOException
	 */
	public void writeKML(ZipOutputStream kmzOut) throws IOException {
		// Write KML
		kmzOut.putNextEntry(new ZipEntry("wct.kml"));
		byte[] kmlBytes = getKML().getBytes();
		kmzOut.write(kmlBytes, 0, kmlBytes.length);
		kmzOut.closeEntry();
	}


	public void setRadialElevAngle(double radialElevAngle) {
		this.radialElevAngle = radialElevAngle;
	}

	public double getRadialElevAngle() {
		return this.radialElevAngle;
	}


	/**
	 * Get a BufferedImage of just the data layer(s) from the a new, fresh, empty map pane 
	 * based on the current viewer's map pane.
	 * @return
	 */
	//    public BufferedImage getCustomMapPaneBufferedImage(GridCoverage gc) throws WCTException {
	//    	
	//    	RenderedImage rimg = gc.getRenderedImage();
	//        int width = rimg.getData().getWidth();
	//        int height = rimg.getData().getHeight();
	//        RenderedGridCoverage rgc = new RenderedGridCoverage(gc);
	//        
	////        RenderedGridCoverage viewerRadarRGC = viewer.getRadarRenderedGridCoverage();
	////        RenderedGridCoverage viewerGridSatelliteRGC = viewer.getGridSatelliteRenderedGridCoverage();
	//        
	//        
	//        
	////        if (viewerRadarRGC.isVisible() && viewer.getNexradHeader() != null && 
	////                viewer.getNexradHeader().getProductType() == NexradHeader.L3VAD && 
	////                viewer.getNexradHeader().getProductCode() == NexradHeader.L3PC_VERTICAL_WIND_PROFILE) {
	////            throw new WCTException("KMZ Export of the Level-III VAD product is not supported.");
	////        }
	//        
	//        
	//        BufferedImage bimage = new BufferedImage(
	//                width, height, 
	//                BufferedImage.TYPE_INT_ARGB);
	//        Graphics2D g = bimage.createGraphics();
	//        
	//        
	//        WCTMapPane wctMapPane = new WCTMapPane();
	//        wctMapPane.setBackground(new Color(0, 0, 0, 0));
	//        wctMapPane.setMagnifierGlass(wctMapPane.getBackground());
	//        wctMapPane.setDoubleBuffered(true);
	//        wctMapPane.setBounds(new Rectangle(width, height));
	//        
	//
	////        g.setComposite(AlphaComposite.Clear);
	////        g.fillRect(0, 0, bimage.getWidth(), bimage.getHeight());
	////        g.setComposite(AlphaComposite.Src);
	//        
	//        g.setColor(new Color(0, 0, 0, 0));
	//        g.fillRect(0, 0, bimage.getWidth(), bimage.getHeight());        
	////        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	//
	//        
	//
	////        RenderedGridCoverage rgc = null;
	////        if (viewerRadarRGC.isVisible()) {
	////            rgc = new RenderedGridCoverage(viewerRadarRGC.getGridCoverage());
	////            rgc.setZOrder(viewerRadarRGC.getZOrder());
	////        }
	////        if (viewerGridSatelliteRGC.isVisible()) {
	////            rgc = new RenderedGridCoverage(viewer.getGridSatelliteGridCoverage());
	////            rgc.setZOrder(viewerGridSatelliteRGC.getZOrder());
	////        }
	//
	//        if (rgc != null) {
	//            ((StyledMapRenderer) wctMapPane.getRenderer()).addLayer(rgc);
	//        }
	//        
	//        // add snapshot layers?
	////        if (includeSnapshotLayers) {
	////        	for (SnapshotLayer sl : viewer.getSnapshotLayers()) {
	////        	if (sl.getRenderedGridCoverage().isVisible()) {
	////        	RenderedGridCoverage snapshotRGC = new RenderedGridCoverage(sl.getRenderedGridCoverage().getGridCoverage());
	////        	snapshotRGC.setZOrder(sl.getRenderedGridCoverage().getZOrder());
	////            
	////        	((StyledMapRenderer) wctMapPane.getRenderer()).addLayer(snapshotRGC);
	////        	}
	////        	}
	////        }
	//        
	//        
	//        wctMapPane.setPreferredSize(new Dimension(width, height));
	//        wctMapPane.setVisibleArea(gc.getGridGeometry().getEnvelope().toRectangle2D());
	//        
	//        
	//        
	////        JFrame frame = new JFrame("Map testing frame");
	////        JPanel wctMapPanel = new JPanel(new BorderLayout());
	////        wctMapPanel.add(wctMapPane, BorderLayout.CENTER);
	////        frame.add(wctMapPanel);
	////        frame.pack();
	////        frame.setVisible(true);
	//        
	//        
	////        System.out.println("wctZoomableBounds: "+wctMapPane.getWCTZoomableBounds(null));
	//        
	////        ((StyledMapRenderer) wctMapPane.getRenderer()).paint(g, 
	////                wctMapPane.getWCTZoomableBounds(null), viewer.getMapPane().getZoom(), true);
	//        
	////        wctMapPanel.paint(g);
	//        
	//        
	//        g.dispose();
	//
	//        for (RenderedLayer l : ((StyledMapRenderer) wctMapPane.getRenderer()).getLayers()) {
	//            l.dispose();
	//        }
	//
	//        
	//        if (rgc != null) {
	//            ((StyledMapRenderer) wctMapPane.getRenderer()).removeAllLayers();
	//            try {
	//                rgc.getGridCoverage().dispose();
	//            } catch (Exception e) {
	//                ;
	//            }
	//            rgc.dispose();
	//        }
	//
	//        wctMapPane = null;
	//
	//        return bimage;
	//    }





	/**
	 * THIS IS NOW WORKING - submission from user with fix
	 * @param gc
	 * @return
	 */
	public static BufferedImage getBufferedImage(GridCoverage gc) {
		RenderedImage rimg = gc.getRenderedImage();
		int width = rimg.getData().getWidth();
		int height = rimg.getData().getHeight();
		Dimension size = new Dimension(width, height);
		rimg = null;

		return getBufferedImage(gc, WCTUtils.toRectangle(gc.getEnvelope()), size);
	}

	/**
	 * THIS IS NOW WORKING - submission from user with fix
	 * @param gc
	 * @return
	 */
	public static BufferedImage getBufferedImage(GridCoverage gc, Rectangle2D bounds, Dimension size) {

		RenderedGridCoverage rgc = new RenderedGridCoverage(gc);
		gc.prefetch(gc.getEnvelope().toRectangle2D());
		rgc.repaint();

//		Rectangle2D bounds = rgc.getPreferredArea();

		BufferedImage bimage = new BufferedImage((int)Math.round(size.getWidth()), (int)Math.round(size.getHeight()), 
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bimage.createGraphics();
		//        g.drawImage(rimg., 0, 0, null);

		WCTMapPane wctMapPane = new WCTMapPane();

		wctMapPane.setBackground(new Color(0, 0, 0, 0));
		wctMapPane.setMagnifierGlass(wctMapPane.getBackground());
		wctMapPane.setDoubleBuffered(true);

		((StyledMapRenderer) wctMapPane.getRenderer()).addLayer(rgc);

//		wctMapPane.setBounds(bounds.getBounds());
		g.setColor(new Color(0, 0, 0, 0));
		g.fillRect(0, 0, bimage.getWidth(), bimage.getHeight());

		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		wctMapPane.setVisibleArea(bounds);


		//        ((StyledMapRenderer) wctMapPane.getRenderer()).paint(g, 
		//                wctMapPane.getWCTZoomableBounds(null), t, true);


		//        Following is submission from user

		Rectangle2D dest = new Rectangle2D.Float(0, 0, bimage.getWidth(),
				bimage.getHeight());
		AffineTransform orig = new AffineTransform();
		orig.setToScale(+1, -1);
		// orig.setToIdentity();
		AffineTransform t = setVisibleArea(orig, bounds, dest, SCALE_X
				| SCALE_Y | TRANSLATE_X | TRANSLATE_Y);

		orig.concatenate(t);
		((StyledMapRenderer) wctMapPane.getRenderer()).paint(g,
				wctMapPane.getWCTZoomableBounds(null), orig, true);





		g.dispose();

		//        AWTImageExport.saveImage(bimage, new File("kmzout"+millis), AWTImageExport.Type.PNG);

		wctMapPane = null;
		rgc = null;
		gc = null;

		return bimage;


	}






	/**
	 * Defines the limits of the visible part, in logical coordinates. This
	 * method will modify the zoom and the translation in order to display the
	 * specified region. If {@link #zoom} contains a rotation, this rotation
	 * will not be modified.
	 * 
	 * @param source
	 *            Logical coordinates of the region to be displayed.
	 * @param dest
	 *            Pixel coordinates of the region of the window in which to draw
	 *            (normally {@link #getZoomableBounds()}).
	 * @param mask
	 *            A mask to <code>OR</code> with the {@link #type} for
	 *            determining which kind of transformation are allowed. The
	 *            {@link #type} is not modified.
	 * @return Change to apply to the affine transform {@link #zoom}.
	 * @throws IllegalArgumentException
	 *             if <code>source</code> is empty.
	 */
	private static final int SCALE_X = (1 << 0);
	private static final int SCALE_Y = (1 << 1);
	private static final int TRANSLATE_X = (1 << 3);
	private static final int TRANSLATE_Y = (1 << 4);
	private static final int UNIFORM_SCALE = SCALE_X | SCALE_Y | (1 << 2);
	private static final int RESET = (1 << 6);
	public static final int DEFAULT_ZOOM = (1 << 7);
	public static final int ROTATE = (1 << 5);
	private static final int MASK = SCALE_X | SCALE_Y | UNIFORM_SCALE
			| TRANSLATE_X | TRANSLATE_Y | ROTATE | RESET | DEFAULT_ZOOM;

	private static AffineTransform setVisibleArea(AffineTransform orig,
			Rectangle2D source, Rectangle2D dest, int mask)
					throws IllegalArgumentException {
		/*
		 * Verifies the validity of the rectangle <code>source</code>. An
		 * invalid rectangle will be rejected. However, we will be more flexible
		 * for <code>dest</code> since the window could have been reduced by the
		 * user.
		 */

		if (!isValid(source)) {
			throw new IllegalArgumentException(Resources.format(
					ResourceKeys.ERROR_BAD_RECTANGLE_$1, source));
		}
		if (!isValid(dest)) {
			return new AffineTransform();
		}
		/*
		 * Converts the destination into logical coordinates. We can then
		 * perform a zoom and a translation which would put <code>source</code>
		 * in <code>dest</code>.
		 */
		try {
			dest = XAffineTransform.inverseTransform(orig, dest, null);
		} catch (NoninvertibleTransformException exception) {
			// unexpectedException("setVisibleArea", exception);
			return new AffineTransform();
		}
		final double sourceWidth = source.getWidth();
		final double sourceHeight = source.getHeight();
		final double destWidth = dest.getWidth();
		final double destHeight = dest.getHeight();
		double sx = destWidth / sourceWidth;
		double sy = destHeight / sourceHeight;
		/*
		 * Standardizes the horizontal and vertical scales, if such a
		 * standardization has been requested.
		 */
		// mask |= type;
		// mask |= RESET;
		mask = 223;
		if ((mask & UNIFORM_SCALE) == UNIFORM_SCALE) {
			if (true) {
				if (sy * sourceWidth > destWidth) {
					sx = sy;
				} else if (sx * sourceHeight > destHeight) {
					sy = sx;
				}
			} else {
				if (sy * sourceWidth < destWidth) {
					sx = sy;
				} else if (sx * sourceHeight < destHeight) {
					sy = sx;
				}
			}
		}
		final AffineTransform change = AffineTransform.getTranslateInstance(
				(mask & TRANSLATE_X) != 0 ? dest.getCenterX() : 0,
						(mask & TRANSLATE_Y) != 0 ? dest.getCenterY() : 0);
		change.scale((mask & SCALE_X) != 0 ? sx : 1, (mask & SCALE_Y) != 0 ? sy
				: 1);
		change.translate((mask & TRANSLATE_X) != 0 ? -source.getCenterX() : 0,
				(mask & TRANSLATE_Y) != 0 ? -source.getCenterY() : 0);
		XAffineTransform.round(change);
		return change;
	}

	private static boolean isValid(final Rectangle2D rect) {
		if (rect == null) {
			return false;
		}
		final double x = rect.getX();
		final double y = rect.getY();
		final double w = rect.getWidth();
		final double h = rect.getHeight();
		return (x > Double.NEGATIVE_INFINITY && x < Double.POSITIVE_INFINITY
				&& y > Double.NEGATIVE_INFINITY && y < Double.POSITIVE_INFINITY
				&& w > 0 && w < Double.POSITIVE_INFINITY && h > 0 && h < Double.POSITIVE_INFINITY);
	}

	//    
	//    
	//    public double getGridAltitude(int zIndex) throws IllegalAccessException, InstantiationException {
	//        if (viewer.getGridDatasetRaster().getLastProcessedGridCoordSystem().getVerticalAxis() == null) {
	//            return Double.NaN;
	//        }
	//        
	//        String vertUnits = viewer.getGridDatasetRaster().getLastProcessedGridCoordSystem().getVerticalAxis().getUnitsString();
	//        if (vertUnits.equalsIgnoreCase("m") || vertUnits.equalsIgnoreCase("meter") || vertUnits.equalsIgnoreCase("meters")) {
	//            return viewer.getGridDatasetRaster().getLastProcessedGridCoordSystem().getVerticalAxis().getCoordValue(zIndex);
	//        }
	//        else if (vertUnits.equalsIgnoreCase("Pa")){
	//            double pressureInPa = viewer.getGridDatasetRaster().getLastProcessedGridCoordSystem().getVerticalAxis().getCoordValue(zIndex);
	//            return NexradEquations.getAltitudeFromPressureInMeters(pressureInPa);
	//
	////            System.out.println("pressure in: "+pressureInPa+" Pa, altitude out: "+this.altitude);
	//        }
	//        else {
	//            return Double.NaN;
	//        }
	//
	//    }




	//    private static BufferedImage convertRGBAToIndexed(BufferedImage src) {
	//        BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_INDEXED);
	//        Graphics g = dest.getGraphics();
	//        g.setColor(new Color(231,20,189));
	//        g.fillRect(0, 0, dest.getWidth(), dest.getHeight()); //fill with a hideous color and make it transparent
	//        dest = makeTransparent(dest,0,0);
	//        dest.createGraphics().drawImage(src,0,0, null);
	//        return dest;
	//    }
	//
	//    private static BufferedImage makeTransparent(BufferedImage image, int x, int y) {
	//        ColorModel cm = image.getColorModel();
	//        if (!(cm instanceof IndexColorModel))
	//            return image; //sorry...
	//        IndexColorModel icm = (IndexColorModel) cm;
	//        WritableRaster raster = image.getRaster();
	//        int pixel = raster.getSample(x, y, 0); //pixel is offset in ICM's palette
	//        int size = icm.getMapSize();
	//        byte[] reds = new byte[size];
	//        byte[] greens = new byte[size];
	//        byte[] blues = new byte[size];
	//        icm.getReds(reds);
	//        icm.getGreens(greens);
	//        icm.getBlues(blues);
	//        IndexColorModel icm2 = new IndexColorModel(8, size, reds, greens, blues, pixel);
	//        return new BufferedImage(icm2, raster, image.isAlphaPremultiplied(), null);
	//    }



	public void setAltMode(AltitudeMode altMode) {
		this.altMode = altMode;
	}
	public AltitudeMode getAltMode() {
		return altMode;
	}
	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}
	public double getAltitude() {
		return altitude;
	}
	public void setCreateShadowImages(boolean createShadowImages) {
		this.createShadowImages = createShadowImages;
	}
	public boolean isCreateShadowImages() {
		return createShadowImages;
	}
	public void setDrapeOnColladaSelected(boolean isDrapeOnColladaSelected) {
		this.isDrapeOnColladaSelected = isDrapeOnColladaSelected;
	}
	public boolean isDrapeOnColladaSelected() {
		return this.isDrapeOnColladaSelected;
	}
	public void setElevationExaggeration(int elevationExaggeration) {
		this.elevationExaggeration = elevationExaggeration;
	}
	public int getElevationExaggeration() {
		return elevationExaggeration;
	}




	private static <E> long calculateAverageTimestep(ArrayList<E> kmlElementList) {

		if (kmlElementList.size() < 2) {
			return 0;
		}

		// 1. get unique dates
		ArrayList<Date> uniqueDateList = new ArrayList<Date>();
		for (KMLElement kmlElement : (ArrayList<KMLElement>)kmlElementList) {
			if (! uniqueDateList.contains(kmlElement.getTimestamp())) {
				uniqueDateList.add(kmlElement.getTimestamp());
			}
		}

		if (uniqueDateList.size() < 2) {
			return 0;
		}

		// 2. calculate average timestep

		// find average time difference between layers and use that to calculate the last time step
		long timeDiffTotal = 0;
		for (int n=1; n<uniqueDateList.size(); n++) {
			//            System.out.println("time diff: "+(kmlOverlayList.get(n).getTimestamp().getTime()-kmlOverlayList.get(n-1).getTimestamp().getTime()));
			timeDiffTotal += uniqueDateList.get(n).getTime()-uniqueDateList.get(n-1).getTime();
			//            System.out.println("time diff total: "+timeDiffTotal);
		}

		return Math.round(timeDiffTotal/(uniqueDateList.size()-1));
	}

	public void setOriginLat(double originLat) {
		this.originLat = originLat;
	}

	public double getOriginLat() {
		return originLat;
	}

	public void setOriginLon(double originLon) {
		this.originLon = originLon;
	}

	public double getOriginLon() {
		return originLon;
	}

	public void setOriginAltInMeters(double originAltInMeters) {
		this.originAltInMeters = originAltInMeters;
	}

	public double getOriginAltInMeters() {
		return originAltInMeters;
	}


	public String getLegendLocationKML() {
		return legendLocationKML;
	}
	public void setLegendLocationKML(String legendLocationKML) {
		this.legendLocationKML = legendLocationKML;
	}
	public String getLogoLocationKML() {
		return logoLocationKML;
	}
	public void setLogoLocationKML(String logoLocationKML) {
		this.logoLocationKML = logoLocationKML;
	}

























	private interface KMLElement {
		public Date getTimestamp();
		public Date getEndTimestamp();
	}

	private class KMLModel implements KMLElement {
		private String frameName;
		private java.awt.geom.Rectangle2D.Double bounds;
		private double alt;
		private int frameIndex;
		private String imageName;
		private Date timestamp;
		private Date endTimestamp;
		private boolean addLegendOverlays;
		private double originLat;
		private double originLon;
		private double originAltInMeters;


		public void setFrameName(String frameName) {
			this.frameName = frameName;
		}
		public String getFrameName() {
			return frameName;
		}
		public void setBounds(java.awt.geom.Rectangle2D.Double bounds) {
			this.bounds = bounds;
		}
		public java.awt.geom.Rectangle2D.Double getBounds() {
			return bounds;
		}
		public void setAltitude(double alt) {
			this.alt = alt;
		}
		public double getAltitude() {
			return alt;
		}
		public void setFrameIndex(int frameIndex) {
			this.frameIndex = frameIndex;
		}
		public int getFrameIndex() {
			return frameIndex;
		}
		public void setImageName(String imageName) {
			this.imageName = imageName;
		}
		public String getImageName() {
			return imageName;
		}
		public void setTimestamp(Date timestamp) {
			this.timestamp = timestamp;
		}
		@Override
		public Date getTimestamp() {
			return timestamp;
		}
		public void setEndTimestamp(Date endTimestamp) {
			this.endTimestamp = endTimestamp;
		}
		@Override
		public Date getEndTimestamp() {
			return endTimestamp;
		}
		public void setAddLegendOverlays(boolean addLegendOverlays) {
			this.addLegendOverlays = addLegendOverlays;
		}
		public boolean isAddLegendOverlays() {
			return addLegendOverlays;
		}
		public void setOriginLat(double originLat) {
			this.originLat = originLat;
		}
		public double getOriginLat() {
			return originLat;
		}
		public void setOriginLon(double originLon) {
			this.originLon = originLon;
		}
		public double getOriginLon() {
			return originLon;
		}
		public void setOriginAltInMeters(double originAltInMeters) {
			this.originAltInMeters = originAltInMeters;
		}
		public double getOriginAltInMeters() {
			return originAltInMeters;
		}
	}


	private class KMLGroundOverlay implements KMLElement {


		private String frameName;
		private java.awt.geom.Rectangle2D.Double bounds;
		private AltitudeMode altitudeMode;
		private double alt;
		private int frameIndex;
		private String imageName;
		private Date timestamp;
		private Date endTimestamp;
		private boolean addLegendOverlays;


		public void setFrameName(String frameName) {
			this.frameName = frameName;
		}
		public String getFrameName() {
			return this.frameName;
		}

		public void setBounds(Rectangle2D.Double bounds) {
			this.bounds = bounds;
		}
		public Rectangle2D.Double getBounds() {
			return this.bounds;
		}

		public void setAltitudeMode(AltitudeMode altMode) {
			this.altitudeMode = altMode;
		}
		public AltitudeMode getAltitudeMode() {
			return this.altitudeMode;
		}

		public void setAltitude(double altitude) {
			this.alt = altitude;
		}
		public double getAltitude() {
			return this.alt;
		}

		public void setFrameIndex(int kmlFrameIndex) {
			this.frameIndex = kmlFrameIndex;
		}
		public int getFrameIndex() {
			return this.frameIndex;
		}

		public void setImageName(String imageName) {
			this.imageName = imageName;
		}
		public String getImageName() {
			return this.imageName;
		}

		public void setTimestamp(Date startTime) {
			this.timestamp = startTime;
		}
		@Override
		public Date getTimestamp() {
			return this.timestamp;
		}
		public void setEndTimestamp(Date endTimestamp) {
			this.endTimestamp = endTimestamp;
		}
		@Override
		public Date getEndTimestamp() {
			return endTimestamp;
		}
		public void setAddLegendOverlays(boolean addLegendOverlays) {
			this.addLegendOverlays = addLegendOverlays;
		}
		public boolean isAddLegendOverlays() {
			return addLegendOverlays;
		}

	}
}