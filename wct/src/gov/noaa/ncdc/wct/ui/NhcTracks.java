package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.common.URLDirectory;
import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.io.WCTTransfer;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.IllegalFilterException;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapLayer;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;

public class NhcTracks {

    
    final public static String NHC_DOWNLOAD_DIR = "http://www.nhc.noaa.gov/gis/forecast/archive/";
    final public static boolean ignoreCache = true;
    
    private StyleBuilder sb = new StyleBuilder();
    
    public ArrayList<URL> getLatestUrlList() throws MalformedURLException {
        
        ArrayList<URL> list = new ArrayList<URL>();
        
        URL[] urls = URLDirectory.getURLs(new URL(NHC_DOWNLOAD_DIR));
        for (URL url : urls) {
            if (url.toString().contains(".latest_")) {
                list.add(url);
            }
        }
        
        return list;
    }
    
    
    
    public ArrayList<URL> getCachedShapefileLatestUrlList() throws ZipException, IOException {
        
        ArrayList<URL> shpList = new ArrayList<URL>();
        
        ArrayList<URL> zipList = getLatestUrlList();
        for (URL url : zipList) {
            URL zipCacheURL = WCTTransfer.getURL(url, ignoreCache);
            String name = zipCacheURL.getFile().substring(zipCacheURL.getFile().lastIndexOf('/')+1);
            
            
            System.out.println(name);
            File cachedZipFile = new File(WCTConstants.getInstance().getDataCacheLocation() + File.separator + name);
            
            File unzipDir = new File(WCTConstants.getInstance().getDataCacheLocation() + File.separator + name.substring(0, name.length()-4));
            unzipDir.mkdirs();
            
            ZipFile zip = new ZipFile(cachedZipFile);
            Enumeration entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry)entries.nextElement();
                
                System.out.println("Extracting: "+entry.getName());
                File outFile = new File(unzipDir+File.separator+entry.getName());
                
                InputStream in = zip.getInputStream(entry);
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFile));
                WCTUtils.copyInputStream(in, out);
                in.close();
                out.close();
                
                if (entry.getName().endsWith(".shp")) {
                    shpList.add(outFile.toURI().toURL());
                }
            }
            zip.close();
            
        }
        
        
        return shpList;
    }
    
    
    
    public MapLayer getTrackLineLayer(ArrayList<URL> shpList) throws IOException {
        
        FeatureCollection fc = FeatureCollections.newCollection();

        boolean foundFile = false;
        
        for (URL url : shpList) {
            if (url.toString().endsWith("_lin.shp")) {
                foundFile = true;
                addToCollection(fc, url);
            }
        }
        
        System.out.println("TOTAL: ADDED "+fc.size()+" TRACK LINE FEATURES");
        
        if (! foundFile) {        
            throw new IOException("No *_lin.shp file found...");
        }
        
        Style style = sb.createStyle(sb.createLineSymbolizer(Color.BLACK, 3));
        return new DefaultMapLayer(fc, style);
    }

    
    
    public MapLayer getTrackPolygonLayer(ArrayList<URL> shpList) throws IOException {
        
        FeatureCollection fc = FeatureCollections.newCollection();

        boolean foundFile = false;
        
        for (URL url : shpList) {
            if (url.toString().endsWith("_pgn.shp")) {
                foundFile = true;
                addToCollection(fc, url);
            }
        }
        
        System.out.println("TOTAL: ADDED "+fc.size()+" TRACK POLY FEATURES");
        
        if (! foundFile) {        
            throw new IOException("No *_pgn.shp file found...");
        }
        
        PolygonSymbolizer polySymbolizer = sb.createPolygonSymbolizer(new Color(255, 255, 255, 188), Color.BLACK, 1);
        polySymbolizer.getFill().setOpacity(sb.literalExpression(0.5));
        Style style = sb.createStyle(polySymbolizer);
        return new DefaultMapLayer(fc, style);
    }
    

    
    
    
    public MapLayer getCoastalWarningLayer(ArrayList<URL> shpList) throws IOException {
        
        FeatureCollection fc = FeatureCollections.newCollection();

        boolean foundFile = false;
        
        for (URL url : shpList) {
            if (url.toString().endsWith("_wwlin.shp")) {
                foundFile = true;
                addToCollection(fc, url);
            }
        }
        
        System.out.println("TOTAL: ADDED "+fc.size()+" TRACK WARN FEATURES");
        
        if (! foundFile) {        
            throw new IOException("No *_wwlin.shp file found...");
        }
        
        Style style = sb.createStyle(sb.createLineSymbolizer(Color.RED, 5));
        return new DefaultMapLayer(fc, style);
    }

    

    
    public MapLayer getTrackPointLayer(ArrayList<URL> shpList) throws IOException, IllegalFilterException {
        
        FeatureCollection fc = FeatureCollections.newCollection();

        boolean foundFile = false;
        
        ArrayList<String> typeNameList = new ArrayList<String>();
        
        for (URL url : shpList) {
            if (url.toString().endsWith("_pts.shp")) {
                foundFile = true;
                typeNameList.add(addToCollection(fc, url));
            }
        }
        
        System.out.println("TOTAL: ADDED "+fc.size()+" TRACK POINT FEATURES");
        
        if (! foundFile) {        
            throw new IOException("No *_pts.shp file found...");
        }
        
        Mark mark1 = sb.createMark(StyleBuilder.MARK_CIRCLE, Color.BLUE, Color.BLUE, 10);
        Graphic gr1 = sb.createGraphic(null, mark1, null);
        PointSymbolizer pntSymbolizer1 = sb.createPointSymbolizer(gr1);

        org.geotools.styling.Font font = sb.createFont(new Font("Arial", Font.PLAIN, 14));
        TextSymbolizer textSymbolizer1 = sb.createTextSymbolizer(Color.white, font, "DVLBL");
        textSymbolizer1.setLabelPlacement(sb.createPointPlacement(0.0, 0.0, -5.0, 5.0, 0.0));
        textSymbolizer1.setHalo(sb.createHalo(Color.BLACK, .7, 2.2));
        TextSymbolizer textSymbolizer2 = sb.createTextSymbolizer(Color.white, font, "DATELBL");
        textSymbolizer2.setLabelPlacement(sb.createPointPlacement(0.0, 0.0, 10.0, 5.0, 0.0));
        textSymbolizer2.setHalo(sb.createHalo(Color.BLACK, .7, 2.2));
        TextSymbolizer textSymbolizer3 = sb.createTextSymbolizer(Color.white, font, "STORMNAME");
        textSymbolizer3.setLabelPlacement(sb.createPointPlacement(0.0, 0.0, 10.0, -12.0, 0.0));
        textSymbolizer3.setHalo(sb.createHalo(Color.BLACK, .7, 2.2));

        
        Style style = sb.createStyle();
        for (String typeName : typeNameList) {
            
            style.addFeatureTypeStyle(sb.createFeatureTypeStyle(typeName, 
                    new Symbolizer[] { pntSymbolizer1 }, 5, 5000000));
            style.addFeatureTypeStyle(sb.createFeatureTypeStyle(typeName, 
                    new Symbolizer[] { textSymbolizer1 }, 5, 5000000));
            style.addFeatureTypeStyle(sb.createFeatureTypeStyle(typeName, 
                    new Symbolizer[] { textSymbolizer2 }, 14, 5000000));
            style.addFeatureTypeStyle(sb.createFeatureTypeStyle(typeName, 
                    new Symbolizer[] { textSymbolizer3 }, 14, 5000000));

            
        }

        
        
        return new DefaultMapLayer(fc, style);
    }

    
    
    
    private String addToCollection(FeatureCollection fc, URL shpURL) throws IOException {
        ShapefileDataStore ds = new ShapefileDataStore(shpURL);                
        
        String urlString = shpURL.getFile();
        String shpFile = urlString.substring(urlString.lastIndexOf('/')+1);
        
//        System.out.println(Arrays.deepToString( ds.getTypeNames() ));
        String typeName = ds.getTypeNames()[0];
        
        FeatureCollection collection = ds.getFeatureSource(ds.getTypeNames()[0]).getFeatures().collection();
        FeatureIterator iter = collection.features();
        
        System.out.println("ADDING "+collection.size()+" FEATURES FROM: "+shpFile);
        
        while (iter.hasNext()) {
            fc.add(iter.next());
        }        
        
        return typeName;
    }
    
}
