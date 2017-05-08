package com.koszacharis.bss.app.models;

import java.io.Serializable;

public class Station implements Serializable, Comparable<Station>, Stations {
    private String id;
    private String name;
    private double latitude;
    private double longitude;
    private int freeBikes;
    private int emptySlots;
    private Status status;

    public Station(String id, String name, double latitude, double longitude, int freeBikes, int emptySlots) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.freeBikes = freeBikes;
        this.emptySlots = emptySlots;
        this.status = null;
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

    public int getFreeBikes() {
        return freeBikes;
    }

    public void setFreeBikes(int freeBikes) {
        this.freeBikes = freeBikes;
    }

    public int getEmptySlots() {
        return emptySlots;
    }

    public void setEmptySlots(int emptySlots) {
        this.emptySlots = emptySlots;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public int compareTo(Station another) {
        return name.compareToIgnoreCase(another.getName()) > 0 ? 1 :
                (name.compareToIgnoreCase(another.getName()) < 0 ? -1 : 0);
    }
}
