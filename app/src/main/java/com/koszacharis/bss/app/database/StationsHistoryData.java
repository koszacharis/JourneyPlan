package com.koszacharis.bss.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.koszacharis.bss.app.models.StationHistory;

import java.util.ArrayList;

public class StationsHistoryData {

    private final DatabaseHelper databaseHelper;

    public StationsHistoryData(Context context) {
        databaseHelper = DatabaseHelper.getInstance(context);
    }

    public void storeStationsHistory(ArrayList<StationHistory> stationsHistory) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (StationHistory stationHistory : stationsHistory) {
                ContentValues values = new ContentValues();

                values.put(DatabaseHelper.STATIONS_COLUMN_ID, stationHistory.getId());
                values.put(DatabaseHelper.STATIONS_COLUMN_NAME, stationHistory.getName());
                values.put(DatabaseHelper.STATIONS_HISTORY_INTERVAL, String.valueOf(stationHistory.getInterval()));
                values.put(DatabaseHelper.STATIONS_COLUMN_FREE_BIKES, stationHistory.getFreeBikes());
                values.put(DatabaseHelper.STATIONS_COLUMN_EMPTY_SLOTS, stationHistory.getEmptySlots());

                db.insert(DatabaseHelper.STATIONS_HISTORY_TABLE_NAME, null, values);

            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public ArrayList<StationHistory> getStationsHistory() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        ArrayList<StationHistory> stationsHistory = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT id, name, interval, "
                + "freeBikes, emptySlots "
                + "FROM " + DatabaseHelper.STATIONS_HISTORY_TABLE_NAME, null);

        try {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    StationHistory stationHistory = toStationHistory(cursor);
                    stationsHistory.add(stationHistory);
                    cursor.moveToNext();
                }
            }
            return stationsHistory;
        } finally {
            cursor.close();
        }
    }

    public ArrayList<StationHistory> getStationHistory(String interval, String name) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        ArrayList<StationHistory> stationsHistory = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT id, name, interval, "
                + "freeBikes, emptySlots "
                + "FROM " + DatabaseHelper.STATIONS_HISTORY_TABLE_NAME
                + " WHERE interval = ? AND name = ?", new String[]{interval, name});
        try {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    StationHistory stationHistory = toStationHistory(cursor);
                    stationsHistory.add(stationHistory);
                    cursor.moveToNext();
                }
            }
            return stationsHistory;
        } finally {
            cursor.close();
        }
    }

    private StationHistory toStationHistory(Cursor cursor) {

        StationHistory stationHistory = new StationHistory(
                cursor.getString(0), // id
                cursor.getString(1), // name
                cursor.getString(2), // interval
                cursor.getInt(3), // free bikes
                cursor.getInt(4) // empty slots
        );
        return stationHistory;
    }

    private void clearStations() {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.delete(DatabaseHelper.STATIONS_HISTORY_TABLE_NAME, null, null);
    }
}
