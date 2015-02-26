/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.di.caching;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.geogit.api.ObjectId;
import org.geogit.api.RevObject;
import org.geogit.storage.ObjectDatabase;

import com.google.common.cache.Cache;
import com.google.inject.Provider;

/**
 * Method interceptor for {@link ObjectDatabase#delete(ObjectId)} methods that removes cached
 * objects from the cache.
 * <p>
 * <!-- increases random object lookup on revtrees by 20x, ~40K/s instad of ~2K/s as per
 * RevSHA1TreeTest.testPutGet -->
 */
class ObjectDatabaseDeleteCacheInterceptor implements MethodInterceptor {

    private Provider<? extends CacheFactory> cacheProvider;

    ObjectDatabaseDeleteCacheInterceptor(Provider<? extends CacheFactory> cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        final ObjectId oid = (ObjectId) invocation.getArguments()[0];

        final Cache<ObjectId, RevObject> cache = cacheProvider.get().get();
        cache.invalidate(oid);
        return invocation.proceed();
    }

}
