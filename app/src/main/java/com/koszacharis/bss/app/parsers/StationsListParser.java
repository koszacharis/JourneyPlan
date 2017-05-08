package com.koszacharis.bss.app.parsers;

import android.util.Log;

import com.koszacharis.bss.app.models.BicycleNetwork;
import com.koszacharis.bss.app.models.Station;
import com.koszacharis.bss.app.models.StationHistory;
import com.koszacharis.bss.app.models.StationsHistories;
import com.koszacharis.bss.app.models.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class StationsListParser {
    private final static String TAG = StationsListParser.class.getSimpleName();
    private StationsHistories stationsHistories;
    private BicycleNetwork bicycleNetwork;

    public StationsListParser(String url) {
        ArrayList<Station> stations = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(url);
            JSONObject rawNetwork = jsonObject.getJSONObject("network");

            String networkId = rawNetwork.optString("id");
            String networkName = rawNetwork.optString("name");
            String networkCompany = rawNetwork.optString("company");

            JSONObject rawLocation = rawNetwork.getJSONObject("location");

            double lat = rawLocation.optDouble("latitude");
            double lng = rawLocation.optDouble("longitude");
            String city = rawLocation.optString("city");
            String country = rawLocation.optString("country");

            ArrayList<StationHistory> sh = new ArrayList<>();
            {
                JSONArray rawStations = rawNetwork.getJSONArray("stations");

                for (int i = 0; i < rawStations.length(); i++) {
                    JSONObject rawStation = rawStations.getJSONObject(i);

                    String id = rawStation.optString("id");
                    String name = rawStation.optString("name");
                    String timestamp = rawStation.optString("timestamp");
                    double latitude = rawStation.optDouble("latitude");
                    double longitude = rawStation.optDouble("longitude");
                    int freeBikes = rawStation.optInt("free_bikes");
                    int emptySlots;
                    if (!rawStation.isNull("empty_slots")) {
                        emptySlots = rawStation.optInt("empty_slots");
                    } else {
                        emptySlots = -1;
                    }

                    Station station = new Station(id, name, latitude, longitude,
                            freeBikes, emptySlots);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH-mm");
                    String dateTime = simpleDateFormat.format(new Date());

                    dateTime = parseTime(dateTime);
                    Log.i("Time", dateTime);
                    StationHistory stationHistory = new StationHistory(id, name, dateTime, freeBikes, emptySlots);


                    if (rawStation.has("extra")) {
                        JSONObject rawExtra = rawStation.getJSONObject("extra");

                        if (rawExtra.has("status")) {
                            String status = rawExtra.optString("status");
                            if (status.equals("CLOSED") || status.equals("offline")) {
                                station.setStatus(Status.CLOSED);
                            } else {
                                station.setStatus(Status.OPEN);
                            }
                        }
                    }
                    stations.add(station);
                    sh.add(stationHistory);
                }
            }
            stationsHistories = new StationsHistories(sh);
            bicycleNetwork = new BicycleNetwork(networkId, networkName, networkCompany, lat, lng, city, country, stations);
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
//            throw new RuntimeException(e);
        }
    }

    public StationsHistories getStationsHistories() {
        return stationsHistories;
    }

    public void setStationsHistories(StationsHistories stationsHistories) {
        this.stationsHistories = stationsHistories;
    }

    public BicycleNetwork getBicycleNetwork() {
        return bicycleNetwork;
    }

    public void setBicycleNetwork(BicycleNetwork bicycleNetwork) {
        this.bicycleNetwork = bicycleNetwork;
    }

    public BicycleNetwork getNetwork() {
        return bicycleNetwork;
    }

    public StationsHistories getStationHistory() {
        return stationsHistories;
    }

    private String parseTime(String s) {
        String hour;
        String time;
        String min;

        String[] line = s.split("-");
        hour = line[0];
        min = line[1];

        if (hour.length() == 1) {
            hour = "0" + hour;
        }

        if (min.length() == 1) {
            min = "0" + min;
        }

        time = hour + min;
        return time;
    }
}
