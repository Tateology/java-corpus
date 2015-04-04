package steve.test.nxstats;

import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.DecodeHintNotSupportedException;
import gov.noaa.ncdc.wct.decoders.StreamingProcess;
import gov.noaa.ncdc.wct.decoders.cdm.DecodeRadialDatasetSweep;
import gov.noaa.ncdc.wct.decoders.cdm.DecodeRadialDatasetSweepHeader;
import gov.noaa.ncdc.wct.export.WCTExportException;
import gov.noaa.ncdc.wct.export.WCTExportNoDataException;
import gov.noaa.ncdc.wct.export.raster.WCTRasterExport;
import gov.noaa.ncdc.wct.export.raster.WCTRasterizer;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.Formatter;

import ucar.nc2.constants.FeatureType;
import ucar.nc2.dt.RadialDatasetSweep;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.util.CancelTask;

public class MosaicExample {
    
    public static void main(String[] args) {        
        try {
            doMosaic();
        } catch (Exception e) {
            e.printStackTrace();
        }        
    }
    
    
    public static void doMosaic() throws DecodeException, WCTExportNoDataException, WCTExportException, IOException, ParseException, DecodeHintNotSupportedException {
        
        // Setup files
        URL[] urls = new URL[] {
            new File("E:\\work\\mosaic\\Level2_KCAE_20090716_1956.ar2v").toURI().toURL(),
            new File("E:\\work\\mosaic\\Level2_KGSP_20090716_1958.ar2v").toURI().toURL(),
            new File("E:\\work\\mosaic\\Level2_KLTX_20090716_1957.ar2v").toURI().toURL()
        };
        
        // Setup rasterizer
        final WCTRasterizer rasterizer = new WCTRasterizer(1200, 1200);
        rasterizer.setAttName("value");
        // Setup geographic extent of interest
        // -83.5, 31, -77, 37.5
        rasterizer.setBounds(new Rectangle.Double(-83.5, 31, 6.5, 6.5));
        rasterizer.setEqualCellsize(true);

        // Loop through files, decode and rasterize onto single grid keeping the max value at each pixel
        for (URL url : urls) {

            // Create a bogus CancelTask to send in to the data decoder
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
            DecodeRadialDatasetSweep radialDatasetDecoder = new DecodeRadialDatasetSweep(header);
            
            // This sets the 'moment' or variable to process
            RadialDatasetSweep.RadialVariable radialVar = (RadialDatasetSweep.RadialVariable) radialDataset.getDataVariable("Reflectivity");
            radialDatasetDecoder.setRadialVariable(radialVar);

            
//            USE THIS OPTION TO FILTER ON ATTRIBUTES SUCH AS VALUE RANGE, HEIGHT, ETC...
//            radialDatasetDecoder.setDecodeHint("nexradFilter", exportRadialFilter);
            
//            THIS OPTION SETS THE SWEEPS TO PROCESS TO ONLY THE FIRST (LOWEST)
            radialDatasetDecoder.setDecodeHint("startSweep", new Integer(0));
            radialDatasetDecoder.setDecodeHint("endSweep", new Integer(0));

            radialDatasetDecoder.decodeData(new StreamingProcess[] { rasterizer });
            
        }
        
        WCTRasterExport export = new WCTRasterExport();
        // Export mosaic grid to ASCII GRID file
        export.saveAsciiGrid(new File("E:\\work\\mosaic\\mosaic-out.asc"), rasterizer);
        
    }

}
