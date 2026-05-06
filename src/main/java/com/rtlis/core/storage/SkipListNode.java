package com.rtlis.core.storage;

import com.rtlis.core.model.Point;

/**
 * Represents a node in a skip list data structure. Each node contains a payload in the form of a {@code Point} object,
 * an array of forward pointers connecting it to other nodes at various levels, and a height indicating
 * the number of levels the node participates in.
 */
public class SkipListNode {
    //-- the payload
    private final Point point;

    /**
     * array of forward pointers.
     * forward[0] = next node on Level 0 (base linked list)
     * forward[1] = next node on Level 1 (first express lane)
     * forward[2] = next node on Level 2 (second express lane)
     * ...
     * forward[height - 1] = next node on the highest level this node exists on
     * -- visualize as something like a stack of LL
     */
    private SkipListNode[] forward;


    //-- the number of levels this node participates in
    private final int height;

    public SkipListNode(Point point, int height) {
        this.point = point;
        this.height = height;
        this.forward = new SkipListNode[height]; //-- all init forward defaults to null
    }

    public Point getPoint() {
        return point;
    }

    public int getHeight() {
        return height;
    }

    /**
     * Retrieves the forward pointer at the specified level for this SkipListNode.
     *
     * @param level the level of the forward pointer to retrieve, where 0 represents the base linked list level and top level = height - 1
     * @return the SkipListNode that the forward pointer at the specified level references, or {@code null} if no such node exists
     */
    public SkipListNode getForward(int level) {
        return forward[level];
    }

    /**
     * Updates the forward pointer for this SkipListNode at the specified level.
     * The forward pointer at a given level is used to establish a connection to another node
     * in the skip list at that level.
     *
     * @param level the level at which the forward pointer should be set, where 0 represents
     *              the base linked list level and top level = height - 1
     * @param node  the SkipListNode to be referenced by the forward pointer at the specified level
     */
    public void setForward(int level, SkipListNode node) {
        forward[level] = node;
    }
}
