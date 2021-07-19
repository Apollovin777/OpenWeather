package com.example.yurko.openweather.view;

import androidx.appcompat.app.AppCompatActivity;

public interface MainView {
    //void setCurrentCondition(String data);
    void setTemp(String temp);
    void setDescription(String description);
    void setWindSpeed(String speed);
    void setCloud(String cloud);
    void setPressure(String pressure);
    void setHumidity(String humidity);
    void setSunRiseSetTime(String riseSetTime);
    void setCCImage(Integer id);
    void setUpdateBarText(String text);
    void showToast(String info);
    void setStatusBarCaption(String city, String country);
    void setForecast(String data);
    void setNexAlarmTime(String alarmTime);
    AppCompatActivity getViewActivity();
}
