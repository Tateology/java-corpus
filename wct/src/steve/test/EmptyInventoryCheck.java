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

import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.Calendar;

public class EmptyInventoryCheck {

   public static void main(String[] args) {
   
      URL url1, url2, url3;
      Calendar calendar = Calendar.getInstance();
      calendar.clear();
      
      DecimalFormat fmt02 = new DecimalFormat("00");      
      
      int yyyy=2001;
      int mm=06;
      int dd=01;
      
      try {
         calendar.set(yyyy, mm-1, dd);
      
         for (int n=0; n<60; n++) {
            String id1 = "KFDX";
            url1 = new URL("http://www.ncdc.noaa.gov/nexradinv/displayfiles.jsp"+
               "?id="+id1+"&yyyymmdd="+calendar.get(Calendar.YEAR)+""+
               fmt02.format(calendar.get(Calendar.MONTH)+1)+""+ 
               fmt02.format(calendar.get(Calendar.DATE))+"&tz=GMT&product=ADL3N0R)");
         
            String id2 = "KCAE";
            url2 = new URL("http://www.ncdc.noaa.gov/nexradinv/displayfiles.jsp"+
               "?id="+id2+"&yyyymmdd="+calendar.get(Calendar.YEAR)+""+
               fmt02.format(calendar.get(Calendar.MONTH)+1)+""+ 
               fmt02.format(calendar.get(Calendar.DATE))+"&tz=GMT&product=ADL3N0R)");
               

            String id3 = "KLZK";
            url3 = new URL("http://www.ncdc.noaa.gov/nexradinv/displayfiles.jsp"+
               "?id="+id3+"&yyyymmdd="+calendar.get(Calendar.YEAR)+""+
               fmt02.format(calendar.get(Calendar.MONTH)+1)+""+ 
               fmt02.format(calendar.get(Calendar.DATE))+"&tz=GMT&product=ADL3N0R)");

               
            calendar.add(Calendar.DATE, 1);

            URLConnection conn1= url1.openConnection();
            int size1 = conn1.getContentLength();           
            URLConnection conn2= url2.openConnection();
            int size2 = conn2.getContentLength();           
            URLConnection conn3= url3.openConnection();
            int size3 = conn3.getContentLength();           
            
System.out.println(url1.toString().substring(69, 77)+
   " "+id1+": "+((size3 == -1) ? "YES   " : " NO   ")+
   " "+id2+": "+((size1 == -1) ? "YES   " : " NO   ")+
   " "+id3+": "+((size2 == -1) ? "YES   " : " NO   ")
   );         
            
            conn1.getInputStream().close();
            conn2.getInputStream().close();
            conn3.getInputStream().close();
            
            Thread.sleep(5000);               
            
            
            /*
            InputStream is = url.openStream();
            DataInputStream dis = new DataInputStream(new BufferedInputStream(is));
            
            String str;
            while ((str = dis.readLine()) != null) {
               System.out.println(url.toString().substring(69, 77)+"   "+size+"   "+str);
            }
            //BufferedWriter bw = new BufferedWriter(new FileWriter(new File("D:\\NexradViewerTest\\Inventory
            */
            
            
            
            
            
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}
