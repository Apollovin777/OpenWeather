package com.example.yurko.openweather;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Locations extends AppCompatActivity implements View.OnClickListener {
    private static final String LOG_TAG = "COORD";

    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;

    private Boolean isFabOpen = false;
    private FloatingActionButton mFab,mFabSearch,mFabPoint;
    private Animation fab_open,fab_close,rotate_forward,rotate_backward;
    private ListView mListView;

    /**
     * Permissions that need to be explicitly requested from end user.
     */
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFab = findViewById(R.id.fab);
        mFabSearch = findViewById(R.id.fab_search);
        mFabPoint = findViewById(R.id.fab_point);

        mFab.setOnClickListener(this);
        mFabSearch.setOnClickListener(this);
        mFabPoint.setOnClickListener(this);

        fab_open = AnimationUtils.loadAnimation(this, R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(this,R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(this,R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(this,R.anim.rotate_backward);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
                            Log.i(LOG_TAG, "COORDINATES");
                            Log.i("LOG_TAG getLongitude", String.valueOf(location.getLongitude()));
                            Log.i("LOG_TAG getLatitude", String.valueOf(location.getLatitude()));
                        }
                    }
                });

        mListView = findViewById(R.id.locations_view);
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(parent.getContext());
                builder.setTitle(R.string.remove)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                AppDatabase db = App.getInstance().getDatabase();
                                final WeatherLocationDAO weatherLocationDAO = db.WeatherLocationDAO();
                                final WeatherLocation weatherLocation = (WeatherLocation)parent.getItemAtPosition(position);

                                new AsyncTask<WeatherLocation,Void,Void >(){
                                    ArrayList<WeatherLocation> arrayList;

                                    @Override
                                    protected Void doInBackground(WeatherLocation... weatherLocations) {
                                        weatherLocationDAO.delete(weatherLocation);
                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Void aVoid) {
                                        super.onPostExecute(aVoid);
                                        loadLocations();
                                    }
                                }.execute();
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
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.fab:
                animateFAB();
                break;
            case R.id.fab_search:
                Log.d("Raj", "Fab 1");
                Intent intent = new Intent(this,SearchLocation.class);
                startActivityForResult(intent,1);
                break;
            case R.id.fab_point:
                Log.d("Raj", "Fab 2");
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {return;}
        loadLocations();
    }

    public void animateFAB(){
        if(isFabOpen){
            mFab.startAnimation(rotate_backward);
            mFabPoint.startAnimation(fab_close);
            mFabSearch.startAnimation(fab_close);
            mFabPoint.setClickable(false);
            mFabSearch.setClickable(false);
            isFabOpen = false;
            mFab.setBackgroundTintList(getResources().getColorStateList(R.color.colorAccent));
            Log.d("Raj", "close");

        } else {
            mFab.startAnimation(rotate_forward);
            mFabPoint.startAnimation(fab_open);
            mFabSearch.startAnimation(fab_open);
            mFabPoint.setClickable(true);
            mFabSearch.setClickable(true);
            isFabOpen = true;
            //mFab.setBackgroundTintList(getResources().getColorStateList(R.color.colorGrey));
            //mFab.setBackgroundColor(getResources().getColor(R.color.colorGrey));
            Log.d("Raj","open");
        }
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
                        finish();
                        return;
                    }
                }
                // all permissions were granted

                break;
        }
    }

    private void loadLocations(){

        AppDatabase db = App.getInstance().getDatabase();
        final WeatherLocationDAO weatherLocationDAO = db.WeatherLocationDAO();
        ArrayList<WeatherLocation> arrayList = new ArrayList<>();
        final LocationListAdapter adapter =  new LocationListAdapter(this,arrayList);
        mListView.setAdapter(adapter);

        new AsyncTask<Void,Void,ArrayList<WeatherLocation> >(){
            ArrayList<WeatherLocation> arrayList;
            @Override
            protected ArrayList<WeatherLocation> doInBackground(Void... voids) {
                List<WeatherLocation> list = weatherLocationDAO.checkCount();
                arrayList = new ArrayList<>();
                arrayList.addAll(list);
                return arrayList;
            }

            @Override
            protected void onPostExecute(ArrayList<WeatherLocation> weatherLocations) {
                super.onPostExecute(weatherLocations);
                adapter.setLocations(weatherLocations);
                adapter.notifyDataSetChanged();
            }
        }.execute();
    }
}
