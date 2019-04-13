package com.example.yurko.openweather;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class JRequest {
    private final String LOG_TAG = "JRequest";

    private final String APP_ID = BuildConfig.OpenW_key;

    private final String urlByCityCurrent = "http://api.openweathermap.org/data/2.5/weather";
    private final String urlByCityForecast = "http://api.openweathermap.org/data/2.5/forecast";
    private final String urlByCoordCurrent = "http://api.openweathermap.org/data/2.5/weather";
    private final String urlByCoordForecast ="http://api.openweathermap.org/data/2.5/forecast";
    private final String queryParam = "q";
    private final String latParam = "lat";
    private final String lonParam = "lon";

    private String mCityName;
    private boolean mForecast;
    private double mLatitude;
    private double mLongitude;
    private String mResult;

    public String getResult() {
        return mResult;
    }

    public JRequest(boolean forecast, double latitude, double longitude) {
        mForecast = forecast;
        mLatitude = latitude;
        mLongitude = longitude;
        Log.i(LOG_TAG,"ConstuctorCurrent");
        makeHttpRequest(buildByCoordURL());
    }

    public JRequest(String cityName, boolean forecast) {
        mCityName = cityName;
        mForecast = forecast;
        Log.i(LOG_TAG,"Constuctor");
        makeHttpRequest(buildByCityURL(mCityName,mForecast));
    }

    private URL buildByCityURL(String cityName, boolean forecast) {
        Uri baseUri;
        if (forecast) {
            baseUri = Uri.parse(urlByCityForecast);
        } else
            baseUri = Uri.parse(urlByCityCurrent);

        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendQueryParameter(queryParam, cityName);
        uriBuilder.appendQueryParameter("lang","ua");

        URL url = null;
        try {
            url = new URL(uriBuilder.toString());
        } catch (MalformedURLException e) {
            android.util.Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }

    private URL buildByCoordURL() {
        Uri baseUri;
        if (mForecast) {
            baseUri = Uri.parse(urlByCoordForecast);
        } else
            baseUri = Uri.parse(urlByCoordCurrent);

        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendQueryParameter(latParam, String.valueOf(mLatitude));
        uriBuilder.appendQueryParameter(lonParam, String.valueOf(mLongitude));

        URL url = null;
        try {
            url = new URL(uriBuilder.toString());
        } catch (MalformedURLException e) {
            android.util.Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }

    private void makeHttpRequest(URL url) {
        String jsonResponse = "";

        if (url == null) {
            mResult = jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream stream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("x-api-key", APP_ID);
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                stream = urlConnection.getInputStream();
                jsonResponse = readFromStream(stream);
            } else {
                android.util.Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            android.util.Log.i(LOG_TAG, e.getMessage());
            mResult = jsonResponse;
        } finally {
            urlConnection.disconnect();
        }

        mResult = jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        String result = "";
        if (inputStream == null) {
            return result;
        }
        InputStreamReader ir = new InputStreamReader(inputStream);
        BufferedReader reader = new BufferedReader(ir);

        StringBuilder builder = new StringBuilder();

        String line = reader.readLine();
        while (line != null) {
            builder.append(line);
            line = reader.readLine();
        }
        return builder.toString();
    }
}
