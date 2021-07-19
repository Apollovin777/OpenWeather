package com.example.yurko.openweather.presenter;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.yurko.openweather.model.MyApplication;
import com.example.yurko.openweather.widget.AppWidget;

public class UpdateJob extends JobService {
    public static int JOB_ID=9;
    private static String LOG_TAG ="UpdateJob";
    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private static final String CURRENT_COND_OBJECT = "current_cond_object";

    @Override
    public boolean onStartJob(JobParameters params) {
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(CURRENT_COND_OBJECT)) {
                    MainPresenter.refreshWidget();
                }
            }
        };
        SharedPreferences sharedPreferences =  MyApplication.getAppContext().getSharedPreferences(CURRENT_COND_OBJECT, Context.MODE_PRIVATE);
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);

        RequestData.request();
        Log.i(LOG_TAG,"onStartJob");
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(LOG_TAG,"onStopJob");
        SharedPreferences sharedPreferences =  MyApplication.getAppContext().getSharedPreferences(CURRENT_COND_OBJECT, Context.MODE_PRIVATE);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
        return true;
    }
}