package steve.test;

import gov.noaa.ncdc.wct.export.raster.FeatureRasterizer;
import gov.noaa.ncdc.wct.export.raster.FeatureRasterizerException;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.NoSuchElementException;

import javax.swing.JFrame;

import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.gc.GridCoverage;

import com.sun.media.jai.widget.DisplayJAI;

public class TestFeatureRasterizer {

    
    public static void main(String[] args) {
        try {
            testRasterizer();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    public static void testRasterizer() throws IOException, NoSuchElementException, IllegalAttributeException, FeatureRasterizerException {
        
        
        URL url = new File("H:\\ESRI\\usa\\counties.shp").toURI().toURL();
        ShapefileDataStore ds = new ShapefileDataStore(url);
        FeatureSource fs = ds.getFeatureSource("counties");

        
        FeatureRasterizer rasterizer = new FeatureRasterizer(800, 800, -999.0f);
        rasterizer.setAttName("POP2001");

//        Envelope env = fs.getBounds();
//        Rectangle2D.Double bounds = new Rectangle2D.Double(env.getMinX(), env.getMinY(), env.getWidth(), env.getHeight());
        Rectangle2D.Double bounds = new Rectangle2D.Double(-120.0, 20.0, 40.0, 20.0);
        
        rasterizer.setBounds(bounds);
        
        
        
        FeatureReader fr = fs.getFeatures().reader();
        while (fr.hasNext()) {
            rasterizer.addFeature(fr.next());
        }
        rasterizer.close();
        
        
        GridCoverage gc = new GridCoverage("TEST1", rasterizer.getWritableRaster(), new org.geotools.pt.Envelope(bounds));
        DisplayJAI display = new DisplayJAI(gc.getRenderedImage());
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.getContentPane().add(display);
        frame.pack();
        frame.setVisible(true);
        
        
        
        
        FeatureCollection fc = fs.getFeatures().collection();
        rasterizer.rasterize(fc, "POP2002");
        
        GridCoverage gc2 = new GridCoverage("TEST1", rasterizer.getWritableRaster(), new org.geotools.pt.Envelope(bounds));
        DisplayJAI display2 = new DisplayJAI(gc2.getRenderedImage());
        JFrame frame2 = new JFrame();
        frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame2.getContentPane().add(display2);
        frame2.pack();
        frame2.setVisible(true);


    }
}
