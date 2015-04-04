package gov.noaa.ncdc.wct.decoders.goes;

import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.event.DataDecodeEvent;
import gov.noaa.ncdc.wct.event.DataDecodeListener;
import gov.noaa.ncdc.wct.export.raster.WCTRasterExport;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;


public class GoesRemappedRasterTest {

//    @Test
    public void testGoesRemappedRaster() {
        


        try {
            
            String source = 
                "E:\\work\\goes\\katrina\\goes12.2005.241.144513.BAND_04";


            GoesRemappedRaster goes = new GoesRemappedRaster();
            goes.setHeight(500);
            goes.setWidth(500);
            
            Rectangle2D.Double bounds = 
                new Rectangle2D.Double(-102.0, 17.0, 24.0, 24.0);

            goes.process(source, bounds);

            System.out.println("WRITING ASCII Grid");
            WCTRasterExport rasterExport = new WCTRasterExport();
            rasterExport.saveAsciiGrid(new File(source+".asc"), goes, WCTUtils.DECFMT_pDpppp);

            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    
    
    
//    @Test
    public void testGiniRemappedRaster() {
        


        try {
            

            String source = "E:\\work\\satellite\\gini\\EAST-CONUS_4km_IR_20080911_0145.gini";


            GoesRemappedRaster goes = new GoesRemappedRaster();
            goes.setHeight(500);
            goes.setWidth(500);
            goes.addDataDecodeListener(new DataDecodeListener() {

                public void metadataUpdate(DataDecodeEvent decodeEvent) {
                }
                
                public void decodeEnded(DataDecodeEvent event) {
                    System.out.println("DECODE ENDED.");
                }

                public void decodeProgress(DataDecodeEvent event) {
                    System.out.println("DECODE PROGRESS: "+event.getProgress());
                }

                public void decodeStarted(DataDecodeEvent event) {
                    // TODO Auto-generated method stub
                    
                }
                
            });
            
            //java.awt.geom.Rectangle2D.Double bounds = new java.awt.geom.Rectangle2D.Double(-100.0, 0.0, 50.0, 50.0);         
            //java.awt.geom.Rectangle2D.Double bounds = new java.awt.geom.Rectangle2D.Double(-180.0, -20.0, 212.0, 80.0);
            java.awt.geom.Rectangle2D.Double bounds = new java.awt.geom.Rectangle2D.Double(-102.0, 17.0, 24.0, 24.0);

//            goes.process(source);
            goes.process(source, bounds);
            //goes.getGridCoverage().show();



            System.out.println("WRITING ASCII Grid");
            WCTRasterExport rasterExport = new WCTRasterExport();
            rasterExport.saveAsciiGrid(new File(source+".asc"), goes, WCTUtils.DECFMT_pDpppp);

        
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }


    
    
    
    @Test
    public void testStage4RemappedRaster() {
        


        try {

            Logger.getLogger("gov.noaa.ncdc").setLevel(Level.SEVERE);


            String source = "E:\\work\\stage-iv\\ST4.2007040112.24h";


            GoesRemappedRaster goes = new GoesRemappedRaster();
            goes.setHeight(500);
            goes.setWidth(500);
            goes.addDataDecodeListener(new DataDecodeListener() {
                public void metadataUpdate(DataDecodeEvent decodeEvent) {
                }
                
                public void decodeEnded(DataDecodeEvent event) {
                    System.out.println("DECODE ENDED.");
                }

                public void decodeProgress(DataDecodeEvent event) {
                    if (event.getProgress() % 50 == 0) {
                        System.out.println("DECODE PROGRESS: "+event.getProgress());
                    }
                }

                public void decodeStarted(DataDecodeEvent event) {
                    // TODO Auto-generated method stub
                    
                }
                
            });
            
            //java.awt.geom.Rectangle2D.Double bounds = new java.awt.geom.Rectangle2D.Double(-100.0, 0.0, 50.0, 50.0);         
            //java.awt.geom.Rectangle2D.Double bounds = new java.awt.geom.Rectangle2D.Double(-180.0, -20.0, 212.0, 80.0);
//            lat= [19.78,53.52] lon= [-134.09,-59.91]
            java.awt.geom.Rectangle2D.Double bounds = new java.awt.geom.Rectangle2D.Double(-130.0, 20.0, 60.0, 30.0);

//            goes.process(source);
            goes.process(source, bounds);
            //goes.getGridCoverage().show();



            System.out.println("WRITING ASCII Grid");
            WCTRasterExport rasterExport = new WCTRasterExport();
            rasterExport.saveAsciiGrid(new File(source+".asc"), goes, WCTUtils.DECFMT_pDpppp);

        
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }


}
