package com.example.pengzhizhou.meetup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
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

import com.androidquery.AQuery;
import com.mobsandgeeks.adapters.Sectionizer;
import com.mobsandgeeks.adapters.SimpleSectionAdapter;

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

    private List<ActivityItem> itemsList;
    private ListView list;
    private ListAdapter adapter = null;
    private SimpleSectionAdapter<ActivityItem> sectionAdapter = null;
    private String loginUser = null;
    private AQuery aq;
    private ImageViewRounded ir;

    private ProgressDialog progressDialog;
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
    private GridView gridViewFilter;
    private CustomGridViewAdapter customGridAdapter;
    private ImageView closeFilter;

    private String activityTypeId = null;
    private String activityTitle = null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        SharedPreferences settings = this.getActivity().getSharedPreferences("MyPrefsFile", 0);
        loginUser = settings.getString("KEY_LOGIN_USER", null);
        city = settings.getString("KEY_CITY", null);

        aq = new AQuery(getActivity());
        ir = new ImageViewRounded(getActivity());

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
        progressDialog = new ProgressDialog(getActivity());
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
        createGridArray();
        gridViewFilter = (GridView) _rootView.findViewById(R.id.gridFilter);
        customGridAdapter = new CustomGridViewAdapter(getActivity(), R.layout.grid_single_1, gridArray);
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
                    }
                }
            } else if (currentFirstVisibleItem == 0 && currentFirstVisibleItem < myLastVisiblePos) {
                //scroll up and refresh
                new RefreshListTask().execute();
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

                progressDialog.setMessage("刷新...");
                progressDialog.setCancelable(true);
                progressDialog.show();


        }
        @Override
        protected String doInBackground(Void... voids) {
            HttpClient httpclient = new DefaultHttpClient();
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
                Toast.makeText(getActivity().getApplicationContext(), "没有更多活动了",
                        Toast.LENGTH_SHORT).show();
                return;
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
                    String type = jsonChildNode.optString("activity_type");
                    String city = jsonChildNode.optString("city");
                    String state = jsonChildNode.optString("state");
                    String country = jsonChildNode.optString("country");
                    String activityImage = jsonChildNode.optString("image_name");
                    String eventCreator = jsonChildNode.optString("event_creator");

                    String imageUrl;
                    Bitmap bitmap;
                    if (!activityImage.isEmpty() && activityImage != null && !activityImage.equals("null")) {
                        imageUrl = Utility.getServerUrl() + "imgupload/" + activityImage;
                        bitmap = aq.getCachedImage(imageUrl);
                    }
                    else{
                        bitmap = null;
                    }

                    item.setBitmap(bitmap);
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
            } catch (JSONException e) {
                Toast.makeText(getActivity().getApplicationContext(), "Error" + e.toString(),
                        Toast.LENGTH_SHORT).show();
            }

            adapter.notifyDataSetChanged();

            if (itemsList.size() > 0) {
                startIndex = (long)itemsList.size();
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
            HttpClient httpclient = new DefaultHttpClient();
            String jsonResult = null;

            HttpGet request = new HttpGet();
            try {
                String url = "get-events.php?start="+startIndex
                        +"&limit="+offset+"&city="+city;
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

            return jsonResult;
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
                        String eventCreator = jsonChildNode.optString("event_creator");

                        if (!activityImage.isEmpty() && activityImage != null && !activityImage.equals("null")) {
                            String imageUrl = Utility.getServerUrl() + "imgupload/" + activityImage;
                            bitmap = Utility.getBitmapFromURL(imageUrl);
                            //bitmap=Bitmap.createScaledBitmap(bitmap, 100,100, true);
                        } else {
                            bitmap = null;
                        }

                        item.setBitmap(bitmap);
                        item.setActivityImage(activityImage);
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
            city = "襄阳市";
            Toast.makeText(getActivity().getApplicationContext(), "城市: " + city,
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // When the user clicks START ALARM, set the alarm.
            case R.id.start_action:
                return true;
            // When the user clicks CANCEL ALARM, cancel the alarm.
            case R.id.cancel_action:
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void createGridArray(){
        Bitmap quanbuIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.quanbu);
        Bitmap zhuoyouIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.zhuoyou);
        Bitmap jieriIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.jieri);
        Bitmap dianyingIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.dianying);
        Bitmap chuangyeIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.chuangye);
        Bitmap mishiIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.mishi);
        Bitmap tiyuIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.tiyu);
        Bitmap jiangzuoIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.jiangzuo);
        Bitmap zijiayouIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.zijiayou);
        Bitmap qitaIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.qita);

        //Bitmap userIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.zhuoyou);
        gridArray = new ArrayList<Item>();
        gridArray.add(new Item(quanbuIcon, "全部活动"));
        gridArray.add(new Item(jieriIcon, "节日派对"));
        gridArray.add(new Item(zhuoyouIcon, "桌游聚会"));
        gridArray.add(new Item(mishiIcon, "密室逃脱"));
        gridArray.add(new Item(chuangyeIcon, "创意展览"));
        gridArray.add(new Item(jiangzuoIcon, "行业讲座"));
        gridArray.add(new Item(dianyingIcon, "电影鉴赏"));
        gridArray.add(new Item(tiyuIcon, "体育活动"));
        gridArray.add(new Item(zijiayouIcon, "旅游同行"));
        gridArray.add(new Item(qitaIcon, "其他类别"));
    }

}


class ActivitySectionizer implements Sectionizer<ActivityItem> {

    @Override
    public String getSectionTitleForItem(ActivityItem activity) {
        return activity.getActivityDate();
    }
}