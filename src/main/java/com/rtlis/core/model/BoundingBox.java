package com.rtlis.core.model;

public class BoundingBox {
    private final double centerX;
    private final double centerY;
    private final double halfWidth;
    private final double halfHeight;

    public BoundingBox(double centerX, double centerY, double halfWidth, double halfHeight) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.halfWidth = halfWidth;
        this.halfHeight = halfHeight;
    }

    public double getCenterX() {
        return centerX;
    }

    public double getCenterY() {
        return centerY;
    }

    public double getHalfWidth() {
        return halfWidth;
    }

    public double getHalfHeight() {
        return halfHeight;
    }

    public boolean contains(Point p) {
        //-- x-coordinate -> p.getlongitude
        //-- y-coordinate -> p.getLatitude
        return p.getLongitude() >= (centerX - halfWidth) && //-- point is not too far left
                p.getLongitude() <= (centerX + halfWidth) && //--point is not too far right
                p.getLatitude() >= (centerY - halfHeight) && //-- point is not too far down
                p.getLatitude() <= (centerY + halfHeight); //--- point is not too far up
    }

    /**
     * Determines whether this bounding box intersects/overlaps with the specified bounding box.
     *
     * @param range the other bounding box to check for intersection
     * @return {@code true} if the bounding boxes intersect; {@code false} otherwise
     */
    public boolean intersects(BoundingBox range) {
        double thisLeft = this.centerX - this.halfWidth;
        double thisRight = this.centerX + this.halfWidth;
        double thisTop = this.centerY + this.halfHeight;
        double thisBottom = this.centerY - this.halfHeight;

        double rangeLeft = range.centerX - range.halfWidth;
        double rangeRight = range.centerX + range.halfWidth;
        double rangeTop = range.centerY + range.halfHeight;
        double rangeBottom = range.centerY - range.halfHeight;


        return !(rangeLeft > thisRight ||
                rangeRight < thisLeft ||
                rangeTop < thisBottom ||
                rangeBottom > thisTop
        );
    }
}
