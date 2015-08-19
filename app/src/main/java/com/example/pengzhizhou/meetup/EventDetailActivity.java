package com.example.pengzhizhou.meetup;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class EventDetailActivity extends ActionBarActivity{

    private ArrayList<Item> gridArray;
    private String url = Utility.getServerUrl();

    private Button joinEventButton;

    private String titleText = null;
    private String imageNameText = null;
    private String eventTimeText = null;
    private String addressText = null;
    private String eventID = null;
    private String durationText = null;
    private String loginUser = null, loginUserId = null;
    private String type = null;
    private String detail = null;
    private String city = null;
    private String state = null;
    private String eventCreatorId = null;
    private ListView hostListView, userListView;
    private List<User> hostList;
    private List<User> usersList;
    private UserListAdapter uAdapter = null;
    private UserListAdapter uAdapter1 = null;
    private int m_flag = 0;
    private DisplayImageOptions options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.activity_event_detail_actionbar);
        actionBar.setDisplayHomeAsUpEnabled(true);

        SharedPreferences settings = getSharedPreferences("MyPrefsFile", 0);
        loginUser = settings.getString("KEY_LOGIN_USER", null);
        loginUserId = settings.getString("KEY_LOGIN_USER_ID", null);

        Intent iin= getIntent();
        Bundle b = iin.getExtras();
        usersList = new ArrayList<User>();

        TextView title = (TextView) findViewById(R.id.eventTitle);
        TextView eventTime = (TextView) findViewById(R.id.eventTime);
        joinEventButton = (Button) findViewById(R.id.joinActivity);
        ImageView eventImage = (ImageView) findViewById(R.id.eventImage);
        TextView description = (TextView) findViewById(R.id.eventDescription);
        TextView eventAddress = (TextView) findViewById(R.id.eventAddress);
        TextView duration = (TextView) findViewById(R.id.duration);

        if(b!=null)
        {
            titleText = (String) b.get("itemTitle");
            title.setText(titleText);

            imageNameText = (String) b.get("itemImage");
            eventTimeText = (String) b.get("eventTime");
            addressText = (String) b.get("itemAddress");
            eventID = (String) b.get("itemId");
            type = (String) b.get("itemType");
            detail = (String) b.get("itemDetail");
            city = (String) b.get("itemCity");
            state = (String) b.get("itemState");
            eventCreatorId = (String) b.get("eventCreator");
            durationText = (String) b.get("duration");

            if (detail != null){
                description.setText("详情：" + detail);
            }

            if (!imageNameText.isEmpty() && imageNameText != null && !imageNameText.equals("NULL") & !imageNameText.equals("null")) {
                options = new DisplayImageOptions.Builder()
                        .showImageOnLoading(R.drawable.ic_launcher)
                        .showImageForEmptyUri(R.drawable.default_user)
                        .showImageOnFail(R.drawable.ic_launcher)
                        .cacheInMemory(true)
                        .cacheOnDisk(true)
                        .considerExifParams(true)
                        .bitmapConfig(Bitmap.Config.RGB_565)
                        .build();
                String imageUrl = Utility.getServerUrl() + "imgupload/activity_image/" + imageNameText;
                ImageLoader.getInstance().displayImage(imageUrl, eventImage, options);
            }
            else{
                int iType = Integer.parseInt(type);
                if (iType == 0){
                    eventImage.setImageResource(R.drawable.festival_);
                }
                else if (iType == 1){
                    eventImage.setImageResource(R.drawable.board_);
                }
                else if (iType == 2){
                    eventImage.setImageResource(R.drawable.room_);
                }
                else if (iType == 3){
                    eventImage.setImageResource(R.drawable.creative_);
                }
                else if (iType == 4){
                    eventImage.setImageResource(R.drawable.seminar_);
                }
                else if (iType == 5){
                    eventImage.setImageResource(R.drawable.movie_);
                }
                else if (iType == 6){
                    eventImage.setImageResource(R.drawable.sports_);
                }
                else if (iType == 7){
                    eventImage.setImageResource(R.drawable.travel_);
                }
                else{
                    eventImage.setImageResource(R.drawable.others_);
                }

            }

            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm");
            try {
                Date date = formatter.parse(eventTimeText.substring(0, eventTimeText.length()-3));
                eventTime.setText(date.toString());
            }
            catch(Exception e){
            }

            eventAddress.setText(state+city+addressText);
            duration.setText(durationText+"小时");

            initHostListAdapter();
            new GetEventCreatorInfo().execute();
            hostListView = (ListView) findViewById(R.id.list);
            hostListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    Intent i = null;
                    i = new Intent(EventDetailActivity.this, OtherUserProfileActivity.class);
                    i.putExtra("userImg", hostList.get(position).getImageName());
                    i.putExtra("userName", hostList.get(position).getName());
                    i.putExtra("userId", hostList.get(position).getId());
                    startActivity(i);
                }
            });
            initUserListAdapter();

            userListView = (ListView) findViewById(R.id.list1);
            userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    Intent i = null;
                    i = new Intent(EventDetailActivity.this, OtherUserProfileActivity.class);
                    i.putExtra("userImg", usersList.get(position).getImageName());
                    i.putExtra("userName", usersList.get(position).getName());
                    i.putExtra("userId", usersList.get(position).getId());
                    startActivity(i);
                }
            });
        }

        joinEventButton.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);
        joinEventButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if (loginUser != null){
                    final EditText txtPurpose = new EditText(EventDetailActivity.this);
                    txtPurpose.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                    txtPurpose.setLines(3);
                    txtPurpose.setFilters(new InputFilter[] { new InputFilter.LengthFilter(100) });
                    txtPurpose.setHint("请简单输入个人信息或者参加目的（选填，不多于100字）");

                    new AlertDialog.Builder(EventDetailActivity.this)
                            .setTitle("参加目的")
                            .setView(txtPurpose)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    String userActivityDescripton = txtPurpose.getText().toString();
                                    joinEventCall(loginUserId, eventID, userActivityDescripton);
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            })
                            .show();
                }
                else{
                    Intent myIntent;
                    myIntent = new Intent(EventDetailActivity.this, LoginActivity.class);
                    myIntent.putExtra("itemTitle",titleText);
                    myIntent.putExtra("itemImage", imageNameText);
                    myIntent.putExtra("eventTime", eventTimeText);
                    myIntent.putExtra("itemAddress", addressText);
                    myIntent.putExtra("itemId", eventID);
                    myIntent.putExtra("originActivity", "EventDetailActivity");
                    startActivity(myIntent);
                }

            }
        });
    }

    public class GetEventCreatorInfo extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... params) {
            String response = null;
            try {
                URL url1 = new URL(url+"get-user-detail-by-id.php?eventCreatorId="+eventCreatorId);
                response = Utility.createConnectionAndGetResponse(url1);
            }
            catch (IOException e){
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(final String response) {

            hostListView.setAdapter(uAdapter);
            if (response == null){
                return;
            }else if(response.equals("[]")){
                return;
            }else {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray jsonMainNode = jsonResponse.optJSONArray("user_info");

                    for (int i = 0; i < jsonMainNode.length(); i++) {

                        JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                        String id = jsonChildNode.optString("id_user");
                        String name = jsonChildNode.optString("username");
                        String image = jsonChildNode.optString("image_thumb");
                        User user = new User();
                        if (name.equals(loginUser)) {
                            m_flag = 1;
                        }

                        String userDescription = jsonChildNode.optString("user_description");

                        user.setId(id);
                        user.setName(name);
                        user.setImageName(image);
                        user.setDescription(userDescription);

                        hostList.add(user);
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Error" + e.toString(),
                            Toast.LENGTH_SHORT).show();
                }

            }

            new GetUserImageNames().execute();

        }

    }

    public class GetUserImageNames extends AsyncTask<String,Void,String>{
          @Override
        protected String doInBackground(String... params) {
              String response  = null;
              try{
                  URL url1 = new URL(url+"get-user-images-names.php?id="+eventID);
                  response = Utility.createConnectionAndGetResponse(url1);
              }
              catch (IOException e){
                  e.printStackTrace();
              }
              return response;
        }

        @Override
        protected void onPostExecute(final String response) {
            userListView.setAdapter(uAdapter1);
            if (response == null){
                if (m_flag == 0) {
                    joinEventButton.setVisibility(View.VISIBLE);
                }
                return;
            }else if(response.equals("[]")){
                if (m_flag == 0) {
                    joinEventButton.setVisibility(View.VISIBLE);
                }
                return;
            }else {

                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray jsonMainNode = jsonResponse.optJSONArray("user_info");

                    for (int i = 0; i < jsonMainNode.length(); i++) {
                        JSONArray innerArray = jsonMainNode.optJSONArray(i);

                        for (int j = 0; j < innerArray.length(); j++) {
                            JSONObject jsonChildNode = innerArray.getJSONObject(j);
                            User user = new User();
                            String id = jsonChildNode.optString("id_user");
                            String name = jsonChildNode.optString("username");
                            String image = jsonChildNode.optString("image_thumb");
                            String uaDescription = jsonChildNode.optString("uaDescription");

                            if (name.equals(loginUser)) {
                                m_flag = 1;
                            }

                            String userDescription = jsonChildNode.optString("user_description");

                            user.setId(id);
                            user.setName(name);
                            user.setImageName(image);
                            user.setDescription(userDescription);
                            user.setUaDescription(uaDescription);

                            usersList.add(user);
                        }

                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Error" + e.toString(),
                            Toast.LENGTH_SHORT).show();
                }

            }

            if (m_flag == 0){
                joinEventButton.setVisibility(View.VISIBLE);
            }else{
                joinEventButton.setVisibility(View.GONE);
            }

            Utility.setListViewHeightBasedOnChildren(userListView);
        }

    }

    public boolean joinEventCall(String userId, String eventId, String uaDescription){
        new JoinEventTask().execute(userId, eventId, uaDescription);
        return true;
    }

    public class JoinEventTask extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... params) {
            String result = postData(params[0], params[1], params[2]);
            return result;
        }

        public String postData(String userId, String eventID, String uaDescription) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url + "join-event.php");

            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("userId", userId));
                nameValuePairs.add(new BasicNameValuePair("eventID", eventID));
                nameValuePairs.add(new BasicNameValuePair("uaDescription", uaDescription));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                BufferedReader in = new BufferedReader
                        (new InputStreamReader(response.getEntity().getContent(), "UTF-8"));

                StringBuffer sb = new StringBuffer("");
                String line = "";
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                }
                in.close();

                return sb.toString();

            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                return e.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                return e.toString();
            }
        }

        @Override
        protected void onPostExecute(final String result) {

            if (result!=null && result.equals("success")){

                Toast.makeText(getApplicationContext(),
                        "成功参加这个活动",
                        Toast.LENGTH_LONG).show();

                Intent i = getIntent();
                i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                i.putExtra("itemTitle", titleText);
                i.putExtra("itemImage", imageNameText);
                i.putExtra("eventTime", eventTimeText);
                i.putExtra("itemAddress", addressText);
                i.putExtra("itemId", eventID);
                finish();
                startActivity(i);

            }else{
                Toast.makeText(getApplicationContext(),
                        result,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public void initHostListAdapter(){
        hostList = new ArrayList<User>();
        uAdapter = new UserListAdapter(this, R.layout.list_users_row, hostList);
    }

    public void initUserListAdapter(){
        usersList = new ArrayList<User>();
        uAdapter1 = new UserListAdapter(this, R.layout.list_users_row, usersList);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }
}
