package gov.noaa.ncdc.wct.io;

import gov.noaa.ncdc.common.FTPList;

import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

public class WCTDirectoryScannerTest {

    // Test out some listing
    WCTDirectoryScanner dirScanner;
    ScanResults[] scanResults;

    
    @Before
    public void init() throws NumberFormatException, XPathExpressionException, SAXException, IOException, ParserConfigurationException {
        dirScanner = new WCTDirectoryScanner();
    }
    
    @Test
    public void testNWSGateway() {

        try {
            System.out.println("dirScanner.scanURL(new URL(\"ftp://tgftp.nws.noaa.gov/SL.us008001/DF.of/DC.radar/DS.34cfc/SI.ksrx\"));");
            scanResults = dirScanner.scanUrlDirectory(new URL("ftp://tgftp.nws.noaa.gov/SL.us008001/DF.of/DC.radar/DS.34cfc/SI.ksrx"), false);
            printResults(scanResults, 3);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    @Test
    public void testRawFTP() throws Exception {
        FileInfo[] files = new FTPList().getDirectoryList(
                "ftp.class.ncdc.noaa.gov", 
                "ftp", 
                "wct", 
                "bin", 
                "/A7908933");
        
        for (FileInfo file : files) {
            System.out.println("CLASS FTP FILE: "+file);
        }
        System.out.println("\n\n\n");
    }

    
    @Test
    public void testCLASS() throws Exception {
        
        System.out.println("dirScanner.scanCLASSDirectory(107618393);");
        scanResults = dirScanner.scanCLASSDirectory("107618393");
        printResults(scanResults, 3);
        
    }

    @Test
    public void testLocal() {
        try {
            System.out.println("dirScanner.scanURL(new URL(\"file:/H:/ViewerData/HAS999900001/\"));");
            scanResults = dirScanner.scanUrlDirectory((new URL("file:/H:/ViewerData/HAS999900001/")), false);
            printResults(scanResults, 3);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    


    @Test
    public void testCLASSHTTP() {

        try {
            System.out.println("dirScanner.scanURL(new URL(\"http://www.class.ncdc.noaa.gov/download/A7908933\"));");
            scanResults = dirScanner.scanUrlDirectory(new URL("http://www.class.ncdc.noaa.gov/download/A7908933"), false);
            printResults(scanResults, 3);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testCustomHTTP() {

        try {
//            System.out.println("dirScanner.scanURL(new URL(\"http://www1.ncdc.noaa.gov/pub/data/nexradviewer/katrina/data/klix-level2\"));");
//            scanResults = dirScanner.scanUrlDirectory(new URL("http://www1.ncdc.noaa.gov/pub/data/nexradviewer/katrina/data/klix-level2"), false);
            System.out.println("dirScanner.scanURL(new URL(\"http://thredds.ucar.edu/cgi-bin/ldm/genweb?native/radar/level2/KABR/20110121\"));");
            scanResults = dirScanner.scanUrlDirectory(new URL("http://thredds.ucar.edu/cgi-bin/ldm/genweb?native/radar/level2/KABR/20110121"), false);
            printResults(scanResults, 3);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    
    public void printResults(ScanResults[] scanResults, int numResults) {
        int cnt = 0;
        for ( ScanResults result : scanResults) {
        	System.out.println("SCANNED URL:  "+result.getUrl());
            System.out.println("SCANNED INFO: "+result);
            cnt++;
            
            if (cnt == numResults) {
                break;
            }
        }
    }
    
//
//    @Test
//    public void testCustomFTP() {
//        try {
//            System.out.println("dirScanner.scanURL(new URL(\"ftp://ftp.ncdc.noaa.gov/pub/data/nexradviewer/katrina/data/klix-level2\"));");
//            scanResults = dirScanner.scanURL(new URL("ftp://ftp.ncdc.noaa.gov/pub/data/nexradviewer/katrina/data/klix-level2"));
//            System.out.println(scanResults.toString(3));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void testLocal() {
//        try {
//            System.out.println("dirScanner.listURL(new URL(\"file:/H:/ViewerData/HAS999900001/\"));");
//            scanResults = dirScanner.listURL(new URL("file:/H:/ViewerData/HAS999900001/"));
//            System.out.println(scanResults.toString(3));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void testFTP() {
//        try {
//            System.out.println("dirScanner.listFtpDirectory(\"ftp.ncdc.noaa.gov\", \"anonymous\", \"wrt@noaa.gov\", \"pub/data/nexradviewer/katrina/data/klix-level2\");");
//            scanResults = dirScanner.listFtpDirectory("ftp.ncdc.noaa.gov", "anonymous", "wrt@noaa.gov", "pub/data/nexradviewer/katrina/data/klix-level2");
//            System.out.println(scanResults.toString(3));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void testHAS() {
//
//        try {
//            System.out.println("dirScanner.listHASDirectory(\"HAS000654354\");");
//            scanResults = dirScanner.listHASDirectory("HAS000654354");
//            System.out.println(scanResults.toString(3));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
//
//
//    @Test
//    public void testHAS2() {
//        try {
//            System.out.println("dirScanner.listHASDirectory(\"000654354\");");
//            scanResults = dirScanner.listHASDirectory("000654354");
//            System.out.println(scanResults.toString(3));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void testHAS3() {
//        try {
//            System.out.println("dirScanner.listHASDirectory(\"654354\");");
//            scanResults = dirScanner.listHASDirectory("654354");
//            System.out.println(scanResults.toString(3));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void testLocal2() {
//
//        try {
//            System.out.println("dirScanner.listLocalDirectory(new File(\"H:\\ViewerData\\HAS999900001\"));");
//            scanResults = dirScanner.listLocalDirectory(new File("H:\\ViewerData\\HAS999900001"));
//            System.out.println(scanResults.toString(3));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void testTHREDDS() {
//
//        try {
//            System.out.println("dirScanner.listTHREDDSDirectory(http://motherlode.ucar.edu:8080/thredds/catalog/nexrad/level3/N1R/OHX/20060804/catalog.xml\"));");
//            scanResults = dirScanner.listTHREDDSDirectory(new URL("http://motherlode.ucar.edu:8080/thredds/catalog/nexrad/level3/N1R/OHX/20060804/catalog.xml"));
//            System.out.println(scanResults.toString(3));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }



}
