package com.example.yurko.openweather;
import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    private AppDatabase mDb;

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

        Log.i("JRequest","onCreate");

        setAutoLocationPlace();

// get location for coordinates -----------------------------------



//-----------------------------------------------------------

        new AsyncTask<Void,Void,Void>(){

            JRequest mJRequest;

            @Override
            protected void onPreExecute() {
                mUpdatedStatus.setText(R.string.downloading);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                WeatherLocation location = mDb.WeatherLocationDAO().getCurrentLocation();
                if (location != null) {
                    mJRequest = new JRequest(false,location.latitude,location.longitude);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                Log.i("JRequest",mJRequest.getResult());
                mCurrent = mJRequest.getResult();
                updateCurrent();

            }
        }.execute();

    }

    private void updateCurrent(){
        if (mCurrent != null){
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

                //String nextAlarm = Settings.System.getString(getContentResolver(),
                 //       Settings.System.NEXT_ALARM_FORMATTED);

                AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
                AlarmManager.AlarmClockInfo clockInfo = alarmManager.getNextAlarmClock();
                android.text.format.DateFormat df = new android.text.format.DateFormat();
                if (clockInfo!=null) {
                    long nextAlarmTime = clockInfo.getTriggerTime();
                    Date nextAlarmDate = new Date(nextAlarmTime);

                    // Format alarm time as e.g. "Di. 06:30"
                    String nextAlarm = df.format("EEE HH:mm", nextAlarmDate).toString();

                    mNextAlarmTime.setText(nextAlarm);
                }
                else {
                    ImageView alarmImage = findViewById(R.id.id_alarmtime_image);
                    alarmImage.setVisibility(View.GONE);
                }

                String updateTime = df.format("HH:mm", new Date()).toString();
                String currCondTime = df.format("HH:mm", current.getDate()).toString();
                String updated = getResources().getString(R.string.updated);
                mUpdatedStatus.setText(updated + " " + updateTime + " / " + currCondTime);
            }
            catch (JSONException e){
                Log.i("JRequest",e.getMessage());
            }
        }
    }

    private void setCurrCondImage(String descriptionID) {

        if( descriptionID == null){
            mCurrCondImage.setImageResource(R.drawable.cond_na);
            return;
        }
        int id = Integer.parseInt(descriptionID.substring(0,1));
        switch (id){
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

    private String getFormatSunRiseSet(JSONParseCurrent jObj){
        try {

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(jObj.getSunRiseTime()*1000);

            StringBuilder builder = new StringBuilder();
            builder.append(timeFormat.format(calendar.getTime()));

            builder.append(" - ");

            calendar.setTimeInMillis(jObj.getSunSetTime()*1000);
            builder.append(timeFormat.format(calendar.getTime()));

            return builder.toString();
        }
        catch (JSONException e){
            Log.i("JRequest",e.getMessage());
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.menu_location:
                startActivity(new Intent(this,LocationsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setAutoLocationPlace(){
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                List<WeatherLocation> list = mDb.WeatherLocationDAO().getAutoLocation();
                if(list.isEmpty()) {
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
                                    "99999","Auto Location",null,
                                    location.getLatitude(),location.getLongitude(),1,0
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

    protected void checkPermissions() {
        final List<String> missingPermissions = new ArrayList<String>();
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

}

