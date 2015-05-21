package com.example.pengzhizhou.meetup;

/**
 * Created by pengzhizhou on 5/16/15.
 */
import java.util.ArrayList;

public class Province {

    private String name;
    private ArrayList<City> cityList = new ArrayList<City>();

    public Province(String name, ArrayList<City> cityList) {
        super();
        this.name = name;
        this.cityList = cityList;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public ArrayList<City> getCityList() {
        return cityList;
    }
    public void setCityList(ArrayList<City> cityList) {
        this.cityList = cityList;
    };


}