/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.test.integration;

import org.geogit.api.Ref;
import org.geogit.api.plumbing.RefParse;
import org.geogit.api.plumbing.UpdateRef;
import org.geogit.api.porcelain.AddOp;
import org.geogit.api.porcelain.BranchCreateOp;
import org.geogit.api.porcelain.BranchDeleteOp;
import org.geogit.api.porcelain.CheckoutOp;
import org.geogit.api.porcelain.CommitOp;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.base.Optional;

public class BranchDeleteOpTest extends RepositoryTestCase {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Override
    protected void setUpInternal() throws Exception {
        repo.getConfigDatabase().put("user.name", "groldan");
        repo.getConfigDatabase().put("user.email", "groldan@opengeo.org");
    }

    @Test
    public void NoBranchNameTest() {
        BranchDeleteOp testOp = new BranchDeleteOp();
        testOp.setName(null);

        exception.expect(IllegalStateException.class);

        testOp.call();
    }

    @Test
    public void BranchNotPresentTest() {
        Optional<? extends Ref> branchref = geogit.command(BranchDeleteOp.class)
                .setName("noBranch").call();
        assertEquals(Optional.absent(), branchref);
    }

    @Test
    public void BranchPresentTest() throws Exception {
        insertAndAdd(points1);
        geogit.command(AddOp.class).call();
        geogit.command(CommitOp.class).call();
        geogit.command(BranchCreateOp.class).setName("TestBranch").call();
        geogit.command(BranchDeleteOp.class).setName("TestBranch").call();

        Optional<Ref> result = geogit.command(RefParse.class).setName("TestBranch").call();

        assertFalse(result.isPresent());
    }

    @Test
    public void BranchIsHeadTest() throws Exception {
        insertAndAdd(points1);
        geogit.command(AddOp.class).call();
        geogit.command(CommitOp.class).call();
        geogit.command(BranchCreateOp.class).setName("TestMasterBranch").call();

        geogit.command(BranchCreateOp.class).setName("TestBranch").call();
        geogit.command(CheckoutOp.class).setSource("TestBranch").call();

        insertAndAdd(points2);
        geogit.command(AddOp.class).call();
        geogit.command(CommitOp.class).call();

        exception.expect(IllegalStateException.class);
        geogit.command(BranchDeleteOp.class).setName("TestBranch").call();
    }

    @Test
    public void InvalidBranchNameTest() throws Exception {
        insertAndAdd(points1);
        geogit.command(AddOp.class).call();
        geogit.command(CommitOp.class).call();
        Ref testBranch = geogit.command(BranchCreateOp.class).setName("TestBranch").call();

        testBranch = geogit.command(UpdateRef.class).setName("TestBranch")
                .setNewValue(testBranch.getObjectId()).call().get();

        exception.expect(IllegalArgumentException.class);
        geogit.command(BranchDeleteOp.class).setName("TestBranch").call();
    }
}
