package core.model;

public class Point {
    private final double longitude;
    private final double latitude;
    private final String vehicleId;

    public Point(double longitude, double latitude, String vehicleId){
        this.longitude = longitude;
        this.latitude = latitude;
        this.vehicleId = vehicleId;
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
}
