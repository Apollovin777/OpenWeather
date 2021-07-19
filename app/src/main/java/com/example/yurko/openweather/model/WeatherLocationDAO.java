package com.example.yurko.openweather.model;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import java.util.List;

@Dao
public abstract class WeatherLocationDAO {
    @Insert
    public abstract long insert(WeatherLocation location);

    @Delete
    public abstract void delete(WeatherLocation location);

    @Query("SELECT * FROM weatherlocation WHERE recordType = 1")
    public abstract WeatherLocation getAutoLocation();

    @Query("SELECT * FROM weatherlocation WHERE currentLocation = 1")
    public abstract WeatherLocation getCurrentLocation();

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

    @Query("UPDATE weatherlocation set currentLocation = 1 where recordType = 1")
    public abstract void setAutoAsCurrent();

    @Transaction
    void updateCurrentLocation(long id) {
        updateSetAllNonDefault();
        setCurrent(id);
    }


}
