/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.storage.datastream;

import static org.geogit.storage.datastream.FormatCommon.COMMIT_AUTHOR_PREFIX;
import static org.geogit.storage.datastream.FormatCommon.COMMIT_COMMITTER_PREFIX;
import static org.geogit.storage.datastream.FormatCommon.COMMIT_PARENT_REF;
import static org.geogit.storage.datastream.FormatCommon.COMMIT_TREE_REF;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.geogit.api.ObjectId;
import org.geogit.api.RevCommit;
import org.geogit.storage.ObjectWriter;

public class CommitWriter implements ObjectWriter<RevCommit> {
    @Override
    public void write(RevCommit commit, OutputStream out) throws IOException {
        DataOutput data = new DataOutputStream(out);
        FormatCommon.writeHeader(data, "commit");
        data.writeByte(COMMIT_TREE_REF);
        data.write(commit.getTreeId().getRawValue());
        for (ObjectId pId : commit.getParentIds()) {
            data.writeByte(COMMIT_PARENT_REF);
            data.write(pId.getRawValue());
        }
        data.writeByte(COMMIT_AUTHOR_PREFIX);
        FormatCommon.writePerson(commit.getAuthor(), data);
        data.writeByte(COMMIT_COMMITTER_PREFIX);
        FormatCommon.writePerson(commit.getCommitter(), data);
        data.writeUTF(commit.getMessage());
    }
}
