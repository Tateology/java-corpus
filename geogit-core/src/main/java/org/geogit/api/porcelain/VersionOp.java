/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.api.porcelain;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.geogit.api.AbstractGeoGitOp;
import org.geogit.di.CanRunDuringConflict;

import com.google.common.base.Throwables;
import com.google.inject.Inject;

/**
 * Retrieves GeoGit version information.
 * 
 */
@CanRunDuringConflict
public class VersionOp extends AbstractGeoGitOp<VersionInfo> {

    /**
     * Constructs a new {@code VersionOp}.
     */
    @Inject
    public VersionOp() {
    }

    /**
     * Executes the Version operation.
     * 
     * @return the version info of the current build
     * @see org.geogit.api.AbstractGeoGitOp#call()
     */
    public VersionInfo call() {
        Properties properties = new Properties();
        VersionInfo info = null;
        try {
            InputStream resource = getClass().getClassLoader()
                    .getResourceAsStream("git.properties");
            if (resource != null) {
                properties.load(resource);
                info = new VersionInfo(properties);
            }
        } catch (IOException e) {
            Throwables.propagate(e);
        }
        return info;

    }

}
