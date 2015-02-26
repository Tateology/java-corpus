/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.api.plumbing.merge;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.geogit.api.AbstractGeoGitOp;
import org.geogit.api.Platform;
import org.geogit.api.plumbing.ResolveGeogitDir;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.io.Files;
import com.google.inject.Inject;

public class ReadMergeCommitMessageOp extends AbstractGeoGitOp<String> {

    private Platform platform;

    @Inject
    public ReadMergeCommitMessageOp(Platform platform) {
        checkNotNull(platform);
        this.platform = platform;
    }

    @Override
    public String call() {
        URL envHome = new ResolveGeogitDir(platform).call().get();
        try {
            File file = new File(envHome.toURI());
            file = new File(file, "MERGE_MSG");
            if (!file.exists()) {
                return "";
            }
            List<String> lines = Files.readLines(file, Charsets.UTF_8);
            return Joiner.on("\n").join(lines);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

}