package com.koszacharis.bss.app.models;

import java.util.ArrayList;

public class StationsHistories {

    private ArrayList<StationHistory> stations;

    public StationsHistories(ArrayList<StationHistory> stations) {
        this.stations = stations;
    }

    public ArrayList<StationHistory> getStations() {
        return stations;
    }

    public void setStations(ArrayList<StationHistory> stations) {
        this.stations = stations;
    }
}
