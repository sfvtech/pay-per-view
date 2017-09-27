package com.sfvtech.payperview.fragment;

import android.os.Bundle;
import android.preference.ListPreference;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.sfvtech.payperview.R;

/**
 * A simple {@link Fragment} subclass.
 */

public class PreferenceFragment extends PreferenceFragmentCompat {

    private ListPreference mListPreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Indicate here the XML resource you created above that holds the preferences
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}


