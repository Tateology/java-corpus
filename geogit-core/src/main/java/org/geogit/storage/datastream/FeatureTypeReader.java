/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.storage.datastream;

import static org.geogit.storage.datastream.FormatCommon.readFeatureType;
import static org.geogit.storage.datastream.FormatCommon.requireHeader;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.geogit.api.ObjectId;
import org.geogit.api.RevFeatureType;
import org.geogit.storage.ObjectReader;

public class FeatureTypeReader implements ObjectReader<RevFeatureType> {
    @Override
    public RevFeatureType read(ObjectId id, InputStream rawData) throws IllegalArgumentException {
        DataInput in = new DataInputStream(rawData);
        try {
            requireHeader(in, "featuretype");
            return readFeatureType(id, in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
