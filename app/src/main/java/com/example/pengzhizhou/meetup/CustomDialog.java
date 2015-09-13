package com.example.pengzhizhou.meetup;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pengzhizhou on 8/29/15.
 */
public class CustomDialog extends Dialog implements android.view.View.OnClickListener {

    public Activity activity;
    public Dialog d;
    public Button yes, no;
    private String eventID = null;
    private String loginUserID = null;
    private EditText txtPurpose;
    private String titleText, imageNameText, eventTimeText, addressText;
    private String url = Utility.getServerUrl();

    public CustomDialog(Activity a, String uid, String id, String tt, String it, String et, String at) {
        super(a);
        // TODO Auto-generated constructor stub
        this.activity = a;
        this.loginUserID = uid;
        this.eventID = id;
        this.titleText = tt;
        this.imageNameText = it;
        this.eventTimeText = et;
        this.addressText = at;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog);
        yes = (Button) findViewById(R.id.btnConfirm);
        no = (Button) findViewById(R.id.btnCancel);

        yes.setOnClickListener(this);
        no.setOnClickListener(this);

        txtPurpose = (EditText) findViewById(R.id.joinEventPurpose);
        txtPurpose.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        txtPurpose.setLines(3);
        txtPurpose.setFilters(new InputFilter[] { new InputFilter.LengthFilter(100) });
        txtPurpose.setHint("请简单输入个人信息或者参加目的（选填，不多于100字）");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnConfirm:
                joinEventCall(loginUserID, eventID, txtPurpose.getText().toString());
                break;
            case R.id.btnCancel:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
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

                Toast.makeText(activity.getApplicationContext(),
                        "成功参加这个活动",
                        Toast.LENGTH_LONG).show();

                Intent i = activity.getIntent();
                i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                i.putExtra("itemTitle", titleText);
                i.putExtra("itemImage", imageNameText);
                i.putExtra("eventTime", eventTimeText);
                i.putExtra("itemAddress", addressText);
                i.putExtra("itemId", eventID);
                activity.finish();
                activity.startActivity(i);

            }else{
                Toast.makeText(activity.getApplicationContext(),
                        result,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

}