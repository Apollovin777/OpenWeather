package com.example.yurko.openweather.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import com.example.yurko.openweather.SettingsActivity;
import com.example.yurko.openweather.model.MyApplication;
import com.example.yurko.openweather.presenter.RequestData;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.yurko.openweather.JSONParseForecast;
import com.example.yurko.openweather.LocationsActivity;
import com.example.yurko.openweather.R;
import com.example.yurko.openweather.presenter.MainPresenter;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;



public class MainViewActivity extends AppCompatActivity implements MainView, NavigationView.OnNavigationItemSelectedListener {

    private static final String LOG_TAG = "MainViewActivity";
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

    LinearLayout mForecastLinearLayout;

    DrawerLayout mDrawerLayout;
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    private static final String CURRENT_COND_OBJECT = "current_cond_object";

    private MainPresenter presenter;
    SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences sharedPreferences =  MyApplication.getAppContext().getSharedPreferences(CURRENT_COND_OBJECT,Context.MODE_PRIVATE);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(presenter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_view);

        init();
        startService();
        presenter = new MainPresenter(this);
        presenter.viewIsReady();

        SharedPreferences sharedPreferences =  MyApplication.getAppContext().getSharedPreferences(CURRENT_COND_OBJECT,Context.MODE_PRIVATE);
        sharedPreferences.registerOnSharedPreferenceChangeListener(presenter);
    }

    private void startService(){
        Log.i(LOG_TAG,"startService func");
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
        RequestData.request();
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
                presenter.updateOnClicked();
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
        //mForecastTextView = findViewById(R.id.id_txt_forecast);
        mForecastLinearLayout = findViewById(R.id.id_forecast_view);

//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.addDrawerListener(toggle);
//        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nvView);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void setForecast(String data){
        Log.i(LOG_TAG,"setForecast");
        if (data != null) {
            JSONParseForecast forecast;
            try {
                JSONObject rootObject = new JSONObject(data);
                forecast = new JSONParseForecast(rootObject);
                Date[] arrayDates = forecast.getDatesArray();
                String[] arrayTemps = forecast.getTempsArray();

                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                timeFormat.setTimeZone(TimeZone.getDefault());

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(arrayDates[0]);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                mForecastLinearLayout.removeAllViews();
                LinearLayout linearLayout = addColumn(arrayDates[0]);

                for (int i=0;i<arrayDates.length;i++) {
//                    builder.append(timeFormat.format(arrayDates[i]));
//                    builder.append(" ");
//                    builder.append(String.valueOf(arrayTemps[i]));
//                    builder.append("\n");

                    calendar.setTime(arrayDates[i]);
                    if (day != calendar.get(Calendar.DAY_OF_MONTH)){
                        linearLayout = addColumn(arrayDates[i]);
                    }
                    TextView textView = new TextView(this);
                    textView.setTextSize(12f);
                    textView.append(timeFormat.format(arrayDates[i]));
                    textView.append("  ");
                    textView.append(arrayTemps[i]);
                    textView.append("°C");
                    //LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                            //LinearLayout.LayoutParams.WRAP_CONTENT);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(             //select linearlayoutparam- set the width & height
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                    textView.setGravity(Gravity.LEFT);
                    params.setMargins(0,0,0,20);
                    textView.setLayoutParams(params);
                    linearLayout.addView(textView);

                    day = calendar.get(Calendar.DAY_OF_MONTH);

                }
                //mForecastTextView.setText(builder.toString());

            }
            catch (JSONException e) {
                Log.i("JRequest", e.getMessage());
            }
        }
        else{
            LinearLayout linearLayout = findViewById(R.id.id_forecast_view);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            TextView textView = new TextView(this);
            textView.setText("\\_(\")_/");
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            textView.setGravity(Gravity.CENTER);
            params.setMargins(8,8,8,8);
            textView.setLayoutParams(params);

            linearLayout.addView(textView);
        }
    }

    private LinearLayout addColumn(Date date){
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(8, 8, 16, 8);
        column.setLayoutParams(params);

        TextView textViewDay = new TextView(this);
        textViewDay.setTextSize(16f);
        String dayOfWeek = new SimpleDateFormat("EEEE").format(date);
        dayOfWeek = dayOfWeek.substring(0,1).toUpperCase() + dayOfWeek.substring(1);
        textViewDay.setText(dayOfWeek);
        textViewDay.setGravity(Gravity.CENTER);
        column.addView(textViewDay);

        TextView textViewDate = new TextView(this);
        textViewDate.setTextSize(14f);
        textViewDate.setText(new SimpleDateFormat("dd MMMM").format(date));
        textViewDate.setGravity(Gravity.CENTER);
        column.addView(textViewDate);

        mForecastLinearLayout.addView(column);
        return column;
    }

    @Override
    public void setTemp(String temp) {
        mCurrTemp.setText(temp);
    }

    @Override
    public void setDescription(String description) {
        mCurrDesc.setText(description);
    }

    @Override
    public void setWindSpeed(String speed) {
        mCurrWind.setText(speed);
    }

    @Override
    public void setCloud(String cloud) {
        mCurrCloud.setText(cloud);
    }

    @Override
    public void setPressure(String pressure) {
        mCurrPress.setText(pressure);
    }

    @Override
    public void setHumidity(String humidity) {
        mCurrHumid.setText(humidity);
    }

    @Override
    public void setSunRiseSetTime(String riseSetTime) {
        mCurrSunRiseSet.setText(riseSetTime);
        //mCurrSunRiseSet.setText(Utilities.getFormatSunRiseSet(current));
    }

    @Override
    public void setNexAlarmTime(String alarmTime) {
//        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
//        AlarmManager.AlarmClockInfo clockInfo = alarmManager.getNextAlarmClock();
//        android.text.format.DateFormat df = new android.text.format.DateFormat();
//        if (clockInfo != null) {
//            long nextAlarmTime = clockInfo.getTriggerTime();
//            Date nextAlarmDate = new Date(nextAlarmTime);
//
//            // Format alarm time as e.g. "Di. 06:30"
//            String nextAlarm = df.format("EEE HH:mm", nextAlarmDate).toString();
//
//            mNextAlarmTime.setText(nextAlarm);
//        } else {
//            ImageView alarmImage = findViewById(R.id.id_alarmtime_image);
//            alarmImage.setVisibility(View.GONE);
//        }
    }

    @Override
    public void setCCImage(Integer id) {
        setCurrCondImage(String.valueOf(id));
    }

    @Override
    public void setUpdateBarText(String text) {
       mUpdatedStatus.setText(text);
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
    public AppCompatActivity getViewActivity() {
        return this;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.nav_update:
                presenter.updateOnClicked();
                break;
            case R.id.nav_location:
                startActivityForResult(new Intent(this, LocationsActivity.class), 1);
                break;
            case R.id.nav_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.check_service:
                MainPresenter.checkService();
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
