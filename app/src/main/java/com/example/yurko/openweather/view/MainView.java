package com.example.yurko.openweather.view;

import android.support.v7.app.AppCompatActivity;

public interface MainView {
    void setCurrentCondiiton(String data);
    void showToast(String info);
    void setStatusBarCaption(String city, String country);
    void setLoadingBar(String data);
    void setForecast(String data);
    AppCompatActivity getViewActivity();
}
