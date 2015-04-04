package gov.noaa.ncdc.wct.decoders.cdm;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Formatter;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ucar.nc2.NCdumpW;
import ucar.nc2.NetcdfFile;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dt.RadialDatasetSweep;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.util.CancelTask;

public abstract class TestRadialDataset {

    
    protected RadialDatasetSweep radialDataset;
    
    
    /**
     * Override to set the input test file for the test
     * @return
     */
    public abstract String getTestDataFile();
        
    @BeforeClass 
    public static void init() throws IllegalAccessException, InstantiationException {
        System.out.print("REGISTERING IOSP and TypedDataset");
        NetcdfFile.registerIOProvider(gov.noaa.ncdc.wct.decoders.sigmet.SigmetIOServiceProvider.class);
        FeatureDatasetFactoryManager.registerFactory(FeatureType.RADIAL, gov.noaa.ncdc.wct.decoders.sigmet.SigmetDataset.class);
        System.out.println("   ---- Done ");

    }
    
    
    
    
    @Before
    public void setUp() throws IOException {
        
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
        
        radialDataset = (RadialDatasetSweep) FeatureDatasetFactoryManager.open(
                FeatureType.RADIAL, 
                getTestDataFile(), emptyCancelTask, new Formatter());

    }
    
    
    @Test
    public void ncDump() throws IOException {
    	StringWriter sw = new StringWriter();
        NCdumpW.print(getTestDataFile(), sw, null);    
        System.out.println(sw);
    }
    
    
    public abstract RadialDatasetSweep.Sweep getSweep() throws IOException;
    
    
 

    @Test 
    public abstract void testNumSweeps() throws IOException;
    
    @Test
    public abstract void testGateSize() throws IOException;

    @Test
    public abstract void testGateNumber() throws IOException;
    
    @Test
    public abstract void testMeanElevation() throws IOException;
    
    @Test
    public abstract void testRadialNumber() throws IOException;
    
    @Test
    public abstract void testBeamWidth() throws IOException;
    
    @Test
    public abstract void testRangeToFirstGate() throws IOException;

}
