/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.api.porcelain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.geogit.api.AbstractGeoGitOp;
import org.geogit.api.FeatureInfo;
import org.geogit.api.NodeRef;
import org.geogit.api.ObjectId;
import org.geogit.api.Ref;
import org.geogit.api.RevCommit;
import org.geogit.api.SymRef;
import org.geogit.api.plumbing.DiffTree;
import org.geogit.api.plumbing.FindCommonAncestor;
import org.geogit.api.plumbing.RefParse;
import org.geogit.api.plumbing.ResolveBranchId;
import org.geogit.api.plumbing.UpdateRef;
import org.geogit.api.plumbing.UpdateSymRef;
import org.geogit.api.plumbing.diff.DiffEntry;
import org.geogit.api.plumbing.merge.CheckMergeScenarioOp;
import org.geogit.api.plumbing.merge.Conflict;
import org.geogit.api.plumbing.merge.ConflictsWriteOp;
import org.geogit.api.plumbing.merge.MergeScenarioReport;
import org.geogit.api.plumbing.merge.ReportMergeScenarioOp;
import org.geogit.api.plumbing.merge.SaveMergeCommitMessageOp;
import org.geogit.repository.Repository;
import org.geotools.util.SubProgressListener;
import org.opengis.util.ProgressListener;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * 
 * Merge two or more histories together.
 * 
 */
public class MergeOp extends AbstractGeoGitOp<MergeOp.MergeReport> {

    private List<ObjectId> commits = new ArrayList<ObjectId>();;

    private String message = null;

    private Repository repository;

    private boolean ours;

    private boolean theirs;

    private boolean noCommit;

    private Optional<String> authorName = Optional.absent();

    private Optional<String> authorEmail = Optional.absent();

    /**
     * Constructs a new {@code MergeOp} using the specified parameters.
     * 
     * @param repository the repository to use
     */
    @Inject
    public MergeOp(Repository repository) {
        this.repository = repository;
    }

    /**
     * @param message the message for the merge commit
     * @return {@code this}
     */
    public MergeOp setMessage(final String message) {
        this.message = message;
        return this;
    }

    /**
     * Adds a commit whose history should be merged.
     * 
     * @param onto a supplier for the commit id
     * @return {@code this}
     */
    public MergeOp addCommit(final Supplier<ObjectId> commit) {
        Preconditions.checkNotNull(commit);

        this.commits.add(commit.get());
        return this;
    }

    /**
     * 
     * @param ours true if the "ours" strategy should be used
     * @return {@code this}
     */
    public MergeOp setOurs(boolean ours) {
        this.ours = ours;
        return this;
    }

    /**
     * 
     * @param ours true if the "theirs" strategy should be used
     * @return {@code this}
     */
    public MergeOp setTheirs(boolean theirs) {
        this.theirs = theirs;
        return this;
    }

    /**
     * 
     * @param ours true if no commit should be made after the merge, leaving just the index with the
     *        merge result
     * @return {@code this}
     */
    public MergeOp setNoCommit(boolean noCommit) {
        this.noCommit = noCommit;
        return this;
    }

    /**
     * 
     * @param author the author of the commit
     * @param email email of author
     * @return {@code this}
     */
    public MergeOp setAuthor(@Nullable String authorName, @Nullable String authorEmail) {
        this.authorName = Optional.fromNullable(authorName);
        this.authorEmail = Optional.fromNullable(authorEmail);
        return this;
    }

    /**
     * Executes the merge operation.
     * 
     * @return always {@code true}
     */
    @Override
    public MergeReport call() throws RuntimeException {

        Preconditions.checkArgument(commits.size() > 0, "No commits specified for merge.");
        Preconditions.checkArgument(!(ours && theirs), "Cannot use both --ours and --theirs.");

        final Optional<Ref> currHead = command(RefParse.class).setName(Ref.HEAD).call();
        Preconditions.checkState(currHead.isPresent(), "Repository has no HEAD, can't rebase.");
        Ref headRef = currHead.get();
        ObjectId oursId = headRef.getObjectId();
        // Preconditions.checkState(currHead.get() instanceof SymRef,
        // "Can't rebase from detached HEAD");
        // SymRef headRef = (SymRef) currHead.get();
        // final String currentBranch = headRef.getTarget();

        getProgressListener().started();

        boolean fastForward = true;
        boolean changed = false;

        Optional<MergeScenarioReport> mergeScenario = Optional.absent();

        List<CommitAncestorPair> pairs = Lists.newArrayList();

        boolean hasConflictsOrAutomerge;
        List<RevCommit> revCommits = Lists.newArrayList();
        if (!ObjectId.NULL.equals(headRef.getObjectId())) {
            revCommits.add(repository.getCommit(headRef.getObjectId()));
        }
        for (ObjectId commitId : commits) {
            revCommits.add(repository.getCommit(commitId));
        }
        hasConflictsOrAutomerge = command(CheckMergeScenarioOp.class).setCommits(revCommits).call()
                .booleanValue();

        if (hasConflictsOrAutomerge && !theirs) {
            Preconditions.checkState(commits.size() < 2,
                    "Conflicted merge.\nCannot merge more than two commits when conflicts exist"
                            + " or features have been modified in several histories");

            RevCommit headCommit = repository.getCommit(headRef.getObjectId());
            ObjectId commitId = commits.get(0);
            Preconditions.checkArgument(!ObjectId.NULL.equals(commitId),
                    "Cannot merge a NULL commit.");
            Preconditions.checkArgument(repository.commitExists(commitId), "Not a valid commit: "
                    + commitId.toString());

            final RevCommit targetCommit = repository.getCommit(commitId);
            Optional<RevCommit> ancestorCommit = command(FindCommonAncestor.class)
                    .setLeft(headCommit).setRight(targetCommit).call();

            pairs.add(new CommitAncestorPair(commitId, ancestorCommit.get().getId()));

            mergeScenario = Optional.of(command(ReportMergeScenarioOp.class)
                    .setMergeIntoCommit(headCommit).setToMergeCommit(targetCommit).call());

            List<FeatureInfo> merged = mergeScenario.get().getMerged();
            for (FeatureInfo feature : merged) {
                this.getWorkTree().insert(NodeRef.parentPath(feature.getPath()),
                        feature.getFeature());
                Iterator<DiffEntry> unstaged = getWorkTree().getUnstaged(null);
                getIndex().stage(getProgressListener(), unstaged, 0);
                changed = true;
                fastForward = false;
            }
            List<DiffEntry> unconflicting = mergeScenario.get().getUnconflicted();
            if (!unconflicting.isEmpty()) {
                getIndex().stage(getProgressListener(), unconflicting.iterator(), 0);
                changed = true;
                fastForward = false;
            }

            getWorkTree().updateWorkHead(getIndex().getTree().getId());

            List<Conflict> conflicts = mergeScenario.get().getConflicts();
            if (!ours && !conflicts.isEmpty()) {
                // In case we use the "ours" strategy, we do nothing. We ignore conflicting
                // changes and leave the current elements
                command(UpdateRef.class).setName(Ref.MERGE_HEAD).setNewValue(commitId).call();
                command(UpdateRef.class).setName(Ref.ORIG_HEAD).setNewValue(headCommit.getId())
                        .call();
                command(ConflictsWriteOp.class).setConflicts(conflicts).call();

                StringBuilder msg = new StringBuilder();
                Optional<Ref> ref = command(ResolveBranchId.class).setObjectId(commitId).call();
                if (ref.isPresent()) {
                    msg.append("Merge branch " + ref.get().getName());
                } else {
                    msg.append("Merge commit '" + commitId.toString() + "'. ");
                }
                msg.append("\n\nConflicts:\n");
                for (Conflict conflict : mergeScenario.get().getConflicts()) {
                    msg.append("\t" + conflict.getPath() + "\n");
                }

                command(SaveMergeCommitMessageOp.class).setMessage(msg.toString()).call();

                StringBuilder sb = new StringBuilder();
                for (Conflict conflict : conflicts) {
                    sb.append("CONFLICT: Merge conflict in " + conflict.getPath() + "\n");
                }
                sb.append("Automatic merge failed. Fix conflicts and then commit the result.\n");
                throw new MergeConflictsException(sb.toString(), headCommit.getId(), commitId);

            }
        } else {
            Preconditions.checkState(!hasConflictsOrAutomerge || commits.size() < 2,
                    "Conflicted merge.\nCannot merge more than two commits when conflicts exist"
                            + " or features have been modified in several histories");
            for (ObjectId commitId : commits) {
                ProgressListener subProgress = subProgress(100.f / commits.size());

                Preconditions.checkArgument(!ObjectId.NULL.equals(commitId),
                        "Cannot merge a NULL commit.");
                Preconditions.checkArgument(repository.commitExists(commitId),
                        "Not a valid commit: " + commitId.toString());

                subProgress.started();
                if (ObjectId.NULL.equals(headRef.getObjectId())) {
                    // Fast-forward
                    if (headRef instanceof SymRef) {
                        final String currentBranch = ((SymRef) headRef).getTarget();
                        command(UpdateRef.class).setName(currentBranch).setNewValue(commitId)
                                .call();
                        headRef = (SymRef) command(UpdateSymRef.class).setName(Ref.HEAD)
                                .setNewValue(currentBranch).call().get();
                    } else {
                        headRef = command(UpdateRef.class).setName(headRef.getName())
                                .setNewValue(commitId).call().get();
                    }

                    getWorkTree().updateWorkHead(commitId);
                    getIndex().updateStageHead(commitId);
                    subProgress.complete();
                    changed = true;
                    continue;
                }

                RevCommit headCommit = repository.getCommit(headRef.getObjectId());
                final RevCommit targetCommit = repository.getCommit(commitId);

                Optional<RevCommit> ancestorCommit = command(FindCommonAncestor.class)
                        .setLeft(headCommit).setRight(targetCommit).call();

                pairs.add(new CommitAncestorPair(commitId, ancestorCommit.get().getId()));

                subProgress.progress(10.f);

                Preconditions.checkState(ancestorCommit.isPresent(),
                        "No ancestor commit could be found.");

                if (commits.size() == 1) {
                    mergeScenario = Optional.of(command(ReportMergeScenarioOp.class)
                            .setMergeIntoCommit(headCommit).setToMergeCommit(targetCommit).call());
                    if (ancestorCommit.get().getId().equals(headCommit.getId())) {
                        // Fast-forward
                        if (headRef instanceof SymRef) {
                            final String currentBranch = ((SymRef) headRef).getTarget();
                            command(UpdateRef.class).setName(currentBranch).setNewValue(commitId)
                                    .call();
                            headRef = (SymRef) command(UpdateSymRef.class).setName(Ref.HEAD)
                                    .setNewValue(currentBranch).call().get();
                        } else {
                            headRef = command(UpdateRef.class).setName(headRef.getName())
                                    .setNewValue(commitId).call().get();
                        }

                        getWorkTree().updateWorkHead(commitId);
                        getIndex().updateStageHead(commitId);
                        subProgress.complete();
                        changed = true;
                        continue;
                    } else if (ancestorCommit.get().getId().equals(commitId)) {
                        continue;
                    }
                }

                // get changes
                Iterator<DiffEntry> diff = command(DiffTree.class)
                        .setOldTree(ancestorCommit.get().getId()).setNewTree(targetCommit.getId())
                        .setReportTrees(true).call();
                // stage changes
                getIndex().stage(new SubProgressListener(subProgress, 100.f), diff, 0);
                changed = true;
                fastForward = false;

                getWorkTree().updateWorkHead(getIndex().getTree().getId());

                subProgress.complete();

            }

        }

        if (!changed) {
            throw new NothingToCommitException("The branch has already been merged.");
        }

        RevCommit mergeCommit = commit(fastForward);

        MergeReport result = new MergeReport(mergeCommit, mergeScenario, oursId, pairs);

        return result;

    }

    private RevCommit commit(boolean fastForward) {

        RevCommit mergeCommit;
        if (fastForward) {
            mergeCommit = repository.getCommit(commits.get(0));
        } else {
            String commitMessage = message;
            if (commitMessage == null) {
                commitMessage = "";
                for (ObjectId commit : commits) {
                    Optional<Ref> ref = command(ResolveBranchId.class).setObjectId(commit).call();
                    if (ref.isPresent()) {
                        commitMessage += "Merge branch " + ref.get().getName();
                    } else {
                        commitMessage += "Merge commit '" + commit.toString() + "'. ";
                    }
                }
            }
            if (noCommit) {
                final Optional<Ref> currHead = command(RefParse.class).setName(Ref.HEAD).call();
                SymRef headRef = (SymRef) currHead.get();
                RevCommit headCommit = repository.getCommit(headRef.getObjectId());
                command(UpdateRef.class).setName(Ref.MERGE_HEAD).setNewValue(commits.get(0)).call();
                // TODO:how to store multiple ids when octopus merge
                command(UpdateRef.class).setName(Ref.ORIG_HEAD).setNewValue(headCommit.getId())
                        .call();
                mergeCommit = headCommit;
                command(SaveMergeCommitMessageOp.class).setMessage(commitMessage).call();
            } else {
                mergeCommit = command(CommitOp.class).setAllowEmpty(true).setMessage(commitMessage)
                        .addParents(commits).setAuthor(authorName.orNull(), authorEmail.orNull())
                        .call();
            }
        }

        getProgressListener().complete();

        return mergeCommit;
    }

    public class CommitAncestorPair {
        private ObjectId theirs;

        private ObjectId ancestor;

        public ObjectId getTheirs() {
            return theirs;
        }

        public ObjectId getAncestor() {
            return ancestor;
        }

        public CommitAncestorPair(ObjectId theirs, ObjectId ancestor) {
            this.theirs = theirs;
            this.ancestor = ancestor;
        }
    }

    public class MergeReport {
        private RevCommit mergeCommit;

        private Optional<MergeScenarioReport> report;

        private ObjectId ours;

        private List<CommitAncestorPair> pairs;

        public RevCommit getMergeCommit() {
            return mergeCommit;
        }

        public ObjectId getOurs() {
            return ours;
        }

        public List<CommitAncestorPair> getPairs() {
            return pairs;
        }

        public Optional<MergeScenarioReport> getReport() {
            return report;
        }

        public MergeReport(RevCommit mergeCommit, Optional<MergeScenarioReport> report,
                ObjectId ours, List<CommitAncestorPair> pairs) {
            this.mergeCommit = mergeCommit;
            this.report = report;
            this.ours = ours;
            this.pairs = pairs;
        }
    }
}
