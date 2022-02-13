package com.fbspiele.fbspieleapp;

import android.content.SharedPreferences;
import android.database.CrossProcessCursor;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.preference.EditTextPreference;
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
            Preference prefHeadersTechnical = findPreference(getString(R.string.settings_headersKey_technical));
            if(prefHeadersTechnical!=null){
                prefHeadersTechnical.setOnPreferenceClickListener(preference -> {
                    fragmentManager
                            .beginTransaction()
                            .replace(R.id.settings, new TechnicalSettingsFragment())
                            .addToBackStack(null)
                            .commit();
                    return false;
                });
            }
        }
    }


    public static class TechnicalSettingsFragment extends PreferenceFragmentCompat {
        int a = 0;

        EditTextPreference portPref;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.pref_technicalsettings, rootKey);
            final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());

            portPref = findPreference(getString(R.string.settings_key_port));
            assert portPref != null;
            portPref.setText(sharedPref.getString(getString(R.string.settings_key_port),getString(R.string.settings_default_port)));
            portPref.setOnPreferenceChangeListener((preference, newValue) -> {
                portPref.setText(newValue.toString());
                sharedPref.edit().putString(getString(R.string.settings_key_port),newValue.toString()).apply();
                return false;
            });

        }
    }

    public static class UserSettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.pref_usersettings, rootKey);
            final Crypto settingsEncryptionCrypto = new Crypto(getString(R.string.settings_settingsEncryption_password),getString(R.string.settings_settingsEncryption_salt));
            final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());

            EditTextPreference passwordPref = findPreference(getString(R.string.settings_key_password));
            assert passwordPref != null;
            if(sharedPref.getString(getString(R.string.settings_encryptedPassword_pref_key),"").length()!=0){
                passwordPref.setText("***");
            }
            passwordPref.setOnPreferenceChangeListener((preference, newValue) -> {
                if(!newValue.equals("")){
                    passwordPref.setText("***");
                    String encryptedPassword = settingsEncryptionCrypto.encryptHex(newValue.toString());
                    sharedPref.edit().putString(getString(R.string.settings_encryptedPassword_pref_key),encryptedPassword).apply();
                }
                return false;
            });

            EditTextPreference saltPref = findPreference(getString(R.string.settings_key_salt));
            assert saltPref != null;
            if(sharedPref.getString(getString(R.string.settings_encryptedSalt_pref_key),"").length()!=0){
                saltPref.setText("***");
            }
            saltPref.setOnPreferenceChangeListener((preference, newValue) -> {
                if(!newValue.equals("")){
                    saltPref.setText("***");
                    String encryptedSalt = settingsEncryptionCrypto.encryptHex(newValue.toString());
                    sharedPref.edit().putString(getString(R.string.settings_encryptedSalt_pref_key),encryptedSalt).apply();
                }
                return false;
            });

            EditTextPreference ipPref = findPreference(getString(R.string.settings_key_ipAddress));
            assert ipPref != null;
            ipPref.setText(sharedPref.getString(getString(R.string.settings_key_ipAddress),getString(R.string.settings_default_ipAddress)));
            ipPref.setOnPreferenceChangeListener((preference, newValue) -> {
                ipPref.setText(newValue.toString());
                sharedPref.edit().putString(getString(R.string.settings_key_ipAddress),newValue.toString()).apply();
                return false;
            });

            Preference colorPref = findPreference(getString(R.string.settings_key_color));
            assert colorPref != null;
            colorPref.setOnPreferenceClickListener(preference -> {
                // todo
                return false;
            });
        }

    }



}