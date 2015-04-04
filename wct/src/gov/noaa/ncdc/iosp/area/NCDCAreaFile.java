/**
*      Copyright (c) 2007-2010 Work of U.S. Government.
*      No rights may be assigned.
*
* LIST OF CONDITIONS
* Redistribution and use of this program in source and binary forms, with or
* without modification, are permitted for any purpose (including commercial purposes) 
* provided that the following conditions are met:
*
* 1.  Redistributions of source code must retain the above copyright notice,
*     this list of conditions, and the following disclaimer.
*
* 2.  Redistributions in binary form must reproduce the above copyright notice,
*     this list of conditions, and the following disclaimer in the documentation
*    and/or materials provided with the distribution.
*
* 3.  In addition, redistributions of modified forms of the source or binary
*     code must carry prominent notices stating that the original code was
*     changed, the author of the revisions, and the date of the change.
*
* 4.  All publications or advertising materials mentioning features or use of
*     this software are asked, but not required, to acknowledge that it was
*     developed at the NOAA's National Climatic Data Center in Asheville, NC and to
*     credit the contributors.
*
* 5.  THIS SOFTWARE IS PROVIDED BY THE GOVERNMENT AND THE CONTRIBUTORS  "AS IS"
*     WITH NO WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED.  In no event
*     shall the Government or the Contributors be liable for any damages
*     suffered by the users arising out of the use of this software, even if
*     advised of the possibility of such damage.
*/


package gov.noaa.ncdc.iosp.area;

import java.io.IOException;

import ucar.unidata.io.RandomAccessFile;
import edu.wisc.ssec.mcidas.AREAnav;
import edu.wisc.ssec.mcidas.AreaDirectory;
import edu.wisc.ssec.mcidas.AreaFileException;
import edu.wisc.ssec.mcidas.GVARnav;
import edu.wisc.ssec.mcidas.McIDASException;
import edu.wisc.ssec.mcidas.McIDASUtil;

/*
 * 
 * @author Arthur Fotos
 * @since 2008.04.01
 * 
 * @version 2.2
 * 
 * Upgraded to ver2.1 now using netcdf-4.1  wrh 2009.08.01   
 */


public class NCDCAreaFile {

      /** AD_STATUS - old status field, now used as position num in ADDE */
      public static final int AD_STATUS     = 0;
      /** AD_VERSION - McIDAS area file format version number */
      public static final int AD_VERSION    = 1;
      /** AD_SENSORID - McIDAS sensor identifier */
      public static final int AD_SENSORID   = 2;
      /** AD_IMGDATE - nominal year and day of the image, YYYDDD format */
      public static final int AD_IMGDATE    = 3;
      /** AD_IMGTIME - nominal time of the image, HHMMSS format */
      public static final int AD_IMGTIME    = 4;
      /** AD_STLINE - upper left image line coordinate */
      public static final int AD_STLINE     = 5;
      /** AD_STELEM - upper left image element coordinate */
      public static final int AD_STELEM     = 6;
      /** AD_NUMLINES - number of lines in the image */
      public static final int AD_NUMLINES   = 8;
      /** AD_NUMELEMS - number of data points per line */
      public static final int AD_NUMELEMS   = 9;
      /** AD_DATAWIDTH - number of bytes per data point */
      public static final int AD_DATAWIDTH  = 10;
      /** AD_LINERES - data resolution in line direction */
      public static final int AD_LINERES    = 11;
      /** AD_ELEMRES - data resolution in element direction */
      public static final int AD_ELEMRES    = 12;
      /** AD_NUMBANDS - number of spectral bands, or channels, in image */
      public static final int AD_NUMBANDS   = 13;
      /** AD_PFXSIZE - length in bytes of line prefix section */
      public static final int AD_PFXSIZE    = 14;
      /** AD_PROJNUM - SSEC project number used in creating image */
      public static final int AD_PROJNUM    = 15;
      /** AD_CRDATE - year and day image was created, CCYYDDD format */
      public static final int AD_CRDATE     = 16;
      /** AD_CRTIME - time image was created, HHMMSS format */
      public static final int AD_CRTIME     = 17;
      /** AD_BANDMAP - spectral band map, bit set for each of 32 bands present */
      public static final int AD_BANDMAP    = 18;
      /** AD_DATAOFFSET - byte offset to start of data block */
      public static final int AD_DATAOFFSET = 33;
      /** AD_NAVOFFSET - byte offset to start of navigation block */
      public static final int AD_NAVOFFSET  = 34;
      /** AD_VALCODE - validity code */
      public static final int AD_VALCODE    = 35;
      /** AD_STARTDATE - actual image start year and Julian day, yyyddd format */
      public static final int AD_STARTDATE  = 45;
      /** AD_STARTTIME - actual image start time, hhmmss; 
       *  in milliseconds for POES data */
      public static final int AD_STARTTIME  = 46;
      /** AD_STARTSCAN - starting scan number (sensor based) of image */
      public static final int AD_STARTSCAN  = 47;
      /** AD_DOCLENGTH - length in bytes of line prefix documentation */
      public static final int AD_DOCLENGTH  = 48;
      /** AD_CALLENGTH - length in bytes of line prefix calibration information */
      public static final int AD_CALLENGTH  = 49;
      /** AD_LEVLENGTH - length in bytes of line prefix level section */
      public static final int AD_LEVLENGTH  = 50;
      /** AD_SRCTYPE - McIDAS source type (ascii, satellite specific) */
      public static final int AD_SRCTYPE    = 51;
      /** AD_CALTYPE - McIDAS calibration type (ascii, satellite specific) */
      public static final int AD_CALTYPE    = 52;
      /** AD_AVGSMPFLAG - data is averaged (1), or sampled (0) */
      public static final int AD_AVGSMPFLAG = 53;
      /** AD_SRCTYPEORIG - original source type (ascii, satellite specific) */
      public static final int AD_SRCTYPEORIG    = 56;
      /** AD_CALTYPEUNIT - calibration unit */
      public static final int AD_CALTYPEUNIT    = 57;
      /** AD_CALTYPESCALE - calibration scaling factor */
      public static final int AD_CALTYPESCALE    = 58;
      /** AD_AUXOFFSET - byte offset to start of auxilliary data section */
      public static final int AD_AUXOFFSET  = 59;
      /** AD_CALOFFSET - byte offset to start of calibration section */
      public static final int AD_CALOFFSET  = 62;
      /** AD_DIRSIZE - size in 4 byte words of an image directory block */
      public static final int AD_DIRSIZE    = 64;

      /** VERSION_NUMBER - version number for a valid AREA file (since 1985) */
      public static final int VERSION_NUMBER = 4;
      
      private boolean flipwords;
      private boolean fileok;
//    private boolean hasReadData;
      //transient private RandomAccessFile af;
      private RandomAccessFile af;
      private int status=0;
      private int navLoc, calLoc, auxLoc, datLoc;
      private int navbytes, calbytes, auxbytes;
      private int linePrefixLength, lineDataLength, lineLength, numberLines;
      private long position;
      private int skipByteCount;
      private long newPosition;
      private int numBands;
      int[] dir;
      int[] nav = null;
      int[] cal = null;
      int[] aux = null;
      int[][][] data;
      private AreaDirectory areaDirectory;
      private String imageSource = null;
      private AREAnav areaNav;
//    private AreaCalibration areaCal;

    public NCDCAreaFile(String source) throws AreaFileException{
        imageSource = source;
   
        try {
            // Use Unidata's RandomAccessFile which is buffered.
            // Set buffer to 1 Megs - this should pull 250,000 4-byte pixels into memory at once,
            // which should cover one scan line and minimize IO.
            af = new RandomAccessFile(source,"r", 1024*1);
            af.order(RandomAccessFile.BIG_ENDIAN);
            
            
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        fileok=true;
        position = 0;
        readMetaData();
//      areaCal = new AreaCalibration(areaDirectory);
    }

    /**
     * Close underlying random access file
     * @throws IOException
     */
    public void close() throws IOException {
    	af.close();
    	af = null;
    }
    

    
    /**
     * returns data for all bands, lines, and elements for an area file
     * line and element index starts at 0, band number at 1
     * @return int[][][] - int[numBands][numLines][numElements]
     * @throws AreaFileException
     */
    public int[][][] getData() throws AreaFileException {
        int[][][] mydata = readData(0,0,dir[AD_NUMELEMS],numberLines);
        return mydata;
    }


    /**
     * 
     * @param lineNumber - first line of data desired. 0 indexed. 
     * @param eleNumber - first element in line desired, 0 indexed.
     * @param numLines - number of lines to read
     * @param numEles   - number of elements to read
     * @param bandNumber - band number to return data for.  1 indexed
     * @return int[][] - integer array[numLines][numEles]
     * @throws AreaFileException
     */
    public int[][] getData(int lineNumber, int eleNumber, int numLines, int numEles, int bandNumber) throws AreaFileException {
        int[][][] data = readData(eleNumber,lineNumber, eleNumber + numEles, lineNumber + numLines);
        int[][] subset = new int[numLines][numEles];

        for(int i =0;i<numLines;i++){
            for(int j=0;j<numEles;j++){
                subset[i][j] = data[0][i][j];
            }
        }
        return subset;      
        
    }


    /**
     *  returns data for a selected range of an area file. uses band #1 if more than one band
     * @param lineNumber - first line of data desired. 0 indexed. 
     * @param eleNumber - first element in line desired, 0 indexed.
     * @param numLines - number of lines to read
     * @param numEles   - number of elements to read
     * @return int[][] - integer array[numLines][numEles]
     * @throws AreaFileException
     */
    public int[][] getData(int lineNumber, int eleNumber, int numLines, int numEles) throws AreaFileException {
        return getData(lineNumber, eleNumber,numLines,numEles,1);
    }
    

    /**
     * reads area file for a given range of lines and elements. reads all bands
     * @param startElement
     * @param startLine
     * @param endElement
     * @param endLine
     * @return
     * @throws AreaFileException
     */
      private int[][][] readData(int startElement, int startLine, int endElement, int endLine) throws AreaFileException {
//          int[][][]myData = new int[numBands][dir[AD_NUMLINES]][dir[AD_NUMELEMS]];
            int numLines = endLine - startLine;
            int numEles = endElement - startElement;
//  System.out.println("linePrefixLength: " + linePrefixLength);
            int[][][] myData = new int[numBands][numLines][numEles];
            
            if (! fileok) {
              throw new AreaFileException("Error reading AreaFile data");
            }

            short shdata;
            int intdata;
            boolean isBrit = 
               areaDirectory.getCalibrationType().equalsIgnoreCase("BRIT");         
            long newPos = 0;

            for (int i = 0; i<numLines; i++) {
                newPos = datLoc + linePrefixLength + lineLength * (i + startLine) + startElement * dir[AD_DATAWIDTH];
                position = newPos;
//              System.out.println(position);
                try {
                    af.seek(newPos);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                
                for (int k=0; k<numEles; k++) {
                  for (int j = 0; j<numBands; j++) {
                    try {
                      if (dir[AD_DATAWIDTH] == 1) {
                        myData[j][i][k] = (int)af.readByte();
                          if (myData[j][i][k] < 0 && isBrit) {
                              myData[j][i][k] += 256;
                          }                   
//                      position = position + 1;
                      } else 

                      if (dir[AD_DATAWIDTH] == 2) {
                        shdata = af.readShort();
                        if (flipwords) {
                          myData[j][i][k] = (int) ( ((shdata >> 8) & 0xff) | 
                                                  ((shdata << 8) & 0xff00) );
                        } else {
                          myData[j][i][k] = (int) (shdata);
                        }
//                      position = position + 2;
                      } else 

                      if (dir[AD_DATAWIDTH] == 4) {
                        intdata = af.readInt();
                        if (flipwords) {
                          myData[j][i][k] = ( (intdata >>> 24) & 0xff) | 
                                          ( (intdata >>> 8) & 0xff00) | 
                                          ( (intdata & 0xff) << 24 )  | 
                                          ( (intdata & 0xff00) << 8);
                        } else {
                          myData[j][i][k] = intdata;
                        }
//                      position = position + 4;
                      }            
                    } 
                    catch (IOException e) {
                        myData[j][i][k] = 0;
                    }
                  }
                }
            }
            return myData;
            } // end of areaReadData method

      /** 
       *  Read the metadata for an area file (directory, nav,  and cal). 
       *
       * @exception AreaFileException if there is a problem
       * reading any portion of the metadata.
       *
       */

      private void readMetaData() throws AreaFileException {
        
        int i;
//      hasReadData = false;

//      if (! fileok) {
//        throw new AreaFileException("Error reading AreaFile directory");
//      }

        dir = new int[AD_DIRSIZE];

        for (i=0; i < AD_DIRSIZE; i++) {
          try { dir[i] = af.readInt();
          } catch (IOException e) {
            status = -1;
            throw new AreaFileException("Error reading AreaFile directory:" + e);
          }
        }
        position += AD_DIRSIZE * 4;

        // see if the directory needs to be byte-flipped

        if (dir[AD_VERSION] != VERSION_NUMBER) {
          McIDASUtil.flip(dir,0,19);
          // check again
         if (dir[AD_VERSION] != VERSION_NUMBER)
             throw new AreaFileException(
                 "Invalid version number - probably not an AREA file");
          // word 20 may contain characters -- if small integer, flip it...
          if ( (dir[20] & 0xffff) == 0) McIDASUtil.flip(dir,20,20);
          McIDASUtil.flip(dir,21,23);
          // words 24-31 contain memo field
          McIDASUtil.flip(dir,32,50);
          // words 51-2 contain cal info
          McIDASUtil.flip(dir,53,55);
          // word 56 contains original source type (ascii)
          McIDASUtil.flip(dir,57,63);
          flipwords = true;
        }

        areaDirectory = new AreaDirectory(dir);

        // pull together some values needed by other methods
        navLoc = dir[AD_NAVOFFSET];
        calLoc = dir[AD_CALOFFSET];
        auxLoc = dir[AD_AUXOFFSET];
        datLoc = dir[AD_DATAOFFSET];
        numBands = dir[AD_NUMBANDS];
        linePrefixLength = 
          dir[AD_DOCLENGTH] + dir[AD_CALLENGTH] + dir[AD_LEVLENGTH];
        if (dir[AD_VALCODE] != 0) linePrefixLength = linePrefixLength + 4;
        if (linePrefixLength != dir[AD_PFXSIZE]) 
          throw new AreaFileException("Invalid line prefix length in AREA file.");
        lineDataLength = numBands * dir[AD_NUMELEMS] * dir[AD_DATAWIDTH];
        lineLength = linePrefixLength + lineDataLength;
        numberLines = dir[AD_NUMLINES];

        if (datLoc > 0 && datLoc != McIDASUtil.MCMISSING) {
          navbytes = datLoc - navLoc;
          calbytes = datLoc - calLoc;
          auxbytes = datLoc - auxLoc;
        }
        if (auxLoc > 0 && auxLoc != McIDASUtil.MCMISSING) {
          navbytes = auxLoc - navLoc;
          calbytes = auxLoc - calLoc;
        }

        if (calLoc > 0 && calLoc != McIDASUtil.MCMISSING ) {
          navbytes = calLoc - navLoc;
        }


        // Read in nav block
        if (navLoc > 0 && navbytes > 0) {
            nav = new int[navbytes/4];
            newPosition = (long) navLoc;
            skipByteCount = (int) (newPosition - position);
            try {
                af.skipBytes(skipByteCount);
            } catch (IOException e) {
                status = -1;
                throw new AreaFileException("Error skipping AreaFile bytes: " + e);
            }
            for (i=0; i<navbytes/4; i++) {
                try {
                    nav[i] = af.readInt();
                } catch (IOException e) {
                    status = -1;
                    throw new AreaFileException("Error reading AreaFile navigation:"+e);
                }
            }
            if (flipwords){
                flipnav(nav);
            }
            position = navLoc + navbytes;
        }

        // Read in cal block
        if (calLoc > 0 && calbytes > 0) {
          cal = new int[calbytes/4];
          newPosition = (long)calLoc;
          skipByteCount = (int) (newPosition - position);
          try {
            af.skipBytes(skipByteCount);
          } catch (IOException e) {
            status = -1;
            throw new AreaFileException("Error skipping AreaFile bytes: " + e);
          }
          for (i=0; i<calbytes/4; i++) {
              try { 
                  cal[i] = af.readInt();
              } catch (IOException e) {
                  status = -1;
                  throw new AreaFileException("Error reading AreaFile calibration:"+e);
              }
          }
          // if (flipwords) flipcal(cal);
          position = calLoc + calbytes;
        }

        // Read in aux block
        if (auxLoc > 0 && auxbytes > 0){
            aux = new int[auxbytes/4];
            newPosition = (long) auxLoc;
            skipByteCount = (int) (newPosition - position);
            try{
                af.skipBytes(skipByteCount);
            }catch (IOException e){
                status = -1;
                throw new AreaFileException("Error skipping AreaFile bytes: " + e);
            }
            for (i = 0; i < auxbytes/4; i++){
                try{
                    aux[i] = af.readInt();
                }catch (IOException e){
                    status = -1;
                    throw new AreaFileException("Error reading AreaFile aux block:" + e);
                }
            }
            position = auxLoc + auxbytes;
        }

        // now return the Dir, as requested...
        status = 1;
        return;
      } 

      
      /**
       * selectively flip the bytes of words in nav block
       *
       * @param array[] of nav parameters
       *
       */
      private void flipnav(int[] nav) {

        // first word is always the satellite id in ASCII
        // check on which type:

        if (nav[0] == AREAnav.GVAR) {

          McIDASUtil.flip(nav,2,126);
          McIDASUtil.flip(nav,129,254);
          McIDASUtil.flip(nav,257,382);
          McIDASUtil.flip(nav,385,510);
          McIDASUtil.flip(nav,513,638);
        }

        else if (nav[0] == AREAnav.DMSP) {
          McIDASUtil.flip(nav,1,43);
          McIDASUtil.flip(nav,45,51);
        }

        else if (nav[0] == AREAnav.POES) {
          McIDASUtil.flip(nav,1,119);
        }

        else {
          McIDASUtil.flip(nav,1,nav.length-1);
        }

        return;
      }   
      

      /**
       * returns the string of the image source location
       *
       * @return name of image source
       *
       */
       public String getImageSource() {
         return imageSource;
       }

      /** 
       * Returns the directory block
       *
       * @return an integer array containing the area directory
       *
       *
       */
      public int[] getDir() {
        return dir;
      }


      /** 
       * Returns the AreaDirectory object for this AreaFile
       *
       * @return AreaDirectory
       *
       *
       */
      public AreaDirectory getAreaDirectory() {
        return areaDirectory;
      }

      
      /** 
       * Returns the navigation block
       *
       * @return an integer array containing the nav block data
       *
       *
       */

      public int[] getNav() {

        if (navLoc <= 0 || navLoc == McIDASUtil.MCMISSING) {
          nav = null;
        } 

        return nav;

      }

      /**
       * Get the navigation, and pre-set it
       * for the ImageStart and Res (from Directory block), and
       * the file start (0,0), and Mag (1,1).
       *
       * @return  AREAnav for this image  (may be null)

       */
      public AREAnav getNavigation()
          throws AreaFileException
      {
        if (areaNav == null) {
          // make the nav module
          try {
            areaNav = AREAnav.makeAreaNav(getNav(), getAux());
            areaNav.setImageStart(dir[AD_STLINE], dir[AD_STELEM]);
            areaNav.setRes(dir[AD_LINERES], dir[AD_ELEMRES]);
            areaNav.setStart(0,0);
            areaNav.setMag(1,1);

          } catch (McIDASException excp) {
            areaNav = null;
          }
        }
        return areaNav;
      }

      /**
         * Returns calibration block
         * 
         * @return an integer array containing the nav block data
         */

    public int[] getCal() {

        if (calLoc <= 0 || calLoc == McIDASUtil.MCMISSING) {
            cal = null;
        }
        return cal;
    }


      /**
         * Returns AUX block
         * 
         * @return an integer array containing the aux block data
         */

      public int[] getAux() {

        if (auxLoc <= 0 || auxLoc == McIDASUtil.MCMISSING) {
          aux = null;
        } 

        return aux;

      }   
      
      public void readScanLinePrefix(int scanline){
            long newPos = 0;
            newPos = datLoc + (lineLength * scanline);
            try {
                af.seek(newPos);
                for(int x=0;x<linePrefixLength;x++){
//                  System.out.println((x-3) +  "=  : " + af.readByte());
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }         
      }
      

      public int getDetectorForScanLine(int scanline){
          int detector = 0;
          long newPos = 0;
          newPos = datLoc + (lineLength * scanline);
          try {
                af.seek(newPos);
                AreaLinePrefix prefix = new AreaLinePrefix();
                prefix.readPrefix(af);
                detector = prefix.getLdoc().getLidet();
            } catch (IOException e1) {
                e1.printStackTrace();
            }         
//          System.out.println("detector number-->" + detector);
            return detector;
      }
      
      public float[][] getCalibratedData(int lineNumber, int eleNumber, int numLines, int numEles) throws AreaFileException{
          float[][] calData = new float[numLines][numEles];
          int[][] rawData = getData(lineNumber,eleNumber,numLines,numEles);
          int[] mydata = new int[rawData[0].length];
          AreaCalibration ac = new AreaCalibration(areaDirectory);
          
          for(int i=0;i<numLines;i++){
              int detectorNum  = getDetectorForScanLine(lineNumber + i);
              for(int j=0;j<mydata.length;j++){
                  mydata[j] = rawData[i][j];
              }
              float[] whatever = ac.calibrate(mydata,  detectorNum);
              if(whatever != null){
                  for(int j=0;j<numEles;j++){
                      calData[i][j] = whatever[j];
                  }
              }
          }
          return calData;
      }
      
      public float[][] getRadianceData(int lineNumber, int eleNumber, int numLines, int numEles) throws AreaFileException{
          float[][] calData = new float[numLines][numEles];
          int[][] rawData = getData(lineNumber,eleNumber,numLines,numEles);
          int[] mydata = new int[rawData[0].length];
          AreaCalibration ac = new AreaCalibration(areaDirectory);
          for(int i=0;i<numLines;i++){
              for(int j=0;j<mydata.length;j++){
                  mydata[j] = rawData[i][j];
              }
              float[] whatever = ac.calculateRadiance(mydata);;
              if(whatever != null){
                  for(int j=0;j<numEles;j++){
                      calData[i][j] = whatever[j];
                  }
              }
          }
          return calData;
      }   
      
      public void readScanlinePrefix(int startLine, int endLine){
            AreaLinePrefix prefix = new AreaLinePrefix();
            long newPos = 0;
            for(int i=0;i<endLine;i++){
                newPos = datLoc + ( lineLength) * startLine;
                try {
                    af.seek(newPos);
                    prefix.readPrefix(af);
//                  prefix.getLidet();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
      }
      
      public AreaLinePrefix readScanlinePrefix(int scan){
            AreaLinePrefix prefix = new AreaLinePrefix();
            long newPos = 0;
            newPos = datLoc + ( lineLength) * scan;
            try {
                af.seek(newPos);
                prefix.readPrefix(af);
//                  prefix.getLidet();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            return prefix;
      }
      
      
      
      public short[] readGoesLinePrefix(int size, int scan) {
        short[] data = new short[size];
        long newPos = 0;
        newPos = datLoc + (lineLength) * scan;
        try {
            af.seek(newPos);
            for (int i = 0; i < size; i++) {
                data[i] = (short) af.readUnsignedByte();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return data;
    }
      
      public GoesPrefix readGoesLinePrefix(int scan) {
        GoesPrefix gp = new GoesPrefix();
        long newPos = 0;
        newPos = datLoc + (lineLength) * scan;
        try {
            af.seek(newPos);
            gp.readPrefix(af);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return gp;
    }
      
      
      
      private int flipShort(short s){
          return s >> 8 & 255 | s << 8 & 65280;
      }

      private int flipInt(int i){
          return i >>> 24 & 255 | i >>> 8 & 65280 | (i & 255) << 24 | (i & 65280) << 8;
      }
        
      
      public static void main (String args[]){
      String filename = "/home/afotos/testfiles/goes-area/goes12.2005.240.221013.BAND_04.L9274041";
      
//        String filename = "/home/afotos/test_files/goes12.2005.241.214013.BAND_01.L9275441";        
          
          int[][] blah = new int[3][25];
          //System.out.println("array dim 0= " + blah.length);
          //System.out.println("array dim 1 = " + blah[2].length);
          NCDCAreaFile myaf; 
          try { 
                myaf = new NCDCAreaFile(filename);
                //read headers
                int[] mydir = myaf.getDir();
                AreaDirectory  myad;
                myad = new AreaDirectory(mydir);
                int numPixels = myad.getElements();
                int numScans = myad.getLines();
                int numBands= myad.getNumberOfBands();
                
                int[] mynav = myaf.getNav();
                GVARnav myng = new GVARnav(mynav);  // XXXXnav is the specific implementation
                myaf.readScanlinePrefix(0,10);
//              myaf.readScanLinePrefix(0);
          }catch(Exception e){
              e.printStackTrace();
          }
      }
}
