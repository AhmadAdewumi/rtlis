package com.rtlis.core;

import com.rtlis.core.model.Point;
import com.rtlis.core.storage.SkipList;
import com.rtlis.core.storage.disk.SSTableWriter;

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



        System.out.println("Size before flush: " + skipList.getSize());
        System.out.println("Current Level: " + skipList.getCurrentLevel());

        List<Point> flushed = skipList.flushAll();

        //-- write to disk
        SSTableWriter writer = new SSTableWriter();
        writer.write(flushed, "test_sstable.dat");

        System.out.println("SSTable written successfully to disk");
        System.out.println("File size: " + new File("test_sstable.dat").length() + " bytes");

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