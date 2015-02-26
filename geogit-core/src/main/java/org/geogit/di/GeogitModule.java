/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.di;

import static com.google.inject.matcher.Matchers.subclassesOf;

import java.lang.reflect.Method;
import java.util.Iterator;

import org.geogit.api.AbstractGeoGitOp;
import org.geogit.api.CommandLocator;
import org.geogit.api.DefaultPlatform;
import org.geogit.api.Platform;
import org.geogit.api.RevObject;
import org.geogit.repository.Index;
import org.geogit.repository.Repository;
import org.geogit.repository.StagingArea;
import org.geogit.repository.WorkingTree;
import org.geogit.storage.ConfigDatabase;
import org.geogit.storage.DeduplicationService;
import org.geogit.storage.GraphDatabase;
import org.geogit.storage.ObjectDatabase;
import org.geogit.storage.ObjectSerializingFactory;
import org.geogit.storage.RefDatabase;
import org.geogit.storage.StagingDatabase;
import org.geogit.storage.datastream.DataStreamSerializationFactory;
import org.geogit.storage.fs.FileObjectDatabase;
import org.geogit.storage.fs.FileRefDatabase;
import org.geogit.storage.fs.IniFileConfigDatabase;
import org.geogit.storage.memory.HeapDeduplicationService;
import org.geogit.storage.memory.HeapGraphDatabase;
import org.geogit.storage.memory.HeapStagingDatabase;

import com.google.common.base.Throwables;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;

/**
 * Provides bindings for GeoGit singletons.
 * 
 * @see CommandLocator
 * @see Platform
 * @see Repository
 * @see ConfigDatabase
 * @see StagingArea
 * @see WorkingTree
 * @see ObjectDatabase
 * @see StagingDatabase
 * @see RefDatabase
 * @see GraphDatabase
 * @see ObjectSerializingFactory
 * @see DeduplicationService
 */

public class GeogitModule extends AbstractModule {

    /**
     * 
     * @see com.google.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {

        bind(CommandLocator.class).to(GuiceCommandLocator.class).in(Scopes.SINGLETON);

        bind(Platform.class).to(DefaultPlatform.class).asEagerSingleton();

        bind(Repository.class).in(Scopes.SINGLETON);
        bind(ConfigDatabase.class).to(IniFileConfigDatabase.class).in(Scopes.SINGLETON);
        bind(StagingArea.class).to(Index.class).in(Scopes.SINGLETON);
        bind(StagingDatabase.class).to(HeapStagingDatabase.class).in(Scopes.SINGLETON);
        bind(WorkingTree.class).in(Scopes.SINGLETON);
        bind(GraphDatabase.class).to(HeapGraphDatabase.class).in(Scopes.SINGLETON);

        bind(ObjectDatabase.class).to(FileObjectDatabase.class).in(Scopes.SINGLETON);
        bind(RefDatabase.class).to(FileRefDatabase.class).in(Scopes.SINGLETON);

        bind(ObjectSerializingFactory.class).to(DataStreamSerializationFactory.class).in(
                Scopes.SINGLETON);

        bind(DeduplicationService.class).to(HeapDeduplicationService.class).in(Scopes.SINGLETON);

        bindCommitGraphInterceptor();

        bindConflictCheckingInterceptor();
    }

    private void bindConflictCheckingInterceptor() {
        final Method callMethod;
        try {
            callMethod = AbstractGeoGitOp.class.getMethod("call");
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
        Matcher<Method> callMatcher = new MethodMatcher(callMethod);

        Matcher<Class<?>> canRunDuringCommitMatcher = new AbstractMatcher<Class<?>>() {

            @Override
            public boolean matches(Class<?> clazz) {
                // TODO: this is not a very clean way of doing this...
                return !(clazz.getPackage().getName().contains("plumbing") || clazz
                        .isAnnotationPresent(CanRunDuringConflict.class));
            }
        };

        bindInterceptor(canRunDuringCommitMatcher, callMatcher, new ConflictInterceptor());
    }

    private void bindCommitGraphInterceptor() {
        final Method putRevObject;
        final Method putAll;
        try {
            putRevObject = ObjectDatabase.class.getMethod("put", RevObject.class);
            putAll = ObjectDatabase.class.getMethod("putAll", Iterator.class);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
        Matcher<Method> methodMatcher = new MethodMatcher(putRevObject)
                .or(new MethodMatcher(putAll));

        bindInterceptor(subclassesOf(ObjectDatabase.class), methodMatcher,
                new ObjectDatabasePutInterceptor(getProvider(GraphDatabase.class)));
    }
}
