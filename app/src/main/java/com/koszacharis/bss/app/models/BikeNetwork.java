package com.koszacharis.bss.app.models;


public class BikeNetwork implements Comparable {

    private String id;
    private String name;
    private double latitude;
    private double longitude;

    public BikeNetwork(String id, String name, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }


    public int compareTo(BikeNetwork another) {
        return name.compareToIgnoreCase(another.getName()) > 0 ? 1 :
                (name.compareToIgnoreCase(another.getName()) < 0 ? -1 : 0);
    }

    @Override
    public int compareTo(Object another) {
        return 0;
    }
}
