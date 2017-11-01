package com.sfvtech.payperview;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.sfvtech.payperview.fragment.AdminFragment;

/**
 * Some useful methods for view manipulation
 */
public class ViewHelper {
    
    private static final String LOG_TAG = "ViewHelper";

    public static void startAdminFragment(Context context, String currentFragmentTag, Bundle adminArgs) {
        Log.v("View", "startAdminfrom" + currentFragmentTag);
        final Fragment adminFragment = new AdminFragment();
        adminArgs.putString(context.getString(R.string.fragmentTag), currentFragmentTag);
        adminFragment.setArguments(adminArgs);
        ((AppCompatActivity) context).getSupportFragmentManager().
                beginTransaction().replace(R.id.container, adminFragment, AdminFragment.FRAGMENT_TAG).
                commit();
    }
}
