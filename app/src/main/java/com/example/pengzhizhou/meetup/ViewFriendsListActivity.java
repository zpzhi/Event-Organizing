package com.example.pengzhizhou.meetup;

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


public class ViewFriendsListActivity extends ActionBarActivity {

    private UserListAdapter uAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_friends_list);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.activity_event_detail_actionbar);
        actionBar.setDisplayHomeAsUpEnabled(true);

        TextView actionbarTitle = (TextView)findViewById(R.id.actionBarTitle);

        UsersWrapper uw = (UsersWrapper) getIntent().getSerializableExtra("FriendsList");
        final ArrayList<User> list = uw.getUserList();

        actionbarTitle.setText("关注好友");
        Utility.setActionBarTitleByLeftMargin(actionbarTitle, this, 0);

        ListView friendsList =  (ListView) findViewById(R.id.list);
        uAdapter = new UserListAdapter(this, R.layout.list_users_row, list);
        friendsList.setAdapter(uAdapter);
        friendsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent i = new Intent(ViewFriendsListActivity.this, OtherUserProfileActivity.class);
                i.putExtra("userImg", list.get(position).getImageName());
                i.putExtra("userName", list.get(position).getName());
                i.putExtra("userId", list.get(position).getId());
                startActivity(i);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_friends_list, menu);
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
