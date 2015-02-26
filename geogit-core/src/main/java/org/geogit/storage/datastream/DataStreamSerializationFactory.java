/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.storage.datastream;

import java.io.Serializable;
import java.util.Map;

import org.geogit.api.RevCommit;
import org.geogit.api.RevFeature;
import org.geogit.api.RevFeatureType;
import org.geogit.api.RevObject;
import org.geogit.api.RevObject.TYPE;
import org.geogit.api.RevTag;
import org.geogit.api.RevTree;
import org.geogit.storage.ObjectReader;
import org.geogit.storage.ObjectSerializingFactory;
import org.geogit.storage.ObjectWriter;

public class DataStreamSerializationFactory implements ObjectSerializingFactory {
    private final static ObjectReader<RevCommit> COMMIT_READER = new CommitReader();

    private final static ObjectReader<RevTree> TREE_READER = new TreeReader();

    private final static ObjectReader<RevFeature> FEATURE_READER = new FeatureReader();

    private final static ObjectReader<RevFeatureType> FEATURETYPE_READER = new FeatureTypeReader();

    private final static ObjectReader<RevObject> OBJECT_READER = new org.geogit.storage.datastream.ObjectReader();

    private final static ObjectReader<RevTag> TAG_READER = new TagReader();

    private final static ObjectWriter<RevCommit> COMMIT_WRITER = new CommitWriter();

    private final static ObjectWriter<RevTree> TREE_WRITER = new TreeWriter();

    private final static ObjectWriter<RevFeature> FEATURE_WRITER = new FeatureWriter();

    private final static ObjectWriter<RevFeatureType> FEATURETYPE_WRITER = new FeatureTypeWriter();

    private final static ObjectWriter<RevTag> TAG_WRITER = new TagWriter();

    @Override
    public ObjectReader<RevCommit> createCommitReader() {
        return COMMIT_READER;
    }

    @Override
    public ObjectReader<RevTree> createRevTreeReader() {
        return TREE_READER;
    }

    @Override
    public ObjectReader<RevFeature> createFeatureReader() {
        return FEATURE_READER;
    }

    @Override
    public ObjectReader<RevFeature> createFeatureReader(Map<String, Serializable> hints) {
        return FEATURE_READER;
    }

    @Override
    public ObjectReader<RevFeatureType> createFeatureTypeReader() {
        return FEATURETYPE_READER;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends RevObject> ObjectWriter<T> createObjectWriter(TYPE type) {
        switch (type) {
        case COMMIT:
            return (ObjectWriter<T>) COMMIT_WRITER;
        case TREE:
            return (ObjectWriter<T>) TREE_WRITER;
        case FEATURE:
            return (ObjectWriter<T>) FEATURE_WRITER;
        case FEATURETYPE:
            return (ObjectWriter<T>) FEATURETYPE_WRITER;
        case TAG:
            return (ObjectWriter<T>) TAG_WRITER;
        default:
            throw new UnsupportedOperationException("No writer for " + type);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ObjectReader<T> createObjectReader(TYPE type) {
        switch (type) {
        case COMMIT:
            return (ObjectReader<T>) COMMIT_READER;
        case TREE:
            return (ObjectReader<T>) TREE_READER;
        case FEATURE:
            return (ObjectReader<T>) FEATURE_READER;
        case FEATURETYPE:
            return (ObjectReader<T>) FEATURETYPE_READER;
        case TAG:
            return (ObjectReader<T>) TAG_READER;
        default:
            throw new UnsupportedOperationException("No reader for " + type);
        }
    }

    @Override
    public ObjectReader<RevObject> createObjectReader() {
        return OBJECT_READER;
    }
}
