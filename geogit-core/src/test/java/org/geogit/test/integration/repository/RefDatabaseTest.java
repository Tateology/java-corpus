/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.test.integration.repository;

import java.util.Arrays;

import org.geogit.api.ObjectId;
import org.geogit.api.Ref;
import org.geogit.storage.RefDatabase;
import org.geogit.test.integration.RepositoryTestCase;
import org.junit.Test;

public class RefDatabaseTest extends RepositoryTestCase {

    private RefDatabase refDb;

    @Override
    protected void setUpInternal() throws Exception {
        refDb = repo.getRefDatabase();
    }

    @Test
    public void testEmpty() {
        assertEquals(ObjectId.NULL.toString(), refDb.getRef(Ref.MASTER));
        assertEquals(Ref.MASTER, refDb.getSymRef(Ref.HEAD));
    }

    @Test
    public void testPutGetRef() {
        byte[] raw = new byte[20];
        Arrays.fill(raw, (byte) 1);
        ObjectId oid = new ObjectId(raw);

        assertEquals(ObjectId.NULL.toString(), refDb.getRef(Ref.MASTER));

        refDb.putRef(Ref.MASTER, oid.toString());

        assertEquals(oid.toString(), refDb.getRef(Ref.MASTER));
    }

    @Test
    public void testPutGetSymRef() {

        String branch = "refs/heads/branch";

        assertEquals(Ref.MASTER, refDb.getSymRef(Ref.HEAD));

        refDb.putSymRef(Ref.HEAD, branch);

        assertEquals(branch, refDb.getSymRef(Ref.HEAD));
    }
}
