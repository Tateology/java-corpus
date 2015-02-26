/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.api.plumbing.diff;

import org.geogit.api.Bucket;
import org.geogit.api.Node;
import org.geogit.api.NodeRef;
import org.geogit.api.ObjectId;
import org.geogit.api.RevObject.TYPE;
import org.geogit.api.RevTree;
import org.geogit.api.RevTreeBuilder;
import org.geogit.storage.ObjectDatabase;
import org.geogit.storage.datastream.DataStreamSerializationFactory;
import org.geogit.storage.memory.HeapObjectDatabse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class DiffCounterTest extends Assert {

    /**
     * All original feature noderefs have this objectid
     */
    private static final ObjectId FAKE_FEATURE_ID = ObjectId
            .forString("1100000000000000000000000000000000000000");

    /**
     * All changed feature noderefs have this objectid
     */
    private static final ObjectId FAKE_FEATURE_ID_CHANGED = ObjectId
            .forString("2200000000000000000000000000000000000000");

    private ObjectDatabase odb;

    private RevTree childrenFeatureTree;

    /** single level tree with 2 * {@link RevTree#NORMALIZED_SIZE_LIMIT} feature references */
    private RevTree bucketsFeatureTree;

    private RevTree childrenFeatureTypesTree;

    RevTreeBuilder childTree1;

    RevTreeBuilder childTree2;

    @Before
    public void setUp() {
        odb = new HeapObjectDatabse(new DataStreamSerializationFactory());
        odb.open();
        {
            RevTreeBuilder builder = createFeaturesTree("", 10);
            this.childrenFeatureTree = builder.build();
        }
        {
            RevTreeBuilder rootBuilder = new RevTreeBuilder(odb);
            childTree1 = createFeaturesTree("tree1", 10);
            createFeatureTypesTree(rootBuilder, "tree1", childTree1);
            childTree2 = createFeaturesTree("tree2", 5);
            createFeatureTypesTree(rootBuilder, "tree2", childTree2);
            childrenFeatureTypesTree = rootBuilder.build();
        }

        {
            RevTreeBuilder builder = createFeaturesTree("", 2 * RevTree.NORMALIZED_SIZE_LIMIT);
            this.bucketsFeatureTree = builder.build();
            assertTrue(bucketsFeatureTree.buckets().isPresent());
        }
    }

    private void createFeatureTypesTree(RevTreeBuilder rootBuilder, String treePath,
            RevTreeBuilder childBuilder) {
        RevTree childTree = childBuilder.build();
        odb.put(childTree);
        Node childRef = Node.create(treePath, childTree.getId(), ObjectId.NULL, TYPE.TREE, null);
        rootBuilder.put(childRef);
    }

    private DiffObjectCount count(RevTree left, RevTree right) {
        DiffCounter counter = new DiffCounter(odb, left, right);
        DiffObjectCount count = counter.get();
        return count;
    }

    @Test
    public void testSameTree() {
        DiffObjectCount count = count(childrenFeatureTree, childrenFeatureTree);
        assertEquals(0, count.getFeaturesCount());
        assertEquals(0, count.getTreesCount());
    }

    @Test
    public void testChildrenEmpty() {
        assertEquals(childrenFeatureTree.size(), count(childrenFeatureTree, RevTree.EMPTY)
                .getFeaturesCount());
        assertEquals(childrenFeatureTree.size(), count(RevTree.EMPTY, childrenFeatureTree)
                .getFeaturesCount());
    }

    @Test
    public void testChildrenChildren() {
        RevTreeBuilder builder = new RevTreeBuilder(odb, childrenFeatureTree);
        RevTree changed = builder.remove("3").build();
        assertEquals(1, count(childrenFeatureTree, changed).getFeaturesCount());
        assertEquals(1, count(changed, childrenFeatureTree).getFeaturesCount());

        changed = builder.put(
                Node.create("new", FAKE_FEATURE_ID, ObjectId.NULL, TYPE.FEATURE, null)).build();
        assertEquals(2, count(childrenFeatureTree, changed).getFeaturesCount());
        assertEquals(2, count(changed, childrenFeatureTree).getFeaturesCount());

        changed = builder.put(
                Node.create("1", FAKE_FEATURE_ID_CHANGED, ObjectId.NULL, TYPE.FEATURE, null))
                .build();
        assertEquals(3, count(childrenFeatureTree, changed).getFeaturesCount());
        assertEquals(3, count(changed, childrenFeatureTree).getFeaturesCount());
    }

    @Test
    public void testChildrenChildrenNestedTrees() {
        RevTreeBuilder rootBuilder = new RevTreeBuilder(odb, childrenFeatureTypesTree);
        childTree1.put(featureRef("tree1", 1000));
        createFeatureTypesTree(rootBuilder, "tree1", childTree1);
        RevTree newRoot = rootBuilder.build();

        assertEquals(1, count(childrenFeatureTypesTree, newRoot).getFeaturesCount());

        childTree2.remove("tree2/2");
        createFeatureTypesTree(rootBuilder, "tree2", childTree2);
        newRoot = rootBuilder.build();
        assertEquals(2, count(childrenFeatureTypesTree, newRoot).getFeaturesCount());

        childTree2.put(Node.create("tree2/1", FAKE_FEATURE_ID_CHANGED, ObjectId.NULL, TYPE.FEATURE,
                null));
        createFeatureTypesTree(rootBuilder, "tree2", childTree2);
        newRoot = rootBuilder.build();
        assertEquals(3, count(childrenFeatureTypesTree, newRoot).getFeaturesCount());
    }

    @Test
    public void testBucketBucketAdd() {
        RevTreeBuilder builder = new RevTreeBuilder(odb, bucketsFeatureTree);

        final int from = (int) bucketsFeatureTree.size();
        final int added = 2 * RevTree.NORMALIZED_SIZE_LIMIT;
        for (int i = from; i < (from + added); i++) {
            builder.put(featureRef("", i));
        }

        RevTree changed = builder.build();
        assertEquals(bucketsFeatureTree.size() + added, changed.size());

        assertEquals(added, count(bucketsFeatureTree, changed).getFeaturesCount());
        assertEquals(added, count(changed, bucketsFeatureTree).getFeaturesCount());
    }

    @Test
    public void testBucketBucketRemove() {
        RevTreeBuilder builder = new RevTreeBuilder(odb, bucketsFeatureTree);

        RevTree changed;
        changed = builder.remove("3").build();
        assertEquals(1, count(bucketsFeatureTree, changed).getFeaturesCount());
        assertEquals(1, count(changed, bucketsFeatureTree).getFeaturesCount());

        for (int i = 0; i < RevTree.NORMALIZED_SIZE_LIMIT - 1; i++) {
            builder.remove(String.valueOf(i));
        }
        changed = builder.build();
        assertEquals(RevTree.NORMALIZED_SIZE_LIMIT + 1, changed.size());
        assertTrue(changed.buckets().isPresent());

        assertEquals(RevTree.NORMALIZED_SIZE_LIMIT - 1, count(bucketsFeatureTree, changed)
                .getFeaturesCount());
        assertEquals(RevTree.NORMALIZED_SIZE_LIMIT - 1, count(changed, bucketsFeatureTree)
                .getFeaturesCount());

        builder.remove(String.valueOf(RevTree.NORMALIZED_SIZE_LIMIT + 1));

        changed = builder.build();
        assertEquals(RevTree.NORMALIZED_SIZE_LIMIT, changed.size());
        assertFalse(changed.buckets().isPresent());
    }

    @Test
    public void testBucketBucketChange() {
        RevTreeBuilder builder;
        RevTree changed;

        builder = new RevTreeBuilder(odb, bucketsFeatureTree);

        changed = builder.put(
                Node.create("1023", FAKE_FEATURE_ID_CHANGED, ObjectId.NULL, TYPE.FEATURE, null))
                .build();
        DiffObjectCount count = count(bucketsFeatureTree, changed);
        assertEquals(1, count.getFeaturesCount());
        assertEquals(0, count.getTreesCount());
        count = count(changed, bucketsFeatureTree);
        assertEquals(1, count.getFeaturesCount());
        assertEquals(0, count.getTreesCount());

        builder = new RevTreeBuilder(odb, bucketsFeatureTree);
        int expected = 0;
        for (int i = 0; i < bucketsFeatureTree.size(); i += 2) {
            changed = builder.put(
                    Node.create(String.valueOf(i), FAKE_FEATURE_ID_CHANGED, ObjectId.NULL,
                            TYPE.FEATURE, null)).build();
            expected++;
        }
        changed = builder.build();
        assertEquals(expected, count(bucketsFeatureTree, changed).getFeaturesCount());
        assertEquals(expected, count(changed, bucketsFeatureTree).getFeaturesCount());
    }

    @Test
    public void testBucketChildren() {
        RevTreeBuilder builder = new RevTreeBuilder(odb, bucketsFeatureTree);
        RevTree changed;
        for (int i = 0; i < RevTree.NORMALIZED_SIZE_LIMIT; i++) {
            builder.remove(String.valueOf(i));
        }
        changed = builder.build();
        assertEquals(RevTree.NORMALIZED_SIZE_LIMIT, changed.size());
        assertFalse(changed.buckets().isPresent());

        assertEquals(RevTree.NORMALIZED_SIZE_LIMIT, count(bucketsFeatureTree, changed)
                .getFeaturesCount());
        assertEquals(RevTree.NORMALIZED_SIZE_LIMIT, count(changed, bucketsFeatureTree)
                .getFeaturesCount());
    }

    @Test
    public void testBucketChildrenDeeperBuckets() {

        final RevTree deepTree = createFeaturesTree("", 20000 + RevTree.NORMALIZED_SIZE_LIMIT)
                .build();
        // sanity check
        assertTrue(deepTree.buckets().isPresent());

        {// sanity check to ensure we're testing with a tree with depth > 1 (i.e. at least two
         // levels of buckets)
            final int maxDepth = depth(deepTree, 0);
            assertTrue(maxDepth > 1);
        }

        RevTreeBuilder builder = new RevTreeBuilder(odb, deepTree);
        {
            final int count = (int) (deepTree.size() - RevTree.NORMALIZED_SIZE_LIMIT);
            for (int i = 0; i < count; i++) {
                String path = String.valueOf(i);
                builder.remove(path);
            }
        }
        RevTree changed = builder.build();
        assertEquals(RevTree.NORMALIZED_SIZE_LIMIT, changed.size());
        // sanity check
        assertTrue(changed.features().isPresent());
        assertFalse(changed.buckets().isPresent());

        final long expected = deepTree.size() - changed.size();

        assertEquals(expected, count(deepTree, changed).getFeaturesCount());
        assertEquals(expected, count(changed, deepTree).getFeaturesCount());
    }

    private int depth(RevTree deepTree, int currDepth) {
        if (!deepTree.buckets().isPresent()) {
            return currDepth;
        }
        int depth = currDepth;
        for (Bucket bucket : deepTree.buckets().get().values()) {
            RevTree bucketTree = odb.get(bucket.id(), RevTree.class);
            int d = depth(bucketTree, currDepth + 1);
            depth = Math.max(depth, d);
        }
        return depth;
    }

    private RevTreeBuilder createFeaturesTree(final String parentPath, final int numEntries) {

        RevTreeBuilder tree = new RevTreeBuilder(odb);
        for (int i = 0; i < numEntries; i++) {
            tree.put(featureRef(parentPath, i));
        }
        return tree;
    }

    private Node featureRef(String parentPath, int i) {
        String path = NodeRef.appendChild(parentPath, String.valueOf(i));
        Node ref = Node.create(path, FAKE_FEATURE_ID, ObjectId.NULL, TYPE.FEATURE, null);
        return ref;
    }

}
