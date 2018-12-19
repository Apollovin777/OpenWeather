package com.example.yurko.openweather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Yurko on 18.11.2017.
 */

public class JSONParseForecast extends JSONParse{

    public JSONParseForecast(JSONObject jsonObject) {
        super(jsonObject);
    }

    public String getCityName() throws  JSONException{
        return mJsonObj.getJSONObject("city").getString("name");
    }

    public String getCityID() throws  JSONException{
        return mJsonObj.getJSONObject("city").getString("id");
    }

    public String getTemps() throws JSONException{
        StringBuilder builder = new StringBuilder();
        double temp;
        JSONArray array = mJsonObj.getJSONArray("list");
        for(int i = 0; i<array.length();i++){
            temp = array.getJSONObject(i).getJSONObject("main").getDouble("temp")-273;
             builder.append(round(temp,0)+"\n");
        }
        return builder.toString();
    }

    public String[] getTempsArray() throws JSONException{
        int count = mJsonObj.getInt("cnt");
        String[] arrayTemps = new String[count];
        double temp;
        JSONArray array = mJsonObj.getJSONArray("list");
        for(int i = 0; i<array.length();i++){
            temp = array.getJSONObject(i).getJSONObject("main").getDouble("temp")-273;
            arrayTemps[i]=String.valueOf(round(temp,0));
        }
        return arrayTemps;
    }

    public String getPressures() throws JSONException{
        StringBuilder builder = new StringBuilder();
        double temp;
        JSONArray array = mJsonObj.getJSONArray("list");
        for(int i = 0; i<array.length();i++){
            temp = array.getJSONObject(i).getJSONObject("main").getDouble("pressure")-273;
            builder.append(round(temp,0)+"\n");
        }
        return builder.toString();
    }

    public String[] getPressuresArray() throws JSONException{
        int count = mJsonObj.getInt("cnt");
        String[] arrayTemps = new String[count];
        double temp;
        JSONArray array = mJsonObj.getJSONArray("list");
        for(int i = 0; i<array.length();i++){
            temp = array.getJSONObject(i).getJSONObject("main").getDouble("pressure")-273;
            arrayTemps[i]=String.valueOf(round(temp,0));
        }
        return arrayTemps;
    }

    public Date[] getDatesArray() throws JSONException{
        int count = mJsonObj.getInt("cnt");
        Date[] arrayTemps = new Date[count];
        long temp;
        JSONArray array = mJsonObj.getJSONArray("list");
        for(int i = 0; i<array.length();i++){
            temp = array.getJSONObject(i).getLong("dt");
            arrayTemps[i]=JSONParse.convertUnix(temp);
        }
        return arrayTemps;
    }

}
