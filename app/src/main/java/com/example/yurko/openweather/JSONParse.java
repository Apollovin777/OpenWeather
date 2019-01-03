package com.example.yurko.openweather;

import org.json.JSONObject;

import java.util.Date;

public class JSONParse {
    protected JSONObject mJsonObj;

    public JSONParse(JSONObject jsonObject) {
        this.mJsonObj = jsonObject;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static Date convertUnix(long timeStamp){
        Date time = new Date(timeStamp*1000);
        return time;
    }


}
