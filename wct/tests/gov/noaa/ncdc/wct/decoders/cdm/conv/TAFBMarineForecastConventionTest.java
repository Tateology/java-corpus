package gov.noaa.ncdc.wct.decoders.cdm.conv;

import java.io.IOException;
import java.util.Formatter;

import org.junit.Test;

import ucar.nc2.dataset.CoordSysBuilder;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.util.CancelTask;

public class TAFBMarineForecastConventionTest {

    
    @Test
    public void testIsMine() throws IOException {
        CoordSysBuilder.registerConvention("TAFBMarineForecast", gov.noaa.ncdc.wct.decoders.cdm.conv.TAFBMarineForecastConvention.class);
        
        String source = "E:\\work\\nhc-2dimgrids\\NH2_experimental_grids_latest.nc";
        
        Formatter errlog = new Formatter(new StringBuilder());
        CancelTask cancelTask = new CancelTask() {
            @Override
            public boolean isCancel() {
                return false;
            }
            @Override
            public void setError(String msg) {
            }
			@Override
			public void setProgress(String arg0, int arg1) {				
			}
        };
        
        
        
        
        ucar.nc2.dt.GridDataset gds = (ucar.nc2.dt.GridDataset) FeatureDatasetFactoryManager.open(
        		ucar.nc2.constants.FeatureType.GRID, source, cancelTask, errlog);
        System.out.println("\nCF Grids found in 'fixed' dataset from coordsys plugin': \n"+gds.getGrids());
        System.out.println(errlog.toString());
        
        
        
//        PrintWriter outWriter = new PrintWriter(System.out);
//        NCdumpW.print(gds.getNetcdfFile(), "-h", outWriter, cancelTask);
//        outWriter.close();


        
        
        
        
//        FileWriter.writeToFile(gds.getNetcdfFile(), source+"_convout.nc").close();
        gds.close();
        

        
//        ucar.nc2.dt.GridDataset gds2 = (ucar.nc2.dt.GridDataset) TypedDatasetFactory.open(ucar.nc2.constants.FeatureType.GRID, source, cancelTask, errlog);
//        System.out.println("\nCF Grids found in 'fixed' dataset from file': \n"+gds.getGrids());
//        System.out.println(errlog.toString());
//        gds2.close();

    }
}
