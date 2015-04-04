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
import java.util.List;

import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 *  Some examples for using the NetCDF-Java library.
 *
 * @author    steve.ansari
 */
public class ReadNetCDFTest1 {

   /**
    *  Reads file and prints out metadata and example values from the dataset
    *
    * @param  inFile  Description of the Parameter
    */
   public void describeFile(String inFile) throws IOException {
  
         
      // Open a file readable by the NetCDF-Java library
      NetcdfFile ncFile = NetcdfFile.open(inFile);
   
      // List the attributes of the file (general information about data)
      System.out.println("\n\nNetCDF ATTRIBUTES: \n");
      List globalAtts = ncFile.getGlobalAttributes();
      for (int n = 0; n < globalAtts.size(); n++) {
         System.out.println(globalAtts.get(n).toString());
      }

      // List the variables present in the file (gate, azimuth, value)
      System.out.println("\n\nNetCDF VARIABLES: \n");
      List vars = ncFile.getVariables();
      for (int n = 0; n < vars.size(); n++) {
         System.out.println(((Variable) (vars.get(n))).toString());
      }

      
   }
   
   
   /**
    *  The main program for the ReadNetCDFTest1 class
    *
    * @param  args  The command line arguments
    */
   public static void main(String[] args) {

      try {
         
         ReadNetCDFTest1 test = new ReadNetCDFTest1();
         
         String inFile = "H:\\Nexrad_Viewer_Test\\Satellite\\NDFD_CONUS_5km_20060808_1200.grib2";
         test.describeFile(inFile);


      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}

