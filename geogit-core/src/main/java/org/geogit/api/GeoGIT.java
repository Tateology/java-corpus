/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.api;

import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.net.URL;

import javax.annotation.Nullable;

import org.geogit.api.plumbing.ResolveGeogitDir;
import org.geogit.api.plumbing.diff.DiffObjectCount;
import org.geogit.api.porcelain.InitOp;
import org.geogit.repository.Repository;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.inject.Injector;

/**
 * A facade to Geo-GIT operations.
 * <p>
 * Represents the checkout of user's working tree and repository and provides the operations to work
 * on them.
 * </p>
 * 
 */
public class GeoGIT {

    private Injector injector;

    private Repository repository;

    /**
     * Constructs a new instance of the GeoGit facade.
     */
    public GeoGIT() {
        injector = GlobalInjectorBuilder.builder.build();// Guice.createInjector(new
                                                         // GeogitModule());
    }

    /**
     * Constructs a new instance of the GeoGit facade with the given working directory.
     * 
     * @param workingDir the working directory for this instance of GeoGit
     */
    public GeoGIT(File workingDir) {
        this();
        injector.getInstance(Platform.class).setWorkingDir(workingDir);
    }

    /**
     * Constructs a new instance of the GeoGit facade with the given Guice injector
     * 
     * @param injector the injector to use
     * @see Injector
     */
    public GeoGIT(final Injector injector) {
        this(injector, null);
    }

    /**
     * Constructs a new instance of the GeoGit facade with the given Guice injector and working
     * directory.
     * 
     * @param injector the injector to use
     * @param workingDir the working directory for this instance of GeoGit
     * @see Injector
     */
    public GeoGIT(final Injector injector, @Nullable final File workingDir) {
        Preconditions.checkNotNull(injector, "injector");
        this.injector = injector;
        if (workingDir != null) {
            Platform instance = injector.getInstance(Platform.class);
            instance.setWorkingDir(workingDir);
        }
    }

    /**
     * Closes the current repository.
     */
    public void close() {
        if (repository != null) {
            repository.close();
            repository = null;
        }
        injector = null;
    }

    /**
     * Finds and returns an instance of a command of the specified class.
     * 
     * @param commandClass the kind of command to locate and instantiate
     * @return a new instance of the requested command class, with its dependencies resolved
     */
    public <T extends AbstractGeoGitOp<?>> T command(Class<T> commandClass) {
        return injector.getInstance(commandClass);
    }

    /**
     * Sets the repository for this GeoGIT instance.
     * 
     * @param repository
     */
    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    /**
     * Obtains the repository for the current directory or creates a new one and returns it if no
     * repository can be found on the current directory.
     * 
     * @return the existing or newly created repository, never {@code null}
     * @throws RuntimeException if the repository cannot be created at the current directory
     * @see InitOp
     */
    public Repository getOrCreateRepository() {
        if (getRepository() == null) {
            try {
                repository = command(InitOp.class).call();
                checkState(repository != null,
                        "Repository shouldn't be null as we checked it didn't exist before calling init");
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }
        return repository;
    }

    /**
     * @return the configured repository or {@code null} if no repository is found on the current
     *         directory
     */
    public synchronized Repository getRepository() {
        if (repository != null) {
            return repository;
        }

        final Optional<URL> repoLocation = command(ResolveGeogitDir.class).call();
        if (repoLocation.isPresent()) {
            try {
                repository = injector.getInstance(Repository.class);
                repository.open();
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }
        return repository;
    }

    /**
     * @return the platform for this GeoGit facade
     */
    public Platform getPlatform() {
        return injector.getInstance(Platform.class);
    }

    /**
     * @return
     */
    public CommandLocator getCommandLocator() {
        return injector.getInstance(CommandLocator.class);
    }

    public DiffObjectCount countUnstaged() {
        return getRepository().getWorkingTree().countUnstaged(null);
    }

    public DiffObjectCount countStaged() {
        return getRepository().getIndex().countStaged(null);
    }

    public boolean isOpen() {
        return repository != null;
    }
}
