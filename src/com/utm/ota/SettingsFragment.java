package com.utbm.ota;

import com.utbm.ota.R;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment{

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        // Create the Setting fragment from its xml definition
        addPreferencesFromResource(R.xml.settings);
    }
}
