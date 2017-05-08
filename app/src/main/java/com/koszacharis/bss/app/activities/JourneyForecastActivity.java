package com.koszacharis.bss.app.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.koszacharis.bss.app.R;
import com.koszacharis.bss.app.database.StationsData;
import com.koszacharis.bss.app.database.StationsHistoryData;
import com.koszacharis.bss.app.models.Station;
import com.koszacharis.bss.app.models.StationHistory;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class JourneyForecastActivity extends AppCompatActivity {

    private static final int bikesThreshold = 3;
    private static final int slotsThreshold = 3;
    private static final double rateThreshold = 0.8;
    private static final String TAG = JourneyForecastActivity.class.getSimpleName();
    private final HashMap<String, Integer> destSlots = new HashMap<>();
    private final HashMap<String, Integer> originBikes = new HashMap<>();
    private String destName;
    private String originName;
    private ArrayList<StationHistory> stationHistory;
    private StationsHistoryData stationsHistoryData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey_forecast);
        Toast.makeText(this, "Please type the origin and destination stations", Toast.LENGTH_SHORT);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calculate(view);
            }
        });

        StationsData stationsData = new StationsData(this);

        ArrayList<Station> stations = stationsData.getStations();

        stationsHistoryData = new StationsHistoryData(this);
        stationHistory = stationsHistoryData.getStationsHistory();

        for (StationHistory sh : stationHistory) {
            Log.i(TAG, sh.getName() + " " + sh.getInterval());
        }

        String[] stationArr = new String[stations.size()];
        int i = 0;
        for (Station station : stations) {
            stationArr[i] = station.getName();
            i++;
            destSlots.put(station.getName(), station.getEmptySlots());
            originBikes.put(station.getName(), station.getFreeBikes());
        }
        Log.i(TAG, String.valueOf(stationArr.length));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, stationArr);
        AutoCompleteTextView textViewOrigin = (AutoCompleteTextView)
                findViewById(R.id.origin);
        AutoCompleteTextView textViewDestination = (AutoCompleteTextView)
                findViewById(R.id.destination);

        textViewOrigin.setThreshold(1); // will start working from first character
        textViewDestination.setThreshold(1); // will start working from first character

        // set text color
        textViewDestination.setTextColor(Color.parseColor("#009688"));
        textViewOrigin.setTextColor(Color.parseColor("#009688"));

        textViewOrigin.setAdapter(adapter);
        textViewDestination.setAdapter(adapter);

        textViewOrigin.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                originName = (String) parent.getItemAtPosition(position);
            }
        });

        textViewDestination.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                destName = (String) parent.getItemAtPosition(position);
            }
        });


        Snackbar mySnackbar;
        mySnackbar = Snackbar.make(findViewById(R.id.myCoordinatorLayoutPred),
                "Please type the origin and destination stations", Snackbar.LENGTH_SHORT);
        mySnackbar.show();
    }

    private String calculateLVP(String s1, String s2) {
        TextView lvp = (TextView) findViewById(R.id.probB);

        if (s1 != null && s2 != null) {
            if (originBikes.get(s1) > bikesThreshold && destSlots.get(s2) > slotsThreshold) {
                lvp.setText("100%");
                return "GO";
            } else {
                lvp.setText("0%");
                return "NO GO";
            }
        } else {
            return "";
        }
    }

    /* method to parse time in the right format */
    private String parseTime(String s) {
        String hour;
        String time;
        String min;
        int m;

        String[] line = s.split("-");
        hour = line[0];
        min = line[1];

        if (hour.length() == 1) {
            hour = "0" + hour;
        }

        if (min.length() == 1) {
            min = "0" + min;
        }

        m = Integer.valueOf(min);
        if (m <= 15) {
            min = "15";
        } else if (m > 15 && m <= 30) {
            min = "30";
        } else if (m > 30 && m <= 45) {
            min = "45";
        } else {
            min = "00";
        }

        time = hour + min;
        return time;
    }

    private String calculateHP(String s1, String s2, String time) {
        double probB;
        double probS;
        int countOrigin = 0;
        int countDest = 0;
        int counterB = 0;
        int counterS = 0;

        time = parseTime(time);
        stationHistory = stationsHistoryData.getStationHistory(time, s1);
        Log.i(TAG, time + " " + String.valueOf(stationHistory.size()));

        for (StationHistory stationH : stationHistory) {
            countOrigin = stationHistory.size();
            Log.i(TAG, stationH.getName() + " " + stationH.getInterval() + " " + String.valueOf(stationH.getFreeBikes()));
            if (stationH.getFreeBikes() >= bikesThreshold) {
                counterB++;
                Log.i(TAG, String.valueOf(counterB));

            }
        }

        stationHistory.clear();
        stationHistory = stationsHistoryData.getStationHistory(time, s2);

        for (StationHistory stationH : stationHistory) {
            countDest = stationHistory.size();
            if (stationH.getFreeBikes() >= slotsThreshold) {
                counterS++;
                Log.i(TAG, String.valueOf(counterS));

            }
            Log.i(TAG, stationH.getName() + " " + stationH.getInterval() + " " + String.valueOf(stationH.getFreeBikes()));
        }

        stationHistory.clear();

        if (countOrigin != 0 && countDest != 0) {
            probB = counterB / countOrigin;
            probS = counterS / countDest;
            TextView hp = (TextView) findViewById(R.id.probS);
            hp.setText(String.valueOf(new DecimalFormat("##.##").format((probB * probS) * 100) + "%"));
        } else {
            probB = 0;
            probS = 0;
            TextView hp = (TextView) findViewById(R.id.probS);
            hp.setText(String.valueOf(new DecimalFormat("##.##").format(0) + "%"));
        }
        TextView originR = (TextView) findViewById(R.id.tv_Orecords);
        originR.setText(String.valueOf(countOrigin));

        TextView destR = (TextView) findViewById(R.id.tv_Drecords);
        destR.setText(String.valueOf(countDest));


        if (probB > rateThreshold && probS > rateThreshold) {
//            SpannableStringBuilder builder = new SpannableStringBuilder();
//            builder.append("Computation completed").append(" ");
//            builder.setSpan(new ImageSpan(JourneyForecast.this, R.drawable.ic_done), builder.length() - 1, builder.length(), 0);
//            Snackbar.make(findViewById(R.id.myCoordinatorLayoutMap), builder, Snackbar.LENGTH_LONG).show();
            return "GO";
        } else {
//            SpannableStringBuilder builder = new SpannableStringBuilder();
//            builder.append("Origin or destination").append(" ");
//            builder.setSpan(new ImageSpan(JourneyForecast.this, R.drawable.ic_highlight_remove), builder.length() - 1, builder.length(), 0);
//            Snackbar.make(findViewById(R.id.myCoordinatorLayoutMap), builder, Snackbar.LENGTH_LONG).show();
            return "NO GO";
        }
    }


    private void calculate(View view) {
        TextView lastValuePredictor = (TextView) findViewById(R.id.LastValuePredictor);
        TextView historyPredictor = (TextView) findViewById(R.id.HistoricPredictor);
        SimpleDateFormat sdf = new SimpleDateFormat("HH-mm");
        String dt = sdf.format(new Date());

        if (originBikes.containsKey(originName) && destSlots.containsKey(destName)) {
            lastValuePredictor.setText(calculateLVP(originName, destName));
            historyPredictor.setText(calculateHP(originName, destName, dt));

            Toast.makeText(this, "Calculation completed", Toast.LENGTH_SHORT).show();
        } else {

            Toast.makeText(this, "Invalid origin and destination", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, StationsListActivity.class);
        startActivity(intent);
        finish();
    }
}
