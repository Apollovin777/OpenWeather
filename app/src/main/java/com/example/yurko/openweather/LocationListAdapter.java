package com.example.yurko.openweather;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
        TwoLineListItem twoLineListItem;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            twoLineListItem = (TwoLineListItem) inflater.inflate(
                    android.R.layout.simple_list_item_2, null);
        } else {
            twoLineListItem = (TwoLineListItem) convertView;
        }

        TextView text1 = twoLineListItem.getText1();
        TextView text2 = twoLineListItem.getText2();

        text1.setText(mLocations.get(position).cityName);
        text2.setText(mLocations.get(position).country);

        return twoLineListItem;
    }
}
