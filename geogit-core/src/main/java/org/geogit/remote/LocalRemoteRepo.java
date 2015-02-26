/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.remote;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.geogit.api.Bucket;
import org.geogit.api.GeoGIT;
import org.geogit.api.Node;
import org.geogit.api.ObjectId;
import org.geogit.api.Ref;
import org.geogit.api.RevCommit;
import org.geogit.api.RevObject;
import org.geogit.api.RevObject.TYPE;
import org.geogit.api.RevTree;
import org.geogit.api.SymRef;
import org.geogit.api.plumbing.ForEachRef;
import org.geogit.api.plumbing.RefParse;
import org.geogit.api.plumbing.RevObjectParse;
import org.geogit.api.plumbing.UpdateRef;
import org.geogit.api.plumbing.UpdateSymRef;
import org.geogit.api.porcelain.SynchronizationException;
import org.geogit.repository.Repository;
import org.geogit.storage.ObjectInserter;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;

/**
 * An implementation of a remote repository that exists on the local machine.
 * 
 * @see IRemoteRepo
 */
class LocalRemoteRepo extends AbstractRemoteRepo {

    private GeoGIT remoteGeoGit;

    private Injector injector;

    private File workingDirectory;

    private List<ObjectId> touchedIds;

    /**
     * Constructs a new {@code LocalRemoteRepo} with the given parameters.
     * 
     * @param injector the Guice injector for the new repository
     * @param workingDirectory the directory of the remote repository
     */
    public LocalRemoteRepo(Injector injector, File workingDirectory, Repository localRepository) {
        super(localRepository);
        this.injector = injector;
        this.workingDirectory = workingDirectory;
    }

    /**
     * @param geogit manually set a geogit for this remote repository
     */
    public void setGeoGit(GeoGIT geogit) {
        this.remoteGeoGit = geogit;
    }

    /**
     * Opens the remote repository.
     * 
     * @throws IOException
     */
    @Override
    public void open() throws IOException {
        if (remoteGeoGit == null) {
            remoteGeoGit = new GeoGIT(injector, workingDirectory);
            remoteGeoGit.getRepository();
        }

    }

    /**
     * Closes the remote repository.
     * 
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        remoteGeoGit.close();

    }

    /**
     * @return the remote's HEAD {@link Ref}.
     */
    @Override
    public Ref headRef() {
        final Optional<Ref> currHead = remoteGeoGit.command(RefParse.class).setName(Ref.HEAD)
                .call();
        Preconditions.checkState(currHead.isPresent(), "Remote repository has no HEAD.");
        return currHead.get();
    }

    /**
     * List the remote's {@link Ref refs}.
     * 
     * @param getHeads whether to return refs in the {@code refs/heads} namespace
     * @param getTags whether to return refs in the {@code refs/tags} namespace
     * @return an immutable set of refs from the remote
     */
    @Override
    public ImmutableSet<Ref> listRefs(final boolean getHeads, final boolean getTags) {
        Predicate<Ref> filter = new Predicate<Ref>() {
            @Override
            public boolean apply(Ref input) {
                boolean keep = false;
                if (getHeads) {
                    keep = input.getName().startsWith(Ref.HEADS_PREFIX);
                }
                if (getTags) {
                    keep = keep || input.getName().startsWith(Ref.TAGS_PREFIX);
                }
                return keep;
            }
        };
        return remoteGeoGit.command(ForEachRef.class).setFilter(filter).call();
    }

    /**
     * Fetch all new objects from the specified {@link Ref} from the remote.
     * 
     * @param ref the remote ref that points to new commit data
     * @param fetchLimit the maximum depth to fetch
     */
    @Override
    public void fetchNewData(Ref ref, Optional<Integer> fetchLimit) {

        touchedIds = new LinkedList<ObjectId>();

        CommitTraverser traverser = getFetchTraverser(fetchLimit);

        try {
            traverser.traverse(ref.getObjectId());
            while (!traverser.commits.isEmpty()) {
                walkCommit(traverser.commits.pop(), true);
            }

        } catch (Exception e) {
            for (ObjectId oid : touchedIds) {
                localRepository.getObjectDatabase().delete(oid);
            }
            Throwables.propagate(e);
        } finally {
            touchedIds.clear();
            touchedIds = null;
        }
    }

    /**
     * Push all new objects from the specified {@link Ref} to the given refspec.
     * 
     * @param ref the local ref that points to new commit data
     * @param refspec the refspec to push to
     */
    @Override
    public void pushNewData(Ref ref, String refspec) throws SynchronizationException {
        Optional<Ref> remoteRef = remoteGeoGit.command(RefParse.class).setName(refspec).call();
        checkPush(ref, remoteRef);
        touchedIds = new LinkedList<ObjectId>();

        CommitTraverser traverser = getPushTraverser(remoteRef);

        try {
            traverser.traverse(ref.getObjectId());
            while (!traverser.commits.isEmpty()) {
                walkCommit(traverser.commits.pop(), false);
            }

            Ref updatedRef = remoteGeoGit.command(UpdateRef.class).setName(refspec)
                    .setNewValue(ref.getObjectId()).call().get();

            Ref remoteHead = headRef();
            if (remoteHead instanceof SymRef) {
                if (((SymRef) remoteHead).getTarget().equals(updatedRef.getName())) {
                    remoteGeoGit.command(UpdateSymRef.class).setName(Ref.HEAD)
                            .setNewValue(ref.getName()).call();
                    RevCommit commit = remoteGeoGit.getRepository().getCommit(ref.getObjectId());
                    remoteGeoGit.getRepository().getWorkingTree()
                            .updateWorkHead(commit.getTreeId());
                    remoteGeoGit.getRepository().getIndex().updateStageHead(commit.getTreeId());
                }
            }
        } catch (Exception e) {
            for (ObjectId oid : touchedIds) {
                remoteGeoGit.getRepository().getObjectDatabase().delete(oid);
            }
            Throwables.propagate(e);
        } finally {
            touchedIds.clear();
            touchedIds = null;
        }
    }

    /**
     * Delete the given refspec from the remote repository.
     * 
     * @param refspec the refspec to delete
     */
    @Override
    public void deleteRef(String refspec) {
        remoteGeoGit.command(UpdateRef.class).setName(refspec).setDelete(true).call();
    }

    protected void walkCommit(ObjectId commitId, boolean fetch) {
        Repository from = localRepository;
        Repository to = remoteGeoGit.getRepository();
        if (fetch) {
            from = to;
            to = localRepository;
        }
        ObjectInserter objectInserter = to.newObjectInserter();

        Optional<RevObject> object = from.command(RevObjectParse.class).setObjectId(commitId)
                .call();
        if (object.isPresent() && object.get().getType().equals(TYPE.COMMIT)) {
            RevCommit commit = (RevCommit) object.get();
            walkTree(commit.getTreeId(), from, to, objectInserter);

            objectInserter.insert(commit);
            touchedIds.add(commitId);
        }
    }

    private void walkTree(ObjectId treeId, Repository from, Repository to,
            ObjectInserter objectInserter) {
        // See if we already have it
        if (to.getObjectDatabase().exists(treeId)) {
            return;
        }

        Optional<RevObject> object = from.command(RevObjectParse.class).setObjectId(treeId).call();
        if (object.isPresent() && object.get().getType().equals(TYPE.TREE)) {
            RevTree tree = (RevTree) object.get();

            objectInserter.insert(tree);
            touchedIds.add(treeId);
            // walk subtrees
            if (tree.buckets().isPresent()) {
                for (Bucket bucket : tree.buckets().get().values()) {
                    walkTree(bucket.id(), from, to, objectInserter);
                }
            } else {
                // get new objects
                for (Iterator<Node> children = tree.children(); children.hasNext();) {
                    Node ref = children.next();
                    moveObject(ref.getObjectId(), from, to, objectInserter);
                    ObjectId metadataId = ref.getMetadataId().or(ObjectId.NULL);
                    if (!metadataId.isNull()) {
                        moveObject(metadataId, from, to, objectInserter);
                    }
                }
            }
        }
    }

    private void moveObject(ObjectId objectId, Repository from, Repository to,
            ObjectInserter objectInserter) {
        // See if we already have it
        if (to.getObjectDatabase().exists(objectId)) {
            return;
        }

        Optional<RevObject> childObject = from.command(RevObjectParse.class).setObjectId(objectId)
                .call();
        if (childObject.isPresent()) {
            RevObject revObject = childObject.get();
            if (TYPE.TREE.equals(revObject.getType())) {
                walkTree(objectId, from, to, objectInserter);
            }
            objectInserter.insert(revObject);
            touchedIds.add(objectId);
        }
    }

    /**
     * @return the {@link RepositoryWrapper} for this remote
     */
    @Override
    public RepositoryWrapper getRemoteWrapper() {
        return new LocalRepositoryWrapper(remoteGeoGit.getRepository());
    }

    /**
     * Gets the depth of the remote repository.
     * 
     * @return the depth of the repository, or {@link Optional#absent()} if the repository is not
     *         shallow
     */
    @Override
    public Optional<Integer> getDepth() {
        return remoteGeoGit.getRepository().getDepth();
    }
}
