package com.example.pengzhizhou.meetup;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by pengzhizhou on 4/30/15.
 * Class to hold static string and methods to be used by all class
 */
public class Utility {
    private final static String serverUrl = "http://172.20.10.8";

    public static String getServerUrl(){
        return serverUrl;
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static StringBuilder inputStreamToString(InputStream is) {
        String rLine = "";
        StringBuilder answer = new StringBuilder();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));

        try {
            while ((rLine = rd.readLine()) != null) {
                answer.append(rLine);
            }
        }
        catch (IOException e) {
            e.toString();
        }
        return answer;
    }

    //to calculate the dimensions of the bitmap...see comments below
    public static int getSquareCropDimensionForBitmap(Bitmap bitmap)
    {
        int dimension;
        //If the bitmap is wider than it is tall
        //use the height as the square crop dimension
        if (bitmap.getWidth() >= bitmap.getHeight())
        {
            dimension = bitmap.getHeight();
        }
        //If the bitmap is taller than it is wide
        //use the width as the square crop dimension
        else
        {
            dimension = bitmap.getWidth();
        }

        return dimension;
    }

    public static Bitmap getBitmapByType(Resources rs, String type){
        Bitmap bitmap = null;
        int iType = Integer.parseInt(type);
        if (iType == 0){
            bitmap = BitmapFactory.decodeResource(rs, R.drawable.festival_);
        }
        else if (iType == 1){
            bitmap = BitmapFactory.decodeResource(rs, R.drawable.board_);
        }
        else if (iType == 2){
            bitmap = BitmapFactory.decodeResource(rs, R.drawable.room_);
        }
        else if (iType == 3){
            bitmap = BitmapFactory.decodeResource(rs, R.drawable.creative_);
        }
        else if (iType == 4){
            bitmap = BitmapFactory.decodeResource(rs, R.drawable.seminar_);
        }
        else if (iType == 5){
            bitmap = BitmapFactory.decodeResource(rs, R.drawable.movie_);
        }
        else if (iType == 6){
            bitmap = BitmapFactory.decodeResource(rs, R.drawable.sports_);
        }
        else{
            bitmap = BitmapFactory.decodeResource(rs, R.drawable.others_);
        }
        return bitmap;
    }

}
