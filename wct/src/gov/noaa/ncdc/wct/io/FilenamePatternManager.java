package gov.noaa.ncdc.wct.io;

import gov.noaa.ncdc.wct.WCTUtils;

import java.io.File;
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

public class FilenamePatternManager {

	private ArrayList<FilenamePattern> patternList = new ArrayList<FilenamePattern>();

	public void addPatterns(URL url) throws SAXException, IOException, ParserConfigurationException, NumberFormatException, XPathExpressionException {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        
//        URL url = this.getClass().getResource("/config/filenamePatterns.xml");		
        Document doc = builder.parse(url.toString());

        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();

//		int count = Integer.parseInt(WCTUtils.getXPathValue(doc, xpath, "//filename/count()"));
		int count = WCTUtils.getXPathValues(doc, xpath, "//filename/dataType/text()").size();
		
		for (int n=1; n<count+1; n++) {
//			System.out.println(n+": "+getXPathValue(doc, xpath, "//filename["+n+"]/filePattern/text()"));
		
			FilenamePattern p = new FilenamePattern();
			p.setDataType(WCTUtils.getXPathValue(doc, xpath, "//filename["+n+"]/dataType/text()"));
			p.setDescription(WCTUtils.getXPathValue(doc, xpath, "//filename["+n+"]/description/text()"));
			p.setFilePattern(WCTUtils.getXPathValue(doc, xpath, "//filename["+n+"]/filePattern/text()"));
			p.setFileTimestampLocation(WCTUtils.getXPathValue(doc, xpath, "//filename["+n+"]/fileTimestampLocation/text()"));
			p.setSourceID(WCTUtils.getXPathValue(doc, xpath, "//filename["+n+"]/sourceID/text()"));
			p.setProductCode(WCTUtils.getXPathValue(doc, xpath, "//filename["+n+"]/productCode/text()"));
            p.setProductLookupTable(WCTUtils.getXPathValue(doc, xpath, "//filename["+n+"]/productLookupTable/text()"));
            p.setIdAliasLookupTable(WCTUtils.getXPathValue(doc, xpath, "//filename["+n+"]/idAliasLookupTable/text()"));
			
			patternList.add(p);
		}
		
//		System.out.println(Arrays.deepToString(
//				getXPathValues(doc, xpath, "//filename/filePattern/text()").toArray()
//			));
	}


	public ArrayList<FilenamePattern> getPatternList() {
		return patternList;
	}
	
	public FilenamePattern matchFilename(URL url) {
		String urlString = url.toString();
		return matchFilename(urlString.substring(urlString.lastIndexOf('/')+1));
	}
	/**
	 * Matches the file object's file name (without path) to the list of loaded patterns.  
	 * The first matching pattern is returned.
	 * @param file
	 * @return null if no matching pattern is found
	 */
	public FilenamePattern matchFilename(File file) {
		return matchFilename(file.getName());
	}
	
	/**
	 * Matches the filename string (without path) to the list of loaded patterns.  
	 * The first matching pattern is returned.
	 * @param filename
	 * @return null if no matching pattern is found
	 */
	public FilenamePattern matchFilename(String filename) {
				
		for (FilenamePattern p : patternList) {
			
//			System.out.println("Checking "+filename+" against: "+p);
			if (filename.matches(p.getFilePattern())) {
//				System.out.println("FOUND A MATCH!  "+filename+" matches "+p);
//			    System.out.println("FOUND MATCH FOR: "+filename);
				return p;
			}
		}
		return null;
	}
	
	
	
	
	
}
