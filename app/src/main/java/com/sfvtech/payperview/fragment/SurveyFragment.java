package com.sfvtech.payperview.fragment;


import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sfvtech.payperview.R;
import com.sfvtech.payperview.Viewer;
import com.sfvtech.payperview.database.DatabaseContract;
import com.sfvtech.payperview.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.ListIterator;


/**
 * A simple {@link Fragment} subclass.
 */
public class SurveyFragment extends Fragment implements View.OnClickListener {

    public static final String FRAGMENT_TAG = "SurveyFragment";
    SurveyFragment.OnSurveyFinishedListener mCallback;
    private ArrayList<Viewer> mViewers;
    private ListIterator<Viewer> mViewersIterator;
    private Viewer mCurrentViewer;
    private LinearLayout mLayout;
    private LinearLayout surveyLayout;
    private TextView mSurveyTitle;
    private String surveyChoice = "";
    private Button okButton;
    private Button surveyOption1;
    private Button surveyOption2;
    private Button surveyOption3;
    private Button surveyOption4;
    private Button surveyOption5;
    private Button emailConfirmButton;
    private EditText emailTextView;
    private TextView emailQuestion;

    public SurveyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Correct for orienation changes>??
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("mViewers")) {
                mViewers = savedInstanceState.getParcelableArrayList("mViewers");
            }
            if (savedInstanceState.containsKey("mCurrentViewer")) {
                mCurrentViewer = savedInstanceState.getParcelable("mCurrentViewer");
            }
        } else {
            mViewers = getArguments().getParcelableArrayList("mViewers");
        }

        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_survey, container, false);

        // UI references
        mLayout = (LinearLayout) v.findViewById(R.id.my_layout);

        surveyLayout = (LinearLayout) v.findViewById(R.id.survey_options);
        mSurveyTitle = (TextView) v.findViewById(R.id.survey_title);
        okButton = (Button) v.findViewById(R.id.okButton);
        okButton.setOnClickListener(this);
        surveyOption1 = (Button) v.findViewById(R.id.survey_option_one);
        surveyOption1.setOnClickListener(this);
        surveyOption2 = (Button) v.findViewById(R.id.survey_option_two);
        surveyOption2.setOnClickListener(this);
        surveyOption3 = (Button) v.findViewById(R.id.survey_option_three);
        surveyOption3.setOnClickListener(this);
        surveyOption4 = (Button) v.findViewById(R.id.survey_option_four);
        surveyOption4.setOnClickListener(this);
        surveyOption5 = (Button) v.findViewById(R.id.survey_option_five);
        surveyOption5.setOnClickListener(this);

        emailConfirmButton = (Button) v.findViewById(R.id.email_confirm_button);
        emailConfirmButton.setOnClickListener(this);
        emailQuestion = (TextView) v.findViewById(R.id.email_title);
        emailTextView = (EditText) v.findViewById(R.id.email_dislay);

        // Start iteration
        mViewersIterator = mViewers.listIterator(0);
        incrementViewer();

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (SurveyFragment.OnSurveyFinishedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnSurveyFinishedListener");
        }
    }

    private void incrementViewer() {
        mLayout.requestFocus();
        mCurrentViewer = mViewersIterator.next();

        // Replace the name placeholder in the title
        Resources res = getResources();
        emailTextView.setText(mCurrentViewer.getEmail());
        emailQuestion.setText(String.format(res.getString(R.string.correct_email_confirmation), mCurrentViewer.getName()));
        mSurveyTitle.setText(String.format(res.getString(R.string.survey_question), mCurrentViewer.getName()));

        deselectAllButtons();
    }

    @Override
    public void onClick(View view) {
        deselectAllButtons();
        view.setBackgroundResource(R.drawable.button_med_selected);
        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.survey_option_one:
                surveyChoice = getString(R.string.survey_option_one_value);
                break;
            case R.id.survey_option_two:
                surveyChoice = getString(R.string.survey_option_two_value);
                break;
            case R.id.survey_option_three:
                surveyChoice = getString(R.string.survey_option_three_value);
                break;
            case R.id.survey_option_four:
                surveyChoice = getString(R.string.survey_option_four_value);
                break;
            case R.id.survey_option_five:
                surveyChoice = getString(R.string.survey_option_five_value);
                break;
            case R.id.okButton:
                view.setBackgroundResource(R.drawable.button_sm_selected);

                // Make sure the current viewer has submitted an answer
                if (TextUtils.isEmpty(surveyChoice)) {
                    view.setBackgroundResource(R.drawable.button_sm_deselected);
                    return;
                }
                mCurrentViewer.setSurveyAnswer(surveyChoice);
                saveCurrentViewer();
                mViewersIterator.remove();

                if (mViewersIterator.hasNext()) {
                    incrementViewer();
                    surveyChoice = "";
                    emailConfirmButton.setVisibility(View.VISIBLE);
                    emailQuestion.setVisibility(View.VISIBLE);
                    emailTextView.setVisibility(View.VISIBLE);
                    mSurveyTitle.setVisibility(View.GONE);
                    surveyLayout.setVisibility(View.GONE);
                    okButton.setVisibility(View.GONE);
                } else {
                    // Get the session id from the current viewer
                    saveSession(mCurrentViewer.getSessionId());
                    mCallback.onSurveyFinished();
                }
                break;
            case R.id.email_confirm_button:
                mCurrentViewer.setEmail(emailTextView.getText().toString());
                Log.v("tag", mCurrentViewer.getEmail());
                emailConfirmButton.setVisibility(View.GONE);
                emailQuestion.setVisibility(View.GONE);
                emailTextView.setVisibility(View.GONE);
                mSurveyTitle.setVisibility(View.VISIBLE);
                surveyLayout.setVisibility(View.VISIBLE);
                okButton.setVisibility(View.VISIBLE);

        }
    }

    // @todo move this into the Session model
    private void saveSession(long sessionId) {

        // Finalize the session session
        // @todo move getWritableDatabase() into AsyncTask
        SQLiteDatabase database = new DatabaseHelper(getContext()).getWritableDatabase();
        // End time values
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.SessionEntry.COLUMN_END_TIME, new Date().toString());
        // Selection criteria
        String selection = DatabaseContract.SessionEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(sessionId)};

        int count = database.update(
                DatabaseContract.SessionEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );
        Log.d(FRAGMENT_TAG, "Just updated " + Integer.toString(count) + " session row, id is " + Long.toString(sessionId));
        database.close();
    }

    // @todo move into Viewer model
    private void saveCurrentViewer() {
        // Save the current viewer to the db
        // @todo move getWritableDatabase() into AsyncTask
        SQLiteDatabase database = new DatabaseHelper(getContext()).getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ViewerEntry.COLUMN_NAME, mCurrentViewer.getName());
        values.put(DatabaseContract.ViewerEntry.COLUMN_EMAIL, mCurrentViewer.getEmail());
        values.put(DatabaseContract.ViewerEntry.COLUMN_SESSION_ID, mCurrentViewer.getSessionId());
        values.put(DatabaseContract.ViewerEntry.COLUMN_SURVEY_ANSWER, mCurrentViewer.getSurveyAnswer());

        long newRowId;
        newRowId = database.insert(
                DatabaseContract.ViewerEntry.TABLE_NAME,
                DatabaseContract.ViewerEntry.COLUMN_NULLABLE,
                values
        );

        database.close();
        Log.d(FRAGMENT_TAG, "Just inserted user id " + Long.toString(newRowId));
    }

    public void deselectAllButtons() {

        ViewGroup options = (ViewGroup) mLayout.findViewById(R.id.survey_options);
        for (int i = 0, ii = options.getChildCount(); i < ii; i++) {
            View v = options.getChildAt(i);
            if (v instanceof Button) {
                v.setBackgroundResource(R.drawable.button_med_deselected);
            }
        }
        okButton.setBackgroundResource(R.drawable.button_sm_deselected);
        emailConfirmButton.setBackgroundResource(R.drawable.button_sm_deselected);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mViewers != null) {
            // We were not able to start this download yet (waiting for a permission). Save this download
            // so that we can start it once we get restored and receive the permission.
            outState.putParcelableArrayList("mViewers", mViewers);
        }
        if (mCurrentViewer != null) {
            outState.putParcelable("mCurrentViewer", mCurrentViewer);
        }
    }

    // Container Activity must implement this interface
    public interface OnSurveyFinishedListener {
        void onSurveyFinished();
    }

}
