package com.sfvtech.payperview.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "viewer_survey.db";
    public static final String LOG_TAG = "DatabaseHelper";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.v("tag", "helper inst");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.v("tag", "oncreate");
        final String SQL_CREATE_SESSION_TABLE =
                "CREATE TABLE " + DatabaseContract.SessionEntry.TABLE_NAME + " (" +
                        DatabaseContract.SessionEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        DatabaseContract.SessionEntry.COLUMN_START_TIME + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        DatabaseContract.SessionEntry.COLUMN_END_TIME + " DATETIME DEFAULT NULL, " +
                        DatabaseContract.SessionEntry.COLUMN_LOCALE + " TEXT NOT NULL, " +
                        DatabaseContract.SessionEntry.COLUMN_LAT + " REAL, " +
                        DatabaseContract.SessionEntry.COLUMN_LONG + " REAL);";

        final String SQL_CREATE_VIEWER_TABLE =
                "CREATE TABLE " + DatabaseContract.ViewerEntry.TABLE_NAME + " (" +
                        DatabaseContract.ViewerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        DatabaseContract.ViewerEntry.COLUMN_SESSION_ID + " INTEGER NOT NULL, " +
                        DatabaseContract.ViewerEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                        DatabaseContract.ViewerEntry.COLUMN_EMAIL + " TEXT NOT NULL, " +
                        DatabaseContract.ViewerEntry.COLUMN_SURVEY_ANSWER + " TEXT NOT NULL, " +
                        DatabaseContract.ViewerEntry.COLUMN_UPLOADED_TIME + " DATETIME DEFAULT NULL, " +

                        // Set up the location column as a foreign key to location table.
                        " FOREIGN KEY (" + DatabaseContract.ViewerEntry.COLUMN_SESSION_ID + ") REFERENCES " +
                        DatabaseContract.SessionEntry.TABLE_NAME + " (" + DatabaseContract.SessionEntry._ID + "));";

        db.execSQL(SQL_CREATE_SESSION_TABLE);
        db.execSQL(SQL_CREATE_VIEWER_TABLE);
    }

    /**
     * This will be the SQL necessary to migrate existing data to a new version. Will be
     * triggered when DATABASE_VERSION is greater than the version used to create
     * the database.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.v("Tag,", "onupgradecalled");
        switch (oldVersion) {
            // Version 2 adds ViewEntry.COLUMN_UPLOADED_TIME 1-> 2
            case 1:
                // Current version is 1
                break;
        }

    }
}
