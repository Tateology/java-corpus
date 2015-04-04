package steve.test;

import gov.noaa.ncdc.iosp.area.AreaIosp;

import java.io.IOException;

import org.junit.Test;

import ucar.nc2.NetcdfFile;
import ucar.unidata.io.RandomAccessFile;

public class IospPerformanceTest {
    
    @Test
    public void testIsValid() throws IllegalAccessException, InstantiationException, IOException {
        
        String filename = "E:\\work\\goes\\goes12.2005.265.234514.BAND_04";
        
        NetcdfFile.registerIOProvider(gov.noaa.ncdc.iosp.area.AreaIosp.class);

        
        for (int n=0; n<1000; n++) {
            
            
            RandomAccessFile raf = new RandomAccessFile(filename, "r");
            AreaIosp iosp = new AreaIosp();
            iosp.isValidFile(raf);
            iosp.close();
            
            System.out.print("Iosp.isValidFile() :: Max Memory="+Runtime.getRuntime().maxMemory()+"  ::: ");    
            System.out.print("Total Memory="+Runtime.getRuntime().totalMemory()+"  ::: ");    
            System.out.println("Free Memory="+Runtime.getRuntime().freeMemory());
            
            
        }
    }

    

    
    
    @Test
    public void testNetcdfOpen() throws IllegalAccessException, InstantiationException, IOException {
        
        String filename = "E:\\work\\goes\\goes12.2005.265.234514.BAND_04";
        
        NetcdfFile.registerIOProvider(gov.noaa.ncdc.iosp.area.AreaIosp.class);

        
        for (int n=0; n<100000; n++) {
            
            NetcdfFile nc = NetcdfFile.open(filename);
            nc.close();
            
            
            System.out.print("NetcdfFile.open() :: Max Memory="+Runtime.getRuntime().maxMemory()+"  ::: ");    
            System.out.print("Total Memory="+Runtime.getRuntime().totalMemory()+"  ::: ");    
            System.out.println("Free Memory="+Runtime.getRuntime().freeMemory());
            
            
        }
        
    }

}
