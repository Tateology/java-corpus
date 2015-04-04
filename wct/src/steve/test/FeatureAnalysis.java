/**
 * NOAA's National Climatic Data Center
 * NOAA/NESDIS/NCDC
 * 151 Patton Ave, Asheville, NC  28801
 * 
 * THIS SOFTWARE AND ITS DOCUMENTATION ARE CONSIDERED TO BE IN THE 
 * PUBLIC DOMAIN AND THUS ARE AVAILABLE FOR UNRESTRICTED PUBLIC USE.  
 * THEY ARE FURNISHED "AS IS." THE AUTHORS, THE UNITED STATES GOVERNMENT, ITS
 * INSTRUMENTALITIES, OFFICERS, EMPLOYEES, AND AGENTS MAKE NO WARRANTY,
 * EXPRESS OR IMPLIED, AS TO THE USEFULNESS OF THE SOFTWARE AND
 * DOCUMENTATION FOR ANY PURPOSE. THEY ASSUME NO RESPONSIBILITY (1)
 * FOR THE USE OF THE SOFTWARE AND DOCUMENTATION; OR (2) TO PROVIDE
 * TECHNICAL SUPPORT TO USERS.
 */

package steve.test;

import gov.noaa.ncdc.nexrad.EasyJNX;
import gov.noaa.ncdc.wct.WCTFilter;
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.DecodeHintNotSupportedException;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Header;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradHeader;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradUtilities;
import gov.noaa.ncdc.wct.decoders.nexrad.StreamingRadialDecoder;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.geotools.cs.GeodeticCalculator;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.io.TableWriter;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class FeatureAnalysis {
   
   public static DecimalFormat fmt3 = new DecimalFormat("0");

   
   private static Vector list = new Vector();
   
   
   
   
   
   public static void processPolygonData(String icao) throws IOException, IllegalAttributeException, DecodeException, DecodeHintNotSupportedException, SQLException, NumberFormatException, XPathExpressionException, SAXException, ParserConfigurationException, ParseException {
      
         String path = "D:\\ESRI\\eastusa\\va";
         String filename = "vards";
      
         //File inputFile = new File("D:\\ESRI\\usa\\places.shp");
         File inputFile = new File(path+"\\"+filename+".shp");

         // Load some local data
         URL data_url = inputFile.toURL();
         ShapefileDataStore data_ds = new ShapefileDataStore(data_url);
         FeatureSource data_fs = data_ds.getFeatureSource(filename);
         FeatureReader dataReader = data_fs.getFeatures().reader();
         System.out.println(data_fs.getFeatures().getCount() + " INPUT FEATURES IN "+inputFile.getName());
         

         // Load some NEXRAD
         // list at http://weather.noaa.gov/pub/SL.us008001/DF.of/DC.radar/
         URL nxURL = new URL("http://weather.noaa.gov/pub/SL.us008001/DF.of/DC.radar/DS.p19r1/SI."+icao.toLowerCase()+"/sn.last");
         EasyJNX jnx = new EasyJNX();
         StreamingRadialDecoder data = jnx.getDecoder(nxURL);
         NexradHeader header = jnx.getMostRecentHeader();
         // set up filter
         WCTFilter nxfilter = new WCTFilter();
         nxfilter.setValueIndices(NexradUtilities.getCategoryIndices((DecodeL3Header)header, 35, 100, false));
         data.setDecodeHint("nxfilter", nxfilter);
         data.decodeData();         
         
         FeatureCollection nxFeatures = data.getFeatures();
         System.out.println(nxFeatures.size() + " NEXRAD FEATURES");
         

         // get general bounds
         java.awt.geom.Rectangle2D.Double bounds = header.getNexradBounds();
         GeometryFactory geoFactory = new GeometryFactory();
         Geometry nxBounds = geoFactory.toGeometry(new Envelope(bounds.x, bounds.y, 
            bounds.x + bounds.width, bounds.y + bounds.height));
         
         
         // Compare NEXRAD data to highways
         int cnt = 0;
         while (dataReader.hasNext()) {            
            Feature dataFeature = dataReader.next();
            Geometry dataGeometry = dataFeature.getDefaultGeometry();
            
            // do initial check to make sure input features are within bounds of NEXRAD site
            if (dataGeometry.within(nxBounds)) {
            
               FeatureIterator fci = nxFeatures.features();
               while (fci.hasNext()) {
                  Feature nxFeature = fci.next();
                  Geometry nxGeometry = nxFeature.getDefaultGeometry();

                  if (dataGeometry.intersects(nxGeometry)) {                  
                  
                     //Object name = dataFeature.getAttribute(1);
                     //Object state = dataFeature.getAttribute(3);
                     //Object pop = dataFeature.getAttribute(7);
                     //String item = name+"\t"+state+"\t"+pop+"\t"+nxFeature.getAttribute("value");
                        
                     Object name = dataFeature.getAttribute(2);
                     Object type = dataFeature.getAttribute(3);
                     Object altname = dataFeature.getAttribute(4);
                     String item = name+"\t"+altname+"\t"+type+"\t"+nxFeature.getAttribute("value");
                     
                     if (! list.contains(item)) {
                        list.add(item);
                     }                                         
                  }                  
               }
            }
            
            
            if (++cnt%1000 == 0) System.out.println("PROCESSED "+cnt+" INPUT DATA FEATURES");
         }
         




         // Use GeoTools TableWriter to format output
         TableWriter out = new TableWriter(new OutputStreamWriter(System.out), 3);
         // Sort alphabetically         
         Object[] listArray = list.toArray();
         Arrays.sort(listArray);
         
         // print out metadata
         System.out.println("--------------------------------------------------------------------");
         System.out.println(NexradUtilities.getLongName(header.getProductCode()));
         System.out.println(header.getICAO()+"  "+header.getDate()+" "+header.getHourString()+
            ":"+header.getMinuteString()+":"+header.getSecondString()+" UTC");
         out.nextLine('-');
         // Print out list
         
         //out.write("COUNT\tNAME\tSTATE\tPOP2000\tVALUE");
         out.write("COUNT\tNAME\tALTNAME\tTYPE\tVALUE");
         out.nextLine();
         out.nextLine('-');
         for (int n=0; n<listArray.length; n++) {
            //System.out.println(n + " ::: " + listArray[n].toString());
            out.write(n+"\t" + listArray[n].toString());
            out.nextLine();
         }
         out.flush();
         
         
      
      
      
      
      
      
      
   }
   
   
   
   
   
   
   
   
   
   
   
   
   
   public static void processAlphanumeric(String icao) throws IOException, IllegalAttributeException, DecodeException, SQLException, NumberFormatException, XPathExpressionException, SAXException, ParserConfigurationException, ParseException {
      
         double distanceBuffer = 10000; // 10 km;



         //File inputFile = new File("D:\\ESRI\\centusa\\ks\\kszip.shp");
         File inputFile = new File("D:\\ESRI\\usa\\places.shp");
         
   
         // Load some local data
         URL data_url = inputFile.toURL();
         ShapefileDataStore data_ds = new ShapefileDataStore(data_url);
         FeatureSource data_fs = data_ds.getFeatureSource("places");
         FeatureReader dataReader = data_fs.getFeatures().reader();
         System.out.println(data_fs.getFeatures().getCount() + " INPUT FEATURES IN "+inputFile.getName());
         

         // Load some NEXRAD
         // list at http://weather.noaa.gov/pub/SL.us008001/DF.of/DC.radar/
         //URL nxURL = new URL("http://weather.noaa.gov/pub/SL.us008001/DF.of/DC.radar/DS.61tvs/SI.kddc/sn.last");
         URL nxURL = new URL("http://weather.noaa.gov/pub/SL.us008001/DF.of/DC.radar/DS.p59hi/SI."+icao.toLowerCase()+"/sn.last");
         //URL nxURL = new URL("http://weather.noaa.gov/pub/SL.us008001/DF.of/DC.radar/DS.61tvs/SI."+icao.toLowerCase()+"/sn.last");
         //DecodeL3Header header = new DecodeL3Header();
         //header.decodeHeader(nxURL);               
         //DecodeHail data = new DecodeHail(header);
         EasyJNX jnx = new EasyJNX();
         StreamingRadialDecoder data = jnx.getDecoder(nxURL);
         NexradHeader header = jnx.getMostRecentHeader();
         
         FeatureCollection nxFeatures = data.getFeatures();
         System.out.println(nxFeatures.size() + " NEXRAD FEATURES");
         

         
         
         // Clear list
         list.clear();
         
         // Initiate Geodetic Calculator
         GeodeticCalculator geoCalc = new GeodeticCalculator();
         
         // Compare NEXRAD data to highways
         while (dataReader.hasNext()) {            
            Feature dataFeature = dataReader.next();
            Geometry dataGeometry = dataFeature.getDefaultGeometry();
            Coordinate dataCoord = dataGeometry.getCoordinate();
            geoCalc.setAnchorPoint(dataCoord.x, dataCoord.y);
            
            FeatureIterator fci = nxFeatures.features();
            while (fci.hasNext()) {
               Feature nxFeature = fci.next();
               Geometry nxGeometry = nxFeature.getDefaultGeometry();
               // Calculate distance between hail feature and city feature
               Coordinate nxCoord = nxGeometry.getCoordinate();
               geoCalc.setDestinationPoint(nxCoord.x, nxCoord.y);
               double distanceBetween = geoCalc.getOrthodromicDistance();
               
               if (distanceBetween <= distanceBuffer) {
                  
               //if (dataGeometry.intersects(nxGeometry)) {
               //if (nxGeometry.isWithinDistance(dataGeometry, .2)) { // uses decimal degrees which may be confusing
                  
                  String maxsizeString = nxFeature.getAttribute("maxsize").toString().replaceAll("<"," ");
                  double maxsize;                  
                  try {
                     maxsize = Double.parseDouble(maxsizeString);
                  } catch (Exception e) {
                     maxsize = -999.0;
                  }
                  
                  if (maxsize > 0.0) {
                  
                     Object name = dataFeature.getAttribute(1);
                     Object state = dataFeature.getAttribute(3);
                     Object pop = dataFeature.getAttribute(7);
                     String item = name+"\t"+state+"\t"+pop+"\t"+nxFeature.getAttribute("maxsize")+
                        "\t"+fmt3.format(geoCalc.getAzimuth())+"\t"+fmt3.format(distanceBetween);
                     if (! list.contains(item)) {
                        list.add(item);
                     }

                  }                  
               }
               
            }
         }
         
         
         
         // Use GeoTools TableWriter to format output
         TableWriter out = new TableWriter(new OutputStreamWriter(System.out), 3);
         // Sort alphabetically         
         Object[] listArray = list.toArray();
         Arrays.sort(listArray);
         
         // print out metadata
         out.nextLine('-');
         System.out.println(NexradUtilities.getLongName(header.getProductCode()));
         System.out.println(header.getICAO()+"  "+header.getDate()+" "+header.getHourString()+
            ":"+header.getMinuteString()+":"+header.getSecondString()+" UTC");
         out.nextLine('-');
         // Print out list
         
         out.write("COUNT\tNAME\tSTATE\tPOP2000\tMAXSIZE\tBEARING\tRANGE");
         out.nextLine();
         out.nextLine('-');
         for (int n=0; n<listArray.length; n++) {
            //System.out.println(n + " ::: " + listArray[n].toString());
            out.write(n+"\t" + listArray[n].toString());
            out.nextLine();
         }
         out.flush();
         
         
         
   }
   
   
   public static void main(String[] args) {
   
     try {
         
         
         //processPolygonData(args[0]);
         processAlphanumeric(args[0]);


   
      } catch (Exception e) {
         e.printStackTrace();
      }   
   }
}
