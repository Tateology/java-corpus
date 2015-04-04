package gov.noaa.ncdc.wct.decoders.cdm;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import ucar.nc2.dt.RadialDatasetSweep;
import ucar.nc2.dt.RadialDatasetSweep.RadialVariable;

public class TestRadialDatasetNexrad extends TestRadialDataset {

    
    
    
    @Override
    public String getTestDataFile() {
        return "testdata"+File.separator+"KLIX20050829_135451";
    }

    
    @Override
    public RadialDatasetSweep.Sweep getSweep() {
        RadialVariable radialVar = (RadialVariable) radialDataset.getDataVariable("Reflectivity");
        return radialVar.getSweep(0);
    }
    
    
    
    @Test
    @Override
    public void testBeamWidth() {
        Assert.assertEquals(0.95, getSweep().getBeamWidth(), 0.001);        
    }

    @Test
    @Override
    public void testGateNumber() {
//        System.out.println(getSweep().getGateNumber());
        Assert.assertEquals(460, getSweep().getGateNumber());        
    }

    @Test
    @Override
    public void testGateSize() {
//        System.out.println(getSweep().getGateSize());
        Assert.assertEquals(1000.0, getSweep().getGateSize(), 0.001);        
    }

    @Test
    @Override
    public void testMeanElevation() {
//        System.out.println(getSweep().getMeanElevation());
        Assert.assertEquals(0.38449156, getSweep().getMeanElevation(), 0.001);        
    }

    @Test
    @Override
    public void testRadialNumber() {
//        System.out.println(getSweep().getRadialNumber());
        Assert.assertEquals(367, getSweep().getRadialNumber());        
    }

    @Test
    @Override
    public void testRangeToFirstGate() {
//        System.out.println(getSweep().getRangeToFirstGate());
        Assert.assertEquals(0.0, getSweep().getRangeToFirstGate(), 0.001);        
    }

    @Test
    @Override
    public void testNumSweeps() {
        RadialVariable radialVar = (RadialVariable) radialDataset.getDataVariable("Reflectivity");
        Assert.assertEquals(18, radialVar.getNumSweeps());        
    }

}
