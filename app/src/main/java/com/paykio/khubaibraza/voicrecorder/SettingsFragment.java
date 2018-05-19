package com.paykio.khubaibraza.voicrecorder;

import android.Manifest;
import android.app.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.codekidlabs.storagechooser.StorageChooser;

import java.io.File;

/**
 * Created by Khubaib raza on 18/02/2018.
 */

public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener,Preference.OnPreferenceChangeListener {
    private static final int PERMISSIONS_REQUEST_READ_STORAGE = 0;
    private Preference changeDirPref;
    CheckBoxPreference checkBoxPreference;
    ListPreference mListPreference;
    private static final String ARG_POSITION = "position";

    public static SettingsFragment newInstance(int position) {
        SettingsFragment f = new SettingsFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);

        return f;
    }
    public void onCreatePreferences(Bundle bundle, String s) {
        // Load the Preferences from the XML file
        addPreferencesFromResource(R.xml.setting_preference);

        changeDirPref = findPreference("dir");
        changeDirPref.setSummary(Environment.getExternalStorageDirectory()
                .getAbsolutePath()+"/VoiceRecorder/");
        SharedPreferences sharedPreferences= android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(getActivity());

        String value=sharedPreferences.getString("path","-1");//first
        if(!(value.equals("-1"))){
            changeDirPref.setSummary(value);
        }
        changeDirPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                if (Build.VERSION.SDK_INT >= 23) {

                    if (getContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                            PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                                , PERMISSIONS_REQUEST_READ_STORAGE);
                    } else {
                        selectDir();
                    }

                } else {
                    selectDir();
                }

                return true;
            }
        });

        PreferenceScreen preferenceScreen = getPreferenceScreen();

//        mListPreference=(ListPreference) preference;
        int count = preferenceScreen.getPreferenceCount();
        for (int i = 0; i < count; i++) {
            Preference p = preferenceScreen.getPreference(i);

            //same like other list preference or statusbar preference or checkbox preference done in morning
            if ((p instanceof ListPreference)) {
                ListPreference listPreference = (ListPreference) p;//
                listPreference.setSummary(listPreference.getEntry());
            }

        }
        checkBoxPreference = (CheckBoxPreference) getPreferenceScreen()
                .findPreference("StatusBar");
        final Preference pref =  findPreference("StatusBar");
        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                if(checkBoxPreference.isChecked()) {
                    Toast.makeText(getContext(), "Some text true case", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(getContext(), "Some text fadjshdjhsgbxdhc case", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        mListPreference=(ListPreference) findPreference(getString(R.string.RecordingFormat));


        if (Build.VERSION.SDK_INT >= 23) {

            if (getContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                        , PERMISSIONS_REQUEST_READ_STORAGE);
            } else {
                selectDir();
            }

        } else {
            selectDir();
        }


        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);

        if(!(key.equals("StatusBar"))){// becuase here we not deal with checkbox prefrence
        //if for re queries
        Toast.makeText(getActivity(), key, Toast.LENGTH_LONG).show();
        if (preference != null ) {
            //commented all other code becuase i simply set summary of all Lists and prefrences

//            if ((key.equals(getResources().getString(R.string.RecordingFormat)))) {
                String value = sharedPreferences.getString(preference.getKey(), "");
                setPreferenceSummery(preference, value);
//            } else
//                if (preference instanceof CheckBoxPreference) {
//                setPreferenceSummery(preference, "check");
//                checkBoxPreference.
//                        setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//
//                            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                                if (newValue.toString().equals("true")) {
//                                    Toast.makeText(getActivity(), "CB: " + "true",
//                                            Toast.LENGTH_SHORT).show();
//                                } else {
//                                    Toast.makeText(getActivity(), "CB: " + "false",
//                                            Toast.LENGTH_SHORT).show();
//                                }
//                                return true;
//                            }
//                        });
//            }
        }
    }

    }

    //    Addd data for storage slection
    private void selectDir(){

        File file = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath()+"/VoiceRecorder/");

        if (!file.exists()){
            file.mkdirs();
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());


        StorageChooser chooser = new StorageChooser.Builder()
                .withActivity(getActivity())
                .withFragmentManager(getActivity().getFragmentManager())
                .withMemoryBar(true)
                .allowCustomPath(true)
                .setDialogTitle("Select a Directory")
                .setType(StorageChooser.DIRECTORY_CHOOSER)
                .withPredefinedPath(file.getAbsolutePath())
                .actionSave(true)
                .withPreference(sp)
                .build();

        // Show dialog whenever you want by
        chooser.show();

        // get path that the user has chosen
        chooser.setOnSelectListener(new StorageChooser.OnSelectListener() {
            @Override
            public void onSelect(String path) {
                if (changeDirPref != null){
                    changeDirPref.setSummary(path);
                }
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("path", path);
                editor.commit();
                Toast.makeText(getActivity(), path, Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_READ_STORAGE){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Permission added
                selectDir();
            } else {
                Toast.makeText(getContext(), "oh! you don't give permission to access storage" +
                        ".To backup your data you need to grant permission", Toast.LENGTH_LONG).show();
            }
        }

    }


    //set summary of prefrences
    private void setPreferenceSummery(Preference preference, Object value) {

        String stringValue = value.toString();

        if (preference instanceof ListPreference) {

            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);

            if (prefIndex >= 0) {
                listPreference.setSummary(listPreference.getEntries()[prefIndex]);
                Toast.makeText(getActivity(), "in entry", Toast.LENGTH_LONG).show();
            }

        }
        else if (preference instanceof CheckBoxPreference){

        }
            else{
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }
    }

    //register and unregister on lifecycle
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }




}
