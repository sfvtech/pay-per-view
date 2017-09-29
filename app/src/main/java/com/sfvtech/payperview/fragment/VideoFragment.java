package com.sfvtech.payperview.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.sfvtech.payperview.R;
import com.sfvtech.payperview.SubtitleView;

import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 */
public class VideoFragment extends Fragment implements SurfaceHolder.Callback {

    public static final String FRAGMENT_TAG = "VideoFragment";
    // Log tag.
    private static final String TAG = VideoFragment.class.getName();
    VideoFragment.OnVideoFinishedListener mCallback;
    SubtitleView subtitleView;
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
        subtitleView = (SubtitleView) v.findViewById(R.id.subs_box);
        SurfaceHolder mSurfaceHolder = mSurface.getHolder();
        mSurfaceHolder.addCallback(this);
        mPlayer.setSubtitleView(subtitleView);
        mPlayer.setSurface(mSurfaceHolder);

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
            final Uri uri = openVideo();
            Log.v("URI", uri.toString());
            final String subtitlesUri = getSubtitles();
            Log.v("Tag subtitles", subtitlesUri);
            try {
                mPlayer = new MediaPlayer();
                try {
                    mPlayer.setDataSource(c, openVideo());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mPlayer.setDisplay(mSurfaceHolder);
                subtitleView.setPlayer(mPlayer);
                // Replace with actual subs file
                // .srt file https://en.wikipedia.org/wiki/SubRip
                // Sample srt file download link https://tinyurl.com/ybsz3gw3
                subtitleView.setSubSourceFromFile(subtitlesUri, MediaPlayer.MEDIA_MIMETYPE_TEXT_SUBRIP);
                mPlayer.setLooping(false);
                mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        stop();
                        mCallback.onVideoFinished();
                    }
                });
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

        public Uri openVideo() {
            final SharedPreferences preferences = getActivity().getSharedPreferences("preferences", Context.MODE_PRIVATE);
            final String localUriString = preferences.getString("localURIForVideo", null);
            return Uri.parse(localUriString);
        }

        public String getSubtitles() {
            final SharedPreferences preferences = getActivity().getSharedPreferences("preferences", Context.MODE_PRIVATE);
            final String localUriString = preferences.getString("localURIForSubtitles", null);
            return localUriString;
        }
    }
}
