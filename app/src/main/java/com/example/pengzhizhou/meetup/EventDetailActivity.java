package com.example.pengzhizhou.meetup;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.ImageOptions;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.text.*;
import java.util.Date;


public class EventDetailActivity extends ActionBarActivity{

    private AQuery aq;
    List<User> usersList;
    GridView gridView;
    ArrayList<Item> gridArray;
    CustomGridViewAdapter customGridAdapter;
    ImageViewRounded ir;
    String url = Utility.getServerUrl();
    GetUserImageNames getUserImageNamesTask;

    private Button joinEventButton;

    private String titleText = null;
    private String imageNameText = null;
    private String eventTimeText = null;
    private String addressText = null;
    private String eventID = null;
    private String loginUser = null;
    private String type = null;
    private String detail = null;
    private String city = null;
    private String state = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.activity_event_detail_actionbar);

        SharedPreferences settings = getSharedPreferences("MyPrefsFile", 0);
        loginUser = settings.getString("KEY_LOGIN_USER", null);

        Intent iin= getIntent();
        Bundle b = iin.getExtras();
        aq = new AQuery(this);
        usersList = new ArrayList<User>();
        ir = new ImageViewRounded(this);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        TextView title = (TextView) findViewById(R.id.eventTitle);
        //ImageView activityImage = (ImageView) findViewById(R.id.eventImage);
        TextView eventTime = (TextView) findViewById(R.id.eventTime);
        //TextView address = (TextView) findViewById(R.id)
        joinEventButton = (Button) findViewById(R.id.joinActivity);
        ImageView eventImage = (ImageView) findViewById(R.id.eventImage);
        TextView description = (TextView) findViewById(R.id.eventDescription);
        TextView eventAddress = (TextView) findViewById(R.id.eventAddress);

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

            if (detail != null){
                description.setText(detail);
            }

            if (imageNameText != null && !imageNameText.isEmpty() && !imageNameText.equals("null")){
                String imageUrl = url + "/signin/imgupload/" + imageNameText;
                ImageOptions options = new ImageOptions();
                aq.id(R.id.eventImage).image(imageUrl, options);
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

            getUserImageNamesTask = new GetUserImageNames();
            getUserImageNamesTask.execute(eventID);
        }

        joinEventButton.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);
        joinEventButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if (loginUser != null){
                    joinEventCall(loginUser, eventID);
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

    public class GetUserImageNames extends AsyncTask<String,Void,String>{
          @Override
        protected String doInBackground(String... params) {

              HttpClient httpclient = new DefaultHttpClient();
              HttpPost httppost = new HttpPost(url + "/signin/getUserImagesNames.php");

              try {
                  // Add your data
                  List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                  nameValuePairs.add(new BasicNameValuePair("id", eventID));
                  httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                  // Execute HTTP Post Request
                  HttpResponse response = httpclient.execute(httppost);
                  BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                  String json = reader.readLine();

                  return json;

              } catch (ClientProtocolException e) {
                  // TODO Auto-generated catch block
                  return e.toString();
              } catch (IOException e) {
                  // TODO Auto-generated catch block
                  return e.toString();
              }
        }

        @Override
        protected void onPostExecute(final String response) {
            int flag = 0;
            if (response == null){
                joinEventButton.setVisibility(View.VISIBLE);
                return;
            }else if(response.equals("[]")){
                joinEventButton.setVisibility(View.VISIBLE);
                return;
            }else {

                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray jsonMainNode = jsonResponse.optJSONArray("user_info");

                    for (int i = 0; i < jsonMainNode.length(); i++) {
                        JSONArray innerArray = jsonMainNode.optJSONArray(i);

                        for (int j = 0; j < innerArray.length(); j++) {
                            JSONObject jsonChildNode = innerArray.getJSONObject(j);
                            User item = new User();
                            String id = jsonChildNode.optString("id_user");
                            String name = jsonChildNode.optString("username");
                            String image = jsonChildNode.optString("image_name");

                            if (name.equals(loginUser)) {
                                joinEventButton.setVisibility(View.GONE);
                                flag = 1;
                            }
                            item.setName(name);
                            item.setImageName(image);

                            usersList.add(item);
                        }

                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Error" + e.toString(),
                            Toast.LENGTH_SHORT).show();
                }


                int size = usersList.size();
                if (size > 0) {
                    gridArray = new ArrayList<Item>();

                    for (int i = 0; i < size; i++) {

                        String imageUrl = url + "/signin/imgupload/" + usersList.get(i).getImageName();
                        Bitmap bt = Utility.getBitmapFromURL(imageUrl); //aq.getCachedImage(imageUrl);
                        //Bitmap bt = BitmapFactory.decodeFile(imageUrl);
                        if (bt == null) {
                            bt = BitmapFactory.decodeResource(EventDetailActivity.this.getResources(),
                                    R.drawable.default_user);
                        }

                        bt = ir.getCircledBitmap(bt);
                        String name = usersList.get(i).getName();
                        gridArray.add(new Item(bt, name));

                    }
                    gridView = (GridView) findViewById(R.id.gridView2);
                    customGridAdapter = new CustomGridViewAdapter(EventDetailActivity.this, R.layout.grid_user, gridArray);
                    gridView.setAdapter(customGridAdapter);

                    gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view,
                                                int position, long id) {
                            Intent i = null;
                            i = new Intent(EventDetailActivity.this, OtherUserProfileActivity.class);
                            i.putExtra("userImg", usersList.get(position).getImageName());
                            i.putExtra("userName", usersList.get(position).getName());
                            startActivity(i);

                        }
                    });

                }
            }

            if (flag == 0){
                joinEventButton.setVisibility(View.VISIBLE);
            }
        }

    }

    public boolean joinEventCall(String userName, String eventId){
        new MyAsyncTask().execute(userName, eventId);
        return true;
    }

    public class MyAsyncTask extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... params) {
            String result = postData(params[0], params[1]);
            return result;
        }

        public String postData(String userName, String eventID) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url + "/signin/join-event-android.php");

            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("userName", userName));
                nameValuePairs.add(new BasicNameValuePair("eventID", eventID));
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event_detail, menu);
        return true;
    }

}
