package gov.noaa.ncdc.wct.decoders;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.junit.Test;

import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.iosp.IOServiceProvider;
import ucar.nc2.util.CancelTask;
import ucar.unidata.io.RandomAccessFile;

public abstract class AbstractIospTest {
    
    public abstract File getTestFile();
    public abstract Class<IOServiceProvider> getIospClass();
    

    @Test
    public void testIsValidFile() throws IOException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
//        AreaIosp iosp = new AreaIosp();
        Class<IOServiceProvider> iospClass = getIospClass();        
        Constructor<IOServiceProvider>[] cons = (Constructor<IOServiceProvider>[]) iospClass.getConstructors();
        IOServiceProvider iosp = cons[0].newInstance();
             
        RandomAccessFile raf = new RandomAccessFile(getTestFile().toString(), "r");
        assertTrue(iosp.isValidFile(raf));
        raf.close();
    }

    @Test
    public void testOpenFile() throws IOException, IllegalAccessException, InstantiationException {
        NetcdfFile.registerIOProvider(getIospClass());
        NetcdfFile ncfile = NetcdfFile.open(getTestFile().toString());
        ncfile.close();
//        NCdump.print(ncfile, "", System.out, getCancelTask());
    }

    
    @Test
    public void testReadData() throws IOException, IllegalAccessException, InstantiationException, InvalidRangeException {
        NetcdfFile.registerIOProvider(getIospClass());
        
        NetcdfFile ncfile = NetcdfFile.open(getTestFile().toString());
        List<Variable> varList = ncfile.getVariables();
        for (Variable var : varList) {
            
//            if (var.getDimensions().size() < 2) {
//                continue;
//            }
            
            try {
            
            Section section = new Section();
            List<Dimension> dimList = var.getDimensions();
            System.out.println("VARIABLE: "+var.getName()+" - READING "+dimList.size()+" DIMENSIONS");
            for (Dimension dim : dimList) {
                System.out.println(var.getName()+" : "+ dim.getName()+": 0-"+dim.getLength());
                section.appendRange(0, 0);
            }
            var.read(section);
            
            } catch (Exception e) {
                System.out.println("VARIABLE: "+var+" - ERROR READING SECTION");
            }
        }
        ncfile.close();
    }
    

    
    private CancelTask getCancelTask() {
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
        return cancelTask;
    }


}
