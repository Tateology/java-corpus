/**
 * NOAA's National Climatic Data Center
 * NOAA/NESDIS/NCDC
 * 151 Patton Ave, Asheville, NC  28801
 * 
 * THIS SOFTWARE AND ITS DOCUMENTATION ARE CONSIDERED TO BE IN THE 
 * PUBLIC DOMAIN AND THUS ARE AVAILABLE FOR UNRESTRICTED PUBLIC USE.  
 * THEY ARE FURNISHED "AS IS." THE AUTHORS, THE UNITED STATES GOVERNMENT, ITS
 * INSTRUMENTALITIES, OFFICERS, EMPLOYEES, AND AGENTS MAKE NO WARRANTY,
 * EXPRESS OR IMPLIED, AS TO THE USEFULNESS OF THE SOFTWARE AND
 * DOCUMENTATION FOR ANY PURPOSE. THEY ASSUME NO RESPONSIBILITY (1)
 * FOR THE USE OF THE SOFTWARE AND DOCUMENTATION; OR (2) TO PROVIDE
 * TECHNICAL SUPPORT TO USERS.
 */


package steve.test;

import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.StreamingProcess;
import gov.noaa.ncdc.wct.decoders.StreamingProcessException;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Header;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Nexrad;
import gov.noaa.ncdc.wct.event.GeneralProgressEvent;
import gov.noaa.ncdc.wct.event.GeneralProgressListener;
import gov.noaa.ncdc.wct.export.raster.WCTRaster;
import gov.noaa.ncdc.wct.export.raster.WCTRasterizer;

import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;

import org.geotools.feature.Feature;

import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;

/**
 *  Some examples for using the NetCDF-Java library.
 *
 * @author    steve.ansari
 */
public class NetCDFStack {


    // The list of event listeners.
    private Vector listeners = new Vector();
    private GeneralProgressEvent event = new GeneralProgressEvent(this);

    private NetcdfFileWriteable ncfile;
    private java.awt.geom.Rectangle2D.Double bounds;
    private int numRows;
    private int numCols;
    private int numTimesteps;
    private String longName;
    private String units;

    private ArrayDouble valueArray;
    private Index valueIndex;

    private Dimension latDim; 
    private Dimension lonDim;
    private Dimension timeDim;

    private ArrayDouble latArray;
    private ArrayDouble lonArray;
    private ArrayInt timeArray;
    private Index latIndex;
    private Index lonIndex;
    private Index timeIndex;   


    public void initStack(File outfile, java.awt.geom.Rectangle2D.Double bounds, 
            int numRows, int numCols, int numTimesteps,
            String longName, String units) throws IOException {


        this.bounds = bounds;
        this.numRows = numRows;
        this.numCols = numCols;
        this.numTimesteps = numTimesteps;
        this.longName = longName;
        this.units = units;


        // Start
        // --------------
        for (int i = 0; i < listeners.size(); i++) {
            event.setProgress(0);
            ((GeneralProgressListener) listeners.get(i)).started(event);
        }

        ncfile = new NetcdfFileWriteable(outfile.toString(), true);
        // define dimensions
        latDim = ncfile.addDimension("lat", numRows);
        lonDim = ncfile.addDimension("lon", numCols);
        //timeDim = ncfile.addDimension("time", numTimesteps);
        timeDim = ncfile.addDimension("time", numTimesteps, true, true, false);
        // define Variables
        ucar.nc2.Dimension[] dim3 = new ucar.nc2.Dimension[3];
        dim3[0] = timeDim;
        dim3[1] = latDim;
        dim3[2] = lonDim;
        // int rh(time, lat, lon) ;
        // rh:long_name="relative humidity" ;
        // rh:units = "percent" ;
        ncfile.addVariable("value", DataType.DOUBLE, dim3);
        ncfile.addVariableAttribute("value", "long_name", longName);
        ncfile.addVariableAttribute("value", "units", units);
        // :title = "Example Data" ;
        String title = "Stack of NEXRAD Files";
        ncfile.addGlobalAttribute("title", title);

        // float lat(lat) ;
        // lat:units = "degrees_north" ;
        ncfile.addVariable("lat", DataType.DOUBLE, new ucar.nc2.Dimension[] {latDim});
        ncfile.addVariableAttribute("lat", "units", "degrees_north");
        ncfile.addVariableAttribute("lat", "spacing", ""+bounds.getWidth()/(double)numCols);
        // float lon(lon) ;
        // lon:units = "degrees_east" ;
        ncfile.addVariable("lon", DataType.DOUBLE, new ucar.nc2.Dimension[] {lonDim});
        ncfile.addVariableAttribute("lon", "units", "degrees_east");         
        ncfile.addVariableAttribute("lon", "spacing", ""+bounds.getHeight()/(double)numRows);         

        // int time(time) ;
        ncfile.addVariable("time", DataType.INT, new ucar.nc2.Dimension[] {timeDim});
        ncfile.addVariableAttribute("time", "units", "seconds since 1970-1-1");         

        // create the file
        ncfile.create();

        System.out.println( "ncfile = "+ ncfile);

        // Create value array
        valueArray = new ArrayDouble.D3(timeDim.getLength(), latDim.getLength(), lonDim.getLength());
        valueIndex = valueArray.getIndex();

        // Store the rest of variable values 
        latArray = new ArrayDouble.D1(latDim.getLength());
        latIndex = latArray.getIndex();
        lonArray = new ArrayDouble.D1(lonDim.getLength());
        lonIndex = lonArray.getIndex();
        timeArray = new ArrayInt.D1(timeDim.getLength());
        timeIndex = timeArray.getIndex();

        double latCellsize = bounds.getHeight()/(double)numRows;
        double lonCellsize = bounds.getWidth()/(double)numCols;
        for (int n=0; n<numRows; n++) {
            latArray.setDouble(latIndex.set(n), bounds.y + n*latCellsize);
        }
        for (int n=0; n<numCols; n++) {
            lonArray.setDouble(lonIndex.set(n), bounds.x + n*lonCellsize);
        }



    }


    public void addRaster(WCTRaster nexradRaster, int timestep) throws DecodeException, IOException, Exception {

        System.out.println("\n\naddNexradRaster: timestep="+timestep+"  millis="+nexradRaster.getDateInMilliseconds()+"\n\n\n");

        if (timestep >= numTimesteps) {
            throw new Exception("Timestep ("+timestep+") exceeds number of timesteps ("+numTimesteps+")");
        }

        if (! nexradRaster.getBounds().equals(bounds)) {
            throw new Exception("The NexradRaster bounds do not match the NetCDF bounds defined in 'initStack'");
        }

        WritableRaster raster = nexradRaster.getWritableRaster();
        if (raster.getWidth() != numCols || raster.getHeight() != numRows) {
            throw new Exception("The NexradRaster size does not match the defined numRows and numCols");
        }

        // Extract data from WritableRaster
        double noData = nexradRaster.getNoDataValue();
        int width = raster.getWidth();
        int height = raster.getHeight();
        double[][][] data = new double[1][numRows][numCols];
        for (int j = 0; j < numRows; j++) {
            for (int i = 0; i < numCols; i++) {
                data[0][numRows-j-1][i] = raster.getSampleFloat(i+(width-numCols),  j+(height-numRows), 0);
            }
        }



        double maxval = 0.0;      
        int count = 0;      

        // The values of this layer in the stack
        for (int j=0; j<latDim.getLength(); j++) {
            for (int k=0; k<lonDim.getLength(); k++) {
                if (data[0][j][k] != noData) {

                    if (data[0][j][k] > maxval) maxval = data[0][j][k];                 
                    count++;               
                    valueArray.setDouble(valueIndex.set(timestep,j,k), data[0][j][k]);
                }
                else {
                    valueArray.setDouble(valueIndex.set(timestep,j,k), Double.NaN);
                }
            }
            // Progress
            // --------------
            for (int n = 0; n < listeners.size(); n++) {
                event.setProgress( (int)( ( ((double)j) / latDim.getLength() ) * 100.0) );
                ((GeneralProgressListener) listeners.get(n)).progress(event);
            }
        }

        // Set timevalue
        timeArray.setLong(timeIndex.set(timestep), (long)(nexradRaster.getDateInMilliseconds()/1000.0));


        System.out.println("TTTTTTTTTTTTTTTTTTTTT: maxval="+maxval+" :: valid count="+count);      


    }




    public void finish() throws DecodeException, IOException, Exception {

        // write data out to disk
        ncfile.write("time", timeArray);
        ncfile.write("lat", latArray);
        ncfile.write("lon", lonArray);

        ncfile.write("value", valueArray);

        ncfile.close();

        // End
        // --------------
        for (int i = 0; i < listeners.size(); i++) {
            event.setProgress(0);
            ((GeneralProgressListener) listeners.get(i)).ended(event);
        }

    }


    public void processStack(URL[] urls, File outfile) throws DecodeException, IOException, Exception {




        //--------------------------------------------------------------------
        // Create LiteProcess objects for and rasterizer
        //--------------------------------------------------------------------
        // remember the rasterizer is a LiteProcess too!
        WCTRasterizer rasterizer = new WCTRasterizer(1200, 1200, -999.0f); // x, y, noData

        //--------------------------------------------------------------------
        // Set up rasterizer
        //--------------------------------------------------------------------
        // Sets the attribute from the Feature that will represent the pixel value 
        rasterizer.setAttName("value");
        // Sets the bounds for the raster
        java.awt.geom.Rectangle2D.Double bounds = new java.awt.geom.Rectangle2D.Double(-93.0, 25.0, 8.0, 8.0); 
        rasterizer.setBounds(bounds);
        // Usually you would want these bounds generated from the header object
        //rasterizer.setBounds(header.getNexradBounds());
        // initialize raster to NoData value
        rasterizer.clearRaster();



        StreamingProcess featureCounter = new StreamingProcess() {

            private int count = 0;

//            @Override
            public void addFeature(Feature feature) throws StreamingProcessException {
                count++;
            }

//            @Override
            public void close() throws StreamingProcessException {
                System.out.println("\n\n----------- Number of features decoded: "+count);
            }
        };
        
        
        
        


        /*
         int active_moment = Level2Format.REFLECTIVITY;
         int cut = 0;
         boolean useRFvalues = false;
         URL url = new File("H:\\Nexrad_Viewer_Test\\Korea\\data\\RKSG20060419_000308.Z").toURL();
         url = Level2Transfer.getNCDCLevel2UNIXZ(url);
         DecodeL2Header header = new DecodeL2Header();
         header.decodeHeader(url);
         DecodeL2NexradLite data = new DecodeL2NexradLite(header);
         java.awt.geom.Rectangle2D.Double bounds = header.getNexradBounds(); 
         data.decodeData(new LiteProcess[] { rasterizer }, active_moment, cut, useRFvalues)
         */

        // Create Level-III decoders
        DecodeL3Header header = new DecodeL3Header();
        DecodeL3Nexrad data = new DecodeL3Nexrad(header);
        java.awt.geom.Rectangle2D.Double dataBounds = null;
        java.awt.geom.Rectangle2D.Double lastDataBounds = null;

        this.initStack(outfile, bounds, 1200, 1200, urls.length, "Test Stack", "dBZ");

        for (int n=0; n<urls.length; n++) {
            System.out.println("PROCESSING: "+urls[n]);
            header.decodeHeader(urls[n]);               
            rasterizer.setDateInMilliseconds(header.getMilliseconds());
            data.decodeData(new StreamingProcess[] { rasterizer, featureCounter });


            //bounds = header.getNexradBounds();

            this.addRaster(rasterizer, n);

            // Check for change in grid location
            if (lastDataBounds != null && !lastDataBounds.equals(dataBounds)) {
                throw new Exception("The grid location has changed!");
            }
            lastDataBounds = dataBounds;
            rasterizer.clearRaster();
        }

        this.finish();

    }





    /**
     *  The main program for the NetCDFTest class
     *
     * @param  args  The command line arguments
     */
    public static void main(String[] args) {

        try {

            File outfile = new File("H:\\Nexrad_Viewer_Test\\1.1.0\\Stack\\stack-1.nc");


            URL[] urls = new URL[] {
                    new URL("http://www1.ncdc.noaa.gov/pub/data/nexradviewer/katrina/data/kmob-level3/KMOB_SDUS74_N0ZMOB_200508291359"),  
                    new URL("http://www1.ncdc.noaa.gov/pub/data/nexradviewer/katrina/data/kmob-level3/KMOB_SDUS74_N0ZMOB_200508291404"),  
                    new URL("http://www1.ncdc.noaa.gov/pub/data/nexradviewer/katrina/data/kmob-level3/KMOB_SDUS74_N0ZMOB_200508291409"),  
                    new URL("http://www1.ncdc.noaa.gov/pub/data/nexradviewer/katrina/data/kmob-level3/KMOB_SDUS74_N0ZMOB_200508291415"),  
                    new URL("http://www1.ncdc.noaa.gov/pub/data/nexradviewer/katrina/data/kmob-level3/KMOB_SDUS74_N0ZMOB_200508291420")            
            };

            NetCDFStack ncdf = new NetCDFStack();
            ncdf.processStack(urls, outfile);



        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

