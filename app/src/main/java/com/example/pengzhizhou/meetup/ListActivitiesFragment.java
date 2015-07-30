package com.example.pengzhizhou.meetup;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobsandgeeks.adapters.Sectionizer;
import com.mobsandgeeks.adapters.SimpleSectionAdapter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class ListActivitiesFragment extends Fragment implements OnScrollListener {

    private List<ActivityItem> itemsList;
    private ListView list;
    private ListAdapter adapter = null;
    private SimpleSectionAdapter<ActivityItem> sectionAdapter = null;
    private ProgressDialog progressRefress, progressLoading;
    private int myLastVisiblePos;
    private int currentFirstVisibleItem = 0;
    private int currentVisibleItemCount = 0;
    private int totalItemCount = 0;
    boolean loadingMore = false;
    private Long startIndex = 0L;
    private Long offset = 10L;
    private View _rootView;
    private double latitude, longitude;
    private String city;
    private TextView noActivity;
    private Bitmap bitmap = null;
    private ArrayList<Item> gridArray;
    private ArrayList<Integer> resource;
    private GridView gridViewFilter;
    private CustomGridViewAdapter customGridAdapter;
    private ImageView closeFilter;

    private String activityTypeId = null;
    private String activityTitle = null;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        SharedPreferences settings = this.getActivity().getSharedPreferences("MyPrefsFile", 0);
        city = settings.getString("KEY_CITY", null);
        city = "襄阳市";

        Bundle b = getActivity().getIntent().getExtras();
        if (b != null) {
            activityTitle = b.getString("activityTypes");
            activityTypeId = b.getString("activityTypesId");
        }

        if (activityTitle == null) {
            ((TabHostActivity) getActivity())
                    .setActionBarTitle("全部活动");
        }
        else{
            ((TabHostActivity) getActivity())
                    .setActionBarTitle(activityTitle);
        }

        ((TabHostActivity) getActivity()).setImageViewable(View.GONE);
        ((TabHostActivity) getActivity()).setSearchCityViewable(View.VISIBLE);

        ImageView otherCity = (ImageView) getActivity().findViewById(R.id.searchCity);
        otherCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent;
                myIntent = new Intent(getActivity(), SearchCityActivity.class);
                startActivity(myIntent);
            }
        });

        // ignore NetworkOnMainThreadException currently, will try to fix it later
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

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
                    timerDelayRemoveDialog(6000, progressLoading);
                }

                // create filtering pictures
                gridArray = new ArrayList<Item>();
                resource = new ArrayList<Integer>();
                if (gridArray == null || gridArray.size() < 1) {
                    Utility.createGridArray(this.getResources(), gridArray, resource, 1);
                }

                noActivity = (TextView) _rootView.findViewById(R.id.noActivities);
                noActivity.setVisibility(View.GONE);
                list = (ListView) _rootView.findViewById(R.id.list);
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        ActivityItem ai = (ActivityItem) sectionAdapter.getItem(position);

                        Intent i = new Intent(getActivity(),EventDetailActivity.class);
                        i.putExtra("itemTitle",ai.getTitle());
                        i.putExtra("itemImage", ai.getActivityImage());
                        i.putExtra("eventTime", ai.getActivityTime());
                        i.putExtra("itemAddress", ai.getAddress());
                        i.putExtra("itemId", ai.getId());
                        i.putExtra("itemType", ai.getActivityType());
                        i.putExtra("itemDetail", ai.getDetail());
                        i.putExtra("itemCity", ai.getCity());
                        i.putExtra("itemState", ai.getState());
                        i.putExtra("eventCreator", ai.getEventCreator());
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

                if (bitmap != null){
                    bitmap.recycle();
                    bitmap = null;
                }

            } else {
                // Do not inflate the layout again.
                // The returned View of onCreateView will be added into the fragment.
                // However it is not allowed to be added twice even if the parent is same.
                // So we must remove _rootView from the existing parent view group
                // (it will be added back).
                ((ViewGroup)_rootView.getParent()).removeView(_rootView);
            }

        TextView actionbarTitle = (TextView)getActivity().findViewById(R.id.actionbarTitle);
        ImageView pullDownIcon = (ImageView)getActivity().findViewById(R.id.pulldown);
        pullDownIcon.setVisibility(View.VISIBLE);
        gridViewFilter = (GridView) _rootView.findViewById(R.id.gridFilter);
        customGridAdapter = new CustomGridViewAdapter(getActivity(), R.layout.grid_single_1, gridArray, resource);
        gridViewFilter.setAdapter(customGridAdapter);
        closeFilter = (ImageView)_rootView.findViewById(R.id.closeFilter);

        actionbarTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gridViewFilter.setVisibility(View.VISIBLE);
                closeFilter.setVisibility(View.VISIBLE);
            }
        });
        pullDownIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gridViewFilter.setVisibility(View.VISIBLE);
                closeFilter.setVisibility(View.VISIBLE);
            }
        });


        gridViewFilter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent i = getActivity().getIntent();
                i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                i.putExtra("activityTypes", gridArray.get(position).getTitle());
                i.putExtra("activityTypesId", Integer.toString(position));
                i.putExtra("tab", 0);
                startActivity(i);
            }
        });


        closeFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gridViewFilter.setVisibility(View.GONE);
                closeFilter.setVisibility(View.GONE);
            }
        });

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
             if (currentFirstVisibleItem > myLastVisiblePos) {
                if (this.currentVisibleItemCount > 0 && scrollState == SCROLL_STATE_IDLE && this.totalItemCount == (currentFirstVisibleItem + currentVisibleItemCount)) {
                    /*** In this way I detect if there's been a scroll which has completed ***/
                    /*** do the work for load more date! ***/
                    if (!loadingMore) {
                        loadingMore = true;
                        new LoadMoreItemsTask(getActivity()).execute();
                        timerDelayRemoveDialog(6000, progressLoading);
                    }
                }
            } else if (currentFirstVisibleItem == 0 && currentFirstVisibleItem < myLastVisiblePos) {
                //scroll up and refresh
                new RefreshListTask().execute();
                timerDelayRemoveDialog(6000, progressRefress);
            }

            myLastVisiblePos = currentFirstVisibleItem;
    }

    public String getRefactorUrl(String url){
        if (activityTypeId != null) {
            int activityType = Integer.parseInt(activityTypeId);
            if (activityType != 0) {
                activityType = activityType - 1;
                //"get-events.php?start="+0+"&limit="+startIndex+"&city="+city;
                url = url + "&type=" + activityType;
            }
        }
        return url;
    }

    private class RefreshListTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressRefress = new ProgressDialog(getActivity());
            progressRefress = ProgressDialog.show(getActivity(), "", "刷新...");
        }
        @Override
        protected String doInBackground(Void... voids) {
            /*HttpClient httpclient = new DefaultHttpClient();
            String jsonResult = null;

            HttpGet request = new HttpGet();
            try {
                String url = "get-events.php?start="+0
                        +"&limit="+startIndex+"&city="+city;
                url = getRefactorUrl(url);
                URI website = new URI(Utility.getServerUrl()+url);
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

            return jsonResult;*/
            StringBuilder response  = new StringBuilder();
            try {
                String url = "get-events.php";
                URL url1 = new URL(Utility.getServerUrl()+url);
                HttpURLConnection httpconn = (HttpURLConnection)url1.openConnection();
                httpconn.setReadTimeout(100000);
                httpconn.setConnectTimeout(150000);
                httpconn.setRequestMethod("GET");
                httpconn.setDoInput(true);
                httpconn.setDoOutput(true);

                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("start", "0"));
                params.add(new BasicNameValuePair("limit", String.valueOf(startIndex)));
                params.add(new BasicNameValuePair("city", city));

                if (activityTypeId != null) {
                    int activityType = Integer.parseInt(activityTypeId);
                    if (activityType != 0) {
                        activityType = activityType - 1;
                        params.add(new BasicNameValuePair("type", String.valueOf(activityType)));
                    }
                }

                OutputStream os = httpconn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getQuery(params));
                writer.flush();
                writer.close();
                os.close();

                if (httpconn.getResponseCode() == HttpURLConnection.HTTP_OK)
                {
                    // if response code = 200 ok
                    BufferedReader input = new BufferedReader(new InputStreamReader(httpconn.getInputStream()));
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
            if (jsonResult == null)
            {
                adapter.notifyDataSetChanged();
            }
            else if (jsonResult.equals("[]") && startIndex != 0){
                Toast.makeText(getActivity().getApplicationContext(), "没有更多活动了",
                        Toast.LENGTH_SHORT).show();
            }
            else if (jsonResult.equals("[]") && startIndex == 0){
                noActivity.setVisibility(View.VISIBLE);
                if (activityTitle != null) {
                    noActivity.setText(city + "暂时没有"+activityTitle);
                }else{
                    if (activityTitle != null) {
                        noActivity.setText(city + "暂时没有活动");
                    }
                }
            }
            else {
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
                        String type = jsonChildNode.optString("activity_type");
                        String city = jsonChildNode.optString("city");
                        String state = jsonChildNode.optString("state");
                        String country = jsonChildNode.optString("country");
                        String activityImage = jsonChildNode.optString("image_name");
                        String activityThumbImage = jsonChildNode.optString("image_thumb");
                        String eventCreator = jsonChildNode.optString("event_creator");

                        /*String imageUrl;
                        Bitmap bitmap;
                        if (!activityThumbImage.isEmpty() && activityThumbImage != null && !activityThumbImage.equals("null")) {
                            imageUrl = Utility.getServerUrl() + "imgupload/activity_thumb_image/" + activityThumbImage;
                            bitmap = aq.getCachedImage(imageUrl);
                        }
                        else{
                            bitmap = null;
                        }

                        item.setThumbBitmap(bitmap);*/
                        item.setActivityImage(activityThumbImage);
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
                } catch (JSONException e) {
                    Toast.makeText(getActivity().getApplicationContext(), "Error" + e.toString(),
                            Toast.LENGTH_SHORT).show();
                }

                adapter.notifyDataSetChanged();

                if (itemsList.size() > 0) {
                    startIndex = (long)itemsList.size();
                }

                try{
                    if(progressRefress.isShowing()){
                        progressRefress.dismiss();
                    }
                }
                catch(Exception e){
                    e.printStackTrace();
                }
                finally
                {
                    progressRefress.dismiss();
                }
            }
        }

        @Override
        protected void onCancelled() {

            super.onCancelled();
            progressRefress.dismiss();

        }
    }

    public void timerDelayRemoveDialog(long time, final Dialog d){
        new android.os.Handler().postDelayed(new Runnable() {
            public void run() {
                if (d!=null) {
                    d.dismiss();
                }
            }
        }, time);
    }

    private class LoadMoreItemsTask extends AsyncTask<Void, Void, String> {
        private View footer;

        private LoadMoreItemsTask(Activity activity) {
            loadingMore = true;
            footer = activity.getLayoutInflater().inflate(R.layout.list_footer, null);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            list.addFooterView(footer);
        }

        @Override
        protected String doInBackground(Void... voids) {
            StringBuilder response  = new StringBuilder();
            try {
                String url = "get-events.php";
                URL url1 = new URL(Utility.getServerUrl()+url);
                HttpURLConnection httpconn = (HttpURLConnection)url1.openConnection();
                httpconn.setReadTimeout(100000);
                httpconn.setConnectTimeout(150000);
                httpconn.setRequestMethod("GET");
                httpconn.setDoInput(true);
                httpconn.setDoOutput(true);

                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("start", String.valueOf(startIndex)));
                params.add(new BasicNameValuePair("limit", String.valueOf(offset)));
                params.add(new BasicNameValuePair("city", city));

                if (activityTypeId != null) {
                    int activityType = Integer.parseInt(activityTypeId);
                    if (activityType != 0) {
                        activityType = activityType - 1;
                        params.add(new BasicNameValuePair("type", String.valueOf(activityType)));
                    }
                }

                OutputStream os = httpconn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getQuery(params));
                writer.flush();
                writer.close();
                os.close();

                if (httpconn.getResponseCode() == HttpURLConnection.HTTP_OK)
                {
                    // if response code = 200 ok
                    BufferedReader input = new BufferedReader(new InputStreamReader(httpconn.getInputStream()));
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

            loadingMore = false;
            if (jsonResult == null){
                // do nothing
            }
            else if (jsonResult.startsWith("<HTML><HEAD>")){
                Toast.makeText(getActivity().getApplicationContext(), "No internet available",
                        Toast.LENGTH_SHORT).show();
            }
            else if (jsonResult.equals("[]") && startIndex != 0){
                Toast.makeText(getActivity().getApplicationContext(), "没有更多活动了",
                        Toast.LENGTH_SHORT).show();
            }
            else if (jsonResult.equals("[]") && startIndex == 0){
                noActivity.setVisibility(View.VISIBLE);
                if (activityTitle != null) {
                    noActivity.setText(city + "暂时没有"+activityTitle);
                }else{
                    if (activityTitle != null) {
                        noActivity.setText(city + "暂时没有活动");
                    }
                }
            }
            else {
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
                        String type = jsonChildNode.optString("activity_type");
                        String city = jsonChildNode.optString("city");
                        String state = jsonChildNode.optString("state");
                        String country = jsonChildNode.optString("country");
                        String activityImage = jsonChildNode.optString("image_name");
                        String activityThumbImage = jsonChildNode.optString("image_thumb");
                        String eventCreator = jsonChildNode.optString("event_creator");

                        item.setActivityImage(activityThumbImage);
                        item.setAddress(address);
                        item.setActivityType(type);
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
                if (sectionAdapter == null) {
                    sectionAdapter = new SimpleSectionAdapter<ActivityItem>(getActivity(),
                            adapter, R.layout.section_header, R.id.title,
                            new ActivitySectionizer());
                }

                if (list.getAdapter() == null) {
                    //list.setAdapter(adapter);
                    list.setAdapter(sectionAdapter);
                }

                sectionAdapter.notifyDataSetChanged();

                if (itemsList.size() > 0) {
                    //startIndex = startIndex + itemsList.size();
                    startIndex = (long)itemsList.size();
                }
                super.onPostExecute(jsonResult);
            }

            if (footer != null) {
                list.removeFooterView(footer);

            }
        }
    }

    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    private class MatchingNearByLocationTask extends
            AsyncTask<Void, Void, Void>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            progressLoading = new ProgressDialog(getActivity());
            progressLoading = ProgressDialog.show(getActivity(), "", "寻找你所在城市...");

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
            city = "襄阳市";
            Toast.makeText(getActivity().getApplicationContext(), "城市: " + city,
                    Toast.LENGTH_SHORT).show();
            new LoadMoreItemsTask(getActivity()).execute();
            // Dismiss the progress dialog
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

        @Override
        protected void onCancelled() {

            super.onCancelled();
            progressLoading.dismiss();

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


class ActivitySectionizer implements Sectionizer<ActivityItem> {

    @Override
    public String getSectionTitleForItem(ActivityItem activity) {
        return activity.getActivityDate();
    }
}