/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.api.plumbing;

import org.geogit.api.AbstractGeoGitOp;
import org.geogit.api.Ref;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

/**
 * Update the object name stored in a {@link Ref} safely.
 * <p>
 * 
 */
public class ForEachRef extends AbstractGeoGitOp<ImmutableSet<Ref>> {

    private Predicate<Ref> filter;

    @Inject
    public ForEachRef() {
    }

    public ForEachRef setFilter(Predicate<Ref> filter) {
        this.filter = filter;
        return this;
    }

    public ForEachRef setPrefixFilter(final String prefix) {
        this.filter = new Predicate<Ref>() {
            @Override
            public boolean apply(Ref ref) {
                return ref != null && ref.getName().startsWith(prefix);
            }
        };
        return this;
    }

    /**
     * @return the new value of the ref
     */
    @Override
    public ImmutableSet<Ref> call() {

        @SuppressWarnings("unchecked")
        final Predicate<Ref> filter = (Predicate<Ref>) (this.filter == null ? Predicates
                .alwaysTrue() : this.filter);

        ImmutableSet.Builder<Ref> refs = new ImmutableSet.Builder<Ref>();
        for (String refName : getRefDatabase().getAll().keySet()) {
            Optional<Ref> ref = command(RefParse.class).setName(refName).call();
            if (ref.isPresent() && filter.apply(ref.get())) {
                Ref accepted = ref.get();
                refs.add(accepted);
            }
        }
        return refs.build();
    }
}
