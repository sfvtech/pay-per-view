package com.sfvtech.payperview.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.sfvtech.payperview.R;
import com.sfvtech.payperview.ViewHelper;

import java.util.Date;
import java.util.Locale;


public class ViewerNumberFragment extends Fragment implements View.OnClickListener {
    public static final String FRAGMENT_TAG = "viewer-number";
    private static final long MAGIC_BUTTON_MAX_MS = 2000; // milliseconds
    Button mOneButton;
    Button mTwoButton;
    Button mThreeButton;
    Button mLanguageSelector;
    OnViewerNumberSelectedListener mCallback;
    Bundle args;
    Date mTimerStart;
    int mLastButtonIndex = -1;
    private int MAX_VIEWERS;
    private Button button1;
    private Button button2;
    private Button button3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_viewer_number, container, false);

        final LinearLayout buttonLayout = (LinearLayout) view.findViewById(R.id.buttonLayout);

        // UI References
        mOneButton = (Button) view.findViewById(R.id.oneButton);
        mTwoButton = (Button) view.findViewById(R.id.twoButton);
        mThreeButton = (Button) view.findViewById(R.id.threeButton);
        mLanguageSelector = (Button) view.findViewById(R.id.languageSelector);

        // Event Listeners
        mOneButton.setOnClickListener(this);
        mOneButton.setOnClickListener(this);
        mTwoButton.setOnClickListener(this);
        mThreeButton.setOnClickListener(this);
        mLanguageSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLanguage((String) v.getTag());
            }
        });

        button1 = view.findViewById(R.id.button1);
        button2 = view.findViewById(R.id.button2);
        button3 = view.findViewById(R.id.button3);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);

        args = new Bundle();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (OnViewerNumberSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnViewerNumberSelectedListener");
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.oneButton:
                getViewerInfo(1);
                break;

            case R.id.twoButton:
                getViewerInfo(2);
                break;

            case R.id.threeButton:
                getViewerInfo(3);
                break;
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
                        Bundle args = new Bundle();
                        ViewHelper.startAdminFragment(getContext(), FRAGMENT_TAG, args);
                    }
                }
                // Reset the button tracker
                mLastButtonIndex = -1;
                break;
        }
    }

    private void toggleLanguage(String lang) {
        Log.v("ViewerNumber", "Langauge toggled " + lang);
        final Context c = getActivity();

        final Configuration cfg = new Configuration();
        if (!TextUtils.isEmpty(lang)) {
            cfg.locale = new Locale(lang);
        } else {
            cfg.locale = Locale.getDefault();
        }

        c.getResources().updateConfiguration(cfg, null);

        // Reload Fragment when locale changed
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new ViewerNumberFragment(), ViewerNumberFragment.FRAGMENT_TAG)
                .commit();
    }

    private void getViewerInfo(int nViewers) {
        mCallback.onViewerNumberSelected(nViewers);
    }

    // Container Activity must implement this interface
    public interface OnViewerNumberSelectedListener {
        void onViewerNumberSelected(int nViewers);
    }
}
