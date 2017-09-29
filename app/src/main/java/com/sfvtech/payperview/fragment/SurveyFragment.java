package com.sfvtech.payperview.fragment;


import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sfvtech.payperview.R;
import com.sfvtech.payperview.Viewer;
import com.sfvtech.payperview.database.DatabaseContract;
import com.sfvtech.payperview.database.DatabaseHelper;

import java.util.Date;
import java.util.List;
import java.util.ListIterator;


/**
 * A simple {@link Fragment} subclass.
 */
public class SurveyFragment extends Fragment implements View.OnClickListener {

    public static final String FRAGMENT_TAG = "SurveyFragment";
    SurveyFragment.OnSurveyFinishedListener mCallback;
    private List<Viewer> mViewers;
    private ListIterator<Viewer> mViewersIterator;
    private Viewer mCurrentViewer;
    private LinearLayout mLayout;
    private TextView mSurveyTitle;
    private Button okButton;
    private Button surveyOption1;
    private Button surveyOption2;
    private Button surveyOption3;
    private Button surveyOption4;
    private Button surveyOption5;

    public SurveyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_survey, container, false);

        mViewers = getArguments().getParcelableArrayList("mViewers");

        // UI references
        mLayout = (LinearLayout) v.findViewById(R.id.my_layout);
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
        mSurveyTitle.setText(String.format(res.getString(R.string.survey_question), mCurrentViewer.getName()));

        deselectAllButtons();
    }

    @Override
    public void onClick(View view) {
        String choice = "";
        deselectAllButtons();
        view.setBackgroundResource(R.drawable.button_med_selected);
        mCurrentViewer.setSurveyAnswer(choice);
        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.survey_option_one:
                choice = getString(R.string.survey_option_one_value);
                break;
            case R.id.survey_option_two:
                choice = getString(R.string.survey_option_two_value);
                break;
            case R.id.survey_option_three:
                choice = getString(R.string.survey_option_three_value);
                break;
            case R.id.survey_option_four:
                choice = getString(R.string.survey_option_four_value);
                break;
            case R.id.survey_option_five:
                choice = getString(R.string.survey_option_five_value);
                break;
            case R.id.okButton:
                view.setBackgroundResource(R.drawable.button_sm_selected);

                // Make sure the current viewer has submitted an answer
                if ((mCurrentViewer).getSurveyAnswer() == null) {
                    view.setBackgroundResource(R.drawable.button_sm_deselected);
                    return;
                }

                saveCurrentViewer();

                if (mViewersIterator.hasNext()) {
                    incrementViewer();
                } else {
                    // Get the session id from the current viewer
                    saveSession(mCurrentViewer.getSessionId());
                    mCallback.onSurveyFinished();
                }
                break;
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
    }

    // Container Activity must implement this interface
    public interface OnSurveyFinishedListener {
        void onSurveyFinished();
    }

}
