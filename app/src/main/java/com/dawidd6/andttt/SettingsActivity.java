package com.dawidd6.andttt;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.WindowManager;

public class SettingsActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean isNightModeEnabled = getIntent().getBooleanExtra("night_mode", false);
        boolean isStatusBarEnabled = getIntent().getBooleanExtra("show_status_bar", false);

        if(isStatusBarEnabled)
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setTheme(isNightModeEnabled ? R.style.theme_dark : R.style.theme_light);

        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch(key) {
            case "night_mode":
                getIntent().putExtra("night_mode", sharedPreferences.getBoolean("night_mode", false));
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        recreate();
                    }
                }, 250);
                break;
            case "show_status_bar":
                if(sharedPreferences.getBoolean("show_status_bar", false))
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                else
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                break;
        }
    }

    @Override
    public void onBackPressed()
    {
        onNavigateUp();
    }
}