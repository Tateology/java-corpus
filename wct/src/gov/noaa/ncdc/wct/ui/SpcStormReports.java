package gov.noaa.ncdc.wct.ui;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.geotools.factory.FactoryConfigurationError;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.filter.IllegalFilterException;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapLayer;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class SpcStormReports {

	private FeatureType schema = null;
	private GeometryFactory geoFactory = new GeometryFactory();
	
    private final static Font[] fontArray = new Font[]{
        new Font("Arial", Font.PLAIN, 10),
        new Font("Arial", Font.PLAIN, 11),
        new Font("Arial", Font.PLAIN, 12),
        new Font("Arial", Font.PLAIN, 14),
        new Font("Arial", Font.PLAIN, 16)
    };
    
	public static final AttributeType[] ATTRIBUTES = {
		AttributeTypeFactory.newAttributeType("geom", Geometry.class),
		AttributeTypeFactory.newAttributeType("location", String.class, true, 20)
	};

	public SpcStormReports() throws FactoryConfigurationError, SchemaException {
		init();
	}
	
	private void init() throws FactoryConfigurationError, SchemaException {
		this.schema = FeatureTypeFactory.newFeatureType(
			ATTRIBUTES, "Storm Reports Attributes");
	}

	public MapLayer getMapLayer(String yyyymmdd) 
		throws IOException, NumberFormatException, IllegalAttributeException, IllegalFilterException {
		
		
		File file = new File("C:\\work\\spc\\120229_rpts_filtered_hail.csv");

		//	    	Time,Size,Location,County,State,Lat,Lon,Comments
		//	    	1220,125,SHANNON,RANDOLPH,AR,36.21,-90.96,(MEG)
		//	    	1245,100,5 N HANSON,HOPKINS,KY,37.49,-87.47,QUARTER SIZE HAIL REPORTED. (PAH)
		//	    	1250,150,1 NNW STRAWBERRY,LAWRENCE,AR,35.98,-91.33,(MEG)

		FeatureCollection fc = FeatureCollections.newCollection();

		BufferedReader br = new BufferedReader(new FileReader(file));
		String str;
		String[] headerCols;
		int latIndex = 0;
		int lonIndex = 0;
		int geoIndex = 0;

		if ((str = br.readLine()) != null) {
			headerCols = str.split(",");
			List headerList = Arrays.asList(headerCols);
			latIndex = headerList.indexOf("Lat");
			lonIndex = headerList.indexOf("Lon");
		}
		while ((str = br.readLine()) != null) {
			String[] cols = str.split(",");

			System.out.println(cols[latIndex]+" , "+cols[lonIndex]);
			
			Feature feature = schema.create(
	                  new Object[] { 
	                		  geoFactory.createPoint(new Coordinate(
	                				  Double.parseDouble(cols[lonIndex]), 
	                				  Double.parseDouble(cols[latIndex])))
	                		  , cols[2] },
	                  new Integer(geoIndex++).toString()
	                  );
			fc.add(feature);
			
			System.out.println(feature);
		}

		return new DefaultMapLayer(fc, getDefaultStyle());
	}


	
	
	
	
	public static Style getDefaultStyle() throws IllegalFilterException {
        StyleBuilder sb = new StyleBuilder();
        Style style = sb.createStyle();

//        Color defaultColor = new Color(30, 144, 255);
        Color defaultColor = Color.GREEN;

        Mark mark1 = sb.createMark(StyleBuilder.MARK_CIRCLE, Color.BLACK, Color.BLACK, 7);
        Graphic gr1 = sb.createGraphic(null, mark1, null);
        PointSymbolizer pntSymbolizer1 = sb.createPointSymbolizer(gr1);

        Mark mark2 = sb.createMark(StyleBuilder.MARK_CIRCLE, defaultColor, defaultColor, 4);
        Graphic gr2 = sb.createGraphic(null, mark2, null);
        PointSymbolizer pntSymbolizer2 = sb.createPointSymbolizer(gr2);

        Mark mark3 = sb.createMark(StyleBuilder.MARK_CIRCLE, Color.WHITE, Color.WHITE, 1);
        Graphic gr3 = sb.createGraphic(null, mark3, null);
        PointSymbolizer pntSymbolizer3 = sb.createPointSymbolizer(gr3);

//        try {
//            ExternalGraphic eg = sb.createExternalGraphic(new URL("http://maps.google.com/mapfiles/kml/pal4/icon50.png"), "image/png");        
//            Graphic gr4 = sb.createGraphic(eg, null, null);
//            pntSymbolizer3 = sb.createPointSymbolizer(gr4);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        

        /*
      org.geotools.styling.ExternalGraphicImpl egi = new org.geotools.styling.ExternalGraphicImpl();
      java.net.URL markerURL = MarkerEditor.class.getResource("/icons/bluepin.gif");
      egi.setLocation(markerURL);
      gr.addExternalGraphic(egi);

      PointSymbolizer pointSymbolizer = sb.createPointSymbolizer(gr);

      LineSymbolizer lineSymbolizer00 = sb.createLineSymbolizer(Color.BLACK, 3.0);
      LineSymbolizer lineSymbolizer01 = sb.createLineSymbolizer(Color.WHITE, 1.0);
      LineSymbolizer lineSymbolizer = sb.createLineSymbolizer(defaultColor, 2.0);
         */


        org.geotools.styling.Font font = sb.createFont(fontArray[0]);
        TextSymbolizer textSymbolizer1 = sb.createTextSymbolizer(Color.white, font, "label1");
        textSymbolizer1.setLabelPlacement(sb.createPointPlacement(0.0, 0.0, 10.0, -10.0, 0.0));
        textSymbolizer1.setHalo(sb.createHalo(Color.BLACK, .7, 2.2));
        TextSymbolizer textSymbolizer2 = sb.createTextSymbolizer(Color.white, font, "label2");
        textSymbolizer2.setLabelPlacement(sb.createPointPlacement(0.0, 0.0, 10.0, 0.0, 0.0));
        textSymbolizer2.setHalo(sb.createHalo(Color.BLACK, .7, 2.2));
        TextSymbolizer textSymbolizer3 = sb.createTextSymbolizer(Color.white, font, "label3");
        textSymbolizer3.setLabelPlacement(sb.createPointPlacement(0.0, 0.0, 10.0, 10.0, 0.0));
        textSymbolizer3.setHalo(sb.createHalo(Color.BLACK, .7, 2.2));

        /*      
      style.addFeatureTypeStyle(sb.createFeatureTypeStyle("Marker Attributes", 
         //new Symbolizer[] { pointSymbolizer, textSymbolizer }));
         new Symbolizer[] { lineSymbolizer00, lineSymbolizer, lineSymbolizer01, 
            textSymbolizer1, textSymbolizer2, textSymbolizer3 }));
         */
//        style.addFeatureTypeStyle(sb.createFeatureTypeStyle("Attributes", 
//                //new Symbolizer[] { pointSymbolizer, textSymbolizer }));
//                new Symbolizer[] { pntSymbolizer1, pntSymbolizer2, pntSymbolizer3, 
//                textSymbolizer1, textSymbolizer2, textSymbolizer3 }));

        style.addFeatureTypeStyle(sb.createFeatureTypeStyle("Attributes", 
                new Symbolizer[] { pntSymbolizer2 }));

        
        return style;         

        //return sb.createStyle(pointSymbolizer);
    }
}
