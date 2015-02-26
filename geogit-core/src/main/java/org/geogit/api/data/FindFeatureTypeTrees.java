/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.api.data;

import java.util.Iterator;
import java.util.List;

import org.geogit.api.AbstractGeoGitOp;
import org.geogit.api.NodeRef;
import org.geogit.api.ObjectId;
import org.geogit.api.plumbing.LsTreeOp;
import org.geogit.api.plumbing.RevParse;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;

/**
 * 
 */
public class FindFeatureTypeTrees extends AbstractGeoGitOp<List<NodeRef>> {

    private String refSpec;

    /**
     * @param refSpec a ref spec, as supported by {@link RevParse}, that resolves to the root tree
     *        that's to be inspected for leaf trees with metadata ids set; most common use being of
     *        the type {@code <head name>[:<path>]}
     */
    public FindFeatureTypeTrees setRootTreeRef(String refSpec) {
        this.refSpec = refSpec;
        return this;
    }

    @Override
    public List<NodeRef> call() {
        Preconditions.checkNotNull(refSpec, "refSpec was not provided");
        Iterator<NodeRef> allTrees;
        try {
            allTrees = commandLocator.command(LsTreeOp.class).setReference(refSpec)
                    .setStrategy(LsTreeOp.Strategy.DEPTHFIRST_ONLY_TREES).call();
        } catch (IllegalArgumentException noWorkHead) {
            return ImmutableList.of();
        }
        Iterator<NodeRef> typeTrees = Iterators.filter(allTrees, new Predicate<NodeRef>() {
            @Override
            public boolean apply(NodeRef input) {
                ObjectId metadataId = input.getMetadataId();
                return !metadataId.isNull();
            }
        });

        return ImmutableList.copyOf(typeTrees);
    }

}
