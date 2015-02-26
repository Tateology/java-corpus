/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.api;

import org.geogit.repository.StagingArea;
import org.geogit.repository.WorkingTree;
import org.geogit.storage.RefDatabase;

/**
 * Service locator interface for acquiring command instances
 */
public interface CommandLocator {

    /**
     * Finds and returns an instance of a command of the specified class.
     * 
     * @param commandClass the kind of command to locate and instantiate
     * @return a new instance of the requested command class, with its dependencies resolved
     */
    public <T extends AbstractGeoGitOp<?>> T command(Class<T> commandClass);

    public WorkingTree getWorkingTree();

    /**
     * @return
     */
    public StagingArea getIndex();

    /**
     * @return
     */
    public RefDatabase getRefDatabase();
    
    public Platform getPlatform();
}