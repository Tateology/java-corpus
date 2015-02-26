/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.remote;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;

import org.geogit.api.ObjectId;
import org.geogit.api.RevObject;
import org.geogit.api.plumbing.diff.DiffEntry;

/**
 * Iterates over all changes from a {@link BinaryPackedChanges} object.
 */
public class HttpFilteredDiffIterator extends FilteredDiffIterator {

    private Queue<DiffEntry> objects;

    /**
     * Constructs a new {@code HttpFilteredDiffIterator}.
     * 
     * @param in the input stream
     * @param changes the object that will be used to ingest the stream
     */
    public HttpFilteredDiffIterator(InputStream in, BinaryPackedChanges changes) {
        super(null, null, null);
        objects = new LinkedList<DiffEntry>();
        BinaryPackedChanges.Callback<Void> callback = new BinaryPackedChanges.Callback<Void>() {
            @Override
            public Void callback(DiffEntry object, Void state) {
                objects.add(object);
                return null;
            }
        };
        changes.ingest(in, callback);
        filtered = changes.wasFiltered();
    }

    /**
     * Iterate to the next change.
     * 
     * @return the next {@code DiffEntry}
     */
    protected DiffEntry computeNext() {
        if (objects.peek() != null) {
            return objects.poll();
        }
        return endOfData();
    }

    @Override
    protected boolean trackingObject(ObjectId objectId) {
        return false;
    }

    @Override
    protected void processObject(RevObject object) {
    }
}
