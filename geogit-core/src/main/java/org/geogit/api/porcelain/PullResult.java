/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.api.porcelain;

import org.geogit.api.Ref;
import org.geogit.api.porcelain.MergeOp.MergeReport;

import com.google.common.base.Optional;

public class PullResult {

    private Ref oldRef = null;

    private Ref newRef = null;

    private String remoteName = null;

    private FetchResult fetchResult = null;

    private Optional<MergeReport> mergeReport = Optional.absent();

    public String getRemoteName() {
        return remoteName;
    }

    public void setRemoteName(String remoteName) {
        this.remoteName = remoteName;
    }

    public FetchResult getFetchResult() {
        return fetchResult;
    }

    public void setFetchResult(FetchResult fetchResult) {
        this.fetchResult = fetchResult;
    }

    public Ref getOldRef() {
        return oldRef;
    }

    public void setOldRef(Ref oldRef) {
        this.oldRef = oldRef;
    }

    public Ref getNewRef() {
        return newRef;
    }

    public void setNewRef(Ref newRef) {
        this.newRef = newRef;
    }

    public Optional<MergeReport> getMergeReport() {
        return mergeReport;
    }

    public void setMergeReport(Optional<MergeReport> mergeReport) {
        this.mergeReport = mergeReport;
    }
}
