/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.test.integration;

import org.geogit.api.InjectorBuilder;
import org.geogit.api.MemoryModule;
import org.geogit.api.Platform;
import org.geogit.di.GeogitModule;
import org.geogit.repository.Hints;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

public class TestInjectorBuilder extends InjectorBuilder {

    Platform platform;

    public TestInjectorBuilder(Platform platform) {
        this.platform = platform;
    }

    @Override
    public Injector build(Hints hints) {
        return Guice.createInjector(Modules.override(new GeogitModule()).with(
                new MemoryModule(platform), new HintsModule(hints)));
    }

}
