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

import java.util.Vector;

import ucar.unidata.io.RandomAccessFile;



public class Level2AsciiRLEFormat {
   
   public final static int VALUE_SNR = 0;
   public final static int VALUE_AP1 = 1;
   public final static int VALUE_AP2 = 2;
   
   
   
   public final static int RLE_FLAG = 1;
   // Sweep Header Index for number of sweeps
   public final static int SHI_NUMSWEEPS = 6; 
   // Sweep Header Index for number of rays in sweep  
   public final static int SHI_NUMRAYS = 7; 
   // Sweep Header Index for range to first gate in meters
   public final static int SHI_RANGETOFIRSTGATE = 9;    
   // Sweep Header Index for gate size in meters
   public final static int SHI_GATESIZE = 10; 
   // Sweep Header Index for elevation of current sweep
   public final static int SHI_ELEVANGLE = 16;    
   // Sweep Header Index for max number of bins in sweep
   public final static int SHI_NUMBINS = 40;      
   
   
   
   private long[] sweepIndices;
   private int numSweeps;
   private RandomAccessFile f;
   private Vector currentSweepHeader, currentSweepRays;
   
   public Level2AsciiRLEFormat(RandomAccessFile f) 
      throws java.io.IOException {
         
      this.f = f;
      this.sweepIndices = indexSweeps(f);
      currentSweepHeader = decodeSweepHeader(f);
   }
   
   public void readSweep(int sweep)
      throws java.io.IOException {
         
      if (sweep > sweepIndices.length) {
         System.err.println("ERROR -- "+sweepIndices.length+" SWEEPS IN FILE.  REQUESTED ZERO-BASED SWEEP: "+sweep);
         return;
      }
      // position file pointer at desired sweep
      f.seek(sweepIndices[sweep]);
      currentSweepHeader = decodeSweepHeader(f);
      currentSweepRays = decodeSweepRays(f);
   }
   
   public double getSweepElevation() {
      return Double.parseDouble((String)(currentSweepHeader.elementAt(SHI_ELEVANGLE))); 
   }
   
   public int getNumberOfRadials() {
      return Integer.parseInt((String)(currentSweepHeader.elementAt(SHI_NUMRAYS))); 
   }   
   
   public double getGateSize() {
      return Double.parseDouble((String)(currentSweepHeader.elementAt(SHI_GATESIZE))); 
   }
   
   public double getRangeToFirstGate() {
      return Double.parseDouble((String)(currentSweepHeader.elementAt(SHI_RANGETOFIRSTGATE))); 
   }
   
   public Vector getCurrentSweepRays() {
      return currentSweepRays;
   }
   
   public SweepRay getSweepRay(int index) {
      if (index < 0 || index+1 > currentSweepRays.size()) {
         return null;
      }
      else {
         return (SweepRay)(currentSweepRays.elementAt(index));
      }
   }
   
   // max number of bins in current sweep
   public int getNumberOfBins() {
      return Integer.parseInt((String)(currentSweepHeader.elementAt(SHI_NUMBINS))); 
   }
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   public static Vector decodeSweepHeader(RandomAccessFile f) 
      throws java.io.IOException {
   
      return decodeSweepHeader(f.readLine(), f);      
   }
   
   
   public static Vector decodeSweepHeader(String firstLine, RandomAccessFile f) 
      throws java.io.IOException {
 
         // Read ITR Ascii RLE Header information

         // read first line and check how other header lines are present
         String[] firstLineArray = firstLine.split(" "); 
         int numHeaderLines = Integer.parseInt(firstLineArray[0]);
         String[][] headerLines = new String[numHeaderLines][];
         headerLines[0] = firstLineArray;
         for (int n=1; n<numHeaderLines; n++) {
            headerLines[n] = f.readLine().split(" ");         
         }
         
         // add elements in sweep header to Vector
         Vector sweepHeader = new Vector();
         for (int n=0; n<numHeaderLines; n++) {
            for (int i=0; i<headerLines[n].length; i++) {
               sweepHeader.addElement(headerLines[n][i]);
            }
         }
         
         return sweepHeader;

   }
   
   /**
    * Returns Vector of SweepRay objects -- must be called directly after "decodeSweepRays" method
    **/
   public static Vector decodeSweepRays(RandomAccessFile f) 
      throws java.io.IOException {
      
         System.out.println("DECODING RLE RAY");
         
      Vector sweepRays = new Vector();   
         
      String str = f.readLine();   
      String[] rayHeader = str.split(" ");
      
 System.out.println(rayHeader[1]+" array length: "+rayHeader.length);
 
      while (rayHeader.length == 4) {
         int rayNumber = Integer.parseInt(rayHeader[0]);
         double azimuth = Double.parseDouble(rayHeader[1])/64.0;
         int numRLECodes = Integer.parseInt(rayHeader[2]);
         double timeDiff = Double.parseDouble(rayHeader[3]);
         //String rleData = f.readLine();
         
         SweepRay ray = new SweepRay(rayNumber, azimuth, numRLECodes, timeDiff);
         
         int rleTotalCount = 0;
         // loop through each line of rle codes for this ray
         int [] decodedInts = new int[numRLECodes];
         while (rleTotalCount < numRLECodes) {
         
            int countChar = f.readUnsignedByte()-33;
  //System.out.println("COUNT: "+countChar);
            int rleCount = 0;
            byte[] bytes = new byte[4];
            // loop through each set of 3 bytes of 4 characters
            while (rleCount < countChar) {

                  
               f.read(bytes);
               int[] rle = UUDecode.decodeRLEString(new String(bytes));
               
               for (int i=0; i<rle.length; i++) {
                  if (rleTotalCount+rleCount+i < decodedInts.length) {
                     decodedInts[rleTotalCount+rleCount+i] = rle[i];
                  }
               }
               rleCount+=3;
            }
            // add to the total rle count
            rleTotalCount+=countChar;
            // advance file pointer past white space \n or \r\n, etc...
            f.readLine(); 
         }
               



         int val;
         int run;
         int runCount = 0;
            
         for (int i=0; i<decodedInts.length; i++) {
                  //System.out.println("  RLE INT "+i+": "+decodedInts[i]);
                                       // if(true)break;
               
               
               val = decodedInts[i];
               // check for rle
               if (val == RLE_FLAG) {
                  run = decodedInts[++i];                 
                  if (run == 0) {
                     run = 1;
                     val = 1;
                     //System.out.println(" 0 RUN 1 , VAL 1  ***");
                  }
                  else {
                     val = decodedInts[++i];
                     //System.out.println(" 1 RUN "+run+" , VAL "+val);
                  }
               }
               else {
                  run = 1;
                  //System.out.println(" 2 RUN 1 , VAL "+val);
               }
            
               ray.addRLEBin(new RLEBin(runCount, run, val));
               runCount+=run;

        }
        
        //System.out.println("NUMBER OF INTS: "+decodedInts.length);
        //if(true)break;
         

  //System.out.println("RAY: "+ray);
  //Vector bins = ray.getRLEBinVector();
  //for (int i=0; i<bins.size(); i++) {
  //   System.out.println("BIN "+i+":   "+bins.elementAt(i).toString());
  //}  
  //          if(true)break;
         
         
         sweepRays.addElement(ray);
         
         // read next line and check for next ray header
         rayHeader = f.readLine().split(" ");
         
         
         //throw new java.io.IOException();        
      }
      
      System.out.println(sweepRays.size()+" RAYS DECODED");
      
      return sweepRays;
   }
   
   

   
   
   
   public static long[] indexSweeps(RandomAccessFile f)  
      throws java.io.IOException {
         
System.out.println("INDEXING ASCII-RLE FILE");         
         
         Vector headerIndices = new Vector();
         // rewind
         f.seek(0);


         String str = null;
         long filePos = 0;
         int numSweeps, numRays;
         try {
            
            boolean firstTime = true;
            // Add first location
            headerIndices.addElement(new Long(0));
            // decode sweep header
            Vector sweepHeader = decodeSweepHeader(f);
            // get number of rays for this sweep
            numSweeps = Integer.parseInt((String)(sweepHeader.elementAt(SHI_NUMSWEEPS)));            
            numRays = Integer.parseInt((String)(sweepHeader.elementAt(SHI_NUMRAYS)));            
            // loop through sweeps
            for (int i=0; i<numSweeps; i++ ) {   
               if (! firstTime) {
                  sweepHeader = decodeSweepHeader(str, f); // read sweep with first line already read
                  numRays = Integer.parseInt((String)(sweepHeader.elementAt(SHI_NUMRAYS))); 
               }
               firstTime = false;
               
               // loop through rays
               int cnt = 0;
               while (cnt<numRays+1) {
                  filePos = f.getFilePointer();
                  // read ray header
                  if ((str = f.readLine()) == null) {
                     throw new java.io.EOFException();
                  }
                  
                  if (str.indexOf(" ") > 0) {
                     //System.out.println(str);
                     cnt++;
                  }             
               }
               //System.in.read();
               // Add location of start of last line read
               headerIndices.addElement(new Long(filePos));                     
            }
         } catch (java.io.EOFException eof) {
            //System.out.println("END OF FILE FOUND");
            // rewind
            f.seek(0);   
         }
           
         // rewind
         f.seek(0);   
         
         
         
         
         long[] indices = new long[headerIndices.size()];
         for (int n=0; n<headerIndices.size(); n++) {
            indices[n] = ((Long)(headerIndices.elementAt(n))).longValue();
         }
         
         return indices;         
   }
   
   
   
   
   
   public static class SweepRay {
      
      public int rayNumber;
      public double azimuth;
      public int numRLECodes;
      public double timeDiff;
      public Vector rleBins = new Vector();
      
      public SweepRay (int rayNumber, double azimuth, int numRLECodes, double timeDiff) {
         this.rayNumber = rayNumber;
         this.azimuth = azimuth;
         this.numRLECodes = numRLECodes;
         this.timeDiff = timeDiff;
      }
      
      public void addRLEBin(RLEBin rleBin) {
         rleBins.addElement(rleBin);
      }
      
      public Vector getRLEBinVector() {
         return rleBins;
      }
      
      public int getNumberOfBins() {
         return rleBins.size();
      }
      
      public String toString() {
         return ("Ray Number: "+rayNumber+" , Azimuth: "+azimuth+
            " , Num. RLE Codes: "+numRLECodes+" , Time Offset:"+timeDiff+
            " , Num. RLE Bin Objects: "+rleBins.size());
      }
   }  
   
   
   
   public static class RLEBin {
      public int startBin;
      public int binLength;
      public int value;
      
      public RLEBin(int startBin, int binLength, int value) {
         this.startBin = startBin;
         this.binLength = binLength;
         this.value = value;
      }
      public String toString() {
         return ("Start Bin: "+startBin+" , Bin Length: "+binLength+
            " , Value: "+value);
      }      
   }
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   public static void main(String[] args) {
      try {

         java.io.File file = new java.io.File("C:\\Documents and Settings\\ncdc.laptop\\My Documents\\nexrad-data\\ITR\\4piotr\\testdata.rle");
         java.net.URL url = file.toURL();
         
         //*
         ucar.unidata.io.RandomAccessFile f;
         if (url.getProtocol().equals("file")) {
            f = new ucar.unidata.io.RandomAccessFile(url.getFile().replaceAll("%20", " "),"r");
         }
         else {
            f = new ucar.unidata.io.http.HTTPRandomAccessFile(url.toString());
         }
         f.order(ucar.unidata.io.RandomAccessFile.BIG_ENDIAN);
         
         //*/
         //RandomAccessFile f = new RandomAccessFile(file, "r"); // open as read-only
         Level2AsciiRLEFormat rleFormat = new Level2AsciiRLEFormat(f);
         rleFormat.readSweep(0);
         
         
         
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

}
