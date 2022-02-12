package com.fbspiele.fbspieleapp;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {


    FragmentManager fragmentManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        fragmentManager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment(fragmentManager))
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    // macht dass der backbutton oben links das selbe macht wie der physical back button
    @Override
    public boolean onSupportNavigateUp() {
        super.onBackPressed();
        return true;
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        FragmentManager fragmentManager;
        SettingsFragment (FragmentManager fragmentManager){
            this.fragmentManager = fragmentManager;
        }
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.pref_headers, rootKey);
            Preference prefHeadersUser = findPreference(getString(R.string.settings_headersKey_user));
            if(prefHeadersUser!=null){
                prefHeadersUser.setOnPreferenceClickListener(preference -> {
                    fragmentManager
                            .beginTransaction()
                            .replace(R.id.settings, new UserSettingsFragment())
                            .addToBackStack(null)
                            .commit();
                    return false;
                });
            }
        }
    }


    public static class UserSettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.pref_usersettings, rootKey);
        }

    }



}