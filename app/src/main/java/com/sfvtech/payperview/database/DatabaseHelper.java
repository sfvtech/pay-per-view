package com.sfvtech.payperview.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.sfvtech.payperview.Viewer;

import java.util.Date;

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

    public void saveSession(long sessionId) {
        SQLiteDatabase database = this.getWritableDatabase();
        // End time values
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.SessionEntry.COLUMN_END_TIME, new Date().toString());
        // Selection criteria
        String selection = DatabaseContract.SessionEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(sessionId)};

        int count = database.update(
                DatabaseContract.SessionEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );
        Log.d(LOG_TAG, "Just updated " + Integer.toString(count) + " session row, id is " + Long.toString(sessionId));
        database.close();
    }

    public void saveViewer(Viewer viewer) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ViewerEntry.COLUMN_NAME, viewer.getName());
        values.put(DatabaseContract.ViewerEntry.COLUMN_EMAIL, viewer.getEmail());
        values.put(DatabaseContract.ViewerEntry.COLUMN_SESSION_ID, viewer.getSessionId());
        values.put(DatabaseContract.ViewerEntry.COLUMN_SURVEY_ANSWER, viewer.getSurveyAnswer());

        long newRowId;
        newRowId = db.insert(
                DatabaseContract.ViewerEntry.TABLE_NAME,
                DatabaseContract.ViewerEntry.COLUMN_NULLABLE,
                values
        );

        db.close();
        Log.d(LOG_TAG, "Just inserted user id " + Long.toString(newRowId));
    }

    public String getRecordsAsCSV() {
        StringBuilder csv = new StringBuilder();

        SQLiteDatabase database = this.getReadableDatabase();
        String selectQuery = "SELECT " +
                "S." + DatabaseContract.SessionEntry._ID + ", " +
                "V." + DatabaseContract.ViewerEntry.COLUMN_SESSION_ID + ", " +
                "V." + DatabaseContract.ViewerEntry.COLUMN_NAME + ", " +
                "V." + DatabaseContract.ViewerEntry.COLUMN_EMAIL + ", " +
                "V." + DatabaseContract.ViewerEntry.COLUMN_SURVEY_ANSWER + ", " +
                "S." + DatabaseContract.SessionEntry.COLUMN_START_TIME + ", " +
                "S." + DatabaseContract.SessionEntry.COLUMN_END_TIME + ", " +
                "S." + DatabaseContract.SessionEntry.COLUMN_LAT + ", " +
                "S." + DatabaseContract.SessionEntry.COLUMN_LONG + ", " +
                "S." + DatabaseContract.SessionEntry.COLUMN_LOCALE +
                " FROM " + DatabaseContract.ViewerEntry.TABLE_NAME + " V" +
                " JOIN " + DatabaseContract.SessionEntry.TABLE_NAME + " S" +
                " ON " + "S." + DatabaseContract.SessionEntry._ID +
                " = " + "V." + DatabaseContract.ViewerEntry.COLUMN_SESSION_ID +
                " WHERE " + "V." + DatabaseContract.ViewerEntry.COLUMN_UPLOADED_TIME + " IS NULL;";

        Cursor cursor = null;
        try {
            cursor = database.rawQuery(selectQuery, null);
            // looping through all rows and adding string
            if (cursor.moveToFirst()) {
                do {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(cursor.getString(0)).append(","); // viewer id
                    stringBuilder.append(cursor.getString(1)).append(","); // session id
                    stringBuilder.append(cursor.getString(2)).append(","); // viewer name
                    stringBuilder.append(cursor.getString(3)).append(","); // viewer email
                    stringBuilder.append(cursor.getString(4)).append(","); // viewer pledge
                    stringBuilder.append("\"").append(cursor.getString(5)).append("\","); // session start time
                    stringBuilder.append("\"").append(cursor.getString(6)).append("\","); // session end time
                    stringBuilder.append(cursor.getDouble(7)).append(","); // session latitude
                    stringBuilder.append(cursor.getDouble(8)).append(","); // session longitude
                    stringBuilder.append(cursor.getString(9)); // session locale
                    csv.append(stringBuilder).append("\n");
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        database.close();
        return csv.toString();
    }

    public void updateNewRecords() {
        SQLiteDatabase database = this.getWritableDatabase();

        // Upload time values
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ViewerEntry.COLUMN_UPLOADED_TIME, new Date().toString());

        // Selection criteria
        String selection = DatabaseContract.ViewerEntry.COLUMN_UPLOADED_TIME + " IS NULL";

        int count = database.update(
                DatabaseContract.ViewerEntry.TABLE_NAME,
                values,
                selection,
                null
        );
        Log.d(LOG_TAG, "Just updated " + Integer.toString(count) + " viewer record upload dates.");
        database.close();
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
