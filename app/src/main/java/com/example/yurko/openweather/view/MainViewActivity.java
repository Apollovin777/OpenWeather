package com.example.yurko.openweather.view;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yurko.openweather.JSONParseCurrent;
import com.example.yurko.openweather.JSONParseForecast;
import com.example.yurko.openweather.LocationsActivity;
import com.example.yurko.openweather.R;
import com.example.yurko.openweather.Utilities;
import com.example.yurko.openweather.presenter.MainPresenter;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class MainViewActivity extends AppCompatActivity implements MainView {

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

    TextView mForecastTextView;

    DrawerLayout mDrawerLayout;
    CollapsingToolbarLayout mCollapsingToolbarLayout;

    private MainPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_view);
        init();
        presenter = new MainPresenter(this);
        presenter.viewIsReady();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        presenter.onPermissionsResult(requestCode,permissions,grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        presenter.updateCurrentConditionData();
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
            case R.id.menu_update:
                presenter.updateClicked();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void init() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null){
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        mCurrCityName = findViewById(R.id.currCityName);
        mCurrCountryName = findViewById(R.id.currCountryName);

        mCollapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        mCollapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

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
        mForecastTextView = findViewById(R.id.id_txt_forecast);
    }

    @Override
    public void setCurrentCondiiton(String data) {
        if (data != null) {
            JSONParseCurrent current;
            try {
                JSONObject rootObject = new JSONObject(data);
                current = new JSONParseCurrent(rootObject);
                StringBuilder builder = new StringBuilder();
                builder.append(current.getCityName() + '\n');

                mCurrTemp.setText(String.valueOf(current.getTemp()));
                mCurrDesc.setText(current.getDesc());
                mCurrWind.setText(String.valueOf(current.getWindSpeed()));
                mCurrCloud.setText(String.valueOf(current.getClouds()));
                mCurrPress.setText(String.valueOf(current.getPressure()));
                mCurrHumid.setText(String.valueOf(current.getHumidity()));
                mCurrSunRiseSet.setText(Utilities.getFormatSunRiseSet(current));

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

    @Override
    public void setForecast(String data){
        if (data != null) {
            JSONParseForecast forecast;
            try {
                JSONObject rootObject = new JSONObject(data);
                forecast = new JSONParseForecast(rootObject);
                Date[] arrayDates = forecast.getDatesArray();
                String[] arrayTemps = forecast.getTempsArray();

                SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-mm-dd HH:mm");
                timeFormat.setTimeZone(TimeZone.getDefault());

                StringBuilder builder = new StringBuilder();
                for (int i=0;i<arrayDates.length;i++) {
                    builder.append(timeFormat.format(arrayDates[i]));
                    builder.append(" ");
                    builder.append(String.valueOf(arrayTemps[i]));
                    builder.append("\n");
                }

                mForecastTextView.setText(builder.toString());

            } catch (JSONException e) {
                Log.i("JRequest", e.getMessage());
            }
        }
    }

    @Override
    public void showToast(String info) {
        Toast.makeText(this,info,Toast.LENGTH_LONG).show();
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

    @Override
    public void setStatusBarCaption(String city, String country) {
        mCollapsingToolbarLayout.setTitle(city);
        mCurrCityName.setText(city);
        mCurrCountryName.setText(country);
    }

    @Override
    public void setLoadingBar(String data) {
        mUpdatedStatus.setText(data);
    }

    @Override
    public AppCompatActivity getViewActivity() {
        return this;
    }
}
