/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.storage.datastream;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.OutputStream;

import org.geogit.api.RevTag;
import org.geogit.storage.ObjectWriter;

import com.google.common.base.Throwables;

public class TagWriter implements ObjectWriter<RevTag> {
    public void write(RevTag tag, OutputStream out) {
        final DataOutput data = new DataOutputStream(out);
        try {
            FormatCommon.writeHeader(data, "tag");
            FormatCommon.writeTag(tag, data);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
