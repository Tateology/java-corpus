package steve.test;

import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;


public class XmlParsingTest {

	
	public static void main(String[] args) {
		
		try {
			
			URL url = new URL("http://www.ndbc.noaa.gov/activestations.xml");
	        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
	        domFactory.setNamespaceAware(true); // never forget this!
	        DocumentBuilder builder = domFactory.newDocumentBuilder();

	        System.out.println("::: LOADING: "+url);
	        Document doc = builder.parse(url.toString());
	        
	        XPathFactory factory = XPathFactory.newInstance();
	        XPath xpath = factory.newXPath();
	        
//	        <station id="13001" lat="12" lon="-23" 
//	        	name="NE Extension" owner="Prediction and Research Moored Array in the Atlantic" 
//	        	pgm="International Partners" type="buoy" met="y" currents="n" waterquality="n" dart="n"/>
	        

	        
	        ArrayList<String> ids = getXPathValues(doc, xpath, "//station/@id");
	        
	        
	        
			
	        for (int n=1; n<ids.size()+1; n++) {
	        	String id = ids.get(n-1);
	        	String lat = getXPathValue(doc, xpath, "//station["+n+"]/@lat");
	        	String lon = getXPathValue(doc, xpath, "//station["+n+"]/@lon");
	        	String name = getXPathValue(doc, xpath, "//station["+n+"]/@name");
	        	String owner = getXPathValue(doc, xpath, "//station["+n+"]/@owner");
	        	String pgm = getXPathValue(doc, xpath, "//station["+n+"]/@pgm");
	        	String type = getXPathValue(doc, xpath, "//station["+n+"]/@type");
	        	String met = getXPathValue(doc, xpath, "//station["+n+"]/@met");
	        	String currents = getXPathValue(doc, xpath, "//station["+n+"]/@currents");
	        	String waterquality = getXPathValue(doc, xpath, "//station["+n+"]/@waterquality");
	        	String dart = getXPathValue(doc, xpath, "//station["+n+"]/@dart");
	        	
	        	
	        	System.out.println(id+" ~ "+lat+" ~ "+lon+" ~ "+name+" ~ "+owner+
	        			" ~ "+pgm+" ~ "+type+" ~ "+met+" ~ "+currents+" ~ "+waterquality+" ~ "+dart);
	        	
	        }
	        
	        System.out.println(ids.size()+" records processed");
	        
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	
	
	
	
	

	public static String getXPathValue(Document doc, XPath xpath, String xpathString) throws XPathExpressionException {
		XPathExpression expr = xpath.compile(xpathString);
		NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

		if (nodes.getLength() > 0) {
			return nodes.item(0).getNodeValue().trim();
		}
		else {
			return null;
		}
	}

	public static ArrayList<String> getXPathValues(Document doc, XPath xpath, String xpathString) throws XPathExpressionException {
		XPathExpression expr = xpath.compile(xpathString);         
		NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		ArrayList<String> values = new ArrayList<String>(nodes.getLength());

		for (int i=0; i<nodes.getLength(); i++) {           
			if (nodes.item(i).getNodeValue() != null) {
				values.add(nodes.item(i).getNodeValue().trim());
			}
			else {
				values.add("null");
			}
		}
		return values;
	}


	
}
