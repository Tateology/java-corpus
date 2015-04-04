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

import gov.noaa.ncdc.wct.decoders.nexrad.Level2Transfer;

import java.io.File;
import java.net.URL;
import java.util.List;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.Attribute;
import ucar.nc2.FileWriter;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 *  Some examples for using the NetCDF-Java library.
 *
 * @author    steve.ansari
 */
public class NetCDFTest {




    /**
     *  The main program for the NetCDFTest class
     *
     * @param  args  The command line arguments
     */
    public static void main(String[] args) {

        try {


            writeRadialNetCDF(new File("H:\\Nexrad_Viewer_Test\\Korea\\data\\RKJK20060227_160217.Z").toURL());
//            readVCP(new File("H:\\Nexrad_Viewer_Test\\Korea\\data\\RKJK20060227_160217.Z").toURL());


//          String inFile = "H:\\Nexrad_Viewer_Test\\QPE\\QPE_2d_20060427_00-07\\QPE_20060427-0000_t1_2d.netcdf";
//          describeQPEFile(inFile);
            /*
          String inFile = "H:\\Nexrad_Viewer_Test\\CASA\\data\\rushsprings.ok-20060907-181000.netcdf";
          describeCASAFile(inFile);

          String inFile = "H:\\ViewerData\\HAS999900001\\7000KCLE_SDUS81_DPACLE_200211102356";
          describeDPAFile(inFile);
             */


            //checkNetCDFBounds();


            //String inFile = "C:\\ViewerData\\HAS999900001\\7000KCLE_SDUS31_NVWCLE_200211102200";
            //String inFile = "H:\\Nexrad_Viewer_Test\\Stage4\\ST4.2005110212.24h";
            //String inFile = "H:\\Nexrad_Viewer_Test\\1.1.0\\LDM\\KLIX_20050829_1349";
            //describeFile(inFile);
            //describeST4File(inFile);

            //String inFile = "C:\\ViewerData\\level2-uncompressed\\6500KGSP20041029_000938";
//          String inFile = "C:\\ViewerData\\HAS999900001\\7000KCLE_SDUS51_N0RCLE_200211102258";
//          String outFile = "D:\\Nexrad_Viewer_Test\\NetCDF\\radial.nc";
//          saveNetCDF(outFile, inFile);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void writeRadialNetCDF(URL nexradURL) {
        
        try {

            NetcdfFile ncIn = NetcdfFile.open(Level2Transfer.getNCDCLevel2UNIXZ(nexradURL).toString());
            NetcdfFile ncOut = FileWriter.writeToFile(ncIn, "H:\\Nexrad_Viewer_Test\\RadialNetCDF\\RKJK20060227_160217.nc");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }






    public static void readVCP(URL nexradURL) {

        try {

            NetcdfFile ncIn = NetcdfFile.open(Level2Transfer.getNCDCLevel2UNIXZ(nexradURL).toString());

            System.out.println("VCP="+ncIn.findGlobalAttribute("VolumeCoveragePattern").getNumericValue());


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *  Reads file and prints out metadata and example values from the dataset
     *
     * @param  inFile  Description of the Parameter
     */
    public static void describeFile(String inFile) {
        try {
            /*
    // need the file length to create a byte[] of the proper size
    File file = new File(inFile);
    long len = file.length();
    System.out.println("file length=" + len);

    byte[] fileData = new byte[(int)len];

    // read entire file into byte array
    RandomAccessFile raf = new RandomAccessFile(file, "r");
    raf.readFully(fileData);
             */




            // Open a file readable by the NetCDF-Java library
            NetcdfFile ncfileIn = NetcdfFile.open(inFile);
            //NetcdfFile ncfileIn = NetcdfFile.openInMemory(inFile, fileData);

            // List the attributes of the file (general information about data)
            System.out.println("\n\nNetCDF ATTRIBUTES: \n");
            List globalAtts = ncfileIn.getGlobalAttributes();
            for (int n = 0; n < globalAtts.size(); n++) {
                System.out.println(globalAtts.get(n).toString());
            }

            // List the variables present in the file (gate, azimuth, value)
            System.out.println("\n\nNetCDF VARIABLES: \n");
            List vars = ncfileIn.getVariables();
            for (int n = 0; n < vars.size(); n++) {
                System.out.println(((Variable) (vars.get(n))).toString());
            }

            // List the dimensions of the "value" array of data
            System.out.println("\n\nBASE REFLECTIVITY DIMENSIONS: \n");
            Variable bref = ncfileIn.findVariable("BaseReflectivity");
            List dims = bref.getDimensions();
            for (int n = 0; n < dims.size(); n++) {
                System.out.println(dims.get(n).toString());
            }

            // Print the primitive datatype
            System.out.println("\n\nBASE REFLECTIVITY DATATYPE: \n");
            System.out.println(bref.getDataType().toString());

            // Read the data from the "value" array, create an index and print some values
            System.out.println("\n\nBASE REFLECTIVITY DATA: \n");
            Array data = bref.read();
            Index index = data.getIndex();
            int[] shape = data.getShape();
            System.out.println("Data Array Dimensions: " + shape[0] + " , " + shape[1]);
//          for (int i=0; i<shape[0]; i++) {
//          for (int j=0; j<shape[1]; j++) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    System.out.println("i=" + i + " j=" + j + " value=" + data.getDouble(index.set(i, j)));
                }
            }

            System.out.println("\n\nAZIMUTH DIMENSIONS: \n");
            Variable azimuth = ncfileIn.findVariable("azimuth");
            List azdims = azimuth.getDimensions();
            for (int n = 0; n < azdims.size(); n++) {
                System.out.println(azdims.get(n).toString());
            }

            System.out.println("\n\nAZIMUTH DATATYPE: \n");
            System.out.println(azimuth.getDataType().toString());

            System.out.println("\n\nRANGE DIMENSIONS: \n");
            Variable range = ncfileIn.findVariable("gate");
            List rdims = range.getDimensions();
            for (int n = 0; n < rdims.size(); n++) {
                System.out.println(rdims.get(n).toString());
            }

            System.out.println("\n\nRANGE DATATYPE: \n");
            System.out.println(range.getDataType().toString());

            // Read the data from all variables into an array, create an index and print some values
            System.out.println("\n\nBASE REFLECTIVITY DATA WITH RANGE AND AZIMUTH: \n");
//          Array data = bref.read(); ALREADY DEFINED
            Array azdata = azimuth.read();
            Array rdata = range.read();
//          Index index = data.getIndex(); ALREADY DEFINED
            Index azindex = azdata.getIndex();
            Index rindex = rdata.getIndex();
//          int[] shape = data.getShape(); ALREADY DEFINED
            // because bref is dependent on range and azimuth we can use this shape for all
            System.out.println("Data Array Dimensions: " + shape[0] + " , " + shape[1]);
//          for (int i=0; i<shape[0]; i++) {
//          for (int j=0; j<shape[1]; j++) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    System.out.println("i=" + i + " j=" + j + " az=" + azdata.getDouble(azindex.set(i)) +
                            " r=" + rdata.getDouble(rindex.set(j)) + " value=" + data.getDouble(index.set(i, j)));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    /**
     *  Reads file and prints out metadata and example values from the dataset
     *
     * @param  inFile  Description of the Parameter
     */
    public static void describeST4File(String inFile) {
        try {
            /*
    // need the file length to create a byte[] of the proper size
    File file = new File(inFile);
    long len = file.length();
    System.out.println("file length=" + len);

    byte[] fileData = new byte[(int)len];

    // read entire file into byte array
    RandomAccessFile raf = new RandomAccessFile(file, "r");
    raf.readFully(fileData);
             */




            // Open a file readable by the NetCDF-Java library
            NetcdfFile ncfileIn = NetcdfFile.open(inFile);
            //NetcdfFile ncfileIn = NetcdfFile.openInMemory(inFile, fileData);

            // List the attributes of the file (general information about data)
            System.out.println("\n\nNetCDF ATTRIBUTES: \n");
            List globalAtts = ncfileIn.getGlobalAttributes();
            for (int n = 0; n < globalAtts.size(); n++) {
                System.out.println(globalAtts.get(n).toString());
            }

            // List the variables present in the file (gate, azimuth, value)
            System.out.println("\n\nNetCDF VARIABLES: \n");
            List vars = ncfileIn.getVariables();
            for (int n = 0; n < vars.size(); n++) {
                System.out.println(((Variable) (vars.get(n))).toString());
            }

            // List the dimensions of the "value" array of data
            System.out.println("\n\nBASE REFLECTIVITY DIMENSIONS: \n");
            Variable bref = ncfileIn.findVariable("Total_precipitation");
            List dims = bref.getDimensions();
            for (int n = 0; n < dims.size(); n++) {
                System.out.println(dims.get(n).toString());
            }

            // Print the primitive datatype
            System.out.println("\n\nBASE REFLECTIVITY DATATYPE: \n");
            System.out.println(bref.getDataType().toString());

            // Read the data from the "value" array, create an index and print some values
            System.out.println("\n\nBASE REFLECTIVITY DATA: \n");
            Array data = bref.read();
            Index index = data.getIndex();
            int[] shape = data.getShape();
            System.out.println("Data Array Dimensions: " + shape[1] + " , " + shape[2]);
//          for (int i=0; i<shape[0]; i++) {
//          for (int j=0; j<shape[1]; j++) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    System.out.println("i=" + i + " j=" + j + " value=" + data.getDouble(index.set(0, i, j)));
                }
            }

            System.out.println("\n\nLAT DIMENSIONS: \n");
            Variable azimuth = ncfileIn.findVariable("lat");
            List azdims = azimuth.getDimensions();
            for (int n = 0; n < azdims.size(); n++) {
                System.out.println(azdims.get(n).toString());
            }

            System.out.println("\n\nLAT DATATYPE: \n");
            System.out.println(azimuth.getDataType().toString());

            System.out.println("\n\nLON DIMENSIONS: \n");
            Variable range = ncfileIn.findVariable("lon");
            List rdims = range.getDimensions();
            for (int n = 0; n < rdims.size(); n++) {
                System.out.println(rdims.get(n).toString());
            }

            System.out.println("\n\nLON DATATYPE: \n");
            System.out.println(range.getDataType().toString());

            // Read the data from all variables into an array, create an index and print some values
            System.out.println("\n\nBASE REFLECTIVITY DATA WITH RANGE AND AZIMUTH: \n");
//          Array data = bref.read(); ALREADY DEFINED
            Array azdata = azimuth.read();
            Array rdata = range.read();
//          Index index = data.getIndex(); ALREADY DEFINED
            Index azindex = azdata.getIndex();
            Index rindex = rdata.getIndex();
//          int[] shape = data.getShape(); ALREADY DEFINED
            // because bref is dependent on range and azimuth we can use this shape for all
            System.out.println("Data Array Dimensions: " + shape[0] + " , " + shape[1]);
//          for (int i=0; i<shape[0]; i++) {
//          for (int j=0; j<shape[1]; j++) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    System.out.println("i=" + i + " j=" + j + " lat=" + azdata.getDouble(azindex.set(i)) +
                            " lon=" + rdata.getDouble(rindex.set(j)) + " value=" + data.getDouble(index.set(0, i, j)));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    /**
     *  Reads file and prints out metadata and example values from the dataset
     *
     * @param  inFile  Description of the Parameter
     */
    public static void describeDPAFile(String inFile) {
        try {
            /*
    // need the file length to create a byte[] of the proper size
    File file = new File(inFile);
    long len = file.length();
    System.out.println("file length=" + len);

    byte[] fileData = new byte[(int)len];

    // read entire file into byte array
    RandomAccessFile raf = new RandomAccessFile(file, "r");
    raf.readFully(fileData);
             */




            // Open a file readable by the NetCDF-Java library
            NetcdfFile ncfileIn = NetcdfFile.open(inFile);
            //NetcdfFile ncfileIn = NetcdfFile.openInMemory(inFile, fileData);

            // List the attributes of the file (general information about data)
            System.out.println("\n\nNetCDF ATTRIBUTES: \n");
            List globalAtts = ncfileIn.getGlobalAttributes();
            for (int n = 0; n < globalAtts.size(); n++) {
                System.out.println(globalAtts.get(n).toString());
            }

            // List the variables present in the file (gate, azimuth, value)
            System.out.println("\n\nNetCDF VARIABLES: \n");
            List vars = ncfileIn.getVariables();
            for (int n = 0; n < vars.size(); n++) {
                System.out.println(((Variable) (vars.get(n))).toString());
            }

            // List the dimensions of the "value" array of data
            System.out.println("\n\nBASE REFLECTIVITY DIMENSIONS: \n");
            Variable bref = ncfileIn.findVariable("Total_precipitation");
            List dims = bref.getDimensions();
            for (int n = 0; n < dims.size(); n++) {
                System.out.println(dims.get(n).toString());
            }

            // Print the primitive datatype
            System.out.println("\n\nBASE REFLECTIVITY DATATYPE: \n");
            System.out.println(bref.getDataType().toString());

            // Read the data from the "value" array, create an index and print some values
            System.out.println("\n\nBASE REFLECTIVITY DATA: \n");
            Array data = bref.read();
            Index index = data.getIndex();
            int[] shape = data.getShape();
            System.out.println("Data Array Dimensions: " + shape[1] + " , " + shape[2]);
//          for (int i=0; i<shape[0]; i++) {
//          for (int j=0; j<shape[1]; j++) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    System.out.println("i=" + i + " j=" + j + " value=" + data.getDouble(index.set(0, i, j)));
                }
            }

            System.out.println("\n\nLAT DIMENSIONS: \n");
            Variable azimuth = ncfileIn.findVariable("lat");
            List azdims = azimuth.getDimensions();
            for (int n = 0; n < azdims.size(); n++) {
                System.out.println(azdims.get(n).toString());
            }

            System.out.println("\n\nLAT DATATYPE: \n");
            System.out.println(azimuth.getDataType().toString());

            System.out.println("\n\nLON DIMENSIONS: \n");
            Variable range = ncfileIn.findVariable("lon");
            List rdims = range.getDimensions();
            for (int n = 0; n < rdims.size(); n++) {
                System.out.println(rdims.get(n).toString());
            }

            System.out.println("\n\nLON DATATYPE: \n");
            System.out.println(range.getDataType().toString());

            // Read the data from all variables into an array, create an index and print some values
            System.out.println("\n\nBASE REFLECTIVITY DATA WITH RANGE AND AZIMUTH: \n");
//          Array data = bref.read(); ALREADY DEFINED
            Array azdata = azimuth.read();
            Array rdata = range.read();
//          Index index = data.getIndex(); ALREADY DEFINED
            Index azindex = azdata.getIndex();
            Index rindex = rdata.getIndex();
//          int[] shape = data.getShape(); ALREADY DEFINED
            // because bref is dependent on range and azimuth we can use this shape for all
            System.out.println("Data Array Dimensions: " + shape[0] + " , " + shape[1]);
//          for (int i=0; i<shape[0]; i++) {
//          for (int j=0; j<shape[1]; j++) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    System.out.println("i=" + i + " j=" + j + " lat=" + azdata.getDouble(azindex.set(i)) +
                            " lon=" + rdata.getDouble(rindex.set(j)) + " value=" + data.getDouble(index.set(0, i, j)));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    /**
     *  Description of the Method
     *
     * @param  outFile  Description of the Parameter
     * @param  inFile   Description of the Parameter
     * @return          Description of the Return Value
     */
    public static boolean saveNetCDF(String outFile, String inFile) {
        try {

            //NetcdfFile ncfileIn = ucar.nc2.dataset.NetcdfDataset.openFile(inFile, null);
            NetcdfFile ncfileIn = NetcdfFile.open(inFile);
            NetcdfFile ncfileOut = ucar.nc2.FileWriter.writeToFile(ncfileIn, outFile);
            ncfileOut.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }




    public static void checkNetCDFBounds() throws Exception {   

        File dir = new File("H:\\Nexrad_Viewer_Test\\TonyWimmers\\nc\\KMLB2");
        File[] files = dir.listFiles();
        for (int x=0; x<files.length; x++) {

            // Open a file readable by the NetCDF-Java library
            NetcdfFile ncfileIn = NetcdfFile.open(files[x].toString());
            //NetcdfFile ncfileIn = NetcdfFile.openInMemory(inFile, fileData);

            // List the attributes of the file (general information about data)
//          System.out.println("\n\nNetCDF ATTRIBUTES: \n");
            List globalAtts = ncfileIn.getGlobalAttributes();
            for (int n = 0; n < globalAtts.size(); n++) {
                System.out.println(globalAtts.get(n).toString());
            }

            // List the variables present in the file (gate, azimuth, value)
//          System.out.println("\n\nNetCDF VARIABLES: \n");
            List vars = ncfileIn.getVariables();
            for (int n = 0; n < vars.size(); n++) {
//              System.out.println(((Variable) (vars.get(n))).toString());
            }


            Variable value = ncfileIn.findVariable("value");
            List dims = value.getDimensions();
            for (int n = 0; n < dims.size(); n++) {
//              System.out.println(dims.get(n).toString());
            }


            Array data = value.read();
            Index index = data.getIndex();
            int[] shape = data.getShape();

            Variable lon = ncfileIn.findVariable("lon");
            Array londata = lon.read();
            Index lonindex = londata.getIndex();
            int[] lonshape = londata.getShape();

            Variable lat = ncfileIn.findVariable("lat");
            Array latdata = lat.read();
            Index latindex = latdata.getIndex();
            int[] latshape = latdata.getShape();


            System.out.println("Data Array Dimensions: " + shape[1] + " , " + shape[2]);
            System.out.println("i=0 j=0 lat=" + latdata.getDouble(latindex.set(0)) +
                    " lon=" + londata.getDouble(lonindex.set(0)) + " value=" + data.getDouble(index.set(0, 0, 0)));


            ncfileIn.close();

        }

    }


    public static void describeQPEFile(String inFile) {

        try {

            // Open a file readable by the NetCDF-Java library
            NetcdfFile ncFile = NetcdfFile.open(inFile);

            // List the attributes of the file (general information about data)
            System.out.println("\n\nNetCDF ATTRIBUTES: \n");
            List globalAtts = ncFile.getGlobalAttributes();
            for (int n = 0; n < globalAtts.size(); n++) {
                System.out.println(globalAtts.get(n).toString());
            }

            // Print specific attribute
            Attribute latAtt = ncFile.findGlobalAttribute("Latitude"); 
            System.out.println("\n\nLATITUDE ATTRIBUTE = "+latAtt.getNumericValue());

            // List the variables present in the file (gate, azimuth, value)
            System.out.println("\n\nNetCDF VARIABLES: \n");
            List vars = ncFile.getVariables();
            for (int n = 0; n < vars.size(); n++) {
                System.out.println(((Variable) (vars.get(n))).toString());
            }


            // List the dimensions of the "value" array of data
            String variableName = "pcp_flag";
            System.out.println("\n\n"+variableName+" DIMENSIONS: \n");
            Variable var = ncFile.findVariable(variableName);
            List dims = var.getDimensions();
            for (int n = 0; n < dims.size(); n++) {
                System.out.println(dims.get(n).toString());
            }

            // Print the primitive datatype
            System.out.println("\n\n"+variableName+" DATATYPE: \n");
            System.out.println(var.getDataType().toString());

            // Read the data from the "value" array, create an index and print some values
            System.out.println("\n\n"+variableName+" DATA: \n");
            Array data = var.read();
            Index index = data.getIndex();
            int[] shape = data.getShape();
            System.out.println("Data Array Dimensions: " + shape[0] + " , " + shape[1]);
//          for (int i=0; i<shape[0]; i++) {
//          for (int j=0; j<shape[1]; j++) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    System.out.println("i=" + i + " j=" + j + " value=" + data.getDouble(index.set(i, j)));
                }
            }



        } catch (Exception e) {
            e.printStackTrace();
        }

    }







    public static void describeCASAFile(String inFile) {

        try {

            // Open a file readable by the NetCDF-Java library
            NetcdfFile ncFile = NetcdfFile.open(inFile);

            // List the attributes of the file (general information about data)
            /*         
         System.out.println("\n\nNetCDF ATTRIBUTES: \n");
         List globalAtts = ncFile.getGlobalAttributes();
         for (int n = 0; n < globalAtts.size(); n++) {
            System.out.println(globalAtts.get(n).toString());
         }

         // Print specific attribute
         Attribute latAtt = ncFile.findGlobalAttribute("Latitude"); 
         System.out.println("\n\nLATITUDE ATTRIBUTE = "+latAtt.getNumericValue());
             */
            // List the variables present in the file (gate, azimuth, value)
            System.out.println("\n\nNetCDF VARIABLES: \n");
            List vars = ncFile.getVariables();
            for (int n = 0; n < vars.size(); n++) {
                System.out.println(((Variable) (vars.get(n))).toString());
            }


            // List the dimensions of the "value" array of data
            String variableName = "Azimuth";
            System.out.println("\n\n"+variableName+" DIMENSIONS: \n");
            Variable var = ncFile.findVariable(variableName);
            List dims = var.getDimensions();
            for (int n = 0; n < dims.size(); n++) {
                System.out.println(dims.get(n).toString());
            }

            // Print the primitive datatype
            System.out.println("\n\n"+variableName+" DATATYPE: \n");
            System.out.println(var.getDataType().toString());

            // Read the data from the "value" array, create an index and print some values
            System.out.println("\n\n"+variableName+" DATA: \n");
            Array data = var.read();
            Index index = data.getIndex();
            int[] shape = data.getShape();
            System.out.println("Data Array Dimensions: " + shape[0]);
//          for (int i=0; i<shape[0]; i++) {
//          for (int j=0; j<shape[1]; j++) {
            for (int i = 0; i < shape[0]; i++) {
                System.out.println("i=" + i + " value=" + data.getDouble(index.set(i)));
            }



        } catch (Exception e) {
            e.printStackTrace();
        }

    }



}

