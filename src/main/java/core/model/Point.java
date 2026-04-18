package core.model;

public class Point {
    private final double longitude;
    private final double latitude;
    private final String vehicleId;
    private final long timestamp;

    public Point(double longitude, double latitude, String vehicleId, long timestamp) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.vehicleId = vehicleId;
        this.timestamp = timestamp;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
