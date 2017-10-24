package com.sfvtech.payperview.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.sfvtech.payperview.R;
import com.sfvtech.payperview.Viewer;
import com.sfvtech.payperview.ViewerAdapter;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class EditViewersFragment extends Fragment implements View.OnClickListener {
    public static final String FRAGMENT_TAG = "EditViewersFragment";

    ListView listView;
    EditViewersFragment.onEditViewersFinishedListener mCallback;
    Button confirmButton;
    Button addNewButton;
    Button restartButton;
    Button backToAdminButton;
    int MAX_VIEWERS;
    private ArrayList<Viewer> mViewers;
    private ViewerAdapter myAdapter;
    private String fragmentTag;


    public EditViewersFragment() {
        // Required empty public constructor
    }

    public static EditViewersFragment create(ArrayList<Viewer> mViewers, int MAX_VIEWERS, String fragmentTag) {
        final Bundle arguments = new Bundle();
        arguments.putParcelableArrayList("mViewers", mViewers);
        arguments.putInt("MAX_VIEWERS", MAX_VIEWERS);
        arguments.putString("fragmentTag", fragmentTag);
        Log.v("CREATE MAX", MAX_VIEWERS + "");
        final EditViewersFragment fragment = new EditViewersFragment();
        fragment.setArguments(arguments);
        return fragment;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (EditViewersFragment.onEditViewersFinishedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement onEditViewersFinishedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            mViewers = savedInstanceState.getParcelableArrayList("mViewers");
            MAX_VIEWERS = savedInstanceState.getInt("MAX_VIEWERS");
        }

        if (getArguments().containsKey("mViewers")) {
            mViewers = getArguments().getParcelableArrayList("mViewers");
        }

        if (getArguments().containsKey("MAX_VIEWERS")) {
            MAX_VIEWERS = getArguments().getInt("MAX_VIEWERS");
        }
        if (getArguments().containsKey("fragmentTag")) {
            fragmentTag = getArguments().getString("fragmentTag");
        }

        Log.v("MAX ON CREATE", MAX_VIEWERS + "");

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_edit_viewers, container, false);

        // Inflate list of viewers
        listView = v.findViewById(R.id.list);
        myAdapter = new ViewerAdapter(getContext(), mViewers, MAX_VIEWERS);
        listView.setAdapter(myAdapter);

        confirmButton = (Button) v.findViewById(R.id.confirm);
        confirmButton.setOnClickListener(this);

        restartButton = (Button) v.findViewById(R.id.restartButton);
        restartButton.setOnClickListener(this);
        backToAdminButton = (Button) v.findViewById(R.id.backtoadminbutton);
        backToAdminButton.setOnClickListener(this);

        addNewButton = (Button) v.findViewById(R.id.addNew);

        if (mViewers.size() < MAX_VIEWERS) {
            addNewButton.setOnClickListener(this);
        } else {
            addNewButton.setVisibility(View.GONE);
        }

        // Only let adding before video
        if (!TextUtils.isEmpty(fragmentTag)) {
            switch (fragmentTag) {
                case VideoFragment.FRAGMENT_TAG:
                    addNewButton.setVisibility(View.GONE);
                    break;
                case SurveyFragment.FRAGMENT_TAG:
                    addNewButton.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }
        return v;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addNew:
                final Fragment viewerInfoFragment = new ViewerInfoFragment();
                final Bundle args = new Bundle();
                args.putParcelableArrayList("mViewers", mViewers);
                args.putInt("MAX_VIEWERS", MAX_VIEWERS);
                args.putString("fragmentTag", fragmentTag);
                args.putBoolean("adding", true);
                viewerInfoFragment.setArguments(args);
                ((AppCompatActivity) getContext()).getSupportFragmentManager().
                        beginTransaction().replace(R.id.container, viewerInfoFragment, ViewerInfoFragment.FRAGMENT_TAG).
                        commit();
                break;
            case R.id.confirm:
                mCallback.onEditViewersFinished(mViewers, fragmentTag);
                break;
            case R.id.restartButton:
                final Fragment viewerNumberFragment = new ViewerNumberFragment();
                final Bundle restartArgs = new Bundle();
                restartArgs.putParcelableArrayList("mViewers", new ArrayList<Viewer>());
                viewerNumberFragment.setArguments(restartArgs);
                ((AppCompatActivity) getContext()).getSupportFragmentManager().
                        beginTransaction().replace(R.id.container, viewerNumberFragment, ViewerNumberFragment.FRAGMENT_TAG).
                        commit();
                break;
            case R.id.backtoadminbutton:
                final Fragment adminFragment = new AdminFragment();
                final Bundle adminArgs = new Bundle();
                adminArgs.putParcelableArrayList("mViewers", mViewers);
                adminArgs.putInt("MAX_VIEWERS", MAX_VIEWERS);
                adminArgs.putString("fragmentTag", fragmentTag);
                adminFragment.setArguments(adminArgs);
                ((AppCompatActivity) getContext()).getSupportFragmentManager().
                        beginTransaction().replace(R.id.container, adminFragment, AdminFragment.FRAGMENT_TAG).
                        commit();
                break;
            default:
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("mViewers", mViewers);
        outState.putInt("MAX_VIEWERS", MAX_VIEWERS);
    }

    // Container Activity must implement this interface
    public interface onEditViewersFinishedListener {
        void onEditViewersFinished(ArrayList<Viewer> mViewers, String fragmentTag);
    }

}
