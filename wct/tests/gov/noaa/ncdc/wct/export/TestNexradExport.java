package gov.noaa.ncdc.wct.export;

import gov.noaa.ncdc.wct.WCTFilter;
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.DecodeHintNotSupportedException;
import gov.noaa.ncdc.wct.decoders.cdm.DecodeRadialDatasetSweep;
import gov.noaa.ncdc.wct.decoders.cdm.DecodeRadialDatasetSweepHeader;
import gov.noaa.ncdc.wct.decoders.nexrad.Level2Transfer;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradUtilities;
import gov.noaa.ncdc.wct.export.raster.FeatureRasterizerException;
import gov.noaa.ncdc.wct.export.raster.WCTGridCoverageSupport;
import gov.noaa.ncdc.wct.export.raster.WCTRasterExport;
import gov.noaa.ncdc.wct.export.raster.WCTRasterizer;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Formatter;

import org.geotools.feature.FeatureCollection;
import org.junit.Test;

import ucar.ma2.InvalidRangeException;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dt.RadialDatasetSweep;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.util.CancelTask;

public class TestNexradExport {



    @Test
    public void testLevel2Export() 
        throws FeatureRasterizerException, IOException, ParseException, DecodeHintNotSupportedException, 
            DecodeException, WCTExportNoDataException, InvalidRangeException {

        /*         
     java.net.URL url =
           new File("C:\\ViewerData\\HAS999900001\\7000KCLE_SDUS51_N0RCLE_200211102351").toURL();
     gov.noaa.ncdc.nexradexport.decoders.DecodeL3Header header =
           new gov.noaa.ncdc.nexradexport.decoders.DecodeL3Header();
     header.decodeHeader(url);
     gov.noaa.ncdc.nexradexport.decoders.DecodeL3Nexrad data =
           new gov.noaa.ncdc.nexradexport.decoders.DecodeL3Nexrad(header);
     java.awt.geom.Rectangle2D.Double bounds = header.getNexradBounds(); 
     data.decodeData();
         */         


        java.net.URL url =
            new File("H:\\Nexrad_Viewer_Test\\Korea\\data\\RKSG20060419_000308.Z").toURI().toURL();
        url = Level2Transfer.getNCDCLevel2UNIXZ(url);
        
        CancelTask emptyCancelTask = new CancelTask() {
			@Override
            public boolean isCancel() {
                return false;
            }
			@Override
            public void setError(String arg0) {
            }
			@Override
			public void setProgress(String arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}
        };
        
        RadialDatasetSweep radialDataset = (RadialDatasetSweep) FeatureDatasetFactoryManager.open(
                FeatureType.RADIAL, 
                url.toString(), emptyCancelTask, new Formatter());

        DecodeRadialDatasetSweepHeader header = new DecodeRadialDatasetSweepHeader();
        header.setRadialDatasetSweep(radialDataset);
        DecodeRadialDatasetSweep decoder = new DecodeRadialDatasetSweep((DecodeRadialDatasetSweepHeader)header);
        
        decoder.setRadialVariable(
                (RadialDatasetSweep.RadialVariable) radialDataset.getDataVariable("Reflectivity")
                );
        decoder.setDecodeHint("attributes", DecodeRadialDatasetSweep.EXPORT_POLY_ATTRIBUTES);               
        decoder.setDecodeHint("classify", new Boolean(false));
        decoder.setDecodeHint("nexradFilter", new WCTFilter());
        decoder.setDecodeHint("startSweep", new Integer(0));
        decoder.setDecodeHint("endSweep", new Integer(0));
        decoder.decodeData();

        


        //File f = new File("D:\\asctest\\nex2asc.asc");
        File f = new File("H:\\Nexrad_Viewer_Test\\1.1.0\\VelocitySmoothing\\velo-1.nc");
        //File f = new File("D:\\Nexrad_Viewer_Test\\NetCDF\\geotiffex2");

        long startTime = System.currentTimeMillis();
        WCTRasterizer rasterizer = new WCTRasterizer();
        System.out.println("CONSTRUCTED OBJECT IN " + (System.currentTimeMillis() - startTime) + " MILLISECONDS");

        //for (int i=0; i<20; i++) {

        java.awt.geom.Rectangle2D.Double bounds = header.getNexradBounds(); 
        FeatureCollection fc = decoder.getFeatures();   

        double x = fc.getBounds().getMinX();
        double y = fc.getBounds().getMinY();
        double width = fc.getBounds().getWidth();
        double height = fc.getBounds().getHeight();
        bounds = new java.awt.geom.Rectangle2D.Double(x, y, width, height);            

        rasterizer.setLongName(NexradUtilities.getLongName(header));
        rasterizer.setUnits(NexradUtilities.getUnits(header));
        rasterizer.setDateInMilliseconds(header.getMilliseconds());
        rasterizer.rasterize(fc, bounds, "value");

        System.out.println("RASTERIZED AND SMOOTHED " + decoder.getFeatures().size() + " FEATURES IN " + (System.currentTimeMillis() - startTime) + " MILLISECONDS (total)");

        WCTGridCoverageSupport gcSupport = new WCTGridCoverageSupport();
        
        rasterizer.setWritableRaster(gcSupport.getSmoothedRaster(rasterizer, 8, 0.0, new double[0]));



        System.out.println("RASTERIZED " + decoder.getFeatures().size() + " FEATURES IN " + (System.currentTimeMillis() - startTime) + " MILLISECONDS (total)");

        File outFile = new File(f+".tif");

        WCTRasterExport rasterExport = new WCTRasterExport();

        //rasterExport.saveAsciiGrid(f, rasterizer);
        rasterExport.saveNetCDF(outFile, rasterizer);
        //rasterExport.saveGeoTIFF(outFile, rasterizer);
        System.out.println("SAVED RASTER IN " + (System.currentTimeMillis() - startTime) + " MILLISECONDS (total)");


        //}


    }

}
