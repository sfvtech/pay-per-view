package com.sfvtech.payperview;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class AdminActivity extends Activity {

    public static final String EXTRA_INSTALLATION_ID = ViewerSurvey.PACKAGE + "EXTRA_INSTALLATION_ID";
    public static final String LOG_TAG = "AdminActivity";
    private String mInstallationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_admin);

        // Set up metadata display
        mInstallationId = new Installation().getId(this.getApplicationContext());
        TextView installationIdView = (TextView) findViewById(R.id.installationIdValue);
        installationIdView.setText(mInstallationId);

        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = packageInfo.versionName;
            TextView versionView = (TextView) findViewById(R.id.versionValue);
            versionView.setText(version);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.toString());
        }

        // Hide the Action Bar
        ActionBar actionBar = getActionBar();
        actionBar.hide();
    }

    public void handleUpload(View view) {
        Intent intent = new Intent(this, DataUploadActivity.class);
        intent.putExtra(EXTRA_INSTALLATION_ID, mInstallationId);
        startActivity(intent);
    }

    public void handleRestart(View view) {

        finish();

        // Return to the first activity
        Intent intent = new Intent(this, ViewerNumberActivity.class);
        startActivity(intent);
    }

    public void handleCancel(View view) {
        finish();
    }

}
