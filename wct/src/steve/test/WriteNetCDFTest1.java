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

import java.io.IOException;

import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;

/**
 *  Some examples for using the NetCDF-Java library.
 *
 * @author    steve.ansari
 */
public class WriteNetCDFTest1 {

   

   private NetcdfFileWriteable ncfile;
   private int numRows = 3;
   private int numCols = 3;
   private int numTimesteps = 2;
   private String longName = "This is a test of writing NetCDF";
   private String units = "None";

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
   
   
   
   /**
    *  Reads file and prints out metadata and example values from the dataset
    *
    * @param  inFile  Description of the Parameter
    */
   public void writeFile(String outFile) throws IOException, InvalidRangeException {
  
      ncfile = NetcdfFileWriteable.createNew(outFile.toString(), true);
        
        
      // define dimensions
      latDim = ncfile.addDimension("lat", numRows);
      lonDim = ncfile.addDimension("lon", numCols);
      timeDim = ncfile.addDimension("time", numTimesteps);
      //timeDim = ncfile.addDimension("time", numTimesteps, true, true, false);
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
      // :title = "Test NetCDF File" ;
      String title = "Test NetCDF File";
      ncfile.addGlobalAttribute("title", title);
      
      // float lat(lat) ;
      // lat:units = "degrees_north" ;
      ncfile.addVariable("lat", DataType.DOUBLE, new ucar.nc2.Dimension[] {latDim});
      ncfile.addVariableAttribute("lat", "units", "degrees_north");
      // float lon(lon) ;
      // lon:units = "degrees_east" ;
      ncfile.addVariable("lon", DataType.DOUBLE, new ucar.nc2.Dimension[] {lonDim});
      ncfile.addVariableAttribute("lon", "units", "degrees_east");         
      
      // int time(time) ;
      ncfile.addVariable("time", DataType.INT, new ucar.nc2.Dimension[] {timeDim});
      ncfile.addVariableAttribute("time", "units", "seconds since 1970-1-1");         
      
      // create the file
      ncfile.create();
      
      System.out.println( "Create NetCDF File: "+ ncfile);

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
      
      
      int[] sampleTimeData = getSampleTimeData();
      double[] sampleLatData = getSampleLatData();
      double[] sampleLonData = getSampleLonData();
      
      
      for (int n=0; n<numTimesteps; n++) {
         timeArray.setDouble(timeIndex.set(n), sampleTimeData[n]);
      }
      for (int n=0; n<numRows; n++) {
         latArray.setDouble(latIndex.set(n), sampleLatData[n]);
      }
      for (int n=0; n<numCols; n++) {
         lonArray.setDouble(lonIndex.set(n), sampleLonData[n]);
      }

 
      // Get the data
      double[][][] data = getSampleData();
      
      // The values of this layer in the stack
      for (int i=0; i<timeDim.getLength(); i++) {
         for (int j=0; j<latDim.getLength(); j++) {
            for (int k=0; k<lonDim.getLength(); k++) {
               valueArray.setDouble(valueIndex.set(i,j,k), data[i][j][k]);
            }
         }
      }
      
      

      // write data out to disk
      ncfile.write("time", timeArray);
      ncfile.write("lat", latArray);
      ncfile.write("lon", lonArray);

      ncfile.write("value", valueArray);

      ncfile.close();
      
      
   
   }
   
   
   
   private int[] getSampleTimeData() {
      int[] time = new int[] {(int)(System.currentTimeMillis()/1000.0), (int)(System.currentTimeMillis()/1000.0)-60*60*24};
      return time; 
   }
   
   private double[] getSampleLatData() {
      double[] lat = new double[] {34.0, 35.5, 36.0};
      return lat; 
   }
   
   private double[] getSampleLonData() {
      double[] lon = new double[] {-80.0, -79.0, -77.0};
      return lon; 
   }
   
   private double[][][] getSampleData() {
      
      int[] time = getSampleTimeData();
      double[] lat = getSampleLatData();
      double[] lon = getSampleLonData();
      
      double[][][] data = new double[time.length][lat.length][lon.length];
      for (int i=0; i<time.length; i++) {
         for (int j=0; j<lat.length; j++) {
            for (int k=0; k<lon.length; k++) {
               data[i][j][k] = (i+1)*(j+1)*(k+1);
            }
         }
      }

      return data;      
      
   }
   
   
      
   
   
   
   
   
   
   
   
   /**
    *  The main program for the WriteNetCDFTest1 class
    *
    * @param  args  The command line arguments
    */
   public static void main(String[] args) {

      try {
         
         WriteNetCDFTest1 test = new WriteNetCDFTest1();
         
         String outFile = "H:\\Nexrad_Viewer_Test\\Satellite\\test2.nc";
         test.writeFile(outFile);


      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}

