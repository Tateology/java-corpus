/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.api;

import org.geogit.storage.GraphDatabase;
import org.geogit.storage.ObjectDatabase;
import org.geogit.storage.RefDatabase;
import org.geogit.storage.StagingDatabase;
import org.geogit.storage.memory.HeapGraphDatabase;
import org.geogit.storage.memory.HeapObjectDatabse;
import org.geogit.storage.memory.HeapRefDatabase;
import org.geogit.storage.memory.HeapStagingDatabase;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * @see HeapObjectDatabse
 * @see HeapStagingDatabase
 * @see HeapRefDatabase
 * @see HeapGraphDatabase
 */
public class MemoryModule extends AbstractModule {

    private Platform testPlatform;

    /**
     * @param testPlatform
     */
    public MemoryModule(Platform testPlatform) {
        this.testPlatform = testPlatform;
    }

    @Override
    protected void configure() {
        if (testPlatform != null) {
            bind(Platform.class).toInstance(testPlatform);
        }
        bind(ObjectDatabase.class).to(HeapObjectDatabse.class).in(Scopes.SINGLETON);
        bind(StagingDatabase.class).to(HeapStagingDatabase.class).in(Scopes.SINGLETON);
        bind(RefDatabase.class).to(HeapRefDatabase.class).in(Scopes.SINGLETON);
        bind(GraphDatabase.class).to(HeapGraphDatabase.class).in(Scopes.SINGLETON);
    }

}
