package com.paykio.khubaibraza.voicrecorder;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.icu.text.AlphabeticIndex;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "granted";
    public static String extenstion="mp3";
    private static final int PERMISSIONS_REQUEST_READ_STORAGE = 0;
    private int[] tabIcons = {
            R.drawable.ic_tab_rec,
            R.drawable.ic_tab_pla,
            R.drawable.ic_tab_set};
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private final int MY_PERMISSIONS_RECORD_AUDIO = 1;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    TabLayout mTabLayout;
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                            PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                                , 0);
                    } else {

                    }
                    new RecordFragment();

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permissions Denied to record audio", Toast.LENGTH_LONG).show();
                }
                return;
            }

            case 3: {
                Log.d(TAG, "External storage1");
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
                    //resume tasks needing this permission
                    new RecordFragment();
                } else {

                }
                break;
            }

            case 2: {
                Log.d(TAG, "External storage2");
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
                    //resume tasks needing this permission
                   new RecordFragment();
                } else {
//                    progress.dismiss();
                }
                break;
            }
        }
    }
    public  boolean isWriteStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted2");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked2");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted2");
            return true;
        }
    }
    private void requestAudioPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            //When permission is not granted by user, show them message why this permission is needed.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                Toast.makeText(this, "Please grant permissions to record audio", Toast.LENGTH_LONG).show();

                //Give user option to still opt-in the permissions
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_RECORD_AUDIO);

            } else {
                // Show user dialog to grant permission to record audio
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_RECORD_AUDIO);
            }
        }
    }


//    ------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Toast.makeText(this, Environment.getExternalStorageDirectory().getAbsolutePath(), Toast.LENGTH_SHORT).show();
//        Context hostActivity = ;
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
//
//        getFragmentManager().beginTransaction()
//                .replace(android.R.id.content, new SettingsFragment())
//                .commit();
//        StringBuilder info=new StringBuilder();
//        Toast.makeText(this, "s"+sharedPreferences.getString("RecordingFormat",""), Toast.LENGTH_SHORT).show();
//        Toast.makeText(this, SettingsFragment.getDefaults(getString(R.string.RecordingFormat),this), Toast.LENGTH_SHORT).show();
        // Set up the ViewPager with the sections adapter.
        mViewPager =  findViewById(R.id.container);
        setupViewPager(mViewPager);
        mTabLayout = findViewById(R.id.view_pager_tab);
        mTabLayout.setupWithViewPager(mViewPager);

        setupTabIcons();
requestAudioPermissions();


//            if (getApplicationContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
//                    PackageManager.PERMISSION_GRANTED) {
//                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
//                        , PERMISSIONS_REQUEST_READ_STORAGE);
//            } else {
//                new RecordFragment();
//            }
    }



    //-------------------------------------


    //    int count = preferenceScreen.getPreferenceCount();
//
//    //        for (int i = 0; i < count; i++) {
//    Preference p = preferenceScreen.getPreference(0);
//            if ((p instanceof ListPreference)) {
//        ListPreference listPreference=(ListPreference) p;
//        Toast.makeText(getActivity(), "in list", Toast.LENGTH_SHORT).show();
////                String value = "go";
//        listPreference.setSummary("array wow");
    //----------------------
    private void setupTabIcons() {

        TextView tabOne = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabOne.setText("Recorder");
        tabOne.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_tab_pla, 0, 0);
        mTabLayout.getTabAt(0).setCustomView(tabOne);

        TextView tabTwo = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabTwo.setText("Player");
        tabTwo.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_tab_rec, 0, 0);
        mTabLayout.getTabAt(1).setCustomView(tabTwo);

        TextView tabThree = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabThree.setText("Settings");
        tabThree.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_tab_set, 0, 0);
        mTabLayout.getTabAt(2).setCustomView(tabThree);
    }

    private void setupViewPager(ViewPager viewPager) {
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mSectionsPagerAdapter.addFragment(new RecordFragment(), "ONE");
        mSectionsPagerAdapter.addFragment(new CallsFragment(), "TWO");
        mSectionsPagerAdapter.addFragment(new SettingsFragment(), "THREE");
        viewPager.setAdapter(mSectionsPagerAdapter);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
        //here is position of fragment
            switch(position){
                case 0:{
                    return RecordFragment.newInstance(position);
                }
//                case 1:{
//                    return CallsFragment.newInstance(position);
//                }
                case 1:
                {
                    return FileViewerFragment.newInstance(position);

                }

                case 2:{
                    return SettingsFragment.newInstance(position);
                }
            }
            return null;
        }

        public void addFragment(Fragment fragment, String title) {

            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }
        @Override
        public int getCount() {
            // Show 3 total pages.
            return mFragmentTitleList.size();

        }
        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
