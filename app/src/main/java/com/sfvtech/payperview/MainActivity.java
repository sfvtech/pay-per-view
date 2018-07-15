package com.sfvtech.payperview;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.sfvtech.payperview.database.DatabaseHelper;
import com.sfvtech.payperview.fragment.AdminFragment;
import com.sfvtech.payperview.fragment.EditViewersFragment;
import com.sfvtech.payperview.fragment.SurveyFragment;
import com.sfvtech.payperview.fragment.ThankYouFragment;
import com.sfvtech.payperview.fragment.VideoFragment;
import com.sfvtech.payperview.fragment.ViewerInfoFragment;
import com.sfvtech.payperview.fragment.ViewerNumberFragment;

import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements ViewerNumberFragment.OnViewerNumberSelectedListener,
        ViewerInfoFragment.OnViewerInfoSubmittedListener, VideoFragment.OnVideoFinishedListener,
        SurveyFragment.OnSurveyFinishedListener, ThankYouFragment.OnSessionFinishedListener,
        EditViewersFragment.onEditViewersFinishedListener, AdminFragment.OnRestartCalledListener {

    public static final String LOG_TAG = "MainActivity";
    public static final int MY_PERMISSION_REQUEST_LOCATION = 100;
    public static String ID;
    public static double longitude;
    public static double latitude;

    // Attributes
    public int MAX_VIEWERS = 0;
    public int nViewers = 0;
    public ArrayList<Viewer> mViewers = new ArrayList<>();

    /**
     * Attachment download complete receiver.
     * 1. Receiver gets called once attachment download completed.
     * 2. Open the downloaded file.
     */
    BroadcastReceiver attachmentDownloadCompleteReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v("Tag: Received", "received");
            final String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                Log.v("Tag: download ID", "" + downloadId);
                final DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                final DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);
                Cursor cursor = null;
                try {
                    cursor = downloadManager.query(query);
                    if (cursor.moveToFirst()) {
                        Log.v("Tag: cursormovetofirst", "" + downloadId);
                        final String uriString = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                        final String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uriString);
                        Log.v("Tag: uriString", uriString);
                        final String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
                        //final String fileName = URLUtil.guessFileName(Uri.decode(localUri), null, mimeType);
                        //final Uri uriForFile = FileProvider.getUriForFile(context, "com.sfvtech.payperview" + FILE_PROVIDER_EXTENSION, file);
                        int downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                        Log.v("Tag: downloadstatus", "" + downloadStatus);
                        Log.v("Tag: uriString", uriString);
                        if ((downloadStatus == DownloadManager.STATUS_SUCCESSFUL)) {
                            final SharedPreferences preferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
                            if (fileExtension.equals(".mp4")) {
                                preferences.edit().putString("localURIForVideo", uriString).apply();
                                Log.v("TAG download local URI", uriString);
                            } else {
                                preferences.edit().putString("localURIForSubtitles", uriString).apply();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }
    };

    private String currentFragmentTag;
    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Trying to trigger database update
        final SQLiteDatabase database = new DatabaseHelper(this).getWritableDatabase();
        database.close();

        // Shared preferences to keep track of max viewers
        SharedPreferences preferences = this.getSharedPreferences("preferences", Context.MODE_PRIVATE);
        MAX_VIEWERS = Integer.parseInt(preferences.getString("MAX_VIEWERS", "3"));
        Log.v("Tag:Max", MAX_VIEWERS + "");

        // Installation ID
        final String id = preferences.getString("ID", null);
        if (id == null) {
            String uniqueID = UUID.randomUUID().toString();
            preferences.edit().putString("ID", uniqueID).apply();
            ID = uniqueID;
        } else {
            ID = id;
        }
        Log.v("MainActivity:ID", ID);

        getLocation();

        if (savedInstanceState == null) {
            Log.v("Main:savedInstance", "is null");
            currentFragmentTag = ViewerNumberFragment.FRAGMENT_TAG;
            final FragmentManager fragmentManager = getSupportFragmentManager();
            final FragmentTransaction ft = fragmentManager.beginTransaction();
            final Fragment viewerNumberFragment = new ViewerNumberFragment();
            ft.replace(R.id.container, viewerNumberFragment);
            ft.commit();
        } else {
            Log.v("SavedInstance", "not null");
            currentFragmentTag = savedInstanceState.getString("currentFragmentTag");
            Log.v("Main:currentFrag", currentFragmentTag);
            MAX_VIEWERS = savedInstanceState.getInt("MAX_VIEWERS");
            nViewers = savedInstanceState.getInt("nViewers");
            mViewers = savedInstanceState.getParcelableArrayList("mViewers");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.v("MainOnSave", "onsaveinstancestate");
        outState.putInt("MAX_VIEWERS", MAX_VIEWERS);
        outState.putInt("nViewers", nViewers);
        outState.putParcelableArrayList("mViewers", mViewers);
        outState.putString("currentFragmentTag", currentFragmentTag);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(attachmentDownloadCompleteReceive, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(attachmentDownloadCompleteReceive);
    }

    @Override
    public void onViewerNumberSelected(int nViewers) {
        this.nViewers = nViewers;
        final Fragment viewerInfoFragment = new ViewerInfoFragment();
        final Bundle args = new Bundle();
        currentFragmentTag = ViewerInfoFragment.FRAGMENT_TAG;
        args.putParcelableArrayList("mViewers", mViewers);
        args.putInt("nViewers", nViewers);
        args.putInt("MAX_VIEWERS", MAX_VIEWERS);
        args.putString("fragmentTag", currentFragmentTag);
        args.putString("ID", ID);
        viewerInfoFragment.setArguments(args);
        Log.v("MainActivity:fragTag", currentFragmentTag);
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.container, viewerInfoFragment);
        ft.commit();
    }

    @Override
    public void onViewerInfoSubmitted(ArrayList<Viewer> mViewers, boolean viewerInfoCompleted) {
        this.mViewers = mViewers;
        final Bundle args = new Bundle();
        args.putParcelableArrayList("mViewers", mViewers);
        args.putString("ID", ID);
        if (viewerInfoCompleted) {
            // Go to video
            final Fragment videoFragment = new VideoFragment();
            final FragmentManager fragmentManager = getSupportFragmentManager();
            final FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.container, videoFragment);
            ft.commit();
            currentFragmentTag = VideoFragment.FRAGMENT_TAG;
        } else {
            currentFragmentTag = ViewerInfoFragment.FRAGMENT_TAG;
        }
    }

    @Override
    public void onVideoFinished() {
        if (!mViewers.isEmpty()) {
            // Go to survey
            Log.v("onVideoFinished", "mViewers not empty");
            final Fragment surveyFragment = new SurveyFragment();
            final Bundle args = new Bundle();
            args.putParcelableArrayList("mViewers", mViewers);
            args.putString("ID", ID);
            surveyFragment.setArguments(args);
            final FragmentManager fragmentManager = getSupportFragmentManager();
            final FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.container, surveyFragment);
            ft.commit();
            currentFragmentTag = SurveyFragment.FRAGMENT_TAG;
        } else {
            mViewers.clear();
            final Fragment viewerNumberFragment = new ViewerNumberFragment();
            final FragmentManager fragmentManager = getSupportFragmentManager();
            final FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.container, viewerNumberFragment);
            ft.commit();
            currentFragmentTag = ViewerNumberFragment.FRAGMENT_TAG;
        }
    }

    @Override
    public void onEditViewersFinished(ArrayList<Viewer> mViewers, String fragmentTag) {
        if (!TextUtils.isEmpty(fragmentTag)) {
            Log.v("EditViewerFinished", fragmentTag);
        }
        // Edit list of viewers
        this.mViewers = mViewers;
        this.nViewers = mViewers.size();

        // Go back to whatever we were doing...
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final Bundle args = new Bundle();
        args.putParcelableArrayList("mViewers", mViewers);
        args.putInt("nViewers", nViewers);
        args.putInt("MAX_VIEWERS", MAX_VIEWERS);
        args.putString("fragmentTag", fragmentTag);
        args.putString("ID", ID);
        final FragmentTransaction ft = fragmentManager.beginTransaction();
        if (!TextUtils.isEmpty(fragmentTag)) {
            Log.v("EditViewerFin:FragTag", fragmentTag);
            switch (fragmentTag) {
                case SurveyFragment.FRAGMENT_TAG:
                    final Fragment surveyFragment = new SurveyFragment();
                    surveyFragment.setArguments(args);
                    ft.replace(R.id.container, surveyFragment);
                    ft.commit();
                    currentFragmentTag = SurveyFragment.FRAGMENT_TAG;
                    break;
                case ViewerInfoFragment.FRAGMENT_TAG:
                    final Fragment viewerInfoFragment = new ViewerInfoFragment();
                    viewerInfoFragment.setArguments(args);
                    ft.replace(R.id.container, viewerInfoFragment);
                    ft.commit();
                    currentFragmentTag = ViewerInfoFragment.FRAGMENT_TAG;
                    break;
                case ViewerNumberFragment.FRAGMENT_TAG:
                    final Fragment viewerNumberFragment = new ViewerNumberFragment();
                    ft.replace(R.id.container, viewerNumberFragment);
                    ft.commit();
                    currentFragmentTag = ViewerNumberFragment.FRAGMENT_TAG;
                    break;
                case VideoFragment.FRAGMENT_TAG:
                    final Fragment videoFragment = new VideoFragment();
                    ft.replace(R.id.container, videoFragment);
                    ft.commit();
                    currentFragmentTag = VideoFragment.FRAGMENT_TAG;
                    break;
                default:
                    // If we don't know where it came from, restart
                    Log.v("Onedit", "don't know where it came from");
                    onSessionFinished();
                    break;
            }
        } else {
            onSessionFinished();
        }
    }

    @Override
    public void onSurveyFinished() {
        // Go to thank you
        final Fragment thankYouFragment = new ThankYouFragment();
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.container, thankYouFragment);
        ft.commit();
        currentFragmentTag = ThankYouFragment.FRAGMENT_TAG;
        final Bundle args = new Bundle();
        // Disable magic buttons for Thank You Fragment
    }

    @Override
    public void onSessionFinished() {
        // Restart with viewer numbers
        mViewers.clear();
        nViewers = 0;
        final Bundle args = new Bundle();
        args.putParcelableArrayList("mViewers", mViewers);
        args.putString("ID", ID);
        final Fragment viewerNumberFragment = new ViewerNumberFragment();
        viewerNumberFragment.setArguments(args);
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.container, viewerNumberFragment);
        ft.commit();
        currentFragmentTag = ViewerNumberFragment.FRAGMENT_TAG;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    getLocation();

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;

        }
    }

    public void getLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new myLocationlistener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MainActivity.MY_PERMISSION_REQUEST_LOCATION);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
    }

    @Override
    public void onRestartCalled() {
        onSessionFinished();
    }

    private class myLocationlistener implements LocationListener {

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Log.v("Lat", Double.toString(latitude));
                Log.v("Long", Double.toString(longitude));
            }
        }
    }
}
