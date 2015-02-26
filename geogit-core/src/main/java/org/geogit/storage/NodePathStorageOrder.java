/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.geogit.storage;

import java.io.Serializable;

import org.geogit.api.Node;
import org.geogit.api.ObjectId;
import org.geogit.api.RevTree;

import com.google.common.base.Preconditions;
import com.google.common.collect.Ordering;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.primitives.UnsignedBytes;
import com.google.common.primitives.UnsignedLong;

/**
 * Implements storage order of {@link Node} based on the non cryptographic 64-bit <a
 * href="http://en.wikipedia.org/wiki/Fowler%E2%80%93Noll%E2%80%93Vo_hash_function">FNV-1a</a>
 * variation of the "Fowler/Noll/Vo" hash algorithm.
 * <p>
 * This class mandates in which order {@link Node nodes} are stored inside {@link RevTree trees},
 * hence defining the prescribed order in which tree elements are traversed, regardless of in how
 * many subtrees a given tree is split into.
 * <p>
 * The resulting structure where a given node (identified by its name) always falls on the same
 * bucket (subtree) for a given subtree depth makes it possible to compute diffs between two trees
 * very quickly, by traversing both trees in parallel and finding both bucket and node differences
 * and skipping equal bucket trees, as two buckets at the same depth with the same contents will
 * always hash out to the same {@link ObjectId}.
 * <p>
 * The FNV-1 hash for a node name is computed as in the following pseudo-code:
 * 
 * <pre>
 * <code>
 * hash = FNV_offset_basis
 * for each octet_of_data to be hashed
 *      hash = hash × FNV_prime
 *      hash = hash XOR octet_of_data
 * return hash
 * </code>
 * </pre>
 * 
 * Where {@code FNV_offset_basis} is the 64-bit literal {@code 0xcbf29ce484222325}, and
 * {@code FNV_prime} is the 64-bit literal {@code 0x100000001b3}.
 * <p>
 * To compute the node name hash, each two-byte char in the node name produces two
 * {@code octet_of_data}, in big-endian order.
 * <p>
 * This hash function proved to be extremely fast while maintaining a good distribution and low
 * collision rate, and is widely used by the computer science industry as explained <a
 * href="http://www.isthe.com/chongo/tech/comp/fnv/index.html#FNV-param">here</a> when speed is
 * needed in contrast to cryptographic security.
 * 
 * @since 0.6
 */
public final class NodePathStorageOrder extends Ordering<String> implements Serializable {

    private static final long serialVersionUID = -685759544293388523L;

    private static final HashOrder hashOrder = new FNV1a64bitHash();

    @Override
    public int compare(String p1, String p2) {
        return hashOrder.compare(p1, p2);
    }

    /**
     * Computes the bucket index that corresponds to the given node name at the given depth.
     * 
     * @return and Integer between zero and {@link RevTree#MAX_BUCKETS} minus one
     */
    public Integer bucket(final String nodeName, final int depth) {

        final int byteN = hashOrder.byteN(nodeName, depth);
        Preconditions.checkState(byteN >= 0);
        Preconditions.checkState(byteN < 256);

        final int maxBuckets = RevTree.MAX_BUCKETS;

        final int bucket = (byteN * maxBuckets) / 256;
        return Integer.valueOf(bucket);
    }

    private static abstract class HashOrder extends Ordering<String> implements Serializable {

        private static final long serialVersionUID = -469599567110937126L;

        public abstract int byteN(String path, int depth);

    }

    /**
     * The FNV-1a hash function used as {@link Node} storage order.
     */
    private static class FNV1a64bitHash extends HashOrder {

        private static final long serialVersionUID = -1931193743208260766L;

        private static final UnsignedLong FNV64_OFFSET_BASIS = UnsignedLong
                .valueOf("14695981039346656037");

        private static final UnsignedLong FNV64_PRIME = UnsignedLong.valueOf("1099511628211");

        @Override
        public int compare(final String p1, final String p2) {
            UnsignedLong hash1 = fnv(p1);
            UnsignedLong hash2 = fnv(p2);
            return hash1.compareTo(hash2);
        }

        private static UnsignedLong fnv(CharSequence chars) {
            final int length = chars.length();

            UnsignedLong hash = FNV64_OFFSET_BASIS;

            for (int i = 0; i < length; i++) {
                char c = chars.charAt(i);
                byte b1 = (byte) (c >> 8);
                byte b2 = (byte) c;
                hash = update(hash, b1);
                hash = update(hash, b2);
            }
            return hash;
        }

        private static UnsignedLong update(UnsignedLong hash, final byte octet) {
            // it's ok to use the signed long value here, its a bitwise operation anyways, and its
            // on the lower byte of the long value
            final long longValue = hash.longValue();
            final long bits = longValue ^ octet;

            // System.err.println("hash : " + Long.toBinaryString(longValue));
            // System.err.println("xor  : " + Long.toBinaryString(bits));
            // System.err.println("octet: " + Integer.toBinaryString(octet));

            // convert back to unsigned long
            hash = UnsignedLong.fromLongBits(bits);
            // multiply by prime
            hash = hash.times(FNV64_PRIME);
            return hash;
        }

        /**
         * Returns the Nth unsigned byte in the hash of {@code nodeName} where N is given by
         * {@code depth}
         */
        @Override
        public int byteN(final String nodeName, final int depth) {
            Preconditions.checkArgument(depth < 8, "depth too deep: %s", Integer.valueOf(depth));

            final long longBits = fnv(nodeName).longValue();

            final int displaceBits = 8 * (7 - depth);// how many bits to right shift longBits to get
                                                     // the byte N

            final int byteN = ((byte) (longBits >> displaceBits)) & 0xFF;
            return byteN;
        }
    }

    /**
     * A HashOrder based on a {@link HashFunction}, for testing purposes, used to be the default
     * with a SHA-1 hash algorithm before 0.6.0
     */
    private static class HashFunctionOrder extends HashOrder {

        private static final long serialVersionUID = 1266787641620204350L;

        private final HashFunction hasher;

        public HashFunctionOrder(HashFunction hasher) {
            this.hasher = hasher;
        }

        @Override
        public int compare(String p1, String p2) {
            HashCode h1 = hasher.hashString(p1);
            HashCode h2 = hasher.hashString(p2);
            return ObjectId.compare(h1.asBytes(), h2.asBytes());
        }

        /**
         * Computes the bucket index that corresponds to the given node name at the given depth.
         * 
         * @return and Integer between zero and {@link RevTree#MAX_BUCKETS} minus one
         */
        @Override
        public int byteN(final String nodeName, final int depth) {
            byte[] hashCode = hasher.hashString(nodeName).asBytes();
            final int byteN = (byte) hashCode[depth] & 0xFF;
            return byteN;
        }
    }

    // public static void main(String a[]) {
    // try {
    // final int c = 5 * 1000 * 1000;
    //
    // List<Node> nodes = new ArrayList<Node>(c);
    // for (int i = 0; i < c; i++) {
    // Node node = node(i);
    // nodes.add(node);
    // }
    // nodes = Collections.unmodifiableList(nodes);
    //
    // System.err.println("\nTesting FNV1-A hash");
    // NodePathStorageOrder.hashOrder = new FNV1a64bitHash();
    // test(c, nodes);
    //
    // System.err.println("\nTesting murmur3_32 hash");
    // NodePathStorageOrder.hashOrder = new HashFunctionOrder(Hashing.murmur3_32());
    // test(c, nodes);
    //
    // System.err.println("\nTesting SHA-1 hash");
    // NodePathStorageOrder.hashOrder = new HashFunctionOrder(Hashing.sha1());
    // test(c, nodes);
    // } finally {
    // System.exit(0);
    // }
    // }
    //
    // private static void test(int c, List<Node> nodes) {
    // ObjectDatabase db = new HeapObjectDatabse(new DataStreamSerializationFactory());
    // db.open();
    //
    // RevTreeBuilder builder = new RevTreeBuilder(db);
    // nodes = new ArrayList<Node>(nodes);
    // System.err.print("sorting... ");
    // Stopwatch s = new Stopwatch().start();
    // Collections.sort(nodes, new NodeStorageOrder());
    // System.err.printf("%,d nodes sorted in %s\n", nodes.size(), s.stop());
    //
    // s.reset().start();
    // for (Node node : nodes) {
    // builder.put(node);
    // }
    // RevTree tree = builder.build();
    // db.put(tree);
    // System.err.printf("%,d nodes tree created in %s\n", tree.size(), s.stop());
    //
    // Set<Node> set = new TreeSet<Node>(new NodeStorageOrder());
    // set.addAll(nodes);
    // int diff = nodes.size() - set.size();
    // System.err.printf("%,d collisions\n", diff);
    // print(tree, db);
    // db.close();
    // }
    //
    // private static void print(RevTree tree, ObjectDatabase db) {
    // Iterator<RevTree> it = new DeepMove.AllTrees(tree.getId(), db);
    // int n = Iterators.size(it);
    // System.err.printf("%,d trees\n", n);
    //
    // Iterator<NodeRef> refs = new DepthTreeIterator("", ObjectId.NULL, tree, db,
    // Strategy.FEATURES_ONLY);
    // Stopwatch s = new Stopwatch().start();
    // n = Iterators.size(refs);
    // System.err.printf("%,d features traversed in %s\n", n, s.stop());
    // }
    //
    // private static Node node(int i) {
    // String name = String.valueOf(i);
    // return Node.create(name, ObjectId.forString(name), ObjectId.NULL, TYPE.FEATURE);
    // }
}