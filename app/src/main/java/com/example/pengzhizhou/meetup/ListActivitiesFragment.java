package com.example.pengzhizhou.meetup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class ListActivitiesFragment extends Fragment implements OnScrollListener {

    List<ActivityItem> itemsList;
    View footerView;

    ListView list;
    ListAdapter adapter = null;
    private String loginUser = null;

    ProgressDialog progressDialog;
    private int myLastVisiblePos;
    int currentFirstVisibleItem = 0;
    int currentVisibleItemCount = 0;
    int totalItemCount = 0;
    int currentScrollState = 0;
    boolean loadingMore = false;
    Long startIndex = 0L;
    Long offset = 5L;
    View _rootView;
    double latitude, longitude;
    String city;
    TextView noActivity;
    public View footer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        SharedPreferences settings = this.getActivity().getSharedPreferences("MyPrefsFile", 0);
        loginUser = settings.getString("KEY_LOGIN_USER", null);
        city = settings.getString("KEY_CITY", null);

        ((TabHostActivity) getActivity())
                .setActionBarTitle("活动主题");
        ((TabHostActivity) getActivity()).setImageViewable(View.GONE);
        ((TabHostActivity) getActivity()).setSearchCityViewable(View.VISIBLE);

        TextView otherCity = (TextView) getActivity().findViewById(R.id.searchCity);
        otherCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent;
                myIntent = new Intent(getActivity(), SearchCityActivity.class);
                startActivity(myIntent);
            }
        });
        progressDialog = new ProgressDialog(getActivity());

        if (_rootView == null) {
                // Inflate the layout for this fragment
                _rootView = inflater.inflate(R.layout.list_activities_view, container, false);
                if (city == null) {
                    GPSTracker gps = new GPSTracker(getActivity());
                    if(gps.canGetLocation()){
                        latitude = gps.getLatitude();
                        longitude = gps.getLongitude();
                    }else{
                        // can't get location
                        // GPS or Network is not enabled
                        // Ask user to enable GPS/network in settings
                        gps.showSettingsAlert();
                    }
                    new MatchingNearByLocationTask().execute();
                }


                noActivity = (TextView) _rootView.findViewById(R.id.noActivities);
                noActivity.setVisibility(View.GONE);
                list = (ListView) _rootView.findViewById(R.id.list);
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {

                        Intent i = new Intent(getActivity(),EventDetailActivity.class);
                        i.putExtra("itemTitle",itemsList.get(position).getTitle());
                        i.putExtra("itemImage", itemsList.get(position).getActivityImage());
                        i.putExtra("eventTime", itemsList.get(position).getActivityTime());
                        i.putExtra("itemAddress", itemsList.get(position).getAddress());
                        i.putExtra("itemId", itemsList.get(position).getId());
                        startActivity(i);
                    }
                });
                myLastVisiblePos = list.getFirstVisiblePosition();
                list.setOnScrollListener(this);

                itemsList = new ArrayList<ActivityItem>();
                initAdapter();

                if (city != null){
                    new LoadMoreItemsTask(getActivity()).execute();
                }

            } else {
                // Do not inflate the layout again.
                // The returned View of onCreateView will be added into the fragment.
                // However it is not allowed to be added twice even if the parent is same.
                // So we must remove _rootView from the existing parent view group
                // (it will be added back).
                ((ViewGroup)_rootView.getParent()).removeView(_rootView);
            }
            return _rootView;

    }

    public void initAdapter(){
        adapter = new ListAdapter(getActivity(), R.layout.list_row, itemsList);
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        this.currentFirstVisibleItem = firstVisibleItem;
        this.currentVisibleItemCount = visibleItemCount;
        this.totalItemCount = totalItemCount;

        //currentFirstVisPos = absListView.getFirstVisiblePosition();
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int scrollState) {
        final int currentFirstVisibleItem1 = list.getFirstVisiblePosition();
             if (currentFirstVisibleItem > myLastVisiblePos) {
                if (this.currentVisibleItemCount > 0 && scrollState == SCROLL_STATE_IDLE && this.totalItemCount == (currentFirstVisibleItem + currentVisibleItemCount)) {
                    /*** In this way I detect if there's been a scroll which has completed ***/
                    /*** do the work for load more date! ***/
                    if (!loadingMore) {
                        loadingMore = true;
                        new LoadMoreItemsTask(getActivity()).execute();
                    }
                }
            } else if (currentFirstVisibleItem == 0 && currentFirstVisibleItem < myLastVisiblePos) {
                //scroll up and refresh
                new RefreshListTask().execute();
            }

            myLastVisiblePos = currentFirstVisibleItem;
    }

    private class RefreshListTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

                progressDialog.setMessage("Refresh...");
                progressDialog.setCancelable(true);
                progressDialog.show();


        }
        @Override
        protected String doInBackground(Void... voids) {
            HttpClient httpclient = new DefaultHttpClient();
            String jsonResult = null;

            HttpGet request = new HttpGet();
            try {
                URI website = new URI(Utility.getServerUrl()+"/signin/get_activities_android.php?start="+0
                        +"&limit="+startIndex+"&city="+city);
                request.setURI(website);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            try {
                HttpResponse response = httpclient.execute(request);
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
            if (jsonResult == null)
            {
                adapter.notifyDataSetChanged();
                return;
            }
            else if (jsonResult.equals("[]") && startIndex != 0){
                Toast.makeText(getActivity().getApplicationContext(), "NO MORE DATA",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            else if (jsonResult.equals("[]") && startIndex == 0){
                noActivity.setVisibility(View.VISIBLE);
                noActivity.setText("There is no activity in " + city);
                return;
            }
            try {
                itemsList = new ArrayList<ActivityItem>();

                JSONObject jsonResponse = new JSONObject(jsonResult);
                JSONArray jsonMainNode = jsonResponse.optJSONArray("act_info");

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
            } catch (JSONException e) {
                Toast.makeText(getActivity().getApplicationContext(), "Error" + e.toString(),
                        Toast.LENGTH_SHORT).show();
            }

            adapter.notifyDataSetChanged();

            if (itemsList.size() > 0) {
                startIndex = startIndex + itemsList.size();
            }
            super.onPostExecute(jsonResult);

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }

        @Override
        protected void onCancelled() {

            super.onCancelled();
            progressDialog.dismiss();

        }
    }

    private class LoadMoreItemsTask extends AsyncTask<Void, Void, String> {

        private Activity activity;
        //private View footer;

        private LoadMoreItemsTask(Activity activity) {
            this.activity = activity;
            loadingMore = true;
            footer = activity.getLayoutInflater().inflate(R.layout.list_footer, null);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            list.addFooterView(footer);
            if (list.getAdapter() == null) {
                list.setAdapter(adapter);
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            HttpClient httpclient = new DefaultHttpClient();
            String jsonResult = null;

            HttpGet request = new HttpGet();
            try {
                URI website = new URI(Utility.getServerUrl()+"/signin/get_activities_android.php?start="+startIndex
                        +"&limit="+offset+"&city="+city);
                request.setURI(website);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            try {
                HttpResponse response = httpclient.execute(request);
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
            if (footer != null) {
                list.removeFooterView(footer);
            }

            loadingMore = false;

            if (jsonResult == null){
                return;
            }
            else if (jsonResult.startsWith("<HTML><HEAD>")){
                Toast.makeText(getActivity().getApplicationContext(), "No internet available",
                        Toast.LENGTH_SHORT).show();

                return;
            }
            else if (jsonResult.equals("[]") && startIndex != 0){
                Toast.makeText(getActivity().getApplicationContext(), "NO MORE DATA",
                        Toast.LENGTH_SHORT).show();

                return;
            }
            else if (jsonResult.equals("[]") && startIndex == 0){
                noActivity.setVisibility(View.VISIBLE);
                noActivity.setText("There is no activity in " + city);
                return;
            }
            try {
                JSONObject jsonResponse = new JSONObject(jsonResult);
                JSONArray jsonMainNode = jsonResponse.optJSONArray("act_info");

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
            } catch (JSONException e) {
                Toast.makeText(getActivity().getApplicationContext(), "Error" + e.toString(),
                        Toast.LENGTH_SHORT).show();
            }

            adapter.notifyDataSetChanged();

            if (itemsList.size() > 0) {
                //startIndex = startIndex + itemsList.size();
                startIndex = (long)itemsList.size();
            }
            super.onPostExecute(jsonResult);
        }


    }


    private class MatchingNearByLocationTask extends
            AsyncTask<Void, Void, Void>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(true);
            progressDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {

            String jsonStr = getLocationInfo(latitude, longitude).toString();
            if (jsonStr != null) {
                Log.i("location--??", jsonStr);

                JSONObject jsonObj;
                try {
                    jsonObj = new JSONObject(jsonStr);

                    String Status = jsonObj.getString("status");
                    if (Status.equalsIgnoreCase("OK")) {
                        JSONArray Results = jsonObj.getJSONArray("results");
                        JSONObject zero = Results.getJSONObject(0);
                        JSONArray address_components = zero.getJSONArray("address_components");

                        for (int i = 0; i < address_components.length(); i++) {
                          //  if (i == 3) {
                                JSONObject zero2 = address_components.getJSONObject(i);
                                String long_name = zero2.getString("long_name");
                                JSONArray mtypes = zero2.getJSONArray("types");
                                String Type = mtypes.getString(0);
                                String Address1 = null;
                                if (TextUtils.isEmpty(long_name) == false || !long_name.equals(null) || long_name.length() > 0 || long_name != "") {
                                    if (Type.equalsIgnoreCase("street_number")) {
                                        Address1 = long_name + " ";
                                    } else if (Type.equalsIgnoreCase("route")) {
                                        Address1 = Address1 + long_name;
                                    } else if (Type.equalsIgnoreCase("sublocality")) {
                                        String Address2 = long_name;
                                    } else if (Type.equalsIgnoreCase("locality")) {
                                        // Address2 = Address2 + long_name + ", ";
                                        city = long_name;
                                    } else if (Type.equalsIgnoreCase("administrative_area_level_2")) {
                                        String County = long_name;
                                    } else if (Type.equalsIgnoreCase("administrative_area_level_1")) {
                                        String State = long_name;
                                    } else if (Type.equalsIgnoreCase("country")) {
                                        String Country = long_name;
                                    } else if (Type.equalsIgnoreCase("postal_code")) {
                                        String PIN = long_name;
                                    }
                                }
                            }

                        }

                }

                catch (JSONException e) {

                    e.printStackTrace();
                }

            }


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            Toast.makeText(getActivity().getApplicationContext(), "City is: " + city,
                    Toast.LENGTH_SHORT).show();
            new LoadMoreItemsTask(getActivity()).execute();
            // Dismiss the progress dialog
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

        }

        @Override
        protected void onCancelled() {

            super.onCancelled();
            progressDialog.dismiss();

        }

    }


    private JSONObject getLocationInfo(double lat, double lng) {

        HttpGet httpGet = new HttpGet(
                "http://maps.googleapis.com/maps/api/geocode/json?latlng="
                        + lat + "," + lng + "&sensor=false");
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(stringBuilder.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

}