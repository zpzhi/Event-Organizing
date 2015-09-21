package com.example.pengzhizhou.meetup;

/**
 * The page for showing events list
 * Created by pengzhizhou on 9/2/15.
 */

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


public class ViewEventsListActivity extends ActionBarActivity {

    private ListAdapterS adapter = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_events_list);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.activity_event_detail_actionbar);
        actionBar.setDisplayHomeAsUpEnabled(true);

        TextView actionbarTitle = (TextView)findViewById(R.id.actionBarTitle);

        EventsWrapper dw = (EventsWrapper) getIntent().getSerializableExtra("EventsList");
        final ArrayList<ActivityItem> list = dw.getItemList();
        int origin = getIntent().getIntExtra("Origin", 0);
        if (origin == 0){
            actionbarTitle.setText("发布的活动");
        }
        else if (origin == 1){
            actionbarTitle.setText("参加的活动");
        }
        Utility.setActionBarTitleByMargin(actionbarTitle, this, 0, 4);

        ListView eventList =    (ListView) findViewById(R.id.list);
        adapter = new ListAdapterS(this, R.layout.list_event_row, list);
        eventList.setAdapter(adapter);
        eventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Intent i = new Intent(ViewEventsListActivity.this, EventDetailActivity.class);
                i.putExtra("itemTitle", list.get(position).getTitle());
                i.putExtra("itemImage", list.get(position).getActivityImage());
                i.putExtra("eventTime", list.get(position).getActivityTime());
                i.putExtra("itemAddress", list.get(position).getAddress());
                i.putExtra("itemId", list.get(position).getId());
                i.putExtra("itemType", list.get(position).getActivityType());
                i.putExtra("itemDetail", list.get(position).getDetail());
                i.putExtra("itemCity", list.get(position).getCity());
                i.putExtra("itemState", list.get(position).getState());
                i.putExtra("eventCreator", list.get(position).getEventCreator());
                i.putExtra("duration", list.get(position).getDuration());
                startActivity(i);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_host_events, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
