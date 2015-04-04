package steve.test.nxstats;

import gov.noaa.ncdc.wct.export.raster.WCTRaster;
import gov.noaa.ncdc.wct.export.raster.WCTRasterizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class WCTArcGridReader {

    
    

    public static WCTRaster read(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String str;

        int ncols;
        int nrows;
        double xllcorner;
        double yllcorner;
        double cellsize;
        double nodata;

        //      NCOLS 1200
        //      NROWS 957
        //      XLLCORNER -84.9797
        //      YLLCORNER 32.6507
        //      CELLSIZE 0.0046
        //      NODATA_VALUE -999.0000

        // must be in this order
        str = br.readLine();
        ncols = Integer.parseInt(str.split(" ")[1]);
        str = br.readLine();
        nrows = Integer.parseInt(str.split(" ")[1]);
        str = br.readLine();
        xllcorner = Double.parseDouble(str.split(" ")[1]);
        str = br.readLine();
        yllcorner = Double.parseDouble(str.split(" ")[1]);
        str = br.readLine();
        cellsize = Double.parseDouble(str.split(" ")[1]);
        str = br.readLine();
        nodata = Double.parseDouble(str.split(" ")[1]);

        
        WCTRasterizer raster = new WCTRasterizer(nrows, ncols, (float)nodata);
        raster.setBounds(new java.awt.geom.Rectangle2D.Double(
                xllcorner, 
//                yllcorner+(cellsize*nrows),
                yllcorner,
                cellsize*(ncols+1),
                cellsize*(nrows+1)));
        // +1 because we need to go from lower left corner of lower left grid cell to upper right corner of upper right grid cell 
        
        
        int row = 0;
        while ((str = br.readLine()) != null) {

            // start data read
            String[] data = str.split(" ");
            for (int n=0; n<data.length; n++) {
             
                raster.getWritableRaster().setSample(n, row, 0, Double.parseDouble(data[n]));
                
            }        
            row++;
        }

        br.close();
        
        return raster;
    }
}
