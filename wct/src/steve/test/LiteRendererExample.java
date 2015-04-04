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

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.renderer.lite.LiteRenderer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.TextSymbolizer;

import com.vividsolutions.jts.geom.Envelope;


public class LiteRendererExample {

   
   public static void main(String[] args) {
      
      try {
      
   
//Create the feature collection
        // Request extent
        /*
        Envelope ex = new Envelope(5, 15, 5, 15);
        
        AttributeType[] types = new AttributeType[2];
        
        GeometryFactory geomFac = new GeometryFactory();
        
        //simpleline
        Coordinate[] linestringCoordinates = new Coordinate[7];
        linestringCoordinates[0] = new Coordinate(5.0d,5.0d);
        linestringCoordinates[1] = new Coordinate(6.0d,5.0d);
        linestringCoordinates[2] = new Coordinate(6.0d,6.0d);
        linestringCoordinates[3] = new Coordinate(7.0d,6.0d);
        linestringCoordinates[4] = new Coordinate(7.0d,7.0d);
        linestringCoordinates[5] = new Coordinate(8.0d,7.0d);
        linestringCoordinates[6] = new Coordinate(8.0d,8.0d);
        LineString line = geomFac.createLineString(linestringCoordinates);
        
        types[0] = AttributeTypeFactory.newAttributeType("centerline", line.getClass());
        types[1] = AttributeTypeFactory.newAttributeType("name", String.class);
        FeatureType lineType = FeatureTypeFactory.newFeatureType(types, "linefeature");
        Feature lineFeature = lineType.create(new Object[]{line, "centerline"});
        
        //simple polygon
        Coordinate[] polygonCoordinates = new Coordinate[10];
        polygonCoordinates[0] = new Coordinate(7,7);
        polygonCoordinates[1] = new Coordinate(6,9);
        polygonCoordinates[2] = new Coordinate(6,11);
        polygonCoordinates[3] = new Coordinate(7,12);
        polygonCoordinates[4] = new Coordinate(9,11);
        polygonCoordinates[5] = new Coordinate(11,12);
        polygonCoordinates[6] = new Coordinate(13,11);
        polygonCoordinates[7] = new Coordinate(13,9);
        polygonCoordinates[8] = new Coordinate(11,7);
        polygonCoordinates[9] = new Coordinate(7,7);
        LinearRing ring = geomFac.createLinearRing(polygonCoordinates);
        Polygon polygon = geomFac.createPolygon(ring,null);
        
        types[0] = AttributeTypeFactory.newAttributeType("edge", polygon.getClass());
        types[1] = AttributeTypeFactory.newAttributeType("name", String.class);
        FeatureType polygonType = FeatureTypeFactory.newFeatureType(types,"polygonfeature");

        Feature polygonFeature = polygonType.create(new Object[]{polygon, "edge"});
        
        //simple point
        Coordinate c = new Coordinate(14.0d,14.0d);
        Point point = geomFac.createPoint(c);
        types[0] = AttributeTypeFactory.newAttributeType("centre", point.getClass());
        types[1] = AttributeTypeFactory.newAttributeType("name", String.class);
        FeatureType pointType = FeatureTypeFactory.newFeatureType(types,"pointfeature");
        
        Feature pointFeature = pointType.create(new Object[]{point, "centre"});
        
        //simple linear ring
        LinearRing ring2 = geomFac.createLinearRing(polygonCoordinates);
        Polygon polygon2 = geomFac.createPolygon(ring2,null);
        // build Shifted Geometry
        Polygon clone = (Polygon) polygon2.clone();
        Coordinate[] coords = clone.getCoordinates();
        for(int i = 0; i < coords.length; i++) {
            Coordinate coord = coords[i];
            coord.x += 0;
            coord.y += 100;
        }
        Polygon polyg = clone;
        LinearRing ring3 = (LinearRing) polyg.getExteriorRing();
        types[0] = AttributeTypeFactory.newAttributeType("centerline", line.getClass());
        types[1] = AttributeTypeFactory.newAttributeType("name", String.class);
        FeatureType lrType = FeatureTypeFactory.newFeatureType(types,"ringfeature");
        Feature ringFeature = lrType.create(new Object[]{ring3, "centerline"});
    
        //memory store
        MemoryDataStore data = new MemoryDataStore();
        data.addFeature(lineFeature);
        data.addFeature(polygonFeature);
        data.addFeature(pointFeature);
        data.addFeature(ringFeature);
        
        String typeName = data.getTypeNames()[0];
    	FeatureCollection ft = data.getFeatureSource( typeName ).getFeatures().collection();
    	
//Create style
    	StyleFactory sFac = StyleFactory.createStyleFactory();
        //The following is complex, and should be built from
        //an SLD document and not by hand
        PointSymbolizer pointsym = sFac.createPointSymbolizer();
        pointsym.setGraphic(sFac.getDefaultGraphic());
        
        LineSymbolizer linesym = sFac.createLineSymbolizer();
        Stroke myStroke = sFac.getDefaultStroke();
        FilterFactory filterFactory = FilterFactory.createFilterFactory();
        myStroke.setColor(filterFactory.createLiteralExpression("#0000ff"));
        myStroke.setWidth(filterFactory.createLiteralExpression(new Integer(5)));
        linesym.setStroke(myStroke);
        
        PolygonSymbolizer polysym = sFac.createPolygonSymbolizer();
        Fill myFill = sFac.getDefaultFill();
        myFill.setColor(filterFactory.createLiteralExpression("#ff0000"));
        polysym.setFill(myFill);
        polysym.setStroke(sFac.getDefaultStroke());
        Rule rule = sFac.createRule();
        rule.setSymbolizers(new Symbolizer[]{polysym});
        FeatureTypeStyle fts = sFac.createFeatureTypeStyle(new Rule[]{rule});
        fts.setFeatureTypeName("polygonfeature");
        
        Rule rule2 = sFac.createRule();
        rule2.setSymbolizers(new Symbolizer[]{linesym});
        FeatureTypeStyle fts2 = sFac.createFeatureTypeStyle();
        fts2.setRules(new Rule[]{rule2});
        fts2.setFeatureTypeName("linefeature");
        
        Rule rule3 = sFac.createRule();
        rule3.setSymbolizers(new Symbolizer[]{pointsym});
        FeatureTypeStyle fts3 = sFac.createFeatureTypeStyle();
        fts3.setRules(new Rule[]{rule3});
        fts3.setFeatureTypeName("pointfeature");
        
        Rule rule4 = sFac.createRule();
        rule4.setSymbolizers(new Symbolizer[]{linesym});
        FeatureTypeStyle fts4 = sFac.createFeatureTypeStyle();
        fts4.setRules(new Rule[]{rule4});
        fts4.setFeatureTypeName("ringFeature");
        
        Style style = sFac.createStyle();
        style.setFeatureTypeStyles(new FeatureTypeStyle[]{fts, fts2, fts3, fts4});
        
        
	MapContext map = new DefaultMapContext();
        map.addLayer(ft,style);
        */
        

        
        
        
        
        
        
        
        
        
        
        
        
        
         StyleBuilder sb = new StyleBuilder();
         java.net.URL url = LiteRendererExample.class.getResource("/shapefiles/counties.shp");
         ShapefileDataStore dsCounties = new ShapefileDataStore(url);
         FeatureSource fsCounties = dsCounties.getFeatureSource("counties");
         Style countiesStyle = sb.createStyle(sb.createLineSymbolizer(new Color(169, 99, 49), 1));
         DefaultMapLayer dmlCounties = new DefaultMapLayer(fsCounties, countiesStyle, "COUNTIES");

         org.geotools.styling.Font font = sb.createFont(new Font("Arial", Font.PLAIN, 12));
         TextSymbolizer tsCounties = sb.createTextSymbolizer(new Color(169, 99, 49), font, "ID");
         tsCounties.setHalo(sb.createHalo(Color.BLACK, .7, 2.2));
         Style countiesLabelStyle = sb.createStyle(tsCounties);
         DefaultMapLayer dmlCountiesLabel = new DefaultMapLayer(fsCounties, countiesLabelStyle, "COUNTIES_LABELS");

         
         
        
        MapContext map = new DefaultMapContext();
        map.addLayer(dmlCounties);
        map.addLayer(dmlCountiesLabel);
        
        
        Envelope env = fsCounties.getBounds();
        double ratio = env.getHeight()/env.getWidth();
        
        
 //Showing render
        Frame frame = new Frame();
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {e.getWindow().dispose(); }
        });
        Panel p = new Panel();
        frame.add(p);
        frame.setSize(600,(int)(600*ratio));
        frame.setVisible(true);

        //AffineTransform transform = new AffineTransform();
        //transform.scale(2.0, -2.0);
        
        
        //LiteRenderer renderer = new LiteRenderer(map);
        
        LiteRenderer renderer = new LiteRenderer();
        renderer.setOutput((Graphics2D) p.getGraphics(), new Rectangle(600, (int)(600*ratio)));
        renderer.setOptimizedDataLoadingEnabled(true);
        renderer.setInteractive(true);
        renderer.render(fsCounties.getFeatures().collection(), fsCounties.getBounds(), countiesStyle);
        
        
        
        
        
        
        //renderer.paint((Graphics2D) p.getGraphics(),p.getBounds(), new AffineTransform());
        /*
        renderer.paint((Graphics2D) p.getGraphics(),p.getBounds(), transform);
        int w = 300, h = 600;
        BufferedImage image = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        g.setColor(Color.white);
        g.fillRect(0,0,w,h);
        //renderer.paint((Graphics2D) g,new Rectangle(0,0,w,h), new AffineTransform());
        
        
        renderer.paint((Graphics2D) g,new Rectangle(0,0,w,h), transform);
        */
        
        
        Thread.sleep(1000);
        //frame.dispose();
        
      } catch (Exception e) {
         e.printStackTrace();
      }
   }  
}
