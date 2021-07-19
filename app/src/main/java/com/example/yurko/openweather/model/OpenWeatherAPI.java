package com.example.yurko.openweather.model;

import com.example.yurko.openweather.model.CCOpenWeatherPojo.CCOpenWeatherPojo;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OpenWeatherAPI {

    @GET("weather")
    Call<CCOpenWeatherPojo> getWeatherByCoord(@Query("lat") Double lat, @Query("lon") Double lon);

    @GET("weather")
    Call<CCOpenWeatherPojo> getWeatherByCity(@Query("q") String cityName);

//    @GET("forecast")
//    Call<ForecastWeatherPojo> getForecastByCity(@Query("q") String cityName);
//
//    @GET("forecast")
//    Call<ForecastWeatherPojo> getForecastByCoord(@Query("lat") Double lat, @Query("lon") Double lon);

    @GET("weather")
    Call<ResponseBody> getJSONResponseByCity(@Query("q") String cityName, @Query("appid") String appID);


}
