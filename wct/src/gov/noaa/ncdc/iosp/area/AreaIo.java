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

import edu.wisc.ssec.mcidas.AREAnav;
import edu.wisc.ssec.mcidas.AreaDirectory;
import edu.wisc.ssec.mcidas.AreaFileException;
import edu.wisc.ssec.mcidas.GVARnav;
import gov.noaa.ncdc.iosp.VariableInfo;
import gov.noaa.ncdc.iosp.VariableInfoManager;
import gov.noaa.ncdc.swath.NcdcSwathTransform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.ma2.Section;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.constants._Coordinate;
import ucar.unidata.geoloc.ProjectionImpl;
import ucar.unidata.util.Parameter;

/**
 *  This routine allows ncjava to access McIdas AREA files
 * 
 * @author Ken Knapp
 * @since 2006.04.01
 * 
 * @version 2.2
 * Upgraded to ver2.1 now using netcdf-4.1  wrh 2009.08.01   
 */

public class AreaIo {
    
    private VariableInfoManager varInfoManager = new VariableInfoManager();

    public ucar.unidata.io.RandomAccessFile raFile = null;

    private GvarNavBlock gvarBlock;

    private NCDCAreaFile af=null;
//    private static AreaDirectory areaDir;
    private AreaDir areaDir;
    private int [] dir;
    private AREAnav ng = null;
    private int [] nav;

    private List<Dimension> dimPixelScanBand = new ArrayList<Dimension>();
    private List<Dimension> dimPixelScan = new ArrayList<Dimension>();
    private List<Dimension> dimScan = new ArrayList<Dimension>();
    private List<Dimension> dimPixel = new ArrayList<Dimension>();
    private List<Dimension> dimTime = new ArrayList<Dimension>();
    private List<Dimension> dimChar = new ArrayList<Dimension>();
    private List<Dimension> dimCharScan = new ArrayList<Dimension>();
    private List<Dimension> dimDeltaLon = new ArrayList<Dimension>();
    private List<Dimension> dimDeltaRad = new ArrayList<Dimension>();
    private List<Dimension> dimDeltaLat = new ArrayList<Dimension>();
    private List<Dimension> dimSinusoid = new ArrayList<Dimension>();
    private List<Dimension> dimNominal = new ArrayList<Dimension>();
//    private List<Dimension> dimPrefixScan = new ArrayList<Dimension> ();
    
    public boolean dimensionsAreSet = false;
    public boolean latLonCalculated = false;
    public boolean imageHasBeenRead = false;

    public int [][][] image = null;
    public int numPixels = 0;
    public int numScans =  0;
    public int numBands =  0;
    
    protected VariableInfo Time, SCAN, PIXEL, IMAGE,RADIANCE,CALIBRATED_DATA; 
    
    AreaDirectory ad;

    //area directory variables
    protected VariableInfo  AREA_STATUS, VERSION_NUM, SENSOR_ID_NUM, IMG_DATE, IMG_TIME, NORTH_BOUND, WEST_VIS_ELEM,
    ZCOORD, NUM_LINES, NUM_ELEM, BYTES_PIXEL, LINE_RES, ELEM_RES, NUM_CHAN, PREFIX_BYTES, PROJ_NUM, CREATION_DATE, CREATION_TIME, SNDR_FILTER_MAP, 
    IMAGE_ID, COMMENT, PRI_KEY_CAL, PRI_KEY_NAV, SEC_KEY_NAV, VAL_CODE, PDL, BAND8, ACT_IMG_DATE, ACT_IMG_TIME,
    ACT_START_SCAN, LEN_PREFIX_DOC, LEN_PREFIX_CAL, LEN_PREFIX_LEV, SRC_TYPE, CALIB_TYPE, /*AVGSAMPLE, POES_SIGNAL, POES_UPDOWN,
    ORIG_SRC_TYPE,  */AUX_BLOCK_OFFSET, AUX_BLOCK_LENGTH, CAL_BLOCK_OFFSET,NUM_COMMENT_REC, BAND_NUM;
    
    //gvar nav vars - only available for GVARNav mcidas files
    protected VariableInfo NAV_TYPE, ID, /*IMC_STATUS,*/ REF_LONG, REF_NOM, REF_LAT, REF_YAW, REF_ROLL, REF_PITCH, REF_ATT_YAW,
            EPOCH_DATE, EPOCH_TIME, DELTA_TIME, EPOCH_TIME_DELTA, IMC_ROLL, IMC_PITCH, IMC_YAW, LON_DELTAS, RAD_DELTAS, LAT_DELTAS,
            YAW_DELTAS, SOLAR_RATE, EXPO_START_TIME, EXMAGR, EXTICR, MATANR, SINANR, SINUSOID_MAG_R, SINUSOID_PA_R, NUMMSR, OAPSR,
            O1MSDR, MMSDR, PAMSR, AEMZR, EXMAGP, EXTICP, MATANP, SINANP, SINUSOID_MAG_P, SINUSOID_PA_P, NUMMSP, OAPSP, O1MSDP,
            MMSDP, PAMSP, AEMZP, EXMAGY, EXTICY, MATANY, SINANY, SINUSOID_MAG_Y, SINUSOID_PA_Y, NUMMSY, OAPSY, O1MSDY, MMSDY,
            PAMSY, AEMZY, EXMAGRM, MATANRM, SINANRM, SINUSOID_MAG_RM, SINUSOID_PA_RM, NUMMSRM, OAPSRM, O1MSDRM, MMSDRM, PAMSRM,
            AEMZRM, EXMAGPM, EXTICPM, MATANPM, SINANPM, SINUSOID_MAG_PM, SINUSOID_PA_PM, NUMMSPM, OAPSPM, O1MSDPM, MMSDPM, PAMSPM,
            AEMZPM, IMC_STATUS_BIT0, IMC_STATUS_BIT1, IMC_STATUS_BIT2, IMC_STATUS_BIT3, IMC_STATUS_BIT4, IMC_STATUS_BIT5,
            IMC_STATUS_BIT6, IMC_STATUS_BIT7, IMC_STATUS_BIT8, IMC_STATUS_BIT9, IMC_STATUS_BIT10, IMC_STATUS_BIT11,
            IMC_STATUS_BIT12, IMC_STATUS_BIT13, IMC_STATUS_BIT14, IMC_STATUS_BIT15, IMC_STATUS_BIT16, IMC_STATUS_BIT17,
            IMC_STATUS_BIT18, IMC_STATUS_BIT19, IMC_STATUS_BIT20, IMC_STATUS_BIT21, IMC_STATUS_BIT22, IMC_STATUS_BIT23,
            IMC_STATUS_BIT24, IMC_STATUS_BIT25, IMC_STATUS_BIT26, IMC_STATUS_BIT27, IMC_STATUS_BIT28, IMC_STATUS_BIT29,
            IMC_STATUS_BIT30, IMC_STATUS_BIT31;
    
 
   
    //Line Prefix variables
   
    //PREFIX -BLOCK HEADER VARS
    private VariableInfo BLOCKID, WORD_SIZE, WORD_COUNT, PRODUCT_ID, REPEAT_FLAG, GVAR_VERSION_NUM, DATA_VALID, ASCII_BINARY, SPS_ID,
        RANGE_WORD, BLOCK_COUNT, SPS_TIME, ERROR_CHECK;
    
    //PREFIX - DOC VARS
    private VariableInfo SPC_ID, SPSID_DOC, ISCAN, IDSUB, TCURR, TCHED, TCTRL, TLHED, TLTRL, TIPFS, TINFS, TISPC, TIECL, TIBBC, TISTR, TIRAN, TIIRT, TIVIT, TCLMT, TIONA;
    
    //PREFIX - LINE DOC
    private VariableInfo SPC_ID_LD, SPS_ID_LD, LSIDE, LIDET, LICHA, RISCT, L1SCAN, L2SCAN, LPIXLS, LWORDS, LZCOR, LLAG;
       
    public static boolean isValidFile ( ucar.unidata.io.RandomAccessFile raf ) {
        String fileName = raf.getLocation();
        try { 
//            NCDCAreaFile af = new NCDCAreaFile(fileName);
            String navtype = AreaFileUtil.getNavType(raf);
            if("GVAR".equals(navtype)){
                return true;
            }
//        } catch (AreaFileException e) {
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public void setAttributesAndVariables(ucar.unidata.io.RandomAccessFile raf, NetcdfFile ncfile) throws IOException,InvalidRangeException, AreaFileException {
        varInfoManager.clearAll();
        this.raFile = raf;
        af = new NCDCAreaFile(raf.getLocation());
        dir = af.getDir();
        areaDir = new AreaDir();
        areaDir.readScanLine(raf);
        


        ad = new AreaDirectory(dir);
        numPixels = ad.getElements();
        numScans = ad.getLines();
        numBands= ad.getNumberOfBands();
        nav = af.getNav();
        ng = new GVARnav(nav);  // XXXXnav is the specific implementation
        gvarBlock = new GvarNavBlock();
        gvarBlock.readNav(raf);
      
        //define dimensions
        ucar.nc2.Dimension GeoX = new ucar.nc2.Dimension("GeoX",numPixels,true);
        ucar.nc2.Dimension GeoY = new ucar.nc2.Dimension("GeoY",numScans,true);
        ucar.nc2.Dimension band = new ucar.nc2.Dimension("band",numBands,true);
        ucar.nc2.Dimension time = new ucar.nc2.Dimension("time",1,true);
        ucar.nc2.Dimension text = new ucar.nc2.Dimension("char",32,true);
        ucar.nc2.Dimension deltaLon = new ucar.nc2.Dimension("deltaLon",13,true);
        ucar.nc2.Dimension deltaRad = new ucar.nc2.Dimension("delatRad",11,true);
        ucar.nc2.Dimension deltaLat = new ucar.nc2.Dimension("deltaLat",9,true);
        ucar.nc2.Dimension sinusoid = new ucar.nc2.Dimension("sinusoid",15,true);
        ucar.nc2.Dimension nominal = new ucar.nc2.Dimension("nominal",4,true);
//        ucar.nc2.Dimension prefix = new ucar.nc2.Dimension("prefix",prefixLength,true);
        if ( ! dimensionsAreSet ) {
            //dimPixelScanBand.add(band);
            dimPixelScanBand.add(GeoY);
            dimPixelScanBand.add(GeoX);
            dimPixelScan.add(GeoY);
            dimPixelScan.add(GeoX);
            dimScan.add(GeoY);
            dimPixel.add(GeoX);
            dimTime.add(time);
            dimChar.add(text);
            
            dimCharScan.add(GeoY);
            dimCharScan.add(text);
            dimDeltaLon.add(time);
            dimDeltaLon.add(deltaLon);
            
            dimDeltaRad .add(time);
            dimDeltaRad .add(deltaRad);
            dimDeltaLat.add(time);
            dimDeltaLat.add(deltaLat);
            dimSinusoid.add(time);
            dimSinusoid.add(sinusoid);
            dimNominal.add(time);
            dimNominal.add(nominal);
//            dimPrefixScan.add(prefix);
//            dimPrefixScan.add(GeoY);
            dimensionsAreSet=true;
        }

        //Global dimensions
//        ncfile.addAttribute(null,new Attribute("IOSP_Author","Kenjimoto"));
//      ncfile.addAttribute(null, new Attribute("Conventions", "CF-1.0"));
        
        //add dimensions
        ncfile.addDimension(null,GeoX);
        ncfile.addDimension(null,GeoY);
        ncfile.addDimension(null,time);
        ncfile.addDimension(null,text);
        ncfile.addDimension(null,deltaLon);
        ncfile.addDimension(null,deltaRad);
        ncfile.addDimension(null,deltaLat);
        ncfile.addDimension(null,sinusoid);
        ncfile.addDimension(null,nominal);
//        ncfile.addDimension(null,prefix);
        
        //ncfile.addDimension(null,band);

        //set variables & attributes using VariableInfo
        //VariableInfo.clearAll();
        //Initialize VariableInfo
        Time = varInfoManager.addVariableInfo("time","Time",null,DataType.INT,dimTime);
        SCAN = varInfoManager.addVariableInfo("GeoY","GeoY line","km",DataType.INT,dimScan);
        PIXEL =varInfoManager.addVariableInfo("GeoX","GeoX number","km",DataType.INT,dimPixel);
        IMAGE = varInfoManager.addVariableInfo("image","Image counts","count",DataType.SHORT,dimPixelScanBand,"GeoSatSwath");
        int bandNum = (byte)ad.getBands()[0];
        if (bandNum != 1) {
        	RADIANCE = varInfoManager.addVariableInfo("radiance","Radiance","mW/(m2-sr-cm-1)",DataType.FLOAT,dimPixelScanBand,"GeoSatSwath");
        	CALIBRATED_DATA = varInfoManager.addVariableInfo("calibratedData","Brightness Temp","kelvin",DataType.FLOAT,dimPixelScanBand,"GeoSatSwath");
        }
        
        ////Area Block Variables (first 256 bytes of file)
        BAND_NUM = varInfoManager.addVariableInfo("bandNum","Band used in this Area File",null,DataType.BYTE,dimTime);
        AREA_STATUS = varInfoManager.addVariableInfo("areaStatus","Area Status - 0 if record is valid",null,DataType.INT, dimTime);
        VERSION_NUM = varInfoManager.addVariableInfo("versionNum","McIDAS area file format version number",null,DataType.INT,dimTime);
        SENSOR_ID_NUM = varInfoManager.addVariableInfo("sensorId","McIDAS sensor identifier",null,DataType.CHAR,dimChar);
//      IMG_DATE = varInfoManager.addVariableInfo("imageDate","nominal year and day of the image"," YYYYDDD",DataType.INT,dimTime);
//      IMG_TIME = varInfoManager.addVariableInfo("imageTime","nominal time of the image","HHMMSS UTC",DataType.INT,dimTime);
        IMG_DATE = varInfoManager.addVariableInfo("imageDate","nominal year and day of the image (YYYYDDD)"," ",DataType.INT,dimTime);
        IMG_TIME = varInfoManager.addVariableInfo("imageTime","nominal time of the image HHMMSS UTC"," ",DataType.INT,dimTime);
        NORTH_BOUND = varInfoManager.addVariableInfo("startLine","upper left image line coordinate",null,DataType.INT,dimTime);
        WEST_VIS_ELEM = varInfoManager.addVariableInfo("startElement","upper left image element coordinate",null,DataType.INT,dimTime);
        ZCOORD = varInfoManager.addVariableInfo("zCoord","Upper Z-coordinate",null,DataType.INT,dimTime);
        NUM_LINES = varInfoManager.addVariableInfo("numLines","number of lines in the image",null,DataType.INT, dimTime);
        NUM_ELEM = varInfoManager.addVariableInfo("numElements","number of data points per line",null,DataType.INT,dimTime); 
        BYTES_PIXEL = varInfoManager.addVariableInfo("bytesPixel","number of bytes per data point ","byte",DataType.INT,dimTime);
        LINE_RES = varInfoManager.addVariableInfo("lineRes","data resolution in line direction ",null,DataType.INT, dimTime);
        ELEM_RES = varInfoManager.addVariableInfo("elementRes","data resolution in element direction",null,DataType.INT,dimTime);
        NUM_CHAN = varInfoManager.addVariableInfo("numChan","number of channels in image",null,DataType.INT,dimTime);
        PREFIX_BYTES = varInfoManager.addVariableInfo("prefixBytes","Number of Bytes in line prefix","byte",DataType.INT,dimTime);   //word 15 = word49 + word 50 + word51 (+ 4 if word 36 validity code is present)
        PROJ_NUM = varInfoManager.addVariableInfo("projectionNum","SSEC project number used in creating image",null,DataType.INT,dimTime);    //word 16
        CREATION_DATE = varInfoManager.addVariableInfo("creationDate","year and day image was created"," YYDDD",DataType.INT,dimTime);     
        CREATION_TIME = varInfoManager.addVariableInfo("creationTime","Time image was created", "HHMMSS",DataType.INT,dimTime);           //word 18
        SNDR_FILTER_MAP = varInfoManager.addVariableInfo("bandMap","spectral band map, bit set for each of 32 bands present ",null,DataType.BYTE, dimTime);  //word 19
        IMAGE_ID = varInfoManager.addVariableInfo("imageId","Image ID Number",null,DataType.INT,dimTime);  
        COMMENT = varInfoManager.addVariableInfo("comment","Comments",null,DataType.CHAR,dimChar);
        PRI_KEY_CAL = varInfoManager.addVariableInfo("priKeyCalib","Calibration colicil (area number)",null,DataType.INT,dimTime); 
        PRI_KEY_NAV = varInfoManager.addVariableInfo("priKeyNav","Primary navigation codicil (date)",null,DataType.INT,dimTime); 
        SEC_KEY_NAV = varInfoManager.addVariableInfo("secKeyNav","Secondary navigation codicil(nav)",null,DataType.INT,dimTime); 
        VAL_CODE = varInfoManager.addVariableInfo("validityCode","Validity Code",null,DataType.INT,dimTime);
        BAND8 = varInfoManager.addVariableInfo("band8","Where band 8 came from",null,DataType.INT,dimTime);
        ACT_IMG_DATE = varInfoManager.addVariableInfo("actualImgDate","Actual Image Date","YYYYDD",DataType.INT,dimTime);
        ACT_IMG_TIME = varInfoManager.addVariableInfo("actualImgTime","Actual Image TIme","HHMMSS",DataType.INT,dimTime);
        ACT_START_SCAN = varInfoManager.addVariableInfo("actualStartScan","Actual start scan",null,DataType.INT,dimTime); 
        LEN_PREFIX_DOC = varInfoManager.addVariableInfo("prefixDocLength","Length of prefix DOC","bytes",DataType.INT,dimTime);
        LEN_PREFIX_CAL = varInfoManager.addVariableInfo("prefixCalLength","Length of prefix CAL","bytes",DataType.INT,dimTime);
        LEN_PREFIX_LEV = varInfoManager.addVariableInfo("prefixLevLength","Length of prefix LEV","bytes",DataType.INT,dimTime);
        SRC_TYPE = varInfoManager.addVariableInfo("sourceType","Source Type",null,DataType.CHAR,dimChar);
        CALIB_TYPE = varInfoManager.addVariableInfo("calibrationType","Calibration Type",null,DataType.CHAR, dimChar);
//        AVGSAMPLE = varInfoManager.addVariableInfo("averageSample","Data was averaged(0), or sampled(1)",null,DataType.BYTE,dimTime);
//        POES_SIGNAL = varInfoManager.addVariableInfo("poesSignal","LAC, GAC, HRPT",null,DataType.INT, dimTime);
//       POES_UPDOWN = varInfoManager.addVariableInfo("poesUpDown","POES ascending/descending",null,DataType.INT,dimTime);
//        ORIG_SRC_TYPE = varInfoManager.addVariableInfo("srcType","original source type",null,DataType.CHAR,dimTime);
        AUX_BLOCK_OFFSET = varInfoManager.addVariableInfo("auxBlockOffset","byte offset to the beginning of the area file's AUX block","byte",DataType.INT,dimTime);
        AUX_BLOCK_LENGTH = varInfoManager.addVariableInfo("auxBlockLength","length of area file's AUX block","byte",DataType.INT,dimTime);
        CAL_BLOCK_OFFSET = varInfoManager.addVariableInfo("calBlockOffset","byte offset to the beginning of the area file's CAL block",null,DataType.INT,dimTime); 
        NUM_COMMENT_REC = varInfoManager.addVariableInfo("commentRecCount","number of comment records in AUDIT block",null,DataType.INT,dimTime);
        //end of Area Directory variables
        
        
        //GVAR nav variables
        NAV_TYPE = varInfoManager.addVariableInfo("navType","Navigation Type",null,DataType.CHAR,dimChar);
        ID = varInfoManager.addVariableInfo("id","ID",null,DataType.CHAR,dimChar);
//          IMC_STATUS = varInfoManager.addVariableInfo("imcStatus","Imager Scan Status",null,DataType.BYTE,dimTime);
        IMC_STATUS_BIT0 = varInfoManager.addVariableInfo("imcBit0","Status Bit 0 - 1 if frame start",null,DataType.BYTE,dimTime);
        IMC_STATUS_BIT1 = varInfoManager.addVariableInfo("imcBit1","Status Bit 1 - 1 if frame end",null,DataType.BYTE,dimTime);
        IMC_STATUS_BIT2 = varInfoManager.addVariableInfo("imcBit2","Status Bit 2 - 1 if frame break - line(s) lost",null,DataType.BYTE,dimTime);
        IMC_STATUS_BIT3 = varInfoManager.addVariableInfo("imcBit3","Status Bit 3 - 1 if pixel(s) lost",null,DataType.BYTE,dimTime);
        IMC_STATUS_BIT4 = varInfoManager.addVariableInfo("imcBit4","Status Bit 4 - 1 if priority 1 frame data",null,DataType.BYTE,dimTime);
        IMC_STATUS_BIT5 = varInfoManager.addVariableInfo("imcBit5","Status Bit 5 - 1 if priority 2 frame data",null,DataType.BYTE,dimTime);
        IMC_STATUS_BIT6 = varInfoManager.addVariableInfo("imcBit6","Status Bit 6 - 0 if west-to-east scan, 1 if east-to-west scan",null,DataType.BYTE,dimTime);
        IMC_STATUS_BIT7 = varInfoManager.addVariableInfo("imcBit7","Status Bit 7 - 0 if north-to-south frame,1 if south-to-north frame",null,DataType.BYTE,dimTime);
        IMC_STATUS_BIT8 = varInfoManager.addVariableInfo("imcBit8","Status Bit 8 - 1 if IMC active",null,DataType.BYTE,dimTime);
        IMC_STATUS_BIT9 = varInfoManager.addVariableInfo("imcBit9","Status Bit 9  -1 if lost header block",null,DataType.BYTE,dimTime);
        IMC_STATUS_BIT10 = varInfoManager.addVariableInfo("imcBit10","Status Bit 10 - 1 if lost trailer block",null,DataType.BYTE,dimTime);
        IMC_STATUS_BIT11 = varInfoManager.addVariableInfo("imcBit11","Status Bit 11 - 1 if lost telemetry data",null,DataType.BYTE,dimTime);
        IMC_STATUS_BIT12 = varInfoManager.addVariableInfo("imcBit12","Status Bit 12 - 1 if (star sense) time",null,DataType.BYTE,dimTime);
        IMC_STATUS_BIT13 = varInfoManager.addVariableInfo("imcBit13","Status Bit 13 - 0 if side 1 (primary) active,1 if side 2 (secondary) active",null,DataType.BYTE,dimTime);
        IMC_STATUS_BIT14 = varInfoManager.addVariableInfo("imcBit14","Status Bit 14 - 1 if visible normalization active",null,DataType.BYTE,dimTime);
        IMC_STATUS_BIT15 = varInfoManager.addVariableInfo("imcBit15","Status Bit 15 - 1 if IR calibration active",null,DataType.BYTE,dimTime);
        IMC_STATUS_BIT16 = varInfoManager.addVariableInfo("imcBit16","Status Bit 16 - if yaw flip processing enabled",null,DataType.BYTE,dimTime);
        IMC_STATUS_BIT17 = varInfoManager.addVariableInfo("imcBit17","Status Bit 17 - 1= IR detector 1 data is invalid",null,DataType.BYTE,dimTime);
        IMC_STATUS_BIT18 = varInfoManager.addVariableInfo("imcBit18","Status Bit 18 - 1= IR detector 2 data is invalid",null,DataType.BYTE,dimTime);
        IMC_STATUS_BIT19 = varInfoManager.addVariableInfo("imcBit19","Status Bit 19 - 1= IR detector 3 data is invalid",null,DataType.BYTE,dimTime);
        IMC_STATUS_BIT20 = varInfoManager.addVariableInfo("imcBit20","Status Bit 20 - 1= IR detector 4 data is invalid",null,DataType.BYTE,dimTime);
        IMC_STATUS_BIT21 = varInfoManager.addVariableInfo("imcBit21","Status Bit 21 - 1= IR detector 5 data is invalid",null,DataType.BYTE,dimTime);
        IMC_STATUS_BIT22 = varInfoManager.addVariableInfo("imcBit22","Status Bit 22 - 1= IR detector 6 data is invalid",null,DataType.BYTE,dimTime);
        IMC_STATUS_BIT23 = varInfoManager.addVariableInfo("imcBit23","Status Bit 23 - 1= IR detector 7 data is invalid",null,DataType.BYTE,dimTime);
        IMC_STATUS_BIT24 = varInfoManager.addVariableInfo("imcBit24","Status Bit 24 - 1= Visible detector 1 data is invalid",null,DataType.BYTE,dimTime);         
        IMC_STATUS_BIT25 = varInfoManager.addVariableInfo("imcBit25","Status Bit 25 - 1= Visible detector 2 data is invalid",null,DataType.BYTE,dimTime);         
        IMC_STATUS_BIT26 = varInfoManager.addVariableInfo("imcBit26","Status Bit 26 - 1= Visible detector 3 data is invalid",null,DataType.BYTE,dimTime);         
        IMC_STATUS_BIT27 = varInfoManager.addVariableInfo("imcBit27","Status Bit 27 - 1= Visible detector 4 data is invalid",null,DataType.BYTE,dimTime);         
        IMC_STATUS_BIT28 = varInfoManager.addVariableInfo("imcBit28","Status Bit 28 - 1= Visible detector 5 data is invalid",null,DataType.BYTE,dimTime);         
        IMC_STATUS_BIT29 = varInfoManager.addVariableInfo("imcBit29","Status Bit 29 - 1= Visible detector 6 data is invalid",null,DataType.BYTE,dimTime);         
        IMC_STATUS_BIT30 = varInfoManager.addVariableInfo("imcBit30","Status Bit 30 - 1= Visible detector 7 data is invalid",null,DataType.BYTE,dimTime);         
        IMC_STATUS_BIT31 = varInfoManager.addVariableInfo("imcBit31","Status Bit 31 - 1= Visible detector 8 data is invalid",null,DataType.BYTE,dimTime);          
            
        REF_LONG = varInfoManager.addVariableInfo("refLong","reference longitude","rad",DataType.INT,dimTime,1E-7f,null,null,null);
        REF_NOM = varInfoManager.addVariableInfo("refDistNominal","reference distance from nominal","km",DataType.INT,dimTime,1E-7f,null,null,null);
        REF_LAT = varInfoManager.addVariableInfo("refLat","reference latitude","rad",DataType.INT,dimTime,1E-7f,null,null,null);
        REF_YAW = varInfoManager.addVariableInfo("refYaw","reference yaw","rad",DataType.INT,dimTime,1E-7f,null,null,null);
        REF_ROLL = varInfoManager.addVariableInfo("refRoll","reference attitude roll","rad",DataType.INT,dimTime,1E-7f,null,null,null);
        REF_PITCH = varInfoManager.addVariableInfo("refPitch","reference attitude pitch","rad",DataType.INT,dimTime,1E-7f,null,null,null);
        REF_ATT_YAW = varInfoManager.addVariableInfo("refAttYaw","reference attitude yaw","rad",DataType.INT,dimTime,1E-7f,null,null,null);
        EPOCH_TIME= varInfoManager.addVariableInfo("epochDate","epoch date/time BCD format",null,DataType.CHAR,dimChar);
        DELTA_TIME = varInfoManager.addVariableInfo("timeDelat","delta from epoch time","min",DataType.INT,dimTime,1E-2f,null,null,null); 
        EPOCH_TIME_DELTA = varInfoManager.addVariableInfo("epochTimeDelta","delta from epoch time","min",DataType.INT,dimTime,1E-2f,null,null,null);
        IMC_ROLL = varInfoManager.addVariableInfo("imcRoll","image motion conpensation roll","rad",DataType.INT,dimTime,1E-7f,null,null,null);
        IMC_PITCH = varInfoManager.addVariableInfo("imcPitch","image motion conpensation pitch","rad",DataType.INT,dimTime,1E-7f,null,null,null);
        IMC_YAW  = varInfoManager.addVariableInfo("imcYaw","image motion conpensation yaw","rad",DataType.INT,dimTime,1E-7f,null,null,null);
        LON_DELTAS = varInfoManager.addVariableInfo("longitudeDeltas","longitude delta from reference values","rad",DataType.INT,dimDeltaLon,1E-7f,null,null,null);
        RAD_DELTAS = varInfoManager.addVariableInfo("radialDeltas","radial distance delta from reference values","rad",DataType.INT,dimDeltaRad,1E-7f,null,null,null);
        LAT_DELTAS  = varInfoManager.addVariableInfo("latDeltas","geocentric latitude delta values",null,DataType.INT,dimDeltaLat,1E-7f,null,null,null);
        YAW_DELTAS = varInfoManager.addVariableInfo("yawDeltas","orbit yaw delta values",null,DataType.INT,dimDeltaLat,1E-7f,null,null,null);
        SOLAR_RATE = varInfoManager.addVariableInfo("solarRate","daily solar rate","rad/min",DataType.INT,dimTime,1E-7f,null,null,null);
        EXPO_START_TIME = varInfoManager.addVariableInfo("expoStartTime","exponential start time from epoch","min",DataType.INT,dimTime,1E-2f,null,null,null);
        
        EXMAGR = varInfoManager.addVariableInfo("exmagr","Roll Exponential magnitude ","rad",DataType.INT,dimTime);
        EXTICR = varInfoManager.addVariableInfo("exticr","Roll Exponential time constant ","min",DataType.INT,dimTime);
        MATANR = varInfoManager.addVariableInfo("matanr","Roll Constant, mean attitude angle","rad",DataType.INT,dimTime);
        SINANR = varInfoManager.addVariableInfo("sinanr","Roll Number of sinusoids or Angles (I*4, none)",null,DataType.INT,dimTime);
        SINUSOID_MAG_R = varInfoManager.addVariableInfo("sinusoidMagRoll","Roll Magnitude of sinusoid","rad",DataType.INT,dimSinusoid);
        SINUSOID_PA_R = varInfoManager.addVariableInfo("sinusoidPaRoll","Roll Phase angle of first-order sinusoid","rad",DataType.INT,dimSinusoid);
        NUMMSR = varInfoManager.addVariableInfo("nummsr","Roll Number of monomial sinusoids",null,DataType.INT,dimTime);
        OAPSR = varInfoManager.addVariableInfo("oapsr","Roll Order of applicable sinusoid","rad",DataType.INT,dimNominal);
        O1MSDR = varInfoManager.addVariableInfo("o1msdr","Roll Order of monomial sinusoid","rad",DataType.INT,dimNominal);
        MMSDR = varInfoManager.addVariableInfo("mmsdr","Roll Magnitude of monomial sinusoid","rad",DataType.INT,dimNominal);
        PAMSR = varInfoManager.addVariableInfo("pamsr","Roll Phase angle of monomial sinusoid ","rad",DataType.INT,dimNominal);
        AEMZR = varInfoManager.addVariableInfo("aemzr","Roll Angle from epoch where monomial is zero ","rad",DataType.INT,dimNominal);
            
        EXMAGP = varInfoManager.addVariableInfo("exmagp","Pitch Exponential magnitude ","rad",DataType.INT,dimTime);
        EXTICP = varInfoManager.addVariableInfo("exticp","Pitch Exponential time constant ","min",DataType.INT,dimTime);
        MATANP = varInfoManager.addVariableInfo("matanp","Pitch Constant, mean attitude angle","rad",DataType.INT,dimTime);
        SINANP = varInfoManager.addVariableInfo("sinanp","Pitch Number of sinusoids or Angles (I*4, none)",null,DataType.INT,dimTime);
        SINUSOID_MAG_P = varInfoManager.addVariableInfo("sinusoidMagPitch","Pitch Magnitude of sinusoid","rad",DataType.INT,dimSinusoid);
        SINUSOID_PA_P = varInfoManager.addVariableInfo("sinusoidPaPitch","Pitch Phase angle of first-order sinusoid","rad",DataType.INT,dimSinusoid);
        NUMMSP = varInfoManager.addVariableInfo("nummsp","Pitch Number of monomial sinusoids",null,DataType.INT,dimTime);
        OAPSP = varInfoManager.addVariableInfo("oapsp","Pitch Order of applicable sinusoid","rad",DataType.INT,dimNominal);
        O1MSDP = varInfoManager.addVariableInfo("o1msdp","Pitch Order of monomial sinusoid","rad",DataType.INT,dimNominal);
        MMSDP = varInfoManager.addVariableInfo("mmsdp","Pitch Magnitude of monomial sinusoid","rad",DataType.INT,dimNominal);
        PAMSP = varInfoManager.addVariableInfo("pamsp","Pitch Phase angle of monomial sinusoid ","rad",DataType.INT,dimNominal);
        AEMZP = varInfoManager.addVariableInfo("aemzp","Pitch Angle from epoch where monomial is zero ","rad",DataType.INT,dimNominal);           
            
        EXMAGY = varInfoManager.addVariableInfo("exmagy","Yaw Exponential magnitude ","rad",DataType.INT,dimTime);
        EXTICY = varInfoManager.addVariableInfo("exticy","Yaw Exponential time constant ","min",DataType.INT,dimTime);
        MATANY = varInfoManager.addVariableInfo("matany","Yaw Constant, mean attitude angle","rad",DataType.INT,dimTime);
        SINANY = varInfoManager.addVariableInfo("sinany","Yaw Number of sinusoids or Angles (I*4, none)",null,DataType.INT,dimTime);
        SINUSOID_MAG_Y = varInfoManager.addVariableInfo("sinusoidMagYaw","Yaw Magnitude of sinusoid","rad",DataType.INT,dimSinusoid);
        SINUSOID_PA_Y = varInfoManager.addVariableInfo("sinusoidPaYaw","Yaw Phase angle of first-order sinusoid","rad",DataType.INT,dimSinusoid);
        NUMMSY = varInfoManager.addVariableInfo("nummsy","Yaw Number of monomial sinusoids",null,DataType.INT,dimTime);
        OAPSY = varInfoManager.addVariableInfo("oapsy","Yaw Order of applicable sinusoid","rad",DataType.INT,dimNominal);
        O1MSDY = varInfoManager.addVariableInfo("o1msdy","Yaw Order of monomial sinusoid","rad",DataType.INT,dimNominal);
        MMSDY = varInfoManager.addVariableInfo("mmsdy","Yaw Magnitude of monomial sinusoid","rad",DataType.INT,dimNominal);
        PAMSY = varInfoManager.addVariableInfo("pamsy","Yaw Phase angle of monomial sinusoid ","rad",DataType.INT,dimNominal);
        AEMZY = varInfoManager.addVariableInfo("aemzy","Yaw Angle from epoch where monomial is zero ","rad",DataType.INT,dimNominal);      
            
        EXMAGRM = varInfoManager.addVariableInfo("exticrm","Roll Misalign Exponential time constant ","min",DataType.INT,dimTime);
        MATANRM = varInfoManager.addVariableInfo("matanrm","Roll Misalign Constant, mean attitude angle","rad",DataType.INT,dimTime);
        SINANRM = varInfoManager.addVariableInfo("sinanrm","Roll Misalign Number of sinusoids or Angles (I*4, none)",null,DataType.INT,dimTime);
        SINUSOID_MAG_RM = varInfoManager.addVariableInfo("sinusoidMagRM","Roll Misalign Magnitude of sinusoid","rad",DataType.INT,dimSinusoid);
        SINUSOID_PA_RM = varInfoManager.addVariableInfo("sinusoidPaRM","Roll Misalign Phase angle of first-order sinusoid","rad",DataType.INT,dimSinusoid);
        NUMMSRM = varInfoManager.addVariableInfo("nummsrm","Roll Misalign Number of monomial sinusoids",null,DataType.INT,dimTime);
        OAPSRM = varInfoManager.addVariableInfo("oapsrm","Roll Misalign Order of applicable sinusoid","rad",DataType.INT,dimNominal);
        O1MSDRM = varInfoManager.addVariableInfo("o1msdrm","Roll Misalign Order of monomial sinusoid","rad",DataType.INT,dimNominal);
        MMSDRM = varInfoManager.addVariableInfo("mmsdrm","Roll Misalign Magnitude of monomial sinusoid","rad",DataType.INT,dimNominal);
        PAMSRM = varInfoManager.addVariableInfo("pamsrmp","Roll Misalign Phase angle of monomial sinusoid ","rad",DataType.INT,dimNominal);
        AEMZRM = varInfoManager.addVariableInfo("aemzrm","Roll Misalign Angle from epoch where monomial is zero ","rad",DataType.INT,dimNominal);         
            
        EXMAGPM = varInfoManager.addVariableInfo("exmagpm","Pitch Misalignment Exponential magnitude ","rad",DataType.INT,dimTime);
        EXTICPM= varInfoManager.addVariableInfo("exticpm","Pitch Misalignment Exponential time constant ","min",DataType.INT,dimTime);
        MATANPM = varInfoManager.addVariableInfo("matanpm","Pitch Misalignment Constant, mean attitude angle","rad",DataType.INT,dimTime);
        SINANPM = varInfoManager.addVariableInfo("sinanpm","Pitch Misalignment Number of sinusoids or Angles (I*4, none)",null,DataType.INT,dimTime);
        SINUSOID_MAG_PM = varInfoManager.addVariableInfo("sinusoidMagPM","Pitch Misalignment Magnitude of sinusoid","rad",DataType.INT,dimSinusoid);
        SINUSOID_PA_PM = varInfoManager.addVariableInfo("sinusoidPaPM","Pitch Misalignment Phase angle of first-order sinusoid","rad",DataType.INT,dimSinusoid);
        NUMMSPM = varInfoManager.addVariableInfo("nummspm","Pitch Misalignment Number of monomial sinusoids",null,DataType.INT,dimTime);
        OAPSPM = varInfoManager.addVariableInfo("oapspm","Pitch Misalignment Order of applicable sinusoid","rad",DataType.INT,dimNominal);
        O1MSDPM = varInfoManager.addVariableInfo("o1msdpm","Pitch Misalignment Order of monomial sinusoid","rad",DataType.INT,dimNominal);
        MMSDPM= varInfoManager.addVariableInfo("mmsdpm","Pitch Misalignment Magnitude of monomial sinusoid","rad",DataType.INT,dimNominal);
        PAMSPM = varInfoManager.addVariableInfo("pamspm","Pitch Misalignment Phase angle of monomial sinusoid ","rad",DataType.INT,dimNominal);
        AEMZPM = varInfoManager.addVariableInfo("aemzpm","Pitch Misalignment Angle from epoch where monomial is zero ","rad",DataType.INT,dimNominal);                    
 //end of gvar variables

/**        
        VariableInfo LATITUDE = varInfoManager.addVariableInfo("lat", "Latitude",
                "degrees_north", DataType.FLOAT, dimPixelScan, -9999.f,
                -9999.f, -9999, -1);
        VariableInfo LONGITUDE = varInfoManager.addVariableInfo("lon", "Longitude",
                "degrees_east", DataType.FLOAT, dimPixelScan, -9999.f, -9999.f,
                -9999, -1);
*/
        VariableInfo LATITUDE = varInfoManager.addVariableInfo("latitude", "Latitude","degrees_north", DataType.FLOAT, dimPixelScan);
        VariableInfo LONGITUDE = varInfoManager.addVariableInfo("longitude", "Longitude","degrees_east", DataType.FLOAT, dimPixelScan);
        
       //PREFIX vars
            //Header block
        BLOCKID = varInfoManager.addVariableInfo("blockId", "GVAR Block ID",null,DataType.SHORT,dimScan );
        WORD_SIZE = varInfoManager.addVariableInfo("wordSize","Word Size",null,DataType.BYTE,dimScan);
        WORD_COUNT = varInfoManager.addVariableInfo("wordCount","Word Count",null,DataType.INT,dimScan);
        PRODUCT_ID = varInfoManager.addVariableInfo("productId","Product Id",null,DataType.SHORT,dimScan);
        REPEAT_FLAG = varInfoManager.addVariableInfo("repeatFlag","Repeat Flag",null,DataType.BYTE,dimScan);
        GVAR_VERSION_NUM = varInfoManager.addVariableInfo("gvarVersionNum","GVAR Version Number",null,DataType.BYTE,dimScan);
        DATA_VALID = varInfoManager.addVariableInfo("dataValid","Data Valid, 0=Filler data, 1=Valid Data",null,DataType.BYTE,dimScan);
        ASCII_BINARY = varInfoManager.addVariableInfo("asciiBinary","ASCII or Binary Data, 0=Binary, 1=ASCII",null,DataType.BYTE,dimScan);
        SPS_ID = varInfoManager.addVariableInfo("spsId","SPS ID",null,DataType.BYTE,dimScan);
        RANGE_WORD = varInfoManager.addVariableInfo("rangeWord","Range Word",null,DataType.CHAR,dimCharScan);
        BLOCK_COUNT = varInfoManager.addVariableInfo("blockCount","Block Count",null,DataType.INT,dimScan);
        SPS_TIME = varInfoManager.addVariableInfo("spsTime","SPS Time",null,DataType.CHAR,dimCharScan);
        ERROR_CHECK = varInfoManager.addVariableInfo("errorCheck","Error Check",null,DataType.INT,dimScan);
       
        //PREFIX - DOC VARS
        SPC_ID = varInfoManager.addVariableInfo("spcId","Spacecraft ID",null,DataType.BYTE,dimScan);
        SPSID_DOC = varInfoManager.addVariableInfo("spsIdentity","SPS Identity",null,DataType.BYTE,dimScan);
        ISCAN = varInfoManager.addVariableInfo("iscan","Imager Scan status",null,DataType.INT,dimScan);
 //      IDSUB = varInfoManager.addVariableInfo(DataType.,dimScan);
        TCURR = varInfoManager.addVariableInfo("tcurr","Current SPS time",null,DataType.CHAR,dimCharScan);
        TCHED = varInfoManager.addVariableInfo("tched","Time of current header block",null,DataType.CHAR,dimCharScan);
        TCTRL = varInfoManager.addVariableInfo("tctrl","Time of current trailer block",null,DataType.CHAR,dimCharScan);
        TLHED = varInfoManager.addVariableInfo("tlhed","Time of lagged header block",null,DataType.CHAR,dimCharScan);
        TLTRL = varInfoManager.addVariableInfo("tltrl","Time of current trailer block",null,DataType.CHAR,dimCharScan);
        TIPFS = varInfoManager.addVariableInfo("tipfs","Time of priority frame start",null,DataType.CHAR,dimCharScan);
        TINFS = varInfoManager.addVariableInfo("tinfs","Time of normal frame start",null,DataType.CHAR,dimCharScan);
        TISPC = varInfoManager.addVariableInfo("tispc","Time of last spacelock calibration",null,DataType.CHAR,dimCharScan);
        TIECL = varInfoManager.addVariableInfo("tiecl","Time of last ECAL",null,DataType.CHAR,dimCharScan);
        TIBBC = varInfoManager.addVariableInfo("tibbc","Time of last BB-Cal",null,DataType.CHAR,dimCharScan);
        TISTR = varInfoManager.addVariableInfo("tistr","Time of last star sense",null,DataType.CHAR,dimCharScan); 
        TIRAN = varInfoManager.addVariableInfo("tiran","Time of last ranging measurement",null,DataType.CHAR,dimCharScan);
        TIIRT = varInfoManager.addVariableInfo("tiirt","Time tag of current IR calibration set",null,DataType.CHAR,dimCharScan);
        TIVIT = varInfoManager.addVariableInfo("tivit","Time tag of current visible NLUT set",null,DataType.CHAR,dimCharScan);
        TCLMT = varInfoManager.addVariableInfo("tclmt","Time tag of current Limits sets",null,DataType.CHAR,dimCharScan);
        TIONA = varInfoManager.addVariableInfo("tiona","Time tag current O&A set implemented",null,DataType.CHAR,dimCharScan);   
       
       //PREFIX - LINE DOC
        SPC_ID_LD = varInfoManager.addVariableInfo("craftId","Spacecraft ID",null,DataType.BYTE,dimScan);   
        SPS_ID_LD = varInfoManager.addVariableInfo("sourceSPS", "Source SPS",null,DataType.BYTE,dimScan);   
        LSIDE = varInfoManager.addVariableInfo("lside","LSIDE - Current active detector configuration",null,DataType.SHORT,dimScan);
        LIDET = varInfoManager.addVariableInfo("lidet","detector number",null,DataType.BYTE,dimScan);
        LICHA = varInfoManager.addVariableInfo("licha","Souce channel",null,DataType.BYTE,dimScan);
        RISCT = varInfoManager.addVariableInfo("risct","RISCT - relative output scan count since start of imaging frame",null,DataType.INT,dimScan);
        L1SCAN = varInfoManager.addVariableInfo("l1scan","imager scan status word 1",null,DataType.SHORT,dimScan);
        L2SCAN = varInfoManager.addVariableInfo("l2scan","imager scan status word 2", null, DataType.SHORT,dimScan);
        LPIXLS = varInfoManager.addVariableInfo("lpixls","Number of pixels in detector data record",null,DataType.INT,dimScan);
        LWORDS = varInfoManager.addVariableInfo("lwords","Number of words in the detector record", null, DataType.INT,dimScan);
        LZCOR = varInfoManager.addVariableInfo("lzcor", "LZCOR - Zonal Correction(pixel offset",null,DataType.SHORT,dimScan);  
        LLAG = varInfoManager.addVariableInfo("llag","LLAG, 0=current, 1=latest lagged, 2=oldest lagged",null,DataType.BYTE,dimScan);
        //end of GVAR Prefix Variables

        
        Variable var3 = new Variable (ncfile,null,null,"GeoSatSwath");
        var3.setDataType(DataType.CHAR);
        var3.setDimensions("");
        var3.addAttribute( new Attribute(_Coordinate.Axes, "GeoX GeoY"));
//      ncfile.addVariableAttribute(var3,new Attribute("_CoordinateTransforms","NcdcSwath"));
        ncfile.addVariableAttribute(var3,new Attribute("_CoordinateTransforms","NcdcSwath"));
//      ncfile.addVariableAttribute(var3,new Attribute("_CoordinateTest","Test"));
        
        ncfile.addVariable(null, var3);
/**
      
    Variable var2 = new Variable (ncfile,null,null,"McIDASProjection");
    var2.setDataType(DataType.CHAR);
    ncfile.addVariableAttribute(var2,new Attribute("_CoordinateTransformType","projection"));
    ncfile.addVariableAttribute(var2,new Attribute("transform_name","mcidas_area"));
    ncfile.addVariableAttribute(var2,new Attribute("_CoordinateTest","Test"));    
         
*/
        Array arrayDir = new ArrayInt.D1(64);
        Index dirIndex = arrayDir.getIndex();
        for (int i=0; i< 64; i++){
            arrayDir.setInt(dirIndex.set(i), dir[i]);
        }

        Array arrayNav = null; 

            arrayNav = new ArrayInt.D1(640);

        Index navIndex = arrayNav.getIndex();
        for (int i=0; i< arrayNav.getSize(); i++){
            arrayNav.setInt(navIndex.set(i), nav[i]);
        }

        ProjectionImpl projection = null;
        projection = new NcdcSwathTransform(arrayDir,arrayNav,"GVAR");
//        projection = new ucar.unidata.geoloc.projection.McIDASProjection(dir,nav);

        // coordinate transform variable
        Variable ct = new Variable( ncfile, null, null, "NcdcSwath");
        ct.setDataType( DataType.CHAR);
        ct.setDimensions("");
        List params = projection.getProjectionParameters();
        for (int i = 0; i < params.size(); i++) {
            Parameter p = (Parameter) params.get(i);
            ct.addAttribute( new Attribute(p));
        }
        ct.addAttribute( new Attribute(_Coordinate.TransformType, "Projection"));
 
        ct.addAttribute( new Attribute("AreaHeader",arrayDir));
        ct.addAttribute( new Attribute("NavHeader", arrayNav));
        // fake data
        Array dataA = Array.factory(DataType.CHAR.getPrimitiveClassType(), new int[] {});
        dataA.setChar(dataA.getIndex(), ' ');
        ct.setCachedData(dataA, false);

        ncfile.addVariable(null, ct);
        ncfile.addAttribute( null, new Attribute("Conventions","Kenjimoto"));

        //ncfile.addAttribute(null,new Attribute("Conventions","CF-1.0"));
        varInfoManager.setVariables( ncfile );

        //Lastly, finish the file   
        ncfile.finish();
    }

    
    public int getChanNum(AreaDirectory ad){
        int chanNum = ad.getBands()[0];
       
    return chanNum;
    }
    
     
    public ucar.ma2.Array getVariableValues(ucar.nc2.Variable v2, Section section) throws IOException, InvalidRangeException {
        Range geoXRange = null;
        Range geoYRange = null;
        if(section != null & section.getRank() > 0){
            geoYRange = section.getRange(0);
            if(section.getRank() > 1){
                geoXRange = section.getRange(1);
            }
        }
        
        String varname = v2.getFullName();
//        System.out.println("var: "+varname+"  X RANGE: "+geoXRange+" Y RANGE: "+geoYRange);

        //ucar.ma2.Array dataArray = Array.factory(v2.getDataType(), v2.getShape());
        ucar.ma2.Array dataArray = Array.factory(v2.getDataType().getPrimitiveClassType(), section.getShape());
        Index dataIndex = dataArray.getIndex();
        
        if (v2.getFullName().equals("latitude") || v2.getFullName().equals("longitude")) {
            double[][] pixel = new double[2][1];
            double[][] latLon;
            //Initialize navigation variables
            ng.setImageStart(dir[5], dir[6]);
            ng.setRes(dir[11], dir[12]);
            ng.setStart(1, 1);

            

            // Use Range object, which calculates requested i, j values and incorporates stride
            for (int i = 0; i < geoXRange.length(); i++) {
                for (int j = 0; j < geoYRange.length(); j++) {
                    pixel[0][0] = (double) geoXRange.element(i);
                    pixel[1][0] = (double) geoYRange.element(j);
                    latLon = ng.toLatLon(pixel);

//                    System.out.println(Arrays.deepToString(latLon));
                    
                    if (v2.getFullName().startsWith("lat")) {
                        dataArray.setFloat(dataIndex.set(j, i), (float) (latLon[0][0]));
                    } else {
                        dataArray.setFloat(dataIndex.set(j, i), (float) (latLon[1][0]));
                    }
                }
            }
        }

        if (v2.getFullName().equals(PIXEL.getName()) || v2.getFullName().equals(SCAN.getName())) {
            int[] shape = v2.getShape();
            System.out.println();
            for (int i = 0; i < section.getRange(0).length(); i++) {
                dataArray.setInt(dataIndex.set(i), section.getRange(0).first()+i);
            }
        }

        
        // WRH 2010.06.28 per Steve's Climate toolkit changes for performance
        if (v2.getFullName().equals(IMAGE.getName())) {
            Range xrange = section.getRange(0);
            Range yrange = section.getRange(1);
            try {
              // Use Range object, which calculates requested i, j values and incorporates stride
              // int[][] pixelData = new int[1][1];
              // for (int j = 0; j < geoYRange.length(); j++) {
              //   for (int i = 0; i < geoXRange.length(); i++) {
              //     pixelData = af.getData(geoYRange.element(j),geoXRange.element(i), 1, 1);
              //     dataArray.setInt(dataIndex.set(j, i),(pixelData[0][0]) / 32);
              //   }
              // } 
              int bytesPer = areaDir.getBytesPixel();   
              int[][] pixelData = new
              int[xrange.length()][yrange.length()];
              pixelData = af.getData(xrange.first(),
              yrange.first(),xrange.length(), yrange.length());
              for (int i = 0; i < xrange.length(); i++) {
                for (int j = 0; j <yrange.length(); j++) {
                  if (2 == bytesPer) {      
                    dataArray.setFloat(dataIndex.set(i,j), (pixelData[i][j]/32));
                  }else{
                    dataArray.setFloat(dataIndex.set(i,j), (pixelData[i][j]));  
                  }
                }
              }
              
              } catch (AreaFileException afe) {
                afe.printStackTrace();
                throw new IOException(afe.toString());
              }

           }

                
//              int bytesPer = areaDir.getBytesPixel();
//              int[][] pixelData = new int[1][1];
//              for (int j = 0; j < geoYRange.length(); j++) {
//                  for (int i = 0; i < geoXRange.length(); i++) {
//                      pixelData = af.getData(geoYRange.element(j), geoXRange.element(i), 1, 1);
//                      if(2 == bytesPer){
//                          dataArray.setInt(dataIndex.set(j, i), (pixelData[0][0])/32);
//                      }else{
//                          dataArray.setInt(dataIndex.set(j, i), (pixelData[0][0]));
//                      }
//                  }
//              }

//          } catch (AreaFileException afe) {
//              afe.printStackTrace();
//              throw new IOException(afe.toString());
//          }

//      }
        
        
        int chanNum = getChanNum(ad); 
        if (chanNum > 1) {  
          if (v2.getFullName().equals(CALIBRATED_DATA.getName())) {
            Range xrange = section.getRange(0);
            Range yrange = section.getRange(1);
            try {
             // Use Range object, which calculates requested i, j values and incorporates stride
              float[][] data = new float[xrange.length()][yrange.length()];
              data = af.getCalibratedData(xrange.first(), yrange.first(),xrange.length(), yrange.length());
              for (int i = 0; i < xrange.length(); i++) {
                for (int j = 0; j <yrange.length(); j++) {
                  dataArray.setFloat(dataIndex.set(i,j), (data[i][j]));
                }
              }

              } catch (AreaFileException afe) {
                afe.printStackTrace();
                throw new IOException(afe.toString());
              }
          }
          
        }  

        
        if (RADIANCE != null && v2.getFullName().equals(RADIANCE.getName())) {
            Range xrange = section.getRange(0);
            Range yrange = section.getRange(1);
            try {
                // Use Range object, which calculates requested i, j values and incorporates stride
                float[][] data = new float[xrange.length()][yrange.length()];
                data = af.getRadianceData(xrange.first(), yrange.first(),xrange.length(), yrange.length());
                for (int i = 0; i < xrange.length(); i++) {
                    for (int j = 0; j <yrange.length(); j++) {
                        dataArray.setFloat(dataIndex.set(i,j), (data[i][j]));
                    }
                }

            } catch (AreaFileException afe) {
                afe.printStackTrace();
                throw new IOException(afe.toString());
            }

        }
        
        
        if (v2.getFullName().equals(Time.getName())) {
            int[] shape = v2.getShape();
            for (int i = 0; i < shape[0]; i++) {
                dataArray.setInt(dataIndex.set(i), (int) (19991231235959l));
            }

        }
        
        
//###########       area block vars         ######################//
        if(varname.equals(BAND_NUM.getName())){
            dataArray.setInt(dataIndex.set(0),(byte)ad.getBands()[0]);  
        }else if(varname.equals(AREA_STATUS.getName())){
            dataArray.setInt(dataIndex.set(0),areaDir.getStatus());
        }else if(varname.equals(VERSION_NUM.getName())){
            dataArray.setInt(dataIndex.set(0),areaDir.getVersion());
        }else if(varname.equals(SENSOR_ID_NUM.getName())){
            int instNum = areaDir.getSatId();
            String s = " " + instNum + " - " + AreaFileUtil.findSensorSource(instNum);
            for(int i=0;i<s.length();i++){
                dataArray.setChar(dataIndex.set(i),s.charAt(i));
            }
            //need to get sat/instrument name and append to number
            dataArray.setInt(dataIndex.set(0),instNum);
        }else if(varname.equals(IMG_DATE.getName())){
            dataArray.setInt(dataIndex.set(0), areaDir.getImageDate());
        }else if(varname.equals(IMG_TIME.getName())){
            dataArray.setInt(dataIndex.set(0),areaDir.getImageTime());
        }else if(varname.equals(NORTH_BOUND.getName())){
            dataArray.setInt(dataIndex.set(0), areaDir.getYCoord());
        }else if(varname.equals(WEST_VIS_ELEM.getName())){
            dataArray.setInt(dataIndex.set(0),areaDir.getXCoord());
        }else if(varname.equals(ZCOORD.getName())){
            dataArray.setInt(dataIndex.set(0),areaDir.getZCoord());
        }else if(varname.equals(NUM_LINES.getName())){
            dataArray.setInt(dataIndex.set(0),areaDir.getNumLine());
        }else if(varname.equals(NUM_ELEM.getName())){
            dataArray.setInt(dataIndex.set(0),areaDir.getNumEle());
        }else if(varname.equals(BYTES_PIXEL.getName())){
            dataArray.setInt(dataIndex.set(0),areaDir.getBytesPixel());
        }else if(varname.equals(LINE_RES.getName())){
            dataArray.setInt(dataIndex.set(0),areaDir.getLinesRes());
        }else if(varname.equals(ELEM_RES.getName())){
            dataArray.setInt(dataIndex.set(0),areaDir.getElemRes());
        }else if(varname.equals(NUM_CHAN.getName())){
            dataArray.setInt(dataIndex.set(0),areaDir.getNumChan());
        }else if(varname.equals(PREFIX_BYTES.getName())){
            dataArray.setInt(dataIndex.set(0),areaDir.getPrefixBytes());
        }else if(varname.equals(PROJ_NUM.getName())){
            dataArray.setInt(dataIndex.set(0),areaDir.getProjNum());
        }else if(varname.equals(CREATION_DATE.getName())){
            dataArray.setInt(dataIndex.set(0), areaDir.getCreationDate());
        }else if(varname.equals(CREATION_TIME.getName())){
            dataArray.setInt(dataIndex.set(0), areaDir.getCreationTime());
        }else if(varname.equals(SNDR_FILTER_MAP.getName())){
            int i = areaDir.getFilterMap();
            String s = Integer.toBinaryString(i);
            dataArray.setByte(dataIndex.set(0),(byte)s.length());
        }else if(varname.equals(IMAGE_ID.getName())){
            dataArray.setInt(dataIndex.set(0),areaDir.getImageIdNum());
        }else if(varname.equals(COMMENT.getName())){
            String comment = areaDir.getCommnets();;
            for(int i=0;i<comment.length();i++){
                dataArray.setChar(dataIndex.set(i),comment.charAt(i));
            }
        }else if(varname.equals(PRI_KEY_CAL.getName())){
            dataArray.setInt(dataIndex.set(0),areaDir.getPriKeyCalib());
        }else if(varname.equals(PRI_KEY_NAV.getName())){
            dataArray.setInt(dataIndex.set(0),areaDir.getPriKeyNav());
        }else if(varname.equals(SEC_KEY_NAV.getName())){
            dataArray.setInt(dataIndex.set(0),areaDir.getSecKeyNav());
        }else if(varname.equals(VAL_CODE.getName())){
            dataArray.setInt(dataIndex.set(0),areaDir.getValidityCode());
        }else if(varname.equals(BAND8.getName())){
            dataArray.setInt(dataIndex.set(0),areaDir.getBand8());
        }else if(varname.equals(ACT_IMG_DATE.getName())){
            dataArray.setInt(dataIndex.set(0),areaDir.getActImgDate());
        }else if(varname.equals(ACT_IMG_TIME.getName())){
            dataArray.setInt(dataIndex.set(0),areaDir.getActImgTime());
        }else if(varname.equals(ACT_START_SCAN.getName())){
            dataArray.setInt(dataIndex.set(0),areaDir.getActStartScan());
        }else if(varname.equals(LEN_PREFIX_DOC.getName())){
            dataArray.setInt(dataIndex.set(0),dir[48]);
        }else if(varname.equals(LEN_PREFIX_CAL.getName())){
            dataArray.setInt(dataIndex.set(0),dir[49]);
        }else if(varname.equals(LEN_PREFIX_LEV.getName())){
            dataArray.setInt(dataIndex.set(0),dir[50]);
        }else if(varname.equals(SRC_TYPE.getName())){
            String st = areaDir.getSrcType();
            for(int i=0;i<st.length();i++){
                dataArray.setObject(dataIndex.set(i),st.charAt(i));
            }
        }else if(varname.equals(CALIB_TYPE.getName())){
            String ct = areaDir.getCalType();
            for(int i=0;i<ct.length();i++){
                dataArray.setObject(dataIndex.set(i),ct.charAt(i));
            }
        }
        else if(varname.equals(AUX_BLOCK_OFFSET.getName())){
            dataArray.setInt(dataIndex.set(0),dir[59]);
        }else if(varname.equals(AUX_BLOCK_LENGTH.getName())){
            dataArray.setInt(dataIndex.set(0), dir[60]);
        }
        
        /**
        else if(varname.equals(AVGSAMPLE.getName())){
            dataArray.setByte(dataIndex.set(0),(byte)areaDir.getAvgSample());
        }else if(varname.equals(POES_SIGNAL.getName())){
            dataArray.setByte(dataIndex.set(0),(byte)areaDir.getAvgSample());
        }else if(varname.equals(POES_UPDOWN.getName())){
            dataArray.setByte(dataIndex.set(0),(byte)areaDir.getAvgSample());
        }else if(varname.equals(ORIG_SRC_TYPE.getName())){
            String ost = areaDir.getOrigSrcType();
            for(int i=0;i<ost.length();i++){
                dataArray.setChar(dataIndex.set(0),ost.charAt(i));
            }
        }  

        */
        else if(varname.equals(CAL_BLOCK_OFFSET.getName())){
            dataArray.setInt(dataIndex.set(0),dir[62]);
        }
        else if(varname.equals(NUM_COMMENT_REC.getName())){
            dataArray.setInt(dataIndex.set(0),dir[63]);
        }
//  ###############  end of Area Directory Variable ###############################################################    
        
        
//  ############## Beginning of GVAR Nav Variables       #############################################
        
        else if(varname.equals(NAV_TYPE.getName())){
            String type = gvarBlock.getType();
            for(int i=0;i<4;i++){
                dataArray.setChar(dataIndex.set(i),type.charAt(i));
            }
        }else if(varname.equals(ID.getName())){
            String type = gvarBlock.getId();
            for(int i=0;i<4;i++){
                dataArray.setChar(dataIndex.set(i),type.charAt(i));
            }
        }else if(varname.equals(IMC_STATUS_BIT0.getName())){
            dataArray.setByte(dataIndex.set(0),gvarBlock.getScanStatus()[0]);
        }else if(varname.equals(IMC_STATUS_BIT1.getName())){
            dataArray.setByte(dataIndex.set(0),gvarBlock.getScanStatus()[1]);
        }else if(varname.equals(IMC_STATUS_BIT2.getName())){
            dataArray.setByte(dataIndex.set(0),gvarBlock.getScanStatus()[2]);
        }else if(varname.equals(IMC_STATUS_BIT3.getName())){
            dataArray.setByte(dataIndex.set(0),gvarBlock.getScanStatus()[3]);
        }else if(varname.equals(IMC_STATUS_BIT4.getName())){
            dataArray.setByte(dataIndex.set(0),gvarBlock.getScanStatus()[4]);
        }else if(varname.equals(IMC_STATUS_BIT5.getName())){
            dataArray.setByte(dataIndex.set(0),gvarBlock.getScanStatus()[5]);
        }else if(varname.equals(IMC_STATUS_BIT6.getName())){
            dataArray.setByte(dataIndex.set(0),gvarBlock.getScanStatus()[6]);
        }else if(varname.equals(IMC_STATUS_BIT7.getName())){
            dataArray.setByte(dataIndex.set(0),gvarBlock.getScanStatus()[7]);
        }else if(varname.equals(IMC_STATUS_BIT8.getName())){
            dataArray.setByte(dataIndex.set(0),gvarBlock.getScanStatus()[8]);
        }else if(varname.equals(IMC_STATUS_BIT9.getName())){
            dataArray.setByte(dataIndex.set(0),gvarBlock.getScanStatus()[9]);
        }else if(varname.equals(IMC_STATUS_BIT10.getName())){
            dataArray.setByte(dataIndex.set(0),gvarBlock.getScanStatus()[10]);
        }else if(varname.equals(IMC_STATUS_BIT11.getName())){
            dataArray.setByte(dataIndex.set(0),gvarBlock.getScanStatus()[11]);
        }else if(varname.equals(IMC_STATUS_BIT12.getName())){
            dataArray.setByte(dataIndex.set(0),gvarBlock.getScanStatus()[12]);
        }else if(varname.equals(IMC_STATUS_BIT13.getName())){
            dataArray.setByte(dataIndex.set(0),gvarBlock.getScanStatus()[13]);
        }else if(varname.equals(IMC_STATUS_BIT14.getName())){
            dataArray.setByte(dataIndex.set(0),gvarBlock.getScanStatus()[14]);
        }else if(varname.equals(IMC_STATUS_BIT15.getName())){
            dataArray.setByte(dataIndex.set(0),gvarBlock.getScanStatus()[15]);
        }else if(varname.equals(IMC_STATUS_BIT16.getName())){
            dataArray.setByte(dataIndex.set(0),gvarBlock.getScanStatus()[16]);
        }else if(varname.equals(IMC_STATUS_BIT17.getName())){
            dataArray.setByte(dataIndex.set(0),gvarBlock.getScanStatus()[17]);
        }else if(varname.equals(IMC_STATUS_BIT18.getName())){
            dataArray.setByte(dataIndex.set(0),gvarBlock.getScanStatus()[18]);
        }else if(varname.equals(IMC_STATUS_BIT19.getName())){
            dataArray.setByte(dataIndex.set(0),gvarBlock.getScanStatus()[19]);
        }else if(varname.equals(IMC_STATUS_BIT20.getName())){
            dataArray.setByte(dataIndex.set(0),gvarBlock.getScanStatus()[20]);
        }else if(varname.equals(IMC_STATUS_BIT21.getName())){
            dataArray.setByte(dataIndex.set(0),gvarBlock.getScanStatus()[21]);
        }else if(varname.equals(IMC_STATUS_BIT22.getName())){
            dataArray.setByte(dataIndex.set(0),gvarBlock.getScanStatus()[22]);
        }else if(varname.equals(IMC_STATUS_BIT23.getName())){
            dataArray.setByte(dataIndex.set(0),gvarBlock.getScanStatus()[23]);
        }else if(varname.equals(IMC_STATUS_BIT24.getName())){
            dataArray.setByte(dataIndex.set(0),gvarBlock.getScanStatus()[24]);
        }else if(varname.equals(IMC_STATUS_BIT25.getName())){
            dataArray.setByte(dataIndex.set(0),gvarBlock.getScanStatus()[25]);
        }else if(varname.equals(IMC_STATUS_BIT26.getName())){
            dataArray.setByte(dataIndex.set(0),gvarBlock.getScanStatus()[26]);
        }else if(varname.equals(IMC_STATUS_BIT27.getName())){
            dataArray.setByte(dataIndex.set(0),gvarBlock.getScanStatus()[27]);
        }else if(varname.equals(IMC_STATUS_BIT28.getName())){
            dataArray.setByte(dataIndex.set(0),gvarBlock.getScanStatus()[28]);
        }else if(varname.equals(IMC_STATUS_BIT29.getName())){
            dataArray.setByte(dataIndex.set(0),gvarBlock.getScanStatus()[29]);
        }else if(varname.equals(IMC_STATUS_BIT30.getName())){
            dataArray.setByte(dataIndex.set(0),gvarBlock.getScanStatus()[30]);
        }else if(varname.equals(IMC_STATUS_BIT31.getName())){
            dataArray.setByte(dataIndex.set(0),gvarBlock.getScanStatus()[31]);
        }else if(varname.equals(REF_LONG.getName())){
            dataArray.setInt(dataIndex.set(0),gvarBlock.getRefLong());
        }else if(varname.equals(REF_NOM.getName())){
            dataArray.setInt(dataIndex.set(0),gvarBlock.getRefNominalDist());
        }else if(varname.equals(REF_LAT.getName())){
            dataArray.setInt(dataIndex.set(0),gvarBlock.getRefLat());
        }else if(varname.equals(REF_YAW.getName())){
            dataArray.setInt(dataIndex.set(0),gvarBlock.getRefYaw());
        }else if(varname.equals(REF_ROLL.getName())){
        dataArray.setInt(dataIndex.set(0),gvarBlock.getRefAttitudeRoll());
        }else if(varname.equals(REF_PITCH.getName())){
            dataArray.setInt(dataIndex.set(0),gvarBlock.getRefAttitudePitch());
        }else if(varname.equals(REF_ATT_YAW.getName())){
            dataArray.setInt(dataIndex.set(0),gvarBlock.getRefAttitudeYaw());
        }else if(varname.equals(EPOCH_TIME.getName())){
            String s = gvarBlock.getEpochTime();
            for(int i=0;i<s.length();i++){
                dataArray.setChar(dataIndex.set(i),s.charAt(i));
            }
        }else if(varname.equals(DELTA_TIME.getName())){
                
        }else if(varname.equals(EPOCH_TIME_DELTA.getName())){
//          dataArray.setInt(dataIndex.set(i),gvarBlock.get);
        }else if(varname.equals(IMC_ROLL.getName())){
            dataArray.setInt(dataIndex.set(0),gvarBlock.getImcRoll());
        }else if(varname.equals(IMC_PITCH.getName())){
            dataArray.setInt(dataIndex.set(0),gvarBlock.getImcPitch());
        }else if(varname.equals(IMC_YAW.getName())){
            dataArray.setInt(dataIndex.set(0),gvarBlock.getImcYaw());
        }else if(varname.equals(LON_DELTAS.getName())){
            for(int i=0;i<13;i++){
                dataArray.setInt(dataIndex.set(0,i),gvarBlock.getLongDeltas()[i]);
            }
        }else if(varname.equals(RAD_DELTAS.getName())){
            for(int i=0;i<11;i++){
                dataArray.setInt(dataIndex.set(0,i),gvarBlock.getRadialDeltas()[i]);
            }
        }else if(varname.equals(LAT_DELTAS.getName())){
            for(int i=0;i<9;i++){
                dataArray.setInt(dataIndex.set(0,i),gvarBlock.getGeocentricLatDeltas()[i]);
            }
        }else if(varname.equals(YAW_DELTAS.getName())){
            for(int i=0;i<9;i++){
                dataArray.setInt(dataIndex.set(0,i),gvarBlock.getOrbitYawDeltas()[i]);
            }
        }else if(varname.equals(SOLAR_RATE.getName())){
            dataArray.setInt(dataIndex.set(0),gvarBlock.getSolarRate());
        }else if(varname.equals(EXPO_START_TIME.getName())){
            dataArray.setInt(dataIndex.set(0),gvarBlock.getExpEpochStartTime());
        }else if(varname.equals(EXMAGPM.getName())){
            dataArray.setInt(dataIndex.set(0),gvarBlock.getPitchMisalign().getExpMagnitude());
        }else if(varname.equals(EXTICPM.getName())){
            dataArray.setInt(dataIndex.set(0),gvarBlock.getPitchMisalign().getExpTimeConst());
        }else if(varname.equals(MATANPM.getName())){
            dataArray.setInt(dataIndex.set(0),gvarBlock.getPitchMisalign().getMeanAttitudeAngleConst());
        }else if(varname.equals(SINANPM.getName())){
            dataArray.setInt(dataIndex.set(0),gvarBlock.getPitchMisalign().getNumSinusoids());
        }else if(varname.equals(SINUSOID_MAG_PM.getName())){
            for(int i=0;i<15;i++){
                dataArray.setInt(dataIndex.set(0,i),gvarBlock.getPitchMisalign().getSinusoidMagnitude()[i]);
            }
        }else if(varname.equals(SINUSOID_PA_PM.getName())){
            for(int i=0;i<15;i++){
                dataArray.setInt(dataIndex.set(0,i),gvarBlock.getPitchMisalign().getSinusoidPhaseAngle()[i]);
            }
        }else if(varname.equals(NUMMSPM.getName())){
            dataArray.setInt(dataIndex.set(0),gvarBlock.getPitchMisalign().getNumMonoSinusoids());
        }else if(varname.equals(OAPSPM.getName())){
            for(int i=0;i<4;i++){
                dataArray.setInt(dataIndex.set(0,i),gvarBlock.getPitchMisalign().getOrderOfApplicableSinusoid()[i]);
            }
        }else if(varname.equals(O1MSDPM.getName())){
            for(int i=0;i<4;i++){
                dataArray.setInt(dataIndex.set(0,i),gvarBlock.getPitchMisalign().getOrderOfMonomialSinusoid()[i]);
            }
        }else if(varname.equals(MMSDPM.getName())){
            for(int i=0;i<4;i++){
                dataArray.setInt(dataIndex.set(0,i),gvarBlock.getPitchMisalign().getMagOfMonomialSinusoid()[i]);
            }
        }else if(varname.equals(PAMSPM.getName())){
            for(int i=0;i<4;i++){
                dataArray.setInt(dataIndex.set(0,i),gvarBlock.getPitchMisalign().getMonoSinuPhaseAngle()[i]);
            }
        }else if(varname.equals(AEMZPM.getName())){
            for(int i=0;i<4;i++){
                dataArray.setInt(dataIndex.set(0,i),gvarBlock.getPitchMisalign().getAngFromEpoch()[i]);
            }
        }            
  
//  ########## End   of GVAR Nav Variables ######################################## ////        
        

    
// #################  GVAR prefix vars  ###################     
        
//block0 vars

        else if(varname.equals(SPC_ID.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                dataArray.setByte(dataIndex.set(z),alp.getB0().getSpcid());
            }           
        }else if(varname.equals(SPSID_DOC.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                dataArray.setByte(dataIndex.set(z),alp.getB0().getSpsid());
            }           
        }
        /**
        else if(varname.equals(ISCAN.getName())){
            dataIndex = dataArray.getIndex();
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                dataArray.set(dataIndex.set(z),alp.getB0().get);
            }           
        }else if(varname.equals(IDSUB.getName())){
            dataIndex = dataArray.getIndex();
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                dataArray.set(dataIndex.set(z),alp.getB0().);
            }           
        }
        */
        else if(varname.equals(TCURR.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                String val = alp.getB0().getTcurr();
                for(int i=0;i<section.getRange(1).length();i++){
                    dataArray.setChar(dataIndex.set(z,i),( section.getRange(1).first() + i < val.length() ) ? val.charAt(section.getRange(1).first() + i) : ' ');
                }
            }           
        }
        else if(varname.equals(TCHED.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                String val = alp.getB0().getTched();
                for(int i=0;i<section.getRange(1).length();i++){
                    dataArray.setChar(dataIndex.set(z,i),( section.getRange(1).first() + i < val.length() ) ? val.charAt(section.getRange(1).first() + i) : ' ');
                }
            }           
        }else if(varname.equals(TCTRL.getName())){
            dataIndex = dataArray.getIndex();
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                String val = alp.getB0().getTctrl();
                for(int i=0;i<section.getRange(1).length();i++){
                    dataArray.setChar(dataIndex.set(z,i),( section.getRange(1).first() + i < val.length() ) ? val.charAt(section.getRange(1).first() + i) : ' ');
                }
            }           
        }else if(varname.equals( TLHED.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                String val = alp.getB0().getTlhed();
                for(int i=0;i<section.getRange(1).length();i++){
                    dataArray.setChar(dataIndex.set(z,i),( section.getRange(1).first() + i < val.length() ) ? val.charAt(section.getRange(1).first() + i) : ' ');
                }
            }           
        }else if(varname.equals(TLTRL.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                String val = alp.getB0().getTltrl();
                for(int i=0;i<section.getRange(1).length();i++){
                    dataArray.setChar(dataIndex.set(z,i),( section.getRange(1).first() + i < val.length() ) ? val.charAt(section.getRange(1).first() + i) : ' ');
                }
            }           
        }else if(varname.equals(TIPFS.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                String val = alp.getB0().getTipfs();
                for(int i=0;i<section.getRange(1).length();i++){
                    dataArray.setChar(dataIndex.set(z,i),( section.getRange(1).first() + i < val.length() ) ? val.charAt(section.getRange(1).first() + i) : ' ');
                }
            }           
        }
        else if(varname.equals(TINFS.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                String val = alp.getB0().getTinfs();
                for(int i=0;i<section.getRange(1).length();i++){
                    dataArray.setChar(dataIndex.set(z,i),( section.getRange(1).first() + i < val.length() ) ? val.charAt(section.getRange(1).first() + i) : ' ');
                }
            }           
        }else if(varname.equals(TISPC.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                String val = alp.getB0().getTispc();
                for(int i=0;i<section.getRange(1).length();i++){
                    dataArray.setChar(dataIndex.set(z,i),( section.getRange(1).first() + i < val.length() ) ? val.charAt(section.getRange(1).first() + i) : ' ');
                }
            }           
        }else if(varname.equals(TIECL.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                String val = alp.getB0().getTiecl();
                for(int i=0;i<section.getRange(1).length();i++){
                    dataArray.setChar(dataIndex.set(z,i),( section.getRange(1).first() + i < val.length() ) ? val.charAt(section.getRange(1).first() + i) : ' ');
                }
            }           
        }else if(varname.equals(TIBBC.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                String val = alp.getB0().getTibbc();
                for(int i=0;i<section.getRange(1).length();i++){
                    dataArray.setChar(dataIndex.set(z,i),( section.getRange(1).first() + i < val.length() ) ? val.charAt(section.getRange(1).first() + i) : ' ');
                }
            }           
        }else if(varname.equals(TISTR.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                String val = alp.getB0().getTistr();
                for(int i=0;i<section.getRange(1).length();i++){
                    dataArray.setChar(dataIndex.set(z,i),( section.getRange(1).first() + i < val.length() ) ? val.charAt(section.getRange(1).first() + i) : ' ');
                }
            }           
        }else if(varname.equals(TIRAN.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                String val = alp.getB0().getTiran();
                for(int i=0;i<section.getRange(1).length();i++){
                    dataArray.setChar(dataIndex.set(z,i),( section.getRange(1).first() + i < val.length() ) ? val.charAt(section.getRange(1).first() + i) : ' ');
                }
            }           
        }else if(varname.equals(TIIRT.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                String val = alp.getB0().getTiirt();
                for(int i=0;i<section.getRange(1).length();i++){
                    dataArray.setChar(dataIndex.set(z,i),( section.getRange(1).first() + i < val.length() ) ? val.charAt(section.getRange(1).first() + i) : ' ');
                }
            }           
        }else if(varname.equals(TIVIT.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                String val = alp.getB0().getTivit();
                for(int i=0;i<section.getRange(1).length();i++){
                    dataArray.setChar(dataIndex.set(z,i),( section.getRange(1).first() + i < val.length() ) ? val.charAt(section.getRange(1).first() + i) : ' ');
                }
            }           
        }else if(varname.equals(TCLMT.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                String val = alp.getB0().getTclmt();
                for(int i=0;i<section.getRange(1).length();i++){
                    dataArray.setChar(dataIndex.set(z,i),( section.getRange(1).first() + i < val.length() ) ? val.charAt(section.getRange(1).first() + i) : ' ');
                }
            }           
        }else if(varname.equals(TIONA.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                String val = alp.getB0().getTiona();
                for(int i=0;i<section.getRange(1).length();i++){
                    dataArray.setChar(dataIndex.set(z,i),( section.getRange(1).first() + i < val.length() ) ? val.charAt(section.getRange(1).first() + i) : ' ');
                }
            }           
        }               
//block header
        else if(varname.equals(BLOCKID.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                dataArray.setShort(dataIndex.set(z),alp.getBh().getBlockId());
            }
        }else if(varname.equals(WORD_SIZE.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                dataArray.setByte(dataIndex.set(z),alp.getBh().getWordSize());
            }
        }else if(varname.equals(WORD_COUNT.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                dataArray.setInt(dataIndex.set(z),alp.getBh().getWordCount());
            }
        }else if(varname.equals(PRODUCT_ID.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                dataArray.setShort(dataIndex.set(z),alp.getBh().getProductId());
            }
        }else if(varname.equals(REPEAT_FLAG.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                dataArray.setByte(dataIndex.set(z),alp.getBh().getRepeatFlag());
            }
        }else if(varname.equals(GVAR_VERSION_NUM.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                dataArray.setByte(dataIndex.set(z),alp.getBh().getGvarVersionNum());
            }
        }else if(varname.equals(DATA_VALID.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                dataArray.setByte(dataIndex.set(z),alp.getBh().getDataValid());
            }
        }else if(varname.equals(ASCII_BINARY.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                dataArray.setByte(dataIndex.set(z),alp.getBh().getAsciiBinary());
            }
        }else if(varname.equals(SPS_ID.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                dataArray.setByte(dataIndex.set(z),alp.getBh().getSpsId());
            }
        }else if(varname.equals(RANGE_WORD.getName())){
            Range xrange = section.getRange(1);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                String s = alp.getBh().getRangeWord();
                for(int i=0;i<section.getRange(1).length();i++){
                    dataArray.setChar(dataIndex.set(z,i), ( section.getRange(1).first() + i < s.length() ) ? s.charAt(section.getRange(1).first() + i) : ' ');
                }
            }
        }else if(varname.equals(BLOCK_COUNT.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                dataArray.setInt(dataIndex.set(z),alp.getBh().getBlockCount());
            }
        }else if(varname.equals(SPS_TIME.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                String val = alp.getBh().getSpsTime();
                for(int i=0;i<section.getRange(1).length();i++){
                    dataArray.setFloat(dataIndex.set(z,i),( section.getRange(1).first() + i < val.length() ) ? val.charAt(section.getRange(1).first() + i) : ' ');
                }
            }
        }else if(varname.equals(ERROR_CHECK.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                dataArray.setShort(dataIndex.set(z),alp.getBh().getErrorCheck());
            }
        }else if(varname.equals(SPC_ID_LD.getName())){   //line documentation   
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                dataArray.setByte(dataIndex.set(z),(byte)alp.getLdoc().getSpcid());
            }
        }else if(varname.equals(SPS_ID_LD.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                dataArray.setByte(dataIndex.set(z),(byte)alp.getLdoc().getSpsid());
            }
        }else if(varname.equals(LSIDE.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                dataArray.setShort(dataIndex.set(z),(byte)alp.getLdoc().getLside());
            }
        }else if(varname.equals(LIDET.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                dataArray.setByte(dataIndex.set(z),(byte)alp.getLdoc().getLidet());
            }
        }else if(varname.equals(LICHA.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                dataArray.setByte(dataIndex.set(z),(byte)alp.getLdoc().getChan());
            }
        } else if(varname.equals(RISCT.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                dataArray.setInt(dataIndex.set(z),(byte)alp.getLdoc().getRisct());
            }
        } else if(varname.equals(L1SCAN.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                dataArray.setShort(dataIndex.set(z),(short)alp.getLdoc().getL1scan());
            }
        } else if(varname.equals(L2SCAN.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                dataArray.setShort(dataIndex.set(z),(short)alp.getLdoc().getL2scan());
            }
        } else if(varname.equals(LPIXLS.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                dataArray.setInt(dataIndex.set(z),alp.getLdoc().getLpixls());
            }
        } else if(varname.equals( LZCOR.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                dataArray.setShort(dataIndex.set(z),(short)alp.getLdoc().getLzcor());
            }
        } else if(varname.equals(LWORDS.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                dataArray.setInt(dataIndex.set(z),alp.getLdoc().getLwords());
            }
        } else if(varname.equals(LLAG.getName())){
            Range xrange = section.getRange(0);
            AreaLinePrefix alp = null;
            for(int z=0;z<xrange.length();z++){
                alp = af.readScanlinePrefix(xrange.first() + z);
                dataArray.setByte(dataIndex.set(z),(byte)alp.getLdoc().getLlag());
            }
        }

        return dataArray;
    }

    
    public void close() throws IOException {
        varInfoManager.clearAll();
        af.close();        
    }
    
}
