package com.example.pengzhizhou.meetup;

/**
 * Created by pengzhizhou on 5/16/15.
 */
public class City {

    private String code = "";
    private String name = "";

    public City(String code, String name) {
        super();
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

}