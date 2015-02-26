/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.api.plumbing;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.geogit.api.AbstractGeoGitOp;
import org.geogit.api.Node;
import org.geogit.api.NodeRef;
import org.geogit.api.ObjectId;
import org.geogit.api.Ref;
import org.geogit.api.RevObject.TYPE;
import org.geogit.api.RevTree;
import org.geogit.api.RevTreeBuilder;
import org.geogit.api.plumbing.diff.DiffEntry;
import org.geogit.api.plumbing.diff.DiffEntry.ChangeType;
import org.geogit.storage.ObjectDatabase;
import org.opengis.util.ProgressListener;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

/**
 * Creates a new root tree in the {@link ObjectDatabase object database} from the current index,
 * based on the current {@code HEAD} and returns the new root tree id.
 * <p>
 * This command creates a tree object using the current index. The id of the new root tree object is
 * returned. No {@link Ref ref} is updated as a result of this operation, so the resulting root tree
 * is "orphan". It's up to the calling code to update any needed reference.
 * 
 * The index must be in a fully merged state.
 * 
 * Conceptually, write-tree sync()s the current index contents into a set of tree objects on the
 * {@link ObjectDatabase}. In order to have that match what is actually in your directory right now,
 * you need to have done a {@link UpdateIndex} phase before you did the write-tree.
 * 
 * @see FindOrCreateSubtree
 * @see DeepMove
 * @see ResolveTreeish
 * @see CreateTree
 * @see RevObjectParse
 */
public class WriteTree extends AbstractGeoGitOp<ObjectId> {

    private ObjectDatabase repositoryDatabase;

    private Supplier<RevTree> oldRoot;

    private final List<String> pathFilters = Lists.newLinkedList();

    private Supplier<Iterator<DiffEntry>> diffSupplier = null;

    /**
     * Creates a new {@code WriteTree} operation using the specified parameters.
     * 
     * @param repositoryDatabase the object database to use
     */
    @Inject
    public WriteTree(ObjectDatabase repositoryDatabase) {
        this.repositoryDatabase = repositoryDatabase;
    }

    /**
     * @param oldRoot a supplier for the old root tree
     * @return {@code this}
     */
    public WriteTree setOldRoot(Supplier<RevTree> oldRoot) {
        this.oldRoot = oldRoot;
        return this;
    }

    /**
     * 
     * @param pathFilter the pathfilter to pass on to the index
     * @return {@code this}
     */
    public WriteTree addPathFilter(String pathFilter) {
        if (pathFilter != null) {
            this.pathFilters.add(pathFilter);
        }
        return this;
    }

    public WriteTree setPathFilter(@Nullable List<String> pathFilters) {
        this.pathFilters.clear();
        if (pathFilters != null) {
            this.pathFilters.addAll(pathFilters);
        }
        return this;
    }

    public WriteTree setDiffSupplier(@Nullable Supplier<Iterator<DiffEntry>> diffSupplier) {
        this.diffSupplier = diffSupplier;
        return this;
    }

    /**
     * Executes the write tree operation.
     * 
     * @return the new root tree id, the current HEAD tree id if there are no differences between
     *         the index and the HEAD, or {@code null} if the operation has been cancelled (as
     *         indicated by the {@link #getProgressListener() progress listener}.
     */
    @Override
    public ObjectId call() {
        final ProgressListener progress = getProgressListener();

        final RevTree oldRootTree = resolveRootTree();

        Iterator<DiffEntry> diffs = null;
        long numChanges = 0;
        if (diffSupplier == null) {
            diffs = getIndex().getStaged(pathFilters);
            numChanges = getIndex().countStaged(pathFilters).getCount();
        } else {
            diffs = diffSupplier.get();
        }

        if (!diffs.hasNext()) {
            return oldRootTree.getId();
        }
        if (progress.isCanceled()) {
            return null;
        }

        Map<String, RevTreeBuilder> repositoryChangedTrees = Maps.newHashMap();
        Map<String, NodeRef> indexChangedTrees = Maps.newHashMap();
        Map<String, ObjectId> changedTreesMetadataId = Maps.newHashMap();
        Set<String> deletedTrees = Sets.newHashSet();
        NodeRef ref;
        int i = 0;
        while (diffs.hasNext()) {
            if (numChanges != 0) {
                progress.progress((float) (++i * 100) / numChanges);
            }
            if (progress.isCanceled()) {
                return null;
            }

            DiffEntry diff = diffs.next();
            // ignore the root entry
            if (NodeRef.ROOT.equals(diff.newName()) || NodeRef.ROOT.equals(diff.oldName())) {
                continue;
            }
            ref = diff.getNewObject();

            if (ref == null) {
                ref = diff.getOldObject();
            }

            final String parentPath = ref.getParentPath();
            final boolean isDelete = ChangeType.REMOVED.equals(diff.changeType());
            final TYPE type = ref.getType();
            if (isDelete && deletedTrees.contains(parentPath)) {
                // this is to avoid re-creating the parentTree for a feature delete after its parent
                // tree delete entry was processed
                continue;
            }
            RevTreeBuilder parentTree = resolveTargetTree(oldRootTree, parentPath,
                    repositoryChangedTrees, changedTreesMetadataId, ObjectId.NULL);
            if (type == TYPE.TREE && !isDelete) {
                // cache the tree
                resolveTargetTree(oldRootTree, ref.name(), repositoryChangedTrees,
                        changedTreesMetadataId, ref.getMetadataId());
            }

            resolveSourceTreeRef(parentPath, indexChangedTrees, changedTreesMetadataId);

            Preconditions.checkState(parentTree != null);

            if (isDelete) {
                String oldName = diff.getOldObject().getNode().getName();
                parentTree.remove(oldName);
                if (TYPE.TREE.equals(type)) {
                    deletedTrees.add(ref.path());
                }
            } else {
                if (ref.getType().equals(TYPE.TREE)) {
                    RevTree tree = getIndex().getDatabase().getTree(ref.objectId());
                    if (ref.getMetadataId() != null && !ref.getMetadataId().equals(ObjectId.NULL)) {
                        repositoryDatabase.put(getIndex().getDatabase().getFeatureType(
                                ref.getMetadataId()));
                    }
                    if (tree.isEmpty()) {
                        repositoryDatabase.put(tree);

                    } else {
                        continue;
                    }
                } else {
                    deepMove(ref.getNode());
                }
                parentTree.put(ref.getNode());
            }
        }

        if (progress.isCanceled()) {
            return null;
        }

        // now write back all changed trees
        ObjectId newTargetRootId = oldRootTree.getId();
        RevTreeBuilder directRootEntries = repositoryChangedTrees.remove(NodeRef.ROOT);
        if (directRootEntries != null) {
            RevTree newRoot = directRootEntries.build();
            repositoryDatabase.put(newRoot);
            newTargetRootId = newRoot.getId();
        }
        for (Map.Entry<String, RevTreeBuilder> e : repositoryChangedTrees.entrySet()) {
            String treePath = e.getKey();
            ObjectId metadataId = changedTreesMetadataId.get(treePath);
            RevTreeBuilder treeBuilder = e.getValue();
            RevTree newRoot = getTree(newTargetRootId);
            RevTree tree = treeBuilder.build();
            newTargetRootId = writeBack(newRoot.builder(repositoryDatabase), tree, treePath,
                    metadataId);
        }

        progress.complete();

        return newTargetRootId;
    }

    /**
     * @param parentPath2
     * @param indexChangedTrees
     * @param metadataCache
     * @return
     */
    private void resolveSourceTreeRef(String parentPath, Map<String, NodeRef> indexChangedTrees,
            Map<String, ObjectId> metadataCache) {

        if (NodeRef.ROOT.equals(parentPath)) {
            return;
        }
        NodeRef indexTreeRef = indexChangedTrees.get(parentPath);

        if (indexTreeRef == null) {
            RevTree stageHead = getIndex().getTree();
            Optional<NodeRef> treeRef = command(FindTreeChild.class).setIndex(true)
                    .setParent(stageHead).setChildPath(parentPath).call();
            if (treeRef.isPresent()) {// may not be in case of a delete
                indexTreeRef = treeRef.get();
                indexChangedTrees.put(parentPath, indexTreeRef);
                metadataCache.put(parentPath, indexTreeRef.getMetadataId());
            }
        } else {
            metadataCache.put(parentPath, indexTreeRef.getMetadataId());
        }
    }

    /**
     * @param treePath
     * @param treeCache
     * @param metadataCache
     * @return
     */
    private RevTreeBuilder resolveTargetTree(final RevTree root, String treePath,
            Map<String, RevTreeBuilder> treeCache, Map<String, ObjectId> metadataCache,
            ObjectId fallbackMetadataId) {

        RevTreeBuilder treeBuilder = treeCache.get(treePath);
        if (treeBuilder == null) {
            if (NodeRef.ROOT.equals(treePath)) {
                treeBuilder = root.builder(repositoryDatabase);
            } else {
                Optional<NodeRef> treeRef = command(FindTreeChild.class).setIndex(false)
                        .setParent(root).setChildPath(treePath).call();
                if (treeRef.isPresent()) {
                    metadataCache.put(treePath, treeRef.get().getMetadataId());
                    treeBuilder = command(RevObjectParse.class)
                            .setObjectId(treeRef.get().objectId()).call(RevTree.class).get()
                            .builder(repositoryDatabase);
                } else {
                    metadataCache.put(treePath, fallbackMetadataId);
                    treeBuilder = new RevTreeBuilder(repositoryDatabase);
                }
            }
            treeCache.put(treePath, treeBuilder);
        }
        return treeBuilder;
    }

    private RevTree getTree(ObjectId treeId) {
        if (treeId.isNull()) {
            return RevTree.EMPTY;
        }
        return command(RevObjectParse.class).setObjectId(treeId).call(RevTree.class).get();
    }

    private void deepMove(Node ref) {

        Supplier<Node> objectRef = Suppliers.ofInstance(ref);
        command(DeepMove.class).setObjectRef(objectRef).setToIndex(false).call();

    }

    /**
     * @return the resolved root tree id
     */
    private ObjectId resolveRootTreeId() {
        if (oldRoot != null) {
            RevTree rootTree = oldRoot.get();
            return rootTree.getId();
        }
        ObjectId targetTreeId = command(ResolveTreeish.class).setTreeish(Ref.HEAD).call().get();
        return targetTreeId;
    }

    /**
     * @return the resolved root tree
     */
    private RevTree resolveRootTree() {
        if (oldRoot != null) {
            return oldRoot.get();
        }
        final ObjectId targetTreeId = resolveRootTreeId();
        if (targetTreeId.isNull()) {
            return RevTree.EMPTY;
        }
        return command(RevObjectParse.class).setObjectId(targetTreeId).call(RevTree.class).get();
    }

    private ObjectId writeBack(RevTreeBuilder root, final RevTree tree, final String pathToTree,
            final ObjectId metadataId) {

        return command(WriteBack.class).setAncestor(root).setAncestorPath("").setTree(tree)
                .setChildPath(pathToTree).setToIndex(false).setMetadataId(metadataId).call();
    }

}
