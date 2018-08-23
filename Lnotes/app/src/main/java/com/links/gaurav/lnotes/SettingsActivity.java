package com.links.gaurav.lnotes;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Gaurav on 3/2/2018.
 */

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SettingsFragment settingsFragment=new SettingsFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, settingsFragment)
                .commit();


        getSupportActionBar().setTitle(R.string.action_settings);
        getSupportActionBar().setBackgroundDrawable(getDrawable(android.R.color.holo_green_dark));

    }
}
