/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.di.caching;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.geogit.api.ObjectId;
import org.geogit.api.RevFeatureType;
import org.geogit.api.RevObject;
import org.geogit.api.RevTree;

import com.google.common.cache.Cache;
import com.google.inject.Provider;

/**
 * Method interceptor for {@linnk ObjectDatabase#get(...)} methods that applies caching.
 * <p>
 * <!-- increases random object lookup on revtrees by 20x, ~40K/s instad of ~2K/s as per
 * RevSHA1TreeTest.testPutGet -->
 */
class ObjectDatabaseGetCacheInterceptor implements MethodInterceptor {

    private Provider<? extends CacheFactory> cacheProvider;

    ObjectDatabaseGetCacheInterceptor(Provider<? extends CacheFactory> cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        final ObjectId oid = (ObjectId) invocation.getArguments()[0];

        final Cache<ObjectId, RevObject> cache = cacheProvider.get().get();

        Object object = cache.getIfPresent(oid);
        if (object == null) {
            object = invocation.proceed();
            if (isCacheable(object)) {
                cache.put(oid, (RevObject) object);
            }
        }
        return object;
    }

    private final boolean isCacheable(Object object) {
        if (object == null) {
            return false;
        }
        if (object instanceof RevFeatureType) {
            return true;
        }
        if ((object instanceof RevTree) && ((RevTree) object).buckets().isPresent()) {
            return true;
        }
        return false;
    }

}
