/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.di.caching;

import org.geogit.storage.ConfigDatabase;

import com.google.inject.Provider;

class ObjectDatabaseCacheFactory extends CacheFactory {

    public ObjectDatabaseCacheFactory(Provider<ConfigDatabase> configDb) {
        super("objectdb.cache", configDb);
    }

}
