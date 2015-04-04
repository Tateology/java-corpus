package gov.noaa.ncdc.wct.io;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.xml.sax.SAXException;

public class TestFileScanner {

    


    
    @Test
    public void testReadPatternFile() throws XPathExpressionException, NumberFormatException, SAXException, IOException, ParserConfigurationException {
        FilenamePatternManager fp = new FilenamePatternManager();
        URL url = this.getClass().getResource("/config/filenamePatterns.xml");      
        fp.addPatterns(url);
    }


    @Test
    public void testPatternMatchL2() throws NumberFormatException, XPathExpressionException, SAXException, IOException, ParserConfigurationException {
        FilenamePatternManager fp = new FilenamePatternManager();
        URL url = this.getClass().getResource("/config/filenamePatterns.xml");      
        fp.addPatterns(url);
        
        String filename = "6500KHTX20020424_164107.Z";
        FilenamePattern p = fp.matchFilename(filename);
        System.out.println(p);
    }
    
    
    @Test
    public void testPatternMatchL2Tar() throws NumberFormatException, XPathExpressionException, SAXException, IOException, ParserConfigurationException {
        FilenamePatternManager fp = new FilenamePatternManager();
        URL url = this.getClass().getResource("/config/filenamePatterns.xml");      
        fp.addPatterns(url);
        
        String filename = "KMKX2012010421.tar";
        FilenamePattern p = fp.matchFilename(filename);
        System.out.println(p);
    }
    
    
    @Test
    public void testPatternMatchL3() throws NumberFormatException, XPathExpressionException, SAXException, IOException, ParserConfigurationException, ParseException {
        FilenamePatternManager fp = new FilenamePatternManager();
        URL url = this.getClass().getResource("/config/filenamePatterns.xml");      
        fp.addPatterns(url);
        
        File file = new File("E:\\devel\\wct\\testdata\\7000KCLE_SDUS61_NCZCLE_200211102304");
        FilenamePattern p = fp.matchFilename(file);
        System.out.println(p);
        

        String timestamp = FilenamePatternParser.getTimestamp(p, file.getName(), "yyyyMMddHHmmss");
        System.out.println("TIMESTAMP: "+timestamp);
    }
    
    @Test
    public void testTimestampExtract() throws NumberFormatException, XPathExpressionException, SAXException, IOException, ParserConfigurationException, ParseException {
        FilenamePatternManager fp = new FilenamePatternManager();
        URL url = this.getClass().getResource("/config/filenamePatterns.xml");      
        fp.addPatterns(url);
        
        String filename = "6500KHTX20020424_164107.Z";
        FilenamePattern p = fp.matchFilename(filename);
        System.out.println(p);
        
        String timestamp = FilenamePatternParser.getTimestamp(p, filename, "yyyyMMddHHmmss");
        System.out.println("TIMESTAMP: "+timestamp);
    }
    
    @Test
    public void testSourceIDExtract() throws NumberFormatException, XPathExpressionException, SAXException, IOException, ParserConfigurationException, ParseException {
        FilenamePatternManager fp = new FilenamePatternManager();
        URL url = this.getClass().getResource("/config/filenamePatterns.xml");      
        fp.addPatterns(url);
        
        String filename = "7000KCLE_SDUS61_NCZCLE_200211102304";
        FilenamePattern p = fp.matchFilename(filename);
        System.out.println(p);
        
        String id = FilenamePatternParser.getSourceID(p, filename);
        System.out.println("SOURCE ID: "+id);
    }
    


    @Test
    public void testProductCodeL2Extract() throws NumberFormatException, XPathExpressionException, SAXException, IOException, ParserConfigurationException, ParseException {
        FilenamePatternManager fp = new FilenamePatternManager();
        URL url = this.getClass().getResource("/config/filenamePatterns.xml");      
        fp.addPatterns(url);
        
        String filename = "6500KHTX20020424_164107.Z";
        FilenamePattern p = fp.matchFilename(filename);
        System.out.println(p);
        
        String code = FilenamePatternParser.getProductCode(p, filename);
        System.out.println("PRODUCT CODE: "+code);
        
        
    }
    

    @Test
    public void testProductCodeL3Extract() throws NumberFormatException, XPathExpressionException, SAXException, IOException, ParserConfigurationException, ParseException {
        FilenamePatternManager fp = new FilenamePatternManager();
        URL url = this.getClass().getResource("/config/filenamePatterns.xml");      
        fp.addPatterns(url);
        
        String filename = "7000KCLE_SDUS61_NCZCLE_200211102304";
        FilenamePattern p = fp.matchFilename(filename);
        System.out.println(p);
        
        String code = FilenamePatternParser.getProductCode(p, filename);
        System.out.println("PRODUCT CODE: "+code);
    }
    
    
    @Test
    public void testGetSupportedDataTypes() throws ClassNotFoundException, NumberFormatException, XPathExpressionException, SAXException, IOException, ParserConfigurationException {
        
        FilenamePatternManager fp = new FilenamePatternManager();
        URL url = this.getClass().getResource("/config/filenamePatterns.xml");      
        fp.addPatterns(url);
        
        String filename = "7000KCLE_SDUS61_NCZCLE_200211102304";
        FilenamePattern p = fp.matchFilename(filename);
        System.out.println(p);
        
        FilenamePatternParser.getDataType(p);
    }
    
    
    @Test
    public void testPatternMatchL3NwsGateway() throws NumberFormatException, XPathExpressionException, SAXException, IOException, ParserConfigurationException, ParseException {
        FilenamePatternManager fp = new FilenamePatternManager();
        URL url = this.getClass().getResource("/config/filenamePatterns.xml");      
        fp.addPatterns(url);
        
        FilenamePattern p = fp.matchFilename("sn.last");
        System.out.println(p);
      
    }
    
    @Test
    public void testProductLookupTable() throws NumberFormatException, XPathExpressionException, SAXException, IOException, ParserConfigurationException {
        FilenamePatternManager fp = new FilenamePatternManager();
        URL url = this.getClass().getResource("/config/filenamePatterns.xml");      
        fp.addPatterns(url);
        
        String filename = "7000KCLE_SDUS61_NCZCLE_200211102304";
        FilenamePattern p = fp.matchFilename(filename);
        System.out.println("PRODUCT LOOKUP TABLE: "+p.getProductLookupTable()+" , isNull: "+(p.getProductLookupTable() == null));
        
        String prodDesc = FilenamePatternParser.getProductDesc(p, filename);
        
        System.out.println(prodDesc);
        
    }
    
    @Test
    public void testXMRG() throws NumberFormatException, XPathExpressionException, SAXException, IOException, ParserConfigurationException {
//        xmrg-ggggg-0818200216z
        
        FilenamePatternManager fp = new FilenamePatternManager();
        URL url = this.getClass().getResource("/config/filenamePatterns.xml");      
        fp.addPatterns(url);
        
//        String filename = "xmrg-ggggg-0818200216z";
        String filename = "xmrg_08132004_05z_SE";
        FilenamePattern p = fp.matchFilename(filename);
        System.out.println(p);

    }
}
