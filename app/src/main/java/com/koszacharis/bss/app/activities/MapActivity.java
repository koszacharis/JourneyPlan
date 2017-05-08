package com.koszacharis.bss.app.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.koszacharis.bss.app.R;
import com.koszacharis.bss.app.database.StationsData;
import com.koszacharis.bss.app.models.Station;
import com.koszacharis.bss.app.models.Status;
import com.koszacharis.bss.app.parsers.DirectionsParser;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, LocationListener, GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnInfoWindowLongClickListener {

    private static final String TAG = MapActivity.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final String DIRECTIONS_API_URL = "https://maps.googleapis.com/maps/api/directions/";
    private final List<Polyline> polylinePaths = new ArrayList<>();
    private LatLng currentLoc;
    private boolean locationPermissionGranted = false;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private ArrayList<Station> stations;
    private Marker lastMarker;
    private MenuItem lastItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        requestLocationAccess();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            locationPermissionGranted = checkLocationPermission();
        }

        StationsData stationsData = new StationsData(this);
        stations = stationsData.getStations();
        Snackbar mySnackbar;
        mySnackbar = Snackbar.make(findViewById(R.id.myCoordinatorLayoutMap),
                "Stations represented in markers", Snackbar.LENGTH_SHORT);
        mySnackbar.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this).cancel();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
//        mMap.setTrafficEnabled(true);
//        mMap.setIndoorEnabled(true);
//        mMap.setBuildingsEnabled(true);
//        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (locationPermissionGranted) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();

                SpannableStringBuilder builder = new SpannableStringBuilder();
                builder.append("Station ").append(marker.getTitle()).append(" selected").append(" ");
                builder.setSpan(new ImageSpan(MapActivity.this, R.drawable.ic_done), builder.length() - 1, builder.length(), 0);
                Snackbar.make(findViewById(R.id.myCoordinatorLayoutMap), builder, Snackbar.LENGTH_LONG).show();

//                Snackbar mySnackbar;
//                mySnackbar = Snackbar.make(findViewById(R.id.myCoordinatorLayoutMap), marker.getTitle() + " station selected", Snackbar.LENGTH_LONG);
//                mySnackbar.show();


                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 14));
                Toast.makeText(getApplicationContext(), "Station selected", Toast.LENGTH_SHORT);
                lastMarker = marker;
                Log.i(TAG, String.valueOf(marker.getPosition()));

                return true;
            }
        });

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Disconnected!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationRequest mLocationRequest;
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT);
        Log.i(TAG, "Connected");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Connection suspended!", Toast.LENGTH_SHORT).show();
    }


    private void requestLocationAccess() {
        // Get Location Manager and check for GPS & Network location services
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
//        || !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Build the alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Location Services are not Active");
            builder.setMessage("Please enable Location Services and GPS");
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Show location settings when the user acknowledges the alert dialog
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            builder.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Show location settings when the user acknowledges the alert dialog
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            Dialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    buildGoogleApiClient();
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mMap.setMyLocationEnabled(true);
                    Log.i(TAG, "Permission granted");
                } else {
                    // permission denied
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
        Log.i(TAG, "Google API created");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map, menu);
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLoc = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions mOptions = new MarkerOptions();
        LatLng latLng;
        for (final Station station : stations) {
            int emptySlots = station.getEmptySlots();
            int freeBikes = station.getFreeBikes();
            Log.i(TAG, freeBikes + " " + emptySlots);
            latLng = new LatLng(station.getLatitude(), station.getLongitude());

            Log.i(TAG, String.valueOf(latLng));

            if ((emptySlots == 0 && freeBikes == 0) || station.getStatus() == Status.CLOSED) {
                mOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            } else {

                double bikes = (double) freeBikes;
                double slots = (double) emptySlots;
                Log.i(TAG, bikes + " " + slots);

                double availabilityRatio = bikes / (bikes + slots);
                Log.i(TAG, String.valueOf(availabilityRatio));

                if (freeBikes == 0 || emptySlots <= 0) {
                    mOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                } else if (freeBikes > 0 && emptySlots > 0 && availabilityRatio <= 0.3) {
                    mOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                } else if (freeBikes > 0 && emptySlots > 0 && availabilityRatio > 0.3 && availabilityRatio < 0.7) {
                    mOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                } else if (freeBikes > 0 && emptySlots > 0 && availabilityRatio >= 0.7) {
                    mOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                }
            }
            mOptions.position(latLng)
                    .snippet("Free Bikes: " + station.getFreeBikes() + " Empty Slots: " + station.getEmptySlots())
                    .title(station.getName());
            mMap.addMarker(mOptions);
        }
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnInfoWindowLongClickListener(this);
        mMap.getUiSettings().setMapToolbarEnabled(true);

        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                finish();
                return true;
            case R.id.action_navigate:
                lastItem = item;
                if (polylinePaths != null) {
                    for (Polyline polyline : polylinePaths) {
                        polyline.remove();
                    }
                    Log.i(TAG, "Cleared previous polyline");
//                    polylinePaths.clear();
                }

                if (lastMarker == null) {
                    Toast.makeText(this, "Click a station marker", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Navigating", Toast.LENGTH_SHORT).show();
                    String url = getDirectionsUrl(currentLoc, lastMarker.getPosition());

                    DownloadTask downloadTask = new DownloadTask();
                    downloadTask.execute(url);

                    Log.i(TAG, "Download completed");
                }
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onInfoWindowClick(Marker marker) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 14));

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append("Station ").append(marker.getTitle()).append(" selected").append(" ");
        builder.setSpan(new ImageSpan(MapActivity.this, R.drawable.ic_done), builder.length() - 1, builder.length(), 0);
        Snackbar.make(findViewById(R.id.myCoordinatorLayoutMap), builder, Snackbar.LENGTH_LONG).show();

        Toast.makeText(this, "Info window clicked",
                Toast.LENGTH_SHORT);
        Log.i(TAG, "Info window clicked");

    }

    @Override
    public void onInfoWindowLongClick(Marker marker) {
        Intent intent = new Intent(this, JourneyForecastActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, StationsListActivity.class));
        finish();
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String sensor = "sensor=false";
        String mode = "mode=bicycling";

        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;

        String output = "json";

        return DIRECTIONS_API_URL + output + "?" + parameters;
    }

    private String downloadUrl(String s) throws IOException {
        String data = "";
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;
        String line;
        URL url;
        BufferedReader bufferedReader;

        try {
            url = new URL(s);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            inputStream = urlConnection.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();


            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            bufferedReader.close();
        } catch (Exception e) {
            Log.d(TAG, e.toString());
            e.printStackTrace();
        } finally {
            inputStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    /* http://wptrafficanalyzer.in/blog/drawing-driving-route-directions-between-two-locations-using-google-directions-in-google-map-android-api-v2/ */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        DirectionsParser directionsParser;

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jsonObject;
            List<List<HashMap<String, String>>> routes;

            try {
                jsonObject = new JSONObject(jsonData[0]);
                directionsParser = new DirectionsParser();
                routes = directionsParser.parse(jsonObject);
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions polylineOptions = null;
            String distance = "";
            String duration = "";

            if (result.size() < 1) {
                Toast.makeText(getApplicationContext(), "No bicycling path available", Toast.LENGTH_SHORT).show();
                return;
            }

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                polylineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    if (j == 0) {    // Get distance from the list
                        distance = point.get("distance");
                        continue;
                    } else if (j == 1) { // Get duration from the list
                        duration = point.get("duration");
                        continue;
                    }

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }
                ((TextView) findViewById(R.id.tvDuration)).setText(duration);
                ((TextView) findViewById(R.id.tvDistance)).setText(distance);

                polylineOptions.addAll(points);
                polylineOptions.width(7);
                polylineOptions.color(Color.BLUE);
            }

            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }


    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            lastItem.setEnabled(false);
        }

        @Override
        protected String doInBackground(String... url) {

            String data;

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, e.toString());
                return e.getMessage();
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
            lastItem.setEnabled(true);
        }
    }

}
