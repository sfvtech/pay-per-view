package com.sfvtech.payperview.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
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
    private ArrayList<Viewer> mViewers;
    private ViewerAdapter myAdapter;

    public EditViewersFragment() {
        // Required empty public constructor
    }

    public static EditViewersFragment create(ArrayList<Viewer> mViewers) {
        final Bundle arguments = new Bundle();
        arguments.putParcelableArrayList("mViewers", mViewers);
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

        mViewers = getArguments().getParcelableArrayList("mViewers");

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_edit_viewers, container, false);

        // Inflate list of viewers
        listView = v.findViewById(R.id.list);
        myAdapter = new ViewerAdapter(getContext(), mViewers);
        listView.setAdapter(myAdapter);

        addNewButton = (Button) v.findViewById(R.id.addNew);
        addNewButton.setOnClickListener(this);
        confirmButton = (Button) v.findViewById(R.id.confirm);
        confirmButton.setOnClickListener(this);
        restartButton = (Button) v.findViewById(R.id.restartButton);
        restartButton.setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addNew:
                final Fragment viewerInfoFragment = new ViewerInfoFragment();
                Bundle args = new Bundle();
                args.putParcelableArrayList("mViewers", mViewers);
                viewerInfoFragment.setArguments(args);
                ((AppCompatActivity) getContext()).getSupportFragmentManager().
                        beginTransaction().replace(R.id.container, viewerInfoFragment, ViewerInfoFragment.FRAGMENT_TAG).
                        commit();
                break;
            case R.id.confirm:
                mCallback.onEditViewersFinished(mViewers);
                break;
            case R.id.restartButton:
                final Fragment viewerNumberFragment = new ViewerNumberFragment();
                Bundle restartArgs = new Bundle();
                restartArgs.putParcelableArrayList("mViewers", new ArrayList<Viewer>());
                viewerNumberFragment.setArguments(restartArgs);
                ((AppCompatActivity) getContext()).getSupportFragmentManager().
                        beginTransaction().replace(R.id.container, viewerNumberFragment, ViewerInfoFragment.FRAGMENT_TAG).
                        commit();
            default:
                break;
        }
    }

    // Container Activity must implement this interface
    public interface onEditViewersFinishedListener {
        void onEditViewersFinished(ArrayList<Viewer> mViewers);
    }

}
