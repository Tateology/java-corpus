package gov.noaa.ncdc.wms;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.xml.sax.SAXException;

public class TestWMSConfigManager {

    @Test
    public void testListNames() throws Exception {
        
        WMSConfigManager config = WMSConfigManager.getInstance();
//        config.addConfig(this.getClass().getResource("/config/wmsInfo.xml"));
        
        System.out.println(config.getWmsNames().toString());
        
    }
    
    @Test
    public void testListAll() throws NumberFormatException, XPathExpressionException, 
        SAXException, IOException, ParserConfigurationException {
        
        WMSConfigManager config = WMSConfigManager.getInstance();
        config.addConfig(this.getClass().getResource("/config/wmsInfo.xml"));
        
        ArrayList<WMSRequestInfo> wmsList = config.getWmsList();
        for (WMSRequestInfo w : wmsList) {
            System.out.println(w);
        }
    }
    
    @Test
    public void testListSingle() throws Exception {
        
        WMSConfigManager config = WMSConfigManager.getInstance();
        config.addConfig(this.getClass().getResource("/config/wmsInfo.xml"));
        
        WMSRequestInfo w = config.getWmsRequestInfo("Shaded Relief");
        System.out.println(w);
    }
}
