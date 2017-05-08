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

public class StationActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnInfoWindowClickListener {

    private static final String TAG = StationActivity.class.getSimpleName();
    private static final String DIRECTIONS_API_URL = "https://maps.googleapis.com/maps/api/directions/";
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final String KEY_STATION = "station";
    private final List<Polyline> polylinePaths = new ArrayList<>();
    private Station station;
    private Marker lastMarker;
    private LatLng currentLoc;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private boolean bPermissionGranted = false;
    private StationsData stationsData;
    private MenuItem lastItem;
    private ArrayList<Station> stations;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station);

        stationsData = new StationsData(this);
        station = (Station) getIntent().getSerializableExtra(KEY_STATION);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.stationMap);
        mapFragment.getMapAsync(this);

        requestLocationAccess();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bPermissionGranted = checkLocationPermission();
        }

        TextView stationName = (TextView) findViewById(R.id.stationName);
        TextView stationEmptySlots = (TextView) findViewById(R.id.stationEmptySlots);
        TextView stationFreeBikes = (TextView) findViewById(R.id.stationFreeBikes);

        if (station.getName() != null) {
            stationName.setText(station.getName());
            stationFreeBikes.setText(String.valueOf(station.getFreeBikes()));
        } else {
            stationName.setText(R.string.notFound);
            stationFreeBikes.setText(R.string.notFound);
        }

        if (station.getEmptySlots() < 0) {
            stationEmptySlots.setText("0");
        } else if (station.getFreeBikes() < 0) {
            stationFreeBikes.setText("0");
        } else {
            stationEmptySlots.setText(String.valueOf(station.getEmptySlots()));
        }

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append("Station ").append(station.getName()).append(" selected").append(" ");
        builder.setSpan(new ImageSpan(StationActivity.this, R.drawable.ic_done), builder.length() - 1, builder.length(), 0);
        Snackbar.make(findViewById(R.id.myCoordinatorLayoutStation), builder, Snackbar.LENGTH_SHORT).show();
    }

    private void requestLocationAccess() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
//        || !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

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
    public void onLocationChanged(Location location) {
        currentLoc = new LatLng(location.getLatitude(), location.getLongitude());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.station, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
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
                    Toast.makeText(this, "Click the station marker", Toast.LENGTH_SHORT);
                    SpannableStringBuilder builder = new SpannableStringBuilder();
                    builder.append("Station ").append(station.getName()).append(" ");
                    builder.setSpan(new ImageSpan(StationActivity.this, R.drawable.ic_done), builder.length() - 1, builder.length(), 0);
                    Snackbar.make(findViewById(R.id.myCoordinatorLayoutStation), builder, Snackbar.LENGTH_SHORT).show();

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
            case R.id.action_map:
                Intent mapIntent = new Intent(this, MapActivity.class);
                startActivity(mapIntent);
                finish();
                return true;
            case R.id.action_probabilities:
                Intent intent = new Intent(this, JourneyForecastActivity.class);
                startActivity(intent);
                station = (Station) getIntent().getSerializableExtra(KEY_STATION);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (bPermissionGranted) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        stationsData = new StationsData(this);
        station = (Station) getIntent().getSerializableExtra(KEY_STATION);
        LatLng stationLoc = new LatLng(station.getLatitude(), station.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(stationLoc, 12));

        int emptySlots = station.getEmptySlots();
        int freeBikes = station.getFreeBikes();
        MarkerOptions mOptions = new MarkerOptions();

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

        mOptions.position(stationLoc).title(station.getName());
        mMap.addMarker(mOptions);


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 14));
                Toast.makeText(getBaseContext(), "Station selected", Toast.LENGTH_SHORT);

                SpannableStringBuilder builder = new SpannableStringBuilder();
                builder.append("Station ").append(marker.getTitle()).append(" ");
                builder.setSpan(new ImageSpan(StationActivity.this, R.drawable.ic_done), builder.length() - 1, builder.length(), 0);
                Snackbar.make(findViewById(R.id.myCoordinatorLayoutStation), builder, Snackbar.LENGTH_SHORT).show();
                lastMarker = marker;
                return true;
            }
        });
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationRequest mLocationRequest;
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT);
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
                } else {
                    // permission denied
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }

            }

        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Connection suspended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 14));
        Toast.makeText(this, "Info window clicked",
                Toast.LENGTH_SHORT);
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

    private String openConnection(String s) throws IOException {
        String data = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        BufferedReader bufferedReader;
        StringBuilder stringBuilder;
        try {
            URL url = new URL(s);

            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            inputStream = httpURLConnection.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            stringBuilder = new StringBuilder();

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            data = stringBuilder.toString();
            bufferedReader.close();
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            inputStream.close();
            httpURLConnection.disconnect();
        }
        return data;
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            lastItem.setEnabled(false);
        }

        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                data = openConnection(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            JsonParser jsonParser = new JsonParser();
            jsonParser.execute(result);
            lastItem.setEnabled(true);

        }
    }

    /* http://wptrafficanalyzer.in/blog/drawing-driving-route-directions-between-two-locations-using-google-directions-in-google-map-android-api-v2/ */

    private class JsonParser extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... data) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes;

            try {
                jObject = new JSONObject(data[0]);
                DirectionsParser parser = new DirectionsParser();

                routes = parser.parse(jObject);
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
            PolylineOptions lineOptions = null;
            String distance = "";
            String duration = "";

            if (result.size() < 1) {
                Toast.makeText(getBaseContext(), "No bicycle paths available", Toast.LENGTH_SHORT).show();
                return;
            }

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    if (j == 0) {
                        distance = point.get("distance");
                        continue;
                    } else if (j == 1) {
                        duration = point.get("duration");
                        continue;
                    }

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }
                TextView dur = (TextView) findViewById(R.id.tvDuration);
                dur.setTextColor(Color.BLACK);
                dur.setText(duration);
                TextView dis = (TextView) findViewById(R.id.tvDistance);
                dis.setText(distance);

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(7);
                lineOptions.color(Color.BLUE);
            }

            // Drawing polyline in the Google Map for the i-th route
            polylinePaths.add(mMap.addPolyline(lineOptions));
        }

    }
}

