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

import gov.noaa.ncdc.nexrad.NexradEquations;
import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.io.WCTTransfer;

import java.awt.Frame;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.zip.GZIPInputStream;

import org.apache.tools.bzip2.CBZip2InputStream;

import ucar.unidata.io.UncompressInputStream;


/**
 *  Description of the Class
 *
 * @author     steve.ansari
 * created    November 10, 2004
 */
public class Level2Transfer {

   
   public static final File TEMP_DIR = new File(WCTConstants.getInstance().getDataCacheLocation());
   
   
   /**
    *  Gets the Level2 GZIP Nexrad file from NCDC FTP Server.  Decompresses file after retrieval.
    *  File must be in format: ex) 6500KATX20040829_002515.gz
    *
    * @param  nexradURL  URL of Level-2 GZIP Nexrad File
    * @return             URL for decompressed file
    */
   public static URL getNCDCLevel2GZIP(URL nexradURL) 
      throws java.net.ConnectException, IOException {
         
      return getNCDCLevel2GZIP(nexradURL, false);
   }
   /**
    *  Gets the Level2 GZIP Nexrad file from NCDC FTP Server.  Decompresses file after retrieval.
    *  File must be in format: ex) 6500KATX20040829_002515.gz
    *
    * @param  nexradURL  URL of Level-2 GZIP Nexrad File
    * @param  force       Perform operation if file already exists?
    * @return             URL for decompressed file
    */
   public static URL getNCDCLevel2GZIP(URL nexradURL, boolean force) 
      throws java.net.ConnectException, IOException {
         
      return getNCDCLevel2GZIP(nexradURL, force, null);
   }

   /**
    *  Gets the Level2 GZIP Nexrad file from NCDC FTP Server.  Decompresses file after retrieval.
    *  File must be in format: ex) 6500KATX20040829_002515.gz
    *
    * @param  nexradURL  URL of Level-2 GZIP Nexrad File
    * @param  force       Perform operation if file already exists?
    * @param  owner       The parent frame for interactive non-batch operations.  
    * If null, we default to batch mode and no pop-up.  Otherwise we get the 
    * download progress pop-up.
    * @return             URL for decompressed file
    */
   public static URL getNCDCLevel2GZIP(URL nexradURL, boolean force, Frame owner) 
      throws java.net.ConnectException, IOException {
       
           if (! TEMP_DIR.exists()) {
               TEMP_DIR.mkdirs();
           }
       
         // ---------------------------------------------------------
         // Get GZIP Compressed file and save to local tmp.dir
         // ---------------------------------------------------------
         nexradURL = WCTTransfer.getURL(nexradURL, force, owner);

         String urlString = nexradURL.toString();
         int index = urlString.lastIndexOf('/') + 1;
         String filename = urlString.substring(index, urlString.length());
         //File file = new File(TEMP_DIR.toString() + File.separator + filename);
         //File file = new File(urlString.replaceAll("file:/", ""));
         
         
         // ---------------------------------------------------------
         // Uncompress GZIP File
         // ---------------------------------------------------------
         //GZIPInputStream gzin = new GZIPInputStream(new FileInputStream(file));
         // Output file is same without the .gz
         //String fileString = file.toString();
         //File outFile = new File(fileString.substring(0, fileString.length() - 3));
         GZIPInputStream gzin = new GZIPInputStream(nexradURL.openStream());
         
         
         // Output filename is same without the .Z and is in user.tmp directory
         File outFile = new File(TEMP_DIR.toString() + File.separator + 
            filename.substring(0, filename.length() - 3));
         
         
         outFile.deleteOnExit();
         if (! outFile.exists() || force) {
            OutputStream out = new FileOutputStream(outFile);

            // Transfer bytes from the compressed file to the output file
            byte[] buf = new byte[1024];
            int len;
            while ((len = gzin.read(buf)) > 0) {
               out.write(buf, 0, len);
            }

            // Close the file and stream
            gzin.close();
            out.close();
         }
         
         
         // Return url of decompressed file
         return new URL(URLDecoder.decode(outFile.toURI().toURL().toString(), "UTF-8"));
//         return outFile.toURI().toURL();

   }
   
   /**
    *  Gets the Level2 UNIX .Z Compressed Nexrad file from NCDC FTP Server.  
    *  Decompresses file after retrieval.
    *  File must be in format: ex) 6500KATX20040829_002515.Z
    *
    * @param  nexradURL  URL of Level-2 UNIX .Z Compressed Nexrad File
    * @return             URL for decompressed file
    */
   public static URL getNCDCLevel2UNIXZ(URL nexradURL) 
      throws java.net.ConnectException, IOException {
         
      return getNCDCLevel2UNIXZ(nexradURL, false);
   }
   
   /**
    *  Gets the Level2 UNIX .Z Compressed Nexrad file from NCDC FTP Server.  
    *  Decompresses file after retrieval.
    *  File must be in format: ex) 6500KATX20040829_002515.Z
    *
    * @param  nexradURL  URL of Level-2 UNIX .Z Compressed Nexrad File
    * @param  force       Perform operation if file already exists?
    * @return             URL for decompressed file
    */
   public static URL getNCDCLevel2UNIXZ(URL nexradURL, boolean force) 
      throws java.net.ConnectException, IOException {

         return getNCDCLevel2UNIXZ(nexradURL, force, null);
   }

         
   /**
    *  Gets the Level2 UNIX .Z Compressed Nexrad file from NCDC FTP Server.  
    *  Decompresses file after retrieval.
    *  File must be in format: ex) 6500KATX20040829_002515.Z
    *
    * @param  nexradURL  URL of Level-2 UNIX .Z Compressed Nexrad File
    * @param  force       Perform operation if file already exists?
    * @param  owner       The parent frame for interactive non-batch operations.  
    * If null, we default to batch mode and no pop-up.  Otherwise we get the 
    * download progress pop-up.
    * @return             URL for decompressed file
    */
   public static URL getNCDCLevel2UNIXZ(URL nexradURL, boolean force, Frame owner) 
      throws java.net.ConnectException, IOException {

       if (! TEMP_DIR.exists()) {
           TEMP_DIR.mkdirs();
       }

         // ---------------------------------------------------------
         // Get UNIX .Z Compressed file and save to local tmp.dir
         // ---------------------------------------------------------
         nexradURL = WCTTransfer.getURL(nexradURL, force, owner);
         
         
         String urlString = nexradURL.toString();
         int index = urlString.lastIndexOf('/') + 1;
         String filename = urlString.substring(index, urlString.length());
         //File file = new File(TEMP_DIR.toString() + File.separator + filename);
         //File file = new File(urlString.replaceAll("file:/", ""));
         
         
         // ---------------------------------------------------------
         // Uncompress UNIX .Z File
         // ---------------------------------------------------------
         //InputStream zin = Compression.decompressStatic(new FileInputStream(file));
         //InputStream zin = new UncompressInputStream(new FileInputStream(file));
         // Output file is same without the .Z
         //String fileString = file.toString();
         //File outFile = new File(fileString.substring(0, fileString.length() - 2));
System.out.println("DECOMPRESSING: "+nexradURL);         

         InputStream zin = new UncompressInputStream(nexradURL.openStream());
         // Output filename is same without the .Z and is in user.tmp directory
         File outFile = new File(TEMP_DIR.toString() + File.separator + 
            filename.substring(0, filename.length() - 2));
         
         
         
         //File outFile = File.createTempFile(fileString.substring(fileString.length()-26, fileString.length() - 3), "");
//System.out.println("TEMP UNCOMPRESSED FILE: "+outFile);         
         outFile.deleteOnExit();
         if (! outFile.exists() || force) {
            OutputStream out = new FileOutputStream(outFile);

            // Transfer bytes from the compressed file to the output file
            byte[] buf = new byte[1024];
            int len;
            while ((len = zin.read(buf)) > 0) {
               out.write(buf, 0, len);
            }

            // Close the file and stream
            zin.close();
            out.close();
         }
         
         // Return url of decompressed file
         return new URL(URLDecoder.decode(outFile.toURI().toURL().toString(), "UTF-8"));
//         return outFile.toURI().toURL();
   }
   
   



   
   
   
   
   
   public static URL decompressAR2V0001(URL nexradURL) throws MalformedURLException, IOException {
      return decompressAR2V0001(nexradURL, null);
   }
   
   
   public static URL decompressAR2V0001(URL nexradURL, File uncompressedFile) throws MalformedURLException, IOException {   

      return decompressAR2V0001(nexradURL, uncompressedFile, false);
   }
      
   public static URL decompressAR2V0001(URL nexradURL, boolean overwrite) throws MalformedURLException, IOException {
      return decompressAR2V0001(nexradURL, null, overwrite);
   }
   
   
   public static URL decompressAR2V0001(URL nexradURL, File uncompressedFile, boolean overwrite) throws MalformedURLException, IOException {   

       if (! TEMP_DIR.exists()) {
           TEMP_DIR.mkdirs();
       }

      
      ucar.unidata.io.RandomAccessFile f;
         
         if (nexradURL.getProtocol().equals("file")) {
            f = new ucar.unidata.io.RandomAccessFile(new URL(URLDecoder.decode(nexradURL.toString(), "UTF-8")).getFile(), "r");
         }
         else {
            f = new ucar.unidata.io.http.HTTPRandomAccessFile(nexradURL.toString());
         }
         f.order(ucar.unidata.io.RandomAccessFile.BIG_ENDIAN);

         
         // Read format - old or bzipped new?
         f.seek(0);

         // volume scan header
         String dataFormat = f.readString(8);
         
         // format = "ARCHIVE2" (OLD) OR "AR2V0001" (NEW)         

         f.skipBytes(1);
         
         String volumeNo = f.readString(3);
         
         int gendate = f.readInt();
         int gentime = f.readInt();
         
         String yyyymmdd = String.valueOf(NexradEquations.convertJulianDate(gendate));
         String hhmmss = NexradEquations.convertMilliseconds(gentime);
         
         String urlString = nexradURL.toString();
         int index = urlString.lastIndexOf('/') + 1;
         String filename = urlString.substring(index, urlString.length());
            
         
         // new version of Level2 has icao embedded
         String stationId = f.readString(4).trim(); // only in AR2V0001
         
         //if (! stationId.equals(icao)) {
         //   throw new NexradDecodeException("Data Format: "+dataFormat+
         //      ": Internal ICAO ("+stationId+") does not match filename ICAO ("+icao+")", url);               
         //}
         
                  
         //see if we have to uncompress
         if (dataFormat.equals("AR2V0001")) {
            f.skipBytes(4);
            String BZ = f.readString(2);
         
            
            
         
            if (BZ.equals("BZ")) {
               ucar.unidata.io.RandomAccessFile uraf = null;
               
               
               if (uncompressedFile == null) {
                  String ufilename = "6500"+stationId+yyyymmdd+"_"+hhmmss; 
                  uncompressedFile = new File(Level2Transfer.TEMP_DIR.toString()+File.separator+
                  ufilename);
               }
                  
               if (uncompressedFile.exists() && ! overwrite) {
                  uraf = new ucar.unidata.io.RandomAccessFile(uncompressedFile.getPath(), "r");
               } else {
                  // nope, gotta uncompress it
                  //uraf = uncompress( raf, uncompressedFile.getPath(), debug);
                  uraf = uncompress( f, uncompressedFile.getPath(), false);
                  uraf.flush();
                  //if (debug) log.debug("flushed uncompressed file= "+uncompressedFile.getPath());
               }  
               // close files
               uraf.close();
               f.close();
               nexradURL = uncompressedFile.toURI().toURL();
            }

            //f.seek(Level2Format.FILE_HEADER_SIZE);
            
            
         }
         
      
//         return nexradURL;
         return new URL(URLDecoder.decode(nexradURL.toString(), "UTF-8"));
    
   }
   
   


  /**
   * Write equivilent uncompressed version of the file.
   * @param raf2  file to uncompress
   * @param ufilename write to this file
   * @return raf of uncompressed file
   * @throws IOException
   */
  private static ucar.unidata.io.RandomAccessFile uncompress( ucar.unidata.io.RandomAccessFile raf2, String ufilename, boolean debug)      
      throws IOException {
         
         
     
    ucar.unidata.io.RandomAccessFile dout2 = new ucar.unidata.io.RandomAccessFile(ufilename, "rw");
    raf2.seek(0);
    byte[] header = new byte[Level2Format.FILE_HEADER_SIZE];
    raf2.read(header);
    dout2.write(header);

    boolean eof = false;
    int numCompBytes;
    byte[] ubuff = new byte[40000];
    
    ByteArrayInputStream bis = null;
    CBZip2InputStream cbzip2 = null;
    
    try {
      while (!eof) {

        try {
          numCompBytes = raf2.readInt();
          if (numCompBytes == -1) {
            //if (debug) log.debug("  done: numCompBytes=-1 ");
            break;
          }
        } catch (EOFException ee) {
          //if (debug) log.debug("  got EOFException ");
          break; // assume this is ok
        }

        //if (debug) {
        //  log.debug("reading compressed bytes " + numCompBytes+" input starts at " + raf2.getFilePointer()+"; output starts at " + dout2.getFilePointer());
        //}
        /*
        * For some reason, the last block seems to
        * have the number of bytes negated.  So, we just
        * assume that any negative number (other than -1)
        * is the last block and go on our merry little way.
        */
        if (numCompBytes < 0) {
          //if (debug) log.debug("last block?"+numCompBytes);
          numCompBytes = -numCompBytes;
          eof = true;
        }
        byte[] buf = new byte[numCompBytes];
        raf2.readFully(buf);
        bis = new ByteArrayInputStream(buf, 2, numCompBytes - 2);

        cbzip2 = new CBZip2InputStream(bis);
        int total = 0;
        int nread;
        while ((nread = cbzip2.read(ubuff)) != -1) {
          dout2.write(ubuff, 0, nread);
          total += nread;
        }
        float nrecords = (float) (total/2432.0);
        //if (debug)
        //  log.debug("  unpacked "+total+" num bytes "+nrecords+" records; ouput ends at " + dout2.getFilePointer());
      }
    } catch (EOFException e) {
      e.printStackTrace();
    }

    // close input raf
    cbzip2.close();
    bis.close();
    raf2.close();
    
    System.out.println(raf2.getLocation());
    System.out.println(dout2.getLocation());
    
    dout2.flush();
    return dout2;
  }



  
  
  
  
  
   
   
   
   
   
   
}

