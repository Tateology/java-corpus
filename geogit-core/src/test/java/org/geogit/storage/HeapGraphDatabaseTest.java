/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.storage;

import org.geogit.api.MemoryModule;
import org.geogit.api.TestPlatform;
import org.geogit.di.GeogitModule;
import org.geogit.storage.memory.HeapGraphDatabase;
import org.junit.Assert;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

/**
 * Concrete test suite for {@link HeapGraphDatabase}
 */
public class HeapGraphDatabaseTest extends GraphDatabaseTest {

    @Override
    protected Injector createInjector() {
        Injector injector = Guice.createInjector(Modules.override(new GeogitModule()).with(
                new MemoryModule(new TestPlatform(super.envHome))));
        Assert.assertTrue(injector.getInstance(GraphDatabase.class) instanceof HeapGraphDatabase);
        return injector;
    }
}
