package com.example.workshop;

public class ScheduledRide {

    private int rideId;
    private String startLocation;
    private String endLocation;
    private double price;
    private double driverRating;
    private String username;
    private String start_time;


    // Default constructor
    public ScheduledRide() {}

    // Constructor with parameters
    public ScheduledRide(int rideId,  String startLocation, String endLocation, double price, double driverRating, String username,String starttime) {
        this.rideId = rideId;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.price = price;
        this.driverRating = driverRating;
        this.username=username;
        this.start_time=starttime;
    }
    public String getUsername(){
        return username;
    }
    public void setUsername(String username){
        this.username=username;
    }

    // Гетери и сетери
    public int getRideId() {
        return rideId;
    }


    public String getStartLocation() {
        return startLocation;
    }

    public String getEndLocation() {
        return endLocation;
    }

    public double getPrice() {
        return price;
    }

    public double getDriverRating() {
        return driverRating;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public void setEndLocation(String endLocation) {
        this.endLocation = endLocation;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setDriverRating(double driverRating) {
        this.driverRating = driverRating;
    }
    public void setStartTime(String StartTime){
        this.start_time=StartTime;
    }
    public String getStartTime(){
        return this.start_time;
    }
}

