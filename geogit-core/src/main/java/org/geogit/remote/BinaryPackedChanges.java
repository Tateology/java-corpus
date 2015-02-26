/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.remote;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import org.geogit.api.ObjectId;
import org.geogit.api.RevFeature;
import org.geogit.api.RevFeatureType;
import org.geogit.api.RevObject;
import org.geogit.api.RevObject.TYPE;
import org.geogit.api.RevTree;
import org.geogit.api.plumbing.diff.DiffEntry;
import org.geogit.repository.Repository;
import org.geogit.storage.ObjectReader;
import org.geogit.storage.ObjectSerializingFactory;
import org.geogit.storage.ObjectWriter;
import org.geogit.storage.datastream.DataStreamSerializationFactory;
import org.geogit.storage.datastream.FormatCommon;

import com.google.common.base.Throwables;

/**
 * Provides a method of packing a set of changes and the affected objects to and from a binary
 * stream.
 */
public final class BinaryPackedChanges {

    private final ObjectWriter<RevTree> treeWriter;

    private final ObjectWriter<RevFeatureType> featureTypeWriter;

    private final ObjectWriter<RevFeature> featureWriter;

    private final ObjectReader<RevObject> objectReader;

    private final int CAP = 100;

    private final Repository repository;

    private boolean filtered;

    private static enum CHUNK_TYPE {
        DIFF_ENTRY {
            @Override
            public int value() {
                return 0;
            }
        },
        OBJECT_AND_DIFF_ENTRY {
            @Override
            public int value() {
                return 1;
            }
        },
        METADATA_OBJECT_AND_DIFF_ENTRY {
            @Override
            public int value() {
                return 2;
            }
        },
        FILTER_FLAG {
            @Override
            public int value() {
                return 3;
            }
        };

        public int value() {
            return -1;
        }
    };

    /**
     * Constructs a new {@code BinaryPackedChanges} instance using the provided {@link Repository}.
     * 
     * @param repository the repository to save objects to, or read objects from, depending on the
     *        operation
     */
    public BinaryPackedChanges(Repository repository) {
        this.repository = repository;
        final ObjectSerializingFactory factory = new DataStreamSerializationFactory();
        this.treeWriter = factory.createObjectWriter(RevObject.TYPE.TREE);
        this.featureTypeWriter = factory.createObjectWriter(RevObject.TYPE.FEATURETYPE);
        this.featureWriter = factory.createObjectWriter(RevObject.TYPE.FEATURE);
        this.objectReader = factory.createObjectReader();
        filtered = false;
    }

    public boolean wasFiltered() {
        return filtered;
    }

    /**
     * Writes the set of changes to the provided output stream.
     * 
     * @param out the stream to write to
     * @param changes the changes to write
     * @throws IOException
     */
    public void write(OutputStream out, Iterator<DiffEntry> changes) throws IOException {
        write(out, changes, DEFAULT_CALLBACK);
    }

    /**
     * Writes the set of changes to the provided output stream, calling the provided callback for
     * each item.
     * 
     * @param out the stream to write to
     * @param changes the changes to write
     * @param callback the callback function to call for each element written
     * @return the state of the operation at the conclusion of writing
     * @throws IOException
     */
    public <T> T write(OutputStream out, Iterator<DiffEntry> changes, Callback<T> callback)
            throws IOException {
        T state = null;
        int changesSent = 0;

        while (changes.hasNext() && changesSent < CAP) {
            DiffEntry diff = changes.next();

            RevObject object = null;
            RevObject metadata = null;
            if (diff.getNewObject() != null) {
                if (diff.getNewObject().getType() != TYPE.FEATURE) {
                    out.write(CHUNK_TYPE.METADATA_OBJECT_AND_DIFF_ENTRY.value());
                    metadata = repository.getObjectDatabase().get(
                            diff.getNewObject().getMetadataId());
                    out.write(metadata.getId().getRawValue());
                    if (metadata instanceof RevTree) {
                        treeWriter.write((RevTree) metadata, out);
                    } else if (metadata instanceof RevFeature) {
                        featureWriter.write((RevFeature) metadata, out);
                    } else if (metadata instanceof RevFeatureType) {
                        featureTypeWriter.write((RevFeatureType) metadata, out);
                    }

                } else {
                    out.write(CHUNK_TYPE.OBJECT_AND_DIFF_ENTRY.value());
                }
                object = repository.getObjectDatabase().get(
                        diff.getNewObject().getNode().getObjectId());

                out.write(object.getId().getRawValue());
                if (object instanceof RevTree) {
                    treeWriter.write((RevTree) object, out);
                } else if (object instanceof RevFeature) {
                    featureWriter.write((RevFeature) object, out);
                } else if (object instanceof RevFeatureType) {
                    featureTypeWriter.write((RevFeatureType) object, out);
                }

            } else {
                out.write(CHUNK_TYPE.DIFF_ENTRY.value());
            }
            DataOutput dataOut = new DataOutputStream(out);
            FormatCommon.writeDiff(diff, dataOut);
            state = callback.callback(diff, state);

        }
        // signal the end of changes
        out.write(CHUNK_TYPE.FILTER_FLAG.value());
        if (changes instanceof FilteredDiffIterator
                && ((FilteredDiffIterator) changes).wasFiltered()) {
            out.write(1);
        } else {
            out.write(0);
        }

        return state;
    }

    /**
     * Read in the changes from the provided input stream. The input stream represents the output of
     * another {@code BinaryPackedChanges} instance.
     * 
     * @param in the stream to read from
     */
    public void ingest(final InputStream in) {
        ingest(in, DEFAULT_CALLBACK);
    }

    /**
     * Read in the changes from the provided input stream and call the provided callback for each
     * change. The input stream represents the output of another {@code BinaryPackedChanges}
     * instance.
     * 
     * @param in the stream to read from
     * @param callback the callback to call for each item
     */
    public <T> T ingest(final InputStream in, Callback<T> callback) {
        T state = null;
        while (true) {
            try {
                state = ingestOne(in, callback, state);
            } catch (EOFException e) {
                break;
            } catch (IOException e) {
                Throwables.propagate(e);
            }
        }
        return state;
    }

    /**
     * Reads in a single change from the provided input stream.
     * 
     * @param in the stream to read from
     * @param callback the callback to call on the resulting item
     * @param state the current state of the operation
     * @return the new state of the operation
     * @throws IOException
     */
    private <T> T ingestOne(final InputStream in, Callback<T> callback, T state) throws IOException {
        int chunkType = in.read();
        final T result;
        if (chunkType == CHUNK_TYPE.FILTER_FLAG.value()) {
            int changesFiltered = in.read();
            if (changesFiltered != 0) {
                filtered = true;
            }
            throw new EOFException();
        }
        if (chunkType == CHUNK_TYPE.METADATA_OBJECT_AND_DIFF_ENTRY.value()) {
            ObjectId id = readObjectId(in);
            RevObject revObj = objectReader.read(id, in);

            if (!repository.getObjectDatabase().exists(id)) {
                repository.getObjectDatabase().put(revObj);
            }
        }
        if (chunkType != CHUNK_TYPE.DIFF_ENTRY.value()) {
            ObjectId id = readObjectId(in);
            RevObject revObj = objectReader.read(id, in);

            if (!repository.getObjectDatabase().exists(id)) {
                repository.getObjectDatabase().put(revObj);
            }
        }
        DataInput dataIn = new DataInputStream(in);
        DiffEntry diff = FormatCommon.readDiff(dataIn);
        result = callback.callback(diff, state);
        return result;
    }

    /**
     * Reads an {@link ObjectId} from the provided input stream.
     * 
     * @param in the stream to read from
     * @return the {@code ObjectId} that was read
     * @throws IOException
     */
    private ObjectId readObjectId(final InputStream in) throws IOException {
        byte[] rawBytes = new byte[20];
        int amount = 0;
        int len = 20;
        int offset = 0;
        while ((amount = in.read(rawBytes, offset, len - offset)) != 0) {
            if (amount < 0)
                throw new EOFException("Came to end of input");
            offset += amount;
            if (offset == len)
                break;
        }
        ObjectId id = ObjectId.createNoClone(rawBytes);
        return id;
    }

    /**
     * Inteface for callback methods to be used by the read and write operations.
     * 
     * @param <T> the type of the state parameter
     */
    public static interface Callback<T> {
        public abstract T callback(DiffEntry diff, T state);
    }

    private static final Callback<Void> DEFAULT_CALLBACK = new Callback<Void>() {
        @Override
        public Void callback(DiffEntry diff, Void state) {
            return null;
        }
    };
}
