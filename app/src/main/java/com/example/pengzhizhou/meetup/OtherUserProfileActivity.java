package com.example.pengzhizhou.meetup;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class OtherUserProfileActivity extends ActionBarActivity {

    private String loginUser = null, loginUserId = null, user = null, userId = null, imageName = null;
    private String url = Utility.getServerUrl();
    private ImageView userImg;
    private ListView eventList;
    private List<ActivityItem> itemsList;
    private ListAdapterS adapter = null;
    private Long startIndex = 0L;
    private Long offset = 5L;
    private Button addFriendButton;
    private Button removeFriendButton;
    private DisplayImageOptions options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_profile);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.activity_other_user_profile_actionbar);
        actionBar.setDisplayHomeAsUpEnabled(true);

        options = new DisplayImageOptions.Builder()
                .displayer(new RoundedBitmapDisplayer(100))
                .showImageOnLoading(R.drawable.ic_launcher)
                .showImageForEmptyUri(R.drawable.default_user)
                .showImageOnFail(R.drawable.ic_launcher)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        SharedPreferences settings = getSharedPreferences("MyPrefsFile", 0);
        loginUser = settings.getString("KEY_LOGIN_USER", null);
        loginUserId = settings.getString("KEY_LOGIN_USER_ID", null);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            user = extras.getString("userName");
            imageName = extras.getString("userImg");
            userId = extras.getString("userId");
        }

        addFriendButton = (Button) findViewById(R.id.addFriend);
        if (user!=null && loginUser!=null && user.equals(loginUser)){
            addFriendButton.setVisibility(View.GONE);
        }

        TextView title = (TextView) findViewById(R.id.actionbartitle);
        Utility.setActionBarTitleByLeftMargin(title, this, 1);
        title.setText(user+"的主页");

        userImg = (ImageView)findViewById(R.id.userImg);

        if (!imageName.isEmpty() && imageName != null && !imageName.equals("NULL") && !imageName.equals("null")) {
            String imageUrl = Utility.getServerUrl() + "imgupload/user_image/" + imageName;
            ImageLoader.getInstance().displayImage(imageUrl, userImg, options);
        }
        else{
            ImageLoader.getInstance().displayImage("", userImg, options);
        }

        eventList = (ListView) findViewById(R.id.list);
        eventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Intent i = new Intent(OtherUserProfileActivity.this, EventDetailActivity.class);
                i.putExtra("itemTitle",itemsList.get(position).getTitle());
                i.putExtra("itemImage", itemsList.get(position).getActivityImage());
                i.putExtra("eventTime", itemsList.get(position).getActivityTime());
                i.putExtra("itemAddress", itemsList.get(position).getAddress());
                i.putExtra("itemId", itemsList.get(position).getId());
                i.putExtra("itemType", itemsList.get(position).getActivityType());
                i.putExtra("itemDetail", itemsList.get(position).getDetail());
                i.putExtra("itemCity", itemsList.get(position).getCity());
                i.putExtra("itemState", itemsList.get(position).getState());
                i.putExtra("eventCreator", itemsList.get(position).getEventCreator());
                i.putExtra("duration", itemsList.get(position).getDuration());
                startActivity(i);
            }
        });

        initAdapter();

        new ListJoinEventsTask().execute();

        new CheckIfFriendsTask().execute();
        //addFriendButton.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);
        addFriendButton.setBackgroundColor(Color.parseColor("#04AAF8"));
        addFriendButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if (loginUser == null){
                    Intent myIntent;
                    myIntent = new Intent(OtherUserProfileActivity.this, LoginActivity.class);
                    myIntent.putExtra("fromPage", 0);
                    startActivity(myIntent);

                }else {
                    addFriendCall();
                }
            }
        });

        removeFriendButton = (Button) findViewById(R.id.removeFriend);
        //removeFriendButton.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);
        removeFriendButton.setBackgroundColor(Color.parseColor("#FF7373"));
        removeFriendButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                removeFriendCall();
            }
        });
    }

    private class ListJoinEventsTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {

            String response  = null;
            try{
                URL url1 = new URL(url+"get-events-by-user.php?userId="+userId);
                response = Utility.createConnectionAndGetResponse(url1);
            }
            catch (IOException e){
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String jsonResult) {

            eventList.setAdapter(adapter);
            TextView hostEvents = (TextView)findViewById(R.id.joinEventsCount);
            if (jsonResult == null)
            {
                return;
            }
            else if (jsonResult.equals("[]")){
                hostEvents.setText(hostEvents.getText()+" (0)");
                return;
            }
            try {
                JSONObject jsonResponse = new JSONObject(jsonResult);
                JSONArray jsonMainNode = jsonResponse.optJSONArray("activity_info");
                hostEvents.setText("参加的活动 ("+jsonMainNode.length()+")");
                for (int i = 0; i < jsonMainNode.length(); i++) {
                    JSONArray innerArray = jsonMainNode.optJSONArray(i);
                    for (int j = 0; j < innerArray.length(); j++) {
                        JSONObject jsonChildNode = innerArray.getJSONObject(j);
                        ActivityItem item = new ActivityItem();
                        String id = jsonChildNode.optString("id");
                        String title = jsonChildNode.optString("title");
                        String address = jsonChildNode.optString("activity_address");
                        String activityTime = jsonChildNode.optString("activity_time");
                        String postTime = jsonChildNode.optString("post_time");
                        String duration = jsonChildNode.optString("activity_duration");
                        String pNumber = jsonChildNode.optString("phone_number");
                        String detail = jsonChildNode.optString("activity_detail");
                        String type = jsonChildNode.optString("activity_type");
                        String city = jsonChildNode.optString("city");
                        String state = jsonChildNode.optString("state");
                        String country = jsonChildNode.optString("country");
                        String activityImage = jsonChildNode.optString("image_name");
                        String activityThumbImage = jsonChildNode.optString("image_name");
                        String eventCreator = jsonChildNode.optString("event_creator");

                        item.setActivityImage(activityThumbImage);
                        item.setAddress(address);
                        item.setCity(city);
                        item.setCountry(country);
                        item.setDetail(detail);
                        item.setActivityType(type);
                        item.setId(id);
                        item.setTitle(title);
                        item.setPhoneNumber(pNumber);
                        item.setState(state);
                        item.setDuration(duration);
                        item.setActivityTime(activityTime);
                        item.setPostTime(postTime);
                        item.setEventCreator(eventCreator);

                        itemsList.add(item);
                    }
                }
            } catch (JSONException e) {
                Toast.makeText(OtherUserProfileActivity.this.getApplicationContext(), "Error" + e.toString(),
                        Toast.LENGTH_SHORT).show();
            }

            adapter.notifyDataSetChanged();

            if (itemsList.size() > 0) {
                startIndex = startIndex + itemsList.size();
            }
            super.onPostExecute(jsonResult);

            Utility.setListViewHeightBasedOnChildren(eventList);
        }


    }
    public void initAdapter(){
        itemsList = new ArrayList<ActivityItem>();
        adapter = new ListAdapterS(this, R.layout.list_event_row, itemsList);
    }


    public boolean addFriendCall(){
        new AddFriendTask().execute();
        return true;
    }

    public class AddFriendTask extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... params) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url + "add-friend.php");
            String result = null;

            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("userName", loginUser));
            nameValuePairs.add(new BasicNameValuePair("otherUserName", user));

            // Execute HTTP Post Request
            try {
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            try {
                HttpResponse response = httpclient.execute(httppost);
                result = Utility.inputStreamToString(
                        response.getEntity().getContent()).toString();
            }

            catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(final String result) {

            if (result!=null && result.equals("success")){

                Toast.makeText(getApplicationContext(),
                        "关注成功",
                        Toast.LENGTH_LONG).show();

                Intent i = getIntent();
                i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                i.putExtra("userImg", imageName);
                i.putExtra("userName", user);
                i.putExtra("loginUser", loginUser);
                finish();
                startActivity(i);

            }else{
                Toast.makeText(getApplicationContext(),
                        result,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private class CheckIfFriendsTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url+"check-friend.php");
            String result = null;
            //add name value pair for the country code
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("userName",String.valueOf(loginUser)));
            nameValuePairs.add(new BasicNameValuePair("otherUserName",String.valueOf(user)));
            try {
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            try {
                HttpResponse response = httpclient.execute(httppost);
                result = Utility.inputStreamToString(
                        response.getEntity().getContent()).toString();
            }

            catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            //can
            if (result.equals("0") && user!=null && !user.equals(loginUser)){
                removeFriendButton.setVisibility(View.GONE);
                addFriendButton.setVisibility(View.VISIBLE);
            }
            else if (result.equals("0") && user!=null && user.equals(loginUser)){
                removeFriendButton.setVisibility(View.GONE);
                addFriendButton.setVisibility(View.GONE);
            }
            else if (result.equals("1")){
                addFriendButton.setVisibility(View.GONE);
                removeFriendButton.setVisibility(View.VISIBLE);
            }
            else{
                Toast.makeText(getApplicationContext(),
                        result,
                        Toast.LENGTH_LONG).show();
            }
        }


    }

    public boolean removeFriendCall(){
        new RemoveFriendTask().execute();
        return true;
    }


    public class RemoveFriendTask extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... params) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url + "remove-friend.php");
            String result = null;

            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("userName", loginUser));
            nameValuePairs.add(new BasicNameValuePair("otherUserName", user));

            // Execute HTTP Post Request
            try {
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            try {
                HttpResponse response = httpclient.execute(httppost);
                result = Utility.inputStreamToString(
                        response.getEntity().getContent()).toString();
            }

            catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(final String result) {

            if (result!=null && result.equals("success")){

                Toast.makeText(getApplicationContext(),
                        "成功取消关注",
                        Toast.LENGTH_LONG).show();

                Intent i = getIntent();
                i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                i.putExtra("userImg", imageName);
                i.putExtra("userName", user);
                i.putExtra("loginUser", loginUser);
                finish();
                startActivity(i);

            }else{
                Toast.makeText(getApplicationContext(),
                        result,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
