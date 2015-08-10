package com.example.pengzhizhou.meetup;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.SearchView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchCityActivity extends Activity implements
        SearchView.OnQueryTextListener, SearchView.OnCloseListener{

    private SearchView search;
    private CityFilterAdapter listAdapter;
    private ExpandableListView myList;
    private ArrayList<Province> continentList = new ArrayList<Province>();
    Map<String, String> provinces = new HashMap<>();
    Map<String, List<City>> citiesByProvinces = new HashMap<String, List<City>>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_city);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        search = (SearchView) findViewById(R.id.search);
        search.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        search.setIconifiedByDefault(false);
        search.setOnQueryTextListener(this);
        search.setOnCloseListener(this);

        //display the list
        displayList();
        if (myList != null) {
            myList.setOnChildClickListener(new OnChildClickListener() {

                @Override
                public boolean onChildClick(ExpandableListView parent, View v,
                                            int groupPosition, int childPosition, long id) {
                    // TODO Auto-generated method stub
                    if (listAdapter != null) {
                        //int cp = (int) listAdapter.getChildId(groupPosition, childPosition);

                        City cityClicked = (City)listAdapter.getChild(groupPosition, childPosition);
                        String cityName = cityClicked.getName();
                        Toast.makeText(getApplicationContext(), cityName,
                                Toast.LENGTH_SHORT).show();

                        SharedPreferences settings = getSharedPreferences("MyPrefsFile", 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("KEY_CITY", cityName);
                        editor.commit();

                        Intent myIntent = null;
                        myIntent = new Intent(SearchCityActivity.this, TabHostActivity.class);
                        startActivity(myIntent);
                        //search.setQueryHint(Integer.toString(childPosition));
                    }

                    return false;

                }
            });
        }
        //expand all Groups
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_city, menu);
        return true;
    }

    //method to expand all groups
    private void expandAll() {
        int count = 0;
        if (listAdapter != null) {
            count = listAdapter.getGroupCount();
        }
        for (int i = 0; i < count; i++){
            myList.expandGroup(i);
        }
    }

    //method to expand all groups
    private void displayList() {

        //display the list
        loadSomeData();
        //get reference to the ExpandableListView
        myList = (ExpandableListView) findViewById(R.id.expandableList);
        //create the adapter by passing your ArrayList data

    }

    private void loadDataToSearch(){
        for (int i = 1; i <= provinces.size(); i++){
            List<City> a = citiesByProvinces.get(String.valueOf(i));
            String provinceName = provinces.get(String.valueOf(i));
            Province continent = new Province(provinceName, (ArrayList)a);
            continentList.add(continent);
        }
    }


    private void loadSomeData() {

        new GetProvinceDetailTask().execute();
        new GetCitiesDetailTask().execute();

    }

    public class GetProvinceDetailTask extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... params) {
            StringBuilder response  = new StringBuilder();
            try{
                URL url1 = new URL(Utility.getServerUrl()+"get-provinces-names.php");
                HttpURLConnection httpconn = (HttpURLConnection)url1.openConnection();
                httpconn.setReadTimeout(10000);
                httpconn.setConnectTimeout(15000);
                httpconn.setRequestMethod("GET");
                httpconn.setDoInput(true);
                httpconn.setDoOutput(true);
                if (httpconn.getResponseCode() == HttpURLConnection.HTTP_OK)
                {
                    BufferedReader input = new BufferedReader(new InputStreamReader(httpconn.getInputStream(), "UTF-8"));
                    String strLine = null;
                    while ((strLine = input.readLine()) != null)
                    {
                        response.append(strLine);
                    }
                    input.close();
                }
            }
            catch (IOException e){
                String error = e.toString();
                return error;
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
                    JSONArray jsonMainNode = jsonResponse.optJSONArray("province_data");
                    for (int i = 0; i < jsonMainNode.length(); i++) {
                        JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                        String id = jsonChildNode.optString("pid");
                        String name = jsonChildNode.optString("provincial");
                        provinces.put(id, name);
                    }

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Error" + e.toString(),
                            Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    public class GetCitiesDetailTask extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... params) {
            StringBuilder response  = new StringBuilder();
            try{
                URL url1 = new URL(Utility.getServerUrl()+"get-cities-detail.php");
                HttpURLConnection httpconn = (HttpURLConnection)url1.openConnection();
                httpconn.setReadTimeout(10000);
                httpconn.setConnectTimeout(15000);
                httpconn.setRequestMethod("GET");
                httpconn.setDoInput(true);
                httpconn.setDoOutput(true);
                if (httpconn.getResponseCode() == HttpURLConnection.HTTP_OK)
                {
                    BufferedReader input = new BufferedReader(new InputStreamReader(httpconn.getInputStream(), "UTF-8"));
                    String strLine = null;
                    while ((strLine = input.readLine()) != null)
                    {
                        response.append(strLine);
                    }
                    input.close();
                }
            }
            catch (IOException e){
                String error = e.toString();
                return error;
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
                    JSONArray jsonMainNode = jsonResponse.optJSONArray("city_data");
                    List<City> cities = new ArrayList<City>();
                    for (int i = 0; i < jsonMainNode.length(); i++) {
                        JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                        JSONObject jsonChildNode1 = null;
                        String name = jsonChildNode.optString("city");
                        String proId = jsonChildNode.optString("pid");
                        String code = jsonChildNode.optString("cid");
                        City city = new City(code, name);

                        if ((i+1) < jsonMainNode.length() ) {
                            jsonChildNode1 = jsonMainNode.getJSONObject(i+1);
                        }
                        else {
                            cities.add(city);
                        }

                        if (jsonChildNode1 != null) {
                            if (jsonChildNode1.optString("pid").equals(proId)) {
                                cities.add(city);
                            }else{
                                cities.add(city);
                                citiesByProvinces.put(proId, cities);
                                cities = new ArrayList<City>();
                            }
                        }
                        else{
                            //cities.add(city);
                            citiesByProvinces.put(proId, cities);
                            cities = new ArrayList<City>();
                        }

                    }

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Error" + e.toString(),
                            Toast.LENGTH_SHORT).show();
                }

            }
            loadDataToSearch();
            listAdapter = new CityFilterAdapter(SearchCityActivity.this, continentList);
            //attach the adapter to the list
            myList.setAdapter(listAdapter);
            //myList.setOnChildClickListener(this);
            //expandAll();

        }
    }

    @Override
    public boolean onClose() {
        listAdapter.filterData("");
        expandAll();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        if (listAdapter != null) {
            listAdapter.filterData(query);
        }
        expandAll();
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (listAdapter != null) {
            listAdapter.filterData(query);
        }
        expandAll();
        return false;
    }
}