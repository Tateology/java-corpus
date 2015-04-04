/**
 * NOAA's National Climatic Data Center
 * NOAA/NESDIS/NCDC
 * 151 Patton Ave, Asheville, NC  28801
 * 
 * THIS SOFTWARE AND ITS DOCUMENTATION ARE CONSIDERED TO BE IN THE 
 * PUBLIC DOMAIN AND THUS ARE AVAILABLE FOR UNRESTRICTED PUBLIC USE.  
 * THEY ARE FURNISHED "AS IS." THE AUTHORS, THE UNITED STATES GOVERNMENT, ITS
 * INSTRUMENTALITIES, OFFICERS, EMPLOYEES, AND AGENTS MAKE NO WARRANTY,
 * EXPRESS OR IMPLIED, AS TO THE USEFULNESS OF THE SOFTWARE AND
 * DOCUMENTATION FOR ANY PURPOSE. THEY ASSUME NO RESPONSIBILITY (1)
 * FOR THE USE OF THE SOFTWARE AND DOCUMENTATION; OR (2) TO PROVIDE
 * TECHNICAL SUPPORT TO USERS.
 */

package gov.noaa.ncdc.wct.decoders.nexrad;

import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.StreamingProcess;
import gov.noaa.ncdc.wct.decoders.StreamingProcessException;
import gov.noaa.ncdc.wct.export.WCTExportException;
import gov.noaa.ncdc.wct.export.WCTExportNoDataException;
import gov.noaa.ncdc.wct.export.raster.WCTRasterExport;
import gov.noaa.ncdc.wct.export.raster.WCTRasterizer;
import gov.noaa.ncdc.wct.export.vector.StreamingShapefileExport;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Test;

public class StreamingProcessTest {
   
    
   @Test 
   public static void runBasicExample() throws DecodeException, IOException, 
       StreamingProcessException, WCTExportNoDataException, WCTExportException {
      
        
         //--------------------------------------------------------------------
         // Input file or URL:
         //--------------------------------------------------------------------
         URL url = new File("H:\\ViewerData\\HAS999900001\\7000KCLE_SDUS51_N0RCLE_200211102258").toURI().toURL();
         //URL url = new File("C:\\ViewerData\\HAS000202248\\7000KDTX_SDUS53_N0RDTX_200211102300").toURL();

         // BEGIN LITE DECODE
         long startTime = System.currentTimeMillis();
            
         //--------------------------------------------------------------------
         // Create Level-III decoders
         //--------------------------------------------------------------------
         DecodeL3Header header = new DecodeL3Header();
         header.decodeHeader(url);               
         DecodeL3Nexrad data = new DecodeL3Nexrad(header);

         //--------------------------------------------------------------------
         // Output files for shapefile and ASCII raster files
         //--------------------------------------------------------------------
         File outShapefile = new File("H:\\Nexrad_Viewer_Test\\1.0.12 BETA\\lite\\lite-vec-ex01");
         File outRaster = new File("H:\\Nexrad_Viewer_Test\\1.0.12 BETA\\lite\\lite-ras-ex01");
         
         //--------------------------------------------------------------------
         // Create StreamingProcess objects for vector export and rasterizer
         //--------------------------------------------------------------------
         StreamingProcess vectorExport = new StreamingShapefileExport(outShapefile);
         // remember the rasterizer is a StreamingProcess too!
         WCTRasterizer rasterizer = new WCTRasterizer(1000, 1000, -999.0f); // x, y, noData
         
         //--------------------------------------------------------------------
         // Set up rasterizer
         //--------------------------------------------------------------------
         // Sets the bounds for the raster
         rasterizer.setBounds(new java.awt.geom.Rectangle2D.Double(-85.0, 35.0, 10.0, 10.0));
         //rasterizer.setBounds(new java.awt.geom.Rectangle2D.Double(-83.0, 41.0, 1.0, 1.0)); // closer look
         // Usually you would want these bounds generated from the header object
         //rasterizer.setBounds(header.getNexradBounds());
         // initialize raster to NoData value
         rasterizer.clearRaster();
         
         //--------------------------------------------------------------------
         // Decode the data
         //--------------------------------------------------------------------
         data.decodeData(new StreamingProcess[] { vectorExport, rasterizer });
         
         //--------------------------------------------------------------------
         // Close the shapefile export 
         //--------------------------------------------------------------------
         vectorExport.close();
         
         //--------------------------------------------------------------------
         // Save the raster
         //--------------------------------------------------------------------
         WCTRasterExport rasterExport = new WCTRasterExport();
         rasterExport.saveAsciiGrid(outRaster, rasterizer);

         System.out.println("SAVED AND RASTERIZED IN " + (System.currentTimeMillis() - startTime) + " MILLISECONDS (total)");
         
   }
   
   
//   
//   public static void runMosaicExample() {
//      
//      try {
//         
//         //--------------------------------------------------------------------
//         // Input file or URL:
//         //--------------------------------------------------------------------
//         java.net.URL[] urls = new URL[] {
//            new java.io.File("C:\\ViewerData\\HAS999900001\\7000KCLE_SDUS51_N0RCLE_200211102258").toURL(),
//            new java.io.File("C:\\ViewerData\\HAS000202646\\7000KILN_SDUS51_N0RILN_200211102258").toURL(),
//            new java.io.File("C:\\ViewerData\\HAS000202674\\7000KLMK_SDUS53_N0RLVX_200211102259").toURL(),
//            new java.io.File("C:\\ViewerData\\HAS000202248\\7000KDTX_SDUS53_N0RDTX_200211102300").toURL()
//         };
//
//         // BEGIN LITE DECODE
//         long startTime = System.currentTimeMillis();
//            
//         //--------------------------------------------------------------------
//         // Output files for ASCII raster files
//         //--------------------------------------------------------------------
//         File outRaster = new File("D:\\Nexrad_Viewer_Test\\1.0.12 BETA\\lite\\lite-ras-ex02");
//         
//         //--------------------------------------------------------------------
//         // Create StreamingProcess objects for and rasterizer
//         //--------------------------------------------------------------------
//         // remember the rasterizer is a StreamingProcess too!
//         FeatureRasterizer rasterizer = new FeatureRasterizer(1200, 1200, -999.0f); // x, y, noData
//         
//         //--------------------------------------------------------------------
//         // Set up rasterizer
//         //--------------------------------------------------------------------
//         // This sets the operation when multiple features intersect the same
//         // grid cell.         
//         //  CMD_KEEP_LARGER_VALUE --or-- CMD_KEEP_LARGER_ABSOLUTE_VALUE
//         // I use CMD_KEEP_LARGER_VALUE for Reflectivity and Spectrum Width 
//         // and CMD_KEEP_LARGER_ABSOLUTE_VALUE for Velocity moments.
//         rasterizer.setCellMathDecision(FeatureRasterizer.CMD_KEEP_LARGER_VALUE);
//         // Sets the data type for the raster
//         //rasterizer.setRasterType(FeatureRasterizer.FLOAT);
//         rasterizer.setRasterType(FeatureRasterizer.INT);
//         // Sets the attribute from the Feature that will represent the pixel value 
//         //rasterizer.setAttName("value");
//         rasterizer.setAttName("colorIndex");
//         // Sets the bounds for the raster
//         java.awt.geom.Rectangle2D.Double bounds = new java.awt.geom.Rectangle2D.Double(-91.0, 35.0, 12.0, 10.0); 
////         java.awt.geom.Rectangle2D.Double bounds = new java.awt.geom.Rectangle2D.Double(-83.0, 41.0, 1.0, 1.0); 
//         rasterizer.setBounds(bounds);
//         // Usually you would want these bounds generated from the header object
//         //rasterizer.setBounds(header.getNexradBounds());
//         // initialize raster to NoData value
//         rasterizer.clearRaster();
//         
//         //--------------------------------------------------------------------
//         // Decode the data and rasterize for each url
//         //--------------------------------------------------------------------
//         // Create Level-III decoders
//         for (int n=0; n<urls.length; n++) {
//            System.out.println("PROCESSING: "+urls[n]);
//            DecodeL3Header header = new DecodeL3Header();
//            header.decodeHeader(urls[n]);               
//            DecodeL3NexradLite data = new DecodeL3NexradLite(header);
//            data.decodeData(new StreamingProcess[] { rasterizer });
//         }
//
//         
//         
//         //--------------------------------------------------------------------
//         // Save the raster
//         //--------------------------------------------------------------------
//         //NexradRasterExport.saveAsciiGrid(outRaster, rasterizer);
//
//         //System.out.println("SAVED AND RASTERIZED IN " + (System.currentTimeMillis() - startTime) + " MILLISECONDS (total)");
//         
//         
//         System.out.println("DISPLAYING MOSAIC");
//         
//         gov.noaa.ncdc.nexradiv.NexradIAViewer nexview = new gov.noaa.ncdc.nexradiv.NexradIAViewer(true);
//         nexview.setCurrentExtent(bounds);
//         nexview.setMaxExtent(bounds);
//         nexview.pack();
//         nexview.show();
//         
//         DecodeL3Header header = new DecodeL3Header();
//         header.decodeHeader(urls[0]);               
//         //rasterizer.setSmoothing(true);
//         //rasterizer.setSmoothFactor(20);
//         nexview.setNexradGridCoverage(rasterizer.getGridCoverage(header));
//         nexview.setNexradGridCoverageVisibility(true);
//         /*
//         gov.noaa.ncdc.nexradiv.MapSelector mapSelect = new gov.noaa.ncdc.nexradiv.MapSelector(nexview);
//         mapSelect.getWMSPanel().setSelectedWMS(0, "Topo Map");
//         //mapSelect.getWMSPanel().setSelectedWMSLayers(0, " ", "Topo Map", 2);
//         nexview.getNexradMapPaneZoomChange().setWMSPanel(mapSelect.getWMSPanel());
//         mapSelect.getWMSPanel().refreshWMS();
//         nexview.setNexradTransparency(50, rasterizer);
//         */
//
//         gov.noaa.ncdc.nexradiv.AWTImageExport imgExport = 
//            new gov.noaa.ncdc.nexradiv.AWTImageExport(nexview.getViewerAWTImage());
//         imgExport.exportImage(new File[] {
//               new File("D:\\Nexrad_Viewer_Test\\Batch\\Viewer\\batchTest")
//            },
//            gov.noaa.ncdc.nexradiv.AWTImageExport.PNG);
//         
//         
//      } catch (Exception e) {
//         e.printStackTrace();
//      }
//   }
//
//
//   public static void runMosaicExample2(String outDir) {
//      
//      try {
//         
//         //--------------------------------------------------------------------
//         // Input file or URL:
//         //--------------------------------------------------------------------
//         java.net.URL[] urls = new URL[] {
//            new URL("http://weather.noaa.gov/pub/SL.us008001/DF.of/DC.radar/DS.p19r0/SI.kgsp/sn.last"),
//            new URL("http://weather.noaa.gov/pub/SL.us008001/DF.of/DC.radar/DS.p19r1/SI.kgsp/sn.last"),
//            new URL("http://weather.noaa.gov/pub/SL.us008001/DF.of/DC.radar/DS.p19r0/SI.kmrx/sn.last"),
//            new URL("http://weather.noaa.gov/pub/SL.us008001/DF.of/DC.radar/DS.p19r1/SI.kmrx/sn.last"),
//         };
//
//         // BEGIN LITE DECODE
//         long startTime = System.currentTimeMillis();
//            
//         //--------------------------------------------------------------------
//         // Output files for ASCII raster files
//         //--------------------------------------------------------------------
//         //File outRaster = new File("D:\\Nexrad_Viewer_Test\\1.0.12 BETA\\lite\\lite-ras-ex02");
//         
//         //--------------------------------------------------------------------
//         // Create StreamingProcess objects for and rasterizer
//         //--------------------------------------------------------------------
//         // remember the rasterizer is a StreamingProcess too!
//         FeatureRasterizer rasterizer = new FeatureRasterizer(1200, 1200, -999.0f); // x, y, noData
//         
//         //--------------------------------------------------------------------
//         // Set up rasterizer
//         //--------------------------------------------------------------------
//         // This sets the operation when multiple features intersect the same
//         // grid cell.         
//         // Sets the attribute from the Feature that will represent the pixel value 
//         rasterizer.setAttName("value");
//         // Sets the bounds for the raster
//         double centerLon = -82.54;
//         double centerLat = 35.57;
//         double width = 1.2;
//         double height = 1.0;
//         java.awt.geom.Rectangle2D.Double rasterBounds = new java.awt.geom.Rectangle2D.Double(centerLon-width/1.5, centerLat-height/1.5, width*1.5, height*1.5); 
//         java.awt.geom.Rectangle2D.Double bounds = new java.awt.geom.Rectangle2D.Double(centerLon-width/2.0, centerLat-height/2.0, width, height); 
////         java.awt.geom.Rectangle2D.Double bounds = new java.awt.geom.Rectangle2D.Double(-83.0, 41.0, 1.0, 1.0); 
//         rasterizer.setBounds(rasterBounds);
//         // Usually you would want these bounds generated from the header object
//         //rasterizer.setBounds(header.getNexradBounds());
//         // initialize raster to NoData value
//         rasterizer.clearRaster();
//         
//         //--------------------------------------------------------------------
//         // Decode the data and rasterize for each url
//         //--------------------------------------------------------------------
//         // Create Level-III decoders
//         for (int n=0; n<urls.length; n++) {
//            System.out.println("PROCESSING: "+urls[n]);
//            DecodeL3Header header = new DecodeL3Header();
//            header.decodeHeader(urls[n]);               
//            DecodeL3NexradLite data = new DecodeL3NexradLite(header);
//            data.decodeData(new StreamingProcess[] { rasterizer });
//         }
//
//         
//         
//         //--------------------------------------------------------------------
//         // Save the raster
//         //--------------------------------------------------------------------
//         //NexradRasterExport.saveAsciiGrid(outRaster, rasterizer);
//
//         //System.out.println("SAVED AND RASTERIZED IN " + (System.currentTimeMillis() - startTime) + " MILLISECONDS (total)");
//         
//         
//         System.out.println("DISPLAYING MOSAIC");
//         
//         gov.noaa.ncdc.nexradiv.NexradIAViewer nexview = new gov.noaa.ncdc.nexradiv.NexradIAViewer(true);
//         nexview.setCurrentExtent(bounds);
//         nexview.setMaxExtent(bounds);
//         nexview.getMapPane().reset();
//         nexview.pack();
//         nexview.setMainPanelSize(600, 600);
//         //nexview.show();
//         
//         DecodeL3Header header = new DecodeL3Header();
//         header.decodeHeader(urls[0]);               
//         rasterizer.setSmoothing(true);
//         rasterizer.setSmoothFactor(14);
//
//         //nexview.setNexradGridCoverage(rasterizer.getGridCoverage(header));
//         // Use Level-II color scale and don't classify
//         java.awt.Color[] colors = NexradColorFactory.getColors(NexradHeader.LEVEL2_REFLECTIVITY, false);
//         double[] maxmin = NexradValueFactory.getProductMaxMinValues(NexradHeader.LEVEL2_REFLECTIVITY, 12, false);
//         nexview.setNexradGridCoverage(rasterizer.getGridCoverage(header, false, colors, maxmin));
//         nexview.setNexradGridCoverageVisibility(true);
//
//         gov.noaa.ncdc.nexradiv.AWTImageExport imgExport = 
//            new gov.noaa.ncdc.nexradiv.AWTImageExport(nexview.getViewerAWTImage());
//         imgExport.exportImage(new File[] {
//               new File(outDir+File.separator+"avl-plain")
//            },
//            gov.noaa.ncdc.nexradiv.AWTImageExport.PNG);
//         imgExport.exportImage(new File[] {
//               new File(outDir+File.separator+"avl-plain")
//            },
//            gov.noaa.ncdc.nexradiv.AWTImageExport.JPEG);
//
//
//         //nexview.setNexradGridCoverage(rasterizer.getGridCoverage(header));
//         // Use Level-II color scale and don't classify
//         java.awt.Color[] transcolors = NexradColorFactory.getTransparentColors(NexradHeader.LEVEL2_REFLECTIVITY, false, 180);
//         nexview.setNexradGridCoverage(rasterizer.getGridCoverage(header, false, transcolors, maxmin));
//         
//         gov.noaa.ncdc.nexradiv.MapSelector mapSelect = new gov.noaa.ncdc.nexradiv.MapSelector(nexview);
//         mapSelect.setNexradOnTop(true);
//         mapSelect.setIsThemeVisible(gov.noaa.ncdc.nexradiv.NexradIAViewer.HWY_INT, false);
//         nexview.setThemeVisibility(gov.noaa.ncdc.nexradiv.NexradIAViewer.HWY_INT, false);
//         mapSelect.getWMSPanel().setSelectedWMS(0, "Shaded Relief");
//         mapSelect.getWMSPanel().setSelectedWMS(1, "Reference");
//         mapSelect.getWMSPanel().setSelectedWMSLayers(1, "Federal_Lands,Names-Federal_Lands,Urban_Areas,"+
//            "Names-Urban_Areas,Water_Bodies,Names-Water_Bodies,Streams,Names-Streams,Roads,"+
//            "Interstate%20Labels,Route_Numbers,State_Labels,County_Labels"
//            , "Reference", 4);
//         nexview.getNexradMapPaneZoomChange().setWMSPanel(mapSelect.getWMSPanel());
//         mapSelect.getWMSPanel().refreshWMS();
//         
//         System.out.println("WAITING FOR WMS");
//         while (mapSelect.getWMSPanel().isWaiting(0) || mapSelect.getWMSPanel().isWaiting(1)) {
//            Thread.sleep(100);
//         }
//         System.out.println("DONE WAITING FOR WMS");
//         
//         gov.noaa.ncdc.nexradiv.AWTImageExport imgExport2 = 
//            new gov.noaa.ncdc.nexradiv.AWTImageExport(nexview.getViewerAWTImage());
//         imgExport2.exportImage(new File[] {
//               new File(outDir+File.separator+"avl-labels")
//            },
//            gov.noaa.ncdc.nexradiv.AWTImageExport.PNG);
//         imgExport2.exportImage(new File[] {
//               new File(outDir+File.separator+"avl-labels")
//            },
//            gov.noaa.ncdc.nexradiv.AWTImageExport.JPEG);
//
//            
//
//            
//            
//         mapSelect.getWMSPanel().setSelectedWMS(0, "Topo Map");
//         bounds = new java.awt.geom.Rectangle2D.Double(centerLon-width/4.0, centerLat-height/4.0, width/2.0, height/2.0); 
//         nexview.setCurrentExtent(bounds);
//         mapSelect.getWMSPanel().setSelectedWMS(1, "None");
//         //mapSelect.getWMSPanel().setSelectedWMSLayers(1, " ", "None", gov.noaa.ncdc.wms.WMSData.NONE);
//         mapSelect.getWMSPanel().refreshWMS();
//
//         System.out.println("WAITING FOR WMS");
//         while (mapSelect.getWMSPanel().isWaiting(0) || mapSelect.getWMSPanel().isWaiting(1)) {
//            Thread.sleep(100);
//         }
//         System.out.println("DONE WAITING FOR WMS");
//         
//         gov.noaa.ncdc.nexradiv.AWTImageExport imgExport3 = 
//            new gov.noaa.ncdc.nexradiv.AWTImageExport(nexview.getViewerAWTImage());
//         imgExport3.exportImage(new File[] {
//               new File(outDir+File.separator+"avl-topo")
//            },
//            gov.noaa.ncdc.nexradiv.AWTImageExport.PNG);
//         imgExport3.exportImage(new File[] {
//               new File(outDir+File.separator+"avl-topo")
//            },
//            gov.noaa.ncdc.nexradiv.AWTImageExport.JPEG);
//
//            
//            
//            
//            
//            
//         //nexview.show();
//         nexview.dispose();
//         
//      } catch (Exception e) {
//         e.printStackTrace();
//      }
//   }

   
}

