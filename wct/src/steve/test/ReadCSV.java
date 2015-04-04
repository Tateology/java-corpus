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

public class ReadCSV {


   public static final DecimalFormat fmt02 = new DecimalFormat("00");

   public ReadCSV() {
      readData();
   }
   
   private void readData() {
    
      String delimiter = ",";
      
      String prevStr = null;
      String str = null;
      String dateSec = null;
      String[] cols = null;
      String[] dateCols = null;
      int cnt = 0;
      try {
         
         
         //URL url = NexradHashtables.class.getResource("/shapefiles/wsr.dbf");
         File file = new File("H:\\SWDI\\StormData\\eventsText.csv");
         
         String[] exts = new String[] {"Torn", "Hail", "Wind", "Flood", "Winter", "Heat", "Drought", "Lightning", "Other"};
         File[] outFiles = new File[exts.length];
         for (int n=0; n<exts.length; n++) {
            outFiles[n] = new File("H:\\SWDI\\StormData\\eventsESRI-"+exts[n]+".csv");
         }
         
         BufferedReader br = new BufferedReader(new FileReader(file));
         BufferedWriter[] writers = new BufferedWriter[exts.length];
         for (int n=0; n<outFiles.length; n++) {
            writers[n] = new BufferedWriter(new FileWriter(outFiles[n]));
         }
         
         
         
         
         while ((str = br.readLine()) != null) {
            cnt++;
            
            try {
               
                  //System.out.println(str);
                  //System.exit(1);
          
               //str = str.replaceAll("\"","").replaceAll("_","").replaceAll("/","").replaceAll(":","");
            
               cols = str.split(delimiter);

               
               

               BufferedWriter bw;
               if (cols.length == 35 && (
                  cols[7].toUpperCase().indexOf("TORNADO") != -1 ||
                  cols[7].toUpperCase().indexOf("FUNNEL") != -1)
               ) {
                  bw = writers[0];
               }
               else if (cols.length == 35 && cols[7].toUpperCase().indexOf("HAIL") != -1) {
                  bw = writers[1];
               }
               else if (cols.length == 35 && cols[7].toUpperCase().indexOf("WIND") != -1) {
                  bw = writers[2];
               }
               else if (cols.length == 35 && (
                  cols[7].toUpperCase().indexOf("FLOOD") != -1 ||
                  cols[7].toUpperCase().indexOf("HEAVY RAIN") != -1)
               ) {
                  bw = writers[3];
               }
               else if (cols.length == 35 && (
                  cols[7].toUpperCase().indexOf("SNOW") != -1 ||
                  cols[7].toUpperCase().indexOf("FREEZING") != -1 ||
                  cols[7].toUpperCase().indexOf("BLIZZARD") != -1 ||
                  cols[7].toUpperCase().indexOf("WINTER") != -1 ) 
               ) {
                  bw = writers[4];
               }
               else if (cols.length == 35 && (
                  cols[7].toUpperCase().indexOf("HEAT") != -1 ) 
               ) {
                  bw = writers[5];
               }
               else if (cols.length == 35 && (
                  cols[7].toUpperCase().indexOf("DROUGHT") != -1 ) 
               ) {
                  bw = writers[6];
               }
               else if (cols.length == 35 && (
                  cols[7].toUpperCase().indexOf("LIGHTNING") != -1 ) 
               ) {
                  bw = writers[7];
               }
               else {
                  bw = writers[8];
               }
                     
                     
               
               
               
               
               if (cnt == 1) {
                  for (int n=0; n<writers.length; n++) {
                     writers[n].write("\"YYYYMM\"");
                     writers[n].write(delimiter);
                     writers[n].write(str);
                     writers[n].newLine();
                  }
            
                  
               }
               else {
                  //System.out.println("NUMBER OF COLS: "+cols.length);
                  //System.out.println(str);
                  //System.exit(1);
                  
                  
                  if (cols.length == 35) {

                     dateSec = cols[1].split(" ")[0];
                     dateCols = dateSec.split("/");

                     bw.write("\""+fmt02.format(Integer.parseInt(dateCols[0]))+"/"+dateCols[2]+"\"");
                     bw.write(delimiter);
                     
                     
                     if (cols.length == 35) {
                        bw.write(str);
                        bw.newLine();
                     }
            
                     
                  }
               }
               
               //if (cnt == 5) break;
            
               prevStr = str;
               
               
               
            } catch (Exception e) {
               System.out.println("ERROR ON ROW "+cnt+", NUMBER OF COLS: "+cols.length);
               //throw e;
            }
            
         }
         
         br.close();
         for (int n=0; n<writers.length; n++) {
            writers[n].flush();
            writers[n].close();
         }
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
      ReadCSV csv = new ReadCSV();
      System.out.println("FINISHED TESTING");
   }
   
}

