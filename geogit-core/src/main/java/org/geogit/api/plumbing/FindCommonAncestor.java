/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.api.plumbing;

import org.geogit.api.AbstractGeoGitOp;
import org.geogit.api.ObjectId;
import org.geogit.api.RevCommit;
import org.geogit.repository.Repository;
import org.geogit.storage.GraphDatabase;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Finds the common {@link RevCommit commit} ancestor of two commits.
 */
public class FindCommonAncestor extends AbstractGeoGitOp<Optional<RevCommit>> {

    private RevCommit left;

    private RevCommit right;

    private Repository repository;

    private GraphDatabase graphDb;

    /**
     * Construct a new {@code FindCommonAncestor} using the specified {@link Repository}.
     * 
     * @param repository the repository
     */
    @Inject
    public FindCommonAncestor(Repository repository, GraphDatabase graphDb) {
        this.repository = repository;
        this.graphDb = graphDb;
    }

    /**
     * @param left the left {@link RevCommit}
     */
    public FindCommonAncestor setLeft(RevCommit left) {
        this.left = left;
        return this;
    }

    /**
     * @param right the right {@link RevCommit}
     */
    public FindCommonAncestor setRight(RevCommit right) {
        this.right = right;
        return this;
    }

    /**
     * Finds the common {@link RevCommit commit} ancestor of two commits.
     * 
     * @return an {@link Optional} of the ancestor commit, or {@link Optional#absent()} if no common
     *         ancestor was found
     */
    @Override
    public Optional<RevCommit> call() {
        Preconditions.checkState(left != null, "Left commit has not been set.");
        Preconditions.checkState(right != null, "Right commit has not been set.");

        if (left.getId().equals(right.getId())) {
            // They are the same commit
            return Optional.of(left);
        }

        getProgressListener().started();

        Optional<ObjectId> ancestor = graphDb.findLowestCommonAncestor(left.getId(), right.getId());

        Optional<RevCommit> ancestorCommit = Optional.absent();
        if (ancestor.isPresent()) {
            ancestorCommit = Optional.of(repository.getCommit(ancestor.get()));
        }

        getProgressListener().complete();

        return ancestorCommit;
    }
}
