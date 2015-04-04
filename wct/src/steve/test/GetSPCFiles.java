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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Calendar;

//import gov.noaa.ncdc.common.*;

/**
 *  Description of the Class
 *
 * @author    steve.ansari
 */
public class GetSPCFiles {

   private final static String SPC_DIR = "http://www.spc.noaa.gov/climo/reports";
   private final static String OUTPUT_DIR = "H:\\SWDI\\SPC";

   private final static DecimalFormat fmt02 = new DecimalFormat("00");


   /**
    *  Gets the files attribute of the GetSPCFiles object
    *
    * @exception  IOException  Description of the Exception
    */
   private void getFiles() throws IOException {

      //String begDate = "20040101"
      //String endDate = "200601


      Calendar cal = Calendar.getInstance();
      cal.set(2004, 01 - 1, 01);

      while (!(cal.get(Calendar.YEAR) == 2005 && cal.get(Calendar.MONTH) + 1 == 01)) {


         String yearString = "" + cal.get(Calendar.YEAR);
         String year = yearString.substring(2, 4);
         String month = fmt02.format(cal.get(Calendar.MONTH) + 1);
         String date = fmt02.format(cal.get(Calendar.DATE));
         String fileString = year + "" + month + "" + date + "_rpts_torn.csv";

         URL url = new URL(SPC_DIR + "/" + fileString);
         File file = new File(OUTPUT_DIR + File.separator + fileString);

         if (!file.exists()) {

            System.out.println("GETTING: " + url);
            //System.exit(1);

            try {

               DataOutputStream os = new DataOutputStream(new BufferedOutputStream(
                     new FileOutputStream(file), 1 * 1024));


               BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
               
               String str;
               while ((str = in.readLine()) != null) {
                
                  
                  // Do the stuff
                  String[] columns = str.split(",");
                  
                  // 
                  
                  // |Col1|Col2|.etc...|2001|8307|LON|LAT|
               }
                     
                     

                     /*
               URLTransfer urlTransfer = new URLTransfer(url, os);
               try {
                  urlTransfer.run();
                  urlTransfer.close();
               } catch (Exception e) {
                  urlTransfer.close();
                  os.flush();
                  os.close();
                  throw new java.net.ConnectException();
               }
               */
               
               
               
               in.close();
               os.flush();
               os.close();

            } catch (Exception e) {
               System.err.println("ERROR GETTING: " + url);
               file.delete();
            }

            try {
               Thread.sleep(2000);
            } catch (Exception e) {}

         }


         cal.add(Calendar.DATE, 1);


      }


   }



   /**
    *  Description of the Method
    *
    * @param  in               Description of the Parameter
    * @param  out              Description of the Parameter
    * @exception  IOException  Description of the Exception
    */
   public static void copy(InputStream in, OutputStream out) throws IOException {

      try {
         int length = in.available();
         // danger!
         byte[] bytes = new byte[length];
         in.read(bytes);
         out.write(bytes);
      } finally {
         if (in != null) {
            in.close();
         }
         if (out != null) {
            out.close();
         }
      }
   }



   /**
    *  The main program for the GetSPCFiles class
    *
    * @param  args  The command line arguments
    */
   public static void main(String[] args) {

      try {

         GetSPCFiles spc = new GetSPCFiles();
         spc.getFiles();

      } catch (Exception e) {
         e.printStackTrace();
      }

   }

}

