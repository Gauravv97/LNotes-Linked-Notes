package com.links.gaurav.lnotes;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SeekBarPreference;

/**
 * Created by Gaurav on 3/2/2018.
 */

public class SettingsFragment extends PreferenceFragmentCompat {
    public static final String KEY_PREF_SCANNINGMODE = "pref_scanningType";
    public static final String KEY_PREF_SCANQUALITY="pref_scannedQuality";


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.pref_general);
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        ListPreference listPreference=(ListPreference)findPreference(KEY_PREF_SCANNINGMODE);
        listPreference.setDefaultValue(sharedPref.getString(KEY_PREF_SCANNINGMODE,"3"));
        final SeekBarPreference seekBarPreference=(SeekBarPreference)findPreference(KEY_PREF_SCANQUALITY);
        seekBarPreference.setMin(10);
        seekBarPreference.setMax(100);
        seekBarPreference.setSeekBarIncrement(10);
        seekBarPreference.setDefaultValue(sharedPref.getInt(KEY_PREF_SCANQUALITY,10));
        seekBarPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                seekBarPreference.setValue((int)newValue);
                return false;
            }

        });
    }
}
