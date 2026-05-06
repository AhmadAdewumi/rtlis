package com.rtlis.core.spatial;

import com.rtlis.core.model.BoundingBox;
import com.rtlis.core.model.Point;

import java.util.List;

public class QuadTree {
    private final BoundingBox boundary;
    private int capacity;
    private final Point[] points;
    private int count;
    private boolean isDivided;

    private QuadTree northWest;
    private QuadTree northEast;
    private QuadTree southWest;
    private QuadTree southEast;

    public QuadTree(BoundingBox boundary, int capacity) {
        this.boundary = boundary;
        this.capacity = capacity;
        this.points = new Point[this.capacity];
        this.isDivided = false;
        this.count = 0;
    }

    /**
     * Attempts to insert the given point into the QuadTree. If the QuadTree node has
     * reached its capacity, it will subdivide and delegate the insertion to one of its
     * child nodes.
     *
     * @param p the point to be inserted into the QuadTree
     * @return {@code true} if the point was successfully inserted; {@code false} if the point
     * does not lie within the boundary of this QuadTree node or if insertion fails
     */
    public boolean insert(Point p) {
        //-- TODO--  recursive insertion and subdivision logic here
        //-- if the point does not belong in the re boundary, i.e rectangle,
        //-- it means the node cannot store ut
        if (!this.boundary.contains(p)) return false;

        //-- check if there is still free space in the node's point array
        if (this.count < this.capacity) {
            this.points[this.count] = p;
            this.count++;
            return true;
        }

        //-- if the node is full and not yet split, we split it by creating the four children
        //-- divide if needed
        if (!this.isDivided) {
            this.subdivide();
        }

        //--0 we try each child until a child accepts it
        return this.northWest.insert(p) ||
                this.northEast.insert(p) ||
                this.southWest.insert(p) ||
                this.southEast.insert(p);
    }

    /**
     * Divides the current QuadTree node into four child nodes, each representing one quadrant
     * of the current node's boundary. This method is typically invoked when the node reaches
     * its capacity and needs to manage more points.
     * <p>
     * The following operations are performed:
     * 1. The center of the current node's boundary is computed.
     * 2. The boundaries of the four child quadrants (northwest, northeast, southwest, southeast)
     * are calculated based on the center and half-dimensions of the current node's boundary.
     * 3. Four new child QuadTree nodes are created, each with its respective boundary and capacity.
     * 4. The current node is marked as divided to indicate that it now contains child nodes.
     * <p>
     * Preconditions:
     * - The current node's boundary must be non-null.
     * - The capacity of the node must have been reached to justify subdivision.
     * <p>
     * Postconditions:
     * - The `isDivided` property of the current QuadTree instance is set to `true`.
     * - Four child QuadTree instances (northWest, northEast, southWest, southEast) are initialized.
     * <p>
     * This method is typically a private helper for managing the hierarchical structure of
     * the QuadTree as points are inserted and the storage capacity of the node is exceeded.
     */
    private void subdivide() {
        //-- boundary splitting impl. here
        //-- get the parent center
        double centerX = this.getBoundary().getCenterX();
        double centerY = this.getBoundary().getCenterY();

        //-- compute half size for each child
        double childHalfWidth = this.boundary.getHalfWidth() / 2;
        double childHalfHeight = this.boundary.getHalfHeight() / 2;

        //-- build four child boxes around the parent center
        BoundingBox nwBox = new BoundingBox(
                centerX - childHalfWidth,
                centerY + childHalfHeight,
                childHalfWidth,
                childHalfHeight
        );

        BoundingBox neBox = new BoundingBox(
                centerX + childHalfWidth,
                centerY + childHalfHeight,
                childHalfWidth,
                childHalfHeight
        );

        BoundingBox swBox = new BoundingBox(
                centerX - childHalfWidth,
                centerY - childHalfHeight,
                childHalfWidth,
                childHalfHeight
        );

        BoundingBox seBox = new BoundingBox(
                centerX + childHalfWidth,
                centerY - childHalfHeight,
                childHalfWidth,
                childHalfHeight
        );

        //-- create 4 QuadTrees
        this.northWest = new QuadTree(nwBox, this.capacity);
        this.northEast = new QuadTree(neBox, this.capacity);
        this.southWest = new QuadTree(swBox, this.capacity);
        this.southEast = new QuadTree(seBox, this.capacity);

        //-- mark the node as divided
        this.isDivided = true;
    }

    /**
     * Retrieves all points within the specified search range and stores them in the provided list.
     * If the current QuadTree node's boundary does not intersect with the search range, the method exits early.
     * Otherwise, it checks points in the current node and recursively queries child QuadTree nodes if subdivided.
     *
     * @param searchRange the bounding box defining the area to search for points
     * @param foundPoints the list where points within the specified search range will be added
     */
    public void query(BoundingBox searchRange, List<Point> foundPoints) {
        //-- check if the node is relevant, if not we won't bother searching
        if (!this.boundary.intersects(searchRange)) return;

        //--check the points stored in the node, if it lies in the rectangle,
        //-- we add it to our list of found nodes
        for (int i = 0; i < this.count; i++) {
            Point p = this.points[i];
            if (searchRange.contains(p)) {
                foundPoints.add(p);
            }
        }

        //-- if the rectangle has been divided, we search inside each child also
        if (this.isDivided) {
            this.northWest.query(searchRange, foundPoints);
            this.northEast.query(searchRange, foundPoints);
            this.southWest.query(searchRange, foundPoints);
            this.southEast.query(searchRange, foundPoints);
        }
    }

    public BoundingBox getBoundary() {
        return boundary;
    }

    public int getCapacity() {
        return capacity;
    }

    public Point[] getPoints() {
        return points;
    }

    public boolean isDivided() {
        return isDivided;
    }

    public QuadTree getNorthWest() {
        return northWest;
    }

    public QuadTree getNorthEast() {
        return northEast;
    }

    public QuadTree getSouthWest() {
        return southWest;
    }

    public QuadTree getSouthEast() {
        return southEast;
    }
}
