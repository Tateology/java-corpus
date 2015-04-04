package gov.noaa.ncdc.wct.export;

import gov.noaa.ncdc.wct.decoders.StreamingProcessException;
import gov.noaa.ncdc.wct.export.vector.StreamingCsvExport;

import java.io.File;
import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
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
import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class StreamingCsvExportTest {

	@Test
	public void testCsvExportFormat() throws IOException, IllegalAttributeException, FactoryConfigurationError, SchemaException, StreamingProcessException {
		

	    FeatureCollection fc = FeatureCollections.newCollection();
        AttributeType geom = AttributeTypeFactory.newAttributeType("geom", Point.class);
        AttributeType col1 = AttributeTypeFactory.newAttributeType("col1", String.class, true, 5);
        AttributeType col2 = AttributeTypeFactory.newAttributeType("col2", String.class, true, 5);
        AttributeType[] attTypes = { geom, col1, col2 };
        FeatureType schema = FeatureTypeFactory.newFeatureType(attTypes, "Test Data");
        GeometryFactory geoFactory = new GeometryFactory();
        Feature feature = schema.create(new Object[] { geoFactory.createPoint(new Coordinate(-90, 35)), "abcd", "efg" });
		
        File tmpFile = File.createTempFile("wcttest", ".csv");
		StreamingCsvExport export = new StreamingCsvExport(tmpFile);
		export.addFeature(feature);
		export.close();
		
		List<String> data = FileUtils.readLines(tmpFile);
		System.out.println(data);
		tmpFile.delete();
		
		
		Assert.assertEquals("col1,col2,latitude,longitude", data.get(0));
		Assert.assertEquals("abcd,efg,35.0000,-90.0000", data.get(1));
		
	}
}
