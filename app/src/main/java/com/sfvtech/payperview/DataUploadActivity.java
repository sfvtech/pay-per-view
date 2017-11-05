package com.sfvtech.payperview;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DataUploadActivity extends Activity implements View.OnClickListener {

    public static final int MY_PERMISSION_REQUEST_STORAGE = 101;
    private final String LOG_TAG = "DataUploadActivity";
    long mRecordsToUpload = 0;
    boolean mIsClientOnline = false;
    boolean mIsServerResponsive = false;
    Button emailButton;

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

        emailButton = findViewById(R.id.emailButton);
        emailButton.setOnClickListener(this);

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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.emailButton:
                sendDataToEmail();
                break;
            default:
                break;
        }
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

    public Uri prepareCSVForEmail() {
        final String csv = getNewRecordsAsCsv();
        final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        final File csvFile = new File(
                Environment.getExternalStorageDirectory(), "data" + timeStamp + ".csv");

        try {
            final FileOutputStream stream = new FileOutputStream(csvFile);
            stream.write(csv.getBytes());
            stream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Uri.fromFile(csvFile);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_STORAGE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    sendDataToEmail();

                } else {
                    Toast.makeText(this, "Need storage permissions to send CSV by email", Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    public void sendDataToEmail() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, DataUploadActivity.MY_PERMISSION_REQUEST_STORAGE);
            return;
        }

        Uri savedCSV = prepareCSVForEmail();
        if (savedCSV != null) {
            final File file = new File(savedCSV.getPath());
            Uri contentUri = null;
            try {
                contentUri = FileProvider.getUriForFile(this, "com.sfvtech.payperview.fileprovider", file);
            } catch (IllegalArgumentException e) {
                Log.e("Tag", "The selected file can't be shared");
            }
            if (contentUri != null) {
                final Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, "Subject");
                i.putExtra(Intent.EXTRA_TEXT, "Body");
                // TODO add ability to add preferred email to preferences
                i.putExtra(Intent.EXTRA_STREAM, contentUri);
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                try {
                    startActivity(Intent.createChooser(i, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
            }
        }
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
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        return dbHelper.getRecordsAsCSV();
    }

    /**
     * Sets the "uploaded time" for uploaded files, in bulk.
     */
    private void updateNewRecords() {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        new updateRecordsTask().execute(databaseHelper);
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

    private class updateRecordsTask extends AsyncTask<DatabaseHelper, Void, Boolean> {
        @Override
        protected Boolean doInBackground(DatabaseHelper... databaseHelpers) {
            databaseHelpers[0].updateNewRecords();
            return true;
        }
    }

    private class CheckServerStatus extends AsyncTask<String, Void, Boolean> {

        @Override
        // @todo require https URLs by default, but allow for configurable (insecure) override
        protected Boolean doInBackground(String... params) {
            try {
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
