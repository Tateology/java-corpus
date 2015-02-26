/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.di;

import java.util.Iterator;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.geogit.api.ObjectId;
import org.geogit.api.RevCommit;
import org.geogit.api.RevObject;
import org.geogit.storage.GraphDatabase;
import org.geogit.storage.ObjectDatabase;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.inject.Provider;

/**
 * Method interceptor for {@link ObjectDatabase#put(RevObject)} that adds new commits to the graph
 * database.
 */
class ObjectDatabasePutInterceptor implements MethodInterceptor {

    private Provider<GraphDatabase> graphDb;

    public ObjectDatabasePutInterceptor(Provider<GraphDatabase> graphDb) {
        this.graphDb = graphDb;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        final String methodName = invocation.getMethod().getName();
        if (methodName.equals("put")) {
            return putRevObjectInterceptor(invocation);
        } else if (methodName.equals("putAll")) {
            return putAllInterceptor(invocation);
        }
        return invocation.proceed();
    }

    private Object putAllInterceptor(MethodInvocation invocation) throws Throwable {
        Object[] arguments = invocation.getArguments();

        @SuppressWarnings("unchecked")
        final Iterator<? extends RevObject> objects = (Iterator<? extends RevObject>) arguments[0];

        final Iterator<? extends RevObject> sideEffectIterator;
        final List<RevCommit> addedCommits = Lists.newLinkedList();
        sideEffectIterator = Iterators.transform(objects, new Function<RevObject, RevObject>() {

            @Override
            public RevObject apply(RevObject input) {
                if (input instanceof RevCommit) {
                    addedCommits.add((RevCommit) input);
                }
                return input;
            }
        });
        arguments[0] = sideEffectIterator;

        Object result = invocation.proceed();
        if (!addedCommits.isEmpty()) {
            GraphDatabase graphDatabase = graphDb.get();
            for (RevCommit commit : addedCommits) {
                ObjectId commitId = commit.getId();
                ImmutableList<ObjectId> parentIds = commit.getParentIds();
                graphDatabase.put(commitId, parentIds);
            }
        }

        return result;
    }

    private Object putRevObjectInterceptor(MethodInvocation invocation) throws Throwable {
        final RevObject revObject = (RevObject) invocation.getArguments()[0];

        final boolean inserted = ((Boolean) invocation.proceed()).booleanValue();

        if (inserted && RevObject.TYPE.COMMIT.equals(revObject.getType())) {
            RevCommit commit = (RevCommit) revObject;
            graphDb.get().put(commit.getId(), commit.getParentIds());
        }
        return Boolean.valueOf(inserted);
    }
}
