/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.storage;

import java.io.IOException;

import org.geogit.api.ObjectId;
import org.geogit.test.integration.RepositoryTestCase;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;

/**
 * Abstract test suite for {@link GraphDatabase} implementations.
 * <p>
 * Create a concrete subclass of this test suite and implement {@link #createInjector()} so that
 * {@code GraphDtabase.class} is bound to your implementation instance as a singleton.
 */
public abstract class GraphDatabaseTest extends RepositoryTestCase {

    protected GraphDatabase database;

    @Override
    protected void setUpInternal() throws Exception {
        database = (GraphDatabase) geogit.getRepository().getGraphDatabase();
    }

    protected abstract Injector createInjector();

    @Test
    public void testNodes() throws IOException {
        ObjectId rootId = ObjectId.forString("root commit");
        ImmutableList<ObjectId> parents = ImmutableList.of();
        database.put(rootId, parents);
        ObjectId commit1 = ObjectId.forString("commit1");
        parents = ImmutableList.of(rootId);
        database.put(commit1, parents);
        ObjectId commit2 = ObjectId.forString("commit2");
        parents = ImmutableList.of(commit1);
        database.put(commit2, parents);

        ImmutableList<ObjectId> children = database.getChildren(commit2);
        parents = database.getParents(commit2);
        assertTrue(database.exists(commit2));
        assertEquals("Size of " + children, 0, children.size());
        assertEquals(1, parents.size());
        assertEquals(commit1, parents.get(0));
        children = database.getChildren(commit1);
        parents = database.getParents(commit1);
        assertTrue(database.exists(commit1));
        assertEquals(1, children.size());
        assertEquals(commit2, children.get(0));
        assertEquals(1, parents.size());
        assertEquals(rootId, parents.get(0));
        children = database.getChildren(rootId);
        parents = database.getParents(rootId);
        assertTrue(database.exists(rootId));
        assertEquals(1, children.size());
        assertEquals(commit1, children.get(0));
        assertEquals(0, parents.size());
    }

    @Test
    public void testFindCommonAncestor1() throws IOException {
        // Create the following revision graph
        // o - root commit
        // |\
        // | o - commit1
        // |
        // o - commit2
        ObjectId rootId = ObjectId.forString("root commit");
        ImmutableList<ObjectId> parents = ImmutableList.of();
        database.put(rootId, parents);
        ObjectId commit1 = ObjectId.forString("commit1");
        parents = ImmutableList.of(rootId);
        database.put(commit1, parents);
        ObjectId commit2 = ObjectId.forString("commit2");
        database.put(commit2, parents);

        Optional<ObjectId> ancestor = database.findLowestCommonAncestor(commit1, commit2);
        assertTrue(ancestor.isPresent());
        assertEquals(rootId, ancestor.get());
    }

    @Test
    public void testFindCommonAncestor2() throws IOException {
        // Create the following revision graph
        // o - root commit
        // |\
        // | o - commit1
        // | |
        // | o - commit2
        // | |
        // | o - commit3
        // |
        // o - commit4
        ObjectId rootId = ObjectId.forString("root commit");
        ImmutableList<ObjectId> parents = ImmutableList.of();
        database.put(rootId, parents);
        ObjectId commit1 = ObjectId.forString("commit1");
        parents = ImmutableList.of(rootId);
        database.put(commit1, parents);
        ObjectId commit2 = ObjectId.forString("commit2");
        parents = ImmutableList.of(commit1);
        database.put(commit2, parents);
        ObjectId commit3 = ObjectId.forString("commit3");
        parents = ImmutableList.of(commit2);
        database.put(commit3, parents);
        ObjectId commit4 = ObjectId.forString("commit4");
        parents = ImmutableList.of(rootId);
        database.put(commit4, parents);

        Optional<ObjectId> ancestor = database.findLowestCommonAncestor(commit3, commit4);
        assertTrue(ancestor.isPresent());
        assertEquals(rootId, ancestor.get());
    }

    @Test
    public void testFindCommonAncestor3() throws IOException {
        // Create the following revision graph
        // o - root commit
        // |\
        // | o - commit1
        // | |
        // | o - commit2
        // | |\
        // | | o - commit3
        // | |
        // o | - commit4
        // | |
        // | o - commit5
        // |/
        // o - commit6
        ObjectId rootId = ObjectId.forString("root commit");
        ImmutableList<ObjectId> parents = ImmutableList.of();
        database.put(rootId, parents);
        ObjectId commit1 = ObjectId.forString("commit1");
        parents = ImmutableList.of(rootId);
        database.put(commit1, parents);
        ObjectId commit2 = ObjectId.forString("commit2");
        parents = ImmutableList.of(commit1);
        database.put(commit2, parents);
        ObjectId commit3 = ObjectId.forString("commit3");
        parents = ImmutableList.of(commit2);
        database.put(commit3, parents);
        ObjectId commit4 = ObjectId.forString("commit4");
        parents = ImmutableList.of(rootId);
        database.put(commit4, parents);
        ObjectId commit5 = ObjectId.forString("commit5");
        parents = ImmutableList.of(commit2);
        database.put(commit5, parents);
        ObjectId commit6 = ObjectId.forString("commit6");
        parents = ImmutableList.of(commit4, commit5);
        database.put(commit6, parents);

        Optional<ObjectId> ancestor = database.findLowestCommonAncestor(commit6, commit3);
        assertTrue(ancestor.isPresent());
        assertEquals(commit2, ancestor.get());
    }

    @Test
    public void testFindCommonAncestor4() throws IOException {
        // Create the following revision graph
        // o - root commit
        // |\
        // | o - commit1
        // | |
        // | o - commit2
        // | |\
        // | | o - commit3
        // | | |\
        // | | | o - commit4
        // | | | |
        // | | o | - commit5
        // | | |/
        // | | o - commit6
        // | |
        // o | - commit7
        // | |
        // | o - commit8
        // |/
        // o - commit9
        ObjectId rootId = ObjectId.forString("root commit");
        ImmutableList<ObjectId> parents = ImmutableList.of();
        database.put(rootId, parents);
        ObjectId commit1 = ObjectId.forString("commit1");
        parents = ImmutableList.of(rootId);
        database.put(commit1, parents);
        ObjectId commit2 = ObjectId.forString("commit2");
        parents = ImmutableList.of(commit1);
        database.put(commit2, parents);
        ObjectId commit3 = ObjectId.forString("commit3");
        parents = ImmutableList.of(commit2);
        database.put(commit3, parents);
        ObjectId commit4 = ObjectId.forString("commit4");
        parents = ImmutableList.of(commit3);
        database.put(commit4, parents);
        ObjectId commit5 = ObjectId.forString("commit5");
        parents = ImmutableList.of(commit3);
        database.put(commit5, parents);
        ObjectId commit6 = ObjectId.forString("commit6");
        parents = ImmutableList.of(commit5, commit4);
        database.put(commit6, parents);
        ObjectId commit7 = ObjectId.forString("commit7");
        parents = ImmutableList.of(rootId);
        database.put(commit7, parents);
        ObjectId commit8 = ObjectId.forString("commit8");
        parents = ImmutableList.of(commit2);
        database.put(commit8, parents);
        ObjectId commit9 = ObjectId.forString("commit9");
        parents = ImmutableList.of(commit7, commit8);
        database.put(commit9, parents);

        Optional<ObjectId> ancestor = database.findLowestCommonAncestor(commit9, commit6);
        assertTrue(ancestor.isPresent());
        assertEquals(commit2, ancestor.get());
    }

    @Test
    public void testMapNode() throws IOException {
        ObjectId commitId = ObjectId.forString("commitId");
        ObjectId mappedId = ObjectId.forString("mapped");
        database.map(mappedId, commitId);
        ObjectId mapping = database.getMapping(mappedId);
        assertEquals(commitId, mapping);

        // update mapping
        ObjectId commitId2 = ObjectId.forString("commitId2");
        database.map(mappedId, commitId2);
        mapping = database.getMapping(mappedId);
        assertEquals(commitId2, mapping);
    }

    @Test
    public void testSparsePath() throws IOException {
        // Create the following revision graph
        // o - root commit
        // |\
        // | o - commit1
        // | |
        // | o - commit2
        // | |\
        // | | o - commit3
        // | | |\
        // | | | o - commit4 (Sparse)
        // | | | |
        // | | o | - commit5
        // | | |/
        // | | o - commit6
        // | |
        // o | - commit7
        // | |
        // | o - commit8
        // |/
        // o - commit9
        ObjectId rootId = ObjectId.forString("root commit");
        ImmutableList<ObjectId> parents = ImmutableList.of();
        database.put(rootId, parents);
        ObjectId commit1 = ObjectId.forString("commit1");
        parents = ImmutableList.of(rootId);
        database.put(commit1, parents);
        ObjectId commit2 = ObjectId.forString("commit2");
        parents = ImmutableList.of(commit1);
        database.put(commit2, parents);
        ObjectId commit3 = ObjectId.forString("commit3");
        parents = ImmutableList.of(commit2);
        database.put(commit3, parents);
        ObjectId commit4 = ObjectId.forString("commit4");
        parents = ImmutableList.of(commit3);
        database.put(commit4, parents);
        database.setProperty(commit4, GraphDatabase.SPARSE_FLAG, "true");
        ObjectId commit5 = ObjectId.forString("commit5");
        parents = ImmutableList.of(commit3);
        database.put(commit5, parents);
        ObjectId commit6 = ObjectId.forString("commit6");
        parents = ImmutableList.of(commit5, commit4);
        database.put(commit6, parents);
        ObjectId commit7 = ObjectId.forString("commit7");
        parents = ImmutableList.of(rootId);
        database.put(commit7, parents);
        ObjectId commit8 = ObjectId.forString("commit8");
        parents = ImmutableList.of(commit2);
        database.put(commit8, parents);
        ObjectId commit9 = ObjectId.forString("commit9");
        parents = ImmutableList.of(commit7, commit8);
        database.put(commit9, parents);

        assertTrue(database.isSparsePath(commit6, rootId));
        assertFalse(database.isSparsePath(commit5, rootId));
        assertTrue(database.isSparsePath(commit4, commit1));
        assertFalse(database.isSparsePath(commit9, rootId));
        assertFalse(database.isSparsePath(commit9, commit2));
    }

    @Test
    public void testSparsePathMapped() throws IOException {
        // Create the following mapped revision graph
        // o--------o - root commit
        // |\ . . . |\
        // | o------|-o - commit1
        // | | . . .| |
        // | o------|-o - commit2 (sparse)
        // | | .. --|/
        // | o---/. | - commit4 (mapped to commit 2)
        // | . . . .|
        // o--------o - commit3
        ObjectId rootId = ObjectId.forString("root commit");
        ImmutableList<ObjectId> parents = ImmutableList.of();
        database.put(rootId, parents);
        ObjectId rootMap = ObjectId.forString("root mapped");
        database.put(rootMap, parents);
        database.map(rootId, rootMap);
        database.map(rootMap, rootId);
        ObjectId commit1 = ObjectId.forString("commit1");
        parents = ImmutableList.of(rootId);
        database.put(commit1, parents);
        ObjectId commit1Map = ObjectId.forString("commit1 mapped");
        parents = ImmutableList.of(rootMap);
        database.put(commit1Map, parents);
        database.map(commit1, commit1Map);
        database.map(commit1Map, commit1);
        ObjectId commit2 = ObjectId.forString("commit2");
        parents = ImmutableList.of(commit1);
        database.put(commit2, parents);
        ObjectId commit2Map = ObjectId.forString("commit2 mapped");
        parents = ImmutableList.of(commit1Map);
        database.put(commit2Map, parents);
        database.map(commit2, commit2Map);
        database.map(commit2Map, commit2);
        ObjectId commit4Map = ObjectId.forString("commit4 mapped");
        parents = ImmutableList.of(commit2Map);
        database.put(commit4Map, parents);
        database.map(commit4Map, commit2);
        database.setProperty(commit2, GraphDatabase.SPARSE_FLAG, "true");
        ObjectId commit3 = ObjectId.forString("commit3");
        parents = ImmutableList.of(rootId);
        database.put(commit3, parents);
        ObjectId commit3Map = ObjectId.forString("commit3 mapped");
        parents = ImmutableList.of(rootMap);
        database.put(commit3Map, parents);
        database.map(commit3, commit3Map);
        database.map(commit3Map, commit3);

        assertTrue(database.isSparsePath(commit2, rootId));

    }

    @Test
    public void testDepth() throws IOException {
        // Create the following revision graph
        // x o - root commit
        // | |\
        // | | o - commit1
        // | | |
        // | | o - commit2
        // | | |\
        // | | | o - commit3
        // | | | |\
        // | | | | o - commit4
        // | | | | |
        // | | | o | - commit5
        // | | | |/
        // | | | o - commit6
        // | | |
        // | o | - commit7
        // | | |
        // | | o - commit8
        // | |/
        // | o - commit9
        // |
        // o - commit10
        // |
        // o - commit11
        ObjectId rootId = ObjectId.forString("root commit");
        ImmutableList<ObjectId> parents = ImmutableList.of();
        database.put(rootId, parents);
        ObjectId commit1 = ObjectId.forString("commit1");
        parents = ImmutableList.of(rootId);
        database.put(commit1, parents);
        ObjectId commit2 = ObjectId.forString("commit2");
        parents = ImmutableList.of(commit1);
        database.put(commit2, parents);
        ObjectId commit3 = ObjectId.forString("commit3");
        parents = ImmutableList.of(commit2);
        database.put(commit3, parents);
        ObjectId commit4 = ObjectId.forString("commit4");
        parents = ImmutableList.of(commit3);
        database.put(commit4, parents);
        ObjectId commit5 = ObjectId.forString("commit5");
        parents = ImmutableList.of(commit3);
        database.put(commit5, parents);
        ObjectId commit6 = ObjectId.forString("commit6");
        parents = ImmutableList.of(commit5, commit4);
        database.put(commit6, parents);
        ObjectId commit7 = ObjectId.forString("commit7");
        parents = ImmutableList.of(rootId);
        database.put(commit7, parents);
        ObjectId commit8 = ObjectId.forString("commit8");
        parents = ImmutableList.of(commit2);
        database.put(commit8, parents);
        ObjectId commit9 = ObjectId.forString("commit9");
        parents = ImmutableList.of(commit7, commit8);
        database.put(commit9, parents);
        ObjectId commit10 = ObjectId.forString("commit10");
        parents = ImmutableList.of();
        database.put(commit10, parents);
        ObjectId commit11 = ObjectId.forString("commit11");
        parents = ImmutableList.of(commit10);
        database.put(commit11, parents);

        System.out.println("Testing depth");
        assertEquals(0, database.getDepth(rootId));
        System.out.println("Testing depth 9");
        assertEquals(2, database.getDepth(commit9));
        System.out.println("Testing depth 8");
        assertEquals(3, database.getDepth(commit8));
        System.out.println("Testing depth 6");
        assertEquals(5, database.getDepth(commit6));
        System.out.println("Testing depth 4");
        assertEquals(4, database.getDepth(commit4));
        System.out.println("Testing depth 11");
        assertEquals(1, database.getDepth(commit11));
    }
}
