package steve.test.collada;

import gov.noaa.ncdc.gis.collada.ColladaCalculator;
import gov.noaa.ncdc.gis.collada.ColladaWriter;
import gov.noaa.ncdc.wct.decoders.ColorsAndValues;
import gov.noaa.ncdc.wct.decoders.cdm.GridDatasetColorFactory;
import gov.noaa.ncdc.wct.decoders.cdm.GridDatasetRemappedRaster;
import gov.noaa.ncdc.wct.export.BatchKmzUtilities;
import gov.noaa.ncdc.wct.ui.animation.ExportKMZThread;
import gov.noaa.ncdc.wct.ui.ge.WCTColladaUtils;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import org.geotools.gc.GridCoverage;

import com.vividsolutions.jts.geom.Coordinate;

public class BumpySurface {

	

	public static void main(String[] args) {
		try {
			process(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void process(String[] args) throws Exception {

		String source = "E:\\work\\gridsat\\GRIDSAT-USA.2005.08.29.00.v01.nc";
		
//        StringBuilder errlog = new StringBuilder();
//        GridDataset gds = GridDatasetUtils.openGridDataset(source, errlog);
//        if (gds.getGrids().size() == 0) {
//        	gds.close();
//            throw new DecodeException("No Grids found in file.  Check CF-compliance for gridded data");
//        }

		
        String heightVar = "cldhgt";
        String dataVar = "irwin";
		
        GridDatasetRemappedRaster gdrr = new GridDatasetRemappedRaster();
        gdrr.setVariableName(dataVar);
        gdrr.process(source);
        
//		ColorsAndValues cav = GridDatasetColorFactory.getColorsAndValues("Infrared Window (Default)");
		ColorsAndValues cav = GridDatasetColorFactory.getColorsAndValues("McIDAS_TSTORM1.ET");
		gdrr.setAutoMinMaxValues(false);
		gdrr.setDisplayColors(cav.getColors());
		gdrr.setDisplayMinValue(cav.getValues()[0]);
		gdrr.setDisplayMaxValue(cav.getValues()[cav.getValues().length-1]);
        
        GridCoverage gc = gdrr.getGridCoverage();

        
        
        
        

        
		
        
        
        
        
        
        
        
		BatchKmzUtilities utils = new BatchKmzUtilities();

		File outFile = new File("bumpysurface.kmz");
		Dimension size = new Dimension(gc.getRenderedImage().getWidth(), gc.getRenderedImage().getHeight());
		
		utils.setSize(size);
		
		Rectangle2D.Double extent = new Rectangle2D.Double(
				gc.getEnvelope().getMinimum(0),
				gc.getEnvelope().getMinimum(1),
				gc.getEnvelope().getLength(0),
				gc.getEnvelope().getLength(1));
		utils.setExtent(extent); 
		
		
//		utils.initKML();
//		
//		utils.addKMLBoundsFolder(utils.getExtent());
		
//		utils.addKML3DOutlineBoxFolder(utils.getExtent());
		
		// Create the ZIP file
        ZipOutputStream kmzOut = new ZipOutputStream(new FileOutputStream(outFile));
      
        BufferedImage image = BatchKmzUtilities.createImage(gc, size);
        
        String filename = outFile.getName().substring(0, outFile.getName().length()-4);
//		utils.addFrameToKMZ(kmzOut, image,
//				filename+".png", filename+".png", null, null, utils.getExtent(), 
//				utils.getAltMode(), 0.0, true);
		
		
//    	utils.addKMZImageEntry("meta-"+filename+".png", 
//    			legendProducer.createLargeLegendImage(new Dimension(180, 500)), kmzOut);


        
        

		
        // Write Image file
        kmzOut.putNextEntry(new ZipEntry(filename+".png"));
        ImageIO.write(image, "png", kmzOut);
        kmzOut.closeEntry();
        image = null;

		
		
        System.out.println(gdrr.getGridMinValue()+" , "+gdrr.getGridMaxValue());

        // Process height variable
        gdrr.setVariableName(heightVar);
        gdrr.process(source);
        System.out.println(gdrr.getGridMinValue()+" , "+gdrr.getGridMaxValue());

        // Write COLLADA .dae file
        Coordinate[][] coords = WCTColladaUtils.generateGridVariableHeightCoordinateArray(gdrr, 120, 100, 100);
		ColladaCalculator collada = new ColladaCalculator();
		collada.loadData(coords);
		ColladaWriter writer = new ColladaWriter();
		String xml = writer.createColladaXML(collada, filename+".png");
        kmzOut.putNextEntry(new ZipEntry(filename+".dae"));
        kmzOut.write(xml.getBytes());

        kmzOut.closeEntry();

		
		
        
        
        
		
		
		
		
//		utils.finishKML();
		
        // Write KML file
        
        
        String title = "Bumpy Surface example";
        String desc = "Bumpy Surface example";
		
		
		StringBuilder sb = new StringBuilder();

		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n");
		sb.append("<kml xmlns=\"http://earth.google.com/kml/2.2\"> \n");
		sb.append("<Document> \n");
		sb.append("		<name>Collada Snapshot</name> \n");
		sb.append("		<visibility>1</visibility> \n");
		sb.append("		<open>1</open> \n");
		sb.append("		<description><![CDATA["+desc+"]]></description> \n");
		sb.append("		<LookAt> \n");
		sb.append("  		<altitudeMode>absolute</altitudeMode> \n");
		sb.append("  		<longitude>"+extent.getCenterX()+"</longitude> \n");
		sb.append("  		<latitude>"+extent.getCenterY()+"</latitude> \n");
		sb.append(" 		<altitude>0</altitude> \n");
		sb.append("  		<range>700000</range> \n");
		sb.append("  		<tilt>0</tilt> \n");
		sb.append("  		<heading>0</heading> \n");
		sb.append(" 	</LookAt> \n");
		
		sb.append(" 	<Placemark> \n");
		sb.append(" 	<name>"+title+"</name> \n");
		sb.append(" 	<visibility>1</visibility> \n");
		sb.append(" 	<Style id=\"default\"></Style> \n");
		sb.append(" 	<LookAt> \n");
		sb.append(" 		<altitudeMode>relativeToGround</altitudeMode> \n");
		sb.append("  		<longitude>"+extent.getCenterX()+"</longitude> \n");
		sb.append("  		<latitude>"+extent.getCenterY()+"</latitude> \n");
		sb.append(" 		<altitude>"+2000+"</altitude> \n");
		sb.append(" 	    <range>300000</range> \n");
		sb.append(" 	    <tilt>65</tilt> \n");
		sb.append(" 	    <heading>0</heading> \n");
		sb.append(" 	</LookAt> \n");
		sb.append(" 	<Model id=\"model_1\"> \n");
		sb.append(" 		<altitudeMode>relativeToGround</altitudeMode> \n");
		sb.append(" 		<Location> \n");
		sb.append("  			<longitude>"+extent.getCenterX()+"</longitude> \n");
		sb.append("  			<latitude>"+extent.getCenterY()+"</latitude> \n");
		sb.append(" 			<altitude>0</altitude> \n");
		sb.append(" 		</Location> \n");
		sb.append(" 		<Orientation> \n");
		sb.append(" 			<heading>0</heading> \n");
		sb.append(" 			<tilt>0</tilt> \n");
		sb.append(" 			<roll>0</roll> \n");
		sb.append(" 		</Orientation> \n");
		sb.append(" 		<Scale> \n");
		sb.append(" 			<x>1</x> \n");
		sb.append(" 			<y>1</y> \n");
		sb.append(" 			<z>1</z> \n");
		sb.append(" 		</Scale> \n");
		sb.append(" 		<Link> \n");
		sb.append(" 			<href>"+filename+".dae</href> \n");
		sb.append(" 		</Link> \n");
		sb.append(" 	</Model> \n");
		sb.append(" </Placemark> \n");

		sb.append("</Document> \n");
		sb.append("</kml> \n");

		
		String kml = sb.toString();		

        
        

        // Write KML
        kmzOut.putNextEntry(new ZipEntry("wct.kml"));
//        byte[] kmlBytes = utils.getKML().getBytes();
        byte[] kmlBytes = kml.getBytes(); 
        kmzOut.write(kmlBytes, 0, kmlBytes.length);
        kmzOut.closeEntry();

        // Copy NOAA logo to KMZ
        kmzOut.putNextEntry(new ZipEntry("noaalogo.gif"));
        // Transfer bytes from the file to the ZIP file
        URL logoURL = ExportKMZThread.class.getResource("/images/noaalogont-20tr.gif");
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

        kmzOut.close();

        
	}
	
	
	
	
}
