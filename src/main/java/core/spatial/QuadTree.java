package core.spatial;

import core.model.BoundingBox;
import core.model.Point;

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

    private boolean insert(Point p) {
        //-- TODO--  recursive insertion and subdivision logic here
        return false;
    }

    private void subdivide() {
        //-- boundary splitting impl. here
        //-- get the parent center
        double centerX = this.getBoundary().getCenterX();
        double centerY = this.getBoundary().getCenterY();

        //-- compute half size for each child
        double childHalfWidth = this.boundary.getHalfWidth() / 2;
        double childHalfHeight = this.boundary.getHalfHeight() / 2;

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

        this.northWest = new QuadTree(nwBox, this.capacity);
        this.northEast = new QuadTree(neBox, this.capacity);
        this.southWest = new QuadTree(swBox, this.capacity);
        this.southEast = new QuadTree(seBox, this.capacity);

        this.isDivided = true;
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
