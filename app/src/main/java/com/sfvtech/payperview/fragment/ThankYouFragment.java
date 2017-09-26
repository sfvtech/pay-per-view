package com.sfvtech.payperview.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sfvtech.payperview.R;
import com.sfvtech.payperview.ViewHelper;

import java.util.Timer;
import java.util.TimerTask;


/**
 * A simple {@link Fragment} subclass.
 */
public class ThankYouFragment extends Fragment {

    ThankYouFragment.OnSessionFinishedListener mCallback;

    public ThankYouFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (ThankYouFragment.OnSessionFinishedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnSurveyFinishedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_thank_you, container, false);
        // Magic Menu Buttons
        ViewHelper.addMagicMenuButtons(view);

        // Go to start activity after 10 seconds
        new Timer().schedule(new TimerTask() {
            public void run() {
                mCallback.onSessionFinished();
            }
        }, 10000);

        return view;
    }

    // Container Activity must implement this interface
    public interface OnSessionFinishedListener {
        void onSessionFinished();
    }

}
