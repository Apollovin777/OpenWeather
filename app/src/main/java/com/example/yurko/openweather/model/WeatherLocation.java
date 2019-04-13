package com.example.yurko.openweather.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "weatherlocation")
public class WeatherLocation {

    public static String AUTOLOCATION_ID = "99999";

    @PrimaryKey(autoGenerate = true)
    public long id;
    public String cityId;
    public String cityName;
    public String country;
    public double latitude;
    public double longitude;
    public int recordType;
    public int currentLocation;

    public WeatherLocation(String cityId, String cityName, String country, double latitude, double longitude, int recordType, int currentLocation) {
        this.cityId = cityId;
        this.cityName = cityName;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
        this.recordType = recordType;
        this.currentLocation = currentLocation;
    }
}
