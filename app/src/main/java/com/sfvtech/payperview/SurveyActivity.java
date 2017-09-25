package com.sfvtech.payperview;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Date;
import java.util.List;
import java.util.ListIterator;

public class SurveyActivity extends Activity {

    private List<Viewer> mViewers;
    private ListIterator<Viewer> mViewersIterator;
    private Viewer mCurrentViewer;

    private RelativeLayout mLayout;
    private TextView mSurveyTitle;

    public static final String LOG_TAG = "ViewerSurveyActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Full screen
        ActionBar actionBar = getActionBar();
        actionBar.hide();

        LayoutInflater inflater = (LayoutInflater) getSystemService(this.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.activity_survey, null);
        setContentView(layout);

        // Extras
        mViewers = (List<Viewer>) getIntent()
                .getExtras()
                .getSerializable(ViewerInfoActivity.EXTRA_VIEWERS);

        // UI references
        mLayout = (RelativeLayout) findViewById(R.id.my_layout);
        mSurveyTitle = (TextView) findViewById(R.id.survey_title);

        // Start iteration
        mViewersIterator = mViewers.listIterator(0);
        incrementViewer();

        // Magic Menu Buttons
        ViewHelper.addMagicMenuButtons(layout);
    }

    private void incrementViewer() {

        mLayout.requestFocus();
        mCurrentViewer = mViewersIterator.next();

        // Replace the name placeholder in the title
        Resources res = getResources();
        mSurveyTitle.setText(String.format(res.getString(R.string.survey_question), mCurrentViewer.getName()));

        deselectAllButtons();
    }

    public void onSurveyButtonClicked(View view) {
        String choice = "";
        deselectAllButtons();
        view.setBackgroundResource(R.drawable.button_med_selected);

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
        }
        mCurrentViewer.setSurveyAnswer(choice);
    }

    public void okButtonHandler(View view) {
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
            goToThankYouActivity();
        }
    }

    // @todo move this into the Session model
    private void saveSession(long sessionId) {

        // Finalize the session session
        // @todo move getWritableDatabase() into AsyncTask
        SQLiteDatabase database = new DatabaseHelper(this).getWritableDatabase();
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
        Log.d(LOG_TAG, "Just updated " + Integer.toString(count) + " session row, id is " + Long.toString(sessionId));
        database.close();
    }

    // @todo move into Viewer model
    private void saveCurrentViewer() {
        // Save the current viewer to the db
        // @todo move getWritableDatabase() into AsyncTask
        SQLiteDatabase database = new DatabaseHelper(this).getWritableDatabase();

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
        Log.d(LOG_TAG, "Just inserted user id " + Long.toString(newRowId));

    }

    public void deselectAllButtons() {

        ViewGroup options = (ViewGroup) mLayout.findViewById(R.id.survey_options);
        for (int i = 0, ii = options.getChildCount(); i < ii; i++) {
            View v = options.getChildAt(i);
            if (v instanceof Button) {
                v.setBackgroundResource(R.drawable.button_med_deselected);
            }
        }
        findViewById(R.id.okButton).setBackgroundResource(R.drawable.button_sm_deselected);
    }

    private void goToThankYouActivity() {

        Intent intent = new Intent(this, ThankYouActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }
}
