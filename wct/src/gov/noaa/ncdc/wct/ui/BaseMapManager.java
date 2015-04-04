package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.nexradiv.BaseMapStyleInfo;
import gov.noaa.ncdc.wct.ResourceUtils;
import gov.noaa.ncdc.wct.WCTConstants;

import java.awt.Color;
import java.awt.Font;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.IllegalFilterException;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapLayer;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class BaseMapManager {
	
	public enum DeclutterType { FULL, SIMPLE, NONE };

	public final static StyleBuilder sb = new StyleBuilder();
	
	public final static org.geotools.styling.Font[] GT_FONT_ARRAY = new org.geotools.styling.Font[] {
        sb.createFont(new Font("Arial", Font.PLAIN, 12)),
        sb.createFont(new Font("Arial", Font.PLAIN, 13)),
        sb.createFont(new Font("Arial", Font.PLAIN, 14)),
        sb.createFont(new Font("Arial", Font.PLAIN, 15)),
        sb.createFont(new Font("Arial", Font.PLAIN, 16))      
	};

	public final static  Font[] FONT_ARRAY = new Font[] {
        new Font("Arial", Font.PLAIN, 10),
        new Font("Arial", Font.PLAIN, 11),
        new Font("Arial", Font.PLAIN, 12),
        new Font("Arial", Font.PLAIN, 14),
        new Font("Arial", Font.PLAIN, 16)
	};

	
	
    private Vector<MapLayer> baseMapLayers = new Vector<MapLayer>();
    private Vector<MapLayer> baseMapLabelLayers = new Vector<MapLayer>();
    private Vector<BaseMapStyleInfo> baseMapStyleInfo = new Vector<BaseMapStyleInfo>();


    public static Style generateStyle(BaseMapStyleInfo styleInfo) {
    	Style style = sb.createStyle();
		Rule rule = null;
		if (styleInfo.getFillColor() != null) {
			rule = sb.createRule(sb.createPolygonSymbolizer(styleInfo.getFillColor(), styleInfo.getLineColor(), 
					styleInfo.getLineWidth()));
		}
		else {
			rule = sb.createRule(sb.createLineSymbolizer(styleInfo.getLineColor(), 
					styleInfo.getLineWidth()));
		}
		
//		System.out.println( styleInfo.getMaxScale() + " , "+ styleInfo.getMinScale());
				
		rule.setMaxScaleDenominator(styleInfo.getMaxScale());
		rule.setMinScaleDenominator(styleInfo.getMinScale());
		style.addFeatureTypeStyle(sb.createFeatureTypeStyle(null, rule));
		return style;
    }

    public static Style generateLabelStyle(int index, Font awtFont, Color color, Color haloColor, FeatureSource fs, 
    		double minScaleDenom, double maxScaleDenom) throws IllegalFilterException {

		return generateLabelStyle(index, awtFont, color, haloColor, fs, minScaleDenom, maxScaleDenom, DeclutterType.FULL);
    }
    public static Style generateLabelStyle(int index, Font awtFont, Color color, Color haloColor, FeatureSource fs, 
    		double minScaleDenom, double maxScaleDenom, DeclutterType declutterType) throws IllegalFilterException {

		org.geotools.styling.Font font = sb.createFont(awtFont);
		return generateLabelStyle(index, font, color, haloColor, fs, minScaleDenom, maxScaleDenom, declutterType);
    }

    public static Style generateLabelStyle(int index, org.geotools.styling.Font gtFont, Color color, Color haloColor, FeatureSource fs, 
    		double minScaleDenom, double maxScaleDenom) throws IllegalFilterException {
    	
    	return generateLabelStyle(index, gtFont, color, haloColor, fs, minScaleDenom, maxScaleDenom, DeclutterType.FULL);
    }
    public static Style generateLabelStyle(int index, org.geotools.styling.Font gtFont, Color color, Color haloColor, FeatureSource fs, 
    		double minScaleDenom, double maxScaleDenom, DeclutterType declutterType) throws IllegalFilterException {

    	String attName = getLabelAttributeName(index);
    	String[] attNameArray = attName.split(",");

    	Style labelStyle = sb.createStyle();

    	double rotationAsDeclutterFlag = 0;
    	if (declutterType == DeclutterType.NONE) {
    		rotationAsDeclutterFlag = 720;
    	}
    	else if (declutterType == DeclutterType.SIMPLE) {
    		rotationAsDeclutterFlag = 360;
    	}
    	
		double yDisplacment = -5;
		Symbolizer[] symbArray = new Symbolizer[attNameArray.length];
    	for (int n=0; n<attNameArray.length; n++) {
    		TextSymbolizer ts = sb.createTextSymbolizer(color, gtFont, attNameArray[n]);
    		ts.setHalo(sb.createHalo(haloColor, .7, 2.2));
    		ts.setLabelPlacement(sb.createPointPlacement(0.0, 0.0, 5.0, yDisplacment, rotationAsDeclutterFlag));
    		yDisplacment += 10;    		
    		symbArray[n] = ts;
    	}
		labelStyle.addFeatureTypeStyle(sb.createFeatureTypeStyle(symbArray, minScaleDenom, maxScaleDenom));
		return labelStyle;
    }
    
    public static String getLabelAttributeName(int index) {
    	if (index == WCTViewer.STATES || index == WCTViewer.STATES_OUT) {
    		return "ID";
    	}
    	else if (index == WCTViewer.COUNTRIES || index == WCTViewer.COUNTRIES_USA ||
    			index == WCTViewer.COUNTRIES_OUT || index == WCTViewer.COUNTRIES_OUT_USA) {
    		return "CNTRY_NAME";
    	}
    	else if (index == WCTViewer.COUNTIES) {
    		return "ID";
    	}
    	else if (index == WCTViewer.HWY_INT) {
    		return "ROUTE_NUM";
    	}
    	else if (index == WCTViewer.RIVERS) {
    		return "ID";
    	}
    	else if (index == WCTViewer.CITY250 || index == WCTViewer.CITY100 || index == WCTViewer.CITY35
    			|| index == WCTViewer.CITY10 || index == WCTViewer.CITY_SMALL) {
    		return "AREANAME";
    	}
    	else if (index == WCTViewer.AIRPORTS) {
    		return "ID";
    	}
    	else if (index == WCTViewer.ASOS_AWOS) {
    		return "ASOSID";
    	}
    	else if (index == WCTViewer.CRN) {
    		return "SITEID";
    	}
    	else if (index == WCTViewer.WSR) {
    		return "SITE";
    	}
    	else if (index == WCTViewer.TDWR) {
    		return "ICAO";
    	}
    	else if (index == WCTViewer.CLIMATE_DIV) {
    		return "NAME,DIVISION_I";
    	}
    	else {
    		return null;
    	}
    }
    
	public void loadBaseMaps(WCTViewer viewer) throws MalformedURLException {
		
		baseMapLayers.clear();
		baseMapLabelLayers.clear();
		baseMapStyleInfo.clear();

		baseMapLayers.setSize(WCTViewer.NUM_LAYERS);
		baseMapLabelLayers.setSize(WCTViewer.NUM_LAYERS);
		baseMapStyleInfo.setSize(WCTViewer.NUM_LAYERS);

		
        URL mapDataURL = new URL(WCTConstants.MAP_DATA_JAR_URL);
        URL url;

		try {
			url = ResourceUtils.getInstance().getJarResource(mapDataURL, ResourceUtils.RESOURCE_CACHE_DIR, "/shapefiles/states-360.shp", viewer);
			ShapefileDataStore ds = new ShapefileDataStore(url);
			FeatureSource fs = ds.getFeatureSource("states-360");
			
			BaseMapStyleInfo styleInfo = new BaseMapStyleInfo(10.0, BaseMapStyleInfo.NO_MAX_SCALE, 2, 
					Color.black, Color.black);
			baseMapLayers.setElementAt( 
					new DefaultMapLayer(fs, BaseMapManager.generateStyle(styleInfo), "_STATES_BG"), WCTViewer.STATES);
			baseMapStyleInfo.setElementAt( styleInfo, WCTViewer.STATES);


			Style styleFG = sb.createStyle();
			Rule ruleFG = sb.createRule(new Symbolizer[] {sb.createLineSymbolizer(new Color(235, 167, 0), 2)});
			ruleFG.setMinScaleDenominator(10.0);
			styleFG.addFeatureTypeStyle(sb.createFeatureTypeStyle(null, ruleFG));
			baseMapLayers.setElementAt( new DefaultMapLayer(fs, styleFG, "_STATES"), WCTViewer.STATES_OUT);


			org.geotools.styling.Font font = sb.createFont(FONT_ARRAY[3]);
			TextSymbolizer ts = sb.createTextSymbolizer(new Color(235, 167, 0), font, "ID");
			ts.setHalo(sb.createHalo(Color.BLACK, 1, 2));
			//        Style labelStyle = sb.createStyle(ts);

			Style labelStyle = sb.createStyle();
			Rule labelRule = sb.createRule(new Symbolizer[] {ts});
			labelRule.setMinScaleDenominator(10.0);
			labelStyle.addFeatureTypeStyle(sb.createFeatureTypeStyle(null, labelRule));
			baseMapLabelLayers.setElementAt( new DefaultMapLayer(fs, labelStyle, "STATES_LABELS"), WCTViewer.STATES_OUT);


			baseMapStyleInfo.setElementAt( new BaseMapStyleInfo(10.0, BaseMapStyleInfo.NO_MAX_SCALE, 2, new Color(235, 167, 0)), WCTViewer.STATES_OUT);

		} catch (Exception e) {
			System.out.println("EXCEPTION WHILE LOADING STATES DATA");
			e.printStackTrace();
		}

//		addLayer(fs, "_STATES_BG", 10.0, BaseMapStyleInfo.NO_MAX_SCALE, Color.black, Color.black, 1)  just an idea

		try {

			// Update splash window
			//splashWindow.setStatus("Loading countries-noUSA base map", 15);

			url = ResourceUtils.getInstance().getJarResource(mapDataURL, ResourceUtils.RESOURCE_CACHE_DIR, "/shapefiles/countries-noUSA-360.shp", viewer);
			ShapefileDataStore ds = new ShapefileDataStore(url);
			FeatureSource fs = ds.getFeatureSource("countries-noUSA-360");
			Style styleBG = sb.createStyle(sb.createPolygonSymbolizer(Color.black, Color.black, 1));
			Style styleFG = sb.createStyle(sb.createLineSymbolizer(new Color(5, 167, 0), 1));
			baseMapLayers.setElementAt( new DefaultMapLayer(fs, styleBG, "_COUNTRIES_BG"), WCTViewer.COUNTRIES);
			baseMapStyleInfo.setElementAt( new BaseMapStyleInfo(2, Color.black, Color.black), WCTViewer.COUNTRIES);

			
			
			baseMapLayers.setElementAt( new DefaultMapLayer(fs, styleFG, "_COUNTRIES"), WCTViewer.COUNTRIES_OUT);
//			baseMapLayers.setElementAt( new DefaultMapLayer(duplicateFeatureLongitudeAdd360( fs.getFeatures().collection() ), styleFG, "_COUNTRIES"), WCTViewer.COUNTRIES_OUT);

			org.geotools.styling.Font font = sb.createFont(FONT_ARRAY[3]);
			TextSymbolizer ts = sb.createTextSymbolizer(new Color(5, 167, 0), font, "CNTRY_NAME");
			ts.setHalo(sb.createHalo(Color.BLACK, 1, 2));
			Style labelStyle = sb.createStyle(ts);
			baseMapLabelLayers.setElementAt( new DefaultMapLayer(fs, labelStyle, "COUNTRIES_LABELS"), WCTViewer.COUNTRIES_OUT);
//			baseMapLabelLayers.setElementAt( new DefaultMapLayer(duplicateFeatureLongitudeAdd360( fs.getFeatures().collection() ), labelStyle, "COUNTRIES_LABELS"), WCTViewer.COUNTRIES_OUT);

			baseMapStyleInfo.setElementAt( new BaseMapStyleInfo(1, new Color(5, 167, 0)), WCTViewer.COUNTRIES_OUT);

		} catch (Exception e) {
			System.out.println("EXCEPTION WHILE LOADING COUNTRIES DATA");
			e.printStackTrace();
		}
		
		
		try {

			// Update splash window
			//splashWindow.setStatus("Loading countries-noUSA base map", 15);

			url = ResourceUtils.getInstance().getJarResource(mapDataURL, ResourceUtils.RESOURCE_CACHE_DIR, "/shapefiles/countries-USA-360.shp", viewer);
			ShapefileDataStore ds = new ShapefileDataStore(url);
			FeatureSource fs = ds.getFeatureSource("countries-USA-360");
			
			Style styleBG = sb.createStyle();
			Rule ruleBG = sb.createRule(new Symbolizer[] {sb.createPolygonSymbolizer(Color.black, Color.black, 1)});
			ruleBG.setMaxScaleDenominator(10.0);
			styleBG.addFeatureTypeStyle(sb.createFeatureTypeStyle(null, ruleBG));
			baseMapLayers.setElementAt( new DefaultMapLayer(fs, styleBG, "_COUNTRIES_BG_USA"), WCTViewer.COUNTRIES_USA);
			baseMapStyleInfo.setElementAt( new BaseMapStyleInfo(BaseMapStyleInfo.NO_MIN_SCALE, 10.0, 2, Color.black, Color.black), WCTViewer.COUNTRIES_USA);


			Style styleFG = sb.createStyle();
			Rule ruleFG = sb.createRule(new Symbolizer[] {sb.createLineSymbolizer(new Color(5, 167, 0), 1)});
			ruleFG.setMaxScaleDenominator(10.0);
			styleFG.addFeatureTypeStyle(sb.createFeatureTypeStyle(null, ruleFG));
			baseMapLayers.setElementAt( new DefaultMapLayer(fs, styleFG, "_COUNTRIES_USA"), WCTViewer.COUNTRIES_OUT_USA);

			// example showing opacity
//			Style styleFG = sb.createStyle();
//			Stroke stroke = sb.createStroke(new Color(5, 167, 0), 1, 0.5);
//			Rule ruleFG = sb.createRule(new Symbolizer[] {sb.createLineSymbolizer(stroke)});
//			ruleFG.setMaxScaleDenominator(10.0);
//			styleFG.addFeatureTypeStyle(sb.createFeatureTypeStyle(null, ruleFG));
//			

			

			org.geotools.styling.Font font = sb.createFont(FONT_ARRAY[3]);
			TextSymbolizer ts = sb.createTextSymbolizer(new Color(5, 167, 0), font, "CNTRY_NAME");
			ts.setHalo(sb.createHalo(Color.BLACK, 1, 2));
			//        Style labelStyle = sb.createStyle(ts);

			Style labelStyle = sb.createStyle();
			Rule labelRule = sb.createRule(new Symbolizer[] {ts});
			labelRule.setMaxScaleDenominator(10.0);
			labelStyle.addFeatureTypeStyle(sb.createFeatureTypeStyle(null, labelRule));
			baseMapLabelLayers.setElementAt( new DefaultMapLayer(fs, labelStyle, "COUNTRIES_LABELS_USA"), WCTViewer.COUNTRIES_OUT_USA);

			baseMapStyleInfo.setElementAt( new BaseMapStyleInfo(BaseMapStyleInfo.NO_MIN_SCALE, 10.0, 1, new Color(5, 167, 0)), WCTViewer.COUNTRIES_OUT_USA);

			
			
			

		} catch (Exception e) {
			System.out.println("EXCEPTION WHILE LOADING COUNTRIES DATA");
			e.printStackTrace();
		}


		try {

			// Update splash window
			//splashWindow.setStatus("Loading counties base map", 18);

			url = ResourceUtils.getInstance().getJarResource(mapDataURL, ResourceUtils.RESOURCE_CACHE_DIR, "/shapefiles/counties.shp", viewer);
			ShapefileDataStore dsCounties = new ShapefileDataStore(url);
			FeatureSource fsCounties = dsCounties.getFeatureSource("counties");
			//FeatureSource fsCounties = dsCounties.getView(query);
			Style countiesStyle = sb.createStyle(sb.createLineSymbolizer(new Color(169, 99, 49), 1));
			baseMapLayers.setElementAt( new DefaultMapLayer(fsCounties, countiesStyle, "_COUNTIES"), WCTViewer.COUNTIES);

			org.geotools.styling.Font font = sb.createFont(FONT_ARRAY[1]);
			TextSymbolizer tsCounties = sb.createTextSymbolizer(new Color(169, 99, 49), font, "ID");
			//tsCounties.setHalo(sb.createHalo(Color.BLACK, 1, 2));
			tsCounties.setHalo(sb.createHalo(Color.BLACK, .7, 2.2));
			Style countiesLabelStyle = sb.createStyle(tsCounties);
			baseMapLabelLayers.setElementAt( new DefaultMapLayer(fsCounties, countiesLabelStyle, "COUNTIES_LABELS"), WCTViewer.COUNTIES);

			baseMapStyleInfo.setElementAt( new BaseMapStyleInfo(1, new Color(169, 99, 49)), WCTViewer.COUNTIES);

		} catch (Exception e) {
			System.out.println("EXCEPTION WHILE LOADING COUNTY DATA");
			e.printStackTrace();
		}
		try {
			// Update splash window
			//splashWindow.setStatus("Loading interstates base map", 25);

			url = ResourceUtils.getInstance().getJarResource(mapDataURL, ResourceUtils.RESOURCE_CACHE_DIR, "/shapefiles/major_highways.shp", viewer);
			ShapefileDataStore dsHwyInt = new ShapefileDataStore(url);
			FeatureSource fsHwyInt = dsHwyInt.getFeatureSource("major_highways");
			Style hwyIntStyle = sb.createStyle();
			Rule hwyIntRule = sb.createRule(new Symbolizer[] {sb.createLineSymbolizer(Color.red, 1)});
			hwyIntRule.setMinScaleDenominator(10.0);
			hwyIntStyle.addFeatureTypeStyle(sb.createFeatureTypeStyle(null, hwyIntRule));
			baseMapLayers.setElementAt( new DefaultMapLayer(fsHwyInt, hwyIntStyle, "_HWY_INT"), WCTViewer.HWY_INT);


			
			org.geotools.styling.Font font = sb.createFont(FONT_ARRAY[1]);
			TextSymbolizer ts = sb.createTextSymbolizer(Color.red, font, "ROUTE_NUM");
			ts.setLabelPlacement(sb.createLinePlacement(0.0));
			ts.setHalo(sb.createHalo(Color.BLACK, .7, 2.2));
			Style style = sb.createStyle();
			Rule hwyIntLabelRule = sb.createRule(ts);
			hwyIntLabelRule.setMinScaleDenominator(10.0);
			style.addFeatureTypeStyle(sb.createFeatureTypeStyle(null, hwyIntLabelRule));
			baseMapLabelLayers.setElementAt( new DefaultMapLayer(fsHwyInt, style, "HWYINT_LABELS"), WCTViewer.HWY_INT);

			baseMapStyleInfo.setElementAt( new BaseMapStyleInfo(10.0, BaseMapStyleInfo.NO_MAX_SCALE, 1, Color.red), WCTViewer.HWY_INT);

		} catch (Exception e) {
			System.out.println("EXCEPTION WHILE LOADING HWYINT DATA");
			e.printStackTrace();
		}
		try {
			// Update splash window
			//splashWindow.setStatus("Loading rivers base map", 30);

			url = ResourceUtils.getInstance().getJarResource(mapDataURL, ResourceUtils.RESOURCE_CACHE_DIR, "/shapefiles/rivers.shp", viewer);
			ShapefileDataStore dsRivers = new ShapefileDataStore(url);
			FeatureSource fsRivers = dsRivers.getFeatureSource("rivers");
			Style riversStyle = sb.createStyle(sb.createLineSymbolizer(new Color(28, 134, 238), 1));
			baseMapLayers.setElementAt( new DefaultMapLayer(fsRivers, riversStyle, "_RIVERS"), WCTViewer.RIVERS);

			org.geotools.styling.Font font = sb.createFont(FONT_ARRAY[2]);
			TextSymbolizer ts = sb.createTextSymbolizer(new Color(28, 134, 238), font, "ID");
			ts.setLabelPlacement(sb.createLinePlacement(0.0));
			ts.setHalo(sb.createHalo(Color.BLACK, .7, 2.2));
			Style style = sb.createStyle(ts);
			baseMapLabelLayers.setElementAt( new DefaultMapLayer(fsRivers, style, "RIVERS_LABEL"), WCTViewer.RIVERS);

			baseMapStyleInfo.setElementAt( new BaseMapStyleInfo(1, new Color(28, 134, 238)), WCTViewer.RIVERS);

		} catch (Exception e) {
			System.out.println("EXCEPTION WHILE LOADING RIVER DATA");
			e.printStackTrace();
		}
		try {
			// Update splash window
			//splashWindow.setStatus("Loading cities base map", 30);

			url = ResourceUtils.getInstance().getJarResource(mapDataURL, ResourceUtils.RESOURCE_CACHE_DIR, "/shapefiles/cities_250k.shp", viewer);
			ShapefileDataStore ds = new ShapefileDataStore(url);
			FeatureSource fs = ds.getFeatureSource("cities_250k");
			Mark mark = sb.createMark(StyleBuilder.MARK_CIRCLE, new Color(240, 240, 240), new Color(240, 240, 240), 1);
			Graphic gr = sb.createGraphic(null, mark, null);
			Style style = sb.createStyle(sb.createPointSymbolizer(gr));
			baseMapLayers.setElementAt( new DefaultMapLayer(fs, style, "U.S. Cities [Pop > 250k]"), WCTViewer.CITY250);

			Style labelStyle = generateLabelStyle(WCTViewer.CITY250, FONT_ARRAY[4], new Color(240, 240, 240), Color.BLACK, fs, 0.0, BaseMapStyleInfo.NO_MAX_SCALE);
			baseMapLabelLayers.setElementAt( new DefaultMapLayer(fs, labelStyle, "LABELS_CITY250"), WCTViewer.CITY250);

			baseMapStyleInfo.setElementAt( new BaseMapStyleInfo(1, new Color(240, 240, 240), 
					new Color(240, 240, 240)), WCTViewer.CITY250);

		} catch (Exception e) {
			System.out.println("EXCEPTION WHILE LOADING CITY250 DATA");
			e.printStackTrace();
		}

		try {

			url = ResourceUtils.getInstance().getJarResource(mapDataURL, ResourceUtils.RESOURCE_CACHE_DIR, "/shapefiles/cities_100k.shp", viewer);
			ShapefileDataStore ds = new ShapefileDataStore(url);
			FeatureSource fs = ds.getFeatureSource("cities_100k");
			Mark mark = sb.createMark(StyleBuilder.MARK_CIRCLE, new Color(220, 220, 200), new Color(220, 220, 220), 1);
			Graphic gr = sb.createGraphic(null, mark, null);
			Style style = sb.createStyle(sb.createPointSymbolizer(gr));
			baseMapLayers.setElementAt( new DefaultMapLayer(fs, style, "U.S. Cities [Pop: 100k to 250k]"), WCTViewer.CITY100);

			Style labelStyle = generateLabelStyle(WCTViewer.CITY100, FONT_ARRAY[3], new Color(220, 220, 220), Color.BLACK, fs, 0.0, BaseMapStyleInfo.NO_MAX_SCALE);
			baseMapLabelLayers.setElementAt( new DefaultMapLayer(fs, labelStyle, "LABELS_CITY100"), WCTViewer.CITY100);

			
			
			baseMapStyleInfo.setElementAt( new BaseMapStyleInfo(1, new Color(220, 220, 220), 
					new Color(220, 220, 220)), WCTViewer.CITY100);

		} catch (Exception e) {
			System.out.println("EXCEPTION WHILE LOADING CITY100 DATA");
			e.printStackTrace();
		}

		try {

			url = ResourceUtils.getInstance().getJarResource(mapDataURL, ResourceUtils.RESOURCE_CACHE_DIR, "/shapefiles/cities_35k.shp", viewer);
			ShapefileDataStore ds = new ShapefileDataStore(url);
			FeatureSource fs = ds.getFeatureSource("cities_35k");
			Mark mark = sb.createMark(StyleBuilder.MARK_CIRCLE, new Color(220, 220, 220), new Color(220, 220, 220), 1);
			Graphic gr = sb.createGraphic(null, mark, null);
			Style style = sb.createStyle(sb.createPointSymbolizer(gr));
			baseMapLayers.setElementAt( new DefaultMapLayer(fs, style, "U.S. Cities [Pop: 35k to 100k]"), WCTViewer.CITY35);

			Style labelStyle = generateLabelStyle(WCTViewer.CITY35, FONT_ARRAY[2], new Color(220, 220, 220), Color.BLACK, fs, 0.0, BaseMapStyleInfo.NO_MAX_SCALE);
			baseMapLabelLayers.setElementAt( new DefaultMapLayer(fs, labelStyle, "LABELS_CITY35"), WCTViewer.CITY35);

			baseMapStyleInfo.setElementAt( new BaseMapStyleInfo(1, new Color(220, 220, 220), 
					new Color(220, 220, 220)), WCTViewer.CITY35);

		} catch (Exception e) {
			System.out.println("EXCEPTION WHILE LOADING CITY35 DATA");
			e.printStackTrace();
		}

		try {

			url = ResourceUtils.getInstance().getJarResource(mapDataURL, ResourceUtils.RESOURCE_CACHE_DIR, "/shapefiles/cities_10k.shp", viewer);
			ShapefileDataStore ds = new ShapefileDataStore(url);
			FeatureSource fs = ds.getFeatureSource("cities_10k");
			Mark mark = sb.createMark(StyleBuilder.MARK_CIRCLE, new Color(220, 220, 220), new Color(220, 220, 220), 1);
			Graphic gr = sb.createGraphic(null, mark, null);
			Style style = sb.createStyle(sb.createPointSymbolizer(gr));
			baseMapLayers.setElementAt( new DefaultMapLayer(fs, style, "U.S. Cities [Pop: 10k to 35k]"), WCTViewer.CITY10);

			Style labelStyle = generateLabelStyle(WCTViewer.CITY10, FONT_ARRAY[1], new Color(220, 220, 220), Color.BLACK, fs, 0.0, BaseMapStyleInfo.NO_MAX_SCALE);
			baseMapLabelLayers.setElementAt( new DefaultMapLayer(fs, labelStyle, "LABELS_CITY10"), WCTViewer.CITY10);

			baseMapStyleInfo.setElementAt( new BaseMapStyleInfo(1, new Color(220, 220, 220), 
					new Color(220, 220, 220)), WCTViewer.CITY10);

		} catch (Exception e) {
			System.out.println("EXCEPTION WHILE LOADING CITY10 DATA");
			e.printStackTrace();
		}

		try {

			url = ResourceUtils.getInstance().getJarResource(mapDataURL, ResourceUtils.RESOURCE_CACHE_DIR, "/shapefiles/cities_small.shp", viewer);
			ShapefileDataStore ds = new ShapefileDataStore(url);
			FeatureSource fs = ds.getFeatureSource("cities_small");
			Mark mark = sb.createMark(StyleBuilder.MARK_CIRCLE, new Color(220, 220, 220), new Color(220, 220, 220), 1);
			Graphic gr = sb.createGraphic(null, mark, null);
			Style style = sb.createStyle(sb.createPointSymbolizer(gr));
			baseMapLayers.setElementAt( new DefaultMapLayer(fs, style, "U.S. Cities [Pop: < 10k]"), WCTViewer.CITY_SMALL);

			Style labelStyle = generateLabelStyle(WCTViewer.CITY100, FONT_ARRAY[0], new Color(220, 220, 220), Color.BLACK, fs, 0.0, BaseMapStyleInfo.NO_MAX_SCALE);
			baseMapLabelLayers.setElementAt( new DefaultMapLayer(fs, labelStyle, "LABELS_CITY_SMALL"), WCTViewer.CITY_SMALL);

			baseMapStyleInfo.setElementAt( new BaseMapStyleInfo(1, new Color(220, 220, 220), 
					new Color(220, 220, 220)), WCTViewer.CITY_SMALL);

		} catch (Exception e) {
			System.out.println("EXCEPTION WHILE LOADING CITY_SMALL DATA");
			e.printStackTrace();
		}

		try {

			url = ResourceUtils.getInstance().getJarResource(mapDataURL, ResourceUtils.RESOURCE_CACHE_DIR, "/shapefiles/airports.shp", viewer);
			ShapefileDataStore ds = new ShapefileDataStore(url);
			FeatureSource fs = ds.getFeatureSource("airports");
			Mark mark = sb.createMark(StyleBuilder.MARK_CIRCLE, new Color(0, 200, 0), new Color(0, 200, 0), 1);
			Graphic gr = sb.createGraphic(null, mark, null);
			Style style = sb.createStyle(sb.createPointSymbolizer(gr));
			baseMapLayers.setElementAt( new DefaultMapLayer(fs, style, "U.S. Airports"), WCTViewer.AIRPORTS);

			Style labelStyle = generateLabelStyle(WCTViewer.AIRPORTS, FONT_ARRAY[2], new Color(0, 200, 0), Color.BLACK, fs, 0.0, BaseMapStyleInfo.NO_MAX_SCALE);
			baseMapLabelLayers.setElementAt( new DefaultMapLayer(fs, labelStyle, "LABELS_AIRPORTS"), WCTViewer.AIRPORTS);

			baseMapStyleInfo.setElementAt( new BaseMapStyleInfo(1, new Color(0, 200, 0), 
					new Color(0, 200, 0)), WCTViewer.AIRPORTS);

		} catch (Exception e) {
			System.out.println("EXCEPTION WHILE LOADING AIRPORTS DATA");
			e.printStackTrace();
		}

		try {

			url = ResourceUtils.getInstance().getJarResource(mapDataURL, ResourceUtils.RESOURCE_CACHE_DIR, "/shapefiles/asos_awos.shp", viewer);
			ShapefileDataStore ds = new ShapefileDataStore(url);
			FeatureSource fs = ds.getFeatureSource("asos_awos");
			Mark mark = sb.createMark(StyleBuilder.MARK_CIRCLE, new Color(102, 153, 255), new Color(102, 153, 255), 1);
			Graphic gr = sb.createGraphic(null, mark, null);
			Style style = sb.createStyle(sb.createPointSymbolizer(gr));
			baseMapLayers.setElementAt( new DefaultMapLayer(fs, style, "U.S. ASOS Network"), WCTViewer.ASOS_AWOS);

			Style labelStyle = generateLabelStyle(WCTViewer.ASOS_AWOS, FONT_ARRAY[2], new Color(102, 153, 255), Color.BLACK, fs, 0.0, BaseMapStyleInfo.NO_MAX_SCALE);
			baseMapLabelLayers.setElementAt( new DefaultMapLayer(fs, labelStyle, "LABELS_ASOS"), WCTViewer.ASOS_AWOS);

			baseMapStyleInfo.setElementAt( new BaseMapStyleInfo(1, new Color(102, 153, 255), new Color(102, 153, 255)), WCTViewer.ASOS_AWOS);

		} catch (Exception e) {
			System.out.println("EXCEPTION WHILE LOADING ASOS_AWOS DATA");
			e.printStackTrace();
		}

		try {

			url = ResourceUtils.getInstance().getJarResource(mapDataURL, ResourceUtils.RESOURCE_CACHE_DIR, "/shapefiles/crn.shp", viewer);
			ShapefileDataStore ds = new ShapefileDataStore(url);
			FeatureSource fs = ds.getFeatureSource("crn");
			Mark mark = sb.createMark(StyleBuilder.MARK_CIRCLE, new Color(32, 178, 170), new Color(32, 178, 170), 1);
			Graphic gr = sb.createGraphic(null, mark, null);
			Style style = sb.createStyle(sb.createPointSymbolizer(gr));
			baseMapLayers.setElementAt( new DefaultMapLayer(fs, style, "U.S. CRN Network"), WCTViewer.CRN);

			Style labelStyle = generateLabelStyle(WCTViewer.CRN, FONT_ARRAY[2], new Color(32, 178, 170), Color.BLACK, fs, 0.0, BaseMapStyleInfo.NO_MAX_SCALE);
			baseMapLabelLayers.setElementAt( new DefaultMapLayer(fs, labelStyle, "LABELS_CRN"), WCTViewer.CRN);

			baseMapStyleInfo.setElementAt( new BaseMapStyleInfo(1, new Color(32, 178, 170), new Color(32, 178, 170)), WCTViewer.CRN);

		} catch (Exception e) {
			System.out.println("EXCEPTION WHILE LOADING CRN DATA");
			e.printStackTrace();
		}

		try {
			// Update splash window
			//splashWindow.setStatus("Loading radar base map", 35);

			url = ResourceUtils.getInstance().getJarResource(mapDataURL, ResourceUtils.RESOURCE_CACHE_DIR, "/shapefiles/wsr.shp", viewer);
			ShapefileDataStore ds = new ShapefileDataStore(url);
			FeatureSource fs = ds.getFeatureSource("wsr");
			Mark mark = sb.createMark(StyleBuilder.MARK_CIRCLE, new Color(204, 102, 255), new Color(204, 102, 255), 2);
			Graphic gr = sb.createGraphic(null, mark, null);
			Style style = sb.createStyle(sb.createPointSymbolizer(gr));
			baseMapLayers.setElementAt( new DefaultMapLayer(fs, style, "U.S. NEXRAD Radar Network"), WCTViewer.WSR);

			Style labelStyle = generateLabelStyle(WCTViewer.WSR, FONT_ARRAY[3], new Color(204, 102, 255), Color.BLACK, fs, 0.0, BaseMapStyleInfo.NO_MAX_SCALE);
			baseMapLabelLayers.setElementAt( new DefaultMapLayer(fs, labelStyle, "LABELS_WSR"), WCTViewer.WSR);

			baseMapStyleInfo.setElementAt( new BaseMapStyleInfo(1, new Color(204, 102, 255), 
					new Color(204, 102, 255)), WCTViewer.WSR);

		} catch (Exception e) {
			System.out.println("EXCEPTION WHILE LOADING WSR DATA");
			e.printStackTrace();
		}


		try {
			// Update splash window
			//splashWindow.setStatus("Loading radar base map", 35);

			url = ResourceUtils.getInstance().getJarResource(mapDataURL, ResourceUtils.RESOURCE_CACHE_DIR, "/shapefiles/tdwr.shp", viewer);
			ShapefileDataStore ds = new ShapefileDataStore(url);
			FeatureSource fs = ds.getFeatureSource("tdwr");
			Mark mark = sb.createMark(StyleBuilder.MARK_CIRCLE, new Color(198, 0, 190), new Color(198, 0, 190), 2);
			Graphic gr = sb.createGraphic(null, mark, null);
			Style style = sb.createStyle(sb.createPointSymbolizer(gr));
			baseMapLayers.setElementAt( new DefaultMapLayer(fs, style, "U.S. TDWR Radar Network"), WCTViewer.TDWR);

			Style labelStyle = generateLabelStyle(WCTViewer.TDWR, FONT_ARRAY[3], new Color(198, 0, 190), Color.BLACK, fs, 0.0, BaseMapStyleInfo.NO_MAX_SCALE);
			baseMapLabelLayers.setElementAt( new DefaultMapLayer(fs, labelStyle, "LABELS_TDWR"), WCTViewer.TDWR);

			baseMapStyleInfo.setElementAt( new BaseMapStyleInfo(1, new Color(198, 0, 190), 
					new Color(198, 0, 190)), WCTViewer.TDWR);

		} catch (Exception e) {
			System.out.println("EXCEPTION WHILE LOADING TDWR DATA");
			e.printStackTrace();
		}


		try {

			// Update splash window
			//splashWindow.setStatus("Loading climate division base map", 42);

			url = ResourceUtils.getInstance().getJarResource(mapDataURL, ResourceUtils.RESOURCE_CACHE_DIR, "/shapefiles/climate-divisions.shp", viewer);
			ShapefileDataStore ds = new ShapefileDataStore(url);
			FeatureSource fs = ds.getFeatureSource("climate-divisions");
			//FeatureSource fs = ds.getView(query);
			Style climateDivStyle = sb.createStyle(sb.createLineSymbolizer(new Color(245, 174, 138), 1));
			baseMapLayers.setElementAt( new DefaultMapLayer(fs, climateDivStyle, "_CLIMATE_DIV"), WCTViewer.CLIMATE_DIV);

//			Style labelStyle = sb.createStyle();
//			org.geotools.styling.Font font = sb.createFont(FONT_ARRAY[1]);
//			TextSymbolizer ts = sb.createTextSymbolizer(new Color(245, 174, 138), font, "NAME");
//			ts.setHalo(sb.createHalo(Color.BLACK, .7, 2.2));
//			ts.setLabelPlacement(sb.createPointPlacement(0.0, 0.0, 5.0, -5.0, 0.0));
//			//Style labelStyle = sb.createStyle(tsClimateDiv);
//			TextSymbolizer ts2 = sb.createTextSymbolizer(new Color(245, 174, 138), font, "DIVISION_I");
//			ts2.setHalo(sb.createHalo(Color.BLACK, .7, 2.2));
//			ts2.setLabelPlacement(sb.createPointPlacement(0.0, 0.0, 5.0, 5.0, 0.0));
//
//			labelStyle.addFeatureTypeStyle(sb.createFeatureTypeStyle("climate-divisions", 
//					new Symbolizer[] { ts, ts2 }));
			Style labelStyle = generateLabelStyle(WCTViewer.CLIMATE_DIV, FONT_ARRAY[1], new Color(245, 174, 138), Color.BLACK, 
					fs, 0.0, BaseMapStyleInfo.NO_MAX_SCALE, DeclutterType.NONE);
			


			baseMapLabelLayers.setElementAt( new DefaultMapLayer(fs, labelStyle, "CLIMATE_DIV_LABELS"), WCTViewer.CLIMATE_DIV);

			baseMapStyleInfo.setElementAt( new BaseMapStyleInfo(1, new Color(245, 174, 138)), WCTViewer.CLIMATE_DIV);

		} catch (Exception e) {
			System.out.println("EXCEPTION WHILE LOADING CLIMATE_DIVISION DATA");
			e.printStackTrace();
		}

		//*/




	}

	public Vector<MapLayer> getBaseMapLayers() {
		return baseMapLayers;
	}
	public Vector<MapLayer> getBaseMapLabelLayers() {
		return baseMapLabelLayers;
	}
	public Vector<BaseMapStyleInfo> getBaseMapStyleInfo() {
		return baseMapStyleInfo;
	}

	
	
	
	
	
	
	
	
	
	public static FeatureCollection duplicateFeatureLongitudeAdd360(FeatureCollection fc) throws IllegalAttributeException {
        FeatureIterator iter = fc.features();
        FeatureCollection fc360 = FeatureCollections.newCollection();
        int geoIndex = 0;
        while (iter.hasNext()) {
            Feature f = iter.next();
            
//            System.out.println(f.getFeatureType());
            
            Feature f1 = f.getFeatureType().create(f.getAttributes(null), String.valueOf(geoIndex++));
            System.out.println(f1.toString().substring(0, 80));
            fc360.add(f1);
            
            Geometry geom = f.getDefaultGeometry();
            Coordinate[] coords = geom.getCoordinates();
            for (Coordinate c : coords) {
//                c.x += 360;
                c.x += 720;
            }
            
            Feature f2 = f.getFeatureType().create(f.getAttributes(null), String.valueOf(geoIndex++));
            System.out.println(f2.toString().substring(0, 80));
            fc360.add(f2);

        }
        
        return fc360;
	}
}
