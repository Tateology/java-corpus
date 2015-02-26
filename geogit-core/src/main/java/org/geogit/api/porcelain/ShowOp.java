/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.api.porcelain;

import java.io.PrintStream;

import org.geogit.api.AbstractGeoGitOp;
import org.geogit.api.ObjectId;
import org.geogit.di.CanRunDuringConflict;
import org.geogit.repository.Repository;

import com.google.inject.Inject;

/**
 * Pulls the object with the given {@link ObjectId} from the repository and prints it to the given
 * {@link PrintStream}.
 * 
 * @see ObjectId
 * @see Repository
 * @see PrintStream
 */
@CanRunDuringConflict
public class ShowOp extends AbstractGeoGitOp<Void> {

    private PrintStream out;

    private ObjectId oid;

    private Repository repo;

    /**
     * Constructs a new {@code ShowOp} with the given repository.
     * 
     * @param repository the repository where the object is stored
     */
    @Inject
    public ShowOp(final Repository repository) {
        this.repo = repository;
        this.out = System.err;
    }

    /**
     * @param out the stream to print the object to
     * @return {@code this}
     */
    public ShowOp setPrintStream(final PrintStream out) {
        this.out = out;
        return this;
    }

    /**
     * @param oid the id for the object to print
     * @return {@code this}
     */
    public ShowOp setObjectId(final ObjectId oid) {
        this.oid = oid;
        return this;
    }

    /**
     * Executes the show operation.
     * 
     * @return {@code Void}
     */
    @Override
    public Void call() {
        throw new UnsupportedOperationException("not yet implemented");
    }

}
