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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DecimalFormat;

public class ReadFixed {


   public static final DecimalFormat fmt02 = new DecimalFormat("00");

   public ReadFixed() {
      readData();
   }
   
   private void readData() {
    
      
      String prevStr = null;
      String str = null;
      String dateSec = null;
      String[] cols = null;
      String[] dateCols = null;
      int cnt = 0;
      try {
         
         
         //URL url = NexradHashtables.class.getResource("/shapefiles/wsr.dbf");
         File file = new File("H:\\SWDI\\StormData\\eventsText4.txt");
         File outfile = new File("H:\\SWDI\\StormData\\eventsESRI.csv");
         BufferedReader br = new BufferedReader(new FileReader(file));
         BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
         while ((str = br.readLine()) != null) {
            cnt++;
            
            try {
          
               if (cnt == 0) {
                  bw.write("\"YYYYMM\"");
               }
               else {
                  bw.write("999901");
               }
               
               bw.write(str);
               bw.newLine();
            
               if (cnt == 5) {
                  break;
               }
            
               prevStr = str;
               
            } catch (Exception e) {
               System.out.println("ERROR ON ROW "+cnt);
               //throw e;
            }
            
         }
         
         br.close();
         bw.flush();
         bw.close();
      } catch (Exception e) {
         System.out.println();
         //System.out.println("ERROR! ROW NUMBER: "+cnt+" ::: DATE SEC: "+dateSec+" ::: NUM COLS: "+cols.length);
         System.out.println("ERROR! ROW NUMBER: "+cnt+" ::: DATE SEC: "+dateSec);
         System.out.println(prevStr);
         System.out.println(str);
         System.out.println();
         e.printStackTrace();
      }

   }

    
   
   
   
   
   public static void main(String[] args) {
    
      System.out.println("TESTING");
      ReadFixed fixed = new ReadFixed();
      System.out.println("FINISHED TESTING");
   }
   
}

