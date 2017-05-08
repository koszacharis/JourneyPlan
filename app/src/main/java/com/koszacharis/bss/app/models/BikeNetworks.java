package com.koszacharis.bss.app.models;

import java.util.ArrayList;

/**
 * Created by koszacharis on 19/08/2016.
 */
public class BikeNetworks extends BikeNetwork {

    private ArrayList<BikeNetwork> bikeNetworks;

    public BikeNetworks(String id, String name, double latitude, double longitude) {
        super(id, name, latitude, longitude);
    }

    public ArrayList<BikeNetwork> getBikeNetworks() {
        return bikeNetworks;
    }

    public void setBikeNetworks(ArrayList<BikeNetwork> bikeNetworks) {
        this.bikeNetworks = bikeNetworks;
    }
}
