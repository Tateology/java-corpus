/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.api.plumbing.diff;

import static com.google.common.base.Preconditions.checkState;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.geogit.api.Node;
import org.geogit.api.NodeRef;
import org.geogit.api.ObjectId;
import org.geogit.api.RevObject.TYPE;
import org.geogit.api.RevTree;
import org.geogit.repository.DepthSearch;
import org.geogit.storage.ObjectDatabase;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

/**
 * Composes an {@link Iterator} of {@link DiffEntry} out of two {@link RevTree}ss
 */
public class DiffTreeWalk {

    @Nonnull
    private final RevTree fromRootTree;

    @Nonnull
    private final RevTree toRootTree;

    @Nonnull
    private ObjectDatabase objectDb;

    private final List<String> pathFilter = Lists.newLinkedList();

    private boolean reportTrees;

    private boolean recursive;

    public DiffTreeWalk(final ObjectDatabase db, final RevTree fromRootTree,
            final RevTree toRootTree) {
        Preconditions.checkNotNull(db);
        Preconditions.checkNotNull(fromRootTree);
        Preconditions.checkNotNull(toRootTree);
        this.objectDb = db;
        this.fromRootTree = fromRootTree;
        this.toRootTree = toRootTree;
        this.recursive = true;
    }

    public void addFilter(final String pathPrefix) {
        if (pathPrefix != null && !pathPrefix.isEmpty()) {
            this.pathFilter.add(pathPrefix);
        }
    }

    public void setFilter(@Nullable List<String> pathFitlers) {
        this.pathFilter.clear();
        if (pathFitlers != null) {
            this.pathFilter.addAll(pathFitlers);
        }
    }

    /**
     * @param reportTrees tells the diff tree walk whether to report a {@link DiffEntry} for each
     *        changed tree or not, defaults to {@code false}
     */
    public void setReportTrees(boolean reportTrees) {
        this.reportTrees = reportTrees;
    }

    /**
     * Sets whether to return differences recursively ({@code true} or just for direct children (
     * {@code false}. Defaults to {@code true}
     */
    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public Iterator<DiffEntry> get() {

        RevTree oldTree = this.fromRootTree;
        RevTree newTree = this.toRootTree;

        Optional<NodeRef> oldObjectRef = getFilteredObjectRef(fromRootTree);
        Optional<NodeRef> newObjectRef = getFilteredObjectRef(toRootTree);
        boolean pathFiltering = !pathFilter.isEmpty();
        if (pathFiltering && pathFilter.size() == 1) {
            if (Objects.equal(oldObjectRef, newObjectRef)) {
                // filter didn't match anything
                return Iterators.emptyIterator();
            }

            TYPE oldObjectType = oldObjectRef.isPresent() ? oldObjectRef.get().getType() : null;
            TYPE newObjectType = newObjectRef.isPresent() ? newObjectRef.get().getType() : null;

            checkState(oldObjectType == null || newObjectType == null
                    || Objects.equal(oldObjectType, newObjectType));

            final TYPE type = oldObjectType == null ? newObjectType : oldObjectType;
            switch (type) {
            case FEATURE:
                return Iterators.singletonIterator(new DiffEntry(oldObjectRef.orNull(),
                        newObjectRef.orNull()));
            case TREE:
                if (oldObjectRef.isPresent()) {
                    oldTree = objectDb.getTree(oldObjectRef.get().objectId());
                } else {
                    oldTree = null;
                }
                if (newObjectRef.isPresent()) {
                    newTree = objectDb.getTree(newObjectRef.get().objectId());
                } else {
                    newTree = null;
                }
                break;
            default:
                throw new IllegalStateException(
                        "Only FEATURE or TREE objects expected at this stage: " + type);
            }

        }

        NodeRef oldRef, newRef;

        oldRef = oldObjectRef.orNull();
        newRef = newObjectRef.orNull();

        // TODO: pass pathFilter to TreeDiffEntryIterator so it ignores inner trees where the path
        // is guaranteed not to be present
        Iterator<DiffEntry> iterator = new TreeDiffEntryIterator(oldRef, newRef, oldTree, newTree,
                reportTrees, recursive, objectDb);

        // boolean comparingTree = (oldRef == null ? newRef : oldRef).getType().equals(TYPE.TREE);
        // if (reportTrees && comparingTree && !Objects.equal(oldRef, newRef)) {
        // DiffEntry self = new DiffEntry(oldRef, newRef);
        // iterator = Iterators.concat(Iterators.singletonIterator(self), iterator);
        // }

        if (pathFiltering) {
            iterator = Iterators.filter(iterator, new Predicate<DiffEntry>() {
                @Override
                public boolean apply(@Nullable DiffEntry input) {
                    String oldPath = input.oldPath();
                    String newPath = input.newPath();
                    for (String path : pathFilter) {
                        boolean apply = (oldPath != null && oldPath.startsWith(path))
                                || (newPath != null && newPath.startsWith(path));
                        if (apply) {
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
        return iterator;
    }

    private Optional<NodeRef> getFilteredObjectRef(RevTree tree) {
        if (pathFilter.size() != 1) {
            Node node = Node.create("", tree.getId(), ObjectId.NULL, TYPE.TREE, null);
            String parentPath = "";
            ObjectId metadataId = ObjectId.NULL;
            NodeRef rootRef = new NodeRef(node, parentPath, metadataId);
            return Optional.of(rootRef);
        }

        final DepthSearch search = new DepthSearch(objectDb);
        Optional<NodeRef> ref = search.find(tree, pathFilter.get(0));
        return ref;

    }

}
