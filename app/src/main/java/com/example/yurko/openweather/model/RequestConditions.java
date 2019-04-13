package com.example.yurko.openweather.model;

import android.os.AsyncTask;
import android.util.Log;

import com.example.yurko.openweather.JRequest;
import com.example.yurko.openweather.presenter.CurrentConditionLoadListener;

public class RequestConditions extends AsyncTask<WeatherLocation, Void, String> {

    public static final String REQUEST_CC = "cc";
    public static final String REQUEST_FORECAST = "fc";

    private CurrentConditionLoadListener listener;
    private String requestKind;

    @Override
    protected String doInBackground(WeatherLocation... location) {
        listener.onLoading();
        try {
            WeatherLocation currLocation = location[0];
            String[] result = new String[2];
            if (currLocation != null) {
                JRequest jRequest = new JRequest(false, currLocation.latitude, currLocation.longitude);
                return jRequest.getResult();
            }
        } catch (Exception e) {
            Log.e("RequestCurrentCond", e.getMessage());
            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(String value) {
        listener.onLoadFinished(111,value);
    }

    public void setListener(CurrentConditionLoadListener listener) {
        this.listener = listener;
    }

    public void setRequestKind(String kind){
        this.requestKind = kind;
    }
}
