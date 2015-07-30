package com.example.pengzhizhou.meetup;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by pengzhizhou on 4/30/15.
 * Class to hold static string and methods to be used by all class
 */
public class Utility {
    //private final static String serverUrl = "http://loquimeetup.w174.mc-test.com/meetup-web/";
    //private final static String serverUrl = "http://meetup.wcpsjshxnna.com/meetup-web/";
    private final static String serverUrl = "http://hyu1714700001.my3w.com/";
    //private final static String serverUrl = "http://192.168.0.12/meetup-web/";

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
        else if (iType == 7){
            bitmap = BitmapFactory.decodeResource(rs, R.drawable.travel_);
        }
        else{
            bitmap = BitmapFactory.decodeResource(rs, R.drawable.others_);
        }
        return bitmap;
    }

    /**** Method for Setting the Height of the ListView dynamically.
     **** Hack to fix the issue of not showing all the items of the ListView
     **** when placed inside a ScrollView  ****/
    // function 2: when too many activities under this user, try a way to let them scroll instead
    // of covering the button "logout"
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        Adapter listAdapter = null;
        if(listView.getAdapter() instanceof ListAdapterS){
            listAdapter = (ListAdapterS)listView.getAdapter();
        }
        else if (listView.getAdapter() instanceof UserListAdapter){
            listAdapter = (UserListAdapter)listView.getAdapter();
        }
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;

        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
            if (i == 3) break;
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();

    }

    public static void createGridArray(Resources res, ArrayList<Item> gridArray, ArrayList<Integer> resource, int flag){
        //Bitmap userIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.zhuoyou)
        if (flag == 1) {
            gridArray.add(new Item(null, "全部活动"));
            resource.add(R.drawable.quanbu);
        }
        gridArray.add(new Item(null, "节日派对"));
        gridArray.add(new Item(null, "桌游聚会"));
        gridArray.add(new Item(null, "密室逃脱"));
        gridArray.add(new Item(null, "创意展览"));
        gridArray.add(new Item(null, "行业讲座"));
        gridArray.add(new Item(null, "电影鉴赏"));
        gridArray.add(new Item(null, "体育活动"));
        gridArray.add(new Item(null, "旅游同行"));
        gridArray.add(new Item(null, "其他类别"));

        resource.add(R.drawable.jieri);
        resource.add(R.drawable.zhuoyou);
        resource.add(R.drawable.mishi);
        resource.add(R.drawable.chuangye);
        resource.add(R.drawable.jiangzuo);
        resource.add(R.drawable.dianying);
        resource.add(R.drawable.tiyu);
        resource.add(R.drawable.zijiayou);
        resource.add(R.drawable.qita);

    }

}
