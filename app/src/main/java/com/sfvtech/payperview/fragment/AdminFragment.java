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
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sfvtech.payperview.DataUploadActivity;
import com.sfvtech.payperview.R;
import com.sfvtech.payperview.Viewer;
import com.sfvtech.payperview.ViewerSurvey;
import com.sfvtech.payperview.utils.DownloadUtils;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class AdminFragment extends Fragment implements View.OnClickListener {

    public static final String EXTRA_INSTALLATION_ID = ViewerSurvey.PACKAGE + "EXTRA_INSTALLATION_ID";
    public static final String FRAGMENT_TAG = "AdminFragment";

    private String mInstallationId;
    private Button cancelButton;
    private Button restartButton;
    private Button uploadButton;
    private EditText videoURL;
    private EditText subtitlesURL;
    private Button downloadVideoButton;
    private Button downloadSubtitlesButton;
    private TextView subtitlesStatus;
    private TextView videoStatus;

    public AdminFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_admin, container, false);

        // Set up metadata display
        if (getArguments().containsKey("ID")) {
            mInstallationId = getArguments().getString("ID");
        }

        TextView installationIdView = (TextView) v.findViewById(R.id.installationIdValue);
        installationIdView.setText(mInstallationId);

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

        final SharedPreferences preferences = getActivity().getSharedPreferences("preferences", Context.MODE_PRIVATE);
        final String localURIForSubtitles = preferences.getString("localURIForSubtitles", null);
        final String localURIForVideo = preferences.getString("localURIForVideo", null);
        subtitlesStatus = (TextView) v.findViewById(R.id.subtitlesFileSet);
        subtitlesStatus.setText(TextUtils.isEmpty(localURIForSubtitles) ? "Not set" : "File is set and at " + localURIForSubtitles);
        videoStatus = (TextView) v.findViewById(R.id.videoFileSet);
        videoStatus.setText(TextUtils.isEmpty(localURIForVideo) ? "Not set" : "File is set and at " + localURIForVideo);

        try {
            PackageInfo packageInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            String version = packageInfo.versionName;
            TextView versionView = (TextView) v.findViewById(R.id.versionValue);
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
                String urlForVideo = videoURL.getText().toString().trim();
                queueDownload(urlForVideo, "video");
                break;
            case R.id.download_subtitles:
                String urlForSubtitles = subtitlesURL.getText().toString().trim();
                queueDownload(urlForSubtitles, "subtitles");
                break;
        }
    }

    public void handleUpload() {
        Intent intent = new Intent(getActivity(), DataUploadActivity.class);
        intent.putExtra(EXTRA_INSTALLATION_ID, mInstallationId);
        startActivity(intent);
    }

    public void handleCancel() {
        // Go back to where we were when we called admin menu
        FragmentManager fm = ((AppCompatActivity) getContext()).getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 1) {
            fm.popBackStackImmediate();
            fm.beginTransaction().commit();
        }
    }

    private void restart() {
        final Fragment viewerNumberFragment = new ViewerNumberFragment();
        Bundle restartArgs = new Bundle();
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
        String fileName = videoOrSubtitles.equals("video") ? "video.mp4" : "subtitles.srt";
        try {
            if (URL != null && !URL.isEmpty()) {
                Uri uri = Uri.parse(URL);
                DownloadManager manager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
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
