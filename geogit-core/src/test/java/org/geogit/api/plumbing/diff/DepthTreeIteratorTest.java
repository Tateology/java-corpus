/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.api.plumbing.diff;

import java.util.List;

import org.geogit.api.Node;
import org.geogit.api.NodeRef;
import org.geogit.api.ObjectId;
import org.geogit.api.RevObject.TYPE;
import org.geogit.api.RevTree;
import org.geogit.api.RevTreeBuilder;
import org.geogit.api.plumbing.diff.DepthTreeIterator.Strategy;
import org.geogit.storage.ObjectDatabase;
import org.geogit.storage.datastream.DataStreamSerializationFactory;
import org.geogit.storage.memory.HeapObjectDatabse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

/**
 *
 */
public class DepthTreeIteratorTest extends Assert {

    private ObjectDatabase source;

    private String treePath;

    private ObjectId metadataId;

    private RevTree emptyTree, featuresLeafTree, treesLeafTree, mixedLeafTree;

    private RevTree featuresBucketsTree;

    @Before
    public void setUp() {
        source = new HeapObjectDatabse(new DataStreamSerializationFactory());
        source.open();

        metadataId = ObjectId.forString("fake id");
        treePath = "";
        emptyTree = RevTree.EMPTY;
        featuresLeafTree = createFeaturesTree("featuresLeafTree", 100).build();
        assertTrue(featuresLeafTree.features().isPresent());

        treesLeafTree = createTreesTree(100, 10).build();
        assertTrue(treesLeafTree.trees().isPresent());

        RevTreeBuilder builder = createTreesTree(10, 10);
        for (int i = 0; i < 100; i++) {
            builder.put(featureRef("feature.", i));
        }
        mixedLeafTree = builder.build();

        featuresBucketsTree = createFeaturesTree("feature.", 25000).build();
    }

    @Test
    public void testFeaturesLeafTree() {
        assertEquals(0, list(emptyTree, Strategy.FEATURES_ONLY).size());
        assertEquals(100, list(featuresLeafTree, Strategy.FEATURES_ONLY).size());
        assertEquals(0, list(treesLeafTree, Strategy.FEATURES_ONLY).size());
        assertEquals(100, list(mixedLeafTree, Strategy.FEATURES_ONLY).size());
    }

    @Test
    public void testFeaturesBucketsTree() {
        int numEntries = 2 * RevTree.NORMALIZED_SIZE_LIMIT;
        RevTree tree = createFeaturesTree("feature.", numEntries).build();
        assertEquals(numEntries, list(tree, Strategy.FEATURES_ONLY).size());

        assertEquals(featuresBucketsTree.size(), list(featuresBucketsTree, Strategy.FEATURES_ONLY)
                .size());
    }

    @Test
    public void testChildren() {
        assertEquals(0, list(emptyTree, Strategy.CHILDREN).size());
        assertEquals(100, list(featuresLeafTree, Strategy.CHILDREN).size());
        assertEquals(100, list(treesLeafTree, Strategy.CHILDREN).size());
        assertEquals(110, list(mixedLeafTree, Strategy.CHILDREN).size());
        assertEquals(25000, list(featuresBucketsTree, Strategy.CHILDREN).size());
    }

    @Test
    public void testTrees() {
        assertEquals(0, list(emptyTree, Strategy.TREES_ONLY).size());
        assertEquals(0, list(featuresLeafTree, Strategy.TREES_ONLY).size());
        assertEquals(100, list(treesLeafTree, Strategy.TREES_ONLY).size());
        assertEquals(10, list(mixedLeafTree, Strategy.TREES_ONLY).size());
        assertEquals(0, list(featuresBucketsTree, Strategy.TREES_ONLY).size());

        int numSubTrees = RevTree.NORMALIZED_SIZE_LIMIT + 1;
        int featuresPerTree = RevTree.NORMALIZED_SIZE_LIMIT + 1;
        RevTreeBuilder builder = createTreesTree(numSubTrees, featuresPerTree);
        for (int i = 0; i < 25000; i++) {
            builder.put(featureRef("f", i));
        }
        RevTree mixedBucketsTree = builder.build();
        assertEquals(numSubTrees, list(mixedBucketsTree, Strategy.TREES_ONLY).size());
    }

    @Test
    public void testRecursive() {
        assertEquals(0, list(emptyTree, Strategy.RECURSIVE).size());
        assertEquals(100, list(featuresLeafTree, Strategy.RECURSIVE).size());
        assertEquals(treesLeafTree.size() + treesLeafTree.numTrees(),
                list(treesLeafTree, Strategy.RECURSIVE).size());
        assertEquals(mixedLeafTree.size() + mixedLeafTree.numTrees(),
                list(mixedLeafTree, Strategy.RECURSIVE).size());
        assertEquals(featuresBucketsTree.size(), list(featuresBucketsTree, Strategy.RECURSIVE)
                .size());

        int numSubTrees = RevTree.NORMALIZED_SIZE_LIMIT + 1;
        int featuresPerTree = RevTree.NORMALIZED_SIZE_LIMIT + 1;
        RevTreeBuilder builder = createTreesTree(numSubTrees, featuresPerTree);
        for (int i = 0; i < 25000; i++) {
            builder.put(featureRef("f", i));
        }
        RevTree mixedBucketsTree = builder.build();
        assertEquals(mixedBucketsTree.size() + mixedBucketsTree.numTrees(),
                list(mixedBucketsTree, Strategy.RECURSIVE).size());
    }

    @Test
    public void testRecursiveFeaturesOnly() {
        assertEquals(0, list(emptyTree, Strategy.RECURSIVE_FEATURES_ONLY).size());
        assertEquals(100, list(featuresLeafTree, Strategy.RECURSIVE_FEATURES_ONLY).size());
        assertEquals(treesLeafTree.size(), list(treesLeafTree, Strategy.RECURSIVE_FEATURES_ONLY)
                .size());
        assertEquals(mixedLeafTree.size(), list(mixedLeafTree, Strategy.RECURSIVE_FEATURES_ONLY)
                .size());
        assertEquals(featuresBucketsTree.size(),
                list(featuresBucketsTree, Strategy.RECURSIVE_FEATURES_ONLY).size());

        int numSubTrees = RevTree.NORMALIZED_SIZE_LIMIT + 1;
        int featuresPerTree = RevTree.NORMALIZED_SIZE_LIMIT + 1;
        RevTreeBuilder builder = createTreesTree(numSubTrees, featuresPerTree);
        for (int i = 0; i < 25000; i++) {
            builder.put(featureRef("f", i));
        }
        RevTree mixedBucketsTree = builder.build();
        assertEquals(mixedBucketsTree.size(),
                list(mixedBucketsTree, Strategy.RECURSIVE_FEATURES_ONLY).size());
    }

    @Test
    public void testRecursiveTreesOnly() {
        assertEquals(0, list(emptyTree, Strategy.RECURSIVE_TREES_ONLY).size());
        assertEquals(0, list(featuresLeafTree, Strategy.RECURSIVE_TREES_ONLY).size());
        assertEquals(treesLeafTree.numTrees(), list(treesLeafTree, Strategy.RECURSIVE_TREES_ONLY)
                .size());
        assertEquals(mixedLeafTree.numTrees(), list(mixedLeafTree, Strategy.RECURSIVE_TREES_ONLY)
                .size());
        assertEquals(0, list(featuresBucketsTree, Strategy.RECURSIVE_TREES_ONLY).size());

        int numSubTrees = RevTree.NORMALIZED_SIZE_LIMIT + 1;
        int featuresPerTree = RevTree.NORMALIZED_SIZE_LIMIT + 1;
        RevTreeBuilder builder = createTreesTree(numSubTrees, featuresPerTree);
        for (int i = 0; i < 25000; i++) {
            builder.put(featureRef("f", i));
        }
        RevTree mixedBucketsTree = builder.build();
        Stopwatch sw = new Stopwatch().start();
        assertEquals(numSubTrees, list(mixedBucketsTree, Strategy.RECURSIVE_TREES_ONLY).size());
        sw.stop();
        System.err.println(sw);
    }

    private List<NodeRef> list(RevTree tree, Strategy strategy) {
        List<NodeRef> refs = Lists.newArrayList(iterator(tree, strategy));
        return refs;
    }

    private DepthTreeIterator iterator(RevTree tree, Strategy strategy) {
        DepthTreeIterator iterator = new DepthTreeIterator(treePath, metadataId, tree, source,
                strategy);
        return iterator;
    }

    private RevTreeBuilder createTreesTree(int numSubTrees, int featuresPerSubtree) {
        RevTreeBuilder builder = new RevTreeBuilder(source);
        for (int treeN = 0; treeN < numSubTrees; treeN++) {
            RevTree subtree = createFeaturesTree("subtree" + treeN, featuresPerSubtree).build();
            source.put(subtree);
            builder.put(Node.create("subtree" + treeN, subtree.getId(), metadataId, TYPE.TREE, null));
        }
        return builder;
    }

    private RevTreeBuilder createFeaturesTree(final String namePrefix, final int numEntries) {

        RevTreeBuilder tree = new RevTreeBuilder(source);
        for (int i = 0; i < numEntries; i++) {
            tree.put(featureRef(namePrefix, i));
        }
        return tree;
    }

    private Node featureRef(String namePrefix, int i) {
        String name = namePrefix + String.valueOf(i);
        Node ref = Node.create(name, ObjectId.forString(name), ObjectId.NULL, TYPE.FEATURE, null);
        return ref;
    }

}
