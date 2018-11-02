package com.belaku.dialogue;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.ArrayList;

public class UserSettingActivity extends PreferenceActivity {

    public static Context mContext;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getApplicationContext();

        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();

        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().getString("Wall") != null) {
                new MainActivity().Update(getIntent().getExtras().getString("fName"),getIntent().getExtras().getString("Wall"), getIntent().getExtras().getString("Quote"), getIntent().getExtras().getString("Author"));
            }
        }


    }



    @SuppressLint("ValidFragment")
    public static class MyPreferenceFragment extends PreferenceFragment
    {
        public CheckBoxPreference CheckBoxPreferenceKan, CheckBoxPreferenceHin, CheckBoxPreferenceEng;
        public ArrayList<String> languagesSelected = new ArrayList<>();
        public SwitchPreference switchPreferenceSound;


        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);

            CheckBoxPreferenceKan = (CheckBoxPreference) findPreference("prefCheckBoxKan");
            CheckBoxPreferenceHin = (CheckBoxPreference) findPreference("prefCheckBoxHin");
            CheckBoxPreferenceEng = (CheckBoxPreference) findPreference("prefCheckBoxEng");

            switchPreferenceSound= (SwitchPreference) findPreference("Sound");
            switchPreferenceSound.setEnabled(true);

            switchPreferenceSound.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    Log.d("hereITisS", "yay");

                    if (((SwitchPreference) preference).isChecked()) {
                        ((SwitchPreference) preference).setChecked(false);
                        ((SwitchPreference) preference).setTitle("Vibrate");
                        beep(0);
                    }
                    else {
                        ((SwitchPreference) preference).setChecked(true);
                        ((SwitchPreference) preference).setTitle("Sound");
                        SelectSound();
                        beep(10);
                    }
                    return false;
                }
            });

        //    if (!languagesSelected.contains("English"))
            languagesSelected.add("English");


            Preference pref = findPreference("done_btn_ref");
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // TODO Auto-generated method stub

                    ReadUserPreferences();

                    startActivity(new Intent(mContext, MainActivity.class).putStringArrayListExtra("prefs", languagesSelected));

                    return false;
                }
            });




        }

        private void SelectSound() {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
                    RingtoneManager.TYPE_NOTIFICATION
                            | RingtoneManager.TYPE_RINGTONE);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                    RingtoneManager
                            .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            startActivityForResult(intent, 0);
        }

        private void beep(int n)
        {
            AudioManager manager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            manager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, n,
                    AudioManager.FLAG_SHOW_UI + AudioManager.FLAG_PLAY_SOUND);
            Uri notification = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(mContext,
                    notification);
            r.play();
        }

        public void ReadUserPreferences() {

            if (CheckBoxPreferenceKan.isChecked())
                languagesSelected.add("Kannada");
            if (CheckBoxPreferenceHin.isChecked())
                languagesSelected.add("Hindi");
       //     if (CheckBoxPreferenceEng.isChecked())
       //         languagesSelected.add("English");


        }
    }
}