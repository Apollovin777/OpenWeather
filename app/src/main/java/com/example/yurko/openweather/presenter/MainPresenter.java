package com.example.yurko.openweather.presenter;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.example.yurko.openweather.model.MyApplication;
import com.example.yurko.openweather.model.WeatherLocation;
import com.example.yurko.openweather.model.AppDatabase;
import com.example.yurko.openweather.AppExecutors;
import com.example.yurko.openweather.JRequest;
import com.example.yurko.openweather.view.MainView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainPresenter implements CurrentConditionLoadListener, Presenter {

    private final static String LOG_TAG = "tester";
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    private final static int REQUEST_CURRENT_CONDITIONS = 0;
    private final static int REQUEST_FORECAST = 1;

    private RequestCurrentCond mRequestCurrentCond;
    private String mCurrent;
    private String mForecast;
    private AppDatabase mDb;
    private MainView mView;
    //private WeatherLocation mCurrentLocation;
    private long mLastClickTime;

    public MainPresenter(MainView view) {
        mView = view;
        mDb = AppDatabase.getInstance(MyApplication.getAppContext());
    }

    public void viewIsReady() {
        updateCurrentConditionData();

        if (ActivityCompat.checkSelfPermission(MyApplication.getAppContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MyApplication.getAppContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            renewAutoLocationPlace();
        }
        else{
            requestPermissions();
        }
        loadForecast();
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

    public void updateCurrentConditionData() {
        Log.i(LOG_TAG,"updateCurrentConditionData");
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                WeatherLocation mCurrentLocation = mDb.WeatherLocationDAO().getCurrentLocation();
                if (mCurrentLocation == null) {
                    return;
                }
                RequestCurrentCond requestCurrentCond = new RequestCurrentCond();
                requestCurrentCond.setListener(MainPresenter.this);
                requestCurrentCond.execute(mCurrentLocation);
            }
        });
    }

    public void loadForecast(){
        Log.i(LOG_TAG,"START FORECAST");
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                WeatherLocation mCurrentLocation = mDb.WeatherLocationDAO().getCurrentLocation();
                if (mCurrentLocation == null) {
                    return;
                }
                Log.i(LOG_TAG,"FORECAST......");
                RequestForecast requestForecast = new RequestForecast();
                requestForecast.setListener(MainPresenter.this);
                requestForecast.execute(mCurrentLocation);
            }
        });
    }

    @Override
    public void updateClicked() {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
            mView.setLoadingBar("Too fast!");
            return;
        }
        updateCurrentConditionData();
        mLastClickTime = SystemClock.elapsedRealtime();
    }

    public void renewAutoLocationPlace() {
        Log.i(LOG_TAG,"Start renewAutoLocationPlace");

                addAutoLocation();

    }

    private void addAutoLocation() {
        Log.i(LOG_TAG,"Start addAutoLocation");

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
                                        if (autoLocation != null){
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
                                            updateCurrentConditionData();
                                            loadForecast();
                                        }
                                    }
                                });
                            }
                        }
                    });
        }
    }
//TODO avoid duplicate current conditions request
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
                    updateCurrentConditionData();
                }
            }
        });
    }

    private void setStatusBarCaption(){
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

    @Override
    public void onLoading() {
        mView.getViewActivity().runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        mView.setLoadingBar("Loading...");
                    }
                });
    }

    @Override
    public void onLoadFinished(int requestKind,String value) {
        String res;
        if (requestKind == REQUEST_CURRENT_CONDITIONS)
            res = "REQUEST_CURRENT_CONDITIONS";
        else
            res = "REQUEST_CURRENT_CONDITIONS";
        Log.i(LOG_TAG,"REQUEST COMPLETED" + res);


        if (value != null) {
            if (requestKind == REQUEST_CURRENT_CONDITIONS) {
                mView.setCurrentCondiiton(value);
                setStatusBarCaption();
            }
            else if(requestKind == REQUEST_FORECAST){
                mForecast = value;
                mView.setForecast(value);
                //Log.i(LOG_TAG,value);
            }
        }
    }

    static class RequestCurrentCond extends AsyncTask<WeatherLocation, Void, String> {
        private CurrentConditionLoadListener listener;

        @Override
        protected String doInBackground(WeatherLocation... location) {
            listener.onLoading();
            try {
                WeatherLocation currLocation = location[0];
                if (currLocation != null) {
                    JRequest jRequest = new JRequest(false, currLocation.latitude, currLocation.longitude);
                    return jRequest.getResult();
                }
            } catch (Exception e) {
                Log.e("RequestCurrentCond", e.getMessage());
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String value) {
            listener.onLoadFinished(REQUEST_CURRENT_CONDITIONS,value);
        }

        public void setListener(CurrentConditionLoadListener listener) {
            this.listener = listener;
        }
    }

    static class RequestForecast extends AsyncTask<WeatherLocation, Void, String> {
        private CurrentConditionLoadListener listener;

        @Override
        protected String doInBackground(WeatherLocation... location) {
            try {
                WeatherLocation currLocation = location[0];
                if (currLocation != null) {
                    JRequest jRequest = new JRequest(true, currLocation.latitude, currLocation.longitude);
                    return jRequest.getResult();
                }
            } catch (Exception e) {
                Log.e("RequestForecast", e.getMessage());
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String value) {
            listener.onLoadFinished(REQUEST_FORECAST,value);
        }

        public void setListener(CurrentConditionLoadListener listener) {
            this.listener = listener;
        }
    }


}
