/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.api.plumbing;

import org.geogit.api.AbstractGeoGitOp;
import org.geogit.api.ObjectId;
import org.geogit.api.RevObject;
import org.geogit.api.RevObject.TYPE;
import org.geogit.storage.StagingDatabase;

import com.google.inject.Inject;

/**
 * Gets the object type of the object that matches the given {@link ObjectId}.
 */
public class ResolveObjectType extends AbstractGeoGitOp<RevObject.TYPE> {

    private StagingDatabase indexDb;

    private ObjectId oid;

    /**
     * Constructs a new instance of {@code ResolveObjectType} using the specified parameters.
     * 
     * @param indexDb the staging database
     */
    @Inject
    public ResolveObjectType(StagingDatabase indexDb) {
        this.indexDb = indexDb;
    }

    /**
     * @param oid the {@link ObjectId object id} of the object to check
     * @return {@code this}
     */
    public ResolveObjectType setObjectId(ObjectId oid) {
        this.oid = oid;
        return this;
    }

    /**
     * Executes the command.
     * 
     * @return the type of the object specified by the object id.
     * @throws IllegalArgumentException if the object doesn't exist
     */
    @Override
    public TYPE call() throws IllegalArgumentException {
        RevObject o = indexDb.get(oid);
        return o.getType();
    }
}
