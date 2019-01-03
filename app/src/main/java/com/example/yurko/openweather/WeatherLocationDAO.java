package com.example.yurko.openweather;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface WeatherLocationDAO {
    @Insert
    void insert(WeatherLocation location);

    // Удаление Person из бд
    @Delete
    void delete(WeatherLocation person);

    // Получение всех Person из бд
    @Query("SELECT * FROM weatherlocation WHERE currentLocation = 1")
    WeatherLocation getCurrentLocation();

    @Query("SELECT * FROM weatherlocation WHERE recordType != 0")
    List<WeatherLocation> getall();

    @Query("SELECT * FROM weatherlocation")
    List<WeatherLocation> checkCount();
}
