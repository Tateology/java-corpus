/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.api.plumbing;

import static org.geogit.api.RevObject.TYPE.COMMIT;
import static org.geogit.api.RevObject.TYPE.FEATURE;
import static org.geogit.api.RevObject.TYPE.FEATURETYPE;
import static org.geogit.api.RevObject.TYPE.TAG;
import static org.geogit.api.RevObject.TYPE.TREE;

import org.geogit.api.AbstractGeoGitOp;
import org.geogit.api.ObjectId;
import org.geogit.api.RevObject;

import com.google.common.base.Preconditions;
import com.google.common.hash.Funnel;
import com.google.common.hash.Hasher;

/**
 * Hashes a RevObject and returns the ObjectId.
 * 
 * @see RevObject
 * @see ObjectId#HASH_FUNCTION
 */
public class HashObject extends AbstractGeoGitOp<ObjectId> {

    @SuppressWarnings("unchecked")
    private static final Funnel<? extends RevObject>[] FUNNELS = new Funnel[RevObject.TYPE.values().length];
    static {
        FUNNELS[COMMIT.value()] = HashObjectFunnels.commitFunnel();
        FUNNELS[TREE.value()] = HashObjectFunnels.treeFunnel();
        FUNNELS[FEATURE.value()] = HashObjectFunnels.featureFunnel();
        FUNNELS[TAG.value()] = HashObjectFunnels.tagFunnel();
        FUNNELS[FEATURETYPE.value()] = HashObjectFunnels.featureTypeFunnel();
    }

    private RevObject object;

    /**
     * @param object {@link RevObject} to hash.
     * @return {@code this}
     */
    public HashObject setObject(RevObject object) {
        this.object = object;
        return this;
    }

    /**
     * Hashes a RevObject using a SHA1 hasher.
     * 
     * @return a new ObjectId created from the hash of the RevObject.
     */
    @Override
    public ObjectId call() {
        Preconditions.checkState(object != null, "Object has not been set.");

        final Hasher hasher = ObjectId.HASH_FUNCTION.newHasher();
        @SuppressWarnings("unchecked")
        final Funnel<RevObject> funnel = (Funnel<RevObject>) FUNNELS[object.getType().value()];
        funnel.funnel(object, hasher);
        final byte[] rawKey = hasher.hash().asBytes();
        final ObjectId id = ObjectId.createNoClone(rawKey);

        return id;
    }

}
