package com.koszacharis.bss.app.models;

import java.io.Serializable;

/**
 * Information on a bike network.
 */
public class BicycleNetworkDetails implements Serializable, Comparable<BicycleNetworkDetails> {
    private String id;
    private String name;
    private String company;
    private double latitude;
    private double longitude;
    private String city;
    private String country;

    public BicycleNetworkDetails(String id, String name, String company, double latitude, double longitude, String city, String country) {
        this.id = id;
        this.name = name;
        this.company = company;
        this.latitude = latitude;
        this.longitude = longitude;
        this.city = city;
        this.country = country;
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
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

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }


    @Override
    public int compareTo(BicycleNetworkDetails another) {
        return city.compareToIgnoreCase(another.city) > 0 ? 1 :
                (city.compareToIgnoreCase(another.city) < 0 ? -1 : 0);
    }
}
