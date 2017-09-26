package com.sfvtech.payperview;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.sfvtech.payperview.fragment.EditViewersFragment;
import com.sfvtech.payperview.fragment.SurveyFragment;
import com.sfvtech.payperview.fragment.ThankYouFragment;
import com.sfvtech.payperview.fragment.VideoFragment;
import com.sfvtech.payperview.fragment.ViewerInfoFragment;
import com.sfvtech.payperview.fragment.ViewerNumberFragment;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ViewerNumberFragment.OnViewerNumberSelectedListener,
        ViewerInfoFragment.OnViewerInfoSubmittedListener, VideoFragment.OnVideoFinishedListener,
        SurveyFragment.OnSurveyFinishedListener, ThankYouFragment.OnSessionFinishedListener,
        EditViewersFragment.onEditViewersFinishedListener {

    // Constants
    public static final String EXTRA_VIEWERS = ViewerSurvey.PACKAGE + ":EXTRA_VIEWERS";
    public static final String LOG_TAG = "ViewerInfoActivity";

    // Attributes
    private int mNViewers;
    private ArrayList<Viewer> mViewers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Fragment viewerNumberFragment = new ViewerNumberFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.container, viewerNumberFragment);
        ft.commit();
    }

    @Override
    public void onViewerNumberSelected(int nViewers) {
        mNViewers = nViewers;
        Fragment viewerInfoFragment = new ViewerInfoFragment();
        Bundle args = new Bundle();
        args.putInt("nViewers", nViewers);
        viewerInfoFragment.setArguments(args);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, viewerInfoFragment);
        ft.commit();
    }

    @Override
    public void onViewerInfoSubmitted(ArrayList<Viewer> mViewers) {
        this.mViewers = mViewers;

        // Go to video
        Fragment videoFragment = new VideoFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, videoFragment);
        ft.commit();
    }

    @Override
    public void onVideoFinished() {
        // Go to edit viewers
        Fragment editViewersFragment = EditViewersFragment.create(mViewers);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, editViewersFragment);
        ft.commit();
    }

    @Override
    public void onEditViewersFinished(ArrayList<Viewer> mViewers) {
        // Edit list of viewers
        this.mViewers = mViewers;

        // Go to survey
        Fragment surveyFragment = new SurveyFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("mViewers", mViewers);
        surveyFragment.setArguments(args);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, surveyFragment);
        ft.commit();
    }

    @Override
    public void onSurveyFinished() {
        // Go to thank you
        Fragment thankYouFragment = new ThankYouFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, thankYouFragment);
        ft.commit();
    }

    @Override
    public void onSessionFinished() {
        // Restart with viewer numbers
        mViewers.clear();
        mNViewers = 0;
        Fragment viewerNumberFragment = new ViewerNumberFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, viewerNumberFragment);
        ft.commit();
    }
}
