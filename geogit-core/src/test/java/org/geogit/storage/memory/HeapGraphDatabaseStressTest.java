/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.storage.memory;

import org.geogit.di.GeogitModule;
import org.geogit.storage.GraphDatabase;
import org.geogit.storage.GraphDatabaseStressTest;
import org.junit.Assert;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class HeapGraphDatabaseStressTest extends GraphDatabaseStressTest {

    @Override
    protected Injector createInjector() {
        // relies on HeapGraphDatabase being the default
        Injector injector = Guice.createInjector(new GeogitModule()); 
        Assert.assertTrue(injector.getInstance(GraphDatabase.class) instanceof HeapGraphDatabase);
        return injector;
    }

}
