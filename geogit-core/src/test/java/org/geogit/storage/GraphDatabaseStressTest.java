/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.storage;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;

import org.geogit.api.ObjectId;
import org.geogit.test.integration.RepositoryTestCase;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;

/**
 * Abstract test suite for {@link GraphDatabase} implementations.
 * <p>
 * Create a concrete subclass of this test suite and implement {@link #createInjector()} so that
 * {@code GraphDtabase.class} is bound to your implementation instance as a singleton.
 */
public abstract class GraphDatabaseStressTest extends RepositoryTestCase {
    protected GraphDatabase database;

    @Override
    protected void setUpInternal() throws Exception {
        database = (GraphDatabase) geogit.getRepository().getGraphDatabase();
    }

    protected abstract Injector createInjector();

    @Test
    public void testConcurrentUses() throws Exception {
        ConcurrentLinkedQueue<String> errorLog = new ConcurrentLinkedQueue<String>();
        //ExecutorService executor = Executors.newCachedThreadPool();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        List<Future<?>> futures = new ArrayList<Future<?>>();
        for (String s : new String[] { "a", "b", "c", "d" }) {
            Runnable task = new InsertMany(s, errorLog);
            futures.add(executor.submit(task));
        }
        for (Future<?> f : futures) {
            f.get();
        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        assertEquals(errorLog.toString(), 0, errorLog.size());
        assertEquals(100, database.getDepth(ObjectId.forString("a_commit_100")));
    }

    private class InsertMany implements Runnable {
        private final String key;
        private final ConcurrentLinkedQueue errorLog;
        public InsertMany(String key, ConcurrentLinkedQueue<String> errorLog) {
            this.key = key;
            this.errorLog = errorLog;
        }

        public void run() {
            try {
                for (int i = 0; i < 100; i++) {
                    ObjectId root = ObjectId.forString(key + "_commit_" + i);
                    ObjectId commit = ObjectId.forString(key + "_commit_" + (i + 1));
                    database.put(commit, ImmutableList.of(root));
                }
            } catch (Exception e) {
                errorLog.offer(e.toString());
            }
        }
    }
}
