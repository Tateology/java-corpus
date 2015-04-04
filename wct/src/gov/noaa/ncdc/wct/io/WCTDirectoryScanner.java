package gov.noaa.ncdc.wct.io;

import ftp.FtpException;
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.event.GeneralProgressEvent;
import gov.noaa.ncdc.wct.event.GeneralProgressListener;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import com.jcraft.jsch.JSchException;



/**
 *  Scans a directory (HAS, local disk, remote URL, THREDDS) and returns either
 *  an array of FileScanner.ScanResult objects
 *
 * @author     steve.ansari
 * @created    September 23, 2004
 */
public class WCTDirectoryScanner extends DirectoryScanner {

    private FileScanner fileScanner;
    private Vector<GeneralProgressListener> listeners = new Vector<GeneralProgressListener>();
    private GeneralProgressEvent event = new GeneralProgressEvent(this);

    
    public void addProgressListener(GeneralProgressListener listener) {
        listeners.add(listener);
    }
    
    public void removeProgressListener(GeneralProgressListener listener) {
        listeners.remove(listener);
    }
    
    public void clearProgressListeners() {
        listeners.clear();
    }
    


    /**
     * List files - defaults to sort by scanResults.getFilename()
     * @param sourceType
     * @param location
     * @return
     * @throws FtpException
     * @throws IOException
     * @throws DecodeException
     * @throws JSchException
     * @throws SQLException 
     * @throws ParseException 
     * @throws ParserConfigurationException 
     * @throws SAXException 
     * @throws XPathExpressionException 
     * @throws NumberFormatException 
     */
    public ScanResults[] listFiles(String sourceType, String location)
        throws FtpException, IOException, DecodeException, JSchException, SQLException, ParseException, NumberFormatException, XPathExpressionException, SAXException, ParserConfigurationException {
        
        return listFiles(sourceType, location, new ScanResultsComparator(ScanResultsComparator.CompareBy.FILE_NAME), false);
    }
    

    
    public ScanResults[] listFiles(String sourceType, String location, ScanResultsComparator comparator, boolean includeUnknownTypes)
        throws FtpException, IOException, DecodeException, JSchException, SQLException, ParseException, NumberFormatException, XPathExpressionException, SAXException, ParserConfigurationException {
        
        
        
        
        
        ScanResults[] scanResults = null;

        // Scan for valid files
        if (sourceType.equals(WCTDataSourceDB.NCDC_HAS_FTP)) {
            scanResults = scanHASDirectory(location);
        }
        else if (sourceType.equals(WCTDataSourceDB.CLASS_ORDER)) {
            scanResults = scanCLASSDirectory(location);
        }
        else if (sourceType.equals(WCTDataSourceDB.LOCAL_DISK)) {
            scanResults = scanLocalDirectory(new File(location), includeUnknownTypes);
        }
        else if (sourceType.equals(WCTDataSourceDB.URL_DIRECTORY)) {
            scanResults = scanUrlDirectory(new URL(location), includeUnknownTypes);
        }
        else if (sourceType.equals(WCTDataSourceDB.THREDDS)) {
            scanResults = scanTHREDDSDirectory(new URL(location));
        }
        else if (sourceType.equals(WCTDataSourceDB.SSH_DIRECTORY)) {
            String sshLocation = location;
            // sansari@mesohigh:/home/sansari/data
            String user = sshLocation.split("@")[0];
            String server = sshLocation.split("@")[1].split(":/")[0];
            String directory = sshLocation.split("@")[1].split(":/")[1];
            scanResults = scanSSHDirectory(server, user, "", directory);
        }

//        System.out.println(Arrays.deepToString(scanResults));
        Arrays.sort(scanResults, comparator);
//        System.out.println(Arrays.deepToString(scanResults));
        return scanResults;


    }






    public WCTDirectoryScanner() throws NumberFormatException, XPathExpressionException, SAXException, IOException, ParserConfigurationException {
        fileScanner = new FileScanner(false);
    }


    /**
     * Only works with remote servers that return a scan of directory contents as HTML.
     * May not work with some server settings.  FTP URLs will only work with anonymous
     * FTP.
     *
     * @param  directoryURL               Description of the Parameter
     * @return                            Description of the Return Value
     * @exception  MalformedURLException  Description of the Exception
     * @exception  FtpException           Description of the Exception
     * @exception  IOException            Description of the Exception
     * @throws SQLException 
     * @throws ParseException 
     * @throws ParserConfigurationException 
     * @throws SAXException 
     * @throws XPathExpressionException 
     * @throws NumberFormatException 
     */
    public ScanResults[] scanUrlDirectory(URL directoryURL, boolean includeUnknownTypes)
    throws MalformedURLException, FtpException, IOException, DecodeException, SQLException, ParseException, NumberFormatException, XPathExpressionException, SAXException, ParserConfigurationException {

        event.setProgress(0.0);
        event.setStatus("Listing Directory...");
        for (int n=0; n<listeners.size(); n++) {
            listeners.get(n).started(event);
        }

        
        
        String dirString = directoryURL.toString();
        if (dirString.startsWith("http://weather.noaa.gov/pub/SL.us008001/DF.of/DC.radar/")) {
//            dirString = dirString.concat("?C=M;O=D");
            dirString = dirString.replaceAll("http://weather.noaa.gov/pub/SL.us008001/DF.of/DC.radar", 
                    "ftp://tgftp.nws.noaa.gov/SL.us008001/DF.of/DC.radar");
            
            directoryURL = new URL(dirString);
        }

                
        
        ListResults listResults = listURL(directoryURL);
        
        if (directoryURL.getHost().equals("tgftp.nws.noaa.gov")) {
            return scanNwsListResults(listResults);
        }
        else {
            return scanListResults(listResults, includeUnknownTypes);
        }
    }

    /**
     *  Lists directory contents of FTP directory
     *
     * @param  host       Description of the Parameter
     * @param  user       Description of the Parameter
     * @param  pass       Description of the Parameter
     * @param  directory  Description of the Parameter
     * @return            Description of the Return Value
     * @throws SQLException 
     * @throws ParseException 
     * @throws ParserConfigurationException 
     * @throws SAXException 
     * @throws XPathExpressionException 
     * @throws NumberFormatException 
     */
    public ScanResults[] scanFtpDirectory(String host, String user, String pass, String directory) 
    throws FtpException, IOException, DecodeException, SQLException, ParseException, NumberFormatException, XPathExpressionException, SAXException, ParserConfigurationException {
        
        event.setProgress(0.0);
        event.setStatus("Listing Directory...");
        for (int n=0; n<listeners.size(); n++) {
            listeners.get(n).started(event);
        }

        ListResults listResults = listFtpDirectory(host, user, pass, directory);
        
        
        // 1. Check for NWS server for this special case
        if (host.equals("tgftp.nws.noaa.gov") && user.equals("anonymous") && pass.equals("wct.ncdc.at.noaa.gov")) {
            return scanNwsListResults(listResults);
        }
        else {
            return scanListResults(listResults);
        }
           
        

//        ScanResults[] scanResults = scanListResults(listResults);
//        return scanResults;

    }

    
    
    
    /**
     *  Lists contents of local directory
     *
     * @param  dir  
     * @return      
     * @throws SQLException 
     * @throws ParserConfigurationException 
     * @throws SAXException 
     * @throws ParseException 
     * @throws XPathExpressionException 
     * @throws NumberFormatException 
     */
    public ScanResults[] scanLocalDirectory(File dir)
    throws IOException, DecodeException, SQLException, NumberFormatException, XPathExpressionException, ParseException, SAXException, ParserConfigurationException {

        return scanLocalDirectory(dir, false);
    }
    
    
    /**
     *  Lists contents of local directory
     *
     * @param  dir  
     * @param  includeUnknownTypes
     * @return      
     * @throws SQLException 
     * @throws ParserConfigurationException 
     * @throws SAXException 
     * @throws ParseException 
     * @throws XPathExpressionException 
     * @throws NumberFormatException 
     */
    public ScanResults[] scanLocalDirectory(File dir, boolean includeUnknownTypes)
    throws IOException, DecodeException, SQLException, NumberFormatException, XPathExpressionException, ParseException, SAXException, ParserConfigurationException {

        event.setProgress(0.0);
        event.setStatus("Listing Directory...");
        for (int n=0; n<listeners.size(); n++) {
            listeners.get(n).started(event);
        }

        ListResults listResults = listLocalDirectory(dir);
        ScanResults[] scanResults = scanListResults(listResults, includeUnknownTypes);

        return scanResults;

    }

    /**
     *  Lists contents of HAS directory on NCDC FTP server
     *
     * @param  hasNumber  Description of the Parameter
     * @return            Description of the Return Value
     * @throws SQLException 
     * @throws ParserConfigurationException 
     * @throws SAXException 
     * @throws ParseException 
     * @throws XPathExpressionException 
     * @throws NumberFormatException 
     */
    public ScanResults[] scanHASDirectory(String hasJobString) 
    throws FtpException, IOException, DecodeException, SQLException, NumberFormatException, XPathExpressionException, ParseException, SAXException, ParserConfigurationException {

        event.setProgress(0.0);
        event.setStatus("Listing Directory...");
        for (int n=0; n<listeners.size(); n++) {
            listeners.get(n).started(event);
        }

//        ListResults listResults = listHASDirectory(hasJobString);
//        ScanResults[] scanResults = scanListResults(listResults, false);


        
        ScanResults[] scanResults = null;
        try {
        	ListResults listResults = listHASDirectory(hasJobString, NCDC_HAS_HTTP_SERVER, NCDC_HAS_HTTP_DIRECTORY);
        	scanResults = scanListResults(listResults, false);
        	if (scanResults.length == 0) {
        		scanResults = scanListResults(listResults, true);
        	}
            
        } catch (Exception e) {
        	scanResults = new ScanResults[0];
        }
        
        
        // TODO - set this backup to something else
        if (scanResults.length == 0) {
            System.out.println("Trying HAS backup site at...  "+NCDC_HAS_HTTP_SERVER_BACKUP);
        	ListResults listResults = listHASDirectory(hasJobString, NCDC_HAS_HTTP_SERVER_BACKUP, NCDC_HAS_HTTP_DIRECTORY_BACKUP);
            scanResults = scanListResults(listResults, false);
        	if (scanResults.length == 0) {
        		scanResults = scanListResults(listResults, true);
        	}

            System.out.println("found "+scanResults.length);
        }
        
        
        
        return scanResults;

    }

    /**
     *  Lists contents of THREDDS catalog given URL to catalog.xml file
     *  Currently only supports HTTPServer ServiceType
     *
     * @param  catalogURL  URL to catalog.xml file
     * @return            Description of the Return Value
     * @throws SQLException 
     * @throws ParserConfigurationException 
     * @throws SAXException 
     * @throws ParseException 
     * @throws XPathExpressionException 
     * @throws NumberFormatException 
     */
    public ScanResults[] scanTHREDDSDirectory(URL catalogURL) 
    throws IOException, DecodeException, SQLException, NumberFormatException, XPathExpressionException, ParseException, SAXException, ParserConfigurationException {

        event.setProgress(0.0);
        event.setStatus("Listing Directory...");
        for (int n=0; n<listeners.size(); n++) {
            listeners.get(n).started(event);
        }

        ListResults listResults = listTHREDDSDirectory(catalogURL);
        ScanResults[] scanResults = scanListResults(listResults, false);

        return scanResults;

    }



    /**
     *  Lists contents of CLASS order directory on CLASS HTTP/FTP server
     *
     * @param  hasNumber  Description of the Parameter
     * @return            Description of the Return Value
     * @throws SQLException 
     * @throws ParserConfigurationException 
     * @throws SAXException 
     * @throws ParseException 
     * @throws XPathExpressionException 
     * @throws NumberFormatException 
     */
    public ScanResults[] scanCLASSDirectory(String classJobString) 
    throws FtpException, IOException, DecodeException, SQLException, NumberFormatException, XPathExpressionException, ParseException, SAXException, ParserConfigurationException {

        event.setProgress(0.0);
        event.setStatus("Listing Directory...");
        for (int n=0; n<listeners.size(); n++) {
            listeners.get(n).started(event);
        }

        ScanResults[] scanResults = null;
        try {
        	ListResults listResults = listCLASSDirectory(classJobString, CLASS_SERVER_PRIMARY);
        	scanResults = scanListResults(listResults, false);
        	if (scanResults.length == 0) {
        		scanResults = scanListResults(listResults, true);
        	}
            
        } catch (Exception e) {
        	scanResults = new ScanResults[0];
        }
        
        
        if (scanResults.length == 0) {
            System.out.print("Trying CLASS backup site at NGDC:  ");
            ListResults listResults = listCLASSDirectory(classJobString, CLASS_SERVER_BACKUP);    
            scanResults = scanListResults(listResults, false);
        	if (scanResults.length == 0) {
        		scanResults = scanListResults(listResults, true);
        	}

            System.out.println("found "+scanResults.length);
        }
        

        return scanResults;

    }



    /**
     *  Lists directory contents of SSH directory
     *
     * @param  server       Description of the Parameter
     * @param  user       Description of the Parameter
     * @param  pass       Description of the Parameter
     * @param  directory  Description of the Parameter
     * @return            Description of the Return Value
     * @throws SQLException 
     * @throws ParserConfigurationException 
     * @throws SAXException 
     * @throws ParseException 
     * @throws XPathExpressionException 
     * @throws NumberFormatException 
     */
    public ScanResults[] scanSSHDirectory(String server, String user, String pass, String directory) 
    throws FtpException, IOException, DecodeException, JSchException, SQLException, NumberFormatException, XPathExpressionException, ParseException, SAXException, ParserConfigurationException {
        
        event.setProgress(0.0);
        event.setStatus("Listing Directory...");
        for (int n=0; n<listeners.size(); n++) {
            listeners.get(n).started(event);
        }

        ListResults listResults = listSSHDirectory(server, user, pass, directory);
        ScanResults[] scanResults = scanListResults(listResults);

        return scanResults;

    }







    /**
     * Scans a ListResult object representing directory contents and returns an array of
     * ScanResults objects.  These objects represent the URLs that do not have
     * an FileScanner.UNKNOWN filetype.
     *
     * @param  listResults  
     * @return           
     * @throws SQLException 
     * @throws ParseException 
     * @throws ParserConfigurationException 
     * @throws IOException 
     * @throws SAXException 
     * @throws XPathExpressionException 
     * @throws NumberFormatException 
     */
    public ScanResults[] scanListResults(ListResults listResults) 
        throws DecodeException, SQLException, ParseException, NumberFormatException, XPathExpressionException, SAXException, IOException, ParserConfigurationException {

        return scanListResults(listResults, true);
    }
    
    /**
     * Scans a ListResult object representing directory contents and returns an array of
     * ScanResults objects.  These objects represent the URLs that do not have
     * an FileScanner.UNKNOWN filetype (if includeUnknownTypes is set to false).
     *
     * @param  listResults  
     * @param  includeUnknownTypes  
     * @return           
     * @throws SQLException 
     * @throws ParseException 
     * @throws ParserConfigurationException 
     * @throws IOException 
     * @throws SAXException 
     * @throws XPathExpressionException 
     * @throws NumberFormatException 
     */
    public ScanResults[] scanListResults(ListResults listResults, boolean includeUnknownTypes) 
        throws DecodeException, SQLException, ParseException, NumberFormatException, XPathExpressionException, SAXException, IOException, ParserConfigurationException {


        
        event.setProgress(0.0);
        event.setStatus("Scanning Results...");
        for (int n=0; n<listeners.size(); n++) {
            listeners.get(n).started(event);
        }

        
        
        URL[] urlList = listResults.getUrlList();
        ArrayList<ScanResults> scanResultList = new ArrayList<ScanResults>();


        
        scanResultList.ensureCapacity(urlList.length);
        
        for (int n=0; n<urlList.length; n++) {
            try {
                fileScanner.scanURL(urlList[n]);
                ScanResults scanResults = fileScanner.getLastScanResult();          
                if (includeUnknownTypes || scanResults.getDataType() != SupportedDataType.UNKNOWN) {
                    scanResultList.add(scanResults);
                }
            } catch (Exception e) {                
            }
            
            event.setProgress(n/(double)urlList.length);
            event.setStatus("Scanning "+n+"/"+urlList.length+" Results...");
            for (int i=0; i<listeners.size(); i++) {
                listeners.get(i).progress(event);
            }

        }

        // Remove extra unneeded space
        scanResultList.trimToSize();

        ScanResults[] scanResults = scanResultList.toArray(new ScanResults[scanResultList.size()]);      

        event.setProgress(0.0);
        event.setStatus("");
        for (int n=0; n<listeners.size(); n++) {
            listeners.get(n).ended(event);
        }

                
        return scanResults;      
    }

    
    public ScanResults[] scanNwsListResults(ListResults listResults) throws ParseException {
        
        event.setProgress(0.0);
        event.setStatus("Scanning Results...");
        for (int n=0; n<listeners.size(); n++) {
            listeners.get(n).started(event);
        }

        
        
        
        SimpleDateFormat ftpInfoDF1 = new SimpleDateFormat("MMM dd HH:mm");        
        SimpleDateFormat ftpInfoDF2 = new SimpleDateFormat("MMM dd yyyy");        
        SimpleDateFormat outDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        
        URL[] urlList = listResults.getUrlList();
        FileInfo[] infoList = listResults.getFileInfoList();
        ArrayList<ScanResults> scanResultsList = new ArrayList<ScanResults>();
        for (int n=0; n<urlList.length; n++) {
            
//            System.out.println(infoList[n].getName()+" -- "+infoList[n].getTimestamp());
//            Need to handle two types of date formats 
//            sn.0179 -- Jun 16 08:08
//            sn.0180 -- Dec 11 2009
            
            ScanResults scan = new ScanResults();
            scan.setUrl(urlList[n]);
            scan.setFileName(infoList[n].getName());
            scan.setDataType(SupportedDataType.NEXRAD_LEVEL3_NWS);
            if (infoList[n].getName().trim().equalsIgnoreCase("sn.last")) {
            	
                if (infoList[n].getTimestamp().length() == 12) {
                    scan.setTimestamp(outDF.format(new Date(ftpInfoDF1.parse(infoList[n].getTimestamp()).getTime()+1000)));
                }
                else if (infoList[n].getTimestamp().length() == 11) {
                    scan.setTimestamp(outDF.format(new Date(ftpInfoDF2.parse(infoList[n].getTimestamp()).getTime()+1000)));
                }                
            	
            	scan.setDisplayName("NWS Gateway: "+infoList[n].getPath().substring(28)+" (latest file)");
                scan.setLongName("NWS Gateway: "+infoList[n].getPath().substring(28)+" (latest file)");
                
            }
            else {
                scan.setDisplayName("NWS Gateway: "+infoList[n].getName()+
                		"  File Timestamp: "+infoList[n].getTimestamp() + 
                		"  Size: "+FileUtils.byteCountToDisplaySize(infoList[n].getSize()));
                scan.setLongName("NWS Gateway: "+infoList[n].getName()+
                		"  File Timestamp: "+infoList[n].getTimestamp() + 
                		"  Size: "+FileUtils.byteCountToDisplaySize(infoList[n].getSize()));
                
                if (infoList[n].getTimestamp().length() == 12) {
                    scan.setTimestamp(outDF.format(ftpInfoDF1.parse(infoList[n].getTimestamp()).getTime()));
                }
                else if (infoList[n].getTimestamp().length() == 11) {
                    scan.setTimestamp(outDF.format(ftpInfoDF2.parse(infoList[n].getTimestamp()).getTime()));
                }
                else {
                    scan.setTimestamp("99/99 99:99");
                }

            }
            
            scan.setProductID(infoList[n].getName());
            
            scanResultsList.add(scan);
        }
        
        ScanResults[] scanResults = scanResultsList.toArray(new ScanResults[scanResultsList.size()]);
        
        
        
        event.setProgress(0.0);
        event.setStatus("");
        for (int n=0; n<listeners.size(); n++) {
            listeners.get(n).ended(event);
        }

        return scanResults;
    }


}

