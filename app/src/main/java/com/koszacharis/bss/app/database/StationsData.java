package com.koszacharis.bss.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.koszacharis.bss.app.models.Station;
import com.koszacharis.bss.app.models.Status;

import java.util.ArrayList;
import java.util.Collections;


public class StationsData {
    private final DatabaseHelper databaseHelper;
    private final String TAG = StationsData.class.getSimpleName();

    public StationsData(Context context) {
        databaseHelper = DatabaseHelper.getInstance(context);
    }

    public void storeStations(ArrayList<Station> stations) {
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        sqLiteDatabase.beginTransaction();
        try {
            DatabaseHelper.DbClear(sqLiteDatabase, DatabaseHelper.STATIONS_TABLE_NAME);
            Log.i(TAG, "cleared");
            for (Station station : stations) {
                ContentValues values = new ContentValues();

                values.put(DatabaseHelper.STATIONS_COLUMN_ID, station.getId());
                values.put(DatabaseHelper.STATIONS_COLUMN_NAME, station.getName());
                values.put(DatabaseHelper.STATIONS_COLUMN_LATITUDE, String.valueOf(station.getLatitude()));
                values.put(DatabaseHelper.STATIONS_COLUMN_LONGITUDE, String.valueOf(station.getLongitude()));
                values.put(DatabaseHelper.STATIONS_COLUMN_FREE_BIKES, String.valueOf(station.getFreeBikes()));
                values.put(DatabaseHelper.STATIONS_COLUMN_EMPTY_SLOTS, String.valueOf(station.getEmptySlots()));

                if (station.getStatus() != null)
                    values.put(DatabaseHelper.STATIONS_COLUMN_STATUS, station.getStatus().name());

                sqLiteDatabase.insert(DatabaseHelper.STATIONS_TABLE_NAME, null, values);
            }
            sqLiteDatabase.setTransactionSuccessful();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }

    private Station toStation(Cursor cursor) {
        Station station = new Station(
                cursor.getString(0), // id
                cursor.getString(1), // name
                cursor.getDouble(2), // latitude
                cursor.getDouble(3), // longitude
                cursor.getInt(4), // free_bikes
                cursor.getInt(5) // empty_slots
        );

        if (!cursor.isNull(6)) {
            station.setStatus(Status.valueOf(cursor.getString(6))); // status
        }
        return station;
    }

    public ArrayList<Station> getStations() {
        SQLiteDatabase sqLiteDatabase = databaseHelper.getReadableDatabase();
        ArrayList<Station> stations = new ArrayList<>();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT id, name, latitude, longitude, "
                + "freeBikes, emptySlots, status "
                + "FROM " + DatabaseHelper.STATIONS_TABLE_NAME, null);

        try {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    Station station = toStation(cursor);
                    stations.add(station);
                    cursor.moveToNext();
                }
            }
            Collections.sort(stations);
            return stations;
        } finally {
            cursor.close();
        }
    }

}
