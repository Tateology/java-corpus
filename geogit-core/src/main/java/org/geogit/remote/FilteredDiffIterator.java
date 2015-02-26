/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.remote;

import java.util.Iterator;

import org.geogit.api.NodeRef;
import org.geogit.api.ObjectId;
import org.geogit.api.RepositoryFilter;
import org.geogit.api.RevFeatureType;
import org.geogit.api.RevObject;
import org.geogit.api.RevObject.TYPE;
import org.geogit.api.plumbing.RevObjectParse;
import org.geogit.api.plumbing.diff.DiffEntry;
import org.geogit.repository.Repository;

import com.google.common.collect.AbstractIterator;

/**
 * An implementation of a {@link DiffEntry} iterator that filters entries based on a provided
 * {@link RepositoryFilter}.
 */
public class FilteredDiffIterator extends AbstractIterator<DiffEntry> {

    protected boolean filtered = false;

    private Iterator<DiffEntry> source;

    private Repository sourceRepo;

    private RepositoryFilter repoFilter;

    public final boolean wasFiltered() {
        return filtered;
    }

    /**
     * Constructs a new {@code FilteredDiffIterator}.
     * 
     * @param source the unfiltered iterator
     * @param sourceRepo the repository where objects are stored
     * @param repoFilter the filter to use
     */
    public FilteredDiffIterator(Iterator<DiffEntry> source, Repository sourceRepo,
            RepositoryFilter repoFilter) {
        this.source = source;
        this.sourceRepo = sourceRepo;
        this.repoFilter = repoFilter;
        filtered = false;
    }

    /**
     * Compute the next {@link DiffEntry} that matches our {@link RepositoryFilter}.
     */
    protected DiffEntry computeNext() {
        while (source.hasNext()) {
            DiffEntry input = source.next();

            NodeRef oldObject = filter(input.getOldObject());
            NodeRef newObject;
            if (oldObject != null) {
                newObject = input.getNewObject();
                if (newObject != null) {
                    // we are tracking this object, but we still need to process the new object
                    RevObject object = sourceRepo.command(RevObjectParse.class)
                            .setObjectId(newObject.getNode().getObjectId()).call().get();

                    RevObject metadata = null;
                    if (newObject.getMetadataId() != ObjectId.NULL) {
                        metadata = sourceRepo.command(RevObjectParse.class)
                                .setObjectId(newObject.getMetadataId()).call().get();
                    }
                    processObject(object);
                    processObject(metadata);
                }
            } else {
                newObject = filter(input.getNewObject());
            }

            if (oldObject == null && newObject == null) {
                filtered = true;
                continue;
            }

            return new DiffEntry(oldObject, newObject);
        }
        return endOfData();
    }

    private NodeRef filter(NodeRef node) {
        if (node == null) {
            return null;
        }

        RevObject object = sourceRepo.command(RevObjectParse.class)
                .setObjectId(node.getNode().getObjectId()).call().get();

        RevObject metadata = null;
        if (node.getMetadataId() != ObjectId.NULL) {
            metadata = sourceRepo.command(RevObjectParse.class).setObjectId(node.getMetadataId())
                    .call().get();
        }
        if (node.getType() == TYPE.FEATURE) {
            if (trackingObject(object.getId())) {
                // We are already tracking this object, continue to do so
                return node;
            }

            RevFeatureType revFeatureType = (RevFeatureType) metadata;

            if (!repoFilter.filterObject(revFeatureType, node.getParentPath(), object)) {
                return null;
            }

        }
        processObject(object);
        processObject(metadata);
        return node;
    }

    /**
     * An overridable method for hinting that the given object should be tracked, regardless of
     * whether or not it matches the filter.
     * 
     * @param objectId the id of the object
     * @return true if the object should be tracked, false if it should only be tracked if it
     *         matches the filter
     */
    protected boolean trackingObject(ObjectId objectId) {
        return true;
    }

    /**
     * An overridable method to process all objects that match the filter.
     * 
     * @param object the object to process
     */
    protected void processObject(RevObject object) {

    }

}
