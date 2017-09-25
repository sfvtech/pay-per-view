package com.sfvtech.payperview;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

public class ThankYouActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Full screen
        ActionBar actionBar = getActionBar();
        actionBar.hide();

        LayoutInflater inflater = (LayoutInflater) getSystemService(this.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.activity_survey, null);
        setContentView(layout);

        setContentView(R.layout.activity_thank_you);

        // Magic Menu Buttons
        ViewHelper.addMagicMenuButtons(layout);

        // Go to start activity after 10 seconds
        new Timer().schedule(new TimerTask() {
            public void run() {
                goToViewerNumberActivity();
            }
        }, 10000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.thank_you, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Restart the app
    private void goToViewerNumberActivity() {

        Intent intent = new Intent(this, ViewerNumberActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }
}
