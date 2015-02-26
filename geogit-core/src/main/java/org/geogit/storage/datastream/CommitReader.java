/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.storage.datastream;

import static org.geogit.storage.datastream.FormatCommon.readCommit;
import static org.geogit.storage.datastream.FormatCommon.requireHeader;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.geogit.api.ObjectId;
import org.geogit.api.RevCommit;
import org.geogit.storage.ObjectReader;

import com.google.common.base.Throwables;

public class CommitReader implements ObjectReader<RevCommit> {
    @Override
    public RevCommit read(ObjectId id, InputStream rawData) throws IllegalArgumentException {
        DataInput in = new DataInputStream(rawData);
        try {
            requireHeader(in, "commit");
            return readCommit(id, in);
        } catch (IOException e) {
            Throwables.propagate(e);
        }
        throw new IllegalStateException(
                "Unexpected state: neither succeeded nor threw exception while trying to read commit "
                        + id);
    }
}
