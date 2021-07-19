package com.example.yurko.openweather.presenter;

import android.util.Log;

import com.example.yurko.openweather.AppExecutors;
import com.example.yurko.openweather.Utilities;
import com.example.yurko.openweather.model.AppDatabase;
import com.example.yurko.openweather.model.CCOpenWeatherPojo.CCOpenWeatherPojo;
import com.example.yurko.openweather.model.MyApplication;
import com.example.yurko.openweather.model.WeatherLocation;

import java.util.Date;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestData {
    private final static String LOG_TAG = "RequestData";

    public static void request() {
        AppExecutors.getInstance ().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                AppDatabase mDb = AppDatabase.getInstance(MyApplication.getAppContext());
                final WeatherLocation mCurrentLocation = mDb.WeatherLocationDAO().getCurrentLocation();
                if (mCurrentLocation == null) {
                    return;
                }
                String city = mCurrentLocation.cityName;
                double lat = mCurrentLocation.latitude;
                double lon = mCurrentLocation.longitude;
                Call<CCOpenWeatherPojo> call;
                if (city.equals("Auto Location")) {
                    call = MyApplication.getApi().getWeatherByCoord(lat, lon);
                } else {
                    call = MyApplication.getApi().getWeatherByCity(city);
                }
                call.enqueue(new Callback<CCOpenWeatherPojo>() {
                    @Override
                    public void onResponse(Call<CCOpenWeatherPojo> call, Response<CCOpenWeatherPojo> response) {
                        Utilities.saveObjectToPreferences(response.body());
                        Log.i(LOG_TAG,response.body().getMain().getTemp().toString());
                        Log.i(LOG_TAG,response.body().getTimestamp().toString());
                    }

                    @Override
                    public void onFailure(Call<CCOpenWeatherPojo> call, Throwable t) {
                        Log.i(LOG_TAG, t.getMessage());
                    }
                });
            }
        });
    }
}
