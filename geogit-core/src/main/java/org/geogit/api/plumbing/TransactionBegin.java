/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.api.plumbing;

import java.util.UUID;
import java.util.concurrent.TimeoutException;

import org.geogit.api.AbstractGeoGitOp;
import org.geogit.api.GeogitTransaction;
import org.geogit.repository.Repository;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.inject.Inject;

/**
 * Creates a new {@link GeogitTransaction} and copies all of the repository refs for that
 * transaction to use.
 * 
 * @see GeogitTransaction
 */
public class TransactionBegin extends AbstractGeoGitOp<GeogitTransaction> {

    private Repository repository;

    /**
     * Constructs a new {@code TransactionBegin} with the given parameters.
     * 
     * @param repository the geogit repository
     */
    @Inject
    public TransactionBegin(final Repository repository) {
        this.repository = repository;
    }

    /**
     * Creates a new transaction and returns it.
     * 
     * @return the {@link GeogitTransaction} that was created by the operation
     */
    @Override
    public GeogitTransaction call() {
        Preconditions.checkState(!(commandLocator instanceof GeogitTransaction),
                "Cannot start a new transaction within a transaction!");

        GeogitTransaction t = new GeogitTransaction(commandLocator, repository, UUID.randomUUID());

        // Lock the repository
        try {
            getRefDatabase().lock();
        } catch (TimeoutException e) {
            Throwables.propagate(e);
        }
        try {
            // Copy original refs
            t.create();
        } finally {
            // Unlock the repository
            getRefDatabase().unlock();
        }
        // Return the transaction
        return t;
    }
}
