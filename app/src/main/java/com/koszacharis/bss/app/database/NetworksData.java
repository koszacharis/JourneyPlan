package com.koszacharis.bss.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.koszacharis.bss.app.models.BikeNetwork;

import java.util.ArrayList;
import java.util.Collections;

public class NetworksData {

    private final DatabaseHelper databaseHelper;
    private final String TAG = NetworksData.class.getSimpleName();

    public NetworksData(Context context) {
        databaseHelper = DatabaseHelper.getInstance(context);
    }

    public void storeNetworks(ArrayList<BikeNetwork> bikeNetworks) {
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        sqLiteDatabase.beginTransaction();
        try {

            DatabaseHelper.DbClear(sqLiteDatabase, DatabaseHelper.NETWORKS_TABLE_NAME);
            Log.i(TAG, "cleared");
            for (BikeNetwork bikeNetwork : bikeNetworks) {
                ContentValues values = new ContentValues();

                values.put(DatabaseHelper.NETWORKS_COLUMN_ID, bikeNetwork.getId());
                values.put(DatabaseHelper.NETWORKS_COLUMN_NAME, bikeNetwork.getName());
                values.put(DatabaseHelper.NETWORKS_COLUMN_LATITUDE, String.valueOf(bikeNetwork.getLatitude()));
                values.put(DatabaseHelper.NETWORKS_COLUMNS_LONGITUDE, String.valueOf(bikeNetwork.getLongitude()));

                sqLiteDatabase.insert(DatabaseHelper.NETWORKS_TABLE_NAME, null, values);
            }
            sqLiteDatabase.setTransactionSuccessful();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }

    private BikeNetwork toNetwork(Cursor cursor) {
        BikeNetwork bikeNetwork = new BikeNetwork(
                cursor.getString(0), // id
                cursor.getString(1), // name
                cursor.getDouble(2), // latitude
                cursor.getDouble(3) // longitude
        );


        return bikeNetwork;
    }


    public ArrayList<BikeNetwork> getBikeNetworks() {
        SQLiteDatabase sqLiteDatabase = databaseHelper.getReadableDatabase();
        ArrayList<BikeNetwork> bikeNetworks = new ArrayList<>();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT id, name, latitude, longitude, "
                + DatabaseHelper.NETWORKS_TABLE_NAME, null);

        try {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    BikeNetwork bikeNetwork = toNetwork(cursor);
                    bikeNetworks.add(bikeNetwork);
                    cursor.moveToNext();
                }
            }
            Collections.sort(bikeNetworks);
            return bikeNetworks;
        } finally {
            cursor.close();
        }
    }
}
