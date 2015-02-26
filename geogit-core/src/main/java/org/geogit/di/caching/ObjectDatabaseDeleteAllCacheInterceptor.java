/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.di.caching;

import java.util.Iterator;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.geogit.api.ObjectId;
import org.geogit.api.RevObject;
import org.geogit.storage.ObjectDatabase;

import com.google.common.base.Function;
import com.google.common.cache.Cache;
import com.google.common.collect.Iterators;
import com.google.inject.Provider;

/**
 * Method interceptor for {@link ObjectDatabase#deleteAll} methods that removes cached objects from
 * the cache.
 * <p>
 * <!-- increases random object lookup on revtrees by 20x, ~40K/s instad of ~2K/s as per
 * RevSHA1TreeTest.testPutGet -->
 */
class ObjectDatabaseDeleteAllCacheInterceptor implements MethodInterceptor {

    private Provider<? extends CacheFactory> cacheProvider;

    ObjectDatabaseDeleteAllCacheInterceptor(Provider<? extends CacheFactory> cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        final Cache<ObjectId, RevObject> cache = cacheProvider.get().get();

        @SuppressWarnings("unchecked")
        Iterator<ObjectId> ids = (Iterator<ObjectId>) invocation.getArguments()[0];

        ids = Iterators.transform(ids, new Function<ObjectId, ObjectId>() {
            @Override
            public ObjectId apply(ObjectId input) {
                cache.invalidate(input);
                return input;
            }
        });

        invocation.getArguments()[0] = ids;

        return invocation.proceed();
    }

}
