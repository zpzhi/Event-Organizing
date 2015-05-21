package com.example.pengzhizhou.meetup;

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.ActionBarActivity;
import android.widget.ImageView;
import android.widget.TextView;


public class TabHostActivity extends ActionBarActivity {

    private FragmentTabHost mTabHost;
    private String loginUser = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_afterlogin);

        SharedPreferences settings = getSharedPreferences("MyPrefsFile", 0);
        loginUser = settings.getString("KEY_LOGIN_USER", null);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.activity_afterlogin_actionbar);

        mTabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

        mTabHost.addTab(mTabHost.newTabSpec("活动").setIndicator("活动"),
                ListActivitiesFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("发布活动").setIndicator("发布活动"),
                PostActivityFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("我的主页").setIndicator("我的主页"),
                UserProfileFragment.class, null);
    }

    public void setActionBarTitle(String title) {
        TextView actionbarTitle = (TextView) findViewById(R.id.actionbarTitle);
        actionbarTitle.setText(title);
    }

    public void setImageViewable(int i){
        ImageView imgView = (ImageView)findViewById(R.id.editInfo);
        imgView.setVisibility(i);
    }

    public void setSearchCityViewable(int i){
        TextView searchCity = (TextView)findViewById(R.id.searchCity);
        searchCity.setVisibility(i);
    }


}
