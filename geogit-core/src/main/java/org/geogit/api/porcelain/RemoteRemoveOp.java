/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.api.porcelain;

import java.util.List;

import org.geogit.api.AbstractGeoGitOp;
import org.geogit.api.Ref;
import org.geogit.api.Remote;
import org.geogit.api.plumbing.LsRemote;
import org.geogit.api.plumbing.UpdateRef;
import org.geogit.api.porcelain.RemoteException.StatusCode;
import org.geogit.storage.ConfigDatabase;

import com.google.common.base.Optional;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

/**
 * Removes a remote from the local config database.
 * 
 * @see ConfigDatabase
 */
public class RemoteRemoveOp extends AbstractGeoGitOp<Remote> {

    private String name;

    final private ConfigDatabase config;

    /**
     * Constructs a new {@code RemoteRemoveOp} with the given config database.
     * 
     * @param config where the remote is stored
     */
    @Inject
    public RemoteRemoveOp(ConfigDatabase config) {
        this.config = config;
    }

    /**
     * Executes the remote-remove operation.
     * 
     * @return the {@link Remote} that was removed, or {@link Optional#absent()} if the remote
     *         didn't exist.
     */
    @Override
    public Remote call() {
        if (name == null || name.isEmpty()) {
            throw new RemoteException(StatusCode.MISSING_NAME);
        }
        List<String> allRemotes = config.getAllSubsections("remote");
        if (!allRemotes.contains(name)) {
            throw new RemoteException(StatusCode.REMOTE_NOT_FOUND);
        }

        Remote remote = null;
        String remoteSection = "remote." + name;
        Optional<String> remoteFetchURL = config.get(remoteSection + ".url");
        Optional<String> remoteFetch = config.get(remoteSection + ".fetch");
        Optional<String> remotePushURL = Optional.absent();
        Optional<String> remoteMapped = config.get(remoteSection + ".mapped");
        Optional<String> remoteMappedBranch = config.get(remoteSection + ".mappedBranch");
        if (remoteFetchURL.isPresent() && remoteFetch.isPresent()) {
            remotePushURL = config.get(remoteSection + ".pushurl");
        }

        remote = new Remote(name, remoteFetchURL.or(""), remotePushURL.or(remoteFetchURL.or("")),
                remoteFetch.or(""), remoteMapped.or("false").equals("true"),
                remoteMappedBranch.orNull());

        config.removeSection(remoteSection);

        // Remove refs
        final ImmutableSet<Ref> localRemoteRefs = command(LsRemote.class).retrieveLocalRefs(true)
                .setRemote(Suppliers.ofInstance(Optional.of(remote))).call();

        for (Ref localRef : localRemoteRefs) {
            command(UpdateRef.class).setDelete(true).setName(localRef.getName()).call();
        }

        return remote;
    }

    /**
     * @param name the name of the remote to remove
     * @return {@code this}
     */
    public RemoteRemoveOp setName(String name) {
        this.name = name;
        return this;
    }

}
