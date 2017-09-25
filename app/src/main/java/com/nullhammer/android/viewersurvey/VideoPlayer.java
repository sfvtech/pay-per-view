package com.nullhammer.android.viewersurvey;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.view.SurfaceHolder;

public class VideoPlayer {

    private MediaPlayer mPlayer;
    private int mPosition = 0;
    private SurfaceHolder mSurfaceHolder;
    private Intent mNextActivityIntent;

    public void stop() {
        if(mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    public void pause() {
        if(mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
            mPosition = mPlayer.getCurrentPosition();
        }
    }

    public void play(final Context c) {
        stop();
        // mPlayer = MediaPlayer.create(c, R.raw.short_plants);
        mPlayer = MediaPlayer.create(c, R.raw.final_16x9);
        mPlayer.setDisplay(mSurfaceHolder);
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stop();
                // @todo this feels dirty.  The video player shouldn't care about the context or intents.  Maybe call setOnCompletionListener() in the caller?
                // @todo figure out why the videoplayer intent is looping
                c.startActivity(mNextActivityIntent);
                ((Activity) c).overridePendingTransition(0,0);
                // @todo figure out why this seems to finish the next activity, effectively restarting the video activity
                ((Activity) c).finish();
            }
        });
        if(mPosition > 0) {
            mPlayer.seekTo(mPosition);
        }
        mPlayer.start();
    }

    public void setNextActivityIntent(Intent intent) {
        mNextActivityIntent = intent;
    }

    public void setSurface(SurfaceHolder sh) {
        mSurfaceHolder = sh;
    }

    public void resetPosition() {
        mPosition = 0;
    }
}
