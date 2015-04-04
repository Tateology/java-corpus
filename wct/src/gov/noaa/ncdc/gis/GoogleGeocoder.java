package gov.noaa.ncdc.gis;

import gov.noaa.ncdc.wct.WCTUtils;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class GoogleGeocoder {

	public final static String GOOGLE_GEOCODE_URL_BASE = 
		"http://maps.googleapis.com/maps/api/geocode/xml?address=";

	private Document doc;
	private XPath xpath;

	public List<GoogleGeocodeResult> locationSearch(String location, Rectangle2D.Double currentExtent) 
	throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {


		if (this.xpath == null) {
			XPathFactory factory = XPathFactory.newInstance();
			this.xpath = factory.newXPath();
		}



		String boundsBiasParameter = "&bounds="+currentExtent.getMinY()+
		","+currentExtent.getMinX()+
		","+currentExtent.getMaxY()+
		","+currentExtent.getMaxX();
		URL url = new URL(GOOGLE_GEOCODE_URL_BASE+URLEncoder.encode(location, "UTF-8")+boundsBiasParameter+"&sensor=false");

		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true); // never forget this!
		DocumentBuilder builder = domFactory.newDocumentBuilder();

		System.out.println("GoogleGeocoderDialog ::: LOADING: "+url);
		this.doc = builder.parse(url.toString());


		String status = WCTUtils.getXPathValue(doc, xpath, "//GeocodeResponse/status/text()");
		System.out.println("GEOCODE STATUS: "+status);

		//	        String formattedAddress = WCTUtils.getXPathValue(doc, xpath, "//GeocodeResponse/result/formatted_address/text()");
		//	        String lat = WCTUtils.getXPathValue(doc, xpath, "//GeocodeResponse/result/geometry/location/lat/text()");
		//	        String lon = WCTUtils.getXPathValue(doc, xpath, "//GeocodeResponse/result/geometry/location/lng/text()");
		//	        System.out.println(location+": "+lat+" , "+lon);



		ArrayList<GoogleGeocodeResult> resultList = new ArrayList<GoogleGeocodeResult>();

		int count = WCTUtils.getXPathValues(doc, xpath, "//GeocodeResponse/result/geometry/location/lat/text()").size();
		for (int n=1; n<count+1; n++) {
			String formattedAddress = WCTUtils.getXPathValue(doc, xpath, 
					"//GeocodeResponse/result["+n+"]/formatted_address/text()");

			double lat = Double.parseDouble(WCTUtils.getXPathValue(doc, xpath, 
					"//GeocodeResponse/result["+n+"]/geometry/location/lat/text()"));

			double lon = Double.parseDouble(WCTUtils.getXPathValue(doc, xpath, 
					"//GeocodeResponse/result["+n+"]/geometry/location/lng/text()"));

			GoogleGeocodeResult ggr = new GoogleGeocodeResult();
			ggr.setFormattedAddress(formattedAddress);
			ggr.setLat(lat);
			ggr.setLon(lon);
			resultList.add(ggr);

		}

		return resultList;

	}


	public class GoogleGeocodeResult {
		private String formattedAddress;
		private double lat;
		private double lon;

		public void setFormattedAddress(String formattedAddress) {
			this.formattedAddress = formattedAddress;
		}
		public String getFormattedAddress() {
			return formattedAddress;
		}
		public void setLat(double lat) {
			this.lat = lat;
		}
		public double getLat() {
			return lat;
		}
		public void setLon(double lon) {
			this.lon = lon;
		}
		public double getLon() {
			return lon;
		}
	}

}
