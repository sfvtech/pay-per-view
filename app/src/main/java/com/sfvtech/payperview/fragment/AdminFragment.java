package com.sfvtech.payperview.fragment;


import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.sfvtech.payperview.DataUploadActivity;
import com.sfvtech.payperview.MainActivity;
import com.sfvtech.payperview.R;
import com.sfvtech.payperview.Viewer;
import com.sfvtech.payperview.ViewerSurvey;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class AdminFragment extends Fragment implements View.OnClickListener {

    public static final String EXTRA_INSTALLATION_ID = ViewerSurvey.PACKAGE + "EXTRA_INSTALLATION_ID";
    public static final String FRAGMENT_TAG = "AdminFragment";
    private String mInstallationId;
    private String currentFragmentTag;
    private Button cancelButton;
    private Button restartButton;
    private Button uploadButton;
    private Button quitButton;


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

        if (getArguments().containsKey("currentFragmentTag")) {
            currentFragmentTag = getArguments().getString("currentFragmentTag");
        } else {
            currentFragmentTag = "ViewerNumberFragment";
        }

        cancelButton = (Button) v.findViewById(R.id.adminCancelButton);
        cancelButton.setOnClickListener(this);

        restartButton = (Button) v.findViewById(R.id.adminRestartButton);
        restartButton.setOnClickListener(this);

        uploadButton = (Button) v.findViewById(R.id.adminUploadButton);
        uploadButton.setOnClickListener(this);

        quitButton = (Button) v.findViewById(R.id.adminQuitButton);
        quitButton.setOnClickListener(this);

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
            case R.id.adminQuitButton:
                handleQuit();
                break;
            case R.id.adminRestartButton:
                restart();
                break;
            case R.id.adminUploadButton:
                handleUpload();
                break;
        }
    }

    public void handleUpload() {
        Intent intent = new Intent(getActivity(), DataUploadActivity.class);
        intent.putExtra(EXTRA_INSTALLATION_ID, mInstallationId);
        startActivity(intent);
    }

    public void handleQuit() {
        // Quits the application? Seems unecessary
    }

    public void handleCancel() {
        // Go back to where we were when we called admin menu
        Bundle args = new Bundle();
        args.putParcelableArrayList("mViewers", MainActivity.mViewers);
        args.putInt("MAX_VIEWERS", MainActivity.MAX_VIEWERS);
        switch (currentFragmentTag) {
            case EditViewersFragment.FRAGMENT_TAG:
                final Fragment editViewersFragment = EditViewersFragment.create(MainActivity.mViewers, MainActivity.MAX_VIEWERS);
                ((AppCompatActivity) getContext()).getSupportFragmentManager().
                        beginTransaction().replace(R.id.container, editViewersFragment, EditViewersFragment.FRAGMENT_TAG).
                        commit();
                break;
            case SurveyFragment.FRAGMENT_TAG:
                final Fragment surveyFragment = new SurveyFragment();
                surveyFragment.setArguments(args);
                ((AppCompatActivity) getContext()).getSupportFragmentManager().
                        beginTransaction().replace(R.id.container, surveyFragment, SurveyFragment.FRAGMENT_TAG).
                        commit();
                break;
            case VideoFragment.FRAGMENT_TAG:
                final Fragment videoFragment = new VideoFragment();
                ((AppCompatActivity) getContext()).getSupportFragmentManager().
                        beginTransaction().replace(R.id.container, videoFragment, VideoFragment.FRAGMENT_TAG).
                        commit();
                break;
            case ViewerInfoFragment.FRAGMENT_TAG:
                final Fragment viewerInfoFragment = new ViewerInfoFragment();
                Bundle infoArgs = new Bundle();
                infoArgs.putParcelableArrayList("mViewers", MainActivity.mViewers);
                viewerInfoFragment.setArguments(args);
                ((AppCompatActivity) getContext()).getSupportFragmentManager().
                        beginTransaction().replace(R.id.container, viewerInfoFragment, ViewerInfoFragment.FRAGMENT_TAG).
                        commit();
                break;
            case ViewerNumberFragment.FRAGMENT_TAG:
                restart();
                break;
            case ThankYouFragment.FRAGMENT_TAG:
                restart();
                break;
            default:
                restart();
                break;
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
}
