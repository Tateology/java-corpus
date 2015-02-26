/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.geogit.api.Bucket;
import org.geogit.api.Node;
import org.geogit.api.ObjectId;
import org.geogit.api.RevObject;
import org.geogit.api.RevTree;
import org.geogit.api.RevTreeImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.vividsolutions.jts.geom.Envelope;

public abstract class RevTreeSerializationTest extends Assert {
    protected ObjectSerializingFactory factory = getObjectSerializingFactory();

    protected abstract ObjectSerializingFactory getObjectSerializingFactory();

    private RevTree tree1_leaves;

    private RevTree tree2_internal;

    private RevTree tree3_buckets;

    private RevTree tree4_spatial_leaves;

    private RevTree tree5_spatial_internal;

    private RevTree tree6_spatial_buckets;

    @Before
    public void initialize() {
        ImmutableList<Node> features = ImmutableList.of(Node.create("foo",
                ObjectId.forString("nodeid"), ObjectId.forString("metadataid"),
                RevObject.TYPE.FEATURE, null));
        ImmutableList<Node> spatialFeatures = ImmutableList.of(Node.create("foo",
                ObjectId.forString("nodeid"), ObjectId.forString("metadataid"),
                RevObject.TYPE.FEATURE, new Envelope(1, 2, 1, 2)));
        ImmutableList<Node> trees = ImmutableList.of(Node.create("bar",
                ObjectId.forString("barnodeid"), ObjectId.forString("barmetadataid"),
                RevObject.TYPE.TREE, null));
        ImmutableList<Node> spatialTrees = ImmutableList.of(Node.create("bar",
                ObjectId.forString("barnodeid"), ObjectId.forString("barmetadataid"),
                RevObject.TYPE.TREE, new Envelope(1, 2, 1, 2)));
        ImmutableMap<Integer, Bucket> spatialBuckets = ImmutableMap.of(1,
                Bucket.create(ObjectId.forString("buckettree"), new Envelope()));
        ImmutableMap<Integer, Bucket> buckets = ImmutableMap.of(1,
                Bucket.create(ObjectId.forString("buckettree"), new Envelope(1, 2, 1, 2)));
        tree1_leaves = RevTreeImpl.createLeafTree(ObjectId.forString("leaves"), 1, features,
                ImmutableList.<Node> of());
        tree2_internal = RevTreeImpl.createLeafTree(ObjectId.forString("internal"), 1,
                ImmutableList.<Node> of(), trees);
        tree3_buckets = RevTreeImpl.createNodeTree(ObjectId.forString("buckets"), 1, 1, buckets);
        tree4_spatial_leaves = RevTreeImpl.createLeafTree(ObjectId.forString("leaves"), 1,
                spatialFeatures, ImmutableList.<Node> of());
        tree5_spatial_internal = RevTreeImpl.createLeafTree(ObjectId.forString("internal"), 1,
                ImmutableList.<Node> of(), spatialTrees);
        tree6_spatial_buckets = RevTreeImpl.createNodeTree(ObjectId.forString("buckets"), 1, 1,
                spatialBuckets);
    }

    @Test
    public void testRoundTripLeafTree() {
        RevTree roundTripped = read(tree1_leaves.getId(), write(tree1_leaves));
        assertTreesAreEqual(tree1_leaves, roundTripped);
    }

    @Test
    public void testRoundTripInternalTree() {
        RevTree roundTripped = read(tree2_internal.getId(), write(tree2_internal));
        assertTreesAreEqual(tree2_internal, roundTripped);
    }

    @Test
    public void testRoundTripBuckets() {
        RevTree roundTripped = read(tree3_buckets.getId(), write(tree3_buckets));
        assertTreesAreEqual(tree3_buckets, roundTripped);
    }

    @Test
    public void testRoundTripSpatialLeafTree() {
        RevTree roundTripped = read(tree4_spatial_leaves.getId(), write(tree4_spatial_leaves));
        assertTreesAreEqual(tree4_spatial_leaves, roundTripped);
    }

    @Test
    public void testRoundTripSpatialInternalTree() {
        RevTree roundTripped = read(tree5_spatial_internal.getId(), write(tree5_spatial_internal));
        assertTreesAreEqual(tree5_spatial_internal, roundTripped);
    }

    @Test
    public void testRoundTripSpatialBuckets() {
        RevTree roundTripped = read(tree6_spatial_buckets.getId(), write(tree6_spatial_buckets));
        assertTreesAreEqual(tree6_spatial_buckets, roundTripped);
    }

    private byte[] write(RevTree tree) {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectWriter<RevTree> treeWriter = factory
                    .<RevTree> createObjectWriter(RevObject.TYPE.TREE);
            treeWriter.write(tree, bout);
            return bout.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private RevTree read(ObjectId id, byte[] bytes) {
        ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
        ObjectReader<RevTree> treeReader = factory
                .<RevTree> createObjectReader(RevObject.TYPE.TREE);
        return treeReader.read(id, bin);
    }

    private void assertTreesAreEqual(RevTree a, RevTree b) {
        assertTrue(a.getId().equals(b.getId()));
        assertTrue(a.buckets().equals(b.buckets()));
        assertTrue(a.features().equals(b.features()));
        assertTrue(a.trees().equals(b.trees()));
        assertTrue(a.numTrees() == b.numTrees());
        assertTrue(a.size() == b.size());
    }
}
