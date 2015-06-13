package com.example.pengzhizhou.meetup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import android.view.ViewGroup.LayoutParams;

import com.androidquery.AQuery;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class UserProfileFragment extends Fragment {
    private String loginUser;
    private GetUserDetailTask getUserDetailTask;
    private String url = Utility.getServerUrl();
    private User item;
    private AQuery aq;
    private ImageViewRounded ir;
    private ImageView userImg;
    private ListView eventList;
    private List<ActivityItem> itemsList;
    private ListAdapterS adapter = null;
    private Bitmap bt;
    Long startIndex = 0L;
    Long offset = 5L;
    private User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        SharedPreferences settings = this.getActivity().getSharedPreferences("MyPrefsFile", 0);
        loginUser = settings.getString("KEY_LOGIN_USER", null);
        View V = null;

        if (loginUser == null){
            Intent myIntent;
            myIntent = new Intent(getActivity(), LoginActivity.class);
            startActivity(myIntent);

        }else {

            // Inflate the layout for this fragment
            V = inflater.inflate(R.layout.user_profile_view, container, false);

            //android.support.v7.app.ActionBar actionBar = ((TabHostActivity) getActivity()).getSupportActionBar();
            //actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            //actionBar.setCustomView(R.layout.fragment_user_profile_actionbar);

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            aq = new AQuery(V);
            ir = new ImageViewRounded(getActivity());

            ((TabHostActivity) getActivity())
                    .setActionBarTitle(loginUser + "的主页");
            ((TabHostActivity) getActivity()).setImageViewable(View.VISIBLE);
            ((TabHostActivity) getActivity()).setSearchCityViewable(View.GONE);
            userImg = (ImageView) V.findViewById(R.id.userImg);

            getUserDetailTask = new GetUserDetailTask();
            getUserDetailTask.execute(loginUser);

            eventList = (ListView) V.findViewById(R.id.list);
            // function 1: when too many activities under this user, try a way to let them scroll instead
            // of covering the button "logout"
            eventList.setOnTouchListener(new ListView.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int action = event.getAction();
                    switch (action) {
                        case MotionEvent.ACTION_DOWN:
                            // Disallow ScrollView to intercept touch events.
                            v.getParent().requestDisallowInterceptTouchEvent(true);
                            break;

                        case MotionEvent.ACTION_UP:
                            // Allow ScrollView to intercept touch events.
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }

                    // Handle ListView touch events.
                    v.onTouchEvent(event);
                    return true;
                }
            });


            eventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    Intent i = new Intent(getActivity(), EventDetailActivity.class);
                    i.putExtra("itemTitle", itemsList.get(position).getTitle());
                    i.putExtra("itemImage", itemsList.get(position).getActivityImage());
                    i.putExtra("eventTime", itemsList.get(position).getActivityTime());
                    i.putExtra("itemAddress", itemsList.get(position).getAddress());
                    i.putExtra("itemId", itemsList.get(position).getId());

                    startActivity(i);
                }
            });

            initAdapter();

            new LoadItemsTask(getActivity()).execute();

            Button logout = (Button) V.findViewById(R.id.logout);
            logout.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);
            logout.setOnClickListener(new Button.OnClickListener() {
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
        return V;
    }

    /**** Method for Setting the Height of the ListView dynamically.
     **** Hack to fix the issue of not showing all the items of the ListView
     **** when placed inside a ScrollView  ****/
    // function 2: when too many activities under this user, try a way to let them scroll instead
    // of covering the button "logout"
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapterS listAdapter = (ListAdapterS)listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        if (listAdapter.getCount() > 5){
            for (int i = 0; i < 5; i++) {
                view = listAdapter.getView(i, view, listView);
                if (i == 0)
                    view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, LayoutParams.WRAP_CONTENT));

                view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                totalHeight += view.getMeasuredHeight();
            }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
        }
    }

    public class GetUserDetailTask extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... params) {

                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(url + "/signin/getUserDetail.php");

                try {
                    // Add your data
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("userName", params[0]));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    // Execute HTTP Post Request
                    HttpResponse response = httpclient.execute(httppost);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                    String json = reader.readLine();

                    return json;

                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                    return e.toString();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    return e.toString();
                }
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
                            user.setImageName(image);
                            user.setName(name);
                            user.setRealName(realName);
                            user.setId(id);
                            user.setPhoneNumber(phone);

                            ImageView userImage = (ImageView)getActivity().findViewById(R.id.userImg);
                            String imageUrl;
                            if (!image.isEmpty() && image != null) {
                                imageUrl = Utility.getServerUrl() + "/signin/imgupload/" + image;

                                bt = Utility.getBitmapFromURL(imageUrl);
                                if(bt!=null) {
                                    bt = ir.getCircledBitmap(bt);

                                }
                                userImage.setImageBitmap(bt);
                            }
                            else{

                                Bitmap icon = BitmapFactory.decodeResource(getActivity().getResources(),
                                        R.drawable.default_activity);
                                icon = ir.getCircledBitmap(icon);

                                userImg.setImageBitmap(icon);
                            }
                        }

                } catch (JSONException e) {
                    Toast.makeText(getActivity().getApplicationContext(), "Error" + e.toString(),
                            Toast.LENGTH_SHORT).show();
                }

            }
        }
    }


    private class LoadItemsTask extends AsyncTask<Void, Void, String> {
        private Activity activity;
        private LoadItemsTask(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url+"/signin/get_activities_by_user.php");
            String jsonResult = null;
            //add name value pair for the country code
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
            nameValuePairs.add(new BasicNameValuePair("start",String.valueOf(startIndex)));
            nameValuePairs.add(new BasicNameValuePair("limit",String.valueOf(offset)));
            nameValuePairs.add(new BasicNameValuePair("username",String.valueOf(loginUser)));

            try {
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            try {
                HttpResponse response = httpclient.execute(httppost);
                jsonResult = Utility.inputStreamToString(
                        response.getEntity().getContent()).toString();
            }

            catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return jsonResult;
        }

        @Override
        protected void onPostExecute(String jsonResult) {

            eventList.setAdapter(adapter);
            if (jsonResult == null || jsonResult.equals("[]")){

                return;
            }
            try {
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
                        String city = jsonChildNode.optString("city");
                        String state = jsonChildNode.optString("state");
                        String country = jsonChildNode.optString("country");
                        String activityImage = jsonChildNode.optString("image_name");
                        String eventCreator = jsonChildNode.optString("event_creator");

                        item.setActivityImage(activityImage);
                        item.setAddress(address);
                        item.setCity(city);
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
            setListViewHeightBasedOnChildren(eventList);
        }


    }
    public void initAdapter(){
        itemsList = new ArrayList<ActivityItem>();
        adapter = new ListAdapterS(getActivity(), R.layout.list_event_row, itemsList);
    }

}