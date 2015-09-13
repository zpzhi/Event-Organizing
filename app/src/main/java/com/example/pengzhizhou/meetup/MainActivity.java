package com.example.pengzhizhou.meetup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.viewpagerindicator.UnderlinePageIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends FragmentActivity {

    private String loginUser = null;
    private MyPageAdapter pageAdapter;
    private ViewPagerCustomDuration pager;
    private static final float MIN_SCALE = 0.85f;
    private static final float MIN_ALPHA = 0.5f;
    Timer timer;
    int page;
    int flag = 0;

    UnderlinePageIndicator mIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences settings = this.getSharedPreferences("MyPrefsFile", 0);
        loginUser = settings.getString("KEY_LOGIN_USER", null);
        super.onCreate(savedInstanceState);

        if (loginUser != null){
            Intent myIntent;
            myIntent = new Intent(this, TabHostActivity.class);
            startActivity(myIntent);

        }else {
            setContentView(R.layout.activity_main);

            List<Fragment> fragments = getFragments();
            pageAdapter = new MyPageAdapter(getSupportFragmentManager(), fragments);
            pager = (ViewPagerCustomDuration)findViewById(R.id.pager);
            pager.setAdapter(pageAdapter);
            pager.setScrollDurationFactor(3); // make the animation twice as slow
            //Bind the title indicator to the adapter
            mIndicator = (UnderlinePageIndicator)findViewById(R.id.indicator);
            mIndicator.setViewPager(pager);
            //mIndicator.setCurrentItem(0);
            //mIndicator.setBackgroundColor(Color.parseColor("#7396FF"));
            pager.setPageTransformer(false, new ViewPager.PageTransformer() {
                @Override
                public void transformPage(View view, float position) {
                    int pageWidth = view.getWidth();
                    int pageHeight = view.getHeight();

                    if (position < -1) { // [-Infinity,-1)
                        // This page is way off-screen to the left.
                        view.setAlpha(0);

                    } else if (position <= 1) { // [-1,1]
                        // Modify the default slide transition to shrink the page as well
                        float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                        float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                        float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                        if (position < 0) {
                            view.setTranslationX(horzMargin - vertMargin / 2);
                        } else {
                            view.setTranslationX(-horzMargin + vertMargin / 2);
                        }

                        // Scale the page down (between MIN_SCALE and 1)
                        view.setScaleX(scaleFactor);
                        view.setScaleY(scaleFactor);

                        // Fade the page relative to its size.
                        view.setAlpha(MIN_ALPHA +
                                (scaleFactor - MIN_SCALE) /
                                        (1 - MIN_SCALE) * (1 - MIN_ALPHA));

                    } else { // (1,+Infinity]
                        // This page is way off-screen to the right.
                        view.setAlpha(0);
                    }
                }
            });

            pageSwitcher(3);
        }
    }

    public void onClick1(View v) {
        if (timer!=null) timer.cancel();
        Intent myIntent;
        myIntent = new Intent(MainActivity.this, RegistrationActivity.class);
        myIntent.putExtra("fromPage", 0);
        startActivity(myIntent);
    }


    public void onClick(View v) {
        if (timer!=null) timer.cancel();
        Intent myIntent;
        myIntent = new Intent(this, LoginActivity.class);
        myIntent.putExtra("fromPage", 1);
        startActivity(myIntent);
    }

    public void listActivity(View v){
        if (timer!=null) timer.cancel();
        Intent intent = new Intent(this, TabHostActivity.class);
        startActivity(intent);
    }

    private List<Fragment> getFragments(){
        List<Fragment> fList = new ArrayList<Fragment>();

        fList.add(ScreenBackgroundFragment.newInstance(Color.parseColor("#7396FF"), R.drawable.index1));
        fList.add(ScreenBackgroundFragment.newInstance(Color.parseColor("#27A3EB"), R.drawable.index));
        fList.add(ScreenBackgroundFragment.newInstance(Color.parseColor("#6CD6D9"), R.drawable.index2));

        return fList;
    }

    private class MyPageAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragments;

        public MyPageAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }
        @Override
        public Fragment getItem(int position) {
            return this.fragments.get(position);
        }

        @Override
        public int getCount() {
            return this.fragments.size();
        }
    }

    public void pageSwitcher(int seconds) {
        timer = new Timer(); // At this line a new Thread will be created
        page = pager.getCurrentItem();
        timer.scheduleAtFixedRate(new RemindTask(), 0, seconds * 1000); // delay
    }

    class RemindTask extends TimerTask {

        @Override
        public void run() {

            // As the TimerTask run on a seprate thread from UI thread we have
            // to call runOnUiThread to do work on UI thread.
            runOnUiThread(new Runnable() {
                public void run() {

                    //pager.setCurrentItem( (pager.getCurrentItem() + 1) % (pager.getChildCount() + 1) );
                    if (flag == 0 && page < 3){
                        pager.setCurrentItem(page);
                        page++;
                        if (page == 3)
                        {
                            flag = 1;
                        }
                    }

                    if (flag == 1 && page >= 0 ) {
                        pager.setCurrentItem(page);
                        page--;
                        if (page == -1){
                            flag = 0;
                        }
                    }


                    /*if (pager.getCurrentItem() == 0) {
                        mIndicator.setBackgroundColor(Color.parseColor("#7396FF"));
                    }
                    else if(pager.getCurrentItem() == 1){
                        mIndicator.setBackgroundColor(Color.parseColor("#27A3EB"));
                    }
                    else if(pager.getCurrentItem() == 2) {
                        mIndicator.setBackgroundColor(Color.parseColor("#6CD6D9"));
                    }*/
                }
            });

        }
    }
}
