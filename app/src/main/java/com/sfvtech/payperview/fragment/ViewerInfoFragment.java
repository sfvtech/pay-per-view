package com.sfvtech.payperview.fragment;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mobsandgeeks.saripaar.Rule;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.Regex;
import com.mobsandgeeks.saripaar.annotation.Required;
import com.mobsandgeeks.saripaar.annotation.TextRule;
import com.sfvtech.payperview.MainActivity;
import com.sfvtech.payperview.R;
import com.sfvtech.payperview.Viewer;
import com.sfvtech.payperview.ViewerSurvey;
import com.sfvtech.payperview.database.DatabaseContract;
import com.sfvtech.payperview.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.Date;


public class ViewerInfoFragment extends Fragment implements Validator.ValidationListener {

    // Constants
    public static final String EXTRA_VIEWERS = ViewerSurvey.PACKAGE + ":EXTRA_VIEWERS";
    public static final String LOG_TAG = "ViewerInfoActivity";
    public static final String FRAGMENT_TAG = "viewer-info";

    // UI References
    @Required(order = 1, messageResId = R.string.message_required)
    @TextRule(order = 2, minLength = 2, messageResId = R.string.message_name_too_short)

    //@todo make spaces valid in names.  Rename field hint to First Name
    @Regex(order = 3, patternResId = R.string.pattern_name, messageResId = R.string.message_invalid_name)
    EditText mNameEditText;
    @Required(order = 4, messageResId = R.string.message_required)
    @Email(order = 5, messageResId = R.string.message_invalid_email)
    EditText mEmailEditText;
    ViewerInfoFragment.OnViewerInfoSubmittedListener mCallback;

    // Attributes
    private int mNViewers;
    private ArrayList<Viewer> mViewers;
    private Validator mValidator;
    private TextView mViewerSalutation;
    private long mSessionId;
    private Button okButton;
    private Viewer editViewer;
    private boolean editing;
    private boolean adding;
    private int MAX_VIEWERS;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_viewer_info, container, false);

        Bundle args = getArguments();

        String installationId = MainActivity.ID;

        // Create a new session
        // @todo move getWritableDatabase() into AsyncTask
        SQLiteDatabase database = new DatabaseHelper(getContext()).getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseContract.SessionEntry.COLUMN_START_TIME, new Date().toString());
        String locale = getResources().getConfiguration().locale.toString();
        values.put(DatabaseContract.SessionEntry.COLUMN_LOCALE, locale);

        values.put(DatabaseContract.SessionEntry.COLUMN_LAT, MainActivity.longitude);
        values.put(DatabaseContract.SessionEntry.COLUMN_LONG, MainActivity.latitude);

        long newRowId = database.insert(
                DatabaseContract.SessionEntry.TABLE_NAME,
                DatabaseContract.SessionEntry.COLUMN_NULLABLE,
                values
        );
        database.close();

        mSessionId = newRowId;

        // Extras
        mNViewers = getArguments().getInt("nViewers");
        mViewers = new ArrayList<Viewer>();

        // UI References
        mViewerSalutation = (TextView) view.findViewById(R.id.viewerSalutation);
        mViewerSalutation.setText(getString(R.string.viewer_info_salutation) + " 1,");
        mNameEditText = (EditText) view.findViewById(R.id.nameEditText);
        mEmailEditText = (EditText) view.findViewById(R.id.emailEditText);

        // Make sure soft keyboard pops up when Name has focus
        mNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (hasFocus) {
                    imm.showSoftInput(mNameEditText, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });
        mNameEditText.requestFocus();

        if (args.containsKey("Viewer")) {
            editing = true;
            editViewer = args.getParcelable("Viewer");
            if (editViewer.getEmail() != null) {
                mEmailEditText.setText(editViewer.getEmail());
            }
            if (editViewer.getName() != null) {
                mNameEditText.setText(editViewer.getName());
            }
        }

        if (args.containsKey("mViewers")) {
            if (!args.containsKey("Viewer")) {
                adding = true;
            }
            mViewers = getArguments().getParcelableArrayList("mViewers");
        }

        if (args.containsKey("MAX_VIEWERS")) {
            MAX_VIEWERS = args.getInt("MAX_VIEWERS");
        }

        // Validator
        mValidator = new Validator(this);
        mValidator.setValidationListener(this);

        okButton = (Button) view.findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setBackgroundResource(R.drawable.button_sm_selected);
                mValidator.validate();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (ViewerInfoFragment.OnViewerInfoSubmittedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnViewerInfoSubmittedListener");
        }
    }

    private void getViewerInfo(ArrayList<Viewer> mViewers) {
        mCallback.onViewerInfoSubmitted(mViewers);
    }

    @Override
    public void onValidationSucceeded() {
        mViewers.add(getViewer());
        if (editing) {
            Fragment editViewerInfo = new EditViewersFragment();
            Bundle args = new Bundle();
            args.putParcelable("editedViewer", getViewer());
            args.putParcelableArrayList("mViewers", mViewers);
            args.putInt("MAX_VIEWERS", MAX_VIEWERS);
            editViewerInfo.setArguments(args);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.container, editViewerInfo);
            ft.commit();
            editing = false;
        } else if (adding) {
            Fragment editViewerInfo = EditViewersFragment.create(mViewers, MAX_VIEWERS);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.container, editViewerInfo);
            ft.commit();
            adding = false;
        } else if (mViewers.size() == mNViewers) {
            getViewerInfo(mViewers);
        } else {
            clearForm();
            okButton.setBackgroundResource(R.drawable.button_sm_deselected);
        }
    }

    @Override
    public void onValidationFailed(View failedView, Rule<?> failedRule) {
        // Reset the OK button's appearance
        okButton.setBackgroundResource(R.drawable.button_sm_deselected);
        if (failedView instanceof EditText) {
            ((EditText) failedView).setError(failedRule.getFailureMessage());
            failedView.requestFocus();
        }
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

    // Container Activity must implement this interface
    public interface OnViewerInfoSubmittedListener {
        void onViewerInfoSubmitted(ArrayList<Viewer> mViewers);
    }
}
