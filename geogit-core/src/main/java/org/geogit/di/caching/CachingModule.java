/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.di.caching;

import static com.google.inject.matcher.Matchers.not;
import static com.google.inject.matcher.Matchers.subclassesOf;

import java.lang.reflect.Method;
import java.util.Iterator;

import org.geogit.api.ObjectId;
import org.geogit.di.GeogitModule;
import org.geogit.di.MethodMatcher;
import org.geogit.storage.BulkOpListener;
import org.geogit.storage.ConfigDatabase;
import org.geogit.storage.ObjectDatabase;
import org.geogit.storage.StagingDatabase;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.matcher.Matcher;

/**
 * 
 * <p>
 * Depends on {@link GeogitModule} or similar that provides bindings for {@link ConfigDatabase},
 * {@link ObjectDatabase}, and {@link StagingDatabase}.
 * 
 * @see CacheFactory
 * @see ObjectDatabaseGetCacheInterceptor
 * @see ObjectDatabaseDeleteCacheInterceptor
 * @see ObjectDatabaseDeleteAllCacheInterceptor
 */

public class CachingModule extends AbstractModule {

    /**
     */
    @SuppressWarnings("rawtypes")
    @Override
    protected void configure() {

        final Matcher<Method> getMatcher = new MethodMatcher(ObjectDatabase.class, "get",
                ObjectId.class).or(new MethodMatcher(ObjectDatabase.class, "get", ObjectId.class,
                Class.class));

        final Matcher<Method> deleteMatcher = new MethodMatcher(ObjectDatabase.class, "delete",
                ObjectId.class);

        final Matcher<Method> deleteAllMatcher = new MethodMatcher(ObjectDatabase.class,
                "deleteAll", Iterator.class, BulkOpListener.class);

        // bind separate caches for the object and staging databases

        {
            final Matcher<Class> stagingDatabaseMatcher = subclassesOf(StagingDatabase.class);
            final StagingDatabaseCacheFactory indexCacheProvider;
            indexCacheProvider = new StagingDatabaseCacheFactory(getProvider(ConfigDatabase.class));
            bind(StagingDatabaseCacheFactory.class).toInstance(indexCacheProvider);

            bindCacheAwareMethodInterceptors(stagingDatabaseMatcher,
                    getProvider(StagingDatabaseCacheFactory.class), getMatcher, deleteMatcher,
                    deleteAllMatcher);
        }
        {
            final ObjectDatabaseCacheFactory odbCacheProvider;
            odbCacheProvider = new ObjectDatabaseCacheFactory(getProvider(ConfigDatabase.class));
            bind(ObjectDatabaseCacheFactory.class).toInstance(odbCacheProvider);

            final Matcher<Class> objectDatabaseMatcher = subclassesOf(ObjectDatabase.class).and(
                    not(subclassesOf(StagingDatabase.class)));

            bindCacheAwareMethodInterceptors(objectDatabaseMatcher,
                    getProvider(ObjectDatabaseCacheFactory.class), getMatcher, deleteMatcher,
                    deleteAllMatcher);

        }
    }

    @SuppressWarnings("rawtypes")
    private void bindCacheAwareMethodInterceptors(final Matcher<Class> objectDatabaseMatcher,
            final Provider<? extends CacheFactory> cacheProvider, final Matcher<Method> getMatcher,
            final Matcher<Method> deleteMatcher, final Matcher<Method> deleteAllMatcher) {

        bindInterceptor(objectDatabaseMatcher, getMatcher, new ObjectDatabaseGetCacheInterceptor(
                cacheProvider));

        bindInterceptor(objectDatabaseMatcher, deleteMatcher,
                new ObjectDatabaseDeleteCacheInterceptor(cacheProvider));

        bindInterceptor(objectDatabaseMatcher, deleteAllMatcher,
                new ObjectDatabaseDeleteAllCacheInterceptor(cacheProvider));
    }

}
