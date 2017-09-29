package com.sfvtech.payperview;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.RelativeLayout;

import com.sfvtech.payperview.database.DatabaseHelper;
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
        EditViewersFragment.onEditViewersFinishedListener {

    // Constants
    //public static final String EXTRA_VIEWERS = ViewerSurvey.PACKAGE + ":EXTRA_VIEWERS";
    public static final String LOG_TAG = "ViewerInfoActivity";
    public static final int MY_PERMISSION_REQUEST_LOCATION = 100;

    // Attributes
    public static int MAX_VIEWERS = 0;
    public static int mNViewers = 0;
    public static ArrayList<Viewer> mViewers;
    public static String currentFragmentTag;
    public static String ID;
    public static double longitude;
    public static double latitude;
    private RelativeLayout mainRoot;
    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Trying to trigger database update
        SQLiteDatabase database = new DatabaseHelper(this).getWritableDatabase();
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
        Log.v("TAG", ID);

        getLocation();

        Fragment viewerNumberFragment = new ViewerNumberFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.container, viewerNumberFragment);
        ft.commit();

        // Add our magic buttons to the main activity layout
        mainRoot = (RelativeLayout) findViewById(R.id.main_root);
        ViewHelper.addMagicMenuButtons(mainRoot);

        currentFragmentTag = ViewerNumberFragment.FRAGMENT_TAG;
    }

    @Override
    public void onViewerNumberSelected(int nViewers) {
        mNViewers = nViewers;
        Fragment viewerInfoFragment = new ViewerInfoFragment();
        Bundle args = new Bundle();
        args.putInt("nViewers", nViewers);
        args.putInt("MAX_VIEWERS", MAX_VIEWERS);
        viewerInfoFragment.setArguments(args);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, viewerInfoFragment);
        ft.commit();
        currentFragmentTag = ViewerInfoFragment.FRAGMENT_TAG;
    }

    @Override
    public void onViewerInfoSubmitted(ArrayList<Viewer> mViewers) {
        this.mViewers = mViewers;

        // Go to video
        Fragment videoFragment = new VideoFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, videoFragment);
        ft.commit();
        currentFragmentTag = VideoFragment.FRAGMENT_TAG;
    }

    @Override
    public void onVideoFinished() {
        // Go to edit viewers
        Log.v("TAG from video MAX", MAX_VIEWERS + "");
        Fragment editViewersFragment = EditViewersFragment.create(mViewers, MAX_VIEWERS);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, editViewersFragment);
        ft.commit();
        currentFragmentTag = EditViewersFragment.FRAGMENT_TAG;
    }

    @Override
    public void onEditViewersFinished(ArrayList<Viewer> mViewers) {
        // Edit list of viewers
        this.mViewers = mViewers;

        if (!mViewers.isEmpty()) {
            // Go to survey
            Fragment surveyFragment = new SurveyFragment();
            Bundle args = new Bundle();
            args.putParcelableArrayList("mViewers", mViewers);
            surveyFragment.setArguments(args);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.container, surveyFragment);
            ft.commit();
            currentFragmentTag = SurveyFragment.FRAGMENT_TAG;
        } else {
            mViewers.clear();
            Fragment viewerNumberFragment = new ViewerNumberFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.container, viewerNumberFragment);
            ft.commit();
            currentFragmentTag = ViewerNumberFragment.FRAGMENT_TAG;
        }
    }

    @Override
    public void onSurveyFinished() {
        // Go to thank you
        Fragment thankYouFragment = new ThankYouFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, thankYouFragment);
        ft.commit();
        currentFragmentTag = ThankYouFragment.FRAGMENT_TAG;
    }

    @Override
    public void onSessionFinished() {
        // Restart with viewer numbers
        mViewers.clear();
        mNViewers = 0;
        Fragment viewerNumberFragment = new ViewerNumberFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, viewerNumberFragment);
        ft.commit();
        currentFragmentTag = ViewerNumberFragment.FRAGMENT_TAG;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    getLocation();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
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
