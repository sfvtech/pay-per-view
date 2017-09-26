package com.sfvtech.payperview.admin;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.sfvtech.payperview.DataUploadActivity;
import com.sfvtech.payperview.Installation;
import com.sfvtech.payperview.MainActivity;
import com.sfvtech.payperview.R;
import com.sfvtech.payperview.ViewerSurvey;

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
    }

    public void handleUpload(View view) {
        Intent intent = new Intent(this, DataUploadActivity.class);
        intent.putExtra(EXTRA_INSTALLATION_ID, mInstallationId);
        startActivity(intent);
    }

    public void handleRestart(View view) {

        finish();

        // Return to the first activity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void handleCancel(View view) {
        finish();
    }

}
