/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.storage.text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;

import org.geogit.api.ObjectId;
import org.geogit.api.RevCommit;
import org.geogit.api.RevObject.TYPE;
import org.geogit.storage.ObjectReader;
import org.geogit.storage.ObjectSerializingFactory;
import org.geogit.storage.RevCommitSerializationTest;
import org.junit.Test;

/**
 *
 */
public class RevCommitTextSerializationTest extends RevCommitSerializationTest {

    @Override
    protected ObjectSerializingFactory getObjectSerializingFactory() {
        return new TextSerializationFactory();
    }

    @Test
    public void testMalformedSerializedObject() throws Exception {

        // a missing entry
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
        writer.write(TYPE.COMMIT.name() + "\n");
        writer.write("tree\t" + ObjectId.forString("TREE_ID_STRING") + "\n");
        writer.write("author\tvolaya\tvolaya@opengeo.org\n");
        writer.write("commiter\tvolaya<volaya@opengeo.org>\n");
        writer.write("timestamp\t" + Long.toString(12345678) + "\n");
        writer.write("message\tMy message\n");
        writer.flush();

        ObjectReader<RevCommit> reader = factory.createCommitReader();
        try {
            reader.read(ObjectId.forString("ID_STRING"),
                    new ByteArrayInputStream(out.toByteArray()));
            fail();
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Expected parents"));
        }

        // a wrongly formatted author
        out = new ByteArrayOutputStream();
        writer = new OutputStreamWriter(out, "UTF-8");
        writer.write(TYPE.COMMIT.name() + "\n");
        writer.write("tree\t" + ObjectId.forString("TREE_ID_STRING") + "\n");
        writer.write("parents\t" + ObjectId.forString("PARENT_ID_STRING") + "\n");
        writer.write("author\tvolaya volaya@opengeo.org\n");
        writer.write("commiter\tvolaya volaya@opengeo.org\n");
        writer.write("timestamp\t" + Long.toString(12345678) + "\n");
        writer.write("message\tMy message\n");
        writer.flush();

        try {
            reader.read(ObjectId.forString("ID_STRING"),
                    new ByteArrayInputStream(out.toByteArray()));
            fail();
        } catch (Exception e) {
            assertTrue(true);
        }

        // a wrong category
        out = new ByteArrayOutputStream();
        writer = new OutputStreamWriter(out, "UTF-8");
        writer.write(TYPE.FEATURE.name() + "\n");
        writer.flush();
        try {
            reader.read(ObjectId.forString("ID_STRING"),
                    new ByteArrayInputStream(out.toByteArray()));
            fail();
        } catch (Exception e) {
            assertTrue(e.getMessage().equals("Wrong type: FEATURE"));
        }

    }

}
