/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.api;

import java.util.UUID;

import javax.annotation.Nullable;

import org.geogit.api.plumbing.TransactionEnd;
import org.geogit.api.porcelain.ConflictsException;
import org.geogit.repository.Index;
import org.geogit.repository.Repository;
import org.geogit.repository.StagingArea;
import org.geogit.repository.WorkingTree;
import org.geogit.storage.RefDatabase;
import org.geogit.storage.TransactionRefDatabase;
import org.geogit.storage.TransactionStagingArea;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

/**
 * Provides a method of performing concurrent operations on a single Geogit repository.
 * 
 * @see org.geogit.api.plumbing.TransactionBegin
 * @see org.geogit.api.plumbing.TransactionEnd
 */
public class GeogitTransaction implements CommandLocator {

    public static final String TRANSACTIONS_NAMESPACE = "transactions";

    public static final String TRANSACTIONS_DIR = TRANSACTIONS_NAMESPACE + "/";

    private UUID transactionId;

    private CommandLocator locator;

    private final StagingArea transactionIndex;

    private final WorkingTree transactionWorkTree;

    private final TransactionRefDatabase transactionRefDatabase;

    private Optional<String> authorName = Optional.absent();

    private Optional<String> authorEmail = Optional.absent();

    /**
     * Constructs the transaction with the given ID and Injector.
     * 
     * @param locator the non transactional command locator
     * @param transactionId the id of the transaction
     */
    public GeogitTransaction(CommandLocator locator, Repository repository, UUID transactionId) {
        Preconditions.checkArgument(!(locator instanceof GeogitTransaction));
        this.locator = locator;
        this.transactionId = transactionId;

        transactionIndex = new TransactionStagingArea(new Index(
                repository.getIndex().getDatabase(), this), transactionId);
        transactionWorkTree = new WorkingTree(repository.getIndex().getDatabase(), this);
        transactionRefDatabase = new TransactionRefDatabase(repository.getRefDatabase(),
                transactionId);
    }

    public void create() {
        transactionRefDatabase.create();
    }

    public void close() {
        transactionRefDatabase.close();
    }

    /**
     * 
     * @param authorName name of the author of this transaction
     * @param authorEmail email of the author of this transaction
     * @return {@code this}
     */
    public GeogitTransaction setAuthor(@Nullable String authorName, @Nullable String authorEmail) {
        this.authorName = Optional.fromNullable(authorName);
        this.authorEmail = Optional.fromNullable(authorEmail);
        return this;
    }

    /**
     * @return the transaction id of the transaction
     */
    public UUID getTransactionId() {
        return transactionId;
    }

    @Override
    public WorkingTree getWorkingTree() {
        return transactionWorkTree;
    }

    @Override
    public StagingArea getIndex() {
        return transactionIndex;
    }

    @Override
    public RefDatabase getRefDatabase() {
        return transactionRefDatabase;
    }

    /**
     * Finds and returns an instance of a command of the specified class.
     * 
     * @param commandClass the kind of command to locate and instantiate
     * @return a new instance of the requested command class, with its dependencies resolved
     */
    @Override
    public <T extends AbstractGeoGitOp<?>> T command(Class<T> commandClass) {
        T instance = locator.command(commandClass);
        instance.setCommandLocator(this);
        return instance;
    }

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName()).append('[').append(transactionId)
                .append(']').toString();
    }

    public void commit() throws ConflictsException {
        locator.command(TransactionEnd.class).setAuthor(authorName.orNull(), authorEmail.orNull())
                .setTransaction(this).setCancel(false).setRebase(true).call();
    }

    public void commitSyncTransaction() throws ConflictsException {
        locator.command(TransactionEnd.class).setAuthor(authorName.orNull(), authorEmail.orNull())
                .setTransaction(this).setCancel(false).call();
    }

    public void abort() {
        locator.command(TransactionEnd.class).setTransaction(this).setCancel(true).call();
    }

    @Override
    public Platform getPlatform() {
        return locator.getPlatform();
    }

}
