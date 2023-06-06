package com.tolgaozkaya.androdeprem;

public class Earthquake {
    private String title;
    private String date;
    private double magnitude;
    private int depth;
    private double latitude;
    private double longitude;

    public Earthquake(String title, String date, double magnitude, int depth, double latitude, double longitude) {
        this.title = title;
        this.date = date;
        this.magnitude = magnitude;
        this.depth = depth;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public double getMagnitude() {
        return magnitude;
    }

    public int getDepth() {
        return depth;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
