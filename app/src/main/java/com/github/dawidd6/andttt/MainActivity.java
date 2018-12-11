package com.github.dawidd6.andttt;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.WindowManager;
import com.github.dawidd6.andttt.fragments.MenuFragment;

public class MainActivity extends Activity {
    public static boolean isNightModeEnabled;
    public static boolean isAnimationEnabled;
    public static boolean isStatusBarEnabled;
    public static boolean isMaximizationEnabled;
    public static int animation_duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        isStatusBarEnabled = prefs.getBoolean("show_status_bar", false);
        isNightModeEnabled = prefs.getBoolean("night_mode", false);
        isAnimationEnabled = prefs.getBoolean("animations", true);
        isMaximizationEnabled = prefs.getBoolean("maximization", true);

        if(isAnimationEnabled)
            animation_duration = getResources().getInteger(R.integer.animation_duration);
        else
            animation_duration = 0;

        if(isStatusBarEnabled)
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        else
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setTheme(isNightModeEnabled ? R.style.theme_dark : R.style.theme_light);
        setContentView(R.layout.activity);

        if(savedInstanceState != null)
            return;

        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        android.R.animator.fade_in,
                        android.R.animator.fade_out,
                        android.R.animator.fade_in,
                        android.R.animator.fade_out)
                .replace(R.id.placeholder, new MenuFragment(), MenuFragment.TAG)
                .commit();
    }
}
