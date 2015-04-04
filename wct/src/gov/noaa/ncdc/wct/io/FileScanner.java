package gov.noaa.ncdc.wct.io;

import gov.noaa.ncdc.wct.ResourceUtils;
import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Header;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradDatabase;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradHeader;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

/**
 *  Scans a filename and attempts to use the naming convention to extract
 *  information such as timestamp, product type, etc...  The lone exception
 *  are the NWS Gateway FTP files, which are named sn.last, sn.0001, sn.0002,
 *  etc...  These files are opened and the header scanned for the 
 *  important information.
 *
 * @author     steve.ansari
 * @created    September 23, 2004
 */
public class FileScanner {

    
    private static final Logger logger = Logger.getLogger(FileScanner.class.getName());

    
    /**
     *  Uncompressed ARCHIVE2 Format NEXRAD Level-II
     */
    public final static int RADIAL_NEXRAD_LEVEL2 = 0;

    /**
     *  Supported NEXRAD Level-III Product 
     */
    public final static int NEXRAD_LEVEL3 = 1;

    /**
     * Unsupported NEXRAD Level-III Product
     */
    public final static int NEXRAD_LEVEL3_UNSUPPORTED = 2;

    /**
     *  NEXRAD Level-III File from NWS FTP Site -- Quick Scan method unable to extract time and location info
     */
    public final static int NEXRAD_LEVEL3_NWS = 3;

    /**
     *  NEXRAD XMRG Stage-III / MPE Mosaic from NWS RFCs
     */
    public final static int NEXRAD_XMRG = 4;

    /**
     *  Partially bzipped Level-II data
     */
    public final static int RADIAL_NEXRAD_LEVEL2_AR2V0001 = 5;

    /**
     *  2D Reflectivity NMQ Grid
     */
    public final static int GRIDDED_NEXRAD_Q2_2D = 6;

    /**
     *  3D Reflectivity NMQ Grid
     */
    public final static int GRIDDED_NEXRAD_Q2_3D = 7;

    /**
     *  QPESUMS Grid
     */
    public final static int GRIDDED_NEXRAD_Q2_PCP = 8;

    /**
     *  GOES Satellite Data in AREA File Format
     */
    public final static int SATELLITE_GOES_AREAFILE = 20;

    /**
     * SIGMET Radar format
     */
    public final static int RADIAL_SIGMET = 30;

    /**
     *  Unknown file type
     */
    public final static int UNKNOWN = -10;

    final static DecimalFormat fmt0000 = new DecimalFormat("0000");

    private String saveName = "";

    private ScanResults scanResults;

    private NexradHeader header = null;

    private DecodeL3Header nwsheader = null;

    private URL url;

    // goes12.2005.241.124515.BAND_01
    private static final SimpleDateFormat goesDateFormat = new SimpleDateFormat("yyyy.DDD.HHmmss");
    private static final SimpleDateFormat wctTimestampFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
    
    private static NexradDatabase radDB;

    private FilenamePatternManager fp = new FilenamePatternManager();
    
    final static DecimalFormat fmt2 = new DecimalFormat("0.00");

    
    /**
     *Constructor for the NexradFile object
     * @throws ParserConfigurationException 
     * @throws IOException 
     * @throws SAXException 
     * @throws XPathExpressionException 
     * @throws NumberFormatException 
     */
    public FileScanner() throws NumberFormatException, XPathExpressionException, SAXException, IOException, ParserConfigurationException {
        this(false);
    }

    /**
     *Constructor for the FileScanner object
     *
     * @param  clearTmpDir  Description of the Parameter
     * @throws ParserConfigurationException 
     * @throws IOException 
     * @throws SAXException 
     * @throws XPathExpressionException 
     * @throws NumberFormatException 
     */
    public FileScanner(boolean clearTmpDir) throws NumberFormatException, XPathExpressionException, SAXException, IOException, ParserConfigurationException {

        if (clearTmpDir) {
            // Clears temp/jnx directory of Nexrad files possibly left over
            WCTTransfer.clearTempDirectory();
        }

        try {
            radDB = NexradDatabase.getSharedInstance();
        } catch (Exception e) {
            radDB = null;
        }
        
        
        
        // init filename pattern reader from xml config file
//        URL url = this.getClass().getResource("/config/filenamePatterns.xml");  
        URL url = ResourceUtils.getInstance().getJarResource(
                new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, "/config/filenamePatterns.xml", null);
        
//        System.out.println("FILENAME PATTERNS URL: "+url);
        fp.addPatterns(url);


        try {
			
        	File customFile = new File(ResourceUtils.CONFIG_CACHE_DIR + File.separator + "customFilenamePatterns.xml");
        	if (customFile.exists()) {
        		URL customUrl = customFile.toURI().toURL();        
        		System.out.println("CUSTOM FILENAME PATTERNS URL: "+customUrl);
        		fp.addPatterns(customUrl);
        	}

		} catch (Exception e) {
			e.printStackTrace();
		}

        
    }

    /**
     *  Gets the lastScanResult attribute of the FileScanner object
     *
     * @return    The lastScanResult value
     */
    public ScanResults getLastScanResult() {
        return scanResults;
    }

    /**
     *  Gets the saveName for this file <br>
     *  <b> Level-2: </b> ex) KCCX_20040617_124506<br>
     *  where KCCX = Radar ID, 20040617 = YYYYMMDD, 124506 = HHMMSS (hour, min, sec)<br><br>
     *  <b> Level-3: </b> ex) KCCX_N0R_20040617_1245 <br>
     *  where KCCX = Radar ID, N0R = Level-3 Product Code, 20040617 = YYYYMMDD, 1245 = HHMM (hour, min) <br>
     *
     * @return    The saveName
     */
    public String getSaveName() {
        return saveName;
    }

    /**
     * Use with Level-2 data to attach information about the moment and elevation_angle to
     * the output filename. <br><br>
     *  <b> Level-2: </b> ex) KCCX_20040617_124506_REF_0050 <br>
     *  where KCCX = Radar ID, 20040617 = YYYYMMDD, 124506 = HHMMSS (hour, min, sec),
     *  REF = Moment selected (REF, VEL or SPW), 0052 = elevation angle * 100 (actual = 0.52) <br><br>
     *
     * @param  variableName   Name of variable 
     * @return         The nexradFileSaveName value
     */
    public String getSaveName(String variableName) {

        String abbrev;

        if (variableName.equals("Reflectivity")) {
            abbrev = "REF";
        }
        else if (variableName.equals("RadialVelocity")) {
            abbrev = "VEL";
        }
        else if (variableName.equals("SpectrumWidth")) {
            abbrev = "SPW";
        }
        else {
            int len = variableName.length() < 3 ? variableName.length() : 3;
            abbrev = variableName.substring(0, len);
        }

        return saveName + "_" + abbrev;
//        return saveName;
    }

    /**
     *  Gets the nexradFileSaveName attribute of the FileScanner object
     *
     * @param  variableName   Name of variable 
     * @param  elevation_angle  Description of the Parameter
     * @return                  The nexradFileSaveName value
     */
    public String getSaveName(String variableName, double elevation_angle) {
        return getSaveName(variableName) + "_" + fmt0000.format(elevation_angle * 100);
//        return saveName;
    }

    /**
     *  Scans the URL to determine filetype.  If it is a NWS Gateway FTP file (sn.last, 
     *  sn.0001, sn.0002, etc...) the file is opened and the header is read.
     *
     * @param  url                        Description of the Parameter
     * @exception  DecodeException  Description of the Exception
     * @throws UnsupportedEncodingException 
     * @throws MalformedURLException 
     * @throws SQLException 
     * @throws ParseException 
     */
    public void scanURL(URL url) throws DecodeException, MalformedURLException, UnsupportedEncodingException, SQLException, ParseException {

        this.url = url;

//        String filename = url.getFile();
        String filename = URLDecoder.decode(url.getFile(), "UTF-8");
        
        filename = filename.substring(filename.lastIndexOf('/') + 1, filename.length());

        scanResults = quickScan(filename);
        scanResults.setUrl(url);
        
//        System.out.println(scanResults);
        
        if (scanResults.getDataType() == SupportedDataType.UNKNOWN) {
            return;
        }

        if (url.getProtocol().equals("file") && scanResults.getDataType() == SupportedDataType.NEXRAD_LEVEL3_NWS) {
            if (nwsheader == null) {
               nwsheader = new DecodeL3Header();
            }
            nwsheader.decodeHeader(url);
//            String elevAngle = fmt2.format((double) ((DecodeL3Header)nwsheader).getProductSpecificValue(2) / 10.0) + "° ";
            String elevAngle = fmt2.format((double) ((DecodeL3Header)nwsheader).getProductSpecificValue(2) / 10.0) + " deg ";
            if (elevAngle.startsWith("0.00")) {
                elevAngle = "";
            }
            scanResults.setDisplayName(nwsheader.getICAO() + " " + "p"+nwsheader.getProductCode() + " " +
                  elevAngle + 
                  nwsheader.getDate() + " " + nwsheader.getHourString() + ":" + nwsheader.getMinuteString() + " GMT");
            
            String desc = radDB.getRadarProductLongNamesMap().get(nwsheader.getProductCode());
            String longname = scanResults.getDisplayName();
            if (desc != null) {                
                longname = longname + " ("+desc+")";                    
            }
            scanResults.setLongName(longname);
            
            scanResults.setExtension("");
            scanResults.setProductID("");
            scanResults.setTimestamp(nwsheader.getDate() + " " + nwsheader.getHourString() + ":" + nwsheader.getMinuteString() + ":00");
            saveName = scanResults.getDisplayName().replaceAll(" ", "_").replaceAll(":", "_").replaceAll("/", "-");
            
            nwsheader.close();
         }

        saveName = scanResults.getDisplayName().replaceAll(" ", "_").replaceAll(":", "");

        //logger.fine("******************* SCAN FILE:  "+file);
        //logger.fine("******************* SCAN RETURNING:  "+fileType+ "%" +str);

    }

    /**
     * Is this a file with GZIP Compression?
     *
     * @return    Do we have gzip extension (.gz or .GZ)?
     */
    public boolean isGzipCompressed() {
        String extension = scanResults.getExtension();
        if (extension == null) {
            return false;
        }
        else {
            return (extension.equals(".gz") || extension.equals(".GZ"));
        }
    }

    /**
     * Is this a file with UNIX Z Compression?
     *
     * @return    Do we have UNIX Compression extension (.z or .Z)?
     */
    public boolean isZCompressed() {
        String extension = scanResults.getExtension();
        if (extension == null) {
            return false;
        }
        else {
            return (extension.equals(".z") || extension.equals(".Z"));
        }
    }

    /**
     *  The quick look! <br><br>
     *  Examine filename and determine the filetype based strictly on naming convention.
     *  This method retur
     *
     * @param  filename  Simple filename without any parent directories (ex: 6500KHTX20020424_164107 ) 
     * @return           ScanResults object with url=null, timestamp, filetype, extension, etc.. info
     * @throws ParseException 
     */
    public ScanResults quickScan(String filename) throws ParseException {
    	SupportedDataType dataType = SupportedDataType.UNKNOWN;
        int fileType = UNKNOWN;
        String name = "";
        String longname = "";
        String extension = "";
        String timestamp = "";
        String pid = "";
        String displayName = "";

        
        
        
        
        
        
        
        if (filename.endsWith(".Z") || filename.endsWith(".z")) {
        	extension = ".Z";
        }
        if (filename.endsWith(".gz") || filename.endsWith(".GZ")) {
        	extension = ".gz";
        }
        
        
        FilenamePattern p = fp.matchFilename(filename);     
        if (p == null) {
        	ScanResults results = new ScanResults();
        	results.setDataType(SupportedDataType.UNKNOWN);
            results.setFileName(filename);
            results.setDisplayName(filename + "  ( UNKNOWN DATA TYPE, PLEASE SELECT BELOW )");
            results.setLongName(filename + "  ( UNKNOWN DATA TYPE, PLEASE SELECT BELOW )");
        	
//        	System.out.println("DID NOT MATCH FILENAME: "+filename);
        	return results;
        }
        try {
            dataType = FilenamePatternParser.getDataType(p);
        } catch (ClassNotFoundException e) {
            dataType = SupportedDataType.UNKNOWN;
        }
        name = filename;
        timestamp = FilenamePatternParser.getTimestamp(p, filename, "yyyyMMdd HH:mm:ss");
        pid = FilenamePatternParser.getProductCode(p, filename);
        String sourceID = FilenamePatternParser.getSourceID(p, filename);
        StringBuilder lnsb = new StringBuilder();
        if (sourceID != null) { 
            lnsb.append(sourceID.toUpperCase()).append(" "); 
        }
        
        if (pid != null) { 
            lnsb.append(pid).append(" "); 
        }
        lnsb.append(timestamp);
        
        displayName = lnsb.toString();
        
        lnsb.append(" (").append(p.getDescription());
        String prodDesc = null;
        try {
            prodDesc = FilenamePatternParser.getProductDesc(p, filename);
        } catch (Exception e) {
//            System.out.println("PRODUCT LUT ERROR: "+e);
            e.printStackTrace();
        }
        
        if (prodDesc != null) {
            lnsb.append(" ").append(prodDesc);
        }
        lnsb.append(")");
        longname = lnsb.toString();

        ScanResults results = new ScanResults();
        results.setFileName(filename);
        results.setDataType(dataType);
        results.setDisplayName(displayName);
        results.setExtension(extension);
        results.setProductID(pid);
        results.setTimestamp(timestamp);
        results.setLongName(longname);
        results.setDescription(p.getDescription());
        results.setSourceID(sourceID);
        
        return results;
    }

    /**
     *  Gets the url of the last scanned item
     *
     * @return    The URL
     */
    public URL getURL() {
        return url;
    }

    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public String toString() {
        return scanResults.toString();
    }



}
