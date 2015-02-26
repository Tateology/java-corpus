/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.storage.memory;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;

import java.net.URL;
import java.util.Map;
import java.util.Set;

import org.geogit.api.ObjectId;
import org.geogit.api.Platform;
import org.geogit.api.plumbing.ResolveGeogitDir;
import org.geogit.storage.GraphDatabase;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

/**
 * Provides an default in memory implementation of a GeoGit Graph Database.
 */
public class HeapGraphDatabase implements GraphDatabase {

    static final Function<Node, ObjectId> NODE_TO_ID = new Function<Node, ObjectId>() {
        @Override
        public ObjectId apply(Node n) {
            return n.id;
        }
    };

    static final Map<URL, Ref> graphs = null; //Maps.newConcurrentMap();

    final Platform platform;

    Graph graph;

    @Inject
    public HeapGraphDatabase(Platform platform) {
        this.platform = platform;
    }

    @Override
    public void open() {
        if (isOpen()) {
            return;
        }

        Optional<URL> url = new ResolveGeogitDir(platform).call();
        if (url.isPresent()) {
            synchronized (graphs) {
                URL key = url.get();
                if (!graphs.containsKey(key)) {
                    graphs.put(key, new Ref(new Graph()));
                }
                graph = graphs.get(key).acquire();
            }
        } else {
            graph = new Graph();
        }

    }

    @Override
    public void configure() {
        // No-op
    }

    @Override
    public void checkConfig() {
        // No-op
    }

    @Override
    public boolean isOpen() {
        return graph != null;
    }

    @Override
    public void close() {
        if (!isOpen()) {
            return;
        }
        graph = null;

        Optional<URL> url = new ResolveGeogitDir(platform).call();
        if (url.isPresent()) {
            synchronized (graphs) {
                URL key = url.get();
                Ref ref = graphs.get(key);
                if (ref != null && ref.release() <= -1) {
                    ref.destroy();
                    graphs.remove(key);
                }
            }
        }
    }

    @Override
    public boolean exists(ObjectId commitId) {
        return graph.get(commitId).isPresent();
    }

    @Override
    public ImmutableList<ObjectId> getParents(ObjectId commitId) throws IllegalArgumentException {
        return graph.get(commitId).transform(new Function<Node, ImmutableList<ObjectId>>() {
            @Override
            public ImmutableList<ObjectId> apply(Node n) {
                // transform outgoing nodes to id
                // filter for null to skip fake root node
                return new ImmutableList.Builder<ObjectId>().addAll(
                        filter(transform(n.to(), NODE_TO_ID), Predicates.notNull())).build();
            }
        }).or((ImmutableList) ImmutableList.of());
    }

    @Override
    public ImmutableList<ObjectId> getChildren(ObjectId commitId) throws IllegalArgumentException {
        return graph.get(commitId).transform(new Function<Node, ImmutableList<ObjectId>>() {
            @Override
            public ImmutableList<ObjectId> apply(Node n) {
                return new ImmutableList.Builder<ObjectId>()
                        .addAll(transform(n.from(), NODE_TO_ID)).build();
            }
        }).or((ImmutableList) ImmutableList.of());
    }

    @Override
    public boolean put(ObjectId commitId, ImmutableList<ObjectId> parentIds) {
        Node n = graph.getOrAdd(commitId);

        if (parentIds.isEmpty()) {
            // the root node, only update on first addition
            if (!n.isRoot()) {
                n.setRoot(true);
                return true;
            }
        }

        // has the node been attached to graph?
        if (Iterables.isEmpty(n.to())) {
            // nope, attach it
            for (ObjectId parent : parentIds) {
                Node p = graph.getOrAdd(parent);
                graph.newEdge(n, p);
            }

            // only mark as updated if it is actually attached
            return !Iterables.isEmpty(n.to());
        }
        return false;
    }

    @Override
    public void map(ObjectId mapped, ObjectId original) {
        graph.map(mapped, original);
    }

    @Override
    public ObjectId getMapping(ObjectId commitId) {
        return Optional.fromNullable(graph.getMapping(commitId)).or(ObjectId.NULL);
    }

    @Override
    public int getDepth(ObjectId commitId) {
        PathToRootWalker walker = new PathToRootWalker(graph.get(commitId).get());
        int depth = 0;
        O: while (walker.hasNext()) {
            for (Node n : walker.next()) {
                if (Iterables.size(n.to()) == 0) {
                    break O;
                }
            }
            depth++;
        }
        return depth;
    }

    @Override
    public Optional<ObjectId> findLowestCommonAncestor(ObjectId leftId, ObjectId rightId) {
        PathToRootWalker left = new PathToRootWalker(graph.get(leftId).get());
        PathToRootWalker right = new PathToRootWalker(graph.get(rightId).get());

        Set<Node> ancestors = Sets.newLinkedHashSet();
        while (left.hasNext() || right.hasNext()) {
            if (left.hasNext()) {
                for (Node l : left.next()) {
                    if (right.seen(l)) {
                        ancestors.add(l);
                    }
                }
            }
            if (right.hasNext()) {
                for (Node r : right.next()) {
                    if (left.seen(r)) {
                        ancestors.add(r);
                    }
                }
            }
        }

        if (ancestors.isEmpty()) {
            // no solution
            return Optional.absent();
        }

        if (ancestors.size() > 1) {
            // multiple candidates, try to filter down by removing candidates that are
            // ancestors of other candidates
            Set<Node> filtered = Sets.newLinkedHashSet(ancestors);
            for (Node ancestor : ancestors) {
                PathToRootWalker w = new PathToRootWalker(ancestor);
                w.next();
                while (w.hasNext()) {
                    for (Node n : w.next()) {
                        filtered.remove(n);
                    }
                }
            }

            ancestors = filtered;
        }

        return Optional.of(ancestors.iterator().next().id);
    }

    @Override
    public void setProperty(ObjectId commitId, String propertyName, String propertyValue) {
        graph.get(commitId).get().put(propertyName, propertyValue);
        ;
    }

    @Override
    public boolean isSparsePath(ObjectId start, ObjectId end) {
        ShortestPathWalker p = new ShortestPathWalker(graph.get(start).get(), graph.get(end).get());
        while (p.hasNext()) {
            Node n = p.next();
            if (Boolean.valueOf(n.get(GraphDatabase.SPARSE_FLAG).or("false"))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void truncate() {
        graph.clear();
    }

    static class Ref {

        int count;

        Graph graph;

        Ref(Graph g) {
            graph = g;
            count = 0;
        }

        Graph acquire() {
            count++;
            return graph;
        }

        int release() {
            return --count;
        }

        void destroy() {
            graph = null;
        }
    }
}
