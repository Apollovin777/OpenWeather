package com.example.yurko.openweather;

import android.util.Log;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public  class Utilities {

    private static final String LOG_TAG = "Utilities";

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


}
