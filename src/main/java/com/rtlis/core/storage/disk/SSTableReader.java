package com.rtlis.core.storage.disk;

import com.rtlis.core.model.Point;
import com.rtlis.core.storage.filter.BloomFilter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class SSTableReader {
    private static final int EXPECTED_MAGIC_NUMBER = 0x52544C49; //-- RTLI

    public BloomFilter readHeader(String filePath) throws IOException {
        try (
                FileInputStream fis = new FileInputStream(filePath);
                FileChannel fileChannel = fis.getChannel();
        ) {
            //-- read first 16 bytes
            ByteBuffer headerBuffer = ByteBuffer.allocate(16);
            while (headerBuffer.hasRemaining()) {
                fileChannel.read(headerBuffer);
            }
            headerBuffer.flip(); //-- turn to reading mode

            int magicNumber = headerBuffer.getInt();
            if (magicNumber != EXPECTED_MAGIC_NUMBER) {
                throw new IOException("Magic Number/Signature Mismatch");
            }

            int m = headerBuffer.getInt(); //--total bits
            int k = headerBuffer.getInt(); //-- total number of hashes
            int bloomLength = headerBuffer.getInt(); //-- number of bytes to read

            //-- read bloomLength bytes into a new byte[]
            ByteBuffer bloomBuffer = ByteBuffer.allocate(bloomLength);

            while (bloomBuffer.hasRemaining()) {
                fileChannel.read(bloomBuffer);
            }

            bloomBuffer.flip();

            byte[] bloomBytes = new byte[bloomLength];
            bloomBuffer.get(bloomBytes);

            //-- convert bytes to bitset
            BitSet bitSet = BitSet.valueOf(bloomBytes);
            return new BloomFilter(m, k, bitSet);

        }
    }

    public List<Point> readPoints(String filePath) throws IOException {
//        1. Open file
//        2. Skip header: 16 + bloomLength bytes
//        3. While there are bytes remaining:
//            a. Read 4 bytes --> int idLength
//            b. Read idLength bytes → String vehicleId
//            c. Read 8 bytes --> double longitude
//            d. Read 8 bytes --> double latitude
//            e. Read 8 bytes --> long timestamp
//            f. Create new Point(vehicleId, lat, lon, timestamp)
//            g. Add to list
//        4. Return list

        List<Point> points = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             FileChannel fileChannel = fis.getChannel();
        ) {
            ByteBuffer fixedHeader = ByteBuffer.allocate(16);

            while (fixedHeader.hasRemaining()) {
                fileChannel.read(fixedHeader);
            }
            fixedHeader.flip();

            int magicNumber = fixedHeader.getInt(); //-- bytes 0 to 3
            if (magicNumber != EXPECTED_MAGIC_NUMBER) {
                throw new IOException("Invalid SSTable File: Magic Number/Signature Mismatch");
            }

            int m = fixedHeader.getInt(); //-- bytes 4 to 7
            int k = fixedHeader.getInt();  //-- bytes 7 to 11
            int bloomLength = fixedHeader.getInt(); //-- bytes 11 to 14

            //-- we skip past the bloom bytes, channel at bytes 16, we advance past it
            fileChannel.position(16 + bloomLength);

            //-- read points until end of file
            ByteBuffer buffer = ByteBuffer.allocate(64 * 1024);
            while (fileChannel.read(buffer) != -1) {//-- -1 means end of file
                buffer.flip(); //-- flip to read mode

                //-- we process all complete records in the buffer
                while (buffer.remaining() >= 4) { //-- at the minimum, we need 4 bytes for 4 bytes for idLength
                    int savedPosition = buffer.position(); //-- save position in case we hae partial records

                    //-- to check if we have enough bytes for a full record
                    if (buffer.remaining() < 4) break;
                    int idLength = buffer.getInt(); //-- a: 4 bytes --> idLength

                    if (buffer.remaining() < idLength + 24) { //-- i.e 24 = 8+8+8 for lon, lan and timestamp
                        buffer.position(savedPosition);
                        break;
                    }

                    byte[] idBytes = new byte[idLength];
                    buffer.get(idBytes); //-- b: idLength bytes --> id
                    String vehicleId = new String(idBytes, StandardCharsets.UTF_8);

                    double longitude = buffer.getDouble();  // c: 8 bytes --> longitude
                    double latitude = buffer.getDouble();   // d: 8 bytes --> latitude
                    long timestamp = buffer.getLong();      // // e: 8 bytes --> timestamp

                    points.add(new Point(longitude, latitude, vehicleId, timestamp));
                }

                buffer.compact(); //-- move unread bytes to front of buffer for next read
            }
        }

        return points;
    }


}
