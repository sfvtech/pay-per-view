package com.sfvtech.payperview;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class VideoActivity extends Activity implements SurfaceHolder.Callback {

    // Log tag.
    private static final String TAG = VideoActivity.class.getName();

    private VideoPlayer mPlayer = new VideoPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater) getSystemService(this.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.activity_video, null);
        setContentView(layout);

        // Set full screen
        ActionBar actionBar = getActionBar();
        actionBar.hide();

        SurfaceView mSurface = (SurfaceView) findViewById(R.id.video_surface);
        SurfaceHolder mSurfaceHolder = mSurface.getHolder();
        mSurfaceHolder.addCallback(this);
        mPlayer.setSurface(mSurfaceHolder);
        // Prepare activity to launch on video completion and pass an updated intent
        Intent surveyIntent = getIntent().setClass(this, SurveyActivity.class);
        mPlayer.setNextActivityIntent(surveyIntent);

        // Magic Menu Buttons
        ViewHelper.addMagicMenuButtons(layout);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayer != null) {
            mPlayer.stop();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.video, menu);
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


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mPlayer.play(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}

