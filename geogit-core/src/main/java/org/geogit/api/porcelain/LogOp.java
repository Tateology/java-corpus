/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.api.porcelain;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geogit.api.AbstractGeoGitOp;
import org.geogit.api.ObjectId;
import org.geogit.api.Ref;
import org.geogit.api.RevCommit;
import org.geogit.api.plumbing.RevParse;
import org.geogit.api.plumbing.diff.DiffEntry;
import org.geogit.di.CanRunDuringConflict;
import org.geogit.repository.Repository;
import org.geogit.storage.GraphDatabase;
import org.geotools.util.Range;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

/**
 * Operation to query the commits logs.
 * <p>
 * The list of commits to return can be filtered by setting the following properties:
 * <ul>
 * <li> {@link #setLimit(int) limit}: Limits the number of commits to return.
 * <li> {@link #setTimeRange(Range) timeRange}: return commits that fall in to the given time range.
 * <li> {@link #setSince(ObjectId) since}...{@link #setUntil(ObjectId) until}: Show only commits
 * between the named two commits.
 * <li> {@link #addPath(String) addPath}: Show only commits that affect the specified path.
 * </ul>
 * </p>
 * 
 * 
 */
@CanRunDuringConflict
public class LogOp extends AbstractGeoGitOp<Iterator<RevCommit>> {

    private static final Range<Long> ALWAYS = new Range<Long>(Long.class, 0L, true, Long.MAX_VALUE,
            true);

    private Range<Long> timeRange;

    private Integer skip;

    private Integer limit;

    private ObjectId since;

    private ObjectId until;

    private Set<String> paths;

    private Repository repository;

    private Pattern author;

    private Pattern commiter;

    private boolean topo;

    private boolean firstParent;

    private GraphDatabase graphDb;

    private List<ObjectId> commits = Lists.newArrayList();

    /**
     * Constructs a new {@code LogOp} with the given {@link Repository}.
     * 
     * @param repository the repository to log commits from
     */
    @Inject
    public LogOp(final Repository repository, GraphDatabase graphDb) {
        this.repository = repository;
        this.graphDb = graphDb;
        timeRange = ALWAYS;
    }

    /**
     * @param skip sets the number of commits to skip from the commit list
     * @return {@code this}
     */
    public LogOp setSkip(int skip) {
        Preconditions.checkArgument(skip > 0, "skip shall be > 0: " + skip);
        this.skip = Integer.valueOf(skip);
        return this;
    }

    /**
     * @param limit sets the limit for the amount of commits to show
     * @return {@code this}
     */
    public LogOp setLimit(int limit) {
        Preconditions.checkArgument(limit > 0, "limit shall be > 0: " + limit);
        this.limit = Integer.valueOf(limit);
        return this;
    }

    /**
     * Indicates to return only commits newer than the given one ({@code since} is exclusive)
     * 
     * @param since the initial (oldest and exclusive) commit id, ({@code null} sets the default)
     * @return {@code this}
     * @see #setUntil(ObjectId)
     */
    public LogOp setSince(final ObjectId since) {
        this.since = since;
        return this;
    }

    /**
     * Indicates to return commits up to the provided one, inclusive.
     * 
     * @param until the final (newest and inclusive) commit id, ({@code null} sets the default)
     * @return {@code this}
     * @see #setSince(ObjectId)
     */
    public LogOp setUntil(ObjectId until) {
        this.until = until;
        return this;
    }

    /**
     * Sets whether commits should be ordered not according to its date, but to is structure in the
     * history branches
     * 
     * @param topo true if commits should be ordered not according to its date, but to is structure
     *        in the history branches
     * @return {@code this}
     */
    public LogOp setTopoOrder(boolean topo) {
        this.topo = topo;
        return this;
    }

    /**
     * Sets whether the log should list the first parent of each commit
     * 
     * @param firstParent true if it should show only the first parent
     * @return {@code this}
     */
    public LogOp setFirstParentOnly(boolean firstParent) {
        this.firstParent = firstParent;
        return this;
    }

    /**
     * Adds a commit to be used as starting point for computing history. If no commit is provided,
     * HEAD is used, or the 'until' commit if provided
     * 
     * @param branch the branch to use
     * @return {@code this}
     */
    public LogOp addCommit(ObjectId commit) {
        this.commits.add(commit);
        return this;
    }

    /**
     * Sets the regexp to filter out author names
     * 
     * @param the regexp to use for filtering author names
     * @return {@code this}
     */
    public LogOp setAuthor(String author) {
        this.author = Pattern.compile(author);
        return this;
    }

    /**
     * Sets the regexp to filter out commiter names
     * 
     * @param the regexp to use for filtering commiter names
     * @return {@code this}
     */
    public LogOp setCommiter(String commiter) {
        this.commiter = Pattern.compile(commiter);
        return this;
    }

    /**
     * Show only commits that affect any of the specified paths.
     * 
     * @param path
     * @return {@code this}
     */
    public LogOp addPath(final String path) {
        Preconditions.checkNotNull(path);

        if (this.paths == null) {
            this.paths = new HashSet<String>();
        }
        this.paths.add(path);
        return this;
    }

    /**
     * Show only commits that lie within the specified time range.
     * 
     * @param commitRange time range to show commits from
     * @return {@code this}
     */
    public LogOp setTimeRange(final Range<Date> commitRange) {
        if (commitRange == null) {
            this.timeRange = ALWAYS;
        } else {
            this.timeRange = new Range<Long>(Long.class, commitRange.getMinValue().getTime(),
                    commitRange.isMinIncluded(), commitRange.getMaxValue().getTime(),
                    commitRange.isMaxIncluded());
        }
        return this;
    }

    /**
     * Executes the log operation.
     * 
     * @return the list of commits that satisfy the query criteria, most recent first.
     * @see org.geogit.api.AbstractGeoGitOp#call()
     */
    @Override
    public Iterator<RevCommit> call() {

        ObjectId newestCommitId;
        ObjectId oldestCommitId;
        {
            if (this.until == null) {
                newestCommitId = command(RevParse.class).setRefSpec(Ref.HEAD).call().get();
            } else {
                if (!repository.commitExists(this.until)) {
                    throw new IllegalArgumentException(
                            "Provided 'until' commit id does not exist: " + until.toString());
                }
                newestCommitId = this.until;
            }
            if (this.since == null) {
                oldestCommitId = ObjectId.NULL;
            } else {
                if (!repository.commitExists(this.since)) {
                    throw new IllegalArgumentException(
                            "Provided 'since' commit id does not exist: " + since.toString());
                }
                oldestCommitId = this.since;
            }
        }

        Iterator<RevCommit> history;
        if (firstParent) {
            history = new LinearHistoryIterator(newestCommitId, repository);
        } else {
            if (commits.isEmpty()) {
                commits.add(newestCommitId);
            }
            if (topo) {
                history = new TopologicalHistoryIterator(commits, repository, graphDb);
            } else {
                history = new ChronologicalHistoryIterator(commits, repository);
            }
        }
        LogFilter filter = new LogFilter(oldestCommitId, timeRange, paths, author, commiter);
        Iterator<RevCommit> filteredCommits = Iterators.filter(history, filter);
        if (skip != null) {
            // Iterators.advance(filteredCommits, skip.intValue());
        }
        if (limit != null) {
            filteredCommits = null; //Iterators.limit(filteredCommits, limit.intValue());
        }
        return filteredCommits;
    }

    /**
     * Iterator that traverses the commit history backwards starting from the provided commit, in
     * chronological order. It performs a reverse breadth-first search
     * 
     */
    private static class ChronologicalHistoryIterator extends AbstractIterator<RevCommit> {

        private final Repository repo;

        private Set<RevCommit> parents;

        /**
         * Constructs a new {@code LinearHistoryIterator} with the given parameters.
         * 
         * @param tip the first commit in the history
         * @param repo the repository where the commits are stored.
         */
        public ChronologicalHistoryIterator(final List<ObjectId> tips, final Repository repo) {
            parents = Sets.newHashSet();
            for (ObjectId tip : tips) {
                if (!tip.isNull()) {
                    final RevCommit commit = repo.getCommit(tip);
                    parents.add(commit);
                }
            }
            this.repo = repo;
        }

        /**
         * Calculates the next commit in the history.
         * 
         * @return the next {@link RevCommit commit} in the history
         */
        @Override
        protected RevCommit computeNext() {
            if (parents.isEmpty()) {
                return endOfData();
            } else {
                Iterator<RevCommit> iter = parents.iterator();
                // TODO: Maybe we should make RevCommit implement Comparable?
                RevCommit mostRecent = iter.next();
                while (iter.hasNext()) {
                    RevCommit commit = iter.next();
                    if (mostRecent.getCommitter().getTimestamp() < commit.getCommitter()
                            .getTimestamp()) {
                        mostRecent = commit;
                    }
                }
                parents.remove(mostRecent);
                RevCommit commit;
                for (ObjectId parent : mostRecent.getParentIds()) {
                    if (repo.commitExists(parent)) {
                        commit = repo.getCommit(parent);
                        parents.add(commit);
                    }
                }
                return mostRecent;
            }

        }
    }

    /**
     * Iterator that traverses the commit history backwards starting from the provided commit, in
     * topological order. It performs a reverse depth-first search
     * 
     */
    private static class TopologicalHistoryIterator extends AbstractIterator<RevCommit> {

        private final Repository repo;

        private Stack<RevCommit> tips;

        private RevCommit lastCommit;

        private List<ObjectId> stopPoints;

        private GraphDatabase graphDb;

        /**
         * Constructs a new {@code LinearHistoryIterator} with the given parameters.
         * 
         * @param tipsList the list of tips to start computing history from
         * @param repo the repository where the commits are stored.
         * @param graphDb
         */
        public TopologicalHistoryIterator(final List<ObjectId> tipsList, final Repository repo,
                GraphDatabase graphDb) {
            this.graphDb = graphDb;
            tips = new Stack<RevCommit>();
            stopPoints = Lists.newArrayList();
            for (ObjectId tip : tipsList) {
                if (!tip.isNull()) {
                    final RevCommit commit = repo.getCommit(tip);
                    tips.add(commit);
                    stopPoints.add(tip);
                }
            }
            this.repo = repo;
        }

        /**
         * Calculates the next commit in the history.
         * 
         * @return the next {@link RevCommit commit} in the history
         */
        @Override
        protected RevCommit computeNext() {
            if (lastCommit == null) {
                lastCommit = tips.pop();
                return lastCommit;
            }
            Optional<ObjectId> parent = Optional.absent();
            int index = 0;
            for (ObjectId parentId : lastCommit.getParentIds()) {
                if (repo.commitExists(parentId)) {
                    parent = Optional.of(parentId);
                    break;
                }
                index++;
            }
            if (!parent.isPresent() || parent.get().isNull() || stopPoints.contains(parent.get())) {
                // move to the next tip and start traversing it
                if (tips.isEmpty()) {
                    return endOfData();
                } else {
                    lastCommit = tips.pop();
                }
            } else {
                List<ObjectId> parents = lastCommit.getParentIds();
                for (int i = index + 1; i < parents.size(); i++) {
                    if (repo.commitExists(parents.get(i))) {
                        final RevCommit commit = repo.getCommit(parents.get(i));
                        tips.push(commit);
                    }
                }
                lastCommit = repo.getCommit(parent.get());
                ImmutableList<ObjectId> children = this.graphDb.getChildren(parent.get());
                if (children.size() > 1) {
                    stopPoints.add(parent.get());
                }
            }

            return lastCommit;
        }
    }

    /**
     * Iterator that traverses the commit history backwards starting from the provided commit, using
     * only the first parent of each commit
     * 
     */
    private static class LinearHistoryIterator extends AbstractIterator<RevCommit> {

        private Optional<ObjectId> nextCommitId;

        private final Repository repo;

        /**
         * Constructs a new {@code LinearHistoryIterator} with the given parameters.
         * 
         * @param tip the first commit in the history
         * @param repo the repository where the commits are stored.
         */
        @SuppressWarnings("unchecked")
        public LinearHistoryIterator(final ObjectId tip, final Repository repo) {
            this.nextCommitId = (Optional<ObjectId>) (tip.isNull() ? Optional.absent() : Optional
                    .of(tip));
            this.repo = repo;
        }

        /**
         * Calculates the next commit in the history.
         * 
         * @return the next {@link RevCommit commit} in the history
         */
        @Override
        protected RevCommit computeNext() {
            if (nextCommitId.isPresent()) {
                RevCommit commit = repo.getCommit(nextCommitId.get());
                nextCommitId = commit.parentN(0);
                if (nextCommitId.isPresent() && !repo.commitExists(nextCommitId.get())) {
                    nextCommitId = Optional.absent();
                }
                return commit;
            }
            return endOfData();
        }

    }

    /**
     * Checks whether the given commit satisfies all the filter criteria set to this op.
     * 
     * @return {@code true} if the commit satisfies the filter criteria set to this op
     */
    private class LogFilter implements Predicate<RevCommit> {

        private boolean toReached;

        private final ObjectId oldestCommitId;

        private final Range<Long> timeRange;

        private final Set<String> paths;

        private Pattern author;

        private Pattern committer;

        /**
         * Constructs a new {@code LogFilter} with the given parameters.
         * 
         * @param oldestCommitId the oldest commit, exclusive. Indicates when to stop evaluating.
         * @param timeRange extra time range filter besides oldest commit
         * @param paths extra filter on content, indicates to return only commits that affected any
         *        of the provided paths
         * @param commiter the regexp pattern to filter author names
         * @param author the regexp pattern to filter commiter names
         */
        public LogFilter(final ObjectId oldestCommitId, final Range<Long> timeRange,
                final Set<String> paths, Pattern author, Pattern commiter) {
            Preconditions.checkNotNull(oldestCommitId);
            Preconditions.checkNotNull(timeRange);
            this.oldestCommitId = oldestCommitId;
            this.timeRange = timeRange;
            this.paths = paths;
            this.author = author;
            this.committer = commiter;
        }

        /**
         * @return {@code true} if the commit satisfies the filter criteria set to this op
         * @see com.google.common.base.Predicate#apply(java.lang.Object)
         */
        @Override
        public boolean apply(final RevCommit commit) {
            if (toReached) {
                return false;
            }
            if (oldestCommitId.equals(commit.getId())) {
                toReached = true;
                return false;
            }
            Optional<String> authorName = commit.getAuthor().getName();
            if (author != null && authorName.isPresent()) {
                Matcher authorMatcher = author.matcher(authorName.get());
                if (!authorMatcher.matches()) {
                    return false;
                }
            }
            Optional<String> committerName = commit.getCommitter().getName();
            if (committer != null && committerName.isPresent()) {
                Matcher committerMatcher = committer.matcher(committerName.get());
                if (!committerMatcher.matches()) {
                    return false;
                }
            }
            boolean applies = timeRange
                    .contains(Long.valueOf(commit.getCommitter().getTimestamp()));
            if (!applies) {
                return false;
            }
            if (paths != null && paths.size() > 0) {
                // did this commit touch any of the paths?
                for (String path : paths) {
                    DiffOp diff = command(DiffOp.class);
                    ObjectId parentId = commit.parentN(0).or(ObjectId.NULL);
                    if (!parentId.equals(ObjectId.NULL) && !repository.commitExists(parentId)) {
                        // we have reached the bottom of a shallow clone. We "fake" it and pretend
                        // it is the real first commit of the repo
                        parentId = ObjectId.NULL;
                    }
                    Iterator<DiffEntry> diffResult;
                    try {
                        diff.setOldVersion(parentId).setNewVersion(commit.getId()).setFilter(path);
                        diffResult = diff.call();
                        applies = diffResult.hasNext();
                        if (applies) {
                            break;
                        }
                    } catch (Exception e) {
                        Throwables.propagate(e);
                    }
                }
            }

            return applies;
        }
    }

}
