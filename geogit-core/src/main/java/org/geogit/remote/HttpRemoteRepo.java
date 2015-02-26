/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.geogit.api.ObjectId;
import org.geogit.api.Ref;
import org.geogit.api.RevCommit;
import org.geogit.api.RevObject;
import org.geogit.api.porcelain.SynchronizationException;
import org.geogit.repository.Repository;
import org.geogit.storage.DeduplicationService;
import org.geogit.storage.Deduplicator;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * An implementation of a remote repository that exists on a remote machine and made public via an
 * http interface.
 * 
 * @see AbstractRemoteRepo
 */
class HttpRemoteRepo extends AbstractRemoteRepo {

    private URL repositoryURL;

    private List<ObjectId> fetchedIds;
    
    final private DeduplicationService deduplicationService;

    /**
     * Constructs a new {@code HttpRemoteRepo} with the given parameters.
     * 
     * @param repositoryURL the url of the remote repository
     */
    public HttpRemoteRepo(URL repositoryURL, Repository localRepository, DeduplicationService deduplicationService) {
        super(localRepository);
        this.deduplicationService = deduplicationService;
        String url = repositoryURL.toString();
        if (url.endsWith("/")) {
            url = url.substring(0, url.lastIndexOf('/'));
        }
        try {
            this.repositoryURL = new URL(url);
        } catch (MalformedURLException e) {
            this.repositoryURL = repositoryURL;
        }
    }

    /**
     * Currently does nothing for HTTP Remote.
     * 
     * @throws IOException
     */
    @Override
    public void open() throws IOException {

    }

    /**
     * Currently does nothing for HTTP Remote.
     * 
     * @throws IOException
     */
    @Override
    public void close() throws IOException {

    }

    /**
     * @return the remote's HEAD {@link Ref}.
     */
    @Override
    public Ref headRef() {
        HttpURLConnection connection = null;
        Ref headRef = null;
        try {
            String expanded = repositoryURL.toString() + "/repo/manifest";

            connection = (HttpURLConnection) new URL(expanded).openConnection();
            connection.setRequestMethod("GET");

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            // Get Response
            InputStream is = connection.getInputStream();
            try {
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;

                while ((line = rd.readLine()) != null) {
                    if (line.startsWith("HEAD")) {
                        headRef = HttpUtils.parseRef(line);
                    }
                }
                rd.close();
            } finally {
                is.close();
            }

        } catch (Exception e) {

            Throwables.propagate(e);

        } finally {
            HttpUtils.consumeErrStreamAndCloseConnection(connection);
        }
        return headRef;
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
        HttpURLConnection connection = null;
        ImmutableSet.Builder<Ref> builder = new ImmutableSet.Builder<Ref>();
        try {
            String expanded = repositoryURL.toString() + "/repo/manifest";

            connection = (HttpURLConnection) new URL(expanded).openConnection();
            connection.setRequestMethod("GET");

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            // Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            try {
                while ((line = rd.readLine()) != null) {
                    if ((getHeads && line.startsWith("refs/heads"))
                            || (getTags && line.startsWith("refs/tags"))) {
                        builder.add(HttpUtils.parseRef(line));
                    }
                }
            } finally {
                rd.close();
            }

        } catch (Exception e) {
            throw Throwables.propagate(e);
        } finally {
            HttpUtils.consumeErrStreamAndCloseConnection(connection);
        }
        return builder.build();
    }

    /**
     * Fetch all new objects from the specified {@link Ref} from the remote.
     * 
     * @param ref the remote ref that points to new commit data
     * @param fetchLimit the maximum depth to fetch
     */
    @Override
    public void fetchNewData(Ref ref, Optional<Integer> fetchLimit) {
        fetchedIds = new LinkedList<ObjectId>();

        CommitTraverser traverser = getFetchTraverser(fetchLimit);

        try {
            traverser.traverse(ref.getObjectId());
            List<ObjectId> want = new LinkedList<ObjectId>();
            want.addAll(traverser.commits);
            Collections.reverse(want);
            Set<ObjectId> have = new HashSet<ObjectId>();
            have.addAll(traverser.have);
            while (!want.isEmpty()) {
                fetchMoreData(want, have);
            }
        } catch (Exception e) {
            for (ObjectId oid : fetchedIds) {
                localRepository.getObjectDatabase().delete(oid);
            }
            Throwables.propagate(e);
        } finally {
            fetchedIds.clear();
            fetchedIds = null;
        }
    }

    /**
     * Push all new objects from the specified {@link Ref} to the remote.
     * 
     * @param ref the local ref that points to new commit data
     * @param refspec the remote branch to push to
     */
    @Override
    public void pushNewData(Ref ref, String refspec) throws SynchronizationException {
        Optional<Ref> remoteRef = HttpUtils.getRemoteRef(repositoryURL, refspec);
        checkPush(ref, remoteRef);
        beginPush();

        CommitTraverser traverser = getPushTraverser(remoteRef);

        traverser.traverse(ref.getObjectId());

        List<ObjectId> toSend = new LinkedList<ObjectId>();
        toSend.addAll(traverser.commits);
        Collections.reverse(toSend);
        Set<ObjectId> have = new HashSet<ObjectId>();
        have.addAll(traverser.have);

        Deduplicator deduplicator = deduplicationService.createDeduplicator();
        try {
            sendPackedObjects(toSend, have, deduplicator);
        } finally {
            deduplicator.release();
        }

        ObjectId originalRemoteRefValue = ObjectId.NULL;
        if (remoteRef.isPresent()) {
            originalRemoteRefValue = remoteRef.get().getObjectId();
        }
        endPush(refspec, ref.getObjectId(), originalRemoteRefValue.toString());
    }

    private void sendPackedObjects(final List<ObjectId> toSend, final Set<ObjectId> roots, Deduplicator deduplicator) {
        Set<ObjectId> sent = new HashSet<ObjectId>();
        while (!toSend.isEmpty()) {
            try {
                String expanded = repositoryURL.toString() + "/repo/sendobject";
                HttpURLConnection connection = (HttpURLConnection) new URL(expanded)
                        .openConnection();
                connection.setDoOutput(true);
                connection.setChunkedStreamingMode(4096);

                OutputStream out = connection.getOutputStream();
                BinaryPackedObjects.Callback<Void> callback = new BinaryPackedObjects.Callback<Void>() {
                    @Override
                    public Void callback(RevObject object, Void state) {
                        if (object instanceof RevCommit) {
                            RevCommit commit = (RevCommit) object;
                            toSend.remove(commit.getId());
                            roots.removeAll(commit.getParentIds());
                            roots.add(commit.getId());
                        }
                        return null;
                    }
                };
                BinaryPackedObjects packer = new BinaryPackedObjects(
                        localRepository.getObjectDatabase());
                packer.write(out, toSend, ImmutableList.copyOf(roots), sent, callback, false, deduplicator);
                out.flush();
                out.close();

                InputStream in = connection.getInputStream();
                HttpUtils.consumeAndCloseStream(in);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
        }
    }

    /**
     * Delete a {@link Ref} from the remote repository.
     * 
     * @param refspec the ref to delete
     */
    @Override
    public void deleteRef(String refspec) {
        HttpUtils.updateRemoteRef(repositoryURL, refspec, null, true);
    }

    private void beginPush() {
        HttpUtils.beginPush(repositoryURL);
    }

    private void endPush(String refspec, ObjectId newCommitId, String originalRefValue) {
        HttpUtils.endPush(repositoryURL, refspec, newCommitId, originalRefValue);
    }

    /**
     * Retrieve objects from the remote repository, and update have/want lists accordingly.
     * Specifically, any retrieved commits are removed from the want list and added to the have
     * list, and any parents of those commits are removed from the have list (it only represents the
     * most recent common commits.) Retrieved objects are added to the local repository, and the
     * want/have lists are updated in-place.
     * 
     * @param want a list of ObjectIds that need to be fetched
     * @param have a list of ObjectIds that are in common with the remote repository
     */
    private void fetchMoreData(final List<ObjectId> want, final Set<ObjectId> have) {
        final JsonObject message = createFetchMessage(want, have);
        final URL resourceURL;
        try {
            resourceURL = new URL(repositoryURL.toString() + "/repo/batchobjects");
        } catch (MalformedURLException e) {
            throw Throwables.propagate(e);
        }

        final Gson gson = new Gson();
        final HttpURLConnection connection;
        final OutputStream out;
        final Writer writer;
        try {
            connection = (HttpURLConnection) resourceURL.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            out = connection.getOutputStream();
            writer = new OutputStreamWriter(out);
            gson.toJson(message, writer);
            writer.flush();
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }

        final InputStream in;
        try {
            in = connection.getInputStream();
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }

        BinaryPackedObjects unpacker = new BinaryPackedObjects(localRepository.getObjectDatabase());
        BinaryPackedObjects.Callback<Void> callback = new BinaryPackedObjects.Callback<Void>() {
            @Override
            public Void callback(RevObject object, Void state) {
                if (object instanceof RevCommit) {
                    RevCommit commit = (RevCommit) object;
                    want.remove(commit.getId());
                    have.removeAll(commit.getParentIds());
                    have.add(commit.getId());
                }
                return null;
            }
        };
        unpacker.ingest(in, callback);
    }

    private JsonObject createFetchMessage(List<ObjectId> want, Set<ObjectId> have) {
        JsonObject message = new JsonObject();
        JsonArray wantArray = new JsonArray();
        for (ObjectId id : want) {
            wantArray.add(new JsonPrimitive(id.toString()));
        }
        JsonArray haveArray = new JsonArray();
        for (ObjectId id : have) {
            haveArray.add(new JsonPrimitive(id.toString()));
        }
        message.add("want", wantArray);
        message.add("have", haveArray);
        return message;
    }

    /**
     * @return the {@link RepositoryWrapper} for this remote
     */
    @Override
    public RepositoryWrapper getRemoteWrapper() {
        return new HttpRepositoryWrapper(repositoryURL);
    }

    /**
     * Gets the depth of the remote repository.
     * 
     * @return the depth of the repository, or {@link Optional#absent()} if the repository is not
     *         shallow
     */
    @Override
    public Optional<Integer> getDepth() {
        return HttpUtils.getDepth(repositoryURL, null);
    }
}
