package com.sfvtech.payperview.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.sfvtech.payperview.MainActivity;
import com.sfvtech.payperview.R;

import java.util.Locale;


public class ViewerNumberFragment extends Fragment implements View.OnClickListener {
    public static final String FRAGMENT_TAG = "viewer-number";

    Button mOneButton;
    Button mTwoButton;
    Button mThreeButton;
    Button mLanguageSelector;
    OnViewerNumberSelectedListener mCallback;
    Bundle args;
    private int MAX_VIEWERS;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_viewer_number, container, false);

        final String installationId = MainActivity.ID;
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
        view.setBackgroundResource(R.drawable.button_lg_selected);
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
        }
    }

    private void toggleLanguage(String lang) {
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
