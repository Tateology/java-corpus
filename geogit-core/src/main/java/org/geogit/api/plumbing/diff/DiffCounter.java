/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.api.plumbing.diff;

import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.geogit.api.Bucket;
import org.geogit.api.Node;
import org.geogit.api.ObjectId;
import org.geogit.api.RevObject;
import org.geogit.api.RevObject.TYPE;
import org.geogit.api.RevTree;
import org.geogit.storage.NodeStorageOrder;
import org.geogit.storage.ObjectDatabase;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.PeekingIterator;
import com.google.common.collect.Sets;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;

/**
 * A faster alternative to count the number of diffs between two trees than walking a
 * {@link DiffTreeWalk} iterator; doesn't support filtering, counts the total number of differences
 * between the two trees
 * <p>
 * TODO: add support for path filtering
 */
public class DiffCounter implements Supplier<DiffObjectCount> {

    @Nonnull
    private final RevTree fromRootTree;

    @Nonnull
    private final RevTree toRootTree;

    @Nonnull
    private ObjectDatabase objectDb;

    public DiffCounter(final ObjectDatabase db, final RevTree fromRootTree, final RevTree toRootTree) {
        Preconditions.checkNotNull(db);
        Preconditions.checkNotNull(fromRootTree);
        Preconditions.checkNotNull(toRootTree);
        this.objectDb = db;
        this.fromRootTree = fromRootTree;
        this.toRootTree = toRootTree;
    }

    @Override
    public DiffObjectCount get() {

        RevTree oldTree = this.fromRootTree;
        RevTree newTree = this.toRootTree;

        return countDiffs(oldTree, newTree);
    }

    private DiffObjectCount countDiffs(ObjectId oldTreeId, ObjectId newTreeId) {
        RevTree leftTree = getTree(oldTreeId);
        RevTree rightTree = getTree(newTreeId);
        return countDiffs(leftTree, rightTree);
    }

    private DiffObjectCount countDiffs(RevTree oldTree, RevTree newTree) {
        if (oldTree.getId().equals(newTree.getId())) {
            return new DiffObjectCount(0, 0);
        } else if (newTree.isEmpty()) {
            return countOf(oldTree);
        } else if (oldTree.isEmpty()) {
            return countOf(newTree);
        }

        DiffObjectCount count;

        final boolean childrenVsChildren = !oldTree.buckets().isPresent()
                && !newTree.buckets().isPresent();
        final boolean bucketsVsBuckets = oldTree.buckets().isPresent()
                && newTree.buckets().isPresent();

        if (childrenVsChildren) {
            count = countChildrenDiffs(oldTree, newTree);
        } else if (bucketsVsBuckets) {
            ImmutableSortedMap<Integer, Bucket> leftBuckets = oldTree.buckets().get();
            ImmutableSortedMap<Integer, Bucket> rightBuckets = newTree.buckets().get();
            count = countBucketDiffs(leftBuckets, rightBuckets);
        } else {
            // get the children and buckets from the respective trees, order doesn't matter as we're
            // counting diffs
            ImmutableSortedMap<Integer, Bucket> buckets;
            Iterator<Node> children;

            buckets = oldTree.buckets().isPresent() ? oldTree.buckets().get() : newTree.buckets()
                    .get();

            children = oldTree.buckets().isPresent() ? newTree.children() : oldTree.children();
            count = countBucketsChildren(buckets, children);
        }

        return count;
    }

    /**
     * Handles the case where one version of a tree has so few nodes that they all fit in its
     * {@link RevTree#children() children}, but the other version of the tree has more nodes so its
     * split into {@link RevTree#buckets()}.
     */
    private DiffObjectCount countBucketsChildren(ImmutableSortedMap<Integer, Bucket> buckets,
            Iterator<Node> children) {

        final NodeStorageOrder refOrder = new NodeStorageOrder();
        final int bucketDepth = 0; // start at depth 0
        return countBucketsChildren(buckets, children, refOrder, bucketDepth);
    }

    private DiffObjectCount countBucketsChildren(ImmutableSortedMap<Integer, Bucket> buckets,
            Iterator<Node> children, final NodeStorageOrder refOrder, final int depth) {

        final SortedSetMultimap<Integer, Node> treesByBucket;
        final SortedSetMultimap<Integer, Node> featuresByBucket;
        {
            treesByBucket = TreeMultimap.create(Ordering.natural(), refOrder); // make sure values
                                                                               // are sorted
                                                                               // according to
                                                                               // refOrder
            featuresByBucket = TreeMultimap.create(Ordering.natural(), refOrder);// make sure values
                                                                                 // are sorted
                                                                                 // according to
                                                                                 // refOrder
            while (children.hasNext()) {
                Node ref = children.next();
                Integer bucket = refOrder.bucket(ref, depth);
                if (ref.getType().equals(TYPE.TREE)) {
                    treesByBucket.put(bucket, ref);
                } else {
                    featuresByBucket.put(bucket, ref);
                }
            }
        }

        DiffObjectCount count = new DiffObjectCount();

        {// count full size of all buckets for which no children falls into
            final Set<Integer> loneleyBuckets = Sets.difference(buckets.keySet(),
                    Sets.union(featuresByBucket.keySet(), treesByBucket.keySet()));

            for (Integer bucket : loneleyBuckets) {
                ObjectId bucketId = buckets.get(bucket).id();
                count.add(sizeOfTree(bucketId));
            }
        }
        {// count the full size of all children whose buckets don't exist on the buckets tree
            for (Integer bucket : Sets.difference(featuresByBucket.keySet(), buckets.keySet())) {
                SortedSet<Node> refs = featuresByBucket.get(bucket);
                count.addFeatures(refs.size());
            }

            for (Integer bucket : Sets.difference(treesByBucket.keySet(), buckets.keySet())) {
                SortedSet<Node> refs = treesByBucket.get(bucket);
                count.add(aggregateSize(refs));
            }
        }

        // find the number of diffs of the intersection
        final Set<Integer> commonBuckets = Sets.intersection(buckets.keySet(),
                Sets.union(featuresByBucket.keySet(), treesByBucket.keySet()));
        for (Integer bucket : commonBuckets) {

            Iterator<Node> refs = Iterators.concat(treesByBucket.get(bucket).iterator(),
                    featuresByBucket.get(bucket).iterator());

            final ObjectId bucketId = buckets.get(bucket).id();
            final RevTree bucketTree = getTree(bucketId);

            if (bucketTree.isEmpty()) {
                // unlikely
                count.add(aggregateSize(refs));
            } else if (!bucketTree.buckets().isPresent()) {
                count.add(countChildrenDiffs(bucketTree.children(), refs));
            } else {
                final int deeperBucketsDepth = depth + 1;
                final ImmutableSortedMap<Integer, Bucket> deeperBuckets;
                deeperBuckets = bucketTree.buckets().get();
                count.add(countBucketsChildren(deeperBuckets, refs, refOrder, deeperBucketsDepth));
            }
        }

        return count;
    }

    /**
     * Counts the number of differences between two trees that contain {@link RevTree#buckets()
     * buckets} instead of direct {@link RevTree#children() children}
     */
    private DiffObjectCount countBucketDiffs(ImmutableSortedMap<Integer, Bucket> leftBuckets,
            ImmutableSortedMap<Integer, Bucket> rightBuckets) {

        DiffObjectCount count = new DiffObjectCount();
        final Set<Integer> bucketIds = Sets.union(leftBuckets.keySet(), rightBuckets.keySet());

        ObjectId leftTreeId;
        ObjectId rightTreeId;

        for (Integer bucketId : bucketIds) {
            @Nullable
            Bucket leftBucket = leftBuckets.get(bucketId);
            @Nullable
            Bucket rightBucket = rightBuckets.get(bucketId);

            leftTreeId = leftBucket == null ? null : leftBucket.id();
            rightTreeId = rightBucket == null ? null : rightBucket.id();

            if (leftTreeId == null || rightTreeId == null) {
                count.add(sizeOfTree(leftTreeId == null ? rightTreeId : leftTreeId));
            } else {
                count.add(countDiffs(leftTreeId, rightTreeId));
            }
        }
        return count;
    }

    private DiffObjectCount countChildrenDiffs(RevTree leftTree, RevTree rightTree) {
        return countChildrenDiffs(leftTree.children(), rightTree.children());
    }

    private DiffObjectCount countChildrenDiffs(Iterator<Node> leftTree, Iterator<Node> rightTree) {

        final Ordering<Node> storageOrder = new NodeStorageOrder();

        DiffObjectCount count = new DiffObjectCount();

        PeekingIterator<Node> left = Iterators.peekingIterator(leftTree);
        PeekingIterator<Node> right = Iterators.peekingIterator(rightTree);

        while (left.hasNext() && right.hasNext()) {
            Node peekLeft = left.peek();
            Node peekRight = right.peek();

            if (0 == storageOrder.compare(peekLeft, peekRight)) {
                // same path, consume both
                peekLeft = left.next();
                peekRight = right.next();
                if (!peekLeft.getObjectId().equals(peekRight.getObjectId())) {
                    // find the diffs between these two specific refs
                    if (RevObject.TYPE.FEATURE.equals(peekLeft.getType())) {
                        checkState(RevObject.TYPE.FEATURE.equals(peekRight.getType()));
                        count.addFeatures(1);
                    } else {
                        checkState(RevObject.TYPE.TREE.equals(peekLeft.getType()));
                        checkState(RevObject.TYPE.TREE.equals(peekRight.getType()));
                        ObjectId leftTreeId = peekLeft.getObjectId();
                        ObjectId rightTreeId = peekRight.getObjectId();
                        count.add(countDiffs(leftTreeId, rightTreeId));
                    }
                }
            } else if (peekLeft == storageOrder.min(peekLeft, peekRight)) {
                peekLeft = left.next();// consume only the left value
                count.add(aggregateSize(ImmutableList.of(peekLeft)));
            } else {
                peekRight = right.next();// consume only the right value
                count.add(aggregateSize(ImmutableList.of(peekRight)));
            }
        }

        if (left.hasNext()) {
            count.add(countRemaining(left));
        } else if (right.hasNext()) {
            count.add(countRemaining(right));
        }
        Preconditions.checkState(!left.hasNext());
        Preconditions.checkState(!right.hasNext());
        return count;
    }

    private DiffObjectCount countRemaining(Iterator<Node> remaining) {
        ArrayList<Node> iterable = Lists.newArrayList(remaining);
        return aggregateSize(iterable);
    }

    private DiffObjectCount sizeOfTree(ObjectId treeId) {
        RevTree tree = getTree(treeId);
        return countOf(tree);
    }

    private RevTree getTree(ObjectId treeId) {
        if (treeId.isNull()) {
            return RevTree.EMPTY;
        }
        RevTree tree = objectDb.get(treeId, RevTree.class);
        return tree;
    }

    /**
     * @return the total size of {@code tree}
     */
    private DiffObjectCount countOf(RevTree tree) {
        return new DiffObjectCount(tree.numTrees(), tree.size());
    }

    private DiffObjectCount aggregateSize(Iterable<Node> children) {
        return aggregateSize(children.iterator());
    }

    private DiffObjectCount aggregateSize(Iterator<Node> children) {
        DiffObjectCount size = new DiffObjectCount();
        while (children.hasNext()) {
            Node ref = children.next();
            if (RevObject.TYPE.FEATURE.equals(ref.getType())) {
                size.addFeatures(1);
            } else if (RevObject.TYPE.TREE.equals(ref.getType())) {
                ObjectId treeId = ref.getObjectId();
                size.addTrees(1);// Add this tree
                size.add(sizeOfTree(treeId));// and add its content
            }
        }
        return size;
    }
}
