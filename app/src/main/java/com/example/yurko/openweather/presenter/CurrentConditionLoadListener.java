package com.example.yurko.openweather.presenter;

public interface CurrentConditionLoadListener {
    void onLoadFinished(int kind, String value);
    void onLoading();
}
