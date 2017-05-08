package com.koszacharis.bss.app.activities;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.koszacharis.bss.app.R;
import com.koszacharis.bss.app.database.NetworksData;
import com.koszacharis.bss.app.database.StationsData;
import com.koszacharis.bss.app.database.StationsHistoryData;
import com.koszacharis.bss.app.fragments.StationsListFragment;
import com.koszacharis.bss.app.models.BicycleNetwork;
import com.koszacharis.bss.app.models.BikeNetwork;
import com.koszacharis.bss.app.models.Station;
import com.koszacharis.bss.app.models.StationHistory;
import com.koszacharis.bss.app.models.StationsHistories;
import com.koszacharis.bss.app.parsers.BicycleNetworksListParser;
import com.koszacharis.bss.app.parsers.StationsListParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class StationsListActivity extends FragmentActivity implements ActionBar.TabListener {

    private static final String TAG = StationsListActivity.class.getSimpleName();
    private static final String API_URL = "http://api.citybik.es/v2/";
    private static final String KEY_BICYCLE_NETWORK = "bicycleNetwork";
    private static final String KEY_STATIONS = "stations";
    private static final String KEY_NETWORK_ID = "network-id";
    private BicycleNetwork bicycleNetwork;
    private ArrayList<Station> stations;
    private StationsData stationsData;
    private JSONDownloadTask jsonDownloadTask;
    private ViewPager viewPager;
    private TabsPagerAdapter tabsPagerAdapter;
    private StationsListFragment stationsListFragment;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<StationHistory> stationHistory = new ArrayList<>();
    private StationsHistoryData stationsHistoryData;
    private ArrayList<BikeNetwork> bikeNetworks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stations_list);

        stationsData = new StationsData(this);
        stations = stationsData.getStations();

        stationsHistoryData = new StationsHistoryData(this);
        stationHistory = stationsHistoryData.getStationsHistory();

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                downloadTask();
            }
        });

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabsPagerAdapter);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        ActionBar actionBar = getActionBar();
        ActionBar.Tab tab = actionBar.newTab();
        tab.setTabListener(this);
        actionBar.addTab(tab, true);


        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @SuppressWarnings("unchecked")
                    public void run() {
                        try {
                            downloadTask();
                        } catch (Exception e) {
                            Log.d(TAG, e.toString());
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 900000);

        downloadTask();

        final Handler handler1 = new Handler();
        Timer timer1 = new Timer();
        TimerTask doAsyncTask = new TimerTask() {
            @Override
            public void run() {
                handler1.post(new Runnable() {
                    @SuppressWarnings("unchecked")
                    public void run() {
                        try {
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm");
                            String dateTime = simpleDateFormat.format(new Date());
                            if ((Integer.parseInt(dateTime) % 15 == 0) || (Integer.parseInt(dateTime) == 0)) {
                                downloadTask();
                                Toast.makeText(getApplicationContext(), "Saved at: " + String.valueOf(dateTime), Toast.LENGTH_LONG).show();
                                Log.i(TAG, "Saved at " + dateTime);

                            }
                        } catch (Exception e) {
                            Log.d(TAG, e.toString());
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        timer1.schedule(doAsyncTask, 0, 60000);

        //https://github.com/bparmentier/OpenBikeSharing/blob/master/app/src/main/java/be/brunoparmentier/openbikesharing/app/activities/StationList.java
        boolean firstTime = sharedPreferences.getString(KEY_NETWORK_ID, "").isEmpty();

        if (!firstTime) {
            if (savedInstanceState != null) {
                bicycleNetwork = (BicycleNetwork) savedInstanceState.getSerializable(KEY_BICYCLE_NETWORK);
                stations = (ArrayList<Station>) savedInstanceState.getSerializable(KEY_STATIONS);
            } else {
                String networkId = sharedPreferences.getString(KEY_NETWORK_ID, "");
                String stationUrl = API_URL + "networks/" + networkId;
                jsonDownloadTask = new JSONDownloadTask();
                jsonDownloadTask.execute(stationUrl);
            }
        } else {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage(R.string.welcome_dialog_message).setTitle(R.string.welcome_dialog_title);
            ;
            alertDialogBuilder.setPositiveButton(R.string.welcome_dialog_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(StationsListActivity.this, BicycleNetworksListActivity.class);
                    startActivityForResult(intent, 1);
                }
            });

            String networkId = sharedPreferences.getString(KEY_NETWORK_ID, "");
            String url = API_URL + "networks/" + networkId;
            jsonDownloadTask = new JSONDownloadTask();
            jsonDownloadTask.execute(url);
            alertDialogBuilder.setNegativeButton(R.string.welcome_dialog_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (jsonDownloadTask.getStatus() == AsyncTask.Status.FINISHED) {

            stations = stationsData.getStations();
            tabsPagerAdapter.updateAllStationsListFragment(stations);
            downloadTask();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_BICYCLE_NETWORK, bicycleNetwork);
        outState.putSerializable(KEY_STATIONS, stations);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.stations_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.action_refresh:
                downloadTask();
                return true;
            case R.id.action_map:
                Intent mapIntent = new Intent(this, MapActivity.class);
                startActivity(mapIntent);
                return true;
            case R.id.action_probabilities:
                Intent intent = new Intent(this, JourneyForecastActivity.class);
                startActivity(intent);
                bicycleNetwork = (BicycleNetwork) getIntent().getSerializableExtra(KEY_BICYCLE_NETWORK);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    private void downloadTask() {
        String networkId = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString(KEY_NETWORK_ID, "");
        String url = API_URL + "networks/" + networkId;
        jsonDownloadTask = new JSONDownloadTask();
        jsonDownloadTask.execute(url);
    }

    private class JSONDownloadTask extends AsyncTask<String, Void, String> {
        Exception error;
        StationsListParser stationsListParser;
        BicycleNetworksListParser bicycleNetworksListParser;
        NetworksData networksData;
        StringBuilder stringBuilder;
        HttpURLConnection httpURLConnection;
        URL url;
        BufferedReader bufferedReader;


        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... urls) {
            if (urls[0].isEmpty()) {
                finish();
            }
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
                Log.d(TAG, e.getMessage());
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(final String result) {
            if (error != null) {
                Toast.makeText(getApplicationContext(),
                        getApplicationContext().getResources().getString(R.string.connection_error),
                        Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            } else {
                try {

                    stationsListParser = new StationsListParser(result);
                    bicycleNetwork = stationsListParser.getNetwork();

//                    bikeNetworks = bicycleNetworksListParser.getBikeNetworks();
//                    networksData.storeNetworks(bikeNetworks);

                    stations = bicycleNetwork.getStations();
                    Collections.sort(stations);
                    stationsData.storeStations(stations);

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm");
                    String dateTime = simpleDateFormat.format(new Date());
                    if (Integer.parseInt(dateTime) % 15 == 0 || Integer.parseInt(dateTime) == 0) {
                        StationsHistories sh = stationsListParser.getStationHistory();
                        stationHistory = sh.getStations();
                        stationsHistoryData.storeStationsHistory(stationHistory);
                        Toast.makeText(getApplicationContext(), "saved: " + dateTime, Toast.LENGTH_LONG);
                        Log.i(TAG, "saved: " + dateTime);

                    }

                    tabsPagerAdapter.updateAllStationsListFragment(stations);
                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                    Toast.makeText(StationsListActivity.this,
                            R.string.json_error, Toast.LENGTH_LONG).show();
                } finally {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        }
    }


    private class TabsPagerAdapter extends FragmentPagerAdapter {
        private static final int NUM_ITEMS = 1;

        public TabsPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
            stationsListFragment = StationsListFragment.stationsListFragment(stations
            );
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return stationsListFragment;
                default:
                    return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.all_stations);
                default:
                    return null;
            }
        }

        public void updateAllStationsListFragment(ArrayList<Station> stations) {
            stationsListFragment.updateStationsList(stations);
        }

    }


}

