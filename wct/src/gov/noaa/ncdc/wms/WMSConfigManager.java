package gov.noaa.ncdc.wms;

import gov.noaa.ncdc.wct.ResourceUtils;
import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.WCTUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class WMSConfigManager {

    private static WMSConfigManager config = null;
    private Document doc;
    private XPath xpath;
    private ArrayList<WMSRequestInfo> wmsList = new ArrayList<WMSRequestInfo>();

    
    private WMSConfigManager() throws ParserConfigurationException, SAXException, IOException {
    }
    
    public static WMSConfigManager getInstance() throws ParserConfigurationException, SAXException, IOException, NumberFormatException, XPathExpressionException {
        if (config == null) {
            config = new WMSConfigManager();
            try {
                config.addConfig(ResourceUtils.getInstance().getJarResource(
                        new URL(WCTConstants.CONFIG_JAR_URL), ResourceUtils.CONFIG_CACHE_DIR, "/config/wmsInfo.xml", null));
            } catch (Exception e) {
            	e.printStackTrace();
            }
        }
        return config;
    }

    
    
    public void addConfig(URL url) throws SAXException, IOException, ParserConfigurationException, NumberFormatException, XPathExpressionException {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = domFactory.newDocumentBuilder();

        System.out.println("WMSConfigManager::: LOADING: "+url);
        this.doc = builder.parse(url.toString());

        XPathFactory factory = XPathFactory.newInstance();
        this.xpath = factory.newXPath();

        //          int count = Integer.parseInt(WCTUtils.getXPathValue(doc, xpath, "//filename/count()"));
        int count = WCTUtils.getXPathValues(doc, xpath, "//wms/imageType/text()").size();

        for (int n=1; n<count+1; n++) {
            //              System.out.println(n+": "+getXPathValue(doc, xpath, "//filename["+n+"]/filePattern/text()"));

            WMSRequestInfo w = new WMSRequestInfo();
            w.setName(WCTUtils.getXPathValue(doc, xpath, "//wms["+n+"]/@name"));
            w.setIndex(WCTUtils.getXPathValue(doc, xpath, "//wms["+n+"]/@index"));
            w.setCapabilitiesURL(WCTUtils.getXPathValue(doc, xpath, "//wms["+n+"]/capabilitiesURL/@href"));
            w.setDefaultLayers(WCTUtils.getXPathValue(doc, xpath, "//wms["+n+"]/defaultLayers/text()"));
            w.setImageType(WCTUtils.getXPathValue(doc, xpath, "//wms["+n+"]/imageType/text()"));
            w.setTransparent(WCTUtils.getXPathValue(doc, xpath, "//wms["+n+"]/transparent/text()"));
            w.setVersion(WCTUtils.getXPathValue(doc, xpath, "//wms["+n+"]/version/text()"));
            w.setInfo(WCTUtils.getXPathValue(doc, xpath, "//wms["+n+"]/info/text()"));

            wmsList.add(w);
        }
    }


    public ArrayList<WMSRequestInfo> getWmsList() {
        return wmsList;
    }

    /**
     * Get a list of available datasets.
     * @return
     * @throws Exception
     */
    public ArrayList<String> getWmsNames() throws Exception {
        return WCTUtils.getXPathValues(doc, xpath, "//wms//@name");
    }

    
    public WMSRequestInfo getWmsRequestInfo(String name) throws Exception {
        WMSRequestInfo w = new WMSRequestInfo();
        w.setName(WCTUtils.getXPathValue(doc, xpath, "//wms[@name='"+name+"']/@name"));
        w.setIndex(WCTUtils.getXPathValue(doc, xpath, "//wms[@name='"+name+"']/@index"));
        w.setCapabilitiesURL(WCTUtils.getXPathValue(doc, xpath, "//wms[@name='"+name+"']/capabilitiesURL/@href"));
        w.setDefaultLayers(WCTUtils.getXPathValue(doc, xpath, "//wms[@name='"+name+"']/defaultLayers/text()").replaceAll(" ", ""));
        w.setImageType(WCTUtils.getXPathValue(doc, xpath, "//wms[@name='"+name+"']/imageType/text()"));
        w.setTransparent(WCTUtils.getXPathValue(doc, xpath, "//wms[@name='"+name+"']/transparency/text()"));
        w.setVersion(WCTUtils.getXPathValue(doc, xpath, "//wms[@name='"+name+"']/version/text()"));
        w.setInfo(WCTUtils.getXPathValue(doc, xpath, "//wms[@name='"+name+"']/info/text()"));

        return w;
    }

}
