/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.api.porcelain;

import java.util.List;

import org.geogit.api.AbstractGeoGitOp;
import org.geogit.api.Remote;
import org.geogit.api.porcelain.RemoteException.StatusCode;
import org.geogit.storage.ConfigDatabase;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.inject.Inject;

/**
 * Finds a remote
 * 
 * @see Remote
 */
public class RemoteResolve extends AbstractGeoGitOp<Optional<Remote>> implements
        Supplier<Optional<Remote>> {

    private String name;

    final private ConfigDatabase config;

    /**
     * Constructs a new {@code RemoteAddOp} with the given config database.
     * 
     * @param config where to store the remote
     */
    @Inject
    public RemoteResolve(ConfigDatabase config) {
        this.config = config;
    }

    /**
     * @param name the name of the remote
     * @return {@code this}
     */
    public RemoteResolve setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Executes the remote-add operation.
     * 
     * @return the {@link Remote} that was added.
     */
    @Override
    public Optional<Remote> call() {
        if (name == null || name.isEmpty()) {
            throw new RemoteException(StatusCode.MISSING_NAME);
        }

        Optional<Remote> result = Optional.absent();

        List<String> allRemotes = config.getAllSubsections("remote");
        if (allRemotes.contains(name)) {

            String remoteSection = "remote." + name;
            Optional<String> remoteFetchURL = config.get(remoteSection + ".url");
            Optional<String> remoteFetch = config.get(remoteSection + ".fetch");
            Optional<String> remoteMapped = config.get(remoteSection + ".mapped");
            Optional<String> remoteMappedBranch = config.get(remoteSection + ".mappedBranch");
            if (remoteFetchURL.isPresent() && remoteFetch.isPresent()) {
                Optional<String> remotePushURL = config.get(remoteSection + ".pushurl");

                Remote remote = new Remote(name, remoteFetchURL.get(),
                        remotePushURL.or(remoteFetchURL.get()), remoteFetch.get(), remoteMapped.or(
                                "false").equals("true"), remoteMappedBranch.orNull());
                result = Optional.of(remote);
            }
        }
        return result;
    }

    @Override
    public Optional<Remote> get() {
        return call();
    }
}
