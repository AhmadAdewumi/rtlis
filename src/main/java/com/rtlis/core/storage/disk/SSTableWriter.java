package com.rtlis.core.storage.disk;

import com.rtlis.core.model.Point;
import com.rtlis.core.storage.filter.BloomFilter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.List;

public class SSTableWriter {
    //-- RTLI in ASCII, would have use RTLIS, but that's gonna overflow for int
    private static final int MAGIC_NUMBER = 0x52544C49; //-- signature for the file, to recognize it when we wanna read back

    //-- m --> bloom filter size
    //-- k --> hash functions

    /**
     * Writes a list of {@code Point} objects to the specified file in a structured format.
     * Each {@code Point} is serialized along with a Bloom filter that efficiently represents
     * the vehicleId values. The generated output file contains a header and data sections,
     * with the header containing Bloom filter metadata and the data section storing the serialized
     * {@code Point} objects.
     *
     * @param points   the list of {@code Point} objects to be written to the file. Each
     *                 {@code Point} contains information about longitude, latitude, vehicleId,
     *                 and timestamp.
     * @param filePath the file path where the serialized data will be written.
     * @throws IOException if an I/O error occurs while writing to the file.
     */
    public void write(List<Point> points, String filePath) throws IOException {
        //-- 1% positive rate for the expected number of points
        BloomFilter bloomFilter = BloomFilter.createOptimal(points.size(), 0.01);

        for (Point p : points) {
            bloomFilter.add(p.getVehicleId()); //-- send in each vehicleId
        }

        //-- serialize the bloomfilter to  bytes
        byte[] bloomBytes = bitsSetToBytes(bloomFilter.getBitSet(), bloomFilter.getM());

        try (FileOutputStream fos = new FileOutputStream(filePath);
             FileChannel channel = fos.getChannel()) {

            ByteBuffer headerBuffer = ByteBuffer.allocate(16 + bloomBytes.length);
            headerBuffer.putInt(MAGIC_NUMBER); //--4 bytes
            headerBuffer.putInt(bloomFilter.getM()); //-- 4 bytes
            headerBuffer.putInt(bloomFilter.getK()); //-- 4 bytes
            headerBuffer.putInt(bloomBytes.length); //-- 4 bytes
            headerBuffer.put(bloomBytes); //-- N bytes

            headerBuffer.flip();
            channel.write(headerBuffer);

            //-- this will be reusable and also, it is not managed by the JVM, no GC and off heap
            ByteBuffer dataBuffer = ByteBuffer.allocateDirect(64 * 1024); //-- 64KB

            for (Point p : points) {
                byte[] idBytes = p.getVehicleId().getBytes(StandardCharsets.UTF_8);
                int recordSize = 4 + idBytes.length + 8 + 8 + 8;

                //-- if this record doesn't fit in the remaining dataBuffer space, we flush and clear
                if (dataBuffer.remaining() < recordSize) {
                    dataBuffer.flip(); //-- flip to write mode
                    channel.write(dataBuffer); //-- write
                    dataBuffer.clear(); //-- clear
                }

                //-- we write the data in the order we added their bytes
                dataBuffer.putInt(idBytes.length); //-- 4 bytes: length of vehicleId
                dataBuffer.put(idBytes); //-- N bytes - vehicleId in UTF-8
                dataBuffer.putDouble(p.getLongitude()); //-- 8 bytes longitude
                dataBuffer.putDouble(p.getLatitude()); //-- 8 bytes latitude
                dataBuffer.putLong(p.getTimestamp()); //-- 8 bytes, timestamp
            }

            //-- to write any remaining bytes
            dataBuffer.flip(); //-- we flip/prepare the dataBuffer to reading mode for reading by the channel
            if (dataBuffer.hasRemaining()) {
                channel.write(dataBuffer); //-- we write any remaining bytes to disk
            }
        }
    }

    /**
     * Converts a BitSet into a byte array representation, ensuring the byte array
     * is sized to accommodate the specified number of bits.
     *
     * @param bitSet the BitSet to convert to a byte array
     * @param m the number of bits that the BitSet represents
     * @return a byte array containing the representation of the provided BitSet,
     *         sized to fit the specified number of bits
     */
    private byte[] bitsSetToBytes(BitSet bitSet, int m) {
        //-- we cal how many bytes are needed for m bits
        int numBytes = (m + 7) / 8; //-- this is a roundUp logic , why not just 9/8 = 1, it chops off remainder, we lose the 9th bit, but (9+7)/8 = 2 bytes (covers the remaining bit
        byte[] bytes = new byte[numBytes]; //-- create an array with that size
        byte[] originalContent = bitSet.toByteArray();

        //-- then, we copy iy into our byte array
        //-- arraycopy(Object src, int srcIndex, Object dest, int destIndex, int len -> no. of elements to be copied to the dest.array)
        System.arraycopy(originalContent, 0, bytes, 0, originalContent.length);
        return bytes;
    }
}
