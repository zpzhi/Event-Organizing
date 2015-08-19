package com.example.pengzhizhou.meetup;

/**
 * City filter helper class
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
    private ArrayList<Province> provinceList;
    private ArrayList<Province> originalList;

    // constructor
    public CityFilterAdapter(Context context, ArrayList<Province> provinceList) {
        this.context = context;
        this.provinceList = new ArrayList<Province>();
        this.provinceList.addAll(provinceList);
        this.originalList = new ArrayList<Province>();
        this.originalList.addAll(provinceList);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        ArrayList<City> countryList = provinceList.get(groupPosition).getCityList();
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

        ArrayList<City> countryList = provinceList.get(groupPosition).getCityList();
        return countryList.size();

    }

    @Override
    public Object getGroup(int groupPosition) {
        return provinceList.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return provinceList.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isLastChild, View view,
                             ViewGroup parent) {

        Province province = (Province) getGroup(groupPosition);
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.group_row, null);
        }

        TextView heading = (TextView) view.findViewById(R.id.heading);
        heading.setText(province.getName().trim());

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
        Log.v("CityFilterAdapter", String.valueOf(provinceList.size()));
        provinceList.clear();

        if(query.isEmpty()){
            provinceList.addAll(originalList);
        }
        else {

            for(Province province: originalList){

                ArrayList<City> countryList = province.getCityList();
                ArrayList<City> newList = new ArrayList<City>();
                for(City country: countryList){
                    if(country.getCode().toLowerCase().contains(query) ||
                            country.getName().toLowerCase().contains(query)){
                        newList.add(country);
                    }
                }
                if(newList.size() > 0){
                    Province nProvince = new Province(province.getName(),newList);
                    provinceList.add(nProvince);
                }
            }
        }

        Log.v("CityFilterAdapter", String.valueOf(provinceList.size()));
        notifyDataSetChanged();

    }

}