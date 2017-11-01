package com.sfvtech.payperview.fragment;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
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
import com.sfvtech.payperview.ViewHelper;
import com.sfvtech.payperview.Viewer;
import com.sfvtech.payperview.database.DatabaseContract;
import com.sfvtech.payperview.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.Date;


public class ViewerInfoFragment extends Fragment implements Validator.ValidationListener, View.OnClickListener {

    // Constants
    public static final String FRAGMENT_TAG = "viewer-info";
    private static final long MAGIC_BUTTON_MAX_MS = 2000; // milliseconds
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
    Date mTimerStart;
    int mLastButtonIndex = -1;
    // Attributes
    private int nViewers;
    private ArrayList<Viewer> mViewers;
    private Validator mValidator;
    private TextView mViewerSalutation;
    private long mSessionId;
    private Button okButton;
    private Viewer editViewer;
    private boolean editing;
    private int MAX_VIEWERS;
    private String fragmentTag;
    private Bundle args;
    private String savedName;
    private String savedEmail;
    private Button button1;
    private Button button2;
    private Button button3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_viewer_info, container, false);

        args = getArguments();

        String installationId = MainActivity.ID;

        // Create a new session
        // @todo move getWritableDatabase() into AsyncTask
        final SQLiteDatabase database = new DatabaseHelper(getContext()).getWritableDatabase();

        final ContentValues values = new ContentValues();
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
        if (args.containsKey("nViewers")) {
            nViewers = getArguments().getInt("nViewers");
        }
        mViewers = new ArrayList<Viewer>();

        // UI References
        mViewerSalutation = (TextView) view.findViewById(R.id.viewerSalutation);
        mViewerSalutation.setText(getString(R.string.viewer_info_salutation) + " 1,");
        mNameEditText = (EditText) view.findViewById(R.id.nameEditText);
        mEmailEditText = (EditText) view.findViewById(R.id.emailEditText);

        if (savedInstanceState != null) {
            nViewers = savedInstanceState.getInt("nViewers");
            editViewer = savedInstanceState.getParcelable("Viewer");
            MAX_VIEWERS = savedInstanceState.getInt("MAX_VIEWERS");
            fragmentTag = savedInstanceState.getString("fragmentTag");
            mViewers = savedInstanceState.getParcelableArrayList("mViewers");
            editing = savedInstanceState.getBoolean("editing");
            savedName = savedInstanceState.getString("savedName");
            savedEmail = savedInstanceState.getString("savedEmail");
        }

        if (!TextUtils.isEmpty(savedName)) {
            mNameEditText.setText(savedName);
        }

        if (!TextUtils.isEmpty(savedEmail)) {
            mEmailEditText.setText(savedEmail);
        }

        if (args.containsKey("Viewer")) {
            editViewer = args.getParcelable("Viewer");
            if (editViewer.getEmail() != null) {
                mEmailEditText.setText(editViewer.getEmail());
            }
            if (editViewer.getName() != null) {
                mNameEditText.setText(editViewer.getName());
            }
        }

        if (args.containsKey("editing")) {
            if (args.getBoolean("editing")) {
                editing = true;
            }
        }

        if (args.containsKey("mViewers")) {
            mViewers = getArguments().getParcelableArrayList("mViewers");
        }

        if (args.containsKey("MAX_VIEWERS")) {
            MAX_VIEWERS = args.getInt("MAX_VIEWERS");
        }

        if (args.containsKey("fragmentTag")) {
            fragmentTag = args.getString("fragmentTag");
        }

        if (args.containsKey("nViewers")) {
            nViewers = args.getInt("nViewers");
        }

        // Validator
        mValidator = new Validator(this);
        mValidator.setValidationListener(this);


        okButton = (Button) view.findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mValidator.validate();
            }
        });

        button1 = view.findViewById(R.id.button1);
        button2 = view.findViewById(R.id.button2);
        button3 = view.findViewById(R.id.button3);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
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

    private void getViewerInfo(ArrayList<Viewer> mViewers, boolean completed) {
        mCallback.onViewerInfoSubmitted(mViewers, completed);
    }

    @Override
    public void onValidationSucceeded() {
        Log.v("tag", "Validated");
        mViewers.add(getViewer());
        final Bundle args = new Bundle();
        args.putParcelable("editedViewer", getViewer());
        args.putParcelableArrayList("mViewers", mViewers);
        args.putInt("MAX_VIEWERS", MAX_VIEWERS);
        args.putString("fragmentTag", fragmentTag);
        args.putInt("nViewers", nViewers);
        if (editing) {
            Log.v("ViewerInfo", "editing");
            final Fragment editViewerInfo = new EditViewersFragment();
            editViewerInfo.setArguments(args);
            final FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.container, editViewerInfo);
            ft.commit();
            editing = false;
        } else if (mViewers.size() == nViewers) {
            getViewerInfo(mViewers, true);
        } else {
            getViewerInfo(mViewers, false);
            clearForm();
        }
    }

    @Override
    public void onValidationFailed(View failedView, Rule<?> failedRule) {
        if (failedView instanceof EditText) {
            ((EditText) failedView).setError(failedRule.getFailureMessage());
            failedView.requestFocus();
        }
    }

    private Viewer getViewer() {
        final String name = mNameEditText.getText().toString().trim();
        final String email = mEmailEditText.getText().toString().trim();

        return new Viewer(name, email, mSessionId);
    }

    private void clearForm() {
        // Increment the viewer counter
        mViewerSalutation.setText(getString(R.string.viewer_info_salutation) + " " + Integer.toString(mViewers.size() + 1) + ",");
        mNameEditText.setText(null);
        mEmailEditText.setText(null);
        mNameEditText.requestFocus();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (editViewer != null) {
            outState.putParcelable("Viewer", editViewer);
        }
        outState.putInt("MAX_VIEWERS", MAX_VIEWERS);
        if (fragmentTag != null) {
            outState.putString("fragmentTag", fragmentTag);
            Log.v("ViewerInfo:fragmentTag", fragmentTag);
        }
        if (mViewers != null) {
            outState.putParcelableArrayList("mViewers", mViewers);
        }
        outState.putString("savedName", mNameEditText.getText().toString());
        outState.putString("savedEmail", mEmailEditText.getText().toString());
        outState.putBoolean("editing", editing);
        outState.putInt("nViewers", nViewers);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
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
                        if (editViewer != null) {
                            args.putParcelable("Viewer", editViewer);
                        }
                        args.putInt("MAX_VIEWERS", MAX_VIEWERS);
                        if (fragmentTag != null) {
                            args.putString("fragmentTag", fragmentTag);
                            Log.v("ViewerInfo:fragmentTag", fragmentTag);
                        }
                        if (mViewers != null) {
                            args.putParcelableArrayList("mViewers", mViewers);
                        }
                        args.putString("savedName", mNameEditText.getText().toString());
                        args.putString("savedEmail", mEmailEditText.getText().toString());
                        args.putBoolean("editing", editing);
                        args.putInt("nViewers", nViewers);
                        ViewHelper.startAdminFragment(getContext(), FRAGMENT_TAG, args);
                    }
                }
                // Reset the button tracker
                mLastButtonIndex = -1;
                break;
        }
    }

    // Container Activity must implement this interface
    public interface OnViewerInfoSubmittedListener {
        void onViewerInfoSubmitted(ArrayList<Viewer> mViewers, boolean completed);
    }
}
