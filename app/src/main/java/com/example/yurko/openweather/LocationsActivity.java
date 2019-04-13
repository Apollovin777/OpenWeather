package com.example.yurko.openweather;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class LocationsActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String LOG_TAG = "COORD";

    private Boolean isFabOpen = false;
    private FloatingActionButton mFab, mFabSearch, mFabPoint;
    private Animation fab_open, fab_close, rotate_forward, rotate_backward;
    private ListView mListView;
    private AppDatabase mDb;
    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDb = AppDatabase.getInstance(getApplicationContext());

        mFab = findViewById(R.id.fab);
        mFabSearch = findViewById(R.id.fab_search);
        mFabPoint = findViewById(R.id.fab_point);

        mFab.setOnClickListener(this);
        mFabSearch.setOnClickListener(this);
        mFabPoint.setOnClickListener(this);

        fab_open = AnimationUtils.loadAnimation(this, R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(this, R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(this, R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(this, R.anim.rotate_backward);

        mListView = findViewById(R.id.locations_view);
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(parent.getContext());
                builder.setTitle(R.string.remove)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                final WeatherLocation weatherLocation = (WeatherLocation) parent.getItemAtPosition(position);
                                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.i("weatherLocation",weatherLocation.cityId);
                                        if (!weatherLocation.cityId.equals(WeatherLocation.AUTOLOCATION_ID)) {
                                            if(weatherLocation.currentLocation==1){
                                                makeCurrent(mDb.WeatherLocationDAO().getAutoLocation());
                                            }
                                            mDb.WeatherLocationDAO().delete(weatherLocation);
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    loadLocations();
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .show();
                return true;
            }
        });

        loadLocations();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final WeatherLocation weatherLocation = (WeatherLocation) parent.getItemAtPosition(position);
                makeCurrent(weatherLocation);
            }
        });
    }

    private void makeCurrent(final WeatherLocation weatherLocation){
        weatherLocation.currentLocation = 1;
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.WeatherLocationDAO().updateCurrentLocation(weatherLocation.id);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadLocations();
                    }
                });
            }
        });
        mIntent = new Intent();
        setResult(RESULT_OK,mIntent);
        finish();
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.fab:
                animateFAB();
                break;
            case R.id.fab_search:
                Intent intentSearch = new Intent(this, SearchLocationActivity.class);
                animateFAB();
                startActivityForResult(intentSearch, 1);
                break;
            case R.id.fab_point:
                Intent intentMap = new Intent(this, MapLocationActivity.class);
                animateFAB();
                startActivityForResult(intentMap, 1);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        mIntent = new Intent();
        setResult(RESULT_OK,mIntent);
        finish();
    }

    public void animateFAB() {
        if (isFabOpen) {
            mFab.startAnimation(rotate_backward);
            mFabPoint.startAnimation(fab_close);
            mFabSearch.startAnimation(fab_close);
            mFabPoint.setClickable(false);
            mFabSearch.setClickable(false);
            isFabOpen = false;
            mFab.setBackgroundTintList(getResources().getColorStateList(R.color.colorAccent));
        } else {
            mFab.startAnimation(rotate_forward);
            mFabPoint.startAnimation(fab_open);
            mFabSearch.startAnimation(fab_open);
            mFabPoint.setClickable(true);
            mFabSearch.setClickable(true);
            isFabOpen = true;
            //mFab.setBackgroundTintList(getResources().getColorStateList(R.color.colorGrey));
            //mFab.setBackgroundColor(getResources().getColor(R.color.colorGrey));
        }
    }

    private void loadLocations() {

        final ArrayList<WeatherLocation> arrayList = new ArrayList<>();
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final List<WeatherLocation> list = mDb.WeatherLocationDAO().getall();
                arrayList.addAll(list);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LocationListAdapter adapter = new LocationListAdapter(getApplicationContext(), arrayList);
                        mListView.setAdapter(adapter);
                    }
                });
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return true;
    }
}
