package gov.noaa.ncdc.wct.export.raster;

import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.MaxGeographicExtent;
import gov.noaa.ncdc.wct.decoders.StreamingProcess;
import gov.noaa.ncdc.wct.decoders.cdm.DecodeRadialDatasetSweep;
import gov.noaa.ncdc.wct.decoders.cdm.DecodeRadialDatasetSweepHeader;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Header;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.Formatter;

import org.geotools.feature.FeatureCollection;
import org.junit.Test;

import ucar.nc2.constants.FeatureType;
import ucar.nc2.dt.RadialDatasetSweep;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.util.CancelTask;


public class TestWCTRasterizer {

    @Test
    public void compareRasterizer() throws DecodeException, IOException, ParseException {

       
        
        
        URL url = new File("testdata\\KLIX20050829_135451").toURI().toURL();
        
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
			}
        };
        
        RadialDatasetSweep radialDataset = (RadialDatasetSweep) FeatureDatasetFactoryManager.open(
                FeatureType.RADIAL, 
                url.toString(), emptyCancelTask, new Formatter());

        DecodeRadialDatasetSweepHeader header = new DecodeRadialDatasetSweepHeader();
        header.setRadialDatasetSweep(radialDataset);
        
        DecodeRadialDatasetSweep decoder = new DecodeRadialDatasetSweep(header);

        decoder.setRadialVariable(
                (RadialDatasetSweep.RadialVariable) radialDataset.getDataVariable("Reflectivity")
                );

       
        
        
        
        
        FeatureRasterizer featureRasterizer = new FeatureRasterizer();

        featureRasterizer.setBounds(MaxGeographicExtent.getNexradExtent(header));
        
        

        long startTime = System.currentTimeMillis();
        decoder.decodeData(new StreamingProcess[] { featureRasterizer });
        System.out.println("decode with featureRasterizer: "+(System.currentTimeMillis()-startTime));

    }
    
    
    @Test
    public void testRasterExport() {
       try {
          java.net.URL url =
                new java.net.URL("http://www1.ncdc.noaa.gov/pub/has/HAS000112446/7000KAKQ_SDUS51_N0RAKQ_200309190001");
          gov.noaa.ncdc.wct.decoders.nexrad.NexradHeader header =
                new gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Header();
          header.decodeHeader(url);
          gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Nexrad data =
                new gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Nexrad((DecodeL3Header) header);
          java.awt.geom.Rectangle2D.Double bounds = header.getNexradBounds();
          data.decodeData();

          File f = new File("D:\\asctest\\nex2asc.asc");

          long startTime = System.currentTimeMillis();
          WCTRasterizer rasterizer = new WCTRasterizer();
          System.out.println("CONSTRUCTED OBJECT IN " + (System.currentTimeMillis() - startTime) + " MILLISECONDS");

          //for (int i=0; i<20; i++) {

          FeatureCollection fc = data.getFeatures();

          double x = fc.getBounds().getMinX();
          double y = fc.getBounds().getMinY();
          double width = fc.getBounds().getWidth();
          double height = fc.getBounds().getHeight();
          bounds = new java.awt.geom.Rectangle2D.Double(x, y, width, height);

          rasterizer.rasterize(fc, bounds, "colorIndex");
          System.out.println("RASTERIZED " + data.getFeatures().size() + " FEATURES IN " + (System.currentTimeMillis() - startTime) + " MILLISECONDS (total)");

          WCTRasterExport rasterExport = new WCTRasterExport();
          rasterExport.saveAsciiGrid(f, rasterizer);
          System.out.println("SAVED RASTER IN " + (System.currentTimeMillis() - startTime) + " MILLISECONDS (total)");

          //}

          Thread.sleep(5000);

       } catch (Exception e) {
          e.printStackTrace();
       }

    }
}
