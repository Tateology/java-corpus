/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.test.integration;

import static org.mockito.Mockito.mock;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.geogit.api.ObjectId;
import org.geogit.api.porcelain.ShowOp;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ShowOpTest extends RepositoryTestCase {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Override
    protected void setUpInternal() throws Exception {
        repo.getConfigDatabase().put("user.name", "groldan");
        repo.getConfigDatabase().put("user.email", "groldan@opengeo.org");
    }

    @Test
    public void testShowOp() throws FileNotFoundException {

        PrintStream stream = mock(PrintStream.class);
        exception.expect(UnsupportedOperationException.class);
        geogit.command(ShowOp.class).setPrintStream(stream).setObjectId(ObjectId.NULL).call();
    }
}
