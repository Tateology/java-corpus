/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.api.porcelain;

import java.util.Map;

import org.geogit.api.AbstractGeoGitOp;
import org.geogit.api.porcelain.ConfigOp.ConfigAction;
import org.geogit.api.porcelain.ConfigOp.ConfigScope;
import org.geogit.di.CanRunDuringConflict;

import com.google.common.base.Optional;
import com.google.inject.Inject;

/**
 * Get a repository or global options
 * <p>
 * This is just a shortcut for using ConfigOp in the case of wanting to retrieve a single config
 * value
 * <p>
 * 
 * @see ConfigOp
 */
@CanRunDuringConflict
public class ConfigGet extends AbstractGeoGitOp<Optional<String>> {

    private boolean global;

    private String name;

    /**
     * Constructs a new {@code ConfigGet}
     * 
     */
    @Inject
    public ConfigGet() {
    }

    /**
     * Executes the config command with the specified options.
     * 
     * @return Optional<String> if querying for a value, empty Optional if no matching name was
     *         found.
     * @throws ConfigException if an error is encountered. More specific information can be found in
     *         the exception's statusCode.
     */
    @Override
    public Optional<String> call() {
        ConfigScope scope = global ? ConfigScope.GLOBAL : ConfigScope.LOCAL;
        Optional<Map<String, String>> result = command(ConfigOp.class)
                .setAction(ConfigAction.CONFIG_GET).setName(name).setScope(scope).call();
        if (result.isPresent()) {
            return Optional.of(result.get().get(name));
        } else {
            return Optional.absent();
        }

    }

    /**
     * @param global if true, config actions will be executed on the global configuration file. If
     *        false, then all actions will be done on the config file in the local repository.
     * @return {@code this}
     */
    public ConfigGet setGlobal(boolean global) {
        this.global = global;
        return this;
    }

    /**
     * @param name the name of the variable to get
     * @return {@code this}
     */
    public ConfigGet setName(String name) {
        this.name = name;
        return this;
    }

}