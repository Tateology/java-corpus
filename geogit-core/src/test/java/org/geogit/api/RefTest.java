/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.api;

import org.geogit.api.porcelain.CommitOp;
import org.geogit.test.integration.RepositoryTestCase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class RefTest extends RepositoryTestCase {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Override
    protected void setUpInternal() throws Exception {
        repo.getConfigDatabase().put("user.name", "groldan");
        repo.getConfigDatabase().put("user.email", "groldan@opengeo.org");
    }

    @Test
    public void testConstructor() throws Exception {
        insertAndAdd(points1);
        RevCommit oid = geogit.command(CommitOp.class).call();

        Ref testRef = new Ref(Ref.REFS_PREFIX + "commit1", oid.getId(), RevObject.TYPE.COMMIT);

        assertEquals(Ref.REFS_PREFIX + "commit1", testRef.getName());
        assertEquals(Ref.REFS_PREFIX, testRef.namespace());
        assertEquals("commit1", testRef.localName());
        assertEquals(oid.getId(), testRef.getObjectId());
        assertEquals(RevObject.TYPE.COMMIT, testRef.getType());
    }

    @Test
    public void testToString() throws Exception {
        insertAndAdd(points1);
        RevCommit oid = geogit.command(CommitOp.class).call();

        Ref testRef = new Ref(Ref.REFS_PREFIX + "commit1", oid.getId(), RevObject.TYPE.COMMIT);

        assertEquals("Ref[" + testRef.getName() + " -> " + testRef.getObjectId().toString() + "]",
                testRef.toString());
    }

    @Test
    public void testEquals() throws Exception {
        insertAndAdd(points1);
        RevCommit oid = geogit.command(CommitOp.class).call();

        Ref testRef = new Ref(Ref.REFS_PREFIX + "commit1", oid.getId(), RevObject.TYPE.COMMIT);

        insertAndAdd(lines1);
        RevCommit oid2 = geogit.command(CommitOp.class).call();

        Ref testRef2 = new Ref(Ref.REFS_PREFIX + "commit2", oid2.getId(), RevObject.TYPE.COMMIT);

        assertFalse(testRef.equals(testRef2));

        testRef2 = new Ref(Ref.REFS_PREFIX + "commit1", oid.getId(), RevObject.TYPE.TREE);

        assertFalse(testRef.equals(testRef2));

        testRef2 = new Ref(Ref.REFS_PREFIX + "commit1", oid.getTreeId(), RevObject.TYPE.COMMIT);

        assertFalse(testRef.equals(testRef2));

        assertFalse(testRef.equals("not a ref"));

        assertTrue(testRef.equals(testRef));
    }

    @Test
    public void testCompare() throws Exception {
        insertAndAdd(points1);
        RevCommit oid = geogit.command(CommitOp.class).call();

        Ref testRef = new Ref(Ref.REFS_PREFIX + "commit1", oid.getId(), RevObject.TYPE.COMMIT);

        insertAndAdd(lines1);
        RevCommit oid2 = geogit.command(CommitOp.class).call();

        Ref testRef2 = new Ref(Ref.REFS_PREFIX + "commit2", oid2.getId(), RevObject.TYPE.COMMIT);

        assertTrue(testRef.compareTo(testRef2) < 0);
        assertTrue(testRef2.compareTo(testRef) > 0);
        assertEquals(0, testRef.compareTo(testRef));
    }

    @Test
    public void testLocalNameAndNamespace() {
        String ref = Ref.localName(Ref.HEADS_PREFIX + "branch1");
        assertEquals("branch1", ref);

        ref = Ref.localName(Ref.REFS_PREFIX + "commit1");
        assertEquals("commit1", ref);

        ref = Ref.localName(Ref.REMOTES_PREFIX + "origin/branch1");
        assertEquals("branch1", ref);

        ref = Ref.localName(Ref.TAGS_PREFIX + "tag1");
        assertEquals("tag1", ref);

        ref = Ref.localName("ref1");
        assertEquals("ref1", ref);

        ref = Ref.namespace(Ref.HEADS_PREFIX + "branch1");
        assertEquals(Ref.HEADS_PREFIX, ref);

        ref = Ref.namespace(Ref.REFS_PREFIX + "commit1");
        assertEquals(Ref.REFS_PREFIX, ref);

        ref = Ref.namespace(Ref.REMOTES_PREFIX + "origin/branch1");
        assertEquals(Ref.REMOTES_PREFIX + "/origin", ref);

        ref = Ref.namespace(Ref.TAGS_PREFIX + "tag1");
        assertEquals(Ref.TAGS_PREFIX, ref);

        ref = Ref.namespace("ref1");
        assertEquals("ref1", ref);
    }

    @Test
    public void testAppendAndChild() {
        String ref = "ref1";
        ref = Ref.append(Ref.HEADS_PREFIX, ref);
        assertEquals(Ref.HEADS_PREFIX + "ref1", ref);
        ref = Ref.child(Ref.HEADS_PREFIX, ref);
        assertEquals("ref1", ref);

        ref = Ref.append("", ref);
        assertEquals("ref1", ref);

        ref = Ref.append(Ref.HEADS_PREFIX, ref + "/");
        assertEquals(Ref.HEADS_PREFIX + "ref1", ref);

        ref = Ref.child(Ref.HEADS_PREFIX.substring(0, Ref.HEADS_PREFIX.length() - 1), ref);
        assertEquals("ref1", ref);

        ref = Ref.append(Ref.HEADS_PREFIX, "/" + ref);
        assertEquals(Ref.HEADS_PREFIX + "ref1", ref);

        ref = Ref.append(ref, "");
        assertEquals(Ref.HEADS_PREFIX + "ref1", ref);
    }
}
