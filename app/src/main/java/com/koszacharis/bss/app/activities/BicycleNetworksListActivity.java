package com.koszacharis.bss.app.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.koszacharis.bss.app.R;
import com.koszacharis.bss.app.database.NetworksData;
import com.koszacharis.bss.app.models.BicycleNetworkDetails;
import com.koszacharis.bss.app.models.BikeNetwork;
import com.koszacharis.bss.app.parsers.BicycleNetworksListParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;


public class BicycleNetworksListActivity extends AppCompatActivity {

    private static final String TAG = BicycleNetworksListActivity.class.getSimpleName();
    private static final String API_URL = "http://api.citybik.es/v2/";
    private static final String KEY_ID = "network-id";
    private static final String KEY_NAME = "network-name";
    private static final String KEY_CITY = "network-city";
    private ListView listView;
    private ArrayList<BicycleNetworkDetails> bicycleNetworks;
    private ArrayList<BikeNetwork> bikeNetworks;
    private NetworksData networksData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bicycle_networks_list);

        listView = (ListView) findViewById(R.id.networksListView);
        String apiUrl = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString(API_URL, API_URL) + "networks";
        new JSONDownloadTask().execute(apiUrl);

        Snackbar mySnackbar = Snackbar.make(findViewById(R.id.myCoordinatorLayoutNet),
                "Select a network from the list", Snackbar.LENGTH_LONG);
        mySnackbar.show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bicycle_networks_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    // https://github.com/bparmentier/OpenBikeSharing/blob/master/app/src/main/java/be/brunoparmentier/openbikesharing/app/activities/BikeNetworksList.java
    private class JSONDownloadTask extends AsyncTask<String, Void, String> {
        StringBuilder stringBuilder;
        HttpURLConnection httpURLConnection;
        URL url;
        BufferedReader bufferedReader;

        @Override
        protected String doInBackground(String... urls) {
            try {
                stringBuilder = new StringBuilder();
                url = new URL(urls[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    String strLine;
                    while ((strLine = bufferedReader.readLine()) != null) {
                        stringBuilder.append(strLine);
                    }
                    bufferedReader.close();
                }
                return stringBuilder.toString();
            } catch (IOException e) {
                Log.d(TAG, e.toString());
                return getString(R.string.connection_error);
            }
        }

        @Override
        protected void onPostExecute(final String result) {
            try {
                BicycleNetworksListParser bicycleNetworksListParser = new BicycleNetworksListParser(result);
                bicycleNetworks = bicycleNetworksListParser.getNetworks();
                Collections.sort(bicycleNetworks);

//                bikeNetworks = bicycleNetworksListParser.getBikeNetworks();
//                networksData.storeNetworks(bikeNetworks);

                BicycleNetworksListAdapter bicycleNetworksListAdapter = new BicycleNetworksListAdapter(BicycleNetworksListActivity.this,
                        android.R.layout.simple_expandable_list_item_2,
                        android.R.id.text1,
                        bicycleNetworks);

                listView.setAdapter(bicycleNetworksListAdapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        SharedPreferences sharedPreferences = PreferenceManager
                                .getDefaultSharedPreferences(BicycleNetworksListActivity.this);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(KEY_ID, bicycleNetworks.get(position).getId())
                                .putString(KEY_NAME, bicycleNetworks.get(position).getName())
                                .putString(KEY_CITY, bicycleNetworks.get(position).getCity())
                                .apply();

                        Intent intent = new Intent();
                        intent.putExtra(KEY_ID, bicycleNetworks.get(position).getId());

                        if (getParent() == null) {
                            setResult(Activity.RESULT_OK, intent);
                        } else {
                            getParent().setResult(Activity.RESULT_OK, intent);
                        }
                        finish();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                Toast.makeText(BicycleNetworksListActivity.this,
                        R.string.json_error, Toast.LENGTH_LONG).show();
            }
        }
    }

    private class BicycleNetworksListAdapter extends ArrayAdapter<BicycleNetworkDetails> {
        LayoutInflater layoutInflater;

        public BicycleNetworksListAdapter(Context context, int resource, int textViewResourceId, ArrayList<BicycleNetworkDetails> networks) {
            super(context, resource, textViewResourceId, networks);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                layoutInflater = LayoutInflater.from(getContext());
                convertView = layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            }

            BicycleNetworkDetails bicycleNetworkDetails = getItem(position);

            if (bicycleNetworkDetails != null) {
                String netListText = bicycleNetworkDetails.getCity()
                        + " (" + bicycleNetworkDetails.getCountry() + ")" + " - " + bicycleNetworkDetails.getName();
                TextView tv1 = (TextView) convertView.findViewById(android.R.id.text1);
                tv1.setText(netListText);
            }

            return convertView;
        }
    }

}
