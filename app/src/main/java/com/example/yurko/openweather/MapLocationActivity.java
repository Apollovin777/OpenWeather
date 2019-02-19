package com.example.yurko.openweather;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.yurko.openweather.LocationIq.ApiClient;
import com.example.yurko.openweather.LocationIq.ApiException;
import com.example.yurko.openweather.LocationIq.Configuration;
import com.example.yurko.openweather.LocationIq.auth.*;
import com.locationiq.client.api.ReverseApi;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.locationiq.client.model.Address;

import java.math.BigDecimal;

public class MapLocationActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

    private static String LOG_TAG = "MapLocationActivity";

    private GoogleMap mMap;
    private Marker mMarker;
    private AppDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_location);

        Toolbar toolbar = findViewById(R.id.toolbarMap);
        setSupportActionBar(toolbar);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);


        FusedLocationProviderClient mFusedLocationClient;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                LatLng currLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currLocation, 15.0f));
                                mMarker = mMap.addMarker(new MarkerOptions().position(currLocation));
                                //marker.setDraggable(true);
                            }
                        }
                    });

            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);

            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng point) {
                    mMap.clear();
                    mMarker = mMap.addMarker(new MarkerOptions().position(point));
                }
            });


        } else {
            // Show rationale and request permission.
        }
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
    }

    @Override
    public boolean onMyLocationButtonClick() {
        mMap.animateCamera(CameraUpdateFactory.zoomBy(15.0f));
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_map_position:
                mDb = AppDatabase.getInstance(getApplicationContext());
                final LatLng latLng = mMarker.getPosition();
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        getLocation(latLng);
                    }
                });

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getLocation(LatLng latLng){
        ApiClient defaultClient = Configuration.getDefaultApiClient();

        // Configure API key authorization: key
        ApiKeyAuth keys = (ApiKeyAuth)defaultClient.getAuthentication("key");

        keys.setApiKey(BuildConfig.locIQ_key);
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //keys.setApiKeyPrefix("ffffffffff");

        ReverseApi apiInstance = new ReverseApi();
        BigDecimal lat = BigDecimal.valueOf(latLng.latitude); // BigDecimal | Latitude of the location to generate an address for.
        BigDecimal lon = BigDecimal.valueOf(latLng.longitude); // BigDecimal | Longitude of the location to generate an address for.
        String format = "json"; // String | Format to geocode. Only JSON supported for SDKs
        Integer normalizecity = 1; // Integer | Normalizes village to city level data to city
        Integer addressdetails = 1; // Integer | Include a breakdown of the address into elements. Defaults to 1.
        String acceptLanguage = "en"; // String | Preferred language order for showing search results, overrides the value specified in the Accept-Language HTTP header. Defaults to en. To use native language for the response when available, use accept-language=native
        Integer namedetails = 0; // Integer | Include a list of alternative names in the results. These may include language variants, references, operator and brand.
        Integer extratags = 0; // Integer | Include additional information in the result if available, e.g. wikipedia link, opening hours.
        Integer statecode = 0; // Integer | Adds state or province code when available to the statecode key inside the address element. Currently supported for addresses in the USA, Canada and Australia. Defaults to 0
        try {
            com.locationiq.client.model.Location result = apiInstance.reverse(lat, lon, format, normalizecity, addressdetails, acceptLanguage, namedetails, extratags, statecode);
            Address address = result.getAddress();

            mDb = AppDatabase.getInstance(getApplicationContext());

            final WeatherLocation weatherLocation = new WeatherLocation(
                    "test",
                    address.getCity(),
                    address.getCountry(),
                    Double.valueOf(result.getLat()),
                    Double.valueOf(result.getLon()),
                    2,
                    1
            );

            long id = mDb.WeatherLocationDAO().insert(weatherLocation);
            mDb.WeatherLocationDAO().updateCurrentLocation(id);

            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();

        } catch (ApiException e) {
            Log.e(LOG_TAG,"Exception when calling ReverseApi#reverse");
            Log.e(LOG_TAG,  e.getResponseBody());
        }
    }
}
