package gov.noaa.ncdc.wsdemo;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class CDOExtract {

    public final static String REST_URL = "http://www7.ncdc.noaa.gov:8080/rest/services/";
    public final static String CDO_WFS_URL = "http://gis.ncdc.noaa.gov/wfsconnector/com.esri.wfs.Esrimap/"+
        "WFS_lcd?request=getfeature&service=WFS&version=1.0.0&typename=LocalClimatologicalData-0";
    public final static String CRN_WFS_URL = "http://gis.ncdc.noaa.gov/wfsconnector/com.esri.wfs.Esrimap/"+
    "WFS_crn?request=getfeature&service=WFS&version=1.0.0&typename=USClimateReferenceNetwork-0";
    
    public final static String CRN_DATA_URL = "http://www.ncdc.noaa.gov/crn/xmldata2";
    
//    private Map<String, String> crnIdMap = WSDemoUtils.getIdMap();
    
    public String getDailyData(TimeSeries timeSeries, String stationId, String elementType, String begYYYYMMDD, String endYYYYMMDD) 
        throws IOException, ParserConfigurationException, SAXException, XPathExpressionException, NumberFormatException, ParseException {
        
        StringBuffer sb = new StringBuffer();
        
//        URL url = new URL(REST_URL+"values/isd/stationid/999999"+stationId+"/"+elementType+"/"+begYYYYMMDD+"0000/"+endYYYYMMDD+"2359?output=xml&token=gabFDFgbhGmmlJnHmG");
        URL url = new URL(REST_URL+"values/isd/stationid/999999"+stationId+"/"+elementType+"/"+begYYYYMMDD+"0000/"+endYYYYMMDD+"2359?output=xml&token=cggjaajEFGfemIfJlGl");
        System.out.println("... cdo getting: "+url);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");        

        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document doc = builder.parse(url.toString());

        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        XPathExpression elemFld1Expr = xpath.compile("//value[rptType='FM-15']/elemfld1/text()");
        XPathExpression elemFld2Expr = xpath.compile("//value[rptType='FM-15']/elemfld2/text()");
        XPathExpression gmtDateExpr = xpath.compile("//value[rptType='FM-15']/gmtDate/text()");
        XPathExpression gmtTimeExpr = xpath.compile("//value[rptType='FM-15']/gmtTime/text()");

        Object elemFld1Result = elemFld1Expr.evaluate(doc, XPathConstants.NODESET);
        Object elemFld2Result = elemFld2Expr.evaluate(doc, XPathConstants.NODESET);
        Object gmtDateResult = gmtDateExpr.evaluate(doc, XPathConstants.NODESET);
        Object gmtTimeResult = gmtTimeExpr.evaluate(doc, XPathConstants.NODESET);
        
        NodeList elemFld1ResultNodes = (NodeList) elemFld1Result;
        NodeList elemFld2ResultNodes = (NodeList) elemFld2Result;
        NodeList gmtDateResultNodes = (NodeList) gmtDateResult;
        NodeList gmtTimeResultNodes = (NodeList) gmtTimeResult;
        
        String curGmtDate = null;
        String curGmtTime = null;
        String curElemFld1 = null;
        String curElemFld2 = null;
        for (int i = 0; i < elemFld2ResultNodes.getLength(); i++) {
            curGmtDate = gmtDateResultNodes.item(i).getNodeValue();
            curGmtTime = gmtTimeResultNodes.item(i).getNodeValue();
            curElemFld1 = elemFld1ResultNodes.item(i).getNodeValue();
            curElemFld2 = elemFld2ResultNodes.item(i).getNodeValue();
            
            double value = 0;
            if(elementType.equalsIgnoreCase("TMP")) {
                value = Double.parseDouble(curElemFld1.substring(0, curElemFld1.length()-1) + "." + curElemFld1.substring(curElemFld1.length()-1));
            } else if(elementType.equalsIgnoreCase("AA1")) {
                if(curElemFld1.equalsIgnoreCase("01")) {
                    value = Double.parseDouble(curElemFld2);
                }
            }
            
            if (timeSeries != null) {
                timeSeries.add(new FixedMillisecond(dateFormat.parse(curGmtDate+curGmtTime)), value);
            }
        }


        return sb.toString();
    }
    
    
    
    public ArrayList<Station> getSitesFromWFS() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        
        ArrayList<Station> list = new ArrayList<Station>();
        
        
        URL url = new URL(CDO_WFS_URL);
        System.out.println("... ncdc cdo wfs getting: "+url);

        
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document doc = builder.parse(url.toString());

        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        xpath.setNamespaceContext(new WFSNamespaceContext());

        XPathExpression cityExpr = xpath.compile("//gml:featureMember/ncdc:LocalClimatologicalData-0/ncdc:sde_user.lcd.city/text()");
        XPathExpression stateExpr = xpath.compile("//gml:featureMember/ncdc:LocalClimatologicalData-0/ncdc:sde_user.lcd.state/text()");
        XPathExpression stationExpr = xpath.compile("//gml:featureMember/ncdc:LocalClimatologicalData-0/ncdc:sde_user.lcd.location/text()");
        XPathExpression siteidExpr = xpath.compile("//gml:featureMember/ncdc:LocalClimatologicalData-0/ncdc:sde_user.lcd.wban/text()");
        XPathExpression latitudeExpr = xpath.compile("//gml:featureMember/ncdc:LocalClimatologicalData-0/ncdc:sde_user.lcd.latitude/text()");
        XPathExpression longitudeExpr = xpath.compile("//gml:featureMember/ncdc:LocalClimatologicalData-0/ncdc:sde_user.lcd.longitude/text()");
        
        Object cityResult = cityExpr.evaluate(doc, XPathConstants.NODESET);
        Object stateResult = stateExpr.evaluate(doc, XPathConstants.NODESET);
        Object stationResult = stationExpr.evaluate(doc, XPathConstants.NODESET);
        Object siteidResult = siteidExpr.evaluate(doc, XPathConstants.NODESET);
        Object latitudeResult = latitudeExpr.evaluate(doc, XPathConstants.NODESET);
        Object longitudeResult = longitudeExpr.evaluate(doc, XPathConstants.NODESET);
        
        NodeList cityResultNodes = (NodeList) cityResult;
        NodeList stateResultNodes = (NodeList) stateResult;
        NodeList stationResultNodes = (NodeList) stationResult;
        NodeList siteidResultNodes = (NodeList) siteidResult;
        NodeList latitudeResultNodes = (NodeList) latitudeResult;
        NodeList longitudeResultNodes = (NodeList) longitudeResult;
        
        String curCity = null;
        String curState = null;
        String curStation = null;
        String curSiteId = null;
        String curLat = null;
        String curLon = null;
        
        System.out.println("sites: "+stationResultNodes.getLength());
        System.out.println("states: "+stateResultNodes.getLength());
        System.out.println("ids: "+siteidResultNodes.getLength());
        System.out.println("lats: "+latitudeResultNodes.getLength());
        System.out.println("lons: "+longitudeResultNodes.getLength());
        
        for (int i = 0; i < stationResultNodes.getLength(); i++) {
            try {
                curStation = stationResultNodes.item(i).getNodeValue();
                curCity = (i < cityResultNodes.getLength()) ? cityResultNodes.item(i).getNodeValue() : "";
                curState = (i < stateResultNodes.getLength()) ? stateResultNodes.item(i).getNodeValue() : "";
                curSiteId = siteidResultNodes.item(i).getNodeValue();
                curLat = latitudeResultNodes.item(i).getNodeValue();
                curLon = longitudeResultNodes.item(i).getNodeValue();
            
                Station s = new Station(curStation, curSiteId, curState, curCity, 
                        Double.parseDouble(curLat), Double.parseDouble(curLon));
                
                list.add(s);
            } catch(Exception e) {
                System.out.println("Invalid station: " + curStation);
            }
        }

        return list;
    }
    
    
    
    
    
    
    
    
    
    
//    public void getCrnData(TimeSeries tmpSeries, TimeSeries pcpSeries, String goesIdNumber, String begYYYYMMDD, String endYYYYMMDD) 
//    throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, ParseException {
//        
//        String crnId = crnIdMap.get(goesIdNumber.trim());
//        
//        URL url = new URL(CRN_DATA_URL+"?data="+crnId+":T5_12,P_OFFICIAL&first="+begYYYYMMDD+"00&last="+endYYYYMMDD+"23");
//        
//        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
//        domFactory.setNamespaceAware(true); // never forget this!
//        DocumentBuilder builder = domFactory.newDocumentBuilder();
//        Document doc = builder.parse(url.toString());
//
//        XPathFactory factory = XPathFactory.newInstance();
//        XPath xpath = factory.newXPath();
//        XPathExpression dataExpr = xpath.compile("//data/text()");
//        
//        
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhh");
//        Date startDate = dateFormat.parse(begYYYYMMDD+"00"); 
//        
//        Object dataResult = dataExpr.evaluate(doc, XPathConstants.NODESET);
//        NodeList dataResultNodes = (NodeList) dataResult;
//        for (int i = 0; i < dataResultNodes.getLength(); i++) {
//            String dataString = dataResultNodes.item(i).getNodeValue();
//            
//            String[] rows = dataString.split("\n");
//            for (int n=0; n<rows.length; n++) {
//                
//                if (rows[n].trim().length() > 0) {
//                    System.out.println(rows[n]);
//                
//                    String[] cols = rows[n].split(",");
//                
//                    tmpSeries.add(new FixedMillisecond(startDate.getTime()+(Integer.parseInt(cols[0])*300000)), Double.parseDouble(cols[2]));
//                    pcpSeries.add(new FixedMillisecond(startDate.getTime()+(Integer.parseInt(cols[0])*300000)), Double.parseDouble(cols[4]));
//                }
//            }
//        }
//
//        
//        
//    }
    
    
    
    
    
    
    

    
    public ArrayList<String> getCrnSitesFromWFS() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        
        ArrayList<String> list = new ArrayList<String>();
        
        
        URL url = new URL(CRN_WFS_URL);
        System.out.println("... ncdc wfs crn getting: "+url);

        
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document doc = builder.parse(url.toString());

        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        xpath.setNamespaceContext(new WFSNamespaceContext());
        
        XPathExpression stateExpr = xpath.compile("//gml:featureMember/ncdc:USClimateReferenceNetwork-0/ncdc:sde_user.crn.state/text()");
        XPathExpression stationExpr = xpath.compile("//gml:featureMember/ncdc:USClimateReferenceNetwork-0/ncdc:sde_user.crn.station_name/text()");
        XPathExpression siteidExpr = xpath.compile("//gml:featureMember/ncdc:USClimateReferenceNetwork-0/ncdc:sde_user.crn.siteid/text()");
        XPathExpression latitudeExpr = xpath.compile("//gml:featureMember/ncdc:USClimateReferenceNetwork-0/ncdc:sde_user.crn.latitude/text()");
        XPathExpression longitudeExpr = xpath.compile("//gml:featureMember/ncdc:USClimateReferenceNetwork-0/ncdc:sde_user.crn.longitude/text()");

        Object stateResult = stateExpr.evaluate(doc, XPathConstants.NODESET);
        Object stationResult = stationExpr.evaluate(doc, XPathConstants.NODESET);
        Object siteidResult = siteidExpr.evaluate(doc, XPathConstants.NODESET);
        Object latResult = latitudeExpr.evaluate(doc, XPathConstants.NODESET);
        Object lonResult = longitudeExpr.evaluate(doc, XPathConstants.NODESET);
        
        NodeList stateResultNodes = (NodeList) stateResult;
        NodeList stationResultNodes = (NodeList) stationResult;
        NodeList siteidResultNodes = (NodeList) siteidResult;
        NodeList latResultNodes = (NodeList) latResult;
        NodeList lonResultNodes = (NodeList) lonResult;
        for (int i = 0; i < stationResultNodes.getLength(); i++) {
            String dataString = "CRN: "+stateResultNodes.item(i).getNodeValue()+" "+stationResultNodes.item(i).getNodeValue()+"~"+
                siteidResultNodes.item(i).getNodeValue()+"~"+
                latResultNodes.item(i).getNodeValue()+"~"+lonResultNodes.item(i).getNodeValue(); 
            
            list.add(dataString);
            
            System.out.println("WFS 'featureMember' parsed with XPATH: "+dataString);
        }
        
//        System.out.println(doc.toString());


        return list;
    }
    
    
    
    
    class WFSNamespaceContext implements NamespaceContext {

        public String getNamespaceURI(String prefix) {
            if (prefix.equals("gml")) {
                return "http://www.opengis.net/gml";
            }
            else if (prefix.equals("ncdc")) {
                return "http://gis.ncdc.noaa.gov";
            }
            else {
                return null;
            }
        }

        public String getPrefix(String namespaceURI) {
            if (namespaceURI.equals("http://www.opengis.net/gml")) {
                return "gml";
            }
            else if (namespaceURI.equals("http://gis.ncdc.noaa.gov")) {
                return "ncdc";
            }
            return null;
        }

        public Iterator getPrefixes(String namespaceURI) {
            ArrayList<String> list = new ArrayList<String>();
            if (namespaceURI.equals("http://www.opengis.net/gml")) {
                list.add("gml");
            }
            else if (namespaceURI.equals("http://gis.ncdc.noaa.gov")) {
                list.add("ncdc");
            }
            return list.iterator();
        }
        
    }


    
    
    public class Station {
        private String location;
        private String wban;
        private String city;
        private String state;
        private double lat;
        private double lon;
        
        public Station(String location, String wban, String city,
                String state, double lat, double lon) {
            
            this.location = location;
            this.wban = wban;
            this.city = city;
            this.state = state;
            this.lat = lat;
            this.lon = lon;
        }
        
        public String getLocation() {
            return location;
        }
        public String getWban() {
            return wban;
        }
        public String getCity() {
            return city;
        }
        public String getState() {
            return state;
        }
        public double getLat() {
            return lat;
        }
        public double getLon() {
            return lon;
        }
        
        public String toString() {
            return location+" , "+wban+" , "+city+" , "+state+" , "+lat+" , "+lon;
        }
    }
}

