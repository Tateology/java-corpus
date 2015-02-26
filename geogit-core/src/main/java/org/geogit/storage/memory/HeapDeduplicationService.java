/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.storage.memory;

import org.geogit.storage.DeduplicationService;
import org.geogit.storage.Deduplicator;

public class HeapDeduplicationService implements DeduplicationService {
    @Override
    public Deduplicator createDeduplicator() {
        return new HeapDeduplicator();
    }
}
