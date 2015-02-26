/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.api.plumbing;

import org.geogit.api.AbstractGeoGitOp;
import org.geogit.storage.DeduplicationService;
import org.geogit.storage.Deduplicator;

import com.google.inject.Inject;

public class CreateDeduplicator extends AbstractGeoGitOp<Deduplicator> {
    private final DeduplicationService deduplicationService;
    
    @Inject
    public CreateDeduplicator(DeduplicationService deduplicationService) {
        this.deduplicationService = deduplicationService;
    }

    @Override
    public Deduplicator call() {
        return deduplicationService.createDeduplicator();
    }
}
