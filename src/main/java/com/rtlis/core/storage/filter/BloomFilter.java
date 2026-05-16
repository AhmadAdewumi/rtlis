package com.rtlis.core.storage.filter;

import java.nio.charset.StandardCharsets;
import java.util.BitSet;

public class BloomFilter {
    /**
     * Attributes
     * -- something to store the bitset
     * -- number of bits
     * -- the number of hash fuunctions
     * -- a count of how many things we have inserted(not really necessary tho
     */

    private final BitSet bitSet;
    private final int m;
    private final int k;
    private int insertions;

    public BloomFilter(int m, int k) {
        this.m = m;
        this.k = k;
        this.bitSet = new BitSet(m); //- al its init  as 0
        this.insertions = 0;
    }

    public BloomFilter(int m, int k, BitSet bitSet){
        this.m = m;
        this.k = k;
        this.bitSet = bitSet;
    }

    /**
     * Adds the given key to the Bloom filter by setting k bits in the underlying BitSet.
     * The k bits are determined using double hashing based on two hash values of the key.
     *
     * @param key the input string key to be added to the Bloom filter
     */
    //-- it flips k bits to 1 for  a given key
    public void add(String key) { //-- TODO -- REFACTOR LATER TO USE getBitPosition
        //-- convert key to bytes
        //-- loop up to k positions
        //-- compute position
        //--set bit

        byte[] strBytes = key.getBytes(StandardCharsets.UTF_8);

        long hash1 = MurmurHash3.hash64(strBytes, 0);
        long hash2 = MurmurHash3.hash64(strBytes, 0xDEAD); //-- man, never thought i could pass an hex like this

        for (int i = 0; i < k; i++) {
            long combinedHash = hash1 + ((long) i * hash2); //-- double hashing, to generate a sequence of distinct positions
            int bitPosition = (int) ((combinedHash & Long.MAX_VALUE) % m); //-- bitwise AND ensures the result is +ve and we cast to int after tthe addition to maintain he precision
            bitSet.set(bitPosition);
        }

    }

    /**
     * Checks whether the given key might be present in the Bloom filter.
     * This method evaluates if all the bits corresponding to the key's hash values are set to 1.
     * False negatives are not possible, but there is a probability of false positives.
     *
     * @param key the input string key to be checked for presence in the Bloom filter
     * @return true if the key might be present in the Bloom filter, false if it is definitely not present
     */
    //-- checks if all 0's are flipped to 1's for a single key
    public boolean mightContain(String key) {   //-- TODO -- REFACTOR LATER TO USE getBitPosition
        byte[] strBytes = key.getBytes(StandardCharsets.UTF_8);
        long hash1 = MurmurHash3.hash64(strBytes, 0);
        long hash2 = MurmurHash3.hash64(strBytes, 0xDEAD);

        for (int i = 0; i < k; i++) {
            long combinedHash = hash1 + ((long) i * hash2);

            //-- Long.MAX_VALUE = 0 + sixty-one 1's, the  AND operator forces the first/sign bit to be 0, so no -ve no. is ever possible
            //-- %m to shrinkle it down into the range 0, m-1
            //-- why cast? coz BitSet useds 32 bit int
            int bitPosition = (int) ((combinedHash & Long.MAX_VALUE) % m);
            if (!bitSet.get(bitPosition)) return false;
        }

        return true;
    }

    /**
     * Computes the bit position in the Bloom filter for a given byte array and hash index using
     * MurmurHash3 algorithms for hashing. The bit position is calculated as a non-negative
     * integer within a defined range.
     *
     * @param data The input byte array representing the key to be hashed.
     * @param i The index of the hash function in the series of hash functions used by the Bloom filter.
     * @return The computed bit position as a non-negative integer within the range [0, m-1],
     *         where m is the total number of bits in the Bloom filter.
     */
    private int getBitPosition(byte[] data, int i) {
        //-- generate and calc. the combined hash, this is a.k.a Kirtz Mitzenmacher optimization
        long hash1 = MurmurHash3.hash64(data, 0);
        long hash2 = MurmurHash3.hash64(data, 0xDEAD);

        long combinedHash = hash1 + (i * hash2);

        return (int) (combinedHash & Long.MAX_VALUE) % m;
    }


    /**
     * Creates an optimal Bloom filter configuration given the expected number of insertions
     * and desired false positive rate. The method calculates the required number of bits (m)
     * and hash functions (k) using standard formulas to minimize false positives.
     *
     * @param expectedInsertions the anticipated number of elements to be inserted into the Bloom filter
     * @param falsePositiveRate the acceptable probability of false positives, between 0 and 1 (exclusive)
     * @return a new BloomFilter instance configured with optimal parameters (m and k)
     */
    public static BloomFilter createOptimal(int expectedInsertions, double falsePositiveRate) {
        //-- formula --> m = -n.ln(p)/ln(2)^2
        int m = (int) (-(expectedInsertions * Math.log(falsePositiveRate)) / (Math.pow(Math.log(2), 2)));
        //-- k = m/n * ln(2)
        int k = (int) ((m / (double) expectedInsertions) * Math.log(2));

        return new BloomFilter(m, k);
    }

    public int getK() {
        return k;
    }

    public int getM() {
        return m;
    }

    public int getInsertions() {
        return insertions;
    }

    public BitSet getBitSet(){
        return bitSet;
    }
}
