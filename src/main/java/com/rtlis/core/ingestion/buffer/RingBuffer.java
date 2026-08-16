package com.rtlis.core.ingestion.buffer;

import com.rtlis.core.model.Point;

public class RingBuffer {
    private final Point[] buffer;
    private final int mask;

    /**
     * -- CPU loads data in 64 bytes chunks called cache lines
     */
    private long p1, p2, p3, p4, p5, p6, p7; //-- padding to prevent false sharing, 56 bytes padding

    //-- with volatile, when we write something, it is flushed to main memory, if we do not addd that, let's say Thread A writes 5,
    //-- then, Thread B might see 0 or 5 or garbage, coz the CPU caches it on its L1 cache, but thread A and B's core has their own L1 cache
    //-- Thread B doesn't now Thread A changed the value, with volatile, if Thread A values is modified, it is flushed into main memory,
    //-- Thread A invalidates its own cache, and fetches from the main memory, thread B always see the latest value
    private volatile long writeSequence;

    private long p8, p9, p10, p11, p12, p13, p14; //-- padding to prevent false sharing
    private volatile long readSequence;

    public RingBuffer(int powerOfTwoSize) {
        boolean isPowerOfTwo = ((powerOfTwoSize & (powerOfTwoSize - 1)) == 0 && powerOfTwoSize > 0);

        if (!isPowerOfTwo) {
            throw new IllegalArgumentException("Please, pass in a power of 2");
        }

        this.buffer = new Point[powerOfTwoSize];
        //-- we wil need to perform AND ops with this, e.g 1023
        this.mask = powerOfTwoSize - 1; //-- -1 ensures the mask gives valid indices, this bitwise AND trick only works for power of 2
        this.writeSequence = 0;
        this.readSequence = 0;
    }

    /**
     * Single producer approach, //-- volatile sequence keep everything visible across cores
     * -- No GC pressure,the array is pre allocated and slots can be re used
     * @param point
     * @return
     */
    public boolean offer(Point point) {
        if (writeSequence - readSequence == buffer.length) {
            return false; //-- that means our buffer is
        }

        int index = (int) (writeSequence & mask);
        buffer[index] = point;
        writeSequence++;
        return true;
    }

    public Point poll() {
        if (readSequence == writeSequence) return null; //--consumer caught up, nothing to read

        int index = (int) (readSequence & mask);
        Point point = buffer[index];
        buffer[index] = null; //--GC can then reclaim it
        readSequence++;
        return point;
    }


    

}
