package com.koszacharis.bss.app.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String STATIONS_TABLE_NAME = "STATIONS";
    public static final String STATIONS_HISTORY_TABLE_NAME = "STATIONSHISTORY";
    public static final String NETWORKS_TABLE_NAME = "NETWORKS";
    public static final String NETWORKS_COLUMN_NAME = "name";
    public static final String NETWORKS_COLUMN_ID = "id";
    public static final String NETWORKS_COLUMN_LATITUDE = "latitude";
    public static final String NETWORKS_COLUMNS_LONGITUDE = "longitude";
    public static final String STATIONS_COLUMN_ID = "id";
    public static final String STATIONS_COLUMN_NAME = "name";
    public static final String STATIONS_HISTORY_INTERVAL = "interval";
    public static final String STATIONS_COLUMN_LATITUDE = "latitude";
    public static final String STATIONS_COLUMN_LONGITUDE = "longitude";
    public static final String STATIONS_COLUMN_FREE_BIKES = "freeBikes";
    public static final String STATIONS_COLUMN_EMPTY_SLOTS = "emptySlots";
    public static final String STATIONS_COLUMN_STATUS = "status";
    private static final String DB_NAME = "journeyplan.sqlite";
    private static final int DB_VERSION = 1;
    private static DatabaseHelper instance;

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    public static void DbClear(SQLiteDatabase sqLiteDatabase, String tableName) {
        sqLiteDatabase.delete(tableName, null, null);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLES " + STATIONS_HISTORY_TABLE_NAME + ", "
                + STATIONS_TABLE_NAME + ", " + NETWORKS_TABLE_NAME);

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        // id, name, interval, freeBikes, emptySlots
        sqLiteDatabase.execSQL("CREATE TABLE " + STATIONS_HISTORY_TABLE_NAME
                + "(id TEXT, name TEXT NOT NULL, "
                + "interval INTEGER NOT NULL, "
                + "freeBikes INTEGER NOT NULL, "
                + "emptySlots INTEGER NOT NULL)"

        );

        // id, name, latitude, longitude, freeBikes, emptySlots, status
        sqLiteDatabase.execSQL("CREATE TABLE " + STATIONS_TABLE_NAME
                + "(id TEXT PRIMARY KEY, name TEXT NOT NULL, "
                + "latitude NUMERIC NOT NULL, "
                + "longitude NUMERIC NOT NULL, "
                + "freeBikes INTEGER NOT NULL, "
                + "emptySlots INTEGER NOT NULL, "
                + "status TEXT)"

        );

        // id, name, latitude, longitude
        sqLiteDatabase.execSQL("CREATE TABLE " + NETWORKS_TABLE_NAME
                + "(id TEXT, name TEXT, "
                + "latitude NUMERIC, "
                + "longitude NUMERIC)"
        );

    }
}
