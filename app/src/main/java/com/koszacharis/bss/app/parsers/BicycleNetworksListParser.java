package com.koszacharis.bss.app.parsers;

import android.util.Log;

import com.koszacharis.bss.app.models.BicycleNetworkDetails;
import com.koszacharis.bss.app.models.BikeNetwork;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class BicycleNetworksListParser {
    private final ArrayList<BicycleNetworkDetails> bicycleNetworks;
    private final String TAG = BicycleNetworksListParser.class.getSimpleName();
    private ArrayList<BikeNetwork> bikeNetworks;
    private BikeNetwork bikeNetwork;

    public BicycleNetworksListParser(String url) {
        bicycleNetworks = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(url);
            JSONArray rawNetworks = jsonObject.getJSONArray("networks");

            for (int i = 0; i < rawNetworks.length(); i++) {
                JSONObject rawNetwork = rawNetworks.getJSONObject(i);

                String id = rawNetwork.optString("id");
                String name = rawNetwork.optString("name");
                String company = rawNetwork.optString("company");

                JSONObject rawLocation = rawNetwork.getJSONObject("location");

                double latitude = rawLocation.optDouble("latitude");
                double longitude = rawLocation.optDouble("longitude");
                String city = rawLocation.optString("city");
                String country = rawLocation.optString("country");

                bicycleNetworks.add(new BicycleNetworkDetails(id, name, company, latitude, longitude, city, country));
//                bikeNetwork = new BikeNetwork(id, name, latitude, longitude);
//                bikeNetworks.add(bikeNetwork);
            }
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
//            throw new RuntimeException(e);
        }
    }

    public ArrayList<BicycleNetworkDetails> getNetworks() {
        return bicycleNetworks;
    }

    public ArrayList<BikeNetwork> getBikeNetworks() {
        return bikeNetworks;
    }

}
