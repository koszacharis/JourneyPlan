package com.koszacharis.bss.app.models;

public class StationHistory implements StationHistoric {

    private String id;
    private String name;
    private int freeBikes;
    private String interval;
    private int emptySlots;

    public StationHistory(String id, String name, String interval, int freeBikes, int emptySlots) {
        this.id = id;
        this.name = name;
        this.interval = interval;
        this.freeBikes = freeBikes;
        this.emptySlots = emptySlots;
    }

    public int getEmptySlots() {
        return emptySlots;
    }

    public void setEmptySlots(int emptySlots) {
        this.emptySlots = emptySlots;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getFreeBikes() {
        return freeBikes;
    }

    public void setFreeBikes(int freeBikes) {
        this.freeBikes = freeBikes;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
