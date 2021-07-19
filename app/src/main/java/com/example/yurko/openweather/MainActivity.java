package com.example.yurko.openweather;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static String LOG_TAG="firstactive";

    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    private AppDatabase mDb;
    private RequestCurrentCond mRequestCurrentCond;

    String mCurrent;
    String mForecast;

    TextView mTextView;
    TextView mCurrTemp;
    TextView mCurrDesc;

    TextView mCurrWind;
    TextView mCurrCloud;
    TextView mCurrPress;
    TextView mCurrHumid;
    TextView mCurrSunRiseSet;
    TextView mNextAlarmTime;
    TextView mUpdatedStatus;
    TextView mCurrCityName;
    TextView mCurrCountryName;
    ImageView mCurrCondImage;

    DrawerLayout mDrawerLayout;
    CollapsingToolbarLayout mCollapsingToolbarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDb = AppDatabase.getInstance(getApplicationContext());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

        mCollapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);

        mCurrCityName = findViewById(R.id.currCityName);
        mCurrCountryName = findViewById(R.id.currCountryName);


        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final WeatherLocation weatherLocation = mDb.WeatherLocationDAO().getCurrentLocation();
                if (weatherLocation != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mCollapsingToolbarLayout.setTitle(weatherLocation.cityName);
                            mCurrCityName.setText(weatherLocation.cityName);
                            mCurrCountryName.setText(weatherLocation.country);
                        }
                    });
                }
            }
        });
        mCollapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        mDrawerLayout = findViewById(R.id.drawer_layout);

        mCurrTemp = findViewById(R.id.current_temp);
        mCurrDesc = findViewById(R.id.current_desc);
        mCurrWind = findViewById(R.id.id_wind);
        mCurrCloud = findViewById(R.id.id_cloud);
        mCurrPress = findViewById(R.id.id_text_pressure);
        mCurrHumid = findViewById(R.id.id_humidity);
        mCurrSunRiseSet = findViewById(R.id.id_sunrise);
        mNextAlarmTime = findViewById(R.id.id_alarmtime);
        mUpdatedStatus = findViewById(R.id.id_updateStatus);
        mCurrCondImage = findViewById(R.id.weatherImage);

        setAutoLocationPlace();

// get location for coordinates -----------------------------------
        mRequestCurrentCond = new RequestCurrentCond();
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Log.i(LOG_TAG,"run()");
                WeatherLocation location = mDb.WeatherLocationDAO().getAutoLocation();
                Log.i(LOG_TAG,location.cityName);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(LOG_TAG,"runOnUiThread");
                        mRequestCurrentCond.setListener(new RequestCurrentCond.RequestCurrentCondListener() {

                            @Override
                            public void onExampleAsyncTaskFinished(String value) {
                                Log.i(LOG_TAG,"onExampleAsyncTaskFinished");
                                mCurrent = value;
                                Log.i(LOG_TAG,mCurrent);
                                updateCurrent();
                            }
                        });
                    }
                });
                mRequestCurrentCond.execute(location);
                Log.i(LOG_TAG,"execute in OnCreate");
            }
        });

//-----------------------------------------------------------

//        new AsyncTask<Void,Void,Void>(){
//
//            JRequest mJRequest;
//
//            @Override
//            protected void onPreExecute() {
//                mUpdatedStatus.setText(R.string.downloading);
//            }
//
//            @Override
//            protected Void doInBackground(Void... voids) {
//                WeatherLocation location = mDb.WeatherLocationDAO().getCurrentLocation();
//                if (location != null) {
//                    mJRequest = new JRequest(false,location.latitude,location.longitude);
//                }
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Void aVoid) {
//                //Log.i("JRequest",mJRequest.getResult());
//                if(mJRequest != null) {
//                    mCurrent = mJRequest.getResult();
//                    updateCurrent();
//                }
//            }
//        }.execute();

    }

    private void updateCurrent() {
        Log.i(LOG_TAG,"updateCurrent");
        if (mCurrent != null) {
            JSONParseCurrent current = null;
            try {
                JSONObject rootObject = new JSONObject(mCurrent);
                current = new JSONParseCurrent(rootObject);
                StringBuilder builder = new StringBuilder();
                builder.append(current.getCityName() + '\n');

                //mTextView.setText(builder.toString());
                mCurrTemp.setText(String.valueOf(current.getTemp()));
                mCurrDesc.setText(current.getDesc());
                mCurrWind.setText(String.valueOf(current.getWindSpeed()));
                mCurrCloud.setText(String.valueOf(current.getClouds()));
                mCurrPress.setText(String.valueOf(current.getPressure()));
                mCurrHumid.setText(String.valueOf(current.getHumidity()));
                mCurrSunRiseSet.setText(getFormatSunRiseSet(current));

                setCurrCondImage(current.getDescriptionID());

                AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
                AlarmManager.AlarmClockInfo clockInfo = alarmManager.getNextAlarmClock();
                android.text.format.DateFormat df = new android.text.format.DateFormat();
                if (clockInfo != null) {
                    long nextAlarmTime = clockInfo.getTriggerTime();
                    Date nextAlarmDate = new Date(nextAlarmTime);

                    // Format alarm time as e.g. "Di. 06:30"
                    String nextAlarm = df.format("EEE HH:mm", nextAlarmDate).toString();

                    mNextAlarmTime.setText(nextAlarm);
                } else {
                    ImageView alarmImage = findViewById(R.id.id_alarmtime_image);
                    alarmImage.setVisibility(View.GONE);
                }

                String updateTime = df.format("HH:mm", new Date()).toString();
                String currCondTime = df.format("HH:mm", current.getDate()).toString();
                String updated = getResources().getString(R.string.updated);
                mUpdatedStatus.setText(updated + " " + updateTime + " / " + currCondTime);
            } catch (JSONException e) {
                Log.i("JRequest", e.getMessage());
            }
        }
    }

    private void setCurrCondImage(String descriptionID) {

        if (descriptionID == null) {
            mCurrCondImage.setImageResource(R.drawable.cond_na);
            return;
        }
        int id = Integer.parseInt(descriptionID.substring(0, 1));
        switch (id) {
            case 2:
                mCurrCondImage.setImageResource(R.drawable.cond_day_thunderstorm);
                break;
            case 3:
                mCurrCondImage.setImageResource(R.drawable.cond_drizzle);
                break;
            case 5:
                mCurrCondImage.setImageResource(R.drawable.cond_rain);
                break;
            case 6:
                mCurrCondImage.setImageResource(R.drawable.cond_snow);
                break;
            case 7:
                mCurrCondImage.setImageResource(R.drawable.cond_fog);
                break;
            case 8:
                mCurrCondImage.setImageResource(R.drawable.cond_day_sunny);
                break;
        }
    }

    private String getFormatSunRiseSet(JSONParseCurrent jObj) {
        try {

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(jObj.getSunRiseTime() * 1000);

            StringBuilder builder = new StringBuilder();
            builder.append(timeFormat.format(calendar.getTime()));

            builder.append(" - ");

            calendar.setTimeInMillis(jObj.getSunSetTime() * 1000);
            builder.append(timeFormat.format(calendar.getTime()));

            return builder.toString();
        } catch (JSONException e) {
            Log.i("JRequest", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.menu_location:
                startActivityForResult(new Intent(this, LocationsActivity.class), 1);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setAutoLocationPlace() {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                WeatherLocation autoLocation = mDb.WeatherLocationDAO().getAutoLocation();
                if (autoLocation == null) {
                    addAutoLocation();
                }
            }
        });
    }

    private void addAutoLocation() {
        FusedLocationProviderClient mFusedLocationClient;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        checkPermissions();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkPermissions();
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            final WeatherLocation loc = new WeatherLocation(
                                    WeatherLocation.AUTOLOCATION_ID, "Auto Location", null,
                                    location.getLatitude(), location.getLongitude(), 1, 1
                            );
                            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                                @Override
                                public void run() {
                                    mDb.WeatherLocationDAO().insert(loc);
                                }
                            });
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final WeatherLocation weatherLocation = mDb.WeatherLocationDAO().getCurrentLocation();
                if (weatherLocation != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mCollapsingToolbarLayout.setTitle(weatherLocation.cityName);
                            mCurrCityName.setText(weatherLocation.cityName);
                            mCurrCountryName.setText(weatherLocation.country);
                        }
                    });
                }
            }
        });

        mRequestCurrentCond = new RequestCurrentCond();
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                WeatherLocation location = mDb.WeatherLocationDAO().getCurrentLocation();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRequestCurrentCond.setListener(new RequestCurrentCond.RequestCurrentCondListener() {
                            @Override
                            public void onExampleAsyncTaskFinished(String value) {
                                Log.i(LOG_TAG,"onExampleAsyncTaskFinished");
                                Log.i(LOG_TAG,value);
                                mCurrent = value;
                                updateCurrent();
                            }
                        });
                    }
                });
                mRequestCurrentCond.execute(location);
            }
        });
    }

    protected void checkPermissions() {
        final List<String> missingPermissions = new ArrayList<>();
        // check all required dynamic permissions
        for (final String permission : REQUIRED_SDK_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
            // request all missing permissions
            final String[] permissions = missingPermissions
                    .toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            final int[] grantResults = new int[REQUIRED_SDK_PERMISSIONS.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,
                    grantResults);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int index = permissions.length - 1; index >= 0; --index) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        // exit the app if one permission is not granted
                        Toast.makeText(this, "Required permission '" + permissions[index]
                                + "' not granted, exiting", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                // all permissions were granted
                break;
        }
    }

    static class RequestCurrentCond extends AsyncTask<WeatherLocation, String, String> {
        private RequestCurrentCondListener listener;

        @Override
        protected String doInBackground(WeatherLocation... location) {
            WeatherLocation currLocation = location[0];
            if (location != null) {
                JRequest jRequest = new JRequest(false, currLocation.latitude, currLocation.longitude);
                return jRequest.getResult();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String value) {

            if (listener != null) {
                Log.i(LOG_TAG,"onPostExecute");
                listener.onExampleAsyncTaskFinished(value);
            }
        }

        public void setListener(RequestCurrentCondListener listener) {
            this.listener = listener;
        }

        public interface RequestCurrentCondListener {
            void onExampleAsyncTaskFinished(String value);
        }
    }
}

