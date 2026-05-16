package com.rtlis.core;

import com.rtlis.core.model.Point;
import com.rtlis.core.storage.SkipList;
import com.rtlis.core.storage.disk.SSTableReader;
import com.rtlis.core.storage.disk.SSTableWriter;
import com.rtlis.core.storage.filter.BloomFilter;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        SkipList skipList = new SkipList();

        // Insert with timestamps out of order
//        skipList.insert(new Point(0.0, 0.0, "V1", 100));
//        skipList.insert(new Point(0.0, 0.0, "V2", 50));
//        skipList.insert(new Point(0.0, 0.0, "V3", 150));
//        skipList.insert(new Point(0.0, 0.0, "V4", 75));
//        skipList.insert(new Point(0.0, 0.0, "V5", 200));

        skipList.insert(new Point(3.3792, 6.5244, "ABC-123", 100));
        skipList.insert(new Point(3.3793, 6.5245, "DEF-456", 50));
        skipList.insert(new Point(3.3794, 6.5246, "GHI-789", 75));

        System.out.println("==== WRITE PHASE ====");
        System.out.println("Size before flush: " + skipList.getSize());
        System.out.println("Current Level: " + skipList.getCurrentLevel());

        List<Point> flushed = skipList.flushAll();

        //-- write to disk
        SSTableWriter writer = new SSTableWriter();
        writer.write(flushed, "test_sstable.dat");

        System.out.println("SSTable written successfully to disk");
        System.out.println("File size: " + new File("test_sstable.dat").length() + " bytes");

        System.out.println("\n===== READ PHASE ======");
        SSTableReader reader = new SSTableReader();

        //-- Test 1 -- read header and check bloom filter
        BloomFilter bloomFilter = reader.readHeader("test_sstable.dat");
        System.out.println("BloomFilter: m=" + bloomFilter.getM() + ", k=" + bloomFilter.getK());

        // Test 2: Bloom filter should find inserted vehicles
        System.out.println("\nBloom filter queries:");
        System.out.println("  Might contain 'ABC-123'? " + bloomFilter.mightContain("ABC-123")); // should be true
        System.out.println("  Might contain 'DEF-456'? " + bloomFilter.mightContain("DEF-456")); // should be true
        System.out.println("  Might contain 'GHI-789'? " + bloomFilter.mightContain("GHI-789")); // should be true
        System.out.println("  Might contain 'XYZ-999'? " + bloomFilter.mightContain("XYZ-999")); // should be false

        // Test 3: Read all points back
        List<Point> readPoints = reader.readPoints("test_sstable.dat");
        System.out.println("\nPoints read back: " + readPoints.size());
        for (Point p : readPoints) {
            System.out.println("  Vehicle: " + p.getVehicleId() + ", Timestamp: " + p.getTimestamp() +
                    ", Lat: " + p.getLatitude() + ", Lon: " + p.getLongitude());
        }

        //-- TEST 4 VERIFICATION TO TEST DATA INTEGRITY
        System.out.println("=== VERIFICATION ===");
        boolean allMatch = true;
        for (int i = 0; i < flushed.size(); i++) {
            Point original = flushed.get(i);
            Point readback = readPoints.get(i);

            if (!original.getVehicleId().equals(readback.getVehicleId()) || original.getTimestamp() != readback.getTimestamp()){
                allMatch=false;
                System.out.println("MISMATCH  at index: " + i);
            }

        }

        System.out.println("All points match? "+ allMatch);


//        System.out.println("\n Flushed timestamps (they should be sorted): ");
//        for (Point p : flushed) {
//            System.out.println("Vehicle with ID: " + p.getVehicleId() + " with timestamp: " + p.getTimestamp());
//        }
//
//        //-- testing the reset state
//        System.out.println("\nSize after flush: " + skipList.getSize());
//        System.out.println("Current level after flush: " + skipList.getCurrentLevel());
    }
}