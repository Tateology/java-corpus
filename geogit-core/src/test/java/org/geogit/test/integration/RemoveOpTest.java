/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.test.integration;

import java.util.Iterator;
import java.util.List;

import org.geogit.api.NodeRef;
import org.geogit.api.ObjectId;
import org.geogit.api.Ref;
import org.geogit.api.RevCommit;
import org.geogit.api.plumbing.LsTreeOp;
import org.geogit.api.plumbing.RefParse;
import org.geogit.api.plumbing.RevParse;
import org.geogit.api.plumbing.diff.DiffEntry;
import org.geogit.api.plumbing.merge.Conflict;
import org.geogit.api.porcelain.AddOp;
import org.geogit.api.porcelain.BranchCreateOp;
import org.geogit.api.porcelain.CheckoutOp;
import org.geogit.api.porcelain.CommitOp;
import org.geogit.api.porcelain.MergeConflictsException;
import org.geogit.api.porcelain.MergeOp;
import org.geogit.api.porcelain.RemoveOp;
import org.geogit.api.porcelain.ResetOp;
import org.geogit.api.porcelain.ResetOp.ResetMode;
import org.geogit.repository.WorkingTree;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opengis.feature.Feature;

import com.google.common.base.Optional;
import com.google.common.base.Suppliers;

public class RemoveOpTest extends RepositoryTestCase {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Override
    protected void setUpInternal() throws Exception {
    }

    @Test
    public void testSingleFeatureRemoval() throws Exception {
        populate(false, points1, points2, points3);

        String featureId = points1.getIdentifier().getID();
        String path = NodeRef.appendChild(pointsName, featureId);
        geogit.command(RemoveOp.class).addPathToRemove(path).call();

        Optional<ObjectId> id = geogit.command(RevParse.class)
                .setRefSpec(Ref.WORK_HEAD + ":" + path).call();
        assertFalse(id.isPresent());
        id = geogit.command(RevParse.class).setRefSpec(Ref.STAGE_HEAD + ":" + path).call();
        assertFalse(id.isPresent());
    }

    @Test
    public void testMultipleRemoval() throws Exception {
        populate(false, points1, points2, points3);

        String featureId = points1.getIdentifier().getID();
        String path = NodeRef.appendChild(pointsName, featureId);
        String featureId2 = points2.getIdentifier().getID();
        String path2 = NodeRef.appendChild(pointsName, featureId2);

        geogit.command(RemoveOp.class).addPathToRemove(path).addPathToRemove(path2).call();

        Optional<ObjectId> id = geogit.command(RevParse.class)
                .setRefSpec(Ref.WORK_HEAD + ":" + path).call();
        assertFalse(id.isPresent());
        id = geogit.command(RevParse.class).setRefSpec(Ref.STAGE_HEAD + ":" + path).call();
        assertFalse(id.isPresent());
        id = geogit.command(RevParse.class).setRefSpec(Ref.WORK_HEAD + ":" + path2).call();
        assertFalse(id.isPresent());
        id = geogit.command(RevParse.class).setRefSpec(Ref.STAGE_HEAD + ":" + path2).call();
        assertFalse(id.isPresent());
    }

    @Test
    public void testTreeRemoval() throws Exception {
        populate(false, points1, points2, points3, lines1, lines2);

        geogit.command(RemoveOp.class).addPathToRemove(pointsName).call();
        Optional<ObjectId> id = geogit.command(RevParse.class)
                .setRefSpec(Ref.WORK_HEAD + ":" + pointsName).call();
        assertFalse(id.isPresent());
        id = geogit.command(RevParse.class).setRefSpec(Ref.STAGE_HEAD + ":" + pointsName).call();
        List<DiffEntry> list = toList(repo.getIndex().getStaged(null));
        assertFalse(id.isPresent());
        id = geogit.command(RevParse.class).setRefSpec(Ref.STAGE_HEAD + ":" + linesName).call();
        assertTrue(id.isPresent());
    }

    @Test
    public void testUnexistentPathRemoval() throws Exception {
        populate(false, points1, points2, points3);

        try {
            geogit.command(RemoveOp.class).addPathToRemove("wrong/wrong.1").call();
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testRemovalFixesConflict() throws Exception {
        Feature points1Modified = feature(pointsType, idP1, "StringProp1_2", new Integer(1000),
                "POINT(1 1)");
        Feature points1ModifiedB = feature(pointsType, idP1, "StringProp1_3", new Integer(2000),
                "POINT(1 1)");
        insertAndAdd(points1);
        geogit.command(CommitOp.class).call();
        geogit.command(BranchCreateOp.class).setName("TestBranch").call();
        insertAndAdd(points1Modified);
        geogit.command(CommitOp.class).call();
        geogit.command(CheckoutOp.class).setSource("TestBranch").call();
        insertAndAdd(points1ModifiedB);
        insertAndAdd(points2);
        geogit.command(CommitOp.class).call();

        geogit.command(CheckoutOp.class).setSource("master").call();
        Ref branch = geogit.command(RefParse.class).setName("TestBranch").call().get();
        try {
            geogit.command(MergeOp.class).addCommit(Suppliers.ofInstance(branch.getObjectId()))
                    .call();
            fail();
        } catch (MergeConflictsException e) {
            assertTrue(e.getMessage().contains("conflict"));
        }
        String path = NodeRef.appendChild(pointsName, idP1);
        geogit.command(RemoveOp.class).addPathToRemove(path).call();
        List<Conflict> conflicts = geogit.getRepository().getIndex().getDatabase()
                .getConflicts(null, null);
        assertTrue(conflicts.isEmpty());
        geogit.command(CommitOp.class).call();
        Optional<Ref> ref = geogit.command(RefParse.class).setName(Ref.MERGE_HEAD).call();
        assertFalse(ref.isPresent());
    }

    // TODO: Remove this test
    @SuppressWarnings(value = { "unused" })
    @Test
    public void testRemovalOfAllFeaturesOfAGivenType() throws Exception {
        List<RevCommit> commits = populate(false, points1, points2, points3, lines1, lines2);

        String featureId = lines1.getIdentifier().getID();
        String path = NodeRef.appendChild(linesName, featureId);
        String featureId2 = lines2.getIdentifier().getID();
        String path2 = NodeRef.appendChild(linesName, featureId2);

        WorkingTree tree = geogit.command(RemoveOp.class).addPathToRemove(path)
                .addPathToRemove(path2).call();

        geogit.command(AddOp.class).call();

        RevCommit commit = geogit.command(CommitOp.class).setMessage("Removed lines").call();
        Iterator<NodeRef> nodes = geogit.command(LsTreeOp.class).call();

        while (nodes.hasNext()) {
            NodeRef node = nodes.next();
            assertNotNull(node);
        }

        geogit.command(ResetOp.class).setMode(ResetMode.HARD).call();

        nodes = geogit.command(LsTreeOp.class).call();
        while (nodes.hasNext()) {
            NodeRef node = nodes.next();
            assertNotNull(node);
        }
    }

}
