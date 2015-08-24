package com.example.pengzhizhou.meetup;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;


public class TabHostActivity extends ActionBarActivity {

    private FragmentTabHost mTabHost;
    private boolean doubleBackToExitPressedOnce;
    private android.os.Handler mHandler = new android.os.Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_afterlogin);
        // in case return from userProfiledEditActivity, when cancel was clicked
        // return back to the profile fragments
        if (!ImageLoader.getInstance().isInited()) {
            initImageLoader(getApplicationContext());
        }

        int startTab = getIntent().getIntExtra("tab", 0);
        int displayWidth = getWindowManager().getDefaultDisplay().getWidth();

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.activity_afterlogin_actionbar);

        mTabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
        View tabView1 = getLayoutInflater().inflate(R.layout.custom_post_event_tab_view, mTabHost, false);
        View tabView2 = getLayoutInflater().inflate(R.layout.custom_list_event_tab_view, mTabHost, false);
        View tabView3 = getLayoutInflater().inflate(R.layout.custom_user_profile_tab_view, mTabHost, false);

        mTabHost.addTab(mTabHost.newTabSpec("活动列表").setIndicator(tabView2),
                ListActivitiesFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("发布活动").setIndicator(tabView1),
                PostActivityFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("我的主页").setIndicator(tabView3),
                UserProfileFragment.class, null);

        mTabHost.getTabWidget().getChildAt(0).getLayoutParams().width = displayWidth/3;
        mTabHost.getTabWidget().getChildAt(1).getLayoutParams().width = displayWidth/3;
        mTabHost.getTabWidget().getChildAt(2).getLayoutParams().width = displayWidth/3;

        //setTabIcon(mTabHost, 0, R.drawable.ic_action_device_home);
        //setTabIcon(mTabHost, 1, R.drawable.ic_action_content_add_circle);
        //setTabIcon(mTabHost, 2, R.drawable.ic_action_social_person_outline);

        //TabWidget tabWidget = (TabWidget) findViewById(android.R.id.tabs);
        initTabsAppearance(mTabHost);
        mTabHost.setCurrentTab(startTab);
    }

    public static void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        config.writeDebugLogs(); // Remove for release app

        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config.build());
    }

    public void setTabIcon(TabHost tabHost, int tabIndex, int iconResource) {
        ImageView tabImageView = (ImageView) tabHost.getTabWidget().getChildTabViewAt(tabIndex).findViewById(android.R.id.icon);
        tabImageView.setVisibility(View.VISIBLE);
        tabImageView.setImageResource(iconResource);
    }

    private void initTabsAppearance(FragmentTabHost tabhost) {
        // Change background
        for(int i=0; i < tabhost.getTabWidget().getChildCount(); i++) {
            tabhost.getTabWidget().getChildAt(i).getLayoutParams().height = 80;
        }
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
        ImageView searchCity = (ImageView)findViewById(R.id.searchCity);
        searchCity.setVisibility(i);
    }

    // double click to quit the app, and disable the back button in tab host
    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            doubleBackToExitPressedOnce = false;
        }
    };

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if (mHandler != null) { mHandler.removeCallbacks(mRunnable); }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "请再按一次退出键退出程序", Toast.LENGTH_SHORT).show();

        mHandler.postDelayed(mRunnable, 2000);
    }
}
