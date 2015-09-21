package com.example.pengzhizhou.meetup;
/**
 * The user profile fragment
 * Created by pengzhizhou on 6/11/15.
 */
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class UserProfileFragment extends Fragment {
    private String loginUser;
    private String loginUserId;
    private String url = Utility.getServerUrl();
    private ImageView userImg;
    private List<ActivityItem> joinEventsList;
    private List<ActivityItem> hostEventsList;
    private List<User> friendsList;
    private List<User> followersList;
    private View _rootView;
    private User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        SharedPreferences settings = this.getActivity().getSharedPreferences("MyPrefsFile", 0);
        loginUser = settings.getString("KEY_LOGIN_USER", null);
        loginUserId = settings.getString("KEY_LOGIN_USER_ID", null);

        if (loginUser == null){
            Intent myIntent;
            myIntent = new Intent(getActivity(), LoginActivity.class);
            startActivity(myIntent);

        }else {
            _rootView = inflater.inflate(R.layout.user_profile_view, container, false);

            ((TabHostActivity) getActivity())
                    .setActionBarTitle(loginUser + "的主页");
            ((TabHostActivity) getActivity()).setImageViewable(View.VISIBLE);
            ((TabHostActivity) getActivity()).setSearchCityViewable(View.GONE);
            userImg = (ImageView) _rootView.findViewById(R.id.userImg);

            new GetUserDetailTask().execute(loginUser);
            new ListHostingEventsTask().execute();
            new ListJoinEventsTask().execute();
            new FetchFriendsList().execute();
            new FetchFollowersList().execute();


            Button logout = (Button) _rootView.findViewById(R.id.logout);
            //logout.getBackground().setColorFilter(Color.parseColor("#E35050"), PorterDuff.Mode.MULTIPLY);
            logout.setBackgroundColor(Color.parseColor("#FF7373"));
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

        }
        return _rootView;
    }


    public class GetUserDetailTask extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... params) {
            String response  = null;
            try{
                URL url1 = new URL(url+"get-user-detail.php?userName="+params[0]);
                response = Utility.createConnectionAndGetResponse(url1);
            }
            catch (IOException e){
                e.printStackTrace();
            }
            return response;
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
                                ImageLoader.getInstance().displayImage(imageUrl, userImg, Utility.options_round_100);
                            }
                            else{
                                ImageLoader.getInstance().displayImage("", userImg, Utility.options_round_100);
                            }
                        }

                } catch (JSONException e) {
                    if (getActivity()!=null) {
                        Toast.makeText(getActivity().getApplicationContext(), "Error" + e.toString(),
                                Toast.LENGTH_SHORT).show();
                    }
                }

            }
        }
    }

    private class ListHostingEventsTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {

            String response  = null;
            try{
                URL url1 = new URL(url+"list-hosting-events-by-user.php?userId="+loginUserId);
                response = Utility.createConnectionAndGetResponse(url1);
            }
            catch (IOException e){
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String jsonResult) {
            TextView hostEventsCount = (TextView)_rootView.findViewById(R.id.hostEventsLabel);
            if (jsonResult == null || jsonResult.equals("{\"activity_info\":[]}")){
                hostEventsCount.setText(hostEventsCount.getText()+" (0)");
                return;
            }
            try {
                hostEventsList = new ArrayList<>();
                JSONObject jsonResponse = new JSONObject(jsonResult);
                JSONArray jsonMainNode = jsonResponse.optJSONArray("activity_info");

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

                        hostEventsList.add(item);
                }
            } catch (JSONException e) {
                if (getActivity()!=null){
                    Toast.makeText(getActivity().getApplicationContext(), "Error" + e.toString(),
                            Toast.LENGTH_SHORT).show();
                }

            }
            // use layout instead of ListView to show at most first 3 items
            // to solve the scrolling problem of ListView inside ScrollView
            if (getActivity() != null) {
                LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.hostEventsLayout);
                if (layout!=null) {
                    layout.removeAllViews();
                    Utility.addEventChildViewToLayout(layout, getActivity(), hostEventsList, R.layout.list_event_row, 0, 3);
                }

                if (hostEventsList.size() > 3) {
                    TextView moreHostEvents = (TextView) getActivity().findViewById(R.id.moreHostEvents);
                    if (moreHostEvents != null){
                        moreHostEvents.setText("查看所有发布活动" + " (" + hostEventsList.size() + ")");
                        moreHostEvents.setVisibility(View.VISIBLE);

                        moreHostEvents.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), ViewEventsListActivity.class);
                            intent.putExtra("EventsList", new EventsWrapper((ArrayList) hostEventsList));
                            intent.putExtra("Origin", 0);
                            startActivity(intent);
                            }
                        });
                    }
                }
            }
            super.onPostExecute(jsonResult);
        }

    }

    private class ListJoinEventsTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {

            String response  = null;
            try{
                URL url1 = new URL(url+"get-events-by-user.php?userId="+loginUserId);
                response = Utility.createConnectionAndGetResponse(url1);
            }
            catch (IOException e){
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String jsonResult) {

            TextView joinEvents = (TextView)_rootView.findViewById(R.id.joinEventsLabel);
            if (jsonResult == null || jsonResult.equals("[]")){
                joinEvents.setText(joinEvents.getText()+" (0)");
                return;
            }
            try {
                joinEventsList = new ArrayList<>();
                JSONObject jsonResponse = new JSONObject(jsonResult);
                JSONArray jsonMainNode = jsonResponse.optJSONArray("activity_info");

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

                        joinEventsList.add(item);
                    }
                }
            } catch (JSONException e) {
                Toast.makeText(getActivity().getApplicationContext(), "Error" + e.toString(),
                        Toast.LENGTH_SHORT).show();
            }
            // use layout instead of ListView to show at most first 3 items
            // to solve the scrolling problem of ListView inside ScrollView
            if (getActivity() != null){
                LinearLayout layout = (LinearLayout)getActivity().findViewById(R.id.joinEventsLayout);
                if (layout!=null) {
                    layout.removeAllViews();
                    Utility.addEventChildViewToLayout(layout, getActivity(), joinEventsList, R.layout.list_event_row, 0, 3);
                }
                if (joinEventsList.size() > 3){
                    TextView moreJoinEvents = (TextView) getActivity().findViewById(R.id.moreJoinEvents);
                    if (moreJoinEvents != null) {
                        moreJoinEvents.setText("查看所有参加活动" + " (" + joinEventsList.size() + ")");
                        moreJoinEvents.setVisibility(View.VISIBLE);

                        moreJoinEvents.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getActivity(), ViewEventsListActivity.class);
                                intent.putExtra("EventsList", new EventsWrapper((ArrayList) joinEventsList));
                                intent.putExtra("Origin", 1);
                                startActivity(intent);
                            }
                        });
                    }
                }
            }
            super.onPostExecute(jsonResult);

        }

    }


    private class FetchFriendsList extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {

            String response  = null;
            try{
                URL url1 = new URL(url+"get-user-friends.php?username="+loginUser);
                response = Utility.createConnectionAndGetResponse(url1);
            }
            catch (IOException e){
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String jsonResult) {

            TextView friendsCount = (TextView)_rootView.findViewById(R.id.friendsListLabel);
            if (jsonResult == null || jsonResult.equals("[]")){
                friendsCount.setText(friendsCount.getText()+" (0)");
                return;
            }
            try {
                friendsList = new ArrayList<>();
                JSONObject jsonResponse = new JSONObject(jsonResult);
                JSONArray jsonMainNode = jsonResponse.optJSONArray("friends_info");

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

                        friendsList.add(user);
                    }
                }
            } catch (JSONException e) {
                Toast.makeText(getActivity().getApplicationContext(), "Error" + e.toString(),
                        Toast.LENGTH_SHORT).show();
            }
            // use layout instead of ListView to show at most first 3 items
            // to solve the scrolling problem of ListView inside ScrollView
            if (getActivity() != null) {
                LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.friendListLayout);
                if (layout!=null) {
                    layout.removeAllViews();
                    Utility.addUserChildViewToLayout(layout, getActivity(), friendsList, R.layout.list_users_row, 0, 3);
                }
                if (friendsList.size() > 3) {
                    TextView moreFriendsList = (TextView) getActivity().findViewById(R.id.moreFriendList);
                    if (moreFriendsList != null) {
                        moreFriendsList.setText("查看所有好友" + " (" + friendsList.size() + ")");
                        moreFriendsList.setVisibility(View.VISIBLE);

                        moreFriendsList.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getActivity(), ViewFriendsListActivity.class);
                                intent.putExtra("FriendsList", new UsersWrapper((ArrayList) friendsList));
                                startActivity(intent);
                            }
                        });
                    }
                }
            }
            super.onPostExecute(jsonResult);
        }

    }

    private class FetchFollowersList extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {

            String response  = null;
            try{
                URL url1 = new URL(url+"get-user-followers.php?username="+loginUser);
                response = Utility.createConnectionAndGetResponse(url1);
            }
            catch (IOException e){
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String jsonResult) {

            TextView followersCount = (TextView)_rootView.findViewById(R.id.friendsListLabel1);
            if (jsonResult == null || jsonResult.equals("[]")){
                followersCount.setText(followersCount.getText()+" (0)");
                return;
            }
            try {
                followersList = new ArrayList<>();
                JSONObject jsonResponse = new JSONObject(jsonResult);
                JSONArray jsonMainNode = jsonResponse.optJSONArray("friends_info");

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

                        followersList.add(user);
                    }
                }
            } catch (JSONException e) {
                Toast.makeText(getActivity().getApplicationContext(), "Error" + e.toString(),
                        Toast.LENGTH_SHORT).show();
            }
            // use layout instead of ListView to show at most first 3 items
            // to solve the scrolling problem of ListView inside ScrollView
            if (getActivity() != null) {
                LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.friendListLayout1);
                if (layout!=null) {
                    layout.removeAllViews();
                    Utility.addUserChildViewToLayout(layout, getActivity(), followersList, R.layout.list_users_row, 0, 3);
                }
                if (followersList.size() > 3) {
                    TextView moreFriendsList = (TextView) getActivity().findViewById(R.id.moreFriendList1);
                    if (moreFriendsList != null) {
                        moreFriendsList.setText("查看所有已关注您的用户" + " (" + followersList.size() + ")");
                        moreFriendsList.setVisibility(View.VISIBLE);

                        moreFriendsList.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getActivity(), ViewFriendsListActivity.class);
                                intent.putExtra("FollowersList", new UsersWrapper((ArrayList) followersList));
                                startActivity(intent);
                            }
                        });
                    }
                }
            }
            super.onPostExecute(jsonResult);
        }

    }

}