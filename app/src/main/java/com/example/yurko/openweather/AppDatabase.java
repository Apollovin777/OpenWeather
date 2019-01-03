package com.example.yurko.openweather;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(version = 2, entities = {WeatherLocation.class})
abstract class AppDatabase extends RoomDatabase {

    abstract public WeatherLocationDAO  WeatherLocationDAO();
}

