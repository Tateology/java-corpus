package steve.test.nxstats;

import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.StreamingProcess;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Header;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Nexrad;
import gov.noaa.ncdc.wct.export.WCTExportException;
import gov.noaa.ncdc.wct.export.WCTExportNoDataException;
import gov.noaa.ncdc.wct.export.raster.RasterMathOpException;
import gov.noaa.ncdc.wct.export.raster.WCTMathOp;
import gov.noaa.ncdc.wct.export.raster.WCTRaster;
import gov.noaa.ncdc.wct.export.raster.WCTRasterExport;
import gov.noaa.ncdc.wct.export.raster.WCTRasterizer;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import ucar.unidata.io.InMemoryRandomAccessFile;

public class SummarizeNexrad {

    
    private WCTRasterizer sumRaster = null;
    private WCTRasterizer maxRaster = null;
    private WCTRasterizer avgRaster = null;
    private WCTRasterExport export = new WCTRasterExport();

    private int count = 0;
    
    
    public static void main(String[] args) {
        try {
            SummarizeNexrad s = new SummarizeNexrad();
            
            if (args[0].equals("-zip")) {
//              s.processZip(new File("E:\\work\\nxstat\\data\\KGSP20080628.tar.Z-herz.zip"));
                s.processZip(new File(args[1]), new File(args[2]), args[3]);
            }
            else if (args[0].equals("-ascdir")) {
                s.processAscDir(args[1], new File(args[2]), new File(args[3]));
            }
            else {
                System.out.println("args must be -zip or -ascdir");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    
    
    
    
    
    
    
    public void processZip(File file, File outdir, String regex) 
        throws IOException, DecodeException, RasterMathOpException, WCTExportNoDataException, WCTExportException {
        

        
        count = 0;
        
        int cnt = 0;
        ZipFile zipFile = new ZipFile(file);
        Enumeration entries = zipFile.entries();
        while (entries.hasMoreElements()) {

            ZipEntry zipEntry = (ZipEntry) entries.nextElement();
                  

            if (zipEntry.getName().matches(regex)) {

                
//                if (cnt < 0 || cnt > 25) {
//                    System.out.println("SKIPPING: "+zipEntry);
//                    continue;
//                }
                System.out.println("PROCESSING: "+zipEntry);
                
                InputStream zin = zipFile.getInputStream(zipEntry);
                byte[] bytes = new byte[(int)zipEntry.getSize()];
                zin.read(bytes);
                
                processData(zipEntry.getName(), bytes);
                
                cnt++;
                count++;
            }            
        }
        zipFile.close();
        
        export.saveAsciiGrid(new File(outdir+File.separator+file.getName()+"-max.asc"), maxRaster);
        export.saveAsciiGrid(new File(outdir+File.separator+file.getName()+"-sum.asc"), sumRaster);
        export.saveAsciiGrid(new File(outdir+File.separator+file.getName()+"-avg.asc"), avgRaster);
    }
    
    
    
    
    
    
    
    public void processData(String entryName, byte[] data) throws DecodeException, IOException, RasterMathOpException, WCTExportNoDataException, WCTExportException {
        
        ucar.unidata.io.RandomAccessFile f = new InMemoryRandomAccessFile(
                entryName, data);
        f.order(ucar.unidata.io.RandomAccessFile.BIG_ENDIAN);
        DecodeL3Header header = new DecodeL3Header();
        header.decodeHeader(f);
        DecodeL3Nexrad decoder = new DecodeL3Nexrad(header);

        final WCTRasterizer rasterizer = new WCTRasterizer(1200, 1200);
        rasterizer.setAttName("value");
        
        System.out.println("SETTING BOUNDS: "+header.getNexradBounds());
        
        rasterizer.setBounds(header.getNexradBounds());
        rasterizer.setEqualCellsize(true);

        
        if (maxRaster == null) {
            maxRaster = new WCTRasterizer(1200, 1200);
            maxRaster.setBounds(rasterizer.getBounds());
            maxRaster.setEqualCellsize(true);
        }
        if (sumRaster == null) {
            sumRaster = new WCTRasterizer(1200, 1200);
            sumRaster.setBounds(rasterizer.getBounds());
            sumRaster.setEqualCellsize(true);
        }
        if (avgRaster == null) {
            avgRaster = new WCTRasterizer(1200, 1200);
            avgRaster.setBounds(rasterizer.getBounds());
            avgRaster.setEqualCellsize(true);
        }
        
        decoder.decodeData(new StreamingProcess[] { rasterizer });
        
//        StreamingShapefileExport shpExport = new StreamingShapefileExport(new File("E:\\work\\nxstat\\"+entryName+".shp"));
//        decoder.decodeData(new StreamingProcess[] { shpExport, rasterizer });
        
//        export.saveAsciiGrid(new File("E:\\work\\nxstat\\"+entryName+".asc"), rasterizer);
        WCTMathOp.max(maxRaster, maxRaster, rasterizer, rasterizer.getNoDataValue());
        WCTMathOp.sum(sumRaster, sumRaster, rasterizer, rasterizer.getNoDataValue());
        WCTMathOp.average(avgRaster, avgRaster, count, rasterizer, 1, rasterizer.getNoDataValue());
//      WCTMathOp.sum(processRaster.getWritableRaster(), processRaster.getWritableRaster(), rasterizer.getWritableRaster(), rasterizer.getNoDataValue());
//        WCTMathOp.average(processRaster.getWritableRaster(), processRaster.getWritableRaster(), 
//                count, rasterizer.getWritableRaster(), 1, rasterizer.getNoDataValue());
    }
    
    
    
    
    public void processAscDir(String operation, File ascDir, File outFile) 
        throws IOException, RasterMathOpException, WCTExportNoDataException, WCTExportException {
        
        FileFilter filter = new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.toString().endsWith(".asc");
            }            
        };
        
        
        WCTRaster processRaster = null;
        int count = 0;
        for (File ascFile : ascDir.listFiles(filter)) {
            WCTRaster raster = WCTArcGridReader.read(ascFile);
            
            // first time
            if (processRaster == null) {
                processRaster = WCTArcGridReader.read(ascFile);
            }
            
            if (operation.equals("max")) {
                WCTMathOp.max(processRaster, processRaster, raster, raster.getNoDataValue());
            }
            else if (operation.equals("sum")) {
                WCTMathOp.sum(processRaster, processRaster, raster, raster.getNoDataValue());
            }
            else if (operation.equals("avg")) {
                WCTMathOp.average(processRaster, processRaster, count, raster, 1, raster.getNoDataValue());
            }
            else {
                throw new RasterMathOpException("Operation not supported.  Must be 'max', 'sum' or 'avg'");
            }
            count++;
        }
        
        export.saveAsciiGrid(outFile, processRaster);
        
    }
    
    
}
