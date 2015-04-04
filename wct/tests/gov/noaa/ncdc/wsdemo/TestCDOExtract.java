package gov.noaa.ncdc.wsdemo;

import gov.noaa.ncdc.wsdemo.CDOExtract.Station;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.xml.sax.SAXException;

public class TestCDOExtract {
    
    
    @Test
    public void testCDOExtract() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
        
        CDOExtract cdo = new CDOExtract();
        ArrayList<Station> sites = cdo.getSitesFromWFS();
        
        for (int n=0; n<sites.size(); n++) {
            System.out.println(sites.get(n));
        }
        
    }

}
