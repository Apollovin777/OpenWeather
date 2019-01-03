package com.example.yurko.openweather;
import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.design.widget.CollapsingToolbarLayout;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

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

    DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle("ttttt");
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        mDrawerLayout = findViewById(R.id.drawer_layout);

        mTextView = findViewById(R.id.main_text);
        mCurrTemp = findViewById(R.id.current_temp);
        mCurrDesc = findViewById(R.id.current_desc);
        mCurrWind = findViewById(R.id.id_wind);
        mCurrCloud = findViewById(R.id.id_cloud);
        mCurrPress = findViewById(R.id.id_text_pressure);
        mCurrHumid = findViewById(R.id.id_humidity);
        mCurrSunRiseSet = findViewById(R.id.id_sunrise);
        mNextAlarmTime = findViewById(R.id.id_alarmtime);

        Log.i("JRequest","onCreate");

        new AsyncTask<Void,Void,Void>(){

            JRequest mJRequest;
            @Override
            protected Void doInBackground(Void... voids) {
                mJRequest = new JRequest("London",false);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                Log.i("JRequest",mJRequest.getResult());
                mCurrent = mJRequest.getResult();
                updateCurrent();
            }
        }.execute();

        AppDatabase db = App.getInstance().getDatabase();
        final WeatherLocationDAO weatherLocationDAO = db.WeatherLocationDAO();
        final WeatherLocation weatherLocation = new WeatherLocation(
                "123","Test","UA",45.45,50.50,
                1,1);
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                List<WeatherLocation> list = weatherLocationDAO.checkCount();
                if(list.isEmpty()) {
                    weatherLocationDAO.insert(weatherLocation);
                }
                return null;
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
                builder.append(current.getDate().toString()+ '\n');

                mTextView.setText(builder.toString());
                mCurrTemp.setText(String.valueOf(current.getTemp()));
                //mCurrDesc.setText(current.getDesc());
                mCurrWind.setText(String.valueOf(current.getWindSpeed()));
                mCurrCloud.setText(String.valueOf(current.getClouds()));
                mCurrPress.setText(String.valueOf(current.getPressure()));
                mCurrHumid.setText(String.valueOf(current.getHumidity()));
                mCurrSunRiseSet.setText(getFormatSunRiseSet(current));

                //String nextAlarm = Settings.System.getString(getContentResolver(),
                 //       Settings.System.NEXT_ALARM_FORMATTED);

                AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
                AlarmManager.AlarmClockInfo clockInfo = alarmManager.getNextAlarmClock();
                if (clockInfo!=null) {
                    long nextAlarmTime = clockInfo.getTriggerTime();
                    Date nextAlarmDate = new Date(nextAlarmTime);
                    android.text.format.DateFormat df = new android.text.format.DateFormat();

                    // Format alarm time as e.g. "Di. 06:30"
                    String nextAlarm = df.format("EEE HH:mm", nextAlarmDate).toString();

                    mNextAlarmTime.setText(nextAlarm);
                }
                else {
                    ImageView alarmImage = findViewById(R.id.id_alarmtime_image);
                    alarmImage.setVisibility(View.GONE);
                }
            }
            catch (JSONException e){
                Log.i("JRequest",e.getMessage());
            }
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
                startActivity(new Intent(this,Locations.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}

