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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.DecimalFormat;
import java.util.Calendar;

import org.geotools.data.shapefile.dbf.DbaseFileReader;

/**
 *  Creates hashtables to represent lat, lon and elevation of ~160 nexrad sites.
 *  The included wsr.dbf attribute table for the wsr.shp shapefile is read to produce the hashtables.
 *  The dbffile wsr.dbf must be in the /shapefiles directory.<BR><BR>
 *  The ICAO (WSR ID) is used as the Hashtable key for all hashtables (must be all upper case).
 *
 * @author     steve.ansari
 * @created    October 5, 2004
 */
public class ReadDBF {


   public static final DecimalFormat fmt02 = new DecimalFormat("00");
   
   
   /**
    * Creates the hashtables for lat, lon and elev by reading
    * /shapefiles/wsr.dbf
    */
   public ReadDBF() {
      loadHashtables();
   }
   
   private void loadHashtables() {
    
      try {
         
         Calendar cal = Calendar.getInstance();
         Calendar prevCal = null;
         
         BufferedWriter bw = new BufferedWriter(new FileWriter(new File("H:\\SWDI\\StormData\\Heat_Summary.csv")));
         
         // write header
         bw.write("YYYYMM, Count, MM/YYYY");
         bw.newLine();
         
         String yyyymm;
         String prevDate = null;
         String displayDate;
         
         //URL url = NexradHashtables.class.getResource("/shapefiles/wsr.dbf");
         //URL url = new File("T:\\dbases\\VFP60\\events.dbf").toURL();
         URL url = new File("H:\\SWDI\\StormData\\Sum_Output_Heat.dbf").toURL();
         ReadableByteChannel in = getReadChannel(url);
         DbaseFileReader r = new DbaseFileReader( in );
         Object[] fields = new Object[r.getHeader().getNumFields()];
         while (r.hasNext()) {
            r.readEntry(fields);
            // do stuff
            //for (int i=0; i<fields.length; i++) {
            //   System.out.println(i+" ::: "+fields[i].toString());
            //}
            
            
            yyyymm = fields[0].toString();

            if (prevCal == null) {
               prevCal = Calendar.getInstance();
               prevCal.set(Integer.parseInt(yyyymm.substring(0, 4)), Integer.parseInt(yyyymm.substring(4, 6))-1, 1);
            }
            
            
            cal.set(Integer.parseInt(yyyymm.substring(0, 4)), Integer.parseInt(yyyymm.substring(4, 6))-1, 1);
            
            int curYear = cal.get(Calendar.YEAR);
            int curMonth = cal.get(Calendar.MONTH);
            int prevYear = prevCal.get(Calendar.YEAR);
            int prevMonth = prevCal.get(Calendar.MONTH);
            
            
            while(prevDate != null && ! (prevYear == curYear && prevMonth == curMonth)) {
                  prevCal.roll(Calendar.MONTH, 1); 
                  prevYear = prevCal.get(Calendar.YEAR);
                  prevMonth = prevCal.get(Calendar.MONTH);
                  
                  System.out.println("ROLLING TO: "+prevCal.get(Calendar.YEAR)+""+fmt02.format(prevCal.get(Calendar.MONTH)+1));
         
                  yyyymm = prevCal.get(Calendar.YEAR)+""+fmt02.format(prevCal.get(Calendar.MONTH)+1);
                  displayDate = fmt02.format(prevCal.get(Calendar.MONTH)+1)+"/"+prevCal.get(Calendar.YEAR);
                  
                  bw.write(yyyymm+",0,"+displayDate);
                  bw.newLine();
                  
            }
            

            displayDate = yyyymm.substring(4, 6)+"/"+yyyymm.substring(0, 4);
            bw.write(yyyymm+","+fields[1].toString()+","+displayDate);
            bw.newLine();
               
               
            //System.out.println(cal);
            
            
            
            //bw.write("");
            
               
            
            prevDate = yyyymm;
            
            prevCal.roll(Calendar.MONTH, 1);
               
         }
         r.close();
         
         
         bw.flush();
         bw.close();
         
         
      } catch (Exception e) {
         System.out.println("JNX HASHTABLES ERROR!");
         e.printStackTrace();
      }

   }

    
    /**
     * Obtain a ReadableByteChannel from the given URL. If the url protocol is
     * file, a FileChannel will be returned. Otherwise a generic channel will
     * be obtained from the urls input stream.
     */
    private ReadableByteChannel getReadChannel(URL url) throws IOException {
        ReadableByteChannel channel = null;
        if (url.getProtocol().equals("file")) {
            File file = new File(java.net.URLDecoder.decode(url.getFile(), "UTF-8"));
            if (! file.exists() || !file.canRead()) {
                throw new IOException("File either doesn't exist or is unreadable : " + file);
            }
            FileInputStream in = new FileInputStream(file);
            channel = in.getChannel();
        } else {
            InputStream in = url.openConnection().getInputStream();
            channel = Channels.newChannel(in);
        }
        return channel;
    }
    
   
   
   
   
   public static void main(String[] args) {
    
      System.out.println("TESTING NexradHashtables Object");
      ReadDBF dbf = new ReadDBF();
      System.out.println("FINISHED TESTING");
   }
   
}

