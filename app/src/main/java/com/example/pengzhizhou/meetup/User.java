package com.example.pengzhizhou.meetup;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by pengzhizhou on 4/25/15.
 */
public class User implements Serializable {
    private String name;
    private String imageName;
    private String realName;
    private String id;
    private String phoneNumber;
    private String description;
    private String uaDescription;
    private Bitmap bitmap;

    public void setName(String n){ name = n; }
    public String getName() { return name; }
    public void setImageName(String in) {imageName = in;}
    public String getImageName() { return imageName; }
    public void setId(String i) {id = i; }
    public String getId() { return id; }
    public String getRealName() { return realName; }
    public void setRealName(String r) { realName = r; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String p) { phoneNumber = p; }
    public String getDescription() { return description; }
    public void setDescription(String d) { description = d; }
    public String getUaDescription() { return uaDescription; }
    public void setUaDescription(String d) { uaDescription = d; }

    public Bitmap getBitmap() { return bitmap; }
    public void setBitmap(Bitmap b) { bitmap = b; }

}
