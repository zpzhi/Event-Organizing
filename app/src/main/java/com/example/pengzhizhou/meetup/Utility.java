package com.example.pengzhizhou.meetup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pengzhizhou on 4/30/15.
 * Class to hold static string and methods to be used by all class
 */
public class Utility {
    //private final static String serverUrl = "http://loquimeetup.w174.mc-test.com/meetup-web/";
    //private final static String serverUrl = "http://meetup.wcpsjshxnna.com/meetup-web/";
    private final static String serverUrl = "http://www.luoke-xy.cn/";
    //private final static String serverUrl = "http://192.168.0.12/meetup-web/";
    public final static DisplayImageOptions
            eventOptions = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.ic_launcher)
            .showImageForEmptyUri(R.drawable.ic_launcher)
            .showImageOnFail(R.drawable.ic_launcher)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    public final static DisplayImageOptions
            userOptions = new DisplayImageOptions.Builder()
            .displayer(new RoundedBitmapDisplayer(50))
            .showImageOnLoading(R.drawable.default_user)
            .showImageForEmptyUri(R.drawable.default_user)
            .showImageOnFail(R.drawable.default_user)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    public final static DisplayImageOptions
            options_round_100 = new DisplayImageOptions.Builder()
            .displayer(new RoundedBitmapDisplayer(100))
            .showImageOnLoading(R.drawable.ic_launcher)
            .showImageForEmptyUri(R.drawable.default_user)
            .showImageOnFail(R.drawable.ic_launcher)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    public final static DisplayImageOptions
            options_round_40 = new DisplayImageOptions.Builder()
            .displayer(new RoundedBitmapDisplayer(40))
            .showImageOnLoading(R.drawable.ic_launcher)
            .showImageForEmptyUri(R.drawable.default_user)
            .showImageOnFail(R.drawable.ic_launcher)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    public static String getServerUrl() {
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
        } catch (IOException e) {
            e.toString();
        }
        return answer;
    }

    //to calculate the dimensions of the bitmap...see comments below
    public static int getSquareCropDimensionForBitmap(Bitmap bitmap) {
        int dimension;
        //If the bitmap is wider than it is tall
        //use the height as the square crop dimension
        if (bitmap.getWidth() >= bitmap.getHeight()) {
            dimension = bitmap.getHeight();
        }
        //If the bitmap is taller than it is wide
        //use the width as the square crop dimension
        else {
            dimension = bitmap.getWidth();
        }

        return dimension;
    }

    public static Bitmap getBitmapByType(Resources rs, String type) {
        Bitmap bitmap = null;
        int iType = Integer.parseInt(type);
        if (iType == 0) {
            bitmap = BitmapFactory.decodeResource(rs, R.drawable.festival_);
        } else if (iType == 1) {
            bitmap = BitmapFactory.decodeResource(rs, R.drawable.board_);
        } else if (iType == 2) {
            bitmap = BitmapFactory.decodeResource(rs, R.drawable.room_);
        } else if (iType == 3) {
            bitmap = BitmapFactory.decodeResource(rs, R.drawable.creative_);
        } else if (iType == 4) {
            bitmap = BitmapFactory.decodeResource(rs, R.drawable.seminar_);
        } else if (iType == 5) {
            bitmap = BitmapFactory.decodeResource(rs, R.drawable.movie_);
        } else if (iType == 6) {
            bitmap = BitmapFactory.decodeResource(rs, R.drawable.sports_);
        } else if (iType == 7) {
            bitmap = BitmapFactory.decodeResource(rs, R.drawable.travel_);
        } else {
            bitmap = BitmapFactory.decodeResource(rs, R.drawable.others_);
        }
        return bitmap;
    }

    /**
     * * Method for Setting the Height of the ListView dynamically.
     * *** Hack to fix the issue of not showing all the items of the ListView
     * *** when placed inside a ScrollView  ***
     */
    // function 2: when too many activities under this user, try a way to let them scroll instead
    // of covering the button "logout"
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        Adapter listAdapter = null;
        if (listView.getAdapter() instanceof ListAdapterS) {
            listAdapter = (ListAdapterS) listView.getAdapter();
        } else if (listView.getAdapter() instanceof UserListAdapter) {
            listAdapter = (UserListAdapter) listView.getAdapter();
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
            if (i == 2) break;
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();

    }

    public static void createGridArray(Resources res, ArrayList<Item> gridArray, ArrayList<Integer> resource, int flag) {
        //Bitmap userIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.zhuoyou)
        if (flag == 1) {
            gridArray.add(new Item(null, "全部活动"));
            resource.add(R.drawable.quanbu_1);
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

        resource.add(R.drawable.jieri_1);
        resource.add(R.drawable.zhuoyou_1);
        resource.add(R.drawable.mishi_1);
        resource.add(R.drawable.chuangye_1);
        resource.add(R.drawable.jiangzuo_1);
        resource.add(R.drawable.dianying_1);
        resource.add(R.drawable.tiyu_1);
        resource.add(R.drawable.zijiayou_1);
        resource.add(R.drawable.qita_1);

    }

    public static HttpURLConnection createConnection(URL url) {
        HttpURLConnection httpConnection = null;
        try {
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setReadTimeout(100000);
            httpConnection.setConnectTimeout(150000);
            httpConnection.setRequestMethod("GET");
            httpConnection.setDoInput(true);
            httpConnection.setDoOutput(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return httpConnection;
    }


    public static String createConnectionAndGetResponse(URL url) {
        StringBuilder response = new StringBuilder();

        try {
            HttpURLConnection httpconn = (HttpURLConnection) url.openConnection();
            httpconn.setReadTimeout(10000);
            httpconn.setConnectTimeout(30000);
            httpconn.setRequestMethod("GET");
            httpconn.setDoInput(true);
            httpconn.setDoOutput(true);
            if (httpconn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader input = new BufferedReader(new InputStreamReader(httpconn.getInputStream(), "UTF-8"), 8192);
                String strLine = null;
                while ((strLine = input.readLine()) != null) {
                    response.append(strLine);
                }
                input.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response.toString();
    }

    public static void setActionBarTitleByMargin(TextView actionBarTitle, Activity act, int direction, int adjust) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) actionBarTitle.getLayoutParams();
        int displayWidth = act.getWindowManager().getDefaultDisplay().getWidth();

        if (direction == 0) {
            params.leftMargin = displayWidth / adjust;
        } else {
            params.rightMargin = displayWidth / adjust;
        }
        actionBarTitle.setLayoutParams(params);
    }


    public static void setCustomChildEventViewByItemContent(View v, ActivityItem p) {
        if (p != null) {
            TextView title = (TextView) v.findViewById(R.id.activityTitle);
            ImageView thumbN = (ImageView) v.findViewById(R.id.thumbImage);
            TextView time = (TextView) v.findViewById(R.id.activityTime);

            String titleA = p.getTitle();
            String timeA = p.getActivityTime();
            String imageName = p.getActivityImage();
            //Bitmap bitmap = p.getThumbBitmap();
            if (!imageName.isEmpty() && imageName != null && !imageName.equals("NULL") && !imageName.equals("null")) {
                String imageUrl = Utility.getServerUrl() + "imgupload/activity_thumb_image/" + imageName;
                ImageLoader.getInstance().displayImage(imageUrl, thumbN, eventOptions);
            } else {
                thumbN.setImageResource(R.drawable.jieri);
                int iType = Integer.parseInt(p.getActivityType());

                if (iType == 0) {
                    thumbN.setImageResource(R.drawable.festival_);
                } else if (iType == 1) {
                    thumbN.setImageResource(R.drawable.board_);
                } else if (iType == 2) {
                    thumbN.setImageResource(R.drawable.room_);
                } else if (iType == 3) {
                    thumbN.setImageResource(R.drawable.creative_);
                } else if (iType == 4) {
                    thumbN.setImageResource(R.drawable.seminar_);
                } else if (iType == 5) {
                    thumbN.setImageResource(R.drawable.movie_);
                } else if (iType == 6) {
                    thumbN.setImageResource(R.drawable.sports_);
                } else if (iType == 7) {
                    thumbN.setImageResource(R.drawable.travel_);
                } else {
                    thumbN.setImageResource(R.drawable.others_);
                }
            }

            if (title != null) {
                title.setText(titleA);
            }

            if (time != null) {
                time.setText(timeA);
            }
        }
    }

    public static void setCustomChildUserViewByItemContent(View v, User user){
        if (user != null) {

            TextView name = (TextView)v.findViewById(R.id.userName);
            ImageView thumbN = (ImageView) v.findViewById(R.id.thumbImage);
            TextView description = (TextView)v.findViewById(R.id.userDescription);

            String nameA = user.getName();
            String descriptionA = user.getDescription();
            String uaDescription = user.getUaDescription();
            String imageName = user.getImageName();

            if (!imageName.isEmpty() && imageName != null && !imageName.equals("NULL") && !imageName.equals("null")) {
                String imageUrl = Utility.getServerUrl() + "imgupload/user_thumb_image/" + imageName;
                ImageLoader.getInstance().displayImage(imageUrl, thumbN, userOptions);
            }else{
                ImageLoader.getInstance().displayImage("", thumbN, userOptions);
            }

            if (name != null) {
                name.setText(nameA);
            }

            if (description != null) {
                if (descriptionA != null || (uaDescription!= null && !uaDescription.equals("NULL"))){
                    description.setVisibility(View.VISIBLE);
                }

                if (uaDescription!= null && !uaDescription.equals("NULL")){
                    description.setText(uaDescription);
                }
                else{
                    description.setText(descriptionA);
                }

            }
        }
    }

    // iterate to add all the join users into layout
    public static void addUserChildViewToLayout(LinearLayout layout, Activity activity, List<User> list, int resource, int count, int limit){
        if (count < limit && count < list.size()){
            View child = activity.getLayoutInflater().inflate(resource, null);
            setCustomChildUserViewByItemContent(child, list.get(count));
            child.setOnClickListener(new OnUserClickListener(activity,list.get(count)));
            layout.addView(child);
            count++;
            addUserChildViewToLayout(layout, activity, list, resource, count, limit);
        }
    }

    // iterate to add all the events into layout
    public static void addEventChildViewToLayout(LinearLayout layout, Activity activity, List<ActivityItem> list, int resource, int count, int limit){
        if (count < limit && count < list.size()){
            View child = activity.getLayoutInflater().inflate(resource, null);
            setCustomChildEventViewByItemContent(child, list.get(count));
            child.setOnClickListener(new OnEventClickListener(activity,list.get(count)));
            layout.addView(child);
            count++;
            addEventChildViewToLayout(layout, activity, list, resource, count, limit);
        }
    }

    public static void dismissProgressDialog(ProgressDialog progressLoading){
        try{
            if(progressLoading.isShowing()){
                progressLoading.dismiss();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally
        {
            progressLoading.dismiss();
        }
    }

}

class OnEventClickListener implements View.OnClickListener {
    private Activity activity;
    private ActivityItem item;

    public OnEventClickListener(Activity a, ActivityItem i){
        activity = a;
        item = i;
    }

    @Override
    public void onClick(View v) {
        Intent i = new Intent(activity, EventDetailActivity.class);
        i.putExtra("itemTitle", item.getTitle());
        i.putExtra("itemImage", item.getActivityImage());
        i.putExtra("eventTime", item.getActivityTime());
        i.putExtra("itemAddress", item.getAddress());
        i.putExtra("itemId", item.getId());
        i.putExtra("itemType", item.getActivityType());
        i.putExtra("itemDetail", item.getDetail());
        i.putExtra("itemCity", item.getCity());
        i.putExtra("itemState", item.getState());
        i.putExtra("eventCreator", item.getEventCreator());
        i.putExtra("duration", item.getDuration());
        activity.startActivity(i);
    }
}

class OnUserClickListener implements View.OnClickListener {
    private Activity activity;
    private User user;

    public OnUserClickListener(Activity a, User u){
        activity = a;
        user = u;
    }

    @Override
    public void onClick(View v) {
        Intent i = new Intent(activity, OtherUserProfileActivity.class);
        i.putExtra("userImg", user.getImageName());
        i.putExtra("userName", user.getName());
        i.putExtra("userId", user.getId());
        activity.startActivity(i);
    }
}
