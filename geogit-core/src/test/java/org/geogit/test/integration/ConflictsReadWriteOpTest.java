/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.test.integration;

import java.util.ArrayList;
import java.util.List;

import org.geogit.api.ObjectId;
import org.geogit.api.plumbing.merge.Conflict;
import org.geogit.api.plumbing.merge.ConflictsReadOp;
import org.geogit.api.plumbing.merge.ConflictsWriteOp;
import org.junit.Test;

import com.google.common.collect.Lists;

public class ConflictsReadWriteOpTest extends RepositoryTestCase {

    @Override
    protected void setUpInternal() throws Exception {
    }

    @Test
    public void testReadWriteConflicts() throws Exception {
        Conflict conflict = new Conflict(idP1, ObjectId.forString("ancestor"),
                ObjectId.forString("ours"), ObjectId.forString("theirs"));
        Conflict conflict2 = new Conflict(idP2, ObjectId.forString("ancestor2"),
                ObjectId.forString("ours2"), ObjectId.forString("theirs2"));
        ArrayList<Conflict> conflicts = Lists.newArrayList(conflict, conflict2);
        geogit.command(ConflictsWriteOp.class).setConflicts(conflicts).call();
        List<Conflict> returnedConflicts = geogit.command(ConflictsReadOp.class).call();
        assertEquals(conflicts, returnedConflicts);
    }

}
