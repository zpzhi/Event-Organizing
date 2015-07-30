package com.example.pengzhizhou.meetup;

/**
 * Created by pengzhizhou on 5/16/15.
 */
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CityFilterAdapter extends BaseExpandableListAdapter {

    private Context context;
    private ArrayList<Province> continentList;
    private ArrayList<Province> originalList;

    public CityFilterAdapter(Context context, ArrayList<Province> continentList) {
        this.context = context;
        this.continentList = new ArrayList<Province>();
        this.continentList.addAll(continentList);
        this.originalList = new ArrayList<Province>();
        this.originalList.addAll(continentList);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        ArrayList<City> countryList = continentList.get(groupPosition).getCityList();
        return countryList.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View view, ViewGroup parent) {

        City country = (City) getChild(groupPosition, childPosition);
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.child_row, null);
        }

        TextView name = (TextView) view.findViewById(R.id.name);
        name.setText(country.getName().trim());

        return view;
    }

    @Override
    public int getChildrenCount(int groupPosition) {

        ArrayList<City> countryList = continentList.get(groupPosition).getCityList();
        return countryList.size();

    }

    @Override
    public Object getGroup(int groupPosition) {
        return continentList.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return continentList.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isLastChild, View view,
                             ViewGroup parent) {

        Province continent = (Province) getGroup(groupPosition);
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.group_row, null);
        }

        TextView heading = (TextView) view.findViewById(R.id.heading);
        heading.setText(continent.getName().trim());

        return view;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void filterData(String query){

        query = query.toLowerCase();
        Log.v("CityFilterAdapter", String.valueOf(continentList.size()));
        continentList.clear();

        if(query.isEmpty()){
            continentList.addAll(originalList);
        }
        else {

            for(Province continent: originalList){

                ArrayList<City> countryList = continent.getCityList();
                ArrayList<City> newList = new ArrayList<City>();
                for(City country: countryList){
                    if(country.getCode().toLowerCase().contains(query) ||
                            country.getName().toLowerCase().contains(query)){
                        newList.add(country);
                    }
                }
                if(newList.size() > 0){
                    Province nContinent = new Province(continent.getName(),newList);
                    continentList.add(nContinent);
                }
            }
        }

        Log.v("CityFilterAdapter", String.valueOf(continentList.size()));
        notifyDataSetChanged();

    }

}