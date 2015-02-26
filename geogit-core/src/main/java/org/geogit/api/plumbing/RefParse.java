/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.api.plumbing;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.geogit.api.AbstractGeoGitOp;
import org.geogit.api.ObjectId;
import org.geogit.api.Ref;
import org.geogit.api.RevObject.TYPE;
import org.geogit.api.SymRef;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

/**
 * Resolve a ref name to the stored {@link Ref reference} object
 */
public class RefParse extends AbstractGeoGitOp<Optional<Ref>> {

    private static final Set<String> STANDARD_REFS = ImmutableSet.of(Ref.HEAD, Ref.MASTER,
            Ref.ORIGIN, Ref.STAGE_HEAD, Ref.WORK_HEAD);

    private String refSpec;

    /**
     * Constructs a new {@code RefParse} operation.
     */
    @Inject
    public RefParse() {
    }

    /**
     * @param name the name of the ref to parse
     * @return {@code this}
     */
    public RefParse setName(String name) {
        this.refSpec = name;
        return this;
    }

    /**
     * Parses a geogit reference string (possibly abbreviated) and return the resolved {@link Ref}
     * or {@code absent} if the ref spec didn't resolve to any actual reference.
     * 
     * Combinations of these operators are supported:
     * <ul>
     * <li><b>HEAD</b>, <b>MERGE_HEAD</b>, <b>FETCH_HEAD</b>, <b>STAGE_HEAD</b>, <b>WORK_HEAD</b></li>
     * <li><b>refs/...</b>: a complete reference name</li>
     * <li><b>short-name</b>: a short reference name under {@code refs/heads}, {@code refs/tags}, or
     * {@code refs/remotes} namespace, in that order of precedence</li>
     * </ul>
     * 
     * @return an {@code Optional} that contains a {@link Ref reference} or
     *         {@link Optional#absent()} if revstr can't be resolved to any {@link ObjectId}
     * @throws IllegalArgumentException if {@code refSpec} resolves to more than one ref on the same
     *         namespace
     */
    @Override
    public Optional<Ref> call() {
        Preconditions.checkState(refSpec != null, "name has not been set");

        if (STANDARD_REFS.contains(refSpec) || refSpec.startsWith("refs/")) {
            return getRef(refSpec);
        }

        // is it a top level ref?
        if (!refSpec.startsWith("refs/")) {
            Optional<Ref> ref = getRef(refSpec);
            if (ref.isPresent()) {
                return ref;
            }
        }

        Map<String, String> allRefs = getRefDatabase().getAll();

        class PrePostfixPredicate implements Predicate<String> {

            private String prefix;

            private String suffix;

            public PrePostfixPredicate(String prefix, String suffix) {
                this.prefix = prefix;
                this.suffix = suffix;
            }

            @Override
            public boolean apply(String refName) {
                boolean applies = refName.startsWith(prefix) && refName.endsWith(suffix);
                return applies;
            }

        }
        Collection<String> heads = Collections2.filter(allRefs.keySet(), new PrePostfixPredicate(
                "refs/heads", "/" + refSpec));
        Collection<String> tags = Collections2.filter(allRefs.keySet(), new PrePostfixPredicate(
                "refs/tags", "/" + refSpec));
        Collection<String> remotes = Collections2.filter(allRefs.keySet(), new PrePostfixPredicate(
                "refs/remotes", "/" + refSpec));

        String refName;
        refName = resolveSingle(heads);
        if (refName == null) {
            refName = resolveSingle(tags);
        }
        if (refName == null) {
            refName = resolveSingle(remotes);
        }
        if (refName == null) {
            return Optional.absent();
        }
        return getRef(refName);
    }

    private String resolveSingle(Collection<String> refNames) {
        if (refNames.isEmpty()) {
            return null;
        }
        if (refNames.size() == 1) {
            return refNames.iterator().next();
        }
        throw new IllegalArgumentException(refSpec + " resolves to more than one ref: " + refNames);
    }

    private Optional<Ref> getRef(final String name) {
        String storedValue;
        boolean sym = false;
        try {
            storedValue = getRefDatabase().getRef(name);
        } catch (IllegalArgumentException notARef) {
            storedValue = getRefDatabase().getSymRef(name);
            if (null == storedValue) {
                return Optional.absent();
            }
            sym = true;
        }
        if (null == storedValue) {
            storedValue = getRefDatabase().getSymRef(name);
            if (null == storedValue) {
                return Optional.absent();
            }
            sym = true;
        }

        if (sym) {
            Optional<Ref> target = getRef(storedValue);
            if (!target.isPresent()) {
                throw new RuntimeException(storedValue + " was not present.");
            }
            Ref resolved = new SymRef(name, target.get());
            return Optional.of(resolved);
        }
        ObjectId objectId = ObjectId.valueOf(storedValue);
        TYPE type = TYPE.COMMIT; //objectId.isNull() ? null : command(ResolveObjectType.class).setObjectId(objectId).call();
        return Optional.of(new Ref(name, objectId, type));
    }

}
