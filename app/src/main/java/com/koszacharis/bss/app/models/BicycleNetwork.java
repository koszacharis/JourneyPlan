package com.koszacharis.bss.app.models;

import java.util.ArrayList;

public class BicycleNetwork extends BicycleNetworkDetails {
    private ArrayList<Station> stations;

    public BicycleNetwork(String id, String name, String company, double latitude,
                          double longitude, String city, String country, ArrayList<Station> stations) {
        super(id, name, company, latitude, longitude, city, country);
        this.stations = stations;
    }

    public ArrayList<Station> getStations() {
        return stations;
    }

    public void setStations(ArrayList<Station> stations) {
        this.stations = stations;
    }
}
