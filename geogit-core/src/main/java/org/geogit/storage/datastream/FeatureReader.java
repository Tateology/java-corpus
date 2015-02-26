/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.storage.datastream;

import static org.geogit.storage.datastream.FormatCommon.readFeature;
import static org.geogit.storage.datastream.FormatCommon.requireHeader;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.geogit.api.ObjectId;
import org.geogit.api.RevFeature;
import org.geogit.storage.ObjectReader;

import com.google.common.base.Throwables;

public class FeatureReader implements ObjectReader<RevFeature> {

    @Override
    public RevFeature read(ObjectId id, InputStream rawData) throws IllegalArgumentException {
        DataInput in = new DataInputStream(rawData);
        try {
            requireHeader(in, "feature");
            return readFeature(id, in);
        } catch (IOException e) {
            Throwables.propagate(e);
        }
        throw new IllegalStateException(
                "Didn't expect to reach end of FeatureReader.read(); We should have returned or thrown an error before this point.");
    }

}
