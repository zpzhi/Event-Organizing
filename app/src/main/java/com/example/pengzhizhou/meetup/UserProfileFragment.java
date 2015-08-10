package com.example.pengzhizhou.meetup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class UserProfileFragment extends Fragment {
    private String loginUser;
    private String loginUserId;
    private GetUserDetailTask getUserDetailTask;
    private String url = Utility.getServerUrl();
    private User item;
    private ImageView userImg;
    private ListView eventList;
    private ListView eventListView1;
    private ListView friendsListView;
    private List<ActivityItem> itemsList;
    private List<ActivityItem> itemsList1;
    private List<User> friendsList;
    private UserListAdapter uAdapter = null;
    private ListAdapterS adapter = null, adapter1 = null;
    private Bitmap bt;
    private View _rootView;
    Long startIndex = 0L;
    Long offset = 5L;
    private User user;
    private Bitmap mIcon;
    private LinearLayout friendsListRoot;
    private DisplayImageOptions options;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        SharedPreferences settings = this.getActivity().getSharedPreferences("MyPrefsFile", 0);
        loginUser = settings.getString("KEY_LOGIN_USER", null);
        loginUserId = settings.getString("KEY_LOGIN_USER_ID", null);

        ImageView pullDownIcon = (ImageView)getActivity().findViewById(R.id.pulldown);
        pullDownIcon.setVisibility(View.GONE);

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

        if (loginUser == null){
            Intent myIntent;
            myIntent = new Intent(getActivity(), LoginActivity.class);
            startActivity(myIntent);

        }else {
            //if (_rootView == null) {
                _rootView = inflater.inflate(R.layout.user_profile_view, container, false);

                ((TabHostActivity) getActivity())
                        .setActionBarTitle(loginUser + "的主页");
                ((TabHostActivity) getActivity()).setImageViewable(View.VISIBLE);
                ((TabHostActivity) getActivity()).setSearchCityViewable(View.GONE);
                userImg = (ImageView) _rootView.findViewById(R.id.userImg);

                getUserDetailTask = new GetUserDetailTask();
                getUserDetailTask.execute(loginUser);

                eventListView1 = (ListView) _rootView.findViewById(R.id.list2);
                eventListView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {

                        Intent i = new Intent(getActivity(), EventDetailActivity.class);
                        i.putExtra("itemTitle", itemsList1.get(position).getTitle());
                        i.putExtra("itemImage", itemsList1.get(position).getActivityImage());
                        i.putExtra("eventTime", itemsList1.get(position).getActivityTime());
                        i.putExtra("itemAddress", itemsList1.get(position).getAddress());
                        i.putExtra("itemId", itemsList1.get(position).getId());
                        i.putExtra("itemType", itemsList1.get(position).getActivityType());
                        i.putExtra("itemDetail", itemsList1.get(position).getDetail());
                        i.putExtra("itemCity", itemsList1.get(position).getCity());
                        i.putExtra("itemState", itemsList1.get(position).getState());
                        i.putExtra("eventCreator", itemsList1.get(position).getEventCreator());
                        i.putExtra("duration", itemsList1.get(position).getDuration());
                        startActivity(i);
                    }
                });
                initAdapter1();
                new ListHostingEventsTask().execute();


                eventList = (ListView) _rootView.findViewById(R.id.list);
                eventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {

                        Intent i = new Intent(getActivity(), EventDetailActivity.class);
                        i.putExtra("itemTitle", itemsList.get(position).getTitle());
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
                new ListJoinEventsTask(getActivity()).execute();

                Button logout = (Button) _rootView.findViewById(R.id.logout);
                logout.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);
                logout.setOnClickListener(new Button.OnClickListener()  {
                    public void onClick(View v) {
                        SharedPreferences preferences = getActivity().getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.clear();
                        editor.commit();
                        getActivity().finish();

                        Intent myIntent;
                        myIntent = new Intent(getActivity(), MainActivity.class);
                        startActivity(myIntent);
                    }
                });

                initFriendListAdapter();
                friendsListView = (ListView) _rootView.findViewById(R.id.list1);
                friendsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        Intent i = null;
                        i = new Intent(getActivity(), OtherUserProfileActivity.class);
                        i.putExtra("userImg", friendsList.get(position).getImageName());
                        i.putExtra("userName", friendsList.get(position).getName());
                        i.putExtra("userId", friendsList.get(position).getId());
                        startActivity(i);
                    }
                });
                new FetchFriendsList().execute();

                ImageView edit = (ImageView) getActivity().findViewById(R.id.editInfo);
                edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent myIntent;
                        myIntent = new Intent(getActivity(), UserProfileEditActivity.class);
                        myIntent.putExtra("userInformation", user);
                        startActivity(myIntent);
                    }
                });
            //} //else {
                // Do not inflate the layout again.
                // The returned View of onCreateView will be added into the fragment.
                // However it is not allowed to be added twice even if the parent is same.
                // So we must remove _rootView from the existing parent view group
                // (it will be added back).
             //   ((ViewGroup)_rootView.getParent()).removeView(_rootView);
            //}

        }
        return _rootView;
    }


    public class GetUserDetailTask extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... params) {
            StringBuilder response  = new StringBuilder();
            try{
                URL url1 = new URL(url+"get-user-detail.php?userName="+params[0]);
                HttpURLConnection httpconn = (HttpURLConnection)url1.openConnection();
                httpconn.setReadTimeout(10000);
                httpconn.setConnectTimeout(15000);
                httpconn.setRequestMethod("GET");
                httpconn.setDoInput(true);
                httpconn.setDoOutput(true);
                if (httpconn.getResponseCode() == HttpURLConnection.HTTP_OK)
                {
                    BufferedReader input = new BufferedReader(new InputStreamReader(httpconn.getInputStream()),8192);
                    String strLine = null;
                    while ((strLine = input.readLine()) != null)
                    {
                        response.append(strLine);
                    }
                    input.close();
                }
            }
            catch (IOException e){
                e.printStackTrace();
            }
            return response.toString();
        }

        @Override
        protected void onPostExecute(final String response) {

            if (response == null){
                return;
            }else if(response.equals("[]")){
                return;
            }else {

                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray jsonMainNode = jsonResponse.optJSONArray("user_info");
                    user = new User();
                    for (int i = 0; i < jsonMainNode.length(); i++) {
                        JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                            String id = jsonChildNode.optString("id_user");
                            String name = jsonChildNode.optString("username");
                            String image = jsonChildNode.optString("image_name");
                            String realName = jsonChildNode.optString("name");
                            String phone = jsonChildNode.optString("phone_number");
                            String userDescription = jsonChildNode.optString("user_description");
                            user.setImageName(image);
                            user.setName(name);
                            user.setRealName(realName);
                            user.setId(id);
                            user.setPhoneNumber(phone);
                            user.setDescription(userDescription);

                            String imageUrl;
                            //int flag = 1;
                            if (!image.isEmpty() && image != null && !image.equals("NULL") && !image.equals("null")) {
                                imageUrl = Utility.getServerUrl() + "imgupload/user_image/" + image;
                                ImageLoader.getInstance().displayImage(imageUrl, userImg, options);
                            }
                            else{
                                ImageLoader.getInstance().displayImage("", userImg, options);
                            }
                        }

                } catch (JSONException e) {
                    Toast.makeText(getActivity().getApplicationContext(), "Error" + e.toString(),
                            Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    private class ListHostingEventsTask extends AsyncTask<Void, Void, String> {
        private Activity activity;

        @Override
        protected String doInBackground(Void... params) {

            StringBuilder response  = new StringBuilder();
            try{
            URL url1 = new URL(url+"list-hosting-events-by-user.php?userId="+loginUserId);
            HttpURLConnection httpconn = (HttpURLConnection)url1.openConnection();
                httpconn.setReadTimeout(10000);
                httpconn.setConnectTimeout(15000);
                httpconn.setRequestMethod("GET");
                httpconn.setDoInput(true);
                httpconn.setDoOutput(true);
            if (httpconn.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                BufferedReader input = new BufferedReader(new InputStreamReader(httpconn.getInputStream()),8192);
                String strLine = null;
                while ((strLine = input.readLine()) != null)
                {
                    response.append(strLine);
                }
                input.close();
            }
            }
            catch (IOException e){
                e.printStackTrace();
            }
            return response.toString();
        }

        @Override
        protected void onPostExecute(String jsonResult) {

            eventListView1.setAdapter(adapter1);
            if (jsonResult == null || jsonResult.equals("[]")){
                return;
            }
            try {
                JSONObject jsonResponse = new JSONObject(jsonResult);
                JSONArray jsonMainNode = jsonResponse.optJSONArray("activity_info");

                TextView hostEvents = (TextView)_rootView.findViewById(R.id.hostEventsCount);
                hostEvents.setText(hostEvents.getText()+" ("+jsonMainNode.length()+")");

                for (int i = 0; i < jsonMainNode.length(); i++) {
                        JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
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
                        String eventCreator = jsonChildNode.optString("event_creator");

                        item.setActivityImage(activityImage);
                        item.setAddress(address);
                        item.setCity(city);
                        item.setActivityType(type);
                        item.setCountry(country);
                        item.setDetail(detail);
                        item.setId(id);
                        item.setTitle(title);
                        item.setPhoneNumber(pNumber);
                        item.setState(state);
                        item.setDuration(duration);
                        item.setActivityTime(activityTime);
                        item.setPostTime(postTime);
                        item.setEventCreator(eventCreator);

                        itemsList1.add(item);
                }
            } catch (JSONException e) {
                Toast.makeText(getActivity().getApplicationContext(), "Error" + e.toString(),
                        Toast.LENGTH_SHORT).show();
            }

            adapter1.notifyDataSetChanged();

            super.onPostExecute(jsonResult);
            Utility.setListViewHeightBasedOnChildren(eventListView1);
        }

    }
    public void initAdapter1(){
        itemsList1 = new ArrayList<ActivityItem>();
        adapter1 = new ListAdapterS(getActivity(), R.layout.list_event_row, itemsList1);
    }

    private class ListJoinEventsTask extends AsyncTask<Void, Void, String> {
        private Activity activity;
        private ListJoinEventsTask(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected String doInBackground(Void... params) {

            StringBuilder response  = new StringBuilder();
            try{
                URL url1 = new URL(url+"get-events-by-user.php?userId="+loginUserId);
                HttpURLConnection httpconn = (HttpURLConnection)url1.openConnection();
                httpconn.setReadTimeout(10000);
                httpconn.setConnectTimeout(15000);
                httpconn.setRequestMethod("GET");
                httpconn.setDoInput(true);
                httpconn.setDoOutput(true);
                if (httpconn.getResponseCode() == HttpURLConnection.HTTP_OK)
                {
                    BufferedReader input = new BufferedReader(new InputStreamReader(httpconn.getInputStream()),8192);
                    String strLine = null;
                    while ((strLine = input.readLine()) != null)
                    {
                        response.append(strLine);
                    }
                    input.close();
                }
            }
            catch (IOException e){
                e.printStackTrace();
            }
            return response.toString();
        }

        @Override
        protected void onPostExecute(String jsonResult) {

            eventList.setAdapter(adapter);
            TextView joinEvents = (TextView)_rootView.findViewById(R.id.joinEventsCount);
            if (jsonResult == null || jsonResult.equals("[]")){
                joinEvents.setText(joinEvents.getText()+" (0)");
                return;
            }
            try {
                JSONObject jsonResponse = new JSONObject(jsonResult);
                JSONArray jsonMainNode = jsonResponse.optJSONArray("activity_info");
                joinEvents.setText("参加的活动 ("+jsonMainNode.length()+")");
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
                        String eventCreator = jsonChildNode.optString("event_creator");

                        item.setActivityImage(activityImage);
                        item.setAddress(address);
                        item.setCity(city);
                        item.setActivityType(type);
                        item.setCountry(country);
                        item.setDetail(detail);
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
                Toast.makeText(getActivity().getApplicationContext(), "Error" + e.toString(),
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
        adapter = new ListAdapterS(getActivity(), R.layout.list_event_row, itemsList);
    }

    private class FetchFriendsList extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {

            StringBuilder response  = new StringBuilder();
            try{
                URL url1 = new URL(url+"get-user-friends.php?username="+loginUser);
                HttpURLConnection httpconn = (HttpURLConnection)url1.openConnection();
                httpconn.setReadTimeout(10000);
                httpconn.setConnectTimeout(15000);
                httpconn.setRequestMethod("GET");
                httpconn.setDoInput(true);
                httpconn.setDoOutput(true);
                if (httpconn.getResponseCode() == HttpURLConnection.HTTP_OK)
                {
                    BufferedReader input = new BufferedReader(new InputStreamReader(httpconn.getInputStream()),8192);
                    String strLine = null;
                    while ((strLine = input.readLine()) != null)
                    {
                        response.append(strLine);
                    }
                    input.close();
                }
            }
            catch (IOException e){
                e.printStackTrace();
            }
            return response.toString();
        }

        @Override
        protected void onPostExecute(String jsonResult) {

            friendsListView.setAdapter(uAdapter);
            TextView friendsCount = (TextView)_rootView.findViewById(R.id.friendsCount);
            if (jsonResult == null || jsonResult.equals("[]")){
                friendsCount.setText(friendsCount.getText()+" (0)");
                return;
            }
            try {
                JSONObject jsonResponse = new JSONObject(jsonResult);
                JSONArray jsonMainNode = jsonResponse.optJSONArray("friends_info");
                friendsCount.setText("关注的好友 ("+jsonMainNode.length()+")");

                for (int i = 0; i < jsonMainNode.length(); i++) {
                    JSONArray innerArray = jsonMainNode.optJSONArray(i);
                    for (int j = 0; j < innerArray.length(); j++) {
                        JSONObject jsonChildNode = innerArray.getJSONObject(j);
                        User user = new User();
                        String id = jsonChildNode.optString("id_user");
                        String userName = jsonChildNode.optString("username");
                        String imageName = jsonChildNode.optString("image_thumb");
                        String userDescription = jsonChildNode.optString("user_description");

                        user.setId(id);
                        user.setName(userName);
                        user.setImageName(imageName);
                        user.setDescription(userDescription);
                        //user.setThumbImage(bitmap);

                        friendsList.add(user);
                    }
                }
            } catch (JSONException e) {
                Toast.makeText(getActivity().getApplicationContext(), "Error" + e.toString(),
                        Toast.LENGTH_SHORT).show();
            }

            adapter.notifyDataSetChanged();

            super.onPostExecute(jsonResult);
            Utility.setListViewHeightBasedOnChildren(friendsListView);
        }

    }

    public void initFriendListAdapter(){
        friendsList = new ArrayList<User>();
        uAdapter = new UserListAdapter(getActivity(), R.layout.list_users_row, friendsList);
    }
}