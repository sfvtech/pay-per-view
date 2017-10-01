package com.sfvtech.payperview;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sfvtech.payperview.database.DatabaseContract;
import com.sfvtech.payperview.database.DatabaseHelper;
import com.sfvtech.payperview.fragment.AdminFragment;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class DataUploadActivity extends Activity {

    private final String LOG_TAG = "DataUploadActivity";
    long mRecordsToUpload = 0;
    boolean mIsClientOnline = false;
    boolean mIsServerResponsive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_data_upload);

        // Count the number of viewer records to upload
        updateRecordsToUploadView();

        // Disable the Upload button if there are no records to upload
        if (mRecordsToUpload == 0) {
            Button uploadButton = (Button) findViewById(R.id.uploadButton);
            uploadButton.setEnabled(false);
        }

        // See if we're online
        TextView networkStatusView = (TextView) findViewById(R.id.network_status_value);
        mIsClientOnline = isClientOnline();
        if (mIsClientOnline) {
            networkStatusView.setText(getString(R.string.network_status_online));
        }

        // See if we can resolve the upload endpoint
        new CheckServerStatus().execute(getString(R.string.upload_endpoint_url));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.data_upload, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return (id == R.id.action_settings) || super.onOptionsItemSelected(item);
    }

    public void uploadData(View view) {
        String csv = getNewRecordsAsCsv();
        new PostData().execute(getString(R.string.upload_endpoint_url), csv);
    }

    /**
     * Returns the number of viewer records on the device not yet uploaded to the server
     *
     * @return long
     */
    private long getNewRecordCount() {
        SQLiteDatabase database = new DatabaseHelper(this).getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(database, "viewers",
                DatabaseContract.ViewerEntry.COLUMN_UPLOADED_TIME + " IS NULL;");
        database.close();
        return count;
    }

    /**
     * Returns the viewer records on the device not yet uploaded to the server, as a CSV
     *
     * @return String
     * @todo put in an AsyncTask
     */
    private String getNewRecordsAsCsv() {

        StringBuilder csv = new StringBuilder();

        SQLiteDatabase database = new DatabaseHelper(this).getReadableDatabase();
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

        // Auto closeable cursor try-with-resources
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

    /**
     * Sets the "uploaded time" for uploaded files, in bulk.
     *
     * @return Boolean
     * @todo move getWritableDatabase() into AsyncTask
     */
    private boolean updateNewRecords() {
        SQLiteDatabase database = new DatabaseHelper(this).getWritableDatabase();

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
        return true;
    }

    /**
     * Returns whether the device is network-enabled
     *
     * @return boolean
     */
    private boolean isClientOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /**
     * Updates the "number of records to update" counter
     */
    protected void updateRecordsToUploadView() {

        TextView recordsToUploadView = (TextView) findViewById(R.id.records_to_upload_value);
        mRecordsToUpload = getNewRecordCount();
        recordsToUploadView.setText(Long.toString(mRecordsToUpload));
    }

    public void handleCancel(View view) {
        finish();
    }

    /**
     * @return Integer the version number of the application
     */
    public int getApplicationVersionNumber() {
        PackageInfo info;
        try {
            PackageManager manager = getApplication().getPackageManager();
            info = manager.getPackageInfo(
                    getApplication().getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

    private class CheckServerStatus extends AsyncTask<String, Void, Boolean> {

        @Override
        // @todo require https URLs by default, but allow for configurable (insecure) override
        protected Boolean doInBackground(String... params) {
            try {
                // @todo refactor to use HttpURLConnection instead. HttpClient is @deprecated
                URL urlObj = new URL(params[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) urlObj.openConnection();
                urlConnection.setReadTimeout(10000); // 10 seconds
                urlConnection.setConnectTimeout(15000); // 15 seconds
                int status = urlConnection.getResponseCode();

                if (status == HttpURLConnection.HTTP_OK) {
                    return true;
                } else {
                    String reason = urlConnection.getResponseMessage();
                    Log.e(LOG_TAG, "Got a not-OK server status (" + status + ") from server: " + reason);
                    return false;
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error pinging server: " + e.toString());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                TextView serverStatusView = (TextView) findViewById(R.id.server_status_value);
                mIsServerResponsive = true;
                serverStatusView.setText(getString(R.string.server_status_online));
            }
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    private class PostData extends AsyncTask<String, Void, Boolean> {

        /**
         * @param params URL of POST endpoint, and string of data to post
         * @return boolean
         */
        @Override
        protected Boolean doInBackground(String... params) {

            String url = params[0];
            String data = params[1];

            String installationId = getIntent().getExtras().getString(AdminFragment.EXTRA_INSTALLATION_ID);

            try {
                URL urlObj = new URL(url);
                HttpURLConnection urlConnection = (HttpURLConnection) urlObj.openConnection();
                urlConnection.setReadTimeout(10000); // 10 seconds
                urlConnection.setConnectTimeout(15000); // 15 seconds
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("sandwich", "fudge pickle") // @todo devise better security :)
                        .appendQueryParameter("installation_id", installationId)
                        .appendQueryParameter("data", data)
                        .appendQueryParameter("version", Integer.toString(getApplicationVersionNumber()));
                // @todo add GPS data
                String query = builder.build().getEncodedQuery();

                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8")
                );
                writer.write(query);
                writer.flush();
                writer.close();
                int response = urlConnection.getResponseCode();
                if (response != HttpURLConnection.HTTP_OK) {
                    String reason = urlConnection.getResponseMessage();
                    Log.e(LOG_TAG, "Got a not-OK response while posting data (" + response + ") to server: " + reason);
                    return false;
                }
                os.close();

                urlConnection.connect();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error POSTing data to server: " + e.toString());
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;
            if (result) {
                // @todo this should really be the actual number of records fetched from the table, not the number we expected to fetch during onCreate()
                CharSequence text = "Successfully uploaded " + Long.toString(mRecordsToUpload) + " records.";
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                updateNewRecords();
                updateRecordsToUploadView();
            } else {
                CharSequence text = "Failed to upload data.";
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }
}
