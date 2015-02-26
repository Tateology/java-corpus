/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.storage;

/**
 * A service for providing deduplicators.
 */
public interface DeduplicationService {
    /**
     * Create a new Deduplicator.  Clients MUST ensure that the deduplicator's
     * release() method is called.  For example:
     *
     * <code>
     *   Deduplicator deduplicator = deduplicationService().createDeduplicator();
     *   try {
     *       client.use(deduplicator);
     *   } finally {
     *       deduplicator.release();
     *   }
     * </code>
     */
    Deduplicator createDeduplicator();
}
