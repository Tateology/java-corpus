/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.geogit.api.RevFeatureType;
import org.geogit.api.RevObject.TYPE;
import org.geotools.data.DataUtilities;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeatureType;

public abstract class RevFeatureTypeSerializationTest extends Assert {
    private ObjectSerializingFactory factory = getObjectSerializingFactory();
    private String namespace = "http://geoserver.org/test";
    private String typeName = "TestType";
    private String typeSpec = "str:String," + "bool:Boolean," + "byte:java.lang.Byte,"
                + "doub:Double," + "bdec:java.math.BigDecimal," + "flt:Float," + "int:Integer,"
                + "bint:java.math.BigInteger," + "pp:Point:srid=4326," + "lng:java.lang.Long,"
                + "uuid:java.util.UUID";
    private SimpleFeatureType featureType;
    protected abstract ObjectSerializingFactory getObjectSerializingFactory();

    @Before
    public void setUp() throws Exception {
        featureType = DataUtilities.createType(namespace, typeName, typeSpec);
    }
    
    @Test
    public void testSerialization() throws Exception {
        RevFeatureType revFeatureType = RevFeatureType.build(featureType);
        ObjectWriter<RevFeatureType> writer = factory.createObjectWriter(TYPE.FEATURETYPE);
    
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        writer.write(revFeatureType, output);
    
        byte[] data = output.toByteArray();
        assertTrue(data.length > 0);
    
        ObjectReader<RevFeatureType> reader = factory.createObjectReader(TYPE.FEATURETYPE);
        ByteArrayInputStream input = new ByteArrayInputStream(data);
        RevFeatureType rft = reader.read(revFeatureType.getId(), input);
    
        assertNotNull(rft);
        SimpleFeatureType serializedFeatureType = (SimpleFeatureType) rft.type();
        assertEquals(serializedFeatureType.getDescriptors().size(), featureType.getDescriptors()
                .size());
    
        for (int i = 0; i < featureType.getDescriptors().size(); i++) {
            assertEquals(featureType.getDescriptor(i), serializedFeatureType.getDescriptor(i));
        }
    
        assertEquals(featureType.getGeometryDescriptor(),
                serializedFeatureType.getGeometryDescriptor());
        assertEquals(featureType.getCoordinateReferenceSystem(),
                serializedFeatureType.getCoordinateReferenceSystem());
    }
}
