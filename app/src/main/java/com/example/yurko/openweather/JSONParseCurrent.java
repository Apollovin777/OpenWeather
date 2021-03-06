package com.example.yurko.openweather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Date;

public class JSONParseCurrent extends JSONParse{

    public JSONParseCurrent(JSONObject jsonObject) {
        super(jsonObject);
    }

    public double getTemp() throws JSONException {
        double result = mJsonObj.getJSONObject("main").getDouble("temp")-273;
        return round(result,1);
    }

    public int getPressure() throws JSONException{
        return mJsonObj.getJSONObject("main").getInt("pressure");
    }

    public int getHumidity() throws JSONException{
        return mJsonObj.getJSONObject("main").getInt("humidity");
    }

    public double getTempMin() throws JSONException{
        double result = mJsonObj.getJSONObject("main").getInt("temp_min")-273;
        return round(result,0);
    }

    public double getTempMax() throws JSONException{
        double result = mJsonObj.getJSONObject("main").getInt("temp_max")-273;
        return round(result,0);
    }

    public int getClouds() throws JSONException{
        return mJsonObj.getJSONObject("clouds").getInt("all");
    }

    public double getWindSpeed() throws JSONException{
        double result = mJsonObj.getJSONObject("wind").getDouble("speed");
        return round(result,1);
    }

    public double getWindDireection() throws JSONException{
        double result = mJsonObj.getJSONObject("wind").getDouble("deg");
        return round(result,1);
    }

    public String getCityName() throws  JSONException{
        return mJsonObj.getString("name");
    }

    public Date getDate() throws JSONException{
        return JSONParse.convertUnix(mJsonObj.getLong("dt"));
    }

    public long getSunRiseTime() throws JSONException{
        return mJsonObj.getJSONObject("sys").getLong("sunrise");
    }

    public long getSunSetTime() throws JSONException {
        return mJsonObj.getJSONObject("sys").getLong("sunset");
    }

    public String getCountryName() throws JSONException{
        return mJsonObj.getJSONObject("sys").getString("country");
    }

    public String getDescriptionID() throws JSONException{
        JSONArray array = mJsonObj.getJSONArray("weather");
        String id = array.getJSONObject(0).getString("id");
        return id;
    }

    public String getDesc() throws JSONException{
        JSONArray array = mJsonObj.getJSONArray("weather");
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i<array.length();i++){
                builder.append(array.getJSONObject(i).getString("description"));
                builder.append(" ");
        }
        return builder.toString();
    }
}
