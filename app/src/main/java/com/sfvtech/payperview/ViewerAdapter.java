package com.sfvtech.payperview;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.sfvtech.payperview.fragment.ViewerInfoFragment;

import java.util.ArrayList;

public class ViewerAdapter extends BaseAdapter implements View.OnClickListener {

    private ArrayList<Viewer> viewerList = new ArrayList<Viewer>();
    private Context context;

    public ViewerAdapter(Context context, ArrayList<Viewer> viewers) {
        this.viewerList = viewers;
        this.context = context;
    }

    @Override
    public int getCount() {
        return viewerList.size();
    }

    @Override
    public Viewer getItem(int pos) {
        return viewerList.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return 0;
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Get the data item for this position
        Viewer viewer = (Viewer) getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.viewer_edit, null);
        }
        // Lookup view for data population
        final TextView viewerName = (TextView) convertView.findViewById(R.id.userName);
        final TextView viewerEmail = (TextView) convertView.findViewById(R.id.userEmail);
        final Button deleteViewer = (Button) convertView.findViewById(R.id.deleteUser);
        deleteViewer.setTag(position);
        final Button editViewer = (Button) convertView.findViewById(R.id.editUser);
        editViewer.setTag(position);
        deleteViewer.setOnClickListener(this);
        editViewer.setOnClickListener(this);
        // Populate the data into the template view using the data object
        viewerName.setText(viewer.getName());
        viewerEmail.setText(viewer.getEmail());
        // Return the completed view to render on screen
        return convertView;
    }

    @Override
    public void onClick(View view) {
        int position = (Integer) view.getTag();
        // Access the row position here to get the correct data item
        Viewer viewer = (Viewer) getItem(position);
        switch (view.getId()) {
            case R.id.deleteUser:
                viewerList.remove(position);
                notifyDataSetChanged();
                break;
            case R.id.editUser:
                viewerList.remove(position);
                notifyDataSetChanged();
                final Fragment viewerInfoFragment = new ViewerInfoFragment();
                Bundle args = new Bundle();
                args.putParcelableArrayList("mViewers", viewerList);
                args.putParcelable("Viewer", viewer);
                viewerInfoFragment.setArguments(args);
                ((AppCompatActivity) context).getSupportFragmentManager().
                        beginTransaction().replace(R.id.container, viewerInfoFragment, ViewerInfoFragment.FRAGMENT_TAG).
                        commit();
                break;
        }
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}
