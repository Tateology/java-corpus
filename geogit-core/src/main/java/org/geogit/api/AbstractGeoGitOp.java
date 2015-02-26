/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.api;

import java.util.concurrent.Callable;

import org.geogit.repository.StagingArea;
import org.geogit.repository.WorkingTree;
import org.geogit.storage.RefDatabase;
import org.geotools.util.NullProgressListener;
import org.geotools.util.SubProgressListener;
import org.opengis.util.ProgressListener;

import com.google.inject.Inject;

/**
 * Provides a base implementation for internal GeoGit operations.
 * 
 * @param <T> the type of the result of the execution of the command
 */
public abstract class AbstractGeoGitOp<T> implements Callable<T> {

    private static final ProgressListener NULL_PROGRESS_LISTENER = new NullProgressListener();

    private ProgressListener progressListener = NULL_PROGRESS_LISTENER;

    protected CommandLocator commandLocator;

    /**
     * Constructs a new abstract operation.
     */
    public AbstractGeoGitOp() {
        //
    }

    /**
     * Finds and returns an instance of a command of the specified class.
     * 
     * @param commandClass the kind of command to locate and instantiate
     * @return a new instance of the requested command class, with its dependencies resolved
     */
    public <C extends AbstractGeoGitOp<?>> C command(Class<C> commandClass) {
        return commandLocator.command(commandClass);
    }

    /**
     * @param locator the command locator to use when finding commands
     */
    @Inject
    public void setCommandLocator(CommandLocator locator) {
        this.commandLocator = locator;
    }

    /**
     * @param listener the progress listener to use
     * @return {@code this}
     */
    public AbstractGeoGitOp<T> setProgressListener(final ProgressListener listener) {
        this.progressListener = listener == null ? NULL_PROGRESS_LISTENER : listener;
        return this;
    }

    /**
     * @return the progress listener that is currently set
     */
    protected ProgressListener getProgressListener() {
        return progressListener;
    }

    /**
     * Constructs a new progress listener based on a specified sub progress amount.
     * 
     * @param amount amount of progress
     * @return the newly constructed progress listener
     */
    protected ProgressListener subProgress(float amount) {
        return new SubProgressListener(getProgressListener(), amount);
    }

    /**
     * Subclasses shall implement to do the real work.
     * 
     * @see java.util.concurrent.Callable#call()
     */
    public abstract T call();

    protected CommandLocator getCommandLocator() {
        return commandLocator;
    }

    /**
     * Shortcut for {@link CommandLocator#getWorkingTree() getCommandLocator().getWorkingTree()}
     */
    protected WorkingTree getWorkTree() {
        return getCommandLocator().getWorkingTree();
    }

    /**
     * Shortcut for {@link CommandLocator#getIndex() getCommandLocator().getIndex()}
     */
    protected StagingArea getIndex() {
        return getCommandLocator().getIndex();
    }

    /**
     * Shortcut for {@link CommandLocator#getRefDatabase() getCommandLocator().getRefDatabase()}
     */
    protected RefDatabase getRefDatabase() {
        return getCommandLocator().getRefDatabase();
    }

}
