package gov.noaa.ncdc.iosp.area;

import gov.noaa.ncdc.iosp.avhrr.AvhrrFile;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/*
 * @author Arthur Fotos
 * @since 2008.04.01
 * 
 * @version 2.2
 * 
 * Upgraded to ver2.2 now using netcdf-4.1  wrh 2009.08.01   
 */


public class AreaFileUtil {
    
    private static NumberFormat dayFormat = new DecimalFormat("000");
    private static NumberFormat hourFormat = new DecimalFormat("00");

    public static String findSensorSource(int sensorId){
        switch(sensorId){
        case 4 : return  "PDUS METEOSAT Visible";
        case 5 : return  "PDUS METEOSAT Infrared";
        case 6 : return  "PDUS METEOSAT Water Vapor";
        case 7 : return  "Radar'";
        case 8 : return  "Miscellaneous Aircraft Data (MAMS)";
        case 9 : return  "Raw METEOSAT";
        case 12: return  "GMS Visible prior to GMS-5";
        case 13: return  "GMS Infrared prior to GMS-5";
        case 14: return  "ATS 6 Visible";
        case 15: return  "ATS 6 Infrared";  
        case 16: return  "SMS-1 Visible";       
        case 17: return  "SMS-1 Infrared";            
        case 18: return  "SMS-2 Visible";         
        case 19: return  "SMS-2 Infrared";            
        case 20: return  "GOES-1 Visible";           
        case 21: return  "GOES-1 Infrared";           
        case 22: return  "GOES-2 Visible";         
        case 23: return  "GOES-2 Infrared";           
        case 24: return  "GOES-3 Visible";         
        case 25: return  "GOES-3 Infrared";           
        case 26: return  "GOES-4 Visible-VAS";         
        case 27: return  "GOES-4 Infrared-VAS"; 
        case 28: return  "GOES-5 Visible";            
        case 29: return  "GOES-5 Infrared-VAS"; 
        case 30: return  "GOES-6 Visible";
        case 31: return  "GOES-6 Infrared"; 
        case 32: return  "GOES-7 Visible"; 
        case 33: return  "GOES-7 Infrared";
        case 41: return  "TIROS-N POES";
        case 42: return  "NOAA-6";
        case 43: return  "NOAA-7";      
        case 44: return  "NOAA-8"; 
        case 45: return  "NOAA-9";
        case 54: return  "METEOSAT-3";
        case 55: return  "METEOSAT-4";
        case 56: return  "METEOSAT-5";
        case 57: return  "METEOSAT-6";
        case 60: return  "NOAA-10";
        case 61: return  "NOAA-11";
        case 62: return  "NOAA-12";
        case 63: return  "NOAA-13";
        case 64: return  "NOAA-14";
        case 70: return  "GOES-8 Imager";
        case 71: return  "GOES-8 Sounder";
        case 72: return  "GOES-9 Imager";
        case 73: return  "GOES-9 Sounder";
        case 74: return  "GOES-10 Imager";
        case 75: return  "GOES-10 Sounder";
        case 76: return  "GOES-11 Imager";
        case 77: return  "GOES-11 Sounder";
        case 78: return  "GOES-12 Imager";
        case 79: return  "GOES-12 Sounder";
        case 80: return  "ERBE";
        case 82: return  "GMS-4";
        case 83: return  "GMS-5";
        case 84: return "GMS-6";
        case 85: return "GMS-7";
        case 87: return "DMSP F-8";
        case 88: return "DMSP F-9";
        case 89: return "DMSP F-10";
        case 90: return "DMSP F-11";
        case 91: return "DMSP F-12";
        case 95: return "FY-1b";               
        case 96: return "FY-1c";              
        case 97: return "FY-1d";
        case 180: return  "GOES-13 Imager";
        case 181: return  "GOES-13 Sounder";
        case 182: return  "GOES-14 Imager";
        case 183: return  "GOES-14 Sounder";
        case 184: return  "GOES-15 Imager";
        case 185: return  "GOES-15 Sounder";

        default: return "Unknown";
        }           
    }

    public static String getNavType(ucar.unidata.io.RandomAccessFile raf){
        String navType = null;
        try {
            raf.seek(256);
            navType = raf.readString(4);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return navType;
    }
    
    public static String getSatelliteName(ucar.unidata.io.RandomAccessFile raf) throws IOException{
        raf.order(ucar.unidata.io.RandomAccessFile.BIG_ENDIAN);
        String satName = "";
        raf.seek(8);
        int id = raf.readInt();
        satName = findSensorSource(id);
        
        return satName;
    }
    
    
    
    public static String readBCDDate(byte[] data){
        String date = "";

        int yearK = AvhrrFile.readFourBitFlag(data[0],4);
        int yearH = AvhrrFile.readFourBitFlag(data[0],0);

        int yearT = AvhrrFile.readFourBitFlag(data[1],4);
        int yearO = AvhrrFile.readFourBitFlag(data[1],0);

        int doyH = AvhrrFile.readFourBitFlag(data[2],4);
        int doyT = AvhrrFile.readFourBitFlag(data[2],0);

        int doyO = AvhrrFile.readFourBitFlag(data[3],4);
        int hourT = AvhrrFile.readFourBitFlag(data[3],0);       
        
        int hour0 = AvhrrFile.readFourBitFlag(data[4],4);
        int minT = AvhrrFile.readFourBitFlag(data[4],0);

        int minO = AvhrrFile.readFourBitFlag(data[5],4);
        int secT = AvhrrFile.readFourBitFlag(data[5],0);

        int secO = AvhrrFile.readFourBitFlag(data[6],4);
        int msH = AvhrrFile.readFourBitFlag(data[6],0);

        int msT = AvhrrFile.readFourBitFlag(data[7],4);
        int msO = AvhrrFile.readFourBitFlag(data[7],0);     
        
        int year = 1000 * yearK + 100 * yearH + 10 * yearT + yearO;
        int day = 100 * doyH + 10 * doyT + doyO;
        int hour = 10 * hourT + hour0;
        int min = 10 * minT + minO;
        int sec = 10 * secT + secO;
        int ms = 100 * msH + 10 * msT + msO;
        date = Integer.toString(year) +  dayFormat.format(day) + hourFormat.format(hour) + hourFormat.format(min) + hourFormat.format(sec) + dayFormat.format(ms);
        return date;
    }
    
    
    /**
     * returns an int array of the image size {scanlines,pixels/line}
     * @param raf
     * @return
     */
    public static int[] getImageSize(ucar.unidata.io.RandomAccessFile raf){
        int[] size = new int[2];
        try{
            raf.seek(0);
            raf.skipBytes(32);
            size[0] = raf.readInt();
            size[1] = raf.readInt();
        }catch(Exception e){
            e.printStackTrace();
        }
        return size;
    }
    
    public static double[] calVals ={
    330,
    329.5,
    329,
    328.5,
    328,
    327.5,
    327,
    326.5,
    326,           
    325.5,           
    325,             
    324.5,           
    324,             
    323.5,           
    323,             
    322.5,           
    322,            
    321.5,          
    321,             
    320.5,           
    320,            
    319.5,           
    319,             
    318.5,           
    318,            
    317.5,           
    317,            
    316.5,           
    316,             
    315.5,           
    315,            
    314.5,          
    314,             
    313.5,           
    313,             
    312.5,           
    312,             
    311.5,          
    311,             
    310.5,          
    310,            
    309.5,          
    309,             
    308.5,           
    308,            
    307.5,           
    307,             
    306.5,           
    306,             
    305.5,          
    305,            
    304.5,          
    304,            
    303.5,          
    303,            
    302.5,           
    302,            
    301.5,          
    301,             
    300.5,           
    300,            
    299.5,          
    299,             
    298.5,          
    298,            
    297.5,           
    297,             
    296.5,          
    296,            
    295.5,          
    295,             
    294.5,           
    294,             
    293.5,           
    293,             
    292.5,           
    292,             
    291.5,           
    291,             
    290.5,           
    290,             
    289.5,           
    289,             
    288.5,           
    288,             
    287.5,           
    287,             
    286.5,           
    286,             
    285.5,           
    285,             
    284.5,           
    284,             
    283.5,           
    283,             
    282.5,           
    282,             
    281.5,           
    281,             
    280.5,           
    280,             
    279.5,           
    279,             
    278.5,           
    278,             
    277.5,           
    277,             
    276.5,           
    276,             
    275.5,           
    275,             
    274.5,           
    274,             
    273.5,           
    273,             
    272.5,           
    272,             
    271.5,           
    271,             
    270.5,           
    270,             
    269.5,           
    269,             
    268.5,           
    268,             
    267.5,           
    267,             
    266.5,           
    266,             
    265.5,           
    265,             
    264.5,           
    264,             
    263.5,           
    263,             
    262.5,           
    262,             
    261.5,           
    261,             
    260.5,           
    260,             
    259.5,           
    259,             
    258.5,           
    258,             
    257.5,           
    257,             
    256.5,           
    256,             
    255.5,           
    255,             
    254.5,           
    254,             
    253.5,           
    253,             
    252.5,           
    252,             
    251.5,           
    251,             
    250.5,           
    250,             
    249.5,           
    249,             
    248.5,          
    248,             
    247.5,          
    247,            
    246.5,          
    246,             
    245.5,          
    245,             
    244.5,          
    244,             
    243.5,          
    243,             
    242.5,          
    242,             
    241,             
    240,             
    239,             
    238,             
    237,             
    236,             
    235,             
    234,             
    233,             
    232,             
    231,             
    230,             
    229,             
    228,             
    227,             
    226,             
    225,             
    224,             
    223,             
    222,             
    221,             
    220,             
    219,             
    218,             
    217,             
    216,             
    215,             
    214,             
    213,             
    212,             
    211,             
    210,             
    209,             
    208,             
    207,             
    206,             
    205,             
    204,             
    203,             
    202,             
    201,             
    200,             
    199,             
    198,             
    197,             
    196,             
    195,             
    194,             
    193,             
    192,             
    191,             
    190,             
    189,             
    188,             
    187,             
    186,             
    185,             
    184,             
    183,             
    182,             
    181,             
    180,             
    179,             
    178,             
    177,             
    176,             
    175,             
    174,             
    173,             
    172,             
    171,             
    170,             
    169,             
    168,             
    167,             
    166,             
    165,             
    164,            
    163} ;  
    
    public static double[] getCalibratedValues(){
        return calVals;
    }
}
