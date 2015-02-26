/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.api.plumbing.merge;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.net.URL;

import org.geogit.api.AbstractGeoGitOp;
import org.geogit.api.Platform;
import org.geogit.api.plumbing.ResolveGeogitDir;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.Files;
import com.google.inject.Inject;

public class SaveMergeCommitMessageOp extends AbstractGeoGitOp<Void> {

    private String message;

    private Platform platform;

    @Inject
    public SaveMergeCommitMessageOp(Platform platform) {
        checkNotNull(platform);
        this.platform = platform;
    }

    public SaveMergeCommitMessageOp setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public Void call() {
        URL envHome = new ResolveGeogitDir(platform).call().get();
        try {
            File file = new File(envHome.toURI());
            file = new File(file, "MERGE_MSG");
            Files.write(message, file, Charsets.UTF_8);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
        return null;
    }

}
