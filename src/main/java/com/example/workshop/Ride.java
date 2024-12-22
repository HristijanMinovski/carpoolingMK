package com.example.workshop;

public class Ride {
    private int rideId;
    private int driverId;
    private String driverUsername;

    public Ride(int rideId, int driverId, String driverUsername) {
        this.rideId = rideId;
        this.driverId = driverId;
        this.driverUsername = driverUsername;
    }

    public int getRideId() { return rideId; }
    public int getDriverId() { return driverId; }
    public String getDriverUsername() { return driverUsername; }
}
