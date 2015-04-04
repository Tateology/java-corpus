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

import gov.noaa.ncdc.nexradiv.RangeRings;
import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Header;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Nexrad;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradColorFactory;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradHeader;
import gov.noaa.ncdc.wct.decoders.nexrad.StreamingRadialDecoder;
import gov.noaa.ncdc.wct.ui.AWTImageExport;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.filter.BetweenFilter;
import org.geotools.filter.FilterFactory;
import org.geotools.renderer.lite.LiteRenderer;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.TextSymbolizer;

import com.vividsolutions.jts.geom.Envelope;

public class NexradViewerBatchExample {
   
   private StyleBuilder sb = new StyleBuilder();
   private LiteRenderer renderer = new LiteRenderer();
   private FeatureCollection rangeRingFeatures = FeatureCollections.newCollection();
   
   
   private URL nexradURL;
   private File outputFile;
   
   
   //private NexradFile nexradFile = new NexradFile();

   
   public NexradViewerBatchExample() {
   }
   
 
   
   /*
   public void setInput(URL nexradURL);
   public void setOutput(File outputFile);
   public void setImageSize(int size);
   public void setImageExtent(Rectangle2D rect);
   
   
   //public void renderShapefile(URL shpURL, URL styleURL);
   
   public void renderShapefile(URL shpURL, URL styleURL {
     Style style = SLDUtilities.readStyle(styleURL); 
     
     String urlString = shpURL.toString();
     String name = urlString.substring(urlString.lastIndexOf("/")+1, urlString.length()-4);
     
     ShapefileDataStore ds = new ShapefileDataStore(url);
     FeatureSource fs = ds.getFeatureSource(name);
     FeatureCollection fc = fs.getFeatures().collection();
     renderer.render(fc, env, style);
   }
   
   
   
   */
   
   
   
   public void loadNexrad(URL nexradURL) throws DecodeException, MalformedURLException, IOException {

      // decode NEXRAD
      //NexradFile nexradFile = new NexradFile();
      //nexradFile.scanFile(nexradURL);
      
      final DecodeL3Header header = new DecodeL3Header();
      header.decodeHeader(nexradURL);
      final DecodeL3Nexrad decoder = new DecodeL3Nexrad(header);
      decoder.decodeData();
      
      
      // Don't load alphanumeric products
      if (header.getProductType() == NexradHeader.L3ALPHA ||
         header.getProductType() == NexradHeader.L3VAD) {
            
         System.err.println("ERROR: This product is not supported"); 
         return;
      }
      





      
      rangeRingFeatures = RangeRings.getRangeRingFeatures(
         header.getRadarCoordinate(), 0.0, 230.0, 25.0, RangeRings.KM, 0.0, 360.0, 30.0, rangeRingFeatures);
      //final Envelope env = new Envelope(rangeRingFeatures.getBounds());
      //Rectangle2D.Double rect = header.getNexradBounds();
      Rectangle2D.Double rect = new Rectangle2D.Double(-83.0, 41.0, 1.0, 1.0);
      Envelope env = new Envelope(rect.x, rect.x+rect.width, rect.y, rect.y+rect.height);
      
      double ratio = env.getWidth()/env.getHeight();
      int w = (int)(400*ratio);
      int h = 400;
        
      

      

      


/*
EXAMPLE FROM:
http://forum.java.sun.com/thread.jspa?threadID=474687&messageID=2199739
      
        URL[] urls = {
            new URL("http://weblogs.java.net/images/people/chris_adamson.jpg"),
            new URL("http://weblogs.java.net/images/people/ken_arnold.jpg"),
            new URL("http://weblogs.java.net/images/people/john_bobowicz.jpg"),
        };
        BufferedImage[] images = {
            ImageIO.read(urls[0]),
            ImageIO.read(urls[1]),
            ImageIO.read(urls[2]),
        };
        int w = images[0].getWidth(), h = images[0].getHeight();
        BufferedImage total = new BufferedImage(2*w, 2*h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = total.createGraphics();
        g.drawImage(images[0], 0, 0, null);
        g.drawImage(images[1], w, 0, null);
        g.drawImage(images[2], 0, h, null);
 
        //the text part
        String text = "Missing?";
        g.setFont(new Font("Lucida Bright", Font.PLAIN, 20));
        g.setPaint(Color.RED);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
 
        TextLayout tl = new TextLayout(text, g.getFont(), g.getFontRenderContext());
        Rectangle2D bounds = tl.getBounds();
        float x = (float)(w + (w-bounds.getWidth())/2 - bounds.getX());
        float y = (float)(h + (h-bounds.getHeight())/2 - bounds.getY());;
        tl.draw(g, x, y);
        g.dispose();
 */      
      
      
      
   // COMBINE IMAGES AND ADD TEXT LABELS      
      
      
      
      //BufferedImage bimage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
      //http://terraserver-usa.com/ogcmap.ashx?&version=1.1.1&request=GetMap&layers=DRG&styles=&srs=EPSG:4326&format=png&transparent=false&WIDTH=775&HEIGHT=455&BBOX=-84.52710723876953,38.98346094931325,-77.09584331512451,43.34633202706613
      URL mapURL = new URL("http://terraserver-usa.com/ogcmap.ashx?&version=1.1.1&request=GetMap&layers=DRG&styles=&srs=EPSG:4326&format=png&transparent=false&WIDTH="+w+"&HEIGHT="+h+"&BBOX="+rect.x+","+rect.y+","+(rect.x+rect.width)+","+(rect.y+rect.height));
      BufferedImage bimage = ImageIO.read(mapURL);
      
      if (bimage == null) {
         System.out.println("NO MAP IMAGE: "+mapURL);
      }
      
      Graphics g = bimage.getGraphics();
      
      
      
      
      
      
      
      
      renderer.setOutput((Graphics2D) g, new Rectangle(w, h));
      renderer.setOptimizedDataLoadingEnabled(true);
      renderer.setInteractive(true);
      renderer.render(decoder.getFeatures(), env, getNexradPolygonStyle(header, decoder));
     
      // add range rings
      Style rangeRingStyle = sb.createStyle(sb.createLineSymbolizer(Color.white, 1));
      renderer.render(rangeRingFeatures, env, rangeRingStyle);
      





      // Rivers
      
      try {
      
         URL url = NexradViewerBatchExample.class.getResource("/shapefiles/rivers.shp");
         ShapefileDataStore ds = new ShapefileDataStore(url);
         FeatureSource fs = ds.getFeatureSource("rivers");
         
         //FeatureResults fr = fsRivers.getFeatures();
         //FeatureCollection fc = fr.collection();

         FeatureCollection fc = fs.getFeatures().collection();
         Style style = sb.createStyle(sb.createLineSymbolizer(new Color(28, 134, 238), 1));

         renderer.render(fc, env, style);
         
      } catch (Exception e) {
         System.out.println("PROBLEM IN RIVERS");
         e.printStackTrace();         
      }


      // Cities
      
      try {

         // Points 
         
         URL url = NexradViewerBatchExample.class.getResource("/shapefiles/cities_10k.shp");
         ShapefileDataStore ds = new ShapefileDataStore(url);
         FeatureSource fs = ds.getFeatureSource("cities_10k");

         FeatureCollection fc = fs.getFeatures().collection();

         Mark mark = sb.createMark(StyleBuilder.MARK_CIRCLE, new Color(120, 120, 120), new Color(120, 120, 120), 1);
         Graphic gr = sb.createGraphic(null, mark, null);
         Style style = sb.createStyle(sb.createPointSymbolizer(gr));

         renderer.render(fc, env, style);
         
         
         // Labels

         org.geotools.styling.Font font = sb.createFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
         TextSymbolizer ts = sb.createTextSymbolizer(new Color(120, 120, 120), font, "AREANAME");
         ts.setLabelPlacement(sb.createPointPlacement(0.0, 0.0, 5.0, -5.0, 0.0));
         ts.setHalo(sb.createHalo(Color.BLACK, .7, 2.2));
         Style labelStyle = sb.createStyle(ts);
         renderer.render(fc, env, labelStyle);

         

      } catch (Exception e) {
         System.out.println("EXCEPTION WHILE LOADING CITY10 DATA");
         e.printStackTrace();
      }

      
      
      
         /*
         org.geotools.styling.Font font = sb.createFont(fontArray[1]);
         TextSymbolizer ts = sb.createTextSymbolizer(new Color(120, 120, 120), font, "AREANAME");
         ts.setLabelPlacement(sb.createPointPlacement(0.0, 0.0, 5.0, -5.0, 0.0));
         ts.setHalo(sb.createHalo(Color.BLACK, .7, 2.2));
         Style labelStyle = sb.createStyle(ts);
         baseMapLabels.setElementAt((Object) new DefaultMapLayer(fs, labelStyle, "CITY10_LABEL"), CITY10);

         baseMapStyleInfo.setElementAt((Object) new BaseMapStyleInfo(1, new Color(120, 120, 120), 
            new Color(120, 120, 120)), CITY10);
         */

      


         
         
         
         
         
         
        // Add label
        String text = "THIS IS A TEST OF A LABEL!";
        g.setFont(new java.awt.Font("Lucida Bright", java.awt.Font.PLAIN, 20));
        ((Graphics2D)g).setPaint(Color.RED);
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
 
        TextLayout tl = new TextLayout(text, g.getFont(), ((Graphics2D)g).getFontRenderContext());
        Rectangle2D bounds = tl.getBounds();
        float x = (float)(w + (w-bounds.getWidth())/2 - bounds.getX());
        float y = (float)(h + (h-bounds.getHeight())/2 - bounds.getY());;

System.out.println("FONT XY: "+x+" , "+y);        
        
        tl.draw(((Graphics2D)g), 100, 100);
        tl.draw(((Graphics2D)g), x, y);
        
        
        
        
        
        g.dispose();
         
         


      
      
      AWTImageExport.saveImage(bimage, new File("test"), AWTImageExport.Type.PNG);
      
      
      
      
      
      
   }
   
   
   
   
   
   
   
   private Style getNexradPolygonStyle(NexradHeader header, StreamingRadialDecoder decoder) {
         // Create Filters and Style for NEXRAD Polygons!
         Color[] color = NexradColorFactory.getColors(header.getProductCode());

         Rule rules[] = new Rule[color.length];
         Style nexradStyle = sb.createStyle();
         try {
            BetweenFilter filters[] = new BetweenFilter[color.length];
            FilterFactory ffi = FilterFactory.createFilterFactory();

            for (int i = 0; i < color.length; i++) {

               filters[i] = ffi.createBetweenFilter();
               PolygonSymbolizer polysymb = sb.createPolygonSymbolizer(color[i], color[i], 1);
               //polysymb.getFill().setOpacity(sb.literalExpression(nexradAlphaChannelValue/255.0));
               //polysymb.getStroke().setOpacity(sb.literalExpression(nexradAlphaChannelValue/255.0));
               rules[i] = sb.createRule(polysymb);

               filters[i].addLeftValue(sb.literalExpression(i));
               filters[i].addRightValue(sb.literalExpression(i + 1));
               filters[i].addMiddleValue(ffi.createAttributeExpression(decoder.getFeatureTypes()[0], "colorIndex"));
               rules[i].setFilter(filters[i]);

               nexradStyle.addFeatureTypeStyle(sb.createFeatureTypeStyle(null, rules[i]));
            }
         } catch (Exception e) {
            e.printStackTrace();
         }
         
         return nexradStyle;      
   }
      
   
   
   
   public static void main(String[] args) {
      NexradViewerBatchExample jnxQL = new NexradViewerBatchExample();
      try {
         URL url = new java.io.File("H:\\ViewerData\\HAS999900001\\7000KCLE_SDUS31_N1PCLE_200211102200").toURL();
         jnxQL.loadNexrad(url);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
   
   
}
