package com.example.pengzhizhou.meetup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;

public class PostActivityFragment extends Fragment {

    GridView gridView;
    ArrayList<Item> gridArray;
    ArrayList<Integer> resource;
    CustomGridViewAdapter customGridAdapter;
    private String loginUser = null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        SharedPreferences settings = this.getActivity().getSharedPreferences("MyPrefsFile", 0);
        loginUser = settings.getString("KEY_LOGIN_USER", null);
        View V = null;

        TextView title = (TextView)getActivity().findViewById(R.id.actionbarTitle);
        title.setOnClickListener(null);

        if (loginUser == null){
            Intent myIntent;
            myIntent = new Intent(getActivity(), LoginActivity.class);
            startActivity(myIntent);

        }else {
            V = inflater.inflate(R.layout.post_activity_view, container, false);


            ((TabHostActivity) getActivity())
                    .setActionBarTitle("发布活动");
            ((TabHostActivity) getActivity()).setImageViewable(View.GONE);
            ((TabHostActivity) getActivity()).setSearchCityViewable(View.GONE);
            //set grid view item
            gridArray = new ArrayList<Item>();
            resource = new ArrayList<Integer>();

            if (gridArray == null || gridArray.size() < 1){
                Utility.createGridArray(this.getResources(), gridArray, resource, 0);
            }

            gridView = (GridView) V.findViewById(R.id.gridView1);
            customGridAdapter = new CustomGridViewAdapter(getActivity(), R.layout.grid_single, gridArray, resource);
            gridView.setAdapter(customGridAdapter);

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    Intent myIntent;
                    myIntent = new Intent(getActivity(), PostActivityDetailActivity.class);
                    myIntent.putExtra("activityTypes", gridArray.get(position).getTitle());
                    myIntent.putExtra("activityTypesId", Integer.toString(position));
                    startActivity(myIntent);
                }
            });
        }
        return V;
    }
}