package com.example.pengzhizhou.meetup;
/**
 * Implement the comment system in activities
 * Created by pengzhizhou on Sep/17/15.
 */
import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


public class CommentActivity extends ActionBarActivity {
    private String loginUser = null, loginUserId = null;
    private User user;
    private ImageView userImg;
    private String url = Utility.getServerUrl();
    private EditText comment;
    private TextView postCommentButton;
    private String eventId = null;
    private ProgressBar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.activity_event_detail_actionbar);
        actionBar.setDisplayHomeAsUpEnabled(true);
        TextView actionBarTitle = (TextView) findViewById(R.id.actionBarTitle);
        Utility.setActionBarTitleByMargin(actionBarTitle, this, 0, 3);
        actionBarTitle.setText("评论");

        SharedPreferences settings = getSharedPreferences("MyPrefsFile", 0);
        loginUser = settings.getString("KEY_LOGIN_USER", null);
        loginUserId = settings.getString("KEY_LOGIN_USER_ID", null);

        bar = (ProgressBar) this.findViewById(R.id.progressBar);

        Intent iin= getIntent();
        Bundle b = iin.getExtras();
        if (b!=null){
            eventId = (String) b.getString("itemId");
        }

        userImg = (ImageView) findViewById(R.id.commentUserIcon);
        comment = (EditText) findViewById(R.id.commentInput);
        postCommentButton = (TextView) findViewById(R.id.postCommentButton);
        comment.addTextChangedListener(textWatcher);

        postCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PostCommentTask().execute(loginUserId, eventId, comment.getText().toString());
                comment.setText("");
            }
        });

        new GetUserDetailTask().execute(loginUser);

    }

    public class GetUserDetailTask extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... params) {
            String response  = null;
            try{
                URL url1 = new URL(url+"get-user-detail.php?userName="+params[0]);
                response = Utility.createConnectionAndGetResponse(url1);
            }
            catch (IOException e){
                e.printStackTrace();
            }
            return response;
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
                    JSONArray jsonMainNode = jsonResponse.optJSONArray("user_info");
                    user = new User();
                    for (int i = 0; i < jsonMainNode.length(); i++) {
                        JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                        String id = jsonChildNode.optString("id_user");
                        String name = jsonChildNode.optString("username");
                        String image = jsonChildNode.optString("image_name");
                        String realName = jsonChildNode.optString("name");
                        String phone = jsonChildNode.optString("phone_number");
                        String userDescription = jsonChildNode.optString("user_description");
                        user.setImageName(image);
                        user.setName(name);
                        user.setRealName(realName);
                        user.setId(id);
                        user.setPhoneNumber(phone);
                        user.setDescription(userDescription);

                        String imageUrl;
                        //int flag = 1;
                        if (!image.isEmpty() && image != null && !image.equals("NULL") && !image.equals("null")) {
                            imageUrl = Utility.getServerUrl() + "imgupload/user_image/" + image;
                            ImageLoader.getInstance().displayImage(imageUrl, userImg, Utility.options_round_40);
                        }
                        else{
                            ImageLoader.getInstance().displayImage("", userImg, Utility.options_round_40);
                        }
                    }

                } catch (JSONException e) {
                     Toast.makeText(getApplicationContext(), "Error" + e.toString(),
                                Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    public class PostCommentTask extends AsyncTask<String,Void,String> {

        @Override
        protected void onPreExecute(){
            bar.setVisibility(View.VISIBLE);
        }
        @Override
        protected String doInBackground(String... params) {
            StringBuilder response  = new StringBuilder();
            try{
                String url = "post-comment.php";
                URL url1 = new URL(Utility.getServerUrl()+url);
                //URL url1 = new URL(url+"post-comment.php?userId="+params[0]+"&eventId="+params[1]+"&comment="+params[2]);
                HttpURLConnection httpconn = Utility.createConnection(url1);

                List<NameValuePair> params1 = new ArrayList<NameValuePair>();
                params1.add(new BasicNameValuePair("userId", params[0]));
                params1.add(new BasicNameValuePair("eventId", params[1]));
                params1.add(new BasicNameValuePair("comment", params[2]));

                OutputStream os = httpconn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getQuery(params1));
                writer.flush();
                writer.close();
                os.close();

                if (httpconn.getResponseCode() == HttpURLConnection.HTTP_OK)
                {
                    // if response code = 200 ok
                    BufferedReader input = new BufferedReader(new InputStreamReader(httpconn.getInputStream()));
                    String strLine = null;
                    while ((strLine = input.readLine()) != null)
                    {
                        response.append(strLine);
                    }
                    input.close();
                }
            }
            catch (IOException e){
                e.printStackTrace();
            }
            return response.toString();
        }

        @Override
        protected void onPostExecute(final String response) {
            bar.setVisibility(View.GONE);
            if (response.equals("success")){
                Toast.makeText(getApplicationContext(), "评论已发布",
                        Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(getApplicationContext(), response,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    //TextWatcher
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3)
        {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            checkFieldsForEmptyValues();
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

    private void checkFieldsForEmptyValues(){

        String s1 = comment.getText().toString().trim();
        if (s1.length() > 0 ) {
            postCommentButton.setEnabled(true);
            postCommentButton.setClickable(true);
            postCommentButton.setTextColor(Color.parseColor("#04AAF8"));

        } else {
            postCommentButton.setEnabled(false);
            postCommentButton.setClickable(false);
            postCommentButton.setTextColor(Color.parseColor("#989996"));
        }
    }

    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
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
