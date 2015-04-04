package gov.noaa.ncdc.wct.decoders.nexrad;


import gov.noaa.ncdc.nexradiv.RangeRings;
import gov.noaa.ncdc.wct.export.WCTExportException;
import gov.noaa.ncdc.wct.export.vector.WCTVectorExport;

import java.io.File;
import java.io.IOException;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;

public class TestRangeRings {

    @Test
    public void testRangeBinFeatures() throws IOException, IllegalAttributeException, WCTExportException, SchemaException {

        
            FeatureCollection fc = FeatureCollections.newCollection();

            RadarHashtables nxhash = RadarHashtables.getSharedInstance();
            
            RangeRings.getRangeBinFeatures(
//                    new Coordinate(-95.0, 35.0), 
                    new Coordinate(nxhash.getLon("KCLE"), nxhash.getLat("KCLE")),
                    0.5, 
                    330.5, 
                    1.0, 
                    RangeRings.KM, 
                    0.5, 
                    360.5, 
                    1.0, 
                    fc,
                    RangeRings.DEFAULT_SEGMENT_AZIMUTH);

            WCTVectorExport export = new WCTVectorExport();
            export.saveShapefile(new File("E:\\work\\spec-presentation-data\\kcle-rangebins-330.shp"), fc, RangeRings.getRangeRingFeatureType());

        
    }
    

}
