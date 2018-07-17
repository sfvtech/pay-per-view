package com.sfvtech.payperview.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.sfvtech.payperview.R;
import com.sfvtech.payperview.SubtitleView;
import com.sfvtech.payperview.ViewHelper;

import java.io.IOException;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class VideoFragment extends Fragment implements SurfaceHolder.Callback, View.OnClickListener {

    public static final String FRAGMENT_TAG = "VideoFragment";
    private static final long MAGIC_BUTTON_MAX_MS = 2000; // milliseconds
    VideoFragment.OnVideoFinishedListener mCallback;
    SubtitleView subtitleView;
    Date mTimerStart;
    int mLastButtonIndex = -1;
    private VideoPlayer mPlayer = new VideoPlayer();
    private int position = 0;
    private Button button1;
    private Button button2;
    private Button button3;

    public VideoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_video, container, false);
        if (savedInstanceState != null) {
            position = savedInstanceState.getInt("position", 0);
        }

        button1 = v.findViewById(R.id.button1);
        button2 = v.findViewById(R.id.button2);
        button3 = v.findViewById(R.id.button3);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);

        final SurfaceView mSurface = (SurfaceView) v.findViewById(R.id.video_surface);
        subtitleView = (SubtitleView) v.findViewById(R.id.subs_box);
        final SurfaceHolder mSurfaceHolder = mSurface.getHolder();
        mSurfaceHolder.addCallback(this);
        mPlayer.setSubtitleView(subtitleView);
        mPlayer.setSurface(mSurfaceHolder);

        return v;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button1:
                mTimerStart = new Date();
                mLastButtonIndex = 0;
                break;
            case R.id.button2:
                if (mLastButtonIndex == 0) {
                    mLastButtonIndex = 1;
                } else {
                    mLastButtonIndex = -1;
                }
                break;
            case R.id.button3:
                if (mLastButtonIndex == 1) {
                    long interval = new Date().getTime() - mTimerStart.getTime();
                    if (interval < MAGIC_BUTTON_MAX_MS) {
                        ViewHelper.startAdminFragment(getContext(), FRAGMENT_TAG, new Bundle());
                    }
                }
                // Reset the button tracker
                mLastButtonIndex = -1;
                break;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
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
        mPlayer.stop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("position", mPlayer.mPosition);
    }

    // Container Activity must implement this interface
    public interface OnVideoFinishedListener {
        void onVideoFinished();
    }

    class VideoPlayer {

        private MediaPlayer mPlayer;
        private int mPosition = 0;
        private SurfaceHolder mSurfaceHolder;
        private SubtitleView subtitleView;

        public void stop() {
            if (mPlayer != null) {
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
            final String videoUriString = openVideo();
            final String subtitlesUri = getSubtitles();
            try {
                if (!TextUtils.isEmpty(videoUriString)) {
                    try {
                        mPlayer = new MediaPlayer();
                        mPlayer.setDataSource(c, Uri.parse(videoUriString));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    mPlayer = MediaPlayer.create(c, R.raw.video);
                }
                mPlayer.setDisplay(mSurfaceHolder);
                if (shouldUseSubtitles()) {
                    if (!TextUtils.isEmpty(subtitlesUri)) {
                        subtitleView.setPlayer(mPlayer);
                        // Replace with actual subs file
                        // .srt file https://en.wikipedia.org/wiki/SubRip
                        // Sample srt file download link https://tinyurl.com/ybsz3gw3
                        subtitleView.setSubSourceFromFile(subtitlesUri, MediaPlayer.MEDIA_MIMETYPE_TEXT_SUBRIP);
                    } else {
                        // set subtitles from raw file or no subtitles
                    }
                }
                mPlayer.setLooping(false);
                mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        stop();
                        mCallback.onVideoFinished();
                    }
                });
                if (position > 0) {
                    mPosition = position;
                }
                if (mPosition > 0) {
                    mPlayer.seekTo(mPosition);
                }
                mPlayer.setOnPreparedListener(
                        new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                mp.start();
                            }
                        });
                mPlayer.prepareAsync();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        public void setSurface(SurfaceHolder sh) {
            mSurfaceHolder = sh;
        }

        public void setSubtitleView(SubtitleView sbtview) {
            subtitleView = sbtview;
        }

        public void resetPosition() {
            mPosition = 0;
        }

        public String openVideo() {
            final SharedPreferences preferences = getActivity().getSharedPreferences("preferences", Context.MODE_PRIVATE);
            // Earlier installations of the app used a hack that depended on a video.mp4 file in the
            // Downloads directory. Let's not make those devices reconfigure and download the file.
            // @todo it would be nice to ensure that Download/video.mp4 exists before defaulting to it
            final String localUriString = preferences.getString("localURIForVideo", "file:///storage/emulated/0/Download/video.mp4");
            return localUriString;
        }

        public String getSubtitles() {
            final SharedPreferences preferences = getActivity().getSharedPreferences("preferences", Context.MODE_PRIVATE);
            final String localUriString = preferences.getString("localURIForSubtitles", null);
            return localUriString;
        }

        public boolean shouldUseSubtitles() {
            final SharedPreferences preferences = getActivity().getSharedPreferences("preferences", Context.MODE_PRIVATE);
            return preferences.getBoolean("useSubtitles", false);
        }
    }
}
