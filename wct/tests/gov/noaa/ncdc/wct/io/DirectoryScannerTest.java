package gov.noaa.ncdc.wct.io;

import gov.noaa.ncdc.common.FTPList;
import gov.noaa.ncdc.wct.io.DirectoryScanner.ListResults;

import java.net.URL;

import org.junit.Test;

public class DirectoryScannerTest {

    // Test out some listing
    DirectoryScanner dirScanner = new DirectoryScanner();
    ListResults listResults;

    
    
    @Test
    public void testRawFTP() throws Exception {
        FileInfo[] files = new FTPList().getDirectoryList(
                "ftp.class.ncdc.noaa.gov", 
                "ftp", 
                "ndit", 
                "bin", 
                "/A7908933");
        
        for (FileInfo file : files) {
            System.out.println("CLASS FTP FILE: "+file);
        }
    }

    
    @Test
    public void testCLASS() throws Exception {
        
        System.out.println("dirScanner.listCLASSDirectory(A7908933);");
        listResults = dirScanner.listCLASSDirectory("A7908933", DirectoryScanner.CLASS_SERVER_PRIMARY);
        System.out.println(listResults.toString(3));
        
    }

    @Test
    public void testLocal() {
        try {
            System.out.println("dirScanner.listURL(new URL(\"file:/H:/ViewerData/HAS999900001/\"));");
            listResults = dirScanner.listURL(new URL("file:/H:/ViewerData/HAS999900001/"));
            System.out.println(listResults.toString(3));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    


    @Test
    public void testCLASSHTTP() {

        try {
            System.out.println("dirScanner.listURL(new URL(\"http://www.class.ncdc.noaa.gov/download/A7908933\"));");
            listResults = dirScanner.listURL(new URL("http://www.class.ncdc.noaa.gov/download/A7908933"));
            System.out.println(listResults.toString(3));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testCustomHTTP() {

        try {
            System.out.println("dirScanner.listURL(new URL(\"http://www1.ncdc.noaa.gov/pub/data/nexradviewer/katrina/data/klix-level2\"));");
            listResults = dirScanner.listURL(new URL("http://www1.ncdc.noaa.gov/pub/data/nexradviewer/katrina/data/klix-level2"));
            System.out.println(listResults.toString(3));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
//
//    @Test
//    public void testCustomFTP() {
//        try {
//            System.out.println("dirScanner.listURL(new URL(\"ftp://ftp.ncdc.noaa.gov/pub/data/nexradviewer/katrina/data/klix-level2\"));");
//            listResults = dirScanner.listURL(new URL("ftp://ftp.ncdc.noaa.gov/pub/data/nexradviewer/katrina/data/klix-level2"));
//            System.out.println(listResults.toString(3));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void testLocal() {
//        try {
//            System.out.println("dirScanner.listURL(new URL(\"file:/H:/ViewerData/HAS999900001/\"));");
//            listResults = dirScanner.listURL(new URL("file:/H:/ViewerData/HAS999900001/"));
//            System.out.println(listResults.toString(3));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void testFTP() {
//        try {
//            System.out.println("dirScanner.listFtpDirectory(\"ftp.ncdc.noaa.gov\", \"anonymous\", \"wrt@noaa.gov\", \"pub/data/nexradviewer/katrina/data/klix-level2\");");
//            listResults = dirScanner.listFtpDirectory("ftp.ncdc.noaa.gov", "anonymous", "wrt@noaa.gov", "pub/data/nexradviewer/katrina/data/klix-level2");
//            System.out.println(listResults.toString(3));
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
//            listResults = dirScanner.listHASDirectory("HAS000654354");
//            System.out.println(listResults.toString(3));
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
//            listResults = dirScanner.listHASDirectory("000654354");
//            System.out.println(listResults.toString(3));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void testHAS3() {
//        try {
//            System.out.println("dirScanner.listHASDirectory(\"654354\");");
//            listResults = dirScanner.listHASDirectory("654354");
//            System.out.println(listResults.toString(3));
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
//            listResults = dirScanner.listLocalDirectory(new File("H:\\ViewerData\\HAS999900001"));
//            System.out.println(listResults.toString(3));
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
//            listResults = dirScanner.listTHREDDSDirectory(new URL("http://motherlode.ucar.edu:8080/thredds/catalog/nexrad/level3/N1R/OHX/20060804/catalog.xml"));
//            System.out.println(listResults.toString(3));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }



}
