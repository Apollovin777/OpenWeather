package com.example.yurko.openweather.presenter;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.widget.Toast;

import com.example.yurko.openweather.R;
import com.example.yurko.openweather.Utilities;
import com.example.yurko.openweather.model.MyApplication;
import com.example.yurko.openweather.model.CCOpenWeatherPojo.CCOpenWeatherPojo;
import com.example.yurko.openweather.model.OpenWeatherAPI;
import com.example.yurko.openweather.model.WeatherLocation;
import com.example.yurko.openweather.model.AppDatabase;
import com.example.yurko.openweather.AppExecutors;
import com.example.yurko.openweather.view.MainView;
import com.example.yurko.openweather.widget.AppWidget;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainPresenter implements Presenter, SharedPreferences.OnSharedPreferenceChangeListener {

    private final static String LOG_TAG = "MainPresenter";
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    private final static int REQUEST_CURRENT_CONDITIONS = 0;
    private final static int REQUEST_FORECAST = 1;

    private final static String CURRENT_COND = "current_cond";
    private final static String CURRENT_TEMPERATURE = "current_temperature";

    private final static String CURRENT_UPDATETIME = "current_updatetime";
    private static final String CURRENT_COND_OBJECT = "current_cond_object";

    private String mCurrent;
    private String mForecast;
    private AppDatabase mDb;
    private MainView mView;
    private long mLastClickTime;
    private String city;
    private Double lat;
    private Double lon;



    public MainPresenter(MainView view) {
        mView = view;
        mDb = AppDatabase.getInstance(MyApplication.getAppContext());
    }

    public static void checkService() {
        Log.i(LOG_TAG,"checkService");
        JobScheduler mJobScheduler = (JobScheduler)
                MyApplication.getAppContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            JobInfo jobInfo = mJobScheduler.getPendingJob(UpdateJob.JOB_ID);
            if (jobInfo != null) {
                Log.i(LOG_TAG,"UpdateJob is scheduled");
                Toast.makeText(MyApplication.getAppContext(), "UpdateJob is scheduled", Toast.LENGTH_LONG).show();
            } else{
                Log.i(LOG_TAG,"Not scheduled");
                Toast.makeText(MyApplication.getAppContext(), "Not scheduled", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void viewIsReady() {
        if (ActivityCompat.checkSelfPermission(MyApplication.getAppContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MyApplication.getAppContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            renewAutoLocationPlace();
        } else {
            requestPermissions();
        }
        //updateScreenData();

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final WeatherLocation mCurrentLocation = mDb.WeatherLocationDAO().getCurrentLocation();
                if (mCurrentLocation == null) {
                    return;
                }
                mView.getViewActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //testRetrofit(city);
                        city = mCurrentLocation.cityName;
                        lat = mCurrentLocation.latitude;
                        lon = mCurrentLocation.longitude;
                        updateScreenData();
                    }
                });
            }
        });
    }

    private static String getSUNRiseSet(Long sunRise, Long sunSet) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(sunRise * 1000);

        StringBuilder builder = new StringBuilder();
        builder.append(timeFormat.format(calendar.getTime()));

        builder.append(" - ");

        calendar.setTimeInMillis(sunSet * 1000);
        builder.append(timeFormat.format(calendar.getTime()));
        return builder.toString();
    }

    private void testRetrofit(String city) {
        Call<CCOpenWeatherPojo> call = MyApplication.getApi().getWeatherByCity(city);
        mView.setUpdateBarText("Downloading...");
        call.enqueue(new Callback<CCOpenWeatherPojo>() {
            @Override
            public void onResponse(Call<CCOpenWeatherPojo> call, Response<CCOpenWeatherPojo> response) {
                Log.i(LOG_TAG, response.body().getMain().getTemp().toString());
                android.text.format.DateFormat df = new android.text.format.DateFormat();
                String updateTime = df.format("HH:mm", new Date()).toString();
                String currCondTime = df.format("HH:mm", response.body().getDataTime()).toString();
                String updated = MyApplication.getAppContext().getResources().getString(R.string.updated);

                mView.setUpdateBarText(updated + " " + updateTime + " / " + currCondTime);
                mView.setTemp(response.body().getMain().getTempCels().toString());
                mView.setCloud(response.body().getClouds().getAll().toString());
                mView.setWindSpeed(response.body().getWind().getSpeed().toString());
                mView.setPressure(response.body().getMain().getPressure().toString());
                mView.setHumidity(response.body().getMain().getHumidity().toString());

                Long sunRise = response.body().getSys().getSunrise().longValue();
                Long sunSet = response.body().getSys().getSunset().longValue();
                mView.setSunRiseSetTime(getSUNRiseSet(sunRise, sunSet));
                mView.setCCImage(response.body().getWeather().get(0).getId());
                mView.setDescription(response.body().getWeather().get(0).getDescription().toString());
                setStatusBarCaption();
            }

            @Override
            public void onFailure(Call<CCOpenWeatherPojo> call, Throwable t) {
                Log.i(LOG_TAG, t.getMessage());
            }
        });
    }

    public void requestPermissions() {
        if (PermissionUtils.getPermissionsRequestsCount(MyApplication.getAppContext(),
                "requests_count") < 2) {
            PermissionUtils.incrementPermissionsRequestsCount(MyApplication.getAppContext(),
                    "requests_count");

            final List<String> missingPermissions = new ArrayList<>();
            // check all required dynamic permissions
            for (final String permission : REQUIRED_SDK_PERMISSIONS) {
                final int result = ContextCompat.checkSelfPermission(MyApplication.getAppContext(), permission);
                if (result != PackageManager.PERMISSION_GRANTED) {
                    missingPermissions.add(permission);
                }
            }

            if (!missingPermissions.isEmpty()) {
                // request all missing permissions
                final String[] permissions = missingPermissions
                        .toArray(new String[missingPermissions.size()]);
                ActivityCompat.requestPermissions(mView.getViewActivity(), permissions, REQUEST_CODE_ASK_PERMISSIONS);
            } else {
                final int[] grantResults = new int[REQUIRED_SDK_PERMISSIONS.length];
                Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
                mView.getViewActivity().onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,
                        grantResults);
            }
        }
    }

    @Override
    public void onPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int index = permissions.length - 1; index >= 0; --index) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        mView.showToast("Required permission not granted");
                        addLastHopeLocation();
                        return;
                    }
                }
                // all permissions were granted
                renewAutoLocationPlace();
                break;
        }
    }

    public static void updateTempData(){
        Log.i(LOG_TAG,"updateTempData");
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
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
                if(city.equals("Auto Location")){
                    call = MyApplication.getApi().getWeatherByCoord(lat,lon);
                }else {
                    call = MyApplication.getApi().getWeatherByCity(city);
                }
                call.enqueue(new Callback<CCOpenWeatherPojo>() {
                    @Override
                    public void onResponse(Call<CCOpenWeatherPojo> call, Response<CCOpenWeatherPojo> response) {
                        android.text.format.DateFormat df = new android.text.format.DateFormat();
                        String updateTime = df.format("HH:mm", new Date()).toString();

                        Utilities.saveObjectToPreferences(response.body());
                        saveTempForWidget(MyApplication.getAppContext(),response.body().getMain().getTempCels().toString(),updateTime);
                        refreshWidget();
                    }

                    @Override
                    public void onFailure(Call<CCOpenWeatherPojo> call, Throwable t) {
                        Log.i(LOG_TAG, t.getMessage());
                    }
                });
            }
        });


    }

    public void requestCurrentWeather() {
        Call<CCOpenWeatherPojo> call;
        if (city != null) {
            if (city.equals("Auto Location")) {
                call = MyApplication.getApi().getWeatherByCoord(lat, lon);
            } else {
                call = MyApplication.getApi().getWeatherByCity(city);
            }
            mView.setUpdateBarText("Downloading...");
            call.enqueue(new Callback<CCOpenWeatherPojo>() {
                @Override
                public void onResponse(Call<CCOpenWeatherPojo> call, Response<CCOpenWeatherPojo> response) {
                    Utilities.saveObjectToPreferences(response.body());
                }

                @Override
                public void onFailure(Call<CCOpenWeatherPojo> call, Throwable t) {
                    Log.i(LOG_TAG, t.getMessage());
                }
            });
        }
    }

    @Override
    public void updateScreenData() {
        CCOpenWeatherPojo currObj = Utilities.getObjectFromPreferences();
        if (currObj != null) {
            android.text.format.DateFormat df = new android.text.format.DateFormat();
            String updateTime = df.format("HH:mm", new Date()).toString();
            String currCondTime = df.format("HH:mm", currObj.getDataTime()).toString();
            String updated = MyApplication.getAppContext().getResources().getString(R.string.updated);

            mView.setUpdateBarText(updated + " " + updateTime + " / " + currCondTime);
            mView.setTemp(currObj.getMain().getTempCels().toString());
            mView.setCloud(currObj.getClouds().getAll().toString());
            mView.setWindSpeed(currObj.getWind().getSpeed().toString());
            mView.setPressure(currObj.getMain().getPressure().toString());
            mView.setHumidity(currObj.getMain().getHumidity().toString());

            Long sunRise = currObj.getSys().getSunrise().longValue();
            Long sunSet = currObj.getSys().getSunset().longValue();
            mView.setSunRiseSetTime(getSUNRiseSet(sunRise, sunSet));
            mView.setCCImage(currObj.getWeather().get(0).getId());
            mView.setDescription(currObj.getWeather().get(0).getDescription().toString());
            setStatusBarCaption();
        }
    }

    public static void refreshWidget() {
        Log.i(LOG_TAG,"refreshWidget");
        Context context = MyApplication.getAppContext();
        Intent intent = new Intent(context, AppWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
// Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
// since it seems the onUpdate() is only fired on that:
        int[] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(
                new ComponentName(context, AppWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }


    @Override
    public void updateOnClicked() {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            mView.setUpdateBarText("Too fast!");
            return;
        }
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final WeatherLocation mCurrentLocation = mDb.WeatherLocationDAO().getCurrentLocation();
                if (mCurrentLocation == null) {
                    return;
                }
                mView.getViewActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        city = mCurrentLocation.cityName;
                        RequestData.request();
                        //updateScreenData();
                    }
                });
            }
        });
        mLastClickTime = SystemClock.elapsedRealtime();
    }

    private void renewAutoLocationPlace() {
        FusedLocationProviderClient mFusedLocationClient;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mView.getViewActivity());

        if (ActivityCompat.checkSelfPermission(MyApplication.getAppContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MyApplication.getAppContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(mView.getViewActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(final Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        WeatherLocation autoLocation =
                                                mDb.WeatherLocationDAO().getAutoLocation();
                                        if (autoLocation != null) {
                                            mDb.WeatherLocationDAO().delete(autoLocation);
                                        }
                                        WeatherLocation currentCheck =
                                                mDb.WeatherLocationDAO().getCurrentLocation();
                                        WeatherLocation loc;
                                        if (currentCheck != null) {
                                            loc = new WeatherLocation(
                                                    WeatherLocation.AUTOLOCATION_ID, "Auto Location", " ",
                                                    location.getLatitude(), location.getLongitude(), 1, 0);
                                        } else {
                                            loc = new WeatherLocation(
                                                    WeatherLocation.AUTOLOCATION_ID, "Auto Location", " ",
                                                    location.getLatitude(), location.getLongitude(), 1, 1);
                                        }
                                        mDb.WeatherLocationDAO().insert(loc);
                                        if (currentCheck == null) {
                                            //updateScreenData();
                                        }
                                    }
                                });
                            }
                        }
                    });
        }
    }

    private void addLastHopeLocation() {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                WeatherLocation autoLocation = mDb.WeatherLocationDAO().getAutoLocation();
                if (autoLocation == null) {
                    WeatherLocation currentCheck =
                            mDb.WeatherLocationDAO().getCurrentLocation();
                    WeatherLocation loc;
                    if (currentCheck != null) {
                        loc = new WeatherLocation(
                                WeatherLocation.AUTOLOCATION_ID, "Kyiv", "Ukraine",
                                50.460417, 30.511587
                                , 1, 0);
                    } else {
                        loc = new WeatherLocation(
                                WeatherLocation.AUTOLOCATION_ID, "Kyiv", "Ukraine",
                                50.460417, 30.511587
                                , 1, 1);
                    }
                    mDb.WeatherLocationDAO().insert(loc);
                    updateScreenData();
                }
            }
        });
    }

    private void setStatusBarCaption() {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final WeatherLocation mCurrentLocation = mDb.WeatherLocationDAO().getCurrentLocation();
                if (mCurrentLocation == null) {
                    return;
                }
                mView.getViewActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mView.setStatusBarCaption(mCurrentLocation.cityName, mCurrentLocation.country);
                    }
                });
            }
        });
    }

    private static void saveTempForWidget(Context context, String temperature,String updatetime) {
        SharedPreferences sPref = context.getSharedPreferences(CURRENT_COND, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.putString(CURRENT_TEMPERATURE, temperature);
        editor.commit();
        sPref = context.getSharedPreferences(CURRENT_COND, Context.MODE_PRIVATE);
        editor.putString(CURRENT_UPDATETIME, updatetime);
        editor.commit();
    }

     public static String loadTempForWidget(Context context) {
        SharedPreferences sPref = context.getSharedPreferences(CURRENT_COND_OBJECT, Context.MODE_PRIVATE);
        return sPref.getString(CURRENT_TEMPERATURE, null);
    }

     public static String loadUpdateTimeForWidget(Context context) {
        SharedPreferences sPref = MyApplication.getAppContext().getSharedPreferences(CURRENT_COND, Context.MODE_PRIVATE);
        return sPref.getString(CURRENT_UPDATETIME, null);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(CURRENT_COND_OBJECT)) {
            Log.i(LOG_TAG, "onSharedPreferenceChanged");
            this.updateScreenData();
            refreshWidget();
        }
    }
}

