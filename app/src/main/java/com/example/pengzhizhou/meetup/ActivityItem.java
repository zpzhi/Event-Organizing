package com.example.pengzhizhou.meetup;

import android.graphics.Bitmap;

/**
 * Created by pengzhizhou on 4/17/15.
 */

class ActivityItem {

    private String title;
    private String id;
    private String address;
    private String postTime;
    private String activityTime;
    private String duration;
    private String phoneNumber;
    private String detail;
    private String city;
    private String state;
    private String country;
    private String activityImage;
    private String eventCreator;
    private Bitmap bitmap;
    private Bitmap thumbBitmap;

    public ActivityItem(){}

    public void setTitle(String t){
        title = t;
    }

    public String getTitle(){
        return title;
    }

    public void setId(String i){
        id = i;
    }

    public String getId(){
        return id;
    }

    public void setAddress(String a){
        address = a;
    }

    public String getAddress(){
        return address;
    }


    public void setDuration(String d){
        duration = d;
    }

    public String getDuration(){
        return duration;
    }

    public void setPhoneNumber(String pn){
        phoneNumber = pn;
    }

    public String getPhoneNumber(){
        return phoneNumber;
    }

    public void setPostTime(String pt){
        postTime = pt;
    }

    public String getPostTime(){
        return postTime;
    }

    public void setActivityTime(String at){
        activityTime = at;
    }

    public String getActivityTime(){
        return activityTime;
    }

    public String getActivityDate() { return activityTime.substring(0, 10); }

    public void setDetail(String d){
        detail = d;
    }

    public String getDetail(){
        return detail;
    }

    public void setCity(String c){
        city = c;
    }

    public String getCity(){
        return city;
    }

    public void setState(String s){
        state = s;
    }

    public String getState(){
        return state;
    }

    public void setCountry(String c){
        country = c;
    }

    public String getCountry(){
        return country;
    }

    public void setActivityImage(String ai){
        activityImage = ai;
    }

    public String getActivityImage(){
        return activityImage;
    }

    public void setEventCreator(String ec) { eventCreator = ec; }

    public String getEventCreator() { return eventCreator; }

    public Bitmap getBitmap() { return bitmap; }
    public void setBitmap(Bitmap b) { bitmap = b; }

    public Bitmap getThumbBitmap() { return thumbBitmap; }
    public void setThumbBitmap(Bitmap b) { thumbBitmap = b; }
}
