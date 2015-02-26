/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.di;

import org.geogit.api.AbstractGeoGitOp;
import org.geogit.api.CommandLocator;
import org.geogit.api.Platform;
import org.geogit.repository.StagingArea;
import org.geogit.repository.WorkingTree;
import org.geogit.storage.RefDatabase;

import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * Provides a method for finding and creating instances of GeoGit operations.
 * 
 * @see Injector
 * @see AbstractGeoGitOp
 */
public class GuiceCommandLocator implements CommandLocator {

    private Injector injector;

    /**
     * Constructs a new {@code GuiceCommandLocator} with the given {@link Injector}.
     * 
     * @param injector the injector which has commands bound to it
     */
    @Inject
    public GuiceCommandLocator(Injector injector) {
        this.injector = injector;
    }

    /**
     * Finds and returns an instance of a command of the specified class.
     * 
     * @param commandClass the kind of command to locate and instantiate
     * @return a new instance of the requested command class, with its dependencies resolved
     */
    @Override
    public <T extends AbstractGeoGitOp<?>> T command(Class<T> commandClass) {
        T instance = injector.getInstance(commandClass);
        return instance;
    }

    @Override
    public WorkingTree getWorkingTree() {
        return injector.getInstance(WorkingTree.class);
    }

    @Override
    public StagingArea getIndex() {
        return injector.getInstance(StagingArea.class);
    }

    @Override
    public RefDatabase getRefDatabase() {
        return injector.getInstance(RefDatabase.class);
    }

    @Override
    public Platform getPlatform() {
        return injector.getInstance(Platform.class);
    }
}
