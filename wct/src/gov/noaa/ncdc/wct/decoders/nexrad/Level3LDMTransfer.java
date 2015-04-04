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

package gov.noaa.ncdc.wct.decoders.nexrad;

import gov.noaa.ncdc.common.URLTransfer;
import gov.noaa.ncdc.wct.decoders.DecodeException;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
/**
 *  Description of the Class
 *
 * @author     steve.ansari
 * created    November 10, 2004
 */
public class Level3LDMTransfer {

   /**
    *  Gets the Level3 BZIP Compressed Nexrad file in Unidata LDM/IDD BZIP Compression Format.  
    *  Decompresses file after retrieval.  
    *
    * @param  nexrad_url  URL of Level-3 LDM/IDD Nexrad File
    * @return             URL for decompressed file
    */
   public static URL decompressLevel3(URL nexrad_url) {

      try {
         // ---------------------------------------------------------
         // Get LDM/IDD BZIP Compressed file and save to local tmp.dir
         // ---------------------------------------------------------
                     String urlString = nexrad_url.toString();
                     int index = urlString.lastIndexOf('/') + 1;
                     String filename = urlString.substring(index, urlString.length());
                     File file = new File(System.getProperty("java.io.tmpdir") + File.separator + filename + ".ldm");
//                     file.deleteOnExit();
                     //if (! file.exists() || force) {
                        DataOutputStream os = new DataOutputStream(new BufferedOutputStream(
                           new FileOutputStream(file), 1 * 1024));
                           
                        URLTransfer urlTransfer = new URLTransfer(nexrad_url, os);
                        urlTransfer.run();

                        os.flush();
                        os.close();
                     //}
                       
         
                     nexrad_url = file.toURL();
                     
      ucar.unidata.io.RandomAccessFile f = null;         
                     
      // Initiate binary buffered read
      try {
         if (nexrad_url.getProtocol().equals("file")) {
            f = new ucar.unidata.io.RandomAccessFile(nexrad_url.getFile().replaceAll("%20", " "),"r");
         //f.order(ucar.unidata.io.RandomAccessFile.BIG_ENDIAN);
         }
         else {
            f = new ucar.unidata.io.http.HTTPRandomAccessFile(nexrad_url.toString());
            //f = new ucar.unidata.io.http.HTTPRandomAccessFile3(url.toString());
         }
         f.order(ucar.unidata.io.RandomAccessFile.BIG_ENDIAN);
         
         
//System.out.println("IIIIIIIIIIIIIIII FILE SIZE: "+f.length());
         
      } catch (Exception e) {
         throw new DecodeException("CONNECTION ERROR ERROR", nexrad_url);
      }
                     
      
      /*
      System.out.println(f.readLine());
      System.out.println(f.getFilePointer());
      
      
      if (true) return nexrad_url;
      */
      
  
/*      
      
      byte[] headerbytes = new byte[30];   
      f.read(headerbytes);
      System.out.println(new String(headerbytes));
      
      System.out.println("AWIPS UNCOMPRESSED HEADER LENGTH = "+f.getFilePointer());


      // write header to final file      
      File outFile = new File(System.getProperty("java.io.tmpdir") + File.separator + filename);                     
//      outFile.deleteOnExit();
      OutputStream out = new FileOutputStream(outFile);
      out.write(headerbytes);
      out.flush();
      
      // write compressed part of file out
      File zFile = new File(System.getProperty("java.io.tmpdir") + File.separator + filename + ".ldm.Z");                     
//      zFile.deleteOnExit();
      OutputStream zout = new FileOutputStream(zFile);
      // Read into buffer and write to output file
      byte[] buf = new byte[1024];
      while (f.read(buf) > 0) {
          zout.write(buf);
      }         
      f.close();
      zout.flush();
      zout.close();
      
                     
         // ---------------------------------------------------------
         // Strip AWIPS Header and Uncompress BZIP NIDS File
         // ---------------------------------------------------------
         
         
         FileInputStream raw_fis = new FileInputStream(zFile);
         //raw_fis.skip(50); // skip AWIPS header
         //InputStream fis = Compression.decompressStatic(raw_fis);
         org.apache.tools.bzip2.CBZip2InputStream fis = new org.apache.tools.bzip2.CBZip2InputStream( raw_fis );

         
         
         //byte[] buf = new byte[1024];
         //File outFile = new File(System.getProperty("java.io.tmpdir") + File.separator + filename);                     
         //outFile.deleteOnExit();
         //OutputStream out = new FileOutputStream(outFile);
         
         // Read into buffer and write to output file
         while (fis.read(buf) > 0) {
            out.write(buf);
         }
         
         fis.close();
         out.flush();
         out.close();
         



System.out.println("LDMFILE: "+file);      
System.out.println("OUTFILE: "+outFile);      
System.out.println("ZFILE: "+zFile);      

*/



      // strip off the uncompressed wmo header for later reattachment
      byte[] zdata = new byte[50];
      f.read(zdata);
      String header = new String(zdata);
      int wmoStart = header.indexOf("SDUS");
      byte[] wmoHeader = new byte[30];
      System.arraycopy(zdata, wmoStart, wmoHeader, 0, 30);
      
//System.out.println(header);      
//System.out.println("WMO START: "+wmoStart);      
//System.out.println(new String(wmoHeader));      
      
      
//      f.read(zdata);
      // get uncompressed NIDS data using decoder in NetCDF library      
      ucar.nc2.iosp.nids.JNXNidsheader nids = new ucar.nc2.iosp.nids.JNXNidsheader();
      byte[] data = nids.readZLibNIDS(f);
      
      File outFile = new File(System.getProperty("java.io.tmpdir") + File.separator + filename);                     
//      outFile.deleteOnExit();
      OutputStream out = new FileOutputStream(outFile);
      out.write(wmoHeader);
      out.write(data);
      out.flush();
      out.close();


         
         
         /*
            ucar.unidata.io.RandomAccessFile f = 
               new ucar.unidata.io.RandomAccessFile(file.toString(), "r");
               
               
                     long fileLength = f.length();
                     byte[] compressedData = new byte[1024];
                     f.seek(61); // skip AWIPS header

            File outFile = new File(System.getProperty("java.io.tmpdir") + File.separator + filename);                     
            outFile.deleteOnExit();
            OutputStream out = new FileOutputStream(outFile);
            
            
                     Inflater decompressor = new Inflater();
                     decompressor.setInput(compressedData);
                     // Create an expandable byte array to hold the decompressed data
                     ByteArrayOutputStream bos = new ByteArrayOutputStream(compressedData.length);
                     
                     while (f.getFilePointer() <= f.length()) {
                        f.read(compressedData);
                        
System.out.println("CUR FP: "+ f.getFilePointer() + "   LENGTH: "+f.length());                        
    
                        // Decompress the data
                        byte[] buf = new byte[1024];
                        while (!decompressor.finished()) {
                           try {
System.out.println("STUCK IN WHILE LOOP");                              
                              int count = decompressor.inflate(buf);
                              bos.write(buf, 0, count);
                           } catch (DataFormatException e) {
                           }
                        }
                        try {
                           bos.close();
                        } catch (IOException e) {
                        }
    
                        // Get the decompressed data
                        //byte[] decompressedData = bos.toByteArray();
                        out.write(bos.toByteArray());
                        
                     }
                     out.flush();
                     out.close();
                   
                     f.close();
                     
                  
*/
         
         // Return url of decompressed file
         return outFile.toURL();

      } catch (Exception e) {
         e.printStackTrace();
      }

      return null;
   }
   
   
//   /**
//    * Deletes all Level-2 files in the user.tmp directory that are older than 60 minutes
//    */
//   public static void clearTempDirectory() throws DecodeException, MalformedURLException {
//
////System.out.println("CLEARING TEMP DIRECTORY!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");      
//      
//       FileScanner fileScanner = new FileScanner();
//       
//      File tmpdir = new File(System.getProperty("java.io.tmpdir"));
//      File[] files = tmpdir.listFiles();
//      for (int i=0; i<files.length; i++) {
////System.out.println("TEMP FILE::: "+files[i].toString());         
//         fileScanner.scanURL(files[i].toURL());
//         if (fileScanner.getLastScanResult().getFileType() == FileScanner.NEXRAD_LEVEL2) {
//            if (System.currentTimeMillis()-files[i].lastModified() > 60*60*1000) {
////               System.out.println("DELETING ::::::::::::: "+files[i].toString());
//               files[i].delete();
//            }
//         }         
//      }      
//   }

   
   
   
   
   
   public static void main(String[] args) {
      try {
         //DecodeL3Header header = new DecodeL3Header();
         //URL url = new URL("http://www1.ncdc.noaa.gov/pub/has/HAS000155503/7000KDTX_SDUS53_N0RDTX_200105252102");
         //URL url = new java.io.File("C:\\ViewerData\\HAS999900001\\7000KCLE_SDUS81_DPACLE_200211102333").toURL();         
         URL url = new java.io.File("D:\\Nexrad_Viewer_Test\\AntonKruger\\data\\ldm\\7000PGUM_SDUS50_N1PGUA_200411291858").toURL();
         
         URL newurl = decompressLevel3(url);
         
         DecodeL3Header header = new DecodeL3Header();
         header.decodeHeader(newurl);
         
         System.out.println("ICAO: "+header.getICAO());
         System.out.println(" LAT: "+header.getLat());
         System.out.println(" LON: "+header.getLon());
         
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
   
   
   
}

