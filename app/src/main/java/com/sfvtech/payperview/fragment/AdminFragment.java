package com.sfvtech.payperview.fragment;


import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sfvtech.payperview.DataUploadActivity;
import com.sfvtech.payperview.MainActivity;
import com.sfvtech.payperview.R;
import com.sfvtech.payperview.Viewer;
import com.sfvtech.payperview.utils.DownloadUtils;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class AdminFragment extends Fragment implements View.OnClickListener {

    public static final String FRAGMENT_TAG = "AdminFragment";
    public final static String EXTRA_INSTALLATION_ID = "EXTRA_INSTALLATION_ID";
    final String installationId = MainActivity.ID;
    private Button cancelButton;
    private Button restartButton;
    private Button uploadButton;
    private Button editViewersButton;
    private EditText videoURL;
    private EditText subtitlesURL;
    private Button downloadVideoButton;
    private Button downloadSubtitlesButton;
    private TextView subtitlesStatus;
    private TextView videoStatus;
    private ArrayList<Viewer> mViewers = new ArrayList<Viewer>();
    private int nViewers = 0;
    private int MAX_VIEWERS = 0;
    private String fragmentTag;

    public AdminFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_admin, container, false);

        if (getArguments().containsKey("mViewers")) {
            mViewers = getArguments().getParcelableArrayList("mViewers");
        }
        if (getArguments().containsKey("nViewers")) {
            nViewers = getArguments().getInt("nViewers");
        }
        if (getArguments().containsKey("MAX_VIEWERS")) {
            MAX_VIEWERS = getArguments().getInt("MAX_VIEWERS");
        }
        if (getArguments().containsKey("fragmentTag")) {
            fragmentTag = getArguments().getString("fragmentTag");
        }

        final TextView installationIdView = (TextView) v.findViewById(R.id.installationIdValue);
        installationIdView.setText(installationId);

        cancelButton = (Button) v.findViewById(R.id.adminCancelButton);
        cancelButton.setOnClickListener(this);

        restartButton = (Button) v.findViewById(R.id.adminRestartButton);
        restartButton.setOnClickListener(this);

        uploadButton = (Button) v.findViewById(R.id.adminUploadButton);
        uploadButton.setOnClickListener(this);

        downloadSubtitlesButton = (Button) v.findViewById(R.id.download_subtitles);
        downloadSubtitlesButton.setOnClickListener(this);

        videoURL = (EditText) v.findViewById(R.id.videoURL);
        subtitlesURL = (EditText) v.findViewById(R.id.subtitlesURL);

        downloadVideoButton = (Button) v.findViewById(R.id.download_video);
        downloadVideoButton.setOnClickListener(this);

        editViewersButton = (Button) v.findViewById(R.id.adminEditViewers);
        editViewersButton.setOnClickListener(this);

        if (fragmentTag.equals(ViewerNumberFragment.FRAGMENT_TAG) || fragmentTag.equals(ViewerInfoFragment.FRAGMENT_TAG) || fragmentTag.equals(VideoFragment.FRAGMENT_TAG)) {
            editViewersButton.setVisibility(View.GONE);
        }

        // TODO make this dynamic to different MAX_VIEWER numbers
        LinearLayout changeViewerNumber = v.findViewById(R.id.change_viewer_number);
        if (fragmentTag.equals(ViewerInfoFragment.FRAGMENT_TAG) && nViewers != MAX_VIEWERS) {
            changeViewerNumber.setVisibility(View.VISIBLE);
            if (nViewers < MAX_VIEWERS) {
                Button button2 = v.findViewById(R.id.adminChangeViewerNumber2);
                Button button3 = v.findViewById(R.id.adminChangeViewerNumber3);
                button2.setOnClickListener(this);
                button3.setOnClickListener(this);
                if (nViewers == 2) {
                    button2.setVisibility(View.GONE);
                }
            }
        }

        final SharedPreferences preferences = getActivity().getSharedPreferences("preferences", Context.MODE_PRIVATE);
        final String localURIForSubtitles = preferences.getString("localURIForSubtitles", null);
        final String localURIForVideo = preferences.getString("localURIForVideo", null);
        subtitlesStatus = (TextView) v.findViewById(R.id.subtitlesFileSet);
        subtitlesStatus.setText(TextUtils.isEmpty(localURIForSubtitles) ? "Not set" : "File is set and at " + localURIForSubtitles);
        videoStatus = (TextView) v.findViewById(R.id.videoFileSet);
        videoStatus.setText(TextUtils.isEmpty(localURIForVideo) ? "Not set" : "File is set and at " + localURIForVideo);

        try {
            final PackageInfo packageInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            final String version = packageInfo.versionName;
            final TextView versionView = (TextView) v.findViewById(R.id.versionValue);
            versionView.setText(version);
        } catch (Exception e) {
            Log.e(FRAGMENT_TAG, e.toString());
        }
        return v;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.adminCancelButton:
                handleCancel();
                break;
            case R.id.adminRestartButton:
                restart();
                break;
            case R.id.adminUploadButton:
                handleUpload();
                break;
            case R.id.download_video:
                final String urlForVideo = videoURL.getText().toString().trim();
                queueDownload(urlForVideo, "video");
                break;
            case R.id.download_subtitles:
                final String urlForSubtitles = subtitlesURL.getText().toString().trim();
                queueDownload(urlForSubtitles, "subtitles");
                break;
            case R.id.adminEditViewers:
                final Fragment editViewersFragment = EditViewersFragment.create(mViewers, nViewers, fragmentTag);
                ((AppCompatActivity) getContext()).getSupportFragmentManager().
                        beginTransaction().replace(R.id.container, editViewersFragment, EditViewersFragment.FRAGMENT_TAG).
                        commit();
                break;
            case R.id.adminChangeViewerNumber2:
                nViewers = 2;
                handleCancel();
                break;
            case R.id.adminChangeViewerNumber3:
                nViewers = 3;
                handleCancel();
                break;

        }
    }

    public void handleUpload() {
        final Intent intent = new Intent(getActivity(), DataUploadActivity.class);
        intent.putExtra(EXTRA_INSTALLATION_ID, installationId);
        startActivity(intent);
    }

    // Go back to where admin fragment was called with the fragment tag
    public void handleCancel() {
        final Bundle args = new Bundle();
        args.putParcelableArrayList("mViewers", mViewers);
        args.putInt("MAX_VIEWERS", MAX_VIEWERS);
        args.putInt("nViewers", nViewers);
        Log.v("AdminFragment:FragTag", fragmentTag);
        switch (fragmentTag) {
            case ViewerNumberFragment.FRAGMENT_TAG:
                final Fragment viewerNumberFragment = new ViewerNumberFragment();
                viewerNumberFragment.setArguments(args);
                ((AppCompatActivity) getContext()).getSupportFragmentManager().
                        beginTransaction().replace(R.id.container, viewerNumberFragment, ViewerNumberFragment.FRAGMENT_TAG).
                        commit();
                break;
            case VideoFragment.FRAGMENT_TAG:
                // If we interrupt the video fragment we should restart
                final Fragment videoFragment = new VideoFragment();
                videoFragment.setArguments(args);
                ((AppCompatActivity) getContext()).getSupportFragmentManager().
                        beginTransaction().replace(R.id.container, videoFragment, VideoFragment.FRAGMENT_TAG).
                        commit();
                break;
            case ViewerInfoFragment.FRAGMENT_TAG:
                final Fragment viewerInfoFragment = new ViewerInfoFragment();
                viewerInfoFragment.setArguments(args);
                ((AppCompatActivity) getContext()).getSupportFragmentManager().
                        beginTransaction().replace(R.id.container, viewerInfoFragment, ViewerInfoFragment.FRAGMENT_TAG).
                        commit();
                break;
            case SurveyFragment.FRAGMENT_TAG:
                // TODO will this make the same viewers restart their surveys..?
                final Fragment surveyFragment = new SurveyFragment();
                surveyFragment.setArguments(args);
                ((AppCompatActivity) getContext()).getSupportFragmentManager().
                        beginTransaction().replace(R.id.container, surveyFragment, SurveyFragment.FRAGMENT_TAG).
                        commit();
                break;
            case ThankYouFragment.FRAGMENT_TAG:
            default:
                // we don't know where we came from... so restart
                restart();
        }
    }

    private void restart() {
        Log.v(AdminFragment.FRAGMENT_TAG, "Restart Called");
        final Fragment viewerNumberFragment = new ViewerNumberFragment();
        final Bundle restartArgs = new Bundle();
        restartArgs.putParcelableArrayList("mViewers", new ArrayList<Viewer>());
        viewerNumberFragment.setArguments(restartArgs);
        ((AppCompatActivity) getContext()).getSupportFragmentManager().
                beginTransaction().replace(R.id.container, viewerNumberFragment, ViewerNumberFragment.FRAGMENT_TAG).
                commit();
    }

    /**
     * Use Android's Download Manager to queue this download.
     */
    private void queueDownload(String URL, String videoOrSubtitles) {
        final String fileName = videoOrSubtitles.equals("video") ? "video.mp4" : "subtitles.srt";
        try {
            if (URL != null && !URL.isEmpty()) {
                Uri uri = Uri.parse(URL);
                final DownloadManager manager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                final DownloadManager.Request request = new DownloadManager.Request(uri)
                        .addRequestHeader("Referer", URL)
                        .setMimeType(DownloadUtils.getMimeType(URL))
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                        .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.allowScanningByMediaScanner();
                manager.enqueue(request);
            }
        } catch (IllegalStateException e) {
            Toast.makeText(getActivity(), "Please insert an SD card to download file", Toast.LENGTH_SHORT).show();
        }
    }
}
