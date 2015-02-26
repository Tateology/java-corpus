/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.storage;

import org.geogit.api.ObjectId;
import org.geogit.repository.RepositoryConnectionException;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

@Beta
public interface GraphDatabase {

    public static final String SPARSE_FLAG = "sparse";

    /**
     * Initializes/opens the databse. It's safe to call this method multiple times, and only the
     * first call shall take effect.
     */
    public void open();

    /**
     * Perform GeoGit configuration before the first connection to the database.
     */
    public void configure() throws RepositoryConnectionException;

    /**
     * Verify the configuration before opening the database
     */
    public void checkConfig() throws RepositoryConnectionException;

    /**
     * @return true if the database is open, false otherwise
     */
    public boolean isOpen();

    /**
     * Closes the database.
     */
    public void close();

    /**
     * Determines if the given commit exists in the graph database.
     * 
     * @param commitId the commit id to search for
     * @return true if the commit exists, false otherwise
     */
    public boolean exists(final ObjectId commitId);

    /**
     * Retrieves all of the parents for the given commit.
     * 
     * @param commitid the commit whose parents should be returned
     * @return a list of the parents of the provided commit
     * @throws IllegalArgumentException
     */
    public ImmutableList<ObjectId> getParents(ObjectId commitId) throws IllegalArgumentException;

    /**
     * Retrieves all of the children for the given commit.
     * 
     * @param commitid the commit whose children should be returned
     * @return a list of the children of the provided commit
     * @throws IllegalArgumentException
     */
    public ImmutableList<ObjectId> getChildren(ObjectId commitId) throws IllegalArgumentException;

    /**
     * Adds a commit to the database with the given parents. If a commit with the same id already
     * exists, it will not be inserted.
     * 
     * @param commitId the commit id to insert
     * @param parentIds the commit ids of the commit's parents
     * @return true if the commit id was inserted or updated, false if it was already there
     */
    public boolean put(final ObjectId commitId, ImmutableList<ObjectId> parentIds);

    /**
     * Maps a commit to another original commit. This is used in sparse repositories.
     * 
     * @param mapped the id of the mapped commit
     * @param original the commit to map to
     */
    public void map(final ObjectId mapped, final ObjectId original);

    /**
     * Gets the id of the commit that this commit is mapped to.
     * 
     * @param commitId the commit to find the mapping of
     * @return the mapped commit id
     */
    public ObjectId getMapping(final ObjectId commitId);

    /**
     * Gets the number of ancestors of the commit until it reaches one with no parents, for example
     * the root or an orphaned commit.
     * 
     * @param commitId the commit id to start from
     * @return the depth of the commit
     */
    public int getDepth(final ObjectId commitId);

    /**
     * Finds the lowest common ancestor of two commits.
     * 
     * @param leftId the commit id of the left commit
     * @param rightId the commit id of the right commit
     * @return An {@link Optional} of the lowest common ancestor of the two commits, or
     *         {@link Optional#absent()} if a common ancestor could not be found.
     */
    public Optional<ObjectId> findLowestCommonAncestor(ObjectId leftId, ObjectId rightId);

    /**
     * Set a property on the provided commit node.
     * 
     * @param commitId the id of the commit
     */
    public void setProperty(ObjectId commitId, String propertyName, String propertyValue);

    /**
     * Determines if there are any sparse commits between the start commit and the end commit, not
     * including the end commit.
     * 
     * @param start the start commit
     * @param end the end commit
     * @return true if there are any sparse commits between start and end
     */
    public boolean isSparsePath(ObjectId start, ObjectId end);

    public void truncate();
}
