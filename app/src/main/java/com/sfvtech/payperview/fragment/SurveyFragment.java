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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mobsandgeeks.saripaar.Rule;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.Required;
import com.sfvtech.payperview.R;
import com.sfvtech.payperview.ViewHelper;
import com.sfvtech.payperview.Viewer;
import com.sfvtech.payperview.database.DatabaseContract;
import com.sfvtech.payperview.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.ListIterator;


/**
 * A simple {@link Fragment} subclass.
 */
public class SurveyFragment extends Fragment implements View.OnClickListener, Validator.ValidationListener {

    public static final String FRAGMENT_TAG = "SurveyFragment";
    private static final long MAGIC_BUTTON_MAX_MS = 2000; // milliseconds
    SurveyFragment.OnSurveyFinishedListener mCallback;
    Date mTimerStart;
    int mLastButtonIndex = -1;
    private ArrayList<Viewer> mViewers;
    private ListIterator<Viewer> mViewersIterator;
    private Viewer mCurrentViewer;
    private RelativeLayout mLayout;
    private LinearLayout surveyLayout;
    private TextView mSurveyTitle;
    private String surveyChoice = "";
    private Button okButton;
    private Button surveyOption1;
    private Button surveyOption2;
    private Button surveyOption3;
    private Button surveyOption4;
    private Validator mValidator;
    private Button surveyOption5;
    private Button emailConfirmButton;
    private boolean currentViewerValidatedEmail;

    @Required(order = 4, messageResId = R.string.message_required)
    @Email(order = 5, messageResId = R.string.message_invalid_email)
    private EditText emailTextView;

    private TextView emailQuestion;
    private Button button1;
    private Button button2;
    private Button button3;

    public SurveyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_survey, container, false);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("mViewers")) {
                mViewers = savedInstanceState.getParcelableArrayList("mViewers");
            }
            if (savedInstanceState.containsKey("mCurrentViewer")) {
                mCurrentViewer = savedInstanceState.getParcelable("mCurrentViewer");
            }
            if (savedInstanceState.containsKey("currentViewerValidatedEmail")) {
                currentViewerValidatedEmail = savedInstanceState.getBoolean("currentViewerValidatedEmail");
            }
        } else {
            Bundle args = getArguments();
            if (args.containsKey("mViewers")) {
                mViewers = args.getParcelableArrayList("mViewers");
            }
            if (args.containsKey("mCurrentViewer")) {
                mCurrentViewer = args.getParcelable("mCurrentViewer");
            }
            if (args.containsKey("currentViewerValidatedEmail")) {
                currentViewerValidatedEmail = args.getBoolean("currentViewerValidatedEmail");
            }
        }

        button1 = v.findViewById(R.id.button1);
        button2 = v.findViewById(R.id.button2);
        button3 = v.findViewById(R.id.button3);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);

        // Validator
        mValidator = new Validator(this);
        mValidator.setValidationListener(this);

        // UI references
        mLayout = v.findViewById(R.id.my_layout);

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
        if (!mViewers.isEmpty()) {
            incrementViewer();
        } else {
            // TODO restart?
        }

        return v;
    }

    @Override
    public void onValidationSucceeded() {
        Log.v("tag", "Validated");
        mCurrentViewer.setEmail(emailTextView.getText().toString());
        currentViewerValidatedEmail = true;
        Log.v("tag", mCurrentViewer.getEmail());
        emailConfirmButton.setVisibility(View.GONE);
        emailQuestion.setVisibility(View.GONE);
        emailTextView.setVisibility(View.GONE);
        mSurveyTitle.setVisibility(View.VISIBLE);
        surveyLayout.setVisibility(View.VISIBLE);
        okButton.setVisibility(View.VISIBLE);
    }

    private void middleSurvey() {
        emailConfirmButton.setVisibility(View.GONE);
        emailQuestion.setVisibility(View.GONE);
        emailTextView.setVisibility(View.GONE);
        mSurveyTitle.setVisibility(View.VISIBLE);
        surveyLayout.setVisibility(View.VISIBLE);
        okButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onValidationFailed(View failedView, Rule<?> failedRule) {
        currentViewerValidatedEmail = false;
        // Reset the OK button's appearance
        emailConfirmButton.setBackgroundResource(R.drawable.button_sm_deselected);
        if (failedView instanceof EditText) {
            ((EditText) failedView).setError(failedRule.getFailureMessage());
            failedView.requestFocus();
        }
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

        if (currentViewerValidatedEmail) {
            Log.v("SurveyFrag", "Coming back after email");
            middleSurvey();
        } else {
            // Replace the name placeholder in the title
            Resources res = getResources();
            emailTextView.setText(mCurrentViewer.getEmail());
            emailQuestion.setText(String.format(res.getString(R.string.correct_email_confirmation), mCurrentViewer.getName()));
            mSurveyTitle.setText(String.format(res.getString(R.string.survey_question), mCurrentViewer.getName()));

            deselectAllButtons();
        }
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
                    currentViewerValidatedEmail = false;
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
                mValidator.validate();
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
                        if (mViewers != null) {
                            args.putParcelableArrayList("mViewers", mViewers);
                        }
                        if (mCurrentViewer != null) {
                            args.putParcelable("mCurrentViewer", mCurrentViewer);
                        }
                        args.putBoolean("currentViewerValidatedEmail", currentViewerValidatedEmail);
                        ViewHelper.startAdminFragment(getContext(), FRAGMENT_TAG, args);
                    }
                }
                // Reset the button tracker
                mLastButtonIndex = -1;
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
        emailConfirmButton.setBackgroundResource(R.drawable.button_sm_deselected);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mViewers != null) {
            outState.putParcelableArrayList("mViewers", mViewers);
        }
        if (mCurrentViewer != null) {
            outState.putParcelable("mCurrentViewer", mCurrentViewer);
        }
        outState.putBoolean("currentViewerValidatedEmail", currentViewerValidatedEmail);
    }

    // Container Activity must implement this interface
    public interface OnSurveyFinishedListener {
        void onSurveyFinished();
    }

}
