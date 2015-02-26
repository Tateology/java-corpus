/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.storage.datastream;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.InputStream;

import org.geogit.api.ObjectId;
import org.geogit.api.RevTag;
import org.geogit.storage.ObjectReader;

import com.google.common.base.Throwables;

public class TagReader implements ObjectReader<RevTag> {
    public RevTag read(ObjectId id, InputStream in) {
        DataInput data = new DataInputStream(in);
        try {
            FormatCommon.requireHeader(data, "tag");
            return FormatCommon.readTag(id, data);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
