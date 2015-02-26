/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.api.plumbing;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.geogit.api.AbstractGeoGitOp;
import org.geogit.api.ObjectId;
import org.geogit.api.Ref;
import org.geogit.api.RevCommit;
import org.geogit.api.porcelain.BranchListOp;
import org.geogit.api.porcelain.LogOp;
import org.geogit.repository.Repository;
import org.geogit.storage.GraphDatabase;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

/**
 * Rebuilds the {@link GraphDatabase} and returns a list of {@link ObjectId}s that were found to be
 * missing or incomplete.
 */
public class RebuildGraphOp extends AbstractGeoGitOp<ImmutableList<ObjectId>> {

    private Repository repository;

    /**
     * Constructs a new {@code RebuildGraphOp} with the provided {@link Repository}.
     * 
     * @param repository the repository
     */
    @Inject
    public RebuildGraphOp(Repository repository) {
        this.repository = repository;
    }

    /**
     * Executes the {@code RebuildGraphOp} operation.
     * 
     * @return a list of {@link ObjectId}s that were found to be missing or incomplete
     */
    @Override
    public ImmutableList<ObjectId> call() {
        Preconditions.checkState(!repository.isSparse(),
                "Cannot rebuild the graph of a sparse repository.");

        List<ObjectId> updated = new LinkedList<ObjectId>();
        ImmutableList<Ref> branches = command(BranchListOp.class).setLocal(true).setRemotes(true)
                .call();

        GraphDatabase graphDb = repository.getGraphDatabase();
        
        for (Ref ref : branches) {
            Iterator<RevCommit> commits = command(LogOp.class).setUntil(ref.getObjectId()).call();
            while (commits.hasNext()) {
                RevCommit next = commits.next();
                if (graphDb.put(next.getId(), next.getParentIds())) {
                    updated.add(next.getId());
                }
            }
        }

        return ImmutableList.copyOf(updated);
    }
}
