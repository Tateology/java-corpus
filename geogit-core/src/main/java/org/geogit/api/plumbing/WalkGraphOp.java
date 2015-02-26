/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.api.plumbing;

import java.util.Iterator;

import org.geogit.api.AbstractGeoGitOp;
import org.geogit.api.ObjectId;
import org.geogit.api.RevObject;
import org.geogit.repository.PostOrderIterator;
import org.geogit.storage.Deduplicator;
import org.geogit.storage.ObjectDatabase;

import com.google.common.base.Optional;
import com.google.common.collect.Iterators;
import com.google.inject.Inject;

public class WalkGraphOp extends AbstractGeoGitOp<Iterator<RevObject>> {

    private String reference;

    private ObjectDatabase odb;
    
    private Deduplicator deduplicator;

    public WalkGraphOp setReference(final String reference) {
        this.reference = reference;
        return this;
    }
    
    public WalkGraphOp setDeduplicator(final Deduplicator deduplicator) {
    	this.deduplicator = deduplicator;
    	return this;
    }

    @Inject
    public WalkGraphOp(ObjectDatabase odb) {
        this.odb = odb;
    }

    @Override
    public Iterator<RevObject> call() {
        Optional<ObjectId> ref = command(RevParse.class).setRefSpec(reference).call();
        if (!ref.isPresent())
            return Iterators.emptyIterator();
        if (deduplicator == null) throw new IllegalStateException("The caller must provide a deduplicator!");
        return PostOrderIterator.all(ref.get(), odb, deduplicator);
    }
}
