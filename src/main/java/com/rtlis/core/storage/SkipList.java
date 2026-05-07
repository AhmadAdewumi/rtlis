package com.rtlis.core.storage;

import com.rtlis.core.model.Point;

import java.util.ArrayList;
import java.util.List;


/**
 * A probabilistic Skip list implementation, not a perfect kip list acting as our MemTable
 * head is a sentinel/placeholder node with height = MAX_LEVEL, it holds no data
 * - For any level L > 0, if a node appears on level L, it MUST also appear
 * on all levels 0 through L-1. (A node's height extends from Level 0 upward.)
 * - currentLevel is the index of the highest level that has at least one
 * non-head node. Searching starts from currentLevel, not MAX_LEVEL.
 * <p>
 * -- this is the mem table which lives on the heap, when it is full or reaches the size threshold set,
 * its content are flushed onto disk and a new SkipList is allocated
 */
public class SkipList {
    /**
     * Maximum possible height for any node in this SkipList.
     * For a MemTable holding up to ~100,000 elements, log₂(100,000) ≈ 17.
     * We use 16 as a power-of-two ceiling that covers this comfortably.
     * Each additional level doubles the reach, so 16 levels covers
     * up to 2^16 ≈ 65,536 with high probability -- and more in expectation
     * due to the probabilistic distribution.
     * Choosing a higher value for {@code MAX_LEVEL} allows for fewer elements per level,
     * improving lookup times in case we have large data sets at the cost of slightly
     * increased memory consumption.
     */
    private static final int MAX_LEVEL = 16;

    /**
     * Probability threshold for a node to gain an additional level.
     * 0.5 means each extra level has a 50% chance. This gives:
     * Level 0: 100% of nodes
     * Level 1: ~50%
     * Level 2: ~25%
     * Level 3: ~12.5%
     * ...and so on
     */
    private static final double P = 0.5;

    /**
     * Sentinel head node. Exists on all levels (height = MAX_LEVEL).
     * Contains no data. All forward pointers start as null.
     */
    private final SkipListNode head;

    /**
     * Number of Point elements currently in the SkipList.
     * Tracked explicitly to make the flush threshold check O(1)
     * rather than requiring a full traversal.
     */
    private int size;

    /**
     * The highest level index that currently has at least one non-head node.
     * - Empty SkipList: currentLevel = 0
     * - Max possible: MAX_LEVEL - 1 (because arrays are 0-indexed)
     * <p>
     * Searching starts from this level to avoid iterating through empty
     * express lanes at the top.
     */
    private int currentLevel;

    public SkipList() {
        // head has no data, we pass null for Point.
        this.head = new SkipListNode(null, MAX_LEVEL);
        this.currentLevel = 0;
        this.size = 0;
    }

    public int getSize() {
        return size;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public SkipListNode getHead() {
        return head;
    }

    public void insert(Point point) {
        /**
         * create update array, which tracks the rightmost node for each level
         * we will need this nodes later to connect the new node into the linked list
         */
        SkipListNode[] update = new SkipListNode[MAX_LEVEL];

        //-- init our traversal from the head
        SkipListNode current = this.head;

        //-- we go downwards (high to low level), searching from top to down
        for (int level = currentLevel; level >= 0; level--) {
            //-- we move right as long as - the next node exists and - the next node's timestamp is less than the new point's timestamp
            while (current.getForward(level) != null && current.getForward(level).getPoint().getTimestamp() < point.getTimestamp()) {
                current = current.getForward(level);
            }

            //-- here, we can't go further without overshooting, we record the current node as  the drop point for this level
            //-- at that level, that is where we will connect the new node at that level
            update[level] = current;
        }

        //-- we generate the new node's height probabilistically but capped at 16
        //-- simulate the coin flipping, keep moving up when it is still head and stop/exit when we get a tail
        int nodeHeight = randomHeight();

        //-- here, we check if the new node is taller than any existing node, so as to handle the new levels
        //-- why -1? coz, nodeHeight count is 1 based, and we start our level from index 0
        if (nodeHeight - 1 > currentLevel) {
            // For all new levels above the old currentLevel:
            // There are no existing nodes at these levels, so the "previous node" at each of these levels is the head.
            // The head exists on all levels up to MAX_LEVEL
            for (int i = currentLevel + 1; i <= nodeHeight - 1; i++) {
                update[i] = this.head;
            }

            //-- we update the current level tracked by the SkipList, important
            //--... so, we won't waste time searching empty levels
            currentLevel = nodeHeight - 1;
        }

        SkipListNode newNode = new SkipListNode(point, nodeHeight);

        //-- time to splice the new node into every linked list at each level
        //-- from level 0 to level[nodeHeight-1]
        for (int i = 0; i < nodeHeight; i++) {
            //-- newNode's forward at this current level = whatever update[i] was pointing to
            //-- if it was Head -> null before, we wanna insert 100, say our i starts from 0 to 2
            newNode.setForward(i, update[i].getForward(i)); //-- at level 0, [100] -> null, same at lvl 1 and 2

            //-- update[i]'s forward at this level will now be the new node
            update[i].setForward(i, newNode); //-- at level 0, Head's forward to 100: Head -> [100]
            //-- then we have Head -> [100] -> null
        }

        size++; //-- we increment the size counter
    }

    private int randomHeight() {
        int level = 1;
        while (Math.random() < P && level < MAX_LEVEL) {
            level++;
        }

        return level;
    }

    public List<Point> flushAll() {
        List<Point> flushed = new ArrayList<>(this.size); //--we create a list with exact capacity to avoid resizing

        //-- level 0 is the base linked list that contains every node in sorted order
        SkipListNode current = this.head.getForward(0);

        //-- just a normal linked list traversal, starting from head
        while (current != null) {
            flushed.add(current.getPoint());
            current = current.getForward(0);
        }

        //-- since the flushed list now contain every points, purge all and reset all head forward pointers to null, GC clean up after
        for (int i = 0; i < MAX_LEVEL; i++) {
            this.head.setForward(i, null);
        }

        //-- reset te state
        this.size = 0;
        this.currentLevel = 0;

        //-- we return sorted data for SSTable flush
        return flushed;
    }
}
