package com.example.yurko.openweather;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class SearchLocation extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_location);

        Toolbar toolbar = findViewById(R.id.toolbarSearch);
        setSupportActionBar(toolbar);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        PlaceAutocompleteFragment places= (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        places.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                Toast.makeText(getApplicationContext(),place.getName(),Toast.LENGTH_SHORT).show();

                AppDatabase db = App.getInstance().getDatabase();
                final WeatherLocationDAO weatherLocationDAO = db.WeatherLocationDAO();
                LatLng latLng =  place.getLatLng();
                final WeatherLocation weatherLocation = new WeatherLocation(
                  "test",
                        place.getName().toString(),
                        place.getAddress().toString().substring(place.getAddress().toString().lastIndexOf(',')+1),
                        latLng.latitude,
                        latLng.longitude,
                        2,
                        1
                );
                new AsyncTask<Void,Void,Void >(){
                    ArrayList<WeatherLocation> arrayList;
                    @Override
                    protected Void doInBackground(Void... voids) {
                        weatherLocationDAO.insert(weatherLocation);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        Intent intent = new Intent();
                        setResult(RESULT_OK,intent);
                        finish();
                    }
                }.execute();


            }

            @Override
            public void onError(Status status) {

                Toast.makeText(getApplicationContext(),status.toString(),Toast.LENGTH_SHORT).show();

            }
        });
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                .build();
        places.setFilter(typeFilter);
    }
}
