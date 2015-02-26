/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.storage.datastream;

import static org.geogit.storage.datastream.FormatCommon.readTree;
import static org.geogit.storage.datastream.FormatCommon.requireHeader;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.geogit.api.ObjectId;
import org.geogit.api.RevTree;
import org.geogit.storage.ObjectReader;

import com.google.common.base.Throwables;

public class TreeReader implements ObjectReader<RevTree> {

    @Override
    public RevTree read(ObjectId id, InputStream rawData) throws IllegalArgumentException {
        DataInput in = new DataInputStream(rawData);
        try {
            requireHeader(in, "tree");
            return readTree(id, in);
        } catch (IOException e) {
            Throwables.propagate(e);
        }
        throw new IllegalStateException(
                "Unexpected state: neither succeeded nor threw exception while trying to read commit "
                        + id);
    }

}
