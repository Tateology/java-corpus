/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.api.plumbing;

import static com.google.common.base.Preconditions.checkNotNull;

import org.geogit.api.AbstractGeoGitOp;
import org.geogit.api.Node;
import org.geogit.api.NodeRef;
import org.geogit.api.ObjectId;
import org.geogit.api.Ref;
import org.geogit.api.RevTree;
import org.geogit.repository.DepthSearch;
import org.geogit.storage.ObjectDatabase;
import org.geogit.storage.StagingDatabase;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.inject.Inject;

/**
 * Finds a {@link Node} by searching the given {@link RevTree} for the given path, returns the
 * {@link NodeRef} that wraps it.
 * 
 * @see DepthSearch
 * @see ResolveTreeish
 * @see RevObjectParse
 */
public class FindTreeChild extends AbstractGeoGitOp<Optional<NodeRef>> {

    private Supplier<RevTree> parent;

    private String childPath;

    private String parentPath;

    private boolean indexDb;

    private ObjectDatabase index;

    private ObjectDatabase odb;

    /**
     * Constructs a new {@code FindTreeChild} instance with the specified parameters.
     * 
     * @param odb the repository object database
     * @param index the staging database
     */
    @Inject
    public FindTreeChild(ObjectDatabase odb, StagingDatabase index) {
        this.odb = odb;
        this.index = index;
    }

    public FindTreeChild(ObjectDatabase odb) {
        this.odb = odb;
        this.index = odb;
    }

    /**
     * @param indexDb whether to look up in the {@link StagingDatabase index db} ({@code true}) or
     *        on the repository's {@link ObjectDatabase object database} (default)
     * @return {@code this}
     */
    public FindTreeChild setIndex(final boolean indexDb) {
        this.indexDb = indexDb;
        return this;
    }

    /**
     * @param tree a supplier that resolves to the tree where to start the search for the nested
     *        child. If not supplied the current HEAD tree is assumed.
     * @return {@code this}
     */
    public FindTreeChild setParent(Supplier<RevTree> tree) {
        this.parent = tree;
        return this;
    }

    /**
     * @param tree the tree to search for the nested child
     * @return {@code this}
     */
    public FindTreeChild setParent(RevTree tree) {
        this.parent = Suppliers.ofInstance(tree);
        return this;
    }

    /**
     * @param parentPath the parent's path. If not given parent is assumed to be a root tree.
     * @return {@code this}
     */
    public FindTreeChild setParentPath(String parentPath) {
        this.parentPath = parentPath;
        return this;
    }

    /**
     * @param childPath the full path of the subtree to look for
     * @return {@code this}
     */
    public FindTreeChild setChildPath(String childPath) {
        this.childPath = childPath;
        return this;
    }

    /**
     * Executes the command.
     * 
     * @return an {@code Optional} that contains the Node if it was found, or
     *         {@link Optional#absent()} if it wasn't
     */
    @Override
    public Optional<NodeRef> call() {
        checkNotNull(childPath, "childPath");
        final RevTree tree;
        if (parent == null) {
            ObjectId rootTreeId = command(ResolveTreeish.class).setTreeish(Ref.HEAD).call().get();
            if (rootTreeId.isNull()) {
                return Optional.absent();
            }
            tree = command(RevObjectParse.class).setObjectId(rootTreeId).call(RevTree.class).get();
        } else {
            tree = parent.get();
        }
        final String path = childPath;
        final String parentPath = this.parentPath == null ? "" : this.parentPath;
        final ObjectDatabase target = indexDb ? index : odb;

        DepthSearch depthSearch = new DepthSearch(target);
        Optional<NodeRef> childRef = depthSearch.find(tree, parentPath, path);
        return childRef;

    }

}
