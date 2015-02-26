/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.api.porcelain;

import java.util.Iterator;

import javax.annotation.Nullable;

import org.geogit.api.AbstractGeoGitOp;
import org.geogit.api.NodeRef;
import org.geogit.api.RevObject.TYPE;
import org.geogit.api.plumbing.DiffWorkTree;
import org.geogit.api.plumbing.FindTreeChild;
import org.geogit.api.plumbing.diff.DiffEntry;
import org.geogit.api.plumbing.diff.DiffEntry.ChangeType;
import org.geogit.di.CanRunDuringConflict;
import org.geogit.repository.WorkingTree;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.inject.Inject;

/**
 * Removes untracked features from the working tree
 * 
 */
@CanRunDuringConflict
public class CleanOp extends AbstractGeoGitOp<WorkingTree> {

    private String path;

    @Inject
    public CleanOp() {
    }

    /**
     * @see java.util.concurrent.Callable#call()
     */
    public WorkingTree call() {

        if (path != null) {
            // check that is a valid path
            NodeRef.checkValidPath(path);

            Optional<NodeRef> ref = command(FindTreeChild.class).setParent(getWorkTree().getTree())
                    .setChildPath(path).setIndex(true).call();

            Preconditions.checkArgument(ref.isPresent(), "pathspec '%s' did not match any tree",
                    path);
            Preconditions.checkArgument(ref.get().getType() == TYPE.TREE,
                    "pathspec '%s' did not resolve to a tree", path);
        }

        final Iterator<DiffEntry> unstaged = command(DiffWorkTree.class).setFilter(path).call();
        final Iterator<DiffEntry> added = Iterators.filter(unstaged, new Predicate<DiffEntry>() {

            @Override
            public boolean apply(@Nullable DiffEntry input) {
                return input.changeType().equals(ChangeType.ADDED);
            }
        });
        Iterator<String> addedPaths = Iterators.transform(added, new Function<DiffEntry, String>() {

            @Override
            public String apply(DiffEntry input) {
                return input.newPath();
            }
        });

        getWorkTree().delete(addedPaths);

        return getWorkTree();

    }

    /**
     * @param path a path to clean
     * @return {@code this}
     */
    public CleanOp setPath(final String path) {
        this.path = path;
        return this;
    }

}
