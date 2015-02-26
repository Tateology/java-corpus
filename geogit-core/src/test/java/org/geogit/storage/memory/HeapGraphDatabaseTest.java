package org.geogit.storage.memory;

import org.geogit.di.GeogitModule;
import org.geogit.storage.GraphDatabase;
import org.geogit.storage.GraphDatabaseTest;
import org.junit.Assert;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class HeapGraphDatabaseTest extends GraphDatabaseTest {

    @Override
    protected Injector createInjector() {
        // relies on HeapGraphDatabase being the default
        Injector injector = Guice.createInjector(new GeogitModule()); 
        Assert.assertTrue(injector.getInstance(GraphDatabase.class) instanceof HeapGraphDatabase);
        return injector;
    }

}
