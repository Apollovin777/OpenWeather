package com.example.yurko.openweather;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TwoLineListItem;

import java.util.ArrayList;

public class LocationListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<WeatherLocation> mLocations;

    public LocationListAdapter(Context context, ArrayList<WeatherLocation> locations) {
        this.context = context;
        mLocations = locations;
    }

    public void setLocations(ArrayList<WeatherLocation> locations) {
        mLocations = locations;
    }

    @Override
    public int getCount() {
        return mLocations.size();
    }

    @Override
    public Object getItem(int position) {
        return mLocations.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mLocations.get(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //View rowView = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
        View rowView = inflater.inflate(R.layout.locations_listview_item, parent, false);

        WeatherLocation location = mLocations.get(position);

        TextView cityName = rowView.findViewById(R.id.cityNameId);
        TextView countryName = rowView.findViewById(R.id.countryNameId);
        ImageView imageView = rowView.findViewById(R.id.currentLocationId);

        cityName.setText(location.cityName);
        countryName.setText(location.country);

        if(location.currentLocation == 1){
            imageView.setVisibility(View.VISIBLE);
        }

        if(location.recordType == 1){
            String latitude = String.valueOf(round(location.latitude,5));
            String longitude = String.valueOf(round(location.longitude,5));
            countryName.setText(String.valueOf(latitude + ", " + longitude));
        }


        return rowView;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}
