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
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.MaxGeographicExtent;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import ucar.unidata.io.InMemoryRandomAccessFile;

import com.vividsolutions.jts.geom.Coordinate;


/**
 *  Decodes header information for Level-III NEXRAD Data.
 *
 * @author     steve.ansari
 * @created    July 23, 2004
 */
public class DecodeL3Header implements NexradHeader {


    private RadarHashtables nxhash;
    private HashMap<String, String> gsmHashMap;

    private boolean verbose;

    // User Accessable Fields
    private double lat;
    private double lon;
    private double alt;
    private short pcode;
    private int[] productSpecific = new int[10];
    private short[] dataThreshold = new short[16];
    private short opmode;
    private short elevnumber;
    private short vcp;
    private short seqnumber;
    private short scannumber;
    private short scandate;
    private int scantime;
    private short gendate;
    private int gentime;
    private int version;
    private int symbologyBlockOffset;
    private int graphicBlockOffset;
    private int tabularBlockOffset;
    private int yyyymmdd;
    private int year, month, day, hour, minute, seconds;

    /**
     *  ICAO (4-letter ID) value
     */
    private String icao;
    /**
     *  Product String (3-letter ex: N0R)
     */
    private String productString;
    /**
     *  NCDC Archive Name 
     */
    private String ncdcFilename;





    /**
     *  Description of the Field
     */
    private int[] dataThresholdInfo = new int[16];
    /**
     *  Description of the Field
     */
    private int[] dataThresholdValue = new int[16];
    /**
     *  Description of the Field
     */
    private String[] dataThresholdString = new String[16];

    private byte[] dataThresholdBytes = new byte[32];


    //private HTTPRandomAccessFile2 f;
    private ucar.unidata.io.RandomAccessFile f;

    private DecimalFormat fmt1 = new DecimalFormat("0.0");
    private DecimalFormat fmt2 = new DecimalFormat("0.00");
    private DecimalFormat fmt02 = new DecimalFormat("00");

    private Calendar genCalendar = Calendar.getInstance();

    private URL url = null;

    /**
     * Constructor
     */
    public DecodeL3Header() { 
        // Initiate the Nexrad Hashtable -- load from /shapefiles/wsr.dbf
        nxhash = RadarHashtables.getSharedInstance();
    }


    /**
     * Returns NEXRAD Site Latitude
     *
     * @return    The lat value
     */
    public double getLat() {
        return lat;
    }


    /**
     * Returns NEXRAD Site Longitude
     *
     * @return    The lon value
     */
    public double getLon() {
        return lon;
    }


    /**
     * Returns NEXRAD Site Altitude in Feet
     *
     * @return    The alt value
     */
    public double getAlt() {
        return alt;
    }


    /**
     * Returns Date of product creation in milliseconds from 1970-1-1 GMT
     *
     * @return    The date value
     */
    public long getMilliseconds() {
        return genCalendar.getTimeInMillis();
    }



    /**
     * Returns Date of product creation
     *
     * @return    The date value
     */
    public int getDate() {
        return yyyymmdd;
    }


    /**
     * Returns Hour of product creation
     *
     * @return    The hour value
     */
    public short getHour() {
        return (short)hour;
    }

    /**
     * Returns Hour of product creation
     *
     * @return    The hour value in 2 digit string
     */
    public String getHourString() {
        return fmt02.format(hour);
    }


    /**
     * Returns Minute of product creation
     *
     * @return    The minute value
     */
    public short getMinute() {
        return (short)minute;
    }

    /**
     * Returns Minute of product creation
     *
     * @return    The minute value in 2 digit string
     */
    public String getMinuteString() {
        return fmt02.format(minute);
    }


    /**
     * Returns Second of product creation
     *
     * @return    The second value
     */
    public short getSecond() {
        return (short)(seconds);
    }

    /**
     * Returns Second of product creation
     *
     * @return    The second value in 2 digit string
     */
    public String getSecondString() {
        return fmt02.format(seconds);
    }




    /**
     * Returns product code
     *
     * @return    The productCode value
     */
    public short getProductCode() {
        return pcode;
    }

    /**
     * Returns operational mode of radar <br>
     *
     * @return    The character representing operational mode of radar. <br>
     * 0 = Maintenance , 1 = Clean Air , 2 = Precipitation/Severe Weather
     */
    public char getOpMode() {
        if (opmode == 0) {
            return 'M';
        }
        else if (opmode == 1) {
            return 'B';
        }
        else if (opmode == 2) {
            return 'A';
        }
        else {
            return 'A';
        }
    }

    /**
     * Returns volume coverage pattern (vcp)
     *
     * @return    The vcp value <br>
     * 11, 12, 121, 21, etc.    
     */
    public short getVCP() {
        return vcp;
    }


    /**
     * Returns Version of the Level-III Product 
     *
     * @return    The version number <br>
     * 0, 1, etc...    
     */
    public float getVersion() {
        return (float)version;
    }


    /**
     * Returns ICAO (4-letter ID)
     *
     * @return    The icao value <br>
     * KGSP, KCAE, etc.    
     */
    public String getICAO() {
        return icao;
    }

    /**
     * Returns 3-letter product code
     *
     * @return    The productString value <br>
     * N0R, N1P, DPA, etc.    
     */
    public String getProductString() {
        return productString;
    }

    /**
     * Returns the NCDC filename standard
     *
     * @return    The NCDC Filename standard <br>
     * ex:) 7000KTBW_SDUS52_N0RTBW_200408130534
     */
    public String getNCDCFilename() {
        return ncdcFilename;
    }

    /**
     * Returns Product Specific Values
     *
     * @param  index  Description of the Parameter
     * @return        The productSpecificValue value
     */
    public int getProductSpecificValue(int index) {
        try {
            return productSpecific[index];
        } catch (Exception e) {
            return -999;
        }
    }

    public int[] getProductSpecificValueArray() {
        return productSpecific;
    }

    /**
     * Override DPA Maximum value in product specific block
     * of header.  The DPA Max in the header is a known problem
     * as identified in: dpadecodeOB6.doc from 
     * http://www.weather.gov/os/whfs/documentation/dpadecodeOB6.doc
     * @param  value  Value in Inches * 100
     */
    public void setDPAMaxValue(short value) {
        productSpecific[3] = value;
    }


    /**
     * Returns Data Threshold Values
     *
     * @param  index  Description of the Parameter
     * @return        The dataThresholdValue value
     */
    public String getDataThresholdString(int index) {
        try {
            // override for 8-bit, non-categorized products
            if (getProductType() == L3RADIAL_8BIT) {
                return null;
            }
            else {
                return dataThresholdString[index];
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "N/A";
        }
    }

    /**
     * Get raw 32 bits used to describe data thresholds.  How this
     * is used can differ depending on the product type (8 bit 16 level, etc...)
     * @return
     */
    public byte[] getDataThresholdBytes() {
        return dataThresholdBytes;
    }

    public String[] getDataThresholdStringArray() {
        // override for 8-bit, non-categorized products
        if (getProductType() == L3RADIAL_8BIT) {
            return null;
        }
        else {
            return dataThresholdString;
        }
    }

    /**
     * Sets Data Threshold Values --- needed because threshold values
     * are unknown until a moment has been selected and decoded.
     *
     * @param  threshold  Array of Threshold strings 
     * @param  index  index of threshold string
     */
    protected void setDataThresholdStringArray(String[] dataThresholdString) {
        this.dataThresholdString = dataThresholdString;
    }




    /**
     * Returns the url of the nexrad file.  Null if none have been decoded yet.
     *
     *
     * @return    The URL of the nexrad file
     */
    public URL getDataURL() {
        return url;
    }


    /**
     * Returns the RandomAccessFile with the file pointer before the dataHeader.
     *
     *
     * @return    The RandomAccessFile value
     */
    //public HTTPRandomAccessFile2 getHTTPRandomAccessFile2() {
    public ucar.unidata.io.RandomAccessFile getRandomAccessFile() {
        return f;
    }



    /**
     * Method that does all the work.
     * Decodes the header portion of a Nexrad Level-III File
     *
     * @param  url  Location of NEXRAD data
     */
    public void decodeHeader(URL url) throws DecodeException {

        this.url = url;

        // Initiate binary buffered read
        ucar.unidata.io.RandomAccessFile raf = null;
        try {
            if (url.getProtocol().equals("file")) {
                raf = new ucar.unidata.io.RandomAccessFile(url.getFile().replaceAll("%20", " "),"r");
                //f.order(ucar.unidata.io.RandomAccessFile.BIG_ENDIAN);
            }
            else {
                raf = new ucar.unidata.io.http.HTTPRandomAccessFile(url.toString());
                //f = new ucar.unidata.io.http.HTTPRandomAccessFile3(url.toString());
            }
            raf.order(ucar.unidata.io.RandomAccessFile.BIG_ENDIAN);


            //System.out.println("IIIIIIIIIIIIIIII FILE SIZE: "+f.length());


        } catch (Exception e) {
        	e.printStackTrace();
            throw new DecodeException("CONNECTION ERROR: "+url, url);
        }
        decodeHeader(raf);      
    }



    /**
     * Method that does all the work.
     * Decodes the header portion of a Nexrad Level-III File
     *
     * @param  f ucar.unidata.io.RandomAccessFile object
     */
    public void decodeHeader(ucar.unidata.io.RandomAccessFile raf) throws DecodeException {





        String header = null; 
        int wmoStart = 0;

        try {



            // strip off the uncompressed wmo header for later reattachment
            byte[] zdata = new byte[50];
            raf.read(zdata);
            header = new String(zdata);
            wmoStart = header.indexOf("SDUS");

            if (wmoStart == -1) {
                wmoStart = header.indexOf("NXUS");
            }

            //System.out.println("----------------- WMO HEADER: "+wmoStart+" ------------------");         
            //System.out.println(header);
            //System.out.println();

            byte[] wmoHeader = new byte[30];
            if (wmoStart >= 0) {

                if (verbose) {
                    System.out.println("System.arraycopy(zdata, "+wmoStart+", wmoHeader, 0, 30);");
                }

                System.arraycopy(zdata, wmoStart, wmoHeader, 0, 30);
            }


            //System.out.println(header);      
            //System.out.println("WMO START: "+wmoStart);      
            //System.out.println(new String(wmoHeader));      

            if (verbose) {
                System.out.println("ORIGINAL FILE LENGTH: "+raf.length());
            }


            //      // get uncompressed NIDS data using stolen decoder in NetCDF library      
            ucar.nc2.iosp.nids.JNXNidsheader nids = new ucar.nc2.iosp.nids.JNXNidsheader();
            byte[] data = nids.readZLibNIDS(raf);

            byte[] alldata = new byte[wmoHeader.length + data.length];
            System.arraycopy(wmoHeader, 0, alldata, 0, wmoHeader.length);
            System.arraycopy(data, 0, alldata, wmoHeader.length, data.length);

            // close file - we will now use in-memory data
            raf.close();
            
            f = new ucar.unidata.io.InMemoryRandomAccessFile("NIDS DATA", alldata);
            f.order(ucar.unidata.io.RandomAccessFile.BIG_ENDIAN);
            f.seek(0);  // rewind            

            if (verbose) {
                System.out.println("DECOMPRESSED FILE LENGTH: "+f.length());
            }


            //checkHeaderProfile(alldata);






            /*
            long fpos = f.getFilePointer();
            for (int i=0; i<60; i++) {
               System.out.println("["+i+"] = "+f.getFilePointer()+" = "+new String(new byte[] {f.readByte()}));
            }
            f.seek(fpos);
            for (int i=0; i<60; i++) {
               System.out.println("["+i+"] = "+f.getFilePointer()+" = "+swabInt(f.readShort()));
            }
            f.seek(fpos);
             */

            // Check for WMO Header ( check if first 4 characters == SDUS ) 
            byte[] awips = new byte[30];
            f.read(awips);
            String wmoString = new String(awips);

            /*
System.out.println("::::::::::::::::::::::::::::: "+wmoString+":::::::::::::::::::::::::");         
System.out.println();         
System.out.println();         
System.out.println("012345678901234567890123456789012345678901234567890");         
System.out.println(wmoString);         
System.out.println();         
System.out.println();
             */
//            012345678901234567 8 901234567
//            SDUS24 KLIX 292353\r\nN1RLIX
            
            if (wmoString.substring(0, 4).equals("SDUS")) {
                this.icao = wmoString.charAt(7) + wmoString.substring(24, 27);
                this.productString = wmoString.substring(21, 24);
                this.ncdcFilename = "7000" + wmoString.substring(7, 11) + "_" +
                wmoString.substring(0, 6) + "_" + productString + wmoString.substring(24, 27) + "_";
                // Skip WMO Header
                f.seek(0);
                while (f.readShort() != -1) {
                    ;
                }
                //System.out.println("--FIRST BREAKPOINT-- FILE POINTER LOCATION = "+f.getFilePointer());
                /*
            long fpos = f.getFilePointer();
            for (int i=0; i<160; i++) {
               System.out.println("["+i+"] = "+f.getFilePointer()+" = "+f.readShort());
            }
            f.seek(fpos);
                 */
            }
            // Special for the GSM (General Status Message)
            else if (wmoString.substring(0, 4).equals("NXUS")) {
                gsmHashMap = processGSM(wmoString);  
                return;
            }
            else {

                f.seek(0);
                while (f.readShort() != -1) {
                    ;
                }
                System.out.println("--FIRST BREAKPOINT-- FILE POINTER LOCATION = "+f.getFilePointer());

                //            throw new NexradDecodeException("WMO Header length = "+firstHeaderSection, url);
                //throw new NexradDecodeException("WMO Header length ERROR:\n"+wmoString, url);
            }            


            //System.out.println("HEADER START = " + f.getFilePointer());

            // Decode Lat and Lon
            lat = f.readInt() / 1000.0;
            lon = f.readInt() / 1000.0;

            String approxICAO = nxhash.getClosestICAO(lat, lon, 0.05);
            
            
//            System.out.println("NEXRAD IDs::::::::::::::: "+icao+", "+approxICAO);

            
            if (approxICAO != null) {
            	
            	if (icao == null) {
            		icao = approxICAO;
            	}
            	else if (icao.substring(1).equals(approxICAO.substring(1))) {
            		icao = approxICAO;             
            	}

            	if (! RadarHashtables.getSharedInstance().getIdList().contains(icao)) {
            		icao = approxICAO;
            	}

            }

            
            // Temporarily disable
            //         if (! icao.equals(approxICAO)) {
            //            throw new NexradDecodeException("Header Decode Error: ICAO in WMO Header does not match lat/lon.\n"+
            //                    "Decoded ICAO = "+icao+" , Closest from Lat/Lon = "+approxICAO+".\n"+
            //                    "Decoded Lat,Lon = "+lat+" , "+lon, url);
            //         }


            // Decode Radar Site Altitude
            alt = f.readShort();
            // Get product code )
            pcode = f.readShort();

            //----
            // Get operational mode
            opmode = f.readShort();
            // Get volume coverage pattern
            vcp = f.readShort();
            // Get sequence number
            seqnumber = f.readShort();
            // Get volume scan number
            scannumber = f.readShort();
            // Get volume scan date
            scandate = f.readShort();
            // Get volume scan time
            scantime = f.readInt();
            // Get product generation date
            gendate = f.readShort();
            // Get product generation time
            gentime = f.readInt();
            // Get first 2 product specific codes (halfwords 27 and 28)
            for (int i = 0; i < 2; i++) {
                productSpecific[i] = f.readShort();
            }
            // Get elevation number
            elevnumber = f.readShort();
            // Get the 3rd product specific code (halfword 30)
            productSpecific[2] = f.readShort();
            // Get the data threshold values
            // read halfword 31-47 again as byte array, which can be used differently
            // depending on the product type (8-bit, 16-level, etc...)
            f.read(dataThresholdBytes);
            // The following will process the data threshold values, which are 
            // applicable for all products but the 8-bit ones
            InMemoryRandomAccessFile bf = new InMemoryRandomAccessFile("Data Threshold", dataThresholdBytes);
            for (int i = 0; i < 16; i++) {
                dataThresholdInfo[i] = bf.readUnsignedByte();
                dataThresholdValue[i] = bf.readUnsignedByte();
            }
            bf.close();

            // Get the remaining 7 product specific codes (halfword
            for (int i = 0; i < 7; i++) {
            	if (pcode == 176) {
            		productSpecific[i + 3] = f.readUnsignedShort();
            	}
            	else {
            		productSpecific[i + 3] = f.readShort();
            	}
            }
            version = f.readUnsignedByte();
            f.readUnsignedByte();


            // Get the offset to the Symbology Block
            symbologyBlockOffset = f.readInt();
            // Get the offset to the Graphic Block
            graphicBlockOffset = f.readInt();
            // Get the offset to the Tabular Block
            tabularBlockOffset = f.readInt();


            if (verbose) {
                System.out.println("VERSION: "+version);

                System.out.println("END OF PROD. DESC. BLOCK: FILE POS: "+f.getFilePointer());
                System.out.println("symbologyBlockOffset: "+symbologyBlockOffset);
                System.out.println("graphicBlockOffset: "+graphicBlockOffset);
                System.out.println("tabularBlockOffset: "+tabularBlockOffset);
                System.out.println("HEADER::::: "+f.readShort());         
            }         

            /*
             *  System.out.println("gendate "+gendate);
             *  System.out.println("gentime "+gentime);
             *  System.out.println("elevnumber "+elevnumber);
             *  for (int i=0; i<10; i++)
             *  System.out.println("productSpecific "+i+" = "+productSpecific[i]);
             *  for (int i=0; i<16; i++)
             *  System.out.println("dataThreshold "+i+" = "+dataThreshold[i]);
             */
            processThresholds();

//            for (int i=0; i<16; i++)
//              System.out.println("dataThresholdString "+i+" = "+dataThresholdString[i]);

            

            // Add date and time info to filename         
            this.ncdcFilename = ncdcFilename + getDate() + getHourString() + getMinuteString();

            yyyymmdd = NexradEquations.convertJulianDate(scandate);
            String str = new Integer(yyyymmdd).toString();
            year = Integer.parseInt(str.substring(0, 4));
            month = Integer.parseInt(str.substring(4, 6));
            day = Integer.parseInt(str.substring(6, 8));
            hour = scantime/3600;
            minute = (scantime/60)%60;
            seconds = scantime - getHour()*3600 - getMinute()*60;
            genCalendar.set(year, month-1, day, hour, minute, seconds);
            genCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));

            return;
        } catch (Exception e) {

            long fploc = 0;
            long fsize = 0;
            try {
                fploc = f.getFilePointer();
                fsize = f.length();
            } catch (Exception ioe) {}

            System.err.println("ERROR DUMP: wmoStart="+wmoStart+"\n"+header);
            System.err.println("ERROR DUMP: f-loc="+fploc+" file-size="+fsize);
            if (fploc == fsize) {
                throw new DecodeException("Header Decode Error = No Section Separators Found: ", url);
            }

            e.printStackTrace();
            System.err.println("CAUGHT EXCEPTION:  " + e);


            try {
                f.close();
            } catch (Exception ee) {
                ee.printStackTrace();
            }
            // Close connection;

            throw new DecodeException("Header Decode Error = "+e.getMessage(), url);

            //return;

        } 


    }
    // END METHOD decodeHeader



    public void close() {
        // close file
        try {
            f.close();
        } catch (Exception ee) {
            ee.printStackTrace();
        }

    }

    /**
     *  Description of the Method
     */
    private void processThresholds() {

        if (pcode == NexradHeader.L3PC_CLUTTER_FILTER_CONTROL) {

            // version 0 (Legacy)
            if (version == 0) {
                //dataThresholdString[0] = "ND";
                dataThresholdString[0] = "FILTER OFF";
                dataThresholdString[1] = "NO CLUTTER";
                dataThresholdString[2] = "L";
                dataThresholdString[3] = "M";
                dataThresholdString[4] = "H (Bypass Map)";
                dataThresholdString[5] = "L";
                dataThresholdString[6] = "M";
                dataThresholdString[7] = "H (Force Filter)";
            }
            // version 1 (ORPG)
            else {
                //dataThresholdString[0] = "ND";
                dataThresholdString[0] = "FILTER OFF";
                dataThresholdString[1] = "NO CLUTTER";
                dataThresholdString[2] = "";
                dataThresholdString[3] = "";
                dataThresholdString[4] = "CLUTTER";
                dataThresholdString[5] = "";
                dataThresholdString[6] = "";
                dataThresholdString[7] = "FORCE FILTER";
            }


            return;
        }


        for (int i = 0; i < 16; i++) {
            String binaryString = getPaddedBitString(dataThresholdInfo[i], 8);

            //System.out.println("binaryString = "+binaryString);

            if (binaryString.charAt(0) == '1') {
                if (dataThresholdValue[i] == 0) {
                    dataThresholdString[i] = "";
                }
                else if (dataThresholdValue[i] == 1) {
                    dataThresholdString[i] = "TH";
                }
                else if (dataThresholdValue[i] == 2) {
                    dataThresholdString[i] = "ND";
                }
                else if (dataThresholdValue[i] == 3) {
                    dataThresholdString[i] = "RF";
                }
            }
            else {
                dataThresholdString[i] = "" + dataThresholdValue[i];

                if (binaryString.charAt(2) == '1') {
                    dataThresholdString[i] = fmt2.format((double) dataThresholdValue[i] / 20.0);
                }
                else if (binaryString.charAt(3) == '1') {
                    dataThresholdString[i] = fmt1.format((double) dataThresholdValue[i] / 10.0);
                }
                else if (binaryString.charAt(4) == '1') {
                    dataThresholdString[i] = "> " + dataThresholdValue[i];
                }
                else if (binaryString.charAt(5) == '1') {
                    dataThresholdString[i] = "< " + dataThresholdValue[i];
                }
                else if (binaryString.charAt(6) == '1') {
                    dataThresholdString[i] = "+ " + dataThresholdValue[i];
                }
                else if (binaryString.charAt(7) == '1') {
                    dataThresholdString[i] = "- " + dataThresholdValue[i];
                }

            }
        }
    }



    /**
     *  Gets the productType attribute of the DecodeL3Header object
     *
     * @return    The productType value
     */
    public int getProductType() {
        // Check for Radial Products
        if (pcode == -1) {
            return LEVEL2;
        }      
        else if (pcode == 19 ||
                pcode == 20 || 
                pcode == 28 ||// 
                pcode == 30 ||
                //            pcode == 138 || // DSP 
                pcode == 25 || //
                pcode == 27 || 
                pcode == 34 || 
                pcode == 56 ||
                pcode == 31 || // USP
                pcode == 78 || // N1P
                pcode == 79 || // N3P
                pcode == 80 ||
                pcode == 181 ||   // TR[0,1,2,3]
                pcode == NexradHeader.L3PC_ONE_HOUR_ACCUMULATION ||
                pcode == NexradHeader.L3PC_STORM_TOTAL_ACCUMULATION
//                pcode == 165      // DHC
            )
        {
            return L3RADIAL;
        }
        else if (pcode == 41 || 
                pcode == 57 || 
                pcode == 65 ||
                pcode == 66 || 
                pcode == 90 || 
                pcode == 36 ||
                pcode == 37 ||
                pcode == 38) {
            return L3RASTER;
        }
        else if (pcode == 58 || 
                pcode == 59 || 
                pcode == 60 || 
                pcode == 61 || 
                pcode == 62 || 
                pcode == 141) 
        {
            return L3ALPHA;
        }
        else if (pcode == 81) {
            return L3DPA;
        }
        else if (pcode == 2) {
            return L3GSM;
        }
        else if (pcode == 48) {
            return L3VAD;
        }
        else if (pcode == 152) {
            return L3RSL;
        }
        else if (pcode == 32 || // DHR
                pcode == 94  || // N*Q
                pcode == 99  || // N*U
                pcode == 138 || // DSP
                pcode == 134 || // Digital VIL
                pcode == 135 || // Enhanced Echo Tops
                pcode == 186 || // 0.6 deg. Long Range
                pcode == 180 || // base elevation (r0, r1, r2, r3) 
                pcode == 182 || // base velocity (v0 - v3)
                pcode == 159 || // digital differential reflectivity
                pcode == 161 || // digital correlation coefficient
                pcode == 163 || // digital differential phase
                pcode == NexradHeader.L3PC_DIGITAL_ONE_HOUR_ACCUMULATION ||
                pcode == NexradHeader.L3PC_DIGITAL_USER_SELECTABLE_ACCUMULATION ||
                pcode == NexradHeader.L3PC_DIGITAL_HYDROMETEOR_CLASSIFICATION || 
                pcode == NexradHeader.L3PC_DIGITAL_STORM_TOTAL_ACCUMULATION ||
                pcode == NexradHeader.L3PC_DIGITAL_ONE_HOUR_ACCUMULATION_DIFFERENCE ||
                pcode == NexradHeader.L3PC_DIGITAL_STORM_TOTAL_ACCUMULATION_DIFFERENCE ||
                pcode == NexradHeader.L3PC_DIGITAL_HYBRID_HYDROMETEOR_CLASSIFICATION || 
                pcode == NexradHeader.L3PC_DIGITAL_INSTANTANEOUS_PRECIP_RATE
                )
        {
            return L3RADIAL_8BIT;
        }
//        else if (
//                pcode == NexradHeader.L3PC_DIGITAL_INSTANTANEOUS_PRECIP_RATE
//        	)
//        {
//        	return L3GENERIC_RADIAL;
//        }
        else {
            return UNKNOWN;
        }
    }


    /**
     *  Gets the numberOfLevels attribute of the DecodeL3Header object.  
     *  Will return UNKNOWN if using DPA since actual DPA values (inches or mm)
     *  are returned.
     *
     * @return    The numberOfLevels value
     */
    public int getNumberOfLevels(boolean classify) {
        return NexradColorFactory.getColors(pcode, classify).length;
    }

    public RadarHashtables getNexradHashtables() {
        return nxhash;
    }



    /**
     * Returns Rectangle.Double Bounds for the NEXRAD Site calculated during decode. (unique to product)
     * Could be 248, 124 or 32 nmi.
     *
     * @return    The bounds value
     */
    public java.awt.geom.Rectangle2D.Double getNexradBounds() {
        return (MaxGeographicExtent.getNexradExtent(this));
    }


    private String getPaddedBitString(int value, int numBits) {   
        String binaryString = Integer.toBinaryString(value);
        // Add enough zeros so binaryString represents all 8 bits
        int binaryLength = binaryString.length();
        for (int n = 0; n < numBits - binaryLength; n++) {
            binaryString = "0" + binaryString;
        }
        return binaryString;
    }


    /**
     *  Gets lon, lat location of Radar site as Coordinate 
     *
     * @return                 The radar location as a Coordinate object
     */
    public Coordinate getRadarCoordinate() {
        return new Coordinate(lon, lat);
    }





    private HashMap<String, String> processGSM(String wmoString) throws IOException {

        // set product code to GSM
        pcode = L3PC_GSM;

        // init a hashmap with space for 41 key-value pairs
        HashMap<String, String> hashMap = new HashMap<String, String>(41);

        this.icao = wmoString.charAt(7) + wmoString.substring(24, 27);
        this.productString = wmoString.substring(21, 24);
        this.ncdcFilename = "7000" + wmoString.substring(7, 11) + "_" +
        wmoString.substring(0, 6) + "_" + productString + wmoString.substring(24, 27) + "_";
        // Skip WMO Header
        f.seek(0);
        while (f.readShort() != -1) {
            ;
        }

        // Start reading GSM message
        // Read into halfword (2-byte) array - documentation starts halfword count at 11
        int[] halfwords = new int[53-11];

        for (int n=0; n<halfwords.length; n++) {
            halfwords[n] = f.readShort();
            //   System.out.println("halfwords["+(n+11)+"]  "+halfwords[n]);
        }


        // BELOW: Use local blocks to keep variable scope limited - this helps catch errors

        {  // Block Length         
            int blockLen = halfwords[0];
            hashMap.put("blockLen", new Integer(blockLen).toString());
        }





        {  // Operation Mode      
            int opMode = halfwords[1];
            if (opMode == 0) {
                hashMap.put("opMode", "Maintenance Mode");
            }
            else if (opMode == 1) {
                hashMap.put("opMode", "Clear Air Mode");
            }
            else if (opMode == 2) {
                hashMap.put("opMode", "Precipitation / Severe Weather Mode");
            }
            else {
                hashMap.put("opMode", "Unknown Mode ("+opMode+")");
            }
        }



        {  // RDA Operability Status
            int rdaOpStatus = halfwords[2];
            String binaryString = getPaddedBitString(rdaOpStatus, 16);
            //System.out.println(binaryString);
            if (binaryString.charAt(15) == '1') {
                hashMap.put("rdaOpStatus", "Automatic Calibration Disabled");
            }
            else if (binaryString.charAt(14) == '1') {
                hashMap.put("rdaOpStatus", "Online");
            }
            else if (binaryString.charAt(13) == '1') {
                hashMap.put("rdaOpStatus", "Maintenance Action Required");
            }
            else if (binaryString.charAt(12) == '1') {
                hashMap.put("rdaOpStatus", "Maintenance Action Mandatory");
            }
            else if (binaryString.charAt(11) == '1') {
                hashMap.put("rdaOpStatus", "Commanded Shutdown");
            }
            else if (binaryString.charAt(10) == '1') {
                hashMap.put("rdaOpStatus", "Inoperable");
            }
            else if (binaryString.charAt(8) == '1') {
                hashMap.put("rdaOpStatus", "Wideband Disconnect");
            }
            // if all bits are '0' (bit 9 and 7-0 are spare), then RPG determines status
            else {
                hashMap.put("rdaOpStatus", "Determined by RPG");
            }
        }



        {  // VCP         
            int vcp = halfwords[3];
            hashMap.put("vcp", new Integer(vcp).toString());
        }



        {  // Number of elevation cuts      
            int numElevCuts = halfwords[4];
            hashMap.put("numElevCuts", new Integer(numElevCuts).toString());
        }



        {  // Elevation Angles
            double[] elevCuts = new double[20];
            for (int n=0; n<20; n++) {
                elevCuts[n] = halfwords[n+5]/10.0;  
                hashMap.put("elevCut["+n+"]", new Double(elevCuts[n]).toString());
            }
        }


        { // RDA Status       
            int rdaStatus = halfwords[25];
            String binaryString = getPaddedBitString(rdaStatus, 16);
            //System.out.println(binaryString);
            if (binaryString.charAt(14) == '1') {
                hashMap.put("rdaStatus", "Startup");
            }
            else if (binaryString.charAt(13) == '1') {
                hashMap.put("rdaStatus", "Standby");
            }
            else if (binaryString.charAt(12) == '1') {
                hashMap.put("rdaStatus", "Restart");
            }
            else if (binaryString.charAt(11) == '1') {
                hashMap.put("rdaStatus", "Operate");
            }
            else if (binaryString.charAt(10) == '1') {
                hashMap.put("rdaStatus", "Playback");
            }
            else if (binaryString.charAt(9) == '1') {
                hashMap.put("rdaStatus", "Off-line Operate");
            }
            // if all bits are '0' (bit 15 and 8-0 are spare), then RPG cannot determine status
            else {
                hashMap.put("rdaStatus", "RPG Cannot Determine the Status");
            }
        }


        {  // RDA Alarms
            int rdaAlarms = halfwords[26];
            String binaryString = getPaddedBitString(rdaAlarms, 16);
            //System.out.println(binaryString);
            if (binaryString.charAt(15) == '1') {
                hashMap.put("rdaAlarms", "The RPG Cannot Determine the Alarms Present");
            }
            else if (binaryString.charAt(14) == '1') {
                hashMap.put("rdaAlarms", "Tower/Utilities");
            }
            else if (binaryString.charAt(13) == '1') {
                hashMap.put("rdaAlarms", "Pedestal");
            }
            else if (binaryString.charAt(12) == '1') {
                hashMap.put("rdaAlarms", "Transmitter");
            }
            else if (binaryString.charAt(11) == '1') {
                hashMap.put("rdaAlarms", "Receiver/Signal Processor");
            }
            else if (binaryString.charAt(10) == '1') {
                hashMap.put("rdaAlarms", "RDA Control");
            }
            else if (binaryString.charAt(9) == '1') {
                hashMap.put("rdaAlarms", "Wideband (RDA/RPG)");
            }
            else if (binaryString.charAt(8) == '1') {
                hashMap.put("rdaAlarms", "Wideband (User)");
            }
            else if (binaryString.charAt(7) == '1') {
                hashMap.put("rdaAlarms", "Archive II");
            }
            // if all bits are '0' (bit 6-0 are spare), then no alarms present
            else {
                hashMap.put("rdaAlarms", "No Alarms Present");
            }
        }


        {  // Data Tranmission Enabled?
            int dataTransEnabled = halfwords[27];
            String binaryString = getPaddedBitString(dataTransEnabled, 16);
            //System.out.println(binaryString);
            if (binaryString.charAt(14) == '1') {
                hashMap.put("dataTransEnabled", "None");
            }
            else {
                String str = "";
                if (binaryString.charAt(13) == '1') {
                    str += "Reflectivity  ";
                }
                else if (binaryString.charAt(12) == '1') {
                    str += "Velocity  ";
                }
                else if (binaryString.charAt(11) == '1') {
                    str += "Spectrum Width  ";
                }
                // if all bits are '0' (bit 15 and 10-0 are spare), then something went wrong
                else {
                    str = "No Message";
                }
                hashMap.put("dataTransEnabled", str);         
            }
        }



        {  // RPG Operation Status
            int rpgOpStatus = halfwords[28];
            String binaryString = getPaddedBitString(rpgOpStatus, 16);
            //System.out.println(binaryString);
            if (binaryString.charAt(15) == '1') {
                hashMap.put("rpgOpStatus", "Loadshed");
            }
            else if (binaryString.charAt(14) == '1') {
                hashMap.put("rpgOpStatus", "On-line");
            }
            else if (binaryString.charAt(13) == '1') {
                hashMap.put("rpgOpStatus", "Maintenance Action Required");
            }
            else if (binaryString.charAt(12) == '1') {
                hashMap.put("rpgOpStatus", "Maintenance Action Mandatory");
            }
            else if (binaryString.charAt(11) == '1') {
                hashMap.put("rpgOpStatus", "Commanded Shutdown");
            }
            // if all bits are '0' (bit 10-0 are spare), then no message present
            else {
                hashMap.put("rpgOpStatus", "No Message");
            }
        }



        {  // RPG Alarms
            int rpgAlarms = halfwords[29];
            String binaryString = getPaddedBitString(rpgAlarms, 16);
            //System.out.println(binaryString);
            if (binaryString.charAt(15) == '1') {
                hashMap.put("rpgAlarms", "No Alarms");
            }
            else if (binaryString.charAt(14) == '1') {
                hashMap.put("rpgAlarms", "CPU Loadshed");
            }
            else if (binaryString.charAt(13) == '1') {
                hashMap.put("rpgAlarms", "Memory Loadshed");
            }
            else if (binaryString.charAt(12) == '1') {
                hashMap.put("rpgAlarms", "RPG Control Task Failure");
            }
            else if (binaryString.charAt(11) == '1') {
                hashMap.put("rpgAlarms", "Data Base Failure");
            }
            else if (binaryString.charAt(9) == '1') {
                hashMap.put("rpgAlarms", "RPG Input Buffer Loadshed (Wideband)");
            }
            else if (binaryString.charAt(8) == '1') {
                hashMap.put("rpgAlarms", "Archive III Loadshed");
            }
            else if (binaryString.charAt(7) == '1') {
                hashMap.put("rpgAlarms", "Product Storage Loadshed");
            }
            else if (binaryString.charAt(6) == '1') {
                hashMap.put("rpgAlarms", "BDDS User Failure");
            }
            else if (binaryString.charAt(5) == '1') {
                hashMap.put("rpgAlarms", "Archive III Failure");
            }
            else if (binaryString.charAt(4) == '1') {
                hashMap.put("rpgAlarms", "MLOS FAS Failure");
            }
            else if (binaryString.charAt(3) == '1') {
                hashMap.put("rpgAlarms", "RPG/RPG Intercomputer Link Failure");
            }
            else if (binaryString.charAt(2) == '1') {
                hashMap.put("rpgAlarms", "Redundant Channel Error");
            }
            else if (binaryString.charAt(1) == '1') {
                hashMap.put("rpgAlarms", "Task Failure");
            }
            else if (binaryString.charAt(0) == '1') {
                hashMap.put("rpgAlarms", "Media Failure");
            }
            // if all bits are '0' (bit 10 is spare), then no alarms message
            else {
                hashMap.put("rpgAlarms", "No Message");
            }
        }



        {  // RPG Status
            int rpgStatus = halfwords[30];
            String binaryString = getPaddedBitString(rpgStatus, 16);
            //System.out.println(binaryString);
            if (binaryString.charAt(15) == '1') {
                hashMap.put("rpgStatus", "Restart");
            }
            else if (binaryString.charAt(14) == '1') {
                hashMap.put("rpgStatus", "Operate");
            }
            else if (binaryString.charAt(13) == '1') {
                hashMap.put("rpgStatus", "Standby");
            }
            else if (binaryString.charAt(11) == '1') {
                hashMap.put("rpgStatus", "Test Mode");
            }
            // if all bits are '0' (bit 12 and 10-0 are spare), then no status message
            else {
                hashMap.put("rpgStatus", "No Message");
            }

        }


        {  // RPG Narrowband Status
            int rpgNarrowbandStatus = halfwords[31];
            String binaryString = getPaddedBitString(rpgNarrowbandStatus, 16);
            //System.out.println(binaryString);
            if (binaryString.charAt(15) == '1') {
                hashMap.put("rpgNarrowbandStatus", "Commanded Disconnect");
            }
            else if (binaryString.charAt(14) == '1') {
                hashMap.put("rpgNarrowbandStatus", "Narrowband Loadshed");
            }
            // if all bits are '0' (bit 13-0 are spare), then no message
            else {
                hashMap.put("rpgNarrowbandStatus", "No Message");
            }
        }



        {  // Reflectivity Calibaration Correction
            int reflectCalibCorr = halfwords[32];
            hashMap.put("reflectCalibCorr", new Double(reflectCalibCorr/4.0).toString());
        }         

        // halfword[33-37] (documentation halfwords 44-48) is reserved for PUP use

        {  // RDA Channel Number
            int rdaChannelNumber = halfwords[38];
            hashMap.put("rdaChannelNumber", new Integer(rdaChannelNumber).toString());
        }


        // halfword[39-40] (documentation halfwords 50-51) is reserved for PUP use

        {  // Build Version
            int buildVersion = halfwords[41];
            hashMap.put("buildVersion", new Double(buildVersion/10.0).toString());
        }


        //System.out.println(hashMap);

        return hashMap;
    }

    /**
     * Gets the HashMap which represents the key-value pairs for the GSM data.  
     * Only applicable for the GSM (General Status Message) products.
     */
    public HashMap<String, String> getGsmHashMap() {
        return gsmHashMap;
    }

    /**
     * Gets a String that represent a text presentation of the data.  
     * Only applicable for the GSM (General Status Message) products.
     */    
    public String getGsmDisplayString(String filename) {

        StringBuffer sb = new StringBuffer();
        sb.append("  NEXRAD Level-III GENERAL STATUS MESSAGE\n");
        sb.append("  FILE: " + filename+"\n");
        sb.append("");
        sb.append("  LEVEL-III PRODUCT (BUILD) VERSION: "+gsmHashMap.get("buildVersion")+"\n");        
        sb.append("  MODE: "+gsmHashMap.get("opMode")+"\n");
        sb.append("  VCP: "+gsmHashMap.get("vcp")+"\n");
        sb.append("  RDA OP. STATUS: "+gsmHashMap.get("rdaOpStatus")+"\n");
        sb.append("  RDA STATUS: "+gsmHashMap.get("rdaStatus")+"\n");
        sb.append("  RDA ALARMS: "+gsmHashMap.get("rdaAlarms")+"\n");
        sb.append("  RPG OP. STATUS: "+gsmHashMap.get("rpgOpStatus")+"\n");
        sb.append("  RPG STATUS: "+gsmHashMap.get("rpgStatus")+"\n");
        sb.append("  RPG ALARMS: "+gsmHashMap.get("rpgAlarms")+"\n");
        sb.append("\n");
        int numElevCuts = Integer.parseInt(gsmHashMap.get("numElevCuts").toString());
        sb.append("  NUMBER OF ELEVATION CUTS: "+numElevCuts+"\n");
        for (int n=0; n<numElevCuts; n++) {
            sb.append("  ELEVATION CUT["+n+"] : "+gsmHashMap.get("elevCut["+n+"]")+"\n");
        }
        sb.append("\n");
        sb.append("  DATA TRANSMISSION ENABLED: "+gsmHashMap.get("dataTransEnabled")+"\n");
        sb.append("  RPG NARROWBAND STATUS: "+gsmHashMap.get("rpgNarrowbandStatus")+"\n");
        sb.append("  REFLECTIVITY CALIBRATION CORRECTION: "+gsmHashMap.get("reflectCalibCorr")+"\n");
        sb.append("  RDA CHANNEL NUMBER: "+gsmHashMap.get("rdaChannelNumber")+"\n");

        return sb.toString();

    }

    public String toString() {

        return this.getICAO()+" "+this.getProductString()+"("+this.getProductCode()+") "+
        this.getDate()+" "+this.getHourString()+":"+this.getMinuteString()+":"+this.getSecondString();


    }





















    public static void checkHeaderProfile(byte[] data) {
        try {

            {
                ucar.unidata.io.RandomAccessFile rafile = new ucar.unidata.io.InMemoryRandomAccessFile("NIDS DATA", data);
                rafile.order(ucar.unidata.io.RandomAccessFile.BIG_ENDIAN);

                System.out.println("CHECKING LOCATION OF BIG_ENDIAN '-1' SEPARATORS...");
                rafile.order(ucar.unidata.io.RandomAccessFile.BIG_ENDIAN);
                // Search for '-1' separators, from 0 offset
                rafile.seek(0);
                boolean done = false;
                while (! done) {
                    try {
                        while (rafile.readShort() != -1);
                        System.out.println("-- '0' Offset -- Separator at: "+rafile.getFilePointer());
                    } catch (Exception e) {
                        done = true;;
                    }
                }           
            }



            {
                ucar.unidata.io.RandomAccessFile rafile = new ucar.unidata.io.InMemoryRandomAccessFile("NIDS DATA", data);
                rafile.order(ucar.unidata.io.RandomAccessFile.BIG_ENDIAN);

                System.out.println("CHECKING LOCATION OF BIG_ENDIAN '-1' SEPARATORS...");
                // Search for '-1' separators, from 0 offset
                rafile.seek(1);
                boolean done = false;
                while (! done) {
                    try {
                        while (rafile.readShort() != -1);
                        System.out.println("-- '1' Offset -- Separator at: "+rafile.getFilePointer());
                    } catch (Exception e) {
                        done = true;;
                    }
                }
            }



            {
                ucar.unidata.io.RandomAccessFile rafile = new ucar.unidata.io.InMemoryRandomAccessFile("NIDS DATA", data);

                System.out.println("CHECKING LOCATION OF LITTLE_ENDIAN '-1' SEPARATORS...");
                rafile.order(ucar.unidata.io.RandomAccessFile.LITTLE_ENDIAN);
                // Search for '-1' separators, from 0 offset
                rafile.seek(0);
                boolean done = false;
                while (! done) {
                    try {
                        while (rafile.readShort() != -1);
                        System.out.println("-- '0' Offset -- Separator at: "+rafile.getFilePointer());
                    } catch (Exception e) {
                        done = true;;
                    }
                }
            }


            {
                ucar.unidata.io.RandomAccessFile rafile = new ucar.unidata.io.InMemoryRandomAccessFile("NIDS DATA", data);

                System.out.println("CHECKING LOCATION OF LITTLE_ENDIAN '-1' SEPARATORS...");
                rafile.order(ucar.unidata.io.RandomAccessFile.LITTLE_ENDIAN);

                // Search for '-1' separators, from 0 offset
                rafile.seek(1);
                boolean done = false;
                while (! done) {
                    try {
                        while (rafile.readShort() != -1);
                        System.out.println("-- '1' Offset -- Separator at: "+rafile.getFilePointer());
                    } catch (Exception e) {
                        done = true;;
                    }
                }

            }

            {
                ucar.nc2.iosp.nids.JNXNidsheader nids = new ucar.nc2.iosp.nids.JNXNidsheader();
                ucar.unidata.io.RandomAccessFile rafile = new ucar.unidata.io.InMemoryRandomAccessFile("NIDS DATA", data);

                System.out.println("CHECKING LOCATION OF MAGIC ZLIB BYTES...");
                rafile.order(ucar.unidata.io.RandomAccessFile.LITTLE_ENDIAN);

                byte[] magicBytes = new byte[2];
                // Search 2-byte sequences, from 0 offset
                rafile.seek(0);
                while (rafile.read(magicBytes) != -1) {
                    if (nids.isZlibHed(magicBytes) == 1) {
                        System.out.println("-- '0' Offset -- Magic Bytes at: "+rafile.getFilePointer());
                    }
                } 
            }



        } catch (Exception e) {
            e.printStackTrace();
        }
    }




























}

