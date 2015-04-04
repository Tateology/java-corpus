package steve.test;

import gov.noaa.ncdc.wct.ResourceUtils;
import gov.noaa.ncdc.wct.WCTConstants;
import gov.noaa.ncdc.wct.export.vector.StreamingShapefileExport;

import java.io.File;
import java.net.URL;

import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class AlterFeatureGeometry {

    public static void main(String[] args) {
        new AlterFeatureGeometry().processCountriesUSA();
//        new AlterFeatureGeometry().processCountries();
//        new AlterFeatureGeometry().processStates();
    }

    
    public void processCountriesUSA() {
        
        try {
            
            URL mapDataURL = new URL(WCTConstants.MAP_DATA_JAR_URL);

//            URL url = ResourceUtils.getInstance().getJarResource(mapDataURL, ResourceUtils.RESOURCE_CACHE_DIR, "/shapefiles/countries-USA-360.shp", null);
            URL url = new File("E:\\work\\shapefiles\\countries-USA.shp").toURI().toURL();
            ShapefileDataStore ds = new ShapefileDataStore(url);
            FeatureSource fs = ds.getFeatureSource("countries-USA");
            FeatureCollection fc = fs.getFeatures().collection();
            
            StreamingShapefileExport shpExport = new StreamingShapefileExport(new File("E:\\work\\shapefiles\\countries-USA-360.shp"));            
            FeatureIterator iter = fc.features();
            int geoIndex = 0;
            while (iter.hasNext()) {
                Feature f = iter.next();
                
//                System.out.println(f.getFeatureType());
                
                Feature f1 = f.getFeatureType().create(f.getAttributes(null), String.valueOf(geoIndex++));
                System.out.println(f1.toString().substring(0, 80));
                shpExport.addFeature(f1);
                
                Geometry geom = f.getDefaultGeometry();
                Coordinate[] coords = geom.getCoordinates();
                for (Coordinate c : coords) {
                    c.x += 360;
                }
                
                Feature f2 = f.getFeatureType().create(f.getAttributes(null), String.valueOf(geoIndex++));
                System.out.println(f2.toString().substring(0, 80));
                shpExport.addFeature(f2);

            }
            shpExport.close();
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    

    
    
    public void processCountries() {
        
        try {
            
            URL mapDataURL = new URL(WCTConstants.MAP_DATA_JAR_URL);

            URL url = ResourceUtils.getInstance().getJarResource(mapDataURL, ResourceUtils.RESOURCE_CACHE_DIR, "/shapefiles/countries-noUSA.shp", null);
            ShapefileDataStore ds = new ShapefileDataStore(url);
            FeatureSource fs = ds.getFeatureSource("countries-noUSA");
            FeatureCollection fc = fs.getFeatures().collection();
            
            StreamingShapefileExport shpExport = new StreamingShapefileExport(new File("E:\\work\\shapefiles\\countries-noUSA-360.shp"));            
            FeatureIterator iter = fc.features();
            int geoIndex = 0;
            while (iter.hasNext()) {
                Feature f = iter.next();
                
//                System.out.println(f.getFeatureType());
                
                Feature f1 = f.getFeatureType().create(f.getAttributes(null), String.valueOf(geoIndex++));
                System.out.println(f1.toString().substring(0, 80));
                shpExport.addFeature(f1);
                
                Geometry geom = f.getDefaultGeometry();
                Coordinate[] coords = geom.getCoordinates();
                for (Coordinate c : coords) {
                    c.x += 360;
                }
                
                Feature f2 = f.getFeatureType().create(f.getAttributes(null), String.valueOf(geoIndex++));
                System.out.println(f2.toString().substring(0, 80));
                shpExport.addFeature(f2);

            }
            shpExport.close();
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    
    
    
    
    
    
    
    public void processStates() {
        
        try {
            
            URL mapDataURL = new URL(WCTConstants.MAP_DATA_JAR_URL);

            URL url = ResourceUtils.getInstance().getJarResource(mapDataURL, ResourceUtils.RESOURCE_CACHE_DIR, "/shapefiles/states.shp", null);
            ShapefileDataStore ds = new ShapefileDataStore(url);
            FeatureSource fs = ds.getFeatureSource("states");
            FeatureCollection fc = fs.getFeatures().collection();
            
            StreamingShapefileExport shpExport = new StreamingShapefileExport(new File("E:\\work\\shapefiles\\states-360.shp"));            
            FeatureIterator iter = fc.features();
            int geoIndex = 0;
            while (iter.hasNext()) {
                Feature f = iter.next();
                
//                System.out.println(f.getFeatureType());
                
                Feature f1 = f.getFeatureType().create(f.getAttributes(null), String.valueOf(geoIndex++));
                System.out.println(f1.toString().substring(0, 80));
                shpExport.addFeature(f1);
                
                Geometry geom = f.getDefaultGeometry();
                Coordinate[] coords = geom.getCoordinates();
                for (Coordinate c : coords) {
                    c.x += 360;
                }
                
                Feature f2 = f.getFeatureType().create(f.getAttributes(null), String.valueOf(geoIndex++));
                System.out.println(f2.toString().substring(0, 80));
                shpExport.addFeature(f2);

            }
            shpExport.close();
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
