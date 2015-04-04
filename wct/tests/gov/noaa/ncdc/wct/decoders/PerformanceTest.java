package gov.noaa.ncdc.wct.decoders;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;

import ucar.ma2.Section;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;


public class PerformanceTest {

    private int readIterations;
    private int openFileIterations;
    
    public static void main(String[] args) {

        try {
            NetcdfFile.registerIOProvider("gov.noaa.ncdc.iosp.area.AreaIosp");
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        
        if (args.length != 4) {
            System.err.println("Arg1 = Dir to scan\n" +
                    "Arg2 = suffix of files to filter\n" +
                    "Arg3 = number of open file iterations\n" +
                    "Arg4 = number of read iterations per variable");
            System.exit(0);
        }
        
        try {
            PerformanceTest p = new PerformanceTest();
            p.setOpenFileIterations(Integer.parseInt(args[2]));
            p.setReadIterations(Integer.parseInt(args[3]));
            
            p.process(args[0], args[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    private void process(final String dir, final String suffix) throws IOException {

        FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return (pathname.toString().endsWith(suffix) && pathname.isFile());
            }
        };
        
        for (File file : new File(dir).listFiles(fileFilter)) {
            System.out.println(file+": TESTING OPEN FILES: ITERATIONS = "+getOpenFileIterations());
            testOpenFiles(file);
            System.out.println(file+": TESTING READ FILES: ITERATIONS = "+getReadIterations());
            testRead(file);
        }
    }
    
    private void testOpenFiles(File file) throws IOException {

        for (int n=0; n<getOpenFileIterations(); n++) {
            
            NetcdfFile nc = NetcdfFile.open(file.toString());
            nc.close();
            
        }       
    }
    
    private void testRead(File file) throws IOException {
        
        for (int n=0; n<getReadIterations(); n++) {
            
            NetcdfFile ncfile = NetcdfFile.open(file.toString());
            List<Variable> varList = ncfile.getVariables();
            for (Variable var : varList) {
                
                try {
                
                    Section section = new Section();
                    List<Dimension> dimList = var.getDimensions();
//                  System.out.println("VARIABLE: "+var.getName()+" - SCANNING "+dimList.size()+" DIMENSIONS, READING 10x10x... RANDOM CHUNK");
                    
                    for (Dimension dim : dimList) {
//                      System.out.print(var.getName()+" : "+ dim.getName()+": 0-"+dim.getLength());
                        
                        int span = (dim.getLength() > 10) ? 10 : dim.getLength()-1;
//                        int span = 0;
                        int start = (int)(Math.random()*(dim.getLength()-span));
                    
//                      System.out.println("   adding section for start="+start+"  span="+span);
                        section.appendRange(start, start+span);
                    }
                  System.out.println("READING DATA FROM: "+section+"  "+var.getName());
                    
                    
                    var.read(section);
                
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("VARIABLE: "+var+" - ERROR READING SECTION");
                }
            }
            ncfile.close();

        }
    }

    
    
    

    public void setOpenFileIterations(int openFileIterations) {
        this.openFileIterations = openFileIterations;
    }


    public int getOpenFileIterations() {
        return openFileIterations;
    }


    public void setReadIterations(int readIterations) {
        this.readIterations = readIterations;
    }


    public int getReadIterations() {
        return readIterations;
    }
}
