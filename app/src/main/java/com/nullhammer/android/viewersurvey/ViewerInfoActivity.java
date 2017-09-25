package com.nullhammer.android.viewersurvey;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.mobsandgeeks.saripaar.Rule;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.Regex;
import com.mobsandgeeks.saripaar.annotation.Required;
import com.mobsandgeeks.saripaar.annotation.TextRule;
import com.nullhammer.ViewerSurvey;
import com.nullhammer.data.DatabaseContract;
import com.nullhammer.data.DatabaseHelper;
import com.nullhammer.viewersurvey.models.Viewer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ViewerInfoActivity extends Activity implements Validator.ValidationListener {
    // Constants
    public static final String EXTRA_VIEWERS = ViewerSurvey.PACKAGE + ":EXTRA_VIEWERS";
    public static final String LOG_TAG = "ViewerInfoActivity";

    // Attributes
    private int mNViewers;
    private List<Viewer> mViewers;
    private Validator mValidator;
    private TextView mViewerSalutation;
    private long mSessionId;

    // UI References
    @Required(order = 1, messageResId = R.string.message_required)
    @TextRule(order = 2, minLength = 2, messageResId = R.string.message_name_too_short)
    //@todo make spaces valid in names.  Rename field hint to First Name
    @Regex(order = 3, patternResId = R.string.pattern_name, messageResId = R.string.message_invalid_name)
    EditText mNameEditText;

    @Required(order = 4, messageResId = R.string.message_required)
    @Email(order = 5, messageResId = R.string.message_invalid_email)
    EditText mEmailEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set full screen
        ActionBar actionBar = getActionBar();
        actionBar.hide();

        LayoutInflater inflater = (LayoutInflater) getSystemService(this.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.activity_viewer_info, null);
        setContentView(layout);

        // Create a new session
        // @todo move getWritableDatabase() into AsyncTask
        SQLiteDatabase database = new DatabaseHelper(this).getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseContract.SessionEntry.COLUMN_START_TIME, new Date().toString());
        String locale = getResources().getConfiguration().locale.toString();
        values.put(DatabaseContract.SessionEntry.COLUMN_LOCALE, locale);
        long newRowId = database.insert(
                DatabaseContract.SessionEntry.TABLE_NAME,
                DatabaseContract.SessionEntry.COLUMN_NULLABLE,
                values
        );
        database.close();

        mSessionId = newRowId;

        // Extras
        mNViewers = getIntent().getExtras().getInt(ViewerNumberActivity.EXTRA_N_VIEWERS, 1);
        mViewers = new ArrayList<Viewer>();

        // UI References
        mViewerSalutation = (TextView) findViewById(R.id.viewerSalutation);
        mViewerSalutation.setText(getString(R.string.viewer_info_salutation) + " 1,");
        mNameEditText = (EditText) findViewById(R.id.nameEditText);
        mEmailEditText = (EditText) findViewById(R.id.emailEditText);

        // Make sure soft keyboard pops up when Name has focus
        mNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if(hasFocus) {
                    imm.showSoftInput(mNameEditText, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });
        mNameEditText.requestFocus();

        // Validator
        mValidator = new Validator(this);
        mValidator.setValidationListener(this);

        // Magic Menu Buttons
        ViewHelper.addMagicMenuButtons(layout);

    }

    @Override
    public void onValidationSucceeded() {
        mViewers.add(getViewer());
        if (mViewers.size() == mNViewers) {
            goToVideoActivity();
        } else {
            clearForm();
            findViewById(R.id.okButton).setBackgroundResource(R.drawable.button_sm_deselected);
        }
    }

    @Override
    public void onValidationFailed(View failedView, Rule<?> failedRule) {
        // Reset the OK button's appearance
        findViewById(R.id.okButton).setBackgroundResource(R.drawable.button_sm_deselected);
        if (failedView instanceof EditText) {
            ((EditText) failedView).setError(failedRule.getFailureMessage());
            failedView.requestFocus();
        }
    }

    public void okButtonHandler(View view) {
        view.setBackgroundResource(R.drawable.button_sm_selected);
        mValidator.validate();
    }

    private Viewer getViewer() {
        String name = mNameEditText.getText().toString().trim();
        String email = mEmailEditText.getText().toString().trim();

        return new Viewer(name, email, mSessionId);
    }

    private void clearForm() {
        // Increment the viewer counter
        mViewerSalutation.setText(getString(R.string.viewer_info_salutation) + " " + Integer.toString(mViewers.size() + 1) + ",");
        mNameEditText.setText(null);
        mEmailEditText.setText(null);
        mNameEditText.requestFocus();
    }

    private void goToVideoActivity() {
        Intent intent = new Intent(this, VideoActivity.class);
        intent.putExtra(EXTRA_VIEWERS, (Serializable) mViewers);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }
}
