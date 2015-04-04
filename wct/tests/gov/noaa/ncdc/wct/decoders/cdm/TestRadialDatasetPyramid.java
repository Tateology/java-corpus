package gov.noaa.ncdc.wct.decoders.cdm;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import ucar.nc2.dt.RadialDatasetSweep;
import ucar.nc2.dt.RadialDatasetSweep.RadialVariable;

public class TestRadialDatasetPyramid extends TestRadialDataset {

    
    
    
    
    @Override
    public String getTestDataFile() {
        return "testdata"+File.separator+"KLIX20050829_135451";
    }

    
    
    

    
    @Test
    public void dumpSweepAzimuthData() throws Exception {
        RadialVariable radialVar = (RadialVariable) radialDataset.getDataVariable("Reflectivity");
        RadialDatasetSweep.Sweep originalSweep = radialVar.getSweep(0);
        
        SweepPyramid sweepPyramid = new SweepPyramid(radialVar.getSweep(0));
        RadialDatasetSweep.Sweep pyramidSweep = sweepPyramid.getDownsampledSweep(3, 3);
        
        
        float[] origAzimuthData = originalSweep.getAzimuth();
        float[] pyramidAzimuthData = pyramidSweep.getAzimuth();
        
        
        for (int n=0; n<origAzimuthData.length; n++) {
            System.out.print("["+n+"] orig="+origAzimuthData[n]+" -- ");
            if (n < pyramidAzimuthData.length) {
                System.out.println(pyramidAzimuthData[n]);
            }
            else {
                System.out.println();
            }
        }
        
    }
    

    @Test
    public void checkSweepAzimuthData() throws Exception {
        RadialVariable radialVar = (RadialVariable) radialDataset.getDataVariable("Reflectivity");
        RadialDatasetSweep.Sweep originalSweep = radialVar.getSweep(0);
        
        SweepPyramid sweepPyramid = new SweepPyramid(radialVar.getSweep(0));
        RadialDatasetSweep.Sweep pyramidSweep = sweepPyramid.getDownsampledSweep(1, 1);
        
        
        float[] origAzimuthData = originalSweep.getAzimuth();
        float[] pyramidAzimuthData = pyramidSweep.getAzimuth();
        
        if (origAzimuthData.length != pyramidAzimuthData.length) {
            throw new Exception("LENGTHS OF AZIMUTH ARRAYS DO NO MATCH: orig="+origAzimuthData.length+"  pyramid="+pyramidAzimuthData.length);
        }
        
        
        for (int n=0; n<origAzimuthData.length; n++) {
            System.out.println("["+n+"] orig="+origAzimuthData[n]+" -- "+pyramidAzimuthData[n]);
        }
        
    }
    
    
    @Test
    public void checkSweepData() throws Exception {
        RadialVariable radialVar = (RadialVariable) radialDataset.getDataVariable("Reflectivity");
        RadialDatasetSweep.Sweep originalSweep = radialVar.getSweep(0);
        
        SweepPyramid sweepPyramid = new SweepPyramid(radialVar.getSweep(0));
        RadialDatasetSweep.Sweep pyramidSweep = sweepPyramid.getDownsampledSweep(1, 1);
        
        
        float[] origData = originalSweep.readData();
        float[] pyramidData = pyramidSweep.readData();
        
        if (origData.length != pyramidData.length) {
            throw new Exception("LENGTHS OF AZIMUTH ARRAYS DO NO MATCH: orig="+origData.length+"  pyramid="+pyramidData.length);
        }
        
        
        for (int n=0; n<origData.length; n++) {
            System.out.println("["+n+"] orig="+origData[n]+" -- "+pyramidData[n]);
        }
        
    }
    
    
    
    
    @Override
    public RadialDatasetSweep.Sweep getSweep() throws IOException {
        RadialVariable radialVar = (RadialVariable) radialDataset.getDataVariable("Reflectivity");
//        return radialVar.getSweep(0);
        SweepPyramid sweepPyramid = new SweepPyramid(radialVar.getSweep(0));
        return sweepPyramid.getDownsampledSweep(3, 3);
    }
    
    
    
    @Test
    @Override
    public void testBeamWidth() throws IOException {
        Assert.assertEquals(2.84999, getSweep().getBeamWidth(), 0.001);        
    }

    @Test
    @Override
    public void testGateNumber() throws IOException {
//        System.out.println(getSweep().getGateNumber());
        Assert.assertEquals(153, getSweep().getGateNumber());        
    }

    @Test
    @Override
    public void testGateSize() throws IOException {
//        System.out.println(getSweep().getGateSize());
        Assert.assertEquals(3000.0, getSweep().getGateSize(), 0.001);        
    }

    @Test
    @Override
    public void testMeanElevation() throws IOException {
//        System.out.println(getSweep().getMeanElevation());
        Assert.assertEquals(0.38449156, getSweep().getMeanElevation(), 0.001);        
    }

    @Test
    @Override
    public void testRadialNumber() throws IOException {
//        System.out.println(getSweep().getRadialNumber());
        Assert.assertEquals(122, getSweep().getRadialNumber());        
    }

    @Test
    @Override
    public void testRangeToFirstGate() throws IOException {
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
