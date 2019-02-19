package com.example.yurko.openweather;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.Update;

import java.util.ArrayList;
import java.util.List;

@Dao
public abstract class WeatherLocationDAO {
    @Insert
    abstract long insert(WeatherLocation location);

    @Delete
    abstract void delete(WeatherLocation person);

    @Query("SELECT * FROM weatherlocation WHERE recordType = 1")
    abstract List<WeatherLocation> getAutoLocation();

    @Query("SELECT * FROM weatherlocation WHERE currentLocation = 1")
    abstract WeatherLocation getCurrentLocation();

    @Query("SELECT * FROM weatherlocation WHERE recordType != 0")
    abstract List<WeatherLocation> getall();

    @Query("SELECT * FROM weatherlocation WHERE recordType = 0")
    abstract List<WeatherLocation> getLastHope();

    @Query("SELECT * FROM weatherlocation")
    abstract List<WeatherLocation> checkCount();

    @Query("UPDATE weatherlocation set currentLocation = 0")
    abstract void updateSetAllNonDefault();

    @Query("UPDATE weatherlocation set currentLocation = 1 where id = :id")
    abstract void setCurrent(long id);

    @Transaction
    void updateCurrentLocation(long id) {
        updateSetAllNonDefault();
        setCurrent(id);
    }


}
