package com.rtlis.core.storage.filter;

public class MurmurHash3 {
    private MurmurHash3() {
        throw new AssertionError("No Instances of MurmurHash");
    }

    public static long hash64(byte[] data, int seed) {
        final long c1 = 0x87c37b91114253d5L;
        final long c2 = 0x4cf5ad432745937fL;

        int length = data.length;
        long h = seed;

        int numChunks = length / 8;
        int offset = 0;
        for (int i = 0; i < numChunks; i++) {
            long k = readLittleEndianLong(data, offset);
            offset += 8;

            k *= c1;
            k = Long.rotateLeft(k, 31);
            k *= c2;

            h ^= k;
            h = Long.rotateLeft(h, 27);
            h = h * 5 + 0x52dce729;
        }

        long k1 = 0;
        int remaining = length % 8;
        int tailOffset = offset;
        switch (remaining) {
            case 7:
                k1 ^= (data[tailOffset + 6] & 0xFFL) << 48;
            case 6:
                k1 ^= (data[tailOffset + 5] & 0xFFL) << 40;
            case 5:
                k1 ^= (data[tailOffset + 4] & 0xFFL) << 32;
            case 4:
                k1 ^= (data[tailOffset + 3] & 0xFFL) << 24;
            case 3:
                k1 ^= (data[tailOffset + 2] & 0xFFL) << 16;
            case 2:
                k1 ^= (data[tailOffset + 1] & 0xFFL) << 8;
            case 1:
                k1 ^= (data[tailOffset] & 0xFFL);
        }

        k1 *= c1;
        k1 = Long.rotateLeft(k1, 31);
        k1 *= c2;
        h ^= k1;

        h ^= length;
        h ^= h >>> 33;
        h *= 0xff51afd7ed558ccdL;
        h ^= h >>> 33;
        h *= 0xc4ceb9fe1a85ec53L;
        h ^= h >>> 33;

        return h;
    }

    private static long readLittleEndianLong(byte[] data, int offset) {
        long result = 0;
        for (int i = 0; i < 8; i++) {
            long byteValue = data[offset + i] & 0xFFL;
            result |= byteValue << (i * 8);
        }

        return result;
    }
}
