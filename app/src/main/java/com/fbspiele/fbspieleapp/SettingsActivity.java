package com.fbspiele.fbspieleapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ResourceCursorAdapter;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.preference.DropDownPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

            SwitchPreference prefAutoReconnect = findPreference(getString(R.string.settings_key_autoReconnect));
            assert prefAutoReconnect != null;
            prefAutoReconnect.setDefaultValue(sharedPref.getBoolean(getString(R.string.settings_key_autoReconnect),true));
            prefAutoReconnect.setOnPreferenceChangeListener((preference, newValue) -> {
                sharedPref.edit().putBoolean(getString(R.string.settings_key_autoReconnect), Boolean.parseBoolean(newValue.toString())).apply();
                return true;
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

            EditTextPreference namePref = findPreference(getString(R.string.settings_key_name));
            assert namePref != null;
            namePref.setOnPreferenceChangeListener((preference, newValue) -> {
                PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString(getString(R.string.settings_key_name),newValue.toString()).apply();
                MainActivity.sendNameUpdate(getContext());
                return true;
            });

            DropDownPreference teamPref = findPreference(getString(R.string.settings_key_team));
            assert teamPref != null;
            teamPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
                    int newTeamInt = 0;
                    if (newValue.toString().length()>0){
                        newTeamInt = Integer.parseInt(newValue.toString());
                    }
                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt(getString(R.string.settings_key_team_int),newTeamInt).apply();
                    MainActivity.sendTeamUpdate(getContext());
                    return true;
                }
            });

            DropDownPreference rolePref = findPreference(getString(R.string.settings_key_role));
            assert rolePref != null;
            rolePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString(getString(R.string.settings_key_role),newValue.toString()).apply();
                    MainActivity.sendRoleUpdate(getContext());
                    return true;
                }
            });

            final Context context = getContext();
            Preference colorPref = findPreference(getString(R.string.settings_key_color));
            assert colorPref != null;
            colorPref.setOnPreferenceClickListener(preference -> {
                assert context != null;

                ScrollView scrollView = new ScrollView(context);
                scrollView.addView(getColorRadioGroup(context, sharedPref));

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("choose your color")
                        .setView(scrollView)
                        .setPositiveButton("ok", (dialog, which) -> {
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                            int intColor = sharedPreferences.getInt(getString(R.string.settings_key_color),0);
                            String hexColor = String.format("#%06X", (0xFFFFFF & intColor));
                            sharedPreferences.edit().putString(getString(R.string.settings_key_color_hex),hexColor).apply();
                            MainActivity.sendColorUpdate(getContext());
                            dialog.dismiss();
                        });
                builder.create().show();
                return false;
            });

        }

    }



    static String[] getAllColorNames(Context context){
        Resources resources = context.getResources();
        String packageName = context.getPackageName();
        String[] colorBaseNames = {"red", "pink", "purple", "deep_purple", "indigo", "blue", "light_blue", "cyan", "teal", "green", "light_green", "lime", "yellow", "amber", "orange", "deep_orange", "brown", "gray", "blue_gray"};
        String[] colorValues = {"50", "100", "200", "300", "400", "500", "600", "700", "800", "900"};
        List<String> colorList = new ArrayList<>();

        for (String colorBaseName : colorBaseNames) {
            for (String colorValue : colorValues){
                String colorName = colorBaseName+"_"+colorValue;
                int colorId = resources.getIdentifier(colorName,"color",packageName);
                if(colorId != 0){
                    colorList.add(colorName);
                }
            }
        }
        return colorList.toArray(new String[0]);
    }

    static View getColorRadioGroup(Context context, SharedPreferences sharedPref){

        String[] colorNameArray = getAllColorNames(context);

        Resources resources = context.getResources();
        String packageName = context.getPackageName();
        RadioGroup radioGroup = new RadioGroup(context);
        int oldColor = sharedPref.getInt(context.getString(R.string.settings_key_color),0);
        int idOfOldColor = 0;
        int index = 0;
        for (String colorName : colorNameArray){
            RadioButton radioButton = new RadioButton(context);
            radioButton.setText(colorName);
            radioButton.setId(index);
            int colorId = resources.getIdentifier(colorName,"color",packageName);
            int color = 0;
            if(colorId != 0){
                color = resources.getColor(colorId);
            }
            Log.v("tag","color " + color);
            radioButton.setTextColor(color);
            if(color == oldColor){
                idOfOldColor = index;
                Log.v("tag","right color");
            }

            final int finalColor = color;
            radioButton.setOnCheckedChangeListener((compoundButton, b) -> {
                if(b){
                    sharedPref.edit().putInt(context.getString(R.string.settings_key_color),finalColor).apply();
                }
            });
            radioGroup.addView(radioButton);
            index++;
        }
        radioGroup.check(idOfOldColor);
        return radioGroup;
    }
}