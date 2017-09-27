package com.sfvtech.payperview.fragment;


import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.sfvtech.payperview.R;
import com.sfvtech.payperview.ViewHelper;


/**
 * A simple {@link Fragment} subclass.
 */
public class VideoFragment extends Fragment implements SurfaceHolder.Callback {

    // Log tag.
    private static final String TAG = VideoFragment.class.getName();

    VideoFragment.OnVideoFinishedListener mCallback;
    private VideoPlayer mPlayer = new VideoPlayer();

    public VideoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_video, container, false);
        SurfaceView mSurface = (SurfaceView) v.findViewById(R.id.video_surface);
        SurfaceHolder mSurfaceHolder = mSurface.getHolder();
        mSurfaceHolder.addCallback(this);
        mPlayer.setSurface(mSurfaceHolder);

        // Magic Menu Buttons
        ViewHelper.addMagicMenuButtons(v);
        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPlayer != null) {
            mPlayer.stop();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (VideoFragment.OnVideoFinishedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnVideoFinishedListener");
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mPlayer.play(getActivity());
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    // Container Activity must implement this interface
    public interface OnVideoFinishedListener {
        void onVideoFinished();
    }

    class VideoPlayer {

        private MediaPlayer mPlayer;
        private int mPosition = 0;
        private SurfaceHolder mSurfaceHolder;

        public void stop() {
            if (mPlayer != null) {
                mCallback.onVideoFinished();
                mPlayer.release();
                mPlayer = null;
            }
        }

        public void pause() {
            if (mPlayer != null && mPlayer.isPlaying()) {
                mPlayer.pause();
                mPosition = mPlayer.getCurrentPosition();
            }
        }

        public void play(final Context c) {
            stop();
            // TODO: replace raw video file with actual file below
            mPlayer = MediaPlayer.create(c, R.raw.small);
            mPlayer.setDisplay(mSurfaceHolder);
            mPlayer.setLooping(false);
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stop();
                }
            });
            if (mPosition > 0) {
                mPlayer.seekTo(mPosition);
            }
            mPlayer.start();
        }

        public void setSurface(SurfaceHolder sh) {
            mSurfaceHolder = sh;
        }

        public void resetPosition() {
            mPosition = 0;
        }
    }
}
