package gov.noaa.ncdc.wct.export;


import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.StreamingProcess;
import gov.noaa.ncdc.wct.decoders.cdm.DecodeRadialDatasetSweep;
import gov.noaa.ncdc.wct.decoders.cdm.DecodeRadialDatasetSweepHeader;
import gov.noaa.ncdc.wct.decoders.nexrad.Level2Transfer;
import gov.noaa.ncdc.wct.decoders.nexrad.RadarHashtables;
import gov.noaa.ncdc.wct.export.raster.WCTRasterExport;
import gov.noaa.ncdc.wct.export.raster.WCTRasterExport.GeoTiffType;
import gov.noaa.ncdc.wct.export.raster.WCTRasterizer;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Test;

import ucar.ma2.InvalidRangeException;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dt.RadialDatasetSweep;
import ucar.nc2.dt.TypedDatasetFactory;

public class TestNexradMosaicExport {


    @Test
    public void testMosaicExport() throws IOException, DecodeException, WCTExportNoDataException, WCTExportException, InvalidRangeException {
        
            java.awt.geom.Rectangle2D.Double bounds = new java.awt.geom.Rectangle2D.Double(
                    -94.0, // lower left corner lon
                    28.0, // lower left corner lat
                    8.0, // width
                    8.0); // height

            
            // For uncompressed data...
//            URL[] nexradURLs = new URL[] {
//                    new File("H:\\Nexrad_Viewer_Test\\Mosaic\\KLIX20050829_135451").toURL(),
//                    new File("H:\\Nexrad_Viewer_Test\\Mosaic\\KMOB20050829_135431").toURL()
//            };
            // For compressed data...
            URL[] nexradURLs = new URL[] {
                    Level2Transfer.getNCDCLevel2UNIXZ(new File("H:\\Nexrad_Viewer_Test\\Mosaic\\KLIX20050829_135451.Z").toURI().toURL(), true),
                    Level2Transfer.getNCDCLevel2UNIXZ(new File("H:\\Nexrad_Viewer_Test\\Mosaic\\KMOB20050829_135431.Z").toURI().toURL(), true)
            };

            // set up our raster (or canvas)
            WCTRasterizer rasterizer = new WCTRasterizer(1000, 1000, -999.0f);
            rasterizer.setAttName("value");
            rasterizer.setBounds(bounds);

            // Loop through each file and rasterize the data onto the raster.
            // If a grid cell has more than one value, the max value is kept.
            // Using the 'Lite' decoder is easy on memory and processes each feature
            // one feature at a time.
            for (int n=0; n<nexradURLs.length; n++) {

                DecodeRadialDatasetSweepHeader level2Header = new DecodeRadialDatasetSweepHeader();
                if (level2Header == null) {
                    level2Header = new DecodeRadialDatasetSweepHeader();
                }
//                level2Header.decodeHeader(nexradURLs[n]);
                // if the file does not have lat/lon/site info encoded inside, set it here based on lookup table
                if (level2Header.getICAO() == null || level2Header.getICAO().equals("XXXX")) {
                    int idx = nexradURLs[n].toString().lastIndexOf('/');
                    String icao = nexradURLs[n].toString().substring(idx+1, idx+5);
                    if (icao.equals("6500")) {
                        icao = nexradURLs[n].toString().substring(idx+5, idx+9); 
                    }

                    System.err.println("SETTING SITE MANUALLY FOR: "+icao);

                    RadarHashtables nxhash = RadarHashtables.getSharedInstance();
                    level2Header.setStationInfo(icao, nxhash.getLat(icao), nxhash.getLon(icao), nxhash.getElev(icao));
                }


                DecodeRadialDatasetSweep decoder = new DecodeRadialDatasetSweep(level2Header);

                
                RadialDatasetSweep radialDataset = (RadialDatasetSweep) TypedDatasetFactory.open(
                        FeatureType.RADIAL, 
                        nexradURLs[n].toString(), WCTUtils.getSharedCancelTask(), new StringBuilder());

                decoder.setRadialVariable(
                        (RadialDatasetSweep.RadialVariable) radialDataset.getDataVariable("Reflectivity")
                        );
                
                // reflectivity cuts are 0, 2, 4, 5, 6, ..., n
                // velocity/spec. width cuts are 1, 3, 4, 5, ..., n
                decoder.decodeData(new StreamingProcess[] { rasterizer });

            }
            
            // Now write out raster
            File outFile1 = new File("H:\\Nexrad_Viewer_Test\\Mosaic\\level2Mosaic-8bit.tif");
            File outFile2 = new File("H:\\Nexrad_Viewer_Test\\Mosaic\\level2Mosaic-32bit.tif");
            File outFile3 = new File("H:\\Nexrad_Viewer_Test\\Mosaic\\level2Mosaic.asc");
            WCTRasterExport rasterExport = new WCTRasterExport();
            rasterExport.saveGeoTIFF(outFile1, rasterizer, GeoTiffType.TYPE_8_BIT);
            rasterExport.saveGeoTIFF(outFile2, rasterizer, GeoTiffType.TYPE_32_BIT);
            rasterExport.saveAsciiGrid(outFile3, rasterizer);
            
        

    }

}
