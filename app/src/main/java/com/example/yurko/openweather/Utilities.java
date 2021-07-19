package com.example.yurko.openweather;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.yurko.openweather.model.CCOpenWeatherPojo.CCOpenWeatherPojo;
import com.example.yurko.openweather.model.MyApplication;
import com.google.gson.Gson;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public  class Utilities {

    private static final String LOG_TAG = "Utilities";
    private static final String CURRENT_COND_OBJECT = "current_cond_object";

    public static String getFormatSunRiseSet(JSONParseCurrent jObj) throws JSONException {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        timeFormat.setTimeZone(TimeZone.getDefault());

        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(jObj.getSunRiseTime() * 1000);
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));

        StringBuilder builder = new StringBuilder();
        builder.append(timeFormat.format(calendar.getTime()));
        builder.append(" - ");

        calendar.setTimeInMillis(jObj.getSunSetTime() * 1000);
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));

        builder.append(timeFormat.format(calendar.getTime()));
        return builder.toString();
    }

    public static Date convertUnixToDate(Integer unix){
        long dv = Long.valueOf(unix)*1000;// its need to be in milisecond
        return new Date(dv);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static CCOpenWeatherPojo getObjectFromPreferences(){
        SharedPreferences mPrefs = MyApplication.getAppContext().getSharedPreferences(CURRENT_COND_OBJECT, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPrefs.getString(CURRENT_COND_OBJECT, "");
        return gson.fromJson(json, CCOpenWeatherPojo.class);
    }

    public static void saveObjectToPreferences(CCOpenWeatherPojo obj){
        SharedPreferences  mPrefs = MyApplication.getAppContext().getSharedPreferences(CURRENT_COND_OBJECT,Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(obj);
        prefsEditor.putString(CURRENT_COND_OBJECT, json);
        prefsEditor.commit();
    }

}
