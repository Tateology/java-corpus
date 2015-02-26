/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.storage.datastream;

import static org.geogit.storage.datastream.FormatCommon.NUL;
import static org.geogit.storage.datastream.FormatCommon.readCommit;
import static org.geogit.storage.datastream.FormatCommon.readFeature;
import static org.geogit.storage.datastream.FormatCommon.readFeatureType;
import static org.geogit.storage.datastream.FormatCommon.readTag;
import static org.geogit.storage.datastream.FormatCommon.readToMarker;
import static org.geogit.storage.datastream.FormatCommon.readTree;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.geogit.api.ObjectId;
import org.geogit.api.RevObject;

public class ObjectReader implements org.geogit.storage.ObjectReader<RevObject> {
    @Override
    public RevObject read(ObjectId id, InputStream rawData) throws IllegalArgumentException {
        DataInput in = new DataInputStream(rawData);
        try {
            return readData(id, in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private RevObject readData(ObjectId id, DataInput in) throws IOException {
        String header = readToMarker(in, NUL);
        if ("commit".equals(header))
            return readCommit(id, in);
        else if ("tree".equals(header))
            return readTree(id, in);
        else if ("feature".equals(header))
            return readFeature(id, in);
        else if ("featuretype".equals(header))
            return readFeatureType(id, in);
        else if ("tag".equals(header))
            return readTag(id, in);
        else
            throw new IllegalArgumentException("Unrecognized object header: " + header);
    }
}
