/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.api.plumbing.merge;

import java.net.URL;
import java.util.List;

import org.geogit.api.AbstractGeoGitOp;
import org.geogit.api.plumbing.ResolveGeogitDir;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class ConflictsReadOp extends AbstractGeoGitOp<List<Conflict>> {
    @Override
    public List<Conflict> call() {
        final Optional<URL> repoUrl = getCommandLocator().command(ResolveGeogitDir.class).call();
        if (repoUrl.isPresent()) {
            return getIndex().getDatabase().getConflicts(null, null);
        } else {
            return ImmutableList.of();
        }
    }
}
