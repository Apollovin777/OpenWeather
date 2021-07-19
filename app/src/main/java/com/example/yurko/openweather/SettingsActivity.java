package com.example.yurko.openweather;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.WindowManager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceFragmentCompat;

import com.example.yurko.openweather.model.MyApplication;
import com.example.yurko.openweather.presenter.UpdateJob;

import java.util.concurrent.TimeUnit;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

public class SettingsActivity extends AppCompatActivity {

    private static final String LOG_TAG = "SettingsActivity";
    SharedPreferences.OnSharedPreferenceChangeListener prefListener;
    SharedPreferences prefs;

    @Override
    protected void onPause() {
        super.onPause();
        prefs.unregisterOnSharedPreferenceChangeListener(prefListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        prefs.registerOnSharedPreferenceChangeListener(prefListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();

        Toolbar toolbar = findViewById(R.id.toolbarSettings);
        setSupportActionBar(toolbar);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext());
        prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if (prefs.getBoolean("sync",false)) {
                    JobScheduler mJobScheduler = (JobScheduler)
                            MyApplication.getAppContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
                    JobInfo.Builder builder = new JobInfo.Builder(UpdateJob.JOB_ID,
                            new ComponentName(MyApplication.getAppContext(), UpdateJob.class));
                    int period = Integer.parseInt(prefs.getString("list","1"));
                    builder.setPeriodic(3600000*period);
                    builder.setPersisted(true);// in every 1 hour
                    builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY); // only when network is available

                    if (mJobScheduler.schedule(builder.build()) <= 0) {
                        Log.i(LOG_TAG, "Service schedule error");
                    }
                }
            }
        };
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }
}